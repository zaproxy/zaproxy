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
import java.awt.Component;
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

	private JPanel panelMisc = null;  //  @jve:decl-index=0:visual-constraint="520,10"
	private JCheckBox chkProcessImages = null;
	// ZAP: Added locale selector
	private JComboBox localeSelect = null;
	private JComboBox editorViewSelect = null;
	private JLabel localeLabel = null;
	private JLabel localeRestartLabel = null;
	private JLabel editorViewLabel = null;
	private Map<String, String> localeMap = new HashMap<String, String>();
	private JLabel displayLabel = null;
	private JComboBox displaySelect = null;
	
    public OptionsViewPanel() {
        super();
 		initialize();
   }

    private static final String[] ROOT = {};
    private static final String[] GENERAL = {"General"};
    private static final String[] MISCELLENOUS = {"Miscellaneous"};
    
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
			
			
			gbcY.gridx = 0;
			gbcY.gridy = 4;
			gbcY.ipadx = 0;
			gbcY.ipady = 0;
			gbcY.insets = new java.awt.Insets(2,2,2,2);
			gbcY.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbcY.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbcY.weightx = 1.0D;
			gbcY.weighty = 1.0D;
			gbcY.gridwidth = 2;

			gbcX.gridx = 0;
			gbcX.gridy = 5;
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
			localeRestartLabel = new JLabel(Constant.messages.getString("view.options.label.restart"));

			panelMisc.add(getChkProcessImages(), gbc0);
			panelMisc.add(localeLabel, gbc1_0);
			panelMisc.add(getLocaleSelect(), gbc1_1);
			panelMisc.add(displayLabel, gbc2_0);
			panelMisc.add(getDisplaySelect(), gbc2_1);
			
			panelMisc.add(editorViewLabel, gbc3_0);
			panelMisc.add(getEditorViewSelect(), gbc3_1);
			
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
	}
	
     }  //  @jve:decl-index=0:visual-constraint="10,10"
