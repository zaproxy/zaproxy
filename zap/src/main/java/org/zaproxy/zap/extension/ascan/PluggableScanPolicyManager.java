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

/**
 * Extension point allowing add-ons to be notified when active scan rules are available.
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
}
