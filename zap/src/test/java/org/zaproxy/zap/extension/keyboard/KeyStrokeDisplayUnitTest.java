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

    private static String htmlSymbols(KeyStroke keyStroke) {
        StringBuilder sb = new StringBuilder();
        KeyStrokeDisplay.appendHtmlSymbols(sb, keyStroke);
        return sb.toString();
    }

    @ParameterizedTest
    @CsvSource({"true, false, ⌘", "false, true, Win", "false, false, Super"})
    void shouldFormatMetaSymbolForEachPlatform(
            boolean isMacOsX, boolean isWindows, String expectedSymbol) {
        // Given
        KeyStroke metaF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK);
        // When
        String symbols;
        try (MockedStatic<Constant> constant = mockStatic(Constant.class)) {
            constant.when(Constant::isMacOsX).thenReturn(isMacOsX);
            constant.when(Constant::isWindows).thenReturn(isWindows);

            symbols = htmlSymbols(metaF);
        }
        // Then
        assertThat(symbols, is(equalTo("<kbd>" + expectedSymbol + "</kbd><kbd>F</kbd>")));
    }

    @ParameterizedTest
    @CsvSource({"'<', &lt;", "'>', &gt;"})
    void shouldEscapeHtmlCharactersInSymbols(char keyChar, String expectedEscaped) {
        // The formatter must escape HTML characters when they appear in key symbols.
        // Note: '&' and '"' can't be used here, as their ASCII values coincidentally equal
        // KeyEvent.VK_UP and KeyEvent.VK_PAGE_DOWN, so they are shown as those keys instead.
        KeyStroke keyStroke = KeyStroke.getKeyStroke((int) keyChar, 0, false);

        assertThat(htmlSymbols(keyStroke), is(equalTo("<kbd>" + expectedEscaped + "</kbd>")));
    }

    @ParameterizedTest
    @MethodSource("namedKeys")
    void shouldFormatNamedKeysAsTheirName(int keyCode, String expectedName) {
        // Keys which have no character or symbol of their own, e.g. End or Page Up, are shown by
        // name.
        // Given
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0, false);
        // When / Then
        assertThat(KeyStrokeDisplay.formatPlain(keyStroke), is(equalTo(expectedName)));
        assertThat(htmlSymbols(keyStroke), is(equalTo("<kbd>" + expectedName + "</kbd>")));
    }

    private static Stream<Arguments> namedKeys() {
        return Stream.of(
                Arguments.of(KeyEvent.VK_BACK_SPACE, "Backspace"),
                Arguments.of(KeyEvent.VK_TAB, "Tab"),
                Arguments.of(KeyEvent.VK_ENTER, "Enter"),
                Arguments.of(KeyEvent.VK_ESCAPE, "Escape"),
                Arguments.of(KeyEvent.VK_SPACE, "Space"),
                Arguments.of(KeyEvent.VK_PAGE_UP, "Page Up"),
                Arguments.of(KeyEvent.VK_PAGE_DOWN, "Page Down"),
                Arguments.of(KeyEvent.VK_END, "End"),
                Arguments.of(KeyEvent.VK_HOME, "Home"),
                Arguments.of(KeyEvent.VK_INSERT, "Insert"),
                Arguments.of(KeyEvent.VK_DELETE, "Delete"),
                Arguments.of(KeyEvent.VK_PRINTSCREEN, "Print Screen"),
                Arguments.of(KeyEvent.VK_SCROLL_LOCK, "Scroll Lock"),
                Arguments.of(KeyEvent.VK_PAUSE, "Pause"),
                Arguments.of(KeyEvent.VK_CAPS_LOCK, "Caps Lock"),
                Arguments.of(KeyEvent.VK_NUM_LOCK, "Num Lock"));
    }

    @ParameterizedTest
    @CsvSource({"37, ←", "38, ↑", "39, →", "40, ↓"})
    void shouldFormatArrowKeysAsSymbols(int keyCode, String expectedSymbol) {
        // Given
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0, false);
        // When / Then
        assertThat(KeyStrokeDisplay.formatPlain(keyStroke), is(equalTo(expectedSymbol)));
    }

    @Test
    void shouldFormatNumPadKeys() {
        // NumPad keys have no character of their own, they are shown as their label.
        // Given / When / Then
        assertThat(
                KeyStrokeDisplay.formatPlain(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0, false)),
                is(equalTo("7")));
        assertThat(
                KeyStrokeDisplay.formatPlain(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0, false)),
                is(equalTo("+")));
        assertThat(
                KeyStrokeDisplay.formatPlain(
                        KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0, false)),
                is(equalTo("-")));
        assertThat(
                KeyStrokeDisplay.formatPlain(
                        KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY, 0, false)),
                is(equalTo("*")));
        assertThat(
                KeyStrokeDisplay.formatPlain(KeyStroke.getKeyStroke(KeyEvent.VK_DIVIDE, 0, false)),
                is(equalTo("/")));
        assertThat(
                KeyStrokeDisplay.formatPlain(KeyStroke.getKeyStroke(KeyEvent.VK_DECIMAL, 0, false)),
                is(equalTo(".")));
        assertThat(
                KeyStrokeDisplay.formatPlain(
                        KeyStroke.getKeyStroke(KeyEvent.VK_SEPARATOR, 0, false)),
                is(equalTo("Enter")));
    }

    @Test
    void shouldTellUnnamedKeys() {
        // Given / When / Then
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_STOP), is(equalTo(true)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_CANCEL), is(equalTo(true)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_A), is(equalTo(false)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_END), is(equalTo(false)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_F13), is(equalTo(false)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_QUOTE), is(equalTo(false)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_BACK_QUOTE), is(equalTo(false)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_COMMA), is(equalTo(false)));
        assertThat(KeyStrokeDisplay.isUnnamedKey(KeyEvent.VK_MINUS), is(equalTo(false)));
    }

    @ParameterizedTest
    @MethodSource("punctuationKeys")
    void shouldFormatPunctuationKeysAsTheirCharacter(int keyCode, String expectedCharacter) {
        // Given
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, 0, false);
        // When / Then
        // Note some punctuation key codes (e.g. VK_BACK_QUOTE 192) don't match their character in
        // Latin-1, so they are mapped explicitly, not cast.
        assertThat(KeyStrokeDisplay.formatPlain(keyStroke), is(equalTo(expectedCharacter)));
    }

    private static Stream<Arguments> punctuationKeys() {
        return Stream.of(
                Arguments.of(KeyEvent.VK_BACK_QUOTE, "`"),
                Arguments.of(KeyEvent.VK_QUOTE, "'"),
                Arguments.of(KeyEvent.VK_MINUS, "-"),
                Arguments.of(KeyEvent.VK_EQUALS, "="),
                Arguments.of(KeyEvent.VK_COMMA, ","),
                Arguments.of(KeyEvent.VK_PERIOD, "."),
                Arguments.of(KeyEvent.VK_SLASH, "/"),
                Arguments.of(KeyEvent.VK_SEMICOLON, ";"),
                Arguments.of(KeyEvent.VK_OPEN_BRACKET, "["),
                Arguments.of(KeyEvent.VK_BACK_SLASH, "\\"),
                Arguments.of(KeyEvent.VK_CLOSE_BRACKET, "]"));
    }

    @Test
    void shouldFormatExtendedFunctionKeys() {
        // Given
        KeyStroke f24 = KeyStroke.getKeyStroke(KeyEvent.VK_F24, 0);
        // When / Then
        assertThat(htmlSymbols(f24), is(equalTo("<kbd>F24</kbd>")));
    }

    @Test
    void shouldNotFormatKeyCodesAboveF24AsFunctionKeys() {
        // F24 is the last function key, codes above it are not function keys.
        // Given
        int keyCode = KeyEvent.VK_F24 + 1;
        // When / Then
        assertThat(
                KeyStrokeDisplay.formatPlain(KeyStroke.getKeyStroke(keyCode, 0, false)),
                is(equalTo(String.valueOf((char) keyCode))));
    }

    @Test
    void shouldFormatPlainKeyStrokeSpaceDelimited() {
        // Given
        KeyStroke ctrlShiftF =
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        // When / Then
        assertThat(KeyStrokeDisplay.formatPlain(ctrlShiftF), is(equalTo("⌃ ⇧ F")));
    }

    @Test
    void shouldFormatPlainWithGivenKeyLabel() {
        // Given
        KeyStroke ctrlShiftF =
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        // When / Then
        assertThat(KeyStrokeDisplay.formatPlain(ctrlShiftF, "ç"), is(equalTo("⌃ ⇧ ç")));
    }

    @Test
    void shouldFormatPlainWithGivenKeyLabelAndNoModifiers() {
        // Given
        KeyStroke plainF = KeyStroke.getKeyStroke(KeyEvent.VK_F, 0);
        // When / Then
        assertThat(KeyStrokeDisplay.formatPlain(plainF, "ç"), is(equalTo("ç")));
    }

    @Test
    void shouldCompareFullKeyStrokes() {
        // Given
        KeyStroke ctrlK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK);
        KeyStroke altK = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_DOWN_MASK);
        // When / Then
        assertThat(KeyStrokeDisplay.compare(ctrlK, altK), is(equalTo(-1)));
        assertThat(KeyStrokeDisplay.compare(altK, ctrlK), is(equalTo(1)));
    }
}
