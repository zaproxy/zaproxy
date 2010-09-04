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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTree;

import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuEmbeddedBrowser extends ExtensionPopupMenu {

    private ExtensionHistory extension = null;
    private Component lastInvoker = null;

	/**
     * 
     */
    public PopupMenuEmbeddedBrowser() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuEmbeddedBrowser(String label) {
        super(label);
        initialize();
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("View in Browser");
        if (!ExtensionHistory.isEnableForNativePlatform()) {
            this.setEnabled(false);
        }

        this.setActionCommand("");
        
        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
                HistoryReference ref = null;
                if (lastInvoker == null) {
                    return;
                }
                if (lastInvoker.getName().equalsIgnoreCase("ListLog")) {
                    JList listLog = extension.getLogPanel().getListLog();
                    if (listLog.getSelectedValues().length != 1) {
                        extension.getView().showWarningDialog("Please select a message in History panel first.");
                        return;
                    }
                    
                    ref = (HistoryReference) listLog.getSelectedValue();
                    showBrowser(ref);                                   
                        

                } else if (lastInvoker.getName().equals("treeSite")) {
                    JTree tree = (JTree) lastInvoker;
                    SiteNode node = (SiteNode) tree.getLastSelectedPathComponent();
                    ref = node.getHistoryReference();
                    showBrowser(ref);
                }
        	}
        });

			
	}
	
    private void showBrowser(HistoryReference ref) {
        HttpMessage msg = null;
        try {
            msg = ref.getHttpMessage();
            // ZAP: Disabled the platform specific browser
            /*
            if (!extension.browserDisplay(ref, msg)) {
                extension.getView().showWarningDialog("Selecetd HTTP message type cannot be shown.");
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    
    public boolean isEnableForComponent(Component invoker) {
        lastInvoker = null;
        if (!ExtensionHistory.isEnableForNativePlatform()) {
            return false;
        }
        
        if (invoker.getName() == null) {
            return false;
        }
        
        if (invoker.getName().equalsIgnoreCase("ListLog")) {
            try {
                JList list = (JList) invoker;
                if (list.getSelectedIndex() >= 0) {
                    this.setEnabled(true);
                } else {
                    this.setEnabled(false);
                }
                lastInvoker = invoker;
            } catch (Exception e) {
                
            }
            return true;
        } else if (invoker.getName().equals("treeSite")) {
                JTree tree = (JTree) invoker;
                lastInvoker = tree;
                return true;
        }
        return false;
    }
    
    void setExtension(ExtensionHistory extension) {
        this.extension = extension;
    }
    
	
}
