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
package org.zaproxy.zap.extension.ascan;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.SessionExcludeFromScanPanel;

public class PopupExcludeFromScanMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 2282358266003940700L;

	private Component invoker = null;
    
	/**
	 * This method initializes 
	 * 
	 */
	public PopupExcludeFromScanMenu() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("sites.exclude.ascan.popup"));
        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        	    if (invoker.getName().equals("treeSite")) {
                    Session session = Model.getSingleton().getSession();
        	        JTree tree = (JTree) invoker;
                    TreePath[] paths = tree.getSelectionPaths();
                    for (int i=0; i<paths.length;i++) {
                        SiteNode node = (SiteNode) paths[i].getLastPathComponent(); 
                        String url = node.getHierarchicNodeName();
                        if (! node.isLeaf()) {
                        	url += "/*";
                        }
                        session.getExcludeFromScanRegexs().add(url);
                    }

                    View.getSingleton().showSessionDialog(session, SessionExcludeFromScanPanel.PANEL_NAME);
                }
        	}
        });
	}
	
	
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("treeSite")) {
            this.invoker = invoker;
	        JTree tree = (JTree) invoker;

		    SiteNode node = (SiteNode) tree.getLastSelectedPathComponent();
		    if (node != null) {
		        this.setEnabled(true);
		    } else {
		        this.setEnabled(false);
		    }
            return true;
        } else {
            this.invoker = null;
            return false;
        }

    }
    
    @Override
    public boolean isSubMenu() {
    	return true;
    }
    
    @Override
    public String getParentMenuName() {
    	return Constant.messages.getString("sites.exclude.popup");
    }

    @Override
    public int getParentMenuIndex() {
    	return EXCLUDE_MENU_INDEX;
    }

}
