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
package org.parosproxy.paros.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit test for {@link HttpResponseHeader}. */
class HttpResponseHeaderUnitTest {

    @Test
    void shouldBeEmptyIfNoContents() {
        // Given
        HttpResponseHeader header = new HttpResponseHeader();
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(true)));
    }

    @Test
    void shouldNotBeEmptyIfItHasStatusLine() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEmptyIfItHasStatusAndHeaders() throws Exception {
        // Given
        HttpResponseHeader header =
                new HttpResponseHeader("HTTP/1.1 200 OK\r\nX-Header: value\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "HTTP/0.9",
                "HTTP/1.0",
                "HTTP/1.1",
                "HTTP/1.2",
                "HTTP/2",
                "HTTP/3.0",
                "HTTP/4.5"
            })
    void shouldParseWithArbitraryHttpVersions(String version) throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader(version + " 200 OK\r\n\r\n");
        // When
        String parsedVersion = header.getVersion();
        // Then
        assertThat(parsedVersion, is(equalTo(version)));
    }

    @Test
    void shouldSetValidStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        header.setStatusCode(100);
        // Then
        assertThat(header.getStatusCode(), is(equalTo(100)));
        assertThat(header.getPrimeHeader(), is(equalTo("HTTP/1.1 100 OK")));
    }

    @Test
    void shouldFailToSetNegative3DigitStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> header.setStatusCode(-200));
        // Then
        assertThat(e.getMessage(), containsString("positive"));
    }

    @Test
    void shouldFailToSet2DigitStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> header.setStatusCode(99));
        // Then
        assertThat(e.getMessage(), containsString("3 digit number"));
    }

    @Test
    void shouldFailToSet4DigitStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> header.setStatusCode(1000));
        // Then
        assertThat(e.getMessage(), containsString("3 digit number"));
    }

    @Test
    void shouldSetReasonPhrase() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        header.setReasonPhrase("So So");
        // Then
        assertThat(header.getReasonPhrase(), is(equalTo("So So")));
        assertThat(header.getPrimeHeader(), is(equalTo("HTTP/1.1 200 So So")));
    }

    @Test
    void shouldSetEmptyReasonPhraseIfNull() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        header.setReasonPhrase(null);
        // Then
        assertThat(header.getReasonPhrase(), is(equalTo("")));
        assertThat(header.getPrimeHeader(), is(equalTo("HTTP/1.1 200")));
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "text/html", // Text but not css
                "image/png", // Not text or css
                "teXt/hTmL", // Mixed case
                "text/html; charset=UTF-8", // Expected charset
                "text/html;charset=UTF-8", // Charset without space
                "charset=UTF-8; text/html" // Charset first
            })
    void isCssShouldReturnFalseWhenContentTypeDoesNotIndicateCss(String contentType) {
        // Given
        HttpResponseHeader hrh = createResponseHeader(contentType);
        // When / Then
        assertFalse(hrh.isCss());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "text/css", // CSS
                "teXt/cSs", // Mixed case
                "text/css; charset=UTF-8", // Expected charset
                "text/css;charset=UTF-8", // Charset without space
                "charset=UTF-8; text/css" // Charset first
            })
    void isCssShouldReturnTrueWhenContentTypeIndicatesCss(String contentType) {
        // Given
        HttpResponseHeader hrh = createResponseHeader(contentType);
        // When / Then
        assertTrue(hrh.isCss());
    }

    private static HttpResponseHeader createResponseHeader(String contentType) {
        HttpResponseHeader hrh = new HttpResponseHeader();
        hrh.setHeader(HttpHeader.CONTENT_TYPE, contentType);
        return hrh;
    }
}
