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
// ZAP: 2012/04/25 Added @Override annotation to the appropriate method.
// ZAP: 2012/10/23 Changed to prevent a NullPointerException when there's no
// parent JFrame (changed to use SwingUtilities.getAncestorOfClass(...)).
// ZAP: 2016/04/05 Issue 2458: Fix xlint warning messages 

package org.parosproxy.paros.extension.edit;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;

public class PopupFindMenu extends ExtensionPopupMenuItem {
    private static final long serialVersionUID = 1L;
    private JTextComponent lastInvoker = null;
    private JFrame parentFrame = null;
    
	/**
     * @return Returns the lastInvoker.
     */
    public JTextComponent getLastInvoker() {
        return lastInvoker;
    }
    
    /**
	 * This method initializes 
	 * 
	 */
	public PopupFindMenu() {
		super();
		initialize();
	}
	
	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setText(Constant.messages.getString("edit.find.popup"));	// ZAP: i18n
	}
	
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent) {
            setLastInvoker((JTextComponent) invoker);
            setParentFrame((JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, invoker));
            return true;
        } else {
            setLastInvoker(null);
            return false;
        }

    }

    /**
     * @return Returns the parentFrame.
     */
    public JFrame getParentFrame() {
        return parentFrame;
    }

    /**
     * @param parentFrame The parentFrame to set.
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * @param lastInvoker The lastInvoker to set.
     */
    public void setLastInvoker(JTextComponent lastInvoker) {
        this.lastInvoker = lastInvoker;
    }
    
}
