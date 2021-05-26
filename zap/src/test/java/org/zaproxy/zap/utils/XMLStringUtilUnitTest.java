/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

class XMLStringUtilUnitTest {

    @Test
    void shouldNotEscapeXmlWithoutControlCharacters() {
        // given
        String xml = "ABCDEF";
        // when
        String result = XMLStringUtil.escapeControlChrs(xml);
        // then
        assertThat(result, is(xml));
    }

    @Test
    void shouldNotRemoveAnythingFromXmlWithoutControlCharacters() {
        // given
        String xml = "ABCDEF";
        // when
        String result = XMLStringUtil.removeControlChrs(xml);
        // then
        assertThat(result, is(xml));
    }

    @Test
    void shouldEscapeControlCharacters() {
        // given
        String xml = "A\u0000B\u0001C\u0010D\uFFFEE\uFFFFF";
        // when
        String result = XMLStringUtil.escapeControlChrs(xml);
        // then
        assertThat(result, is("A\\x0000B\\x0001C\\x0010D\\xfffeE\\xffffF"));
    }

    @Test
    void shouldRemoveControlCharacters() {
        // given
        String xml = "A\u0000B\u0001C\u0010D\uFFFEE\uFFFFF";
        // when
        String result = XMLStringUtil.removeControlChrs(xml);
        // then
        assertThat(result, is("ABCDEF"));
    }
}
