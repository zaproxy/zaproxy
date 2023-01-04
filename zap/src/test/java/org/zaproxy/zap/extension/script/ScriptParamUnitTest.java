/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

/** Unit test for {@link ScriptParam}. */
public class ScriptParamUnitTest {

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
    void shouldMigrateOldOptions() throws IOException {
        // Given
        File dir1 = Files.createTempDirectory("zapUnitTest").toFile();
        File dir2 = Files.createTempDirectory("zapUnitTest").toFile();
        List<File> dirs = new ArrayList<>();
        dirs.add(dir1);
        dirs.add(dir2);

        configuration.setProperty("confRemdir", true);
        configuration.setProperty("dirs", new String[] {dir1.toString(), dir2.toString()});
        // When
        param.load(configuration);
        // Then
        assertThat(param.isConfirmRemoveDir(), is(equalTo(true)));
        assertThat(param.getScriptDirs(), is(equalTo(dirs)));
        assertNull(configuration.getProperty("dirs"));
        assertNull(configuration.getProperty("confRemdir"));

        // Cleanup
        dir1.delete();
        dir2.delete();
    }
}
