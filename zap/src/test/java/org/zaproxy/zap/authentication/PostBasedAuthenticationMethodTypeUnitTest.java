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
package org.zaproxy.zap.authentication;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.NameValuePair;
import org.zaproxy.zap.model.StandardParameterParser;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link PostBasedAuthenticationMethodType}. */
class PostBasedAuthenticationMethodTypeUnitTest {

    /**
     * Test {@link
     * PostBasedAuthenticationMethodType#replaceAntiCsrfTokenValueIfRequired(HttpMessage,
     * HttpMessage, UnaryOperator)}.
     */
    static class ReplaceAntiCsrfTokenValueIfRequired {

        private HttpMessage requestMessage;
        private User user;
        private Context context;
        private StandardParameterParser postParamParser;
        private HttpRequestBody requestMessageBody;
        private HttpMessage loginMsgWithFreshAcsrfToken;
        private Encoder paramEncoder;

        private ExtensionAntiCSRF extAntiCsrf;

        @BeforeEach
        void setup() {
            Constant.messages = mock(I18N.class);
            Control.initSingletonForTesting();

            requestMessage = mock(HttpMessage.class);
            user = mock(User.class);
            given(requestMessage.getRequestingUser()).willReturn(user);
            context = mock(Context.class);
            given(user.getContext()).willReturn(context);
            postParamParser = mock(StandardParameterParser.class);
            given(context.getPostParamParser()).willReturn(postParamParser);

            requestMessageBody = spy(new HttpRequestBody());
            given(requestMessage.getRequestBody()).willReturn(requestMessageBody);

            loginMsgWithFreshAcsrfToken = mock(HttpMessage.class);

            paramEncoder = spy(Encoder.class);

            PostBasedAuthenticationMethodType.setExtAntiCsrf(null);
            extAntiCsrf = mock(ExtensionAntiCSRF.class);
        }

        @Test
        void shouldNotReplaceAnyTokensIfExtensionAntiCSRFNotEnabled() {
            // Given / When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody, times(0)).setBody(anyString());
        }

        @Test
        void shouldNotReplaceAnyTokensIfRefreshMessageDoesNotHaveAny() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(Collections.emptyList());
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody, times(0)).setBody(anyString());
        }

        @Test
        void shouldNotReplaceAnyTokensIfRequestMessageDoesNotHaveAnyParameters() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(asList(mock(AntiCsrfToken.class)));
            String postRequestBody = "";
            given(requestMessageBody.toString()).willReturn(postRequestBody);
            given(postParamParser.parseParameters(postRequestBody))
                    .willReturn(Collections.emptyList());
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody, times(0)).setBody(anyString());
        }

        @Test
        void shouldNotReplaceAnyTokensIfRequestMessageDoesNotHaveAnyAntiCsrfTokens() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            List<AntiCsrfToken> tokens = asList(token("acsrf", "1234"));
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(tokens);
            String postRequestBody = "uid=1";
            given(requestMessageBody.toString()).willReturn(postRequestBody);
            List<NameValuePair> parameters = asList(parameter("uid", "1"));
            given(postParamParser.parseParameters(postRequestBody)).willReturn(parameters);
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody).setBody(postRequestBody);
        }

        @Test
        void shouldReplaceToken() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            List<AntiCsrfToken> tokens = asList(token("acsrf", "1234"));
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(tokens);
            String postRequestBody = "uid=1&acsrf=abcd";
            given(requestMessageBody.toString()).willReturn(postRequestBody);
            List<NameValuePair> parameters =
                    asList(parameter("uid", "1"), parameter("acsrf", "abcd"));
            given(postParamParser.parseParameters(postRequestBody)).willReturn(parameters);
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody).setBody("uid=1&acsrf=1234");
            verify(paramEncoder).apply("1234");
        }

        @Test
        void shouldReplaceMultipleTokens() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            List<AntiCsrfToken> tokens = asList(token("acsrf", "1234"), token("acsrf_", "5678"));
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(tokens);
            String postRequestBody = "uid=1&acsrf=abcd&acsrf_=efgh";
            given(requestMessageBody.toString()).willReturn(postRequestBody);
            List<NameValuePair> parameters =
                    asList(
                            parameter("uid", "1"),
                            parameter("acsrf", "abcd"),
                            parameter("acsrf_", "efgh"));
            given(postParamParser.parseParameters(postRequestBody)).willReturn(parameters);
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody).setBody("uid=1&acsrf=1234&acsrf_=5678");
            verify(paramEncoder).apply("1234");
            verify(paramEncoder).apply("5678");
        }

        @Test
        void shouldReplaceTokenValueEverywhere() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            List<AntiCsrfToken> tokens = asList(token("acsrf", "1234"));
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(tokens);
            String postRequestBody = "uid=1&acsrf=1";
            given(requestMessageBody.toString()).willReturn(postRequestBody);
            List<NameValuePair> parameters = asList(parameter("uid", "1"), parameter("acsrf", "1"));
            given(postParamParser.parseParameters(postRequestBody)).willReturn(parameters);
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody).setBody("uid=1234&acsrf=1234");
            verify(paramEncoder).apply("1234");
        }

        @Test
        void shouldNotReplaceTokenIfItHasDifferentCase() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            List<AntiCsrfToken> tokens = asList(token("ACSRF", "1234"));
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(tokens);
            String postRequestBody = "uid=1&acsrf=1";
            given(requestMessageBody.toString()).willReturn(postRequestBody);
            List<NameValuePair> parameters = asList(parameter("uid", "1"), parameter("acsrf", "1"));
            given(postParamParser.parseParameters(postRequestBody)).willReturn(parameters);
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody).setBody(postRequestBody);
        }

        @Test
        void shouldUseProvidedParameterEncoder() {
            // Given
            PostBasedAuthenticationMethodType.setExtAntiCsrf(extAntiCsrf);
            List<AntiCsrfToken> tokens = asList(token("acsrf", "1234"));
            given(extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken))
                    .willReturn(tokens);
            String postRequestBody = "uid=1&acsrf=abcd";
            given(requestMessageBody.toString()).willReturn(postRequestBody);
            List<NameValuePair> parameters =
                    asList(parameter("uid", "1"), parameter("acsrf", "abcd"));
            given(postParamParser.parseParameters(postRequestBody)).willReturn(parameters);
            given(paramEncoder.apply("1234")).willReturn("encoded");
            // When
            PostBasedAuthenticationMethodType.replaceAntiCsrfTokenValueIfRequired(
                    requestMessage, loginMsgWithFreshAcsrfToken, paramEncoder);
            // Then
            verify(requestMessageBody).setBody("uid=1&acsrf=encoded");
        }

        private static AntiCsrfToken token(String name, String value) {
            AntiCsrfToken token = mock(AntiCsrfToken.class, withSettings().lenient());
            given(token.getName()).willReturn(name);
            given(token.getValue()).willReturn(value);
            return token;
        }

        private static NameValuePair parameter(String name, String value) {
            NameValuePair parameter = mock(NameValuePair.class, withSettings().lenient());
            given(parameter.getName()).willReturn(name);
            given(parameter.getValue()).willReturn(value);
            return parameter;
        }

        static class Encoder implements UnaryOperator<String> {

            Encoder() {}

            @Override
            public String apply(String value) {
                return value;
            }
        }
    }
}
