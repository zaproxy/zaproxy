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
// ZAP: 2014/03/23 Issue 1095: Replace main pop up sub menus with ExtensionPopupMenu when
// appropriate
// ZAP: 2014/08/14 Issue 1302: Context menu item action might not get executed
// ZAP: 2017/06/01 Add more constructors.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2024/02/23 Added support for menu weights.
package org.parosproxy.paros.extension;

import java.awt.Component;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.popup.ExtensionPopupMenuComponent;
import org.zaproxy.zap.view.popup.MenuWeights;

public class ExtensionPopupMenuItem extends JMenuItem implements ExtensionPopupMenuComponent {

    private static final long serialVersionUID = -5454473736753550528L;

    private int weight = MenuWeights.MENU_DEFAULT_WEIGHT;
    private int parentWeight = MenuWeights.MENU_DEFAULT_WEIGHT;

    /**
     * Use weights instead.
     *
     * @deprecated 2.15.0
     */
    @Deprecated(since = "2.15.0")
    public static final int ATTACK_MENU_INDEX = 0;

    /**
     * Use weights instead.
     *
     * @deprecated 2.15.0
     */
    @Deprecated(since = "2.15.0")
    public static final int EXCLUDE_MENU_INDEX = 1;

    /**
     * Use weights instead.
     *
     * @deprecated 2.15.0
     */
    @Deprecated(since = "2.15.0")
    public static final int CONTEXT_FLAG_MENU_INDEX = 1;

    /**
     * Use weights instead.
     *
     * @deprecated 2.15.0
     */
    @Deprecated(since = "2.15.0")
    public static final int FLAG_MENU_INDEX = 2;

    /** Constructs an {@code ExtensionPopupMenuItem} with no text nor icon. */
    public ExtensionPopupMenuItem() {
        super();
    }

    /**
     * Constructs an {@code ExtensionPopupMenuItem} with the given text and no icon.
     *
     * @param text the text of the menu item.
     */
    public ExtensionPopupMenuItem(String text) {
        super(text);
    }

    /**
     * Constructs an {@code ExtensionPopupMenuItem} with the given text and icon.
     *
     * @param text the text of the menu item.
     * @param icon the icon of the menu item.
     * @since 2.7.0
     */
    public ExtensionPopupMenuItem(String text, Icon icon) {
        super(text, icon);
    }

    /**
     * Constructs an {@code ExtensionPopupMenuItem} with the given action.
     *
     * <p>The text and icon (if any) are obtained from the given action.
     *
     * @param action the action of the menu item.
     * @since 2.7.0
     */
    public ExtensionPopupMenuItem(Action action) {
        super(action);
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
     * Defaults to call the method {@code isEnableForComponent(Component)} passing as parameter the
     * component returned by the method {@code MessageContainer#getComponent()} called on the given
     * {@code invoker}.
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
    @Deprecated(since = "2.15.0")
    public int getMenuIndex() {
        return -1;
    }

    /**
     * This is no longer used. Use setWeight instead.
     *
     * @deprecated
     */
    @Deprecated(since = "2.15.0")
    public void setMenuIndex(int menuIndex) {}

    /**
     * Gets the weight of the component.
     *
     * @since 2.15.0
     */
    @Override
    public int getWeight() {
        return weight;
    }

    /**
     * Sets the weight of the component.
     *
     * @since 2.15.0
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    /**
     * Gets the weight of the parent component.
     *
     * @since 2.15.0
     */
    public int getParentWeight() {
        return parentWeight;
    }

    /**
     * Sets the weight of the parent component.
     *
     * @since 2.15.0
     */
    public void setParentWeight(int parentWeight) {
        this.parentWeight = parentWeight;
    }

    /**
     * This is no longer used. Use getParentWeight instead.
     *
     * @deprecated
     */
    @Deprecated(since = "2.15.0")
    public int getParentMenuIndex() {
        return -1;
    }

    @Deprecated(since = "2.15.0")
    /**
     * This is no longer used. Use setParentWeight instead.
     *
     * @deprecated
     */
    public void setParentMenuIndex(int parentMenuIndex) {}

    // ZAP: Support submenus
    public boolean isSubMenu() {
        return false;
    }

    // ZAP: Support dummy menu items - used for dynamic menus
    public boolean isDummyItem() {
        return false;
    }

    @Override
    @Deprecated
    public boolean precedeWithSeparator() {
        return false;
    }

    @Deprecated
    public void setPrecedeWithSeparator(boolean precede) {}

    @Override
    @Deprecated
    public boolean succeedWithSeparator() {
        return false;
    }

    @Deprecated
    public void setSucceedWithSeparator(boolean succeed) {}

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
    public void dismissed(ExtensionPopupMenuComponent selectedMenuComponent) {}
}
