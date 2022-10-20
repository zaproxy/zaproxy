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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link PluginPassiveScanner}. */
class PluginPassiveScannerUnitTest {

    private PluginPassiveScanner scanner;

    @BeforeEach
    void setUp() throws Exception {
        scanner = new TestPluginPassiveScanner();
    }

    @Test
    void shouldHaveUndefinedPluginIdByDefault() {
        assertThat(scanner.getPluginId(), is(equalTo(-1)));
    }

    @Test
    void shouldHaveUnknownStatusByDefault() {
        assertThat(scanner.getStatus(), is(equalTo(AddOn.Status.unknown)));
    }

    @Test
    void shouldFailToSetNullStatus() {
        // Given
        AddOn.Status status = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> scanner.setStatus(status));
    }

    @Test
    void shouldSetValidStatus() {
        // Given
        AddOn.Status status = AddOn.Status.beta;
        // When
        scanner.setStatus(status);
        // Then
        assertThat(scanner.getStatus(), is(equalTo(status)));
    }

    @Test
    void shouldBeEnabledByDefault() {
        assertThat(scanner.isEnabled(), is(equalTo(true)));
    }

    @Test
    void shouldChangeEnabledState() {
        // Given
        boolean enabled = true;
        // When
        scanner.setEnabled(enabled);
        // Then
        assertThat(scanner.isEnabled(), is(equalTo(enabled)));
    }

    @Test
    void shouldHaveMediumAsDefaultAlertThreshold() {
        assertThat(scanner.getAlertThreshold(), is(equalTo(AlertThreshold.MEDIUM)));
    }

    @Test
    void shouldFailToSetNullAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class, () -> scanner.setAlertThreshold(alertThreshold));
    }

    @Test
    void shouldSetValidAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = AlertThreshold.HIGH;
        // When
        scanner.setAlertThreshold(alertThreshold);
        // Then
        assertThat(scanner.getAlertThreshold(), is(equalTo(alertThreshold)));
    }

    @Test
    void shouldFailToSetNullDefaultAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> scanner.setDefaultAlertThreshold(alertThreshold));
    }

    @Test
    void shouldFailToSetDefaultToDefaultAlertThreshold() {
        // Given
        AlertThreshold alertThreshold = AlertThreshold.DEFAULT;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> scanner.setDefaultAlertThreshold(alertThreshold));
    }

    @Test
    void shouldSetValidDefaultAlertThreshold() {
        // Given
        scanner.setAlertThreshold(AlertThreshold.DEFAULT);
        AlertThreshold alertThreshold = AlertThreshold.HIGH;
        // When
        scanner.setDefaultAlertThreshold(alertThreshold);
        // Then
        assertThat(scanner.getAlertThreshold(), is(equalTo(alertThreshold)));
    }

    @Test
    void shouldHaveNoConfigurationByDefault() {
        assertThat(scanner.getConfig(), is(nullValue()));
    }

    @Test
    void shouldFailToSetNullConfiguration() {
        // Given
        Configuration configuration = null;
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> scanner.setConfig(configuration));
    }

    @Test
    void shouldNotChangeEnabledStateOrAlertThresholdIfConfigurationSetIsEmpty() {
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
    void
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
    void shouldEnableByDefaultIfNotSpecifiedInConfigurationSet() {
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
    void shouldDisableIfSpecifiedInConfigurationSet() {
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
    void shouldUseDefaultAlertThresholdIfNotSpecifiedInConfigurationSet() {
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
    void shouldUseAlertThresholdSpecifiedInConfigurationSet() {
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
    void shouldUseClassnameToReadConfigurationSet() {
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
    void shouldReadConfigurationSetEvenIfThereAreMultipleUnrelatedEntries() {
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
    void shouldFailToSaveByDefault() {
        // Given scanner
        // When / Then
        assertThrows(IllegalStateException.class, () -> scanner.save());
    }

    @Test
    void shouldNotPersistEnabledStateOnSaveIfEnabled() {
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
    void shouldPersistEnabledStateOnSaveIfDisabled() {
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
    void shouldNotPersistAlertThresholdOnSaveIfDefaultValue() {
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
    void shouldPersistAlertThresholdOnSaveIfNotDefaultValue() {
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
    void shouldRemoveExistingConfigDataOnSaveIfDefaultValues() {
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
    void shouldRemoveClassnameConfigEntryOnSave() {
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
    void shouldPersistConfigsOnSaveEvenIfThereAreMultipleUnrelatedEntries() {
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
    void
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

    @Test
    void shouldCallTaskHelperWithTagAdded() {
        // Given
        String tag = "tag";
        PassiveScanTaskHelper taskHelper = mock(PassiveScanTaskHelper.class);
        TestPluginPassiveScanner scanner = new TestPluginPassiveScanner();
        HttpMessage msg = mock(HttpMessage.class);
        HistoryReference href = mock(HistoryReference.class);
        when(msg.getHistoryRef()).thenReturn(href);
        PassiveScanData passiveScanData = mock(PassiveScanData.class);
        when(passiveScanData.getMessage()).thenReturn(msg);
        scanner.setHelper(passiveScanData);
        scanner.setTaskHelper(taskHelper);
        // When
        scanner.addHistoryTag(tag);
        // Then
        verify(taskHelper).addHistoryTag(href, tag);
    }

    private static class TestPluginPassiveScanner extends PluginPassiveScanner {

        private static final int PLUGIN_ID = -1;

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
