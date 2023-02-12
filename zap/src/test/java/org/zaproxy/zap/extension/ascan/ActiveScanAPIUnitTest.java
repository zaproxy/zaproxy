/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link ActiveScanAPI}. */
class ActiveScanAPIUnitTest {

    private PluginFactory defaultPluginFactory;
    private ScanPolicy defaultScanPolicy;
    private PolicyManager policyManager;
    private ExtensionActiveScan extension;
    private ActiveScanAPI api;

    @BeforeAll
    static void beforeAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @BeforeEach
    void setUp() {
        defaultPluginFactory = mock(PluginFactory.class);
        defaultScanPolicy = mock(ScanPolicy.class);
        given(defaultScanPolicy.getPluginFactory()).willReturn(defaultPluginFactory);
        policyManager = mock(PolicyManager.class);
        given(policyManager.getDefaultScanPolicy()).willReturn(defaultScanPolicy);
        extension = mock(ExtensionActiveScan.class);
        given(extension.getPolicyManager()).willReturn(policyManager);

        api = new ActiveScanAPI(extension);
    }

    @ParameterizedTest
    @CsvSource({"enableScanners, true", "disableScanners, false"})
    void shouldEnableAndDisableScanRules(String name, boolean enabled) throws Exception {
        // Given
        JSONObject params = new JSONObject();
        Plugin scanRule2 = mock(Plugin.class);
        given(defaultPluginFactory.getPlugin(2)).willReturn(scanRule2);
        Plugin scanRule4 = mock(Plugin.class);
        given(defaultPluginFactory.getPlugin(4)).willReturn(scanRule4);
        params.put("ids", "2, 4");
        // When
        api.handleApiAction(name, params);
        // Then
        verify(scanRule2).setEnabled(enabled);
        verify(scanRule4).setEnabled(enabled);
    }

    @ParameterizedTest
    @ValueSource(strings = {"enableScanners", "disableScanners"})
    void shouldFailFastForInvalidScanRuleIdsWhenEnablingAndDisabling(String name) throws Exception {
        // Given
        JSONObject params = new JSONObject();
        params.put("ids", "1, a, 3, 4");
        Plugin scanRule1 = mock(Plugin.class);
        given(defaultPluginFactory.getPlugin(1)).willReturn(scanRule1);
        Plugin scanRule3 = mock(Plugin.class);
        given(defaultPluginFactory.getPlugin(3)).willReturn(scanRule3);
        // When / Then
        ApiException exception =
                assertThrows(ApiException.class, () -> api.handleApiAction(name, params));
        assertThat(exception.getType(), is(equalTo(ApiException.Type.ILLEGAL_PARAMETER)));
        assertThat(exception.toString(true), containsString("a"));
        verify(scanRule1).setEnabled(anyBoolean());
        verifyNoInteractions(scanRule3);
    }

    @ParameterizedTest
    @ValueSource(strings = {"enableScanners", "disableScanners"})
    void shouldAffectAllScanRulesWhenEnablingAndDisablingEvenWithUnknownIds(String name)
            throws Exception {
        // Given
        JSONObject params = new JSONObject();
        Plugin scanRule2 = mock(Plugin.class);
        given(defaultPluginFactory.getPlugin(2)).willReturn(scanRule2);
        Plugin scanRule4 = mock(Plugin.class);
        given(defaultPluginFactory.getPlugin(4)).willReturn(scanRule4);
        params.put("ids", "1, 2, 3, 4");
        // When / Then
        assertThrows(ApiException.class, () -> api.handleApiAction(name, params));
        verify(scanRule2).setEnabled(anyBoolean());
        verify(scanRule4).setEnabled(anyBoolean());
    }

    @ParameterizedTest
    @ValueSource(strings = {"enableScanners", "disableScanners"})
    void shouldThrowExceptionWithAllUnknownScanRuleIdsWhenEnablingAndDisabling(String name)
            throws Exception {
        // Given
        JSONObject params = new JSONObject();
        given(defaultPluginFactory.getPlugin(2)).willReturn(mock(Plugin.class));
        given(defaultPluginFactory.getPlugin(4)).willReturn(mock(Plugin.class));
        params.put("ids", "1, 2, 3, 4");
        // When / Then
        ApiException exception =
                assertThrows(ApiException.class, () -> api.handleApiAction(name, params));
        assertThat(exception.getType(), is(equalTo(ApiException.Type.DOES_NOT_EXIST)));
        assertThat(exception.toString(true), containsString("IDs: [1, 3]"));
    }
}
