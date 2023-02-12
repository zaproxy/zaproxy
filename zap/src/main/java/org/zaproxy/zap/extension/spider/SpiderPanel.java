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
package org.zaproxy.zap.extension.spider;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.ScanListenner2;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.TableExportButton;
import org.zaproxy.zap.view.ScanPanel2;
import org.zaproxy.zap.view.ZapTable;
import org.zaproxy.zap.view.table.decorator.AbstractTableCellItemIconHighlighter;

/**
 * The Class SpiderPanel implements the Panel that is shown to the users when selecting the Spider
 * Scan Tab.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@SuppressWarnings("serial")
@Deprecated
public class SpiderPanel extends ScanPanel2<SpiderScan, ScanController<SpiderScan>>
        implements ScanListenner2 {

    /**
     * The name of the spider's HTTP messages container.
     *
     * @since 2.5.0
     * @see org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer
     */
    public static final String HTTP_MESSAGE_CONTAINER_NAME = "SpiderHttpMessageContainer";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(SpiderPanel.class);

    private static final String ZERO_REQUESTS_LABEL_TEXT = "0";

    private static final SpiderPanelTableModel EMPTY_URLS_TABLE_MODEL = new SpiderPanelTableModel();

    private static final SpiderPanelTableModel EMPTY_URLS_NO_FLAGS_TABLE_MODEL =
            new SpiderPanelTableModel(false);

    private static final SpiderMessagesTableModel EMPTY_MESSAGES_TABLE_MODEL =
            new SpiderMessagesTableModel(false);

    /** The Constant defining the PANEL's NAME. */
    public static final String PANEL_NAME = "SpiderPanel";

    private static final String ADDED_NODES_CONTAINER_NAME = "SpiderAddedNodesContainer";

    /**
     * The main panel, where the {@link #tabbedPane} or {@link #urlsTableScrollPane} are added.
     *
     * <p>Lazily initialised.
     *
     * @see #getWorkPanel()
     */
    private JPanel mainPanel;

    /** The {@code JTabbedPane} used to show the tabs for URLs found and HTTP messages sent. */
    private JTabbedPane tabbedPane;

    private JButton scanButton = null;

    /**
     * The table with URLs found.
     *
     * <p>Lazily initialised.
     *
     * @see #getUrlsTable()
     * @see #urlsTableScrollPane
     */
    private ZapTable urlsTable;

    /**
     * The table with added nodes.
     *
     * <p>Lazily initialised.
     *
     * @see #getAddedNodesTable()
     * @see #addedNodesTableScrollPane
     */
    private ZapTable addedNodesTable;

    /**
     * The scroll pane for the URLs table.
     *
     * <p>Lazily initialised.
     *
     * @see #getUrlsTableScrollPane()
     * @see #urlsTable
     */
    private JScrollPane urlsTableScrollPane;

    /**
     * The scroll pane for the added nodes table.
     *
     * <p>Lazily initialised.
     *
     * @see #getAddedNodesTableScrollPane()
     * @see #addedNodesTable
     */
    private JScrollPane addedNodesTableScrollPane;

    /**
     * The table with HTTP messages sent.
     *
     * <p>Lazily initialised.
     *
     * @see #getMessagesTable()
     * @see #messagesTableScrollPane
     */
    private SpiderMessagesTable messagesTable;

    /**
     * The scroll pane for the HTTP messages table.
     *
     * <p>Lazily initialised.
     *
     * @see #getMessagesTableScrollPanel()
     * @see #messagesTable
     */
    private JScrollPane messagesTableScrollPane;

    /** The found count name label. */
    private JLabel foundCountNameLabel;

    /** The found count value label. */
    private JLabel foundCountValueLabel;

    /** The added count name label. */
    private JLabel addedCountNameLabel;

    /** The added count value label. */
    private JLabel addedCountValueLabel;

    private TableExportButton<JXTable> exportButton;

    private ExtensionSpider extension = null;

    /**
     * Instantiates a new spider panel.
     *
     * @param extension the extension
     * @param spiderScanParam the spider scan parameters
     */
    public SpiderPanel(
            ExtensionSpider extension, org.zaproxy.zap.spider.SpiderParam spiderScanParam) {
        super(
                "spider",
                new ImageIcon(SpiderPanel.class.getResource("/resource/icon/16/spider.png")),
                extension);

        tabbedPane.addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        switch (tabbedPane.getSelectedIndex()) {
                            case 0:
                                getExportButton().setTable(getUrlsTable());
                                break;
                            case 1:
                                getExportButton().setTable(getAddedNodesTable());
                                break;
                            case 2:
                                getExportButton().setTable(getMessagesTable());
                                break;
                        }
                    }
                });

        this.extension = extension;
        this.setDefaultAccelerator(
                extension
                        .getView()
                        .getMenuShortcutKeyStroke(KeyEvent.VK_D, KeyEvent.SHIFT_DOWN_MASK, false));
        this.setMnemonic(Constant.messages.getChar("spider.panel.mnemonic"));
    }

    /**
     * This method initializes the working Panel.
     *
     * @return javax.swing.JScrollPane
     */
    @Override
    protected JPanel getWorkPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel(new BorderLayout());

            tabbedPane = new JTabbedPane();
            tabbedPane.addTab(
                    Constant.messages.getString("spider.panel.tab.urls"), getUrlsTableScrollPane());
            tabbedPane.addTab(
                    Constant.messages.getString("spider.panel.tab.addednodes"),
                    getAddedNodesTableScrollPane());
            tabbedPane.addTab(
                    Constant.messages.getString("spider.panel.tab.messages"),
                    getMessagesTableScrollPanel());
            tabbedPane.setSelectedIndex(0);

            mainPanel.add(tabbedPane);
        }
        return mainPanel;
    }

    private JScrollPane getUrlsTableScrollPane() {
        if (urlsTableScrollPane == null) {
            urlsTableScrollPane = new JScrollPane();
            urlsTableScrollPane.setName("SpiderUrlsPane");
            urlsTableScrollPane.setViewportView(getUrlsTable());
        }
        return urlsTableScrollPane;
    }

    private JScrollPane getAddedNodesTableScrollPane() {
        if (addedNodesTableScrollPane == null) {
            addedNodesTableScrollPane = new JScrollPane();
            addedNodesTableScrollPane.setName("SpiderAddedUrlsPane");
            addedNodesTableScrollPane.setViewportView(getAddedNodesTable());
        }
        return addedNodesTableScrollPane;
    }

    /**
     * Gets the scan results table.
     *
     * @return the scan results table
     */
    private JXTable getUrlsTable() {
        if (urlsTable == null) {
            // Create the table with a default, empty TableModel and the proper settings
            urlsTable = new ZapTable(EMPTY_URLS_TABLE_MODEL);
            urlsTable.setColumnSelectionAllowed(false);
            urlsTable.setCellSelectionEnabled(false);
            urlsTable.setRowSelectionAllowed(true);
            urlsTable.setAutoCreateRowSorter(true);

            urlsTable.setAutoCreateColumnsFromModel(false);
            urlsTable
                    .getColumnExt(0)
                    .setCellRenderer(
                            new DefaultTableRenderer(
                                    new MappedValue(StringValues.EMPTY, IconValues.NONE),
                                    JLabel.CENTER));
            urlsTable.getColumnExt(0).setHighlighters(new ProcessedCellItemIconHighlighter(0));

            urlsTable.getColumnModel().getColumn(0).setMinWidth(80);
            urlsTable.getColumnModel().getColumn(0).setPreferredWidth(90); // processed

            urlsTable.getColumnModel().getColumn(1).setMinWidth(60);
            urlsTable.getColumnModel().getColumn(1).setPreferredWidth(70); // method

            urlsTable.getColumnModel().getColumn(2).setMinWidth(300); // name

            urlsTable.getColumnModel().getColumn(3).setMinWidth(50);
            urlsTable.getColumnModel().getColumn(3).setPreferredWidth(250); // flags

            urlsTable.setName(PANEL_NAME);
            urlsTable.setDoubleBuffered(true);
            urlsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            urlsTable.setComponentPopupMenu(
                    new JPopupMenu() {

                        private static final long serialVersionUID = 6608291059686282641L;

                        @Override
                        public void show(Component invoker, int x, int y) {
                            View.getSingleton().getPopupMenu().show(invoker, x, y);
                        }
                    });
        }
        return urlsTable;
    }

    private JXTable getAddedNodesTable() {
        if (addedNodesTable == null) {
            // Create the table with a default, empty TableModel and the proper settings
            addedNodesTable = new ZapTable(EMPTY_URLS_NO_FLAGS_TABLE_MODEL);
            addedNodesTable.setColumnSelectionAllowed(false);
            addedNodesTable.setCellSelectionEnabled(false);
            addedNodesTable.setRowSelectionAllowed(true);
            addedNodesTable.setAutoCreateRowSorter(true);

            addedNodesTable.setAutoCreateColumnsFromModel(false);
            addedNodesTable
                    .getColumnExt(0)
                    .setCellRenderer(
                            new DefaultTableRenderer(
                                    new MappedValue(StringValues.EMPTY, IconValues.NONE),
                                    JLabel.CENTER));
            addedNodesTable
                    .getColumnExt(0)
                    .setHighlighters(new ProcessedCellItemIconHighlighter(0));

            addedNodesTable.getColumnModel().getColumn(0).setMinWidth(80);
            addedNodesTable.getColumnModel().getColumn(0).setPreferredWidth(90); // processed

            addedNodesTable.getColumnModel().getColumn(1).setMinWidth(60);
            addedNodesTable.getColumnModel().getColumn(1).setPreferredWidth(70); // method

            addedNodesTable.getColumnModel().getColumn(2).setMinWidth(400); // name
            addedNodesTable.getColumnModel().getColumn(2).setPreferredWidth(1000);

            addedNodesTable.setName(ADDED_NODES_CONTAINER_NAME);
            addedNodesTable.setDoubleBuffered(true);
            addedNodesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            addedNodesTable.setComponentPopupMenu(
                    new JPopupMenu() {

                        private static final long serialVersionUID = 6608291059686282641L;

                        @Override
                        public void show(Component invoker, int x, int y) {
                            View.getSingleton().getPopupMenu().show(invoker, x, y);
                        }
                    });
        }
        return addedNodesTable;
    }

    private JScrollPane getMessagesTableScrollPanel() {
        if (messagesTableScrollPane == null) {
            messagesTableScrollPane = new JScrollPane();
            messagesTableScrollPane.setName("SpiderMessagesPane");
            messagesTableScrollPane.setViewportView(getMessagesTable());
        }
        return messagesTableScrollPane;
    }

    private SpiderMessagesTable getMessagesTable() {
        if (messagesTable == null) {
            messagesTable = new SpiderMessagesTable(EMPTY_MESSAGES_TABLE_MODEL);
            messagesTable.setName(HTTP_MESSAGE_CONTAINER_NAME);
        }
        return messagesTable;
    }

    /**
     * Gets the label storing the name of the count of found URIs.
     *
     * @return the found count name label
     */
    private JLabel getFoundCountNameLabel() {
        if (foundCountNameLabel == null) {
            foundCountNameLabel = new JLabel();
            foundCountNameLabel.setText(Constant.messages.getString("spider.toolbar.found.label"));
        }
        return foundCountNameLabel;
    }

    /**
     * Gets the label storing the value for count of found URIs.
     *
     * @return the found count value label
     */
    private JLabel getFoundCountValueLabel() {
        if (foundCountValueLabel == null) {
            foundCountValueLabel = new JLabel();
            foundCountValueLabel.setText(ZERO_REQUESTS_LABEL_TEXT);
        }
        return foundCountValueLabel;
    }

    /**
     * Gets the label storing the name of the count of added URIs.
     *
     * @return the found count name label
     */
    private JLabel getAddedCountNameLabel() {
        if (addedCountNameLabel == null) {
            addedCountNameLabel = new JLabel();
            addedCountNameLabel.setText(Constant.messages.getString("spider.toolbar.added.label"));
        }
        return addedCountNameLabel;
    }

    /**
     * Gets the label storing the value for count of added URIs.
     *
     * @return the added count value label
     */
    private JLabel getAddedCountValueLabel() {
        if (addedCountValueLabel == null) {
            addedCountValueLabel = new JLabel();
            addedCountValueLabel.setText(ZERO_REQUESTS_LABEL_TEXT);
        }
        return addedCountValueLabel;
    }

    @Override
    protected int addToolBarElements(JToolBar toolBar, Location location, int gridX) {
        if (ScanPanel2.Location.afterProgressBar == location) {
            toolBar.add(new JToolBar.Separator(), getGBC(gridX++, 0));
            toolBar.add(getFoundCountNameLabel(), getGBC(gridX++, 0));
            toolBar.add(getFoundCountValueLabel(), getGBC(gridX++, 0));

            toolBar.add(new JToolBar.Separator(), getGBC(gridX++, 0));
            toolBar.add(getAddedCountNameLabel(), getGBC(gridX++, 0));
            toolBar.add(getAddedCountValueLabel(), getGBC(gridX++, 0));

            toolBar.add(new JToolBar.Separator(), getGBC(gridX++, 0));
            toolBar.add(getExportButton(), getGBC(gridX++, 0));
        }
        return gridX;
    }

    private TableExportButton<JXTable> getExportButton() {
        if (exportButton == null) {
            exportButton = new TableExportButton<>(getUrlsTable());
        }
        return exportButton;
    }

    /** Update the count of found URIs. */
    protected void updateFoundCount() {
        SpiderScan sc = this.getSelectedScanner();
        if (sc != null) {
            this.getFoundCountValueLabel().setText(Integer.toString(sc.getNumberOfURIsFound()));
        } else {
            this.getFoundCountValueLabel().setText(ZERO_REQUESTS_LABEL_TEXT);
        }
    }

    /** Update the count of added nodes. */
    protected void updateAddedCount() {
        SpiderScan sc = this.getSelectedScanner();
        if (sc != null) {
            this.getAddedCountValueLabel().setText(Integer.toString(sc.getNumberOfNodesAdded()));
        } else {
            this.getAddedCountValueLabel().setText(ZERO_REQUESTS_LABEL_TEXT);
        }
    }

    @Override
    protected void switchView(final SpiderScan scanner) {
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
                log.error("Failed to switch view: {}", e.getMessage(), e);
            }
            return;
        }
        if (scanner != null) {
            getUrlsTable().setModel(scanner.getResultsTableModel());
            getMessagesTable().setModel(scanner.getMessagesTableModel());
            getAddedNodesTable().setModel(scanner.getAddedNodesTableModel());
        } else {
            getUrlsTable().setModel(EMPTY_URLS_TABLE_MODEL);
            getMessagesTable().setModel(EMPTY_MESSAGES_TABLE_MODEL);
            getAddedNodesTable().setModel(EMPTY_URLS_TABLE_MODEL);
        }
        this.updateFoundCount();
        this.updateAddedCount();
    }

    @Override
    public JButton getNewScanButton() {
        if (scanButton == null) {
            scanButton = new JButton(Constant.messages.getString("spider.toolbar.button.new"));
            scanButton.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    SpiderPanel.class.getResource(
                                            "/resource/icon/16/spider.png"))));
            scanButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            extension.showSpiderDialog(getSiteTreeTarget());
                        }
                    });
        }
        return scanButton;
    }

    @Override
    protected int getNumberOfScansToShow() {
        return extension.getSpiderParam().getMaxScansInUI();
    }

    /**
     * A {@link org.jdesktop.swingx.decorator.Highlighter Highlighter} for a column that indicates,
     * using icons, whether or not an entry was processed, that is, is or not in scope.
     *
     * <p>The expected type/class of the cell values is {@code Boolean}.
     */
    private static class ProcessedCellItemIconHighlighter
            extends AbstractTableCellItemIconHighlighter {

        /** The icon that indicates the entry was processed. */
        private static final ImageIcon PROCESSED_ICON =
                new ImageIcon(SpiderPanelTableModel.class.getResource("/resource/icon/16/152.png"));

        /** The icon that indicates the entry was not processed. */
        private static final ImageIcon NOT_PROCESSED_ICON =
                new ImageIcon(SpiderPanelTableModel.class.getResource("/resource/icon/16/149.png"));

        public ProcessedCellItemIconHighlighter(final int columnIndex) {
            super(columnIndex);
        }

        @Override
        protected Icon getIcon(final Object cellItem) {
            return getProcessedIcon((Boolean) cellItem);
        }

        private static Icon getProcessedIcon(final boolean processed) {
            return processed ? PROCESSED_ICON : NOT_PROCESSED_ICON;
        }

        @Override
        protected boolean isHighlighted(final Object cellItem) {
            return true;
        }
    }

    private SiteNode getSiteTreeTarget() {
        if (!extension.getView().getSiteTreePanel().getTreeSite().isSelectionEmpty()) {
            return (SiteNode)
                    extension
                            .getView()
                            .getSiteTreePanel()
                            .getTreeSite()
                            .getSelectionPath()
                            .getLastPathComponent();
        }
        return null;
    }
}
