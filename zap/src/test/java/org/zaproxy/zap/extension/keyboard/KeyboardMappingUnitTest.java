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

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Locale;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link KeyboardMapping}. */
class KeyboardMappingUnitTest {

    @BeforeEach
    void setUp() {
        I18N i18n = new I18N(Locale.ENGLISH);
        Constant.messages = i18n;
    }

    @Test
    void shouldFormatCtrlAltShortcutWithSymbols() {
        KeyStroke keyStroke =
                KeyStroke.getKeyStroke(
                        KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);

        assertThat(KeyStrokeDisplay.formatSymbols(keyStroke), is(equalTo("⌃⌥J")));
        assertThat(KeyStrokeDisplay.formatNames(keyStroke), containsString("J"));
        assertThat(KeyStrokeDisplay.formatHtmlNames(keyStroke), containsString("<kbd>"));
    }
}
