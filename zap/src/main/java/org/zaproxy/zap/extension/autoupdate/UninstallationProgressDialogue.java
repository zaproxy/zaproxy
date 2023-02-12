/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.autoupdate;

import java.awt.EventQueue;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnUninstallationProgressCallback;

/**
 * Progress dialogue for uninstallation of add-ons.
 *
 * <p>It shows a progress bar, the current add-on being uninstalled and the current and number of
 * components (e.g. extensions, files...) uninstalled.
 *
 * @since 2.4.0
 */
@SuppressWarnings("serial")
class UninstallationProgressDialogue extends AbstractDialog {

    private static final long serialVersionUID = 6544278337930125848L;

    private static final int MS_TO_WAIT_BEFORE_SHOW = 500;

    private static final int EXTENSION_UNINSTALL_WEIGHT = 10;

    private static final int MINIMUM_TO_IMMEDIATELY_SHOW_DIALOGUE = 50;

    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JLabel customLabel;

    private UninstallationProgressEvent.Type currentType;
    private String keyBaseStatusMessage;

    private AddOn currentAddOn;
    private boolean update;
    private boolean failedUninstallations;

    private long startTime;
    private boolean done;
    private boolean setVisibleInvoked;

    private List<AddOnUninstallListener> listeners;

    private boolean synchronous;

    public UninstallationProgressDialogue(Window parent, Set<AddOn> addOns) {
        super(parent, true);

        keyBaseStatusMessage = "";
        listeners = Collections.emptyList();

        setTitle(Constant.messages.getString("cfu.uninstallation.progress.dialogue.title"));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        int max = 0;
        for (AddOn addOn : addOns) {
            max += addOn.getFiles().size();
            max += addOn.getAscanrules().size();
            max += addOn.getPscanrules().size();
            max += addOn.getLoadedExtensions().size() * EXTENSION_UNINSTALL_WEIGHT;
        }

        getProgressBar().setValue(0);
        getProgressBar().setMaximum(max);

        getStatusLabel().setText(" ");
        getCustomLabel().setText(" ");

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHonorsVisibility(false);

        JLabel messageLabel =
                new JLabel(
                        Constant.messages.getString(
                                "cfu.uninstallation.progress.dialogue.uninstalling"));

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(messageLabel)
                        .addComponent(getStatusLabel())
                        .addComponent(
                                getProgressBar(),
                                200,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.DEFAULT_SIZE)
                        .addComponent(getCustomLabel()));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(messageLabel)
                        .addComponent(getStatusLabel())
                        .addComponent(getProgressBar())
                        .addComponent(getCustomLabel()));

        setContentPane(panel);
        pack();
    }

    private JLabel getStatusLabel() {
        if (statusLabel == null) {
            statusLabel = new JLabel();
        }
        return statusLabel;
    }

    private JProgressBar getProgressBar() {
        if (progressBar == null) {
            progressBar = new JProgressBar();
        }
        return progressBar;
    }

    private JLabel getCustomLabel() {
        if (customLabel == null) {
            customLabel = new JLabel();
        }
        return customLabel;
    }

    private void incrementProgress(int amount) {
        int currentValue = getProgressBar().getValue() + amount;
        getProgressBar().setValue(currentValue);
    }

    private void setCurrentAddOn(AddOn addOn) {
        currentAddOn = addOn;
        getStatusLabel()
                .setText(
                        Constant.messages.getString(
                                "cfu.uninstallation.progress.dialogue.currentAddOn",
                                addOn.getName(),
                                addOn.getVersion()));
    }

    private void setCustomMessage(String message) {
        getCustomLabel().setText(message);
    }

    public void addAddOnUninstallListener(AddOnUninstallListener listener) {
        if (listeners.isEmpty()) {
            listeners = new ArrayList<>(1);
        }
        listeners.add(listener);
    }

    /**
     * Binds the given uninstallation worker to this dialogue.
     *
     * <p>The dialogue is disposed once the worker finishes the uninstallation.
     *
     * @param worker the uninstallation worker
     */
    public void bind(SwingWorker<?, UninstallationProgressEvent> worker) {
        worker.addPropertyChangeListener(new WaitForDoneWorkerCloseListener());
    }

    /**
     * Sets whether or not the dialogue should be shown synchronously.
     *
     * <p>If the dialogue is not shown synchronously the dialogue is only shown immediately if the
     * calculated uninstallations will take some time otherwise it will not be shown unless it
     * passed a given time. It might happen that the dialogue is not shown at all if the
     * uninstallation finishes in a given threshold.
     *
     * @param synchronous {@code true} if the dialogue should be shown synchronously, {@code false}
     *     otherwise
     */
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }

    @Override
    public void setVisible(boolean show) {
        if (show && !synchronous) {
            startTime = System.currentTimeMillis();
            done = false;
            if (MINIMUM_TO_IMMEDIATELY_SHOW_DIALOGUE > getProgressBar().getMaximum()) {
                return;
            }
        }
        super.setVisible(show);
    }

    /**
     * Updates the progress with the given events.
     *
     * @param events the events generated during uninstallation
     */
    public void update(List<UninstallationProgressEvent> events) {
        if (!isVisible()) {
            if ((System.currentTimeMillis() - startTime) >= MS_TO_WAIT_BEFORE_SHOW) {
                if (!done && !setVisibleInvoked) {
                    setVisibleInvoked = true;
                    EventQueue.invokeLater(
                            new Runnable() {

                                @Override
                                public void run() {
                                    if (!done) {
                                        UninstallationProgressDialogue.super.setVisible(true);
                                    }
                                }
                            });
                }
            }
        }

        int totalAmount = 0;
        AddOn addOn = null;

        for (UninstallationProgressEvent event : events) {
            totalAmount += event.getAmount();
            if (UninstallationProgressEvent.Type.FINISHED_ADD_ON == event.getType()) {
                for (AddOnUninstallListener listener : listeners) {
                    failedUninstallations = !event.isUninstalled();
                    listener.addOnUninstalled(currentAddOn, update, event.isUninstalled());
                }
            } else if (UninstallationProgressEvent.Type.ADD_ON == event.getType()) {
                addOn = event.getAddOn();
                currentAddOn = addOn;
                update = event.isUpdate();

                for (AddOnUninstallListener listener : listeners) {
                    listener.uninstallingAddOn(addOn, update);
                }
            }
        }

        UninstallationProgressEvent last = events.get(events.size() - 1);

        if (addOn != null) {
            setCurrentAddOn(addOn);
        }

        if (totalAmount != 0) {
            incrementProgress(totalAmount);
        }

        if (currentType != last.getType()) {
            String keyMessage;
            switch (last.getType()) {
                case FILE:
                    keyMessage = "cfu.uninstallation.progress.dialogue.uninstallingFile";
                    break;
                case ACTIVE_RULE:
                    keyMessage = "cfu.uninstallation.progress.dialogue.uninstallingActiveScanner";
                    break;
                case PASSIVE_RULE:
                    keyMessage = "cfu.uninstallation.progress.dialogue.uninstallingPassiveScanner";
                    break;
                case EXTENSION:
                    keyMessage = "cfu.uninstallation.progress.dialogue.uninstallingExtension";
                    break;
                default:
                    keyMessage = "";
                    break;
            }
            currentType = last.getType();
            keyBaseStatusMessage = keyMessage;
        }

        if (keyBaseStatusMessage.isEmpty()) {
            setCustomMessage("");
        } else {
            setCustomMessage(
                    Constant.messages.getString(
                            keyBaseStatusMessage, last.getValue(), last.getMax()));
        }
    }

    /** A progress uninstallation event. */
    static final class UninstallationProgressEvent {

        private enum Type {
            ADD_ON,
            FILE,
            ACTIVE_RULE,
            PASSIVE_RULE,
            EXTENSION,
            FINISHED_ADD_ON
        }

        private final AddOn addOn;
        private final boolean update;
        private final boolean uninstalled;
        private final Type type;
        private final int amount;
        private final int value;
        private final int max;

        public UninstallationProgressEvent(AddOn addOn, boolean update) {
            this.addOn = addOn;
            this.update = update;
            this.uninstalled = false;
            this.amount = 0;
            this.type = Type.ADD_ON;
            this.value = 0;
            this.max = 0;
        }

        public UninstallationProgressEvent(Type type, int value, int max) {
            addOn = null;
            update = false;
            uninstalled = false;
            this.type = type;
            amount = (type == Type.EXTENSION) ? EXTENSION_UNINSTALL_WEIGHT : 1;
            this.value = value;
            this.max = max;
        }

        public UninstallationProgressEvent(boolean uninstalled) {
            this.uninstalled = uninstalled;
            this.type = Type.FINISHED_ADD_ON;
            this.addOn = null;
            this.update = false;
            this.amount = 0;
            this.value = 0;
            this.max = 0;
        }

        public AddOn getAddOn() {
            return addOn;
        }

        public boolean isUpdate() {
            return update;
        }

        public boolean isUninstalled() {
            return uninstalled;
        }

        public int getAmount() {
            return amount;
        }

        private Type getType() {
            return type;
        }

        public int getValue() {
            return value;
        }

        public int getMax() {
            return max;
        }
    }

    public abstract static class UninstallationProgressHandler
            implements AddOnUninstallationProgressCallback {

        protected abstract void publishEvent(UninstallationProgressEvent event);

        private UninstallationProgressEvent.Type type = null;
        private int currentValue = 0;
        private int currentMax = 0;

        @Override
        public void uninstallingAddOn(AddOn addOn, boolean update) {
            publishEvent(new UninstallationProgressEvent(addOn, update));
        }

        @Override
        public void activeScanRulesWillBeRemoved(int numberOfRules) {
            resetTypeAndValues(UninstallationProgressEvent.Type.ACTIVE_RULE, numberOfRules);
        }

        private void resetTypeAndValues(UninstallationProgressEvent.Type type, int max) {
            this.type = type;
            currentValue = 0;
            currentMax = max;
            publishEvent(new UninstallationProgressEvent(type, currentValue, currentMax));
        }

        @Override
        public void activeScanRuleRemoved(String name) {
            publish();
        }

        private void publish() {
            currentValue++;
            publishEvent(new UninstallationProgressEvent(type, currentValue, currentMax));
        }

        @Override
        public void passiveScanRulesWillBeRemoved(int numberOfRules) {
            resetTypeAndValues(UninstallationProgressEvent.Type.PASSIVE_RULE, numberOfRules);
        }

        @Override
        public void passiveScanRuleRemoved(String name) {
            publish();
        }

        @Override
        public void filesWillBeRemoved(int numberOfFiles) {
            resetTypeAndValues(UninstallationProgressEvent.Type.FILE, numberOfFiles);
        }

        @Override
        public void fileRemoved() {
            publish();
        }

        @Override
        public void extensionsWillBeRemoved(int numberOfExtensions) {
            resetTypeAndValues(UninstallationProgressEvent.Type.EXTENSION, numberOfExtensions);
        }

        @Override
        public void extensionRemoved(String name) {
            publish();
        }

        @Override
        public void addOnUninstalled(boolean uninstalled) {
            publishEvent(new UninstallationProgressEvent(uninstalled));
        }
    }

    public static interface AddOnUninstallListener {

        void uninstallingAddOn(AddOn addOn, boolean updating);

        void addOnUninstalled(AddOn addOn, boolean update, boolean uninstalled);
    }

    private class WaitForDoneWorkerCloseListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if ("state".equals(event.getPropertyName())
                    && SwingWorker.StateValue.DONE == event.getNewValue()) {
                setVisible(false);
                dispose();
                done = true;

                if (failedUninstallations) {
                    View.getSingleton()
                            .showWarningDialog(
                                    getOwner(),
                                    Constant.messages.getString("cfu.uninstall.failed"));
                }
            }
        }
    }
}
