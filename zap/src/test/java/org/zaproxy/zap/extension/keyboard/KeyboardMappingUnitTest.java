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
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

    @ParameterizedTest
    @MethodSource("functionKeys")
    void shouldFormatAndParseFunctionKeys(int keyCode, String keyString) {
        assertThat(KeyboardMapping.keyString(keyCode), is(equalTo(keyString)));
        assertThat(KeyboardMapping.keyCode(keyString), is(equalTo((char) keyCode)));
    }

    private static Stream<Arguments> functionKeys() {
        return Stream.of(
                Arguments.of(KeyEvent.VK_F1, "F1"),
                Arguments.of(KeyEvent.VK_F2, "F2"),
                Arguments.of(KeyEvent.VK_F3, "F3"),
                Arguments.of(KeyEvent.VK_F4, "F4"),
                Arguments.of(KeyEvent.VK_F5, "F5"),
                Arguments.of(KeyEvent.VK_F6, "F6"),
                Arguments.of(KeyEvent.VK_F7, "F7"),
                Arguments.of(KeyEvent.VK_F8, "F8"),
                Arguments.of(KeyEvent.VK_F9, "F9"),
                Arguments.of(KeyEvent.VK_F10, "F10"),
                Arguments.of(KeyEvent.VK_F11, "F11"),
                Arguments.of(KeyEvent.VK_F12, "F12"),
                Arguments.of(KeyEvent.VK_F13, "F13"),
                Arguments.of(KeyEvent.VK_F14, "F14"),
                Arguments.of(KeyEvent.VK_F15, "F15"),
                Arguments.of(KeyEvent.VK_F16, "F16"),
                Arguments.of(KeyEvent.VK_F17, "F17"),
                Arguments.of(KeyEvent.VK_F18, "F18"),
                Arguments.of(KeyEvent.VK_F19, "F19"),
                Arguments.of(KeyEvent.VK_F20, "F20"),
                Arguments.of(KeyEvent.VK_F21, "F21"),
                Arguments.of(KeyEvent.VK_F22, "F22"),
                Arguments.of(KeyEvent.VK_F23, "F23"),
                Arguments.of(KeyEvent.VK_F24, "F24"));
    }

    @ParameterizedTest
    @MethodSource("arrowKeys")
    void shouldFormatAndParseArrowKeys(int keyCode, String keyString) {
        assertThat(KeyboardMapping.keyString(keyCode), is(equalTo(keyString)));
        assertThat(KeyboardMapping.keyCode(keyString), is(equalTo((char) keyCode)));
    }

    private static Stream<Arguments> arrowKeys() {
        return Stream.of(
                Arguments.of(KeyEvent.VK_UP, "Up"),
                Arguments.of(KeyEvent.VK_DOWN, "Down"),
                Arguments.of(KeyEvent.VK_LEFT, "Left"),
                Arguments.of(KeyEvent.VK_RIGHT, "Right"));
    }

    @Test
    void shouldReturnZeroForUnrecognisedKeyString() {
        assertThat(KeyboardMapping.keyCode("NotAKey"), is(equalTo((char) 0)));
    }
}
