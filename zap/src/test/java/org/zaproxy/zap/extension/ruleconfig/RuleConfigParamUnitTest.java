/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.ruleconfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link RuleConfigParam}. */
class RuleConfigParamUnitTest {

    ZapXmlConfiguration configuration;

    RuleConfigParam rcp;

    @BeforeEach
    void setUp() throws Exception {
        rcp = new RuleConfigParam();
        configuration = new ZapXmlConfiguration();
        rcp.load(configuration);
    }

    @Test
    void shouldReturnNullIfNoKey() {
        assertThat(rcp.getRuleConfig("key")).isEqualTo(null);
    }

    @Test
    void shouldRecordAddedRuleCorrectly() {
        rcp.addRuleConfig("key", "defaultValue", "value");
        assertThat(rcp.getRuleConfig("key").getKey()).isEqualTo("key");
        assertThat(rcp.getRuleConfig("key").getDefaultValue()).isEqualTo("defaultValue");
        assertThat(rcp.getRuleConfig("key").getValue()).isEqualTo("value");
        assertThat(configuration.getString("key")).isEqualTo("value");
    }

    @Test
    void shouldUpdateRuleCorrectly() {
        rcp.addRuleConfig("key", "defaultValue", "value");
        rcp.setRuleConfigValue("key", "new value");
        assertThat(rcp.getRuleConfig("key").getKey()).isEqualTo("key");
        assertThat(rcp.getRuleConfig("key").getDefaultValue()).isEqualTo("defaultValue");
        assertThat(rcp.getRuleConfig("key").getValue()).isEqualTo("new value");
        assertThat(configuration.getString("key")).isEqualTo("new value");
    }

    @Test
    void shouldFailToUpdateMissingKey() {
        // Given
        rcp.addRuleConfig("key", "defaultValue", "value");
        // When / Then
        assertThrows(
                IllegalArgumentException.class, () -> rcp.setRuleConfigValue("key2", "new value"));
    }

    @Test
    void shouldFailToResetMissingKey() {
        // Given
        rcp.addRuleConfig("key", "defaultValue", "value");
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> rcp.resetRuleConfigValue("key2"));
    }

    @Test
    void shouldResetRuleCorrectly() {
        rcp.addRuleConfig("key", "defaultValue", "value");
        rcp.resetRuleConfigValue("key");
        assertThat(rcp.getRuleConfig("key").getKey()).isEqualTo("key");
        assertThat(rcp.getRuleConfig("key").getDefaultValue()).isEqualTo("defaultValue");
        assertThat(rcp.getRuleConfig("key").getValue()).isEqualTo("defaultValue");
        assertThat(configuration.getString("key")).isEqualTo("defaultValue");
    }

    @Test
    void shouldResetAllRulesCorrectly() {
        rcp.addRuleConfig("key1", "defaultValue1", "value1");
        rcp.addRuleConfig("key2", "defaultValue2", "value2");
        rcp.resetAllRuleConfigValues();

        assertThat(rcp.getRuleConfig("key1").getKey()).isEqualTo("key1");
        assertThat(rcp.getRuleConfig("key1").getDefaultValue()).isEqualTo("defaultValue1");
        assertThat(rcp.getRuleConfig("key1").getValue()).isEqualTo("defaultValue1");
        assertThat(rcp.getRuleConfig("key2").getKey()).isEqualTo("key2");
        assertThat(rcp.getRuleConfig("key2").getDefaultValue()).isEqualTo("defaultValue2");
        assertThat(rcp.getRuleConfig("key2").getValue()).isEqualTo("defaultValue2");
        assertThat(configuration.getString("key1")).isEqualTo("defaultValue1");
        assertThat(configuration.getString("key2")).isEqualTo("defaultValue2");
    }
}
