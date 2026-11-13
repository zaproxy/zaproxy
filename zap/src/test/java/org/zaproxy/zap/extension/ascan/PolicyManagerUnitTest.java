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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.AbstractHostPlugin;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.zaproxy.zap.WithConfigsTest;

/** Unit test for {@link PolicyManager}. */
class PolicyManagerUnitTest extends WithConfigsTest {

    private static final String POLICY_EXTENSION = ".policy";

    @Test
    void shouldCreatePolicyFileIfSavingPolicy() throws Exception {
        // Given
        ExtensionActiveScan activeScan = new ExtensionActiveScan();
        PolicyManager policyManager = new PolicyManager(activeScan);
        policyManager.init();

        ScanPolicy policy = new ScanPolicy();
        policy.setName("test");

        File file = new File(Constant.getPoliciesDir(), policy.getName() + POLICY_EXTENSION);

        // When
        policyManager.savePolicy(policy);
        // Then
        assertTrue(file.exists());
    }

    @Test
    void shouldChangePolicyFileNameIfModifyingPolicyName() throws Exception {
        // Given
        ExtensionActiveScan activeScan = new ExtensionActiveScan();
        PolicyManager policyManager = new PolicyManager(activeScan);
        policyManager.init();

        ScanPolicy policy = new ScanPolicy();
        policy.setName("test");

        File file = new File(Constant.getPoliciesDir(), policy.getName() + POLICY_EXTENSION);

        ScanPolicy newPolicy = new ScanPolicy();
        newPolicy.setName("newTest");
        File newFile = new File(Constant.getPoliciesDir(), newPolicy.getName() + POLICY_EXTENSION);
        policyManager.savePolicy(policy);

        // When
        policyManager.savePolicy(newPolicy, "test");
        // Then
        assertFalse(file.exists());
        assertTrue(newFile.exists());
    }

    @Test
    void shouldChangePolicyNameIfModifyingUpperOrLowerCase() throws Exception {
        // Given
        ExtensionActiveScan activeScan = new ExtensionActiveScan();
        PolicyManager policyManager = new PolicyManager(activeScan);
        policyManager.init();

        ScanPolicy policy = new ScanPolicy();
        policy.setName("test");

        File file = new File(Constant.getPoliciesDir(), policy.getName() + POLICY_EXTENSION);

        ScanPolicy newPolicy = new ScanPolicy();
        newPolicy.setName("TeSt");
        File newFile = new File(Constant.getPoliciesDir(), newPolicy.getName() + POLICY_EXTENSION);
        policyManager.savePolicy(policy);

        // When
        policyManager.savePolicy(newPolicy, "test");
        // Then
        assertFalse(existsFileCaseSensitive(file.getName()));
        assertTrue(newFile.exists());
    }

    @Test
    void shouldUseNamesFromPluggableManagerIfActive() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("b", "a");
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "b"));
        // When
        List<String> names = policyManager.getAllPolicyNames();
        // Then
        assertEquals(List.of("a", "b"), names);
    }

    @Test
    void shouldApplyPluggablePolicyToLoadedPolicy() throws Exception {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("a", "b");
        when(pluggable.applyPolicy(eq("b"), any()))
                .thenAnswer(
                        inv -> {
                            PluginFactory factory = inv.getArgument(1);
                            for (Plugin plugin : factory.getAllPlugin()) {
                                plugin.setAlertThreshold(AlertThreshold.HIGH);
                            }
                            return true;
                        });
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "b"));
        // When
        ScanPolicy policy = policyManager.getPolicy("b");
        // Then
        assertEquals("b", policy.getName());
        assertTrue(
                policy.getPluginFactory().getAllPlugin().stream()
                        .allMatch(p -> p.getAlertThreshold() == AlertThreshold.HIGH));
    }

    @Test
    void shouldUseConfiguredPolicyAsDefaultIfProvidedByPluggableManager() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("a", "b");
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "b", "a"));
        // When
        ScanPolicy policy = policyManager.getDefaultScanPolicy();
        // Then
        assertEquals("b", policy.getName());
    }

    @Test
    void shouldUsePluggableDefaultIfConfiguredPolicyNotProvided() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("a", "b");
        when(pluggable.getDefaultPolicyName()).thenReturn("b");
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "other", "a"));
        // When
        ScanPolicy policy = policyManager.getDefaultScanPolicy();
        // Then
        assertEquals("b", policy.getName());
    }

    @Test
    void shouldFailToLoadUnknownPluggablePolicy() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("a");
        when(pluggable.applyPolicy(eq("unknown"), any())).thenReturn(false);
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "a"));
        // When / Then
        assertThrows(ConfigurationException.class, () -> policyManager.getPolicy("unknown"));
    }

    @Test
    void shouldKeepNameDefaultsAndRuleSettingsWhenCloningPluggablePolicy() throws Exception {
        // Given
        TestPlugin plugin = loadTestPlugin(new TestPlugin1());
        try {
            PluggableScanPolicyManager pluggable = pluggableManager("a");
            when(pluggable.getDefaultThreshold("a")).thenReturn(AlertThreshold.HIGH);
            when(pluggable.getDefaultStrength("a")).thenReturn(AttackStrength.LOW);
            PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "a"));
            ScanPolicy policy = policyManager.getPolicy("a");
            policy.getPluginFactory().getPlugin(9001).setAlertThreshold(AlertThreshold.OFF);
            // When
            ScanPolicy clone = policy.clonePolicy();
            // Then
            assertEquals("a", clone.getName());
            assertEquals(AlertThreshold.HIGH, clone.getDefaultThreshold());
            assertEquals(AttackStrength.LOW, clone.getDefaultStrength());
            assertEquals(
                    AlertThreshold.OFF,
                    clone.getPluginFactory().getPlugin(9001).getAlertThreshold());
            assertFalse(clone.getPluginFactory().getPlugin(9001).isEnabled());
        } finally {
            PluginFactory.unloadedPlugin(plugin);
        }
    }

    @Test
    void shouldNotSavePolicyIfPluggableManagerActive() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("a");
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "a"));
        ScanPolicy policy = new ScanPolicy();
        policy.setName("saved-by-test");
        File file = new File(Constant.getPoliciesDir(), "saved-by-test" + POLICY_EXTENSION);
        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> policyManager.savePolicy(policy));
        assertThrows(
                UnsupportedOperationException.class, () -> policyManager.savePolicy(policy, "a"));
        assertFalse(file.exists());
    }

    @Test
    void shouldNotDeletePolicyIfPluggableManagerActive() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("a", "b");
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "a"));
        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> policyManager.deletePolicy("b"));
    }

    @Test
    void shouldNotImportPolicyIfPluggableManagerActive() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("a");
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "a"));
        File file = new File("imported.policy");
        // When / Then
        assertThrows(UnsupportedOperationException.class, () -> policyManager.importPolicy(file));
    }

    @Test
    void shouldTakeIntoAccountPluggablePoliciesWhenCheckingIfPolicyExists() {
        // Given
        PluggableScanPolicyManager pluggable = pluggableManager("only-in-pluggable");
        PolicyManager policyManager = new PolicyManager(activeScanWith(pluggable, "a", "a"));
        policyManager.init();
        // When / Then
        assertTrue(policyManager.hasPolicy("only-in-pluggable"));
        assertFalse(policyManager.hasPolicy("not-anywhere"));
    }

    private static TestPlugin loadTestPlugin(TestPlugin plugin) {
        PluginFactory.loadedPlugin(plugin);
        return plugin;
    }

    /** A plugin with ID 9001, public with a default constructor as policies create copies. */
    public static class TestPlugin1 extends TestPlugin {
        @Override
        public int getId() {
            return 9001;
        }
    }

    /** A plugin with ID 9002, public with a default constructor as policies create copies. */
    public static class TestPlugin2 extends TestPlugin {
        @Override
        public int getId() {
            return 9002;
        }
    }

    /** Base for the test plugins. */
    public abstract static class TestPlugin extends AbstractHostPlugin {

        @Override
        public String getName() {
            return "Test " + getId();
        }

        @Override
        public String[] getDependency() {
            return null;
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public int getCategory() {
            return Category.MISC;
        }

        @Override
        public String getSolution() {
            return "";
        }

        @Override
        public String getReference() {
            return "";
        }

        @Override
        public void init() {}

        @Override
        public void scan() {}
    }

    private static PluggableScanPolicyManager pluggableManager(String... names) {
        PluggableScanPolicyManager pluggable = mock(PluggableScanPolicyManager.class);
        lenient().when(pluggable.getPolicyNames()).thenReturn(List.of(names));
        lenient().when(pluggable.applyPolicy(any(), any())).thenReturn(true);
        return pluggable;
    }

    private static ExtensionActiveScan activeScanWith(
            PluggableScanPolicyManager pluggable, String defaultPolicy, String attackPolicy) {
        ExtensionActiveScan activeScan = mock(ExtensionActiveScan.class);
        ScannerParam param = mock(ScannerParam.class);
        lenient().when(param.getDefaultPolicy()).thenReturn(defaultPolicy);
        lenient().when(param.getAttackPolicy()).thenReturn(attackPolicy);
        lenient().when(activeScan.getScannerParam()).thenReturn(param);
        lenient().when(activeScan.getActivePluggableScanPolicyManager()).thenReturn(pluggable);
        return activeScan;
    }

    // Helper Function for checking existing file(case-sensitive). For files.exists() test and Test
    // are the same on some systems
    private static boolean existsFileCaseSensitive(String filename) {
        String[] files = Constant.getPoliciesDir().list();
        for (String file : files) {
            if (file.equals(filename)) {
                return true;
            }
        }
        return false;
    }
}
