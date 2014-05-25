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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.utils.ZapXmlConfiguration;
import org.zaproxy.zap.view.LayoutHelper;

public class PolicyAllCategoryPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PolicyAllCategoryPanel.class);
    
    private ZapTextField nameField = null;
    private JTable tableTest = null;
    private JScrollPane jScrollPane = null;
    private AllCategoryTableModel allCategoryTableModel = null;  //  @jve:decl-index=0:parse,visual-constraint="294,249"
    private JComboBox<String> comboThreshold = null;
    private JLabel labelThresholdNotes = null;
    private JComboBox<String> comboStrength = null;
    private JLabel labelStrengthNotes = null;
	private JButton loadButton = null;
	private JButton saveButton = null;

    // Global configuration for all passive plugins
    private JComboBox<String> comboPassiveThreshold = null;
    private JLabel labelPassiveThresholdNotes = null;
    
    private OptionsParam optionsParam;
    private ScannerParam scannerParam;
    private PluginFactory pluginFactory;
    private ExtensionPassiveScan pscan = null;

    private static final int[] width = {300, 100, 100};

    /**
     *
     */
    public PolicyAllCategoryPanel(OptionsParam optionsParam, ScannerParam scannerParam, 
    		PluginFactory pluginFactory, ExtensionPassiveScan pscan) {
        super();
        this.optionsParam = optionsParam;
        this.scannerParam = scannerParam;
        this.pluginFactory = pluginFactory;
        this.pscan = pscan;
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
        
        int row = 0;
        this.add(new JLabel(Constant.messages.getString("ascan.policy.name.label")),
                LayoutHelper.getGBC(0, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getNameField(),
                LayoutHelper.getGBC(1, row, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

        row++;
        this.add(new JLabel(Constant.messages.getString("ascan.options.level.label")),
                LayoutHelper.getGBC(0, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getComboThreshold(),
                LayoutHelper.getGBC(1, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getThresholdNotes(),
                LayoutHelper.getGBC(2, row, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

        row++;
        this.add(new JLabel(Constant.messages.getString("ascan.options.strength.label")),
                LayoutHelper.getGBC(0, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getComboStrength(),
                LayoutHelper.getGBC(1, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        this.add(getStrengthNotes(),
                LayoutHelper.getGBC(2, row, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

        if (pscan != null) {
            row++;
            this.add(new JLabel(Constant.messages.getString("pscan.options.level.label")),
                    LayoutHelper.getGBC(0, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            this.add(getComboPassiveThreshold(),
                    LayoutHelper.getGBC(1, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
            this.add(getPassiveThresholdNotes(),
                    LayoutHelper.getGBC(2, row, 1, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

            this.updatePassiveThreshold();
        }
    
        // Add the scrolling list of active plugin categories
        row++;
        this.add(getJScrollPane(),
                LayoutHelper.getGBC(0, row, 3, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0)));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        buttonPanel.add(getLoadButton());
        buttonPanel.add(getSaveButton());
        row++;
        this.add(buttonPanel,
                LayoutHelper.getGBC(0, row, 3, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));

        this.setThreshold(scannerParam.getAlertThreshold());
        this.setStrength(scannerParam.getAttackStrength());
    }
    
    private void setThreshold(AlertThreshold threshold) {
            getComboThreshold().setSelectedItem(
            		Constant.messages.getString("ascan.options.level." + threshold.name().toLowerCase()));
            getThresholdNotes().setText(
            		Constant.messages.getString("ascan.options.level." + threshold.name().toLowerCase() + ".label"));
    }
    
    private void setStrength(AttackStrength strength) {
        getComboStrength().setSelectedItem(
        		Constant.messages.getString("ascan.options.strength." + strength.name().toLowerCase()));
        getStrengthNotes().setText(
        		Constant.messages.getString("ascan.options.strength." + strength.name().toLowerCase() + ".label"));
    }

    private ZapTextField getNameField() {
    	if (nameField == null) {
    		nameField = new ZapTextField(
    				optionsParam.getConfig().getString("policy", Constant.messages.getString("ascan.policy.name.default")));
    	}
    	return nameField;
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
            tableTest.setAutoCreateRowSorter(true);
            
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
		optionsParam.getConfig().setProperty("policy", this.getNameField().getText());
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
    private AllCategoryTableModel getAllCategoryTableModel() {
        if (allCategoryTableModel == null) {
            allCategoryTableModel = new AllCategoryTableModel();
            allCategoryTableModel.setTable(this.pluginFactory.getAllPlugin());
        }
        
        return allCategoryTableModel;
    }

    private JLabel getThresholdNotes() {
        if (labelThresholdNotes == null) {
            labelThresholdNotes = new JLabel();
        }
        
        return labelThresholdNotes;
    }

    private JLabel getPassiveThresholdNotes() {
        if (labelPassiveThresholdNotes == null) {
            labelPassiveThresholdNotes = new JLabel();
        }
        
        return labelPassiveThresholdNotes;
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
                    if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.low"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
                        scannerParam.setAlertThreshold(AlertThreshold.LOW);

                    } else if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.medium"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
                        scannerParam.setAlertThreshold(AlertThreshold.MEDIUM);

                    } else {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
                        scannerParam.setAlertThreshold(AlertThreshold.HIGH);
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
                    if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.low"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.low.label"));
                        scannerParam.setAttackStrength(AttackStrength.LOW);

                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.medium"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.medium.label"));
                        scannerParam.setAttackStrength(AttackStrength.MEDIUM);

                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.high"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.high.label"));
                        scannerParam.setAttackStrength(AttackStrength.HIGH);

                    } else {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.insane.label"));
                        scannerParam.setAttackStrength(AttackStrength.INSANE);
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
                    if ((pscan != null) && (value != null) && !value.isEmpty()) {
                        // Set the value for all passive plugins
                        if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.low"))) {
                            getPassiveThresholdNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
                            pscan.setAllScannerThreshold(AlertThreshold.LOW);

                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.medium"))) {
                            getPassiveThresholdNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
                            pscan.setAllScannerThreshold(AlertThreshold.MEDIUM);
                            
                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.high"))) {
                            getPassiveThresholdNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
                            pscan.setAllScannerThreshold(AlertThreshold.HIGH);
                            
                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.off"))) {
                            getPassiveThresholdNotes().setText("");
                            pscan.setAllScannerThreshold(AlertThreshold.OFF);
                            
                        } else if (comboPassiveThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.policy.level.default"))) {
                            getPassiveThresholdNotes().setText("");
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
        if (pscan != null) {
            // Set the correct alert threshold value common to all plugins
            String selectedItem = "";
            AlertThreshold at = pscan.getAllScannerThreshold();
            if (at != null) {
                selectedItem = Constant.messages.getString("ascan.policy.level." + at.name().toLowerCase());
            }
            getComboPassiveThreshold().setSelectedItem(selectedItem);
        }
    }
    
    private File getDefaultPolicyDirectory() {
    	return new File(optionsParam.getConfig().getString("policy.dir", 
    			Model.getSingleton().getOptionsParam().getUserDirectory().getAbsolutePath()));
    }
    
    private void setDefaultPolicyDirectory(File dir) {
    	optionsParam.getConfig().setProperty("policy.dir", dir.getAbsolutePath());
    }
    
	private JButton getLoadButton() {
		if (loadButton == null) {
			loadButton = new JButton(Constant.messages.getString("ascan.policy.button.load"));

			loadButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    JFileChooser chooser = new JFileChooser(getDefaultPolicyDirectory());
				    chooser.setFileFilter(new FileFilter() {
				           @Override
				           public boolean accept(File file) {
				                if (file.isDirectory()) {
				                    return true;
				                } else if (file.isFile() && file.getName().endsWith(".policy")) {
				                    return true;
				                }
				                return false;
				            }
				           @Override
				           public String getDescription() {
				               return Constant.messages.getString("file.format.zap.policy");
				           }
				    });
					File file = null;
				    int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
				    if(rc == JFileChooser.APPROVE_OPTION) {
			    		file = chooser.getSelectedFile();
			    		if (file == null) {
			    			return;
			    		}
			    		setDefaultPolicyDirectory(file.getParentFile());
			    		// Load settings from the config file
			    		try {
			    			ZapXmlConfiguration conf = new ZapXmlConfiguration(file); 
			    			getNameField().setText(conf.getString("policy", ""));

							pluginFactory.loadFrom(conf);

					        if (pscan != null) {
					        	pscan.loadFrom(conf);
					        }
					        
					        setThreshold(AlertThreshold.valueOf(conf.getString("scanner.level", AlertThreshold.MEDIUM.name())));
					        setStrength(AttackStrength.valueOf(conf.getString("scanner.strength", AttackStrength.MEDIUM.name())));

							// Update the UI
							updatePassiveThreshold();
							getAllCategoryTableModel().fireTableDataChanged();
							
						} catch (ConfigurationException e1) {
							logger.error(e1.getMessage(), e1);
							View.getSingleton().showWarningDialog(Constant.messages.getString("ascan.policy.load.error"));
						}
				    }


				}
			});
		}
		return loadButton;
	}

	private JButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JButton(Constant.messages.getString("ascan.policy.button.save"));

			saveButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
				    JFileChooser chooser = new JFileChooser(getDefaultPolicyDirectory());
				    // Default to the name specified
				    File fileproposal = new File(getNameField().getText() + ".policy");
					chooser.setSelectedFile(fileproposal);
				    chooser.setFileFilter(new FileFilter() {
				           @Override
				           public boolean accept(File file) {
				                if (file.isDirectory()) {
				                    return true;
				                } else if (file.isFile() && file.getName().endsWith(".policy")) {
				                    return true;
				                }
				                return false;
				            }
				           @Override
				           public String getDescription() {
				               return Constant.messages.getString("file.format.zap.policy");
				           }
				    });
					File file = null;
				    int rc = chooser.showSaveDialog(View.getSingleton().getMainFrame());
				    if(rc == JFileChooser.APPROVE_OPTION) {
			    		file = chooser.getSelectedFile();
			    		if (file == null) {
			    			return;
			    		}
			    		setDefaultPolicyDirectory(file.getParentFile());
			    		try {
					    	ZapXmlConfiguration conf = new ZapXmlConfiguration();
					    	conf.setProperty("policy", getNameField().getText());

							pluginFactory.saveTo(conf);
							
					        if (pscan != null) {
					        	pscan.saveTo(conf);
					        }
							
					        conf.setProperty("scanner.level", scannerParam.getAlertThreshold().name());
					        conf.setProperty("scanner.strength", scannerParam.getAttackStrength().name());

					    	conf.save(file);
							
						} catch (ConfigurationException e1) {
							logger.error(e1.getMessage(), e1);
							View.getSingleton().showWarningDialog(Constant.messages.getString("ascan.policy.save.error"));
						}
				    }

				}
			});
		}
		return saveButton;
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
