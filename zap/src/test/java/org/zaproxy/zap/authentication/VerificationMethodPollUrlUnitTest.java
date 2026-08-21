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
package org.zaproxy.zap.authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.testutils.TestUtils;
import org.zaproxy.zap.users.AuthenticationState;
import org.zaproxy.zap.users.User;

/** Unit test for {@link VerificationMethod} poll URL behaviour. */
class VerificationMethodPollUrlUnitTest extends TestUtils {

    private static final String LOGGED_IN_INDICATOR = "logged in";
    private static final String LOGGED_IN_BODY =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                    + "Pellentesque auctor nulla id turpis placerat vulputate."
                    + LOGGED_IN_INDICATOR
                    + " Proin tempor bibendum eros rutrum. ";

    private VerificationMethod vm;

    @BeforeEach
    void setUp() throws Exception {
        HttpRequestHeader.setDefaultUserAgent("not-custom-value");

        vm = new VerificationMethod();
        vm.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
    }

    @Test
    void shouldPollOnFirstRequest() throws NullPointerException, IOException {
        // Given
        String test = "/shouldPollOnFirstRequest/test";
        String pollUrl = "/shouldPollOnFirstRequest/pollUrl";
        final List<String> orderedReqs = new ArrayList<>();

        setMessageHandler(
                msg -> {
                    String path = msg.getRequestHeader().getURI().getPath();
                    if (pollUrl.equals(path)) {
                        orderedReqs.add(path);
                        msg.setResponseBody(LOGGED_IN_BODY);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        vm.setPollFrequency(5);
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        assertThat(orderedReqs.get(0), is(pollUrl));
    }

    @Test
    void shouldPollOnSpecifiedNumberOfRequests() throws NullPointerException, IOException {
        // Given
        String test = "/shouldPollOnFirstRequest/test";
        String pollUrl = "/shouldPollOnFirstRequest/pollUrl";
        final List<String> orderedReqs = new ArrayList<>();

        setMessageHandler(
                msg -> {
                    String path = msg.getRequestHeader().getURI().getPath();
                    if (pollUrl.equals(path)) {
                        orderedReqs.add(path);
                        msg.setResponseBody(LOGGED_IN_BODY);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        vm.setPollFrequency(5);
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(2));
        assertThat(orderedReqs.get(0), is(pollUrl));
        assertThat(orderedReqs.get(1), is(pollUrl));
    }

    @Test
    void shouldPollEveryFailingRequest() throws NullPointerException, IOException {
        // Given
        String test = "/shouldPollEveryFailingRequest/test";
        String pollUrl = "/shouldPollEveryFailingRequest/pollUrl";
        final List<String> orderedReqs = new ArrayList<>();

        setMessageHandler(
                msg -> {
                    String path = msg.getRequestHeader().getURI().getPath();
                    if (pollUrl.equals(path)) {
                        orderedReqs.add(path);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        vm.setPollFrequency(5);
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(vm.isAuthenticated(testMsg, user), is(false));
        assertThat(orderedReqs.size(), is(1));
        assertThat(vm.isAuthenticated(testMsg, user), is(false));
        assertThat(orderedReqs.size(), is(2));
        assertThat(vm.isAuthenticated(testMsg, user), is(false));
        assertThat(orderedReqs.size(), is(3));
        assertThat(vm.isAuthenticated(testMsg, user), is(false));
        assertThat(orderedReqs.size(), is(4));
    }

    @Test
    void shouldPollWhenForced() throws NullPointerException, IOException {
        // Given
        String test = "/shouldPollWhenForced/test";
        String pollUrl = "/shouldPollWhenForced/pollUrl";
        final List<String> orderedReqs = new ArrayList<>();

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        setMessageHandler(
                msg -> {
                    String path = msg.getRequestHeader().getURI().getPath();
                    if (pollUrl.equals(path)) {
                        orderedReqs.add(path);
                        msg.setResponseBody(LOGGED_IN_BODY);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        vm.setPollFrequency(500);
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        // When/Then
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        user.getAuthenticationState().setLastPollResult(false);
        assertThat(vm.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(2));
        assertThat(orderedReqs.get(0), is(pollUrl));
        assertThat(orderedReqs.get(1), is(pollUrl));
    }

    @Test
    void shouldPollOnSpecifiedNumberOfRequestsPerUser() throws NullPointerException, IOException {
        // Given
        String test = "/shouldPollOnFirstRequest/test";
        String pollUrl = "/shouldPollOnFirstRequest/pollUrl";
        final List<String> orderedReqs = new ArrayList<>();

        setMessageHandler(
                msg -> {
                    String path = msg.getRequestHeader().getURI().getPath();
                    if (pollUrl.equals(path)) {
                        orderedReqs.add(path);
                        msg.setResponseBody(LOGGED_IN_BODY);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(pollMsg.getRequestHeader().getURI().toString() + "?");
        vm.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        vm.setPollFrequency(5);
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user1 = mock(User.class);
        given(user1.getAuthenticationState()).willReturn(new AuthenticationState());
        User user2 = mock(User.class);
        given(user2.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        // First poll for user1
        assertThat(orderedReqs.size(), is(1));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));

        assertThat(vm.isAuthenticated(testMsg, user2), is(true));
        // First poll for user2
        assertThat(orderedReqs.size(), is(2));
        assertThat(vm.isAuthenticated(testMsg, user2), is(true));

        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        // Should not have changed yet
        assertThat(orderedReqs.size(), is(2));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        // Second poll for user1
        assertThat(orderedReqs.size(), is(3));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        assertThat(vm.isAuthenticated(testMsg, user1), is(true));
        assertThat(vm.isAuthenticated(testMsg, user2), is(true));
        assertThat(vm.isAuthenticated(testMsg, user2), is(true));
        assertThat(vm.isAuthenticated(testMsg, user2), is(true));
        assertThat(vm.isAuthenticated(testMsg, user2), is(true));
        // Should not have changed yet
        assertThat(orderedReqs.size(), is(3));
        assertThat(vm.isAuthenticated(testMsg, user2), is(true));
        // Second poll for user2
        assertThat(orderedReqs.size(), is(4));
    }

    @Test
    void shouldUseGetWhenPollMethodNullAndDataEmpty() throws Exception {
        // Given
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();
        setMessageHandler(pollMessages::add);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.pollAsUser(user);

        // Then
        assertThat(pollMessages, hasSize(1));
        assertThat(pollMessages.get(0).getRequestHeader().getMethod(), is(HttpRequestHeader.GET));
    }

    @Test
    void shouldUsePostWhenPollMethodNullAndDataNonEmpty() throws Exception {
        // Given
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();
        setMessageHandler(pollMessages::add);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        vm.setPollData("param=value");

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.pollAsUser(user);

        // Then
        assertThat(pollMessages, hasSize(1));
        assertThat(pollMessages.get(0).getRequestHeader().getMethod(), is(HttpRequestHeader.POST));
    }

    @Test
    void shouldUseConfiguredMethodWhenPollMethodSet() throws Exception {
        // Given
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();
        setMessageHandler(pollMessages::add);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        vm.setPollData("param=value");
        vm.setPollMethod(HttpRequestHeader.GET);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.pollAsUser(user);

        // Then
        assertThat(pollMessages, hasSize(1));
        assertThat(pollMessages.get(0).getRequestHeader().getMethod(), is(HttpRequestHeader.GET));
    }

    @Test
    void shouldUseConfiguredPostMethodWhenPollMethodSetAndDataEmpty() throws Exception {
        // Given
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();
        setMessageHandler(pollMessages::add);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        vm.setPollMethod(HttpRequestHeader.POST);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.pollAsUser(user);

        // Then
        assertThat(pollMessages, hasSize(1));
        assertThat(pollMessages.get(0).getRequestHeader().getMethod(), is(HttpRequestHeader.POST));
    }

    @Test
    void shouldHandlePollHeadersWithColonsInValues() throws Exception {
        // Given
        String test = "/test";
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();

        setMessageHandler(pollMessages::add);

        HttpMessage testMsg = this.getHttpMessage(test);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        vm.setPollHeaders(
                """
                Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0:signature
                X-Custom-Time: 2025-07-19T10:30:45.123Z
                Content-Type: application/json
                """);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.isAuthenticated(testMsg, user);

        // Then
        assertThat(pollMessages, hasSize(1));
        HttpRequestHeader requestHeader = pollMessages.get(0).getRequestHeader();
        assertThat(
                requestHeader.getHeader("Authorization"),
                is("Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0:signature"));
        assertThat(requestHeader.getHeader("X-Custom-Time"), is("2025-07-19T10:30:45.123Z"));
        assertThat(requestHeader.getHeader("Content-Type"), is("application/json"));
    }

    @Test
    void shouldOverrideExistingHeaderWithFirstPollHeaderOccurrence() throws Exception {
        // Given
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();
        setMessageHandler(pollMessages::add);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        vm.setPollHeaders("user-agent: custom-value");

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.pollAsUser(user);

        // Then
        assertThat(pollMessages, hasSize(1));
        assertThat(
                pollMessages.get(0).getRequestHeader().getHeaderValues("user-agent"),
                is(List.of("custom-value")));
    }

    @Test
    void shouldPreserveDuplicatePollHeaders() throws Exception {
        // Given
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();
        setMessageHandler(pollMessages::add);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        vm.setPollHeaders("X-Token: value1\nX-Token: value2");

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.pollAsUser(user);

        // Then
        assertThat(pollMessages, hasSize(1));
        assertThat(
                pollMessages.get(0).getRequestHeader().getHeaderValues("X-Token"),
                is(List.of("value1", "value2")));
    }

    @Test
    void shouldNotAffectHeadersNotInPollHeaders() throws Exception {
        // Given
        String pollUrl = "/pollUrl";
        List<HttpMessage> pollMessages = new ArrayList<>();
        setMessageHandler(pollMessages::add);

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        vm.setPollHeaders("Authorization: Bearer token");

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.pollAsUser(user);

        // Then
        assertThat(pollMessages, hasSize(1));
        HttpRequestHeader requestHeader = pollMessages.get(0).getRequestHeader();
        assertThat(requestHeader.getHeader("Authorization"), is("Bearer token"));
        assertThat(requestHeader.getHeader("user-agent"), is("not-custom-value"));
    }

    @Test
    void shouldInvokeUserDataReplacerBeforeSendingPollRequest() throws Exception {
        // Given
        String pollUrl = "/shouldInvokeUserDataReplacer/pollUrl";
        List<String> sentBodies = new ArrayList<>();

        setMessageHandler(
                msg -> {
                    if (pollUrl.equals(msg.getRequestHeader().getURI().getPath())) {
                        sentBodies.add(msg.getRequestBody().toString());
                        msg.setResponseBody(LOGGED_IN_BODY);
                    }
                });

        vm.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        vm.setPollUrl(getHttpMessage(pollUrl).getRequestHeader().getURI().toString());
        vm.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        vm.setUserDataReplacer((msg, user) -> msg.setRequestBody("replaced"));

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When
        vm.isAuthenticated(this.getHttpMessage("/test"), user);

        // Then
        assertThat(sentBodies, hasSize(1));
        assertThat(sentBodies.get(0), is("replaced"));
    }
}
