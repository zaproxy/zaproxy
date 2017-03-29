package org.zaproxy.zap.extension.pscan;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import net.htmlparser.jericho.Source;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link PluginPassiveScanner}.
 */
public class PluginPassiveScannerUnitTest {

	private PluginPassiveScanner scanner;

	@Before
	public void setUp() throws Exception {
		scanner = new TestPluginPassiveScanner();
	}

	@Test
	public void shouldHaveUndefinedPluginIdByDefault() {
		assertThat(scanner.getPluginId(), is(equalTo(-1)));
	}

	@Test
	public void shouldHaveUnkownStatusByDefault() {
		assertThat(scanner.getStatus(), is(equalTo(AddOn.Status.unknown)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSetNullStatus() {
		// Given
		AddOn.Status status = null;
		// When
		scanner.setStatus(status);
		// Then = IllegalArgumentException.
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
	public void shouldHaveMediumAsDefaultLevel() {
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.MEDIUM)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSetNullLevel() {
		// Given
		AlertThreshold level = null;
		// When
		scanner.setLevel(level);
		// Then = IllegalArgumentException.
	}

	@Test
	public void shouldSetValidLevel() {
		// Given
		AlertThreshold level = AlertThreshold.HIGH;
		// When
		scanner.setLevel(level);
		// Then
		assertThat(scanner.getLevel(), is(equalTo(level)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSetNullDefaultLevel() {
		// Given
		AlertThreshold level = null;
		// When
		scanner.setDefaultLevel(level);
		// Then = IllegalArgumentException.
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSetDefaultToDefaultLevel() {
		// Given
		AlertThreshold level = AlertThreshold.DEFAULT;
		// When
		scanner.setDefaultLevel(level);
		// Then = IllegalArgumentException.
	}

	@Test
	public void shouldSetValidDefaultLevel() {
		// Given
		scanner.setLevel(AlertThreshold.DEFAULT);
		AlertThreshold level = AlertThreshold.HIGH;
		// When
		scanner.setDefaultLevel(level);
		// Then
		assertThat(scanner.getLevel(), is(equalTo(level)));
	}

	@Test
	public void shouldHaveNoConfigurationByDefault() {
		assertThat(scanner.getConfig(), is(nullValue()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailToSetNullConfiguration() {
		// Given
		Configuration configuration = null;
		// When
		scanner.setConfig(configuration);
		// Then = IllegalArgumentException.
	}

	@Test
	public void shouldNotChangeEnabledStateOrLevelIfConfigurationSetIsEmpty() {
		Configuration configuration = createEmptyConfiguration();
		// given
		scanner.setEnabled(false);
		scanner.setLevel(AlertThreshold.HIGH);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(false));
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.HIGH)));
	}

	@Test
	public void shouldNotChangeEnabledStateOrLevelIfNoApplicableDataIsPresentInConfigurationSet() {
		Configuration configuration = createConfiguration(Integer.MIN_VALUE, Boolean.TRUE, AlertThreshold.LOW);
		// given
		scanner.setEnabled(false);
		scanner.setLevel(AlertThreshold.HIGH);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(false));
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.HIGH)));
	}

	@Test
	public void shouldEnableByDefaultIfNotSpecifiedInConfigurationSet() {
		// given
		Configuration configuration = createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, null, null);
		scanner.setEnabled(false);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(true));
	}

	@Test
	public void shouldDisableIfSpecifiedInConfigurationSet() {
		// given
		Configuration configuration = createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, Boolean.FALSE, null);
		scanner.setEnabled(true);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(false));
	}

	@Test
	public void shouldUseDefaultLevelIfNotSpecifiedInConfigurationSet() {
		// given
		Configuration configuration = createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, Boolean.TRUE, null);
		scanner.setDefaultLevel(AlertThreshold.HIGH);
		scanner.setLevel(AlertThreshold.LOW);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.HIGH)));
	}

	@Test
	public void shouldUseLevelSpecifiedInConfigurationSet() {
		// given
		Configuration configuration = createConfiguration(TestPluginPassiveScanner.PLUGIN_ID, null, AlertThreshold.HIGH);
		scanner.setLevel(AlertThreshold.LOW);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.HIGH)));
	}

	@Test
	public void shouldUseClassnameToReadConfigurationSet() {
		// given
		Configuration configuration = createConfiguration(
				TestPluginPassiveScanner.class.getCanonicalName(),
				Boolean.FALSE,
				AlertThreshold.HIGH);
		scanner.setEnabled(true);
		scanner.setLevel(AlertThreshold.LOW);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(false));
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.HIGH)));
	}

	@Test
	public void shouldReadConfigurationSetEvenIfThereAreMultipleUnrelatedEntries() {
		// given
		Configuration configuration = createEmptyConfiguration();
		addConfiguration(configuration, 0, 10, null, null);
		addConfiguration(configuration, 1, "TestClassName", Boolean.TRUE, AlertThreshold.HIGH);
		addConfiguration(configuration, 2, TestPluginPassiveScanner.PLUGIN_ID, Boolean.FALSE, AlertThreshold.MEDIUM);
		addConfiguration(configuration, 3, 1011, null, AlertThreshold.LOW);
		addConfiguration(configuration, 4, "OtherTestClassName", Boolean.FALSE, AlertThreshold.OFF);
		scanner.setEnabled(true);
		scanner.setLevel(AlertThreshold.LOW);
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(false));
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.MEDIUM)));
	}

	@Test(expected = IllegalStateException.class)
	public void shouldFailToSaveByDefault() {
		// Given / When
		scanner.save();
		// Then = IllegalStateException.
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
		assertThat(configuration.getInt("pscans.pscanner(0).id"), is(equalTo(TestPluginPassiveScanner.PLUGIN_ID)));
	}

	@Test
	public void shouldNotPersistLevelOnSaveIfDefaultValue() {
		// Given
		Configuration configuration = createEmptyConfiguration();
		scanner.setConfig(configuration);
		scanner.setLevel(AlertThreshold.MEDIUM);
		scanner.setEnabled(true);
		// When
		scanner.save();
		// Then
		assertThat(configuration.containsKey("pscans.pscanner(0).level"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(false));
	}

	@Test
	public void shouldPersistLevelOnSaveIfNotDefaultValue() {
		// Given
		Configuration configuration = createEmptyConfiguration();
		scanner.setConfig(configuration);
		scanner.setLevel(AlertThreshold.HIGH);
		scanner.setEnabled(true);
		// When
		scanner.save();
		// Then
		assertThat(configuration.containsKey("pscans.pscanner(0).level"), is(true));
		assertThat(configuration.getString("pscans.pscanner(0).level"), is(equalTo(AlertThreshold.HIGH.name())));
		assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(true));
		assertThat(configuration.getInt("pscans.pscanner(0).id"), is(equalTo(TestPluginPassiveScanner.PLUGIN_ID)));
	}

	@Test
	public void shouldRemoveExistingConfigDataOnSaveIfDefaultValues() {
		// Given
		Configuration configuration = createConfiguration(
				TestPluginPassiveScanner.PLUGIN_ID,
				Boolean.FALSE,
				AlertThreshold.HIGH);
		scanner.setConfig(configuration);
		scanner.setEnabled(true);
		scanner.setLevel(AlertThreshold.MEDIUM);
		// When
		scanner.save();
		// Then
		assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).level"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(false));
	}

	@Test
	public void shouldRemoveClassnameConfigEntryOnSave() {
		// Given
		Configuration configuration = createConfiguration(
				TestPluginPassiveScanner.class.getCanonicalName(),
				Boolean.FALSE,
				AlertThreshold.HIGH);
		scanner.setConfig(configuration);
		scanner.setEnabled(true);
		scanner.setLevel(AlertThreshold.MEDIUM);
		// When
		scanner.save();
		// Then
		assertThat(configuration.containsKey("pscans.pscanner(0).classname"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).level"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(false));
	}

	@Test
	public void shouldPersistConfigsOnSaveEvenIfThereAreMultipleUnrelatedEntries() {
		// Given
		Configuration configuration = createEmptyConfiguration();
		addConfiguration(configuration, 0, 10, null, null);
		addConfiguration(configuration, 1, "TestClassName", Boolean.TRUE, AlertThreshold.HIGH);
		addConfiguration(configuration, 2, TestPluginPassiveScanner.PLUGIN_ID, Boolean.FALSE, AlertThreshold.MEDIUM);
		addConfiguration(configuration, 3, 1011, null, AlertThreshold.LOW);
		scanner.setConfig(configuration);
		scanner.setEnabled(false);
		scanner.setLevel(AlertThreshold.LOW);
		// When
		scanner.save();
		// Then
		assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(true));
		assertThat(configuration.getInt("pscans.pscanner(0).id"), is(equalTo(10)));
		assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).level"), is(false));

		assertThat(configuration.containsKey("pscans.pscanner(1).id"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(1).classname"), is(true));
		assertThat(configuration.getString("pscans.pscanner(1).classname"), is(equalTo("TestClassName")));
		assertThat(configuration.containsKey("pscans.pscanner(1).enabled"), is(true));
		assertThat(configuration.getBoolean("pscans.pscanner(1).enabled"), is(true));
		assertThat(configuration.containsKey("pscans.pscanner(1).level"), is(true));
		assertThat(configuration.getString("pscans.pscanner(1).level"), is(equalTo(AlertThreshold.HIGH.name())));

		assertThat(configuration.containsKey("pscans.pscanner(2).id"), is(true));
		assertThat(configuration.getInt("pscans.pscanner(2).id"), is(equalTo(1011)));
		assertThat(configuration.containsKey("pscans.pscanner(2).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(2).level"), is(true));
		assertThat(configuration.getString("pscans.pscanner(2).level"), is(equalTo(AlertThreshold.LOW.name())));

		assertThat(configuration.containsKey("pscans.pscanner(3).id"), is(true));
		assertThat(configuration.getInt("pscans.pscanner(3).id"), is(equalTo(TestPluginPassiveScanner.PLUGIN_ID)));
		assertThat(configuration.containsKey("pscans.pscanner(3).enabled"), is(true));
		assertThat(configuration.getBoolean("pscans.pscanner(3).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(3).level"), is(true));
		assertThat(configuration.getString("pscans.pscanner(3).level"), is(equalTo(AlertThreshold.LOW.name())));

		assertThat(configuration.containsKey("pscans.pscanner(4).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(4).level"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(5).id"), is(false));
	}

	@Test
	public void shouldRemoveExistingConfigDataOnSaveIfDefaultValuesEvenIfThereAreMultipleUnrelatedEntries() {
		// Given
		Configuration configuration = createEmptyConfiguration();
		addConfiguration(configuration, 0, 10, null, null);
		addConfiguration(configuration, 1, "TestClassName", Boolean.TRUE, AlertThreshold.HIGH);
		addConfiguration(configuration, 2, TestPluginPassiveScanner.PLUGIN_ID, Boolean.FALSE, AlertThreshold.MEDIUM);
		addConfiguration(configuration, 3, 1011, null, AlertThreshold.LOW);
		scanner.setConfig(configuration);
		scanner.setEnabled(true);
		scanner.setLevel(AlertThreshold.MEDIUM);
		// When
		scanner.save();
		// Then
		assertThat(configuration.containsKey("pscans.pscanner(0).id"), is(true));
		assertThat(configuration.getInt("pscans.pscanner(0).id"), is(equalTo(10)));
		assertThat(configuration.containsKey("pscans.pscanner(0).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(0).level"), is(false));

		assertThat(configuration.containsKey("pscans.pscanner(1).id"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(1).classname"), is(true));
		assertThat(configuration.getString("pscans.pscanner(1).classname"), is(equalTo("TestClassName")));
		assertThat(configuration.containsKey("pscans.pscanner(1).enabled"), is(true));
		assertThat(configuration.getBoolean("pscans.pscanner(1).enabled"), is(true));
		assertThat(configuration.containsKey("pscans.pscanner(1).level"), is(true));
		assertThat(configuration.getString("pscans.pscanner(1).level"), is(equalTo(AlertThreshold.HIGH.name())));

		assertThat(configuration.containsKey("pscans.pscanner(2).id"), is(true));
		assertThat(configuration.getInt("pscans.pscanner(2).id"), is(equalTo(1011)));
		assertThat(configuration.containsKey("pscans.pscanner(2).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(2).level"), is(true));
		assertThat(configuration.getString("pscans.pscanner(2).level"), is(equalTo(AlertThreshold.LOW.name())));

		assertThat(configuration.containsKey("pscans.pscanner(3).enabled"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(3).level"), is(false));
		assertThat(configuration.containsKey("pscans.pscanner(3).id"), is(false));
	}

	private static class TestPluginPassiveScanner extends PluginPassiveScanner {

		private static final int PLUGIN_ID = -1;

		@Override
		public void setParent(PassiveScanThread parent) {
		}

		@Override
		public void scanHttpResponseReceive(HttpMessage msg, int id,
				Source source) {
		}

		@Override
		public void scanHttpRequestSend(HttpMessage msg, int id) {
		}

		@Override
		public String getName() {
			return null;
		}
	}

	private static Configuration createEmptyConfiguration() {
		return new ZapXmlConfiguration();
	}

	private static Configuration createConfiguration(String classname, Boolean enabled, AlertThreshold alertThreshold) {
		ZapXmlConfiguration configuration = new ZapXmlConfiguration();
		setClassname(configuration, 0, classname);
		setProperties(configuration, 0, enabled, alertThreshold);
		return configuration;
	}

	private static void setClassname(Configuration configuration, int index, String classname) {
		configuration.setProperty("pscans.pscanner(" + index + ").classname", classname);
	}

	private static Configuration createConfiguration(int pluginId, Boolean enabled, AlertThreshold alertThreshold) {
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

	private static void setProperties(Configuration configuration, int index, Boolean enabled, AlertThreshold alertThreshold) {
		if (enabled != null) {
			configuration.setProperty("pscans.pscanner(" + index + ").enabled", enabled);
		}
		if (alertThreshold != null) {
			configuration.setProperty("pscans.pscanner(" + index + ").level", alertThreshold.name());
		}
	}
}
