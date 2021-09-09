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
package org.parosproxy.paros.network;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test for {@link HttpHeaderField}. */
class HttpHeaderFieldUnitTest {

    private static final String NAME = "name";
    private static final String VALUE = "value";

    static Stream<Arguments> constructorArgsProvider() {
        return Stream.of(
                arguments(NAME, VALUE),
                arguments(NAME, null),
                arguments(null, VALUE),
                arguments(null, null));
    }

    @ParameterizedTest
    @MethodSource("constructorArgsProvider")
    void shouldCreateHttpHeaderField(String name, String value) {
        // Given / When
        HttpHeaderField headerField = new HttpHeaderField(name, value);
        // Then
        assertThat(headerField.getName(), is(equalTo(name)));
        assertThat(headerField.getValue(), is(equalTo(value)));
    }

    static Stream<Arguments> hashCodesProvider() {
        return Stream.of(
                arguments(NAME, VALUE, 216558599),
                arguments(NAME, null, 104585878),
                arguments(null, VALUE, 111973682),
                arguments(null, null, 961));
    }

    @ParameterizedTest
    @MethodSource("hashCodesProvider")
    void shouldProduceConsistentHashCodes(String name, String value, int expectedHashCode) {
        // Given
        HttpHeaderField headerField = new HttpHeaderField(name, value);
        // When
        int hashCode = headerField.hashCode();
        // Then
        assertThat(hashCode, is(equalTo(expectedHashCode)));
    }

    @Test
    void shouldBeEqualToItself() {
        // Given
        HttpHeaderField headerField = new HttpHeaderField(NAME, VALUE);
        // When
        boolean equals = headerField.equals(headerField);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @ParameterizedTest
    @MethodSource("constructorArgsProvider")
    void shouldBeEqualToDifferentHttpHeaderFieldWithSameContents(String name, String value) {
        // Given
        HttpHeaderField headerField = new HttpHeaderField(name, value);
        HttpHeaderField otherEqualHttpHeaderField = new HttpHeaderField(name, value);
        // When
        boolean equals = headerField.equals(otherEqualHttpHeaderField);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    void shouldNotBeEqualToNull() {
        // Given
        HttpHeaderField headerField = new HttpHeaderField(NAME, VALUE);
        // When
        boolean equals = headerField.equals(null);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    static Stream<Arguments> differencesProvider() {
        return Stream.of(
                arguments(NAME, VALUE, "OtherName", VALUE),
                arguments(null, VALUE, NAME, VALUE),
                arguments(NAME, VALUE, NAME, "OtherValue"),
                arguments(NAME, null, NAME, VALUE));
    }

    @ParameterizedTest
    @MethodSource("differencesProvider")
    void shouldNotBeEqualToHttpHeaderFieldWithDifferentContents(
            String name, String value, String otherName, String otherValue) {
        // Given
        HttpHeaderField headerField = new HttpHeaderField(name, value);
        HttpHeaderField otherHttpHeaderField = new HttpHeaderField(otherName, otherValue);
        // When
        boolean equals = headerField.equals(otherHttpHeaderField);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldBeEqualToExtendedHttpHeaderField() {
        // Given
        HttpHeaderField headerField = new HttpHeaderField(NAME, VALUE);
        HttpHeaderField otherHttpHeaderField = new HttpHeaderField(NAME, VALUE) {
                    // Anonymous HttpHeaderField
                };
        // When
        boolean equals = headerField.equals(otherHttpHeaderField);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    static Stream<Arguments> stringsProvider() {
        return Stream.of(
                arguments(NAME, VALUE, "[Name=" + NAME + ", Value=" + VALUE + "]"),
                arguments(NAME, null, "[Name=" + NAME + "]"),
                arguments(null, VALUE, "[Value=" + VALUE + "]"),
                arguments(null, null, "[]"));
    }

    @ParameterizedTest
    @MethodSource("stringsProvider")
    void shouldProduceConsistentStringRepresentations(
            String name, String value, String expectedString) {
        // Given
        HttpHeaderField headerField = new HttpHeaderField(name, value);
        // When
        String toString = headerField.toString();
        // Then
        assertThat(toString, is(equalTo(expectedString)));
    }
}
