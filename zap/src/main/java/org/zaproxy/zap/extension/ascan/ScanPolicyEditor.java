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

import javax.swing.JComponent;

/**
 * An editor of a scan policy, shown in the Policy tab of the active scan dialog instead of the core
 * policy panels.
 *
 * <p>The edits apply only to the scan being configured, they do not change the saved policy.
 *
 * <p>Provided by {@link PluggableScanPolicyManager#createScanPolicyEditor(java.awt.Window)}.
 *
 * @since 2.18.0
 */
public interface ScanPolicyEditor {

    /**
     * Gets the component shown in the Policy tab.
     *
     * @return the component, never {@code null}.
     */
    JComponent getPanel();

    /**
     * Sets the policy to edit, discarding any previous edits.
     *
     * @param scanPolicy the policy selected in the dialog, as loaded from the {@link
     *     PluggableScanPolicyManager}.
     */
    void setScanPolicy(ScanPolicy scanPolicy);

    /**
     * Gets the policy to use for the scan: the policy set with {@link #setScanPolicy(ScanPolicy)},
     * with the edits applied.
     *
     * @return the policy for the scan, never {@code null}.
     */
    ScanPolicy getScanPolicy();
}
