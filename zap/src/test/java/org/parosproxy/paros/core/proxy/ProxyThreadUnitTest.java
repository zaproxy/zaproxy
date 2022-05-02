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
package org.parosproxy.paros.core.proxy;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.network.HttpEncoding;
import org.zaproxy.zap.network.HttpResponseBody;

/** Unit test for {@link ProxyThread}. */
@Deprecated
class ProxyThreadUnitTest {

    @Test
    void shouldDecodeResponseIfNeeded() {
        // Given
        HttpResponseHeader responseHeader = mock(HttpResponseHeader.class);
        given(responseHeader.getHeader(HttpHeader.CONTENT_LENGTH)).willReturn("1");
        HttpResponseBody responseBody = mock(HttpResponseBody.class);
        given(responseBody.getContentEncodings()).willReturn(asList(mock(HttpEncoding.class)));
        byte[] content = "ABC".getBytes(StandardCharsets.ISO_8859_1);
        given(responseBody.getContent()).willReturn(content);
        given(responseBody.length()).willReturn(content.length);
        HttpMessage message = createMessage(responseHeader, responseBody);
        // When
        ProxyThread.decodeResponseIfNeeded(message);
        // Then
        verify(responseBody).setBody(content);
        verify(responseBody).setContentEncodings(Collections.emptyList());
        verify(responseHeader).setHeader(HttpHeader.CONTENT_ENCODING, null);
        verify(responseHeader).setContentLength(content.length);
    }

    @Test
    void shouldDecodeResponseIfNeededButNotSetContentLengthIfNotPresent() {
        // Given
        HttpResponseHeader responseHeader = mock(HttpResponseHeader.class);
        given(responseHeader.getHeader(HttpHeader.CONTENT_LENGTH)).willReturn(null);
        HttpResponseBody responseBody = mock(HttpResponseBody.class);
        given(responseBody.getContentEncodings()).willReturn(asList(mock(HttpEncoding.class)));
        byte[] content = "ABC".getBytes(StandardCharsets.ISO_8859_1);
        given(responseBody.getContent()).willReturn(content);
        given(responseBody.length()).willReturn(content.length);
        HttpMessage message = createMessage(responseHeader, responseBody);
        // When
        ProxyThread.decodeResponseIfNeeded(message);
        // Then
        verify(responseBody).setBody(content);
        verify(responseBody).setContentEncodings(Collections.emptyList());
        verify(responseHeader).setHeader(HttpHeader.CONTENT_ENCODING, null);
        verify(responseHeader, times(0)).setContentLength(anyInt());
    }

    @Test
    void shouldNotDecodeResponseIfNoContentEncodings() {
        // Given
        HttpResponseHeader responseHeader = mock(HttpResponseHeader.class);
        given(responseHeader.getHeader(HttpHeader.CONTENT_LENGTH)).willReturn("1");
        HttpResponseBody responseBody = mock(HttpResponseBody.class);
        given(responseBody.getContentEncodings()).willReturn(Collections.emptyList());
        byte[] content = "ABC".getBytes(StandardCharsets.ISO_8859_1);
        given(responseBody.getContent()).willReturn(content);
        given(responseBody.length()).willReturn(content.length);
        HttpMessage message = createMessage(responseHeader, responseBody);
        // When
        ProxyThread.decodeResponseIfNeeded(message);
        // Then
        verify(responseBody, times(0)).setBody(content);
        verify(responseBody, times(0)).setContentEncodings(Collections.emptyList());
        verify(responseHeader, times(0)).setHeader(HttpHeader.CONTENT_ENCODING, null);
        verify(responseHeader, times(0)).setContentLength(content.length);
    }

    @Test
    void shouldNotDecodeResponseWithContentEncodingErrors() {
        // Given
        HttpResponseHeader responseHeader = mock(HttpResponseHeader.class);
        given(responseHeader.getHeader(HttpHeader.CONTENT_LENGTH)).willReturn("1");
        HttpResponseBody responseBody = mock(HttpResponseBody.class);
        given(responseBody.getContentEncodings()).willReturn(asList(mock(HttpEncoding.class)));
        given(responseBody.hasContentEncodingErrors()).willReturn(true);
        byte[] content = "ABC".getBytes(StandardCharsets.ISO_8859_1);
        given(responseBody.getContent()).willReturn(content);
        given(responseBody.length()).willReturn(content.length);
        HttpMessage message = createMessage(responseHeader, responseBody);
        // When
        ProxyThread.decodeResponseIfNeeded(message);
        // Then
        verify(responseBody, times(0)).setBody(content);
        verify(responseBody, times(0)).setContentEncodings(Collections.emptyList());
        verify(responseHeader, times(0)).setHeader(HttpHeader.CONTENT_ENCODING, null);
        verify(responseHeader, times(0)).setContentLength(content.length);
    }

    private static HttpMessage createMessage(HttpResponseHeader header, HttpResponseBody body) {
        HttpMessage message = mock(HttpMessage.class);
        given(message.getResponseHeader()).willReturn(header);
        given(message.getResponseBody()).willReturn(body);
        return message;
    }
}
