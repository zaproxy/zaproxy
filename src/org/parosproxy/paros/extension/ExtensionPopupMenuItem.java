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
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2012/01/12 Renamed the class from ExtensionPopupMenu to ExtensionPopupMenuItem 
//                 and added the method succeedWithSeparator
// ZAP: 2012/03/03 Added setters for separators
// ZAP: 2012/07/11 Issue 323: Added support for supermenus
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/08/29 Issue 250: Support for authentication management
// ZAP: 2012/10/02 Issue 385: Added support for Contexts
// ZAP: 2014/03/23 Changed to implement the interface ExtensionPopupMenuComponent
// ZAP: 2014/03/23 Changed to implement the new method from ExtensionPopupMenuComponent
// ZAP: 2014/03/23 Issue 1095: Replace main pop up sub menus with ExtensionPopupMenu when appropriate
// ZAP: 2014/08/14 Issue 1302: Context menu item action might not get executed

package org.parosproxy.paros.extension;

import java.awt.Component;

import javax.swing.JMenuItem;

import org.zaproxy.zap.view.popup.ExtensionPopupMenuComponent;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;

public class ExtensionPopupMenuItem extends JMenuItem implements ExtensionPopupMenuComponent {

	private static final long serialVersionUID = -5454473736753550528L;
	
	public static final int ATTACK_MENU_INDEX = 0;
	public static final int EXCLUDE_MENU_INDEX = 1;
	public static final int CONTEXT_FLAG_MENU_INDEX = 1;	// This is just shown in the response tab
	public static final int FLAG_MENU_INDEX = 2;
	
	private int menuIndex = -1;
	private int parentMenuIndex = -1;
	private boolean precedeWithSeparator = false;
	private boolean succeedWithSeparator = false;

	public ExtensionPopupMenuItem() {
        super();
    }
    
    public ExtensionPopupMenuItem(String label) {
        super(label);
    }
    
    /**
     * By default, the pop up menu item button is enabled and it is enable for all {@code invoker}s.
     * 
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    @Override
    public boolean isEnableForComponent(Component invoker) {
        return true;
    }

    /**
     * Defaults to call the method {@code isEnableForComponent(Component)} passing as parameter the component returned by the
     * method {@code MessageContainer#getComponent()} called on the given {@code invoker}.
     * 
     * @see #isEnableForComponent(Component)
     * @see MessageContainer#getComponent()
     */
    @Override
    public boolean isEnableForMessageContainer(MessageContainer<?> invoker) {
        return isEnableForComponent(invoker.getComponent());
    }
    
    public String getParentMenuName() {
    	return null;
    }
    
    @Override
    public int getMenuIndex() {
    	return menuIndex;
    }
    
    public void setMenuIndex(int menuIndex) {
    	this.menuIndex = menuIndex;
    }
    
    public int getParentMenuIndex() {
    	return parentMenuIndex;
    }
    
    public void setParentMenuIndex(int parentMenuIndex) {
    	this.parentMenuIndex = parentMenuIndex;
    }
    
    // ZAP: Support submenus
    public boolean isSubMenu() {
    	return false;
    }
    
    /**
     * @deprecated (2.3.0) Not used. It will be removed in a following release.
     */
    @Deprecated
    @SuppressWarnings("javadoc")
    // ZAP: Support supermenus
    public boolean isSuperMenu() {
    	return false;
    }
    
    // ZAP: Support dummy menu items - used for dynamic menus
    public boolean isDummyItem () {
    	return false;
    }
    
    // ZAP: Added precedeWithSeparator
    @Override
    public boolean precedeWithSeparator() {
    	return precedeWithSeparator;
    }
    
    public void setPrecedeWithSeparator(boolean precede) {
    	this.precedeWithSeparator = precede;
    }
    
    // ZAP: Added succeedWithSeparator
    @Override
    public boolean succeedWithSeparator() {
    	return succeedWithSeparator;
    }
    
    public void setSucceedWithSeparator(boolean succeed) {
    	this.succeedWithSeparator = succeed;
    }

    // Override if the menuitem is safe!
    @Override
    public boolean isSafe() {
    	return false;
    }

    /**
     * Does nothing by default.
     * 
     * @since 2.4.0
     */
    @Override
    public void dismissed(ExtensionPopupMenuComponent selectedMenuComponent) {
    }
}
