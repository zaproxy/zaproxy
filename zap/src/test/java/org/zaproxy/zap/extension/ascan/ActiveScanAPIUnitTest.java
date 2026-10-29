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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
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

    @Test
    void shouldNotDeprecatePolicyActionsIfNotInDevMode() {
        // Given
        try (MockedStatic<Constant> constant = mockStatic(Constant.class, CALLS_REAL_METHODS)) {
            constant.when(Constant::isDevMode).thenReturn(false);
            // When
            ActiveScanAPI api = new ActiveScanAPI(extension);
            // Then
            assertThat(api.getApiAction("addScanPolicy").isDeprecated(), is(equalTo(false)));
            assertThat(api.getApiAction("enableScanners").isDeprecated(), is(equalTo(false)));
        }
    }

    @Test
    void shouldDeprecateRedirectedPolicyActionsInDevMode() {
        // Given
        try (MockedStatic<Constant> constant = mockStatic(Constant.class, CALLS_REAL_METHODS)) {
            constant.when(Constant::isDevMode).thenReturn(true);
            // When
            ActiveScanAPI api = new ActiveScanAPI(extension);
            // Then
            Map<String, String> redirected =
                    Map.ofEntries(
                            Map.entry("addScanPolicy", "addPolicy"),
                            Map.entry("updateScanPolicy", "updatePolicy"),
                            Map.entry("removeScanPolicy", "removePolicy"),
                            Map.entry("importScanPolicy", "importLegacyPolicy"),
                            Map.entry("enableAllScanners", "enableAllRules"),
                            Map.entry("disableAllScanners", "disableAllRules"),
                            Map.entry("enableScanners", "enableRules"),
                            Map.entry("disableScanners", "disableRules"),
                            Map.entry("setEnabledPolicies", "setEnabledCategories"),
                            Map.entry("setPolicyAttackStrength", "setCategoryStrength"),
                            Map.entry("setPolicyAlertThreshold", "setCategoryThreshold"),
                            Map.entry("setScannerAttackStrength", "setRuleStrength"),
                            Map.entry("setScannerAlertThreshold", "setRuleThreshold"));
            redirected.forEach(
                    (action, replacement) -> {
                        ApiAction element = api.getApiAction(action);
                        assertThat(action, element.isDeprecated(), is(equalTo(true)));
                        assertThat(
                                action,
                                element.getDeprecatedDescription(),
                                is(
                                        equalTo(
                                                "Use globalScanPolicy/action/"
                                                        + replacement
                                                        + " instead.")));
                    });
            assertThat(api.getApiAction("scan").isDeprecated(), is(equalTo(false)));
            assertThat(api.getApiAction("stop").isDeprecated(), is(equalTo(false)));
        }
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

    /**
     * Tests the redirection of the policy actions to the Global Scan Policy Manager API, when the
     * policies are provided by a {@link PluggableScanPolicyManager}.
     */
    @Nested
    class PluggablePolicy {

        private static final String POLICY = "Pluggable";

        private MockedStatic<API> apiStatic;
        private ApiImplementor globalScanPolicyApi;

        @BeforeEach
        void setUp() throws Exception {
            globalScanPolicyApi = mock(ApiImplementor.class);
            API zapApi = mock(API.class);
            given(zapApi.getImplementors())
                    .willReturn(Map.of("globalScanPolicy", globalScanPolicyApi));
            apiStatic = mockStatic(API.class);
            apiStatic.when(API::getInstance).thenReturn(zapApi);
            given(defaultScanPolicy.getName()).willReturn(POLICY);
            given(policyManager.getPluggableManager())
                    .willReturn(mock(PluggableScanPolicyManager.class));
            given(policyManager.getPolicy(POLICY)).willReturn(defaultScanPolicy);
            given(policyManager.getAllPolicyNames()).willReturn(List.of(POLICY, "Other"));
            given(policyManager.isLegalPolicyName(anyString())).willReturn(true);
        }

        @AfterEach
        void tearDown() {
            apiStatic.close();
        }

        @Test
        void shouldRedirectAddPolicy() throws Exception {
            // When
            api.handleApiAction(
                    "addScanPolicy",
                    params(
                            "scanPolicyName",
                            "New",
                            "alertThreshold",
                            "High",
                            "attackStrength",
                            ""));
            // Then
            verifyRedirect(
                    "addPolicy",
                    params("policyName", "New", "tool", "ascan", "alertThreshold", "High"));
            verify(policyManager, never()).savePolicy(any());
        }

        @Test
        void shouldNotRedirectAddPolicyWithExistingName() throws Exception {
            // When
            ApiException e =
                    assertThrows(
                            ApiException.class,
                            () ->
                                    api.handleApiAction(
                                            "addScanPolicy", params("scanPolicyName", "Other")));
            // Then
            assertThat(e.getType(), is(equalTo(ApiException.Type.ALREADY_EXISTS)));
            verifyNoInteractions(globalScanPolicyApi);
        }

        @Test
        void shouldRedirectUpdatePolicy() throws Exception {
            // When
            api.handleApiAction(
                    "updateScanPolicy", params("scanPolicyName", POLICY, "attackStrength", "Low"));
            // Then
            verifyRedirect(
                    "updatePolicy",
                    params("policyName", POLICY, "tool", "ascan", "attackStrength", "Low"));
        }

        @Test
        void shouldRedirectRemovePolicy() throws Exception {
            // When
            api.handleApiAction("removeScanPolicy", params("scanPolicyName", POLICY));
            // Then
            verifyRedirect("removePolicy", params("policyName", POLICY));
            verify(policyManager, never()).deletePolicy(anyString());
        }

        @Test
        void shouldRedirectImportPolicy() throws Exception {
            // Given
            File file = File.createTempFile("policy", ".policy");
            file.deleteOnExit();
            // When
            api.handleApiAction("importScanPolicy", params("path", file.getAbsolutePath()));
            // Then
            verifyRedirect("importLegacyPolicy", params("path", file.getAbsolutePath()));
        }

        @Test
        void shouldRedirectEnableAndDisableAllScanners() throws Exception {
            // Given
            JSONObject params = params("scanPolicyName", POLICY);
            // When
            api.handleApiAction("enableAllScanners", params);
            api.handleApiAction("disableAllScanners", params);
            // Then
            verifyRedirect("enableAllRules", params("policyName", POLICY, "tool", "ascan"));
            verifyRedirect("disableAllRules", params("policyName", POLICY, "tool", "ascan"));
            verify(defaultPluginFactory, never()).setAllPluginEnabled(anyBoolean());
        }

        @Test
        void shouldRedirectEnableAndDisableScanners() throws Exception {
            // When
            api.handleApiAction("enableScanners", params("ids", "2, 4"));
            api.handleApiAction("disableScanners", params("ids", "4"));
            // Then
            verifyRedirect(
                    "enableRules",
                    params("policyName", POLICY, "tool", "ascan", "ruleIds", "2, 4"));
            verifyRedirect(
                    "disableRules", params("policyName", POLICY, "tool", "ascan", "ruleIds", "4"));
        }

        @Test
        void shouldRedirectSetEnabledPolicies() throws Exception {
            // When
            api.handleApiAction("setEnabledPolicies", params("ids", "1, 2"));
            // Then
            verifyRedirect(
                    "setEnabledCategories",
                    params(
                            "policyName",
                            POLICY,
                            "tool",
                            "ascan",
                            "categoryKeys",
                            "browser,server"));
        }

        @Test
        void shouldRedirectSetScannerThresholdAndStrength() throws Exception {
            // Given
            Plugin plugin = mock(Plugin.class);
            given(plugin.getId()).willReturn(7);
            given(defaultPluginFactory.getPlugin(7)).willReturn(plugin);
            // When
            api.handleApiAction(
                    "setScannerAlertThreshold", params("id", "7", "alertThreshold", "Off"));
            api.handleApiAction(
                    "setScannerAttackStrength", params("id", "7", "attackStrength", "High"));
            // Then
            verifyRedirect(
                    "setRuleThreshold",
                    params(
                            "policyName",
                            POLICY,
                            "tool",
                            "ascan",
                            "ruleId",
                            "7",
                            "alertThreshold",
                            "OFF"));
            verifyRedirect(
                    "setRuleStrength",
                    params(
                            "policyName",
                            POLICY,
                            "tool",
                            "ascan",
                            "ruleId",
                            "7",
                            "attackStrength",
                            "HIGH"));
            verify(plugin, never()).setAlertThreshold(any());
            verify(plugin, never()).setAttackStrength(any());
        }

        @Test
        void shouldRedirectSetPolicyThresholdAndStrength() throws Exception {
            // When
            api.handleApiAction(
                    "setPolicyAlertThreshold",
                    params("id", String.valueOf(Category.INJECTION), "alertThreshold", "Low"));
            api.handleApiAction(
                    "setPolicyAttackStrength",
                    params("id", String.valueOf(Category.SERVER), "attackStrength", "Insane"));
            // Then
            verifyRedirect(
                    "setCategoryThreshold",
                    params(
                            "policyName",
                            POLICY,
                            "tool",
                            "ascan",
                            "categoryKey",
                            "inject",
                            "alertThreshold",
                            "LOW"));
            verifyRedirect(
                    "setCategoryStrength",
                    params(
                            "policyName",
                            POLICY,
                            "tool",
                            "ascan",
                            "categoryKey",
                            "server",
                            "attackStrength",
                            "INSANE"));
        }

        @Test
        void shouldKeepValidatingBeforeRedirecting() throws Exception {
            // When
            ApiException unknownScanner =
                    assertThrows(
                            ApiException.class,
                            () ->
                                    api.handleApiAction(
                                            "setScannerAlertThreshold",
                                            params("id", "999", "alertThreshold", "Off")));
            ApiException invalidCategory =
                    assertThrows(
                            ApiException.class,
                            () ->
                                    api.handleApiAction(
                                            "setPolicyAlertThreshold",
                                            params("id", "-1", "alertThreshold", "Low")));
            // Then
            assertThat(unknownScanner.getType(), is(equalTo(ApiException.Type.DOES_NOT_EXIST)));
            assertThat(invalidCategory.getType(), is(equalTo(ApiException.Type.ILLEGAL_PARAMETER)));
            verifyNoInteractions(globalScanPolicyApi);
        }

        @Test
        void shouldPropagateFailuresOfRedirectedAction() throws Exception {
            // Given
            doThrow(new ApiException(ApiException.Type.DOES_NOT_EXIST, "ruleIds"))
                    .when(globalScanPolicyApi)
                    .handleApiAction(anyString(), any());
            // When
            ApiException e =
                    assertThrows(
                            ApiException.class,
                            () -> api.handleApiAction("enableScanners", params("ids", "1")));
            // Then
            assertThat(e.getType(), is(equalTo(ApiException.Type.DOES_NOT_EXIST)));
        }

        @Test
        void shouldNotRedirectIfPoliciesNotProvidedByPluggableManager() throws Exception {
            // Given
            given(policyManager.getPluggableManager()).willReturn(null);
            // When
            api.handleApiAction("enableAllScanners", params("scanPolicyName", POLICY));
            // Then
            verifyNoInteractions(globalScanPolicyApi);
            verify(defaultPluginFactory).setAllPluginEnabled(true);
            verify(defaultScanPolicy).save();
        }

        private void verifyRedirect(String action, JSONObject expectedParams) throws Exception {
            verify(globalScanPolicyApi).handleApiAction(action, expectedParams);
        }

        private static JSONObject params(String... keysAndValues) {
            JSONObject params = new JSONObject();
            for (int i = 0; i < keysAndValues.length; i += 2) {
                params.put(keysAndValues[i], keysAndValues[i + 1]);
            }
            return params;
        }
    }
}
