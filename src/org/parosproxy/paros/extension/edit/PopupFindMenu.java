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

import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenu;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PopupFindMenu extends ExtensionPopupMenu {


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
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setText("Find...");


			
	}
    public boolean isEnableForComponent(Component invoker) {
        if (invoker instanceof JTextComponent) {
            setLastInvoker((JTextComponent) invoker);
            Container c = getLastInvoker().getParent();
            while (!(c instanceof JFrame)) {
                c = c.getParent();
            }
            setParentFrame((JFrame) c);
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
