/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.FilteredJTextField;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsPassiveScan extends AbstractParamPanel {

	private static final String NAME_VALID_CHRS = 
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";
	private static final long serialVersionUID = 1L;
	private ExtensionPassiveScan extension = null;
	private JTable tableAuth = null;
	private JScrollPane jScrollPane = null;
	private JPanel editPane = null;
	private ProxyListenerPassiveScan proxyListenerPassiveScan = null;
	private OptionsPassiveScanTableModel tableModel = null;
	private FilteredJTextField editName = null;
	private JTextField editType = null;
	private JTextField editConfig = null;
	private JTextField editRequestUrlRegex = null;
	private JTextField editRequestHeaderRegex = null;
	private JTextField editResponseHeaderRegex = null;
	private JTextField editResponseBodyRegex = null;
	private JCheckBox editEnabled = null;
	private JButton newPScan = null;
	private JButton savePScan = null;
	
    /**
     * @param extension 
     * @param proxyListenerPassiveScan 
     * 
     */
    public OptionsPassiveScan(ExtensionPassiveScan extension, ProxyListenerPassiveScan proxyListenerPassiveScan) {
        super();
        this.extension = extension;
        this.proxyListenerPassiveScan = proxyListenerPassiveScan; 
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

        GridBagConstraints gbc1 = new GridBagConstraints();
        GridBagConstraints gbc2 = new GridBagConstraints();
        GridBagConstraints gbc3 = new GridBagConstraints();

        JLabel jLabel = new JLabel();

        this.setLayout(new GridBagLayout());
        this.setSize(409, 268);
        this.setName(Constant.messages.getString("pscan.options.name"));
        jLabel.setText(Constant.messages.getString("pscan.options.header"));
        jLabel.setPreferredSize(new java.awt.Dimension(494,30));
        jLabel.setMinimumSize(new java.awt.Dimension(494,30));
        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.gridheight = 1;
        gbc1.ipady = 5;
        gbc1.insets = new java.awt.Insets(10,0,5,0);
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        //gbc1.weightx = 1.0D;
        
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.weightx = 1.0;
        gbc2.weighty = 1.0;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.ipadx = 0;
        gbc2.insets = new java.awt.Insets(0,0,0,0);
        gbc2.anchor = GridBagConstraints.NORTHWEST;
        
        gbc3.gridx = 0;
        gbc3.gridy = 2;
        gbc3.weightx = 1.0;
        //gbc3.weighty = 1.0;
        gbc3.fill = GridBagConstraints.BOTH;
        gbc3.ipadx = 0;
        gbc3.insets = new java.awt.Insets(0,0,0,0);
        gbc3.anchor = GridBagConstraints.NORTHWEST;
        
        this.add(jLabel, gbc1);
        this.add(getJScrollPane(), gbc2);
        this.add(getEditPane(), gbc3);
			
	}
	
	private GridBagConstraints getGridBackConstrants(int y, int x, double weight) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = y;
        gbc.gridx = x;
        gbc.insets = new java.awt.Insets(0,0,0,0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = weight;
//		gbc.ipadx = 50;
		return gbc;
	}
	
	private void checkUpdateButton() {
		savePScan.setEnabled(
				(editName.getText().length() > 0) && 
				(editConfig.getText().length() > 0));
	}
	
	private JPanel getEditPane() {
		if (editPane == null) {
			editPane = new JPanel();
			editPane.setBorder(
					javax.swing.BorderFactory.createTitledBorder(
							null, Constant.messages.getString("pscan.options.start"), 
							javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
							javax.swing.border.TitledBorder.DEFAULT_POSITION, 
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), 
							java.awt.Color.black));
			editPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			editPane.setLayout(new GridBagLayout());
			
	        editName = new FilteredJTextField(NAME_VALID_CHRS);
	        editName.setEditable(false);
	        editType = new JTextField();
	        editType.setEditable(false);		// For now cant change
	        editConfig = new JTextField();
	    	editRequestUrlRegex = new JTextField();
	    	editRequestHeaderRegex = new JTextField();
	    	editResponseHeaderRegex = new JTextField();
	    	editResponseBodyRegex = new JTextField();
	    	editEnabled = new JCheckBox();
	    	newPScan = new JButton(Constant.messages.getString("pscan.options.button.new"));
	    	savePScan = new JButton(Constant.messages.getString("pscan.options.button.save"));
	    	savePScan.setEnabled(false);
	    	
	    	editName.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkUpdateButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkUpdateButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkUpdateButton();
				}
	    	});
	    	
	    	editConfig.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent e) {
					checkUpdateButton();
				}
				@Override
				public void insertUpdate(DocumentEvent e) {
					checkUpdateButton();
				}
				@Override
				public void removeUpdate(DocumentEvent e) {
					checkUpdateButton();
				}
	    	});
	    	
			newPScan.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					clearEditForm();
			        editName.setEditable(true);
			        editName.grabFocus();
				}
			});
	    	
			savePScan.addActionListener(new java.awt.event.ActionListener() { 
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					saveEditForm();
				}
			});
	    	
	    	int rowId = 0;
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.name")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editName, getGridBackConstrants(rowId++, 1, 1));
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.type")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editType, getGridBackConstrants(rowId++, 1, 1));
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.config")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editConfig, getGridBackConstrants(rowId++, 1, 1));
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.editRequestUrlRegex")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editRequestUrlRegex, getGridBackConstrants(rowId++, 1, 1));
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.editRequestHeaderRegex")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editRequestHeaderRegex, getGridBackConstrants(rowId++, 1, 1));
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.editResponseHeaderRegex")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editResponseHeaderRegex, getGridBackConstrants(rowId++, 1, 1));
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.editResponseBodyRegex")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editResponseBodyRegex, getGridBackConstrants(rowId++, 1, 1));
	        
	        editPane.add(new JLabel(Constant.messages.getString("pscan.options.label.enabled")), 
	        		getGridBackConstrants(rowId, 0, 0));
	        editPane.add(editEnabled, getGridBackConstrants(rowId++, 1, 1));
	        
	        JPanel buttons = new JPanel();
	        buttons.add(newPScan);
	        buttons.add(savePScan);
	        editPane.add(buttons, getGridBackConstrants(rowId++, 1, 1));
	        
		}
		return editPane;
	}
    /* (non-Javadoc)
     * @see com.proofsecure.paros.view.AbstractParamPanel#initParam(java.lang.Object)
     */
    public void initParam(Object obj) {
	    getTableModel().setScanDefns(proxyListenerPassiveScan.list());
    }

    /* (non-Javadoc)
     * @see com.proofsecure.paros.view.AbstractParamPanel#validateParam(java.lang.Object)
     */
    public void validateParam(Object obj) throws Exception {

    }
    
    /* (non-Javadoc)
     * @see com.proofsecure.paros.view.AbstractParamPanel#saveParam(java.lang.Object)
     */
    public void saveParam(Object obj) throws Exception {
	    // OptionsParam optionsParam = (OptionsParam) obj;
	    // ConnectionParam connectionParam = optionsParam.getConnectionParam();
	    // connectionParam.setListAuth(getTableModel().getListAuth());
	    
    }

    //private static int[] width = {360,55,30};
    private static int[] width = {360,55,30};
    
	/**
	 * This method initializes tableAuth	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getTableAuth() {
		if (tableAuth == null) {
			tableAuth = new JTable();
			tableAuth.setModel(getTableModel());
			tableAuth.setRowHeight(18);
			tableAuth.setIntercellSpacing(new java.awt.Dimension(1,1));
			tableAuth.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
			// Set up the sortable columns
			TableRowSorter<TableModel> sorter 
				= new TableRowSorter<TableModel>(tableAuth.getModel());
			List <RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
			sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
			sorter.setSortKeys(sortKeys); 
			sorter.setSortable(1, false);	// enums not handled well
			tableAuth.setRowSorter(sorter);
			
	        for (int i = 0; i < 2; i++) {
	            TableColumn column = tableAuth.getColumnModel().getColumn(i);
	            column.setPreferredWidth(width[i]);
	        }

	        tableAuth.addMouseListener(new MouseAdapter() {
	        	public void mouseClicked(MouseEvent e){
	        		if (tableAuth.getSelectedRow() > -1) {
	        			PassiveScanDefn defn = proxyListenerPassiveScan.getDefn(
	        					tableAuth.convertRowIndexToModel(
	        							tableAuth.getSelectedRow()));
	        			if (defn != null) {
	        				editName.setText(defn.getName());
	        				editType.setText(defn.getType().name());
	        				editConfig.setText(defn.getConfig());
	        		    	editRequestUrlRegex.setText(defn.getRequestUrlRegex());
	        		    	editRequestHeaderRegex.setText(defn.getRequestHeaderRegex());
	        		    	editResponseHeaderRegex.setText(defn.getResponseHeaderRegex());
	        		    	editResponseBodyRegex.setText(defn.getResponseBodyRegex());
	        		    	editEnabled.setSelected(defn.isEnabled());
	        		    	// Cant change existing names
	        		    	editName.setEditable(false);
	        			}
	        		}
	        	}
	        });
		}
		return tableAuth;
	}
	
	private void clearEditForm() {
		editName.setText("");
		editType.setText(PassiveScanDefn.TYPE.TAG.name());
		editConfig.setText("");
    	editRequestUrlRegex.setText("");
    	editRequestHeaderRegex.setText("");
    	editResponseHeaderRegex.setText("");
    	editResponseBodyRegex.setText("");
    	editEnabled.setSelected(true);
    	editName.setEditable(true);
	}
	
	private void saveEditForm() {
		boolean isNew = false;
		
		PassiveScanDefn defn = proxyListenerPassiveScan.getDefn(editName.getText());
		
		// Check mandatory fields are e
		
		if (defn == null) {
			// New one
			defn = new PassiveScanDefn(
					editName.getText(), PassiveScanDefn.TYPE.TAG, editConfig.getText());
			isNew = true;
		}
		// TODO validate params, eg config is mandatory
		defn.setConfig(editConfig.getText());
		defn.setRequestHeaderRegex(editRequestHeaderRegex.getText());
		defn.setRequestUrlRegex(editRequestUrlRegex.getText());
		defn.setResponseHeaderRegex(editResponseHeaderRegex.getText());
		defn.setResponseBodyRegex(editResponseBodyRegex.getText());
		defn.setEnabled(editEnabled.isSelected());
		proxyListenerPassiveScan.save(defn);
		
	    getTableModel().setScanDefns(proxyListenerPassiveScan.list());

	    // Save in the config file
	    if (isNew) {
	    	extension.add(defn);
	    } else {
	    	extension.save(defn);
	    }
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableAuth());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}
	
		
	/**
	 * This method initializes authModel	
	 * 	
	 * @return com.proofsecure.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsPassiveScanTableModel getTableModel() {
		if (tableModel == null) {
			tableModel = new OptionsPassiveScanTableModel();
		}
		return tableModel;
	}

}
