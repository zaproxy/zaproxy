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

    private KeyStrokeDisplay() {}

    public static boolean isDefaultShowSymbols() {
        return Constant.isMacOsX();
    }

    public static String formatPlain(KeyStroke keyStroke, boolean symbols) {
        if (keyStroke == null || keyStroke.getKeyCode() == 0) {
            return "";
        }
        return symbols ? formatSymbols(keyStroke) : formatNames(keyStroke);
    }

    public static String formatNames(KeyStroke keyStroke) {
        return String.join(" ", getNameParts(keyStroke));
    }

    public static String formatSymbols(KeyStroke keyStroke) {
        return String.join("", getSymbolParts(keyStroke));
    }

    public static String formatHtmlNames(KeyStroke keyStroke) {
        StringBuilder sb = new StringBuilder();
        wrapPartsHtml(sb, getNameParts(keyStroke), true);
        return sb.toString();
    }

    public static String formatHtmlSymbols(KeyStroke keyStroke) {
        StringBuilder sb = new StringBuilder();
        appendHtmlSymbols(sb, keyStroke);
        return sb.toString();
    }

    /**
     * Appends the HTML symbols of the given key stroke directly to the given {@code StringBuilder},
     * avoiding the intermediate string allocated by {@link #formatHtmlSymbols(KeyStroke)}.
     *
     * @param sb the builder to append to.
     * @param keyStroke the key stroke to format, might be {@code null}.
     */
    public static void appendHtmlSymbols(StringBuilder sb, KeyStroke keyStroke) {
        wrapPartsHtml(sb, getSymbolParts(keyStroke), false);
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
        int result = Integer.compare(ks1.getKeyCode(), ks2.getKeyCode());
        if (result != 0) {
            return result;
        }
        return Integer.compare(ks1.getModifiers(), ks2.getModifiers());
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
            parts.add(getControlKeyName());
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            parts.add(getAltKeyName());
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            parts.add(getShiftKeyName());
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
            parts.add(getControlSymbol());
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            parts.add(getAltSymbol());
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            parts.add(getShiftSymbol());
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

    private static String getControlKeyName() {
        return Constant.messages.getString("keyboard.key.control");
    }

    private static String getControlSymbol() {
        return "⌃";
    }

    private static String getShiftKeyName() {
        return Constant.messages.getString("keyboard.key.shift");
    }

    private static String getShiftSymbol() {
        return "⇧";
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
                // Any other code is one of ZAP's own symbol/letter/digit keys (see
                // DialogEditShortcut#getKeyList), not a real AWT virtual-key constant, so it must
                // be
                // treated as a plain character rather than passed to KeyEvent.getKeyText, which
                // would
                // misinterpret coincidental matches (e.g. '#' == KeyEvent.VK_END).
            default -> String.valueOf((char) keyCode);
        };
    }

    private static boolean isFunctionKey(int keyCode) {
        return (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12)
                || (keyCode >= KeyEvent.VK_F13 && keyCode <= KeyEvent.VK_F24);
    }

    private static String formatFunctionKey(int keyCode) {
        // F13-F24 are not contiguous with F1-F12
        if (keyCode >= KeyEvent.VK_F13) {
            return "F" + (keyCode - KeyEvent.VK_F13 + 13);
        }
        return "F" + (keyCode - KeyEvent.VK_F1 + 1);
    }

    private static void wrapPartsHtml(StringBuilder sb, List<String> parts, boolean fullEscape) {
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            sb.append("<kbd>")
                    .append(
                            fullEscape
                                    ? StringEscapeUtils.escapeHtml4(part)
                                    : escapeHtmlMinimal(part))
                    .append("</kbd>");
        }
    }

    private static String escapeHtmlMinimal(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
