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
import javax.swing.Action;
import javax.swing.Icon;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.zaproxy.zap.view.messagecontainer.MessageContainer;

/**
 * An {@code ExtensionPopupMenuItem} that, by default, is enable for all the {@code
 * MessageContainer} invokers and not enable for any {@code Component}.
 *
 * @since 2.3.0
 */
public class ExtensionPopupMenuItemMessageContainer extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 5123729066062943072L;

    /** Constructs an {@code ExtensionPopupMenuItemMessageContainer} with no text nor icon. */
    public ExtensionPopupMenuItemMessageContainer() {
        super();
    }

    /**
     * Constructs an {@code ExtensionPopupMenuItemMessageContainer} with the given text and no icon.
     *
     * @param text the text of the menu item.
     */
    public ExtensionPopupMenuItemMessageContainer(String text) {
        super(text);
    }

    /**
     * Constructs an {@code ExtensionPopupMenuItemMessageContainer} with the given text and icon.
     *
     * @param text the text of the menu item.
     * @param icon the icon of the menu item.
     * @since 2.7.0
     */
    public ExtensionPopupMenuItemMessageContainer(String text, Icon icon) {
        super(text, icon);
    }

    /**
     * Constructs an {@code ExtensionPopupMenuItemMessageContainer} with the given action.
     *
     * <p>The text and icon (if any) are obtained from the given action.
     *
     * @param action the action of the menu item.
     * @since 2.7.0
     */
    public ExtensionPopupMenuItemMessageContainer(Action action) {
        super(action);
    }

    /** By default, the pop up menu item is not enable for any invoker {@code Component}. */
    @Override
    public boolean isEnableForComponent(Component invoker) {
        return false;
    }

    /** By default, the pop up menu item button is enabled and it is enable for all invokers. */
    @Override
    public boolean isEnableForMessageContainer(MessageContainer<?> invoker) {
        return true;
    }
}
