/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.htmlparser.jericho.Source;
import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link PluginPassiveScanner}. */
public class PluginPassiveScannerUnitTest {

    private PluginPassiveScanner scanner;

    @BeforeEach
    public void setUp() throws Exception {
        scanner = new TestPluginPassiveScanner();
    }

    @Test
    public void shouldHaveUndefinedPluginIdByDefault() {
        assertThat(scanner.getPluginId(), is(equalTo(-1)));
    }

    @Test
    public void shouldHaveUnknownStatusByDefault() {
        assertThat(scanner.getStatus(), is(equalTo(AddOn.Status.unknown)));
    }

    @Test
    public void shouldFailToSetNullStatus() {
        // Given
        AddOn.Status status = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> scanner.setStatus(status));
    }

    @Test
    public void shouldSetValidStatus() {
        // Given
        AddOn.Status status = AddOn.Status.beta;
        // When
        scanner.setStatus(status);
        // Then
        assertThat(scanner.getStatus(), is(equalTo(status)));
    }

    @Test
    public void shouldBeEnabledByDefault() {
        assertThat(scanner.isEnabled(), is(equalTo(true)));
    }

    @Test
    public void shouldChangeEnabledState() {
        // Given
        boolean enabled = true;
        // When
        scanner.setEnabled(enabled);
        // Then
        assertThat(scanner.isEnabled(), is(equalTo(enabled)));
    }

    @Test
    public void shouldHaveMediumAsDefaultAlertThreshold() {
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.MEDIUM)));
    }

    @Test
    public void shouldFailToSetNullAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class, () -> scanner.setAlertThreshold(alertThreshold));
    }

    @Test
    public void shouldSetValidAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = AlertThreshold.HIGH;
        // When
        scanner.setAlertThreshold(alertThreshold);
        // Then
        assertThat(scanner.getAlertThreshold(), is(equalTo(alertThreshold)));
    }

    @Test
    public void shouldFailToSetNullDefaultAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> scanner.setDefaultAlertThreshold(alertThreshold));
    }

    @Test
    public void shouldFailToSetDefaultToDefaultAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = AlertThreshold.DEFAULT;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> scanner.setDefaultAlertThreshold(alertThreshold));
    }

    @Test
    public void shouldSetValidDefaultAlertThreshold() {
        // Given
        scanner.setAlertThreshold(AlertThreshold.DEFAULT);
        AlertThreshold alertThreshold = AlertThreshold.HIGH;
        // When
        scanner.setDefaultAlertThreshold(alertThreshold);
        // Then
        assertThat(scanner.getAlertThreshold(), is(equalTo(alertThreshold)));
    }

    @Test
    public void shouldHaveNoConfigurationByDefault() {
        assertThat(scanner.getConfig(), is(nullValue()));
    }

    @Test
    public void shouldFailToSetNullConfiguration() {
        // Given
        Configuration configuration = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> scanner.setConfig(configuration));
    }

    @Test
    public void shouldNotChangeEnabledStateOrAlertThresholdIfConfigurationSetIsEmpty() {
        Configuration configuration = createEmptyConfiguration();
        // given
        scanner.setEnabled(false);
        scanner.setAlertThreshold(AlertThreshold.HIGH);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.isEnabled(), is(false));
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.HIGH)));
    }

    @Test
    public void
            shouldNotChangeEnabledStateOrAlertThresholdIfNoApplicableDataIsPresentInConfigurationSet() {
        Configuration configuration =
                createConfiguration(Integer.MIN_VALUE, Boolean.TRUE, AlertThreshold.LOW);
        // given
        scanner.setEnabled(false);
        scanner.setAlertThreshold(AlertThreshold.HIGH);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.isEnabled(), is(false));
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.HIGH)));
    }

    @Test
    public void shouldEnableByDefaultIfNotSpecifiedInConfigurationSet() {
        // given
        Configuration configuration =
                createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, null, null);
        scanner.setEnabled(false);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.isEnabled(), is(true));
    }

    @Test
    public void shouldDisableIfSpecifiedInConfigurationSet() {
        // given
        Configuration configuration =
                createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, Boolean.FALSE, null);
        scanner.setEnabled(true);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.isEnabled(), is(false));
    }

    @Test
    public void shouldUseDefaultAlertThresholdIfNotSpecifiedInConfigurationSet() {
        // given
        Configuration configuration =
                createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, Boolean.TRUE, null);
        scanner.setDefaultAlertThreshold(AlertThreshold.HIGH);
        scanner.setAlertThreshold(AlertThreshold.LOW);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.HIGH)));
    }

    @Test
    public void shouldUseAlertThresholdSpecifiedInConfigurationSet() {
        // given
        Configuration configuration =
                createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, null, AlertThreshold.HIGH);
        scanner.setAlertThreshold(AlertThreshold.LOW);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.HIGH)));
    }

    @Test
    public void shouldUseClassnameToReadConfigurationSet() {
        // given
        Configuration configuration =
                createConfiguration(
                        TestPluginPassiveScanner.class.getCanonicalName(),
                        Boolean.FALSE,
                        AlertThreshold.HIGH);
        scanner.setEnabled(true);
        scanner.setAlertThreshold(AlertThreshold.LOW);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.isEnabled(), is(false));
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.HIGH)));
    }

    @Test
    public void shouldReadConfigurationSetEvenIfThereAreMultipleUnrelatedEntries() {
        // given
        Configuration configuration = createEmptyConfiguration();
        addConfiguration(configuration, 0, 10, null, null);
        addConfiguration(configuration, 1, "TestClassName", Boolean.TRUE, AlertThreshold.HIGH);
        addConfiguration(
                configuration,
                2,
                TestPluginPassiveScanner.PLUGIN_ID,
                Boolean.FALSE,
                AlertThreshold.MEDIUM);
        addConfiguration(configuration, 3, 1011, null, AlertThreshold.LOW);
        addConfiguration(configuration, 4, "OtherTestClassName", Boolean.FALSE, AlertThreshold.OFF);
        scanner.setEnabled(true);
        scanner.setAlertThreshold(AlertThreshold.LOW);
        // when
        scanner.setConfig(configuration);
        // then
        assertThat(scanner.isEnabled(), is(false));
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.MEDIUM)));
    }

    @Test
    public void shouldFailToSaveByDefault() {
        // Given scanner
        // When / Then
        assertThrows(IllegalStateException.class, () -> scanner.save());
    }

    @Test
    public void shouldNotPersistEnabledStateOnSaveIfEnabled() {
        // given
        Configuration configuration = createEmptyConfiguration();
        scanner.setConfig(configuration);
        scanner.setEnabled(true);
        // when
        scanner.save();
        // then
        assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(false));
    }

    @Test
    public void shouldPersistEnabledStateOnSaveIfDisabled() {
        // Given
        Configuration configuration = createEmptyConfiguration();
        scanner.setConfig(configuration);
        scanner.setEnabled(false);
        // When
        scanner.save();
        // Then
        assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(true));
        assertThat(configuration.getBoolean("pscans.pscanner(0).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(true));
        assertThat(
                configuration.getInt("pscans.pscanner(0).id"),
                is(equalTo(TestPluginPassiveScanner.PLUGIN_ID)));
    }

    @Test
    public void shouldNotPersistAlertThresholdOnSaveIfDefaultValue() {
        // Given
        Configuration configuration = createEmptyConfiguration();
        scanner.setConfig(configuration);
        scanner.setAlertThreshold(AlertThreshold.MEDIUM);
        scanner.setEnabled(true);
        // When
        scanner.save();
        // Then
        assertThat(configuration.containsKey("pscans.pscanner(0).alertthreshold"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(false));
    }

    @Test
    public void shouldPersistAlertThresholdOnSaveIfNotDefaultValue() {
        // Given
        Configuration configuration = createEmptyConfiguration();
        scanner.setConfig(configuration);
        scanner.setAlertThreshold(AlertThreshold.HIGH);
        scanner.setEnabled(true);
        // When
        scanner.save();
        // Then
        assertThat(configuration.containsKey("pscans.pscanner(0).alertthreshold"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(0).alertthreshold"),
                is(equalTo(AlertThreshold.HIGH.name())));
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(true));
        assertThat(
                configuration.getInt("pscans.pscanner(0).id"),
                is(equalTo(TestPluginPassiveScanner.PLUGIN_ID)));
    }

    @Test
    public void shouldRemoveExistingConfigDataOnSaveIfDefaultValues() {
        // Given
        Configuration configuration =
                createConfiguration(
                        TestPluginPassiveScanner.PLUGIN_ID, Boolean.FALSE, AlertThreshold.HIGH);
        scanner.setConfig(configuration);
        scanner.setEnabled(true);
        scanner.setAlertThreshold(AlertThreshold.MEDIUM);
        // When
        scanner.save();
        // Then
        assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).alertthreshold"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(false));
    }

    @Test
    public void shouldRemoveClassnameConfigEntryOnSave() {
        // Given
        Configuration configuration =
                createConfiguration(
                        TestPluginPassiveScanner.class.getCanonicalName(),
                        Boolean.FALSE,
                        AlertThreshold.HIGH);
        scanner.setConfig(configuration);
        scanner.setEnabled(true);
        scanner.setAlertThreshold(AlertThreshold.MEDIUM);
        // When
        scanner.save();
        // Then
        assertThat(configuration.containsKey("pscans.pscanner(0).classname"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).alertthreshold"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(false));
    }

    @Test
    public void shouldPersistConfigsOnSaveEvenIfThereAreMultipleUnrelatedEntries() {
        // Given
        Configuration configuration = createEmptyConfiguration();
        addConfiguration(configuration, 0, 10, null, null);
        addConfiguration(configuration, 1, "TestClassName", Boolean.TRUE, AlertThreshold.HIGH);
        addConfiguration(
                configuration,
                2,
                TestPluginPassiveScanner.PLUGIN_ID,
                Boolean.FALSE,
                AlertThreshold.MEDIUM);
        addConfiguration(configuration, 3, 1011, null, AlertThreshold.LOW);
        scanner.setConfig(configuration);
        scanner.setEnabled(false);
        scanner.setAlertThreshold(AlertThreshold.LOW);
        // When
        scanner.save();
        // Then
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(true));
        assertThat(configuration.getInt("pscans.pscanner(0).id"), is(equalTo(10)));
        assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).alertthreshold"), is(false));

        assertThat(configuration.containsKey("pscans.pscanner(1).id"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(1).classname"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(1).classname"),
                is(equalTo("TestClassName")));
        assertThat(configuration.containsKey("pscans.pscanner(1).enabled"), is(true));
        assertThat(configuration.getBoolean("pscans.pscanner(1).enabled"), is(true));
        assertThat(configuration.containsKey("pscans.pscanner(1).alertthreshold"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(1).alertthreshold"),
                is(equalTo(AlertThreshold.HIGH.name())));

        assertThat(configuration.containsKey("pscans.pscanner(2).id"), is(true));
        assertThat(configuration.getInt("pscans.pscanner(2).id"), is(equalTo(1011)));
        assertThat(configuration.containsKey("pscans.pscanner(2).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(2).alertthreshold"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(2).alertthreshold"),
                is(equalTo(AlertThreshold.LOW.name())));

        assertThat(configuration.containsKey("pscans.pscanner(3).id"), is(true));
        assertThat(
                configuration.getInt("pscans.pscanner(3).id"),
                is(equalTo(TestPluginPassiveScanner.PLUGIN_ID)));
        assertThat(configuration.containsKey("pscans.pscanner(3).enabled"), is(true));
        assertThat(configuration.getBoolean("pscans.pscanner(3).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(3).alertthreshold"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(3).alertthreshold"),
                is(equalTo(AlertThreshold.LOW.name())));

        assertThat(configuration.containsKey("pscans.pscanner(4).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(4).alertthreshold"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(5).id"), is(false));
    }

    @Test
    public void
            shouldRemoveExistingConfigDataOnSaveIfDefaultValuesEvenIfThereAreMultipleUnrelatedEntries() {
        // Given
        Configuration configuration = createEmptyConfiguration();
        addConfiguration(configuration, 0, 10, null, null);
        addConfiguration(configuration, 1, "TestClassName", Boolean.TRUE, AlertThreshold.HIGH);
        addConfiguration(
                configuration,
                2,
                TestPluginPassiveScanner.PLUGIN_ID,
                Boolean.FALSE,
                AlertThreshold.MEDIUM);
        addConfiguration(configuration, 3, 1011, null, AlertThreshold.LOW);
        scanner.setConfig(configuration);
        scanner.setEnabled(true);
        scanner.setAlertThreshold(AlertThreshold.MEDIUM);
        // When
        scanner.save();
        // Then
        assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(true));
        assertThat(configuration.getInt("pscans.pscanner(0).id"), is(equalTo(10)));
        assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(0).alertthreshold"), is(false));

        assertThat(configuration.containsKey("pscans.pscanner(1).id"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(1).classname"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(1).classname"),
                is(equalTo("TestClassName")));
        assertThat(configuration.containsKey("pscans.pscanner(1).enabled"), is(true));
        assertThat(configuration.getBoolean("pscans.pscanner(1).enabled"), is(true));
        assertThat(configuration.containsKey("pscans.pscanner(1).alertthreshold"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(1).alertthreshold"),
                is(equalTo(AlertThreshold.HIGH.name())));

        assertThat(configuration.containsKey("pscans.pscanner(2).id"), is(true));
        assertThat(configuration.getInt("pscans.pscanner(2).id"), is(equalTo(1011)));
        assertThat(configuration.containsKey("pscans.pscanner(2).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(2).alertthreshold"), is(true));
        assertThat(
                configuration.getString("pscans.pscanner(2).alertthreshold"),
                is(equalTo(AlertThreshold.LOW.name())));

        assertThat(configuration.containsKey("pscans.pscanner(3).enabled"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(3).alertthreshold"), is(false));
        assertThat(configuration.containsKey("pscans.pscanner(3).id"), is(false));
    }

    private static class TestPluginPassiveScanner extends PluginPassiveScanner {

        private static final int PLUGIN_ID = -1;

        @Override
        public void setParent(PassiveScanThread parent) {}

        @Override
        public void scanHttpResponseReceive(HttpMessage msg, int id, Source source) {}

        @Override
        public void scanHttpRequestSend(HttpMessage msg, int id) {}

        @Override
        public String getName() {
            return null;
        }
    }

    private static Configuration createEmptyConfiguration() {
        return new ZapXmlConfiguration();
    }

    private static Configuration createConfiguration(
            String classname, Boolean enabled, AlertThreshold alertThreshold) {
        ZapXmlConfiguration configuration = new ZapXmlConfiguration();
        setClassname(configuration, 0, classname);
        setProperties(configuration, 0, enabled, alertThreshold);
        return configuration;
    }

    private static void setClassname(Configuration configuration, int index, String classname) {
        configuration.setProperty("pscans.pscanner(" + index + ").classname", classname);
    }

    private static Configuration createConfiguration(
            int pluginId, Boolean enabled, AlertThreshold alertThreshold) {
        ZapXmlConfiguration configuration = new ZapXmlConfiguration();
        addConfiguration(configuration, 0, pluginId, enabled, alertThreshold);
        return configuration;
    }

    private static void addConfiguration(
            Configuration configuration,
            int index,
            String classname,
            Boolean enabled,
            AlertThreshold alertThreshold) {
        setClassname(configuration, index, classname);
        setProperties(configuration, index, enabled, alertThreshold);
    }

    private static void addConfiguration(
            Configuration configuration,
            int index,
            int pluginId,
            Boolean enabled,
            AlertThreshold alertThreshold) {
        configuration.setProperty("pscans.pscanner(" + index + ").id", pluginId);
        setProperties(configuration, index, enabled, alertThreshold);
    }

    private static void setProperties(
            Configuration configuration,
            int index,
            Boolean enabled,
            AlertThreshold alertThreshold) {
        if (enabled != null) {
            configuration.setProperty("pscans.pscanner(" + index + ").enabled", enabled);
        }
        if (alertThreshold != null) {
            configuration.setProperty(
                    "pscans.pscanner(" + index + ").alertthreshold", alertThreshold.name());
        }
    }
}
