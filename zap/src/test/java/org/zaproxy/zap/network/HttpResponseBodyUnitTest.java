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
package org.zaproxy.zap.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/** Unit test for {@link HttpResponseBody}. */
class HttpResponseBodyUnitTest extends HttpBodyTestUtils {

    private static final byte[] BODY_1_BYTES_UTF_16 =
            BODY_1_STRING.getBytes(StandardCharsets.UTF_16);

    @Test
    void shouldCreateBodyWithNullByteArray() {
        // Given
        HttpResponseBody httpBody = new HttpResponseBody((byte[]) null);
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
        HttpResponseBody httpBody = new HttpResponseBody(BODY_1_BYTES_DEFAULT_CHARSET);
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
        HttpResponseBody httpBody = new HttpResponseBody((String) null);
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
        HttpResponseBody httpBody = new HttpResponseBody(BODY_1_BYTES_DEFAULT_CHARSET);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_DEFAULT_CHARSET)));
    }

    @Test
    void shouldCreateBodyWithStringDeterminingItsCharset() {
        // Given
        HttpResponseBody httpBody = new HttpResponseBody(BODY_1_STRING);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_BYTES_UTF_8)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_BYTES_UTF_8.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_STRING_UTF_8)));
    }

    @Test
    void shouldCreateBodyWithInitialCapacity() {
        // Given
        int initialCapacity = 1024;
        HttpResponseBody httpBody = new HttpResponseBody(initialCapacity);
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
        HttpResponseBody httpBody = new HttpResponseBody(-1);
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
        HttpResponseBody httpBody = new HttpResponseBody(500000);
        // When / Then
        assertThat(httpBody.length(), is(equalTo(LIMIT_INITIAL_CAPACITY)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(allZeroBytes()));
        assertThat(httpBody.getBytes().length, is(equalTo(LIMIT_INITIAL_CAPACITY)));
        assertThat(httpBody.toString(), is(equalTo("")));
    }

    @Test
    void shouldHaveEmptyStringRepresentationByDefault() {
        // Given
        HttpResponseBody httpBody = new HttpResponseBody();
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(stringRepresentation, is(equalTo("")));
    }

    @Test
    void shouldProduceStringRepresentationWithCharsetSet() {
        // Given
        HttpResponseBody httpBody = new HttpResponseBody(BODY_1_BYTES_UTF_8);
        httpBody.setCharset(UTF_8_NAME);
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(stringRepresentation, is(equalTo(BODY_1_STRING_UTF_8)));
    }

    @Test
    void shouldHaveIso8859CharsetByDefault() {
        // Given
        HttpResponseBody httpBody = new HttpResponseBody();
        // When
        String charset = httpBody.getCharset();
        // Then
        assertThat(charset, is(equalTo(DEFAULT_CHARSET_NAME)));
    }

    @Test
    void shouldDefaultToIso8859CharsetIfCharsetOfContentsIsNotSetNorDefined() {
        // Given
        HttpResponseBody httpBody = new HttpResponseBody(BODY_1_BYTES_UTF_16);
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(DEFAULT_CHARSET_NAME)));
        assertThat(stringRepresentation, is(startsWith("þÿ"))); // Wrong contents...
    }

    @Test
    void shouldDefaultToUft8CharsetIfCharsetOfContentsIsNotSetNorDefinedButMatchUtf8() {
        // Given
        HttpResponseBody httpBody = new HttpResponseBody(BODY_1_BYTES_UTF_8);
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
        assertThat(stringRepresentation, is(equalTo(BODY_1_STRING_UTF_8)));
    }

    @Test
    void shouldDefaultToIso8859CharsetIfCharsetOfTheContentsIsDefinedButUnsupported() {
        // Given
        String contents = "<meta  charset='UnsupportedCharset-12345' />";
        HttpResponseBody httpBody = new HttpResponseBody(contents);
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(DEFAULT_CHARSET_NAME)));
        assertThat(stringRepresentation, is(equalTo(contents)));
    }

    @Test
    void shouldUseCharsetOfTheContentsIfDefinedAndSupported() {
        // Given
        String contents = "<meta  charset='UTF-8' />";
        HttpResponseBody httpBody = new HttpResponseBody(contents);
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
        assertThat(stringRepresentation, is(equalTo(contents)));
    }
}
