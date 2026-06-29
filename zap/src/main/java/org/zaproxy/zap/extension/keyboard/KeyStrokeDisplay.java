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
import java.util.function.Consumer;
import javax.swing.KeyStroke;
import org.apache.commons.text.StringEscapeUtils;
import org.parosproxy.paros.Constant;

/** Formats {@link KeyStroke}s for display in the UI and HTML cheatsheets. */
public final class KeyStrokeDisplay {

    private static final String KBD_SEPARATOR = "+";

    private enum Modifier {
        META(InputEvent.META_DOWN_MASK, "⌘", "&#x2318;"),
        CTRL(InputEvent.CTRL_DOWN_MASK, "⌃", "&#x2303;"),
        ALT(InputEvent.ALT_DOWN_MASK, "⌥", "&#x2325;"),
        SHIFT(InputEvent.SHIFT_DOWN_MASK, "⇧", "&#x21E7;");

        private final int mask;
        private final String symbol;
        private final String htmlEntity;

        Modifier(int mask, String symbol, String htmlEntity) {
            this.mask = mask;
            this.symbol = symbol;
            this.htmlEntity = htmlEntity;
        }

        boolean isSet(int modifiers) {
            return (modifiers & mask) != 0;
        }
    }

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
        return String.join(KBD_SEPARATOR, getNameParts(keyStroke));
    }

    public static String formatSymbols(KeyStroke keyStroke) {
        if (keyStroke == null || keyStroke.getKeyCode() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        forEachModifier(getExtendedModifiers(keyStroke), modifier -> sb.append(modifier.symbol));
        sb.append(getKeySymbol(keyStroke.getKeyCode()));
        return sb.toString();
    }

    public static String formatHtmlNames(KeyStroke keyStroke) {
        return wrapPartsHtml(getNameParts(keyStroke));
    }

    public static String formatHtmlSymbols(KeyStroke keyStroke) {
        return wrapSymbolPartsHtml(getHtmlSymbolParts(keyStroke));
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
        int result = Integer.compare(getExtendedModifiers(ks1), getExtendedModifiers(ks2));
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
        int modifiers = getExtendedModifiers(keyStroke);
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            parts.add(Constant.messages.getString("keyboard.key.command"));
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

    private static List<String> getHtmlSymbolParts(KeyStroke keyStroke) {
        List<String> parts = new ArrayList<>();
        if (keyStroke == null || keyStroke.getKeyCode() == 0) {
            return parts;
        }
        forEachModifier(
                getExtendedModifiers(keyStroke), modifier -> parts.add(modifier.htmlEntity));
        parts.add(getHtmlKeySymbol(keyStroke.getKeyCode()));
        return parts;
    }

    private static int getExtendedModifiers(KeyStroke keyStroke) {
        // AWTKeyStroke has no getModifiersEx(); getModifiers() uses extended _DOWN_MASK values.
        return keyStroke.getModifiers();
    }

    private static void forEachModifier(int modifiers, Consumer<Modifier> consumer) {
        for (Modifier modifier : Modifier.values()) {
            if (modifier.isSet(modifiers)) {
                consumer.accept(modifier);
            }
        }
    }

    private static String getAltKeyName() {
        if (Constant.isMacOsX()) {
            return Constant.messages.getString("keyboard.key.option");
        }
        return Constant.messages.getString("keyboard.key.alt");
    }

    private static String getKeyText(int keyCode) {
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            return "F" + (keyCode - KeyEvent.VK_F1 + 1);
        }
        return KeyEvent.getKeyText(keyCode);
    }

    private static String getKeySymbol(int keyCode) {
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            return "F" + (keyCode - KeyEvent.VK_F1 + 1);
        } else if (keyCode == KeyEvent.VK_UP) {
            return "↑";
        } else if (keyCode == KeyEvent.VK_DOWN) {
            return "↓";
        } else if (keyCode == KeyEvent.VK_LEFT) {
            return "←";
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            return "→";
        } else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
            return String.valueOf((char) keyCode);
        } else if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return String.valueOf((char) keyCode);
        }
        return KeyEvent.getKeyText(keyCode);
    }

    private static String getHtmlKeySymbol(int keyCode) {
        if (keyCode >= KeyEvent.VK_F1 && keyCode <= KeyEvent.VK_F12) {
            return "F" + (keyCode - KeyEvent.VK_F1 + 1);
        } else if (keyCode == KeyEvent.VK_UP) {
            return "&#x2191;";
        } else if (keyCode == KeyEvent.VK_DOWN) {
            return "&#x2193;";
        } else if (keyCode == KeyEvent.VK_LEFT) {
            return "&#x2190;";
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            return "&#x2192;";
        } else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9) {
            return String.valueOf((char) keyCode);
        } else if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
            return String.valueOf((char) keyCode);
        }
        return StringEscapeUtils.escapeHtml4(KeyEvent.getKeyText(keyCode));
    }

    private static String wrapSymbolPartsHtml(List<String> parts) {
        if (parts.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(KBD_SEPARATOR);
            }
            sb.append("<kbd>").append(parts.get(i)).append("</kbd>");
        }
        return sb.toString();
    }

    private static String wrapPartsHtml(List<String> parts) {
        if (parts.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (i > 0) {
                sb.append(KBD_SEPARATOR);
            }
            sb.append("<kbd>").append(StringEscapeUtils.escapeHtml4(parts.get(i))).append("</kbd>");
        }
        return sb.toString();
    }
}
