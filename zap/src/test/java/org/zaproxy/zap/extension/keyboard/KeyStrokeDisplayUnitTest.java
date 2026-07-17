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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    void shouldFormatSymbolShortcut() {
        assertThat(
                KeyStrokeDisplay.formatSymbols(CTRL_ALT_J), is(equalTo("⌃" + altSymbol() + "J")));
    }

    @Test
    void shouldFormatNamesWithI18nModifierNames() {
        assertThat(
                KeyStrokeDisplay.formatNames(CTRL_ALT_J),
                is(equalTo("Control+" + altKeyName() + "+J")));
        assertThat(KeyStrokeDisplay.formatNames(CTRL_ALT_J), not(containsString("⌃")));
    }

    @Test
    void shouldFormatMetaKeyNamesForPlatform() {
        KeyStroke metaF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK);

        assertThat(KeyStrokeDisplay.formatNames(metaF), containsString("F"));
        assertThat(KeyStrokeDisplay.formatNames(metaF), not(containsString("⌘")));
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

    @Test
    void shouldFormatHtmlMetaKeyNamesForPlatform() {
        KeyStroke metaF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK);
        String html = KeyStrokeDisplay.formatHtmlNames(metaF);

        assertThat(html, containsString("<kbd>F</kbd>"));
        assertThat(html, not(containsString("⌘")));
    }

    @Test
    void shouldFormatHtmlSymbolsWithUnicodeInKbdTags() {
        KeyStroke metaF = KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.META_DOWN_MASK);

        String html = KeyStrokeDisplay.formatHtmlSymbols(metaF);

        if (Constant.isMacOsX()) {
            assertThat(html, containsString("<kbd>⌘</kbd>"));
        } else {
            assertThat(html, containsString("<kbd>"));
            assertThat(html, not(containsString("⌘")));
        }
        assertThat(html, containsString("<kbd>F</kbd>"));
    }

    @Test
    void shouldEscapeHtmlInSymbolParts() {
        KeyStroke up = KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK);
        String html = KeyStrokeDisplay.formatHtmlSymbols(up);

        assertThat(html, containsString("<kbd>⌃</kbd>"));
        assertThat(html, containsString("<kbd>↑</kbd>"));
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

    private static String altSymbol() {
        return Constant.isMacOsX() ? "⌥" : altKeyName();
    }
}
