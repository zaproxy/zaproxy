/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.network.HttpBodyTestUtils;
import org.zaproxy.zap.network.HttpEncoding;

/** Unit test for {@link HttpBody}. */
class HttpBodyUnitTest extends HttpBodyTestUtils {

    @Test
    void shouldHaveZeroLengthByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        int length = httpBody.length();
        // Then
        assertThat(length, is(equalTo(0)));
    }

    @Test
    void shouldHaveEmptyByteContentByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        byte[] content = httpBody.getBytes();
        // Then
        assertThat(content, is(not(nullValue())));
        assertThat(content.length, is(equalTo(0)));
    }

    @Test
    void shouldHaveEmptyStringRepresentationByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(stringRepresentation, is(equalTo("")));
    }

    @Test
    void shouldCreateBodyWithNullByteArray() {
        // Given
        HttpBody httpBody = new HttpBodyImpl((byte[]) null);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(0)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(0)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldCreateBodyWithByteArray() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_BYTES_DEFAULT_CHARSET);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_DEFAULT_CHARSET)));
    }

    @Test
    void shouldCreateBodyWithNullString() {
        // Given
        HttpBody httpBody = new HttpBodyImpl((String) null);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(0)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(0)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldCreateBodyWithStringUsingDefaultCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_STRING);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_DEFAULT_CHARSET)));
    }

    @Test
    void shouldCreateBodyWithInitialCapacity() {
        // Given
        int initialCapacity = 1024;
        HttpBody httpBody = new HttpBodyImpl(initialCapacity);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(initialCapacity)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(initialCapacity)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldCreateBodyWithZeroLengthIfInitialCapacityIsNegative() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(-1);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(0)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(0)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldLimitInitialCapacityTo128kBytes() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(500000);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(LIMIT_INITIAL_CAPACITY)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(LIMIT_INITIAL_CAPACITY)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldDetermineCharsetByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        boolean determineCharset = httpBody.isDetermineCharset();
        // Then
        assertThat(determineCharset, is(equalTo(true)));
    }

    @Test
    void shouldSetDetermineCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setDetermineCharset(false);
        // Then
        assertThat(httpBody.isDetermineCharset(), is(equalTo(false)));
    }

    @Test
    void shouldDetermineCharsetIfSetAndHasNoCharset() {
        // Given
        HttpBodyImpl httpBody = new HttpBodyImpl();
        httpBody.setDetermineCharset(true);
        httpBody.setCharset(null);
        // When
        httpBody.setBody("X Y Z");
        // Then
        assertThat(httpBody.isDetermineCharsetCalled(), is(equalTo(true)));
    }

    @Test
    void shouldNotDetermineCharsetIfNotSet() {
        // Given
        HttpBodyImpl httpBody = new HttpBodyImpl();
        httpBody.setDetermineCharset(false);
        httpBody.setCharset(null);
        // When
        httpBody.setBody("X Y Z");
        // Then
        assertThat(httpBody.isDetermineCharsetCalled(), is(equalTo(false)));
    }

    @Test
    void shouldHaveIso8859CharsetByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        String charset = httpBody.getCharset();
        // Then
        assertThat(charset, is(equalTo(DEFAULT_CHARSET_NAME)));
    }

    @Test
    void shouldSetValidCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setCharset(UTF_8_NAME);
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    void shouldResetCharsetWithNullCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset(null);
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(DEFAULT_CHARSET_NAME)));
    }

    @Test
    void shouldResetCharsetWithEmptyCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset("");
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(DEFAULT_CHARSET_NAME)));
    }

    @Test
    void shouldIgnoreInvalidCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset("$_NotACharsetName");
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    void shouldIgnoreUnsupportedCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset("UnsupportedCharset-12345");
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    void shouldIgnoreAlreadySetCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset(UTF_8_NAME);
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    void shouldIgnoreNullBytesBodySet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.setBody((byte[]) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] {0})));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    void shouldSetContentEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        List<HttpEncoding> encodings = Arrays.asList(mock(HttpEncoding.class));
        // When
        httpBody.setContentEncodings(encodings);
        // Then
        assertThat(httpBody.getContentEncodings(), is(equalTo(encodings)));
    }

    @Test
    void shouldSetContentEncodingsAndCopyList() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        List<HttpEncoding> encodings = new ArrayList<>();
        encodings.add(mock(HttpEncoding.class));
        // When
        httpBody.setContentEncodings(encodings);
        encodings.add(mock(HttpEncoding.class));
        // Then
        assertThat(httpBody.getContentEncodings(), is(not(equalTo(encodings))));
        assertThat(httpBody.getContentEncodings(), hasSize(1));
    }

    @Test
    void shouldSetContentEncodingsAndNotAllowModificationsToReturnedList() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        // When / Then
        assertThrows(
                UnsupportedOperationException.class,
                () -> httpBody.getContentEncodings().add(mock(HttpEncoding.class)));
    }

    @Test
    void shouldSetEmptyContentEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        List<HttpEncoding> encodings = Collections.emptyList();
        // When
        httpBody.setContentEncodings(encodings);
        // Then
        assertThat(httpBody.getContentEncodings(), is(equalTo(encodings)));
    }

    @Test
    void shouldResetContentEncodingErrorsWhenSettingContentEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        List<HttpEncoding> encodings = Collections.emptyList();
        // When
        httpBody.setContentEncodings(encodings);
        // Then
        assertThat(httpBody.hasContentEncodingErrors(), is(equalTo(false)));
    }

    @Test
    void shouldToStringWithContentEncodingsSet() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpEncoding contentEncoding = mock(HttpEncoding.class);
        String bodyData = "ABC";
        byte[] encodedContent = bytes(bodyData);
        given(contentEncoding.decode(any())).willReturn(encodedContent);
        // When
        httpBody.toString(); // force the creation of the "old" string representation
        httpBody.setContentEncodings(asList(contentEncoding));
        // Then
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
    }

    @Test
    void shouldThrowExceptionWhenSettingNullContentEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        List<HttpEncoding> encodings = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> httpBody.setContentEncodings(encodings));
    }

    @Test
    void shouldThrowExceptionWhenSettingANullContentEncoding() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        List<HttpEncoding> encodings = Arrays.asList(mock(HttpEncoding.class), null);
        // When / Then
        assertThrows(NullPointerException.class, () -> httpBody.setContentEncodings(encodings));
    }

    @Test
    void shouldIgnoreNullStringBodySet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.setBody((String) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] {0})));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    void shouldSetBytesBodyUsingDefaultCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setBody(BODY_1_BYTES_DEFAULT_CHARSET);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_DEFAULT_CHARSET)));
    }

    @Test
    void shouldSetBytesBodyUsingDefaultCharsetAndNotContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpEncoding encoding = mock(HttpEncoding.class);
        httpBody.setContentEncodings(Arrays.asList(encoding));
        given(encoding.decode(BODY_1_BYTES_DEFAULT_CHARSET))
                .willReturn(BODY_1_BYTES_DEFAULT_CHARSET);
        // When
        httpBody.setBody(BODY_1_BYTES_DEFAULT_CHARSET);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_DEFAULT_CHARSET)));
        verify(encoding).decode(BODY_1_BYTES_DEFAULT_CHARSET);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldSetStringBodyUsingDefaultCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setBody(BODY_1_STRING);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_DEFAULT_CHARSET)));
    }

    @Test
    void shouldSetStringBodyUsingDefaultCharsetAndContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpEncoding encoding = mock(HttpEncoding.class);
        httpBody.setContentEncodings(Arrays.asList(encoding));
        given(encoding.encode(BODY_1_BYTES_DEFAULT_CHARSET))
                .willReturn(BODY_1_BYTES_DEFAULT_CHARSET);
        // When
        httpBody.setBody(BODY_1_STRING);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_DEFAULT_CHARSET)));
        verify(encoding).encode(BODY_1_BYTES_DEFAULT_CHARSET);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldSetBytesBodyUsingCharsetSet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setBody(BODY_1_BYTES_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_UTF_8)));
    }

    @Test
    void shouldSetBytesBodyUsingCharsetSetAndNotContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        HttpEncoding encoding = mock(HttpEncoding.class);
        httpBody.setContentEncodings(Arrays.asList(encoding));
        given(encoding.decode(BODY_1_BYTES_UTF_8)).willReturn(BODY_1_BYTES_UTF_8);
        // When
        httpBody.setBody(BODY_1_BYTES_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_UTF_8)));
        verify(encoding).decode(BODY_1_BYTES_UTF_8);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldSetStringBodyUsingCharsetSet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setBody(BODY_1_STRING_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_UTF_8)));
    }

    @Test
    void shouldSetStringBodyUsingCharsetSetAndContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        HttpEncoding encoding = mock(HttpEncoding.class);
        httpBody.setContentEncodings(Arrays.asList(encoding));
        given(encoding.encode(BODY_1_BYTES_UTF_8)).willReturn(BODY_1_BYTES_UTF_8);
        // When
        httpBody.setBody(BODY_1_STRING_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_UTF_8)));
        verify(encoding).encode(BODY_1_BYTES_UTF_8);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldIgnoreNullBytesBodyAppended() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.append((byte[]) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] {0})));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    void shouldIgnoreNullStringBodyAppended() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.append((String) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] {0})));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    void shouldAppendBytesBodyUsingDefaultCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_STRING);
        // When
        httpBody.append(BODY_2_BYTES_DEFAULT_CHARSET);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET)));
        assertThat(
                httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_DEFAULT_CHARSET)));
    }

    @Test
    void shouldAppendBytesBodyUsingDefaultCharsetAndNotContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_STRING);
        HttpEncoding encoding = mock(HttpEncoding.class);
        httpBody.setContentEncodings(Arrays.asList(encoding));
        given(encoding.decode(BODY_1_AND_2_BYTES_DEFAULT_CHARSET))
                .willReturn(BODY_1_AND_2_BYTES_DEFAULT_CHARSET);
        // When
        httpBody.append(BODY_2_BYTES_DEFAULT_CHARSET);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET)));
        assertThat(
                httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_DEFAULT_CHARSET)));
        verify(encoding).decode(BODY_1_AND_2_BYTES_DEFAULT_CHARSET);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldAppendStringBodyUsingDefaultCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_STRING);
        // When
        httpBody.append(BODY_2_STRING);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET)));
        assertThat(
                httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_DEFAULT_CHARSET)));
    }

    @Test
    void shouldAppendStringBodyUsingDefaultCharsetAndContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_STRING);
        HttpEncoding encoding = mock(HttpEncoding.class);
        given(encoding.decode(BODY_1_BYTES_DEFAULT_CHARSET))
                .willReturn(BODY_1_BYTES_DEFAULT_CHARSET);
        given(encoding.encode(BODY_1_AND_2_BYTES_DEFAULT_CHARSET))
                .willReturn(BODY_1_AND_2_BYTES_DEFAULT_CHARSET);
        given(encoding.decode(BODY_1_AND_2_BYTES_DEFAULT_CHARSET))
                .willReturn(BODY_1_AND_2_BYTES_DEFAULT_CHARSET);
        httpBody.setContentEncodings(asList(encoding));
        // When
        httpBody.append(BODY_2_STRING);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET)));
        assertThat(
                httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_DEFAULT_CHARSET)));
        verify(encoding).decode(BODY_1_BYTES_DEFAULT_CHARSET);
        verify(encoding).encode(BODY_1_AND_2_BYTES_DEFAULT_CHARSET);
        verify(encoding).decode(BODY_1_AND_2_BYTES_DEFAULT_CHARSET);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldAppendBytesBodyUsingCharsetSet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_BYTES_UTF_8);
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.append(BODY_2_BYTES_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_UTF_8)));
    }

    @Test
    void shouldAppendBytesBodyUsingCharsetSetAndNotContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_BYTES_UTF_8);
        httpBody.setCharset(UTF_8_NAME);
        HttpEncoding encoding = mock(HttpEncoding.class);
        httpBody.setContentEncodings(asList(encoding));
        given(encoding.decode(BODY_1_AND_2_BYTES_UTF_8)).willReturn(BODY_1_AND_2_BYTES_UTF_8);
        // When
        httpBody.append(BODY_2_BYTES_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_UTF_8)));
        verify(encoding).decode(BODY_1_AND_2_BYTES_UTF_8);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldAppendStringBodyUsingCharsetSet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_BYTES_UTF_8);
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.append(BODY_2_STRING_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_UTF_8)));
    }

    @Test
    void shouldAppendStringBodyUsingCharsetSetAndContentEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_BYTES_UTF_8);
        httpBody.setCharset(UTF_8_NAME);
        HttpEncoding encoding = mock(HttpEncoding.class);
        given(encoding.decode(BODY_1_BYTES_UTF_8)).willReturn(BODY_1_BYTES_UTF_8);
        given(encoding.encode(BODY_1_AND_2_BYTES_UTF_8)).willReturn(BODY_1_AND_2_BYTES_UTF_8);
        given(encoding.decode(BODY_1_AND_2_BYTES_UTF_8)).willReturn(BODY_1_AND_2_BYTES_UTF_8);
        httpBody.setContentEncodings(Arrays.asList(encoding));
        // When
        httpBody.append(BODY_2_STRING_UTF_8);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_UTF_8)));
        verify(encoding).decode(BODY_1_BYTES_UTF_8);
        verify(encoding).encode(BODY_1_AND_2_BYTES_UTF_8);
        verify(encoding).decode(BODY_1_AND_2_BYTES_UTF_8);
        verifyNoMoreInteractions(encoding);
    }

    @Test
    void shouldAppendFullByteArray() {
        // Given
        byte[] chunk = {0, 1, 2, 3, 4, 5};
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.append(chunk, chunk.length);
        // Then
        assertThat(httpBody.length(), is(equalTo(chunk.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(chunk)));
        assertThat(httpBody.getBytes().length, is(equalTo(chunk.length)));
        assertThat(httpBody.toString(), is(equalTo("\0\1\2\3\4\5")));
    }

    @Test
    void shouldAppendByteArrayChunk() {
        // Given
        byte[] bytes = {1, 2, 3, 4, 5};
        int chunkLen = 3;
        byte[] chunk = java.util.Arrays.copyOf(bytes, chunkLen);
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.append(bytes, chunkLen);
        // Then
        assertThat(httpBody.length(), is(equalTo(chunk.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(chunk)));
        assertThat(httpBody.getBytes().length, is(equalTo(chunk.length)));
        assertThat(httpBody.toString(), is(equalTo("\1\2\3")));
    }

    @Test
    void shouldAppendByteArrayToExistingData() {
        // Given
        byte[] bytes = {1, 2, 3, 4, 5, 6, 7};
        byte[] chunk = Arrays.copyOfRange(bytes, 3, bytes.length);
        HttpBody httpBody = new HttpBodyImpl(Arrays.copyOf(bytes, bytes.length - chunk.length));
        // When
        httpBody.append(chunk, chunk.length);
        // Then
        assertThat(httpBody.length(), is(equalTo(bytes.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(bytes)));
        assertThat(httpBody.getBytes().length, is(equalTo(bytes.length)));
        assertThat(httpBody.toString(), is(equalTo("\1\2\3\4\5\6\7")));
    }

    @Test
    void shouldAppendByteArrayToBodyWithHigherInitialCapacity() {
        // Given
        int initialCapacity = 10;
        byte[] chunk = {1, 2, 3, 4, 5};
        HttpBody httpBody = new HttpBodyImpl(initialCapacity);
        byte[] expectedBytes = Arrays.copyOf(chunk, initialCapacity);
        // When
        httpBody.append(chunk, chunk.length);
        // Then
        assertThat(httpBody.length(), is(equalTo(expectedBytes.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(expectedBytes)));
        assertThat(httpBody.getBytes().length, is(equalTo(expectedBytes.length)));
        assertThat(httpBody.toString(), is(equalTo("\1\2\3\4\5")));
    }

    @Test
    void shouldAppendByteArrayToBodyWithLowerInitialCapacity() {
        // Given
        byte[] chunk = {1, 2, 3, 4, 5};
        HttpBody httpBody = new HttpBodyImpl(3);
        // When
        httpBody.append(chunk, chunk.length);
        // Then
        assertThat(httpBody.length(), is(equalTo(chunk.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(chunk)));
        assertThat(httpBody.getBytes().length, is(equalTo(chunk.length)));
        assertThat(httpBody.toString(), is(equalTo("\1\2\3\4\5")));
    }

    @Test
    void shouldIgnoreAppendOfNullByteArray() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.append(null, 5);
        // Then
        assertThat(httpBody.length(), is(equalTo(0)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(0)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldIgnoreAppendOfByteArrayIfNegativeLength() {
        // Given
        byte[] chunk = {1, 2, 3, 4, 5};
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.append(chunk, -1);
        // Then
        assertThat(httpBody.length(), is(equalTo(0)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(0)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldApplyCharsetSetToStringRepresentation() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setCharset(UTF_8_NAME);
        httpBody.toString(); // force the creation of the "old" string representation
        httpBody.setBody(BODY_1_BYTES_UTF_8);
        // Then
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_UTF_8)));
    }

    @Test
    void shouldExpandBodyWithSetLength() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setLength(50);
        // Then
        assertThat(httpBody.length(), is(equalTo(50)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(50)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldTruncateBodyWithSetLength() {
        // Given
        byte[] body = {1, 2, 3, 4, 5};
        HttpBody httpBody = new HttpBodyImpl(body);
        byte[] expectedBytes = Arrays.copyOf(body, 3);
        // When
        httpBody.setLength(expectedBytes.length);
        // Then
        assertThat(httpBody.length(), is(equalTo(expectedBytes.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(expectedBytes)));
        assertThat(httpBody.getBytes().length, is(equalTo(expectedBytes.length)));
        assertThat(httpBody.toString(), is(equalTo("\1\2\3")));
    }

    @Test
    void shouldProduceSameStringRepresentationEvenIfBodyIsExpandedWithSetLength() {
        // Given
        byte[] body = {1, 2, 3, 4, 5};
        HttpBody httpBody = new HttpBodyImpl(body);
        byte[] expectedBytes = concatenate(body, new byte[] {0, 0});
        // When
        httpBody.setLength(expectedBytes.length);
        // Then
        assertThat(httpBody.length(), is(equalTo(expectedBytes.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(expectedBytes)));
        assertThat(httpBody.getBytes().length, is(equalTo(expectedBytes.length)));
        assertThat(httpBody.toString(), is(equalTo("\1\2\3\4\5")));
    }

    @Test
    void shouldIgnoreNegativeLengthSet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setLength(-1);
        // Then
        assertThat(httpBody.length(), is(equalTo(0)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(0)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldIgnoreSameLengthSet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(50);
        // When
        httpBody.setLength(50);
        // Then
        assertThat(httpBody.length(), is(equalTo(50)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(50)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldReturnSameInstanceStringRepresentationOnConsecutiveCalls() {
        // Given
        String body = " X Y Z ";
        HttpBody httpBody = new HttpBodyImpl(body);
        // When
        String string1 = httpBody.toString();
        String string2 = httpBody.toString();
        // Then
        assertThat(string1, is(equalTo(body)));
        assertThat(string2, is(sameInstance(string2)));
    }

    @Test
    void shouldGetContentDecoded() throws IOException {
        // Given
        String bodyData = "ABC";
        HttpBody httpBody = new HttpBodyImpl(bodyData);
        HttpEncoding contentEncoding = mock(HttpEncoding.class);
        byte[] decodedContent = bytes(bodyData);
        given(contentEncoding.decode(any())).willReturn(decodedContent);
        httpBody.setContentEncodings(asList(contentEncoding));
        // When
        byte[] content = httpBody.getContent();
        // Then
        assertThat(content, is(sameInstance(decodedContent)));
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
    }

    @Test
    void shouldGetSameDecodedContent() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl("");
        HttpEncoding contentEncoding = mock(HttpEncoding.class);
        given(contentEncoding.decode(any())).willReturn(bytes("ABC"));
        httpBody.setContentEncodings(asList(contentEncoding));
        // When
        byte[] content = httpBody.getContent();
        byte[] otherContent = httpBody.getContent();
        // Then
        assertThat(content, is(sameInstance(otherContent)));
    }

    @Test
    void shouldGetContentSameAsBytesIfNoContentEncoding() {
        // Given
        String body = " X Y Z ";
        HttpBody httpBody = new HttpBodyImpl(body);
        // When
        byte[] content = httpBody.getContent();
        // Then
        assertThat(content, is(sameInstance(httpBody.getBytes())));
    }

    @Test
    void shouldSetContentAndEncode() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpEncoding contentEncoding = mock(HttpEncoding.class);
        byte[] encodedContent = bytes("ABC");
        given(contentEncoding.encode(any())).willReturn(encodedContent);
        httpBody.setContentEncodings(asList(contentEncoding));
        String bodyData = "CBA";
        byte[] decodedContent = bytes(bodyData);
        // When
        httpBody.setContent(decodedContent);
        // Then
        assertThat(httpBody.getBytes(), is(equalTo(encodedContent)));
        assertThat(httpBody.getContent(), is(sameInstance(decodedContent)));
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
    }

    @Test
    void shouldReturnToStringForContentSet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        String bodyData = "ABC";
        byte[] content = bytes(bodyData);
        // When
        httpBody.toString(); // force the creation of the "old" string representation
        httpBody.setContent(content);
        // Then
        assertThat(httpBody.getBytes(), is(equalTo(content)));
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
    }

    @Test
    void shouldSetContentWithoutEncodingIfNoContentEncoding() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        String bodyData = "ABC";
        byte[] content = bytes(bodyData);
        // When
        httpBody.setContent(content);
        // Then
        assertThat(httpBody.getBytes(), is(equalTo(content)));
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
    }

    @Test
    void shouldNotSetContentIfNull() {
        // Given
        String bodyData = "ABC";
        HttpBody httpBody = new HttpBodyImpl(bodyData);
        // When
        httpBody.setContent(null);
        // Then
        assertThat(httpBody.getBytes(), is(equalTo(bytes(bodyData))));
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
    }

    @Test
    void shouldHandleContentEncodingErrorsWhenDecoding() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpEncoding contentEncoding = mock(HttpEncoding.class);
        given(contentEncoding.decode(any())).willThrow(IOException.class);
        httpBody.setContentEncodings(asList(contentEncoding));
        String bodyData = "CBA";
        byte[] bodyBytes = bytes(bodyData);
        // When
        httpBody.setBody(bodyBytes);
        // Then
        assertThat(httpBody.getBytes(), is(equalTo(bodyBytes)));
        assertThat(httpBody.getContent(), is(equalTo(bodyBytes)));
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
        assertThat(httpBody.hasContentEncodingErrors(), is(equalTo(true)));
    }

    @Test
    void shouldHandleContentEncodingErrorsWhenEncoding() throws IOException {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpEncoding contentEncoding = mock(HttpEncoding.class);
        given(contentEncoding.encode(any())).willThrow(IOException.class);
        httpBody.setContentEncodings(asList(contentEncoding));
        String bodyData = "CBA";
        byte[] bodyBytes = bytes(bodyData);
        // When
        httpBody.setBody(bodyData);
        // Then
        assertThat(httpBody.getBytes(), is(equalTo(bodyBytes)));
        assertThat(httpBody.getContent(), is(equalTo(bodyBytes)));
        assertThat(httpBody.toString(), is(equalTo(bodyData)));
        assertThat(httpBody.hasContentEncodingErrors(), is(equalTo(true)));
    }

    @Test
    void shouldNotBeEqualToNull() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        boolean equals = httpBody.equals(null);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldBeEqualToEqualHttpBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpBody otherHttpBody = new HttpBodyImpl();
        // When / Then
        assertThat(httpBody, is(equalTo(otherHttpBody)));
    }

    @Test
    void shouldBeEqualToEqualEncodings() {
        // Given
        List<HttpEncoding> encodings = Arrays.asList(mock(HttpEncoding.class));
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setContentEncodings(encodings);
        HttpBody otherHttpBody = new HttpBodyImpl();
        otherHttpBody.setContentEncodings(encodings);
        // When / Then
        assertThat(httpBody, is(equalTo(otherHttpBody)));
    }

    @Test
    void shouldBeEqualToEqualHttpBodyAndEncodings() {
        // Given
        List<HttpEncoding> encodings = Arrays.asList(mock(HttpEncoding.class));
        HttpBody httpBody = new HttpBodyImpl("Body");
        httpBody.setContentEncodings(encodings);
        HttpBody otherHttpBody = new HttpBodyImpl("Body");
        otherHttpBody.setContentEncodings(encodings);
        // When / Then
        assertThat(httpBody, is(equalTo(otherHttpBody)));
    }

    @Test
    void shouldBeEqualToSameInstance() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When / Then
        assertThat(httpBody, is(equalTo(httpBody)));
    }

    @Test
    void shouldNotBeEqualToDifferentHttpBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpBody otherDifferentHttpBody = new HttpBodyImpl("Different Contents");
        // When / Then
        assertThat(httpBody, is(not(equalTo(otherDifferentHttpBody))));
    }

    @Test
    void shouldNotBeEqualToDifferentEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("Body");
        httpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        HttpBody otherDifferentHttpBody = new HttpBodyImpl("Body");
        otherDifferentHttpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        // When / Then
        assertThat(httpBody, is(not(equalTo(otherDifferentHttpBody))));
    }

    @Test
    void shouldNotBeEqualToDifferentHttpBodyAndEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("Body");
        httpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        HttpBody otherDifferentHttpBody = new HttpBodyImpl("Different Contents");
        otherDifferentHttpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        // When / Then
        assertThat(httpBody, is(not(equalTo(otherDifferentHttpBody))));
    }

    @Test
    void shouldNotBeEqualToDifferentHttpBodyImplementation() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpBody otherHttpBodyImplementation = new HttpBody() {};
        // When / Then
        assertThat(httpBody, is(not(equalTo(otherHttpBodyImplementation))));
    }

    @Test
    void shouldProduceSameHashCodeForEqualBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("X A");
        HttpBody otherHttpBody = new HttpBodyImpl("X A");
        // When / Then
        assertThat(httpBody.hashCode(), is(equalTo(otherHttpBody.hashCode())));
    }

    @Test
    void shouldProduceSameHashCodeForEqualBodyAndEncodings() {
        // Given
        List<HttpEncoding> encodings = Arrays.asList(mock(HttpEncoding.class));
        HttpBody httpBody = new HttpBodyImpl("X A");
        httpBody.setContentEncodings(encodings);
        HttpBody otherHttpBody = new HttpBodyImpl("X A");
        otherHttpBody.setContentEncodings(encodings);
        // When / Then
        assertThat(httpBody.hashCode(), is(equalTo(otherHttpBody.hashCode())));
    }

    @Test
    void shouldProduceDifferentHashCodeFromDifferentBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("_ X A 1");
        HttpBody otherHttpBody = new HttpBodyImpl("X A 2");
        // When / Then
        assertThat(httpBody.hashCode(), is(not(equalTo(otherHttpBody.hashCode()))));
    }

    @Test
    void shouldProduceDifferentHashCodeFromDifferentEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("X A");
        httpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        HttpBody otherHttpBody = new HttpBodyImpl("X A");
        otherHttpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        // When / Then
        assertThat(httpBody.hashCode(), is(not(equalTo(otherHttpBody.hashCode()))));
    }

    @Test
    void shouldProduceDifferentHashCodeFromDifferentBodyAndEncodings() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("_ X A 1");
        httpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        HttpBody otherHttpBody = new HttpBodyImpl("X A 2");
        otherHttpBody.setContentEncodings(Arrays.asList(mock(HttpEncoding.class)));
        // When / Then
        assertThat(httpBody.hashCode(), is(not(equalTo(otherHttpBody.hashCode()))));
    }

    private static byte[] bytes(String data) {
        return data.getBytes(StandardCharsets.US_ASCII);
    }

    private static class HttpBodyImpl extends HttpBody {

        private boolean determineCharsetCalled;

        HttpBodyImpl() {}

        HttpBodyImpl(int capacity) {
            super(capacity);
        }

        HttpBodyImpl(String data) {
            super(data);
        }

        HttpBodyImpl(byte[] data) {
            super(data);
        }

        @Override
        protected Charset determineCharset(String contents) {
            determineCharsetCalled = true;
            return super.determineCharset(contents);
        }

        boolean isDetermineCharsetCalled() {
            return determineCharsetCalled;
        }
    }
}
