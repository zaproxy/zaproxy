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
package org.zaproxy.zap.view;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.OptionsParam;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProxyDialog extends AbstractDialog {

	private static final long serialVersionUID = 1L;
	private JPanel jPanel = null;
	private OptionsConnectionPanel connPanel = null;
	private OptionsParam options = null;
	private JButton btnOK = null;
    /**
     * @throws HeadlessException
     */
    public ProxyDialog() throws HeadlessException {
        super();
 		initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public ProxyDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setContentPane(getJPanel());
        this.pack();
        this.setSize(406, 193);
        
		getConnPanel().passwordFocus();
	
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */    
	private JPanel getJPanel() {
		if (jPanel == null) {
			// TODO add a cancel button?
			//java.awt.GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			java.awt.GridBagConstraints gridBagConstraints5 = new GridBagConstraints();

			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			gridBagConstraints5.gridx = 0;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.insets = new java.awt.Insets(0,0,0,0);
			gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints5.weightx = 1.0D;
			gridBagConstraints5.weighty = 1.0D;
			gridBagConstraints5.ipady = 2;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints6.gridx = 1;
			gridBagConstraints6.gridy = 1;
			gridBagConstraints6.insets = new java.awt.Insets(2,2,2,2);
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.SOUTHEAST;
			//gridBagConstraints7.gridx = 1;
			//gridBagConstraints7.gridy = 0;
			//gridBagConstraints7.insets = new java.awt.Insets(2,2,2,2);
			//gridBagConstraints7.anchor = java.awt.GridBagConstraints.SOUTHWEST;
			jPanel.add(getConnPanel(), gridBagConstraints5);
			//jPanel.add(getBtnCancel(), gridBagConstraints7);
			jPanel.add(getBtnOK(), gridBagConstraints6);
			
		}
		return jPanel;
	}
	
	private OptionsConnectionPanel getConnPanel() {
		if (connPanel == null) {
			connPanel = new OptionsConnectionPanel();
			connPanel.setProxyDialog(this);
		}
		return connPanel;
	}
	
	public void init (OptionsParam options) {
		this.options = options;
		this.getConnPanel().initParam(options);
	}

	public void saveAndClose() {
		try {
			connPanel.saveParam(options);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	    ProxyDialog.this.dispose();
	}
	/**
	 * This method initializes btnOK	
	 * 	
	 * @return javax.swing.JButton	
	 */    
	private JButton getBtnOK() {
		if (btnOK == null) {
			btnOK = new JButton();
			btnOK.setText("OK");
			btnOK.addActionListener(new java.awt.event.ActionListener() { 

				public void actionPerformed(java.awt.event.ActionEvent e) {    
					saveAndClose();
				}
			});

		}
		return btnOK;
	}

}
