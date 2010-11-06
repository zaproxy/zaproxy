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
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

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
	private JLabel localeLabel = null;
	private JLabel localeRestartLabel = null;
	private Map<String, String> localeMap = new HashMap<String, String>();
	
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
			GridBagConstraints gbc2 = new GridBagConstraints();
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

			gbc2.gridx = 0;
			gbc2.gridy = 2;
			gbc2.ipadx = 0;
			gbc2.ipady = 0;
			gbc2.insets = new java.awt.Insets(2,2,2,2);
			gbc2.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbc2.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbc2.weightx = 1.0D;
			gbc2.weighty = 1.0D;
			gbc2.gridwidth = 2;

			gbcX.gridx = 0;
			gbcX.gridy = 3;
			gbcX.ipadx = 0;
			gbcX.ipady = 0;
			gbcX.insets = new java.awt.Insets(2,2,2,2);
			gbcX.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gbcX.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gbcX.weightx = 1.0D;
			gbcX.weighty = 1.0D;

			localeLabel = new JLabel(Constant.messages.getString("view.options.label.language"));
			localeRestartLabel = new JLabel(Constant.messages.getString("view.options.label.restart"));

			panelMisc.add(getChkProcessImages(), gbc0);
			panelMisc.add(localeLabel, gbc1_0);
			panelMisc.add(getLocaleSelect(), gbc1_1);
			panelMisc.add(localeRestartLabel, gbc2);
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
	
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getChkProcessImages().setSelected(options.getViewParam().getProcessImages() > 0);
	    String [] locales = options.getViewParam().getLocales();
	    for (String locale : locales) {
			localeSelect.addItem(Constant.messages.getString("view.locale." + locale));
			localeMap.put(Constant.messages.getString("view.locale." + locale), locale);
	    }
	    String locale = Constant.messages.getString("view.locale." + options.getViewParam().getLocale());
	    localeSelect.setSelectedItem(locale);
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
	    
	}
	
     }  //  @jve:decl-index=0:visual-constraint="10,10"
