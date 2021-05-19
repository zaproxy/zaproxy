/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ByteBuilderUnitTest {

    private ByteBuilder byteBuilder;

    private static final byte[] TEST_ARRAY = {(byte) 1, (byte) 2, (byte) 3};

    @BeforeEach
    void setUp() throws Exception {
        byteBuilder = new ByteBuilder();
    }

    @Test
    void shouldHaveADefaultCapacityOf10() {
        // given
        byteBuilder = new ByteBuilder();
        // when
        int defaultCapacity = byteBuilder.capacity();
        // then
        assertThat(defaultCapacity, is(10));
    }

    @Test
    void shouldBeInitializedWithGivenCapacity() {
        // given
        byteBuilder = new ByteBuilder(42);
        // when
        int capacity = byteBuilder.capacity();
        // then
        assertThat(capacity, is(42));
    }

    @Test
    void shouldBeInitializedWithDoubleCapacityOfGivenArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        int capacity = byteBuilder.capacity();
        // then
        assertThat(capacity, is(2 * TEST_ARRAY.length));
    }

    @Test
    void shouldIncreaseCapacityWhenBiggerThanActual() {
        // given
        byteBuilder = new ByteBuilder(5);
        // when
        byteBuilder.ensureCapacity(20);
        int capacity = byteBuilder.capacity();
        // then
        assertThat(capacity, is(20));
    }

    @Test
    void shouldHaveZeroSizeByDefault() {
        // given
        byteBuilder = new ByteBuilder();
        // when
        int size = byteBuilder.size();
        // then
        assertThat(size, is(0));
    }

    @Test
    void shouldHaveSizeOfGivenArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        int size = byteBuilder.size();
        // then
        assertThat(size, is(3));
    }

    @Test
    void shouldProduceStringFromContents() {
        // given
        byteBuilder = new ByteBuilder(new byte[] {65, 45, 90});
        byteBuilder.ensureCapacity(50);
        // when
        String string = byteBuilder.toString();
        // then
        assertThat(string, is("A-Z"));
    }

    @Test
    void shouldHaveBytesOfSubSequence() {
        // given
        byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        byteBuilder = new ByteBuilder(bytes);
        // when
        byte[] subSequence = byteBuilder.subSequence(3, 6);
        // then
        assertThat(subSequence, is(new byte[] {4, 5, 6}));
    }

    @Test
    void shouldBeEqualToTheArrayPassed() {
        // given
        byte[] bytes = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        byteBuilder = new ByteBuilder(bytes);
        // when
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(bytes));
    }

    @Test
    void shouldAppendByteValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append((byte) 2);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 2}));
    }

    @Test
    void shouldAppendByteArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(new byte[] {1, 2});
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 1, 2}));
    }

    @Test
    void shouldAppendSpecificValuesOfByteArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(new byte[] {1, 2, 3, 4, 5, 6}, 1, 3);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 2, 3, 4}));
    }

    @Test
    void shouldThrowAnExceptionWhenAppendingValuesToEmptyByteArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // When / Then
        assertThrows(
                ArrayIndexOutOfBoundsException.class, () -> byteBuilder.append(new byte[0], 2, 3));
    }

    @Test
    void shouldAppendCharValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append('3');
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 51}));
    }

    @Test
    void shouldAppendCharArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(new char[] {'3', '5'});
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 51, 53}));
    }

    @Test
    void shouldAppendSpecificValuesOfCharArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(new char[] {'1', '2', '3', '4', '5', '6'}, 1, 3);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 50, 51, 52}));
    }

    @Test
    void shouldThrowAnExceptionWhenAppendingValuesToEmptyCharArray() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // When / Then
        assertThrows(
                ArrayIndexOutOfBoundsException.class, () -> byteBuilder.append(new char[0], 2, 3));
    }

    @Test
    void shouldAppendBooleanValues() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(true);
        byteBuilder.append(false);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 1, 0}));
    }

    @Test
    void shouldAppendShortValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append((short) 3);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 0, 3}));
    }

    @Test
    void shouldAppendIntValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(2);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 0, 0, 0, 2}));
    }

    @Test
    void shouldAppendLongValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(1234567890123456789L);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 17, 34, 16, -12, 125, -23, -127, 21}));
    }

    @Test
    void shouldAppendFloatValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(1234567890123456789.12f);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 93, -119, 16, -120}));
    }

    @Test
    void shouldAppendDoubleValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(1234567890123456789.12);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 67, -79, 34, 16, -12, 125, -23, -127}));
    }

    @Test
    void shouldAppendNewObjectValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        class FooClass {
            public byte[] toByteStructure() {
                return new byte[] {1, 2};
            }
        }
        byteBuilder.append(new FooClass());
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 1, 2}));
    }

    @Test
    void shouldAppendObjectValue() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append((Object) 123456);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 0, 0, 0, 6, 49, 50, 51, 52, 53, 54}));
    }

    @Test
    void shouldAppendStringValueWithGivenCharset() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append("FooBarBaz£$%^&*", StandardCharsets.UTF_8);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(
                toByteArray,
                is(
                        new byte[] {
                            1, 2, 3, 0, 0, 0, 16, 70, 111, 111, 66, 97, 114, 66, 97, 122, -62, -93,
                            36, 37, 94, 38, 42
                        }));
    }

    @Test
    void shouldAppendStringValueWithDefaultCharsetByDefault() {
        // Given
        byteBuilder = new ByteBuilder();
        String value = "FooBarBaz£$%^&*";
        // When
        byteBuilder.append(value);
        // Then
        assertThat(byteBuilder.toByteArray(), is(lengthAndBytesOf(value)));
    }

    @Test
    void shouldAppendStringBufferWithGivenCharset() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(new StringBuffer("FooBarBaz£$%^&*"), StandardCharsets.UTF_8);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(
                toByteArray,
                is(
                        new byte[] {
                            1, 2, 3, 0, 0, 0, 16, 70, 111, 111, 66, 97, 114, 66, 97, 122, -62, -93,
                            36, 37, 94, 38, 42
                        }));
    }

    @Test
    void shouldAppendStringBufferWithDefaultCharsetByDefault() {
        // given
        byteBuilder = new ByteBuilder();
        String value = "FooBarBaz£$%^&*";
        // when
        byteBuilder.append(new StringBuffer(value));
        // then
        assertThat(byteBuilder.toByteArray(), is(lengthAndBytesOf(value)));
    }

    @Test
    void shouldAppendByteBuilder() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.append(new ByteBuilder(new byte[] {5, 5}));
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 5, 5}));
    }

    @Test
    void shouldAppendSpecialPositiveLong() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.appendSpecial(1234567890123456789L, 3, true);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, 105, -127, 21}));
    }

    @Test
    void shouldAppendSpecialNegativeLong() {
        // given
        byteBuilder = new ByteBuilder(TEST_ARRAY);
        // when
        byteBuilder.appendSpecial(-1234567890123456789L, 3, true);
        byte[] toByteArray = byteBuilder.toByteArray();
        // then
        assertThat(toByteArray, is(new byte[] {1, 2, 3, -106, 126, -21}));
    }

    private static byte[] lengthAndBytesOf(String value) {
        byte[] bytes = value.getBytes(Charset.defaultCharset());
        byte[] result = new byte[4 + bytes.length];
        result[3] = (byte) bytes.length;
        System.arraycopy(bytes, 0, result, 4, bytes.length);
        return result;
    }
}
