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
// ZAP: 2012/01/12 Reflected the rename of the class ExtensionPopupMenu to
// ExtensionPopupMenuItem.
// ZAP: 2012/03/15 Changed the method purgeHistory to clear the displayQueue of
// the LogPanel.
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2012/07/29 Issue 43: Cleaned up access to ExtensionHistory UI
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension.history;

import java.awt.Component;
import java.util.List;

import javax.swing.JList;
import javax.swing.JOptionPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.PopupPurgeMenu;
import org.zaproxy.zap.extension.alert.ExtensionAlert;


public class PopupMenuPurgeHistory extends ExtensionPopupMenuItem {

    private ExtensionHistory extension = null;
    
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
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("history.purge.popup"));	// ZAP: i18n

        this.addActionListener(new java.awt.event.ActionListener() { 

        	@Override
        	public void actionPerformed(java.awt.event.ActionEvent e) {    
        	    List<HistoryReference> hrefs = extension.getSelectedHistoryReferences();
        	    if (hrefs.size() > 1) {
        	        int result = extension.getView().showConfirmDialog(Constant.messages.getString("history.purge.warning"));
        	        if (result != JOptionPane.YES_OPTION) {
        	            return;
        	        }
        	    }
        	    synchronized(extension) {
            		for (HistoryReference href : hrefs) {
        	            purgeHistory(href);
        	        }
        	    }
        	}
        });

			
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                JList<?> list = (JList<?>) invoker;
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
        extension.removeFromHistoryList(ref);
        extension.clearLogPanelDisplayQueue();
        
		ExtensionAlert extAlert = (ExtensionAlert) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.NAME);

		if (extAlert != null) {
			//Iterating over the getAlerts() while deleting the alert will result in a ConcurrentModificationException.
			while (!ref.getAlerts().isEmpty()) {
				extAlert.deleteAlert(ref.getAlerts().get(0));
			}
		}
        
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
