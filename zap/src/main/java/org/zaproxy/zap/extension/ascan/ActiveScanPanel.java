/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerListener;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.ScanListenner2;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.TableExportButton;
import org.zaproxy.zap.view.ScanPanel2;
import org.zaproxy.zap.view.ZapTable;
import org.zaproxy.zap.view.table.HistoryReferencesTable;

@SuppressWarnings("serial")
public class ActiveScanPanel extends ScanPanel2<ActiveScan, ScanController<ActiveScan>>
        implements ScanListenner2, ScannerListener {

    private static final Logger LOGGER = LogManager.getLogger(ActiveScanPanel.class);

    private static final long serialVersionUID = 1L;

    /**
     * The name of the active scan HTTP messages container.
     *
     * @see org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer
     */
    public static final String MESSAGE_CONTAINER_NAME = "ActiveScanMessageContainer";

    private static final String ZERO_REQUESTS_LABEL_TEXT = "0";
    private static final String ZERO_NEW_ALERTS_LABEL_TEXT = "0";

    private static final ActiveScanTableModel EMPTY_RESULTS_MODEL = new ActiveScanTableModel();
    private static final FilterMessageTableModel EMPTY_FILTER_MESSAGE_MODEL =
            new FilterMessageTableModel();

    private ExtensionActiveScan extension;
    private HistoryReferencesTable messagesTable;
    private ZapTable filterMessageTable;

    private JButton policyButton = null;
    private JButton scanButton = null;
    private JButton progressButton;
    private JLabel numRequests;
    private JLabel numNewAlerts;
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private TableExportButton<ZapTable> exportButton;

    /**
     * Constructs an {@code ActiveScanPanel} with the given extension.
     *
     * @param extension the active scan extension, to access options and start scans
     */
    public ActiveScanPanel(ExtensionActiveScan extension) {
        // 'fire' icon
        super(
                "ascan",
                new ImageIcon(ActiveScanPanel.class.getResource("/resource/icon/16/093.png")),
                extension);
        tabbedPane.addChangeListener(
                (e) -> {
                    switch (tabbedPane.getSelectedIndex()) {
                        case 0:
                            getExportButton().setTable(getMessagesTable());
                            break;
                        case 1:
                            getExportButton().setTable(getFilterMessageTable());
                            break;
                    }
                });
        this.extension = extension;
        this.setDefaultAccelerator(
                extension
                        .getView()
                        .getMenuShortcutKeyStroke(
                                KeyEvent.VK_A,
                                KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                                false));
        this.setMnemonic(Constant.messages.getChar("ascan.panel.mnemonic"));
    }

    @Override
    protected int addToolBarElements(JToolBar panelToolbar, Location loc, int x) {
        // Override to add elements into the toolbar
        if (Location.start.equals(loc)) {
            panelToolbar.add(getPolicyManagerButton(), getGBC(x++, 0));
        }
        if (Location.beforeProgressBar.equals(loc)) {
            panelToolbar.add(getProgressButton(), getGBC(x++, 0));
        }
        if (Location.afterProgressBar.equals(loc)) {
            panelToolbar.add(new JToolBar.Separator(), getGBC(x++, 0));
            panelToolbar.add(
                    new JLabel(Constant.messages.getString("ascan.toolbar.requests.label")),
                    getGBC(x++, 0));
            panelToolbar.add(getNumRequests(), getGBC(x++, 0));
            panelToolbar.add(new JToolBar.Separator(), getGBC(x++, 0));
            panelToolbar.add(
                    new JLabel(Constant.messages.getString("ascan.toolbar.newalerts.label")),
                    getGBC(x++, 0));
            panelToolbar.add(getNumNewAlerts(), getGBC(x++, 0));
            panelToolbar.add(new JToolBar.Separator(), getGBC(x++, 0));
            panelToolbar.add(getExportButton(), getGBC(x++, 0));
        }
        return x;
    }

    private JButton getPolicyManagerButton() {
        if (policyButton == null) {
            policyButton = new JButton();
            policyButton.setToolTipText(Constant.messages.getString("menu.analyse.scanPolicy"));
            policyButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ActiveScanPanel.class.getResource(
                                            "/resource/icon/fugue/equalizer.png"))));
            policyButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            extension.showPolicyManagerDialog();
                        }
                    });
        }
        return policyButton;
    }

    @Override
    public JButton getNewScanButton() {
        if (scanButton == null) {
            scanButton = new JButton(Constant.messages.getString("ascan.toolbar.button.new"));
            scanButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ActiveScanPanel.class.getResource(
                                            "/resource/icon/16/093.png"))));
            scanButton.addActionListener(e -> extension.showCustomScanDialog((Target) null));
        }
        return scanButton;
    }

    private JButton getProgressButton() {
        if (progressButton == null) {
            progressButton = new JButton();
            progressButton.setEnabled(false);
            progressButton.setToolTipText(
                    Constant.messages.getString("ascan.toolbar.button.progress"));
            progressButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ActiveScanPanel.class.getResource(
                                            "/resource/icon/fugue/system-monitor.png"))));
            progressButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            showScanProgressDialog();
                        }
                    });
        }
        return progressButton;
    }

    private JLabel getNumRequests() {
        if (numRequests == null) {
            numRequests = new JLabel(ZERO_REQUESTS_LABEL_TEXT);
        }
        return numRequests;
    }

    private void showScanProgressDialog() {
        ActiveScan scan = this.getSelectedScanner();
        if (scan != null) {
            ScanProgressDialog spp =
                    new ScanProgressDialog(
                            View.getSingleton().getMainFrame(),
                            scan.getDisplayName(),
                            this.extension);
            spp.setActiveScan(scan);
            spp.setVisible(true);
        }
    }

    private TableExportButton<ZapTable> getExportButton() {
        if (exportButton == null) {
            exportButton = new TableExportButton<>(getMessagesTable());
        }
        return exportButton;
    }

    @Override
    public void clearFinishedScans() {
        if (extension.getScannerParam().isPromptToClearFinishedScans()) {
            // Prompt to double check
            int res =
                    View.getSingleton()
                            .showConfirmDontPromptDialog(
                                    View.getSingleton().getMainFrame(),
                                    Constant.messages.getString("ascan.toolbar.confirm.clear"));
            if (View.getSingleton().isDontPromptLastDialogChosen()) {
                extension.getScannerParam().setPromptToClearFinishedScans(false);
            }
            if (res != JOptionPane.YES_OPTION) {
                return;
            }
        }
        super.clearFinishedScans();
    }

    @Override
    protected JPanel getWorkPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());
            tabbedPane = new JTabbedPane();

            JScrollPane jScrollPane = new JScrollPane();
            jScrollPane.setName("ActiveScanMessagePane");
            jScrollPane.setViewportView(getMessagesTable());
            JScrollPane filterPane = new JScrollPane();
            filterPane.setName("FilterMessagePane");
            filterPane.setViewportView(getFilterMessageTable());

            tabbedPane.add(
                    Constant.messages.getString("ascan.panel.tab.scannedMessages"), jScrollPane);
            tabbedPane.add(
                    Constant.messages.getString("ascan.panel.tab.filteredMessages"), filterPane);
            tabbedPane.setSelectedIndex(0);
            mainPanel.add(tabbedPane);
        }
        return mainPanel;
    }

    private void resetMessagesTable() {
        getMessagesTable().setModel(EMPTY_RESULTS_MODEL);
    }

    private void resetFilterMessageTable() {
        getFilterMessageTable().setModel(EMPTY_FILTER_MESSAGE_MODEL);
    }

    private HistoryReferencesTable getMessagesTable() {
        if (messagesTable == null) {
            messagesTable = new HistoryReferencesTable(EMPTY_RESULTS_MODEL);
            messagesTable.setName(MESSAGE_CONTAINER_NAME);
            messagesTable.setAutoCreateColumnsFromModel(false);
        }
        return messagesTable;
    }

    private ZapTable getFilterMessageTable() {
        if (filterMessageTable == null) {
            this.filterMessageTable = new ZapTable(EMPTY_FILTER_MESSAGE_MODEL);
            this.filterMessageTable.setName("FilterMessageTable");
            this.filterMessageTable.setAutoCreateColumnsFromModel(false);
        }
        return this.filterMessageTable;
    }

    @Override
    public void switchView(final ActiveScan scanner) {
        if (View.isInitialised() && !EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {

                            @Override
                            public void run() {
                                switchView(scanner);
                            }
                        });
            } catch (InvocationTargetException | InterruptedException e) {
                LOGGER.error("Failed to switch view: {}", e.getMessage(), e);
            }
            return;
        }

        if (scanner != null) {
            getMessagesTable().setModel(scanner.getMessagesTableModel());
            getFilterMessageTable().setModel(scanner.getFilterMessageTableModel());
            this.getNumRequests().setText(Integer.toString(scanner.getTotalRequests()));
            this.getNumNewAlerts().setText(Integer.toString(scanner.getTotalNewAlerts()));
            this.getProgressButton().setEnabled(true);

            if (scanner instanceof AttackScan) {
                // Its the custom scanner - none of these controls make sense
                this.getProgressBar().setEnabled(false);
                this.getProgressButton().setEnabled(false);
                this.getPauseScanButton().setEnabled(false);
                this.getStopScanButton().setEnabled(false);
            }
        } else {
            resetMessagesTable();
            resetFilterMessageTable();
            this.getNumRequests().setText(ZERO_REQUESTS_LABEL_TEXT);
            this.getNumNewAlerts().setText(ZERO_NEW_ALERTS_LABEL_TEXT);
            this.getProgressButton().setEnabled(false);
        }
    }

    @Override
    public void alertFound(Alert alert) {
        // Nothing to do, ActiveScanController (through ActiveScan) already raises the alerts.
    }

    @Override
    public void hostComplete(int id, String hostAndPort) {
        this.scanFinshed(id, hostAndPort);
    }

    @Override
    public void hostNewScan(int id, String hostAndPort, HostProcess hostThread) {}

    @Override
    public void hostProgress(int id, String hostAndPort, String msg, int percentage) {
        this.scanProgress(id, hostAndPort, percentage, 100);
        updateRequestCount();
        updateNewAlertCount();
    }

    @Override
    public void scannerComplete(int id) {
        this.scanFinshed(id, this.getName());
    }

    private void updateRequestCount() {
        ActiveScan ac = this.getSelectedScanner();
        if (ac != null) {
            this.getNumRequests().setText(Integer.toString(ac.getTotalRequests()));
        }
    }

    @Override
    public void notifyNewMessage(HttpMessage msg) {}

    private void updateNewAlertCount() {
        ActiveScan ac = this.getSelectedScanner();
        if (ac != null) {
            this.getNumNewAlerts().setText(Integer.toString(ac.getTotalNewAlerts()));
        }
    }

    @Override
    public void reset() {
        super.reset();
        this.resetMessagesTable();
        this.resetFilterMessageTable();
        this.getProgressButton().setEnabled(false);
    }

    @Override
    protected int getNumberOfScansToShow() {
        return extension.getScannerParam().getMaxScansInUI();
    }

    private JLabel getNumNewAlerts() {
        if (numNewAlerts == null) {
            numNewAlerts = new JLabel(ZERO_NEW_ALERTS_LABEL_TEXT);
        }
        return numNewAlerts;
    }
}
