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
package org.zaproxy.zap.extension.ascan;

import java.util.List;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;

/**
 * Extension point allowing add-ons to be notified when active scan rules are available and, when
 * ZAP is in {@link org.parosproxy.paros.Constant#isDevMode() dev mode}, to provide the scan
 * policies used by the active scanner.
 *
 * <p>The policy methods have default implementations so that implementations only need to override
 * what they support. The policies are only consulted once {@link #register(PolicyManager)} has been
 * called, until then (or if no manager is registered) the core scan policies are used.
 *
 * <p>Register an implementation via {@link
 * ExtensionActiveScan#registerPluggableScanPolicyManager(PluggableScanPolicyManager)}.
 *
 * @since 2.18.0
 */
public interface PluggableScanPolicyManager {

    /**
     * Called when active scan rules are available for processing.
     *
     * @param policyManager the active scan policy manager
     */
    void register(PolicyManager policyManager);

    /** Called when active scan rules are being torn down. */
    void unregister();

    /**
     * Gets the names of the scan policies provided by this manager.
     *
     * @return the policy names, empty if this manager does not provide policies.
     */
    default List<String> getPolicyNames() {
        return List.of();
    }

    /**
     * Gets the name of the policy to use by default for active scans.
     *
     * @return the name of the default policy, or {@code null} if none.
     */
    default String getDefaultPolicyName() {
        return null;
    }

    /**
     * Applies the policy with the given name to the given plugin factory, setting the enabled
     * state, threshold and strength of each plugin.
     *
     * @param name the name of the policy.
     * @param pluginFactory the plugin factory to configure.
     * @return {@code true} if the policy was applied, {@code false} if it does not exist or this
     *     manager does not provide policies.
     */
    default boolean applyPolicy(String name, PluginFactory pluginFactory) {
        return false;
    }

    /**
     * Gets the default threshold of the active scan rules of a policy.
     *
     * @param policyName the name of the policy.
     * @return the default threshold, or {@code null} if the policy does not define one.
     */
    default AlertThreshold getDefaultThreshold(String policyName) {
        return null;
    }

    /**
     * Gets the default strength of the active scan rules of a policy.
     *
     * @param policyName the name of the policy.
     * @return the default strength, or {@code null} if the policy does not define one.
     */
    default AttackStrength getDefaultStrength(String policyName) {
        return null;
    }

    /** Shows the dialog used to manage the policies, if supported. */
    default void showPolicyManager() {}
}
