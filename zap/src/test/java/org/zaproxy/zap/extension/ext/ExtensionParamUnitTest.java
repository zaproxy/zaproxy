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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.configuration.FileConfiguration;
import org.junit.jupiter.api.Test;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link ExtensionParam}. */
class ExtensionParamUnitTest {

    @Test
    void shouldNotHaveConfigByDefault() {
        // Given / When
        ExtensionParam param = new ExtensionParam();
        // Then
        assertThat(param.getConfig(), is(equalTo(null)));
    }

    @Test
    void shouldBeCloneableByDefault() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When
        ExtensionParam clone = param.clone();
        // Then
        assertThat(clone, is(not(equalTo(null))));
    }

    @Test
    void shouldParseLoadedFileConfiguration() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig(false, true, false, true, true);
        // When
        param.load(config);
        // Then
        assertThat(param.isExtensionEnabled("Extension 1"), is(equalTo(false)));
        assertThat(param.isExtensionEnabled("Extension 2"), is(equalTo(true)));
        assertThat(param.isExtensionEnabled("Extension 3"), is(equalTo(false)));
        assertThat(param.isExtensionEnabled("Extension 4"), is(equalTo(true)));
        assertThat(param.isExtensionEnabled("Extension 5"), is(equalTo(true)));
    }

    @Test
    void shouldDefaultToAllExtensionsEnabledIfLoadingMalformedFileConfiguration() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When
        param.load(createMalformedTestConfig());
        // Then
        assertThat(param.isExtensionEnabled("Extension 1"), is(equalTo(true)));
        assertThat(param.isExtensionEnabled("Extension 2"), is(equalTo(true)));
        assertThat(param.isExtensionEnabled("Extension 3"), is(equalTo(true)));
    }

    @Test
    void shouldHaveLoadedConfigsAfterCloning() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig(false, true, false);
        param.load(config);
        // When
        ExtensionParam clone = param.clone();
        // Then
        assertThat(clone.isExtensionEnabled("Extension 1"), is(equalTo(false)));
        assertThat(clone.isExtensionEnabled("Extension 2"), is(equalTo(true)));
        assertThat(clone.isExtensionEnabled("Extension 3"), is(equalTo(false)));
    }

    @Test
    void shouldFailToPersistNullExtensionsState() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When / Then
        assertThrows(IllegalArgumentException.class, () -> param.setExtensionsState(null));
    }

    @Test
    void shouldFailToPersistExtensionsStateWithoutConfigurationFile() {
        // Given
        ExtensionParam param = new ExtensionParam();
        // When / Then
        assertThrows(
                NullPointerException.class,
                () -> param.setExtensionsState(Collections.<String, Boolean>emptyMap()));
    }

    @Test
    void shouldSetExtensionsState() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig();
        param.load(config);
        Map<String, Boolean> states = extensionsState(true, false, true, false, true, true);
        // When
        param.setExtensionsState(states);
        // Then
        assertThat(param.isExtensionEnabled("Extension 1"), is(equalTo(true)));
        assertThat(param.isExtensionEnabled("Extension 2"), is(equalTo(false)));
        assertThat(param.isExtensionEnabled("Extension 3"), is(equalTo(true)));
        assertThat(param.isExtensionEnabled("Extension 4"), is(equalTo(false)));
        assertThat(param.isExtensionEnabled("Extension 5"), is(equalTo(true)));
        assertThat(param.isExtensionEnabled("Extension 6"), is(equalTo(true)));
    }

    @Test
    void shouldPersistDisabledExtensions() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig();
        param.load(config);
        Map<String, Boolean> states = extensionsState(true, false, true, false, true, true);
        // When
        param.setExtensionsState(states);
        // Then
        assertThat(config.getString("extensions.extension(0).name"), is(equalTo("Extension 2")));
        assertThat(config.getBoolean("extensions.extension(0).enabled"), is(equalTo(false)));
        assertThat(config.getString("extensions.extension(1).name"), is(equalTo("Extension 4")));
        assertThat(config.getBoolean("extensions.extension(1).enabled"), is(equalTo(false)));
        assertThat(config.containsKey("extensions.extension(2).name"), is(equalTo(false)));
        assertThat(config.containsKey("extensions.extension(2).enabled"), is(equalTo(false)));
    }

    @Test
    void shouldNotPersistNullExtensionNamesOrNullEnabledState() {
        // Given
        ExtensionParam param = new ExtensionParam();
        FileConfiguration config = createTestConfig();
        param.load(config);
        Map<String, Boolean> states = new HashMap<>();
        states.put(null, Boolean.FALSE);
        states.put("Extension 2", null);
        // When
        param.setExtensionsState(states);
        // Then
        assertThat(param.getConfig().getKeys().hasNext(), is(equalTo(false)));
    }

    private static Map<String, Boolean> extensionsState(boolean... states) {
        Map<String, Boolean> extensionsState = new HashMap<>();
        if (states == null || states.length == 0) {
            return extensionsState;
        }

        for (int i = 0; i < states.length; ++i) {
            extensionsState.put("Extension " + (i + 1), states[i]);
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
