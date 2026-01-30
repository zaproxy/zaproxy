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
package org.zaproxy.testutils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractPanel;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Base class for GUI tests that need core ZAP singletons. */
public abstract class AbstractZapGuiTest extends AbstractGuiTest {

    @BeforeEach
    void setUpZapSingletons() throws Exception {
        setUpConstantMessages();
        executeInEdt(
                () -> {
                    Model.setSingletonForTesting(new Model());
                    return null;
                });
        Model.getSingleton().getOptionsParam().load(new ZapXmlConfiguration());
    }

    @AfterEach
    void clearZapSingletons() {
        Constant.messages = null;
    }

    protected AbstractPanel createPanel(String name) {
        return executeInEdt(
                () -> {
                    AbstractPanel panel = new AbstractPanel();
                    panel.setName(name);
                    return panel;
                });
    }

    private static void setUpConstantMessages() {
        I18N i18n = mock(I18N.class, withSettings().strictness(Strictness.LENIENT));
        given(i18n.getString(anyString())).willAnswer(invocation -> invocation.getArgument(0));
        given(i18n.getString(anyString(), any()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(i18n.getLocal()).willReturn(Locale.getDefault());
        Constant.messages = i18n;
    }
}
