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
package org.parosproxy.paros.extension;


import java.util.List;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionHookMenu {
    
    public static final JMenuItem MENU_SEPARATOR = new JMenuItem();
    public static final ExtensionPopupMenu POPUP_MENU_SEPARATOR = new ExtensionPopupMenu();
    
    private Vector newMenuList = new Vector();
    private Vector fileMenuItemList = new Vector();
    private Vector editMenuItemList = new Vector();
    private Vector viewMenuItemList = new Vector();
    private Vector analyseMenuItemList = new Vector();
    private Vector toolsMenuItemList = new Vector();
    private Vector popupMenuList = new Vector();
    // ZAP: Added help and reports menu extension hook
    private Vector helpMenuList = new Vector();
    private Vector reportMenuList = new Vector();
    
    List getNewMenus() {
        return newMenuList;
    }

    List getFile() {
        return fileMenuItemList;
    }

    List getEdit() {
        return editMenuItemList;
    }

    List getView() {
        return viewMenuItemList;
    }

    List getAnalyse() {
        return analyseMenuItemList;
    }
    
    List getTools() {
        return toolsMenuItemList;
    }
    

    
    /**
     * Get the plugin popup menu used for the whole workbench.
     * @return
     */
    List getPopupMenus() {
        return popupMenuList;
    }

    List getHelpMenus() {
        return helpMenuList;
    }

    List getReportMenus() {
        return reportMenuList;
    }

    public void addFileMenuItem(JMenuItem menuItem) {
        getFile().add(menuItem);
    }

    public void addEditMenuItem(JMenuItem menuItem) {
        getEdit().add(menuItem);
    }

    public void addViewMenuItem(JMenuItem menuItem) {
        getView().add(menuItem);
    }

    public void addAnalyseMenuItem(JMenuItem menuItem) {
        getAnalyse().add(menuItem);
    }

    public void addToolsMenuItem(JMenuItem menuItem) {
        getTools().add(menuItem);
    }
    


    public void addNewMenu(JMenu menu) {
        getNewMenus().add(menu);
    }

    /**
     * Add a popup menu item used for the whole workbench.  Conditions can be set in PluginMenu
     * when the popup menu can be used.
     * @param menuItem
     */
    public void addPopupMenuItem(ExtensionPopupMenu menuItem) {
        getPopupMenus().add(menuItem);        
    }
    
    public void addHelpMenuItem(JMenuItem menuItem) {
        getHelpMenus().add(menuItem);        
    }
    
    public void addReportMenuItem(JMenuItem menuItem) {
        getReportMenus().add(menuItem);        
    }
    
    public JMenuItem getMenuSeparator() {
        return MENU_SEPARATOR;
    }

    public ExtensionPopupMenu getPopupMenuSeparator() {
        return POPUP_MENU_SEPARATOR;
    }
}
