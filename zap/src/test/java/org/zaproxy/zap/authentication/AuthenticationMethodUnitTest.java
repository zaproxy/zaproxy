/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;

/** Unit test for {@link AuthenticationMethod}. */
class AuthenticationMethodUnitTest {

    @Test
    void shouldBeEqualToItself() {
        // Given
        AuthenticationMethod authMethod = new AuthenticationMethodTest();
        // When
        boolean equals = authMethod.equals(authMethod);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    void shouldNotBeEqualToNull() {
        // Given
        AuthenticationMethod authMethod = new AuthenticationMethodTest();
        // When
        boolean equals = authMethod.equals(null);
        // Then
        assertThat(equals, is(false));
    }

    @Test
    void shouldNotBeEqualToExtendedAuthenticationMethod() {
        // Given
        AuthenticationMethod authMethod = new AuthenticationMethodTest();
        AuthenticationMethod otherAuthMethod = new AuthenticationMethodTest() {
                    // Anonymous subtype — different class
                };
        // When
        boolean equals = authMethod.equals(otherAuthMethod) | otherAuthMethod.equals(authMethod);
        // Then
        assertThat(equals, is(false));
    }

    private static class AuthenticationMethodTest extends AuthenticationMethod {

        @Override
        public boolean isConfigured() {
            return false;
        }

        @Override
        protected org.zaproxy.zap.authentication.AuthenticationMethod duplicate() {
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
