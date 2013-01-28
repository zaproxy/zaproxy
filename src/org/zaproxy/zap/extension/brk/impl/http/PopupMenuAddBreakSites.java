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
package org.zaproxy.zap.extension.brk.impl.http;

import java.awt.Component;

import javax.swing.JTree;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.brk.ExtensionBreak;



public class PopupMenuAddBreakSites extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;

	private JTree treeSite = null;
    
    private ExtensionBreak extension;
    private HttpBreakpointsUiManagerInterface uiManager;
    
	
    public PopupMenuAddBreakSites(ExtensionBreak extension, HttpBreakpointsUiManagerInterface uiManager) {
        super();
        
        this.extension = extension;
        this.uiManager = uiManager;
        
 		initialize();
    }

    
    public PopupMenuAddBreakSites(String label) {
        super(label);
    }

	private void initialize() {
        this.setText(Constant.messages.getString("brk.add.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {    

                if (treeSite != null) {
        		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
		            if (node == null) {
		                return;
		            }
                    String url = node.getHierarchicNodeName();
                    if (! node.isLeaf()) {
                    	url += "/*";
                    }
                    uiManager.handleAddBreakpoint(url);
                }
        	}
        });

			
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        treeSite = getTree(invoker);
        if (treeSite != null) {
		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
		    if (node != null && ! node.isRoot() && extension.canAddBreakpoint()) {
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
    
}
