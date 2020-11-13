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
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link AbstractPlugin}. */
public class AbstractPluginUnitTest extends PluginTestUtils {

    HostProcess parent;
    HttpMessage message;
    Analyser analyser;
    AbstractPlugin plugin;

    @BeforeEach
    public void setup() {
        Constant.messages = mock(I18N.class);
        parent = mock(HostProcess.class);
        message = mock(HttpMessage.class);
        analyser = mock(Analyser.class);
        plugin = spy(AbstractPlugin.class);
    }

    @Test
    public void shouldFailToSetNullTechSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> plugin.setTechSet(null));
    }

    @Test
    public void shouldHaveAllTechSetByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getTechSet(), is(equalTo(TechSet.getAllTech())));
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
    public void shouldRetrieveUnknownStatusByDefault() {
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

    @Test
    public void shouldFailWhenSettingEnabledStateWithoutConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(NullPointerException.class, () -> plugin.setEnabled(false));
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

    @Test
    public void shouldFailWhenSettingAttackStrengthWithoutConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> plugin.setAttackStrength(Plugin.AttackStrength.INSANE));
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

    @Test
    public void shouldFailWhenSettingAlertThresholdWithoutConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> plugin.setAlertThreshold(Plugin.AlertThreshold.DEFAULT));
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

    @Test
    public void shouldFailToCloneIntoNonAbstractPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        Plugin pluginB = createNonAbstractPlugin();
        // When / Then
        assertThrows(InvalidParameterException.class, () -> pluginA.cloneInto(pluginB));
    }

    @Test
    public void shouldFailToCloneIntoPluginWithoutConfig() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        AbstractPlugin pluginB = createAbstractPlugin();
        // When / Then
        assertThrows(NullPointerException.class, () -> pluginA.cloneInto(pluginB));
    }

    @Test
    public void shouldCloneIntoPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        pluginA.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        pluginA.setDefaultAlertThreshold(Plugin.AlertThreshold.MEDIUM);
        pluginA.setAttackStrength(Plugin.AttackStrength.INSANE);
        pluginA.setDefaultAttackStrength(Plugin.AttackStrength.LOW);
        pluginA.setTechSet(TechSet.getAllTech());
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

    @Test
    public void shouldFailToLoadFromConfigIfConfigNotSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // When / Then
        assertThrows(NullPointerException.class, () -> plugin.loadFrom(config));
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
        assertThat(
                config.getString(basePropertyKey + "level"),
                is(equalTo(Plugin.AlertThreshold.LOW.name())));
        assertThat(
                config.getString(basePropertyKey + "strength"),
                is(equalTo(Plugin.AttackStrength.HIGH.name())));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith7ParamsBingo() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String otherInfo = "otherInfo";
        HttpMessage alertMessage = createAlertMessage();
        // When
        plugin.bingo(risk, confidence, uri, param, attack, otherInfo, alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(plugin.getName())));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(plugin.getDescription())));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo("")));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(plugin.getSolution())));
        assertThat(alert.getReference(), is(equalTo(plugin.getReference())));
        assertThat(alert.getCweId(), is(equalTo(plugin.getCweId())));
        assertThat(alert.getWascId(), is(equalTo(plugin.getWascId())));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith7ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = null;
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(Alert.RISK_LOW, Alert.CONFIDENCE_HIGH, uri, "", "", "", alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith7ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = "";
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(Alert.RISK_LOW, Alert.CONFIDENCE_HIGH, uri, "", "", "", alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith8ParamsBingo() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String otherInfo = "otherInfo";
        String evidence = "evidence";
        HttpMessage alertMessage = createAlertMessage();
        // When
        plugin.bingo(risk, confidence, uri, param, attack, otherInfo, evidence, alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(plugin.getName())));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(plugin.getDescription())));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo(evidence)));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(plugin.getSolution())));
        assertThat(alert.getReference(), is(equalTo(plugin.getReference())));
        assertThat(alert.getCweId(), is(equalTo(plugin.getCweId())));
        assertThat(alert.getWascId(), is(equalTo(plugin.getWascId())));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith8ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = null;
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(Alert.RISK_LOW, Alert.CONFIDENCE_HIGH, uri, "", "", "", "", alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith8ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = "";
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(Alert.RISK_LOW, Alert.CONFIDENCE_HIGH, uri, "", "", "", "", alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith10ParamsBingo() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String name = "name";
        String description = "description";
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String otherInfo = "otherInfo";
        String solution = "solution";
        HttpMessage alertMessage = createAlertMessage();
        // When
        plugin.bingo(
                risk,
                confidence,
                name,
                description,
                uri,
                param,
                attack,
                otherInfo,
                solution,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(name)));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(description)));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo("")));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(solution)));
        assertThat(alert.getReference(), is(equalTo(plugin.getReference())));
        assertThat(alert.getCweId(), is(equalTo(plugin.getCweId())));
        assertThat(alert.getWascId(), is(equalTo(plugin.getWascId())));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith10ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = null;
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW, Alert.CONFIDENCE_HIGH, "", "", uri, "", "", "", "", alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith10ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = "";
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW, Alert.CONFIDENCE_HIGH, "", "", uri, "", "", "", "", alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith11ParamsBingo() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String name = "name";
        String description = "description";
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String evidence = "evidence";
        String otherInfo = "otherInfo";
        String solution = "solution";
        HttpMessage alertMessage = createAlertMessage();
        // When
        plugin.bingo(
                risk,
                confidence,
                name,
                description,
                uri,
                param,
                attack,
                otherInfo,
                solution,
                evidence,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(name)));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(description)));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo(evidence)));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(solution)));
        assertThat(alert.getReference(), is(equalTo(plugin.getReference())));
        assertThat(alert.getCweId(), is(equalTo(plugin.getCweId())));
        assertThat(alert.getWascId(), is(equalTo(plugin.getWascId())));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith11ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = null;
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW,
                Alert.CONFIDENCE_HIGH,
                "",
                "",
                uri,
                "",
                "",
                "",
                "",
                "",
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith11ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = "";
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW,
                Alert.CONFIDENCE_HIGH,
                "",
                "",
                uri,
                "",
                "",
                "",
                "",
                "",
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith13ParamsBingo() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String name = "name";
        String description = "description";
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String evidence = "evidence";
        String otherInfo = "otherInfo";
        String solution = "solution";
        int cweId = 111;
        int wascId = 222;
        HttpMessage alertMessage = createAlertMessage();
        // When
        plugin.bingo(
                risk,
                confidence,
                name,
                description,
                uri,
                param,
                attack,
                otherInfo,
                solution,
                evidence,
                cweId,
                wascId,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(name)));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(description)));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo(evidence)));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(solution)));
        assertThat(alert.getReference(), is(equalTo(plugin.getReference())));
        assertThat(alert.getCweId(), is(equalTo(cweId)));
        assertThat(alert.getWascId(), is(equalTo(wascId)));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith13ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = null;
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW,
                Alert.CONFIDENCE_HIGH,
                "",
                "",
                uri,
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith13ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = "";
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW,
                Alert.CONFIDENCE_HIGH,
                "",
                "",
                uri,
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith14ParamsBingo() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String name = "name";
        String description = "description";
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String evidence = "evidence";
        String otherInfo = "otherInfo";
        String solution = "solution";
        String reference = "reference";
        int cweId = 111;
        int wascId = 222;
        HttpMessage alertMessage = createAlertMessage();
        // When
        plugin.bingo(
                risk,
                confidence,
                name,
                description,
                uri,
                param,
                attack,
                otherInfo,
                solution,
                evidence,
                reference,
                cweId,
                wascId,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(name)));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(description)));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo(evidence)));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(solution)));
        assertThat(alert.getReference(), is(equalTo(reference)));
        assertThat(alert.getCweId(), is(equalTo(cweId)));
        assertThat(alert.getWascId(), is(equalTo(wascId)));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith14ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = null;
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW,
                Alert.CONFIDENCE_HIGH,
                "",
                "",
                uri,
                "",
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldRaiseAlertWith14ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = "";
        String messageUri = "http://example.com/";
        HttpMessage alertMessage = createAlertMessage(messageUri);
        // When
        plugin.bingo(
                Alert.RISK_LOW,
                Alert.CONFIDENCE_HIGH,
                "",
                "",
                uri,
                "",
                "",
                "",
                "",
                "",
                "",
                0,
                0,
                alertMessage);
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getUri(), is(equalTo(messageUri)));
    }

    @Test
    public void shouldFailToRaiseAlertWithNewAlertIfNoMessageProvided() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        // When / Then
        assertThrows(IllegalStateException.class, () -> plugin.newAlert().raise());
    }

    @Test
    public void shouldRaiseAlertWithNewAlertUsingPluginData() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        String uri = "http://example.com";
        HttpMessage alertMessage = createAlertMessage(uri);
        // When
        plugin.newAlert().setMessage(alertMessage).raise();
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(plugin.getName())));
        assertThat(alert.getRisk(), is(equalTo(plugin.getRisk())));
        assertThat(alert.getConfidence(), is(equalTo(Alert.CONFIDENCE_MEDIUM)));
        assertThat(alert.getDescription(), is(equalTo(plugin.getDescription())));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo("")));
        assertThat(alert.getAttack(), is(equalTo("")));
        assertThat(alert.getEvidence(), is(equalTo("")));
        assertThat(alert.getOtherInfo(), is(equalTo("")));
        assertThat(alert.getSolution(), is(equalTo(plugin.getSolution())));
        assertThat(alert.getReference(), is(equalTo(plugin.getReference())));
        assertThat(alert.getCweId(), is(equalTo(plugin.getCweId())));
        assertThat(alert.getWascId(), is(equalTo(plugin.getWascId())));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    public void shouldRaiseAlertWithNewAlert() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        HostProcess hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String name = "name";
        String description = "description";
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String evidence = "evidence";
        String otherInfo = "otherInfo";
        String solution = "solution";
        String reference = "reference";
        int cweId = 111;
        int wascId = 222;
        HttpMessage alertMessage = createAlertMessage();
        // When
        plugin.newAlert()
                .setRisk(risk)
                .setConfidence(confidence)
                .setName(name)
                .setDescription(description)
                .setUri(uri)
                .setParam(param)
                .setAttack(attack)
                .setOtherInfo(otherInfo)
                .setSolution(solution)
                .setEvidence(evidence)
                .setReference(reference)
                .setCweId(cweId)
                .setWascId(wascId)
                .setMessage(alertMessage)
                .raise();
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(name)));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(description)));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo(evidence)));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(solution)));
        assertThat(alert.getReference(), is(equalTo(reference)));
        assertThat(alert.getCweId(), is(equalTo(cweId)));
        assertThat(alert.getWascId(), is(equalTo(wascId)));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    public void shouldFailToSaveToConfigIfConfigNotSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // When / Then
        assertThrows(NullPointerException.class, () -> plugin.saveTo(config));
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
        assertThat(
                config.getString(basePropertyKey + "level"),
                is(equalTo(Plugin.AlertThreshold.HIGH.name())));
        assertThat(
                config.getString(basePropertyKey + "strength"),
                is(equalTo(Plugin.AttackStrength.INSANE.name())));
    }

    @Test
    public void shouldCheckPage200WithParent() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage200(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, type);
        verifyNoInteractions(analyser);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldCheckPage200WithParentAndFallbackToAnalyser(boolean expectedResult) {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        given(analyser.isFileExist(message)).willReturn(expectedResult);
        // When
        boolean result = plugin.isPage200(message);
        // Then
        assertThat(result, is(equalTo(expectedResult)));
        verify(parent).isCustomPage(message, type);
        verify(analyser).isFileExist(message);
    }

    @Test
    public void isPage200ShouldReturnFalseIfCustomPage404Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage200(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verifyNoInteractions(analyser);
    }

    @Test
    public void isPage200ShouldReturnFalseIfCustomPage500Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage200(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verifyNoInteractions(analyser);
    }

    @Test
    public void shouldCheckPage404WithParent() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, type);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldCheckPage404WithParentAndFallbacktoAnalyser(boolean expectedResult) {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // 404 is the opposite of isFileExist
        given(analyser.isFileExist(message)).willReturn(!expectedResult);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(expectedResult)));
        verify(parent).isCustomPage(message, type);
        verify(analyser).isFileExist(message);
    }

    @Test
    public void isPage404ShouldReturnTrueIfNoCustomPageMatchButStatusCode404() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(404);
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage404ShouldReturnTrueIfCustomPageMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage404ShouldReturnTrueIfCustomPageDoesNotMatchAndAnalyserIndicates404() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(404);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void
            isPage404ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesAndAnalyserIndicates200() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void
            isPage404ShouldReturnTrueIfNoStatusCodeOrCustomPageMatchesAndAnalyserIndicates404() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage404ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage200Does() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
    }

    @Test
    public void isPage404ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage500Does() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage404(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
    }

    @Test
    public void shouldCheckPage500WithParent() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage500(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage500ShouldReturnTrueIfNoCustomPageMatchButStatusCode500() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(500);
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage500(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage500ShouldReturnTrueIfCustomPageMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage500(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage500ShouldReturnTrueIfCustomPageDoesNotMatchesButStatusCodeDoes() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(500);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage500(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage500ShouldReturnFalseIfNoStatusCodeOrCustomPageMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage500(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isPage500ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage200Does() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage500(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
    }

    @Test
    public void isPage500ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage404Does() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPage500(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void shouldCheckPageOtherWithParent(boolean expectedResult) {
        // Given
        CustomPage.Type type = CustomPage.Type.OTHER;
        given(parent.isCustomPage(message, type)).willReturn(expectedResult);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPageOther(message);
        // Then
        assertThat(result, is(equalTo(expectedResult)));
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isSuccessShouldReturnTrueIfCustomPage200Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(analyser.isFileExist(message)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isSuccess(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isSuccessShouldReturnTrueIfStatusCodeMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(204);
        plugin.init(message, parent);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(false);
        // When
        boolean result = plugin.isSuccess(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void
            isSuccessShouldReturnTrueIfNoStatusCodeNorCustomPageMatchesButAnalyserIndicates200() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isSuccess(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void
            isSuccessShouldReturnFalseIfNoStatusCodeNorCustomPageMatchesAndAnalyserIndicates404() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(302);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isSuccess(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isSuccessShouldReturnFalseIfCustomPage404Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isSuccess(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
    }

    @Test
    public void isSuccessShouldReturnFalseIfCustomPage500Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isSuccess(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
    }

    @Test
    public void isClientErrorShouldReturnTrueIfCustomPage404Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(analyser.isFileExist(message)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isClientError(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isClientErrorShouldReturnTrueIfStatusCodeMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(403);
        plugin.init(message, parent);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(false);
        // When
        boolean result = plugin.isClientError(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, type);
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
    }

    @Test
    public void
            isClientErrorShouldReturnTrueIfNoStatusCodeOrCustomPageMatchesButAnalyserIndicates404() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isClientError(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void
            isClientErrorShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButAnalyserIndicates200() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isClientError(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isClientErrorShouldReturnFalseIfCustomPage200Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isClientError(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
    }

    @Test
    public void isClientErrorShouldReturnFalseIfCustomPage500Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.ERROR_500)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        given(parent.getAnalyser().isFileExist(message)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isClientError(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.ERROR_500);
    }

    @Test
    public void isServerErrorShouldReturnTrueIfCustomPage500Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isServerError(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isServerErrorShouldReturnTrueIfStatusCodeMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(503);
        plugin.init(message, parent);
        given(parent.isCustomPage(message, type)).willReturn(false);
        // When
        boolean result = plugin.isServerError(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isServerErrorShouldReturnFalseIfNoStatusCodeOrCustomPageMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        message.getResponseHeader().setStatusCode(200);
        given(parent.isCustomPage(message, type)).willReturn(false);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isServerError(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    public void isServerErrorShouldReturnFalseIfCustomPage200Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isServerError(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
    }

    @Test
    public void isServerErrorShouldReturnFalseIfCustomPage404Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage message = new HttpMessage();
        given(parent.isCustomPage(message, type)).willReturn(false);
        given(parent.isCustomPage(message, CustomPage.Type.NOTFOUND_404)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isServerError(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.NOTFOUND_404);
    }

    private static HttpMessage createAlertMessage() {
        return createAlertMessage(null);
    }

    private static HttpMessage createAlertMessage(String requestUri) {
        HttpMessage message = mock(HttpMessage.class);
        HttpRequestHeader requestHeader = mock(HttpRequestHeader.class);
        when(message.getRequestHeader()).thenReturn(requestHeader);
        if (requestUri != null) {
            URI uri = mock(URI.class);
            when(uri.toString()).thenReturn(requestUri);
            when(requestHeader.getURI()).thenReturn(uri);
        }
        when(message.getRequestBody()).thenReturn(mock(HttpRequestBody.class));
        return message;
    }

    private static Alert getRaisedAlert(HostProcess hostProcess) {
        ArgumentCaptor<Alert> arg = ArgumentCaptor.forClass(Alert.class);
        verify(hostProcess).alertFound(arg.capture());
        return arg.getValue();
    }
}
