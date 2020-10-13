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
package org.zaproxy.zap.extension.anticsrf;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.htmlparser.jericho.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.network.HttpResponseBody;

/** Unit test for {@link ExtensionAntiCSRF}. */
class ExtensionAntiCSRFUnitTest {

    /**
     * Tests for {@link ExtensionAntiCSRF#getTokensFromResponse(HttpMessage)} and {@link
     * ExtensionAntiCSRF#getTokensFromResponse(HttpMessage, Source)}.
     */
    static class GetTokensFromResponse {

        private static final String NO_ID = null;
        private static final String NO_NAME = null;
        private static final String NO_VALUE = null;
        private static final String UNKOWN_TOKEN = "UnkownToken";
        private static final String KNOWN_TOKEN_1 = "AcsrfToken 1";
        private static final String KNOWN_TOKEN_2 = "AcsrfToken 2";
        private static final String KNOWN_TOKEN_3 = "AcsrfToken 3";

        private HttpMessage message;
        private AntiCsrfParam antiCsrfParam;
        private ExtensionAntiCSRF extensionAntiCSRF;

        @BeforeEach
        void setup() {
            message = mock(HttpMessage.class);

            extensionAntiCSRF = spy(new ExtensionAntiCSRF());
            antiCsrfParam = mock(AntiCsrfParam.class);
            extensionAntiCSRF.setParam(antiCsrfParam);
            given(antiCsrfParam.getTokensNames())
                    .willReturn(Arrays.asList(KNOWN_TOKEN_1, KNOWN_TOKEN_2, KNOWN_TOKEN_3));
        }

        @Test
        void shouldCreateSourceIfNotProvided() {
            // Given
            HttpResponseBody responseBody = mock(HttpResponseBody.class);
            given(responseBody.toString()).willReturn("Body Content");
            given(message.getResponseBody()).willReturn(responseBody);
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message);
            // Then
            verify(extensionAntiCSRF).getTokensFromResponse(eq(message), any(Source.class));
            assertThat(tokens, is(notNullValue()));
        }

        @Test
        void shouldNotGetTokensWithoutForms() {
            // Given
            Source source = new Source("");
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, is(empty()));
        }

        @Test
        void shouldNotGetTokensWithFormsButNoInputFields() {
            // Given
            Source source = createSource(form(), form());
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, is(empty()));
        }

        @Test
        void shouldNotGetTokensFromInputFieldsWithNoValue() {
            // Given
            Source source =
                    createSource(
                            form(input("id", "name", NO_VALUE), input("id", NO_NAME, NO_VALUE)),
                            form(input(NO_ID, NO_NAME, NO_VALUE)));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, is(empty()));
        }

        @Test
        void shouldNotGetTokensFromInputFieldsWithNoIdNorName() {
            // Given
            String input = input(NO_ID, NO_NAME, "value");
            Source source = createSource(form(input, input), form(input));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, is(empty()));
        }

        @Test
        void shouldNotGetTokensFromInputFieldsIfIdIsNotAKnownToken() {
            // Given
            String input = input(UNKOWN_TOKEN, NO_NAME, "value");
            Source source = createSource(form(input, input), form(input));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, is(empty()));
        }

        @Test
        void shouldNotGetTokensFromInputFieldsIfNameIsNotAKnownToken() {
            // Given
            String input = input(NO_ID, UNKOWN_TOKEN, "value");
            Source source = createSource(form(input, input), form(input));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, is(empty()));
        }

        @Test
        void shouldGetTokensFromInputFieldsIfIdIsAKnownToken() {
            // Given
            Source source =
                    createSource(
                            form(
                                    input(KNOWN_TOKEN_1, UNKOWN_TOKEN, "value1"),
                                    input(KNOWN_TOKEN_2, UNKOWN_TOKEN, "value2")),
                            form(input(KNOWN_TOKEN_3, UNKOWN_TOKEN, "value3")));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, hasSize(3));
            assertAntiCsrfToken(tokens.get(0), message, KNOWN_TOKEN_1, "value1", 0);
            assertAntiCsrfToken(tokens.get(1), message, KNOWN_TOKEN_2, "value2", 0);
            assertAntiCsrfToken(tokens.get(2), message, KNOWN_TOKEN_3, "value3", 1);
        }

        @Test
        void shouldGetTokensFromInputFieldsIfNameIsAKnownToken() {
            // Given
            Source source =
                    createSource(
                            form(
                                    input(UNKOWN_TOKEN, KNOWN_TOKEN_1, "value1"),
                                    input(UNKOWN_TOKEN, KNOWN_TOKEN_2, "value2")),
                            form(input(UNKOWN_TOKEN, KNOWN_TOKEN_3, "value3")));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, hasSize(3));
            assertAntiCsrfToken(tokens.get(0), message, KNOWN_TOKEN_1, "value1", 0);
            assertAntiCsrfToken(tokens.get(1), message, KNOWN_TOKEN_2, "value2", 0);
            assertAntiCsrfToken(tokens.get(2), message, KNOWN_TOKEN_3, "value3", 1);
        }

        @Test
        void shouldGetTokensFromInputFieldsIfIdAndNameIsAKnownToken() {
            // Given
            Source source =
                    createSource(
                            form(
                                    input(UNKOWN_TOKEN, KNOWN_TOKEN_1, "value1"),
                                    input(KNOWN_TOKEN_2, UNKOWN_TOKEN, "value2")),
                            form(input(UNKOWN_TOKEN, KNOWN_TOKEN_3, "value3")));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, hasSize(3));
            assertAntiCsrfToken(tokens.get(0), message, KNOWN_TOKEN_1, "value1", 0);
            assertAntiCsrfToken(tokens.get(1), message, KNOWN_TOKEN_2, "value2", 0);
            assertAntiCsrfToken(tokens.get(2), message, KNOWN_TOKEN_3, "value3", 1);
        }

        @Test
        void shouldGetJustOneTokenFromInputFieldIfNameAndIdAreAKnownToken() {
            // Given
            Source source = createSource(form(input(KNOWN_TOKEN_1, KNOWN_TOKEN_2, "value")));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, hasSize(1));
            assertAntiCsrfToken(tokens.get(0), message, KNOWN_TOKEN_1, "value", 0);
        }

        @Test
        void shouldGetTokensFromInputFieldsIfIdAndNameIsAKnownTokenWithDifferentCase() {
            // Given
            Source source =
                    createSource(
                            form(
                                    input(
                                            UNKOWN_TOKEN,
                                            KNOWN_TOKEN_1.toLowerCase(Locale.ROOT),
                                            "value1"),
                                    input(
                                            KNOWN_TOKEN_2.toLowerCase(Locale.ROOT),
                                            UNKOWN_TOKEN,
                                            "value2")));
            // When
            List<AntiCsrfToken> tokens = extensionAntiCSRF.getTokensFromResponse(message, source);
            // Then
            assertThat(tokens, hasSize(2));
            assertAntiCsrfToken(
                    tokens.get(0), message, KNOWN_TOKEN_1.toLowerCase(Locale.ROOT), "value1", 0);
            assertAntiCsrfToken(
                    tokens.get(1), message, KNOWN_TOKEN_2.toLowerCase(Locale.ROOT), "value2", 0);
        }
    }

    private static void assertAntiCsrfToken(
            AntiCsrfToken token, HttpMessage message, String name, String value, int formIndex) {
        assertThat(token.getMsg(), is(equalTo(message)));
        assertThat(token.getName(), is(equalTo(name)));
        assertThat(token.getValue(), is(equalTo(value)));
        assertThat(token.getFormIndex(), is(equalTo(formIndex)));
    }

    private static Source createSource(String... forms) {
        return new Source(String.join("\n", forms));
    }

    private static String form(String... fields) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<form>");
        if (fields != null && fields.length != 0) {
            strBuilder.append('\n').append(String.join("\n", fields));
        }
        strBuilder.append("\n</form>\n");
        return strBuilder.toString();
    }

    private static String input(String id, String name, String value) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("<input");
        if (id != null) {
            strBuilder.append(" id='").append(id).append('\'');
        }
        if (name != null) {
            strBuilder.append(" name='").append(name).append('\'');
        }
        if (value != null) {
            strBuilder.append(" value='").append(value).append('\'');
        }
        strBuilder.append(">");
        return strBuilder.toString();
    }
}
