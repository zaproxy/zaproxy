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
package org.zaproxy.zap.extension.session;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;

public class OptionsSessionPanel extends AbstractParamPanel {

	private static final long serialVersionUID = 1L;
	private JTable tableAuth = null;
	private JScrollPane jScrollPane = null;
	private OptionsSessionTableModel sessionModel = null;
    /**
     * 
     */
    public OptionsSessionPanel() {
        super();
 		initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

        java.awt.GridBagConstraints gridBagConstraints1 = new GridBagConstraints();

        javax.swing.JLabel jLabel = new JLabel();

        this.setLayout(new GridBagLayout());
        this.setSize(409, 268);
        this.setName(Constant.messages.getString("options.session.title"));
        jLabel.setText(Constant.messages.getString("options.session.label.tokens"));
        jLabel.setPreferredSize(new java.awt.Dimension(494,25));
        jLabel.setMinimumSize(new java.awt.Dimension(494,25));
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridheight = 1;
        gridBagConstraints1.ipady = 50;
        gridBagConstraints1.insets = new java.awt.Insets(10,0,5,0);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.gridx = 0;
        gridBagConstraints2.gridy = 1;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints2.ipadx = 0;
        gridBagConstraints2.insets = new java.awt.Insets(0,0,0,0);
        gridBagConstraints2.anchor = java.awt.GridBagConstraints.NORTHWEST;
        this.add(jLabel, gridBagConstraints1);
        this.add(getJScrollPane(), gridBagConstraints2);
			
	}

	@Override
    public void initParam(Object obj) {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    SessionParam param = optionsParam.getSessionParam();
	    getSessionModel().setTokens(param.getTokens());
    }


    @Override
    public void validateParam(Object obj) throws Exception {

    }


    @Override
    public void saveParam(Object obj) throws Exception {
	    OptionsParam optionsParam = (OptionsParam) obj;
	    SessionParam sessionParam = optionsParam.getSessionParam();
	    sessionParam.setTokens(getSessionModel().getTokens());
    }

	/**
	 * This method initializes tableAuth	
	 * 	
	 * @return javax.swing.JTable	
	 */    
	private JTable getTableAuth() {
		if (tableAuth == null) {
			tableAuth = new JTable();
			tableAuth.setModel(getSessionModel());
			tableAuth.setRowHeight(18);
		}
		return tableAuth;
	}
	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */    
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getTableAuth());
			jScrollPane.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
		}
		return jScrollPane;
	}
	
		
	/**
	 * This method initializes authModel	
	 * 	
	 * @return com.proofsecure.paros.view.OptionsAuthenticationTableModel	
	 */    
	private OptionsSessionTableModel getSessionModel() {
		if (sessionModel == null) {
			sessionModel = new OptionsSessionTableModel();
		}
		return sessionModel;
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.options.session";
	}

}
