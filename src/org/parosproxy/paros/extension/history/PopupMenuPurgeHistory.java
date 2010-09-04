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
package org.parosproxy.paros.extension.history;

import java.awt.Component;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTree;

import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.PopupPurgeMenu;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuPurgeHistory extends ExtensionPopupMenu {

    private ExtensionHistory extension = null;
    private JTree treeSite = null;
    
    /**
     * 
     */
    public PopupMenuPurgeHistory() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuPurgeHistory(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Purge (from DB)");

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        	    JList listLog = extension.getLogPanel().getListLog();
        	    Object[] obj = listLog.getSelectedValues();
        	    if (obj.length > 1) {
        	        int result = extension.getView().showConfirmDialog("The history will be purged from database.  Proceed?");
        	        if (result != JOptionPane.YES_OPTION) {
        	            return;
        	        }
        	    }
        	    synchronized(extension.getHistoryList()) {
        	        
        	        for (int i=0; i<obj.length; i++) {
        	            HistoryReference ref = (HistoryReference) obj[i];
        	            purgeHistory(ref);
        	        }
        	    }
        	}
        });

			
	}
	
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                JList list = (JList) invoker;
                if (list.getSelectedIndex() >= 0) {
                    this.setEnabled(true);
                } else {
                    this.setEnabled(false);
                }
            } catch (Exception e) {}
            return true;
            
            
        }
        
        return false;
    }
    
    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
    
    private void purgeHistory(HistoryReference ref) {

        if (ref == null) {
            return;
        }
        extension.getHistoryList().removeElement(ref);
        ref.delete();

        SiteNode node = ref.getSiteNode();
        if (node == null) {
            return;
        }

        Session session = Model.getSingleton().getSession();
        SiteMap map = session.getSiteTree();

        if (node.getHistoryReference() == ref) {
            // same active Node
            PopupPurgeMenu.purge(map, node);
        } else {
            node.getPastHistoryReference().remove(ref);
        }        

    }
	
}
