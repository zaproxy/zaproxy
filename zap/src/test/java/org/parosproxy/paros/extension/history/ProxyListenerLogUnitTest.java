/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
package org.parosproxy.paros.extension.history;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link ProxyListenerLog}. */
class ProxyListenerLogUnitTest {

    private Model model;
    private ViewDelegate view;
    private ExtensionHistory extension;

    private ProxyListenerLog listener;

    @BeforeEach
    void setUp() {
        Constant.messages = mock(I18N.class);

        model = mock(Model.class);
        view = mock(ViewDelegate.class);
        extension = mock(ExtensionHistory.class);

        listener = new ProxyListenerLog(model, view, extension);
    }

    @AfterEach
    void cleanUp() {
        Constant.messages = null;
    }

    @Test
    void shouldNotModifyProxiedRequest() {
        // Given
        HttpMessage msg = mock(HttpMessage.class);
        // When
        listener.onHttpRequestSend(msg);
        // Then
        verifyNoInteractions(msg);
    }
}
