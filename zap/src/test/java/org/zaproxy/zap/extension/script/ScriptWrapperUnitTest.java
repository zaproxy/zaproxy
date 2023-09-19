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
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Unit test for {@link ScriptWrapper}. */
class ScriptWrapperUnitTest {

    @Test
    void shouldSetContents() {
        // Given
        String contents = "Abc";
        ScriptWrapper scriptWrapper = new ScriptWrapper();
        // When
        scriptWrapper.setContents(contents);
        // Then
        assertThat(scriptWrapper.getContents(), is(equalTo(contents)));
    }

    @Test
    void shouldIncreaseModCountIfContentsChanged() {
        // Given
        String contents = "Abc";
        ScriptWrapper scriptWrapper = new ScriptWrapper();
        int oldModCount = scriptWrapper.getModCount();
        // When
        scriptWrapper.setContents(contents);
        // Then
        assertThat(scriptWrapper.getModCount(), is(not(equalTo(oldModCount))));
    }

    @Test
    void shouldNotIncreaseModCountIfContentsNotChanged() {
        // Given
        String contents = "Abc";
        ScriptWrapper scriptWrapper = new ScriptWrapper();
        scriptWrapper.setContents(contents);
        int oldModCount = scriptWrapper.getModCount();
        // When
        scriptWrapper.setContents(contents);
        // Then
        assertThat(scriptWrapper.getModCount(), is(equalTo(oldModCount)));
    }

    @Test
    void shouldNotChangeScriptWhenEnabled() {
        // Given
        var scriptWrapper = new ScriptWrapper();
        scriptWrapper.setEngine(Mockito.mock(ScriptEngineWrapper.class));
        scriptWrapper.setChanged(false);
        scriptWrapper.setEnabled(false);
        // When
        scriptWrapper.setEnabled(true);
        // Then
        assertThat(scriptWrapper.isEnabled(), is(true));
        assertThat(scriptWrapper.isChanged(), is(false));
    }

    @Test
    void shouldNotEnableScriptWithNullEngine() {
        // Given
        var scriptWrapper = new ScriptWrapper();
        scriptWrapper.setEngine(null);
        scriptWrapper.setChanged(false);
        scriptWrapper.setEnabled(false);
        // When
        scriptWrapper.setEnabled(true);
        // Then
        assertThat(scriptWrapper.isEnabled(), is(false));
    }
}
