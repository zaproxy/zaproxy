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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
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
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
							java.awt.Color.black));
			
			zapPanel.add(getChkDownloadNewRelease(), LayoutHelper.getGBC(0, 1, 1, 1.0D));

			JPanel updPanel = new JPanel();
			updPanel.setLayout(new GridBagLayout());
			updPanel.setBorder(
					BorderFactory.createTitledBorder(
							null, Constant.messages.getString("cfu.options.updates.border"), TitledBorder.DEFAULT_JUSTIFICATION,
							javax.swing.border.TitledBorder.DEFAULT_POSITION,
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
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
							new java.awt.Font("Dialog", java.awt.Font.PLAIN, 11),
							java.awt.Color.black));
			
			newPanel.add(getChkReportReleaseAddons(), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			newPanel.add(getChkReportBetaAddons(), LayoutHelper.getGBC(0, 1, 1, 1.0D));
			newPanel.add(getChkReportAlphaAddons(), LayoutHelper.getGBC(0, 2, 1, 1.0D));

			panelMisc.add(getChkCheckOnStart(), LayoutHelper.getGBC(0, 0, 1, 1.0D));
			panelMisc.add(zapPanel, LayoutHelper.getGBC(0, 1, 1, 1.0D));
			panelMisc.add(updPanel, LayoutHelper.getGBC(0, 2, 1, 1.0D));
			panelMisc.add(newPanel, LayoutHelper.getGBC(0, 3, 1, 1.0D));
			panelMisc.add(new JLabel(""), LayoutHelper.getGBC(0, 4, 1, 1.0D, 1.0D));	// Padding

		}
		return panelMisc;
	}
	
	private void setCheckBoxStates() {
		if (chkCheckOnStart.isSelected()) {
			getChkDownloadNewRelease().setEnabled(true);
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
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.checkforupdates";
	}

}
