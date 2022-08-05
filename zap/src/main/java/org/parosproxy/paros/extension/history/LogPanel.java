/*
 * Paros and its related class files.
 *
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 *
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/01/12 Changed the method valueChanged of the ListSelectionListener.
// ZAP: 2012/03/15 Changed to allow clear the displayQueue.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/04/28 Added logger and log of exception.
// ZAP: 2012/05/02 Changed to use the class literal, instead of getting the
// class at runtime, to get the resource.
// ZAP: 2012/07/29 Issue 43: added Scope support
// ZAP: 2013/02/26 Issue 538: Allow non sequential lines to be selected in the history log
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/11/16 Issue 898: Replace all toggle buttons that set a tool tip text based on button's
// state with ZapToggleButton
// ZAP: 2013/11/16 Issue 899: Remove "manual" update of toggle buttons' icon based on button's state
// ZAP: 2013/11/16 Issue 886: Main pop up menu invoked twice on some components
// ZAP: 2013/12/02 Issue 915: Dynamically filter history based on selection in the sites window
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts
// ZAP: 2014/02/07 Issue 207: Give the most suitable component focus on tab switch
// ZAP: 2014/03/23 Issue 609: Provide a common interface to query the state and
// access the data (HttpMessage and HistoryReference) displayed in the tabs
// ZAP: 2014/03/23 Issue 503: Change the footer tabs to display the data
// with tables instead of lists
// ZAP: 2015/02/10 Issue 1528: Support user defined font size
// ZAP: 2016/04/14 Use View to display the HTTP messages
// ZAP: 2017/01/30 Use HistoryTable.
// ZAP: 2017/03/03 Tweak filter label.
// ZAP: 2017/05/12 Support table export.
// ZAP: 2017/09/02 Use KeyEvent instead of Event (deprecated in Java 9).
// ZAP: 2017/10/20 Add action/shortcut to delete history entries (Issue 3626).
// ZAP: 2018/01/29 Make getHistoryReferenceTable protected (Issue 4000).
// ZAP: 2018/07/17 Use ViewDelegate.getMenuShortcutKeyStroke.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/01/02 Allow to set if messages are displayed.
// ZAP: 2020/07/09 Removed unused variable (and related import) and remove boilerplate javadoc for
// getScrollLog()
// ZAP: 2022/02/26 Remove code deprecated in 2.5.0
// ZAP: 2022/02/28 Remove code deprecated in 2.6.0
// ZAP: 2022/08/05 Address warns with Java 18 (Issue 7389).
package org.parosproxy.paros.extension.history;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.TableExportButton;
import org.zaproxy.zap.view.DeselectableButtonGroup;
import org.zaproxy.zap.view.ZapToggleButton;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.HistoryReferencesTable;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel;

@SuppressWarnings("serial")
public class LogPanel extends AbstractPanel {
    private static final long serialVersionUID = 1L;
    private javax.swing.JScrollPane scrollLog = null;
    private HistoryTable historyReferencesTable = null;
    // ZAP: Added history (filter) toolbar
    private javax.swing.JPanel historyPanel = null;
    private javax.swing.JToolBar panelToolbar = null;
    private JButton filterButton = null;
    private JLabel filterStatus = null;
    private ZapToggleButton scopeButton = null;

    private ExtensionHistory extension = null;

    private ZapToggleButton linkWithSitesTreeButton;

    private LinkWithSitesTreeSelectionListener linkWithSitesTreeSelectionListener;

    private DeselectableButtonGroup historyListFiltersButtonGroup;

    private TableExportButton<HistoryReferencesTable> exportButton;

    private final ViewDelegate view;

    public LogPanel(ViewDelegate view) {
        super();
        this.view = view;
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        historyListFiltersButtonGroup = new DeselectableButtonGroup();

        this.setLayout(new BorderLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(600, 200);
        }
        this.add(getHistoryPanel(), java.awt.BorderLayout.CENTER);

        this.setDefaultAccelerator(
                view.getMenuShortcutKeyStroke(KeyEvent.VK_H, KeyEvent.SHIFT_DOWN_MASK, false));
        this.setMnemonic(Constant.messages.getChar("history.panel.mnemonic"));
    }

    @Override
    public void tabSelected() {
        // Give the history list focus so that the user can immediately use the arrow keys to
        // navigate
        getHistoryReferenceTable().requestFocusInWindow();
    }

    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }

    private javax.swing.JScrollPane getScrollLog() {
        if (scrollLog == null) {
            scrollLog = new javax.swing.JScrollPane();
            scrollLog.setViewportView(getHistoryReferenceTable());
            scrollLog.setName("scrollLog");
        }
        return scrollLog;
    }

    private javax.swing.JPanel getHistoryPanel() {
        if (historyPanel == null) {

            historyPanel = new javax.swing.JPanel();
            historyPanel.setLayout(new java.awt.GridBagLayout());
            historyPanel.setName("History Panel");

            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;

            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;

            historyPanel.add(this.getPanelToolbar(), gridBagConstraints1);
            historyPanel.add(getScrollLog(), gridBagConstraints2);
        }
        return historyPanel;
    }

    private javax.swing.JToolBar getPanelToolbar() {
        if (panelToolbar == null) {

            panelToolbar = new javax.swing.JToolBar();
            panelToolbar.setLayout(new java.awt.GridBagLayout());
            panelToolbar.setEnabled(true);
            panelToolbar.setFloatable(false);
            panelToolbar.setRollover(true);
            panelToolbar.setPreferredSize(new java.awt.Dimension(800, 30));
            panelToolbar.setName("History Toolbar");

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new java.awt.Insets(0, 0, 0, 0);
            gbc.anchor = java.awt.GridBagConstraints.WEST;

            panelToolbar.add(getScopeButton(), gbc);

            ++gbc.gridx;
            panelToolbar.add(getLinkWithSitesTreeButton(), gbc);

            ++gbc.gridx;
            panelToolbar.add(getFilterButton(), gbc);

            filterStatus =
                    new JLabel(
                            Constant.messages.getString("history.filter.label.filter")
                                    + " "
                                    + Constant.messages.getString("history.filter.label.off"));

            ++gbc.gridx;
            panelToolbar.add(filterStatus, gbc);

            ++gbc.gridx;
            panelToolbar.add(getExportButton(), gbc);

            ++gbc.gridx;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.anchor = java.awt.GridBagConstraints.EAST;
            gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;
            panelToolbar.add(new JLabel(), gbc);
        }
        return panelToolbar;
    }

    private JButton getFilterButton() {
        if (filterButton == null) {
            filterButton = new JButton();
            // ZAP: Changed to use the class literal.
            filterButton.setIcon(
                    new ImageIcon(
                            LogPanel.class.getResource(
                                    "/resource/icon/16/054.png"))); // 'filter' icon
            filterButton.setToolTipText(
                    Constant.messages.getString("history.filter.button.filter"));
            DisplayUtils.scaleIcon(filterButton);

            filterButton.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            extension.showFilterPlusDialog();
                        }
                    });
        }
        return filterButton;
    }

    private JToggleButton getScopeButton() {
        if (scopeButton == null) {
            scopeButton = new ZapToggleButton();
            scopeButton.setIcon(
                    new ImageIcon(
                            LogPanel.class.getResource("/resource/icon/fugue/target-grey.png")));
            scopeButton.setToolTipText(
                    Constant.messages.getString("history.scope.button.unselected"));
            scopeButton.setSelectedIcon(
                    new ImageIcon(LogPanel.class.getResource("/resource/icon/fugue/target.png")));
            scopeButton.setSelectedToolTipText(
                    Constant.messages.getString("history.scope.button.selected"));
            DisplayUtils.scaleIcon(scopeButton);

            scopeButton.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            extension.setShowJustInScope(scopeButton.isSelected());
                        }
                    });
            historyListFiltersButtonGroup.add(scopeButton);
        }
        return scopeButton;
    }

    private JToggleButton getLinkWithSitesTreeButton() {
        if (linkWithSitesTreeButton == null) {
            linkWithSitesTreeButton = new ZapToggleButton();
            linkWithSitesTreeButton.setIcon(
                    new ImageIcon(LogPanel.class.getResource("/resource/icon/16/earth-grey.png")));
            linkWithSitesTreeButton.setToolTipText(
                    Constant.messages.getString(
                            "history.linkWithSitesSelection.unselected.button.tooltip"));
            linkWithSitesTreeButton.setSelectedIcon(
                    new ImageIcon(LogPanel.class.getResource("/resource/icon/16/094.png")));
            linkWithSitesTreeButton.setSelectedToolTipText(
                    Constant.messages.getString(
                            "history.linkWithSitesSelection.selected.button.tooltip"));
            DisplayUtils.scaleIcon(linkWithSitesTreeButton);

            linkWithSitesTreeButton.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            setLinkWithSitesTreeSelection(linkWithSitesTreeButton.isSelected());
                        }
                    });
            historyListFiltersButtonGroup.add(linkWithSitesTreeButton);
        }
        return linkWithSitesTreeButton;
    }

    private TableExportButton<HistoryReferencesTable> getExportButton() {
        if (exportButton == null) {
            exportButton = new TableExportButton<>(getHistoryReferenceTable());
        }
        return exportButton;
    }

    public void setLinkWithSitesTreeSelection(boolean enabled) {
        linkWithSitesTreeButton.setSelected(enabled);
        final JTree sitesTree = view.getSiteTreePanel().getTreeSite();
        String baseUri = null;
        if (enabled) {
            final TreePath selectionPath = sitesTree.getSelectionPath();
            if (selectionPath != null) {
                baseUri =
                        getLinkWithSitesTreeBaseUri(
                                (SiteNode) selectionPath.getLastPathComponent());
            }
            sitesTree.addTreeSelectionListener(getLinkWithSitesTreeSelectionListener());
        } else {
            sitesTree.removeTreeSelectionListener(getLinkWithSitesTreeSelectionListener());
        }
        extension.setLinkWithSitesTree(enabled, baseUri);
    }

    private static String getLinkWithSitesTreeBaseUri(SiteNode siteNode) {
        if (!siteNode.isRoot()) {
            HistoryReference historyReference = siteNode.getHistoryReference();
            if (historyReference != null) {
                return historyReference.getURI().toString();
            }
        }
        return null;
    }

    protected HistoryReferencesTable getHistoryReferenceTable() {
        if (historyReferencesTable == null) {
            historyReferencesTable = new HistoryTable();
            historyReferencesTable.addMouseListener(
                    new java.awt.event.MouseAdapter() {

                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            if (SwingUtilities.isLeftMouseButton(e)
                                    && e.getClickCount() > 1) { // double click
                                view.getRequestPanel().setTabFocus();
                                return;
                            }
                        }
                    });

            String deleteHrefKey = "zap.delete.href";
            historyReferencesTable
                    .getInputMap()
                    .put(view.getDefaultDeleteKeyStroke(), deleteHrefKey);
            historyReferencesTable
                    .getActionMap()
                    .put(
                            deleteHrefKey,
                            new AbstractAction() {

                                private static final long serialVersionUID = 1L;

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    extension.purgeHistory(
                                            historyReferencesTable.getSelectedHistoryReferences());
                                }
                            });
        }
        return historyReferencesTable;
    }

    protected void display(final HistoryReference historyRef) {
        getHistoryReferenceTable().selectHistoryReference(historyRef.getHistoryId());
    }

    public void setFilterStatus(HistoryFilter filter) {
        filterStatus.setText(filter.toShortString());
        filterStatus.setToolTipText(filter.toLongString());
    }

    private LinkWithSitesTreeSelectionListener getLinkWithSitesTreeSelectionListener() {
        if (linkWithSitesTreeSelectionListener == null) {
            linkWithSitesTreeSelectionListener = new LinkWithSitesTreeSelectionListener();
        }
        return linkWithSitesTreeSelectionListener;
    }

    private class LinkWithSitesTreeSelectionListener implements TreeSelectionListener {

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            extension.updateLinkWithSitesTreeBaseUri(
                    getLinkWithSitesTreeBaseUri((SiteNode) e.getPath().getLastPathComponent()));
        }
    }

    public HistoryReference getSelectedHistoryReference() {
        return getHistoryReferenceTable().getSelectedHistoryReference();
    }

    public List<HistoryReference> getSelectedHistoryReferences() {
        return getHistoryReferenceTable().getSelectedHistoryReferences();
    }

    public void setModel(
            HistoryReferencesTableModel<DefaultHistoryReferencesTableEntry> historyTableModel) {
        getHistoryReferenceTable().setModel(historyTableModel);
    }

    void setDisplaySelectedMessage(boolean display) {
        historyReferencesTable.setDisplaySelectedMessage(display);
    }
}
