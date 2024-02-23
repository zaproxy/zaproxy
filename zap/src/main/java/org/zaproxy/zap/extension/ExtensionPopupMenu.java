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
import javax.swing.JMenu;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;
import org.zaproxy.zap.view.popup.ExtensionPopupMenuComponent;
import org.zaproxy.zap.view.popup.MenuWeights;
import org.zaproxy.zap.view.popup.PopupMenuUtils;

public class ExtensionPopupMenu extends JMenu implements ExtensionPopupMenuComponent {

    private int weight = MenuWeights.MENU_DEFAULT_WEIGHT;
    private static final long serialVersionUID = 1925623776527543421L;

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
     * Replaced by weights.
     *
     * @deprecated
     */
    @Deprecated(since = "2.15.0")
    public void setOrderChildren(boolean orderChildren) {}

    /**
     * Replaced by weights.
     *
     * @deprecated
     */
    @Deprecated(since = "2.15.0")
    public boolean isOrderChildren() {
        return false;
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
                }
                menuComponent.setVisible(enable);
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

    /**
     * Use getWeight instead.
     *
     * @see #getWeight()
     * @deprecated
     */
    @Override
    @Deprecated(since = "2.15.0")
    public int getMenuIndex() {
        return -1;
    }

    /**
     * Use getParentWeight instead.
     *
     * @see #getParentWeight()
     * @deprecated
     */
    @Deprecated(since = "2.15.0")
    public int getParentMenuIndex() {
        return -1;
    }

    public int getParentWeight() {
        return MenuWeights.MENU_DEFAULT_WEIGHT;
    }

    public boolean isSubMenu() {
        return false;
    }

    /**
     * Replaced by weights.
     *
     * @deprecated
     */
    @Override
    @Deprecated(since = "2.15.0")
    public boolean precedeWithSeparator() {
        return false;
    }

    /**
     * Replaced by weights.
     *
     * @deprecated
     */
    @Override
    @Deprecated(since = "2.15.0")
    public boolean succeedWithSeparator() {
        return false;
    }

    @Override
    public boolean isSafe() {
        return true;
    }

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
