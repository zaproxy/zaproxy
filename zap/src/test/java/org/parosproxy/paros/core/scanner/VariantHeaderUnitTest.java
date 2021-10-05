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
import org.parosproxy.paros.network.HttpHeaderField;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;

/** Unit test for {@link VariantHeader}. */
class VariantHeaderUnitTest {

    @Test
    void shouldHaveParametersListEmptyByDefault() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        // When
        List<NameValuePair> parameters = variantHeader.getParamList();
        // Then
        assertThat(parameters, is(empty()));
    }

    @Test
    void shouldNotAllowToModifyReturnedParametersList() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        NameValuePair header = header("Name", "Value", 0);
        // When / Then
        assertThrows(
                UnsupportedOperationException.class,
                () -> variantHeader.getParamList().add(header));
    }

    @Test
    void shouldFailToExtractParametersFromUndefinedMessage() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage undefinedMessage = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class, () -> variantHeader.setMessage(undefinedMessage));
    }

    @Test
    void shouldNotExtractAnyParameterIfThereAreNoHeaders() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage messageWithHeaders = createMessageWithoutInjectableHeaders();
        // When
        variantHeader.setMessage(messageWithHeaders);
        // Then
        assertThat(variantHeader.getParamList(), is(empty()));
    }

    @Test
    void shouldNotExtractAnyParameterIfThereAreNoInjectableHeaders() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage messageWithHeaders =
                createMessageWithHeaders(
                        header(HttpRequestHeader.CONTENT_LENGTH, "A"),
                        header(HttpRequestHeader.PRAGMA, "1"),
                        header(HttpRequestHeader.CACHE_CONTROL, "B"),
                        header(HttpRequestHeader.COOKIE, "3"),
                        header(HttpRequestHeader.AUTHORIZATION, "C"),
                        header(HttpRequestHeader.PROXY_AUTHORIZATION, "5"),
                        header(HttpRequestHeader.CONNECTION, "D"),
                        header(HttpRequestHeader.PROXY_CONNECTION, "7"),
                        header(HttpRequestHeader.IF_MODIFIED_SINCE, "E"),
                        header(HttpRequestHeader.IF_NONE_MATCH, "9"),
                        header(HttpRequestHeader.X_CSRF_TOKEN, "F"),
                        header(HttpRequestHeader.X_CSRFTOKEN, "11"),
                        header(HttpRequestHeader.X_XSRF_TOKEN, "G"),
                        header(HttpRequestHeader.X_ZAP_SCAN_ID, "13"),
                        header(HttpRequestHeader.X_ZAP_REQUESTID, "H"),
                        header(HttpRequestHeader.X_SECURITY_PROXY, "15"));
        // When
        variantHeader.setMessage(messageWithHeaders);
        // Then
        assertThat(variantHeader.getParamList(), is(empty()));
    }

    @Test
    void shouldExtractParametersFromInjectableHeaders() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage messageWithHeaders =
                createMessageWithHeaders(
                        header("X-Header-A", "X"),
                        header("X-Header-B", "Y"),
                        header("X-Header-C", "Z"));
        // When
        variantHeader.setMessage(messageWithHeaders);
        // Then
        assertThat(variantHeader.getParamList().size(), is(equalTo(3)));
        assertThat(
                variantHeader.getParamList(),
                contains(
                        header("X-Header-A", "X", 0),
                        header("X-Header-B", "Y", 1),
                        header("X-Header-C", "Z", 2)));
    }

    @Test
    void shouldExtractParametersFromInjectableHeadersEvenIfThereAreNoInjectableHeaders() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage messageWithHeaders =
                createMessageWithHeaders(
                        header(HttpRequestHeader.CONTENT_LENGTH, "A"),
                        header("X-Header-A", "X"),
                        header(HttpRequestHeader.CONNECTION, "D"),
                        header("X-Header-B", "Y"),
                        header(HttpRequestHeader.PROXY_AUTHORIZATION, "5"),
                        header("X-Header-C", "Z"));
        // When
        variantHeader.setMessage(messageWithHeaders);
        // Then
        assertThat(variantHeader.getParamList().size(), is(equalTo(3)));
        assertThat(
                variantHeader.getParamList(),
                contains(
                        header("X-Header-A", "X", 0),
                        header("X-Header-B", "Y", 1),
                        header("X-Header-C", "Z", 2)));
    }

    @Test
    void shouldNotAccumulateExtractedParameters() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage messageWithHeaders =
                createMessageWithHeaders(
                        header("X-Header-A", "X"),
                        header("X-Header-B", "Y"),
                        header("X-Header-C", "Z"));
        HttpMessage otherMessageWithHeaders =
                createMessageWithHeaders(
                        header("X-Header-D", "1"),
                        header("X-Header-E", "2"),
                        header("X-Header-F", "3"));
        // When
        variantHeader.setMessage(messageWithHeaders);
        variantHeader.setMessage(otherMessageWithHeaders);
        // Then
        assertThat(variantHeader.getParamList().size(), is(equalTo(3)));
        assertThat(
                variantHeader.getParamList(),
                contains(
                        header("X-Header-D", "1", 0),
                        header("X-Header-E", "2", 1),
                        header("X-Header-F", "3", 2)));
    }

    @Test
    void shouldInjectHeaderValueModification() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage message =
                createMessageWithHeaders(
                        header("X-Header-A", "X"),
                        header("X-Header-B", "Y"),
                        header("X-Header-C", "Z"));
        variantHeader.setMessage(message);
        // When
        String injectedHeader =
                variantHeader.setParameter(
                        message, header("X-Header-A", "X", 0), "X-Header-A", "Value");
        // Then
        assertThat(injectedHeader, is(equalTo("X-Header-A: Value")));
        assertThat(message, containsHeader("X-Header-A", "Value"));
    }

    @Test
    void shouldRemoveHeaderIfInjectedHeaderValueIsNull() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage message =
                createMessageWithHeaders(
                        header("X-Header-A", "X"),
                        header("X-Header-B", "Y"),
                        header("X-Header-C", "Z"));
        variantHeader.setMessage(message);
        // When
        String injectedHeader =
                variantHeader.setParameter(
                        message, header("X-Header-A", "X", 0), "X-Header-A", null);
        // Then
        assertThat(injectedHeader, is(equalTo("")));
        assertThat(message.getRequestHeader().getHeader("X-Header-A"), is((String) null));
    }

    @Test
    void shouldIgnoreChangesToHeaderName() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage message =
                createMessageWithHeaders(
                        header("X-Header-A", "X"),
                        header("X-Header-B", "Y"),
                        header("X-Header-C", "Z"));
        variantHeader.setMessage(message);
        // When
        String injectedHeader =
                variantHeader.setParameter(
                        message, header("X-Header-A", "X", 0), "X-Header-Z", "X");
        // Then
        assertThat(injectedHeader, is(equalTo("X-Header-A: X")));
        assertThat(message, containsHeader("X-Header-A", "X"));
    }

    @Test
    void shouldHaveSameEffectInjectingEscapedHeaderValueModification() {
        // Given
        VariantHeader variantHeader = new VariantHeader();
        HttpMessage message =
                createMessageWithHeaders(
                        header("X-Header-A", "X"),
                        header("X-Header-B", "Y"),
                        header("X-Header-C", "Z"));
        variantHeader.setMessage(message);
        // When
        String injectedHeader =
                variantHeader.setEscapedParameter(
                        message, header("X-Header-A", "X", 0), "X-Header-A", "Value");
        // Then
        assertThat(injectedHeader, is(equalTo("X-Header-A: Value")));
        assertThat(message, containsHeader("X-Header-A", "Value"));
    }

    private static HttpMessage createMessageWithoutInjectableHeaders() {
        HttpMessage message = new HttpMessage();
        try {
            message.setRequestHeader("GET / HTTP/1.1\r\n");
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static HttpMessage createMessageWithHeaders(NameValuePair... headers) {
        HttpMessage message = new HttpMessage();
        try {
            StringBuilder requestHeaderBuilder = new StringBuilder("GET / HTTP/1.1\r\n");
            for (NameValuePair header : headers) {
                requestHeaderBuilder.append(header.getName());
                requestHeaderBuilder.append(": ");
                requestHeaderBuilder.append(header.getValue());
                requestHeaderBuilder.append("\r\n");
            }
            message.setRequestHeader(requestHeaderBuilder.toString());
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
        return message;
    }

    private static NameValuePair header(String name, String value) {
        return new NameValuePair(NameValuePair.TYPE_HEADER, name, value, 0);
    }

    private static NameValuePair header(String name, String value, int position) {
        return new NameValuePair(NameValuePair.TYPE_HEADER, name, value, position);
    }

    private static Matcher<HttpMessage> containsHeader(final String name, final String value) {
        return new BaseMatcher<HttpMessage>() {

            @Override
            public boolean matches(Object actualValue) {
                HttpMessage message = (HttpMessage) actualValue;
                List<HttpHeaderField> headers = message.getRequestHeader().getHeaders();
                if (headers.isEmpty()) {
                    return false;
                }

                for (HttpHeaderField header : headers) {
                    if (name.equals(header.getName()) && value.equals(header.getValue())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("header ").appendValue(name + ": " + value);
            }

            @Override
            public void describeMismatch(Object item, Description description) {
                HttpMessage message = (HttpMessage) item;
                List<HttpHeaderField> headers = message.getRequestHeader().getHeaders();
                if (headers.isEmpty()) {
                    description.appendText("has no headers");
                } else {
                    description.appendText("was ").appendValue(headers);
                }
            }
        };
    }
}
