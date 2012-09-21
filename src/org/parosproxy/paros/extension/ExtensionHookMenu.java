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
// ZAP: 2012/03/15 Added the method addPopupMenuItem(ExtensionPopupMenu menu).
// ZAP: 2012/05/03 Changed to only initialise the class variables MENU_SEPARATOR
// and POPUP_MENU_SEPARATOR if there is a view.

package org.parosproxy.paros.extension;


import java.util.List;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.ExtensionPopupMenu;

public class ExtensionHookMenu {
    
    public static final JMenuItem MENU_SEPARATOR;
    public static final ExtensionPopupMenuItem POPUP_MENU_SEPARATOR;
    
    private Vector<JMenuItem> newMenuList = new Vector<>();
    private Vector<JMenuItem> fileMenuItemList = new Vector<>();
    private Vector<JMenuItem> editMenuItemList = new Vector<>();
    private Vector<JMenuItem> viewMenuItemList = new Vector<>();
    private Vector<JMenuItem> analyseMenuItemList = new Vector<>();
    private Vector<JMenuItem> toolsMenuItemList = new Vector<>();
    private Vector<JMenuItem> popupMenuList = new Vector<>();
    // ZAP: Added help and reports menu extension hook
    private Vector<JMenuItem> helpMenuList = new Vector<>();
    private Vector<JMenuItem> reportMenuList = new Vector<>();
    
    // ZAP: Added static block.
    static {
        // XXX temporary "hack" to check if ZAP is in GUI mode.
        // There is no need to create view elements (subsequently initialising
        // the java.awt.Toolkit) when ZAP is running in non GUI mode.
        if (View.isInitialised()) {
            MENU_SEPARATOR = new JMenuItem();
            POPUP_MENU_SEPARATOR = new ExtensionPopupMenuItem();
        } else {
            MENU_SEPARATOR = null;
            POPUP_MENU_SEPARATOR = null;
        }
    }
    
    List<JMenuItem> getNewMenus() {
        return newMenuList;
    }

    List<JMenuItem> getFile() {
        return fileMenuItemList;
    }

    List<JMenuItem> getEdit() {
        return editMenuItemList;
    }

    List<JMenuItem> getView() {
        return viewMenuItemList;
    }

    List<JMenuItem> getAnalyse() {
        return analyseMenuItemList;
    }
    
    List<JMenuItem> getTools() {
        return toolsMenuItemList;
    }
    

    
    /**
     * Get the plugin popup menu used for the whole workbench.
     * @return
     */
    List<JMenuItem> getPopupMenus() {
        return popupMenuList;
    }

    List<JMenuItem> getHelpMenus() {
        return helpMenuList;
    }

    List<JMenuItem> getReportMenus() {
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
    public void addPopupMenuItem(ExtensionPopupMenuItem menuItem) {
        getPopupMenus().add(menuItem);        
    }
    
    // ZAP: Added the method.
    public void addPopupMenuItem(ExtensionPopupMenu menu) {
        getPopupMenus().add(menu);
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

    public ExtensionPopupMenuItem getPopupMenuSeparator() {
        return POPUP_MENU_SEPARATOR;
    }
}
