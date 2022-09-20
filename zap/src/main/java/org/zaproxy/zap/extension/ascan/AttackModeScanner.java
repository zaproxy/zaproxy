/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Scanner;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMapEventPublisher;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.log4j.ExtensionLog4j;
import org.zaproxy.zap.extension.ruleconfig.ExtensionRuleConfig;
import org.zaproxy.zap.extension.ruleconfig.RuleConfigParam;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.ScanStatus;

public class AttackModeScanner implements EventConsumer {

    private static final String ATTACK_ICON_RESOURCE = "/resource/icon/16/093.png";

    private ExtensionActiveScan extension;
    private long lastUpdated;
    private ScanStatus scanStatus;
    private ExtensionAlert extAlert = null;
    private AttackModeThread attackModeThread = null;
    private boolean rescanOnChange = false;

    private Logger log = LogManager.getLogger(AttackModeScanner.class);

    private List<SiteNode> nodeStack = new ArrayList<>();

    public AttackModeScanner(ExtensionActiveScan extension) {
        this.extension = extension;
        ZAP.getEventBus().registerConsumer(this, SiteMapEventPublisher.class.getCanonicalName());

        if (extension.getView() != null) {
            lastUpdated = System.currentTimeMillis();
            scanStatus =
                    new ScanStatus(
                            new ImageIcon(
                                    ExtensionLog4j.class.getResource(
                                            "/resource/icon/fugue/target.png")),
                            Constant.messages.getString("ascan.attack.icon.title"));
        }
    }

    public void start() {
        log.debug("Starting");
        nodeStack.clear();

        this.addAllInScope();

        if (attackModeThread != null) {
            attackModeThread.shutdown();
        }
        attackModeThread = new AttackModeThread();
        Thread t = new Thread(attackModeThread, "ZAP-AttackMode");
        t.setDaemon(true);
        t.start();
    }

    private void addAllInScope() {
        if (this.rescanOnChange) {
            this.nodeStack.addAll(Model.getSingleton().getSession().getNodesInScopeFromSiteTree());
            log.debug(
                    "Added existing in scope nodes to attack mode stack {}", this.nodeStack.size());
            updateCount();
        }
    }

    public void stop() {
        log.debug("Stopping");
        if (this.attackModeThread != null) {
            this.attackModeThread.shutdown();
        }
        nodeStack.clear();
        updateCount();
    }

    @Override
    public void eventReceived(Event event) {
        if (this.attackModeThread != null && this.attackModeThread.isRunning()) {
            if (event.getEventType().equals(SiteMapEventPublisher.SITE_NODE_ADDED_EVENT)
                    && event.getTarget().getStartNode().isIncludedInScope()) {
                if (event.getTarget().getStartNode().getHistoryReference().getHistoryType()
                        != HistoryReference.TYPE_TEMPORARY) {
                    // Add to the stack awaiting attack
                    log.debug(
                            "Adding node to attack mode stack {}",
                            event.getTarget().getStartNode());
                    nodeStack.add(event.getTarget().getStartNode());
                    updateCount();
                }
            } else if (event.getEventType().equals(SiteMapEventPublisher.SITE_NODE_REMOVED_EVENT)) {
                if (nodeStack.contains(event.getTarget().getStartNode())) {
                    nodeStack.remove(event.getTarget().getStartNode());
                }
            }
        }
    }

    /**
     * Gets the {@link ScanStatus}.
     *
     * @return the {@code ScanStatus}, or {@code null} if there's no view/UI.
     */
    public ScanStatus getScanStatus() {
        return scanStatus;
    }

    public void sessionScopeChanged(Session session) {
        this.addAllInScope();
    }

    public void sessionModeChanged(Mode mode) {
        if (mode.equals(Mode.attack)) {
            if (View.isInitialised()
                    && View.getSingleton().isCanGetFocus()
                    && extension.getScannerParam().isPromptInAttackMode()) {
                SwingUtilities.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                boolean anyInScope = false;
                                for (Context ctx :
                                        Model.getSingleton().getSession().getContexts()) {
                                    if (ctx.isInScope()
                                            && (ctx.getIncludeInContextRegexs() != null
                                                    && !ctx.getIncludeInContextRegexs()
                                                            .isEmpty())) {
                                        anyInScope = true;
                                        break;
                                    }
                                }
                                String promptMsg =
                                        anyInScope
                                                ? Constant.messages.getString("ascan.attack.prompt")
                                                : Constant.messages.getString("ascan.attack.prompt")
                                                        + Constant.messages.getString(
                                                                "ascan.attack.prompt.no.scope",
                                                                Constant.messages.getString(
                                                                        "view.toolbar.mode.attack.select"));
                                int res =
                                        View.getSingleton()
                                                .showYesNoRememberDialog(
                                                        View.getSingleton().getMainFrame(),
                                                        promptMsg);

                                if (View.getSingleton().isRememberLastDialogChosen()) {
                                    extension.getScannerParam().setPromptInAttackMode(false);
                                    extension
                                            .getScannerParam()
                                            .setRescanInAttackMode(res == JOptionPane.YES_OPTION);
                                }
                                rescanOnChange = (res == JOptionPane.YES_OPTION);
                                start();
                            }
                        });
            } else {
                this.rescanOnChange = extension.getScannerParam().isRescanInAttackMode();
                this.start();
            }

        } else {
            this.stop();
        }
    }

    /**
     * Updates the count of the {@link #scanStatus scan status}' label.
     *
     * <p>The call to this method has no effect if the view was not initialised.
     */
    private void updateCount() {
        if (scanStatus == null) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - this.lastUpdated > 200) {
            // Dont update too frequently, e.g. using the spider could hammer the UI unnecessarily
            this.lastUpdated = now;
            SwingUtilities.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            scanStatus.setScanCount(nodeStack.size());
                        }
                    });
        }
    }

    public int getStackSize() {
        int count = nodeStack.size();
        if (count > 0) {
            // There are nodes to scan
            return count;
        }
        // Work out if any scanning is in progress
        if (this.attackModeThread != null && this.attackModeThread.isActive()) {
            return 0;
        }
        return -1;
    }

    public boolean isRescanOnChange() {
        return rescanOnChange;
    }

    public void setRescanOnChange(boolean rescanOnChange) {
        this.rescanOnChange = rescanOnChange;
    }

    private ExtensionAlert getExtensionAlert() {
        if (extAlert == null) {
            extAlert =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
        }
        return extAlert;
    }

    private class AttackModeThread implements Runnable, ScannerListener, AttackModeScannerThread {

        private int scannerCount = 4;
        private List<Scanner> scanners = new ArrayList<>();
        private AttackScan ascanWrapper;
        private boolean running = false;

        @Override
        public void run() {
            log.debug("Starting attack thread");
            this.running = true;

            RuleConfigParam ruleConfigParam = null;
            ExtensionRuleConfig extRC =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionRuleConfig.class);
            if (extRC != null) {
                ruleConfigParam = extRC.getRuleConfigParam();
            }

            ascanWrapper =
                    new AttackScan(
                            Constant.messages.getString("ascan.attack.scan"),
                            extension.getScannerParam(),
                            extension.getPolicyManager().getAttackScanPolicy(),
                            ruleConfigParam,
                            this);
            extension.registerScan(ascanWrapper);
            while (running) {
                if (scanStatus != null && scanStatus.getScanCount() != nodeStack.size()) {
                    updateCount();
                }
                if (nodeStack.isEmpty() || scanners.size() == scannerCount) {
                    if (scanners.size() > 0) {
                        // Check to see if any have finished
                        scannerComplete(-1);
                    }
                    // Still scanning a node or nothing to scan now
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // Ignore
                    }
                    continue;
                }
                while (nodeStack.size() > 0 && scanners.size() < scannerCount) {
                    SiteNode node = nodeStack.remove(0);
                    log.debug("Attacking node {}", node.getNodeName());

                    Scanner scanner =
                            new Scanner(
                                    extension.getScannerParam(),
                                    extension.getPolicyManager().getAttackScanPolicy(),
                                    ruleConfigParam);
                    scanner.setStartNode(node);
                    scanner.setScanChildren(false);
                    scanner.addScannerListener(this);
                    synchronized (this.scanners) {
                        this.scanners.add(scanner);
                    }

                    if (View.isInitialised()) {
                        // set icon to show its being scanned
                        node.addCustomIcon(ATTACK_ICON_RESOURCE, false);
                    }
                    scanner.start(node);
                }
            }
            synchronized (this.scanners) {
                for (Scanner scanner : this.scanners) {
                    scanner.stop();
                }
            }
            log.debug("Attack thread finished");
        }

        @Override
        public void scannerComplete(int id) {
            // Clear so we can attack the next node
            List<Scanner> stoppedScanners = new ArrayList<>();
            synchronized (this.scanners) {
                for (Scanner scanner : this.scanners) {
                    if (scanner.isStop()) {
                        SiteNode node = scanner.getStartNode();
                        if (node != null) {
                            log.debug("Finished attacking node {}", node.getNodeName());
                            if (View.isInitialised()) {
                                // Remove the icon
                                node.removeCustomIcon(ATTACK_ICON_RESOURCE);
                            }
                        }
                        stoppedScanners.add(scanner);
                    }
                }
                for (Scanner scanner : stoppedScanners) {
                    // Cant remove them in the above loop
                    scanners.remove(scanner);
                }
            }
            updateCount();
        }

        @Override
        public void hostNewScan(int id, String hostAndPort, HostProcess hostThread) {
            // Ignore
        }

        @Override
        public void hostProgress(int id, String hostAndPort, String msg, int percentage) {
            // Ignore
        }

        @Override
        public void hostComplete(int id, String hostAndPort) {
            // Ignore
        }

        @Override
        public void alertFound(Alert alert) {
            alert.setSource(Alert.Source.ACTIVE);
            getExtensionAlert().alertFound(alert, alert.getHistoryRef());
        }

        @Override
        public void notifyNewMessage(HttpMessage msg) {
            ascanWrapper.notifyNewMessage(msg);
        }

        public void shutdown() {
            this.running = false;
        }

        @Override
        public boolean isRunning() {
            return this.running;
        }

        /**
         * Tells whether or not any of the scan threads are currently active.
         *
         * @return {@code true} if there's at least one scan active, {@code false} otherwise
         */
        @Override
        public boolean isActive() {
            synchronized (this.scanners) {
                for (Scanner scanner : this.scanners) {
                    if (!scanner.isStop()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    interface AttackModeScannerThread {

        boolean isRunning();

        boolean isActive();
    }
}
