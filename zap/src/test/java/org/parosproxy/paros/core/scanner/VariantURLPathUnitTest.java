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
package org.parosproxy.paros.core.scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;

/** Unit test for {@link VariantURLPath}. */
class VariantURLPathUnitTest {

    @Test
    void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        // When
        List<NameValuePair> parameters = variantUrlPath.getParamList();
        // Then
        assertThat(parameters).isEmpty();
    }

    @Test
    void shouldFailToExtractParametersFromUndefinedMessage() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage undefinedMessage = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> variantUrlPath.setMessage(undefinedMessage));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    void shouldExtractEmptyParameterIfThereIsNoPathOrItIsEmpty(String path) {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath(path);
        // When
        variantUrlPath.setMessage(message);
        // Then
        assertThat(variantUrlPath.getParamList()).containsExactly(parameter("", 1));
    }

    @Test
    void shouldExtractParametersFromPathSegments() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        // When
        variantUrlPath.setMessage(message);
        // Then
        assertThat(variantUrlPath.getParamList())
                .containsExactly(
                        parameter("X", 1), parameter("Y", 2), parameter("Z", 3), parameter("", 4));
    }

    @Test
    void shouldExtractParametersFromEncodedPathSegments() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/+%2F%20/Z/%/%A/");
        // When
        variantUrlPath.setMessage(message);
        // Then
        assertThat(variantUrlPath.getParamList())
                .containsExactly(
                        parameter("X", 1),
                        parameter("+/ ", 2),
                        parameter("Z", 3),
                        parameter("%", 4),
                        parameter("%A", 5),
                        parameter("", 6));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "/"})
    void shouldInjectSegmentModificationToNoPathAndEmptyPath(String path) {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath(path);
        variantUrlPath.setMessage(message);
        // When
        String injectedValue = variantUrlPath.setParameter(message, parameter("", 1), "", "Value");
        // Then
        assertThat(injectedValue).isEqualTo("Value");
        assertThat(message).is(containsPath("/Value"));
    }

    @Test
    void shouldInjectSegmentModification() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        String injectedValue =
                variantUrlPath.setParameter(message, parameter("X", 1), "X", "Value");
        // Then
        assertThat(injectedValue).isEqualTo("Value");
        assertThat(message).is(containsPath("/Value/Y/Z"));
    }

    @Test
    void shouldInjectAndEscapeSegmentModification() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        String injectedValue =
                variantUrlPath.setParameter(message, parameter("X", 1), "X", "Value A");
        // Then
        assertThat(injectedValue).isEqualTo("Value A");
        assertThat(message).is(containsPath("/Value%20A/Y/Z"));
    }

    @Test
    void shouldInjectEscapedSegmentModification() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        String injectedValue =
                variantUrlPath.setEscapedParameter(message, parameter("X", 1), "X", "Value%20A");
        // Then
        assertThat(injectedValue).isEqualTo("Value%20A");
        assertThat(message).is(containsPath("/Value%20A/Y/Z"));
    }

    @Test
    void shouldInjectAndEscapeLastSegmentModification() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        String injectedValue =
                variantUrlPath.setParameter(message, parameter("", 4), "", "Value A");
        // Then
        assertThat(injectedValue).isEqualTo("Value A");
        assertThat(message).is(containsPath("/X/Y/Z/Value%20A"));
    }

    @Test
    void shouldInjectEscapedLastSegmentModification() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        String injectedValue =
                variantUrlPath.setEscapedParameter(message, parameter("", 4), "", "Value%20A");
        // Then
        assertThat(injectedValue).isEqualTo("Value%20A");
        assertThat(message).is(containsPath("/X/Y/Z/Value%20A"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldKeepEmptySegmentIfInjectedValueIsNullOrEmpty(String injection) {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        String injectedValue =
                variantUrlPath.setParameter(message, parameter("X", 1), "X", injection);
        // Then
        assertThat(injectedValue).isEqualTo(injection);
        assertThat(message).is(containsPath("//Y/Z"));
    }

    @Test
    void shouldIgnoreChangesToSegmentName() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        String injectedValue =
                variantUrlPath.setParameter(message, parameter("X", 1), "X-Y-Z", "X");
        // Then
        assertThat(injectedValue).isEqualTo("X");
        assertThat(message).is(containsPath("/X/Y/Z"));
    }

    @Test
    void shouldThrowExceptionOnIllegalPosition() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        variantUrlPath.setMessage(message);
        // When
        Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                variantUrlPath.setParameter(
                                        message, parameter("", 5), "", "Illegal"));
        // Then
        assertThat(e.getClass()).isEqualTo(IllegalArgumentException.class);
        assertThat(e.getMessage()).isEqualTo("Invalid position 5");
    }

    private static HttpMessage createMessageWithPath(String path) {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET http://example.com" + path + " HTTP/1.1\r\n");
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static NameValuePair parameter(String value, int position) {
        return new NameValuePair(NameValuePair.TYPE_URL_PATH, value, value, position);
    }

    private static Condition<HttpMessage> containsPath(String path) {
        return new Condition<>(
                message -> path.equals(message.getRequestHeader().getURI().getEscapedPath()),
                "URL path %s",
                path);
    }
}
