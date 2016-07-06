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
package org.zaproxy.zap.extension.ext;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration.FileConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zaproxy.zap.extension.ext.ExtensionParam.ExtensionState;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/**
 * Unit test for {@link ExtensionParam}.
 */
public class ExtensionParamUnitTest {

    @BeforeClass
    public static void suppressLogging() {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowToChangeReturnedExtensionsStateList() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When
        param.getExtensions().add(extensionState("Extension", false));
        // Then = UnsupportedOperationException
    }

    @Test
    public void shouldNotHaveConfigByDefault() {
        // Given / When
        ExtensionParam param = new ExtensionParam();
        // Then
        assertThat(param.getConfig(), is(equalTo(null)));
    }

    @Test
    public void shouldBeCloneableByDefault() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When
        ExtensionParam clone = param.clone();
        // Then
        assertThat(clone, is(not(equalTo(null))));
        assertThat(clone.getExtensions(), is(not(equalTo(null))));
        assertThat(param.getExtensions().size(), is(equalTo(0)));
    }

    @Test
    public void shouldNotHaveExtensionsStateByDefault() {
        // Given / When
        ExtensionParam param = new ExtensionParam();
        // Then
        assertThat(param.getExtensions(), is(not(equalTo(null))));
        assertThat(param.getExtensions().size(), is(equalTo(0)));
    }

    @Test
    public void shouldParseLoadedFileConfiguration() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig(false, true, false, true, true);
        // When
        param.load(config);
        // Then
        assertThat(param.getExtensions(), is(not(equalTo(null))));
        assertThat(param.getExtensions().size(), is(equalTo(5)));
        assertThat(param.getExtensions().get(0), is(equalTo(extensionState("Extension 1", false))));
        assertThat(param.getExtensions().get(1), is(equalTo(extensionState("Extension 2", true))));
        assertThat(param.getExtensions().get(2), is(equalTo(extensionState("Extension 3", false))));
        assertThat(param.getExtensions().get(3), is(equalTo(extensionState("Extension 4", true))));
        assertThat(param.getExtensions().get(4), is(equalTo(extensionState("Extension 5", true))));
    }

    @Test
    public void shouldDefaultToEmptyExtensionsStateIfLoadingMalformedFileConfiguration() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When
        param.load(createMalformedTestConfig());
        // Then
        assertThat(param.getExtensions(), is(not(equalTo(null))));
        assertThat(param.getExtensions().size(), is(equalTo(0)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotAllowToChangeReturnedListAfterLoadingTheConfigurations() {
        // Given
        ExtensionParam param = new ExtensionParam();
        param.load(createTestConfig());
        // When
        param.getExtensions().add(extensionState("Extension", false));
        // Then = UnsupportedOperationException
    }

    @Test
    public void shouldHaveLoadedConfigsAfterCloning() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig(false, true, false);
        param.load(config);
        // When
        ExtensionParam clone = param.clone();
        // Then
        assertThat(clone.getExtensions(), is(not(equalTo(null))));
        assertThat(clone.getExtensions().size(), is(equalTo(3)));
        assertThat(clone.getExtensions().get(0), is(equalTo(extensionState("Extension 1", false))));
        assertThat(clone.getExtensions().get(1), is(equalTo(extensionState("Extension 2", true))));
        assertThat(clone.getExtensions().get(2), is(equalTo(extensionState("Extension 3", false))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToPersistNullExtensionsState() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When
        param.setExtensions(null);
        // Then = IllegalArgumentException
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailToPersistExtensionsStateWithoutConfigurationFile() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When
        param.setExtensions(Collections.<ExtensionState> emptyList());
        // Then = NullPointerException
    }

    @Test
    public void shouldSetExtensionsState() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig();
        param.load(config);
        List<ExtensionState> states = extensionsState(true, false, true, false, true, true);
        // When
        param.setExtensions(states);
        // Then
        assertThat(param.getExtensions(), is(not(equalTo(null))));
        assertThat(param.getExtensions().size(), is(equalTo(6)));
        assertThat(param.getExtensions().get(0), is(equalTo(extensionState("Extension 1", true))));
        assertThat(param.getExtensions().get(1), is(equalTo(extensionState("Extension 2", false))));
        assertThat(param.getExtensions().get(2), is(equalTo(extensionState("Extension 3", true))));
        assertThat(param.getExtensions().get(3), is(equalTo(extensionState("Extension 4", false))));
        assertThat(param.getExtensions().get(4), is(equalTo(extensionState("Extension 5", true))));
        assertThat(param.getExtensions().get(5), is(equalTo(extensionState("Extension 6", true))));
    }

    @Test
    public void shouldPersistDisabledExtensions() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig();
        param.load(config);
        List<ExtensionState> states = extensionsState(true, false, true, false, true, true);
        // When
        param.setExtensions(states);
        // Then
        assertThat(config.getString("extensions.extension(0).name"), is(equalTo("Extension 2")));
        assertThat(config.getBoolean("extensions.extension(0).enabled"), is(equalTo(false)));
        assertThat(config.getString("extensions.extension(1).name"), is(equalTo("Extension 4")));
        assertThat(config.getBoolean("extensions.extension(1).enabled"), is(equalTo(false)));
        assertThat(config.containsKey("extensions.extension(2).name"), is(equalTo(false)));
        assertThat(config.containsKey("extensions.extension(2).enabled"), is(equalTo(false)));
    }

    private static ExtensionState extensionState(String name, boolean enabled) {
        return new ExtensionState(name, enabled);
    }

    private static List<ExtensionState> extensionsState(boolean... states) {
        List<ExtensionState> extensionsState = new ArrayList<>();
        if (states == null || states.length == 0) {
            return extensionsState;
        }

        for (int i = 0; i < states.length; ++i) {
            extensionsState.add(new ExtensionState("Extension " + (i + 1), states[i]));
        }
        return extensionsState;
    }

    private static FileConfiguration createTestConfig(boolean... states) {
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        if (states == null || states.length == 0) {
            return config;
        }

        for (int i = 0; i < states.length; ++i) {
            String elementBaseKey = "extensions.extension(" + i + ").";

            config.setProperty(elementBaseKey + "name", "Extension " + (i + 1));
            config.setProperty(elementBaseKey + "enabled", states[i]);
        }
        return config;
    }

    private static FileConfiguration createMalformedTestConfig() {
        ZapXmlConfiguration config = new ZapXmlConfiguration();
        for (int i = 0; i < 3; ++i) {
            String elementBaseKey = "extensions.extension(" + i + ").";

            config.setProperty(elementBaseKey, null);
            config.setProperty(elementBaseKey + "enabled", "X");
        }
        return config;
    }
}
