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
package org.zaproxy.zap.extension.autoupdate;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.view.AbstractMultipleOptionsBaseTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsCheckForUpdatesPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelMisc = null;
	
	private JCheckBox chkCheckOnStart = null;
	private JCheckBox chkDownloadNewRelease = null;
	private JCheckBox chkCheckAddonUpdates = null;
	private JCheckBox chkInstallAddonUpdates = null;
	private JCheckBox chkInstallScannerRules = null;
	private JCheckBox chkReportReleaseAddons = null;
	private JCheckBox chkReportBetaAddons = null;
	private JCheckBox chkReportAlphaAddons = null;
	private OptionsAutoupdateDirsTableModel scriptDirModel = null;
	private JComboBox<String> downloadDir = null;

    public OptionsCheckForUpdatesPanel() {
        super();
 		initialize();
    }
    
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("cfu.options.title"));
        this.add(getPanelMisc(), getPanelMisc().getName());

	}
	/**
	 * This method initializes panelMisc	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getPanelMisc() {
		if (panelMisc == null) {
			panelMisc = new JPanel();
			panelMisc.setLayout(new GridBagLayout());
			
			JPanel zapPanel = new JPanel();
			zapPanel.setLayout(new GridBagLayout());
			zapPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.options.zap.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							FontUtils.getFont(FontUtils.Size.standard),
							java.awt.Color.black));
			
			zapPanel.add(getChkDownloadNewRelease(), LayoutHelper.getGBC(0, 1, 1, 1.0D));

			JPanel updPanel = new JPanel();
			updPanel.setLayout(new GridBagLayout());
			updPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.options.updates.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							FontUtils.getFont(FontUtils.Size.standard),
							java.awt.Color.black));
			
			updPanel.add(getChkCheckAddonUpdates(), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			updPanel.add(getChkInstallAddonUpdates(), LayoutHelper.getGBC(0, 1, 1, 1.0D));
			updPanel.add(getChkInstallScannerRules(), LayoutHelper.getGBC(0, 2, 1, 1.0D));
			
			JPanel newPanel = new JPanel();
			newPanel.setLayout(new GridBagLayout());
			newPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.options.new.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							FontUtils.getFont(FontUtils.Size.standard),
							java.awt.Color.black));

			newPanel.add(getChkReportReleaseAddons(), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			newPanel.add(getChkReportBetaAddons(), LayoutHelper.getGBC(0, 1, 1, 1.0D));
			newPanel.add(getChkReportAlphaAddons(), LayoutHelper.getGBC(0, 2, 1, 1.0D));

			JPanel dirsPanel = new JPanel();
			dirsPanel.setLayout(new GridBagLayout());
			dirsPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.options.dir.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							FontUtils.getFont(FontUtils.Size.standard),
							java.awt.Color.black));
			
			dirsPanel.add(new CfuDirsOptionsPanel(getScriptDirModel()), LayoutHelper.getGBC(0, 0, 2, 1.0D, 1.0D));
			JLabel downloadDirLabel = new JLabel(Constant.messages.getString("cfu.options.downloaddir.label"));
			downloadDirLabel.setLabelFor(getDownloadDirCombo());
			dirsPanel.add(downloadDirLabel, LayoutHelper.getGBC(0, 1, 1, 0.5D));
			dirsPanel.add(getDownloadDirCombo(), LayoutHelper.getGBC(1, 1, 1, 0.5D));

			panelMisc.add(getChkCheckOnStart(), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			panelMisc.add(zapPanel, LayoutHelper.getGBC(0, 1, 1, 1.0D));
			panelMisc.add(updPanel, LayoutHelper.getGBC(0, 2, 1, 1.0D));
			panelMisc.add(newPanel, LayoutHelper.getGBC(0, 3, 1, 1.0D));
			panelMisc.add(dirsPanel, LayoutHelper.getGBC(0, 4, 1, 1.0D, 1.0D));

		}
		return panelMisc;
	}
	
	private void setCheckBoxStates() {
		if (chkCheckOnStart.isSelected()) {
			getChkDownloadNewRelease().setEnabled(!Constant.isKali());
			getChkCheckAddonUpdates().setEnabled(true);
			getChkInstallAddonUpdates().setEnabled(this.getChkCheckAddonUpdates().isSelected());
			getChkInstallScannerRules().setEnabled(this.getChkCheckAddonUpdates().isSelected());
			getChkReportReleaseAddons().setEnabled(true);
			getChkReportBetaAddons().setEnabled(this.getChkReportReleaseAddons().isSelected());
			getChkReportAlphaAddons().setEnabled(
					this.getChkReportReleaseAddons().isSelected() && this.getChkReportBetaAddons().isSelected());
			
		} else {
			// Disable everything
			getChkDownloadNewRelease().setEnabled(false);
			getChkCheckAddonUpdates().setEnabled(false);
			getChkInstallAddonUpdates().setEnabled(false);
			getChkInstallScannerRules().setEnabled(false);
			getChkReportReleaseAddons().setEnabled(false);
			getChkReportBetaAddons().setEnabled(false);
			getChkReportAlphaAddons().setEnabled(false);
		}
	}
	
	/**
	 * This method initializes chkProcessImages	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkCheckOnStart() {
		if (chkCheckOnStart == null) {
			chkCheckOnStart = new JCheckBox();
			chkCheckOnStart.setText(Constant.messages.getString("cfu.options.startUp"));
			chkCheckOnStart.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkCheckOnStart.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			chkCheckOnStart.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setCheckBoxStates();
				}
			});
		}
		return chkCheckOnStart;
	}

	private JCheckBox getChkDownloadNewRelease() {
		if (chkDownloadNewRelease == null) {
			chkDownloadNewRelease = new JCheckBox();
			chkDownloadNewRelease.setText(Constant.messages.getString("cfu.options.downloadNewRelease"));
			chkDownloadNewRelease.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkDownloadNewRelease.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			if (Constant.isKali()) {
				chkDownloadNewRelease.setText(Constant.messages.getString("cfu.options.downloadNewRelease.kali"));
			}
		}
		return chkDownloadNewRelease;
	}
	private JCheckBox getChkCheckAddonUpdates() {
		if (chkCheckAddonUpdates == null) {
			chkCheckAddonUpdates = new JCheckBox();
			chkCheckAddonUpdates.setText(Constant.messages.getString("cfu.options.checkAddonUpdates"));
			chkCheckAddonUpdates.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkCheckAddonUpdates.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			chkCheckAddonUpdates.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setCheckBoxStates();
				}
			});
}
		return chkCheckAddonUpdates;
	}
	private JCheckBox getChkInstallAddonUpdates() {
		if (chkInstallAddonUpdates == null) {
			chkInstallAddonUpdates = new JCheckBox();
			chkInstallAddonUpdates.setText(Constant.messages.getString("cfu.options.installAddonUpdates"));
			chkInstallAddonUpdates.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkInstallAddonUpdates.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkInstallAddonUpdates;
	}
	private JCheckBox getChkInstallScannerRules() {
		if (chkInstallScannerRules == null) {
			chkInstallScannerRules = new JCheckBox();
			chkInstallScannerRules.setText(Constant.messages.getString("cfu.options.installScannerRules"));
			chkInstallScannerRules.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkInstallScannerRules.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkInstallScannerRules;
	}
	private JCheckBox getChkReportReleaseAddons() {
		if (chkReportReleaseAddons == null) {
			chkReportReleaseAddons = new JCheckBox();
			chkReportReleaseAddons.setText(Constant.messages.getString("cfu.options.reportReleaseAddons"));
			chkReportReleaseAddons.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkReportReleaseAddons.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			chkReportReleaseAddons.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setCheckBoxStates();
				}
			});
		}
		return chkReportReleaseAddons;
	}
	private JCheckBox getChkReportBetaAddons() {
		if (chkReportBetaAddons == null) {
			chkReportBetaAddons = new JCheckBox();
			chkReportBetaAddons.setText(Constant.messages.getString("cfu.options.reportBetaAddons"));
			chkReportBetaAddons.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkReportBetaAddons.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			chkReportBetaAddons.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					setCheckBoxStates();
				}
			});
		}
		return chkReportBetaAddons;
	}
	private JCheckBox getChkReportAlphaAddons() {
		if (chkReportAlphaAddons == null) {
			chkReportAlphaAddons = new JCheckBox();
			chkReportAlphaAddons.setText(Constant.messages.getString("cfu.options.reportAlphaAddons"));
			chkReportAlphaAddons.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkReportAlphaAddons.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkReportAlphaAddons;
	}
	
	private OptionsAutoupdateDirsTableModel getScriptDirModel() {
		if (scriptDirModel == null) {
			scriptDirModel = new OptionsAutoupdateDirsTableModel();
			scriptDirModel.addTableModelListener(new TableModelListener(){
				@Override
				public void tableChanged(TableModelEvent e) {
					repopulatDownloadDirs();
				}});
		}
		return scriptDirModel;
	}

	private JComboBox<String> getDownloadDirCombo() {
		if (downloadDir == null) {
			downloadDir = new JComboBox<String>();
			repopulatDownloadDirs();
		}
		return downloadDir;
	}

	private void repopulatDownloadDirs() {
		// Save for later
		Object selectedItem = getDownloadDirCombo().getSelectedItem();
		getDownloadDirCombo().removeAllItems();
		downloadDir.addItem(Constant.FOLDER_LOCAL_PLUGIN);
		for (File f : this.getScriptDirModel().getElements()) {
			if (f.canWrite()) {
				downloadDir.addItem(f.getAbsolutePath());
			}
		}
		// The selected item may no longer exist, but thats ok as it will correctly default to the first one
		getDownloadDirCombo().setSelectedItem(selectedItem);
	}
	
	@Override
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getChkCheckOnStart().setSelected(options.getCheckForUpdatesParam().isCheckOnStart());
	    getChkDownloadNewRelease().setSelected(options.getCheckForUpdatesParam().isDownloadNewRelease());
		getChkCheckAddonUpdates().setSelected(options.getCheckForUpdatesParam().isCheckAddonUpdates());
		getChkInstallAddonUpdates().setSelected(options.getCheckForUpdatesParam().isInstallAddonUpdates());
		getChkInstallScannerRules().setSelected(options.getCheckForUpdatesParam().isInstallScannerRules());
		getChkReportReleaseAddons().setSelected(options.getCheckForUpdatesParam().isReportReleaseAddons());
		getChkReportBetaAddons().setSelected(options.getCheckForUpdatesParam().isReportBetaAddons());
		getChkReportAlphaAddons().setSelected(options.getCheckForUpdatesParam().isReportAlphaAddons());
		getScriptDirModel().setFiles(options.getCheckForUpdatesParam().getAddonDirectories());
		getDownloadDirCombo().setSelectedItem(options.getCheckForUpdatesParam().getDownloadDirectory().getAbsolutePath());
		
		setCheckBoxStates();
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    options.getCheckForUpdatesParam().setCheckOnStart(getChkCheckOnStart().isSelected());
	    options.getCheckForUpdatesParam().setDownloadNewRelease(getChkDownloadNewRelease().isSelected());
		options.getCheckForUpdatesParam().setCheckAddonUpdates(getChkCheckAddonUpdates().isSelected());
		options.getCheckForUpdatesParam().setInstallAddonUpdates(getChkInstallAddonUpdates().isSelected());
		options.getCheckForUpdatesParam().setInstallScannerRules(getChkInstallScannerRules().isSelected());
		options.getCheckForUpdatesParam().setReportReleaseAddons(getChkReportReleaseAddons().isSelected());
		options.getCheckForUpdatesParam().setReportBetaAddons(getChkReportBetaAddons().isSelected());
		options.getCheckForUpdatesParam().setReportAlphaAddons(getChkReportAlphaAddons().isSelected());
		options.getCheckForUpdatesParam().setAddonDirectories(getScriptDirModel().getElements());
		options.getCheckForUpdatesParam().setDownloadDirectory(new File(getDownloadDirCombo().getSelectedItem().toString()));
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.checkforupdates";
	}

	private static class CfuDirsOptionsPanel extends AbstractMultipleOptionsBaseTablePanel<File> {
        
		private static final long serialVersionUID = 1L;
        private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("cfu.options.dialog.dirs.remove.title");
	    private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("cfu.options.dialog.dirs.remove.text");
	    private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("cfu.options.dialog.dirs.remove.button.confirm");
	    private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("cfu.options.dialog.dirs.remove.button.cancel");
	    private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("cfu.options.dialog.dirs.remove.checkbox.label");
	    
        public CfuDirsOptionsPanel(OptionsAutoupdateDirsTableModel model) {
            super(model);
            getTable().setSortOrder(0, SortOrder.ASCENDING);
        }

        @Override
        public File showAddDialogue() {
        	return showDirSelectDialog(null);
        }
        
        @Override
        public File showModifyDialogue(File dir) {
        	return showDirSelectDialog(dir);
        }
        
        private File showDirSelectDialog(File dir) {
        	JFileChooser fc = new JFileChooser();
        	fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        	if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        		return fc.getSelectedFile();
        	}
        	return null;
        }
        
        @Override
        public boolean showRemoveDialogue(File f) {
            JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
            int option = JOptionPane.showOptionDialog(View.getSingleton().getMainFrame(), messages, REMOVE_DIALOG_TITLE,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, new String[] { REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL }, null);

            if (option == JOptionPane.OK_OPTION) {
                setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());
                return true;
            }
            
            return false;
        }
	}

}
