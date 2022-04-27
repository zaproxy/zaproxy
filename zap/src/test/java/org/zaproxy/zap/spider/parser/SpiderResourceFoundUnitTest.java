/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.zap.spider.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link SpiderResourceFound}. */
@SuppressWarnings("deprecation")
class SpiderResourceFoundUnitTest {

    private SpiderResourceFound.Builder builder;

    @BeforeEach
    void setUp() {
        builder = SpiderResourceFound.builder();
    }

    @Test
    void shouldSetMessage() {
        // Given
        HttpMessage message = mock(HttpMessage.class);
        // When
        SpiderResourceFound resource = builder.setMessage(message).build();
        // Then
        assertThat(resource.getMessage(), is(equalTo(message)));
    }

    @Test
    void shouldSetShouldIgnore() {
        // Given
        boolean shouldIgnore = true;
        // When
        SpiderResourceFound resource = builder.setShouldIgnore(shouldIgnore).build();
        // Then
        assertThat(resource.isShouldIgnore(), is(equalTo(shouldIgnore)));
    }

    @Test
    void shouldSetUri() {
        // Given
        String uri = "uri";
        // When
        SpiderResourceFound resource = builder.setUri(uri).build();
        // Then
        assertThat(resource.getUri(), is(equalTo(uri)));
    }

    @Test
    void shouldThrowExceptionForNullUri() {
        // Given
        String uri = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> builder.setUri(uri));
    }

    @Test
    void shouldSetDepth() {
        // Given
        int depth = 42;
        // When
        SpiderResourceFound resource = builder.setDepth(depth).build();
        // Then
        assertThat(resource.getDepth(), is(equalTo(depth)));
    }

    @Test
    void shouldThrowExceptionForNegativeDepth() {
        // Given
        int depth = -99;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> builder.setDepth(depth));
    }

    @Test
    void shouldSetMethod() {
        // Given
        String method = "method";
        // When
        SpiderResourceFound resource = builder.setMethod(method).build();
        // Then
        assertThat(resource.getMethod(), is(equalTo(method)));
    }

    @Test
    void shouldThrowExceptionForNullMethod() {
        // Given
        String method = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> builder.setMethod(method));
    }

    @Test
    void shouldSetBody() {
        // Given
        String body = "body";
        // When
        SpiderResourceFound resource = builder.setBody(body).build();
        // Then
        assertThat(resource.getBody(), is(equalTo(body)));
    }

    @Test
    void shouldThrowExceptionForNullBody() {
        // Given
        String body = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> builder.setBody(body));
    }

    @Test
    void shouldThrowExceptionForNullHeaders() {
        // Given
        List<HttpHeaderField> headers = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> builder.setHeaders(headers));
    }

    @Test
    void shouldThrowExceptionForNullHeaderField() {
        // Given
        List<HttpHeaderField> headers = new ArrayList<>();
        headers.add(null);
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> builder.setHeaders(headers));
    }

    @Test
    void shouldSetHeaders() {
        // Given
        HttpHeaderField header1 = new HttpHeaderField("name 1", "value 1");
        HttpHeaderField header2 = new HttpHeaderField("name 2", "value 2");
        List<HttpHeaderField> headers = Arrays.asList(header1, header2);
        // When
        SpiderResourceFound resource = builder.setHeaders(headers).build();
        // Then
        assertThat(resource.getHeaders(), contains(header1, header2));
    }

    @Test
    void shouldSetEmptyHeaders() {
        // Given
        HttpHeaderField header1 = new HttpHeaderField("name 1", "value 1");
        HttpHeaderField header2 = new HttpHeaderField("name 2", "value 2");
        List<HttpHeaderField> headers = Arrays.asList(header1, header2);
        builder.setHeaders(headers);
        // When
        SpiderResourceFound resource = builder.setHeaders(Collections.emptyList()).build();
        // Then
        assertThat(resource.getHeaders(), is(empty()));
    }

    static Stream<Arguments> invalidHeadersProvider() {
        return Stream.of(
                arguments(null, "123"),
                arguments("", "123"),
                arguments(" ", "123"),
                arguments("name", null));
    }

    @ParameterizedTest
    @MethodSource("invalidHeadersProvider")
    void shouldSkipInvalidHeaders(String name, String value) {
        // Given
        HttpHeaderField header1 = new HttpHeaderField("name 1", "value 1");
        HttpHeaderField invalidHeader = new HttpHeaderField(name, value);
        HttpHeaderField header2 = new HttpHeaderField("name 2", "value 2");
        List<HttpHeaderField> headers = Arrays.asList(header1, invalidHeader, header2);
        // When
        SpiderResourceFound resource = builder.setHeaders(headers).build();
        // Then
        assertThat(resource.getHeaders(), contains(header1, header2));
    }
}
