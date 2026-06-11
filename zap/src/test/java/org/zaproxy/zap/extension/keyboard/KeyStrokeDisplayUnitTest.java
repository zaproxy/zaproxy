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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mockStatic;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link KeyStrokeDisplay}. */
class KeyStrokeDisplayUnitTest {

    private static final KeyStroke CTRL_ALT_J =
            KeyStroke.getKeyStroke(
                    KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);

    @BeforeEach
    void setUp() {
        Constant.messages = new I18N(Locale.ENGLISH);
    }

    @Test
    void shouldReturnEmptyHtmlNamesForNullKeyStroke() {
        assertThat(KeyStrokeDisplay.formatHtmlNames(null), is(equalTo("")));
    }

    @Test
    void shouldFormatHtmlNamesWithKbdTags() {
        String html = KeyStrokeDisplay.formatHtmlNames(CTRL_ALT_J);

        assertThat(html, containsString("<kbd>"));
        assertThat(html, containsString("Control"));
        assertThat(html, containsString(altKeyName()));
        assertThat(html, containsString("J"));
        assertThat(html, not(containsString("⌃")));
    }

    @ParameterizedTest
    @CsvSource({"true, false, Command, ⌘", "false, true, Win, Win", "false, false, Super, Super"})
    void shouldFormatMetaKeyForEachPlatform(
            boolean isMacOsX, boolean isWindows, String expectedName, String expectedSymbol) {
        KeyStroke metaF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK);

        String names;
        String symbols;
        try (MockedStatic<Constant> constant = mockStatic(Constant.class)) {
            constant.when(Constant::isMacOsX).thenReturn(isMacOsX);
            constant.when(Constant::isWindows).thenReturn(isWindows);

            names = KeyStrokeDisplay.formatHtmlNames(metaF);
            symbols = KeyStrokeDisplay.formatHtmlSymbols(metaF);
        }

        assertThat(names, containsString("<kbd>" + expectedName + "</kbd>"));
        assertThat(names, containsString("<kbd>F</kbd>"));
        assertThat(symbols, containsString("<kbd>" + expectedSymbol + "</kbd>"));
        assertThat(symbols, containsString("<kbd>F</kbd>"));
    }

    @Test
    void shouldEscapeHtmlInSymbolParts() {
        KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK);
        String html = KeyStrokeDisplay.formatHtmlSymbols(up);

        assertThat(html, containsString("<kbd>⌃</kbd>"));
        assertThat(html, containsString("<kbd>↑</kbd>"));

        // '<' isn't reachable via DialogEditShortcut's key list, but the formatter must still
        // escape it correctly for any KeyStroke it's given. (Note: '&' can't be used here, as its
        // ASCII value coincidentally equals KeyEvent.VK_UP, the same kind of collision as '#'
        // does with VK_END.)
        KeyStroke lessThan = KeyStroke.getKeyStroke((int) '<', 0, false);
        String lessThanHtml = KeyStrokeDisplay.formatHtmlSymbols(lessThan);

        assertThat(lessThanHtml, containsString("<kbd>&lt;</kbd>"));
        assertThat(lessThanHtml, not(containsString("<kbd><</kbd>")));
    }

    @Test
    void shouldFormatNumberSignAsItselfNotEndKey() {
        // '#' shares the same int value as KeyEvent.VK_END in ZAP's own key encoding
        // (see KeyboardMapping#keyCode), so it must not be passed to KeyEvent.getKeyText.
        KeyStroke numberSign = KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false);
        String html = KeyStrokeDisplay.formatHtmlSymbols(numberSign);

        assertThat(html, containsString("<kbd>#</kbd>"));
    }

    @Test
    void shouldFormatExtendedFunctionKeys() {
        KeyStroke f13 = KeyStroke.getKeyStroke(KeyEvent.VK_F13, 0);
        KeyStroke f24 = KeyStroke.getKeyStroke(KeyEvent.VK_F24, 0);

        assertThat(KeyStrokeDisplay.formatHtmlNames(f13), containsString("<kbd>F13</kbd>"));
        assertThat(KeyStrokeDisplay.formatHtmlSymbols(f24), containsString("<kbd>F24</kbd>"));
    }

    @Test
    void shouldNotSeparateHtmlSymbolsWithPlus() {
        KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK);
        String html = KeyStrokeDisplay.formatHtmlSymbols(up);

        assertThat(html, not(containsString("+")));
    }

    @Test
    void shouldNotSeparateHtmlNamesWithPlus() {
        String html = KeyStrokeDisplay.formatHtmlNames(CTRL_ALT_J);

        assertThat(html, not(containsString("+")));
    }

    @Test
    void shouldCompareFullKeyStrokes() {
        KeyStroke ctrlK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK);
        KeyStroke altK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK);

        assertThat(KeyStrokeDisplay.compare(ctrlK, altK), is(equalTo(-1)));
        assertThat(KeyStrokeDisplay.compare(altK, ctrlK), is(equalTo(1)));
    }

    private static String altKeyName() {
        return Constant.isMacOsX()
                ? Constant.messages.getString("keyboard.key.option")
                : Constant.messages.getString("keyboard.key.alt");
    }
}
