/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JMenu;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.popup.ExtensionPopupMenuComponent;
import org.zaproxy.zap.view.popup.PopupMenuUtils;

public class ExtensionPopupMenu extends JMenu implements ExtensionPopupMenuComponent {

    private static final long serialVersionUID = 1925623776527543421L;

    /**
     * Flag that indicates if the children should be ordered.
     *
     * @see #setOrderChildren(boolean)
     * @see #processExtensionPopupChildren(PopupMenuUtils.PopupMenuInvokerWrapper)
     */
    private boolean orderChildren;

    /** Constructs an {@code ExtensionPopupMenu} with no text. */
    public ExtensionPopupMenu() {
        super();
    }

    /**
     * Constructs an {@code ExtensionPopupMenu} with the given text.
     *
     * @param text the text of the menu item.
     */
    public ExtensionPopupMenu(String text) {
        super(text);
    }

    /**
     * Sets whether or not the menu index of the child components, returned by the method {@code
     * ExtensionPopupMenuComponent#getMenuIndex()}, should be honoured.
     *
     * <p>Default is {@code false}.
     *
     * <p>The child components will be ordered from minor to greater index (starting at index 0 from
     * top of the menu). If two or more child components have the same index, the index that those
     * child components will have, between each other, is undefined.
     *
     * <p>Note: Separators added manually (with {@code ExtensionPopupMenu#addSeparator()} or {@code
     * ExtensionPopupMenu#addSeparator(int)} are not preserved between reorderings.
     *
     * @param orderChildren {@code true} if the child components should ordered, {@code false}
     *     otherwise.
     * @see #isOrderChildren()
     * @see #processExtensionPopupChildren(PopupMenuUtils.PopupMenuInvokerWrapper)
     * @see ExtensionPopupMenuComponent#getMenuIndex()
     * @see ExtensionPopupMenu#addSeparator()
     * @see ExtensionPopupMenu#insertSeparator(int)
     */
    public void setOrderChildren(boolean orderChildren) {
        this.orderChildren = orderChildren;
    }

    /**
     * Tells whether or not the menu index of the child components, returned by the method {@code
     * ExtensionPopupMenuComponent#getMenuIndex()}, should be honoured.
     *
     * @return {@code true} if the child components will be ordered, {@code false} otherwise.
     * @see #setOrderChildren(boolean)
     */
    public boolean isOrderChildren() {
        return orderChildren;
    }

    /**
     * By default, the pop up menu button is enabled and the pop up menu is only enable for the
     * given {@code invoker} if at least one of the child menus and menu items is enable for the
     * given {@code invoker}.
     *
     * <p>Although the pop up menu is allowed to contain child menus and menu items of any type of
     * {@code JMenu} or {@code JMenuItem} the only children considered as enablers are the ones of
     * the type of {@code ExtensionPopupMenuComponent}.
     *
     * <p>The {@code ExtensionPopupMenuComponent}s are considered enable if the corresponding method
     * {@code isEnableForComponent(Component)}, with {@code invoker} as parameter, returns {@code
     * true}.
     *
     * <p>All the child menus that implement {@code ExtensionPopupMenuComponent} will have the
     * methods {@code precedeWithSeparator()}, {@code succeedWithSeparator()}, {@code
     * getMenuIndex()} and {@code isSafe()} honoured, with the following caveats:
     *
     * <ul>
     *   <li>{@code precedeWithSeparator()} - the separator will only be added if there's already a
     *       menu component in the menu and if it is not a separator;
     *   <li>{@code succeedWithSeparator()} - the separator will be added always but removed if
     *       there's no item following it when the menu is ready to be shown;
     *   <li>{@code getMenuIndex()} - the menu index will be honoured only if the method {@code
     *       isOrderChildren()} returns {@code true};
     * </ul>
     *
     * The separators will be dynamically added and removed as needed when the pop up menu is shown.
     *
     * <p><strong>Implementation Note:</strong> The method {@code isEnableForComponent(Component)}
     * is called on all child {@code ExtensionPopupMenuComponent}s, even if a previous child has
     * returned {@code true}, as it allows to notify all the children that the pop up menu in which
     * they are, is being invoked. Subclasses should take it into account when overriding this the
     * method.
     *
     * @see #processExtensionPopupChildren(PopupMenuUtils.PopupMenuInvokerWrapper)
     * @see ExtensionPopupMenuComponent#isEnableForComponent(Component)
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    @Override
    public boolean isEnableForComponent(Component invoker) {
        return processExtensionPopupChildren(PopupMenuUtils.getPopupMenuInvokerWrapper(invoker));
    }

    /**
     * Returns {@code true} if at least one of the child {@code ExtensionPopupMenuComponent}s is
     * enable, {@code false} otherwise.
     *
     * <p>The method {@code isEnableForComponent(Component)} or {@code
     * isEnableForMessageContainer(MessageContainer)}, depending on actual implementation of the
     * given {@code invokerWrapper}, is called on all child {@code ExtensionPopupMenuComponent}s.
     *
     * <p>All the child menus that implement {@code ExtensionPopupMenuComponent} will have the
     * methods {@code precedeWithSeparator()}, {@code succeedWithSeparator()}, {@code
     * getMenuIndex()} and {@code isSafe()} honoured, with the following caveats:
     *
     * <ul>
     *   <li>{@code precedeWithSeparator()} - the separator will only be added if there's already a
     *       menu component in the menu and if it is not a separator;
     *   <li>{@code succeedWithSeparator()} - the separator will be added always but removed if
     *       there's no item following it when the menu is ready to be shown;
     *   <li>{@code getMenuIndex()} - the menu index will be honoured only if the method {@code
     *       isOrderChildren()} returns {@code true};
     * </ul>
     *
     * The separators will be dynamically added and removed as needed when the pop up menu is shown.
     *
     * @param invokerWrapper the wrapped invoker
     * @return {@code true} if at least one of the child items is enable, {@code false} otherwise.
     * @see #isOrderChildren()
     * @see ExtensionPopupMenuComponent
     * @see ExtensionPopupMenuComponent#isEnableForComponent(Component)
     * @see ExtensionPopupMenuComponent#isEnableForMessageContainer(MessageContainer)
     * @see ExtensionPopupMenuComponent#precedeWithSeparator()
     * @see ExtensionPopupMenuComponent#succeedWithSeparator()
     * @see ExtensionPopupMenuComponent#isSafe()
     * @see ExtensionPopupMenuComponent#getMenuIndex()
     */
    protected boolean processExtensionPopupChildren(
            PopupMenuUtils.PopupMenuInvokerWrapper invokerWrapper) {
        if (isOrderChildren()) {
            PopupMenuUtils.removeAllSeparators(this);
            List<ExtensionPopupMenuComponent> components = new ArrayList<>();
            for (int i = 0; i < getMenuComponentCount(); i++) {
                Component component = getMenuComponent(i);
                if (PopupMenuUtils.isExtensionPopupMenuComponent(component)) {
                    ExtensionPopupMenuComponent menuComponent =
                            (ExtensionPopupMenuComponent) component;
                    if (menuComponent.getMenuIndex() >= 0) {
                        components.add(menuComponent);
                        remove(i);
                        i--;
                    }
                }
            }
            Collections.sort(
                    components,
                    new Comparator<ExtensionPopupMenuComponent>() {

                        @Override
                        public int compare(
                                ExtensionPopupMenuComponent component,
                                ExtensionPopupMenuComponent otherComponent) {
                            if (component.getMenuIndex() > otherComponent.getMenuIndex()) {
                                return 1;
                            } else if (component.getMenuIndex() < otherComponent.getMenuIndex()) {
                                return -1;
                            }
                            return 0;
                        }
                    });

            for (int i = 0; i < components.size(); i++) {
                ExtensionPopupMenuComponent component = components.get(i);
                int index = Math.max(component.getMenuIndex(), i);
                if (index >= getMenuComponentCount()) {
                    add((Component) component);
                } else {
                    getPopupMenu().insert((Component) component, index);
                }
            }
        }

        boolean childEnable = false;
        Control.Mode mode = Control.getSingleton().getMode();
        for (int i = 0; i < getMenuComponentCount(); ++i) {
            Component menuComponent = getMenuComponent(i);
            if (PopupMenuUtils.isExtensionPopupMenuComponent(menuComponent)) {
                ExtensionPopupMenuComponent extensionMenuComponent =
                        (ExtensionPopupMenuComponent) menuComponent;
                boolean enable = invokerWrapper.isEnable(extensionMenuComponent);

                if (enable && !extensionMenuComponent.isSafe() && mode.equals(Control.Mode.safe)) {
                    menuComponent.setEnabled(false);
                    continue;
                }

                if (enable) {
                    childEnable = true;
                    if (extensionMenuComponent.precedeWithSeparator()) {
                        if (PopupMenuUtils.insertSeparatorIfNeeded(this, i)) {
                            i++;
                        }
                    }
                }
                menuComponent.setVisible(enable);
                if (enable && extensionMenuComponent.succeedWithSeparator()) {
                    if (PopupMenuUtils.insertSeparatorIfNeeded(this, i + 1)) {
                        i++;
                    }
                }
            }
        }

        PopupMenuUtils.removeTopAndBottomSeparators(this);

        return childEnable;
    }

    /**
     * Defaults to call the method {@code isEnableForComponent(Component)} passing as parameter the
     * component returned by the method {@code MessageContainer#getComponent()} called on the given
     * {@code invoker}.
     *
     * @see #isEnableForComponent(Component)
     * @see MessageContainer#getComponent()
     * @since 2.3.0
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
        return -1;
    }

    public int getParentMenuIndex() {
        return -1;
    }

    public boolean isSubMenu() {
        return false;
    }

    @Override
    public boolean precedeWithSeparator() {
        return false;
    }

    @Override
    public boolean succeedWithSeparator() {
        return false;
    }

    @Override
    public boolean isSafe() {
        return true;
    }

    /**
     * Defaults to call the method {@link
     * ExtensionPopupMenuComponent#dismissed(ExtensionPopupMenuComponent)
     * dismissed(ExtensionPopupMenuComponent)} on all child {@code ExtensionPopupMenuComponent}s.
     *
     * @since 2.4.0
     */
    @Override
    public void dismissed(ExtensionPopupMenuComponent selectedMenuComponent) {
        for (int i = 0; i < getMenuComponentCount(); ++i) {
            Component menuComponent = getMenuComponent(i);
            if (PopupMenuUtils.isExtensionPopupMenuComponent(menuComponent)) {
                ((ExtensionPopupMenuComponent) menuComponent).dismissed(selectedMenuComponent);
            }
        }
    }
}
