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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

public class OptionsApiPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelMisc = null;
	private JCheckBox chkEnabled = null;
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
			panelMisc.add(new JLabel("API KEY"), LayoutHelper.getGBC(0, 1, 1, 0.5));	// TODO
			panelMisc.add(getKeyField(), LayoutHelper.getGBC(1, 1, 1, 0.5));
			panelMisc.add(getGenerateKeyButton(), LayoutHelper.getGBC(1, 2, 1, 0.5));
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
	
	private ZapTextField getKeyField() {
		if (keyField == null) {
			keyField = new ZapTextField();
		}
		return keyField;
	}
	
	private JButton getGenerateKeyButton () {
		if (generateKeyButton == null) {
			generateKeyButton = new JButton("Generate Random Key");	// TODO
			generateKeyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SecureRandom random = new SecureRandom();
					getKeyField().setText(new BigInteger(130, random).toString(32));
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
	    getKeyField().setText(options.getApiParam().getKey());
	    //getChkPostActions().setSelected(options.getApiParam().isPostActions());
	}
	
	@Override
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	@Override
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    options.getApiParam().setEnabled(getChkEnabled().isSelected());
    	options.getApiParam().setKey(getKeyField().getText());
	    //options.getApiParam().setPostActions(getChkPostActions().isEnabled());
	    
	}
	
	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.api";
	}
}
