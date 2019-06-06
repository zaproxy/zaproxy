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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.ZapMenuItem;

class KeyboardMapping {

    private ZapMenuItem menuItem;
    private String i18nKey;

    public KeyboardMapping() {}

    public KeyboardMapping(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public KeyboardMapping(ZapMenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public String getName() {
        if (this.menuItem != null) {
            return this.menuItem.getText();
        }
        return null;
    }

    public String getIdentifier() {
        if (this.menuItem != null) {
            return this.menuItem.getIdentifier();
        }
        return this.i18nKey;
    }

    public KeyStroke getKeyStroke() {
        if (this.menuItem != null) {
            return this.menuItem.getAccelerator();
        }
        return null;
    }

    /**
     * Gets the default accelerator of the menu item.
     *
     * @return the default accelerator.
     * @since 2.8.0
     */
    public KeyStroke getDefaultKeyStroke() {
        if (this.menuItem != null) {
            return this.menuItem.getDefaultAccelerator();
        }
        return null;
    }

    public String getKeyStrokeKeyCodeString() {
        if (this.menuItem == null || this.menuItem.getAccelerator() == null) {
            return "";
        }
        return keyString(this.menuItem.getAccelerator().getKeyCode());
    }

    public static String keyString(int keyCode) {
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            // Function key
            return "F" + (keyCode - KeyEvent.VK_F1 + 1);
        } else if (keyCode == KeyEvent.VK_UP) {
            return Constant.messages.getString("keyboard.key.up");
        } else if (keyCode == KeyEvent.VK_DOWN) {
            return Constant.messages.getString("keyboard.key.down");
        } else if (keyCode == KeyEvent.VK_LEFT) {
            return Constant.messages.getString("keyboard.key.left");
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            return Constant.messages.getString("keyboard.key.right");
        } else {
            // A 'normal' key
            return String.valueOf((char) keyCode).toUpperCase();
        }
    }

    public static char keyCode(String keyStr) {
        if (keyStr.length() == 1) {
            return keyStr.charAt(0);
        } else if (keyStr.startsWith("F")) {
            // Function keys
            return (char) (KeyEvent.VK_F1 + Integer.parseInt(keyStr.substring(1)) - 1);
        } else if (keyStr.equals(Constant.messages.getString("keyboard.key.up"))) {
            return KeyEvent.VK_UP;
        } else if (keyStr.equals(Constant.messages.getString("keyboard.key.down"))) {
            return KeyEvent.VK_DOWN;
        } else if (keyStr.equals(Constant.messages.getString("keyboard.key.left"))) {
            return KeyEvent.VK_LEFT;
        } else if (keyStr.equals(Constant.messages.getString("keyboard.key.right"))) {
            return KeyEvent.VK_RIGHT;
        } else {
            return 0;
        }
    }

    public String getKeyStrokeModifiersString() {
        if (this.menuItem == null || this.menuItem.getAccelerator() == null) {
            return "";
        }
        return modifiersString(this.menuItem.getAccelerator().getModifiers());
    }

    public static String modifiersString(int modifiers) {
        StringBuilder sb = new StringBuilder();

        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0) {
            sb.append(Constant.messages.getString("keyboard.key.control"));
            sb.append(" ");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0) {
            sb.append(Constant.messages.getString("keyboard.key.alt"));
            sb.append(" ");
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0) {
            sb.append(Constant.messages.getString("keyboard.key.shift"));
            sb.append(" ");
        }
        return sb.toString();
    }

    public String getKeyStrokeString() {
        if (this.menuItem == null || this.menuItem.getAccelerator() == null) {
            return "";
        }
        return getKeyStrokeModifiersString() + " " + getKeyStrokeKeyCodeString();
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        if (this.menuItem != null) {
            this.menuItem.setAccelerator(keyStroke);
        }
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + ((menuItem == null) ? 0 : menuItem.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KeyboardMapping other = (KeyboardMapping) obj;
        if (menuItem == null) {
            if (other.menuItem != null) {
                return false;
            }
        } else if (!menuItem.equals(other.menuItem)) {
            return false;
        }
        return true;
    }
}
