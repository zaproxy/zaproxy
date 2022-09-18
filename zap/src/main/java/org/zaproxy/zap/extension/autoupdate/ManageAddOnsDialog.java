/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.ScrollableSizeHint;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.IconHighlighter;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;
import org.jdesktop.swingx.renderer.IconAware;
import org.jdesktop.swingx.renderer.IconValues;
import org.jdesktop.swingx.renderer.MappedValue;
import org.jdesktop.swingx.renderer.StringValues;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.view.AbstractFrame;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.AddOnCollection;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapLabel;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.ZapTable;
import org.zaproxy.zap.view.panels.TableFilterPanel;

@SuppressWarnings("serial")
public class ManageAddOnsDialog extends AbstractFrame implements CheckForUpdateCallback {

    protected enum State {
        IDLE,
        DOWNLOADING_ZAP,
        DOWNLOADED_ZAP,
        DOWNLOADING_UPDATES,
        DOWNLOADED_UPDATES
    }

    static final Icon ICON_ADD_ON_ISSUES =
            new ImageIcon(InstalledAddOnsTableModel.class.getResource("/resource/icon/16/050.png"));
    static final Icon ICON_ADD_ON_EXTENSION_ISSUES =
            new ImageIcon(
                    InstalledAddOnsTableModel.class.getResource(
                            "/resource/icon/fugue/information-white.png"));

    private static final String RETRIEVE_PANEL = "RetrievePanel";
    private static final String MARKETPLACE_PANEL = "MarketplacePanel";
    private static final double ADD_ON_DETAILS_RESIZE_WEIGHT = 0.7D;

    private static final String ADD_ON_MANDATORY =
            Constant.messages.getString("cfu.table.mandatory.value.yes");
    private static final String ADD_ON_NOT_MANDATORY =
            Constant.messages.getString("cfu.table.mandatory.value.no");

    private static final Logger logger = LogManager.getLogger(ManageAddOnsDialog.class);
    private static final long serialVersionUID = 1L;
    private JTabbedPane jTabbed = null;
    private JPanel topPanel = null;
    private JPanel installedPanel = null;
    private JPanel browsePanel = null;
    private JPanel corePanel = null;
    private JPanel installedAddOnsPanel = null;
    private JPanel installedAddOnsFilterPanel = null;
    private JPanel uninstalledAddOnsPanel = null;
    private JPanel marketplacePanel;
    private CardLayout marketplaceCardLayout;
    private JPanel retrievePanel = null;

    private JButton addOnInfoButton = null;
    private JButton coreNotesButton = null;
    private JButton downloadZapButton = null;
    private JButton checkForUpdatesButton = null;
    private JButton updateButton = null;
    private JButton updateAllButton = null;
    private JButton uninstallButton = null;
    private JButton installAllButton;
    private JButton installButton = null;
    private JButton close1Button = null;
    private JButton close2Button = null;

    private JLabel downloadProgress = null;
    private JLabel updatesMessage = null;

    private ZapTable installedAddOnsTable = null;
    private ZapTable uninstalledAddOnsTable = null;

    // private ZapRelease latestRelease = null;
    private String currentVersion = null;
    private AddOnCollection latestInfo = null;
    private AddOnCollection prevInfo = null;
    private ExtensionAutoUpdate extension = null;
    private AddOnCollection installedAddOns;
    private final InstalledAddOnsTableModel installedAddOnsModel;
    private final UninstalledAddOnsTableModel uninstalledAddOnsModel;

    private State state = null;

    /** @throws HeadlessException */
    public ManageAddOnsDialog(
            ExtensionAutoUpdate ext, String currentVersion, AddOnCollection installedAddOns)
            throws HeadlessException {
        super();
        this.extension = ext;
        this.currentVersion = currentVersion;
        this.installedAddOns = installedAddOns;

        installedAddOnsModel = new InstalledAddOnsTableModel(installedAddOns);
        uninstalledAddOnsModel = new UninstalledAddOnsTableModel(installedAddOns);

        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setTitle(Constant.messages.getString("cfu.manage.title"));
        // this.setContentPane(getJTabbed());
        this.setContentPane(getTopPanel());
        this.pack();
        centerFrame();
        state = State.IDLE;

        // Handle escape key to close the dialog
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction =
                new AbstractAction() {
                    private static final long serialVersionUID = 3516424501887406165L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispatchEvent(
                                new WindowEvent(
                                        ManageAddOnsDialog.this, WindowEvent.WINDOW_CLOSING));
                    }
                };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);
    }

    private JPanel getTopPanel() {
        if (topPanel == null) {
            topPanel = new JPanel();
            topPanel.setLayout(new GridBagLayout());
            topPanel.add(getJTabbed(), LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D));
            topPanel.add(this.getUpdatesMessage(), LayoutHelper.getGBC(0, 2, 1, 1.0D));
        }
        return topPanel;
    }

    private JTabbedPane getJTabbed() {
        if (jTabbed == null) {
            jTabbed = new JTabbedPane();
            jTabbed.addTab(
                    Constant.messages.getString("cfu.tab.installed"), this.getInstalledPanel());
            jTabbed.addTab(Constant.messages.getString("cfu.tab.browse"), this.getBrowsePanel());
        }
        return jTabbed;
    }

    protected void selectMarketplaceTab() {
        getJTabbed().setSelectedIndex(1);
    }

    private JPanel getInstalledPanel() {
        if (installedPanel == null) {
            installedPanel = new JPanel();
            installedPanel.setLayout(new GridBagLayout());
            installedPanel.add(getCorePanel(true), LayoutHelper.getGBC(0, 0, 1, 1.0D, 0.0D));
            installedPanel.add(getInstalledAddOnsPanel(), LayoutHelper.getGBC(0, 1, 1, 1.0D, 1.0D));
        }
        return installedPanel;
    }

    private JPanel getBrowsePanel() {
        if (browsePanel == null) {
            browsePanel = new JPanel();
            browsePanel.setLayout(new GridBagLayout());
            browsePanel.add(getUninstalledAddOnsPanel(), LayoutHelper.getGBC(0, 0, 1, 1.0D, 1.0D));
        }
        return browsePanel;
    }

    private JPanel getCorePanel(boolean update) {
        boolean refresh = true;
        if (corePanel == null) {
            corePanel = new JPanel();
            corePanel.setLayout(new GridBagLayout());
            corePanel.setBorder(
                    BorderFactory.createTitledBorder(
                            null,
                            Constant.messages.getString("cfu.label.zap.border"),
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));

            if (latestInfo == null || this.latestInfo.getZapRelease() == null) {
                // Haven't checked for updates yet
                corePanel.add(new JLabel(this.currentVersion), LayoutHelper.getGBC(0, 0, 1, 0.0D));
                corePanel.add(new JLabel(""), LayoutHelper.getGBC(1, 0, 1, 1.0D));
                corePanel.add(this.getCheckForUpdatesButton(), LayoutHelper.getGBC(2, 0, 1, 0.0D));
                refresh = false;
            }
        } else if (latestInfo != null && this.latestInfo.getZapRelease() != null) {
            if (update) {
                corePanel.removeAll();
            }
        } else {
            refresh = false;
        }
        if (refresh) {
            if (this.latestInfo.getZapRelease().isNewerThan(this.currentVersion)) {
                addNewerVersionComponents(corePanel);
            } else {
                corePanel.add(
                        new JLabel(
                                Constant.messages.getString(
                                        "cfu.check.zap.latest", this.currentVersion)),
                        LayoutHelper.getGBC(0, 0, 1, 1.0D));
            }
            installedPanel.validate();
        }

        return corePanel;
    }

    private void addNewerVersionComponents(JPanel panel) {
        int x = 0;
        panel.add(
                new JLabel(
                        Constant.messages.getString(
                                "cfu.check.zap.newer",
                                this.latestInfo.getZapRelease().getVersion())),
                LayoutHelper.getGBC(x, 0, 1, 0.0D));
        panel.add(new JLabel(""), LayoutHelper.getGBC(++x, 0, 1, 0.8D));
        panel.add(this.getDownloadProgress(), LayoutHelper.getGBC(++x, 0, 1, 0.2D));
        if (!Constant.isDailyBuild()) {
            panel.add(this.getCoreNotesButton(), LayoutHelper.getGBC(++x, 0, 1, 0.0D));
        }
        panel.add(this.getDownloadZapButton(), LayoutHelper.getGBC(++x, 0, 1, 0.0D));
    }

    private JPanel getInstalledAddOnsPanel() {
        if (installedAddOnsPanel == null) {

            installedAddOnsPanel = new JPanel();
            installedAddOnsPanel.setLayout(new GridBagLayout());
            installedAddOnsPanel.setBorder(
                    BorderFactory.createTitledBorder(
                            null,
                            Constant.messages.getString("cfu.label.addons.border"),
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));

            getInstalledAddOnsTable();

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(
                    javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setViewportView(getInstalledAddOnsTable());

            installedAddOnsFilterPanel = new TableFilterPanel<>(getInstalledAddOnsTable());

            AddOnDetailsPanel addonDetailsPanel = new AddOnDetailsPanel();
            getInstalledAddOnsTable()
                    .getSelectionModel()
                    .addListSelectionListener(
                            e -> {
                                if (e.getValueIsAdjusting()) {
                                    return;
                                }

                                int selectedRow = getInstalledAddOnsTable().getSelectedRow();
                                if (selectedRow != -1) {
                                    AddOnWrapper aow =
                                            (AddOnWrapper)
                                                    installedAddOnsModel.getValueAt(
                                                            getInstalledAddOnsTable()
                                                                    .convertRowIndexToModel(
                                                                            selectedRow),
                                                            AddOnsTableModel.COLUMN_ADD_ON_WRAPPER);
                                    AddOn addOn;
                                    if (AddOn.InstallationStatus.UNINSTALLATION_FAILED
                                                    == aow.getInstallationStatus()
                                            || AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED
                                                    == aow.getInstallationStatus()) {
                                        addOn = aow.getAddOn();
                                    } else {
                                        addOn =
                                                aow.getAddOnUpdate() != null
                                                        ? aow.getAddOnUpdate()
                                                        : aow.getAddOn();
                                    }

                                    addonDetailsPanel.setDetails(addOn);
                                } else {
                                    addonDetailsPanel.clearDetails();
                                }
                            });

            int row = 0;
            installedAddOnsPanel.add(
                    installedAddOnsFilterPanel, LayoutHelper.getGBC(0, row++, 5, 0.0D));
            JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            split.setTopComponent(scrollPane);
            split.setBottomComponent(addonDetailsPanel);
            split.setResizeWeight(ADD_ON_DETAILS_RESIZE_WEIGHT);
            installedAddOnsPanel.add(split, LayoutHelper.getGBC(0, row++, 5, 1.0D, 1.0D));
            installedAddOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(0, row, 1, 1.0D));
            installedAddOnsPanel.add(getUninstallButton(), LayoutHelper.getGBC(1, row, 1, 0.0D));
            installedAddOnsPanel.add(getUpdateButton(), LayoutHelper.getGBC(2, row, 1, 0.0D));
            installedAddOnsPanel.add(getUpdateAllButton(), LayoutHelper.getGBC(3, row, 1, 0.0D));
            installedAddOnsPanel.add(getClose1Button(), LayoutHelper.getGBC(4, row, 1, 0.0D));
        }
        return installedAddOnsPanel;
    }

    private JPanel getUninstalledAddOnsPanel() {
        if (uninstalledAddOnsPanel == null) {

            uninstalledAddOnsPanel = new JPanel();
            uninstalledAddOnsPanel.setLayout(new GridBagLayout());
            uninstalledAddOnsPanel.setBorder(
                    BorderFactory.createTitledBorder(
                            null,
                            Constant.messages.getString("cfu.label.addons.border"),
                            TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));

            if (latestInfo != null) {
                getMarketplaceCardLayout().show(getMarketplacePanel(), MARKETPLACE_PANEL);
            }

            int row = 0;
            int column = 0;
            uninstalledAddOnsPanel.add(
                    getMarketplacePanel(), LayoutHelper.getGBC(column, row++, 5, 1.0D, 1.0D));
            if (Constant.isDevMode()) {
                uninstalledAddOnsPanel.add(
                        getInstallAllButton(), LayoutHelper.getGBC(column++, row, 1, 0.0D));
            }
            uninstalledAddOnsPanel.add(new JLabel(""), LayoutHelper.getGBC(column++, row, 1, 1.0D));
            uninstalledAddOnsPanel.add(
                    getInstallButton(), LayoutHelper.getGBC(column++, row, 1, 0.0D));
            uninstalledAddOnsPanel.add(
                    getAddOnInfoButton(), LayoutHelper.getGBC(column++, row, 1, 0.0D));
            uninstalledAddOnsPanel.add(
                    getClose2Button(), LayoutHelper.getGBC(column, row, 1, 0.0D));
        }
        return uninstalledAddOnsPanel;
    }

    private JPanel getMarketplacePanel() {
        if (marketplacePanel == null) {
            marketplacePanel = new JPanel(getMarketplaceCardLayout());
            JSplitPane marketplaceSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            JScrollPane uninstalledAddOnsScrollPane = new JScrollPane(getUninstalledAddOnsTable());
            uninstalledAddOnsScrollPane.setHorizontalScrollBarPolicy(
                    javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            marketplaceSplitPane.setTopComponent(uninstalledAddOnsScrollPane);
            AddOnDetailsPanel addonDetailsPanel = new AddOnDetailsPanel();
            getUninstalledAddOnsTable()
                    .getSelectionModel()
                    .addListSelectionListener(
                            e -> {
                                if (e.getValueIsAdjusting()) {
                                    return;
                                }

                                int selectedRow = getUninstalledAddOnsTable().getSelectedRow();
                                if (selectedRow != -1) {
                                    addonDetailsPanel.setDetails(
                                            ((AddOnWrapper)
                                                            uninstalledAddOnsModel.getValueAt(
                                                                    getUninstalledAddOnsTable()
                                                                            .convertRowIndexToModel(
                                                                                    selectedRow),
                                                                    AddOnsTableModel
                                                                            .COLUMN_ADD_ON_WRAPPER))
                                                    .getAddOn());
                                } else {
                                    addonDetailsPanel.clearDetails();
                                }
                            });
            marketplaceSplitPane.setBottomComponent(addonDetailsPanel);
            marketplaceSplitPane.setResizeWeight(ADD_ON_DETAILS_RESIZE_WEIGHT);

            JPanel addOnsPanel = new JPanel(new BorderLayout());
            addOnsPanel.add(
                    new TableFilterPanel<>(getUninstalledAddOnsTable()), BorderLayout.PAGE_START);
            addOnsPanel.add(marketplaceSplitPane, BorderLayout.CENTER);

            marketplacePanel.add(getRetrievePanel(), RETRIEVE_PANEL);
            marketplacePanel.add(addOnsPanel, MARKETPLACE_PANEL);
        }
        return marketplacePanel;
    }

    private CardLayout getMarketplaceCardLayout() {
        if (marketplaceCardLayout == null) {
            marketplaceCardLayout = new CardLayout();
        }
        return marketplaceCardLayout;
    }

    private JPanel getRetrievePanel() {
        if (retrievePanel == null) {
            retrievePanel = new JPanel();
            retrievePanel.setLayout(new GridBagLayout());

            JButton retrieveButton = new JButton();
            retrieveButton.setText(Constant.messages.getString("cfu.button.checkForUpdates"));

            retrieveButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            checkForUpdates(false);
                        }
                    });
            retrievePanel.add(new JLabel(""), LayoutHelper.getGBC(0, 0, 1, 1.0D));
            retrievePanel.add(retrieveButton, LayoutHelper.getGBC(1, 0, 1, 0.0D));
            retrievePanel.add(new JLabel(""), LayoutHelper.getGBC(2, 0, 1, 1.0D));
            retrievePanel.add(new JLabel(""), LayoutHelper.getGBC(0, 1, 3, 1.0D, 1.0D));
        }
        return retrievePanel;
    }

    protected void setPreviousVersionInfo(AddOnCollection prevInfo) {
        this.prevInfo = prevInfo;
    }

    protected void setLatestVersionInfo(AddOnCollection latestInfo) {
        this.latestInfo = latestInfo;
        getCorePanel(true);

        if (latestInfo != null) {
            installedAddOnsModel.setAvailableAddOns(latestInfo);
            uninstalledAddOnsModel.setAddOnCollection(latestInfo);

            List<AddOn> addOnsNotInstalled = installedAddOnsModel.updateEntries();
            uninstalledAddOnsModel.setAddOns(addOnsNotInstalled, prevInfo);
        }
        getMarketplaceCardLayout().show(getMarketplacePanel(), MARKETPLACE_PANEL);
    }

    private ZapTable getInstalledAddOnsTable() {
        if (installedAddOnsTable == null) {
            installedAddOnsTable = createCustomZapTable();
            installedAddOnsModel.addTableModelListener(
                    new TableModelListener() {
                        @Override
                        public void tableChanged(TableModelEvent e) {
                            getUpdateButton().setEnabled(installedAddOnsModel.canUpdateSelected());
                            getUpdateAllButton()
                                    .setEnabled(installedAddOnsModel.getAllUpdates().size() > 0);
                            getUninstallButton()
                                    .setEnabled(installedAddOnsModel.canUninstallSelected());
                        }
                    });

            installedAddOnsTable.setModel(installedAddOnsModel);
            installedAddOnsTable.getColumnModel().getColumn(0).setMaxWidth(20); // icon
            installedAddOnsTable
                    .getColumnExt(0)
                    .setSortable(false); // icon doesn't need to be sortable
            installedAddOnsTable.getColumnModel().getColumn(1).setPreferredWidth(200); // name
            installedAddOnsTable.getColumnModel().getColumn(2).setPreferredWidth(60); // version
            installedAddOnsTable
                    .getColumnExt(2)
                    .setSortable(false); // version doesn't need to be sortable
            installedAddOnsTable
                    .getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(400); // description
            installedAddOnsTable
                    .getColumnExt(3)
                    .setSortable(false); // description doesn't need to be sortable
            installedAddOnsTable.getColumnModel().getColumn(4).setPreferredWidth(60); // update
            installedAddOnsTable
                    .getColumnExt(4)
                    .setSortable(false); // update doesn't need to be sortable
            installedAddOnsTable.getColumnModel().getColumn(5).setPreferredWidth(40);
            installedAddOnsTable
                    .getColumnExt(5)
                    .setSortable(false); // checkbox doesn't need to be sortable

            // Default sort by name (column 1)
            List<RowSorter.SortKey> sortKeys = new ArrayList<>(1);
            sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
            installedAddOnsTable.getRowSorter().setSortKeys(sortKeys);

            installedAddOnsTable
                    .getColumnExt(0)
                    .setCellRenderer(
                            new DefaultTableRenderer(
                                    new MappedValue(StringValues.EMPTY, IconValues.NONE),
                                    JLabel.CENTER));
            installedAddOnsTable
                    .getColumnExt(0)
                    .setHighlighters(
                            new CompoundHighlighter(
                                    new WarningRunningIssuesHighlighter(
                                            AddOnsTableModel.COLUMN_ADD_ON_WRAPPER),
                                    new WarningRunningIssuesToolTipHighlighter(
                                            AddOnsTableModel.COLUMN_ADD_ON_WRAPPER)));
            installedAddOnsTable
                    .getColumnExt(3)
                    .setHighlighters(
                            new CompoundHighlighter(
                                    new WarningUpdateIssuesHighlighter(
                                            AddOnsTableModel.COLUMN_ADD_ON_WRAPPER),
                                    new WarningUpdateIssuesToolTipHighlighter(
                                            AddOnsTableModel.COLUMN_ADD_ON_WRAPPER)));
            installedAddOnsTable
                    .getColumnExt(4)
                    .addHighlighter(
                            new DisableSelectionHighlighter(
                                    AddOnsTableModel.COLUMN_ADD_ON_WRAPPER));
        }

        return installedAddOnsTable;
    }

    private static ZapTable createCustomZapTable() {
        ZapTable zapTable =
                new ZapTable() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected AutoScrollAction createAutoScrollAction() {
                        return null;
                    }
                };
        zapTable.setAutoScrollOnNewValues(false);
        return zapTable;
    }

    private ZapTable getUninstalledAddOnsTable() {
        if (uninstalledAddOnsTable == null) {
            uninstalledAddOnsTable = createCustomZapTable();

            uninstalledAddOnsModel.addTableModelListener(
                    new TableModelListener() {
                        @Override
                        public void tableChanged(TableModelEvent e) {
                            getInstallButton()
                                    .setEnabled(uninstalledAddOnsModel.canIinstallSelected());
                            getInstallAllButton()
                                    .setEnabled(uninstalledAddOnsModel.hasAvailableAddOns());
                        }
                    });

            uninstalledAddOnsTable
                    .getSelectionModel()
                    .addListSelectionListener(
                            new ListSelectionListener() {
                                @Override
                                public void valueChanged(ListSelectionEvent e) {
                                    getAddOnInfoButton().setEnabled(false);
                                    if (DesktopUtils.canOpenUrlInBrowser()
                                            && getUninstalledAddOnsTable().getSelectedRowCount()
                                                    == 1) {
                                        // convertRowIndexToModel in-case they sorted
                                        AddOnWrapper ao =
                                                uninstalledAddOnsModel.getAddOnWrapper(
                                                        getUninstalledAddOnsTable()
                                                                .convertRowIndexToModel(
                                                                        getUninstalledAddOnsTable()
                                                                                .getSelectedRow()));
                                        if (ao != null && ao.getAddOn().getInfo() != null) {
                                            getAddOnInfoButton().setEnabled(true);
                                        }
                                    }
                                }
                            });

            uninstalledAddOnsTable.setModel(uninstalledAddOnsModel);

            uninstalledAddOnsTable.getColumnModel().getColumn(0).setMaxWidth(20); // Icon
            uninstalledAddOnsTable.getColumnExt(0).setSortable(false); // Icon doesn't need sorting
            uninstalledAddOnsTable.getColumnModel().getColumn(1).setPreferredWidth(50); // Status
            uninstalledAddOnsTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Name
            uninstalledAddOnsTable
                    .getColumnModel()
                    .getColumn(3)
                    .setPreferredWidth(300); // Description
            uninstalledAddOnsTable
                    .getColumnExt(3)
                    .setSortable(false); // Description doesn't need sorting
            uninstalledAddOnsTable
                    .getColumnModel()
                    .getColumn(4)
                    .setPreferredWidth(60); // Update (version number)
            uninstalledAddOnsTable
                    .getColumnExt(4)
                    .setSortable(false); // Update doesn't need sorting
            uninstalledAddOnsTable.getColumnModel().getColumn(5).setPreferredWidth(40); // Checkbox
            uninstalledAddOnsTable
                    .getColumnExt(5)
                    .setSortable(false); // Checkbox doesn't need sorting

            // Default sort by status (column 1) descending (Release, Beta, Alpha), and name (column
            // 2) ascending
            List<RowSorter.SortKey> sortKeys = new ArrayList<>(2);
            sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
            sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
            uninstalledAddOnsTable.getRowSorter().setSortKeys(sortKeys);

            uninstalledAddOnsTable
                    .getColumnExt(0)
                    .setCellRenderer(
                            new DefaultTableRenderer(
                                    new MappedValue(StringValues.EMPTY, IconValues.NONE),
                                    JLabel.CENTER));
            uninstalledAddOnsTable
                    .getColumnExt(0)
                    .setHighlighters(
                            new CompoundHighlighter(
                                    new WarningRunningIssuesHighlighter(
                                            AddOnsTableModel.COLUMN_ADD_ON_WRAPPER),
                                    new WarningRunningIssuesToolTipHighlighter(
                                            UninstalledAddOnsTableModel.COLUMN_ADD_ON_WRAPPER)));
            uninstalledAddOnsTable
                    .getColumnExt(5)
                    .addHighlighter(
                            new DisableSelectionHighlighter(
                                    UninstalledAddOnsTableModel.COLUMN_ADD_ON_WRAPPER));
        }
        return uninstalledAddOnsTable;
    }

    private JLabel getUpdatesMessage() {
        if (this.updatesMessage == null) {
            this.updatesMessage = new JLabel(" ");
        }
        return this.updatesMessage;
    }

    private JButton getCoreNotesButton() {
        if (coreNotesButton == null) {
            coreNotesButton = new JButton();
            coreNotesButton.setIcon(
                    new ImageIcon(
                            ManageAddOnsDialog.class.getResource(
                                    "/resource/icon/16/022.png"))); // 'Text file' icon
            coreNotesButton.setToolTipText(Constant.messages.getString("cfu.button.zap.relnotes"));
            final ManageAddOnsDialog dialog = this;
            coreNotesButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            URL url = latestInfo.getZapRelease().getReleaseNotesUrl();
                            if (url != null && DesktopUtils.canOpenUrlInBrowser()) {
                                if (DesktopUtils.openUrlInBrowser(url.toString())) {
                                    // It worked :)
                                    return;
                                }
                            }

                            StringBuilder sb = new StringBuilder();
                            sb.append("<html>");
                            sb.append(
                                    Constant.messages.getString(
                                            "cfu.title.relnotes",
                                            latestInfo.getZapRelease().getVersion()));

                            // Reformat the notes into html - the leading and trailing whitespace
                            // does need to be removed for some reason
                            String[] strs =
                                    latestInfo.getZapRelease().getReleaseNotes().split("\n");
                            for (String s : strs) {
                                sb.append(s.replace("&lt;", "<").trim());
                            }
                            sb.append("</html>");

                            View.getSingleton().showMessageDialog(dialog, sb.toString());
                        }
                    });
        }
        return coreNotesButton;
    }

    private JButton getClose1Button() {
        if (close1Button == null) {
            close1Button = new JButton();
            close1Button.setText(Constant.messages.getString("all.button.close"));
            close1Button.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            ManageAddOnsDialog.this.dispose();
                        }
                    });
        }
        return close1Button;
    }

    private JButton getClose2Button() {
        if (close2Button == null) {
            close2Button = new JButton();
            close2Button.setText(Constant.messages.getString("all.button.close"));
            close2Button.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            ManageAddOnsDialog.this.dispose();
                        }
                    });
        }
        return close2Button;
    }

    private JLabel getDownloadProgress() {
        if (downloadProgress == null) {
            downloadProgress = new JLabel("");
        }
        return downloadProgress;
    }

    private JButton getCheckForUpdatesButton() {
        if (checkForUpdatesButton == null) {
            checkForUpdatesButton = new JButton();
            checkForUpdatesButton.setText(
                    Constant.messages.getString("cfu.button.checkForUpdates"));
            checkForUpdatesButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            checkForUpdates(false);
                        }
                    });
        }
        return checkForUpdatesButton;
    }

    protected void checkForUpdates(boolean force) {
        this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        extension.getLatestVersionInfo(this, force);
        this.setCursor(Cursor.getDefaultCursor());
    }

    private JButton getDownloadZapButton() {
        if (downloadZapButton == null) {
            downloadZapButton = new JButton();
            if (Constant.isKali()) {
                getDownloadZapButton()
                        .setText(Constant.messages.getString("cfu.button.zap.options"));
            } else {
                downloadZapButton.setText(Constant.messages.getString("cfu.button.zap.download"));
            }
            downloadZapButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            if (extension.downloadLatestRelease()) {
                                setDownloadingZap();
                            }
                        }
                    });
        }
        return downloadZapButton;
    }

    protected void setDownloadingZap() {
        downloadZapButton.setEnabled(false);
        getUpdateButton().setEnabled(false); // Makes things less complicated
        getUpdateAllButton().setEnabled(false);
        state = State.DOWNLOADING_ZAP;
        getUpdatesMessage().setText(Constant.messages.getString("cfu.check.zap.downloading"));
    }

    protected void setDownloadingUpdates() {
        if (EventQueue.isDispatchThread()) {
            this.getDownloadZapButton().setEnabled(false); // Makes things less complicated
            this.getUpdateButton().setEnabled(false);
            this.getUpdateAllButton().setEnabled(false);
            this.state = State.DOWNLOADING_UPDATES;
            this.getUpdatesMessage()
                    .setText(Constant.messages.getString("cfu.check.upd.downloading"));
        } else {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            setDownloadingUpdates();
                        }
                    });
        }
    }

    /**
     * Notifies that the given {@code addOn} is being downloaded.
     *
     * @param addOn the add-on that is being downloaded
     * @since 2.4.0
     */
    public void notifyAddOnDownloading(AddOn addOn) {
        if (installedAddOnsModel.notifyAddOnDownloading(addOn)) {
            // It's an update...
            return;
        }

        uninstalledAddOnsModel.notifyAddOnDownloading(addOn);
    }

    /**
     * Notifies that the download of the add-on with the given {@code url} as failed.
     *
     * <p>The entry of the add-on is updated to report that the download failed.
     *
     * @param url the URL of the add-on that was being downloaded
     * @since 2.4.0
     */
    public void notifyAddOnDownloadFailed(String url) {
        if (installedAddOnsModel.notifyAddOnDownloadFailed(url)) {
            // It's an update...
            return;
        }

        uninstalledAddOnsModel.notifyAddOnDownloadFailed(url);
    }

    /**
     * Notifies that the given {@code addOn} was installed. The add-on is added to the table of
     * installed add-ons, or if an update, set it as updated, and, if available in marketplace,
     * removed from the table of available add-ons.
     *
     * @param addOn the add-on that was installed
     * @since 2.4.0
     */
    public void notifyAddOnInstalled(final AddOn addOn) {
        if (EventQueue.isDispatchThread()) {
            if (latestInfo != null && latestInfo.getAddOn(addOn.getId()) != null) {
                uninstalledAddOnsModel.removeAddOn(addOn);
            }
            installedAddOnsModel.addOrRefreshAddOn(addOn);
        } else {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            notifyAddOnInstalled(addOn);
                        }
                    });
        }
    }

    /**
     * Notifies that the given {@code addOn} as not successfully uninstalled. Add-ons that were not
     * successfully uninstalled are not re-selectable.
     *
     * @param addOn the add-on that was not successfully uninstalled
     * @since 2.4.0
     */
    public void notifyAddOnFailedUninstallation(final AddOn addOn) {
        if (EventQueue.isDispatchThread()) {
            installedAddOnsModel.notifyAddOnFailedUninstallation(addOn);
        } else {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            notifyAddOnFailedUninstallation(addOn);
                        }
                    });
        }
    }

    /**
     * Notifies that the given {@code addOn} as uninstalled. The add-on is removed from the table of
     * installed add-ons and, if available in marketplace, added to the table of available add-ons.
     *
     * @param addOn the add-on that was uninstalled
     * @since 2.4.0
     */
    public void notifyAddOnUninstalled(final AddOn addOn) {
        if (EventQueue.isDispatchThread()) {
            installedAddOnsModel.removeAddOn(addOn);
            if (latestInfo != null) {
                AddOn availableAddOn = latestInfo.getAddOn(addOn.getId());
                if (availableAddOn != null) {
                    uninstalledAddOnsModel.addAddOn(latestInfo.getAddOn(addOn.getId()));
                }
            }
        } else {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            notifyAddOnUninstalled(addOn);
                        }
                    });
        }
    }

    private JButton getUpdateButton() {
        if (updateButton == null) {
            updateButton = new JButton();
            updateButton.setText(Constant.messages.getString("cfu.button.addons.update"));
            updateButton.setEnabled(false); // Nothing will be selected initially
            updateButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            processUpdates(installedAddOnsModel.getSelectedUpdates());
                        }
                    });
        }
        return updateButton;
    }

    private JButton getUpdateAllButton() {
        if (updateAllButton == null) {
            updateAllButton = new JButton();
            updateAllButton.setText(Constant.messages.getString("cfu.button.addons.updateAll"));
            updateAllButton.setEnabled(false); // Nothing will be selected initially
            updateAllButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            processUpdates(installedAddOnsModel.getAllUpdates());
                        }
                    });
        }
        return updateAllButton;
    }

    private void processUpdates(Set<AddOn> updatedAddOns) {
        if (updatedAddOns.isEmpty()) {
            return;
        }

        AddOnDependencyChecker calc = new AddOnDependencyChecker(installedAddOns, latestInfo);

        AddOnDependencyChecker.AddOnChangesResult result =
                calc.calculateUpdateChanges(updatedAddOns);
        if (!calc.confirmUpdateChanges(ManageAddOnsDialog.this, result)) {
            return;
        }

        extension.processAddOnChanges(ManageAddOnsDialog.this, result);
    }

    private JButton getUninstallButton() {
        if (uninstallButton == null) {
            uninstallButton = new JButton();
            uninstallButton.setText(Constant.messages.getString("cfu.button.addons.uninstall"));
            uninstallButton.setEnabled(false); // Nothing will be selected initially
            uninstallButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            Set<AddOn> selectedAddOns = installedAddOnsModel.getSelectedAddOns();
                            if (selectedAddOns.isEmpty()) {
                                return;
                            }

                            Set<AddOn> addOnsBeingDownloaded =
                                    installedAddOnsModel.getDownloadingAddOns();
                            addOnsBeingDownloaded.addAll(
                                    uninstalledAddOnsModel.getDownloadingAddOns());

                            AddOnDependencyChecker calc =
                                    new AddOnDependencyChecker(installedAddOns, latestInfo);
                            AddOnDependencyChecker.UninstallationResult changes =
                                    calc.calculateUninstallChanges(selectedAddOns);

                            if (!calc.confirmUninstallChanges(
                                    ManageAddOnsDialog.this, changes, addOnsBeingDownloaded)) {
                                return;
                            }

                            Set<AddOn> addOns = changes.getUninstallations();
                            Set<Extension> extensions = changes.getExtensions();
                            if (!extension.warnUnsavedResourcesOrActiveActions(
                                    ManageAddOnsDialog.this, addOns, extensions, false)) {
                                return;
                            }

                            extension.uninstallAddOnsWithView(
                                    ManageAddOnsDialog.this, addOns, false, new HashSet<>());
                        }
                    });
        }
        return uninstallButton;
    }

    private JButton getInstallAllButton() {
        if (installAllButton == null) {
            installAllButton = new JButton();
            installAllButton.setEnabled(false);
            installAllButton.setText(Constant.messages.getString("cfu.button.addons.installall"));
            installAllButton.addActionListener(
                    e -> installAddOns(uninstalledAddOnsModel.getAvailableAddOns()));
        }
        return installAllButton;
    }

    private JButton getInstallButton() {
        if (installButton == null) {
            installButton = new JButton();
            installButton.setText(Constant.messages.getString("cfu.button.addons.install"));
            installButton.setEnabled(false); // Nothing will be selected initially
            installButton.addActionListener(
                    e -> installAddOns(uninstalledAddOnsModel.getSelectedAddOns()));
        }
        return installButton;
    }

    private void installAddOns(Set<AddOn> addOns) {
        if (addOns.isEmpty()) {
            return;
        }

        AddOnDependencyChecker calc = new AddOnDependencyChecker(installedAddOns, latestInfo);

        AddOnDependencyChecker.AddOnChangesResult changes = calc.calculateInstallChanges(addOns);
        if (!calc.confirmInstallChanges(ManageAddOnsDialog.this, changes)) {
            return;
        }

        extension.processAddOnChanges(ManageAddOnsDialog.this, changes);
    }

    private JButton getAddOnInfoButton() {
        if (addOnInfoButton == null) {
            addOnInfoButton = new JButton();
            addOnInfoButton.setText(Constant.messages.getString("cfu.button.addons.info"));
            addOnInfoButton.setEnabled(false); // Nothing will be selected initially
            addOnInfoButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            if (getUninstalledAddOnsTable().getSelectedRow() >= 0) {
                                // convertRowIndexToModel in-case they sorted
                                AddOnWrapper ao =
                                        uninstalledAddOnsModel.getAddOnWrapper(
                                                getUninstalledAddOnsTable()
                                                        .convertRowIndexToModel(
                                                                getUninstalledAddOnsTable()
                                                                        .getSelectedRow()));
                                if (ao != null && ao.getAddOn().getInfo() != null) {
                                    DesktopUtils.openUrlInBrowser(
                                            ao.getAddOn().getInfo().toString());
                                }
                            }
                        }
                    });
        }
        return addOnInfoButton;
    }

    public void showProgress() {
        if (this.state.equals(State.DOWNLOADING_UPDATES)) {
            // Updates
            installedAddOnsModel.updateDownloadsProgresses(extension);

            // New addons
            uninstalledAddOnsModel.updateDownloadsProgresses(extension);

            if (extension.getCurrentDownloadCount() == 0) {
                this.state = State.DOWNLOADED_UPDATES;
                this.getDownloadZapButton().setEnabled(true);
                this.getUpdatesMessage()
                        .setText(Constant.messages.getString("cfu.check.upd.downloaded"));
            }
        } else if (this.state.equals(State.DOWNLOADING_ZAP)) {
            try {
                int progress =
                        extension.getDownloadProgressPercent(
                                this.latestInfo.getZapRelease().getUrl());
                if (progress > 0) {
                    this.getDownloadProgress().setText(progress + "%");
                    if (progress >= 100) {
                        this.zapDownloadComplete();
                    }
                }
            } catch (Exception e) {
                logger.debug("Error on {}", this.latestInfo.getZapRelease().getUrl(), e);
                this.getDownloadProgress()
                        .setText(Constant.messages.getString("cfu.table.label.failed"));
            }
        }
    }

    private void zapDownloadComplete() {
        if (this.state.equals(State.DOWNLOADED_ZAP)) {
            // Prevent re-entry
            return;
        }
        this.state = State.DOWNLOADED_ZAP;
        File f = new File(Constant.FOLDER_LOCAL_PLUGIN, latestInfo.getZapRelease().getFileName());

        if (Desktop.isDesktopSupported()) {
            extension.promptToLaunchReleaseAndClose(
                    this.latestInfo.getZapRelease().getVersion(), f);
        } else {
            View.getSingleton()
                    .showWarningDialog(
                            this,
                            Constant.messages.getString(
                                    "cfu.warn.nolaunch",
                                    this.latestInfo.getZapRelease().getVersion(),
                                    f.getAbsolutePath()));
        }
        // Let people download updates now
        this.getUpdateButton().setEnabled(true);
        this.getUpdateAllButton().setEnabled(true);
        this.getUpdatesMessage()
                .setText(
                        Constant.messages.getString(
                                "cfu.check.zap.downloaded", f.getAbsolutePath()));
    }

    @Override
    public void gotLatestData(AddOnCollection aoc) {
        // Callback
        logger.debug("gotLatestData(AddOnCollection {}", aoc);

        if (aoc != null) {
            EventQueue.invokeLater(() -> setLatestVersionInfo(aoc));
        } else {
            View.getSingleton()
                    .showWarningDialog(this, Constant.messages.getString("cfu.check.failed"));
        }
    }

    @Override
    public void insecureUrl(String url, Exception cause) {
        logger.error("Failed to get check for updates on {}", url, cause);
        View.getSingleton().showWarningDialog(this, Constant.messages.getString("cfu.warn.badurl"));
    }

    private class AddOnDetailsPanel extends JPanel {

        private static final long serialVersionUID = 1L;

        private static final String DETAILS_PANEL = "DetailsPanel";
        private static final String NO_DETAILS_PANEL = "NoDetailsPanel";

        private final ZapLabel nameField;
        private final ZapLabel statusField;
        private final ZapLabel versionField;
        private final JLabel descLabel;
        private final ZapLabel descField;
        private final JLabel changesLabel;
        private final JEditorPane changesField;
        private final JLabel infoLabel;
        private final JXHyperlink infoField;
        private final JLabel repoLabel;
        private final JXHyperlink repoField;
        private final ZapLabel idField;
        private final JLabel authorLabel;
        private final ZapLabel authorField;
        private final JLabel dependenciesLabel;
        private final ZapLabel dependenciesField;
        private final JLabel notBeforeVersionLabel;
        private final ZapLabel notBeforeVersionField;
        private final JLabel notFromVersionLabel;
        private final ZapLabel notFromVersionField;
        private final JLabel fileLabel;
        private final ZapLabel fileField;
        private final JLabel mandatoryLabel;
        private final ZapLabel mandatoryField;

        private final CardLayout cardLayout;

        public AddOnDetailsPanel() {
            JXPanel contentPanel = new JXPanel();
            contentPanel.setScrollableHeightHint(ScrollableSizeHint.NONE);

            JScrollPane contentScrollPane = new JScrollPane(contentPanel);
            contentScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            GroupLayout layout = new GroupLayout(contentPanel);
            contentPanel.setLayout(layout);
            layout.setAutoCreateGaps(true);
            layout.setAutoCreateContainerGaps(true);

            JLabel nameLabel = new JLabel(Constant.messages.getString("cfu.table.header.name"));
            nameField = createZapLabelField(nameLabel);

            JLabel statusLabel = new JLabel(Constant.messages.getString("cfu.table.header.status"));
            statusField = createZapLabelField(statusLabel);

            JLabel versionLabel =
                    new JLabel(Constant.messages.getString("cfu.table.header.version"));
            versionField = createZapLabelField(versionLabel);

            descLabel = new JLabel(Constant.messages.getString("cfu.table.header.desc"));
            descField = createZapLabelField(descLabel);

            changesLabel = new JLabel(Constant.messages.getString("cfu.table.header.changes"));
            changesField = new JEditorPane();
            changesLabel.setLabelFor(changesField);
            changesField.setEditable(false);
            changesField.setContentType("text/html; charset=UTF-8");
            changesField.addHyperlinkListener(
                    evt -> {
                        if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
                            changesField.setToolTipText(evt.getURL().toString());
                        } else if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
                            changesField.setToolTipText(null);
                        } else if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            try {
                                Desktop.getDesktop().browse(evt.getURL().toURI());
                            } catch (IOException | URISyntaxException e) {
                                logger.warn("Failed to open the URL: {}", evt.getURL(), e);
                            }
                        }
                    });
            ((DefaultCaret) changesField.getCaret()).setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

            infoLabel = new JLabel(Constant.messages.getString("cfu.table.header.info"));
            infoField = new JXHyperlink();
            infoLabel.setLabelFor(infoField);

            repoLabel = new JLabel(Constant.messages.getString("cfu.table.header.repo"));
            repoField = new JXHyperlink();
            repoLabel.setLabelFor(repoField);

            JLabel idLabel = new JLabel(Constant.messages.getString("cfu.table.header.id"));
            idField = createZapLabelField(idLabel);

            authorLabel = new JLabel(Constant.messages.getString("cfu.table.header.author"));
            authorField = createZapLabelField(authorLabel);

            dependenciesLabel =
                    new JLabel(Constant.messages.getString("cfu.table.header.dependencies"));
            dependenciesField = createZapLabelField(dependenciesLabel);

            notBeforeVersionLabel =
                    new JLabel(Constant.messages.getString("cfu.table.header.notbefore"));
            notBeforeVersionField = createZapLabelField(notBeforeVersionLabel);

            notFromVersionLabel =
                    new JLabel(Constant.messages.getString("cfu.table.header.notfrom"));
            notFromVersionField = createZapLabelField(notFromVersionLabel);

            fileLabel = new JLabel(Constant.messages.getString("cfu.table.header.file"));
            fileField = createZapLabelField(fileLabel);

            mandatoryLabel = new JLabel(Constant.messages.getString("cfu.table.header.mandatory"));
            mandatoryField = createZapLabelField(mandatoryLabel);

            layout.setHorizontalGroup(
                    layout.createSequentialGroup()
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                            .addComponent(nameLabel)
                                            .addComponent(statusLabel)
                                            .addComponent(versionLabel)
                                            .addComponent(descLabel)
                                            .addComponent(changesLabel)
                                            .addComponent(infoLabel)
                                            .addComponent(repoLabel)
                                            .addComponent(idLabel)
                                            .addComponent(authorLabel)
                                            .addComponent(dependenciesLabel)
                                            .addComponent(notBeforeVersionLabel)
                                            .addComponent(notFromVersionLabel)
                                            .addComponent(fileLabel)
                                            .addComponent(mandatoryLabel))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                            .addComponent(
                                                    nameField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    statusField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    versionField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    descField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    changesField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    infoField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    repoField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    idField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    authorField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    dependenciesField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    notBeforeVersionField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    notFromVersionField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    fileField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)
                                            .addComponent(
                                                    mandatoryField,
                                                    0,
                                                    GroupLayout.DEFAULT_SIZE,
                                                    Short.MAX_VALUE)));

            layout.setVerticalGroup(
                    layout.createSequentialGroup()
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(nameLabel)
                                            .addComponent(nameField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(statusLabel)
                                            .addComponent(statusField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(versionLabel)
                                            .addComponent(versionField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(descLabel)
                                            .addComponent(descField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(changesLabel)
                                            .addComponent(changesField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(infoLabel)
                                            .addComponent(infoField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(repoLabel)
                                            .addComponent(repoField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(idLabel)
                                            .addComponent(idField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(authorLabel)
                                            .addComponent(authorField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(dependenciesLabel)
                                            .addComponent(dependenciesField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(notBeforeVersionLabel)
                                            .addComponent(notBeforeVersionField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(notFromVersionLabel)
                                            .addComponent(notFromVersionField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(fileLabel)
                                            .addComponent(fileField))
                            .addGroup(
                                    layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                            .addComponent(mandatoryLabel)
                                            .addComponent(mandatoryField)));

            cardLayout = new CardLayout();
            setLayout(cardLayout);

            JPanel noDetailsPanel = new JPanel(new BorderLayout());
            noDetailsPanel.add(
                    new JLabel(
                            Constant.messages.getString("cfu.label.selectAddOnForDetails"),
                            JLabel.CENTER));
            add(noDetailsPanel, NO_DETAILS_PANEL);

            JPanel detailsPanel = new JPanel(new BorderLayout());
            detailsPanel.add(contentScrollPane);
            add(detailsPanel, DETAILS_PANEL);
        }

        public void setDetails(AddOn addOn) {
            cardLayout.show(this, DETAILS_PANEL);
            nameField.setText(addOn.getName());
            statusField.setText(
                    Constant.messages.getString("cfu.status." + addOn.getStatus().name()));
            versionField.setText(addOn.getVersion().toString());
            setTextOrHide(descLabel, descField, addOn.getDescription());
            setTextOrHide(changesLabel, changesField, addOn.getChanges());
            setUriOrHide(infoLabel, infoField, addOn.getInfo());
            setUriOrHide(repoLabel, repoField, addOn.getRepo());
            idField.setText(addOn.getId());
            setTextOrHide(authorLabel, authorField, addOn.getAuthor());
            setTextOrHide(
                    dependenciesLabel,
                    dependenciesField,
                    addOn.getIdsAddOnDependencies().isEmpty()
                            ? ""
                            : addOn.getIdsAddOnDependencies().stream()
                                    .map(
                                            addOnId -> {
                                                AddOn dep = installedAddOns.getAddOn(addOnId);
                                                if (dep == null && latestInfo != null) {
                                                    dep = latestInfo.getAddOn(addOnId);
                                                }
                                                return dep != null ? dep.getName() : addOnId;
                                            })
                                    .collect(Collectors.joining(",")));
            setTextOrHide(
                    notBeforeVersionLabel, notBeforeVersionField, addOn.getNotBeforeVersion());
            setTextOrHide(notFromVersionLabel, notFromVersionField, addOn.getNotFromVersion());
            if (extension.getLocalAddOns().contains(addOn)) {
                fileLabel.setVisible(true);
                fileField.setText(addOn.getFile().toString());
                fileField.setVisible(true);

                mandatoryLabel.setVisible(true);
                mandatoryField.setText(
                        addOn.isMandatory() ? ADD_ON_MANDATORY : ADD_ON_NOT_MANDATORY);
                mandatoryField.setVisible(true);
            } else {
                fileLabel.setVisible(false);
                fileField.setText("");
                fileField.setVisible(false);

                mandatoryLabel.setVisible(false);
                mandatoryField.setText(ADD_ON_NOT_MANDATORY);
                mandatoryField.setVisible(false);
            }
        }

        public void clearDetails() {
            cardLayout.show(this, NO_DETAILS_PANEL);
            nameField.setText("");
            statusField.setText("");
            versionField.setText("");
            descField.setText("");
            changesField.setText("");
            infoField.setText("");
            repoField.setText("");
            idField.setText("");
            authorField.setText("");
            dependenciesField.setText("");
            notBeforeVersionField.setText("");
            notFromVersionField.setText("");
            fileField.setText("");
            mandatoryField.setText(ADD_ON_NOT_MANDATORY);
        }
    }

    private static ZapLabel createZapLabelField(JLabel label) {
        ZapLabel field = new ZapLabel();
        field.setLineWrap(true);
        field.setWrapStyleWord(true);
        label.setLabelFor(field);
        return field;
    }

    private static void setTextOrHide(JLabel label, JTextComponent textComponent, String text) {
        boolean visible = !text.isEmpty();
        label.setVisible(visible);
        textComponent.setVisible(visible);
        textComponent.setText(text);
    }

    private static void setUriOrHide(JLabel label, JXHyperlink hyperlink, URL url) {
        boolean visible = url != null;
        label.setVisible(visible);
        hyperlink.setVisible(visible);
        hyperlink.setURI(visible ? URI.create(url.toString()) : null);
    }

    private static class DisableSelectionHighlighter extends AbstractHighlighter {

        public DisableSelectionHighlighter(final int columnIndex) {
            setHighlightPredicate(
                    new HighlightPredicate() {

                        @Override
                        public boolean isHighlighted(
                                final Component renderer, final ComponentAdapter adapter) {
                            AddOn.InstallationStatus status =
                                    ((AddOnWrapper) adapter.getValue(columnIndex))
                                            .getInstallationStatus();

                            return AddOn.InstallationStatus.UNINSTALLATION_FAILED == status
                                    || AddOn.InstallationStatus.SOFT_UNINSTALLATION_FAILED == status
                                    || AddOn.InstallationStatus.DOWNLOADING == status;
                        }
                    });
        }

        @Override
        protected Component doHighlight(Component renderer, ComponentAdapter adapter) {
            renderer.setEnabled(false);
            return renderer;
        }
    }

    private static class WarningRunningIssuesHighlighter extends IconHighlighter {

        private final int columnIndex;

        public WarningRunningIssuesHighlighter(int columnIndex) {
            super();
            this.columnIndex = columnIndex;

            setHighlightPredicate(new HighlightPredicate.EqualsHighlightPredicate(Boolean.TRUE));
        }

        public Icon getIcon(ComponentAdapter adapter) {
            AddOnWrapper aow = (AddOnWrapper) adapter.getValue(columnIndex);
            if (aow.isAddOnRunningIssues()) {
                return ICON_ADD_ON_ISSUES;
            }
            return ICON_ADD_ON_EXTENSION_ISSUES;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            if (component instanceof IconAware) {
                ((IconAware) component).setIcon(getIcon(adapter));
            } else if (component instanceof JLabel) {
                ((JLabel) component).setIcon(getIcon(adapter));
            }
            return component;
        }
    }

    private static class WarningUpdateIssuesHighlighter extends IconHighlighter {

        private final int columnIndex;

        public WarningUpdateIssuesHighlighter(int columnIndex) {
            super();
            this.columnIndex = columnIndex;

            setHighlightPredicate(
                    new HighlightPredicate() {

                        @Override
                        public boolean isHighlighted(
                                final Component renderer, final ComponentAdapter adapter) {
                            AddOnWrapper aow =
                                    (AddOnWrapper)
                                            adapter.getValue(
                                                    WarningUpdateIssuesHighlighter.this
                                                            .columnIndex);
                            if (AddOnWrapper.Status.newVersion == aow.getStatus()) {
                                return aow.hasUpdateIssues();
                            }
                            return false;
                        }
                    });
        }

        public Icon getIcon(ComponentAdapter adapter) {
            AddOnWrapper aow = (AddOnWrapper) adapter.getValue(columnIndex);
            if (aow.isAddOnUpdateIssues()) {
                return ICON_ADD_ON_ISSUES;
            }
            return ICON_ADD_ON_EXTENSION_ISSUES;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            if (component instanceof IconAware) {
                ((IconAware) component).setIcon(getIcon(adapter));
            } else if (component instanceof JLabel) {
                ((JLabel) component).setIcon(getIcon(adapter));
            }
            return component;
        }
    }

    private abstract class AbstractAddOnToolTipHighlighter extends AbstractHighlighter {

        private final int column;

        public AbstractAddOnToolTipHighlighter(int column) {
            this.column = column;
        }

        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            ((JComponent) component)
                    .setToolTipText(getToolTip((AddOnWrapper) adapter.getValue(column)));
            return component;
        }

        protected abstract String getToolTip(AddOnWrapper aow);
    }

    private class WarningRunningIssuesToolTipHighlighter extends AbstractAddOnToolTipHighlighter {

        public WarningRunningIssuesToolTipHighlighter(int column) {
            super(column);
        }

        @Override
        protected String getToolTip(AddOnWrapper aow) {
            return aow.hasRunningIssues() ? aow.getRunningIssues() : null;
        }
    }

    private class WarningUpdateIssuesToolTipHighlighter extends AbstractAddOnToolTipHighlighter {

        public WarningUpdateIssuesToolTipHighlighter(int column) {
            super(column);
        }

        @Override
        protected String getToolTip(AddOnWrapper aow) {
            return aow.hasUpdateIssues() ? aow.getUpdateIssues() : null;
        }
    }
}
