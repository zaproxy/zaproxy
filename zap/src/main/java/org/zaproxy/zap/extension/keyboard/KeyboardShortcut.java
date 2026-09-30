/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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

import javax.swing.KeyStroke;

public class KeyboardShortcut {

    private String name;
    private String identifier;
    private KeyStroke keyStroke;
    private Character keyChar;
    private boolean changed = false;

    public KeyboardShortcut() {}

    public KeyboardShortcut(String identifier, String name, KeyStroke keyStroke) {
        this.identifier = identifier;
        this.name = name;
        this.keyStroke = keyStroke;
    }

    public String getName() {
        return this.name;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public KeyStroke getKeyStroke() {
        return keyStroke;
    }

    public void setKeyStroke(KeyStroke keyStroke) {
        this.keyStroke = keyStroke;
        this.keyChar = null;
        this.changed = true;
    }

    /**
     * Gets the character actually produced by the key stroke's key, on the keyboard layout it was
     * captured on, if known.
     *
     * <p>Used only as a display hint: the key stroke's key code and modifiers, not this character,
     * are what's matched when the shortcut is triggered. Not known (i.e. {@code null}) for
     * shortcuts that were never re-captured through the edit dialogue, e.g. defaults.
     *
     * @return the character, or {@code null} if not known.
     */
    public Character getKeyChar() {
        return keyChar;
    }

    /**
     * Sets the character actually produced by the key stroke's key, as reported at capture time.
     *
     * @param keyChar the character, or {@code null} if not known.
     */
    public void setKeyChar(Character keyChar) {
        this.keyChar = keyChar;
    }

    public boolean isChanged() {
        return changed;
    }
}
