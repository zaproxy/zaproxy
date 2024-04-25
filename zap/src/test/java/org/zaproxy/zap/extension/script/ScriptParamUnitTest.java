/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2023 The ZAP Development Team
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
package org.zaproxy.zap.extension.script;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link ScriptParam}. */
class ScriptParamUnitTest {

    private ScriptParam param;
    private ZapXmlConfiguration configuration;

    @BeforeAll
    static void beforeAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void afterAll() {
        Constant.messages = null;
    }

    @BeforeEach
    void setUp() {
        param = new ScriptParam();
        configuration = new ZapXmlConfiguration();
        param.load(configuration);
    }

    @Test
    void shouldMigrateOldOptions(@TempDir File dir1, @TempDir File dir2) throws IOException {
        // Given
        List<File> dirs = Arrays.asList(dir1, dir2);

        configuration.setProperty("confRemdir", true);
        configuration.setProperty("dirs", new String[] {dir1.toString(), dir2.toString()});
        // When
        param.load(configuration);
        // Then
        assertThat(param.isConfirmRemoveDir(), is(equalTo(true)));
        assertThat(param.getScriptDirs(), is(equalTo(dirs)));
        assertNull(configuration.getProperty("dirs"));
        assertNull(configuration.getProperty("confRemdir"));
    }

    @Test
    void shouldSaveScriptProperties() throws Exception {
        // Given
        var script = new ScriptWrapper();
        script.setEngine(mock(ScriptEngineWrapper.class));
        script.setName("script.js");
        ZapXmlConfiguration config = mock(ZapXmlConfiguration.class);
        var scriptConfigs =
                List.of(
                        mock(HierarchicalConfiguration.class),
                        mock(HierarchicalConfiguration.class));
        when(config.configurationsAt("script.scripts")).thenReturn(scriptConfigs);
        when(scriptConfigs.get(0).getString("name")).thenReturn(script.getName());
        when(scriptConfigs.get(1).getString("name")).thenReturn("differentScript.js");
        var param = new ScriptParam();
        param.load(config);
        // When
        script.setEnabled(true);
        param.saveScriptProperties(script);
        // Then
        verify(scriptConfigs.get(0)).setProperty("enabled", true);
        verify(scriptConfigs.get(1), times(0)).setProperty(anyString(), any());
    }

    @Test
    void shouldNotSaveScriptPropertiesIfScriptIsNotFound() {
        // Given
        var script = new ScriptWrapper();
        script.setEngine(mock(ScriptEngineWrapper.class));
        script.setName("differentScript.js");
        var config = new ZapXmlConfiguration();
        config.setProperty("script.scripts(0).name", "script.js");
        config.setProperty("script.scripts(0).enabled", false);
        var param = new ScriptParam();
        param.load(config);
        // When
        script.setEnabled(true);
        param.saveScriptProperties(script);
        // Then
        assertThat(config.configurationsAt("script.scripts").size(), is(1));
        assertThat(config.getBoolean("script.scripts(0).enabled"), is(false));
    }
}
