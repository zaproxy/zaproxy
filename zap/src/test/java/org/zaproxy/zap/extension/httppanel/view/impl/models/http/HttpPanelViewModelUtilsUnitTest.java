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
package org.zaproxy.zap.extension.httppanel.view.impl.models.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.parosproxy.paros.network.HttpBody;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;

/** Unit test for {@link HttpPanelViewModelUtils}. */
class HttpPanelViewModelUtilsUnitTest {

    private static final String UNSUPPORTED_ENCODING = "UnsupportedEncoding";

    private static final Charset DEFAULT_CHARSET = Charset.forName(HttpBody.DEFAULT_CHARSET);

    private static final String BODY = "Body 123 ABC";
    private static final byte[] BODY_BYTES_DEFAULT_CHARSET = BODY.getBytes(DEFAULT_CHARSET);

    private HttpHeader header;
    private HttpBody body;

    @BeforeEach
    void setup() {
        header = mock(HttpHeader.class);
        body = mock(HttpBody.class);
    }

    @Test
    void shouldUpdateRequestContentLength() {
        // Given
        HttpMessage message = mock(HttpMessage.class);
        HttpRequestHeader requestHeader = spy(HttpRequestHeader.class);
        given(message.getRequestHeader()).willReturn(requestHeader);
        HttpRequestBody requestBody = mock(HttpRequestBody.class);
        given(message.getRequestBody()).willReturn(requestBody);
        int length = 1234;
        given(requestBody.length()).willReturn(length);
        // When
        HttpPanelViewModelUtils.updateRequestContentLength(message);
        // Then
        verify(requestHeader).setContentLength(length);
    }

    @Test
    void shouldUpdateResponseContentLength() {
        // Given
        HttpMessage message = mock(HttpMessage.class);
        HttpResponseHeader responseHeader = spy(HttpResponseHeader.class);
        given(message.getResponseHeader()).willReturn(responseHeader);
        HttpResponseBody responseBody = mock(HttpResponseBody.class);
        given(message.getResponseBody()).willReturn(responseBody);
        int length = 1234;
        given(responseBody.length()).willReturn(length);
        // When
        HttpPanelViewModelUtils.updateResponseContentLength(message);
        // Then
        verify(responseHeader).setContentLength(length);
    }

    @Test
    void shouldGetBodyString() {
        // Given
        given(body.toString()).willReturn(BODY);
        // When
        String bodyString = HttpPanelViewModelUtils.getBodyString(header, body);
        // Then
        assertThat(bodyString, is(equalTo(BODY)));
    }

    @Test
    void shouldGetBodyStringIgnoringUnsupportedEncoding() {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(UNSUPPORTED_ENCODING);
        given(body.toString()).willReturn(BODY);
        // When
        String bodyString = HttpPanelViewModelUtils.getBodyString(header, body);
        // Then
        assertThat(bodyString, is(equalTo(BODY)));
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldGetBodyStringGzipDecoded(String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        given(body.getCharset()).willReturn(DEFAULT_CHARSET.name());
        given(body.getBytes()).willReturn(gzip(BODY_BYTES_DEFAULT_CHARSET));
        // When
        String bodyString = HttpPanelViewModelUtils.getBodyString(header, body);
        // Then
        assertThat(bodyString, is(equalTo(BODY)));
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldGetBodyStringAsOriginalIfNotProperlyGzipEncoded(String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        given(body.getCharset()).willReturn(DEFAULT_CHARSET.name());
        given(body.getBytes()).willReturn(new byte[] {'N', 'o', 't', ' ', 'G', 'Z', 'I', 'P'});
        given(body.toString()).willReturn(BODY);
        // When
        String bodyString = HttpPanelViewModelUtils.getBodyString(header, body);
        // Then
        assertThat(bodyString, is(equalTo(BODY)));
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldGetBodyStringGzipDecodedEvenWithDataLossDueStringToByteConversion(
            String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        given(body.getCharset()).willReturn(DEFAULT_CHARSET.name());
        byte[] bytes = " a → z ".getBytes(StandardCharsets.UTF_8);
        given(body.getBytes()).willReturn(gzip(bytes));
        String expectedBodyString = new String(bytes, DEFAULT_CHARSET);
        // When
        String bodyString = HttpPanelViewModelUtils.getBodyString(header, body);
        // Then
        assertThat(bodyString, is(equalTo(expectedBodyString)));
    }

    @Test
    void shouldGetBodyBytes() {
        // Given
        given(body.getBytes()).willReturn(BODY_BYTES_DEFAULT_CHARSET);
        // When
        byte[] bodyBytes = HttpPanelViewModelUtils.getBodyBytes(header, body);
        // Then
        assertThat(bodyBytes, is(equalTo(BODY_BYTES_DEFAULT_CHARSET)));
    }

    @Test
    void shouldGetBodyBytesIgnoringUnsupportedEncoding() {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(UNSUPPORTED_ENCODING);
        given(body.getBytes()).willReturn(BODY_BYTES_DEFAULT_CHARSET);
        // When
        byte[] bodyBytes = HttpPanelViewModelUtils.getBodyBytes(header, body);
        // Then
        assertThat(bodyBytes, is(equalTo(BODY_BYTES_DEFAULT_CHARSET)));
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldGetBodyBytesGzipDecoded(String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        given(body.getBytes()).willReturn(gzip(BODY_BYTES_DEFAULT_CHARSET));
        // When
        byte[] bodyBytes = HttpPanelViewModelUtils.getBodyBytes(header, body);
        // Then
        assertThat(bodyBytes, is(equalTo(BODY_BYTES_DEFAULT_CHARSET)));
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldGetBodyBytesAsOriginalIfNotProperlyGzipEncoded(String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        byte[] content = new byte[] {'N', 'o', 't', ' ', 'G', 'Z', 'I', 'P'};
        given(body.getBytes()).willReturn(content);
        // When
        byte[] bodyBytes = HttpPanelViewModelUtils.getBodyBytes(header, body);
        // Then
        assertThat(bodyBytes, is(equalTo(content)));
    }

    @Test
    void shouldSetBodyString() {
        // Given
        given(body.length()).willReturn(BODY.length());
        // When
        HttpPanelViewModelUtils.setBody(header, body, BODY);
        // Then
        verify(body).setBody(BODY);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @Test
    void shouldSetBodyStringIgnoringUnsupportedEncoding() {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(UNSUPPORTED_ENCODING);
        given(body.length()).willReturn(BODY.length());
        // When
        HttpPanelViewModelUtils.setBody(header, body, BODY);
        // Then
        verify(body).setBody(BODY);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @Test
    void shouldSetBodyStringWithHeaderCharset() {
        // Given
        Charset charset = StandardCharsets.UTF_8;
        given(header.getCharset()).willReturn(charset.name());
        String bodyContent = " a → z ";
        given(body.length()).willReturn(bodyContent.length());
        // When
        HttpPanelViewModelUtils.setBody(header, body, bodyContent);
        // Then
        verify(body).setCharset(charset.name());
        verify(body).setBody(bodyContent);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldSetBodyStringAndGzipEncode(String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        byte[] gzip = gzip(BODY_BYTES_DEFAULT_CHARSET);
        given(body.length()).willReturn(gzip.length);
        given(body.getCharset()).willReturn(DEFAULT_CHARSET.name());
        // When
        HttpPanelViewModelUtils.setBody(header, body, BODY);
        // Then
        verify(body).setBody(gzip);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldSetBodyStringAndGzipEncodeEvenWithDataLossDueStringToByteConversion(
            String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        String bodyContent = " a → z ";
        byte[] gzip = gzip(bodyContent.getBytes(DEFAULT_CHARSET));
        given(body.length()).willReturn(gzip.length);
        given(body.getCharset()).willReturn(DEFAULT_CHARSET.name());
        // When
        HttpPanelViewModelUtils.setBody(header, body, bodyContent);
        // Then
        verify(body).setBody(gzip);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @Test
    void shouldSetBodyBytes() {
        // Given
        given(body.length()).willReturn(BODY_BYTES_DEFAULT_CHARSET.length);
        // When
        HttpPanelViewModelUtils.setBody(header, body, BODY_BYTES_DEFAULT_CHARSET);
        // Then
        verify(body).setBody(BODY_BYTES_DEFAULT_CHARSET);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @Test
    void shouldSetBodyBytesIgnoringUnsupportedEncoding() {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(UNSUPPORTED_ENCODING);
        given(body.length()).willReturn(BODY_BYTES_DEFAULT_CHARSET.length);
        // When
        HttpPanelViewModelUtils.setBody(header, body, BODY_BYTES_DEFAULT_CHARSET);
        // Then
        verify(body).setBody(BODY_BYTES_DEFAULT_CHARSET);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @Test
    void shouldSetBodyBytesWithHeaderCharset() {
        // Given
        Charset charset = StandardCharsets.UTF_8;
        given(header.getCharset()).willReturn(charset.name());
        byte[] bodyContent = " a → z ".getBytes(charset);
        given(body.length()).willReturn(bodyContent.length);
        // When
        HttpPanelViewModelUtils.setBody(header, body, bodyContent);
        // Then
        verify(body).setCharset(charset.name());
        verify(body).setBody(bodyContent);
        verify(header, times(0)).setContentLength(anyInt());
    }

    @ParameterizedTest
    @MethodSource(value = "gzipEncodingProvider")
    void shouldSetBodyBytesAndGzipEncode(String contentEncoding) {
        // Given
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        byte[] gzip = gzip(BODY_BYTES_DEFAULT_CHARSET);
        given(body.length()).willReturn(gzip.length);
        // When
        HttpPanelViewModelUtils.setBody(header, body, BODY_BYTES_DEFAULT_CHARSET);
        // Then
        verify(body).setBody(gzip);
        verify(header, times(0)).setContentLength(anyInt());
    }

    static Stream<String> gzipEncodingProvider() {
        return Stream.of("gzip", "gZiP", " gzip  ", "x-gzip", "X-gZiP", "  x-gzip ");
    }

    private static byte[] gzip(byte[] value) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gis = new GZIPOutputStream(baos)) {
            gis.write(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}
