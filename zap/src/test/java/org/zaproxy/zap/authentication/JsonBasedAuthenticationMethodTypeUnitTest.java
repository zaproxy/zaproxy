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

import static fi.iki.elonen.NanoHTTPD.newFixedLengthResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.authentication.PostBasedAuthenticationMethodType.PostBasedAuthenticationMethod;
import org.zaproxy.zap.testutils.NanoServerHandler;
import org.zaproxy.zap.users.AuthenticationState;
import org.zaproxy.zap.users.User;

class JsonBasedAuthenticationMethodTypeUnitTest extends WithConfigsTest {

    private static final String LOGGED_IN_INDICATOR = "logged in";
    private static final String LOGGED_IN_BODY =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                    + "Pellentesque auctor nulla id turpis placerat vulputate."
                    + LOGGED_IN_INDICATOR
                    + " Proin tempor bibendum eros rutrum. ";

    private AuthenticationMethod method;
    private JsonBasedAuthenticationMethodType type;

    @BeforeEach
    void setUp() throws Exception {

        type = new JsonBasedAuthenticationMethodType();
        method = type.createAuthenticationMethod(1);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.POLL_URL);
        method.setPollFrequencyUnits(AuthPollFrequencyUnits.REQUESTS);
        method.setPollFrequency(5);
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        this.startServer();
    }

    @AfterEach
    void cleanUpServer() {
        stopServer();
    }

    @Test
    void shouldReplaceUsernameInPollRequest() throws NullPointerException, IOException {
        // Given
        String test = "/shouldReplaceUsernameInPollRequest/test";
        String encodedPattern =
                URLEncoder.encode(
                        PostBasedAuthenticationMethod.MSG_USER_PATTERN,
                        StandardCharsets.UTF_8.name());
        String pollUrl = "/shouldReplaceUsernameInPollRequest/pollUrl";
        String pollData = "user=" + PostBasedAuthenticationMethod.MSG_USER_PATTERN;
        String username = "user";
        final List<String> orderedReqUrls = new ArrayList<>();
        final List<String> orderedReqData = new ArrayList<>();

        this.nano.addHandler(
                new NanoServerHandler(pollUrl.replace(encodedPattern, username)) {
                    @Override
                    protected Response serve(IHTTPSession session) {
                        orderedReqUrls.add(
                                session.getUri() + "?" + session.getQueryParameterString());

                        HashMap<String, String> map = new HashMap<>();
                        try {
                            session.parseBody(map);
                            orderedReqData.add(map.get("postData"));
                        } catch (Exception e) {
                        }
                        return newFixedLengthResponse(LOGGED_IN_BODY);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl + "?" + encodedPattern);

        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        method.setPollData(pollData);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());
        given(user.getAuthenticationCredentials())
                .willReturn(new UsernamePasswordAuthenticationCredentials(username, ""));

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqUrls.size(), is(1));
        assertThat(orderedReqUrls.get(0), is(pollUrl + "?" + username));
        assertThat(orderedReqData.size(), is(1));
        assertThat(
                orderedReqData.get(0),
                is(pollData.replace(PostBasedAuthenticationMethod.MSG_USER_PATTERN, username)));
    }

    @Test
    void shouldNotReplacePasswordInPollRequest() throws NullPointerException, IOException {
        // Given
        String test = "/shouldNotReplacePasswordInPollRequest/test";
        String pollUrl = "/shouldNotReplacePasswordInPollRequest/pollUrl";
        String pollData = "pwd=" + PostBasedAuthenticationMethod.MSG_PASS_PATTERN;
        String password = "password123!";
        final List<String> orderedReqData = new ArrayList<>();

        this.nano.addHandler(
                new NanoServerHandler(pollUrl) {
                    @Override
                    protected Response serve(IHTTPSession session) {
                        HashMap<String, String> map = new HashMap<>();
                        try {
                            session.parseBody(map);
                            orderedReqData.add(map.get("postData"));
                        } catch (Exception e) {
                        }
                        return newFixedLengthResponse(LOGGED_IN_BODY);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl);

        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        method.setPollData(pollData);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());
        given(user.getAuthenticationCredentials())
                .willReturn(new UsernamePasswordAuthenticationCredentials("", password));

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqData.size(), is(1));
        assertThat(orderedReqData.get(0), is(pollData));
    }

    @Test
    void shouldNotUrlEncodeUsernameInPollRequestBody() throws NullPointerException, IOException {
        // Given
        String test = "/shouldEncodeSpacesInBody/test";
        String pollUrl = "/shouldEncodeSpacesInBody/pollUrl";
        String pollData =
                "{ \"user\": \"" + PostBasedAuthenticationMethod.MSG_USER_PATTERN + "\" }";
        String username = "user name";
        final List<String> orderedReqData = new ArrayList<>();

        this.nano.addHandler(
                new NanoServerHandler(pollUrl) {
                    @Override
                    protected Response serve(IHTTPSession session) {
                        HashMap<String, String> map = new HashMap<>();
                        try {
                            session.parseBody(map);
                            orderedReqData.add(map.get("postData"));
                        } catch (Exception e) {
                        }
                        return newFixedLengthResponse(LOGGED_IN_BODY);
                    }
                });
        HttpMessage testMsg = this.getHttpMessage(test);
        HttpMessage pollMsg = this.getHttpMessage(pollUrl);

        method.setPollUrl(pollMsg.getRequestHeader().getURI().toString());
        method.setPollData(pollData);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());
        given(user.getAuthenticationCredentials())
                .willReturn(new UsernamePasswordAuthenticationCredentials(username, ""));

        // When/Then
        assertThat(method.isAuthenticated(testMsg, user), is(true));
        assertThat(orderedReqData.size(), is(1));
        assertThat(
                orderedReqData.get(0),
                is(pollData.replace(PostBasedAuthenticationMethod.MSG_USER_PATTERN, username)));
    }
}
