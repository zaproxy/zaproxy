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
package org.parosproxy.paros.extension.scanner;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.ExtensionPopupMenu;
import org.parosproxy.paros.extension.history.ManualRequestEditorDialog;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupMenuResend extends ExtensionPopupMenu {

    private ExtensionScanner extension = null;
    private JTree treeSite = null;
    private HttpSender httpSender = null;
    
    /**
     * 
     */
    public PopupMenuResend() {
        super();
 		initialize();
    }

    /**
     * @param label
     */
    public PopupMenuResend(String label) {
        super(label);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Resend...");

        this.addActionListener(new java.awt.event.ActionListener() { 

        	public void actionPerformed(java.awt.event.ActionEvent e) {
        	    
        	    ManualRequestEditorDialog dialog = extension.getManualRequestEditorDialog();
        	    
        	    HttpMessage msg = null;
        	    Object obj = null;
        	    obj = extension.getAlertPanel().getTreeAlert().getLastSelectedPathComponent();
        	    if (obj == null) {
        	        return;
        	    }
        	    AlertNode node = (AlertNode) obj;
        	    if (node.getUserObject() != null) {
        	        obj = node.getUserObject();
        	        if (obj instanceof Alert) {
        	            Alert alert = (Alert) obj;
        	            msg = alert.getMessage();
        	            
        	        } else {
        	            return;
        	        }
        	    }

        	    dialog.setMessage(msg);
        	    dialog.setVisible(true);
        	    
        	    
        	    
        	    
        	    
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
