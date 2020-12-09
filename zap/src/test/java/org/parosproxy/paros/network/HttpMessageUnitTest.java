/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.network.HttpEncoding;
import org.zaproxy.zap.network.HttpEncodingDeflate;
import org.zaproxy.zap.network.HttpEncodingGzip;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;
import org.zaproxy.zap.users.User;

/** Unit test for {@link HttpMessage}. */
public class HttpMessageUnitTest {

    @Test
    public void shouldBeEventStreamIfRequestWithoutResponseAcceptsEventStream() throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader("GET / HTTP/1.1\r\nAccept: text/event-stream"));
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream, is(equalTo(true)));
    }

    @Test
    public void shouldNotBeEventStreamIfRequestWithoutResponseDoesNotAcceptJustEventStream()
            throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(new HttpRequestHeader("GET / HTTP/1.1\r\nAccept: */*"));
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream, is(equalTo(false)));
    }

    @Test
    public void shouldBeEventStreamIfResponseHasEventStreamContentType() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        message.getResponseHeader()
                .setHeader(HttpHeader.CONTENT_TYPE, "text/event-stream;charset=utf-8");
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream, is(equalTo(true)));
    }

    @Test
    public void shouldNotBeEventStreamIfResponseDoesNotHaveEventStreamContentType()
            throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        message.getResponseHeader()
                .setHeader(HttpHeader.CONTENT_TYPE, "text/not-event-stream;charset=utf-8");
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream, is(equalTo(false)));
    }

    @Test
    public void shouldCopyHttpMessage() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        // When
        HttpMessage copy = new HttpMessage(message);
        // Then
        assertThat(copy.getRequestHeader(), is(not(sameInstance(message.getRequestHeader()))));
        assertThat(
                copy.getRequestHeader().toString(),
                is(equalTo(message.getRequestHeader().toString())));
        assertThat(copy.getRequestBody(), is(not(sameInstance(message.getRequestBody()))));
        assertThat(
                copy.getRequestBody().toString(), is(equalTo(message.getRequestBody().toString())));
        assertThat(copy.getResponseHeader(), is(not(sameInstance(message.getResponseHeader()))));
        assertThat(
                copy.getResponseHeader().toString(),
                is(equalTo(message.getResponseHeader().toString())));
        assertThat(copy.getResponseBody(), is(not(sameInstance(message.getResponseBody()))));
        assertThat(
                copy.getResponseBody().toString(),
                is(equalTo(message.getResponseBody().toString())));

        assertThat(copy.getUserObject(), is(sameInstance(message.getUserObject())));
        assertThat(copy.getTimeSentMillis(), is(equalTo(message.getTimeSentMillis())));
        assertThat(copy.getTimeElapsedMillis(), is(equalTo(message.getTimeElapsedMillis())));
        assertThat(copy.getNote(), is(equalTo(message.getNote())));
        assertThat(copy.getHistoryRef(), is(sameInstance(message.getHistoryRef())));
        assertThat(copy.getHttpSession(), is(sameInstance(message.getHttpSession())));
        assertThat(copy.getRequestingUser(), is(sameInstance(message.getRequestingUser())));
        assertThat(copy.isForceIntercept(), is(equalTo(message.isForceIntercept())));
        assertThat(
                copy.isResponseFromTargetHost(), is(equalTo(message.isResponseFromTargetHost())));
    }

    @Test
    public void shouldCloneRequest() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        // When
        HttpMessage copy = message.cloneRequest();
        // Then
        assertThat(copy.getRequestHeader(), is(not(sameInstance(message.getRequestHeader()))));
        assertThat(
                copy.getRequestHeader().toString(),
                is(equalTo(message.getRequestHeader().toString())));
        assertThat(copy.getRequestBody(), is(not(sameInstance(message.getRequestBody()))));
        assertThat(
                copy.getRequestBody().toString(), is(equalTo(message.getRequestBody().toString())));
        assertThat(copy.getResponseHeader(), is(not(sameInstance(message.getResponseHeader()))));
        assertThat(copy.getResponseHeader().isEmpty(), is(equalTo(true)));
        assertThat(copy.getResponseBody(), is(not(sameInstance(message.getResponseBody()))));
        assertThat(copy.getResponseBody().toString(), is(equalTo("")));

        assertThat(copy.getUserObject(), is(nullValue()));
        assertThat(copy.getTimeSentMillis(), is(equalTo(0L)));
        assertThat(copy.getTimeElapsedMillis(), is(equalTo(0)));
        assertThat(copy.getNote(), is(equalTo("")));
        assertThat(copy.getHistoryRef(), is(nullValue()));
        assertThat(copy.getHttpSession(), is(nullValue()));
        assertThat(copy.getRequestingUser(), is(nullValue()));
        assertThat(copy.isForceIntercept(), is(equalTo(false)));
        assertThat(copy.isResponseFromTargetHost(), is(equalTo(false)));
    }

    @Test
    public void shouldCloneAll() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        // When
        HttpMessage copy = message.cloneAll();
        // Then
        assertThat(copy.getRequestHeader(), is(not(sameInstance(message.getRequestHeader()))));
        assertThat(
                copy.getRequestHeader().toString(),
                is(equalTo(message.getRequestHeader().toString())));
        assertThat(copy.getRequestBody(), is(not(sameInstance(message.getRequestBody()))));
        assertThat(
                copy.getRequestBody().toString(), is(equalTo(message.getRequestBody().toString())));
        assertThat(copy.getResponseHeader(), is(not(sameInstance(message.getResponseHeader()))));
        assertThat(
                copy.getResponseHeader().toString(),
                is(equalTo(message.getResponseHeader().toString())));
        assertThat(copy.getResponseBody(), is(not(sameInstance(message.getResponseBody()))));
        assertThat(
                copy.getResponseBody().toString(),
                is(equalTo(message.getResponseBody().toString())));

        assertThat(copy.getUserObject(), is(nullValue()));
        assertThat(copy.getTimeSentMillis(), is(equalTo(0L)));
        assertThat(copy.getTimeElapsedMillis(), is(equalTo(0)));
        assertThat(copy.getNote(), is(equalTo("")));
        assertThat(copy.getHistoryRef(), is(nullValue()));
        assertThat(copy.getHttpSession(), is(nullValue()));
        assertThat(copy.getRequestingUser(), is(nullValue()));
        assertThat(copy.isForceIntercept(), is(equalTo(false)));
        assertThat(copy.isResponseFromTargetHost(), is(equalTo(false)));
    }

    @Test
    void shouldNotSetContentEncodingsWhenSettingHttpRequestBody() {
        // Given
        HttpRequestHeader header = mock(HttpRequestHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(HttpHeader.GZIP);
        HttpRequestBody body = mock(HttpRequestBody.class);
        HttpMessage message = new HttpMessage(header, body);
        // When
        message.setRequestBody(body);
        // Then
        verify(body, times(0)).setContentEncodings(any());
    }

    @Test
    void shouldSetContentEncodingsWhenSettingRequestBodyByte() {
        // Given
        HttpRequestHeader header = mock(HttpRequestHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(HttpHeader.GZIP);
        HttpRequestBody body = mock(HttpRequestBody.class);
        HttpMessage message = new HttpMessage(header, body);
        // When
        message.setRequestBody(new byte[0]);
        // Then
        assertThat(encodings(body), is(not(empty())));
    }

    @Test
    void shouldSetContentEncodingsWhenSettingRequestBodyString() {
        // Given
        HttpRequestHeader header = mock(HttpRequestHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(HttpHeader.GZIP);
        HttpRequestBody body = mock(HttpRequestBody.class);
        HttpMessage message = new HttpMessage(header, body);
        // When
        message.setRequestBody("Body");
        // Then
        assertThat(encodings(body), is(not(empty())));
    }

    @Test
    void shouldNotSetContentEncodingsWhenSettingHttpResponseBody() {
        // Given
        HttpResponseHeader header = mock(HttpResponseHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(HttpHeader.GZIP);
        HttpResponseBody body = mock(HttpResponseBody.class);
        HttpMessage message =
                new HttpMessage(
                        mock(HttpRequestHeader.class), mock(HttpRequestBody.class), header, body);
        // When
        message.setResponseBody(body);
        // Then
        verify(body, times(0)).setContentEncodings(any());
    }

    @Test
    void shouldSetContentEncodingsWhenSettingResponseBodyByte() {
        // Given
        HttpResponseHeader header = mock(HttpResponseHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(HttpHeader.GZIP);
        HttpResponseBody body = mock(HttpResponseBody.class);
        HttpMessage message =
                new HttpMessage(
                        mock(HttpRequestHeader.class), mock(HttpRequestBody.class), header, body);
        // When
        message.setResponseBody(new byte[0]);
        // Then
        assertThat(encodings(body), is(not(empty())));
    }

    @Test
    void shouldSetContentEncodingsWhenSettingResponseBodyString() {
        // Given
        HttpResponseHeader header = mock(HttpResponseHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(HttpHeader.GZIP);
        HttpResponseBody body = mock(HttpResponseBody.class);
        HttpMessage message =
                new HttpMessage(
                        mock(HttpRequestHeader.class), mock(HttpRequestBody.class), header, body);
        // When
        message.setResponseBody("Body");
        // Then
        assertThat(encodings(body), is(not(empty())));
    }

    @ParameterizedTest
    @ValueSource(strings = {HttpHeader.GZIP, "x-gzip"})
    void shouldSetGzipEncodingToBody(String contentEncodingHeader) {
        // Given
        HttpHeader header = mock(HttpHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncodingHeader);
        HttpBody body = mock(HttpBody.class);
        // When
        HttpMessage.setContentEncodings(header, body);
        // Then
        verify(body).setContentEncodings(asList(HttpEncodingGzip.getSingleton()));
    }

    @Test
    void shouldSetDeflateEncodingToBody() {
        // Given
        HttpHeader header = mock(HttpHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(HttpHeader.DEFLATE);
        HttpBody body = mock(HttpBody.class);
        // When
        HttpMessage.setContentEncodings(header, body);
        // Then
        verify(body).setContentEncodings(asList(HttpEncodingDeflate.getSingleton()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotSetContentEncodingToBodyIfContentEncodingIsNotPresentOrIsEmpty(
            String contentEncoding) {
        // Given
        HttpHeader header = mock(HttpHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn(contentEncoding);
        HttpBody body = mock(HttpBody.class);
        // When
        HttpMessage.setContentEncodings(header, body);
        // Then
        verify(body).setContentEncodings(Collections.emptyList());
    }

    @Test
    void shouldNotSetContentEncodingToBodyIfContentEncodingNotSupported() {
        // Given
        HttpHeader header = mock(HttpHeader.class);
        given(header.getHeader(HttpHeader.CONTENT_ENCODING)).willReturn("Encoding Not Supported");
        HttpBody body = mock(HttpBody.class);
        // When
        HttpMessage.setContentEncodings(header, body);
        // Then
        verify(body).setContentEncodings(Collections.emptyList());
    }

    private static HttpMessage newHttpMessage() throws Exception {
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n\r\n"),
                        new HttpRequestBody("Request Body"),
                        new HttpResponseHeader("HTTP/1.1 200 OK\r\n"),
                        new HttpResponseBody("Response Body"));
        message.setResponseFromTargetHost(true);
        message.setUserObject(new Object());
        message.setTimeSentMillis(1234L);
        message.setTimeElapsedMillis(50);
        message.setNote("Some Note");
        message.setHistoryRef(mock(HistoryReference.class));
        message.setHttpSession(mock(HttpSession.class));
        message.setRequestingUser(mock(User.class));
        message.setForceIntercept(true);
        message.setResponseFromTargetHost(true);
        return message;
    }

    private static List<HttpEncoding> encodings(HttpBody body) {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<HttpEncoding>> arg = ArgumentCaptor.forClass(List.class);
        verify(body).setContentEncodings(arg.capture());
        return arg.getValue();
    }
}
