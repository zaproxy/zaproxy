/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.extension.spider;

import java.awt.Component;

import javax.swing.JTree;

import org.apache.log4j.Logger;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuSpider extends ExtensionPopupMenu {

    private ExtensionSpider extension = null;
    private JTree treeSite = null;
    // ZAP: Added logger
    private Logger logger = Logger.getLogger(PopupMenuSpider.class);
    
    /**
     * 
     */
    public PopupMenuSpider() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuSpider(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Spider...");



        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        		if (treeSite != null) {
        		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
        		    extension.setStartNode(node);
	                if (node.isRoot()) {
	                    extension.showDialog("All sites will be crawled");
	                } else {
	                    try {
	                        HttpMessage msg = node.getHistoryReference().getHttpMessage();
	                        if (msg != null) {
	                            String tmp = msg.getRequestHeader().getURI().toString();
	                            extension.showDialog(tmp);
	                        }
	                    } catch (Exception e1) {
	                    	// ZAP: Log the exception
	                    	logger.error(e1.getMessage(), e1);
	                    }
	                }
        		}

        	}
        });

			
	}
	
    public boolean isEnableForComponent(Component invoker) {
        treeSite = getTree(invoker);
        if (treeSite != null) {
		    SiteNode node = (SiteNode) treeSite.getLastSelectedPathComponent();
		    if (node != null) {
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
    
    void setExtension(ExtensionSpider extension) {
        this.extension = extension;
    }
	
}
