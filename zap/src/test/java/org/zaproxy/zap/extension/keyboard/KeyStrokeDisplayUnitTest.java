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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
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

    @BeforeEach
    void setUp() {
        Constant.messages = new I18N(Locale.ENGLISH);
    }

    private static String htmlSymbols(KeyStroke keyStroke) {
        StringBuilder sb = new StringBuilder();
        KeyStrokeDisplay.appendHtmlSymbols(sb, keyStroke);
        return sb.toString();
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

            symbols = htmlSymbols(metaF);
        }

        assertThat(symbols, is(equalTo("<kbd>" + expectedSymbol + "</kbd><kbd>F</kbd>")));
    }

    @ParameterizedTest
    @CsvSource({"'<', &lt;", "'>', &gt;", "'\"', &quot;"})
    void shouldEscapeHtmlCharactersInSymbols(char keyChar, String expectedEscaped) {
        // The formatter must escape HTML characters when they appear in key symbols.
        // Note: '&' can't be used here, as its ASCII value coincidentally equals KeyEvent.VK_UP.
        KeyStroke keyStroke = KeyStroke.getKeyStroke((int) keyChar, 0, false);

        assertThat(htmlSymbols(keyStroke), is(equalTo("<kbd>" + expectedEscaped + "</kbd>")));
    }

    @Test
    void shouldFormatExtendedFunctionKeys() {
        KeyStroke f24 = KeyStroke.getKeyStroke(KeyEvent.VK_F24, 0);

        assertThat(htmlSymbols(f24), is(equalTo("<kbd>F24</kbd>")));
    }

    @Test
    void shouldCompareFullKeyStrokes() {
        KeyStroke ctrlK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK);
        KeyStroke altK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK);

        assertThat(KeyStrokeDisplay.compare(ctrlK, altK), is(equalTo(-1)));
        assertThat(KeyStrokeDisplay.compare(altK, ctrlK), is(equalTo(1)));
    }
}
