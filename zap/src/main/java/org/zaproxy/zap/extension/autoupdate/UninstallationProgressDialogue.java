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
import java.util.HashSet;
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
import org.zaproxy.zap.extension.autoupdate.AddOnDependencyChecker.AddOnChangesResult;

/**
 * Progress dialogue for add-on operations (uninstall, download, install).
 *
 * <p>It shows a progress bar, the current add-on being processed and the current step (e.g.
 * extensions, files...) being processed.
 *
 * @since 2.4.0
 */
@SuppressWarnings("serial")
class UninstallationProgressDialogue extends AbstractDialog {

    private static final long serialVersionUID = 6544278337930125848L;

    private static final int MS_TO_WAIT_BEFORE_SHOW = 500;

    private static final int EXTENSION_UNINSTALL_WEIGHT = 10;

    private static final int DOWNLOAD_WEIGHT = 100;

    private static final int INSTALL_WEIGHT = 50;

    private static final int MINIMUM_TO_IMMEDIATELY_SHOW_DIALOGUE = 50;

    private JLabel messageLabel;
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

    private final int totalAddOnCount;
    private int completedAddOnCount;

    public UninstallationProgressDialogue(Window parent, AddOnOperationPlan plan) {
        super(parent, true);

        keyBaseStatusMessage = "";
        listeners = Collections.emptyList();
        totalAddOnCount = plan.getTotalAddOnCount();
        completedAddOnCount = 0;

        setTitle(getTitleKey(plan.getKind()));
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        int max = calculateMaxProgress(plan);

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

        messageLabel = new JLabel(getPhaseMessageKey(UninstallationProgressEvent.Phase.UNINSTALL));

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

    private static String getTitleKey(AddOnOperationPlan.Kind kind) {
        switch (kind) {
            case INSTALL:
                return Constant.messages.getString("cfu.operation.progress.title.install");
            case UPDATE:
                return Constant.messages.getString("cfu.operation.progress.title.update");
            default:
                return Constant.messages.getString("cfu.uninstallation.progress.dialogue.title");
        }
    }

    private static String getPhaseMessageKey(UninstallationProgressEvent.Phase phase) {
        switch (phase) {
            case DOWNLOAD:
                return Constant.messages.getString("cfu.operation.progress.dialogue.downloading");
            case INSTALL:
                return Constant.messages.getString("cfu.operation.progress.dialogue.installing");
            default:
                return Constant.messages.getString(
                        "cfu.uninstallation.progress.dialogue.uninstalling");
        }
    }

    private static int calculateMaxProgress(AddOnOperationPlan plan) {
        int max = 0;
        for (AddOn addOn : plan.getAllUninstallAddOns()) {
            max += uninstallWeight(addOn);
        }
        for (AddOn addOn : plan.getToDownload()) {
            max += DOWNLOAD_WEIGHT;
            max += INSTALL_WEIGHT;
        }
        return max;
    }

    private static int uninstallWeight(AddOn addOn) {
        return addOn.getFiles().size()
                + addOn.getAscanrules().size()
                + addOn.getPscanrules().size()
                + addOn.getLoadedExtensions().size() * EXTENSION_UNINSTALL_WEIGHT;
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
        updateStatusLabelWithCount(addOn);
    }

    private void updateStatusLabelWithCount(AddOn addOn) {
        if (totalAddOnCount > 1) {
            getStatusLabel()
                    .setText(
                            Constant.messages.getString(
                                    "cfu.operation.progress.dialogue.currentAddOnWithCount",
                                    completedAddOnCount + 1,
                                    totalAddOnCount,
                                    addOn.getName(),
                                    addOn.getVersion()));
        } else {
            getStatusLabel()
                    .setText(
                            Constant.messages.getString(
                                    "cfu.uninstallation.progress.dialogue.currentAddOn",
                                    addOn.getName(),
                                    addOn.getVersion()));
        }
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
     * Binds the given worker to this dialogue.
     *
     * <p>The dialogue is disposed once the worker finishes.
     *
     * @param worker the operation worker
     */
    public void bind(SwingWorker<?, UninstallationProgressEvent> worker) {
        worker.addPropertyChangeListener(new WaitForDoneWorkerCloseListener());
    }

    /**
     * Sets whether or not the dialogue should be shown synchronously.
     *
     * <p>If the dialogue is not shown synchronously the dialogue is only shown immediately if the
     * calculated operations will take some time otherwise it will not be shown unless it passed a
     * given time. It might happen that the dialogue is not shown at all if the operation finishes
     * in a given threshold.
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
     * @param events the events generated during the operation
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
                completedAddOnCount++;
                for (AddOnUninstallListener listener : listeners) {
                    failedUninstallations = !event.isUninstalled();
                    listener.addOnUninstalled(currentAddOn, update, event.isUninstalled());
                }
            } else if (UninstallationProgressEvent.Type.FINISHED_INSTALL == event.getType()) {
                completedAddOnCount++;
            } else if (UninstallationProgressEvent.Type.ADD_ON == event.getType()) {
                addOn = event.getAddOn();
                currentAddOn = addOn;
                update = event.isUpdate();

                for (AddOnUninstallListener listener : listeners) {
                    listener.uninstallingAddOn(addOn, update);
                }
            } else if (UninstallationProgressEvent.Type.INSTALL_ADD_ON == event.getType()) {
                addOn = event.getAddOn();
                currentAddOn = addOn;
                setCurrentAddOn(addOn);
            } else if (UninstallationProgressEvent.Type.PHASE == event.getType()) {
                messageLabel.setText(getPhaseMessageKey(event.getPhase()));
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
                case DOWNLOAD:
                    keyMessage = "cfu.operation.progress.dialogue.downloadingPercent";
                    break;
                case INSTALL_ADD_ON:
                    keyMessage = "";
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

    /** Describes an add-on operation shown by the progress dialogue. */
    static final class AddOnOperationPlan {

        enum Kind {
            UNINSTALL,
            INSTALL,
            UPDATE
        }

        private final Kind kind;
        private final Set<AddOn> uninstalls;
        private final Set<AddOn> oldVersions;
        private final Set<AddOn> toDownload;

        AddOnOperationPlan(
                Kind kind, Set<AddOn> uninstalls, Set<AddOn> oldVersions, Set<AddOn> toDownload) {
            this.kind = kind;
            this.uninstalls = uninstalls != null ? uninstalls : Collections.emptySet();
            this.oldVersions = oldVersions != null ? oldVersions : Collections.emptySet();
            this.toDownload = toDownload != null ? toDownload : Collections.emptySet();
        }

        static AddOnOperationPlan fromChanges(AddOnChangesResult changes) {
            Set<AddOn> toDownload = new HashSet<>();
            toDownload.addAll(changes.getInstalls());
            toDownload.addAll(changes.getNewVersions());

            Kind kind;
            if (toDownload.isEmpty()) {
                kind = Kind.UNINSTALL;
            } else if (changes.getOldVersions().isEmpty() && changes.getUninstalls().isEmpty()) {
                kind = Kind.INSTALL;
            } else {
                kind = Kind.UPDATE;
            }

            return new AddOnOperationPlan(
                    kind, changes.getUninstalls(), changes.getOldVersions(), toDownload);
        }

        static AddOnOperationPlan forUninstall(Set<AddOn> addOns, boolean updates) {
            if (updates) {
                return new AddOnOperationPlan(
                        Kind.UNINSTALL, Collections.emptySet(), addOns, Collections.emptySet());
            }
            return new AddOnOperationPlan(
                    Kind.UNINSTALL, addOns, Collections.emptySet(), Collections.emptySet());
        }

        Kind getKind() {
            return kind;
        }

        Set<AddOn> getUninstalls() {
            return uninstalls;
        }

        Set<AddOn> getOldVersions() {
            return oldVersions;
        }

        Set<AddOn> getToDownload() {
            return toDownload;
        }

        Set<AddOn> getAllUninstallAddOns() {
            Set<AddOn> all = new HashSet<>(uninstalls);
            all.addAll(oldVersions);
            return all;
        }

        int getTotalAddOnCount() {
            return getAllUninstallAddOns().size() + toDownload.size();
        }

        boolean hasDownloadOrInstall() {
            return !toDownload.isEmpty();
        }
    }

    /** A progress operation event. */
    static final class UninstallationProgressEvent {

        enum Phase {
            UNINSTALL,
            DOWNLOAD,
            INSTALL
        }

        enum Type {
            ADD_ON,
            FILE,
            ACTIVE_RULE,
            PASSIVE_RULE,
            EXTENSION,
            FINISHED_ADD_ON,
            PHASE,
            DOWNLOAD,
            INSTALL_ADD_ON,
            FINISHED_INSTALL
        }

        private final AddOn addOn;
        private final boolean update;
        private final boolean uninstalled;
        private final Type type;
        private final int amount;
        private final int value;
        private final int max;
        private final Phase phase;

        public UninstallationProgressEvent(AddOn addOn, boolean update) {
            this.addOn = addOn;
            this.update = update;
            this.uninstalled = false;
            this.amount = 0;
            this.type = Type.ADD_ON;
            this.value = 0;
            this.max = 0;
            this.phase = Phase.UNINSTALL;
        }

        public UninstallationProgressEvent(Type type, int value, int max) {
            addOn = null;
            update = false;
            uninstalled = false;
            this.type = type;
            amount = (type == Type.EXTENSION) ? EXTENSION_UNINSTALL_WEIGHT : 1;
            this.value = value;
            this.max = max;
            this.phase = Phase.UNINSTALL;
        }

        public UninstallationProgressEvent(boolean uninstalled) {
            this.uninstalled = uninstalled;
            this.type = Type.FINISHED_ADD_ON;
            this.addOn = null;
            this.update = false;
            this.amount = 0;
            this.value = 0;
            this.max = 0;
            this.phase = Phase.UNINSTALL;
        }

        private UninstallationProgressEvent(
                Type type,
                AddOn addOn,
                int amount,
                int value,
                int max,
                Phase phase,
                boolean uninstalled) {
            this.type = type;
            this.addOn = addOn;
            this.update = false;
            this.uninstalled = uninstalled;
            this.amount = amount;
            this.value = value;
            this.max = max;
            this.phase = phase;
        }

        static UninstallationProgressEvent phase(Phase phase) {
            return new UninstallationProgressEvent(Type.PHASE, null, 0, 0, 0, phase, false);
        }

        static UninstallationProgressEvent downloadProgress(
                AddOn addOn, int previousPercent, int currentPercent) {
            int delta = (currentPercent - previousPercent) * DOWNLOAD_WEIGHT / 100;
            return new UninstallationProgressEvent(
                    Type.DOWNLOAD, addOn, delta, currentPercent, 100, Phase.DOWNLOAD, false);
        }

        static UninstallationProgressEvent installAddOn(AddOn addOn) {
            return new UninstallationProgressEvent(
                    Type.INSTALL_ADD_ON, addOn, 0, 0, 0, Phase.INSTALL, false);
        }

        static UninstallationProgressEvent finishedInstall(AddOn addOn, boolean installed) {
            return new UninstallationProgressEvent(
                    Type.FINISHED_INSTALL,
                    addOn,
                    installed ? INSTALL_WEIGHT : 0,
                    0,
                    0,
                    Phase.INSTALL,
                    installed);
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

        Phase getPhase() {
            return phase;
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
