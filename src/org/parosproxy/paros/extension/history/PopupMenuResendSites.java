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
package org.parosproxy.paros.extension.history;

import java.awt.Component;
import java.sql.SQLException;

import javax.swing.JTree;

import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuResendSites extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;

	private JTree treeSite = null;
    
    private ExtensionHistory extension;

	/**
     * 
     */
    public PopupMenuResendSites() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuResendSites(String label) {
        super(label);
    }

	public void setExtension(ExtensionHistory extension) {
		this.extension = extension;
	}

    /**
	 * This method initialises this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Resend...");

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {    

                if (treeSite != null) {
        		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();

            	    ManualRequestEditorDialog dialog = extension.getResendDialog();
            	    HistoryReference ref = node.getHistoryReference();
            	    HttpMessage msg = null;
            	    try {
                        msg = ref.getHttpMessage().cloneRequest();
                        dialog.setMessage(msg);
                        dialog.setVisible(true);
                    } catch (HttpMalformedHeaderException e1) {
                        e1.printStackTrace();
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
        	}
        });

			
	}
	
    public boolean isEnableForComponent(Component invoker) {
        treeSite = getTree(invoker);
        if (treeSite != null) {
		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
		    if (node != null && ! node.isRoot()) {
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
