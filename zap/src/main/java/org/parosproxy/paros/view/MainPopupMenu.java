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
//      ExtensionPopupMenuItem, added the methods addMenu(ExtensionPopupMenu menu),
//      removeMenu(ExtensionPopupMenu menu), handleMenu and handleMenuItem and
//      changed the method show to use the methods handleMenu and handleMenuItem.
// ZAP: 2012/02/19 Removed the Delete button
// ZAP: 2012/03/03 Moved popups to stdmenus extension
// ZAP: 2012/04/23 Added @Override annotation to the appropriate method.
// ZAP: 2012/06/06 Issue 323: Added isDummyItem to support dynamic menus
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/10/07 Added support for prepareShow() that is run on PopupMenu before showing it
// ZAP: 2012/10/08 Added check for PopupMenu safeness
// ZAP: 2013/04/14 Issue 592: Do not show the main pop up menu if it doesn't have visible pop up menu items
// ZAP: 2013/04/14 Issue 598: Replace/update "old" pop up menu items
// ZAP: 2013/11/16 Issue 878: ExtensionPopupMenuItem#getMenuIndex() as no effect in MainPopupMenu
// ZAP: 2013/11/16 Issue 901: Pop up menu "succeed" separator is not added when using sub-menu in MainPopupMenu
// ZAP: 2013/11/16 Issue 900: IllegalArgumentException when invoking the main pop up menu with
// menus or super menus with high menu index
// ZAP: 2014/03/23 Changed to use PopupMenuUtils.isAtLeastOneChildComponentVisible(Component).
// ZAP: 2014/03/23 Issue 609: Provide a common interface to query the state and 
// access the data (HttpMessage and HistoryReference) displayed in the tabs
// ZAP: 2014/03/23 Issue 1079: Remove misplaced main pop up menu separators
// ZAP: 2014/03/23 Issue 1088: Deprecate the method ExtensionPopupMenu#prepareShow
// ZAP: 2014/08/14 Issue 1302: Context menu item action might not get executed
// ZAP: 2015/01/22 Use ExtensionPopupMenu for super menus

package org.parosproxy.paros.view;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.extension.history.PopupMenuPurgeSites;
import org.zaproxy.zap.view.popup.ExtensionPopupMenuComponent;
import org.zaproxy.zap.view.popup.PopupMenuUtils;
import org.zaproxy.zap.view.popup.PopupMenuUtils.PopupMenuInvokerWrapper;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;

public class MainPopupMenu extends JPopupMenu {

	private static final long serialVersionUID = -3021348328961418293L;

	private List<JMenuItem> itemList = null;
	private PopupMenuPurgeSites popupMenuPurgeSites = null;
	// ZAP: Added support for submenus
    Map<String, JMenu> superMenus = new HashMap<>();
    View view = null;
    private static Logger log = Logger.getLogger(MainPopupMenu.class);

    /**
     * The change listener responsible for updating the {@code pathSelectedMenu} when the path to the currently selected menu
     * item changes.
     * 
     * @see #pathSelectedMenu
     * @see MenuSelectionListenerInstaller
     */
    private MenuSelectionChangeListener menuSelectionChangeListener;

    /**
     * The path to the currently selected menu item, {@code null} when there are no selected menus or the pop up menu was
     * cancelled.
     * <p>
     * Used in the method {@code setVisible(boolean)} to inform the {@code ExtensionPopupMenuComponent}s of which menu is
     * selected when the pop up menu is hidden.
     * 
     * @see #menuSelectionChangeListener
     * @see #setVisible(boolean)
     */
    private MenuElement[] pathSelectedMenu;

    public MainPopupMenu(View view) {
        super();
 		initialize();
        this.view = view;
   }

    /**
     * @param arg0
     */
    public MainPopupMenu(String arg0, View view) {
        super(arg0);
        this.view = view;
    }
    
    public MainPopupMenu(List<JMenuItem> itemList, View view) {
        this(view);
        this.itemList = itemList;
    }
    

	/**
	 * This method initializes this
	 */
	private void initialize() {
        //this.setVisible(true);
        
	    // added pre-set popup menu here
//        this.add(getPopupFindMenu());
        //this.add(getPopupDeleteMenu());
        this.add(getPopupMenuPurgeSites());

        this.menuSelectionChangeListener = new MenuSelectionChangeListener();
        addPopupMenuListener(new MenuSelectionListenerInstaller());
	}

	public void show(final MessageContainer<?> invoker, final int x, final int y) {
		showImpl(PopupMenuUtils.getPopupMenuInvokerWrapper(invoker), x, y);
	}
	
	@Override
	public synchronized void show(Component invoker, int x, int y) {
		showImpl(PopupMenuUtils.getPopupMenuInvokerWrapper(invoker), x, y);
	}

	private synchronized void showImpl(PopupMenuInvokerWrapper invoker, final int x, final int y) {
	    
	    for (int i=0; i<getComponentCount(); i++) {
	        final Component component = getComponent(i);
	        try {
	            if (component != null && component instanceof ExtensionPopupMenuItem) {
	                ExtensionPopupMenuItem menuItem = (ExtensionPopupMenuItem) component;
	                // ZAP: prevents a NullPointerException when the treeSite doesn't have a node selected and a popup menu option (Delete/Purge) is selected
	                menuItem.setVisible(invoker.isEnable(menuItem));
	                
	                if (Control.getSingleton().getMode().equals(Mode.safe) && ! menuItem.isSafe()) {
                		// Safe mode, disable all nor safe menu items
	                	menuItem.setEnabled(false);
	                }
	            }
	        } catch (Exception e) {
	        	log.error(e.getMessage(), e);
	        }
	    }
	    
	    for (int i=0; i<itemList.size(); i++) {
	        final JMenuItem menuItem = itemList.get(i);
			if (menuItem instanceof ExtensionPopupMenuItem) {
				handleMenuItem(invoker, (ExtensionPopupMenuItem) menuItem);
			} else if (menuItem instanceof ExtensionPopupMenu) {
				ExtensionPopupMenu item = (ExtensionPopupMenu) menuItem;
				prepareShow(item);
				handleMenu(invoker, item);
			}
		}
		PopupMenuUtils.removeTopAndBottomSeparators(this);

		if (PopupMenuUtils.isAtLeastOneChildComponentVisible(this)) {
			super.show(invoker.getComponent(), x, y);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Overridden to call the method {@code ExtensionPopupMenuComponent#dismissed(ExtensionPopupMenuComponent)} of child
	 * {@code ExtensionPopupMenuComponent}s when the pop up menu is hidden.
	 * 
	 * @see ExtensionPopupMenuComponent#dismissed(ExtensionPopupMenuComponent)
	 */
	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		if (!b) {
			ExtensionPopupMenuComponent selectedMenuComponent = null;
			if (pathSelectedMenu != null) {
				MenuElement selectedMenuElement = pathSelectedMenu[pathSelectedMenu.length - 1];
				if (PopupMenuUtils.isExtensionPopupMenuComponent(selectedMenuElement)) {
					selectedMenuComponent = (ExtensionPopupMenuComponent) selectedMenuElement;
				}
				pathSelectedMenu = null;
			}

			for (int i = 0; i < getComponentCount(); i++) {
				Component component = getComponent(i);
				if (PopupMenuUtils.isExtensionPopupMenuComponent(component)) {
					((ExtensionPopupMenuComponent) component).dismissed(selectedMenuComponent);
				}
			}
		}
	}

	/**
	 * The method {@code ExtensionPopupMenu#preprepareShow()} is deprecated but it must still be called for backward
	 * compatibility, so to avoid hiding future deprecations of other methods/classes this method was added to suppress the
	 * deprecation warning locally (instead of the whole method {@code showImpl(PopupMenuInvokerWrapper, int, int))}).
	 * 
	 * @see ExtensionPopupMenu#prepareShow()
	 */
	@SuppressWarnings({"deprecation", "javadoc"})
	private static void prepareShow(ExtensionPopupMenu popupMenu) {
		popupMenu.prepareShow();
	}
	
	private void handleMenuItem(PopupMenuInvokerWrapper popupMenuInvoker, ExtensionPopupMenuItem menuItem) {
		try {
            if (menuItem == ExtensionHookMenu.POPUP_MENU_SEPARATOR) {
                PopupMenuUtils.addSeparatorIfNeeded(this);
            } else {
	            if (popupMenuInvoker.isEnable(menuItem)) {
	            	if (menuItem.isSubMenu()) {
	            	    final JMenu superMenu = getSuperMenu(menuItem.getParentMenuName(), menuItem.getParentMenuIndex());
	            		if (menuItem.precedeWithSeparator()) {
	            			PopupMenuUtils.addSeparatorIfNeeded(superMenu.getPopupMenu());
	            		}
	            		if (menuItem.isDummyItem()) {
	            			// This assumes the dummy item is the first of the children - non dummy children will enable this
	            			superMenu.setEnabled(false);
	            		} else {
	            			superMenu.add(menuItem);
	            			superMenu.setEnabled(true);
	            		}
	            		if (menuItem.succeedWithSeparator()) {
	            			superMenu.addSeparator();
	            		}
	            		
	            	} else {
	            		if (menuItem.precedeWithSeparator()) {
	    	                PopupMenuUtils.addSeparatorIfNeeded(this);
	            		}
						addMenuItem(menuItem, menuItem.getMenuIndex());
	            		if (menuItem.succeedWithSeparator()) {
	    	                this.addSeparator();
	            		}
	            	}
	            }
            }
            if (Control.getSingleton().getMode().equals(Mode.safe) && ! menuItem.isSafe()) {
        		// Safe mode, disable all nor safe menu items
            	menuItem.setEnabled(false);
            }

        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
	}
	
	private void handleMenu(PopupMenuInvokerWrapper popupMenuInvoker, ExtensionPopupMenu menu) {
		try {
            if (popupMenuInvoker.isEnable(menu)) {
            	if (menu.isSubMenu()) {
            	    final JMenu superMenu = getSuperMenu(menu.getParentMenuName(), menu.getParentMenuIndex());
            		if (menu.precedeWithSeparator()) {
            			PopupMenuUtils.addSeparatorIfNeeded(superMenu.getPopupMenu());
            		}
            		superMenu.add(menu);
            		if (menu.succeedWithSeparator()) {
            			superMenu.addSeparator();
            		}
            		
            	} else {
            		if (menu.precedeWithSeparator()) {
    	                PopupMenuUtils.addSeparatorIfNeeded(this);
            		}

            		addMenuItem(menu, menu.getMenuIndex());
            		if (menu.succeedWithSeparator()) {
    	                this.addSeparator();
            		}
            	}
                if (Control.getSingleton().getMode().equals(Mode.safe) && ! menu.isSafe()) {
            		// Safe mode, disable all nor safe menus
                	menu.setEnabled(false);
                }
            }
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        }
	}
	
	// ZAP: Added support for submenus
	private JMenu getSuperMenu (String name, int index) {
		JMenu superMenu = superMenus.get(name);
		if (superMenu == null) {
			// Use an ExtensionPopupMenu so child menus are dismissed
			superMenu = new ExtensionPopupMenu(name) {

				private static final long serialVersionUID = 6825880451078204378L;

				@Override
				public boolean isEnableForComponent(Component invoker) {
					return true;
				}
			};
			superMenus.put(name, superMenu);
			addMenuItem(superMenu, index);
		}
		return superMenu;
		
	}

	private void addMenuItem(JMenuItem menuItem, int index) {
		int correctIndex;
		if ((index < 0 && index != -1) || (index > getComponentCount())) {
			correctIndex = -1;
		} else {
			correctIndex = index;
		}
		add(menuItem, correctIndex);
	}

	private PopupMenuPurgeSites getPopupMenuPurgeSites() {
		if (popupMenuPurgeSites == null) {
			popupMenuPurgeSites = new PopupMenuPurgeSites();
		}
		return popupMenuPurgeSites;
	}

	// ZAP: added addMenu and removeMenu to support dynamic changing of menus

	public void addMenu(ExtensionPopupMenuItem menu) {
		itemList.add(menu);
	}
	
	public void removeMenu(ExtensionPopupMenuItem menu) {
		itemList.remove(menu);
	}
	
	public void addMenu(ExtensionPopupMenu menu) {
		itemList.add(menu);
	}
	
	public void removeMenu(ExtensionPopupMenu menu) {
		itemList.remove(menu);
	}
	
	/**
	 * A {@code ChangeListener} responsible for updating the {@code pathSelectedMenu} when the path to the currently selected
	 * menu item changes.
	 *
	 * @see #pathSelectedMenu
	 * @see MenuSelectionManager#getSelectedPath()
	 * @see MenuSelectionListenerInstaller
	 */
	private class MenuSelectionChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			pathSelectedMenu = MenuSelectionManager.defaultManager().getSelectedPath();
		}
	}

	/**
	 * A {@code PopupMenuListener} responsible for adding and removing the {@code menuSelectionChangeListener} when the menu
	 * becomes visible and invisible, respectively. It also sets the {@code pathSelectedMenu} to {@code null} when the menu is
	 * cancelled.
	 * 
	 * @see #menuSelectionChangeListener
	 * @see #pathSelectedMenu
	 * @see PopupMenuListener
	 */
	private class MenuSelectionListenerInstaller implements PopupMenuListener {

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			MenuSelectionManager.defaultManager().addChangeListener(menuSelectionChangeListener);
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			MenuSelectionManager.defaultManager().removeChangeListener(menuSelectionChangeListener);
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
			pathSelectedMenu = null;
		}
	}
}

