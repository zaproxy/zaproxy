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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;

/** Unit test for {@link ExtensionScript}. */
class ExtensionScriptUnitTest {

    @BeforeEach
    void setUp() {
        WithConfigsTest.setUpConstantMessages();
    }

    @AfterEach
    void cleanUp() {
        Constant.messages = null;
    }

    @Test
    void shouldCreateScriptsCache() {
        // Given
        ExtensionScript extensionScript = new ExtensionScript();
        @SuppressWarnings("unchecked")
        Configuration<Script> configuration = mock(Configuration.class);
        // When
        ScriptsCache<Script> scriptsCache = extensionScript.createScriptsCache(configuration);
        // Then
        assertThat(scriptsCache, is(not(nullValue())));
    }

    private interface Script {}
}
