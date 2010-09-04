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
import java.awt.GridLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsCheckForUpdatesPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JPanel panelMisc = null;
	private JCheckBox chkCheckOnStart = null;
	
    public OptionsCheckForUpdatesPanel() {
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
			java.awt.GridLayout gridLayout2 = new GridLayout();

			panelMisc.setLayout(gridLayout2);
			panelMisc.setSize(114, 132);
			panelMisc.setName("Miscellenous");
			gridLayout2.setRows(1);
			panelMisc.add(getChkCheckOnStart(), null);
		}
		return panelMisc;
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
		}
		return chkCheckOnStart;
	}
	
	public void initParam(Object obj) {
	    OptionsParam options = (OptionsParam) obj;
	    getChkCheckOnStart().setSelected(options.getCheckForUpdatesParam().getCheckOnStart() > 0);
	}
	
	public void validateParam(Object obj) {
	    // no validation needed
	}
	
	public void saveParam (Object obj) throws Exception {
	    OptionsParam options = (OptionsParam) obj;
	    options.getCheckForUpdatesParam().setChckOnStart((getChkCheckOnStart().isSelected()) ? 1 : 0);
	    
	}
	
}
