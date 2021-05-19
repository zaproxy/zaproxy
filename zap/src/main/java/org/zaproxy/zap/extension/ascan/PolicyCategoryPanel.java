/*
 *
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
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/11/28 Issue 923: Allow individual rule thresholds and strengths to be set via GUI
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2017/01/09 Remove method no longer needed.
// ZAP: 2018/01/30 Do not rely on default locale for upper/lower case conversions (when locale is
// not important).
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.zaproxy.zap.extension.ascan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.DisplayUtils;

public class PolicyCategoryPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JTable tableTest = null;
    private JScrollPane jScrollPane = null;
    private CategoryTableModel categoryTableModel =
            null; //  @jve:decl-index=0:parse,visual-constraint="294,249"
    private static final int[] width = {300, 100, 100, 200};
    private int category;

    public PolicyCategoryPanel(
            int category, PluginFactory pluginFactory, AlertThreshold defaultThreshold) {
        super();
        this.category = category;
        initialize();
        getCategoryTableModel().setTable(category, pluginFactory, defaultThreshold);
    }

    /** This method initializes this */
    private void initialize() {
        java.awt.GridBagConstraints gridBagConstraints11 = new GridBagConstraints();

        this.setLayout(new GridBagLayout());
        this.setSize(375, 204);
        this.setName("categoryPanel");
        gridBagConstraints11.weightx = 1.0;
        gridBagConstraints11.weighty = 1.0;
        gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints11.gridx = 0;
        gridBagConstraints11.gridy = 1;
        gridBagConstraints11.insets = new java.awt.Insets(0, 0, 0, 0);
        gridBagConstraints11.anchor = java.awt.GridBagConstraints.NORTHWEST;
        this.add(getJScrollPane(), gridBagConstraints11);
    }
    /**
     * This method initializes tableTest
     *
     * @return javax.swing.JTable
     */
    private JTable getTableTest() {
        if (tableTest == null) {
            tableTest = new JTable();
            tableTest.setModel(getCategoryTableModel());
            tableTest.setRowHeight(DisplayUtils.getScaledSize(18));
            tableTest.setIntercellSpacing(new java.awt.Dimension(1, 1));
            tableTest.setAutoCreateRowSorter(true);

            // Default sort by name (column 0)
            List<RowSorter.SortKey> sortKeys = new ArrayList<>(1);
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            tableTest.getRowSorter().setSortKeys(sortKeys);

            for (int i = 0; i < tableTest.getColumnCount() - 1; i++) {
                TableColumn column = tableTest.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            }
            JComboBox<String> jcb1 = new JComboBox<>();
            for (AlertThreshold level : AlertThreshold.values()) {
                jcb1.addItem(
                        Constant.messages.getString(
                                "ascan.policy.level." + level.name().toLowerCase(Locale.ROOT)));
            }
            tableTest.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(jcb1));

            JComboBox<String> jcb2 = new JComboBox<>();
            for (AttackStrength level : AttackStrength.values()) {
                jcb2.addItem(
                        Constant.messages.getString(
                                "ascan.policy.level." + level.name().toLowerCase(Locale.ROOT)));
            }
            tableTest.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(jcb2));
        }
        return tableTest;
    }

    @Override
    public void initParam(Object obj) {}

    @Override
    public void saveParam(Object obj) throws Exception {}

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getTableTest());
            jScrollPane.setBorder(
                    javax.swing.BorderFactory.createEtchedBorder(
                            javax.swing.border.EtchedBorder.RAISED));
        }
        return jScrollPane;
    }
    /**
     * This method initializes categoryTableModel
     *
     * @return org.parosproxy.paros.plugin.scanner.CategoryTableModel
     */
    private CategoryTableModel getCategoryTableModel() {
        if (categoryTableModel == null) {
            categoryTableModel = new CategoryTableModel();
        }
        return categoryTableModel;
    }

    public void setPluginFactory(PluginFactory pluginFactory, AlertThreshold defaultThreshold) {
        getCategoryTableModel().setTable(category, pluginFactory, defaultThreshold);
        this.modelChanged();
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.scanpolicy";
    }

    public void modelChanged() {
        this.getCategoryTableModel().fireTableDataChanged();
    }
}
