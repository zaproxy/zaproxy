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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.util.List;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.utils.I18N;

/**
 * Unit test for the redirection of the policy actions of {@link ActiveScanAPI} to the Global Scan
 * Policy Manager API, when the policies are provided by a {@link PluggableScanPolicyManager}.
 */
class ActiveScanAPIPluggablePolicyUnitTest {

    private static final String POLICY = "Pluggable";

    private ApiImplementor globalScanPolicyApi;
    private PluginFactory pluginFactory;
    private ScanPolicy scanPolicy;
    private PolicyManager policyManager;
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
    void setUp() throws Exception {
        globalScanPolicyApi = mock(ApiImplementor.class);
        pluginFactory = mock(PluginFactory.class);
        scanPolicy = mock(ScanPolicy.class);
        given(scanPolicy.getName()).willReturn(POLICY);
        given(scanPolicy.getPluginFactory()).willReturn(pluginFactory);
        policyManager = mock(PolicyManager.class);
        given(policyManager.getPluggableManager())
                .willReturn(mock(PluggableScanPolicyManager.class));
        given(policyManager.getDefaultScanPolicy()).willReturn(scanPolicy);
        given(policyManager.getPolicy(POLICY)).willReturn(scanPolicy);
        given(policyManager.getAllPolicyNames()).willReturn(List.of(POLICY, "Other"));
        given(policyManager.isLegalPolicyName(anyString())).willReturn(true);
        ExtensionActiveScan extension = mock(ExtensionActiveScan.class);
        given(extension.getPolicyManager()).willReturn(policyManager);
        api = new ActiveScanAPI(extension);
        api.setGlobalScanPolicyApiSupplier(() -> globalScanPolicyApi);
    }

    @Test
    void shouldRedirectAddPolicy() throws Exception {
        // When
        api.handleApiAction(
                "addScanPolicy",
                params("scanPolicyName", "New", "alertThreshold", "High", "attackStrength", ""));
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
        verify(pluginFactory, never()).setAllPluginEnabled(anyBoolean());
    }

    @Test
    void shouldRedirectEnableAndDisableScanners() throws Exception {
        // When
        api.handleApiAction("enableScanners", params("ids", "2, 4"));
        api.handleApiAction("disableScanners", params("ids", "4"));
        // Then
        verifyRedirect(
                "enableRules", params("policyName", POLICY, "tool", "ascan", "ruleIds", "2, 4"));
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
                params("policyName", POLICY, "tool", "ascan", "categoryKeys", "browser,server"));
    }

    @Test
    void shouldRedirectSetScannerThresholdAndStrength() throws Exception {
        // Given
        Plugin plugin = mock(Plugin.class);
        given(plugin.getId()).willReturn(7);
        given(pluginFactory.getPlugin(7)).willReturn(plugin);
        // When
        api.handleApiAction("setScannerAlertThreshold", params("id", "7", "alertThreshold", "Off"));
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
        verify(pluginFactory).setAllPluginEnabled(true);
        verify(scanPolicy).save();
    }

    @Test
    void shouldFailIfGlobalScanPolicyApiNotAvailable() throws Exception {
        // Given
        api.setGlobalScanPolicyApiSupplier(() -> null);
        // When
        ApiException e =
                assertThrows(
                        ApiException.class,
                        () ->
                                api.handleApiAction(
                                        "disableAllScanners", params("scanPolicyName", POLICY)));
        // Then
        assertThat(e.getType(), is(equalTo(ApiException.Type.INTERNAL_ERROR)));
        verifyNoInteractions(globalScanPolicyApi);
        verify(pluginFactory, never()).setAllPluginEnabled(anyBoolean());
        verify(scanPolicy, never()).save();
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
