/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.params;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.utils.TableExportButton;
import org.zaproxy.zap.view.ScanPanel;

@SuppressWarnings("serial")
public class ParamsPanel extends AbstractPanel {

    private static final long serialVersionUID = 1L;

    public static final String PANEL_NAME = "params";

    private ExtensionParams extension = null;
    private JPanel panelCommand = null;
    private JToolBar panelToolbar = null;
    private JScrollPane jScrollPane = null;

    private String currentSite = null;
    private JComboBox<String> siteSelect = null;
    private SortedComboBoxModel<String> siteModel = new SortedComboBoxModel<>();
    // private JButton optionsButton = null;

    private JXTable paramsTable = null;
    private ParamsTableModel paramsModel = new ParamsTableModel();
    private TableExportButton<JXTable> exportButton = null;

    // private static Log log = LogFactory.getLog(ParamsPanel.class);

    public ParamsPanel(ExtensionParams extension) {
        super();
        this.extension = extension;
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setSize(474, 251);
        this.setName(Constant.messages.getString("params.panel.title"));
        this.setIcon(
                new ImageIcon(
                        ParamsPanel.class.getResource("/resource/icon/16/179.png"))); // 'form' icon
        this.setDefaultAccelerator(
                extension
                        .getView()
                        .getMenuShortcutKeyStroke(KeyEvent.VK_P, KeyEvent.SHIFT_DOWN_MASK, false));
        this.setMnemonic(Constant.messages.getChar("params.panel.mnemonic"));
        this.add(getPanelCommand(), getPanelCommand().getName());
    }
    /**
     * This method initializes panelCommand
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getPanelCommand() {
        if (panelCommand == null) {

            panelCommand = new javax.swing.JPanel();
            panelCommand.setLayout(new java.awt.GridBagLayout());
            panelCommand.setName("Params");

            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.weightx = 1.0D;
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 1;
            gridBagConstraints2.weightx = 1.0;
            gridBagConstraints2.weighty = 1.0;
            gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;

            panelCommand.add(this.getPanelToolbar(), gridBagConstraints1);
            panelCommand.add(getJScrollPane(), gridBagConstraints2);
        }
        return panelCommand;
    }

    private javax.swing.JToolBar getPanelToolbar() {
        if (panelToolbar == null) {

            panelToolbar = new javax.swing.JToolBar();
            panelToolbar.setLayout(new java.awt.GridBagLayout());
            panelToolbar.setEnabled(true);
            panelToolbar.setFloatable(false);
            panelToolbar.setRollover(true);
            panelToolbar.setPreferredSize(new java.awt.Dimension(800, 30));
            panelToolbar.setName("ParamsToolbar");

            GridBagConstraints gridBagConstraints0 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            GridBagConstraints gridBagConstraintsx = new GridBagConstraints();

            gridBagConstraints0.gridx = 0;
            gridBagConstraints0.gridy = 0;
            gridBagConstraints0.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints0.anchor = java.awt.GridBagConstraints.WEST;

            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;

            gridBagConstraints2.gridx = 2;
            gridBagConstraints2.gridy = 0;
            gridBagConstraints2.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.WEST;

            gridBagConstraints3.gridx = 3;
            gridBagConstraints3.gridy = 0;
            gridBagConstraints3.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.WEST;

            gridBagConstraintsx.gridx = 4;
            gridBagConstraintsx.gridy = 0;
            gridBagConstraintsx.weightx = 1.0;
            gridBagConstraintsx.weighty = 1.0;
            gridBagConstraintsx.insets = new java.awt.Insets(0, 0, 0, 0);
            gridBagConstraintsx.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraintsx.fill = java.awt.GridBagConstraints.HORIZONTAL;

            JLabel t1 = new JLabel();

            // panelToolbar.add(getOptionsButton(), gridBagConstraints0);

            panelToolbar.add(
                    new JLabel(Constant.messages.getString("params.toolbar.site.label")),
                    gridBagConstraints1);
            panelToolbar.add(getSiteSelect(), gridBagConstraints2);
            panelToolbar.add(getExportButton(), gridBagConstraints3);

            panelToolbar.add(t1, gridBagConstraintsx);
        }
        return panelToolbar;
    }

    private TableExportButton<JXTable> getExportButton() {
        if (exportButton == null) {
            exportButton = new TableExportButton<>(getParamsTable());
        }
        return exportButton;
    }

    /*
     * Displaying the ANTI CSRF options might not actually make that much sense...
    private JButton getOptionsButton() {
    	if (optionsButton == null) {
    		optionsButton = new JButton();
    		optionsButton.setToolTipText(Constant.messages.getString("params.toolbar.button.options"));
    		optionsButton.setIcon(new ImageIcon(ParamsPanel.class.getResource("/resource/icon/16/041.png")));	// 'Gears' icon
    		optionsButton.setEnabled(false);
    		optionsButton.addActionListener(new ActionListener () {

    			@Override
    			public void actionPerformed(ActionEvent e) {
    				Control.getSingleton().getMenuToolsControl().options(Constant.messages.getString("options.acsrf.title"));
    			}

    		});

    	}
    	return optionsButton;
    }
    */

    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getParamsTable());
        }
        return jScrollPane;
    }

    private void setParamsTableColumnSizes() {

        for (int i = 0; i < paramsTable.getColumnCount(); i++) {
            DefaultTableColumnModel colModel =
                    (DefaultTableColumnModel) paramsTable.getColumnModel();
            TableColumn col = colModel.getColumn(i);
            int width = 0;

            TableCellRenderer renderer = col.getHeaderRenderer();
            if (renderer == null) {
                renderer = paramsTable.getTableHeader().getDefaultRenderer();
            }
            Component comp =
                    renderer.getTableCellRendererComponent(
                            paramsTable, col.getHeaderValue(), false, false, 0, 0);
            width = comp.getPreferredSize().width;
            col.setPreferredWidth(width + 2);
        }
        paramsTable.getColumnModel().getColumn(6).setPreferredWidth(999); // value
    }

    protected JXTable getParamsTable() {
        if (paramsTable == null) {
            paramsTable = new JXTable(paramsModel);

            paramsTable.setColumnSelectionAllowed(false);
            paramsTable.setCellSelectionEnabled(false);
            paramsTable.setRowSelectionAllowed(true);
            paramsTable.setAutoCreateRowSorter(true);
            paramsTable.setColumnControlVisible(true);

            this.setParamsTableColumnSizes();

            paramsTable.setName(PANEL_NAME);
            paramsTable.setDoubleBuffered(true);
            paramsTable.addMouseListener(
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
                                int row = paramsTable.rowAtPoint(e.getPoint());
                                if (row < 0
                                        || !paramsTable.getSelectionModel().isSelectedIndex(row)) {
                                    paramsTable.getSelectionModel().clearSelection();
                                    if (row >= 0) {
                                        paramsTable
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
        return paramsTable;
    }

    private JComboBox<String> getSiteSelect() {
        if (siteSelect == null) {
            siteSelect = new JComboBox<>(siteModel);
            siteSelect.addItem(Constant.messages.getString("params.toolbar.site.select"));
            siteSelect.setSelectedIndex(0);

            siteSelect.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {

                            String item = (String) siteSelect.getSelectedItem();
                            if (item != null && siteSelect.getSelectedIndex() > 0) {
                                siteSelected(item);
                            }
                        }
                    });
        }
        return siteSelect;
    }

    public void addSite(String site) {
        site = ScanPanel.cleanSiteName(site, true);
        if (siteModel.getIndexOf(site) < 0) {
            siteModel.addElement(site);
            if (siteModel.getSize() == 2 && currentSite == null) {
                // First site added, automatically select it
                this.getSiteSelect().setSelectedIndex(1);
                siteSelected(site);
            }
        }
    }

    private void siteSelected(String site) {
        site = ScanPanel.cleanSiteName(site, true);
        if (!site.equals(currentSite)) {
            siteModel.setSelectedItem(site);

            paramsModel = extension.getSiteParameters(site).getModel();
            this.getParamsTable().setModel(paramsModel);

            this.setParamsTableColumnSizes();

            currentSite = site;
        }
    }

    public void nodeSelected(SiteNode node) {
        if (node != null) {
            siteSelected(ScanPanel.cleanSiteName(node, true));
        }
    }

    public void reset() {
        currentSite = null;

        siteModel.removeAllElements();
        siteSelect.addItem(Constant.messages.getString("params.toolbar.site.select"));
        siteSelect.setSelectedIndex(0);

        paramsModel.removeAllElements();
        paramsModel.fireTableDataChanged();

        paramsTable.setModel(paramsModel);
    }

    /**
     * Gets the current selected site.
     *
     * @return the current site
     */
    public String getCurrentSite() {
        return currentSite;
    }

    protected HtmlParameterStats getSelectedParam() {
        int selectedRow = this.getParamsTable().getSelectedRow();
        if (selectedRow == -1) {
            return null;
        }

        // TODO type is localized :(
        String type = (String) this.getParamsTable().getValueAt(selectedRow, 0);
        String name = (String) this.getParamsTable().getValueAt(selectedRow, 1);

        SiteParameters sps = extension.getSiteParameters(currentSite);
        if (sps != null) {
            return sps.getParam(HtmlParameter.Type.valueOf(type.toLowerCase()), name); // TODO HACK!
        }
        return null;
    }

    /**
     * Tells whether or not only one of the parameters is selected.
     *
     * @return {@code true} if only one parameter is selected, {@code false} otherwise.
     * @see #getSelectedParam()
     * @since 2.6.0
     */
    boolean isOnlyOneParamSelected() {
        return getParamsTable().getSelectedRowCount() == 1;
    }
}
