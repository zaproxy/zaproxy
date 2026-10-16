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
import static org.hamcrest.Matchers.nullValue;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.stream.Stream;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link KeyboardParam}. */
class KeyboardParamUnitTest {

    private static final String IDENTIFIER = "keyboard.test.action";

    /**
     * Covers the full save/read/display round trip that matters for the ç/PT-layout fix: a captured
     * character must survive a save + reload of the configuration, alongside the key stroke it was
     * captured for, across a representative matrix of key codes/chars.
     */
    @ParameterizedTest
    @MethodSource("shortcuts")
    void shouldPersistAndReloadKeyStrokeAndChar(int keyCode, int modifiers, Character keyChar) {
        // Given
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        KeyboardParam savedParam = new KeyboardParam();
        savedParam.load(config);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, modifiers, false);
        savedParam.setShortcut(IDENTIFIER, keyStroke);
        savedParam.setShortcutChar(IDENTIFIER, keyChar);
        // When
        savedParam.setConfigs();
        KeyboardParam reloadedParam = new KeyboardParam();
        reloadedParam.load(config);
        // Then
        assertThat(reloadedParam.getShortcut(IDENTIFIER), is(equalTo(keyStroke)));
        assertThat(reloadedParam.getShortcutChar(IDENTIFIER), is(equalTo(keyChar)));
    }

    private static Stream<Arguments> shortcuts() {
        return Stream.of(
                // Plain letter, no modifiers, no captured char (US-layout guess is already
                // correct, so the field was never populated).
                Arguments.of(KeyEvent.VK_A, 0, null),
                // Modifier combo, no captured char.
                Arguments.of(
                        KeyEvent.VK_1,
                        InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                        null),
                // Punctuation where the captured character happens to match the US-QWERTY guess.
                Arguments.of(KeyEvent.VK_SEMICOLON, 0, ';'),
                // Punctuation where the captured character is layout-specific, e.g. the PT "ç" key
                // reported for the same VK_SEMICOLON position.
                Arguments.of(KeyEvent.VK_SEMICOLON, 0, 'ç'),
                // Named key (e.g. End), which has no character of its own.
                Arguments.of(KeyEvent.VK_END, 0, null));
    }

    @Test
    void shouldNotHaveShortcutCharByDefault() {
        // Given
        KeyboardParam param = new KeyboardParam();
        // When
        param.load(new ZapXmlConfiguration());
        // Then
        assertThat(param.getShortcutChar(IDENTIFIER), is(nullValue()));
    }

    @Test
    void shouldClearShortcutCharWhenSetToNull() {
        // Given
        KeyboardParam param = new KeyboardParam();
        param.load(new ZapXmlConfiguration());
        param.setShortcutChar(IDENTIFIER, 'ç');
        // When
        param.setShortcutChar(IDENTIFIER, null);
        // Then
        assertThat(param.getShortcutChar(IDENTIFIER), is(nullValue()));
    }
}
