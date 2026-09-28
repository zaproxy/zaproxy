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
        wrapPartsHtml(sb, getSymbolParts(keyStroke));
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

    public static String formatPlain(KeyStroke keyStroke) {
        // Space delimited, to match the format used elsewhere for shortcuts (e.g. in menus).
        return String.join(" ", getSymbolParts(keyStroke));
    }

    /**
     * Formats the given key stroke like {@link #formatPlain(KeyStroke)}, but using the given label
     * for the key itself instead of the one {@link #getKeySymbol(int)} would compute.
     *
     * <p>Used only for live key stroke capture, where the actual character produced by the key
     * (from a {@code KEY_TYPED} event) is known and is more accurate than the static, US-QWERTY
     * based guess in {@link #getKeyName(int)} — e.g. on non-US layouts with keys that have no US
     * equivalent.
     *
     * @param keyStroke the key stroke being captured, never {@code null}.
     * @param keyLabel the label to use for the key, instead of computing one from its key code.
     * @return the formatted key stroke.
     */
    static String formatPlain(KeyStroke keyStroke, String keyLabel) {
        List<String> parts = getModifierSymbolParts(keyStroke.getModifiers());
        parts.add(keyLabel);
        return String.join(" ", parts);
    }

    /**
     * Gets the name of the given key code, as shown in the UI.
     *
     * <p>This is a best-effort, US-QWERTY based guess used only for already-saved shortcuts (e.g.
     * the options table, the HTML cheatsheet), where there's no live {@code KeyEvent} to consult
     * for the character actually produced by the user's keyboard layout. Live capture instead uses
     * the character reported by the {@code KEY_TYPED} event, see {@code KeyStrokeCaptureField}.
     *
     * @param keyCode the key code.
     * @return the name of the key.
     */
    private static String getKeyName(int keyCode) {
        String name = getNamedKey(keyCode);
        if (name != null) {
            return name;
        }
        // Not a key with a name of its own, so it's the character code of a character key of a
        // shortcut configured with an older version of ZAP. Note some punctuation key codes
        // (e.g. VK_BACK_QUOTE 192) don't match their character in Latin-1, map them explicitly.
        return switch (keyCode) {
            case KeyEvent.VK_BACK_QUOTE -> "`";
            case KeyEvent.VK_QUOTE -> "'";
            case KeyEvent.VK_MINUS -> "-";
            case KeyEvent.VK_EQUALS -> "=";
            case KeyEvent.VK_OPEN_BRACKET -> "[";
            case KeyEvent.VK_CLOSE_BRACKET -> "]";
            case KeyEvent.VK_BACK_SLASH -> "\\";
            case KeyEvent.VK_SEMICOLON -> ";";
            case KeyEvent.VK_COMMA -> ",";
            case KeyEvent.VK_PERIOD -> ".";
            case KeyEvent.VK_SLASH -> "/";
            default -> String.valueOf((char) keyCode);
        };
    }

    /**
     * Gets the symbol of the given key code, as shown in the UI.
     *
     * @param keyCode the key code.
     * @return the symbol of the key.
     */
    private static String getKeySymbol(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_UP -> "↑";
            case KeyEvent.VK_DOWN -> "↓";
            case KeyEvent.VK_LEFT -> "←";
            case KeyEvent.VK_RIGHT -> "→";
            default -> getKeyName(keyCode);
        };
    }

    /**
     * Gets the name of the given key code, if it's a key named in the UI, that is, a virtual-key
     * constant which has no character of its own (e.g. End, Page Up, function keys).
     *
     * <p>Character keys, e.g. letters, digits, and punctuation, are not named, they are shown as
     * the character itself.
     *
     * @param keyCode the key code.
     * @return the name of the key, or {@code null} if the key code has no name.
     */
    private static String getNamedKey(int keyCode) {
        int functionKeyNumber = getFunctionKeyNumber(keyCode);
        if (functionKeyNumber > 0) {
            return "F" + functionKeyNumber;
        }
        if (keyCode >= KeyEvent.VK_NUMPAD0 && keyCode <= KeyEvent.VK_NUMPAD9) {
            return String.valueOf(keyCode - KeyEvent.VK_NUMPAD0);
        }
        return switch (keyCode) {
            case KeyEvent.VK_ADD -> "+";
            case KeyEvent.VK_SUBTRACT -> "-";
            case KeyEvent.VK_MULTIPLY -> "*";
            case KeyEvent.VK_DIVIDE -> "/";
            case KeyEvent.VK_SEPARATOR -> Constant.messages.getString("keyboard.key.enter");
            case KeyEvent.VK_DECIMAL -> ".";
            case KeyEvent.VK_BACK_SPACE -> Constant.messages.getString("keyboard.key.backspace");
            case KeyEvent.VK_TAB -> Constant.messages.getString("keyboard.key.tab");
            case KeyEvent.VK_ENTER -> Constant.messages.getString("keyboard.key.enter");
            case KeyEvent.VK_ESCAPE -> Constant.messages.getString("keyboard.key.escape");
            case KeyEvent.VK_SPACE -> Constant.messages.getString("keyboard.key.space");
            case KeyEvent.VK_PAGE_UP -> Constant.messages.getString("keyboard.key.pageUp");
            case KeyEvent.VK_PAGE_DOWN -> Constant.messages.getString("keyboard.key.pageDown");
            case KeyEvent.VK_END -> Constant.messages.getString("keyboard.key.end");
            case KeyEvent.VK_HOME -> Constant.messages.getString("keyboard.key.home");
            case KeyEvent.VK_INSERT -> Constant.messages.getString("keyboard.key.insert");
            case KeyEvent.VK_DELETE -> Constant.messages.getString("keyboard.key.delete");
            case KeyEvent.VK_PRINTSCREEN -> Constant.messages.getString("keyboard.key.printScreen");
            case KeyEvent.VK_SCROLL_LOCK -> Constant.messages.getString("keyboard.key.scrollLock");
            case KeyEvent.VK_PAUSE -> Constant.messages.getString("keyboard.key.pause");
            case KeyEvent.VK_CAPS_LOCK -> Constant.messages.getString("keyboard.key.capsLock");
            case KeyEvent.VK_NUM_LOCK -> Constant.messages.getString("keyboard.key.numLock");
            case KeyEvent.VK_UP -> Constant.messages.getString("keyboard.key.up");
            case KeyEvent.VK_DOWN -> Constant.messages.getString("keyboard.key.down");
            case KeyEvent.VK_LEFT -> Constant.messages.getString("keyboard.key.left");
            case KeyEvent.VK_RIGHT -> Constant.messages.getString("keyboard.key.right");
            default -> null;
        };
    }

    /**
     * Tells whether the given key code has no meaningful display, that is, it's neither a key named
     * in the UI, nor a character key (whose character is shown), e.g. media, browser, or IME keys.
     *
     * <p>Such key codes are not captured as key strokes.
     *
     * @param keyCode the key code.
     * @return {@code true} if the key code has no meaningful display.
     */
    static boolean isUnnamedKey(int keyCode) {
        return getNamedKey(keyCode) == null && !isCharacterKey(keyCode);
    }

    /**
     * Tells whether the given key code is that of a character key, which {@link #getKeyName(int)}
     * displays as its character.
     *
     * <p>Note the key codes of the shifted symbols (e.g. {@code @} or {@code %}) either collide
     * with the codes of the named keys, checked first, or are not reported for physical keys.
     *
     * @param keyCode the key code.
     * @return {@code true} if the key code is that of a character key.
     */
    private static boolean isCharacterKey(int keyCode) {
        if ((keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z)
                || (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9)) {
            return true;
        }
        return switch (keyCode) {
            case KeyEvent.VK_BACK_QUOTE,
                    KeyEvent.VK_MINUS,
                    KeyEvent.VK_EQUALS,
                    KeyEvent.VK_OPEN_BRACKET,
                    KeyEvent.VK_CLOSE_BRACKET,
                    KeyEvent.VK_BACK_SLASH,
                    KeyEvent.VK_SEMICOLON,
                    KeyEvent.VK_QUOTE,
                    KeyEvent.VK_COMMA,
                    KeyEvent.VK_PERIOD,
                    KeyEvent.VK_SLASH -> true;
            default -> false;
        };
    }

    private static List<String> getSymbolParts(KeyStroke keyStroke) {
        if (keyStroke == null || keyStroke.getKeyCode() == 0) {
            return new ArrayList<>();
        }
        List<String> parts = getModifierSymbolParts(keyStroke.getModifiers());
        parts.add(getKeySymbol(keyStroke.getKeyCode()));
        return parts;
    }

    private static List<String> getModifierSymbolParts(int modifiers) {
        List<String> parts = new ArrayList<>();
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

    private static int getFunctionKeyNumber(int keyCode) {
        // F13-F24 are not contiguous with F1-F12
        if (keyCode >= KeyEvent.VK_F13 && keyCode <= KeyEvent.VK_F24) {
            return keyCode - KeyEvent.VK_F13 + 13;
        }
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            return keyCode - KeyEvent.VK_F1 + 1;
        }
        return 0;
    }

    private static void wrapPartsHtml(StringBuilder sb, List<String> parts) {
        for (String part : parts) {
            sb.append("<kbd>").append(StringEscapeUtils.escapeHtml4(part)).append("</kbd>");
        }
    }
}
