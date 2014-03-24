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
// ZAP: 2012/03/15 Removed the options to change the display of the ManualRequestEditorDialog,
// now they are changed dynamically.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/12/13 Added support for a new option 'show tab names'.

package org.parosproxy.paros.extension.option;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

// ZAP: 2011: added more configuration options

public class OptionsViewPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel panelMisc = null;
	
	private JCheckBox chkShowTabNames = null;
	private JCheckBox chkProcessImages = null;
	private JCheckBox chkShowMainToolbar = null;
	private JCheckBox chkAdvancedView = null;
	private JCheckBox chkAskOnExit = null;
	private JCheckBox chkWmUiHandling = null;
	
	private JComboBox<String> brkPanelViewSelect = null;
	private JComboBox<String> displaySelect = null;
	
	private JLabel brkPanelViewLabel = null;
	private JLabel advancedViewLabel = null;
	private JLabel wmUiHandlingLabel = null;
	private JLabel askOnExitLabel = null;
	private JLabel displayLabel = null;
	private JLabel showMainToolbarLabel = null;
	private JLabel processImagesLabel = null;
	private JLabel showTabNames = null;

	
    public OptionsViewPanel() {
        super();
 		initialize();
   }
    
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("view.options.title"));
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
		    if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
		    	panelMisc.setSize(114, 132);
		    }
			panelMisc.setName(Constant.messages.getString("view.options.misc.title"));

			
			GridBagConstraints gbc0_0 = new GridBagConstraints();
			GridBagConstraints gbc0_1 = new GridBagConstraints();
			GridBagConstraints gbc1_0 = new GridBagConstraints();
			GridBagConstraints gbc1_1 = new GridBagConstraints();
			GridBagConstraints gbc2_0 = new GridBagConstraints();
			GridBagConstraints gbc2_1 = new GridBagConstraints();
			GridBagConstraints gbc3_0 = new GridBagConstraints();
			GridBagConstraints gbc3_1 = new GridBagConstraints();
			GridBagConstraints gbc4_0 = new GridBagConstraints();
			GridBagConstraints gbc4_1 = new GridBagConstraints();
			GridBagConstraints gbc5_0 = new GridBagConstraints();
			GridBagConstraints gbc5_1 = new GridBagConstraints();
			GridBagConstraints gbc6_0 = new GridBagConstraints();
			GridBagConstraints gbc6_1 = new GridBagConstraints();
			GridBagConstraints gbc7_0 = new GridBagConstraints();
			GridBagConstraints gbc7_1 = new GridBagConstraints();
			
			GridBagConstraints gbcX = new GridBagConstraints();


			gbc0_0.gridx = 0;
			gbc0_0.gridy = 0;
			gbc0_0.ipadx = 0;
			gbc0_0.ipady = 0;
			gbc0_0.insets = new java.awt.Insets(2,2,2,2);
			gbc0_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc0_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc0_0.weightx = 1.0D;

			gbc0_1.gridx = 1;
			gbc0_1.gridy = 0;
			gbc0_1.ipadx = 0;
			gbc0_1.ipady = 0;
			gbc0_1.insets = new java.awt.Insets(2,2,2,2);
			gbc0_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc0_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc0_1.weightx = 1.0D;

			
			gbc1_0.gridx = 0;
			gbc1_0.gridy = 1;
			gbc1_0.ipadx = 0;
			gbc1_0.ipady = 0;
			gbc1_0.insets = new java.awt.Insets(2,2,2,2);
			gbc1_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc1_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc1_0.weightx = 1.0D;

			gbc1_1.gridx = 1;
			gbc1_1.gridy = 1;
			gbc1_1.ipadx = 0;
			gbc1_1.ipady = 0;
			gbc1_1.insets = new java.awt.Insets(2,2,2,2);
			gbc1_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc1_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc1_1.weightx = 1.0D;
			
			gbc2_0.gridx = 0;
			gbc2_0.gridy = 2;
			gbc2_0.ipadx = 0;
			gbc2_0.ipady = 0;
			gbc2_0.insets = new java.awt.Insets(2,2,2,2);
			gbc2_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc2_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc2_0.weightx = 1.0D;

			gbc2_1.gridx = 1;
			gbc2_1.gridy = 2;
			gbc2_1.ipadx = 0;
			gbc2_1.ipady = 0;
			gbc2_1.insets = new java.awt.Insets(2,2,2,2);
			gbc2_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc2_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc2_1.weightx = 1.0D;
		
			gbc3_0.gridx = 0;
			gbc3_0.gridy = 3;
			gbc3_0.ipadx = 0;
			gbc3_0.ipady = 0;
			gbc3_0.insets = new java.awt.Insets(2,2,2,2);
			gbc3_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc3_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc3_0.weightx = 1.0D;

			gbc3_1.gridx = 1;
			gbc3_1.gridy = 3;
			gbc3_1.ipadx = 0;
			gbc3_1.ipady = 0;
			gbc3_1.insets = new java.awt.Insets(2,2,2,2);
			gbc3_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc3_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc3_1.weightx = 1.0D;
			
			gbc4_0.gridx = 0;
			gbc4_0.gridy = 4;
			gbc4_0.ipadx = 0;
			gbc4_0.ipady = 0;
			gbc4_0.insets = new java.awt.Insets(2,2,2,2);
			gbc4_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc4_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc4_0.weightx = 1.0D;

			gbc4_1.gridx = 1;
			gbc4_1.gridy = 4;
			gbc4_1.ipadx = 0;
			gbc4_1.ipady = 0;
			gbc4_1.insets = new java.awt.Insets(2,2,2,2);
			gbc4_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc4_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc4_1.weightx = 1.0D;
			
			gbc5_0.gridx = 0;
			gbc5_0.gridy = 5;
			gbc5_0.ipadx = 0;
			gbc5_0.ipady = 0;
			gbc5_0.insets = new java.awt.Insets(2,2,2,2);
			gbc5_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc5_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc5_0.weightx = 1.0D;

			gbc5_1.gridx = 1;
			gbc5_1.gridy = 5;
			gbc5_1.ipadx = 0;
			gbc5_1.ipady = 0;
			gbc5_1.insets = new java.awt.Insets(2,2,2,2);
			gbc5_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc5_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc5_1.weightx = 1.0D;
			
			gbc6_0.gridx = 0;
			gbc6_0.gridy = 6;
			gbc6_0.ipadx = 0;
			gbc6_0.ipady = 0;
			gbc6_0.insets = new java.awt.Insets(2,2,2,2);
			gbc6_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc6_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc6_0.weightx = 1.0D;
			
			gbc6_1.gridx = 1;
			gbc6_1.gridy = 6;
			gbc6_1.ipadx = 0;
			gbc6_1.ipady = 0;
			gbc6_1.insets = new java.awt.Insets(2,2,2,2);
			gbc6_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc6_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc6_1.weightx = 1.0D;

			gbc7_0.gridx = 0;
			gbc7_0.gridy = 7;
			gbc7_0.ipadx = 0;
			gbc7_0.ipady = 0;
			gbc7_0.insets = new java.awt.Insets(2,2,2,2);
			gbc7_0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc7_0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc7_0.weightx = 1.0D;

			gbc7_1.gridx = 1;
			gbc7_1.gridy = 7;
			gbc7_1.ipadx = 0;
			gbc7_1.ipady = 0;
			gbc7_1.insets = new java.awt.Insets(2,2,2,2);
			gbc7_1.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc7_1.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc7_1.weightx = 1.0D;
			
			gbcX.gridx = 0;
			gbcX.gridy = 8;
			gbcX.ipadx = 0;
			gbcX.ipady = 0;
			gbcX.insets = new java.awt.Insets(2,2,2,2);
			gbcX.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbcX.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbcX.weightx = 1.0D;
			gbcX.weighty = 1.0D;

			
			displayLabel = new JLabel(Constant.messages.getString("view.options.label.display"));
			brkPanelViewLabel = new JLabel(Constant.messages.getString("view.options.label.brkPanelView"));
			advancedViewLabel = new JLabel(Constant.messages.getString("view.options.label.advancedview"));
			wmUiHandlingLabel = new JLabel(Constant.messages.getString("view.options.label.wmuihandler"));
			askOnExitLabel = new JLabel(Constant.messages.getString("view.options.label.askonexit"));
			showMainToolbarLabel = new JLabel(Constant.messages.getString("view.options.label.showMainToolbar"));
			processImagesLabel = new JLabel(Constant.messages.getString("view.options.label.processImages"));
			showTabNames = new JLabel(Constant.messages.getString("view.options.label.showTabNames"));
			
			panelMisc.add(displayLabel, gbc0_0);
			panelMisc.add(getDisplaySelect(), gbc0_1);

			panelMisc.add(brkPanelViewLabel, gbc1_0);
			panelMisc.add(getBrkPanelViewSelect(), gbc1_1);

			panelMisc.add(advancedViewLabel, gbc2_0);
			panelMisc.add(getChkAdvancedView(), gbc2_1);
			
			panelMisc.add(wmUiHandlingLabel, gbc3_0);
			panelMisc.add(getChkWmUiHandling(), gbc3_1);

			panelMisc.add(askOnExitLabel, gbc4_0);
			panelMisc.add(getChkAskOnExit(), gbc4_1);
			
			panelMisc.add(showMainToolbarLabel, gbc5_0);
			panelMisc.add(getChkShowMainToolbar(), gbc5_1);
			
			panelMisc.add(processImagesLabel, gbc6_0);
			panelMisc.add(getChkProcessImages(), gbc6_1);

			panelMisc.add(showTabNames, gbc7_0);
			panelMisc.add(getShowTabNames(), gbc7_1);
			
			panelMisc.add(new JLabel(""), gbcX);

		}
		return panelMisc;
	}

	private JCheckBox getShowTabNames() {
		if (chkShowTabNames == null) {
			chkShowTabNames = new JCheckBox();
			chkShowTabNames.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkShowTabNames.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkShowTabNames;
	}

	private JCheckBox getChkProcessImages() {
		if (chkProcessImages == null) {
			chkProcessImages = new JCheckBox();
			chkProcessImages.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkProcessImages.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkProcessImages;
	}
	
	private JCheckBox getChkShowMainToolbar() {
		if (chkShowMainToolbar == null) {
			chkShowMainToolbar = new JCheckBox();
			chkShowMainToolbar.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkShowMainToolbar.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkShowMainToolbar;
	}

	private JCheckBox getChkWmUiHandling() {
		if (chkWmUiHandling == null) {
			chkWmUiHandling = new JCheckBox();
			chkWmUiHandling.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkWmUiHandling.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkWmUiHandling;
	}

	private JCheckBox getChkAskOnExit() {
		if (chkAskOnExit == null) {
			chkAskOnExit = new JCheckBox();
			chkAskOnExit.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkAskOnExit.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkAskOnExit;
	}
	
	private JComboBox<String> getDisplaySelect() {
		if (displaySelect == null) {
			displaySelect = new JComboBox<>();
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.left"));
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.bottom"));
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.full"));
		}
		return displaySelect;
	}
	
	private JComboBox<String> getBrkPanelViewSelect() {
		if (brkPanelViewSelect == null) {
			brkPanelViewSelect = new JComboBox<>();
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.toolbaronly"));
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.breakonly"));
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.both"));
		}
		return brkPanelViewSelect; 
	}
	
	private JCheckBox getChkAdvancedView() {
		if (chkAdvancedView == null) {
			chkAdvancedView = new JCheckBox();
			chkAdvancedView.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkAdvancedView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		
		return chkAdvancedView;
	}
	
	
	@Override
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getShowTabNames().setSelected(options.getViewParam().getShowTabNames());
	    getChkProcessImages().setSelected(options.getViewParam().getProcessImages() > 0);
	    displaySelect.setSelectedIndex(options.getViewParam().getDisplayOption());
	    brkPanelViewSelect.setSelectedIndex(options.getViewParam().getBrkPanelViewOption());
	    getChkShowMainToolbar().setSelected(options.getViewParam().getShowMainToolbar() > 0);
	    chkAdvancedView.setSelected(options.getViewParam().getAdvancedViewOption() > 0);
	    chkAskOnExit.setSelected(options.getViewParam().getAskOnExitOption() > 0);
	    chkWmUiHandling.setSelected(options.getViewParam().getWmUiHandlingOption() > 0);
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    options.getViewParam().setShowTabNames(getShowTabNames().isSelected());
	    options.getViewParam().setProcessImages((getChkProcessImages().isSelected()) ? 1 : 0);
	    options.getViewParam().setDisplayOption(displaySelect.getSelectedIndex());
	    options.getViewParam().setBrkPanelViewOption(brkPanelViewSelect.getSelectedIndex());
	    options.getViewParam().setShowMainToolbar((getChkShowMainToolbar().isSelected()) ? 1 : 0);
	    options.getViewParam().setAdvancedViewOption(getChkAdvancedView().isSelected() ? 1 : 0);
	    options.getViewParam().setAskOnExitOption(getChkAskOnExit().isSelected() ? 1 : 0);
	    options.getViewParam().setWmUiHandlingOption(getChkWmUiHandling().isSelected() ? 1 : 0);
	}

	@Override
	public String getHelpIndex() {
		// ZAP: added help index
		return "ui.dialogs.options.view";
	}
	
}
