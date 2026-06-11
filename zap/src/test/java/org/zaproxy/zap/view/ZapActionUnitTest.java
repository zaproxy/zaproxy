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
package org.zaproxy.zap.view;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ZapAction}. */
class ZapActionUnitTest {

    private static final String IDENTIFIER = "test.action";
    private static final String NAME = "Test Action";
    private static final KeyStroke DEFAULT_ACCELERATOR =
            KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK);

    @Test
    void shouldExposeIdentifierNameAndDefaultAccelerator() {
        ZapAction action = new ZapAction(IDENTIFIER, NAME, DEFAULT_ACCELERATOR);

        assertThat(action.getIdentifier(), is(equalTo(IDENTIFIER)));
        assertThat(action.getValue(Action.NAME), is(equalTo(NAME)));
        assertThat(action.getDefaultAccelerator(), is(equalTo(DEFAULT_ACCELERATOR)));
        assertThat(action.getAccelerator(), is(equalTo(DEFAULT_ACCELERATOR)));
    }

    @Test
    void shouldUpdateAndResetAccelerator() {
        ZapAction action = new ZapAction(IDENTIFIER, NAME, DEFAULT_ACCELERATOR);
        KeyStroke newAccelerator = KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.ALT_DOWN_MASK);

        action.setAccelerator(newAccelerator);
        assertThat(action.getAccelerator(), is(equalTo(newAccelerator)));

        action.resetAccelerator();
        assertThat(action.getAccelerator(), is(equalTo(DEFAULT_ACCELERATOR)));
    }

    @Test
    void shouldRejectNullIdentifier() {
        assertThrows(
                NullPointerException.class, () -> new ZapAction(null, NAME, DEFAULT_ACCELERATOR));
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(
                NullPointerException.class,
                () -> new ZapAction(IDENTIFIER, null, DEFAULT_ACCELERATOR));
    }
}
