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
package org.parosproxy.paros.extension.scanner;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.extension.history.ExtensionHistory;


/**
 * ZAP: New Popup Menu Alert Edit
 */
public class PopupMenuAlertEdit extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;

	private ExtensionScanner extension = null;

	private ExtensionHistory extHist = null; 

    /**
     * 
     */
    public PopupMenuAlertEdit() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuAlertEdit(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Edit...");

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    
			    DefaultMutableTreeNode node = (DefaultMutableTreeNode) extension.getAlertPanel().getTreeAlert().getLastSelectedPathComponent();
			    if (node != null && node.getUserObject() != null) {
			        Object obj = node.getUserObject();
			        if (obj instanceof Alert) {
			            Alert alert = (Alert) obj;
			            
						if (extHist == null) {
							extHist = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory");
						}
						if (extHist != null) {
							extHist.showAlertAddDialog(alert);
						}
			        }
			    }
        	    
        	}
        });
			
	}
	
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("treeAlert")) {
            try {
                JTree tree = (JTree) invoker;
                if (tree.getLastSelectedPathComponent() != null) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (!node.isRoot() && node.getUserObject() != null) {
                        return true;
                    }
                }
            } catch (Exception e) {}
            
        }
        return false;
    }
    
    void setExtension(ExtensionScanner extension) {
        this.extension = extension;
    }
}
