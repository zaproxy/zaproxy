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
package org.zaproxy.zap.extension.invoke;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableColumn;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapTextField;

public class OptionsInvokePanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JTable tableAuth = null;
	private JScrollPane jScrollPane = null;
	private JPanel editPane = null;
	private OptionsInvokeTableModel tableModel = null;
	private ZapTextField editDisplayName = null;
	private ZapTextField editFullCommand = null;
	private ZapTextField editWorkingDir = null;
	private ZapTextField editParameters = null;
	private JCheckBox editOutput = null;
	private JCheckBox editNote = null;
	private JButton chooseApp = null;
	private JButton chooseDir = null;
	private JButton newButton = null;
	private JButton saveButton = null;
	private JButton deleteButton = null;
	private ExtensionInvoke extension = null;
	
    public OptionsInvokePanel(ExtensionInvoke extension) {
        super();
        this.extension  = extension;
 		initialize();
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {

        GridBagConstraints gbc1 = new GridBagConstraints();
        GridBagConstraints gbc2 = new GridBagConstraints();
        GridBagConstraints gbc3 = new GridBagConstraints();
        GridBagConstraints gbc4 = new GridBagConstraints();

        JLabel jLabel1 = new JLabel();
        JLabel jLabel2 = new JLabel();

        this.setLayout(new GridBagLayout());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
        	this.setSize(409, 268);
        }
        this.setName(Constant.messages.getString("invoke.options.title"));
        
        jLabel1.setText(Constant.messages.getString("invoke.options.desc1"));
        jLabel1.setPreferredSize(new java.awt.Dimension(494,30));
        jLabel1.setMinimumSize(new java.awt.Dimension(494,30));

        jLabel2.setText(Constant.messages.getString("invoke.options.desc2"));
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
        
        this.add(jLabel1, gbc1);
        this.add(getJScrollPane(), gbc2);
        this.add(getEditPane(), gbc3);
        this.add(jLabel2, gbc4);
			
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
		boolean enabled = (editDisplayName.getText().length() > 0) && (editFullCommand.getText().length() > 0); 
		saveButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
	}
	
	private JPanel getEditPane() {
		if (editPane == null) {
			editPane = new JPanel();
			editPane.setBorder(
					javax.swing.BorderFactory.createTitledBorder(
							null, Constant.messages.getString("invoke.options.edit"), 
							javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
							javax.swing.border.TitledBorder.DEFAULT_POSITION, 
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11), 
							java.awt.Color.black));
			editPane.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11));
			editPane.setLayout(new GridBagLayout());
			
	        editDisplayName = new ZapTextField();
	        editFullCommand = new ZapTextField();
	        editFullCommand.setEditable(false);
	        editWorkingDir = new ZapTextField();
	        editWorkingDir.setEditable(false);
	        
	        chooseApp = new JButton(Constant.messages.getString("invoke.options.label.file")); 
			chooseApp.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
			    	JFileChooser fcCommand = new JFileChooser();
					fcCommand.setFileFilter( new FileFilter()
					{
						@Override
						public String getDescription() {
							return Constant.messages.getString("invoke.options.title");
						}
						@Override
						public boolean accept(File f) {
							return f.isDirectory() || f.canExecute() ;
						}
					} );
					if (editFullCommand.getText() != null && editFullCommand.getText().length() > 0) {
						// If theres and existing file select containing directory 
						File f = new File(editFullCommand.getText());
						fcCommand.setCurrentDirectory(f.getParentFile());
					}
					
					int state = fcCommand.showOpenDialog( null );

					if ( state == JFileChooser.APPROVE_OPTION )
					{
						editFullCommand.setText(fcCommand.getSelectedFile().toString() );
						checkUpdateButton();
					}
				}
			});

	        chooseDir = new JButton(Constant.messages.getString("invoke.options.label.dir")); 
			chooseDir.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
			    	JFileChooser fcDirectory = new JFileChooser();
			    	fcDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			    	 // disable the "All files" option.
			    	fcDirectory.setAcceptAllFileFilterUsed(false);
			    	
					if (editWorkingDir.getText() != null && editWorkingDir.getText().length() > 0) {
						// If theres and existing directory then select it 
						File f = new File(editWorkingDir.getText());
						fcDirectory.setCurrentDirectory(f);
					}
					
					int state = fcDirectory.showOpenDialog( null );

					if ( state == JFileChooser.APPROVE_OPTION )
					{
						editWorkingDir.setText(fcDirectory.getSelectedFile().toString() );
					}
				}
			});

	        editParameters = new ZapTextField();
	        editNote = new JCheckBox();
			editNote.setEnabled(false);

			editOutput = new JCheckBox();
	        editOutput.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (! editOutput.isSelected()) {
						editNote.setSelected(false);
						editNote.setEnabled(false);
					} else {
						editNote.setEnabled(true);
					}
				}});
	        
	    	newButton = new JButton(Constant.messages.getString("invoke.options.button.new"));
	    	saveButton = new JButton(Constant.messages.getString("invoke.options.button.save"));
	    	saveButton.setEnabled(false);
	    	deleteButton = new JButton(Constant.messages.getString("invoke.options.button.delete"));
	    	deleteButton.setEnabled(false);
	    	
	    	editDisplayName.getDocument().addDocumentListener(new DocumentListener() {
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
	    	
	    	editParameters.getDocument().addDocumentListener(new DocumentListener() {
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
			        editDisplayName.setEditable(true);
			        editDisplayName.grabFocus();
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
	        
	        editPane.add(new JLabel(Constant.messages.getString("invoke.options.label.name")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editDisplayName, getGridBackConstrants(rowId++, 1, 1, true));
	        
	        editPane.add(new JLabel(Constant.messages.getString("invoke.options.label.command")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editFullCommand, getGridBackConstrants(rowId++, 1, 1, false));
	        editPane.add(chooseApp, getGridBackConstrants(rowId-1, 2, 0, false));
	        
	        editPane.add(new JLabel(Constant.messages.getString("invoke.options.label.workingDir")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editWorkingDir, getGridBackConstrants(rowId++, 1, 1, false));
	        editPane.add(chooseDir, getGridBackConstrants(rowId-1, 2, 0, false));
	        
	        editPane.add(new JLabel(Constant.messages.getString("invoke.options.label.parameters")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editParameters, getGridBackConstrants(rowId++, 1, 1, true));
	        
	        editPane.add(new JLabel(Constant.messages.getString("invoke.options.label.output")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editOutput, getGridBackConstrants(rowId++, 1, 1, true));
	        
	        editPane.add(new JLabel(Constant.messages.getString("invoke.options.label.note")), 
	        		getGridBackConstrants(rowId, 0, 0, false));
	        editPane.add(editNote, getGridBackConstrants(rowId++, 1, 1, true));
	        
	        
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
	    InvokeParam invokeParam = optionsParam.getInvokeParam();
	    getTableModel().setListInvokableApps(invokeParam.getListInvoke());
    }

    @Override
    public void validateParam(Object obj) throws Exception {

    }
    
    @Override
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    InvokeParam invokeParam = optionsParam.getInvokeParam();
	    invokeParam.setListInvoke(getTableModel().getListInvokableApps());
	    extension.replaceInvokeMenus(getTableModel().getListInvokableApps());
    }

    private static int[] width = {100,300};
    
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
			
	        for (int i = 0; i < 2; i++) {
	            TableColumn column = tableAuth.getColumnModel().getColumn(i);
	            column.setPreferredWidth(width[i]);
	        }

	        tableAuth.addMouseListener(new MouseAdapter() {
	        	@Override
	        	public void mouseClicked(MouseEvent e){
	        		if (tableAuth.getSelectedRow() > -1) {
	        			InvokableApp app = ((OptionsInvokeTableModel)tableAuth.getModel()).getInvokableApp(
	        					tableAuth.getSelectedRow());
	        			if (app != null) {
	        				editDisplayName.setText(app.getDisplayName());
	        				editDisplayName.discardAllEdits();
	        				editFullCommand.setText(app.getFullCommand());
	        				if (app.getWorkingDirectory() != null) {
	        					editWorkingDir.setText(app.getWorkingDirectory().getAbsolutePath());
	        				}
	        				editParameters.setText(app.getParameters());
	        				editParameters.discardAllEdits();
	        				editOutput.setSelected(app.isCaptureOutput());
	        				if (app.isCaptureOutput()) {
	        					editNote.setEnabled(true);
		        				editNote.setSelected(app.isOutputNote());
	        				} else {
	        					editNote.setEnabled(false);
		        				editNote.setSelected(false);
	        				}
	        				saveButton.setEnabled(true);
	        				deleteButton.setEnabled(true);
	        			}
	        		}
	        	}
	        });
		}
		return tableAuth;
	}
	
	private void clearEditForm() {
		editDisplayName.setText("");
		editDisplayName.discardAllEdits();
		editFullCommand.setText("");
		editWorkingDir.setText("");
		editParameters.setText("");
		editParameters.discardAllEdits();
		editOutput.setSelected(false);
		editNote.setSelected(false);
		tableAuth.clearSelection();
	}
	

	private void deleteEditForm() {
		if (tableAuth.getSelectedRow() > -1) {
			((OptionsInvokeTableModel)tableAuth.getModel()).removeInvokableApp(tableAuth.getSelectedRow());
		}
		clearEditForm();
	}

	private void saveEditForm() {
		InvokableApp app = new InvokableApp();
		app.setDisplayName(editDisplayName.getText());
		app.setFullCommand(editFullCommand.getText());
		String workingDir = editWorkingDir.getText();
		if (workingDir != null) {
			File dir = new File(workingDir);
			if (dir.exists() && dir.isDirectory()) {
				app.setWorkingDirectory(dir);
			}
		}
		app.setParameters(editParameters.getText());
		app.setCaptureOutput(editOutput.isSelected());
		app.setOutputNote(editNote.isSelected());

		if (tableAuth.getSelectedRow() > -1) {
			((OptionsInvokeTableModel)tableAuth.getModel()).replaceInvokableApp(tableAuth.getSelectedRow(), app);
		} else {
			((OptionsInvokeTableModel)tableAuth.getModel()).addInvokableApp(app);
		}
		clearEditForm();
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
	 * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsInvokeTableModel getTableModel() {
		if (tableModel == null) {
			tableModel = new OptionsInvokeTableModel();
		}
		return tableModel;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.invokeapp";
	}

}
