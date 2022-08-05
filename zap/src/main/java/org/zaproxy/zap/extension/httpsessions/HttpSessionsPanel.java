/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.httpsessions;

import java.awt.CardLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.utils.TableExportButton;
import org.zaproxy.zap.view.ScanPanel;
import org.zaproxy.zap.view.table.decorator.AbstractTableCellItemIconHighlighter;

/**
 * The HttpSessionsPanel used as a display panel for the {@link ExtensionHttpSessions}, allowing the
 * user to view and control the http sessions.
 */
@SuppressWarnings("serial")
public class HttpSessionsPanel extends AbstractPanel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant PANEL_NAME. */
    public static final String PANEL_NAME = "httpsessions";

    /** The extension. */
    private ExtensionHttpSessions extension = null;

    private JPanel panelCommand = null;
    private JToolBar panelToolbar = null;
    private JScrollPane jScrollPane = null;
    private JComboBox<String> siteSelect = null;
    private JButton newSessionButton = null;
    private JXTable sessionsTable = null;
    private TableExportButton<JXTable> exportButton = null;
    private JButton optionsButton = null;

    /** The current site. */
    private String currentSite = null;

    /** The site model. */
    private SortedComboBoxModel<String> siteModel = new SortedComboBoxModel<>();

    /** The sessions model. */
    private HttpSessionsTableModel sessionsModel = new HttpSessionsTableModel(null);

    /**
     * Instantiates a new http session panel.
     *
     * @param extensionHttpSession the extension http session
     */
    public HttpSessionsPanel(ExtensionHttpSessions extensionHttpSession) {
        super();
        this.extension = extensionHttpSession;
        initialize();
    }

    /** This method initializes this panel. */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("httpsessions.panel.title"));
        this.setIcon(
                new ImageIcon(
                        HttpSessionsPanel.class.getResource("/resource/icon/16/session.png")));
        this.setDefaultAccelerator(
                extension
                        .getView()
                        .getMenuShortcutKeyStroke(
                                KeyEvent.VK_H,
                                KeyEvent.ALT_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK,
                                false));
        this.setMnemonic(Constant.messages.getChar("httpsessions.panel.mnemonic"));
        this.add(getPanelCommand(), getPanelCommand().getName());
    }

    /**
     * This method initializes the main panel.
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getPanelCommand() {
        if (panelCommand == null) {
            panelCommand = new javax.swing.JPanel();
            panelCommand.setLayout(new java.awt.GridBagLayout());
            panelCommand.setName(Constant.messages.getString("httpsessions.panel.title"));

            // Add the two components: toolbar and work pane
            GridBagConstraints toolbarGridBag = new GridBagConstraints();
            GridBagConstraints workPaneGridBag = new GridBagConstraints();

            toolbarGridBag.gridx = 0;
            toolbarGridBag.gridy = 0;
            toolbarGridBag.weightx = 1.0d;
            toolbarGridBag.insets = new java.awt.Insets(2, 2, 2, 2);
            toolbarGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
            toolbarGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;

            workPaneGridBag.gridx = 0;
            workPaneGridBag.gridy = 1;
            workPaneGridBag.weightx = 1.0;
            workPaneGridBag.weighty = 1.0;
            workPaneGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
            workPaneGridBag.anchor = java.awt.GridBagConstraints.NORTHWEST;
            workPaneGridBag.fill = java.awt.GridBagConstraints.BOTH;

            panelCommand.add(this.getPanelToolbar(), toolbarGridBag);
            panelCommand.add(getWorkPane(), workPaneGridBag);
        }
        return panelCommand;
    }

    private TableExportButton<JXTable> getExportButton() {
        if (exportButton == null) {
            exportButton = new TableExportButton<>(getHttpSessionsTable());
        }
        return exportButton;
    }

    /**
     * Gets the options button.
     *
     * @return the options button
     */
    private JButton getOptionsButton() {
        if (optionsButton == null) {
            optionsButton = new JButton();
            optionsButton.setToolTipText(
                    Constant.messages.getString("httpsessions.toolbar.options.button"));
            optionsButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ScanPanel.class.getResource("/resource/icon/16/041.png"))));
            optionsButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            Control.getSingleton()
                                    .getMenuToolsControl()
                                    .options(
                                            Constant.messages.getString(
                                                    "httpsessions.options.title"));
                        }
                    });
        }
        return optionsButton;
    }

    /**
     * Gets the new session button.
     *
     * @return the new session button
     */
    private JButton getNewSessionButton() {
        if (newSessionButton == null) {
            newSessionButton = new JButton();
            newSessionButton.setText(
                    Constant.messages.getString("httpsessions.toolbar.newsession.label"));
            newSessionButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    HttpSessionsPanel.class.getResource(
                                            "/resource/icon/16/103.png"))));
            newSessionButton.setToolTipText(
                    Constant.messages.getString("httpsessions.toolbar.newsession.tooltip"));

            newSessionButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            HttpSessionsSite site = getCurrentHttpSessionSite();
                            if (site != null) {
                                site.createEmptySession();
                            }
                        }
                    });
        }
        return newSessionButton;
    }

    /**
     * Gets the panel's toolbar.
     *
     * @return the panel toolbar
     */
    private javax.swing.JToolBar getPanelToolbar() {
        if (panelToolbar == null) {

            // Initialize the toolbar
            panelToolbar = new javax.swing.JToolBar();
            panelToolbar.setLayout(new java.awt.GridBagLayout());
            panelToolbar.setEnabled(true);
            panelToolbar.setFloatable(false);
            panelToolbar.setRollover(true);
            panelToolbar.setPreferredSize(new java.awt.Dimension(800, 30));
            panelToolbar.setName("HttpSessionToolbar");

            // Add elements
            GridBagConstraints labelGridBag = new GridBagConstraints();
            GridBagConstraints siteSelectGridBag = new GridBagConstraints();
            GridBagConstraints newSessionGridBag = new GridBagConstraints();
            GridBagConstraints emptyGridBag = new GridBagConstraints();
            GridBagConstraints optionsGridBag = new GridBagConstraints();
            GridBagConstraints exportButtonGbc = new GridBagConstraints();

            labelGridBag.gridx = 0;
            labelGridBag.gridy = 0;
            labelGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
            labelGridBag.anchor = java.awt.GridBagConstraints.WEST;

            siteSelectGridBag.gridx = 1;
            siteSelectGridBag.gridy = 0;
            siteSelectGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
            siteSelectGridBag.anchor = java.awt.GridBagConstraints.WEST;

            newSessionGridBag.gridx = 2;
            newSessionGridBag.gridy = 0;
            newSessionGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
            newSessionGridBag.anchor = java.awt.GridBagConstraints.WEST;

            exportButtonGbc.gridx = 3;
            exportButtonGbc.gridy = 0;
            exportButtonGbc.insets = new java.awt.Insets(0, 0, 0, 0);
            exportButtonGbc.anchor = java.awt.GridBagConstraints.WEST;

            emptyGridBag.gridx = 4;
            emptyGridBag.gridy = 0;
            emptyGridBag.weightx = 1.0;
            emptyGridBag.weighty = 1.0;
            emptyGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
            emptyGridBag.anchor = java.awt.GridBagConstraints.WEST;
            emptyGridBag.fill = java.awt.GridBagConstraints.HORIZONTAL;

            optionsGridBag.gridx = 5;
            optionsGridBag.gridy = 0;
            optionsGridBag.insets = new java.awt.Insets(0, 0, 0, 0);
            optionsGridBag.anchor = java.awt.GridBagConstraints.EAST;

            JLabel label =
                    new JLabel(Constant.messages.getString("httpsessions.toolbar.site.label"));

            panelToolbar.add(label, labelGridBag);
            panelToolbar.add(getSiteSelect(), siteSelectGridBag);
            panelToolbar.add(getNewSessionButton(), newSessionGridBag);
            panelToolbar.add(getExportButton(), exportButtonGbc);
            panelToolbar.add(getOptionsButton(), optionsGridBag);

            // Add an empty JLabel to fill the space
            panelToolbar.add(new JLabel(), emptyGridBag);
        }
        return panelToolbar;
    }

    /**
     * Gets the work pane where data is shown.
     *
     * @return the work pane
     */
    private JScrollPane getWorkPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getHttpSessionsTable());
        }
        return jScrollPane;
    }

    /** Sets the sessions table column sizes. */
    private void setSessionsTableColumnSizes() {

        sessionsTable.getColumnModel().getColumn(0).setMinWidth(60);
        sessionsTable.getColumnModel().getColumn(0).setPreferredWidth(60); // active

        sessionsTable.getColumnModel().getColumn(1).setMinWidth(120);
        sessionsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // name

        sessionsTable.getColumnModel().getColumn(3).setMinWidth(100);
        sessionsTable.getColumnModel().getColumn(3).setPreferredWidth(150); // matched
    }

    /**
     * Gets the http sessions table.
     *
     * @return the http sessions table
     */
    private JXTable getHttpSessionsTable() {
        if (sessionsTable == null) {
            sessionsTable = new JXTable(sessionsModel);

            sessionsTable.setColumnSelectionAllowed(false);
            sessionsTable.setCellSelectionEnabled(false);
            sessionsTable.setRowSelectionAllowed(true);
            sessionsTable.setAutoCreateRowSorter(true);
            sessionsTable.setColumnControlVisible(true);
            sessionsTable.setAutoCreateColumnsFromModel(false);

            sessionsTable
                    .getColumnExt(0)
                    .setCellRenderer(
                            new DefaultTableRenderer(
                                    new MappedValue(StringValues.EMPTY, IconValues.NONE),
                                    JLabel.CENTER));
            sessionsTable.getColumnExt(0).setHighlighters(new ActiveSessionIconHighlighter(0));

            this.setSessionsTableColumnSizes();

            sessionsTable.setName(PANEL_NAME);
            sessionsTable.setDoubleBuffered(true);
            sessionsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
            sessionsTable.addMouseListener(
                    new java.awt.event.MouseAdapter() {
                        @Override
                        public void mousePressed(java.awt.event.MouseEvent e) {

                            showPopupMenuIfTriggered(e);
                        }

                        @Override
                        public void mouseReleased(java.awt.event.MouseEvent e) {
                            showPopupMenuIfTriggered(e);
                        }

                        private void showPopupMenuIfTriggered(java.awt.event.MouseEvent e) {
                            if (e.isPopupTrigger()) {

                                // Select table item
                                int row = sessionsTable.rowAtPoint(e.getPoint());
                                if (row < 0
                                        || !sessionsTable
                                                .getSelectionModel()
                                                .isSelectedIndex(row)) {
                                    sessionsTable.getSelectionModel().clearSelection();
                                    if (row >= 0) {
                                        sessionsTable
                                                .getSelectionModel()
                                                .setSelectionInterval(row, row);
                                    }
                                }

                                View.getSingleton()
                                        .getPopupMenu()
                                        .show(e.getComponent(), e.getX(), e.getY());
                            }
                        }
                    });
        }
        return sessionsTable;
    }

    /**
     * Gets the site select ComboBox.
     *
     * @return the site select
     */
    private JComboBox<String> getSiteSelect() {
        if (siteSelect == null) {
            siteSelect = new JComboBox<>(siteModel);
            siteSelect.addItem(Constant.messages.getString("httpsessions.toolbar.site.select"));
            siteSelect.setSelectedIndex(0);

            // Add the item listener for when the site is selected
            siteSelect.addItemListener(
                    new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (ItemEvent.SELECTED == e.getStateChange()) {
                                if (siteSelect.getSelectedIndex() > 0) {
                                    siteSelected((String) e.getItem());

                                } // If the user selects the first option (empty one), force the
                                // selection to
                                // the first valid site
                                else if (siteModel.getSize() > 1) {
                                    siteModel.setSelectedItem(siteModel.getElementAt(1));
                                }
                            }
                        }
                    });
        }
        return siteSelect;
    }

    /**
     * Adds a new site to the "Http Sessions" tab.
     *
     * <p>The method must be called in the EDT, failing to do so might result in thread interference
     * or memory consistency errors.
     *
     * @param site the site
     * @see #addSiteAsynchronously(String)
     * @see EventQueue
     */
    public void addSite(String site) {
        if (siteModel.getIndexOf(site) < 0) {
            siteModel.addElement(site);
            if (currentSite == null) {
                // First site added, automatically select it
                siteModel.setSelectedItem(site);
            }
        }
    }

    /**
     * Adds a new site, asynchronously, to the "Http Sessions" tab.
     *
     * <p>The call to this method will return immediately and the site will be added in the EDT (by
     * calling the method {@code EventQueue#invokeLater(Runnable)}) after all pending events have
     * been processed.
     *
     * @param site the site
     * @see #addSite(String)
     * @see EventQueue#invokeLater(Runnable)
     */
    public void addSiteAsynchronously(final String site) {
        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        addSite(site);
                    }
                });
    }

    /**
     * A new Site was selected.
     *
     * @param site the site
     */
    private void siteSelected(String site) {
        if (!site.equals(currentSite)) {
            this.sessionsModel = extension.getHttpSessionsSite(site).getModel();
            this.getHttpSessionsTable().setModel(this.sessionsModel);

            this.setSessionsTableColumnSizes();

            currentSite = site;
        }
    }

    /**
     * Node selected.
     *
     * @param node the node
     */
    public void nodeSelected(SiteNode node) {
        if (node != null) {
            siteModel.setSelectedItem(ScanPanel.cleanSiteName(node, true));
        }
    }

    /** Reset the panel. */
    public void reset() {
        currentSite = null;

        siteModel.removeAllElements();
        siteModel.addElement(Constant.messages.getString("httpsessions.toolbar.site.select"));

        sessionsModel = new HttpSessionsTableModel(null);
        getHttpSessionsTable().setModel(sessionsModel);
    }

    /**
     * Gets the current http session site.
     *
     * @return the current http session site, or null if no HttpSessionSite is selected
     */
    public HttpSessionsSite getCurrentHttpSessionSite() {
        if (currentSite == null) {
            return null;
        }
        return extension.getHttpSessionsSite(currentSite);
    }

    /**
     * Gets the currently selected site.
     *
     * @return the current site
     */
    public String getCurrentSite() {
        return currentSite;
    }

    /**
     * Gets the selected http session.
     *
     * @return the selected session, or null if nothing is selected
     */
    public HttpSession getSelectedSession() {
        final int selectedRow = this.sessionsTable.getSelectedRow();
        if (selectedRow == -1) {
            // No row selected
            return null;
        }
        final int rowIndex = sessionsTable.convertRowIndexToModel(selectedRow);
        return this.sessionsModel.getHttpSessionAt(rowIndex);
    }

    /**
     * A {@link org.jdesktop.swingx.decorator.Highlighter Highlighter} for a column that indicates,
     * using an icon, whether or not a session is active.
     *
     * <p>The expected type/class of the cell value is {@code Boolean}.
     */
    private static class ActiveSessionIconHighlighter extends AbstractTableCellItemIconHighlighter {

        /** The icon that indicates that a session is active. */
        private static final ImageIcon ACTIVE_ICON =
                new ImageIcon(HttpSessionsPanel.class.getResource("/resource/icon/16/102.png"));

        public ActiveSessionIconHighlighter(int columnIndex) {
            super(columnIndex);
        }

        @Override
        protected Icon getIcon(final Object cellItem) {
            return ACTIVE_ICON;
        }

        @Override
        protected boolean isHighlighted(final Object cellItem) {
            return (Boolean) cellItem;
        }
    }
}
