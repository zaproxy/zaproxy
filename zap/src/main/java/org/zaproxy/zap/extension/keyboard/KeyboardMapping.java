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
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.ZapAction;
import org.zaproxy.zap.view.ZapMenuItem;

class KeyboardMapping {

    private ZapMenuItem menuItem;
    private ZapAction zapAction;
    private JComponent targetComponent;
    private int inputMapCondition = JComponent.WHEN_FOCUSED;
    private String scope;

    public KeyboardMapping() {}

    public KeyboardMapping(ZapMenuItem menuItem) {
        this.menuItem = menuItem;
    }

    public KeyboardMapping(
            ZapAction zapAction, JComponent targetComponent, int inputMapCondition, String scope) {
        this.zapAction = zapAction;
        this.targetComponent = targetComponent;
        this.inputMapCondition = inputMapCondition;
        this.scope = scope;
    }

    public boolean isAction() {
        return zapAction != null;
    }

    public ZapAction getZapAction() {
        return zapAction;
    }

    public JComponent getTargetComponent() {
        return targetComponent;
    }

    public int getInputMapCondition() {
        return inputMapCondition;
    }

    public String getScope() {
        if (scope != null) {
            return scope;
        }
        if (menuItem != null) {
            return Constant.messages.getString("keyboard.scope.menu");
        }
        return "";
    }

    public String getName() {
        if (menuItem != null) {
            return menuItem.getText();
        }
        if (zapAction != null) {
            return (String) zapAction.getValue(Action.NAME);
        }
        return null;
    }

    public String getIdentifier() {
        if (menuItem != null) {
            return menuItem.getIdentifier();
        }
        if (zapAction != null) {
            return zapAction.getIdentifier();
        }
        return null;
    }

    public KeyStroke getKeyStroke() {
        if (menuItem != null) {
            return menuItem.getAccelerator();
        }
        if (zapAction != null) {
            return zapAction.getAccelerator();
        }
        return null;
    }

    /**
     * Gets the default accelerator of the mapping.
     *
     * @return the default accelerator.
     * @since 2.8.0
     */
    public KeyStroke getDefaultKeyStroke() {
        if (menuItem != null) {
            return menuItem.getDefaultAccelerator();
        }
        if (zapAction != null) {
            return zapAction.getDefaultAccelerator();
        }
        return null;
    }

    public String getKeyStrokeKeyCodeString() {
        KeyStroke keyStroke = getKeyStroke();
        if (keyStroke == null) {
            return "";
        }
        return keyString(keyStroke.getKeyCode());
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
        KeyStroke keyStroke = getKeyStroke();
        if (keyStroke == null) {
            return "";
        }
        return modifiersString(keyStroke.getModifiers());
    }

    public static String modifiersString(int modifiers) {
        StringBuilder sb = new StringBuilder();

        if ((modifiers & InputEvent.META_DOWN_MASK) > 0) {
            sb.append(Constant.messages.getString("keyboard.key.command"));
            sb.append(" ");
        }
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
        if (getKeyStroke() == null) {
            return "";
        }
        return getKeyStrokeModifiersString() + " " + getKeyStrokeKeyCodeString();
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        if (menuItem != null) {
            menuItem.setAccelerator(keyStroke);
        } else if (zapAction != null) {
            zapAction.setAccelerator(keyStroke);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((menuItem == null) ? 0 : menuItem.hashCode());
        result = prime * result + ((zapAction == null) ? 0 : zapAction.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
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
        if (zapAction == null) {
            if (other.zapAction != null) {
                return false;
            }
        } else if (!zapAction.equals(other.zapAction)) {
            return false;
        }
        return true;
    }
}
