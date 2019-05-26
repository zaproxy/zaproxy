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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class RuleConfigParamUnitTest {

	ZapXmlConfiguration configuration;

	RuleConfigParam rcp;

	@Before
	public void setUp() throws Exception {
		rcp = new RuleConfigParam();
		configuration = new ZapXmlConfiguration();
		rcp.load(configuration);
	}

	@Test
	public void shouldReturnNullIfNoKey() {
		assertThat(rcp.getRuleConfig("key"), is(equalTo(null)));
	}

	@Test
	public void shouldRecordAddedRuleCorrectly() {
		rcp.addRuleConfig("key", "defaultValue", "value");
		assertThat(rcp.getRuleConfig("key").getKey(), is(equalTo("key")));
		assertThat(rcp.getRuleConfig("key").getDefaultValue(), is(equalTo("defaultValue")));
		assertThat(rcp.getRuleConfig("key").getValue(), is(equalTo("value")));
		assertThat(configuration.getString("key"), is(equalTo("value")));
	}

	@Test
	public void shouldUpdateRuleCorrectly() {
		rcp.addRuleConfig("key", "defaultValue", "value");
		rcp.setRuleConfigValue("key", "new value");
		assertThat(rcp.getRuleConfig("key").getKey(), is(equalTo("key")));
		assertThat(rcp.getRuleConfig("key").getDefaultValue(), is(equalTo("defaultValue")));
		assertThat(rcp.getRuleConfig("key").getValue(), is(equalTo("new value")));
		assertThat(configuration.getString("key"), is(equalTo("new value")));
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldFailToUpdateMissingKey() {
		rcp.addRuleConfig("key", "defaultValue", "value");
		rcp.setRuleConfigValue("key2", "new value");
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldFailToResetMissingKey() {
		rcp.addRuleConfig("key", "defaultValue", "value");
		rcp.resetRuleConfigValue("key2");
	}

	@Test
	public void shouldResetRuleCorrectly() {
		rcp.addRuleConfig("key", "defaultValue", "value");
		rcp.resetRuleConfigValue("key");
		assertThat(rcp.getRuleConfig("key").getKey(), is(equalTo("key")));
		assertThat(rcp.getRuleConfig("key").getDefaultValue(), is(equalTo("defaultValue")));
		assertThat(rcp.getRuleConfig("key").getValue(), is(equalTo("defaultValue")));
		assertThat(configuration.getString("key"), is(equalTo("defaultValue")));
	}

	@Test
	public void shouldResetAllRulesCorrectly() {
		rcp.addRuleConfig("key1", "defaultValue1", "value1");
		rcp.addRuleConfig("key2", "defaultValue2", "value2");
		rcp.resetAllRuleConfigValues();;
		assertThat(rcp.getRuleConfig("key1").getKey(), is(equalTo("key1")));
		assertThat(rcp.getRuleConfig("key1").getDefaultValue(), is(equalTo("defaultValue1")));
		assertThat(rcp.getRuleConfig("key1").getValue(), is(equalTo("defaultValue1")));
		assertThat(rcp.getRuleConfig("key2").getKey(), is(equalTo("key2")));
		assertThat(rcp.getRuleConfig("key2").getDefaultValue(), is(equalTo("defaultValue2")));
		assertThat(rcp.getRuleConfig("key2").getValue(), is(equalTo("defaultValue2")));
		assertThat(configuration.getString("key1"), is(equalTo("defaultValue1")));
		assertThat(configuration.getString("key2"), is(equalTo("defaultValue2")));
	}
}
