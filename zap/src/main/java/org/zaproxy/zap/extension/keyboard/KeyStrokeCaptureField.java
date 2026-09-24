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

import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

/**
 * A read-only text field that captures the key stroke of the key pressed while it has focus.
 *
 * <p>Both the key code and the modifiers are taken from the {@link KeyEvent} reported by the
 * toolkit, so the key stroke itself is captured as-is. Its displayed label, however, is refined
 * using the character reported by the following {@code KEY_TYPED} event when available, since it
 * reflects the user's actual keyboard layout, unlike the static, US-QWERTY based guess otherwise
 * used for the key's key code (see {@link KeyStrokeDisplay}).
 *
 * <p>Key presses that report no key code (e.g. the fn/Globe key on macOS) and modifier keys on
 * their own are ignored, waiting for the actual key to be pressed.
 *
 * <p>Focus traversal keys (e.g. Tab) are left enabled, so they move the focus around the dialogue
 * instead of being captured as key strokes.
 */
class KeyStrokeCaptureField extends JTextField {

    private static final long serialVersionUID = 1L;

    private KeyStroke keyStroke;
    private boolean awaitingTypedChar;

    /**
     * Constructs a {@code KeyStrokeCaptureField} showing the given key stroke.
     *
     * @param keyStroke the key stroke to show, might be {@code null} (meaning no key is set).
     */
    KeyStrokeCaptureField(KeyStroke keyStroke) {
        super(KeyStrokeDisplay.formatPlain(keyStroke));
        this.keyStroke = keyStroke;
        setEditable(false);
        setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Gets the captured key stroke.
     *
     * @return the key stroke, or {@code null} if no key is set.
     */
    KeyStroke getKeyStroke() {
        return keyStroke;
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_TYPED) {
            handleKeyTyped(e);
            return;
        }

        if (e.getID() != KeyEvent.KEY_PRESSED) {
            // Never let released events reach the text field.
            e.consume();
            return;
        }

        // Any new key press invalidates a pending refinement from a previous one.
        awaitingTypedChar = false;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                // Let the dialogue's Escape key binding close it.
                super.processKeyEvent(e);
                return;
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
                // Clear the key stroke, that is, no key set.
                applyKeyStroke(null);
                e.consume();
                return;
            default:
                break;
        }

        if (e.getKeyCode() == KeyEvent.VK_UNDEFINED) {
            // A key press without a key code, e.g. the fn/Globe key on macOS: it's not a key
            // stroke on its own, wait for the actual key pressed while it's held.
            e.consume();
            return;
        }

        if (isModifierKey(e.getKeyCode()) || KeyStrokeDisplay.isUnnamedKey(e.getKeyCode())) {
            // A modifier alone is not a key stroke, wait for the actual key. A key with no
            // meaningful display (e.g. media keys) is not captured either.
            e.consume();
            return;
        }

        // Also consumed, so that e.g. mnemonics don't act on the key stroke being captured.
        applyKeyStroke(KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiersEx(), false));
        awaitingTypedChar = true;
        e.consume();
    }

    private void handleKeyTyped(KeyEvent e) {
        e.consume();
        if (!awaitingTypedChar) {
            // Nothing pending, e.g. no KEY_TYPED is generated for most Ctrl/Alt combinations, or
            // the key just pressed was Escape/Backspace/Delete/ignored.
            return;
        }
        // One-shot: only the first KEY_TYPED after a press can refine the label, so a dead key's
        // second, composed KEY_TYPED (e.g. after typing the accent then the letter) is ignored.
        awaitingTypedChar = false;

        char c = e.getKeyChar();
        if (c == KeyEvent.CHAR_UNDEFINED || Character.isISOControl(c)) {
            // No usable character (e.g. a dead key on its own), keep the existing label.
            return;
        }

        // Reflects the character actually produced by the user's keyboard layout, which can
        // differ from the label KeyStrokeDisplay would otherwise guess from the key code alone
        // (a layout-independent, US-QWERTY based identifier for punctuation keys).
        setText(KeyStrokeDisplay.formatPlain(keyStroke, String.valueOf(c)));
        fireActionPerformed();
    }

    private void applyKeyStroke(KeyStroke keyStroke) {
        this.keyStroke = keyStroke;
        setText(KeyStrokeDisplay.formatPlain(keyStroke));
        fireActionPerformed();
    }

    private static boolean isModifierKey(int keyCode) {
        return switch (keyCode) {
            case KeyEvent.VK_SHIFT,
                    KeyEvent.VK_CONTROL,
                    KeyEvent.VK_ALT,
                    KeyEvent.VK_ALT_GRAPH,
                    KeyEvent.VK_META,
                    KeyEvent.VK_WINDOWS,
                    KeyEvent.VK_CONTEXT_MENU -> true;
            default -> false;
        };
    }
}
