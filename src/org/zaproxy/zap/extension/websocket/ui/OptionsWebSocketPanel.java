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
package org.zaproxy.zap.extension.websocket.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;
import org.zaproxy.zap.utils.ZapTextField;

/**
 * 
 */
public class OptionsWebSocketPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JTable originsTable = null;
	private JScrollPane jScrollPane = null;
	private JPanel editPane = null;
	private OptionsWebSocketTableModel tableModel = null;
	private ZapTextField editDomain = null;
	private ZapTextField editPort = null;
	private JButton newButton = null;
	private JButton saveButton = null;
	private JButton deleteButton = null;
	private ExtensionWebSocket extension = null;
	
    public OptionsWebSocketPanel(ExtensionWebSocket extension) {
        super();
        this.extension  = extension;
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		setLayout(new GridBagLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	setSize(409, 268);
        }
        setName(Constant.messages.getString("websocket.options.title"));
        
        GridBagConstraints gbc1 = new GridBagConstraints();
        GridBagConstraints gbc2 = new GridBagConstraints();
        GridBagConstraints gbc3 = new GridBagConstraints();
        GridBagConstraints gbc4 = new GridBagConstraints();

        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();
        
        jLabel1.setText(Constant.messages.getString("websocket.options.desc1"));
        jLabel1.setPreferredSize(new java.awt.Dimension(494,60));
        jLabel1.setMinimumSize(new java.awt.Dimension(494,60));

        jLabel2.setText(Constant.messages.getString("websocket.options.desc2"));
        jLabel2.setPreferredSize(new java.awt.Dimension(494,70));
        jLabel2.setMinimumSize(new java.awt.Dimension(494,70));

        gbc1.gridx = 0;
        gbc1.gridy = 0;
        gbc1.gridheight = 1;
        gbc1.ipady = 5;
        gbc1.insets = new java.awt.Insets(10,0,5,0);
        gbc1.anchor = GridBagConstraints.NORTHWEST;
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        
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
        
        gbc4.gridx = 0;
        gbc4.gridy = 3;
        gbc4.weightx = 1.0;
        gbc4.weighty = 0.2;
        gbc4.fill = GridBagConstraints.BOTH;
        gbc4.ipadx = 0;
        gbc4.insets = new java.awt.Insets(0,0,0,0);
        gbc4.anchor = GridBagConstraints.NORTHWEST;
        
        add(jLabel1, gbc1);
        add(getJScrollPane(), gbc2);
        add(getEditPane(), gbc3);
        add(jLabel2, gbc4);
	}
	
	private GridBagConstraints getGridBackConstrants(int y, int x, double weight, boolean fullWidth) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = y;
        gbc.gridx = x;
        gbc.insets = new java.awt.Insets(0,0,0,0);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = weight;
		if (fullWidth) {
			gbc.gridwidth = 2;
		}
		return gbc;
	}
	
	private void checkUpdateButton() {
		boolean enabled = (editDomain.getText().length() > 0); 
		saveButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
	}
	
	private JPanel getEditPane() {
		if (editPane == null) {
			editPane = new JPanel();
			editPane.setBorder(
					javax.swing.BorderFactory.createTitledBorder(
							null, Constant.messages.getString("websocket.options.edit"), 
							javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
							javax.swing.border.TitledBorder.DEFAULT_POSITION, 
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), 
							java.awt.Color.black));
			editPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			editPane.setLayout(new GridBagLayout());
			
	        editDomain = new ZapTextField();

	        editPort = new ZapTextField();
	        
	    	newButton = new JButton(Constant.messages.getString("websocket.options.button.new"));
	    	saveButton = new JButton(Constant.messages.getString("websocket.options.button.save"));
	    	saveButton.setEnabled(false);
	    	deleteButton = new JButton(Constant.messages.getString("websocket.options.button.delete"));
	    	deleteButton.setEnabled(false);
	    	
	    	editDomain.getDocument().addDocumentListener(new DocumentListener() {
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
	    	
	    	editPort.getDocument().addDocumentListener(new DocumentListener() {
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
	    	
			newButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					clearEditForm();
			        editDomain.setEditable(true);
			        editDomain.grabFocus();
				}
			});
	    	
			saveButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					saveEditForm();
				}
			});
	    	
			deleteButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					deleteEditForm();
				}
			});
	    	
	    	int rowId = 0;
	        
	        editPane.add(new JLabel(Constant.messages.getString("websocket.options.label.domain")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editDomain, getGridBackConstrants(rowId++, 1, 1, true));
	        
	        editPane.add(new JLabel(Constant.messages.getString("websocket.options.label.port")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editPort, getGridBackConstrants(rowId++, 1, 1, true));
	        
	        JPanel buttons = new JPanel();
	        buttons.add(newButton);
	        buttons.add(saveButton);
	        buttons.add(deleteButton);
	        editPane.add(buttons, getGridBackConstrants(rowId++, 1, 1, true));
		}
		return editPane;
	}

    @Override
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    OptionsWebSocketParam parameters = optionsParam.getWebSocketParam();
	    getTableModel().setBlacklistedChannels(parameters.getBlacklistedChannels());
    }

    @Override
    public void validateParam(Object obj) throws Exception {

    }
    
    @Override
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    OptionsWebSocketParam parameters = optionsParam.getWebSocketParam();
	    parameters.setBlacklistedChannels(getTableModel().getBlacklistedChannels());
//	    extension.replace(getTableModel().getBlacklistedChannels());
    }

    private static int[] width = {300, 100};
    
	/**
	 * This method initializes tableAuth	
	 * 
	 * @return javax.swing.JTable	
	 */    
	private JTable getBlacklistedOriginsTable() {
		if (originsTable == null) {
			originsTable = new JTable();
			originsTable.setModel(getTableModel());
			originsTable.setRowHeight(18);
			originsTable.setIntercellSpacing(new java.awt.Dimension(1,1));
			originsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			
	        for (int i = 0; i < 2; i++) {
	            TableColumn column = originsTable.getColumnModel().getColumn(i);
	            column.setPreferredWidth(width[i]);
	        }

	        originsTable.addMouseListener(new MouseAdapter() {
	        	@Override
	        	public void mouseClicked(MouseEvent e){
	        		if (originsTable.getSelectedRow() > -1) {
	        		    CommunicationChannel channel = ((OptionsWebSocketTableModel)originsTable.getModel()).getBlacklistedChannel(originsTable.getSelectedRow());
	        			if (channel != null) {
	        				editDomain.setText(channel.getDomain());
	        				editDomain.discardAllEdits();
	        				editPort.setText(channel.getPort()+"");
	        				editPort.discardAllEdits();
	        				saveButton.setEnabled(true);
	        				deleteButton.setEnabled(true);
	        			}
	        		}
	        	}
	        });
		}
		return originsTable;
	}
	
	private void clearEditForm() {
		editDomain.setText("");
		editDomain.discardAllEdits();
		editPort.setText("");
		editPort.discardAllEdits();
		originsTable.clearSelection();
	}

	private void deleteEditForm() {
		if (originsTable.getSelectedRow() > -1) {
			((OptionsWebSocketTableModel)originsTable.getModel()).removeBlacklistedChannel(originsTable.getSelectedRow());
		}
		
		clearEditForm();
	}

	private void saveEditForm() {
		CommunicationChannel channel = new CommunicationChannel(editDomain.getText(), new Integer(editPort.getText()));

		if (originsTable.getSelectedRow() > -1) {
			((OptionsWebSocketTableModel)originsTable.getModel()).replaceBlacklistedChannel(originsTable.getSelectedRow(), channel);
		} else {
			((OptionsWebSocketTableModel)originsTable.getModel()).addBlacklistedChannel(channel);
		}
		
		clearEditForm();
	}
	
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getBlacklistedOriginsTable());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		
		return jScrollPane;
	}
		
	/**
	 * This method initializes authModel	
	 * 	
	 * @return	
	 */    
	private OptionsWebSocketTableModel getTableModel() {
		if (tableModel == null) {
			tableModel = new OptionsWebSocketTableModel();
		}
		
		return tableModel;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.websocket";
	}
}
