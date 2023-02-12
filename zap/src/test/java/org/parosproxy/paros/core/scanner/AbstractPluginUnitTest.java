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
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link AbstractPlugin}. */
class AbstractPluginUnitTest extends PluginTestUtils {

    private static final List<String> METHODS_NO_ENCLOSED_CONTENT =
            Arrays.asList(
                    HttpRequestHeader.CONNECT,
                    "connect",
                    HttpRequestHeader.DELETE,
                    "delete",
                    HttpRequestHeader.GET,
                    "get",
                    HttpRequestHeader.HEAD,
                    "head",
                    HttpRequestHeader.TRACE,
                    "trace");

    HostProcess parent;
    HttpMessage message;
    Analyser analyser;
    AbstractPlugin plugin;

    @BeforeEach
    void setup() {
        Constant.messages = mock(I18N.class);
        parent = mock(HostProcess.class);
        message = mock(HttpMessage.class);
        analyser = mock(Analyser.class);
        plugin = spy(AbstractPlugin.class);
    }

    @Test
    void shouldFailToSetNullTechSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> plugin.setTechSet(null));
    }

    @Test
    void shouldHaveAllTechSetByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getTechSet(), is(equalTo(TechSet.getAllTech())));
    }

    @Test
    void shouldNotHaveConfigByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getConfig(), is(equalTo(null)));
    }

    @Test
    void shouldRetrieveConfigSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // WHen
        plugin.setConfig(config);
        // Then
        assertThat(plugin.getConfig(), is(equalTo(config)));
    }

    @Test
    void shouldRetrieveClassNameForCodeName() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getCodeName(), is(equalTo("PluginTestUtils$TestPlugin")));
    }

    @Test
    void shouldNotHaveDependenciesByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getDependency(), is(emptyArray()));
    }

    @Test
    void shouldRetrieveUndefinedWascId() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getWascId(), is(equalTo(0)));
    }

    @Test
    void shouldRetrieveUndefinedCweId() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getCweId(), is(equalTo(0)));
    }

    @Test
    void shouldRetrieveMediumRiskByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getRisk(), is(equalTo(Alert.RISK_MEDIUM)));
    }

    @Test
    void shouldRetrieveZeroDelayInMsByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getDelayInMs(), is(equalTo(0)));
    }

    @Test
    void shouldRetrieveDelaySet() {
        // Given
        int aDelayInMs = 1234;
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        plugin.setDelayInMs(aDelayInMs);
        // Then
        assertThat(plugin.getDelayInMs(), is(equalTo(aDelayInMs)));
    }

    @Test
    void shouldRetrieveUnknownStatusByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getStatus(), is(equalTo(AddOn.Status.unknown)));
    }

    @Test
    void shouldRetrieveStatusSet() {
        // Given
        AddOn.Status aStatus = AddOn.Status.example;
        AbstractPlugin plugin = createAbstractPlugin();
        // When
        plugin.setStatus(aStatus);
        // Then
        assertThat(plugin.getStatus(), is(equalTo(aStatus)));
    }

    @Test
    void shouldBeEnabledByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    void shouldBeDisabledWhenOffThresholdSet() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setAlertThreshold(AlertThreshold.OFF);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.FALSE)));
    }

    @Test
    void shouldBeDisabledWhenDefaultOffThresholdSet() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setDefaultAlertThreshold(AlertThreshold.OFF);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.FALSE)));
    }

    private static Stream<Arguments> alertThresholdsAndEnabledState() {
        return Stream.of(AlertThreshold.values())
                .flatMap(e -> Stream.of(arguments(e, true), arguments(e, false)));
    }

    @ParameterizedTest
    @MethodSource("alertThresholdsAndEnabledState")
    void shouldNotChangeEnabledStateIfNotUsingDefaultThresholdWhenDefaultSet(
            AlertThreshold defaultThreshold, boolean enabled) {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(AlertThreshold.MEDIUM);
        plugin.setEnabled(enabled);
        // When
        plugin.setDefaultAlertThreshold(defaultThreshold);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(enabled)));
    }

    @Test
    void shouldBeDisabledWhenOffAndDefaultOffThresholdSet() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setAlertThreshold(AlertThreshold.OFF);
        plugin.setDefaultAlertThreshold(AlertThreshold.OFF);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.FALSE)));
    }

    @ParameterizedTest
    @EnumSource(
            value = AlertThreshold.class,
            names = {"LOW", "MEDIUM", "HIGH"})
    void shouldBeEnabledWhenThresholdSetButDefaultOff(AlertThreshold threshold) {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setDefaultAlertThreshold(AlertThreshold.OFF);
        plugin.setAlertThreshold(threshold);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.TRUE)));
    }

    @ParameterizedTest
    @EnumSource(
            value = AlertThreshold.class,
            names = {"LOW", "MEDIUM", "HIGH"})
    void shouldBeReEnabledWhenDefaultThresholdSetFromOff(AlertThreshold threshold) {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(AlertThreshold.DEFAULT);
        plugin.setDefaultAlertThreshold(AlertThreshold.OFF);
        // When
        plugin.setDefaultAlertThreshold(threshold);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(Boolean.TRUE)));
    }

    @Test
    void shouldFailWhenSettingEnabledStateWithoutConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(NullPointerException.class, () -> plugin.setEnabled(false));
    }

    @Test
    void shouldRetrieveEnabledStateSet() {
        // Given
        boolean aState = false;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setEnabled(aState);
        // Then
        assertThat(plugin.isEnabled(), is(equalTo(aState)));
    }

    @Test
    void shouldRetrieveMediumAttackStrengthByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAttackStrength(), is(equalTo(Plugin.AttackStrength.MEDIUM)));
    }

    @Test
    void shouldFailWhenSettingAttackStrengthWithoutConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> plugin.setAttackStrength(Plugin.AttackStrength.INSANE));
    }

    @Test
    void shouldRetrieveAttackStrengthSet() {
        // Given
        Plugin.AttackStrength anAttackStrength = Plugin.AttackStrength.INSANE;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setAttackStrength(anAttackStrength);
        // Then
        assertThat(plugin.getAttackStrength(), is(equalTo(anAttackStrength)));
    }

    @Test
    void shouldRetrieveDefaultAttackStrengthByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAttackStrength(true), is(equalTo(Plugin.AttackStrength.DEFAULT)));
    }

    @Test
    void shouldRetrieveDefaultAttackStrengthSet() {
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
    void shouldRetrieveDefaultAttackStrengthSetAsDefault() {
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
    void shouldRetrieveMediumAlertThresholdByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAlertThreshold(), is(equalTo(Plugin.AlertThreshold.MEDIUM)));
    }

    @Test
    void shouldFailWhenSettingAlertThresholdWithoutConfig() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> plugin.setAlertThreshold(Plugin.AlertThreshold.DEFAULT));
    }

    @Test
    void shouldRetrieveAlertThresholdSet() {
        // Given
        Plugin.AlertThreshold anAlertThreshold = Plugin.AlertThreshold.HIGH;
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        // When
        plugin.setAlertThreshold(anAlertThreshold);
        // Then
        assertThat(plugin.getAlertThreshold(), is(equalTo(anAlertThreshold)));
    }

    @Test
    void shouldDisablePluginWhenAlertThresholdSetToOff() {
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
    void shouldEnablePluginWhenAlertThresholdSetToNonOff() {
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
    void shouldSetAlertThresholdToDefaultWhenEnablingPluginWithOffAlertThreshold() {
        // Given
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setAlertThreshold(Plugin.AlertThreshold.OFF);
        // When
        plugin.setEnabled(true);
        // Then
        assertThat(plugin.getAlertThreshold(true), is(equalTo(Plugin.AlertThreshold.DEFAULT)));
    }

    @Test
    void shouldRetrieveDefaultAlertThresholdByDefault() {
        // Given / When
        AbstractPlugin plugin = createAbstractPlugin();
        // Then
        assertThat(plugin.getAlertThreshold(true), is(equalTo(Plugin.AlertThreshold.DEFAULT)));
    }

    @Test
    void shouldRetrieveOffAlertThresholdByDefaultIfDisabled() {
        // Given / When
        AbstractPlugin plugin = createAbstractPluginWithConfig();
        plugin.setEnabled(false);
        // Then
        assertThat(plugin.getAlertThreshold(true), is(equalTo(Plugin.AlertThreshold.OFF)));
    }

    @Test
    void shouldRetrieveDefaultAlertThresholdSet() {
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
    void shouldRetrieveDefaultAlertThresholdSetAsDefault() {
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
    void shouldFailToCloneIntoNonAbstractPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        Plugin pluginB = createNonAbstractPlugin();
        // When / Then
        assertThrows(InvalidParameterException.class, () -> pluginA.cloneInto(pluginB));
    }

    @Test
    void shouldFailToCloneIntoPluginWithoutConfig() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        AbstractPlugin pluginB = createAbstractPlugin();
        // When / Then
        assertThrows(NullPointerException.class, () -> pluginA.cloneInto(pluginB));
    }

    @Test
    void shouldCloneIntoPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPluginWithConfig();
        pluginA.setAlertThreshold(Plugin.AlertThreshold.HIGH);
        pluginA.setDefaultAlertThreshold(Plugin.AlertThreshold.MEDIUM);
        pluginA.setAttackStrength(Plugin.AttackStrength.INSANE);
        pluginA.setDefaultAttackStrength(Plugin.AttackStrength.LOW);
        pluginA.setTechSet(TechSet.getAllTech());
        pluginA.setStatus(AddOn.Status.beta);
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
    void shouldNotBeEqualToNonAbstractPlugin() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin();
        Plugin pluginB = createNonAbstractPlugin();
        // When
        boolean equals = pluginA.equals(pluginB);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldNotBeEqualToAbstractPluginWithDifferentId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(1);
        AbstractPlugin pluginB = createAbstractPlugin(5);
        // When
        boolean equals = pluginA.equals(pluginB);
        // Then
        assertThat(equals, is(equalTo(false)));
    }

    @Test
    void shouldBeEqualToAbstractPluginWithSameId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(1);
        AbstractPlugin pluginB = createAbstractPlugin(1);
        // When
        boolean equals = pluginA.equals(pluginB);
        // Then
        assertThat(equals, is(equalTo(true)));
    }

    @Test
    void shouldBeLesserThanAbstractPluginWithGreaterId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(1);
        AbstractPlugin pluginB = createAbstractPlugin(10);
        // When
        int comparisonResult = pluginA.compareTo(pluginB);
        // Then
        assertThat(comparisonResult, is(equalTo(-1)));
    }

    @Test
    void shouldBeSameAsAbstractPluginWithSameId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(10);
        AbstractPlugin pluginB = createAbstractPlugin(10);
        // When
        int comparisonResult = pluginA.compareTo(pluginB);
        // Then
        assertThat(comparisonResult, is(equalTo(0)));
    }

    @Test
    void shouldBeGreaterThanAbstractPluginWithLesserId() {
        // Given
        AbstractPlugin pluginA = createAbstractPlugin(10);
        AbstractPlugin pluginB = createAbstractPlugin(1);
        // When
        int comparisonResult = pluginA.compareTo(pluginB);
        // Then
        assertThat(comparisonResult, is(equalTo(1)));
    }

    @Test
    void shouldFailToLoadFromConfigIfConfigNotSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // When / Then
        assertThrows(NullPointerException.class, () -> plugin.loadFrom(config));
    }

    @Test
    void shouldNotLoadFromConfigIfPluginHasDifferentId() {
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
    void shouldLoadFromConfigIfPluginsHaveSameId() {
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
    void shouldJustSaveEnabledStateToOwnConfigWhenLoadFromEmptyConfig() {
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
    void shouldSaveToOwnConfigWhenLoadFromOtherConfig() {
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
    void shouldRaiseAlertWith7ParamsBingo() {
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
    void shouldRaiseAlertWith7ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
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
    void shouldRaiseAlertWith7ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
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
    void shouldRaiseAlertWith8ParamsBingo() {
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
    void shouldRaiseAlertWith8ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
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
    void shouldRaiseAlertWith8ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
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
    void shouldRaiseAlertWith10ParamsBingo() {
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
    void shouldRaiseAlertWith10ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
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
    void shouldRaiseAlertWith10ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
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
    void shouldRaiseAlertWith11ParamsBingo() {
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
    void shouldRaiseAlertWith11ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
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
    void shouldRaiseAlertWith11ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
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
    void shouldRaiseAlertWith13ParamsBingo() {
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
    void shouldRaiseAlertWith13ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
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
    void shouldRaiseAlertWith13ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
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
    void shouldRaiseAlertWith14ParamsBingo() {
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
    void shouldRaiseAlertWith14ParamsBingoDefaultingToMessageUriWhenGivenUriIsNull() {
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
    void shouldRaiseAlertWith14ParamsBingoDefaultingToMessageUriWhenGivenUriIsEmpty() {
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
    void shouldFailToRaiseAlertWithNewAlertIfNoMessageProvided() {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        // When / Then
        assertThrows(IllegalStateException.class, () -> plugin.newAlert().raise());
    }

    @Test
    void shouldRaiseAlertWithNewAlertUsingPluginData() {
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
    void shouldRaiseAlertWithNewAlert() {
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
    void shouldFailToSaveToConfigIfConfigNotSet() {
        // Given
        AbstractPlugin plugin = createAbstractPlugin();
        Configuration config = new ZapXmlConfiguration();
        // When / Then
        assertThrows(NullPointerException.class, () -> plugin.saveTo(config));
    }

    @Test
    void shouldSaveToConfig() {
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
    void shouldCheckPage200WithParent() {
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
    void shouldCheckPage200WithParentAndFallbackToAnalyser(boolean expectedResult) {
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
    void isPage200ShouldReturnFalseIfCustomPage404Matches() {
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
    void isPage200ShouldReturnFalseIfCustomPage500Matches() {
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
    void shouldCheckPage404WithParent() {
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
    void shouldCheckPage404WithParentAndFallbacktoAnalyser(boolean expectedResult) {
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
    void isPage404ShouldReturnTrueIfNoCustomPageMatchButStatusCode404() {
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
    void isPage404ShouldReturnTrueIfCustomPageMatches() {
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
    void isPage404ShouldReturnTrueIfCustomPageDoesNotMatchAndAnalyserIndicates404() {
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
    void isPage404ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesAndAnalyserIndicates200() {
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
    void isPage404ShouldReturnTrueIfNoStatusCodeOrCustomPageMatchesAndAnalyserIndicates404() {
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
    void isPage404ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage200Does() {
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
    void isPage404ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage500Does() {
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
    void shouldCheckPage500WithParent() {
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
    void isPage500ShouldReturnTrueIfNoCustomPageMatchButStatusCode500() {
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
    void isPage500ShouldReturnTrueIfCustomPageMatches() {
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
    void isPage500ShouldReturnTrueIfCustomPageDoesNotMatchesButStatusCodeDoes() {
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
    void isPage500ShouldReturnFalseIfNoStatusCodeOrCustomPageMatches() {
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
    void isPage500ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage200Does() {
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
    void isPage500ShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButCustomPage404Does() {
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
    void shouldCheckPageOtherWithParent(boolean expectedResult) {
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
    void shouldCheckAuthIssueWithParent() {
        // Given
        CustomPage.Type type = CustomPage.Type.AUTH_4XX;
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPageAuthIssue(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, type);
        verifyNoInteractions(analyser);
    }

    @Test
    void isPageAuthIssueShouldReturnTrueIfCustomPageMatches() {
        // Given
        CustomPage.Type type = CustomPage.Type.AUTH_4XX;
        HttpMessage message = new HttpMessage();
        given(parent.isCustomPage(message, type)).willReturn(true);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPageAuthIssue(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verify(parent).isCustomPage(message, type);
    }

    @Test
    void isPageAuthIssueShouldReturnFalseIfCustomPage200Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.AUTH_4XX;
        given(parent.isCustomPage(message, type)).willReturn(true);
        given(parent.isCustomPage(message, CustomPage.Type.OK_200)).willReturn(true);
        given(parent.getAnalyser()).willReturn(analyser);
        plugin.init(message, parent);
        // When
        boolean result = plugin.isPageAuthIssue(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(parent).isCustomPage(message, CustomPage.Type.OK_200);
        verifyNoInteractions(analyser);
    }

    @Test
    void isSuccessShouldReturnTrueIfCustomPage200Matches() {
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
    void isSuccessShouldReturnTrueIfStatusCodeMatches() {
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
    void isSuccessShouldReturnTrueIfNoStatusCodeNorCustomPageMatchesButAnalyserIndicates200() {
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
    void isSuccessShouldReturnFalseIfNoStatusCodeNorCustomPageMatchesAndAnalyserIndicates404() {
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
    void isSuccessShouldReturnFalseIfCustomPage404Matches() {
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
    void isSuccessShouldReturnFalseIfCustomPage500Matches() {
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
    void isClientErrorShouldReturnTrueIfCustomPage404Matches() {
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
    void isClientErrorShouldReturnTrueIfStatusCodeMatches() {
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
    void isClientErrorShouldReturnTrueIfNoStatusCodeOrCustomPageMatchesButAnalyserIndicates404() {
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
    void isClientErrorShouldReturnFalseIfNoStatusCodeOrCustomPageMatchesButAnalyserIndicates200() {
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
    void isClientErrorShouldReturnFalseIfCustomPage200Matches() {
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
    void isClientErrorShouldReturnFalseIfCustomPage500Matches() {
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
    void isServerErrorShouldReturnTrueIfCustomPage500Matches() {
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
    void isServerErrorShouldReturnTrueIfStatusCodeMatches() {
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
    void isServerErrorShouldReturnFalseIfNoStatusCodeOrCustomPageMatches() {
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
    void isServerErrorShouldReturnFalseIfCustomPage200Matches() {
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
    void isServerErrorShouldReturnFalseIfCustomPage404Matches() {
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

    @Test
    void shouldSendMessageWithScanRuleIdHeaderIfEnabled() throws IOException {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        ScannerParam scannerParam = mock(ScannerParam.class);
        given(scannerParam.isInjectPluginIdInHeader()).willReturn(true);
        given(parent.getScannerParam()).willReturn(scannerParam);
        HttpSender httpSender = mock(HttpSender.class);
        given(parent.getHttpSender()).willReturn(httpSender);
        plugin.init(message, parent);
        HttpMessage message = new HttpMessage(new URI("http://example.com/", true));
        // When
        plugin.sendAndReceive(message, true, true);
        // Then
        assertThat(
                message.getRequestHeader().getHeader(HttpHeader.X_ZAP_SCAN_ID),
                is(equalTo("123456789")));
    }

    @Test
    void shouldSendMessageWithoutScanRuleIdHeaderIfDisabled() throws IOException {
        // Given
        AbstractPlugin plugin = createDefaultPlugin();
        ScannerParam scannerParam = mock(ScannerParam.class);
        given(scannerParam.isInjectPluginIdInHeader()).willReturn(false);
        given(parent.getScannerParam()).willReturn(scannerParam);
        HttpSender httpSender = mock(HttpSender.class);
        given(parent.getHttpSender()).willReturn(httpSender);
        plugin.init(message, parent);
        HttpMessage message = new HttpMessage(new URI("http://example.com/", true));
        // When
        plugin.sendAndReceive(message, true, true);
        // Then
        assertThat(message.getRequestHeader().getHeader(HttpHeader.X_ZAP_SCAN_ID), is(nullValue()));
    }

    @ParameterizedTest
    @MethodSource("methodsNoEnclosedContent")
    void shouldNotAddContentLenthHeaderWhenNotExpected(String method) {
        // Given
        HttpMessage message = messageWithMethod(method);
        // When
        plugin.updateRequestContentLength(message);
        // Then
        assertContentLength(message.getRequestHeader(), null);
    }

    @ParameterizedTest
    @MethodSource("methodsNoEnclosedContent")
    void shouldRemoveExistingContentLengthHeaderWhenNotExpectedNorNeeded(String method) {
        // Given
        HttpMessage message = messageWithMethod(method);
        message.getRequestHeader().setContentLength(1234);
        // When
        plugin.updateRequestContentLength(message);
        // Then
        assertContentLength(message.getRequestHeader(), null);
    }

    @ParameterizedTest
    @MethodSource("allMethods")
    void shouldAddContentLengthHeaderWhenNeeded(String method) {
        // Given
        HttpMessage message = messageWithMethod(method);
        message.setRequestBody("1234");
        // When
        plugin.updateRequestContentLength(message);
        // Then
        assertContentLength(message.getRequestHeader(), "4");
    }

    @ParameterizedTest
    @MethodSource("allMethodsExceptNoEnclosedContent")
    void shouldAddZeroContentLengthHeaderWhenNeeded(String method) {
        // Given
        HttpMessage message = messageWithMethod(method);
        // When
        plugin.updateRequestContentLength(message);
        // Then
        assertContentLength(message.getRequestHeader(), "0");
    }

    @ParameterizedTest
    @MethodSource("allMethods")
    void shouldUpdateExistingContentLengthHeaderWhenNeeded(String method) {
        // Given
        HttpMessage message = messageWithMethod(method);
        message.setRequestBody("1234");
        message.getRequestHeader().setContentLength(42);
        // When
        plugin.updateRequestContentLength(message);
        // Then
        assertContentLength(message.getRequestHeader(), "4");
    }

    private static void assertContentLength(HttpHeader header, String value) {
        assertThat(header.getHeader(HttpHeader.CONTENT_LENGTH), is(equalTo(value)));
    }

    private static HttpMessage messageWithMethod(String method) {
        try {
            String header =
                    method
                            + (HttpRequestHeader.CONNECT.equalsIgnoreCase(method)
                                    ? " example.com "
                                    : " / ")
                            + "HTTP/1.1";
            return new HttpMessage(new HttpRequestHeader(header));
        } catch (HttpMalformedHeaderException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<String> allMethods() {
        return Stream.of(HttpRequestHeader.METHODS);
    }

    private static Stream<String> methodsNoEnclosedContent() {
        return METHODS_NO_ENCLOSED_CONTENT.stream();
    }

    private static Stream<String> allMethodsExceptNoEnclosedContent() {
        return allMethods().filter(e -> !METHODS_NO_ENCLOSED_CONTENT.contains(e));
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
