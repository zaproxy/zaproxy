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
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.testutils.TestUtils;
import org.zaproxy.zap.users.AuthenticationState;
import org.zaproxy.zap.users.User;

class AuthenticationMethodPollUrlUnitTest extends TestUtils {

    private static final String LOGGED_IN_INDICATOR = "logged in";
    private static final String LOGGED_IN_BODY =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                    + "Pellentesque auctor nulla id turpis placerat vulputate."
                    + LOGGED_IN_INDICATOR
                    + " Proin tempor bibendum eros rutrum. ";

    private HttpMessage loginMessage;
    private AuthenticationMethod method;

    @BeforeEach
    void setUp() throws Exception {
        loginMessage = new HttpMessage();
        HttpRequestHeader header = new HttpRequestHeader();
        header.setURI(new URI("http://www.example.com", true));
        loginMessage.setRequestHeader(header);
        method = Mockito.mock(AuthenticationMethod.class, Mockito.CALLS_REAL_METHODS);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
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

        method.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        method.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        method.setPollFrequency(5);
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user), is(true));
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

        method.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        method.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        method.setPollFrequency(5);
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
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

        method.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        method.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        method.setPollFrequency(5);
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user), is(false));
        assertThat(orderedReqs.size(), is(1));
        assertThat(method.isAuthenticated(testMsg, user), is(false));
        assertThat(orderedReqs.size(), is(2));
        assertThat(method.isAuthenticated(testMsg, user), is(false));
        assertThat(orderedReqs.size(), is(3));
        assertThat(method.isAuthenticated(testMsg, user), is(false));
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

        method.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        method.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        method.setPollFrequency(500);
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqs.size(), is(1));
        user.getAuthenticationState().setLastPollResult(false);
        assertThat(method.isAuthenticated(testMsg, user), is(true));
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

        method.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString() + "?");
        method.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        method.setPollFrequency(5);
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        User user1 = mock(User.class);
        given(user1.getAuthenticationState()).willReturn(new AuthenticationState());
        User user2 = mock(User.class);
        given(user2.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        // First poll for user1
        assertThat(orderedReqs.size(), is(1));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));

        assertThat(method.isAuthenticated(testMsg, user2), is(true));
        // First poll for user2
        assertThat(orderedReqs.size(), is(2));
        assertThat(method.isAuthenticated(testMsg, user2), is(true));

        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        // Should not have changed yet
        assertThat(orderedReqs.size(), is(2));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        // Second poll for user1
        assertThat(orderedReqs.size(), is(3));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        assertThat(method.isAuthenticated(testMsg, user1), is(true));
        assertThat(method.isAuthenticated(testMsg, user2), is(true));
        assertThat(method.isAuthenticated(testMsg, user2), is(true));
        assertThat(method.isAuthenticated(testMsg, user2), is(true));
        assertThat(method.isAuthenticated(testMsg, user2), is(true));
        // Should not have changed yet
        assertThat(orderedReqs.size(), is(3));
        assertThat(method.isAuthenticated(testMsg, user2), is(true));
        // Second poll for user2
        assertThat(orderedReqs.size(), is(4));
    }
}
