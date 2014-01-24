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
package org.zaproxy.zap.extension.ascan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
//import org.zaproxy.zap.extension.pscan.AllPassiveComboBoxModel;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.view.LayoutHelper;

public class PolicyAllCategoryPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    
    private JTable tableTest = null;
    private JScrollPane jScrollPane = null;
    private AllCategoryTableModel allCategoryTableModel = null;  //  @jve:decl-index=0:parse,visual-constraint="294,249"
    private JComboBox<String> comboThreshold = null;
    private JLabel labelThresholdNotes = null;
    private JComboBox<String> comboStrength = null;
    private JLabel labelStrengthNotes = null;
    
    // Global configuration for all passive plugins
    private JComboBox<String> comboPassiveThreshold = null;
    
    private static final int[] width = {300, 100, 100};

    /**
     *
     */
    public PolicyAllCategoryPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setLayout(new GridBagLayout());
        this.setSize(375, 204);
        this.setName("categoryPanel");

        // Add Attack settings section - a copy of the options dialog
        // ---------------------------------------------
        this.add(new JLabel(Constant.messages.getString("ascan.options.level.label")),
                LayoutHelper.getGBC(0, 1, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getComboThreshold(),
                LayoutHelper.getGBC(1, 1, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getThresholdNotes(),
                LayoutHelper.getGBC(2, 1, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

        this.add(new JLabel(Constant.messages.getString("ascan.options.strength.label")),
                LayoutHelper.getGBC(0, 2, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getComboStrength(),
                LayoutHelper.getGBC(1, 2, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getStrengthNotes(),
                LayoutHelper.getGBC(2, 2, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

        // TODO This could be done in a cleaner way...
        ExtensionPassiveScan pscan = (ExtensionPassiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);
        if (pscan != null) {
            this.add(new JLabel(Constant.messages.getString("pscan.options.level.label")),
                    LayoutHelper.getGBC(0, 3, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            this.add(getComboPassiveThreshold(),
                    LayoutHelper.getGBC(1, 3, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            this.updatePassiveThreshold();
        }
    
        // Finally add the scrolling list of active plugin categories
        this.add(getJScrollPane(),
                LayoutHelper.getGBC(0, 4, 3, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0)));

        OptionsParam options = Model.getSingleton().getOptionsParam();
        ScannerParam param = (ScannerParam)options.getParamSet(ScannerParam.class);
        
        switch (param.getAlertThreshold()) {
            case LOW:
                getComboThreshold().setSelectedItem(Constant.messages.getString("ascan.options.level.low"));
                getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
                break;

            case HIGH:
                getComboThreshold().setSelectedItem(Constant.messages.getString("ascan.options.level.high"));
                getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
                break;

            case MEDIUM:
            default:
                getComboThreshold().setSelectedItem(Constant.messages.getString("ascan.options.level.medium"));
                getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
                break;
        }

        switch (param.getAttackStrength()) {
            case LOW:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.low"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.low.label"));
                break;

            case HIGH:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.high"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.high.label"));
                break;

            case INSANE:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.insane"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.insane.label"));
                break;

            case MEDIUM:
            default:
                getComboStrength().setSelectedItem(Constant.messages.getString("ascan.options.strength.medium"));
                getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.medium.label"));
                break;
        }
    }
    
    /**
     * This method initializes tableTest
     *
     * @return javax.swing.JTable
     */
    private JTable getTableTest() {
        if (tableTest == null) {
            tableTest = new JTable();
            tableTest.setModel(getAllCategoryTableModel());
            tableTest.setRowHeight(18);
            tableTest.setIntercellSpacing(new java.awt.Dimension(1, 1));
            
            for (int i = 0; i < 2; i++) {
                TableColumn column = tableTest.getColumnModel().getColumn(i);
                column.setPreferredWidth(width[i]);
            }
            
            JComboBox<String> jcb1 = new JComboBox<>();
            jcb1.addItem("");	// Always show a blank one for where they are not all the same
            for (AlertThreshold level : AlertThreshold.values()) {
                jcb1.addItem(Constant.messages.getString("ascan.policy.level." + level.name().toLowerCase()));
            }
            
            tableTest.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(jcb1));

            JComboBox<String> jcb2 = new JComboBox<>();
            jcb2.addItem("");	// Always show a blank one for where they are not all the same
            for (AttackStrength level : AttackStrength.values()) {
                jcb2.addItem(Constant.messages.getString("ascan.policy.level." + level.name().toLowerCase()));
            }
            
            tableTest.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(jcb2));
        }
        
        return tableTest;
    }

    @Override
    public void initParam(Object obj) {
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    }

    @Override
    public void saveParam(Object obj) throws Exception {
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getTableTest());
            jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        }

        return jScrollPane;
    }

    /**
     * This method initializes categoryTableModel
     *
     * @return TableModel
     */
    private TableModel getAllCategoryTableModel() {
        if (allCategoryTableModel == null) {
            allCategoryTableModel = new AllCategoryTableModel();
            allCategoryTableModel.setTable(PluginFactory.getAllPlugin());
        }
        
        return allCategoryTableModel;
    }

    private JLabel getThresholdNotes() {
        if (labelThresholdNotes == null) {
            labelThresholdNotes = new JLabel();
        }
        
        return labelThresholdNotes;
    }

    private JComboBox<String> getComboThreshold() {
        if (comboThreshold == null) {
            comboThreshold = new JComboBox<>();
            comboThreshold.addItem(Constant.messages.getString("ascan.options.level.low"));
            comboThreshold.addItem(Constant.messages.getString("ascan.options.level.medium"));
            comboThreshold.addItem(Constant.messages.getString("ascan.options.level.high"));
            comboThreshold.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Set the explanation and save
                    OptionsParam options = Model.getSingleton().getOptionsParam();
                    ScannerParam param = (ScannerParam) options.getParamSet(ScannerParam.class);

                    if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.low"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
                        param.setAlertThreshold(AlertThreshold.LOW);

                    } else if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.medium"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
                        param.setAlertThreshold(AlertThreshold.MEDIUM);

                    } else {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
                        param.setAlertThreshold(AlertThreshold.HIGH);
                    }
                }
            });
        }

        return comboThreshold;
    }

    private JLabel getStrengthNotes() {
        if (labelStrengthNotes == null) {
            labelStrengthNotes = new JLabel();
        }

        return labelStrengthNotes;
    }

    private JComboBox<String> getComboStrength() {
        if (comboStrength == null) {
            comboStrength = new JComboBox<>();
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.low"));
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.medium"));
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.high"));
            comboStrength.addItem(Constant.messages.getString("ascan.options.strength.insane"));
            comboStrength.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // Set the explanation and save
                    OptionsParam options = Model.getSingleton().getOptionsParam();
                    ScannerParam param = (ScannerParam) options.getParamSet(ScannerParam.class);
                    if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.low"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.low.label"));
                        param.setAttackStrength(AttackStrength.LOW);

                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.medium"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.medium.label"));
                        param.setAttackStrength(AttackStrength.MEDIUM);

                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.high"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.high.label"));
                        param.setAttackStrength(AttackStrength.HIGH);

                    } else {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.insane.label"));
                        param.setAttackStrength(AttackStrength.INSANE);
                    }
                }
            });
        }
        
        return comboStrength;
    }

    /**
     * 
     * @return 
     */
    private JComboBox<String> getComboPassiveThreshold() {
        if (comboPassiveThreshold == null) {
            comboPassiveThreshold = new JComboBox<>();
            //comboPassiveThreshold.setModel(new AllPassiveComboBoxModel<String>());
            // Add all possible levels
            comboPassiveThreshold.addItem("");	// Always show a blank one for where they are not all the same
            for (AlertThreshold level : AlertThreshold.values()) {
                comboPassiveThreshold.addItem(Constant.messages.getString("ascan.policy.level." + level.name().toLowerCase()));
            }

            comboPassiveThreshold.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String value = (String)comboPassiveThreshold.getSelectedItem();
                    ExtensionPassiveScan pscan = (ExtensionPassiveScan)Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);

                    if ((value != null) && !value.isEmpty() && (pscan != null)) {
                        // Set the value for all passive plugins
                        
                        if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.low"))) {
                            pscan.setAllScannerThreshold(AlertThreshold.LOW);

                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.medium"))) {
                            pscan.setAllScannerThreshold(AlertThreshold.MEDIUM);
                            
                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.high"))) {
                            pscan.setAllScannerThreshold(AlertThreshold.HIGH);
                            
                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.off"))) {
                            pscan.setAllScannerThreshold(AlertThreshold.OFF);
                            
                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.default"))) {
                            pscan.setAllScannerThreshold(AlertThreshold.DEFAULT);
                        }
                    }
                }
            });

        }

        return comboPassiveThreshold;
    }

    /**
     * 
     * @return 
     */
    public void updatePassiveThreshold() {
        ExtensionPassiveScan pscan = (ExtensionPassiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);
        String selectedItem = "";

        // Set the correct alert threshold value common to all plugins
        if (pscan != null) {
            
            AlertThreshold at = pscan.getAllScannerThreshold();
            if (at != null) {
                switch (at) {
                    case LOW:
                        selectedItem = Constant.messages.getString("ascan.policy.level.low");
                        break;

                    case MEDIUM:
                        selectedItem = Constant.messages.getString("ascan.policy.level.medium");
                        break;

                    case HIGH:
                        selectedItem = Constant.messages.getString("ascan.policy.level.high");
                        break;

                    case OFF:
                        selectedItem = Constant.messages.getString("ascan.policy.level.off");
                        break;

                    default:
                        selectedItem = Constant.messages.getString("ascan.policy.level.default");
                        break;
                }
            }
        }

        getComboPassiveThreshold().setSelectedItem(selectedItem);
    }    
    
    /**
     * 
     * @return 
     */
    @Override
    public String getHelpIndex() {
        return "ui.dialogs.scanpolicy";
    }
}
