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

    /**
     * Appends the HTML symbols of the given key stroke directly to the given {@code StringBuilder}.
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

    private static List<String> getSymbolParts(KeyStroke keyStroke) {
        List<String> parts = new ArrayList<>();
        if (keyStroke == null || keyStroke.getKeyCode() == 0) {
            return parts;
        }
        if (isMetaSet(keyStroke.getModifiers())) {
            parts.add(getMetaSymbol());
        }
        if ((keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0) {
            parts.add(getControlSymbol());
        }
        if ((keyStroke.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0) {
            parts.add(getAltSymbol());
        }
        if ((keyStroke.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0) {
            parts.add(getShiftSymbol());
        }
        parts.add(getKeySymbol(keyStroke.getKeyCode()));
        return parts;
    }

    private static boolean isMetaSet(int modifiers) {
        return (modifiers & InputEvent.META_DOWN_MASK) != 0;
    }

    private static String getMetaSymbol() {
        if (Constant.isMacOsX()) {
            return "⌘";
        }
        if (Constant.isWindows()) {
            return Constant.messages.getString("keyboard.key.win");
        }
        return Constant.messages.getString("keyboard.key.super");
    }

    private static String getAltSymbol() {
        return Constant.isMacOsX() ? "⌥" : Constant.messages.getString("keyboard.key.alt");
    }

    private static String getControlSymbol() {
        return "⌃";
    }

    private static String getShiftSymbol() {
        return "⇧";
    }

    private static String getKeySymbol(int keyCode) {
        int functionKeyNumber = getFunctionKeyNumber(keyCode);
        if (functionKeyNumber > 0) {
            return "F" + functionKeyNumber;
        }
        return switch (keyCode) {
            case KeyEvent.VK_UP -> "↑";
            case KeyEvent.VK_DOWN -> "↓";
            case KeyEvent.VK_LEFT -> "←";
            case KeyEvent.VK_RIGHT -> "→";
                // Any other code is one of ZAP's own symbol/letter/digit keys (see
                // DialogEditShortcut#getKeyList), not a real AWT virtual-key constant, so it must
                // be treated as a plain character rather than passed to KeyEvent.getKeyText, which
                // would misinterpret coincidental matches.
            default -> String.valueOf((char) keyCode);
        };
    }

    private static int getFunctionKeyNumber(int keyCode) {
        // F13-F24 are not contiguous with F1-F12
        if (keyCode >= KeyEvent.VK_F13) {
            return keyCode - KeyEvent.VK_F13 + 13;
        }
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            return keyCode - KeyEvent.VK_F1 + 1;
        }
        return 0;
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
