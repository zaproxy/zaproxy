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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
        assertThat(parameters, is(empty()));
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
    void shouldNotExtractAnyParameterIfThereIsNoPathOrItIsEmpty(String path) {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath(path);
        // When
        variantUrlPath.setMessage(message);
        // Then
        assertThat(variantUrlPath.getParamList(), is(empty()));
    }

    @Test
    void shouldExtractParametersFromPathSegments() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/Y/Z/");
        // When
        variantUrlPath.setMessage(message);
        // Then
        assertThat(
                variantUrlPath.getParamList(),
                contains(parameter("X", 1), parameter("Y", 2), parameter("Z", 3)));
    }

    @Test
    void shouldExtractParametersFromEncodedPathSegments() {
        // Given
        VariantURLPath variantUrlPath = new VariantURLPath();
        HttpMessage message = createMessageWithPath("/X/+%2F%20/Z/%/%A/");
        // When
        variantUrlPath.setMessage(message);
        // Then
        assertThat(
                variantUrlPath.getParamList(),
                contains(
                        parameter("X", 1),
                        parameter("+/ ", 2),
                        parameter("Z", 3),
                        parameter("%", 4),
                        parameter("%A", 5)));
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
        assertThat(injectedValue, is(equalTo("Value")));
        assertThat(message, containsPath("/Value/Y/Z"));
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
        assertThat(injectedValue, is(equalTo("Value A")));
        assertThat(message, containsPath("/Value%20A/Y/Z"));
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
        assertThat(injectedValue, is(equalTo("Value%20A")));
        assertThat(message, containsPath("/Value%20A/Y/Z"));
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
        assertThat(injectedValue, is(equalTo(injection)));
        assertThat(message, containsPath("//Y/Z"));
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
        assertThat(injectedValue, is(equalTo("X")));
        assertThat(message, containsPath("/X/Y/Z"));
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

    private static Matcher<HttpMessage> containsPath(String path) {
        return new BaseMatcher<HttpMessage>() {

            @Override
            public boolean matches(Object actualValue) {
                HttpMessage message = (HttpMessage) actualValue;
                return path.equals(message.getRequestHeader().getURI().getEscapedPath());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("URL path ").appendValue(path);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                HttpMessage message = (HttpMessage) item;
                String path = message.getRequestHeader().getURI().getEscapedPath();
                if (path.isEmpty()) {
                    description.appendText("has no path");
                } else {
                    description.appendText("was ").appendValue(path);
                }
            }
        };
    }
}
