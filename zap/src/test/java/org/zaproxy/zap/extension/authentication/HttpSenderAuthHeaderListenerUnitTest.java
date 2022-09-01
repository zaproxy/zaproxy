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
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.script.ScriptVars;

class HttpSenderAuthHeaderListenerUnitTest {

    private HttpSenderAuthHeaderListener authHeaderListener;
    private HttpMessage msg;
    private int origHeaderCount;

    @BeforeEach
    void setUp() throws Exception {
        ScriptVars.clear();
        msg = new HttpMessage(new URI("https://www.example.com", true));
        origHeaderCount = msg.getRequestHeader().getHeaders().size();
    }

    @Test
    void shouldListenerOrderMaxInt() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        HttpSenderAuthHeaderListener listener = new HttpSenderAuthHeaderListener(map::get);

        // When / Then
        assertThat(listener.getListenerOrder(), is(equalTo(Integer.MAX_VALUE)));
    }

    @Test
    void shouldTreatEmptyStringsAsNulls() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, "");
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE, "");
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, "");
        new HttpSenderAuthHeaderListener(map::get);

        // When / Then
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(HttpHeader.AUTHORIZATION)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(null)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(null)));
    }

    @Test
    void shouldNotAddAuthHeaderWhenNotDefined() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(origHeaderCount));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(HttpHeader.AUTHORIZATION)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(null)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(null)));
    }

    @Test
    void shouldAddAuthHeaderWhenDefined() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(origHeaderCount + 1));
        assertThat(msg.getRequestHeader().getHeader(HttpHeader.AUTHORIZATION), is(headerValue));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(HttpHeader.AUTHORIZATION)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(headerValue)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(null)));
    }

    @Test
    void shouldIgnoreEmptyValue() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, "");
        authHeaderListener = new HttpSenderAuthHeaderListener(map::get);

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(origHeaderCount));
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

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(origHeaderCount + 1));
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

        // When
        authHeaderListener.onHttpRequestSend(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(origHeaderCount + 1));
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

        // When
        authHeaderListener.onHttpResponseReceive(msg, -1, null);

        // Then
        assertThat(msg.getRequestHeader().getHeaders().size(), is(origHeaderCount));
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
        assertThat(msgRemote.getRequestHeader().getHeader(headerType), is(emptyOrNullString()));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(headerType)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(headerValue)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(headerHost)));
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

    @Test
    void shouldAddDefaultHeaderGivenValueForGivenSite() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerValue = "example header value";
        String headerHost = "example";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE, headerHost);
        HttpSenderAuthHeaderListener listener = new HttpSenderAuthHeaderListener(map::get);

        // When
        listener.onHttpRequestSend(msg, 0, null);
        HttpRequestHeader header = msg.getRequestHeader();

        // Then
        assertThat(header.getHeaders().size(), is(equalTo(origHeaderCount + 1)));
        assertThat(header.getHeader(HttpHeader.AUTHORIZATION), is(equalTo(headerValue)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(HttpHeader.AUTHORIZATION)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(headerValue)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(headerHost)));
    }

    @Test
    void shouldAddSpecifiedHeaderWhenSetDirectly() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerType = "my-header";
        String headerValue = "example header value";
        HttpSenderAuthHeaderListener listener = new HttpSenderAuthHeaderListener(map::get);
        ScriptVars.setGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, headerType);
        ScriptVars.setGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);

        // When
        listener.onHttpRequestSend(msg, 0, null);
        HttpRequestHeader header = msg.getRequestHeader();

        // Then
        assertThat(header.getHeaders().size(), is(equalTo(origHeaderCount + 1)));
        assertThat(header.getHeader(HttpHeader.AUTHORIZATION), is(equalTo(null)));
        assertThat(header.getHeader(headerType), is(equalTo(headerValue)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(headerType)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(headerValue)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(null)));
    }

    @Test
    void shouldNotAddSpecifiedHeaderWhenUnsetDirectly() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerType = "my-header";
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, headerType);
        HttpSenderAuthHeaderListener listener = new HttpSenderAuthHeaderListener(map::get);
        ScriptVars.setGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, null);

        // When
        listener.onHttpRequestSend(msg, 0, null);
        List<HttpHeaderField> headers = msg.getRequestHeader().getHeaders();

        // Then
        assertThat(headers.size(), is(equalTo(origHeaderCount)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(headerType)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(null)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(null)));
    }

    @Test
    void shouldAddDefaultHeaderWhenSpecifiedHeaderUnsetDirectly() {
        // Given
        HashMap<String, String> map = new HashMap<>();
        String headerType = "my-header";
        String headerValue = "example header value";
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE, headerValue);
        map.put(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, headerType);
        HttpSenderAuthHeaderListener listener = new HttpSenderAuthHeaderListener(map::get);
        ScriptVars.setGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER, null);

        // When
        listener.onHttpRequestSend(msg, 0, null);
        HttpRequestHeader header = msg.getRequestHeader();

        // Then
        assertThat(header.getHeaders().size(), is(equalTo(origHeaderCount + 1)));
        assertThat(header.getHeader(HttpHeader.AUTHORIZATION), is(equalTo(headerValue)));
        assertThat(header.getHeader("my-header"), is(equalTo(null)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER),
                is(equalTo(HttpHeader.AUTHORIZATION)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_VALUE),
                is(equalTo(headerValue)));
        assertThat(
                ScriptVars.getGlobalVar(HttpSenderAuthHeaderListener.ZAP_AUTH_HEADER_SITE),
                is(equalTo(null)));
    }
}
