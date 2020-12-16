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
package org.zaproxy.zap.extension.forceduser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link ExtensionForcedUser}. */
class ExtensionForcedUserUnitTest extends WithConfigsTest {

    private ExtensionForcedUser extensionForcedUser;

    @BeforeEach
    void setup() {
        Constant.messages = mock(I18N.class);

        extensionForcedUser = new ExtensionForcedUser();
    }

    @Test
    void shouldImportContextWithNoForcedUser() {
        // Given
        Context context = mock(Context.class);
        Configuration config = new ZapXmlConfiguration();
        // When
        extensionForcedUser.importContextData(context, config);
        // Then
        verify(context, times(0)).getId();
    }

    @Test
    void shouldNotImportContextWithUnknownForcedUser() {
        // Given
        given(extensionLoader.getExtension(ExtensionUserManagement.class))
                .willReturn(new ExtensionUserManagement());
        Context context = mock(Context.class);
        Configuration config = new ZapXmlConfiguration();
        config.setProperty("context.forceduser", Integer.MIN_VALUE);
        // When / Then
        assertThrows(
                IllegalStateException.class,
                () -> extensionForcedUser.importContextData(context, config));
    }
}
