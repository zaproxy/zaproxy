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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.zaproxy.zap.network.HttpBodyTestUtils;

/**
 * Unit test for {@link HttpBody}.
 */
public class HttpBodyUnitTest extends HttpBodyTestUtils {

    @Test
    public void shouldHaveZeroLengthByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        int length = httpBody.length();
        // Then
        assertThat(length, is(equalTo(0)));
    }

    @Test
    public void shouldHaveEmptyByteContentByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        byte[] content = httpBody.getBytes();
        // Then
        assertThat(content, is(not(nullValue())));
        assertThat(content.length, is(equalTo(0)));
    }

    @Test
    public void shouldHaveEmptyStringRepresentationByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        String stringRepresentation = httpBody.toString();
        // Then
        assertThat(stringRepresentation, is(equalTo("")));
    }

    @Test
    public void shouldCreateBodyWithNullByteArray() {
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
    public void shouldCreateBodyWithByteArray() {
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
    public void shouldCreateBodyWithNullString() {
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
    public void shouldCreateBodyWithStringUsingDefaultCharset() {
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
    public void shouldCreateBodyWithInitialCapacity() {
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
    public void shouldCreateBodyWithZeroLengthIfInitialCapacityIsNegative() {
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
    public void shouldLimitInitialCapacityTo128kBytes() {
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
    public void shouldHaveIso8859CharsetByDefault() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        String charset = httpBody.getCharset();
        // Then
        assertThat(charset, is(equalTo(DEFAULT_CHARSET_NAME)));
    }

    @Test
    public void shouldSetValidCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        httpBody.setCharset(UTF_8_NAME);
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    public void shouldResetCharsetWithNullCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset(null);
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(DEFAULT_CHARSET_NAME)));
    }

    @Test
    public void shouldResetCharsetWithEmptyCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset("");
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(DEFAULT_CHARSET_NAME)));
    }

    @Test
    public void shouldIgnoreInvalidCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset("$_NotACharsetName");
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    public void shouldIgnoreUnsupportedCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset("UnsupportedCharset-12345");
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    public void shouldIgnoreAlreadySetCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        httpBody.setCharset(UTF_8_NAME);
        // When
        httpBody.setCharset(UTF_8_NAME);
        // Then
        assertThat(httpBody.getCharset(), is(equalTo(UTF_8_NAME)));
    }

    @Test
    public void shouldIgnoreNullBytesBodySet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.setBody((byte[]) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] { 0 })));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    public void shouldIgnoreNullStringBodySet() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.setBody((String) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] { 0 })));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    public void shouldSetBytesBodyUsingDefaultCharset() {
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
    public void shouldSetStringBodyUsingDefaultCharset() {
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
    public void shouldSetBytesBodyUsingCharsetSet() {
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
    public void shouldSetStringBodyUsingCharsetSet() {
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
    public void shouldIgnoreNullBytesBodyAppended() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.append((byte[]) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] { 0 })));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    public void shouldIgnoreNullStringBodyAppended() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("\0");
        // When
        httpBody.append((String) null);
        // Then
        assertThat(httpBody.length(), is(equalTo(1)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(new byte[] { 0 })));
        assertThat(httpBody.getBytes().length, is(equalTo(1)));
        assertThat(httpBody.toString(), is(equalTo("\0")));
    }

    @Test
    public void shouldAppendBytesBodyUsingDefaultCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_STRING);
        // When
        httpBody.append(BODY_2_BYTES_DEFAULT_CHARSET);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_DEFAULT_CHARSET)));
    }

    @Test
    public void shouldAppendStringBodyUsingDefaultCharset() {
        // Given
        HttpBody httpBody = new HttpBodyImpl(BODY_1_STRING);
        // When
        httpBody.append(BODY_2_STRING);
        // Then
        assertThat(httpBody.length(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.getBytes(), is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET)));
        assertThat(httpBody.getBytes().length, is(equalTo(BODY_1_AND_2_BYTES_DEFAULT_CHARSET.length)));
        assertThat(httpBody.toString(), is(equalTo(BODY_1_AND_2_STRING_DEFAULT_CHARSET)));
    }

    @Test
    public void shouldAppendBytesBodyUsingCharsetSet() {
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
    public void shouldAppendStringBodyUsingCharsetSet() {
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
    public void shouldAppendFullByteArray() {
        // Given
        byte[] chunk = { 0, 1, 2, 3, 4, 5 };
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
    public void shouldAppendByteArrayChunk() {
        // Given
        byte[] bytes = { 1, 2, 3, 4, 5 };
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
    public void shouldAppendByteArrayToExistingData() {
        // Given
        byte[] bytes = { 1, 2, 3, 4, 5, 6, 7 };
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
    public void shouldAppendByteArrayToBodyWithHigherInitialCapacity() {
        // Given
        int initialCapacity = 10;
        byte[] chunk = { 1, 2, 3, 4, 5 };
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
    public void shouldAppendByteArrayToBodyWithLowerInitialCapacity() {
        // Given
        byte[] chunk = { 1, 2, 3, 4, 5 };
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
    public void shouldIgnoreAppendOfNullByteArray() {
        // Given
        byte[] chunk = { 1, 2, 3, 4, 5 };
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
    public void shouldIgnoreAppendOfByteArrayIfNegativeLength() {
        // Given
        byte[] chunk = { 1, 2, 3, 4, 5 };
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
    public void shouldApplyCharsetSetToStringRepresentation() {
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
    public void shouldExpandBodyWithSetLength() {
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
    public void shouldTruncateBodyWithSetLength() {
        // Given
        byte[] body = { 1, 2, 3, 4, 5 };
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
    public void shouldProduceSameStringRepresentationEvenIfBodyIsExpandedWithSetLength() {
        // Given
        byte[] body = { 1, 2, 3, 4, 5 };
        HttpBody httpBody = new HttpBodyImpl(body);
        byte[] expectedBytes = concatenate(body, new byte[] { 0, 0 });
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
    public void shouldIgnoreNegativeLengthSet() {
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
    public void shouldIgnoreSameLengthSet() {
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
    public void shouldReturnSameInstanceStringRepresentationOnConsecutiveCalls() {
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
    public void shouldNotBeEqualToNull() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When
        boolean equals = httpBody.equals(null);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    public void shouldBeEqualToEqualHttpBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpBody otherHttpBody = new HttpBodyImpl();
        // When / Then
        assertThat(httpBody, is(equalTo(otherHttpBody)));
    }

    @Test
    public void shouldBeEqualToSameInstance() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        // When / Then
        assertThat(httpBody, is(equalTo(httpBody)));
    }

    @Test
    public void shouldNotBeEqualToDifferentHttpBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpBody otherDifferentHttpBody = new HttpBodyImpl("Different Contents");
        // When / Then
        assertThat(httpBody, is(not(equalTo(otherDifferentHttpBody))));
    }

    @Test
    public void shouldNotBeEqualToDifferentHttpBodyImplementation() {
        // Given
        HttpBody httpBody = new HttpBodyImpl();
        HttpBody otherHttpBodyImplementation = new HttpBody() {
        };
        // When / Then
        assertThat(httpBody, is(not(equalTo(otherHttpBodyImplementation))));
    }

    @Test
    public void shouldProduceSameHashCodeForEqualBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("X A");
        HttpBody otherHttpBody = new HttpBodyImpl("X A");
        // When / Then
        assertThat(httpBody.hashCode(), is(equalTo(otherHttpBody.hashCode())));
    }

    @Test
    public void shouldProduceDifferentHashCodeFromDifferentBody() {
        // Given
        HttpBody httpBody = new HttpBodyImpl("_ X A 1");
        HttpBody otherHttpBody = new HttpBodyImpl("X A 2");
        // When / Then
        assertThat(httpBody.hashCode(), is(not(equalTo(otherHttpBody.hashCode()))));
    }

    private static class HttpBodyImpl extends HttpBody {

        public HttpBodyImpl() {
        }

        public HttpBodyImpl(int capacity) {
            super(capacity);
        }

        public HttpBodyImpl(String data) {
            super(data);
        }

        public HttpBodyImpl(byte[] data) {
            super(data);
        }
    }

}
