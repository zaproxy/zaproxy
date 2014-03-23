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
import org.zaproxy.zap.view.messagecontainer.MessageContainer;

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
    public static PopupMenuInvokerWrapper getPopupMenuInvokerWrapper(final MessageContainer<?> messageContainer) {
        return new MessageContainerPopupMenuInvoker(messageContainer);
    }

    /**
     * An abstract class that allows to check if a {@code ExtensionPopupMenuComponent} is enable for the contained wrapped
     * object.
     * <p>
     * It allows to use the same object ({@code PopupMenuInvokerWrapper}) for both types {@code Component} and
     * {@code MessageContainer}, with {@code ComponentPopupMenuInvoker} and {@code MessageContainerPopupMenuInvoker},
     * respectively.
     * 
     * @see #isEnable(ExtensionPopupMenuComponent)
     * @see ComponentPopupMenuInvoker
     * @see MessageContainerPopupMenuInvoker
     */
    public static abstract class PopupMenuInvokerWrapper {

        private final Component component;

        /**
         * Constructs a {@code PopupMenuInvokerWrapper} with the given {@code component} of the wrapped object.
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
         * @return {@code true} if the menu component is enable for this wrapped object, {@code false} otherwise.
         */
        public abstract boolean isEnable(ExtensionPopupMenuComponent menuComponent);
    }

    /**
     * A {@code PopupMenuInvokerWrapper} for {@code Component}s.
     * <p>
     * Calls the method {@code ExtensionPopupMenuComponent#isEnableForComponent(Component)}, with the wrapped object as
     * parameter.
     * 
     * @see PopupMenuInvokerWrapper
     * @see MessageContainerPopupMenuInvoker
     * @see ExtensionPopupMenuComponent#isEnableForComponent(Component)
     */
    public static class ComponentPopupMenuInvoker extends PopupMenuInvokerWrapper {

        /**
         * Constructs a {@code ComponentPopupMenuInvoker} with the given {@code component} as the wrapped object.
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
     * <p>
     * Calls the method {@code ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)}, with the wrapped
     * object as parameter.
     * 
     * @see PopupMenuInvokerWrapper
     * @see ComponentPopupMenuInvoker
     * @see ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)
     */
    public static class MessageContainerPopupMenuInvoker extends PopupMenuInvokerWrapper {

        private final MessageContainer<?> messageContainer;

        /**
         * Constructs a {@code MessageContainerPopupMenuInvoker} with the given {@code messageContainer} as the wrapped object.
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
