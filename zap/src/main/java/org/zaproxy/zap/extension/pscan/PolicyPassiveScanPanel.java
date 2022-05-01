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
package org.zaproxy.zap.extension.pscan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.panels.TableFilterPanel;

public class PolicyPassiveScanPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private JXTable passiveScanRulesTable = null;
    private JScrollPane tableScrollPane = null;
    private PolicyPassiveScanTableModel passiveScanTableModel = null;
    private JComboBox<String> applyToThreshold = null;
    private JComboBox<String> applyToThresholdTarget = null;

    public PolicyPassiveScanPanel() {
        super();
        initialize();
    }

    private void initialize() {
        this.setLayout(new GridBagLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(375, 204);
        }
        this.setName(Constant.messages.getString("pscan.options.policy.title"));
        JPanel passiveScannersFilterPanel = new TableFilterPanel<>(getPassiveScanRulesTable());

        // 'Apply to' controls
        JPanel applyToPanel = new JPanel();
        applyToPanel.setLayout(new GridBagLayout());
        applyToPanel.add(
                new JLabel(Constant.messages.getString("pscan.options.policy.apply.label")),
                LayoutHelper.getGBC(0, 0, 1, 0.0, new Insets(2, 2, 2, 2)));
        applyToPanel.add(getApplyToThreshold(), LayoutHelper.getGBC(1, 0, 1, 0.0));
        applyToPanel.add(
                new JLabel(Constant.messages.getString("pscan.options.policy.thresholdTo.label")),
                LayoutHelper.getGBC(2, 0, 1, 0.0, new Insets(2, 2, 2, 2)));
        applyToPanel.add(getApplyToThresholdTarget(), LayoutHelper.getGBC(3, 0, 1, 0.0));
        applyToPanel.add(
                new JLabel(Constant.messages.getString("pscan.options.policy.rules.label")),
                LayoutHelper.getGBC(4, 0, 1, 0.0, new Insets(2, 2, 2, 2)));
        JButton applyThresholdButton =
                new JButton(Constant.messages.getString("pscan.options.policy.go.button"));
        applyThresholdButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        applyThreshold(
                                strToThreshold((String) getApplyToThreshold().getSelectedItem()),
                                (String) getApplyToThresholdTarget().getSelectedItem());
                        getPassiveScanTableModel().fireTableDataChanged();
                    }
                });
        applyToPanel.add(applyThresholdButton, LayoutHelper.getGBC(5, 0, 1, 0.0));
        applyToPanel.add(new JLabel(""), LayoutHelper.getGBC(6, 0, 1, 1.0)); // Spacer

        this.add(
                applyToPanel,
                LayoutHelper.getGBC(
                        0, 0, 3, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0)));

        this.add(
                passiveScannersFilterPanel,
                LayoutHelper.getGBC(
                        0,
                        1,
                        1,
                        1.0D,
                        0.0D,
                        GridBagConstraints.BOTH,
                        GridBagConstraints.NORTHWEST,
                        new Insets(0, 0, 0, 0)));

        this.add(
                getTableScrollPane(),
                LayoutHelper.getGBC(
                        0,
                        2,
                        1,
                        1.0,
                        1.0,
                        GridBagConstraints.BOTH,
                        GridBagConstraints.NORTHWEST,
                        new Insets(0, 0, 0, 0)));
    }

    private JComboBox<String> getApplyToThreshold() {
        if (applyToThreshold == null) {
            applyToThreshold = new JComboBox<>();
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.off"));
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.low"));
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.medium"));
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.high"));
            // Might as well default to medium, cant think of anything better :/
            applyToThreshold.setSelectedItem(
                    Constant.messages.getString("ascan.options.level.medium"));
        }
        return applyToThreshold;
    }

    private JComboBox<String> getApplyToThresholdTarget() {
        if (applyToThresholdTarget == null) {
            applyToThresholdTarget = new JComboBox<>();
            applyToThresholdTarget.addItem(
                    Constant.messages.getString("ascan.policy.table.status.all"));
            View view = View.getSingleton();
            applyToThresholdTarget.addItem(view.getStatusUI(AddOn.Status.release).toString());
            applyToThresholdTarget.addItem(view.getStatusUI(AddOn.Status.beta).toString());
            applyToThresholdTarget.addItem(view.getStatusUI(AddOn.Status.alpha).toString());
        }
        return applyToThresholdTarget;
    }

    private AlertThreshold strToThreshold(String str) {
        if (str.equals(Constant.messages.getString("ascan.options.level.low"))) {
            return AlertThreshold.LOW;
        }
        if (str.equals(Constant.messages.getString("ascan.options.level.medium"))) {
            return AlertThreshold.MEDIUM;
        }
        if (str.equals(Constant.messages.getString("ascan.options.level.high"))) {
            return AlertThreshold.HIGH;
        }
        return AlertThreshold.OFF;
    }

    private void applyThreshold(AlertThreshold threshold, String target) {
        if (target.equals(Constant.messages.getString("ascan.policy.table.status.all"))) {
            this.getPassiveScanTableModel().applyThresholdToAll(threshold);
        } else {
            this.getPassiveScanTableModel().applyThreshold(threshold, target);
        }
    }

    private static final int[] width = {300, 60, 100};

    private JXTable getPassiveScanRulesTable() {
        if (passiveScanRulesTable == null) {
            passiveScanRulesTable = new JXTable();
            passiveScanRulesTable.setModel(getPassiveScanTableModel());
            passiveScanRulesTable.setRowHeight(DisplayUtils.getScaledSize(18));
            passiveScanRulesTable.setIntercellSpacing(new java.awt.Dimension(1, 1));
            passiveScanRulesTable.setAutoCreateRowSorter(true);

            // Default sort by name (column 0)
            List<RowSorter.SortKey> sortKeys = new ArrayList<>(1);
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            passiveScanRulesTable.getRowSorter().setSortKeys(sortKeys);

            for (int i = 0; i < passiveScanRulesTable.getColumnCount() - 1; i++) {
                TableColumn column = passiveScanRulesTable.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            }

            JComboBox<String> jcb1 = new JComboBox<>();
            for (AlertThreshold level : AlertThreshold.values()) {
                jcb1.addItem(
                        Constant.messages.getString(
                                "ascan.policy.level." + level.name().toLowerCase(Locale.ROOT)));
            }

            passiveScanRulesTable
                    .getColumnModel()
                    .getColumn(1)
                    .setCellEditor(new DefaultCellEditor(jcb1));
        }

        return passiveScanRulesTable;
    }

    @Override
    public void initParam(Object obj) {
        this.getPassiveScanTableModel().reset();
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        this.getPassiveScanTableModel().persistChanges();
    }

    @Override
    public void reset() {
        this.getPassiveScanTableModel().applyThresholdToAll(AlertThreshold.MEDIUM);
        this.getPassiveScanTableModel().persistChanges();
    }

    private JScrollPane getTableScrollPane() {
        if (tableScrollPane == null) {
            tableScrollPane = new JScrollPane();
            tableScrollPane.setViewportView(getPassiveScanRulesTable());
            tableScrollPane.setBorder(
                    javax.swing.BorderFactory.createEtchedBorder(
                            javax.swing.border.EtchedBorder.RAISED));
        }

        return tableScrollPane;
    }

    public PolicyPassiveScanTableModel getPassiveScanTableModel() {
        if (passiveScanTableModel == null) {
            passiveScanTableModel = new PolicyPassiveScanTableModel();
        }

        return passiveScanTableModel;
    }

    public void setPassiveScanTableModel(PolicyPassiveScanTableModel categoryTableModel) {
        this.passiveScanTableModel = categoryTableModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.pscanrules";
    }
}
