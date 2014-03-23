/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.view.popup;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.ExtensionPopupMenu;

/**
 * A helper class with common utility methods for pop up menus ({@code JPopupMenu}) and its related classes ({@code JMenu}s,
 * {@code JMenuItem}s, {@code ExtensionPopupMenu}s, {@code ExtensionPopupMenuItem}s).
 * 
 * @see JPopupMenu
 * @see JMenu
 * @see ExtensionPopupMenu
 * @see ExtensionPopupMenuItem
 */
public final class PopupMenuUtils {

    private PopupMenuUtils() {
    }

    /**
     * Tells whether or not the given {@code component} has at least one child component visible.
     * 
     * @param component the component that will be checked
     * @return {@code true} if at least one child component is visible, {@code false} otherwise.
     */
    public static boolean isAtLeastOneChildComponentVisible(Container component) {
        for (Component comp : component.getComponents()) {
            if (comp.isVisible()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience method that calls the method {@code isAtLeastOneChildComponentVisible(Container)} with the {@code JPopupMenu}
     * of the given {@code menu} as parameter.
     * 
     * @param menu the menu that will be checked
     * @return {@code true} if at least one child component is visible, {@code false} otherwise.
     * @see #isAtLeastOneChildComponentVisible(Container)
     * @see JMenu
     */
    public static boolean isAtLeastOneChildComponentVisible(JMenu menu) {
        return isAtLeastOneChildComponentVisible(menu.getPopupMenu());
    }

    /**
     * Tells whether or not the given {@code component} is an {@code ExtensionPopupMenuItem}.
     * 
     * @param component the component that will be checked.
     * @return {@code true} if the given component is an {@code ExtensionPopupMenuItem}, {@code false} otherwise.
     * @see ExtensionPopupMenuItem
     * @see #isExtensionPopupMenu(Component)
     * @see #isPopupMenuSeparator(Component)
     */
    public static boolean isExtensionPopupMenuItem(Component component) {
        return (component instanceof ExtensionPopupMenuItem);
    }

    /**
     * Tells whether or not the given {@code component} is an {@code ExtensionPopupMenu}.
     * 
     * @param component the component that will be checked.
     * @return {@code true} if the given component is an {@code ExtensionPopupMenu}, {@code false} otherwise.
     * @see ExtensionPopupMenu
     * @see #isExtensionPopupMenuItem(Component)
     * @see #isPopupMenuSeparator(Component)
     */
    public static boolean isExtensionPopupMenu(Component component) {
        return (component instanceof ExtensionPopupMenu);
    }

    /**
     * Tells whether or not the given {@code component} is a {@code JPopupMenu.Separator}.
     * 
     * @param component the component that will be checked.
     * @return {@code true} if the given component is a {@code JPopupMenu.Separator}, {@code false} otherwise.
     * @see javax.swing.JPopupMenu.Separator
     * @see #isExtensionPopupMenu(Component)
     * @see #isExtensionPopupMenuItem(Component)
     */
    public static boolean isPopupMenuSeparator(Component component) {
        return (component instanceof JPopupMenu.Separator);
    }
}
