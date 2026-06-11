/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.KeyStroke;
import org.apache.commons.text.StringEscapeUtils;
import org.parosproxy.paros.Constant;

/** Formats {@link KeyStroke}s for display in the UI and HTML cheatsheets. */
public final class KeyStrokeDisplay {

    private static final String KBD_SEPARATOR = "+";

    private KeyStrokeDisplay() {}

    public static String formatHtmlNames(KeyStroke keyStroke) {
        return wrapPartsHtml(getNameParts(keyStroke), true);
    }

    public static String formatHtmlSymbols(KeyStroke keyStroke) {
        return wrapPartsHtml(getSymbolParts(keyStroke), false);
    }

    public static int compare(KeyStroke ks1, KeyStroke ks2) {
        if (ks1 == null && ks2 == null) {
            return 0;
        }
        if (ks1 == null) {
            return 1;
        }
        if (ks2 == null) {
            return -1;
        }
        int result = Integer.compare(ks1.getModifiers(), ks2.getModifiers());
        if (result != 0) {
            return result;
        }
        return Integer.compare(ks1.getKeyCode(), ks2.getKeyCode());
    }

    private static List<String> getNameParts(KeyStroke keyStroke) {
        List<String> parts = new ArrayList<>();
        if (keyStroke == null || keyStroke.getKeyCode() == 0) {
            return parts;
        }
        int modifiers = keyStroke.getModifiers();
        if (isMetaSet(modifiers)) {
            parts.add(getMetaKeyName());
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            parts.add(Constant.messages.getString("keyboard.key.control"));
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            parts.add(getAltKeyName());
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            parts.add(Constant.messages.getString("keyboard.key.shift"));
        }
        parts.add(getKeyText(keyStroke.getKeyCode()));
        return parts;
    }

    private static List<String> getSymbolParts(KeyStroke keyStroke) {
        List<String> parts = new ArrayList<>();
        if (keyStroke == null || keyStroke.getKeyCode() == 0) {
            return parts;
        }
        int modifiers = keyStroke.getModifiers();
        if (isMetaSet(modifiers)) {
            parts.add(getMetaSymbol());
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            parts.add("⌃");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            parts.add(getAltSymbol());
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            parts.add("⇧");
        }
        parts.add(getKeySymbol(keyStroke.getKeyCode()));
        return parts;
    }

    private static boolean isMetaSet(int modifiers) {
        return (modifiers & InputEvent.META_DOWN_MASK) != 0;
    }

    private static String getMetaKeyName() {
        if (Constant.isMacOsX()) {
            return Constant.messages.getString("keyboard.key.command");
        }
        if (Constant.isWindows()) {
            return Constant.messages.getString("keyboard.key.win");
        }
        return Constant.messages.getString("keyboard.key.super");
    }

    private static String getMetaSymbol() {
        return Constant.isMacOsX() ? "⌘" : getMetaKeyName();
    }

    private static String getAltKeyName() {
        if (Constant.isMacOsX()) {
            return Constant.messages.getString("keyboard.key.option");
        }
        return Constant.messages.getString("keyboard.key.alt");
    }

    private static String getAltSymbol() {
        return Constant.isMacOsX() ? "⌥" : getAltKeyName();
    }

    private static String getKeyText(int keyCode) {
        if (isFunctionKey(keyCode)) {
            return formatFunctionKey(keyCode);
        }
        return KeyEvent.getKeyText(keyCode);
    }

    private static String getKeySymbol(int keyCode) {
        if (isFunctionKey(keyCode)) {
            return formatFunctionKey(keyCode);
        }
        return switch (keyCode) {
            case KeyEvent.VK_UP -> "↑";
            case KeyEvent.VK_DOWN -> "↓";
            case KeyEvent.VK_LEFT -> "←";
            case KeyEvent.VK_RIGHT -> "→";
            default -> keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9
                    ? String.valueOf((char) keyCode)
                    : keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z
                            ? String.valueOf((char) keyCode)
                            : KeyEvent.getKeyText(keyCode);
        };
    }

    private static boolean isFunctionKey(int keyCode) {
        return keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12;
    }

    private static String formatFunctionKey(int keyCode) {
        return "F" + (keyCode - KeyEvent.VK_F1 + 1);
    }

    private static String wrapPartsHtml(List<String> parts, boolean fullEscape) {
        if (parts.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(KBD_SEPARATOR);
            }
            String part = parts.get(i);
            sb.append("<kbd>")
                    .append(
                            fullEscape
                                    ? StringEscapeUtils.escapeHtml4(part)
                                    : escapeHtmlMinimal(part))
                    .append("</kbd>");
        }
        return sb.toString();
    }

    private static String escapeHtmlMinimal(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
