/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.api;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsApiPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelMisc = null;
	private JCheckBox chkEnabled = null;
	private JCheckBox chkSecureOnly = null;
	private JCheckBox disableKey = null;
	private JCheckBox incErrorDetails = null;
	private JCheckBox autofillKey = null;
	private JCheckBox enableJSONP = null;
	private ZapTextField keyField = null; 
	private JButton generateKeyButton = null;

	//private JCheckBox chkPostActions = null;
	
    public OptionsApiPanel() {
        super();
 		initialize();
    }
    
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(Constant.messages.getString("api.options.title"));
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
			panelMisc.add(getChkEnabled(), LayoutHelper.getGBC(0, 0, 1, 0.5));
			panelMisc.add(getChkSecureOnly(), LayoutHelper.getGBC(0, 1, 1, 0.5));
			
			panelMisc.add(new JLabel(Constant.messages.getString("api.options.label.apiKey")), 
					LayoutHelper.getGBC(0, 2, 1, 0.5));
			panelMisc.add(getKeyField(), LayoutHelper.getGBC(1, 2, 1, 0.5));
			panelMisc.add(getGenerateKeyButton(), LayoutHelper.getGBC(1, 3, 1, 0.5));

			JLabel warning = new JLabel(Constant.messages.getString("api.options.label.testingWarning"));
			warning.setForeground(Color.RED);
			panelMisc.add(warning, LayoutHelper.getGBC(0, 4, 2, 0.5D));
			panelMisc.add(getDisableKey(), LayoutHelper.getGBC(0, 5, 1, 0.5));
			panelMisc.add(getIncErrorDetails(), LayoutHelper.getGBC(0, 6, 1, 0.5));
			panelMisc.add(getAutofillKey(), LayoutHelper.getGBC(0, 7, 1, 0.5));
			panelMisc.add(getEnableJSONP(), LayoutHelper.getGBC(0, 8, 1, 0.5));
			
			panelMisc.add(new JLabel(), LayoutHelper.getGBC(0, 10, 1, 0.5D, 1.0D));	// Spacer
		}
		return panelMisc;
	}
	/**
	 * This method initializes chkProcessImages	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */    
	private JCheckBox getChkEnabled() {
		if (chkEnabled == null) {
			chkEnabled = new JCheckBox();
			chkEnabled.setText(Constant.messages.getString("api.options.enabled"));
			chkEnabled.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkEnabled.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkEnabled;
	}
	
	private JCheckBox getChkSecureOnly() {
		if (chkSecureOnly == null) {
			chkSecureOnly = new JCheckBox();
			chkSecureOnly.setText(Constant.messages.getString("api.options.secure"));
			chkSecureOnly.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkSecureOnly.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkSecureOnly;
	}

	private JCheckBox getDisableKey() {
		if (disableKey == null) {
			disableKey = new JCheckBox();
			disableKey.setText(Constant.messages.getString("api.options.disableKey"));
			disableKey.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			disableKey.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
			disableKey.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getKeyField().setEnabled(!disableKey.isSelected());
					getGenerateKeyButton().setEnabled(!disableKey.isSelected());
					if (!disableKey.isSelected()) {
						// Repopulate the previously used value
						getKeyField().setText(
								Model.getSingleton().getOptionsParam().getApiParam().getRealKey());
					}
				}});
		}
		return disableKey;
	}

	private JCheckBox getEnableJSONP() {
		if (enableJSONP == null) {
			enableJSONP = new JCheckBox();
			enableJSONP.setText(Constant.messages.getString("api.options.enableJSONP"));
			enableJSONP.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			enableJSONP.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return enableJSONP;
	}


	private JCheckBox getIncErrorDetails() {
		if (incErrorDetails == null) {
			incErrorDetails = new JCheckBox();
			incErrorDetails.setText(Constant.messages.getString("api.options.incErrors"));
			incErrorDetails.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			incErrorDetails.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return incErrorDetails;
	}
	
	private JCheckBox getAutofillKey() {
		if (autofillKey == null) {
			autofillKey = new JCheckBox();
			autofillKey.setText(Constant.messages.getString("api.options.autofillKey"));
			autofillKey.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			autofillKey.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return autofillKey;
	}

	private ZapTextField getKeyField() {
		if (keyField == null) {
			keyField = new ZapTextField();
		}
		return keyField;
	}
	
	private JButton getGenerateKeyButton () {
		if (generateKeyButton == null) {
			generateKeyButton = new JButton(Constant.messages.getString("api.options.button.generateKey"));
			generateKeyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					getKeyField().setText(ExtensionAPI.generateApiKey());
				}});
		}
		return generateKeyButton;
	}

	/*
	public JCheckBox getChkPostActions() {
		if (chkPostActions == null) {
			chkPostActions = new JCheckBox();
			chkPostActions.setText(Constant.messages.getString("api.options.postactions"));
			chkPostActions.setVerticalAlignment(javax.swing.SwingConstants.TOP);
			chkPostActions.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
		}
		return chkPostActions;
	}
	*/

	@Override
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getChkEnabled().setSelected(options.getApiParam().isEnabled());
	    getChkSecureOnly().setSelected(options.getApiParam().isSecureOnly());
	    getDisableKey().setSelected(options.getApiParam().isDisableKey());
	    getIncErrorDetails().setSelected(options.getApiParam().isIncErrorDetails());
	    getAutofillKey().setSelected(options.getApiParam().isAutofillKey());
	    getEnableJSONP().setSelected(options.getApiParam().isEnableJSONP());
	    getKeyField().setText(options.getApiParam().getKey());
	    //getChkPostActions().setSelected(options.getApiParam().isPostActions());

		getKeyField().setEnabled(!disableKey.isSelected());
		getGenerateKeyButton().setEnabled(!disableKey.isSelected());
}
	
	@Override
	public void validateParam(Object obj) throws Exception {
	    if (! getDisableKey().isSelected() && getKeyField().getText().length() == 0) {
	    	throw new Exception (Constant.messages.getString("api.options.nokey.error"));
	    }
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    options.getApiParam().setEnabled(getChkEnabled().isSelected());
	    options.getApiParam().setSecureOnly(getChkSecureOnly().isSelected());
	    options.getApiParam().setDisableKey(getDisableKey().isSelected());
	    options.getApiParam().setIncErrorDetails(getIncErrorDetails().isSelected());
	    options.getApiParam().setAutofillKey(getAutofillKey().isSelected());
	    options.getApiParam().setEnableJSONP(getEnableJSONP().isSelected());
	    
	    if (!getDisableKey().isSelected()) {
	    	// Dont loose the old value on disabling
	    	options.getApiParam().setKey(getKeyField().getText());
	    }
	    //options.getApiParam().setPostActions(getChkPostActions().isEnabled());
	    
	}
	
	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.api";
	}
}
