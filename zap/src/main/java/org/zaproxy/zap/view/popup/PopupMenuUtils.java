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
import javax.swing.MenuElement;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;

/**
 * A helper class with common utility methods for pop up menus ({@code JPopupMenu}) and its related
 * classes ({@code JMenu}s, {@code JMenuItem}s, {@code ExtensionPopupMenu}s, {@code
 * ExtensionPopupMenuItem}s).
 *
 * @since 2.3.0
 * @see JPopupMenu
 * @see JMenu
 * @see ExtensionPopupMenu
 * @see ExtensionPopupMenuItem
 */
public final class PopupMenuUtils {

    private PopupMenuUtils() {}

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
     * Convenience method that calls the method {@code isAtLeastOneChildComponentVisible(Container)}
     * with the {@code JPopupMenu} of the given {@code menu} as parameter.
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
     * @return {@code true} if the given component is an {@code ExtensionPopupMenuItem}, {@code
     *     false} otherwise.
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
     * @return {@code true} if the given component is an {@code ExtensionPopupMenu}, {@code false}
     *     otherwise.
     * @see ExtensionPopupMenu
     * @see #isExtensionPopupMenuItem(Component)
     * @see #isPopupMenuSeparator(Component)
     */
    public static boolean isExtensionPopupMenu(Component component) {
        return (component instanceof ExtensionPopupMenu);
    }

    /**
     * Tells whether or not the given {@code component} is an {@code ExtensionPopupMenuComponent}.
     *
     * @param component the component that will be checked.
     * @return {@code true} if the given component is an {@code ExtensionPopupMenuComponent}, {@code
     *     false} otherwise.
     * @see ExtensionPopupMenuComponent
     */
    public static boolean isExtensionPopupMenuComponent(Component component) {
        return (component instanceof ExtensionPopupMenuComponent);
    }

    /**
     * Tells whether or not the given {@code menuElement} is an {@code ExtensionPopupMenuComponent}.
     *
     * @param menuElement the menu element that will be checked.
     * @return {@code true} if the given menu element is an {@code ExtensionPopupMenuComponent},
     *     {@code false} otherwise.
     * @since 2.4.0
     * @see ExtensionPopupMenuComponent
     */
    public static boolean isExtensionPopupMenuComponent(MenuElement menuElement) {
        return (menuElement instanceof ExtensionPopupMenuComponent);
    }

    /**
     * Tells whether or not the given {@code component} is a {@code JPopupMenu.Separator}.
     *
     * @param component the component that will be checked.
     * @return {@code true} if the given component is a {@code JPopupMenu.Separator}, {@code false}
     *     otherwise.
     * @see javax.swing.JPopupMenu.Separator
     * @see #isExtensionPopupMenu(Component)
     * @see #isExtensionPopupMenuItem(Component)
     */
    public static boolean isPopupMenuSeparator(Component component) {
        return (component instanceof JPopupMenu.Separator);
    }

    /**
     * Removes all separators from the given pop up menu.
     *
     * @param popupMenu the pop up menu whose separators will be removed
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeAllSeparators(JPopupMenu popupMenu) {
        for (int i = 0; i < popupMenu.getComponentCount(); i++) {
            if (isPopupMenuSeparator(popupMenu.getComponent(i))) {
                popupMenu.remove(i);
                i--;
            }
        }
    }

    /**
     * Convenience method that calls the method {@code removeAllSeparators(JPopupMenu)} with the
     * {@code JPopupMenu} of the given {@code menu} as parameter.
     *
     * @param menu the menu whose separators will be removed
     * @see #removeAllSeparators(JPopupMenu)
     * @see JMenu#getPopupMenu()
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeAllSeparators(JMenu menu) {
        removeAllSeparators(menu.getPopupMenu());
    }

    /**
     * Removes all consecutive separators from the given menu.
     *
     * <p>For example, calling the method on the given menu:
     *
     * <pre>
     *    Menu Entry
     *    Separator
     *    Menu Entry
     *    Separator
     *    Separator
     *    Menu Entry
     * </pre>
     *
     * would result in:
     *
     * <pre>
     *    Menu Entry
     *    Separator
     *    Menu Entry
     *    Separator
     *    Menu Entry
     * </pre>
     *
     * @param popupMenu the pop up menu whose consecutive separators will be removed
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeConsecutiveSeparators(JPopupMenu popupMenu) {
        for (int i = 1; i < popupMenu.getComponentCount(); i++) {
            if (isPopupMenuSeparator(popupMenu.getComponent(i))) {
                if (isPopupMenuSeparator(popupMenu.getComponent(i - 1))) {
                    popupMenu.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Convenience method that calls the method {@code removeConsecutiveSeparators(JPopupMenu)} with
     * the {@code JPopupMenu} of the given {@code menu} as parameter.
     *
     * @param menu the menu whose consecutive separators will be removed
     * @see #removeConsecutiveSeparators(JPopupMenu)
     * @see JMenu#getPopupMenu()
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeConsecutiveSeparators(JMenu menu) {
        removeConsecutiveSeparators(menu.getPopupMenu());
    }

    /**
     * Removes all top separators from the given pop up menu.
     *
     * <p>For example, calling the method on the given menu:
     *
     * <pre>
     *    Separator
     *    Separator
     *    Menu Entry
     *    Separator
     *    Menu Entry
     *    Separator
     *    Menu Entry
     * </pre>
     *
     * would result in:
     *
     * <pre>
     *    Menu Entry
     *    Separator
     *    Menu Entry
     *    Separator
     *    Menu Entry
     * </pre>
     *
     * @param popupMenu the pop up menu whose top separators will be removed
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeTopSeparators(JPopupMenu popupMenu) {
        while (popupMenu.getComponentCount() > 0
                && isPopupMenuSeparator(popupMenu.getComponent(0))) {
            popupMenu.remove(0);
        }
    }

    /**
     * Convenience method that calls the method {@code removeTopSeparators(JPopupMenu)} with the
     * {@code JPopupMenu} of the given {@code menu} as parameter.
     *
     * @param menu the menu whose top separators will be removed
     * @see #removeTopSeparators(JPopupMenu)
     * @see JMenu#getPopupMenu()
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeTopSeparators(JMenu menu) {
        removeTopSeparators(menu.getPopupMenu());
    }

    /**
     * Removes all bottom separators from the given pop up menu.
     *
     * <p>For example, calling the method on the given menu:
     *
     * <pre>
     *    Menu Entry
     *    Separator
     *    Menu Entry
     *    Separator
     *    Menu Entry
     *    Separator
     *    Separator
     * </pre>
     *
     * would result in:
     *
     * <pre>
     *    Menu Entry
     *    Separator
     *    Menu Entry
     *    Separator
     *    Menu Entry
     * </pre>
     *
     * @param popupMenu the pop up menu whose bottom separators will be removed
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeBottomSeparators(JPopupMenu popupMenu) {
        int indexLastComponent = popupMenu.getComponentCount() - 1;
        while (indexLastComponent >= 0
                && isPopupMenuSeparator(popupMenu.getComponent(indexLastComponent))) {
            popupMenu.remove(indexLastComponent);
            indexLastComponent -= 1;
        }
    }

    /**
     * Convenience method that calls the method {@code removeBottomSeparators(JPopupMenu)} with the
     * {@code JPopupMenu} of the given {@code menu} as parameter.
     *
     * @param menu the menu whose bottom separators will be removed
     * @see #removeBottomSeparators(JPopupMenu)
     * @see JMenu#getPopupMenu()
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeBottomSeparators(JMenu menu) {
        removeBottomSeparators(menu.getPopupMenu());
    }

    /**
     * Convenience method that calls the methods {@code removeTopSeparators(JPopupMenu)} and {@code
     * removeBottomSeparators(JPopupMenu)} with the given pop up menu as parameter.
     *
     * @param popupMenu the pop up menu whose top and bottom separators will be removed
     * @see #removeTopSeparators(JPopupMenu)
     * @see #removeBottomSeparators(JPopupMenu)
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeTopAndBottomSeparators(JPopupMenu popupMenu) {
        if (popupMenu.getComponentCount() == 0) {
            return;
        }
        removeTopSeparators(popupMenu);
        removeBottomSeparators(popupMenu);
    }

    /**
     * Convenience method that calls the method {@code removeTopAndBottomSeparators(JPopupMenu)}
     * with the {@code JPopupMenu} of the given {@code menu} as parameter.
     *
     * @param menu the menu whose top and bottom separators will be removed
     * @see #removeTopAndBottomSeparators(JPopupMenu)
     * @see JMenu#getPopupMenu()
     * @see javax.swing.JPopupMenu.Separator
     */
    public static void removeTopAndBottomSeparators(JMenu menu) {
        removeTopAndBottomSeparators(menu.getPopupMenu());
    }

    /**
     * Appends a separator to the end of the menu if it exists at least one non separator menu
     * component immediately before and if there isn't, already, a separator at the end of the menu.
     *
     * @param popupMenu the pop up menu that will be processed
     * @return {@code true} if the separator was added, {@code false} otherwise.
     * @see javax.swing.JPopupMenu.Separator
     */
    public static boolean addSeparatorIfNeeded(JPopupMenu popupMenu) {
        final int menuComponentCount = popupMenu.getComponentCount();
        if (menuComponentCount == 0) {
            return false;
        }
        final Component lastMenuComponent = popupMenu.getComponent(menuComponentCount - 1);
        if (isPopupMenuSeparator(lastMenuComponent)) {
            return false;
        }
        popupMenu.addSeparator();
        return true;
    }

    /**
     * Convenience method that calls the method {@code addSeparatorIfNeeded(JPopupMenu)} with the
     * {@code JPopupMenu} of the given {@code menu} as parameter.
     *
     * @param menu the menu that will be processed
     * @return {@code true} if the separator was added, {@code false} otherwise.
     * @see #addSeparatorIfNeeded(JPopupMenu)
     * @see JMenu#getPopupMenu()
     * @see javax.swing.JPopupMenu.Separator
     */
    public static boolean addSeparatorIfNeeded(JMenu menu) {
        return addSeparatorIfNeeded(menu.getPopupMenu());
    }

    /**
     * Inserts a separator at the given {@code position} if it exists a non separator menu component
     * at the given {@code position} and if there isn't, already, a separator immediately before the
     * insert {@code position} (to prevent consecutive separators).
     *
     * @param popupMenu the pop up menu that will be processed
     * @param position the position where a separator might be inserted
     * @return {@code true} if the separator was inserted, {@code false} otherwise.
     * @see javax.swing.JPopupMenu.Separator
     */
    public static boolean insertSeparatorIfNeeded(JPopupMenu popupMenu, int position) {
        final int menuComponentCount = popupMenu.getComponentCount();
        if (menuComponentCount == 0 || position <= 0 || position > menuComponentCount) {
            return false;
        }
        final Component currentComponent = popupMenu.getComponent(position);
        if (isPopupMenuSeparator(currentComponent)) {
            return false;
        }
        final Component previousComponent = popupMenu.getComponent(position - 1);
        if (isPopupMenuSeparator(previousComponent)) {
            return false;
        }
        popupMenu.insert(new JPopupMenu.Separator(), position);
        return true;
    }

    /**
     * Convenience method that calls the method {@code insertSeparatorIfNeeded(JPopupMenu,int)} with
     * the {@code JPopupMenu} of the given {@code menu} as the first parameter and with the given
     * {@code position} as second parameter.
     *
     * @param menu the menu that will be processed
     * @param position the position where a separator might be inserted
     * @return {@code true} if the separator was inserted, {@code false} otherwise.
     * @see #insertSeparatorIfNeeded(JPopupMenu,int)
     * @see JMenu#getPopupMenu()
     * @see javax.swing.JPopupMenu.Separator
     */
    public static boolean insertSeparatorIfNeeded(JMenu menu, int position) {
        return insertSeparatorIfNeeded(menu.getPopupMenu(), position);
    }

    /**
     * Returns the {@code component} wrapped in a {@code PopupMenuInvokerWrapper}.
     *
     * @param component the component that will be wrapped.
     * @return a {@code PopupMenuInvokerWrapper} wrapping the given {@code component}.
     * @see PopupMenuInvokerWrapper
     */
    public static PopupMenuInvokerWrapper getPopupMenuInvokerWrapper(final Component component) {
        return new ComponentPopupMenuInvoker(component);
    }

    /**
     * Returns the {@code messageContainer} wrapped in a {@code PopupMenuInvokerWrapper}.
     *
     * @param messageContainer the message container that will be wrapped.
     * @return a {@code PopupMenuInvokerWrapper} wrapping the given {@code messageContainer}.
     * @see PopupMenuInvokerWrapper
     */
    public static PopupMenuInvokerWrapper getPopupMenuInvokerWrapper(
            final MessageContainer<?> messageContainer) {
        return new MessageContainerPopupMenuInvoker(messageContainer);
    }

    /**
     * An abstract class that allows to check if a {@code ExtensionPopupMenuComponent} is enable for
     * the contained wrapped object.
     *
     * <p>It allows to use the same object ({@code PopupMenuInvokerWrapper}) for both types {@code
     * Component} and {@code MessageContainer}, with {@code ComponentPopupMenuInvoker} and {@code
     * MessageContainerPopupMenuInvoker}, respectively.
     *
     * @see #isEnable(ExtensionPopupMenuComponent)
     * @see ComponentPopupMenuInvoker
     * @see MessageContainerPopupMenuInvoker
     */
    public abstract static class PopupMenuInvokerWrapper {

        private final Component component;

        /**
         * Constructs a {@code PopupMenuInvokerWrapper} with the given {@code component} of the
         * wrapped object.
         *
         * @param component the component of the wrapped object.
         * @throws IllegalArgumentException if the given {@code component} is null
         */
        public PopupMenuInvokerWrapper(final Component component) {
            if (component == null) {
                throw new IllegalArgumentException("Parameter component must not be null.");
            }

            this.component = component;
        }

        /**
         * Returns the {@code Component} of the wrapped object.
         *
         * @return the component of the wrapped object.
         */
        public Component getComponent() {
            return component;
        }

        /**
         * Tells whether the given {@code menuComponent} is enable for the wrapped object.
         *
         * @param menuComponent the menu component that will be tested
         * @return {@code true} if the menu component is enable for this wrapped object, {@code
         *     false} otherwise.
         */
        public abstract boolean isEnable(ExtensionPopupMenuComponent menuComponent);
    }

    /**
     * A {@code PopupMenuInvokerWrapper} for {@code Component}s.
     *
     * <p>Calls the method {@code ExtensionPopupMenuComponent#isEnableForComponent(Component)}, with
     * the wrapped object as parameter.
     *
     * @see PopupMenuInvokerWrapper
     * @see MessageContainerPopupMenuInvoker
     * @see ExtensionPopupMenuComponent#isEnableForComponent(Component)
     */
    public static class ComponentPopupMenuInvoker extends PopupMenuInvokerWrapper {

        /**
         * Constructs a {@code ComponentPopupMenuInvoker} with the given {@code component} as the
         * wrapped object.
         *
         * @param component the component that will be wrapped
         * @throws IllegalArgumentException if the given {@code component} is null
         */
        public ComponentPopupMenuInvoker(Component component) {
            super(component);
        }

        @Override
        public boolean isEnable(ExtensionPopupMenuComponent menuComponent) {
            return menuComponent.isEnableForComponent(getComponent());
        }
    }

    /**
     * A {@code PopupMenuInvokerWrapper} for {@code MessageContainer}s.
     *
     * <p>Calls the method {@code
     * ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)}, with the wrapped
     * object as parameter.
     *
     * @see PopupMenuInvokerWrapper
     * @see ComponentPopupMenuInvoker
     * @see ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)
     */
    public static class MessageContainerPopupMenuInvoker extends PopupMenuInvokerWrapper {

        private final MessageContainer<?> messageContainer;

        /**
         * Constructs a {@code MessageContainerPopupMenuInvoker} with the given {@code
         * messageContainer} as the wrapped object.
         *
         * @param messageContainer the message container that will be wrapped
         * @throws NullPointerException if the given {@code messageContainer} is null
         */
        public MessageContainerPopupMenuInvoker(final MessageContainer<?> messageContainer) {
            super(messageContainer.getComponent());

            this.messageContainer = messageContainer;
        }

        @Override
        public boolean isEnable(ExtensionPopupMenuComponent menuComponent) {
            return menuComponent.isEnableForMessageContainer(messageContainer);
        }
    }
}
