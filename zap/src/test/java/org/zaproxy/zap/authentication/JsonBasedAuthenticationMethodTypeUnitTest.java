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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.authentication.PostBasedAuthenticationMethodType.PostBasedAuthenticationMethod;
import org.zaproxy.zap.users.User;

class JsonBasedAuthenticationMethodTypeUnitTest extends WithConfigsTest {

    private AuthenticationMethod method;

    @BeforeEach
    void setUp() throws Exception {
        JsonBasedAuthenticationMethodType type = new JsonBasedAuthenticationMethodType();
        method = type.createAuthenticationMethod(1);
    }

    @Test
    void shouldNotUrlEncodeUsernameInPollRequestBody() throws Exception {
        // Given
        String username = "user name";
        HttpMessage msg = new HttpMessage(new URI("http://example.com/pollUrl", true));
        msg.setRequestBody(
                "{ \"user\": \"" + PostBasedAuthenticationMethod.MSG_USER_PATTERN + "\" }");

        User user = mock(User.class);
        given(user.getAuthenticationCredentials())
                .willReturn(new UsernamePasswordAuthenticationCredentials(username, ""));

        // When
        method.replaceUserDataInPollRequest(msg, user);

        // Then
        assertThat(msg.getRequestBody().toString(), is("{ \"user\": \"" + username + "\" }"));
    }
}
