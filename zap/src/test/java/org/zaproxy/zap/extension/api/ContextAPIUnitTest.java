/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link ContextAPI}. */
class ContextAPIUnitTest {

    private ContextAPI contextApi;
    private Session session;
    private Context context;

    @BeforeEach
    void setUp() {
        Model model = mock(Model.class, withSettings().strictness(Strictness.LENIENT));
        Model.setSingletonForTesting(model);
        session = mock(Session.class, withSettings().strictness(Strictness.LENIENT));
        given(model.getSession()).willReturn(session);
        Constant.messages = mock(I18N.class, withSettings().strictness(Strictness.LENIENT));

        context = new Context(session, 1);
        context.setName("Default Context");
        given(session.getContext("Default Context")).willReturn(context);

        contextApi = new ContextAPI();
    }

    @AfterEach
    void cleanUp() {
        Constant.messages = null;
    }

    @Test
    void shouldSetCheckingStrategyPollUrlWithoutPollDataAndPollHeaders() throws Exception {
        // Given
        JSONObject params = new JSONObject();
        params.put("contextName", "Default Context");
        params.put("checkingStrategy", "POLL_URL");
        params.put("pollUrl", "https://example.com/auth-check");
        params.put("pollFrequency", 10);
        params.put("pollFrequencyUnits", "SECONDS");

        // When
        ApiResponse response = contextApi.handleApiAction("setContextCheckingStrategy", params);

        // Then
        assertThat(response, is(equalTo(ApiResponseElement.OK)));
        assertThat(
                context.getVerificationMethod().getAuthCheckingStrategy(),
                is(equalTo(AuthCheckingStrategy.POLL_URL)));
        assertThat(
                context.getVerificationMethod().getPollUrl(),
                is(equalTo("https://example.com/auth-check")));
        assertThat(context.getVerificationMethod().getPollData(), is(equalTo("")));
        assertThat(context.getVerificationMethod().getPollHeaders(), is(equalTo("")));
        assertThat(context.getVerificationMethod().getPollFrequency(), is(equalTo(10)));
        assertThat(
                context.getVerificationMethod().getPollFrequencyUnits(),
                is(equalTo(AuthPollFrequencyUnits.SECONDS)));
    }

    @Test
    void shouldSetCheckingStrategyPollUrlWithPollDataAndPollHeaders() throws Exception {
        // Given
        JSONObject params = new JSONObject();
        params.put("contextName", "Default Context");
        params.put("checkingStrategy", "POLL_URL");
        params.put("pollUrl", "https://example.com/auth-check");
        params.put("pollData", "param=value");
        params.put("pollHeaders", "X-Custom-Header: test");
        params.put("pollFrequency", 5);
        params.put("pollFrequencyUnits", "REQUESTS");

        // When
        ApiResponse response = contextApi.handleApiAction("setContextCheckingStrategy", params);

        // Then
        assertThat(response, is(equalTo(ApiResponseElement.OK)));
        assertThat(
                context.getVerificationMethod().getAuthCheckingStrategy(),
                is(equalTo(AuthCheckingStrategy.POLL_URL)));
        assertThat(
                context.getVerificationMethod().getPollUrl(),
                is(equalTo("https://example.com/auth-check")));
        assertThat(context.getVerificationMethod().getPollData(), is(equalTo("param=value")));
        assertThat(
                context.getVerificationMethod().getPollHeaders(),
                is(equalTo("X-Custom-Header: test")));
        assertThat(context.getVerificationMethod().getPollFrequency(), is(equalTo(5)));
        assertThat(
                context.getVerificationMethod().getPollFrequencyUnits(),
                is(equalTo(AuthPollFrequencyUnits.REQUESTS)));
    }

    @Test
    void shouldFailIfPollUrlIsMissing() {
        // Given
        JSONObject params = new JSONObject();
        params.put("contextName", "Default Context");
        params.put("checkingStrategy", "POLL_URL");
        params.put("pollFrequency", 10);
        params.put("pollFrequencyUnits", "SECONDS");

        // When / Then
        ApiException exception =
                assertThrows(
                        ApiException.class,
                        () -> contextApi.handleApiAction("setContextCheckingStrategy", params));
        assertThat(exception.getType(), is(equalTo(ApiException.Type.ILLEGAL_PARAMETER)));
    }
}
