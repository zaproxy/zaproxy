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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.Assert.assertThat;

import org.apache.commons.configuration.Configuration;
import org.junit.Test;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Unit test for {@link AbstractPlugin}.
 */
public class AbstractPluginUnitTest extends PluginTestUtils {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToSetNullTechSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        plugin.setTechSet(null);
        // Then = IllegalArgumentException.class
    }

    @Test
    public void shouldHaveAllTechSetByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getTechSet(), is(equalTo(TechSet.AllTech)));
    }

    @Test
    public void shouldNotHaveConfigByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getConfig(), is(equalTo(null)));
    }

    @Test
    public void shouldRetrieveConfigSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // WHen
        plugin.setConfig(config);
        // Then
        assertThat(plugin.getConfig(), is(equalTo(config)));
    }

    @Test
    public void shouldRetrieveClassNameForCodeName() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getCodeName(), is(equalTo("PluginTestUtils$TestPlugin")));
    }

    @Test
    public void shouldNotHaveDependenciesByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getDependency(), is(emptyArray()));
    }

    @Test
    public void shouldRetrieveUndefinedWascId() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getWascId(), is(equalTo(0)));
    }

    @Test
    public void shouldRetrieveUndefinedCweId() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getCweId(), is(equalTo(0)));
    }

    @Test
    public void shouldRetrieveMediumRiskByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getRisk(), is(equalTo(Alert.RISK_MEDIUM)));
    }

    @Test
    public void shouldRetrieveZeroDelayInMsByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getDelayInMs(), is(equalTo(0)));
    }

    @Test
    public void shouldRetrieveDelaySet() {
        // Given
        int aDelayInMs = 1234;
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        plugin.setDelayInMs(aDelayInMs);
        // Then
        assertThat(plugin.getDelayInMs(), is(equalTo(aDelayInMs)));
    }

    @Test
    public void shouldRetrieveUnkownStatusByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getStatus(), is(equalTo(AddOn.Status.unknown)));
    }

    @Test
    public void shouldRetrieveStatusSet() {
        // Given
        AddOn.Status aStatus = AddOn.Status.example;
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        plugin.setStatus(aStatus);
        // Then
        assertThat(plugin.getStatus(), is(equalTo(aStatus)));
    }

    @Test
    public void shouldBeEnabledByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.TRUE)));
    }

    @Test(expected = Exception.class)
    public void shouldFailWhenSettingEnabledStateWithoutConfig() {
        // Given / When
        createAbstractPlugin().setEnabled(false);
        // Then = Exception.class
    }

    @Test
    public void shouldRetrieveEnabledStateSet() {
        // Given
        boolean aState = false;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setEnabled(aState);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(aState)));
    }

    @Test
    public void shouldRetrieveMediumAttackStrengthByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAttackStrength(), is(equalTo(Plugin.AttackStrength.MEDIUM)));
    }

    @Test(expected = Exception.class)
    public void shouldFailWhenSettingAttackStrengthWithoutConfig() {
        // Given / When
        createAbstractPlugin().setAttackStrength(Plugin.AttackStrength.INSANE);
        // Then = Exception.class
    }

    @Test
    public void shouldRetrieveAttackStrengthSet() {
        // Given
        Plugin.AttackStrength anAttackStrength = Plugin.AttackStrength.INSANE;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setAttackStrength(anAttackStrength);
        // Then
        assertThat(plugin.getAttackStrength(), is(equalTo(anAttackStrength)));
    }

    @Test
    public void shouldRetrieveDefaultAttackStrengthByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAttackStrength(true), is(equalTo(Plugin.AttackStrength.DEFAULT)));
    }

    @Test
    public void shouldRetrieveDefaultAttackStrengthSet() {
        // Given
        Plugin.AttackStrength anAttackStrength = Plugin.AttackStrength.LOW;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAttackStrength(Plugin.AttackStrength.DEFAULT);
        // When
        plugin.setDefaultAttackStrength(anAttackStrength);
        // Then
        assertThat(plugin.getAttackStrength(false), is(equalTo(anAttackStrength)));
    }

    @Test
    public void shouldRetrieveDefaultAttackStrengthSetAsDefault() {
        // Given
        Plugin.AttackStrength anAttackStrength = Plugin.AttackStrength.DEFAULT;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAttackStrength(Plugin.AttackStrength.LOW);
        // When
        plugin.setAttackStrength(anAttackStrength);
        // Then
        assertThat(plugin.getAttackStrength(true), is(equalTo(anAttackStrength)));
    }

    @Test
    public void shouldRetrieveMediumAlertThresholdByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAlertThreshold(), is(equalTo(Plugin.AlertThreshold.MEDIUM)));
    }

    @Test(expected = Exception.class)
    public void shouldFailWhenSettingAlertThresholdWithoutConfig() {
        // Given / When
        createAbstractPlugin().setAlertThreshold(Plugin.AlertThreshold.DEFAULT);
        // Then = Exception.class
    }

    @Test
    public void shouldRetrieveAlertThresholdSet() {
        // Given
        Plugin.AlertThreshold anAlertThreshold = Plugin.AlertThreshold.HIGH;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setAlertThreshold(anAlertThreshold);
        // Then
        assertThat(plugin.getAlertThreshold(), is(equalTo(anAlertThreshold)));
    }

    @Test
    public void shouldDisablePluginWhenAlertThresholdSetToOff() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        plugin.setEnabled(true);
        // When
        plugin.setAlertThreshold(Plugin.AlertThreshold.OFF);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    public void shouldEnablePluginWhenAlertThresholdSetToNonOff() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(Plugin.AlertThreshold.OFF);
        plugin.setEnabled(false);
        // When
        plugin.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    public void shouldSetAlertThresholdToDefaultWhenEnablingPluginWithOffAlertThreshold() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(Plugin.AlertThreshold.OFF);
        // When
        plugin.setEnabled(true);
        // Then
        assertThat(plugin.getAlertThreshold(true), is(equalTo(Plugin.AlertThreshold.DEFAULT)));
    }

    @Test
    public void shouldRetrieveDefaultAlertThresholdByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAlertThreshold(true), is(equalTo(Plugin.AlertThreshold.DEFAULT)));
    }

    @Test
    public void shouldRetrieveOffAlertThresholdByDefaultIfDisabled() {
        // Given / When
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setEnabled(false);
        // Then
        assertThat(plugin.getAlertThreshold(true), is(equalTo(Plugin.AlertThreshold.OFF)));
    }

    @Test
    public void shouldRetrieveDefaultAlertThresholdSet() {
        // Given
        Plugin.AlertThreshold anAlertThreshold = Plugin.AlertThreshold.LOW;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(AlertThreshold.DEFAULT);
        // When
        plugin.setDefaultAlertThreshold(anAlertThreshold);
        // Then
        assertThat(plugin.getAlertThreshold(false), is(equalTo(anAlertThreshold)));
    }

    @Test
    public void shouldRetrieveDefaultAlertThresholdSetAsDefault() {
        // Given
        Plugin.AlertThreshold anAlertThreshold = Plugin.AlertThreshold.DEFAULT;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(Plugin.AlertThreshold.LOW);
        // When
        plugin.setAlertThreshold(anAlertThreshold);
        // Then
        assertThat(plugin.getAlertThreshold(true), is(equalTo(anAlertThreshold)));
    }

    @Test(expected = Exception.class)
    public void shouldFailToCloneIntoNonAbstractPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        Plugin pluginB = createNonAbstractPlugin();
        // When
        pluginA.cloneInto(pluginB);
        // Then = Exception.class
    }

    @Test(expected = Exception.class)
    public void shouldFailToCloneIntoPluginWithoutConfig() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        AbstractPlugin pluginB = createAbstractPlugin();
        // When
        pluginA.cloneInto(pluginB);
        // Then = Exception.class
    }

    @Test
    public void shouldCloneIntoPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        pluginA.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        pluginA.setDefaultAlertThreshold(Plugin.AlertThreshold.MEDIUM);
        pluginA.setAttackStrength(Plugin.AttackStrength.INSANE);
        pluginA.setDefaultAttackStrength(Plugin.AttackStrength.LOW);
        pluginA.setTechSet(TechSet.AllTech);
        pluginA.setStatus(AddOn.Status.beta);
        pluginA.setEnabled(false);
        AbstractPlugin pluginB = createAbstractPluginWithConfig();
        // When
        pluginA.cloneInto(pluginB);
        // Then
        assertThat(pluginA.isEnabled(), is(equalTo(pluginB.isEnabled())));
        assertThat(pluginA.getAlertThreshold(), is(equalTo(pluginB.getAlertThreshold())));
        assertThat(pluginA.getAlertThreshold(true), is(equalTo(pluginB.getAlertThreshold(true))));
        assertThat(pluginA.getAttackStrength(), is(equalTo(pluginB.getAttackStrength())));
        assertThat(pluginA.getAttackStrength(true), is(equalTo(pluginB.getAttackStrength(true))));
        assertThat(pluginA.getTechSet(), is(equalTo(pluginB.getTechSet())));
        assertThat(pluginA.getStatus(), is(equalTo(pluginB.getStatus())));
    }

    @Test
    public void shouldNotBeEqualToNonAbstractPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin();
        Plugin pluginB = createNonAbstractPlugin();
        // When
        boolean equals = pluginA.equals(pluginB);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    public void shouldNotBeEqualToAbstractPluginWithDifferentId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(1);
        AbstractPlugin pluginB = createAbstractPlugin(5);
        // When
        boolean equals = pluginA.equals(pluginB);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    public void shouldBeEqualToAbstractPluginWithSameId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(1);
        AbstractPlugin pluginB = createAbstractPlugin(1);
        // When
        boolean equals = pluginA.equals(pluginB);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    public void shouldBeLesserThanAbstractPluginWithGreaterId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(1);
        AbstractPlugin pluginB = createAbstractPlugin(10);
        // When
        int comparisonResult = pluginA.compareTo(pluginB);
        // Then
        assertThat(comparisonResult, is(equalTo(-1)));
    }

    @Test
    public void shouldBeSameAsAbstractPluginWithSameId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(10);
        AbstractPlugin pluginB = createAbstractPlugin(10);
        // When
        int comparisonResult = pluginA.compareTo(pluginB);
        // Then
        assertThat(comparisonResult, is(equalTo(0)));
    }

    @Test
    public void shouldBeGreaterThanAbstractPluginWithLesserId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(10);
        AbstractPlugin pluginB = createAbstractPlugin(1);
        // When
        int comparisonResult = pluginA.compareTo(pluginB);
        // Then
        assertThat(comparisonResult, is(equalTo(1)));
    }

    @Test(expected = Exception.class)
    public void shouldFailToLoadFromConfigIfConfigNotSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // When
        plugin.loadFrom(config);
        // Then = Exception.class
    }

    @Test
    public void shouldNotLoadFromConfigIfPluginHasDifferentId() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig(10);
        pluginA.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        pluginA.setAttackStrength(Plugin.AttackStrength.INSANE);
        pluginA.setEnabled(false);
        AbstractPlugin pluginB = createAbstractPluginWithConfig(15);
        // When
        pluginB.loadFrom(pluginA.getConfig());
        // Then
        assertThat(pluginB.isEnabled(), is(equalTo(Boolean.TRUE)));
        assertThat(pluginB.getAlertThreshold(), is(equalTo(Plugin.AlertThreshold.MEDIUM)));
        assertThat(pluginB.getAttackStrength(), is(equalTo(Plugin.AttackStrength.MEDIUM)));
    }

    @Test
    public void shouldLoadFromConfigIfPluginsHaveSameId() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig(10);
        pluginA.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        pluginA.setAttackStrength(Plugin.AttackStrength.INSANE);
        pluginA.setEnabled(false);
        AbstractPlugin pluginB = createAbstractPluginWithConfig(10);
        // When
        pluginB.loadFrom(pluginA.getConfig());
        // Then
        assertThat(pluginB.isEnabled(), is(equalTo(Boolean.FALSE)));
        assertThat(pluginB.getAlertThreshold(), is(equalTo(Plugin.AlertThreshold.HIGH)));
        assertThat(pluginB.getAttackStrength(), is(equalTo(Plugin.AttackStrength.INSANE)));
    }

    @Test
    public void shouldJustSaveEnabledStateToOwnConfigWhenLoadFromEmptyConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig(15);
        Configuration emptyConfig = new ZapXmlConfiguration();
        Configuration config = plugin.getConfig();
        String basePropertyKey = "plugins.p" + plugin.getId() + ".";
        // When
        plugin.loadFrom(emptyConfig);
        // Then
        assertThat(config.getString(basePropertyKey + "enabled"), is(equalTo("true")));
        assertThat(config.getString(basePropertyKey + "level"), is(equalTo(null)));
        assertThat(config.getString(basePropertyKey + "strength"), is(equalTo(null)));
    }

    @Test
    public void shouldSaveToOwnConfigWhenLoadFromOtherConfig() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig(10);
        pluginA.setAlertThreshold(Plugin.AlertThreshold.LOW);
        pluginA.setAttackStrength(Plugin.AttackStrength.HIGH);
        pluginA.setEnabled(false);
        AbstractPlugin pluginB = createAbstractPluginWithConfig(10);
        Configuration config = pluginB.getConfig();
        String basePropertyKey = "plugins.p" + pluginB.getId() + ".";
        // When
        pluginB.loadFrom(pluginA.getConfig());
        // Then
        assertThat(config.getString(basePropertyKey + "enabled"), is(equalTo("false")));
        assertThat(config.getString(basePropertyKey + "level"), is(equalTo(Plugin.AlertThreshold.LOW.name())));
        assertThat(config.getString(basePropertyKey + "strength"), is(equalTo(Plugin.AttackStrength.HIGH.name())));
    }

    @Test(expected = Exception.class)
    public void shouldFailToSaveToConfigIfConfigNotSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // When
        plugin.saveTo(config);
        // Then = Exception.class
    }

    @Test
    public void shouldSaveToConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig(10);
        plugin.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        plugin.setAttackStrength(Plugin.AttackStrength.INSANE);
        Configuration config = new ZapXmlConfiguration();
        String basePropertyKey = "plugins.p" + plugin.getId() + ".";
        // When
        plugin.saveTo(config);
        // Then
        assertThat(config.getString(basePropertyKey + "enabled"), is(equalTo("true")));
        assertThat(config.getString(basePropertyKey + "level"), is(equalTo(Plugin.AlertThreshold.HIGH.name())));
        assertThat(config.getString(basePropertyKey + "strength"), is(equalTo(Plugin.AttackStrength.INSANE.name())));
    }

}
