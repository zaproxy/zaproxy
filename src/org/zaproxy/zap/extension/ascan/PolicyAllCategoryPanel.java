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
// ZAP: 2016/01/19 Allow to obtain the ScanPolicy
// ZAP: 2016/04/04 Use StatusUI in scanners' dialogues
// ZAP: 2016/07/25 Use new AllCategoryTableModel's constructor
package org.zaproxy.zap.extension.ascan;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.TableColumn;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

public class PolicyAllCategoryPanel extends AbstractParamPanel {

    //private static final String ILLEGAL_CHRS = "/`?*\\<>|\":\t\n\r";

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(PolicyAllCategoryPanel.class);
    
    private ZapTextField policyName = null;
    private JTable tableTest = null;
    private JScrollPane jScrollPane = null;
    private AllCategoryTableModel allCategoryTableModel = null;
    private JComboBox<String> policySelector = null;
    private JComboBox<String> comboThreshold = null;
    private JLabel labelThresholdNotes = null;
    private JComboBox<String> comboStrength = null;
    private JLabel labelStrengthNotes = null;
    private JComboBox<String> applyToThreshold = null;
    private JComboBox<String> applyToStrength = null;
    private JComboBox<String> applyToThresholdTarget = null;
    private JComboBox<String> applyToStrengthTarget = null;

    private ExtensionActiveScan extension;
    private ScanPolicy policy;
    private String currentName;
    private boolean switchable = false;
    
    private static final int[] width = {300, 100, 100};

    public PolicyAllCategoryPanel(Window parent, ExtensionActiveScan extension, ScanPolicy policy) {
    	this(parent, extension, policy, false);
    }
    
    public PolicyAllCategoryPanel(Window parent, ExtensionActiveScan extension, ScanPolicy policy, boolean switchable) {
        super();
        this.extension = extension;
        this.policy = policy;
       	this.currentName = policy.getName();
       	this.switchable = switchable;

       	initialize();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setLayout(new GridBagLayout());
        this.setSize(375, 205);
        this.setName("categoryPanel");

        // Add Attack settings section - a copy of the options dialog
        // ---------------------------------------------
        
        int row = 0;
        this.add(new JLabel(Constant.messages.getString("ascan.policy.name.label")),
                LayoutHelper.getGBC(0, row, 1, 0.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        if (this.switchable) {
        	this.add(getPolicySelector(),
        			LayoutHelper.getGBC(1, row, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        } else {
        	this.add(getPolicyName(),
        			LayoutHelper.getGBC(1, row, 2, 1.0D, 0, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2)));
        }

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

        // 'Apply to' controls
        JPanel applyToPanel = new JPanel();
        applyToPanel.setLayout(new GridBagLayout());
        applyToPanel.add(new JLabel(Constant.messages.getString("ascan.options.apply.label")), 
        		LayoutHelper.getGBC(0, 0, 1, 0.0, new Insets(2, 2, 2, 2)));
        applyToPanel.add(getApplyToThreshold(), LayoutHelper.getGBC(1, 0, 1, 0.0));
        applyToPanel.add(new JLabel(Constant.messages.getString("ascan.options.thresholdTo.label")), 
        		LayoutHelper.getGBC(2, 0, 1, 0.0, new Insets(2, 2, 2, 2)));
        applyToPanel.add(getApplyToThresholdTarget(), LayoutHelper.getGBC(3, 0, 1, 0.0));
        applyToPanel.add(new JLabel(Constant.messages.getString("ascan.options.rules.label")), LayoutHelper.getGBC(4, 0, 1, 0.0, new Insets(2, 2, 2, 2)));
        JButton applyThresholdButton = new JButton(Constant.messages.getString("ascan.options.go.button"));
        applyThresholdButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyThreshold (strToThreshold((String)getApplyToThreshold().getSelectedItem()),
						(String)getApplyToThresholdTarget().getSelectedItem());
				getAllCategoryTableModel().fireTableDataChanged();
				
			}});
        applyToPanel.add(applyThresholdButton, LayoutHelper.getGBC(5, 0, 1, 0.0));
        applyToPanel.add(new JLabel(""), LayoutHelper.getGBC(6, 0, 1, 1.0));	// Spacer
        
        applyToPanel.add(new JLabel(Constant.messages.getString("ascan.options.apply.label")), 
        		LayoutHelper.getGBC(0, 1, 1, 0.0, new Insets(2, 2, 2, 2)));
        applyToPanel.add(getApplyToStrength(), LayoutHelper.getGBC(1, 1, 1, 0.0));
        applyToPanel.add(new JLabel(Constant.messages.getString("ascan.options.strengthTo.label")), LayoutHelper.getGBC(2, 1, 1, 0.0, new Insets(2, 2, 2, 2)));
        applyToPanel.add(getApplyToStrengthTarget(), LayoutHelper.getGBC(3, 1, 1, 0.0));
        applyToPanel.add(new JLabel(Constant.messages.getString("ascan.options.rules.label")), LayoutHelper.getGBC(4, 1, 1, 0.0, new Insets(2, 2, 2, 2)));
        JButton applyStrengthButton = new JButton(Constant.messages.getString("ascan.options.go.button"));
        applyStrengthButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applyStrength (strToStrength((String)getApplyToStrength().getSelectedItem()),
						(String)getApplyToStrengthTarget().getSelectedItem());
				getAllCategoryTableModel().fireTableDataChanged();
				
			}});
        applyToPanel.add(applyStrengthButton, LayoutHelper.getGBC(5, 1, 1, 0.0));
        applyToPanel.add(new JLabel(""), LayoutHelper.getGBC(6, 1, 1, 1.0));	// Spacer
        
        row++;
        this.add(applyToPanel,
                LayoutHelper.getGBC(0, row, 3, 0.0D, 0.0D, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0)));
    
        // Add the scrolling list of active plugin categories
        row++;
        this.add(getJScrollPane(),
                LayoutHelper.getGBC(0, row, 3, 1.0D, 1.0D, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0)));
        this.setThreshold(policy.getDefaultThreshold());
        this.setStrength(policy.getDefaultStrength());
    }
    
    public void initialise(ScanPolicy policy) {
    	this.getPolicyName().setText(policy.getName());
    }
    
    private ZapTextField getPolicyName() {
    	if (policyName == null) {
    		policyName = new ZapTextField();
    		policyName.setText(policy.getName());
    	}
    	return policyName;
    }

    private JComboBox<String> getPolicySelector() {
    	if (policySelector == null) {
    		policySelector = new JComboBox<>();
    		for (String policy : extension.getPolicyManager().getAllPolicyNames()) {
    			policySelector.addItem(policy);
    		}
    		policySelector.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					String policyName = (String) policySelector.getSelectedItem();
					if (policyName == null) {
						return;
					}

					ScanPolicy policy;
					try {
						policy = extension.getPolicyManager().getPolicy(policyName);
						if (policy != null) {
							setScanPolicy(policy);
							fireScanPolicyChanged(policy);
						}
					} catch (ConfigurationException e1) {
						logger.error(e1.getMessage(), e1);
					}
				}});
    	}
    	return policySelector;
    }
    
    /**
     * Reloads the scan policies, which will pick any new ones that have been defined and selects the policy with the given
     * name.
     * 
     * @param scanPolicyName the name of the policy that should be selected
     * @since 2.5.0
     */
    public void reloadPolicies(String scanPolicyName) {
        DefaultComboBoxModel<String> policies = new DefaultComboBoxModel<>();
        for (String policy : extension.getPolicyManager().getAllPolicyNames()) {
            policies.addElement(policy);
        }
        getPolicySelector().setModel(policies);
        getPolicySelector().setSelectedItem(scanPolicyName);
    }

    /**
     * Reloads the scan policies, which will pick any new ones that have been defined
     */
    public void reloadPolicies() {
    	// Ensure policySelector is initialized
    	Object selected = getPolicySelector().getSelectedItem(); 
    	reloadPolicies((String) selected);
    }
    
    private AlertThreshold strToThreshold(String str) {
    	if (str.equals(Constant.messages.getString("ascan.options.level.off"))) {
    		return AlertThreshold.OFF;
    	}
    	if (str.equals(Constant.messages.getString("ascan.options.level.low"))) {
    		return AlertThreshold.LOW;
    	}
    	if (str.equals(Constant.messages.getString("ascan.options.level.medium"))) {
    		return AlertThreshold.MEDIUM;
    	}
    	if (str.equals(Constant.messages.getString("ascan.options.level.high"))) {
    		return AlertThreshold.HIGH;
    	}
		return AlertThreshold.DEFAULT;
    }

    private JComboBox<String> getApplyToThreshold() {
        if (applyToThreshold == null) {
            applyToThreshold = new JComboBox<>();
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.default"));
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.off"));
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.low"));
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.medium"));
            applyToThreshold.addItem(Constant.messages.getString("ascan.options.level.high"));
        }
        return applyToThreshold;
    }

    private JComboBox<String> getApplyToThresholdTarget() {
        if (applyToThresholdTarget == null) {
            applyToThresholdTarget = createStatusComboBox();
        }
        return applyToThresholdTarget;
    }
    
    /**
     * Creates a {@code JComboBox} with scanners' statuses, "all", release, beta and alpha.
     *
     * @return a {@code JComboBox} with scanners' statuses
     */
    private JComboBox<String> createStatusComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.addItem(Constant.messages.getString("ascan.policy.table.quality.all"));
        View view = View.getSingleton();
        comboBox.addItem(view.getStatusUI(AddOn.Status.release).toString());
        comboBox.addItem(view.getStatusUI(AddOn.Status.beta).toString());
        comboBox.addItem(view.getStatusUI(AddOn.Status.alpha).toString());
        return comboBox;
    }

    private void applyThreshold(AlertThreshold threshold, String target) {
    	for (Plugin plugin : policy.getPluginFactory().getAllPlugin()) {
    		if (hasSameStatus(plugin, target)) {
    			plugin.setAlertThreshold(threshold);
    		}
    	}
    }

    /**
     * Tells whether or not the given {@code scanner} has the given {@code status}.
     * <p>
     * If the given {@code status} represents all statuses it returns always {@code true}.
     *
     * @param scanner the scanner that will be checked
     * @param status the status to check
     * @return {@code true} if it has the same status, {@code false} otherwise.
     * @see Plugin#getStatus()
     */
    private boolean hasSameStatus(Plugin scanner, String status) {
        if (status.equals(Constant.messages.getString("ascan.policy.table.quality.all"))) {
            return true;
        }
        return status.equals(View.getSingleton().getStatusUI(scanner.getStatus()).toString());
    }

    private AttackStrength strToStrength(String str) {
    	if (str.equals(Constant.messages.getString("ascan.options.strength.low"))) {
    		return AttackStrength.LOW;
    	}
    	if (str.equals(Constant.messages.getString("ascan.options.strength.medium"))) {
    		return AttackStrength.MEDIUM;
    	}
    	if (str.equals(Constant.messages.getString("ascan.options.strength.high"))) {
    		return AttackStrength.HIGH;
    	}
    	if (str.equals(Constant.messages.getString("ascan.options.strength.insane"))) {
    		return AttackStrength.INSANE;
    	}
		return AttackStrength.DEFAULT;
    }

    private JComboBox<String> getApplyToStrength() {
        if (applyToStrength == null) {
            applyToStrength = new JComboBox<>();
            applyToStrength.addItem(Constant.messages.getString("ascan.options.strength.default"));
            applyToStrength.addItem(Constant.messages.getString("ascan.options.strength.low"));
            applyToStrength.addItem(Constant.messages.getString("ascan.options.strength.medium"));
            applyToStrength.addItem(Constant.messages.getString("ascan.options.strength.high"));
            applyToStrength.addItem(Constant.messages.getString("ascan.options.strength.insane"));
        }
        return applyToStrength;
    }

    private JComboBox<String> getApplyToStrengthTarget() {
        if (applyToStrengthTarget == null) {
            applyToStrengthTarget = createStatusComboBox();
        }
        return applyToStrengthTarget;
    }
    
    private void applyStrength(AttackStrength strength, String target) {
    	for (Plugin plugin : policy.getPluginFactory().getAllPlugin()) {
    		if (hasSameStatus(plugin, target)) {
    			plugin.setAttackStrength(strength);
    		}
    	}
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
    
    /**
     * This method initializes tableTest
     *
     * @return javax.swing.JTable
     */
    private JTable getTableTest() {
        if (tableTest == null) {
            tableTest = new JTable();
            tableTest.setModel(getAllCategoryTableModel());
            tableTest.setRowHeight(DisplayUtils.getScaledSize(18));
            tableTest.setIntercellSpacing(new java.awt.Dimension(1, 1));
            tableTest.setAutoCreateRowSorter(true);
            
            //Default sort by name (column 0)
            List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>(1);
            sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
            tableTest.getRowSorter().setSortKeys(sortKeys);
            
            for (int i = 0; i < tableTest.getColumnCount()-1; i++) {
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
    
    public void setScanPolicy(ScanPolicy scanPolicy) {
    	if (! switchable) {
    		throw new InvalidParameterException("Cannot change policy if the panel has not been defined as switchable");
    	}
    	this.policy = scanPolicy;
   		this.getPolicySelector().setSelectedItem(scanPolicy.getName());
   		this.setThreshold(scanPolicy.getDefaultThreshold());
   		this.setStrength(scanPolicy.getDefaultStrength());
   		this.getAllCategoryTableModel().setPluginFactory(scanPolicy.getPluginFactory());
    }

    @Override
    public void initParam(Object obj) {
    }

    @Override
    public void validateParam(Object obj) throws Exception {
    	String newName = getPolicyName().getText();
    	if (newName.length() == 0) {
    		throw new Exception(Constant.messages.getString("ascan.policy.warn.noname"));
    	} else if (! extension.getPolicyManager().isLegalPolicyName(newName)) {
    		throw new Exception(Constant.messages.getString("ascan.policy.warn.badname", PolicyManager.ILLEGAL_POLICY_NAME_CHRS));
			
		} else if (! newName.equals(currentName)) {
			// Name changed
			if (extension.getPolicyManager().getAllPolicyNames().contains(newName)) {
	    		throw new Exception(Constant.messages.getString("ascan.policy.warn.exists"));
			}
		}
    }

	@Override
    public void saveParam(Object obj) throws Exception {
    	this.policy.setName(getPolicyName().getText());
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
            allCategoryTableModel = new AllCategoryTableModel(policy.getPluginFactory());
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
                    if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.low"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.low.label"));
                        policy.setDefaultThreshold(AlertThreshold.LOW);

                    } else if (comboThreshold.getSelectedItem().equals(Constant.messages.getString("ascan.options.level.medium"))) {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.medium.label"));
                        policy.setDefaultThreshold(AlertThreshold.MEDIUM);

                    } else {
                        getThresholdNotes().setText(Constant.messages.getString("ascan.options.level.high.label"));
                        policy.setDefaultThreshold(AlertThreshold.HIGH);
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
                        policy.setDefaultStrength(AttackStrength.LOW);

                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.medium"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.medium.label"));
                        policy.setDefaultStrength(AttackStrength.MEDIUM);

                    } else if (comboStrength.getSelectedItem().equals(Constant.messages.getString("ascan.options.strength.high"))) {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.high.label"));
                        policy.setDefaultStrength(AttackStrength.HIGH);

                    } else {
                        getStrengthNotes().setText(Constant.messages.getString("ascan.options.strength.insane.label"));
                        policy.setDefaultStrength(AttackStrength.INSANE);
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
    @Override
    public String getHelpIndex() {
        return "ui.dialogs.scanpolicy";
    }
    
    /**
     * Adds the given {@code listener} to the list that's notified of each change in the selected scan policy.
     *
     * @param listener the listener that will be added
     * @since 2.5.0
     */
    public void addScanPolicyChangedEventListener(ScanPolicyChangedEventListener listener) {
        listenerList.add(ScanPolicyChangedEventListener.class, listener);
    }

    /**
     * Removes the given {@code listener} from the list that's notified of each change in the selected scan policy.
     *
     * @param listener the listener that will be removed
     * @since 2.5.0
     */
    public void removeScanPolicyChangedEventListener(ScanPolicyChangedEventListener listener) {
        listenerList.remove(ScanPolicyChangedEventListener.class, listener);
    }

    private void fireScanPolicyChanged(ScanPolicy scanPolicy) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ScanPolicyChangedEventListener.class) {
                ((ScanPolicyChangedEventListener) listeners[i + 1]).scanPolicyChanged(scanPolicy);
            }
        }
    }

    /**
     * The listener interface for receiving notifications of changes in the selected scan policy.
     * 
     * @since 2.5.0
     * @see PolicyAllCategoryPanel#addScanPolicyChangedEventListener(ScanPolicyChangedEventListener)
     * @see PolicyAllCategoryPanel#removeScanPolicyChangedEventListener(ScanPolicyChangedEventListener)
     */
    public interface ScanPolicyChangedEventListener extends EventListener {

        /**
         * Notifies that the selected scan policy was changed.
         *
         * @param scanPolicy the new selected scan policy
         */
        public void scanPolicyChanged(ScanPolicy scanPolicy);
    }
}
