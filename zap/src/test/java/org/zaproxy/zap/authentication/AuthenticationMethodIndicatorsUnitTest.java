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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
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
        method = Mockito.mock(AuthenticationMethod.class, Mockito.CALLS_REAL_METHODS);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_RESP);
    }

    @Test
    void shouldStoreSetLoggedInIndicator() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);

        // When/Then
        assertThat(method.getLoggedInIndicatorPattern().pattern()).isEqualTo(LOGGED_IN_INDICATOR);
    }

    @Test
    void shouldStoreSetLoggedOutIndicator() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);

        // When/Then
        assertThat(method.getLoggedOutIndicatorPattern().pattern()).isEqualTo(LOGGED_OUT_INDICATOR);
    }

    @Test
    void shouldNotStoreNullOrEmptyLoggedInIndicator() {
        // Given
        method.setLoggedInIndicatorPattern(null);

        // When/Then
        assertThat(method.getLoggedInIndicatorPattern()).isNull();

        // Given
        method.setLoggedInIndicatorPattern("  ");

        // When/Then
        assertThat(method.getLoggedInIndicatorPattern()).isNull();
    }

    @Test
    void shouldNotStoreNullOrEmptyLoggedOutIndicator() {
        // Given
        method.setLoggedOutIndicatorPattern(null);

        // When/Then
        assertThat(method.getLoggedOutIndicatorPattern()).isNull();

        // Given
        method.setLoggedOutIndicatorPattern("  ");

        // When/Then
        assertThat(method.getLoggedOutIndicatorPattern()).isNull();
    }

    @Test
    void shouldIdentifyLoggedInResponseBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.setResponseBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutResponseBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInResponseHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutResponseHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedOutResponseBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInResponseBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.setResponseBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutResponseHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInResponseHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        loginMessage.getResponseHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutResponseWithComplexRegex() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_COMPLEX_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInResponseWithComplexRegex() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyResponseAsLoggedInWhenNoIndicatorIsSet() {
        // Given
        loginMessage.setResponseBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedInRequestBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutRequestBodyWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInRequestHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutRequestHeaderWhenLoggedInIndicatorIsSet() {
        // Given
        method.setLoggedInIndicatorPattern(LOGGED_IN_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedOutRequestBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInRequestBodyWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_IN_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutRequestHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_OUT_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInRequestHeaderWhenLoggedOutIndicatorIsSet() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.getRequestHeader().addHeader("test", LOGGED_IN_INDICATOR);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyLoggedOutRequestWithComplexRegex() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_COMPLEX_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isFalse();
    }

    @Test
    void shouldIdentifyLoggedInRequestWithComplexRegex() {
        // Given
        method.setLoggedOutIndicatorPattern(LOGGED_OUT_COMPLEX_INDICATOR);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);
        loginMessage.setRequestBody(LOGGED_OUT_BODY);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }

    @Test
    void shouldIdentifyRequestAsLoggedInWhenNoIndicatorIsSet() {
        // Given
        loginMessage.setRequestBody(LOGGED_OUT_BODY);
        method.setAuthCheckingStrategy(AuthCheckingStrategy.EACH_REQ);

        User user = mock(User.class);
        given(user.getAuthenticationState()).willReturn(new AuthenticationState());

        // When/Then
        assertThat(method.isAuthenticated(loginMessage, user)).isTrue();
    }
}
