/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2005 Chinotec Technologies Company
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
package org.zaproxy.zap.extension.ascan;

import java.awt.Component;

import javax.swing.JList;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuScanHistory extends ExtensionPopupMenu {

	private static final long serialVersionUID = 1L;
	private ExtensionActiveScan extension = null;
    private JList listLog = null;
    
    /**
     * 
     */
    public PopupMenuScanHistory() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuScanHistory(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("history.scan.popup"));	// ZAP: i18n

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
                
                Object[] obj = listLog.getSelectedValues();
                if (obj.length != 1) {
                    return;
                }
                
                try {
                    HistoryReference ref = (HistoryReference) obj[0];
                    SiteNode siteNode = ref.getSiteNode();
                    extension.startScan(siteNode);
                } catch (Exception e1) {
                    extension.getView().showWarningDialog(Constant.messages.getString("history.scan.warning"));	// ZAP: i18n
                }
        	}
        });

			
	}
	
    public boolean isEnableForComponent(Component invoker) {

        if (invoker.getName() != null && invoker.getName().equals("ListLog")) {
            try {
                JList list = (JList) invoker;
                listLog = list;
                Object[] obj = listLog.getSelectedValues();

                // ZAP: Fixed NPE bugs
                if (obj.length == 1) {
                    HistoryReference ref = (HistoryReference) obj[0];
                    // TODO this isnt right yet
                    if (extension.isScanning(ref.getSiteNode())) {
                        this.setEnabled(false);
                    } else {
                    	this.setEnabled(true);
                    }
                    /*
                    if (ref.getSiteNode() != null) {
                    	this.setEnabled(true);
                    } else {
                        this.setEnabled(false);
                    }
                    */
                } else {
                    this.setEnabled(false);
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
            return true;
            
        }
        return false;
    }

    void setExtension(ExtensionActiveScan extension) {
        this.extension = extension;
    }

}
