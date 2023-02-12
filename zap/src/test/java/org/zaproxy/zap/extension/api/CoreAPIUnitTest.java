/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link CoreAPI}. */
class CoreAPIUnitTest {

    private ApiImplementor networkApi;
    private CoreAPI coreApi;

    @BeforeEach
    void setUp() {
        Model model = mock(Model.class, withSettings().strictness(Strictness.LENIENT));
        Model.setSingletonForTesting(model);
        Constant.messages = mock(I18N.class, withSettings().strictness(Strictness.LENIENT));
        networkApi = mock(ApiImplementor.class, withSettings().strictness(Strictness.LENIENT));
        given(networkApi.getPrefix()).willReturn("network");
        API.getInstance().registerApiImplementor(networkApi);
        coreApi = new CoreAPI();
    }

    @AfterEach
    void cleanUp() {
        API.getInstance().removeApiImplementor(networkApi);
        Constant.messages = null;
    }

    @Test
    void shouldAddApiElements() {
        assertThat(coreApi.getApiActions(), hasSize(41));
        assertThat(coreApi.getApiViews(), hasSize(40));
        assertThat(coreApi.getApiOthers(), hasSize(11));
    }

    @Test
    void shouldCallNetworkApiWhenGeneratingRootCaCert() throws Exception {
        // Given
        String name = "generateRootCA";
        JSONObject params = new JSONObject();
        given(networkApi.handleApiAction(any(), any())).willReturn(ApiResponseElement.OK);
        // When
        ApiResponse response = coreApi.handleApiAction(name, params);
        // Then
        verify(networkApi).handleApiAction("generateRootCaCert", params);
        assertThat(response, is(equalTo(ApiResponseElement.OK)));
    }

    @Test
    void shouldCallNetworkApiWhenObtainingRootCaCert() throws Exception {
        // Given
        String name = "rootcert";
        JSONObject params = new JSONObject();
        HttpMessage message = new HttpMessage();
        given(networkApi.handleApiOther(any(), any(), any())).willReturn(message);
        // When
        HttpMessage response = coreApi.handleApiOther(message, name, params);
        // Then
        verify(networkApi).handleApiOther(message, "rootCaCert", params);
        assertThat(response, is(sameInstance(message)));
    }
}
