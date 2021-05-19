/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;

import java.util.HashMap;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

class HttpSenderAuthHeaderListenerUnitTest {

    private HttpSenderAuthHeaderListener authHeaderListener;

    @Test
    void shouldNotAddAuthHeaderWhenNotDefined() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msg = new HttpMessage();

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(0));
    }

    @Test
    void shouldAddAuthHeaderWhenDefined() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msg = new HttpMessage();

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(1));
        assertThat(msg.getRequestHeader().getHeader(HttpHeader.AUTHORIZATION), is(headerValue));
    }

    @Test
    void shouldIgnoreEmptyValue() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, "");
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msg = new HttpMessage();

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(0));
    }

    @Test
    void shouldAddSpecifiedHeaderWhenDefined() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerType = "My-header";
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, headerType);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msg = new HttpMessage();

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(1));
        assertThat(msg.getRequestHeader().getHeader(headerType), is(headerValue));
    }

    @Test
    void shouldIgnoreEmptyHeaderType() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, "");
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msg = new HttpMessage();

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(1));
        assertThat(msg.getRequestHeader().getHeader(HttpHeader.AUTHORIZATION), is(headerValue));
    }

    @Test
    void shouldNotChangeResponse() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerType = "My-header";
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, headerType);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msg = new HttpMessage();

        // When
        authHeaderListener.onHttpResponseReceive(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(0));
    }

    @Test
    void shouldAddSpecifiedHeaderToSpecifiedHost() throws Exception {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerType = "My-header";
        String headerValue = "example header value";
        String headerHost = "localhost";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, headerType);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE, headerHost);
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msgLocal = new HttpMessage(new URI("https://localhost:8080/", true));
        HttpMessage msgRemote = new HttpMessage(new URI("https://example.com/", true));

        // When
        authHeaderListener.onHttpRequestSend(msgLocal, -1, null);
        authHeaderListener.onHttpRequestSend(msgRemote, -1, null);

        // Then
        assertThat(msgLocal.getRequestHeader().getHeader(headerType), is(headerValue));
        assertThat(msgRemote.getRequestHeader().getHeader(headerType), isEmptyOrNullString());
    }

    @Test
    void shouldIgnoreEmptyHost() throws Exception {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerType = "My-header";
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, headerType);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE, "");
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);
        HttpMessage msgLocal = new HttpMessage(new URI("https://localhost:8080/", true));
        HttpMessage msgRemote = new HttpMessage(new URI("https://example.com/", true));

        // When
        authHeaderListener.onHttpRequestSend(msgLocal, -1, null);
        authHeaderListener.onHttpRequestSend(msgRemote, -1, null);

        // Then
        assertThat(msgLocal.getRequestHeader().getHeader(headerType), is(headerValue));
        assertThat(msgRemote.getRequestHeader().getHeader(headerType), is(headerValue));
    }
}
