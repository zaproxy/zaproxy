/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpHeaderUnitTest {

    @ParameterizedTest
    @ValueSource(
            strings = {
                "application/taxii+json",
                "application/vnd.api+json",
                "text/json",
                "application/yaml",
                "application/x-yaml",
                "text/yaml",
                "application/xml",
                "application/problem+xml",
                "text/xml"
            })
    void shouldIdentifyContentTypes(String type) {
        // Given
        HttpHeader header = new HttpHeaderTest();
        header.setHeader(HttpHeader.CONTENT_TYPE, type);
        String[] acceptedTypes = {"json", "xml", "yaml"};
        // When
        boolean hasType = header.hasContentType(acceptedTypes);
        // Then
        assertThat(hasType, is(equalTo(true)));
    }

    @Test
    void shouldCloseForMalformedHeader() {
        // Given
        HttpHeader header = new HttpHeaderTest();
        header.mMalformedHeader = true;
        // When / Then
        assertThat(header.isConnectionClose(), is(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"HTTP/0.2", HttpHeader.HTTP09, HttpHeader.HTTP10, HttpHeader.HTTP2})
    void shouldDefaultToCloseForNonHttp11(String version) {
        // Given
        HttpHeader header = new HttpHeaderTest(version);
        // When / Then
        assertThat(header.isConnectionClose(), is(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {HttpHeader.CONNECTION, HttpHeader.PROXY_CONNECTION})
    void shouldRecognizeKeepAliveForHttp10(String headerName) {
        // Given
        HttpHeader header = new HttpHeaderTest(HttpHeader.HTTP10);
        header.setHeader(headerName, HttpHeader._KEEP_ALIVE);
        // When / Then
        assertThat(header.isConnectionClose(), is(false));
    }

    @Test
    void shouldRecognizeKeepAliveForHttp10WithBothHeaders() {
        // Given
        HttpHeader header = new HttpHeaderTest(HttpHeader.HTTP10);
        header.setHeader(HttpHeader.CONNECTION, HttpHeader._KEEP_ALIVE);
        header.setHeader(HttpHeader.PROXY_CONNECTION, HttpHeader._KEEP_ALIVE);
        // When / Then
        assertThat(header.isConnectionClose(), is(false));
    }

    @Test
    void shouldDefaultToKeepAliveForHttp11() {
        // Given
        HttpHeader header = new HttpHeaderTest(HttpHeader.HTTP11);
        // When / Then
        assertThat(header.isConnectionClose(), is(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {HttpHeader.CONNECTION, HttpHeader.PROXY_CONNECTION})
    void shouldRecognizeCloseForHttp11(String headerName) {
        // Given
        HttpHeader header = new HttpHeaderTest(HttpHeader.HTTP11);
        header.setHeader(headerName, HttpHeader._CLOSE);
        // When / Then
        assertThat(header.isConnectionClose(), is(true));
    }

    @Test
    void shouldRecognizeCloseForHttp11WithBothHeaders() {
        // Given
        HttpHeader header = new HttpHeaderTest(HttpHeader.HTTP11);
        header.setHeader(HttpHeader.CONNECTION, HttpHeader._CLOSE);
        header.setHeader(HttpHeader.PROXY_CONNECTION, HttpHeader._CLOSE);
        // When / Then
        assertThat(header.isConnectionClose(), is(true));
    }

    private static class HttpHeaderTest extends HttpHeader {

        private static final long serialVersionUID = 1L;

        HttpHeaderTest() {}

        HttpHeaderTest(String version) {
            setVersion(version);
        }

        @Override
        public void setVersion(String version) {
            mVersion = version;
        }

        @Override
        public String getPrimeHeader() {
            return "";
        }
    }
}
