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

import javax.swing.ImageIcon;
import javax.swing.JTree;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.SiteNode;

public class PopupMenuActiveScanSites extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;
	private ExtensionActiveScan extension = null;
    private JTree treeSite = null;
    
    /**
     * 
     */
    public PopupMenuActiveScanSites() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuActiveScanSites(String label) {
        super(label);
    }

    @Override
    public boolean isSubMenu() {
    	return true;
    }
    
    @Override
    public String getParentMenuName() {
    	return Constant.messages.getString("attack.site.popup");
    }

    @Override
    public int getParentMenuIndex() {
    	return ATTACK_MENU_INDEX;
    }

    /**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("ascan.site.popup"));
        this.setIcon(new ImageIcon(getClass().getResource("/resource/icon/16/093.png")));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        		if (treeSite != null) {
        		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
        		    
        		    if (node != null) {
        		    	// Loop up to get the top parent
        				while (node.getParent() != null && node.getParent().getParent() != null) {
        					node = (SiteNode) node.getParent();
        				}
        		    	extension.startScan(node);
        		    }
        		}

        	}
        });
			
	}
	
    public boolean isEnableForComponent(Component invoker) {
        treeSite = getTree(invoker);
        if (treeSite != null) {
		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
		    if (node != null && ! node.isRoot() && ! extension.isScanning(node)) {
		        this.setEnabled(true);
		    } else {
		        this.setEnabled(false);
		    }
            return true;
        }
        return false;
    }

    private JTree getTree(Component invoker) {
        if (invoker instanceof JTree) {
            JTree tree = (JTree) invoker;
            if (tree.getName().equals("treeSite")) {
                return tree;
            }
        }

        return null;
    }
    
    
    void setExtension(ExtensionActiveScan extension) {
        this.extension = extension;
    }
    	
}
