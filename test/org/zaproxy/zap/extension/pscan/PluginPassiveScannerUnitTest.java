package org.zaproxy.zap.extension.pscan;

import net.htmlparser.jericho.Source;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpMessage;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PluginPassiveScannerUnitTest {

	@Mock
	Configuration configuration;

	PluginPassiveScanner scanner;

	@Before
	public void setUp() throws Exception {
		scanner = new TestPluginPassiveScanner();
	}

	@Test
	public void shouldHaveMediumAsDefaultLevel() {
		assertThat(scanner.getLevel(), is(equalTo(AlertThreshold.MEDIUM)));
	}

	@Test
	public void shouldHaveNoConfigurationByDefault() {
		assertThat(scanner.getConfig(), is(nullValue()));
	}

	@Test
	public void shouldEnableByDefaultIsNotSpecifiedInConfiguration() {
		// given
		given(configuration.getBoolean(anyString(), eq(true))).willReturn(true);
		given(configuration.getString(anyString(), anyString())).willReturn(AlertThreshold.DEFAULT.name());
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(true));
	}

	@Test
	public void shouldDisableIfSpecifiedInConfiguration() {
		// given
		given(configuration.getBoolean(anyString(), anyBoolean())).willReturn(false);
		given(configuration.getString(anyString(), anyString())).willReturn(AlertThreshold.DEFAULT.name());
		// when
		scanner.setConfig(configuration);
		// then
		assertThat(scanner.isEnabled(), is(false));
	}

	@Test
	public void shouldPersistEnabledStatusInConfigurationOnSave() {
		// given
		given(configuration.getBoolean(anyString(), eq(true))).willReturn(true);
		given(configuration.getString(anyString(), anyString())).willReturn(AlertThreshold.DEFAULT.name());
		// when
		scanner.setConfig(configuration);
		scanner.save();
		// then
		verify(configuration).setProperty(
				"pscans." + scanner.getClass().getCanonicalName() + ".enabled",
				true);
	}

	private final class TestPluginPassiveScanner extends PluginPassiveScanner {
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

		@Override
		public boolean appliesToHistoryType(int historyType) {
			return true;
		}
	}

}
