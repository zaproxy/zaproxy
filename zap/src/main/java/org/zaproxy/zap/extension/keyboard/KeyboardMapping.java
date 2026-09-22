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
package org.zaproxy.zap.extension.keyboard;

import javax.swing.KeyStroke;
import org.zaproxy.zap.view.ZapMenuItem;

class KeyboardMapping {

    private ZapMenuItem menuItem;

    public KeyboardMapping(ZapMenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public String getIdentifier() {
        return this.menuItem.getIdentifier();
    }

    public KeyStroke getKeyStroke() {
        return this.menuItem.getAccelerator();
    }

    /**
     * Gets the default accelerator of the menu item.
     *
     * @return the default accelerator.
     * @since 2.8.0
     */
    public KeyStroke getDefaultKeyStroke() {
        return this.menuItem.getDefaultAccelerator();
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        this.menuItem.setAccelerator(keyStroke);
    }
}
