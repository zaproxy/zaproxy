/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.apache.commons.httpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HttpMethodBaseUnitTest {

    @ParameterizedTest
    @MethodSource("cookieHeaderProvider")
    void testParseCookieHeader(String cookieHeaderValue, int numberOfCookies) {
        List<Cookie> cookies = HttpMethodBase.parseCookieHeader("example.com", cookieHeaderValue);
        assertThat(cookies, hasSize(numberOfCookies));
    }

    static Stream<Arguments> cookieHeaderProvider() {
        return Stream.of(
                arguments("", 0),
                arguments("JSESSIONID=5DFA94B903A0063839E0440118808875", 1),
                arguments("has_js=1;JSESSIONID=5DFA94B903A0063839E0440118808875", 2),
                arguments("has_js=1; JSESSIONID=5DFA94B903A0063839E0440118808875", 2),
                arguments("has_js=;JSESSIONID=5DFA94B903A0063839E0440118808875", 2));
    }
}
