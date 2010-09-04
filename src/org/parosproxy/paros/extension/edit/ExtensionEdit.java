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
package org.parosproxy.paros.extension.edit;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.view.FindDialog;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionEdit extends ExtensionAdaptor {

    private FindDialog findDialog = null;
    private JMenuItem menuFind = null;
    private PopupFindMenu popupFindMenu = null;

    /**
     * 
     */
    public ExtensionEdit() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionEdit(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionEdit");
			
	}
	

	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);

	    if (getView() != null) {	        
	        extensionHook.getHookMenu().addEditMenuItem(getMenuFind());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuFind());
	        
	    }

	}
    
    private void showFindDialog(JFrame frame, JTextComponent lastInvoker) {
        if (findDialog == null || findDialog.getParent() != frame) {
            findDialog = new FindDialog(frame, false);            
        }
        
        findDialog.setLastInvoker(lastInvoker);
        findDialog.setVisible(true);
    }

    /**
     * This method initializes menuFind	
     * 	
     * @return javax.swing.JMenuItem	
     */
    private JMenuItem getMenuFind() {
        if (menuFind == null) {
            menuFind = new JMenuItem();
            menuFind.setText("Find...");
            menuFind.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.Event.CTRL_MASK, false));

            menuFind.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showFindDialog(getView().getMainFrame(), null);
                }
            });
        }
        return menuFind;
    }

    /**
     * This method initializes popupMenuFind	
     * 	
     * @return org.parosproxy.paros.extension.ExtensionPopupMenu	
     */
    private PopupFindMenu getPopupMenuFind() {
        if (popupFindMenu== null) {
            popupFindMenu = new PopupFindMenu();
            popupFindMenu.setText("Find...");
            popupFindMenu.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showFindDialog(popupFindMenu.getParentFrame(), popupFindMenu.getLastInvoker());
                    
                }
            });
        }
        return popupFindMenu;
    }
	

         }
