/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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

import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Plugin;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link ScanPolicy}. */
class ScanPolicyUnitTest extends WithConfigsTest {

    private static final String DEFAULT_SCANNER_LEVEL_KEY = "scanner.level";
    private static final String DEFAULT_SCANNER_STRENGTH_KEY = "scanner.strength";

    @Test
    void shouldUseValidDefaultScannerLevelFromConfig() throws Exception {
        // Given
        ZapXmlConfiguration conf = new ZapXmlConfiguration();
        conf.setProperty(DEFAULT_SCANNER_LEVEL_KEY, Plugin.AlertThreshold.HIGH.name());
        // When
        ScanPolicy scanPolicy = new ScanPolicy(conf);
        // Then
        assertThat(scanPolicy.getDefaultThreshold(), is(equalTo(Plugin.AlertThreshold.HIGH)));
    }

    @Test
    void shouldUseMediumIfInvalidDefaultScannerLevelFromConfig() throws Exception {
        // Given
        ZapXmlConfiguration conf = new ZapXmlConfiguration();
        conf.setProperty(DEFAULT_SCANNER_LEVEL_KEY, "NotValid");
        // When
        ScanPolicy scanPolicy = new ScanPolicy(conf);
        // Then
        assertThat(scanPolicy.getDefaultThreshold(), is(equalTo(Plugin.AlertThreshold.MEDIUM)));
    }

    @Test
    void shouldUseValidDefaultScannerStrengthFromConfig() throws Exception {
        // Given
        ZapXmlConfiguration conf = new ZapXmlConfiguration();
        conf.setProperty(DEFAULT_SCANNER_STRENGTH_KEY, Plugin.AttackStrength.LOW.name());
        // When
        ScanPolicy scanPolicy = new ScanPolicy(conf);
        // Then
        assertThat(scanPolicy.getDefaultStrength(), is(equalTo(Plugin.AttackStrength.LOW)));
    }

    @Test
    void shouldUseMediumIfInvalidDefaultScannerStrengthFromConfig() throws Exception {
        // Given
        ZapXmlConfiguration conf = new ZapXmlConfiguration();
        conf.setProperty(DEFAULT_SCANNER_STRENGTH_KEY, "NotValid");
        // When
        ScanPolicy scanPolicy = new ScanPolicy(conf);
        // Then
        assertThat(scanPolicy.getDefaultStrength(), is(equalTo(Plugin.AttackStrength.MEDIUM)));
    }

    @Test
    void shouldThrowIfDefaultScannerLevelIsSetToDefault() throws Exception {
        // Given
        ScanPolicy scanPolicy = new ScanPolicy();
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> scanPolicy.setDefaultThreshold(Plugin.AlertThreshold.DEFAULT));
    }

    @Test
    void shouldThrowIfDefaultScannerStrengthIsSetToDefault() throws Exception {
        // Given
        ScanPolicy scanPolicy = new ScanPolicy();
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> scanPolicy.setDefaultStrength(Plugin.AttackStrength.DEFAULT));
    }

    @Test
    void shouldUseMediumIfDefaultScannerLevelFromConfigIsDefault() throws Exception {
        // Given
        ZapXmlConfiguration conf = new ZapXmlConfiguration();
        conf.setProperty(DEFAULT_SCANNER_LEVEL_KEY, Plugin.AlertThreshold.DEFAULT.name());
        // When
        ScanPolicy scanPolicy = new ScanPolicy(conf);
        // Then
        assertThat(scanPolicy.getDefaultThreshold(), is(equalTo(Plugin.AlertThreshold.MEDIUM)));
    }

    @Test
    void shouldUseMediumIfDefaultScannerStrengthFromConfigIsDefault() throws Exception {
        // Given
        ZapXmlConfiguration conf = new ZapXmlConfiguration();
        conf.setProperty(DEFAULT_SCANNER_STRENGTH_KEY, Plugin.AttackStrength.DEFAULT.name());
        // When
        ScanPolicy scanPolicy = new ScanPolicy(conf);
        // Then
        assertThat(scanPolicy.getDefaultStrength(), is(equalTo(Plugin.AttackStrength.MEDIUM)));
    }
}
