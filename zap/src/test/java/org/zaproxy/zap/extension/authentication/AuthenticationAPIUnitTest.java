/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.zap.extension.authentication;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.authentication.ManualAuthenticationMethodType;
import org.zaproxy.zap.model.Context;

/** Unit test for {@link AuthenticationAPI}. */
class AuthenticationAPIUnitTest extends WithConfigsTest {

    private static final int CONTEXT_ID = 1;

    private Context context;
    private AuthenticationAPI api;

    @BeforeEach
    void setUp() {
        Session session = mock(Session.class);
        given(model.getSession()).willReturn(session);
        context = new Context(session, CONTEXT_ID);
        given(session.getContext(CONTEXT_ID)).willReturn(context);

        ExtensionAuthentication extension = mock(ExtensionAuthentication.class);
        ManualAuthenticationMethodType methodType = new ManualAuthenticationMethodType();
        given(extension.getAuthenticationMethodTypes()).willReturn(List.of(methodType));
        api = new AuthenticationAPI(extension);
    }

    @Test
    void shouldPreserveVerificationMethodWhenSettingAuthenticationMethod() throws Exception {
        // Given
        context.getVerificationMethod().setLoggedInIndicatorPattern("logged-in.*");
        context.getVerificationMethod().setLoggedOutIndicatorPattern("logged-out.*");
        JSONObject params = new JSONObject();
        params.put(AuthenticationAPI.PARAM_CONTEXT_ID, CONTEXT_ID);
        params.put("authMethodName", "manualAuthentication");
        // When
        api.handleApiAction("setAuthenticationMethod", params);
        // Then
        assertThat(
                context.getVerificationMethod().getLoggedInIndicatorPattern().pattern(),
                is("logged-in.*"));
        assertThat(
                context.getVerificationMethod().getLoggedOutIndicatorPattern().pattern(),
                is("logged-out.*"));
    }
}
