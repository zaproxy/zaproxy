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
package org.zaproxy.zap.extension.bruteforce;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.filechooser.FileFilter;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.FileCopier;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.ZapTextField;

public class OptionsBruteForcePanel extends AbstractParamPanel {

	private ExtensionBruteForce extension = null;
	private static final long serialVersionUID = 1L;
	private JPanel panelPortScan = null;
	private JCheckBox checkBoxRecursive = null;
	private JComboBox<ForcedBrowseFile> defaultFileList = null;
	private JButton addFileButton = null;
	private JCheckBox checkBoxBrowseFiles = null;
	private ZapTextField txtFileExtensions = null;

	public OptionsBruteForcePanel(ExtensionBruteForce extension) {
        super();
        this.extension = extension;
 		initialize();
   }
    
	private JSlider sliderThreadsPerScan = null;
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("bruteforce.options.title"));
        this.setSize(314, 345);
        this.add(getPanelPortScan(), getPanelPortScan().getName());
	}

	/**
	 * This method initializes panelSpider	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelPortScan() {
		if (panelPortScan == null) {

			panelPortScan = new JPanel();
			JLabel jLabel1 = new JLabel();
			JLabel jLabel2 = new JLabel();
			JLabel jLabel3 = new JLabel();
			JLabel jLabelx = new JLabel();
			JLabel jLabelExtensions = new JLabel();

			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5a = new GridBagConstraints();
			GridBagConstraints gridBagConstraints5b = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6a = new GridBagConstraints();
			GridBagConstraints gridBagConstraints6b = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsX = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsBrowseFiles = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsFileExtensionsLabel = new GridBagConstraints();
			GridBagConstraints gridBagConstraintsFileExtensionsList = new GridBagConstraints();

			GridBagConstraints checkBoxGridBagConstraints = new GridBagConstraints();

			panelPortScan.setLayout(new GridBagLayout());
			panelPortScan.setSize(114, 132);
			panelPortScan.setName(BruteForceParam.EMPTY_STRING);
			jLabel1.setText(Constant.messages.getString("bruteforce.options.label.threads"));
			jLabel2.setText(Constant.messages.getString("bruteforce.options.label.defaultfile"));
			jLabel3.setText(Constant.messages.getString("bruteforce.options.label.addfile"));
			jLabelExtensions.setText(Constant.messages.getString("bruteforce.options.label.fileextensions"));
		
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.gridy = 2;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.ipady = 0;
			gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints3.insets = new Insets(2,2,2,2);
			gridBagConstraints3.weightx = 1.0D;
			gridBagConstraints3.gridwidth = 2;
			
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.gridy = 3;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.ipady = 0;
			gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints4.insets = new Insets(2,2,2,2);
			gridBagConstraints4.gridwidth = 2;
			
			checkBoxGridBagConstraints.gridx = 0;
			checkBoxGridBagConstraints.gridy = 4;
			checkBoxGridBagConstraints.weightx = 1.0;
			checkBoxGridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			checkBoxGridBagConstraints.ipadx = 0;
			checkBoxGridBagConstraints.ipady = 0;
			checkBoxGridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			checkBoxGridBagConstraints.insets = new Insets(2,2,2,2);
			checkBoxGridBagConstraints.gridwidth = 2;

			gridBagConstraints5a.gridx = 0;
			gridBagConstraints5a.gridy = 5;
			gridBagConstraints5a.weightx = 1.0;
			gridBagConstraints5a.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5a.ipadx = 0;
			gridBagConstraints5a.ipady = 0;
			gridBagConstraints5a.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5a.insets = new Insets(2,2,2,2);
			gridBagConstraints5a.gridwidth = 1;
			
			gridBagConstraints5b.gridx = 1;
			gridBagConstraints5b.gridy = 5;
			gridBagConstraints5b.weightx = 1.0;
			gridBagConstraints5b.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5b.ipadx = 0;
			gridBagConstraints5b.ipady = 0;
			gridBagConstraints5b.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5b.insets = new Insets(2,2,2,2);
			gridBagConstraints5b.gridwidth = 1;
			
			gridBagConstraints6a.gridx = 0;
			gridBagConstraints6a.gridy = 6;
			gridBagConstraints6a.weightx = 1.0;
			gridBagConstraints6a.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6a.ipadx = 0;
			gridBagConstraints6a.ipady = 0;
			gridBagConstraints6a.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints6a.insets = new Insets(2,2,2,2);
			gridBagConstraints6a.gridwidth = 1;
			
			gridBagConstraints6b.gridx = 1;
			gridBagConstraints6b.gridy = 6;
			gridBagConstraints6b.weightx = 1.0;
			gridBagConstraints6b.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints6b.ipadx = 0;
			gridBagConstraints6b.ipady = 0;
			gridBagConstraints6b.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints6b.insets = new Insets(2,2,2,2);
			gridBagConstraints6b.gridwidth = 1;
			
			gridBagConstraintsBrowseFiles.gridx = 0;
			gridBagConstraintsBrowseFiles.gridy = 7;
			gridBagConstraintsBrowseFiles.weightx = 1.0;
			gridBagConstraintsBrowseFiles.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraintsBrowseFiles.ipadx = 0;
			gridBagConstraintsBrowseFiles.ipady = 0;
			gridBagConstraintsBrowseFiles.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraintsBrowseFiles.insets = new Insets(2,2,2,2);
			gridBagConstraintsBrowseFiles.gridwidth = 2;
			
			gridBagConstraintsFileExtensionsLabel.gridx = 0;
			gridBagConstraintsFileExtensionsLabel.gridy = 8;
			gridBagConstraintsFileExtensionsLabel.weightx = 1.0;
			gridBagConstraintsFileExtensionsLabel.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraintsFileExtensionsLabel.ipadx = 0;
			gridBagConstraintsFileExtensionsLabel.ipady = 0;
			gridBagConstraintsFileExtensionsLabel.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraintsFileExtensionsLabel.insets = new Insets(2,2,2,2);
			gridBagConstraintsFileExtensionsLabel.gridwidth = 1;
			
			gridBagConstraintsFileExtensionsList.gridx = 1;
			gridBagConstraintsFileExtensionsList.gridy = 8;
			gridBagConstraintsFileExtensionsList.weightx = 1.0;
			gridBagConstraintsFileExtensionsList.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraintsFileExtensionsList.ipadx = 0;
			gridBagConstraintsFileExtensionsList.ipady = 0;
			gridBagConstraintsFileExtensionsList.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraintsFileExtensionsList.insets = new Insets(2,2,2,2);
			gridBagConstraintsFileExtensionsList.gridwidth = 1;			
			
			gridBagConstraintsX.gridx = 0;
			gridBagConstraintsX.gridy = 10;
			gridBagConstraintsX.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraintsX.fill = GridBagConstraints.BOTH;
			gridBagConstraintsX.insets = new Insets(2,2,2,2);
			gridBagConstraintsX.weightx = 1.0D;
			gridBagConstraintsX.weighty = 1.0D;
			gridBagConstraintsX.gridwidth = 2;
			
			jLabelx.setText(BruteForceParam.EMPTY_STRING);
			panelPortScan.add(jLabel1, gridBagConstraints3);
			panelPortScan.add(getSliderThreadsPerScan(), gridBagConstraints4);
			panelPortScan.add(getCheckBoxRecursive(), checkBoxGridBagConstraints);
			panelPortScan.add(jLabel2, gridBagConstraints5a);
			panelPortScan.add(getDefaultFileList(), gridBagConstraints5b);
			panelPortScan.add(jLabel3, gridBagConstraints6a);
			panelPortScan.add(getAddFileButton(), gridBagConstraints6b);
			panelPortScan.add(getCheckBoxBrowseFiles(), gridBagConstraintsBrowseFiles);
			panelPortScan.add(jLabelExtensions, gridBagConstraintsFileExtensionsLabel);
			panelPortScan.add(getTxtFileExtensions(), gridBagConstraintsFileExtensionsList);
			panelPortScan.add(jLabelx, gridBagConstraintsX);
		}
		return panelPortScan;
	}

	private JComboBox<ForcedBrowseFile> getDefaultFileList() {
		if (defaultFileList == null) {
			defaultFileList = new JComboBox<>();
			refreshFileList();
		}
		return defaultFileList;
	}
	
	private void refreshFileList() {
		ForcedBrowseFile selectedDefaultFile = (ForcedBrowseFile) defaultFileList.getSelectedItem();
		defaultFileList.removeAllItems();
		List<ForcedBrowseFile> files = extension.getFileList();
		for (ForcedBrowseFile file : files) {
			defaultFileList.addItem(file);
		}
		if (selectedDefaultFile != null) {
			// Keep the same selection
			defaultFileList.setSelectedItem(selectedDefaultFile);
		}
	}


	private JCheckBox getCheckBoxRecursive() {
		if (checkBoxRecursive == null) {
			checkBoxRecursive = new JCheckBox();
			checkBoxRecursive.setText(Constant.messages.getString("bruteforce.options.label.recursive"));
			checkBoxRecursive.setSelected(BruteForceParam.DEFAULT_RECURSIVE);
		}
		return checkBoxRecursive;
	}
	 
	@Override
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    BruteForceParam param = (BruteForceParam) options.getParamSet(BruteForceParam.class);
	    if (param == null) {
		    getSliderThreadsPerScan().setValue(BruteForceParam.DEFAULT_THREAD_PER_SCAN);
		    getCheckBoxRecursive().setSelected(BruteForceParam.DEFAULT_RECURSIVE);
		    getCheckBoxBrowseFiles().setSelected(BruteForceParam.DEFAULT_BROWSE_FILES);
		    getTxtFileExtensions().setEnabled(BruteForceParam.DEFAULT_BROWSE_FILES);		    
	    } else {
		    getSliderThreadsPerScan().setValue(param.getThreadPerScan());
		    getCheckBoxRecursive().setSelected(param.getRecursive());
		    getDefaultFileList().setSelectedItem(param.getDefaultFile());
		    getCheckBoxBrowseFiles().setSelected(param.isBrowseFiles());
		    getTxtFileExtensions().setEnabled(param.isBrowseFiles());
		    getTxtFileExtensions().setText(param.getFileExtensions());		    
	    }
	    
	    getTxtFileExtensions().discardAllEdits();
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    BruteForceParam param = (BruteForceParam) options.getParamSet(BruteForceParam.class);
	    if (param == null) {
	    	param = new BruteForceParam();
	    	options.addParamSet(param);
	    }
	   	param.setThreadPerScan(getSliderThreadsPerScan().getValue());
	   	param.setRecursive(getCheckBoxRecursive().isSelected());
	   	
	   	ForcedBrowseFile selectedDefaultFile = (ForcedBrowseFile) getDefaultFileList().getSelectedItem();
	   	param.setDefaultFile(selectedDefaultFile);
	   	extension.setDefaultFile(selectedDefaultFile);
	   	
	   	param.setBrowseFiles(getCheckBoxBrowseFiles().isSelected());
	   	if (getTxtFileExtensions().getText() != null) {
	   		param.setFileExtensions(getTxtFileExtensions().getText());
	   	} else {
	   		param.setFileExtensions(BruteForceParam.EMPTY_STRING);
	   	}
	}
	
	/**
	 * This method initializes sliderThreadsPerHost	
	 * 	
	 * @return JSlider	
	 */    
	private JSlider getSliderThreadsPerScan() {
		if (sliderThreadsPerScan == null) {
			sliderThreadsPerScan = new JSlider();
			sliderThreadsPerScan.setMaximum(20);	// TODO put in Constants?
			sliderThreadsPerScan.setMinimum(1);
			sliderThreadsPerScan.setValue(BruteForceParam.DEFAULT_THREAD_PER_SCAN);
			sliderThreadsPerScan.setPaintTicks(true);
			sliderThreadsPerScan.setPaintLabels(true);
			sliderThreadsPerScan.setMinorTickSpacing(1);
			sliderThreadsPerScan.setMajorTickSpacing(1);
			sliderThreadsPerScan.setSnapToTicks(true);
			sliderThreadsPerScan.setPaintTrack(true);
		}
		return sliderThreadsPerScan;
	}

	private JButton getAddFileButton() {
		if (addFileButton == null) {
	        addFileButton = new JButton(Constant.messages.getString("bruteforce.options.button.addfile")); 
			addFileButton.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
			    	JFileChooser fcCommand = new JFileChooser();
					fcCommand.setFileFilter( new FileFilter() {
						@Override
						public String getDescription() {
							return Constant.messages.getString("bruteforce.options.title");
						}
						@Override
						public boolean accept(File f) {
							return true;
						}
					} );

					// Copy the file into the 'home' dirbuster directory
					int state = fcCommand.showOpenDialog( null );

					if ( state == JFileChooser.APPROVE_OPTION )
					{
				    	FileCopier copier = new FileCopier();
				    	File newFile = new File(Constant.getInstance().DIRBUSTER_CUSTOM_DIR + File.separator + 
				    							fcCommand.getSelectedFile().getName());
				    	if (newFile.exists() || extension.getFileList().contains(newFile.getName())) {
							View.getSingleton().showWarningDialog(Constant.messages.getString("bruteforce.add.duplicate.error"));
				    		
				    	} else if ( ! newFile.getParentFile().canWrite()) {
							View.getSingleton().showWarningDialog(Constant.messages.getString("bruteforce.add.dirperms.error") +
									newFile.getParentFile().getAbsolutePath());
				    		
				    	} else {
				    		try {
								copier.copy(fcCommand.getSelectedFile(), newFile);
								// Refresh list in panel
								extension.refreshFileList();
								// Refresh the list in this popup
								refreshFileList();
								View.getSingleton().showMessageDialog(Constant.messages.getString("bruteforce.add.ok"));
							} catch (IOException e1) {
								View.getSingleton().showWarningDialog(Constant.messages.getString("bruteforce.add.fail.error") +
										e1.getMessage());
							}
				    	}
					}
				}
			});
		}
		return addFileButton;
	}
	
	private JCheckBox getCheckBoxBrowseFiles() {
		if (checkBoxBrowseFiles == null) {
			checkBoxBrowseFiles = new JCheckBox();
			checkBoxBrowseFiles.setText(Constant.messages.getString("bruteforce.options.label.browsefiles"));
			checkBoxBrowseFiles.setSelected(BruteForceParam.DEFAULT_BROWSE_FILES);
			checkBoxBrowseFiles.addActionListener(new java.awt.event.ActionListener() { 
				
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					txtFileExtensions.setEnabled(checkBoxBrowseFiles.isSelected());
				}
			});
		}
		return checkBoxBrowseFiles;
	}
	
	private ZapTextField getTxtFileExtensions() {
		if (txtFileExtensions == null) {
			txtFileExtensions = new ZapTextField();
		}
		return txtFileExtensions;
	}	

	
    public int getThreadPerScan() {
    	return this.sliderThreadsPerScan.getValue();
    }
    
    public boolean getRecursive() {
    	return this.checkBoxRecursive.isSelected();
    }
    
	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.bruteforce";
	}

} 
