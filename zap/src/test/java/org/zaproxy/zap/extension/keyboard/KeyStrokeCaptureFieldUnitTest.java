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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link KeyStrokeCaptureField}. */
class KeyStrokeCaptureFieldUnitTest {

    private KeyStrokeCaptureField field;

    @BeforeEach
    void setUp() {
        Constant.messages = new I18N(Locale.ENGLISH);
        field = new KeyStrokeCaptureField(null);
    }

    @Test
    void shouldAllowFocusTraversalKeys() {
        // Given / When / Then
        // Tab/Shift+Tab must move the focus around the dialogue, not be captured.
        assertThat(field.getFocusTraversalKeysEnabled(), is(equalTo(true)));
    }

    @Test
    void shouldNotHaveKeyStrokeByDefault() {
        // Given / When
        KeyStrokeCaptureField freshField = new KeyStrokeCaptureField(null);
        // Then
        assertThat(freshField.getKeyStroke(), is(nullValue()));
        assertThat(freshField.getText(), is(equalTo("")));
    }

    @Test
    void shouldShowKeyStrokeAsSymbols() {
        // Given
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK);
        // When
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(keyStroke);
        // Then
        assertThat(fieldWithKeyStroke.getKeyStroke(), is(equalTo(keyStroke)));
        assertThat(fieldWithKeyStroke.getText(), is(equalTo("⌃ K")));
    }

    @Test
    void shouldShowCapturedKeyStrokeAsSymbols() {
        // Given
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(null);
        // When
        fieldWithKeyStroke.processKeyEvent(
                keyPressed(fieldWithKeyStroke, KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK));
        // Then
        assertThat(fieldWithKeyStroke.getText(), is(equalTo("⌃ K")));
    }

    @Test
    void shouldCaptureKeyPressed() {
        // Given
        KeyEvent event = keyPressed(field, KeyEvent.VK_K, 0);
        // When
        field.processKeyEvent(event);
        // Then
        assertThat(
                field.getKeyStroke(), is(equalTo(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0, false))));
        assertThat(field.getText(), is(equalTo("K")));
        assertThat(event.isConsumed(), is(equalTo(true)));
    }

    @Test
    void shouldCaptureModifiersOfKeyPressed() {
        // Given
        int modifiers = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
        KeyEvent event = keyPressed(field, KeyEvent.VK_F, modifiers);
        // When
        field.processKeyEvent(event);
        // Then
        assertThat(
                field.getKeyStroke(),
                is(equalTo(KeyStroke.getKeyStroke(KeyEvent.VK_F, modifiers, false))));
        assertThat(event.isConsumed(), is(equalTo(true)));
    }

    @Test
    void shouldDistinguishEndKeyFromHashCharacterKey() {
        // The character code of '#' and VK_END have the same value (35), so key strokes can't be
        // stored/read as characters. The toolkit reports '#' as Shift+3 and the End key as VK_END,
        // which are captured as distinct key strokes, shown as themselves.
        // Given
        KeyStrokeCaptureField fieldWithEnd = new KeyStrokeCaptureField(null);
        // When
        fieldWithEnd.processKeyEvent(keyPressed(fieldWithEnd, KeyEvent.VK_END, 0));
        KeyStroke endKeyStroke = fieldWithEnd.getKeyStroke();
        KeyStrokeCaptureField fieldWithHash = new KeyStrokeCaptureField(null);
        fieldWithHash.processKeyEvent(
                keyPressed(fieldWithHash, KeyEvent.VK_3, InputEvent.SHIFT_DOWN_MASK));
        // Then
        assertThat(endKeyStroke, is(equalTo(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0, false))));
        assertThat(fieldWithEnd.getText(), is(equalTo("End")));
        assertThat(
                fieldWithHash.getKeyStroke(),
                is(
                        equalTo(
                                KeyStroke.getKeyStroke(
                                        KeyEvent.VK_3, InputEvent.SHIFT_DOWN_MASK, false))));
        assertThat(fieldWithHash.getText(), is(equalTo("⇧ 3")));
        assertThat(fieldWithHash.getKeyStroke(), is(not(equalTo(endKeyStroke))));
    }

    @ParameterizedTest
    @ValueSource(
            ints = {
                KeyEvent.VK_F13,
                KeyEvent.VK_F14,
                KeyEvent.VK_F15,
                KeyEvent.VK_F16,
                KeyEvent.VK_F17,
                KeyEvent.VK_F18,
                KeyEvent.VK_F19,
                KeyEvent.VK_F20,
                KeyEvent.VK_F21,
                KeyEvent.VK_F22,
                KeyEvent.VK_F23,
                KeyEvent.VK_F24
            })
    void shouldCaptureFunctionKeys(int keyCode) {
        // When
        field.processKeyEvent(keyPressed(field, keyCode, 0));
        // Then
        assertThat(field.getKeyStroke(), is(equalTo(KeyStroke.getKeyStroke(keyCode, 0, false))));
    }

    @ParameterizedTest
    @ValueSource(
            ints = {
                KeyEvent.VK_SHIFT,
                KeyEvent.VK_CONTROL,
                KeyEvent.VK_ALT,
                KeyEvent.VK_ALT_GRAPH,
                KeyEvent.VK_META,
                KeyEvent.VK_WINDOWS,
                KeyEvent.VK_CONTEXT_MENU
            })
    void shouldNotCaptureModifierKeysAlone(int keyCode) {
        // Given
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_K, 0, false);
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(keyStroke);
        KeyEvent event = keyPressed(fieldWithKeyStroke, keyCode, 0);
        // When
        fieldWithKeyStroke.processKeyEvent(event);
        // Then
        assertThat(fieldWithKeyStroke.getKeyStroke(), is(equalTo(keyStroke)));
        assertThat(event.isConsumed(), is(equalTo(true)));
    }

    @Test
    void shouldNotCaptureKeyPressWithoutKeyCode() {
        // Given
        KeyEvent event = keyPressed(field, KeyEvent.VK_UNDEFINED, 0);
        // When
        field.processKeyEvent(event);
        // Then
        assertThat(field.getKeyStroke(), is(nullValue()));
        assertThat(field.getText(), is(equalTo("")));
        assertThat(event.isConsumed(), is(equalTo(true)));
    }

    @Test
    void shouldNotCaptureKeysWithNoDisplay() {
        // Given
        KeyEvent event = keyPressed(field, KeyEvent.VK_STOP, 0);
        // When
        field.processKeyEvent(event);
        // Then
        assertThat(field.getKeyStroke(), is(nullValue()));
        assertThat(field.getText(), is(equalTo("")));
        assertThat(event.isConsumed(), is(equalTo(true)));
    }

    @ParameterizedTest
    @ValueSource(ints = {KeyEvent.VK_QUOTE, KeyEvent.VK_BACK_QUOTE, KeyEvent.VK_COMMA})
    void shouldCapturePunctuationKeys(int keyCode) {
        // When
        field.processKeyEvent(keyPressed(field, keyCode, 0));
        // Then
        assertThat(field.getKeyStroke(), is(equalTo(KeyStroke.getKeyStroke(keyCode, 0, false))));
    }

    @Test
    void shouldKeepCapturedKeyStrokeWhenKeyWithoutCodePressed() {
        // Given / When
        field.processKeyEvent(keyPressed(field, KeyEvent.VK_F1, 0));
        KeyStroke f1KeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false);
        // Then
        assertThat(field.getKeyStroke(), is(equalTo(f1KeyStroke)));

        // The fn/Globe key on macOS reports no key code (e.g. once the user lets go of it).
        // When
        field.processKeyEvent(keyPressed(field, KeyEvent.VK_UNDEFINED, 0));
        // Then
        assertThat(field.getKeyStroke(), is(equalTo(f1KeyStroke)));
        assertThat(field.getText(), is(equalTo("F1")));
    }

    @ParameterizedTest
    @ValueSource(ints = {KeyEvent.VK_BACK_SPACE, KeyEvent.VK_DELETE})
    void shouldClearKeyStroke(int keyCode) {
        // Given
        KeyStrokeCaptureField fieldWithKeyStroke =
                new KeyStrokeCaptureField(
                        KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK, false));
        KeyEvent event = keyPressed(fieldWithKeyStroke, keyCode, 0);
        // When
        fieldWithKeyStroke.processKeyEvent(event);
        // Then
        assertThat(fieldWithKeyStroke.getKeyStroke(), is(nullValue()));
        assertThat(fieldWithKeyStroke.getText(), is(equalTo("")));
        assertThat(event.isConsumed(), is(equalTo(true)));
    }

    @Test
    void shouldNotCaptureEscapeKey() {
        // Given
        KeyStroke keyStroke =
                KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK, false);
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(keyStroke);
        KeyEvent event = keyPressed(fieldWithKeyStroke, KeyEvent.VK_ESCAPE, 0);
        // When
        fieldWithKeyStroke.processKeyEvent(event);
        // Then
        assertThat(fieldWithKeyStroke.getKeyStroke(), is(equalTo(keyStroke)));
        // Not consumed, so that the dialogue can handle Escape (to close itself).
        assertThat(event.isConsumed(), is(equalTo(false)));
    }

    @ParameterizedTest
    @ValueSource(ints = {KeyEvent.KEY_RELEASED, KeyEvent.KEY_TYPED})
    void shouldIgnoreNonPressedEvents(int eventId) {
        // Given
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_K, 0, false);
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(keyStroke);
        // Key typed events must have an undefined key code, other events a valid one.
        boolean keyTyped = eventId == KeyEvent.KEY_TYPED;
        KeyEvent event =
                new KeyEvent(
                        fieldWithKeyStroke,
                        eventId,
                        System.currentTimeMillis(),
                        0,
                        keyTyped ? KeyEvent.VK_UNDEFINED : KeyEvent.VK_L,
                        keyTyped ? 'l' : KeyEvent.CHAR_UNDEFINED);
        // When
        fieldWithKeyStroke.processKeyEvent(event);
        // Then
        assertThat(fieldWithKeyStroke.getKeyStroke(), is(equalTo(keyStroke)));
        assertThat(event.isConsumed(), is(equalTo(true)));
    }

    @Test
    void shouldNotifyActionListenersWhenKeyStrokeChanges() {
        // Given
        List<KeyStroke> capturedKeyStrokes = new ArrayList<>();
        field.addActionListener(e -> capturedKeyStrokes.add(field.getKeyStroke()));
        // When
        field.processKeyEvent(keyPressed(field, KeyEvent.VK_K, 0));
        field.processKeyEvent(keyPressed(field, KeyEvent.VK_BACK_SPACE, 0));
        // Then
        assertThat(capturedKeyStrokes.size(), is(equalTo(2)));
        assertThat(
                capturedKeyStrokes.get(0),
                is(equalTo(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0, false))));
        assertThat(capturedKeyStrokes.get(1), is(nullValue()));
    }

    @Test
    void shouldUseKeyTypedCharWhenAvailable() {
        // On non-US layouts a punctuation key code (e.g. VK_SEMICOLON) can produce a different
        // character than the US-QWERTY guess, e.g. "ç" on a Portuguese layout.
        // Given
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(null);
        // When
        fieldWithKeyStroke.processKeyEvent(
                keyPressed(fieldWithKeyStroke, KeyEvent.VK_SEMICOLON, 0));
        fieldWithKeyStroke.processKeyEvent(keyTyped(fieldWithKeyStroke, 'ç'));
        // Then
        assertThat(
                fieldWithKeyStroke.getKeyStroke(),
                is(equalTo(KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, 0, false))));
        assertThat(fieldWithKeyStroke.getText(), is(equalTo("ç")));
    }

    @Test
    void shouldKeepKeyCodeFallbackWhenKeyTypedCharIsAControlCharacter() {
        // A control character reaching KEY_TYPED (e.g. from a dead key) is not a usable label, so
        // the fallback computed from the key code alone (";" for VK_SEMICOLON) is kept as-is,
        // rather than being replaced by anything derived from the typed character.
        // Given
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(null);
        // When
        fieldWithKeyStroke.processKeyEvent(
                keyPressed(fieldWithKeyStroke, KeyEvent.VK_SEMICOLON, 0));
        fieldWithKeyStroke.processKeyEvent(keyTyped(fieldWithKeyStroke, '\n'));
        // Then
        assertThat(fieldWithKeyStroke.getText(), is(equalTo(";")));
    }

    @Test
    void shouldFallBackWhenNoKeyTypedFollows() {
        // Given
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(null);
        // When
        fieldWithKeyStroke.processKeyEvent(
                keyPressed(fieldWithKeyStroke, KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK));
        // Then
        assertThat(fieldWithKeyStroke.getText(), is(equalTo("⌃ ;")));
    }

    @Test
    void shouldNotConfuseNewPressWithPriorUnconfirmedOne() {
        // A modifier combo (e.g. Ctrl+;) typically generates no KEY_TYPED at all. A later,
        // unrelated key press must not be refined by a KEY_TYPED left over from that one.
        // Given
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(null);
        // When
        fieldWithKeyStroke.processKeyEvent(
                keyPressed(fieldWithKeyStroke, KeyEvent.VK_SEMICOLON, InputEvent.CTRL_DOWN_MASK));
        fieldWithKeyStroke.processKeyEvent(keyPressed(fieldWithKeyStroke, KeyEvent.VK_A, 0));
        fieldWithKeyStroke.processKeyEvent(keyTyped(fieldWithKeyStroke, 'a'));
        // Then
        assertThat(
                fieldWithKeyStroke.getKeyStroke(),
                is(equalTo(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false))));
        assertThat(fieldWithKeyStroke.getText(), is(equalTo("a")));
    }

    @Test
    void shouldNotifyActionListenersWhenDisplayRefinedByKeyTyped() {
        // Given
        KeyStrokeCaptureField fieldWithKeyStroke = new KeyStrokeCaptureField(null);
        List<String> texts = new ArrayList<>();
        fieldWithKeyStroke.addActionListener(e -> texts.add(fieldWithKeyStroke.getText()));
        // When
        fieldWithKeyStroke.processKeyEvent(
                keyPressed(fieldWithKeyStroke, KeyEvent.VK_SEMICOLON, 0));
        fieldWithKeyStroke.processKeyEvent(keyTyped(fieldWithKeyStroke, 'ç'));
        // Then
        assertThat(texts, is(equalTo(List.of(";", "ç"))));
    }

    private static KeyEvent keyPressed(Component source, int keyCode, int modifiers) {
        return new KeyEvent(
                source,
                KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(),
                modifiers,
                keyCode,
                KeyEvent.CHAR_UNDEFINED);
    }

    private static KeyEvent keyTyped(Component source, char keyChar) {
        return new KeyEvent(
                source,
                KeyEvent.KEY_TYPED,
                System.currentTimeMillis(),
                0,
                KeyEvent.VK_UNDEFINED,
                keyChar);
    }
}
