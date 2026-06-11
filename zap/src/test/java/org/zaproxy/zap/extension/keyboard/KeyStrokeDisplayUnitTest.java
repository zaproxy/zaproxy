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
import java.util.stream.Stream;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link KeyStrokeDisplay}. */
class KeyStrokeDisplayUnitTest {

    @BeforeEach
    void setUp() {
        Constant.messages = new I18N(Locale.ENGLISH);
    }

    @ParameterizedTest
    @CsvSource({"true, false, ⌘", "false, true, Win", "false, false, Super"})
    void shouldFormatMetaSymbolForEachPlatform(
            boolean isMacOsX, boolean isWindows, String expectedSymbol) {
        KeyStroke metaF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK);

        String symbols;
        try (MockedStatic<Constant> constant = mockStatic(Constant.class)) {
            constant.when(Constant::isMacOsX).thenReturn(isMacOsX);
            constant.when(Constant::isWindows).thenReturn(isWindows);

            symbols = KeyStrokeDisplay.formatHtmlSymbols(metaF);
        }

        assertThat(symbols, containsString("<kbd>" + expectedSymbol + "</kbd>"));
        assertThat(symbols, containsString("<kbd>F</kbd>"));
    }

    @Test
    void shouldEscapeHtmlCharactersInSymbols() {
        // The formatter must escape HTML characters when they appear in key symbols.
        // Note: '&' can't be used here, as its ASCII value coincidentally equals KeyEvent.VK_UP.
        KeyStroke lessThan = KeyStroke.getKeyStroke((int) '<', 0, false);
        String lessThanHtml = KeyStrokeDisplay.formatHtmlSymbols(lessThan);

        assertThat(lessThanHtml, containsString("<kbd>&lt;</kbd>"));
        assertThat(lessThanHtml, not(containsString("<kbd><</kbd>")));

        KeyStroke greaterThan = KeyStroke.getKeyStroke((int) '>', 0, false);
        String greaterThanHtml = KeyStrokeDisplay.formatHtmlSymbols(greaterThan);

        assertThat(greaterThanHtml, containsString("<kbd>&gt;</kbd>"));
        assertThat(greaterThanHtml, not(containsString("<kbd>></kbd>")));

        KeyStroke quote = KeyStroke.getKeyStroke((int) '"', 0, false);
        String quoteHtml = KeyStrokeDisplay.formatHtmlSymbols(quote);

        assertThat(quoteHtml, containsString("<kbd>&quot;</kbd>"));
        assertThat(quoteHtml, not(containsString("<kbd>\"</kbd>")));
    }

    @ParameterizedTest
    @MethodSource("characterColliders")
    void shouldFormatCharacterCollidersAsCharactersNotVkCodes(
            int charCode, String expectedHtml, String vkCollision) {
        // ZAP's key encoding allows arbitrary char codes (not just official VK_ constants).
        // Some ASCII characters coincidentally match VK_ constant values (e.g. '#'=35=VK_END,
        // '&'=38=VK_UP). These must be formatted as themselves, not passed to
        // KeyEvent.getKeyText, which would misinterpret them as the VK_ key instead.
        KeyStroke keyStroke = KeyStroke.getKeyStroke(charCode, 0, false);
        String html = KeyStrokeDisplay.formatHtmlSymbols(keyStroke);

        assertThat(html, containsString(expectedHtml));
    }

    private static Stream<Arguments> characterColliders() {
        return Stream.of(
                Arguments.of(35, "<kbd>#</kbd>", "KeyEvent.VK_END"),
                Arguments.of(38, "<kbd>&amp;</kbd>", "KeyEvent.VK_UP"));
    }

    @Test
    void shouldFormatExtendedFunctionKeys() {
        KeyStroke f24 = KeyStroke.getKeyStroke(KeyEvent.VK_F24, 0);

        assertThat(KeyStrokeDisplay.formatHtmlSymbols(f24), containsString("<kbd>F24</kbd>"));
    }

    @Test
    void shouldCompareFullKeyStrokes() {
        KeyStroke ctrlK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK);
        KeyStroke altK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK);

        assertThat(KeyStrokeDisplay.compare(ctrlK, altK), is(equalTo(-1)));
        assertThat(KeyStrokeDisplay.compare(altK, ctrlK), is(equalTo(1)));
    }
}
