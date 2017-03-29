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

import javax.swing.JTree;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;


/**
 * @deprecated No longer used/needed. It will be removed in a future release.
 */
@Deprecated
public class PopupMenuResendSites extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(PopupMenuResendSites.class);

	private JTree treeSite = null;
    
    private ExtensionHistory extension;

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
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("sites.resend.popup"));

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent evt) {    

                if (treeSite != null) {
        		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();

            	    ManualRequestEditorDialog dialog = extension.getResendDialog();
            	    HistoryReference ref = node.getHistoryReference();
            	    HttpMessage msg = null;
            	    try {
                        msg = ref.getHttpMessage().cloneRequest();
                        dialog.setMessage(msg);
                        dialog.setVisible(true);
                    } catch (HttpMalformedHeaderException | DatabaseException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
        	}
        });

			
	}
	
    @Override
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
