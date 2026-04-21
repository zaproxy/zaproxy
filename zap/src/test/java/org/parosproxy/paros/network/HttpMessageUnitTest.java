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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage.HttpEncodingsHandler;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.network.HttpEncoding;
import org.zaproxy.zap.network.HttpEncodingDeflate;
import org.zaproxy.zap.network.HttpEncodingGzip;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;
import org.zaproxy.zap.testutils.Log4jTestAppender;
import org.zaproxy.zap.users.User;

/** Unit test for {@link HttpMessage}. */
class HttpMessageUnitTest {

    private Log4jTestAppender testAppender;

    @AfterEach
    void cleanUp() throws Exception {
        HttpMessage.setContentEncodingsHandler(null);
        HttpMessage.setCharsetProvider(null);
        HttpMessage.resetWarnedContentTypeValues();
        Configurator.reconfigure(getClass().getResource("/log4j2-test.properties").toURI());
    }

    @Test
    void shouldBeEventStreamIfRequestWithoutResponseAcceptsEventStream() throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader("GET / HTTP/1.1\r\nAccept: text/event-stream"));
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream).isTrue();
    }

    @Test
    void shouldNotBeEventStreamIfRequestWithoutResponseDoesNotAcceptJustEventStream()
            throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(new HttpRequestHeader("GET / HTTP/1.1\r\nAccept: */*"));
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream).isFalse();
    }

    @Test
    void shouldBeEventStreamIfResponseHasEventStreamContentType() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        message.getResponseHeader()
                .setHeader(HttpHeader.CONTENT_TYPE, "text/event-stream;charset=utf-8");
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream).isTrue();
    }

    @Test
    void shouldNotBeEventStreamIfResponseDoesNotHaveEventStreamContentType() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        message.getResponseHeader()
                .setHeader(HttpHeader.CONTENT_TYPE, "text/not-event-stream;charset=utf-8");
        // When
        boolean eventStream = message.isEventStream();
        // Then
        assertThat(eventStream).isFalse();
    }

    @Test
    void shouldCopyHttpMessage() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        // When
        HttpMessage copy = new HttpMessage(message);
        // Then
        assertThat(copy.getRequestHeader()).isNotSameAs(message.getRequestHeader());
        assertThat(copy.getRequestHeader()).hasToString(message.getRequestHeader().toString());
        assertThat(copy.getRequestBody()).isNotSameAs(message.getRequestBody());
        assertThat(copy.getRequestBody()).hasToString(message.getRequestBody().toString());
        assertThat(copy.getResponseHeader()).isNotSameAs(message.getResponseHeader());
        assertThat(copy.getResponseHeader()).hasToString(message.getResponseHeader().toString());
        assertThat(copy.getResponseBody()).isNotSameAs(message.getResponseBody());
        assertThat(copy.getResponseBody()).hasToString(message.getResponseBody().toString());

        assertThat(copy.getUserObject()).isSameAs(message.getUserObject());
        assertThat(copy.getTimeSentMillis()).isEqualTo(message.getTimeSentMillis());
        assertThat(copy.getTimeElapsedMillis()).isEqualTo(message.getTimeElapsedMillis());
        assertThat(copy.getNote()).isEqualTo(message.getNote());
        assertThat(copy.getHistoryRef()).isSameAs(message.getHistoryRef());
        assertThat(copy.getHttpSession()).isSameAs(message.getHttpSession());
        assertThat(copy.getRequestingUser()).isSameAs(message.getRequestingUser());
        assertThat(copy.isForceIntercept()).isEqualTo(message.isForceIntercept());
        assertThat(copy.isResponseFromTargetHost()).isEqualTo(message.isResponseFromTargetHost());
    }

    @Test
    void shouldCloneRequest() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        // When
        HttpMessage copy = message.cloneRequest();
        // Then
        assertThat(copy.getRequestHeader()).isNotSameAs(message.getRequestHeader());
        assertThat(copy.getRequestHeader()).hasToString(message.getRequestHeader().toString());
        assertThat(copy.getRequestBody()).isNotSameAs(message.getRequestBody());
        assertThat(copy.getRequestBody()).hasToString(message.getRequestBody().toString());
        assertThat(copy.getResponseHeader()).isNotSameAs(message.getResponseHeader());
        assertThat(copy.getResponseHeader().isEmpty()).isTrue();
        assertThat(copy.getResponseBody()).isNotSameAs(message.getResponseBody());
        assertThat(copy.getResponseBody().toString()).isEmpty();

        assertThat(copy.getUserObject()).isNull();
        assertThat(copy.getTimeSentMillis()).isEqualTo(0L);
        assertThat(copy.getTimeElapsedMillis()).isEqualTo(0);
        assertThat(copy.getNote()).isEmpty();
        assertThat(copy.getHistoryRef()).isNull();
        assertThat(copy.getHttpSession()).isNull();
        assertThat(copy.getRequestingUser()).isNull();
        assertThat(copy.isForceIntercept()).isFalse();
        assertThat(copy.isResponseFromTargetHost()).isFalse();
    }

    @Test
    void shouldCloneAll() throws Exception {
        // Given
        HttpMessage message = newHttpMessage();
        // When
        HttpMessage copy = message.cloneAll();
        // Then
        assertThat(copy.getRequestHeader()).isNotSameAs(message.getRequestHeader());
        assertThat(copy.getRequestHeader()).hasToString(message.getRequestHeader().toString());
        assertThat(copy.getRequestBody()).isNotSameAs(message.getRequestBody());
        assertThat(copy.getRequestBody()).hasToString(message.getRequestBody().toString());
        assertThat(copy.getResponseHeader()).isNotSameAs(message.getResponseHeader());
        assertThat(copy.getResponseHeader()).hasToString(message.getResponseHeader().toString());
        assertThat(copy.getResponseBody()).isNotSameAs(message.getResponseBody());
        assertThat(copy.getResponseBody()).hasToString(message.getResponseBody().toString());

        assertThat(copy.getUserObject()).isNull();
        assertThat(copy.getTimeSentMillis()).isEqualTo(0L);
        assertThat(copy.getTimeElapsedMillis()).isEqualTo(0);
        assertThat(copy.getNote()).isEmpty();
        assertThat(copy.getHistoryRef()).isNull();
        assertThat(copy.getHttpSession()).isNull();
        assertThat(copy.getRequestingUser()).isNull();
        assertThat(copy.isForceIntercept()).isFalse();
        assertThat(copy.isResponseFromTargetHost()).isFalse();
    }

    @Test
    void shouldCorrectlyMutateFromConnectHttpMethodWhenGenericPort() throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader(
                                "CONNECT "
                                        + "www.example.com:9000 HTTP/1.1\r\nHost: www.example.com:9000"));
        // When
        message.mutateHttpMethod(HttpRequestHeader.GET);
        // Then
        assertThat(message.getRequestHeader().getMethod()).isEqualTo(HttpRequestHeader.GET);
        assertThat(message.getRequestHeader().getURI()).hasToString("http://www.example.com:9000");
        assertThat(message.getRequestHeader().getHeader(HttpRequestHeader.HOST))
                .isEqualTo("www.example.com:9000");
    }

    @Test
    void shouldCorrectlyMutateFromConnectHttpMethodWhenHttpsPort() throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader(
                                "CONNECT "
                                        + "www.example.com:443 HTTP/1.1\r\nHost: www.example.com:443"));
        // When
        message.mutateHttpMethod(HttpRequestHeader.GET);
        // Then
        assertThat(message.getRequestHeader().getMethod()).isEqualTo(HttpRequestHeader.GET);
        assertThat(message.getRequestHeader().getURI()).hasToString("https://www.example.com:443");
        assertThat(message.getRequestHeader().getHeader(HttpRequestHeader.HOST))
                .isEqualTo("www.example.com:443");
    }

    @Test
    void shouldCorrectlyMutateToConnectHttpMethod() throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader(
                                "GET "
                                        + "http://www.example.com:9000/path HTTP/1.1\r\nHost: www.example.com:9000"));
        // When
        message.mutateHttpMethod(HttpRequestHeader.CONNECT);
        // Then
        assertThat(message.getRequestHeader().getMethod()).isEqualTo(HttpRequestHeader.CONNECT);
        assertThat(message.getRequestHeader().getURI()).hasToString("www.example.com:9000");
        assertThat(message.getRequestHeader().getHeader(HttpRequestHeader.HOST))
                .isEqualTo("www.example.com:9000");
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

    @ParameterizedTest
    @MethodSource(value = "getNonPostMethods")
    void shouldSetContentTypeWhenMutatingMethodToPost(String method) throws Exception {
        String baseUri = "http://www.example.com:9000/";
        String authority = new URI(baseUri, false).getAuthority();
        String urlParams = "param1=param1&param2=param2";
        // Given
        HttpMessage message;

        if (method.equals(HttpRequestHeader.CONNECT)) {
            message =
                    new HttpMessage(
                            new HttpRequestHeader(
                                    method + " " + authority + " HTTP/1.1\r\nHost: " + authority));
        } else {
            message =
                    new HttpMessage(
                            new HttpRequestHeader(
                                    method
                                            + " "
                                            + baseUri
                                            + "?"
                                            + urlParams
                                            + " HTTP/1.1\r\nHost: "
                                            + authority));
        }
        // When
        message.mutateHttpMethod(HttpRequestHeader.POST);
        // Then
        assertThat(message.getRequestHeader().getMethod()).isEqualTo(HttpRequestHeader.POST);
        assertThat(message.getRequestHeader().getURI().getURI())
                .isEqualTo(
                        method.equals(HttpRequestHeader.CONNECT) ? "http://" + authority : baseUri);
        assertThat(message.getRequestHeader().getHeader(HttpRequestHeader.CONTENT_TYPE))
                .isEqualTo(
                        method.equals(HttpRequestHeader.CONNECT)
                                ? null
                                : HttpRequestHeader.FORM_URLENCODED_CONTENT_TYPE);
        assertThat(message.getRequestBody())
                .hasToString(method.equals(HttpRequestHeader.CONNECT) ? "" : urlParams);
    }

    @ParameterizedTest
    @MethodSource(value = "getNonPostMethods")
    void shouldRemoveContentTypeWhenMutatingMethodFromPost(String method) throws Exception {
        String baseUri = "http://www.example.com:9000/";
        String authority = new URI(baseUri, false).getAuthority();
        String urlParams = "param1=param1&param2=param2";
        // Given
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader(
                                "POST " + baseUri + " HTTP/1.1\r\nHost: " + authority));
        message.setRequestBody(urlParams);
        // When
        message.mutateHttpMethod(method);
        // Then
        assertThat(message.getRequestHeader().getMethod()).isEqualTo(method);
        if (method.equals(HttpRequestHeader.CONNECT)) {
            assertThat(message.getRequestHeader().getURI())
                    .hasToString(new URI(baseUri, false).getAuthority());
        } else {
            assertThat(message.getRequestHeader().getURI()).hasToString(baseUri + "?" + urlParams);
        }
        assertThat(message.getRequestHeader().getHeader(HttpRequestHeader.CONTENT_TYPE)).isNull();
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
        assertThat(encodings(body)).isNotEmpty();
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
        assertThat(encodings(body)).isNotEmpty();
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
        assertThat(encodings(body)).isNotEmpty();
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
        assertThat(encodings(body)).isNotEmpty();
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

    @Test
    void shouldUseContentEncodingsHandlerSet() {
        // Given
        HttpEncodingsHandler handler = mock(HttpEncodingsHandler.class);
        HttpHeader header = mock(HttpHeader.class);
        HttpBody body = mock(HttpBody.class);
        // When
        HttpMessage.setContentEncodingsHandler(handler);
        HttpMessage.setContentEncodings(header, body);
        // Then
        verify(handler).handle(header, body);
    }

    @Test
    void shouldNotUseContentEncodingsHandlerOnceUnset() {
        // Given
        HttpEncodingsHandler handler = mock(HttpEncodingsHandler.class);
        HttpHeader header = mock(HttpHeader.class);
        HttpBody body = mock(HttpBody.class);
        // When
        HttpMessage.setContentEncodingsHandler(handler);
        HttpMessage.setContentEncodings(header, body);
        HttpMessage.setContentEncodingsHandler(null);
        HttpMessage.setContentEncodings(header, body);
        // Then
        verify(handler).handle(header, body);
    }

    @Test
    void
            shouldBeWebSocketUpgradeIfRequestConnectionHeaderContainsUpgradeAndUpgradeHeaderEqualsWebsocket()
                    throws Exception {
        // Given
        HttpMessage message =
                new HttpMessage(
                        new HttpRequestHeader(
                                "GET / HTTP/1.1\r\nConnection: keep-alive, Upgrade\r\nUpgrade: websocket"));
        // When
        boolean webSocketUpgrade = message.isWebSocketUpgrade();
        // Then
        assertThat(webSocketUpgrade).isTrue();
    }

    @Test
    void
            shouldBeWebSocketUpgradeIfResponseConnectionHeaderEqualsUpgradeAndUpgradeHeaderEqualsWebsocket()
                    throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 101 Switching Protocols\r\nConnection: Upgrade\r\nUpgrade: websocket"));
        // When
        boolean webSocketUpgrade = message.isWebSocketUpgrade();
        // Then
        assertThat(webSocketUpgrade).isTrue();
    }

    @Test
    void
            shouldBeWebSocketUpgradeIfResponseConnectionHeaderContainsUpgradeAndUpgradeHeaderEqualsWebsocket()
                    throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 101 Switching Protocols\r\nConnection: keep-alive, Upgrade\r\nUpgrade: websocket"));
        // When
        boolean webSocketUpgrade = message.isWebSocketUpgrade();
        // Then
        assertThat(webSocketUpgrade).isTrue();
    }

    @Test
    void shouldNotBeWebSocketUpgradeIfResponseConnectionHeaderMissUpgradeValue() throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 101 Switching Protocols\r\nConnection: keep-alive\r\nUpgrade: websocket"));
        // When
        boolean webSocketUpgrade = message.isWebSocketUpgrade();
        // Then
        assertThat(webSocketUpgrade).isFalse();
    }

    @Test
    void shouldNotBeWebSocketUpgradeIfResponseMissUpgradeHeader() throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 101 Switching Protocols\r\nConnection: keep-alive, Upgrade"));
        // When
        boolean webSocketUpgrade = message.isWebSocketUpgrade();
        // Then
        assertThat(webSocketUpgrade).isFalse();
    }

    @Test
    void shouldUseCharsetProviderWhenSettingRequestBody() {
        // Given
        HttpMessage.setCharsetProvider((header, body) -> "UTF-16BE");
        HttpMessage message = new HttpMessage();
        withLoggerAppender();
        // When
        message.setRequestBody("");
        // Then
        assertThat(message.getRequestBody().getCharset()).isEqualTo("UTF-16BE");
        assertThat(testAppender.getLogEvents()).hasSize(0);
    }

    @ParameterizedTest
    @MethodSource(value = "setBodyData")
    void shouldUseContentTypeCharsetWhenSettingRequestBody(
            String charset, String expectedCharset, String body, String expectedBody)
            throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setRequestHeader(
                new HttpRequestHeader(
                        "GET / HTTP/1.1\r\nContent-Type: text/plain; charset=" + charset));
        withLoggerAppender();
        // When
        message.setRequestBody(body);
        // Then
        assertThat(message.getRequestBody().getCharset()).isEqualTo(expectedCharset);
        assertThat(message.getRequestBody()).hasToString(expectedBody);
        assertThat(testAppender.getLogEvents()).hasSize(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "!~invalid~name~"})
    void shouldUseDefaultAndWarnOnUnknownCharsetWhenSettingRequestBody(String charset)
            throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setRequestHeader(
                new HttpRequestHeader(
                        "GET / HTTP/1.1\r\nContent-Type: text/plain; charset=" + charset));
        withLoggerAppender();
        // When
        message.setRequestBody("Body");
        // Then
        assertThat(message.getRequestBody().getCharset()).isEqualTo("ISO-8859-1");
        assertThat(message.getRequestBody()).hasToString("Body");
        assertThat(testAppender.getLogEvents()).hasSize(1);
        assertThat(testAppender.getLogEvents().get(0).getMessage())
                .contains("Failed to set charset");
    }

    @Test
    void shouldWarnOnceOnSameUnknownCharsetWhenSettingRequestBody() throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setRequestHeader(
                new HttpRequestHeader(
                        "GET / HTTP/1.1\r\nContent-Type: text/plain; charset=1st_unknown"));
        withLoggerAppender();
        // When
        message.setRequestBody("Body");
        message.setRequestBody("Body");
        message.setRequestHeader(
                new HttpRequestHeader(
                        "GET / HTTP/1.1\r\nContent-Type: text/plain; charset=2nd_unknown"));
        message.setRequestBody("Body");
        message.setRequestBody("Body");
        // Then
        assertThat(testAppender.getLogEvents()).hasSize(2);
        assertThat(testAppender.getLogEvents().get(0).getMessage())
                .contains("Failed to set charset")
                .contains("charset=1st_unknown");
        assertThat(testAppender.getLogEvents().get(1).getMessage())
                .contains("Failed to set charset")
                .contains("charset=2nd_unknown");
    }

    @Test
    void shouldWarnOnceAgainOnSameUnknownCharsetWhenSettingRequestBodyAfterResettingWarns()
            throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setRequestHeader(
                new HttpRequestHeader(
                        "GET / HTTP/1.1\r\nContent-Type: text/plain; charset=unknown"));
        withLoggerAppender();
        // When
        message.setRequestBody("Body");
        message.setRequestBody("Body");
        HttpMessage.resetWarnedContentTypeValues();
        message.setRequestBody("Body");
        message.setRequestBody("Body");
        // Then
        assertThat(testAppender.getLogEvents()).hasSize(2);
        assertThat(testAppender.getLogEvents().get(0).getMessage())
                .contains("Failed to set charset")
                .contains("charset=unknown");
        assertThat(testAppender.getLogEvents().get(1).getMessage())
                .contains("Failed to set charset")
                .contains("charset=unknown");
    }

    static Stream<Arguments> setBodyData() {
        String iso8851 = StandardCharsets.ISO_8859_1.name();
        String utf8 = StandardCharsets.UTF_8.name();
        return Stream.of(
                arguments(iso8851, iso8851, "J/ψ → VP", "J/????VP"),
                arguments(utf8, utf8, "J/ψ → VP", "J/ψ → VP"),
                // Check aliases work as well and don't cause warns
                arguments("ISO_8859-1:1987", iso8851, "J/ψ → VP", "J/????VP"),
                arguments("utf8", utf8, "J/ψ → VP", "J/ψ → VP"));
    }

    static Stream<Arguments> getNonPostMethods() {
        return Stream.of(HttpRequestHeader.METHODS)
                .filter(method -> !method.equals(HttpRequestHeader.POST))
                .map(Arguments::arguments);
    }

    @Test
    void shouldUseCharsetProviderWhenSettingResponseBody() {
        // Given
        HttpMessage.setCharsetProvider((header, body) -> "UTF-16LE");
        HttpMessage message = new HttpMessage();
        withLoggerAppender();
        // When
        message.setResponseBody("");
        // Then
        assertThat(message.getResponseBody().getCharset()).isEqualTo("UTF-16LE");
        assertThat(testAppender.getLogEvents()).hasSize(0);
    }

    @ParameterizedTest
    @MethodSource(value = "setBodyData")
    void shouldUseContentTypeCharsetWhenSettingResponseBody(
            String charset, String expectedCharset, String body, String expectedBody)
            throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=" + charset));
        withLoggerAppender();
        // When
        message.setResponseBody(body);
        // Then
        assertThat(message.getResponseBody().getCharset()).isEqualTo(expectedCharset);
        assertThat(message.getResponseBody()).hasToString(expectedBody);
        assertThat(testAppender.getLogEvents()).hasSize(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "!~invalid~name~"})
    void shouldUseDefaultAndWarnOnUnknownCharsetWhenSettingResponseBody(String charset)
            throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=" + charset));
        withLoggerAppender();
        // When
        message.setResponseBody("Body");
        // Then
        assertThat(message.getResponseBody().getCharset()).isEqualTo("UTF-8");
        assertThat(message.getResponseBody()).hasToString("Body");
        assertThat(testAppender.getLogEvents()).hasSize(1);
        assertThat(testAppender.getLogEvents().get(0).getMessage())
                .contains("Failed to set charset");
    }

    @Test
    void shouldWarnOnceOnSameUnknownCharsetWhenSettingResponseBody() throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=1st_unknown"));
        withLoggerAppender();
        // When
        message.setResponseBody("Body");
        message.setResponseBody("Body");
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=2nd_unknown"));
        message.setResponseBody("Body");
        message.setResponseBody("Body");
        // Then
        assertThat(testAppender.getLogEvents()).hasSize(2);
        assertThat(testAppender.getLogEvents().get(0).getMessage())
                .contains("Failed to set charset")
                .contains("charset=1st_unknown");
        assertThat(testAppender.getLogEvents().get(1).getMessage())
                .contains("Failed to set charset")
                .contains("charset=2nd_unknown");
    }

    @Test
    void shouldWarnOnceAgainOnSameUnknownCharsetWhenSettingResponseBodyAfterResettingWarns()
            throws Exception {
        // Given
        HttpMessage message = new HttpMessage();
        message.setResponseHeader(
                new HttpResponseHeader(
                        "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=unknown"));
        withLoggerAppender();
        // When
        message.setResponseBody("Body");
        message.setResponseBody("Body");
        HttpMessage.resetWarnedContentTypeValues();
        message.setResponseBody("Body");
        message.setResponseBody("Body");
        // Then
        assertThat(testAppender.getLogEvents()).hasSize(2);
        assertThat(testAppender.getLogEvents().get(0).getMessage())
                .contains("Failed to set charset")
                .contains("charset=unknown");
        assertThat(testAppender.getLogEvents().get(1).getMessage())
                .contains("Failed to set charset")
                .contains("charset=unknown");
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

    private void withLoggerAppender() {
        testAppender = new Log4jTestAppender();
        LoggerContext context = LoggerContext.getContext();
        Logger logger = context.getLogger(HttpMessage.class.getCanonicalName());
        context.getConfiguration().addLoggerAppender(logger, testAppender);
        logger.setLevel(Level.WARN);
    }
}
