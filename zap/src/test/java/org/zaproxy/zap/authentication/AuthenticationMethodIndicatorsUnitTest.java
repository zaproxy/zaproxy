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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.AuthenticationState;
import org.zaproxy.zap.users.User;

@ExtendWith(MockitoExtension.class)
class AuthenticationMethodIndicatorsUnitTest {

    private static final String LOGGED_OUT_COMPLEX_INDICATOR = "User [^\\s]* logged out";
    private static final String LOGGED_OUT_COMPLEX_BODY =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                    + "Pellentesque auctor nulla id turpis placerat vulputate. User Test logged out. "
                    + " Proin tempor bibendum eros rutrum. ";
    private static final String LOGGED_IN_INDICATOR = "logged in";
    private static final String LOGGED_OUT_INDICATOR = "logged out";
    private static final String LOGGED_IN_BODY =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                    + "Pellentesque auctor nulla id turpis placerat vulputate."
                    + LOGGED_IN_INDICATOR
                    + " Proin tempor bibendum eros rutrum. ";
    private static final String LOGGED_OUT_BODY =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. "
                    + "Pellentesque auctor nulla id turpis placerat vulputate."
                    + LOGGED_OUT_INDICATOR
                    + " Proin tempor bibendum eros rutrum. ";

    private HttpMessage loginMessage;
    private AuthenticationMethod method;

    @BeforeEach
    void setUp() throws Exception {
        loginMessage = new HttpMessage();
        HttpRequestHeader header = new HttpRequestHeader();
        header.setURI(new URI("http://www.example.com", true));
        loginMessage.setRequestHeader(header);
        method = new TestAuthenticationMethod();
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
    }

    @Test
    void shouldStoreSetLoggedInIndicator() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        // When/Then
        assertEquals(
                LOGGED_IN_INDICATOR,
                method.getVerificationMethod().getLoggedInIndicatorPattern().pattern());
    }

    @Test
    void shouldStoreSetLoggedOutIndicator() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);

        // When/Then
        assertEquals(
                LOGGED_OUT_INDICATOR,
                method.getVerificationMethod().getLoggedOutIndicatorPattern().pattern());
    }

    @Test
    void shouldNotStoreNullOrEmptyLoggedInIndicator() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(null);

        // When/Then
        assertNull(method.getVerificationMethod().getLoggedInIndicatorPattern());

        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern("  ");

        // When/Then
        assertNull(method.getVerificationMethod().getLoggedInIndicatorPattern());
    }

    @Test
    void shouldNotStoreNullOrEmptyLoggedOutIndicator() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(null);

        // When/Then
        assertNull(method.getVerificationMethod().getLoggedOutIndicatorPattern());

        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern("  ");

        // When/Then
        assertNull(method.getVerificationMethod().getLoggedOutIndicatorPattern());
    }

    @Test
    void shouldIdentifyLoggedInResponseBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.setResponseBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutResponseBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInResponseHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutResponseHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedOutResponseBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInResponseBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.setResponseBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutResponseHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInResponseHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutResponseWithComplexRegex() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_COMPLEX_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInResponseWithComplexRegex() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyResponseAsLoggedInWhenNoIndicatorIsSet() {
        // Given
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedInRequestBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutRequestBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInRequestHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutRequestHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedOutRequestBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInRequestBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutRequestHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInRequestHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyLoggedOutRequestWithComplexRegex() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_COMPLEX_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(false));
    }

    @Test
    void shouldIdentifyLoggedInRequestWithComplexRegex() {
        // Given
        method.getVerificationMethod().setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    @Test
    void shouldIdentifyRequestAsLoggedInWhenNoIndicatorIsSet() {
        // Given
        loginMessage.setRequestBody(LOGGED_OUT_BODY);
        method.getVerificationMethod().setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.getVerificationMethod().isAuthenticated(loginMessage, user), is(true));
    }

    private static class TestAuthenticationMethod extends AuthenticationMethod {

        @Override
        public boolean isConfigured() {
            return false;
        }

        @Override
        protected AuthenticationMethod duplicate() {
            return null;
        }

        @Override
        public AuthenticationCredentials createAuthenticationCredentials() {
            return null;
        }

        @Override
        public AuthenticationMethodType getType() {
            return null;
        }

        @Override
        public WebSession authenticate(
                SessionManagementMethod sessionManagementMethod,
                AuthenticationCredentials credentials,
                User user)
                throws UnsupportedAuthenticationCredentialsException {
            return null;
        }

        @Override
        public ApiResponse getApiResponseRepresentation() {
            return null;
        }

        @Override
        public void replaceUserDataInPollRequest(HttpMessage msg, User user) {}
    }
}
