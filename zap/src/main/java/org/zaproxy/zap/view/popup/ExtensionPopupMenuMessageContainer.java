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
import org.zaproxy.zap.extension.ExtensionPopupMenu;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;

/**
 * An {@code ExtensionPopupMenu} that, by default, is enable for all the {@code MessageContainer}
 * invokers and not enable for any {@code Component}.
 *
 * @since 2.3.0
 */
public class ExtensionPopupMenuMessageContainer extends ExtensionPopupMenu {

    private static final long serialVersionUID = 1321249475392775487L;

    public ExtensionPopupMenuMessageContainer() {
        super();
    }

    public ExtensionPopupMenuMessageContainer(String label) {
        super(label);
    }

    /** By default, the pop up menu is not enable for any invoker {@code Component}. */
    @Override
    public boolean isEnableForComponent(Component invoker) {
        return false;
    }

    /**
     * By default, the pop up menu button is enabled and the pop up menu is only enable for the
     * given {@code invoker} if at least one of the child menu items is enable for the given {@code
     * invoker}.
     *
     * <p>Although the pop up menu is allowed to contain child menus and menu items of any type of
     * {@code JMenu} or {@code JMenuItem} the only children considered as enablers are the ones of
     * the type of {@code ExtensionPopupMenuComponent}.
     *
     * <p>The {@code ExtensionPopupMenuComponent}s are considered enable if the corresponding method
     * {@code isEnableForMessageContainer(MessageContainer)}, with {@code invoker} as parameter,
     * returns {@code true}.
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
     * <p><strong>Implementation Note:</strong> The method {@code
     * isEnableForMessageContainer(MessageContainer)} is called on all child {@code
     * ExtensionPopupMenuComponent}s, even if a previous child has returned {@code true}, as it
     * allows to notify all the children that the pop up menu in which they are, is being invoked.
     * Subclasses should take it into account when overriding this the method.
     */
    @Override
    public boolean isEnableForMessageContainer(MessageContainer<?> invoker) {
        return processExtensionPopupChildren(PopupMenuUtils.getPopupMenuInvokerWrapper(invoker));
    }
}
