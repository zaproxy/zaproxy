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
// ZAP: 2014/01/28 Issue 207: Support keyboard shortcuts
// ZAP: 2014/05/02 Fixed method links in Javadocs
// ZAP: 2014/11/11 Issue 1406: Move online menu items to an add-on
// ZAP: 2016/09/26 JavaDoc tweaks
// ZAP: 2018/10/05 Lazily initialise the lists and add JavaDoc.
// ZAP: 2019/03/15 Issue 3578: Added the method addImportMenuItem(ZapMenuItem  menuitem)
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
package org.parosproxy.paros.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * The object to add/hook menus and menu items to the {@link
 * org.parosproxy.paros.view.MainFrame#getMainMenuBar() main menu bar} and to the {@link
 * View#getPopupMenu() main context menu}.
 *
 * <p>The menus added through the hook are removed when the extension is unloaded.
 *
 * <p><strong>Note:</strong> This class is not thread-safe, the menus should be added only through
 * the thread that {@link Extension#hook(ExtensionHook) hooks the extension}.
 *
 * @since 1.0.0
 * @see View#getMainFrame()
 */
public class ExtensionHookMenu {

    public static final JMenuItem MENU_SEPARATOR;
    public static final ExtensionPopupMenuItem POPUP_MENU_SEPARATOR;

    /**
     * The new menus for the main menu bar added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addNewMenu(JMenu)
     * @see #getNewMenus()
     */
    private List<JMenuItem> newMenuList;

    /**
     * The file menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addFileMenuItemImpl(JMenuItem)
     * @see #getFile()
     */
    private List<JMenuItem> fileMenuItemList;

    /**
     * The edit menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addEditMenuItemImpl(JMenuItem)
     * @see #getEdit()
     */
    private List<JMenuItem> editMenuItemList;

    /**
     * The view menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addViewMenuItemImpl(JMenuItem)
     * @see #getView()
     */
    private List<JMenuItem> viewMenuItemList;

    /**
     * The analyse menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addAnalyseMenuItemImpl(JMenuItem)
     * @see #getAnalyse()
     */
    private List<JMenuItem> analyseMenuItemList;

    /**
     * The tools menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addToolsMenuItemImpl(JMenuItem)
     * @see #getTools()
     */
    private List<JMenuItem> toolsMenuItemList;

    /**
     * The import menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addImportMenuItem(ZapMenuItem)
     * @see #getImport()
     * @since 2.8.0
     */
    private List<JMenuItem> importMenuItemList;

    /**
     * The menus for the main context menu added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addPopupMenuImpl(JMenuItem)
     * @see #getPopupMenus()
     */
    private List<JMenuItem> popupMenuList;

    /**
     * The help menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addHelpMenuItemImpl(JMenuItem)
     * @see #getHelpMenus()
     */
    private List<JMenuItem> helpMenuList;

    /**
     * The report menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addReportMenuItemImpl(JMenuItem)
     * @see #getReportMenus()
     */
    private List<JMenuItem> reportMenuList;

    /**
     * The online menus added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addOnlineMenuItem(ZapMenuItem)
     * @see #getOnlineMenus()
     */
    private List<JMenuItem> onlineMenuList;

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
        return unmodifiableList(newMenuList);
    }

    List<JMenuItem> getFile() {
        return unmodifiableList(fileMenuItemList);
    }

    List<JMenuItem> getEdit() {
        return unmodifiableList(editMenuItemList);
    }

    List<JMenuItem> getView() {
        return unmodifiableList(viewMenuItemList);
    }

    List<JMenuItem> getAnalyse() {
        return unmodifiableList(analyseMenuItemList);
    }

    List<JMenuItem> getTools() {
        return unmodifiableList(toolsMenuItemList);
    }

    List<JMenuItem> getImport() {
        return unmodifiableList(importMenuItemList);
    }

    /**
     * Gets the popup menu items used for the whole workbench.
     *
     * @return a {@code List} containing the popup menu items of the extension
     */
    List<JMenuItem> getPopupMenus() {
        return unmodifiableList(popupMenuList);
    }

    List<JMenuItem> getHelpMenus() {
        return unmodifiableList(helpMenuList);
    }

    List<JMenuItem> getReportMenus() {
        return unmodifiableList(reportMenuList);
    }

    List<JMenuItem> getOnlineMenus() {
        return unmodifiableList(onlineMenuList);
    }

    /**
     * Add a menu item to the File menu
     *
     * @param menuItem the file menu item
     * @deprecated use {@link #addFileMenuItem(ZapMenuItem menuItem)} instead.
     */
    @Deprecated
    public void addFileMenuItem(JMenuItem menuItem) {
        addFileMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the File menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addFileSubMenu(JMenu menu) {
        addFileMenuItemImpl(menu);
    }

    private void addFileMenuItemImpl(JMenuItem menuItem) {
        if (fileMenuItemList == null) {
            fileMenuItemList = createList();
        }
        fileMenuItemList.add(menuItem);
    }

    /**
     * Add a menu item to the Edit menu
     *
     * @param menuItem the edit menu item
     * @deprecated use {@link #addEditMenuItem(ZapMenuItem menuItem)} instead.
     */
    @Deprecated
    public void addEditMenuItem(JMenuItem menuItem) {
        addEditMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the Edit menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addEditSubMenu(JMenu menu) {
        addEditMenuItemImpl(menu);
    }

    private void addEditMenuItemImpl(JMenuItem menuItem) {
        if (editMenuItemList == null) {
            editMenuItemList = createList();
        }
        editMenuItemList.add(menuItem);
    }

    /**
     * Add a menu item to the View menu
     *
     * @param menuItem the view menu item
     * @deprecated use {@link #addViewMenuItem(ZapMenuItem menuItem)} instead.
     */
    @Deprecated
    public void addViewMenuItem(JMenuItem menuItem) {
        addViewMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the View menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addViewSubMenu(JMenu menu) {
        addViewMenuItemImpl(menu);
    }

    private void addViewMenuItemImpl(JMenuItem menuItem) {
        if (viewMenuItemList == null) {
            viewMenuItemList = createList();
        }
        viewMenuItemList.add(menuItem);
    }

    /**
     * Add a menu item to the Analyse menu
     *
     * @param menuItem the analyse menu item
     * @deprecated use {@link #addAnalyseMenuItem(ZapMenuItem menuItem)} instead.
     */
    @Deprecated
    public void addAnalyseMenuItem(JMenuItem menuItem) {
        addAnalyseMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the Analyse menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addAnalyseSubMenu(JMenu menu) {
        addAnalyseMenuItemImpl(menu);
    }

    private void addAnalyseMenuItemImpl(JMenuItem menuItem) {
        if (analyseMenuItemList == null) {
            analyseMenuItemList = createList();
        }
        analyseMenuItemList.add(menuItem);
    }

    /**
     * Add a menu item to the Tools menu
     *
     * @param menuItem the tools menu item
     * @deprecated use {@link #addToolsMenuItem(ZapMenuItem menuItem)} instead.
     */
    @Deprecated
    public void addToolsMenuItem(JMenuItem menuItem) {
        addToolsMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the Tools menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addToolsSubMenu(JMenu menu) {
        addToolsMenuItemImpl(menu);
    }

    private void addToolsMenuItemImpl(JMenuItem menuItem) {
        if (toolsMenuItemList == null) {
            toolsMenuItemList = createList();
        }
        toolsMenuItemList.add(menuItem);
    }

    public void addFileMenuItem(ZapMenuItem menuItem) {
        addFileMenuItemImpl(menuItem);
    }

    public void addEditMenuItem(ZapMenuItem menuItem) {
        addEditMenuItemImpl(menuItem);
    }

    public void addViewMenuItem(ZapMenuItem menuItem) {
        addViewMenuItemImpl(menuItem);
    }

    public void addAnalyseMenuItem(ZapMenuItem menuItem) {
        addAnalyseMenuItemImpl(menuItem);
    }

    public void addToolsMenuItem(ZapMenuItem menuItem) {
        addToolsMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the Import menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addImportSubMenu(JMenu menu) {
        addImportMenuItemImpl(menu);
    }

    /** @since 2.8.0 */
    public void addImportMenuItem(ZapMenuItem menuItem) {
        addImportMenuItemImpl(menuItem);
    }

    private void addImportMenuItemImpl(JMenuItem menuItem) {
        if (importMenuItemList == null) {
            importMenuItemList = createList();
        }
        importMenuItemList.add(menuItem);
    }

    public void addNewMenu(JMenu menu) {
        if (newMenuList == null) {
            newMenuList = createList();
        }
        newMenuList.add(menu);
    }

    /**
     * Add a popup menu item used for the whole workbench. Conditions can be set in PluginMenu when
     * the popup menu can be used.
     *
     * @param menuItem the popup menu item
     */
    public void addPopupMenuItem(ExtensionPopupMenuItem menuItem) {
        addPopupMenuImpl(menuItem);
    }

    private void addPopupMenuImpl(JMenuItem menu) {
        if (popupMenuList == null) {
            popupMenuList = createList();
        }
        popupMenuList.add(menu);
    }

    // ZAP: Added the method.
    public void addPopupMenuItem(ExtensionPopupMenu menu) {
        addPopupMenuImpl(menu);
    }

    /**
     * Add a menu item to the Help menu
     *
     * @param menuItem the help menu item
     * @deprecated use {@link #addHelpMenuItem(ZapMenuItem menuItem)} instead.
     */
    @Deprecated
    public void addHelpMenuItem(JMenuItem menuItem) {
        addHelpMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the Help menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addHelpSubMenu(JMenu menu) {
        addHelpMenuItemImpl(menu);
    }

    private void addHelpMenuItemImpl(JMenuItem menuItem) {
        if (helpMenuList == null) {
            helpMenuList = createList();
        }
        helpMenuList.add(menuItem);
    }

    /**
     * Add a menu item to the Report menu
     *
     * @param menuItem the report menu item
     * @deprecated use {@link #addReportMenuItem(ZapMenuItem menuItem)} instead.
     */
    @Deprecated
    public void addReportMenuItem(JMenuItem menuItem) {
        addReportMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the Report menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addReportSubMenu(JMenu menu) {
        addReportMenuItemImpl(menu);
    }

    private void addReportMenuItemImpl(JMenuItem menuItem) {
        if (reportMenuList == null) {
            reportMenuList = createList();
        }
        reportMenuList.add(menuItem);
    }

    public void addHelpMenuItem(ZapMenuItem menuItem) {
        addHelpMenuItemImpl(menuItem);
    }

    public void addReportMenuItem(ZapMenuItem menuItem) {
        addReportMenuItemImpl(menuItem);
    }

    /**
     * Add a sub-menu to the Online menu
     *
     * @param menu the sub-menu to add
     * @since 2.9.0
     */
    public void addOnlineSubMenu(JMenu menu) {
        addOnlineMenuItemImpl(menu);
    }

    public void addOnlineMenuItem(ZapMenuItem menuItem) {
        addOnlineMenuItemImpl(menuItem);
    }

    private void addOnlineMenuItemImpl(JMenuItem menuItem) {
        if (onlineMenuList == null) {
            onlineMenuList = createList();
        }
        onlineMenuList.add(menuItem);
    }

    public JMenuItem getMenuSeparator() {
        return MENU_SEPARATOR;
    }

    public ExtensionPopupMenuItem getPopupMenuSeparator() {
        return POPUP_MENU_SEPARATOR;
    }

    /**
     * Creates an {@link ArrayList} with initial capacity of 1.
     *
     * <p>Most of the extensions just add one menu.
     *
     * @return the {@code ArrayList}.
     */
    private static <T> List<T> createList() {
        return new ArrayList<>(1);
    }

    /**
     * Gets an unmodifiable list from the given list.
     *
     * @param list the list, might be {@code null}.
     * @return an unmodifiable list, never {@code null}.
     */
    private static <T> List<T> unmodifiableList(List<T> list) {
        if (list == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(list);
    }
}
