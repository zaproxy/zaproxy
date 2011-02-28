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
package org.parosproxy.paros.extension.option;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.LocaleUtils;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsViewPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel panelMisc = null;
	private JCheckBox chkProcessImages = null;
	private JCheckBox chkShowMainToolbar = null;
	private JCheckBox chkAdvancedView = null;
	// ZAP: Added locale selector
	private JComboBox localeSelect = null;
	private JComboBox editorViewSelect = null;
	private JComboBox brkPanelViewSelect = null;
	private JLabel localeLabel = null;
	private JLabel localeRestartLabel = null;
	private JLabel editorViewLabel = null;
	private JLabel brkPanelViewLabel = null;
	private JLabel advancedViewLabel = null;
	private Map<String, String> localeMap = new HashMap<String, String>();
	private JLabel displayLabel = null;
	private JComboBox displaySelect = null;
	
    public OptionsViewPanel() {
        super();
 		initialize();
   }
    
	/**
	 * This method initializes this
	 * 
	 * @return void
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
			panelMisc.setSize(114, 132);
			panelMisc.setName(Constant.messages.getString("view.options.misc.title"));

			GridBagConstraints gbc0 = new GridBagConstraints();
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
						
			GridBagConstraints gbc9 = new GridBagConstraints();
						
			GridBagConstraints gbcY = new GridBagConstraints();
			GridBagConstraints gbcX = new GridBagConstraints();
			
			gbc0.gridx = 0;
			gbc0.gridy = 0;
			gbc0.ipadx = 0;
			gbc0.ipady = 0;
			gbc0.insets = new java.awt.Insets(2,2,2,2);
			gbc0.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc0.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc0.weightx = 1.0D;
			gbc0.gridwidth = 2;

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
			
			gbc9.gridx = 0;
			gbc9.gridy = 6;
			gbc9.ipadx = 0;
			gbc9.ipady = 0;
			gbc9.insets = new java.awt.Insets(2,2,2,2);
			gbc9.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc9.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc9.weightx = 1.0D;
			gbc9.gridwidth = 2;
			
			
			gbcY.gridx = 0;
			gbcY.gridy = 7;
			gbcY.ipadx = 0;
			gbcY.ipady = 0;
			gbcY.insets = new java.awt.Insets(2,2,2,2);
			gbcY.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbcY.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbcY.weightx = 1.0D;
			gbcY.weighty = 1.0D;
			gbcY.gridwidth = 2;

			gbcX.gridx = 0;
			gbcX.gridy = 8;
			gbcX.ipadx = 0;
			gbcX.ipady = 0;
			gbcX.insets = new java.awt.Insets(2,2,2,2);
			gbcX.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbcX.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbcX.weightx = 1.0D;
			gbcX.weighty = 1.0D;

			localeLabel = new JLabel(Constant.messages.getString("view.options.label.language"));
			displayLabel = new JLabel(Constant.messages.getString("view.options.label.display"));
			editorViewLabel = new JLabel(Constant.messages.getString("view.options.label.editorView"));
			brkPanelViewLabel = new JLabel(Constant.messages.getString("view.options.label.brkPanelView"));
			localeRestartLabel = new JLabel(Constant.messages.getString("view.options.label.restart"));
			advancedViewLabel = new JLabel(Constant.messages.getString("view.options.label.advancedview"));

			panelMisc.add(getChkProcessImages(), gbc0);
			panelMisc.add(localeLabel, gbc1_0);
			panelMisc.add(getLocaleSelect(), gbc1_1);
			panelMisc.add(displayLabel, gbc2_0);
			panelMisc.add(getDisplaySelect(), gbc2_1);
			
			panelMisc.add(editorViewLabel, gbc3_0);
			panelMisc.add(getEditorViewSelect(), gbc3_1);
			
			panelMisc.add(brkPanelViewLabel, gbc4_0);
			panelMisc.add(getBrkPanelViewSelect(), gbc4_1);

			panelMisc.add(advancedViewLabel, gbc5_0);
			panelMisc.add(getChkAdvancedView(), gbc5_1);
			
			panelMisc.add(getChkShowMainToolbar(), gbc9);
			
			panelMisc.add(localeRestartLabel, gbcY);
			panelMisc.add(new JLabel(), gbcX);

		}
		return panelMisc;
	}

	/**
	 * This method initializes chkProcessImages	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkProcessImages() {
		if (chkProcessImages == null) {
			chkProcessImages = new JCheckBox();
			chkProcessImages.setText(Constant.messages.getString("view.options.label.processImages"));
			chkProcessImages.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkProcessImages.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkProcessImages;
	}
	
	private JCheckBox getChkShowMainToolbar() {
		if (chkShowMainToolbar == null) {
			chkShowMainToolbar = new JCheckBox();
			chkShowMainToolbar.setText(Constant.messages.getString("view.options.label.showMainToolbar"));
			chkShowMainToolbar.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkShowMainToolbar.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkShowMainToolbar;
	}
	
	private JComboBox getLocaleSelect() {
		if (localeSelect == null) {
			localeSelect = new JComboBox();
		}
		return localeSelect;
	}
	
	private JComboBox getDisplaySelect() {
		if (displaySelect == null) {
			displaySelect = new JComboBox();
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.left"));
			displaySelect.addItem(Constant.messages.getString("view.options.label.display.bottom"));
		}
		return displaySelect;
	}
	
	private JComboBox getEditorViewSelect() {
		if (editorViewSelect == null) {
			editorViewSelect = new JComboBox();
			editorViewSelect.addItem(Constant.messages.getString("view.options.label.display.vertical"));
			editorViewSelect.addItem(Constant.messages.getString("view.options.label.display.horizontal"));
			editorViewSelect.addItem(Constant.messages.getString("view.options.label.display.tabs"));
		}
		return editorViewSelect; 
	}
	
	private JComboBox getBrkPanelViewSelect() {
		if (brkPanelViewSelect == null) {
			brkPanelViewSelect = new JComboBox();
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.toolbaronly"));
			brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.breakonly"));
			//TODO Currently this is not possible. Has to be fixed some day ;)
			//brkPanelViewSelect.addItem(Constant.messages.getString("view.options.label.brkPanelView.both"));
		}
		return brkPanelViewSelect; 
	}
	
	private JCheckBox getChkAdvancedView() {
		if (chkAdvancedView == null) {
			chkAdvancedView = new JCheckBox();
//			chkProcessImages.setText(Constant.messages.getString("view.options.label.processImages"));
			chkAdvancedView.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkAdvancedView.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		
		return chkAdvancedView;
	}
	
	
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getChkProcessImages().setSelected(options.getViewParam().getProcessImages() > 0);
	    
	    localeSelect.removeAllItems();
		List <String> locales = LocaleUtils.getAvailableLocales();
		for (String locale : locales) {
			String desc = LocaleUtils.getLocalDisplayName(locale);
			localeSelect.addItem(desc);
			localeMap.put(desc, locale);
		}

	    String locale = LocaleUtils.getLocalDisplayName(options.getViewParam().getLocale());
	    localeSelect.setSelectedItem(locale);
	    displaySelect.setSelectedIndex(options.getViewParam().getDisplayOption());
	    editorViewSelect.setSelectedIndex(options.getViewParam().getEditorViewOption());
	    brkPanelViewSelect.setSelectedIndex(options.getViewParam().getBrkPanelViewOption());
	    getChkShowMainToolbar().setSelected(options.getViewParam().getShowMainToolbar() > 0);
	    chkAdvancedView.setSelected(options.getViewParam().getAdvancedViewOption() > 0);
	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    options.getViewParam().setProcessImages((getChkProcessImages().isSelected()) ? 1 : 0);
	    String selectedLocale = (String) localeSelect.getSelectedItem();
	    String locale = localeMap.get(selectedLocale);
	    if (locale != null) {
		    options.getViewParam().setLocale(locale);
	    }
	    options.getViewParam().setDisplayOption(displaySelect.getSelectedIndex());
	    options.getViewParam().setEditorViewOption(editorViewSelect.getSelectedIndex());
	    options.getViewParam().setBrkPanelViewOption(brkPanelViewSelect.getSelectedIndex());
	    options.getViewParam().setShowMainToolbar((getChkShowMainToolbar().isSelected()) ? 1 : 0);
	    options.getViewParam().setAdvancedViewOption(getChkAdvancedView().isSelected() ? 1 : 0);
	}
	
     }  //  @jve:decl-index=0:visual-constraint="10,10"
