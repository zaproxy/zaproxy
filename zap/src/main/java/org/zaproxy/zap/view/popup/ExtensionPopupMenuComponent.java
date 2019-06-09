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
import org.zaproxy.zap.view.messagecontainer.MessageContainer;

/**
 * A component that can be shown/handled in pop up menus (for example, {@code MainPopupMenu}) with
 * enhanced behaviour (compared to {@code JMenu}s and {@code JMenuItem}s).
 *
 * <p>The menu component will be queried:
 *
 * <ul>
 *   <li>to know if it is enable or not for the {@code Component} where the pop up menu is being
 *       invoked, by calling {@link #isEnableForComponent(Component)};
 *   <li>to know if it is enable or not for the {@code MessageContainer} where the pop up menu is
 *       being invoked, by calling {@link #isEnableForMessageContainer(MessageContainer)};
 *   <li>to know if its action is safe or not so it can be automatically disabled when it's not safe
 *       and the {@code Control.Mode} doesn't allow potential dangerous operations, by calling
 *       {@link #isSafe()};
 *   <li>its position in the pop up menu, by calling {@link #getMenuIndex()}.
 * </ul>
 *
 * The menu component can require the presence of menu separators before and/or after the menu
 * component itself with the methods {@link #precedeWithSeparator()} and {@link
 * #succeedWithSeparator()}, respectively.
 *
 * <p>The menu component will be notified when no longer shown (i.e. the pop up menu was dismissed)
 * by calling {@link #dismissed(ExtensionPopupMenuComponent)}, so it can free any resources (e.g.
 * clean any references to UI components).
 *
 * @since 2.3.0
 * @see org.zaproxy.zap.extension.ExtensionPopupMenu
 * @see org.parosproxy.paros.extension.ExtensionPopupMenuItem
 * @see org.parosproxy.paros.view.MainPopupMenu
 * @see org.parosproxy.paros.control.Control.Mode
 * @see MessageContainer
 */
public interface ExtensionPopupMenuComponent {

    /**
     * Tells whether or not the pop up menu component is enable for the given {@code invoker},
     * therefore shown (or not) in the pop up menu.
     *
     * <p>It is responsibility of the pop up menu component to enable/disable itself in case it
     * should be shown but shouldn't be active/enabled.
     *
     * <p><strong>Note:</strong> Calling this method implies that the pop up menu in which this pop
     * up menu component is, is being invoked.
     *
     * @param invoker the component where the pop up menu was invoked
     * @return {@code true} if the menu component is enable for the given {@code invoker}, {@code
     *     false} otherwise.
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    boolean isEnableForComponent(Component invoker);

    /**
     * Tells whether or not the pop up menu component is enable for the given {@code invoker},
     * therefore shown (or not) in the pop up menu.
     *
     * <p>It is responsibility of the pop up menu component to enable/disable itself in case it
     * should be shown but shouldn't be active/enabled.
     *
     * <p><strong>Note:</strong> Calling this method implies that the pop up menu in which this pop
     * up menu component is, is being invoked.
     *
     * @param invoker the message container where the pop up menu was invoked
     * @return {@code true} if the menu component is enable for the given {@code invoker}, {@code
     *     false} otherwise.
     * @see #isEnableForComponent(Component)
     */
    boolean isEnableForMessageContainer(MessageContainer<?> invoker);

    /**
     * Returns the position that the pop up menu component should occupy in the pop up menu.
     *
     * @return the position that the menu component should occupy in the pop up menu.
     */
    int getMenuIndex();

    /**
     * Tells whether or not the pop up menu component should be preceded with a separator.
     *
     * @return {@code true} if the menu component should be preceded with a separator, {@code false}
     *     otherwise.
     * @see javax.swing.JPopupMenu.Separator
     */
    boolean precedeWithSeparator();

    /**
     * Tells whether or not the pop up menu component should be succeeded with a separator.
     *
     * @return {@code true} if the menu component should be succeeded with a separator, {@code
     *     false} otherwise.
     * @see javax.swing.JPopupMenu.Separator
     */
    boolean succeedWithSeparator();

    /**
     * Tells whether or not the pop up menu component is safe, that is, doesn't do any potentially
     * dangerous operations (for example, active scan a target server).
     *
     * <p>The pop up menu component will be automatically disabled when it is not safe and the
     * {@code Control.Mode} set doesn't allow potentially dangerous operations.
     *
     * @return {@code true} if the menu component is safe, {@code false} otherwise.
     * @see org.parosproxy.paros.control.Control.Mode
     */
    boolean isSafe();

    /**
     * Called after the pop up menu in which this pop up menu component is is dismissed, indicating
     * the menu component that was selected or {@code null} if none.
     *
     * <p>Can be used to free any resources no longer needed (e.g. references to UI components)
     * after being shown in the pop up menu.
     *
     * <p><strong>Note 1:</strong> Any resource needed to execute the action should only be freed in
     * this method if the menu was not selected. If it was selected the resources should only be
     * freed after executing the action.
     *
     * <p><strong>Note 2:</strong> This method will not be called if this pop up menu component is
     * not enabled for the {@code invoker}.
     *
     * @param selectedMenuComponent the selected menu component or {@code null} if none
     * @since 2.4.0
     * @see #isEnableForComponent(Component)
     * @see #isEnableForMessageContainer(MessageContainer)
     */
    void dismissed(ExtensionPopupMenuComponent selectedMenuComponent);
}
