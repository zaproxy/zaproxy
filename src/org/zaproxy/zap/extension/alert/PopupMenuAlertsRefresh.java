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
package org.zaproxy.zap.extension.alert;

import java.awt.Component;

import javax.swing.tree.DefaultTreeModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;


/**
 * ZAP: New Popup Menu Alert Delete
 */
public class PopupMenuAlertsRefresh extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;

	private ExtensionAlert extension = null;

    /**
     * 
     */
    public PopupMenuAlertsRefresh() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuAlertsRefresh(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("alerts.refresh.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {
			    ((DefaultTreeModel)extension.getAlertPanel().getTreeAlert().getModel()).reload();
        	}
        });
			
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("treeAlert")) {
        	return true;
        }
        return false;
    }
    
    void setExtension(ExtensionAlert extension) {
        this.extension = extension;
    }
}
