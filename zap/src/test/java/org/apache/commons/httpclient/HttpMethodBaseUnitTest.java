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
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.httpclient.protocol.Protocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("deprecation")
class HttpMethodBaseUnitTest {

    private static final Header EXPECTED_HOST_HEADER = header("Host", "example.com");

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

    @Test
    void shouldAddHostHeaderIfNotPresent() throws Exception {
        // Given
        HttpMethodBase methodBase = new TestHttpMethodBase();
        Header headerA = header("A", "Value A");
        methodBase.addRequestHeader(headerA);
        HttpConnection conn = connection("example.com", 443);
        // When
        methodBase.addHostRequestHeader(null, conn);
        // Then
        assertThat(
                methodBase.getRequestHeaders(), is(arrayContaining(headerA, EXPECTED_HOST_HEADER)));
    }

    @Test
    void shouldKeepHostHeaderIfValueMatch() throws Exception {
        // Given
        HttpMethodBase methodBase = new TestHttpMethodBase();
        Header hostHeader = header("Host", "example.com");
        methodBase.addRequestHeader(hostHeader);
        Header headerA = header("A", "Value A");
        methodBase.addRequestHeader(headerA);
        HttpConnection conn = connection("example.com", 443);
        // When
        methodBase.addHostRequestHeader(null, conn);
        // Then
        assertThat(
                methodBase.getRequestHeaders(), is(arrayContaining(EXPECTED_HOST_HEADER, headerA)));
    }

    @Test
    void shouldUpdateHostHeaderIfValueMismatch() throws Exception {
        // Given
        HttpMethodBase methodBase = new TestHttpMethodBase();
        methodBase.addRequestHeader(header("Host", "example2.com"));
        HttpConnection conn = connection("example.com", 443);
        // When
        methodBase.addHostRequestHeader(null, conn);
        // Then
        assertThat(methodBase.getRequestHeaders(), is(arrayContaining(EXPECTED_HOST_HEADER)));
    }

    @Test
    void shouldUpdateHostHeaderInPlace() throws Exception {
        // Given
        HttpMethodBase methodBase = new TestHttpMethodBase();
        Header headerA = header("A", "Value A");
        methodBase.addRequestHeader(headerA);
        Header hostHeader = header("Host", "example2.com");
        methodBase.addRequestHeader(hostHeader);
        Header headerB = header("B", "Value B");
        methodBase.addRequestHeader(headerB);
        HttpConnection conn = connection("example.com", 443);
        // When
        methodBase.addHostRequestHeader(null, conn);
        // Then
        assertThat(
                methodBase.getRequestHeaders(),
                is(arrayContaining(headerA, EXPECTED_HOST_HEADER, headerB)));
    }

    @Test
    void shouldKeepOnlyOneHostHeader() throws Exception {
        // Given
        HttpMethodBase methodBase = new TestHttpMethodBase();
        Header headerA = header("A", "Value A");
        methodBase.addRequestHeader(headerA);
        Header hostHeader1 = header("Host", "example.com");
        methodBase.addRequestHeader(hostHeader1);
        Header headerB = header("B", "Value B");
        methodBase.addRequestHeader(headerB);
        Header headerHost2 = header("Host", "Should Remove 1");
        methodBase.addRequestHeader(headerHost2);
        Header headerHost3 = header("Host", "Should Remove 2");
        methodBase.addRequestHeader(headerHost3);
        HttpConnection conn = connection("example.com", 443);
        // When
        methodBase.addHostRequestHeader(null, conn);
        // Then
        assertThat(
                methodBase.getRequestHeaders(),
                is(arrayContaining(headerA, EXPECTED_HOST_HEADER, headerB)));
    }

    @Test
    void shouldUpdateAndKeepOnlyOneHostHeader() throws Exception {
        // Given
        HttpMethodBase methodBase = new TestHttpMethodBase();
        Header headerA = header("A", "Value A");
        methodBase.addRequestHeader(headerA);
        Header hostHeader1 = header("Host", "example2.com");
        methodBase.addRequestHeader(hostHeader1);
        Header headerB = header("B", "Value B");
        methodBase.addRequestHeader(headerB);
        Header headerHost2 = header("Host", "Should Remove 1");
        methodBase.addRequestHeader(headerHost2);
        Header headerHost3 = header("Host", "Should Remove 2");
        methodBase.addRequestHeader(headerHost3);
        HttpConnection conn = connection("example.com", 443);
        // When
        methodBase.addHostRequestHeader(null, conn);
        // Then
        assertThat(
                methodBase.getRequestHeaders(),
                is(arrayContaining(headerA, EXPECTED_HOST_HEADER, headerB)));
    }

    private static HttpConnection connection(String host, int port) {
        HttpConnection connection = mock(HttpConnection.class);
        given(connection.getHost()).willReturn(host);
        given(connection.getPort()).willReturn(port);
        Protocol protocol = mock(Protocol.class);
        given(protocol.getDefaultPort()).willReturn(port);
        given(connection.getProtocol()).willReturn(protocol);
        return connection;
    }

    private static Header header(String name, String value) {
        return new Header(name, value);
    }

    private static class TestHttpMethodBase extends HttpMethodBase {

        @Override
        public String getName() {
            return "TEST";
        }
    }
}
