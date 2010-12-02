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
package org.zaproxy.zap.extension.option;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class OptionsLocalePanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelMisc = null;
	private JComboBox localeSelect = null;
	private JLabel localeLabel = null;
	private JLabel localeChangeLabel = null;
	private Map<String, String> localeMap = new HashMap<String, String>();
	
    public OptionsLocalePanel() {
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

			localeLabel = new JLabel(Constant.messages.getString("locale.options.label.language"));
			localeChangeLabel = new JLabel(Constant.messages.getString("locale.options.label.change"));

			panelMisc.add(localeLabel, gbc1_0);
			panelMisc.add(getLocaleSelect(), gbc1_1);
			panelMisc.add(localeChangeLabel, gbc2);
			panelMisc.add(new JLabel(), gbcX);

		}
		return panelMisc;
	}
	
	private JComboBox getLocaleSelect() {
		if (localeSelect == null) {
			localeSelect = new JComboBox();
			localeSelect.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Change to use the selected language in the dialog
				    String selectedLocale = (String) localeSelect.getSelectedItem();
				    String locale = localeMap.get(selectedLocale);
				    if (locale != null) {
						Constant.setLocale(locale);
						localeLabel.setText(Constant.messages.getString("locale.options.label.language"));
						localeChangeLabel.setText(Constant.messages.getString("locale.options.label.change"));
				    }
				}});
		}
		return localeSelect;
	}
	
	public void initParam(Object obj) {
	    // Pick locales up from filenames
		List <String> locales = LocaleUtils.getAvailableLocales();
		for (String locale : locales) {
			String desc = LocaleUtils.getLocalDisplayName(locale);
			localeSelect.addItem(desc);
			localeMap.put(desc, locale);
		}

		/*
		// Previously got all of the available languages from the config file 
	    //OptionsParam options = (OptionsParam) obj;
	    String [] locales = options.getViewParam().getLocales();
	    localeSelect.removeAllItems();
	    for (String locale : locales) {
			localeSelect.addItem(Constant.messages.getString("view.locale." + locale));
			localeMap.put(Constant.messages.getString("view.locale." + locale), locale);
	    }
	    String locale = Constant.messages.getString("view.locale." + options.getViewParam().getLocale());
	    localeSelect.setSelectedItem(locale);
	    */
	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    String selectedLocale = (String) localeSelect.getSelectedItem();
	    String locale = localeMap.get(selectedLocale);
	    if (locale != null) {
		    options.getViewParam().setLocale(locale);
	    }
	    
	}
	
}
