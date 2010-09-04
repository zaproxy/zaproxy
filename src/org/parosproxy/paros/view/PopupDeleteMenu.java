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
package org.parosproxy.paros.view;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupDeleteMenu extends ExtensionPopupMenu {

    
    private Component invoker = null;
    
	/**
	 * This method initializes 
	 * 
	 */
	public PopupDeleteMenu() {
		super();
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Delete (from view)");
        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        	    if (invoker.getName().equals("treeSite")) {
        	        JTree tree = (JTree) invoker;
                    TreePath[] paths = tree.getSelectionPaths();
                    SiteMap map = (SiteMap) tree.getModel();
                    for (int i=0; i<paths.length;i++) {
                        SiteNode node = (SiteNode) paths[i].getLastPathComponent(); 
                        delete(map, node);
                    }
                }
 
        	    
        	}
        });

			
	}
	
	
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("treeSite")) {
            this.invoker = invoker;
//	        JTree tree = (JTree) invoker;
//
//		    SiteNode node = (SiteNode) tree.getLastSelectedPathComponent();
//		    if (node != null) {
//		        this.setEnabled(true);
//		    } else {
//		        this.setEnabled(false);
//		    }
            return true;
        } else {
            this.invoker = null;
            return false;
        }

    }
    
   public static void delete(SiteMap map, SiteNode node) {
        SiteNode child = null;
        synchronized(map) {
            while (node.getChildCount() > 0) {
                try {
                    child = (SiteNode) node.getChildAt(0);
                    delete(map, child);
                } catch (Exception e) {}
            }
            
            if (node.isRoot()) {
                return;
            }

            // delete reference in node
            
            ExtensionHistory ext = (ExtensionHistory) Control.getSingleton().getExtensionLoader().getExtension("ExtensionHistory");
            ext.getHistoryList().removeElement(node.getHistoryReference());

            // delete past reference in node
            while (node.getPastHistoryReference().size() > 0) {
                HistoryReference ref = (HistoryReference) node.getPastHistoryReference().get(0);
                ext.getHistoryList().removeElement(ref);
                node.getPastHistoryReference().remove(0);
            }
            
            map.removeNodeFromParent(node);
        }
    }

    
}
