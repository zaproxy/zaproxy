/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link PluginFactory}. */
@ExtendWith(MockitoExtension.class)
class PluginFactoryUnitTest extends PluginTestUtils {

    @BeforeEach
    void setUp() throws Exception {
        I18N i18n = mock(I18N.class, withSettings().strictness(Strictness.LENIENT));
        given(i18n.getString(anyString())).willReturn("");
        given(i18n.getString(anyString(), any())).willReturn("");
        Constant.messages = i18n;

        PluginFactory.init(false);
    }

    @Test
    void shouldNotHaveUndefinedPluginLoaded() {
        // Given
        AbstractPlugin undefinedPlugin = null;
        // When
        boolean loaded = PluginFactory.isPluginLoaded(undefinedPlugin);
        // Then
        assertThat(loaded, is(equalTo(false)));
    }

    @Test
    void shouldAddLoadedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        PluginFactory.loadedPlugin(plugin);
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
    }

    @Test
    void shouldRemoveLoadedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        // When
        PluginFactory.unloadedPlugin(plugin);
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(false)));
    }

    @Test
    void shouldHaveNoEffectRemovingANotYetLoadedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        PluginFactory.unloadedPlugin(plugin);
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(false)));
    }

    @Test
    void shouldHaveOnePluginByDefault() {
        // Given
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(1)));
    }

    @Test
    void shouldNotLoadNonVisiblePlugins() {
        // Given
        AbstractPlugin plugin = createNonVisibleAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(1)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
    }

    @Test
    void shouldNotLoadDeprecatedPlugins() {
        // Given
        AbstractPlugin plugin = createDeprecatedAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(1)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
    }

    @Test
    void shouldLoadVisibleNonDeprecatedPlugin() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(pluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
    }

    @Test
    void shouldHaveNoEffectLoadingAPluginMoreThanOnce() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory pluginFactory = new PluginFactory();
        // When
        PluginFactory.loadedPlugin(plugin);
        PluginFactory.loadedPlugin(plugin);
        pluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(pluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
    }

    @Test
    void shouldHaveDifferentPluginInstancesPerPluginFactory() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        PluginFactory.loadedPlugin(plugin);
        PluginFactory pluginFactory = new PluginFactory();
        PluginFactory otherPluginFactory = new PluginFactory();
        // When
        pluginFactory.loadAllPlugin(emptyConfig());
        otherPluginFactory.loadAllPlugin(emptyConfig());
        // Then
        assertThat(PluginFactory.isPluginLoaded(plugin), is(equalTo(true)));
        assertThat(pluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(otherPluginFactory.getAllPlugin(), hasSize(equalTo(2)));
        assertThat(pluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(otherPluginFactory.getAllPlugin().get(0), is(not(equalTo((Plugin) plugin))));
        assertThat(pluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
        assertThat(otherPluginFactory.getAllPlugin().get(1), is(equalTo((Plugin) plugin)));
        assertThat(
                pluginFactory.getAllPlugin().get(1),
                is(not(sameInstance(otherPluginFactory.getAllPlugin().get(1)))));
    }

    @Test
    void shouldOrderHighRiskAlertPluginsBeforeMedium() {
        // Given
        AbstractPlugin pluginHighAlert = createAbstractPlugin(1, Alert.RISK_HIGH);
        AbstractPlugin pluginMediumAlert = createAbstractPlugin(2, Alert.RISK_MEDIUM);
        PluginFactory.loadedPlugin(pluginHighAlert);
        PluginFactory.loadedPlugin(pluginMediumAlert);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(pluginHighAlert, pluginFactory),
                is(lessThan(indexOf(pluginMediumAlert, pluginFactory))));
    }

    @Test
    void shouldOrderMediumRiskAlertPluginsBeforeLow() {
        // Given
        AbstractPlugin pluginMediumAlert = createAbstractPlugin(1, Alert.RISK_MEDIUM);
        AbstractPlugin pluginLowAlert = createAbstractPlugin(2, Alert.RISK_LOW);
        PluginFactory.loadedPlugin(pluginMediumAlert);
        PluginFactory.loadedPlugin(pluginLowAlert);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(pluginMediumAlert, pluginFactory),
                is(lessThan(indexOf(pluginLowAlert, pluginFactory))));
    }

    @Test
    void shouldOrderLowRiskAlertPluginsBeforeInfo() {
        // Given
        AbstractPlugin pluginLowAlert = createAbstractPlugin(1, Alert.RISK_LOW);
        AbstractPlugin pluginInfoAlert = createAbstractPlugin(2, Alert.RISK_INFO);
        PluginFactory.loadedPlugin(pluginLowAlert);
        PluginFactory.loadedPlugin(pluginInfoAlert);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(pluginLowAlert, pluginFactory),
                is(lessThan(indexOf(pluginInfoAlert, pluginFactory))));
    }

    @Test
    void shouldOrderReleaseStatusPluginsBeforeBeta() {
        // Given
        AbstractPlugin pluginReleaseStatus =
                createAbstractPlugin(1, Alert.RISK_LOW, AddOn.Status.release);
        AbstractPlugin pluginBetaStatus =
                createAbstractPlugin(2, Alert.RISK_HIGH, AddOn.Status.beta);
        PluginFactory.loadedPlugin(pluginReleaseStatus);
        PluginFactory.loadedPlugin(pluginBetaStatus);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(pluginReleaseStatus, pluginFactory),
                is(lessThan(indexOf(pluginBetaStatus, pluginFactory))));
    }

    @Test
    void shouldOrderBetaStatusPluginBeforeAlpha() {
        // Given
        AbstractPlugin pluginBetaStatus =
                createAbstractPlugin(1, Alert.RISK_LOW, AddOn.Status.beta);
        AbstractPlugin pluginAlphaStatus =
                createAbstractPlugin(2, Alert.RISK_HIGH, AddOn.Status.alpha);
        PluginFactory.loadedPlugin(pluginBetaStatus);
        PluginFactory.loadedPlugin(pluginAlphaStatus);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(pluginBetaStatus, pluginFactory),
                is(lessThan(indexOf(pluginAlphaStatus, pluginFactory))));
    }

    @Test
    void shouldOrderAlphaStatusPluginBeforeUnknown() {
        // Given
        AbstractPlugin pluginAlphaStatus =
                createAbstractPlugin(1, Alert.RISK_LOW, AddOn.Status.alpha);
        AbstractPlugin pluginUnknownStatus =
                createAbstractPlugin(2, Alert.RISK_HIGH, AddOn.Status.unknown);
        PluginFactory.loadedPlugin(pluginAlphaStatus);
        PluginFactory.loadedPlugin(pluginUnknownStatus);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(pluginAlphaStatus, pluginFactory),
                is(lessThan(indexOf(pluginUnknownStatus, pluginFactory))));
    }

    @Test
    void shouldOrderByLowerPluginIdIfSameAlertRiskAndStatus() {
        // Given
        AbstractPlugin pluginLowerId = createAbstractPlugin(52, Alert.RISK_LOW, AddOn.Status.beta);
        AbstractPlugin pluginHigherId =
                createAbstractPlugin(150, Alert.RISK_LOW, AddOn.Status.beta);
        PluginFactory.loadedPlugin(pluginLowerId);
        PluginFactory.loadedPlugin(pluginHigherId);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(pluginLowerId, pluginFactory),
                is(lessThan(indexOf(pluginHigherId, pluginFactory))));
    }

    @Test
    void shouldOrderDependenciesBeforeDependentPlugins() {
        // Given
        AbstractPlugin dependentPlugin =
                createAbstractPlugin(
                        1, "DependentPlugin", "DependencyPlugin1", "DependencyPlugin2");
        AbstractPlugin dependencyPlugin1 =
                createAbstractPlugin(2, "DependencyPlugin1", "DependencyPlugin2");
        AbstractPlugin dependencyPlugin2 = createAbstractPlugin(3, "DependencyPlugin2");
        PluginFactory.loadedPlugin(dependentPlugin);
        PluginFactory.loadedPlugin(dependencyPlugin1);
        PluginFactory.loadedPlugin(dependencyPlugin2);
        PluginFactory pluginFactory = new PluginFactory();
        pluginFactory.loadAllPlugin(emptyConfig());
        // When
        pluginFactory.reset();
        // Then
        assertThat(
                indexOf(dependencyPlugin1, pluginFactory),
                is(lessThan(indexOf(dependentPlugin, pluginFactory))));
        assertThat(
                indexOf(dependencyPlugin2, pluginFactory),
                is(lessThan(indexOf(dependentPlugin, pluginFactory))));
        assertThat(
                indexOf(dependencyPlugin2, pluginFactory),
                is(lessThan(indexOf(dependencyPlugin1, pluginFactory))));
    }

    private static int indexOf(AbstractPlugin plugin, PluginFactory pluginFactory) {
        List<Plugin> pending = pluginFactory.getPending();
        for (int i = 0; i < pending.size(); i++) {
            if (plugin.getId() == pending.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }
}
