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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
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
