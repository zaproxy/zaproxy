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

import java.awt.event.KeyEvent;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link KeyboardMapping}. */
class KeyboardMappingUnitTest {

    @BeforeEach
    void setUp() {
        Constant.messages = new I18N(Locale.ENGLISH);
    }

    @Test
    void shouldFormatNormalKeyAsUppercaseChar() {
        assertThat(KeyboardMapping.keyString('a'), is(equalTo("A")));
    }

    @Test
    void shouldParseNormalKeyAsChar() {
        assertThat(KeyboardMapping.keyCode("A"), is(equalTo('A')));
    }

    @Test
    void shouldFormatFunctionKeysF1ToF12() {
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F1), is(equalTo("F1")));
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F12), is(equalTo("F12")));
    }

    @Test
    void shouldParseFunctionKeysF1ToF12() {
        assertThat(KeyboardMapping.keyCode("F1"), is(equalTo((char) KeyEvent.VK_F1)));
        assertThat(KeyboardMapping.keyCode("F12"), is(equalTo((char) KeyEvent.VK_F12)));
    }

    @Test
    void shouldFormatExtendedFunctionKeysF13ToF24() {
        // F13-F24 are not numerically contiguous with F1-F12 in KeyEvent.
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F13), is(equalTo("F13")));
        assertThat(KeyboardMapping.keyString(KeyEvent.VK_F24), is(equalTo("F24")));
    }

    @Test
    void shouldParseExtendedFunctionKeysF13ToF24() {
        assertThat(KeyboardMapping.keyCode("F13"), is(equalTo((char) KeyEvent.VK_F13)));
        assertThat(KeyboardMapping.keyCode("F24"), is(equalTo((char) KeyEvent.VK_F24)));
    }

    @Test
    void shouldFormatArrowKeys() {
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_UP),
                is(equalTo(Constant.messages.getString("keyboard.key.up"))));
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_DOWN),
                is(equalTo(Constant.messages.getString("keyboard.key.down"))));
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_LEFT),
                is(equalTo(Constant.messages.getString("keyboard.key.left"))));
        assertThat(
                KeyboardMapping.keyString(KeyEvent.VK_RIGHT),
                is(equalTo(Constant.messages.getString("keyboard.key.right"))));
    }

    @Test
    void shouldParseArrowKeys() {
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.up")),
                is(equalTo((char) KeyEvent.VK_UP)));
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.down")),
                is(equalTo((char) KeyEvent.VK_DOWN)));
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.left")),
                is(equalTo((char) KeyEvent.VK_LEFT)));
        assertThat(
                KeyboardMapping.keyCode(Constant.messages.getString("keyboard.key.right")),
                is(equalTo((char) KeyEvent.VK_RIGHT)));
    }

    @Test
    void shouldReturnZeroForUnrecognisedKeyString() {
        assertThat(KeyboardMapping.keyCode("NotAKey"), is(equalTo((char) 0)));
    }
}
