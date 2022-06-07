/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.network;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.junit.jupiter.api.Test;

/** Unit test for {@link ZapCookieSpec}. */
class ZapCookieSpecUnitTest {

    private static final String HOST = "example.com";
    private static final int PORT = 8443;
    private static final String PATH = "/path/file";
    private static final boolean SECURE = true;

    @Test
    void shouldThrowWhenValidatingWithNullHost() throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        String host = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> cookieSpec.validate(host, PORT, PATH, SECURE, new Cookie()));
    }

    @Test
    void shouldThrowWhenValidatingWithEmptyHost() throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        String host = "";
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> cookieSpec.validate(host, PORT, PATH, SECURE, new Cookie()));
    }

    @Test
    void shouldThrowWhenValidatingWithNegativePort() throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        int port = -1;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> cookieSpec.validate(HOST, port, PATH, SECURE, new Cookie()));
    }

    @Test
    void shouldThrowWhenValidatingWithNullPath() throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        String path = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> cookieSpec.validate(HOST, PORT, path, SECURE, new Cookie()));
    }

    @Test
    void shouldThrowWhenValidatingWithNullCookie() throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        Cookie cookie = null;
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> cookieSpec.validate(HOST, PORT, PATH, SECURE, cookie));
    }

    @Test
    void shouldThrowWhenValidatingWithNullCookieDomain() throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        Cookie cookie = new Cookie(null, "name", "value");
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> cookieSpec.validate(HOST, PORT, PATH, SECURE, cookie));
    }

    @Test
    void shouldBeMalformedWhenValidatingWithNegativeCookieVersion()
            throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        Cookie cookie = new Cookie(HOST, "name", "value");
        cookie.setVersion(-1);
        // When / Then
        assertThrows(
                MalformedCookieException.class,
                () -> cookieSpec.validate(HOST, PORT, PATH, SECURE, cookie));
    }

    @Test
    void shouldBeValidEvenIfCookiePathIsDifferentThanOrigin() throws MalformedCookieException {
        // Given
        CookieSpec cookieSpec = createCookieSpec();
        Cookie cookie = new Cookie(HOST, "name", "value");
        cookie.setPath("/other/path/");
        // When / Then
        assertDoesNotThrow(() -> cookieSpec.validate(HOST, PORT, PATH, SECURE, cookie));
    }

    @SuppressWarnings("deprecation")
    private static CookieSpec createCookieSpec() {
        return new ZapCookieSpec();
    }
}
