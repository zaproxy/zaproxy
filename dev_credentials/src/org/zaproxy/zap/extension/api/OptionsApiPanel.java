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
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

public class OptionsApiPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelMisc = null;
	private JCheckBox chkEnabled = null;
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
			java.awt.GridLayout gridLayout2 = new GridLayout();

			panelMisc.setLayout(gridLayout2);
			panelMisc.setSize(114, 132);
			panelMisc.setName("Miscellaneous");
			gridLayout2.setRows(1);
			panelMisc.add(getChkEnabled(), null);
			//panelMisc.add(getChkPostActions(), null);
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
	    //options.getApiParam().setPostActions(getChkPostActions().isEnabled());
	    
	}
	
	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.api";
	}
}
