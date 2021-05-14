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
package org.parosproxy.paros.core.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;

/** Unit test for {@link VariantCustom}. */
class VariantCustomUnitTest {

    @Test
    void shouldCallScriptForGetLeafName() throws Exception {
        // Given
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.isEnabled()).willReturn(true);
        String nodeName = "name";
        String expectedName = "newname";
        ExtensionScript extScript = mock(ExtensionScript.class);
        VariantScript variantScript = mock(VariantScript.class);
        given(extScript.getInterface(scriptWrapper, VariantScript.class)).willReturn(variantScript);
        VariantCustom variantCustom = new VariantCustom(scriptWrapper, extScript);
        HttpMessage msg = mock(HttpMessage.class);
        given(variantScript.getLeafName(variantCustom, nodeName, msg)).willReturn(expectedName);

        // When
        String name = variantCustom.getLeafName(nodeName, msg);

        // Then
        assertThat(name, is(equalTo(expectedName)));
    }

    @Test
    void shouldCallScriptForGetTreePath() throws Exception {
        // Given
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.isEnabled()).willReturn(true);
        String expectedPath = "newpath";
        List<String> list = new ArrayList<>();
        list.add(expectedPath);
        ExtensionScript extScript = mock(ExtensionScript.class);
        VariantScript variantScript = mock(VariantScript.class);
        given(extScript.getInterface(scriptWrapper, VariantScript.class)).willReturn(variantScript);
        VariantCustom variantCustom = new VariantCustom(scriptWrapper, extScript);
        HttpMessage msg = mock(HttpMessage.class);
        given(variantScript.getTreePath(variantCustom, msg)).willReturn(list);

        // When
        List<String> path = variantCustom.getTreePath(msg);

        // Then
        assertThat(path.size(), is(equalTo(1)));
        assertThat(path.get(0), is(equalTo(expectedPath)));
    }

    @Test
    void shouldReturnNullLeafNameWithDisabledScript() throws Exception {
        // Given
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.isEnabled()).willReturn(false);
        String nodeName = "name";
        String expectedName = "newname";
        ExtensionScript extScript = mock(ExtensionScript.class);
        VariantScript variantScript = mock(VariantScript.class);
        given(extScript.getInterface(scriptWrapper, VariantScript.class)).willReturn(variantScript);
        VariantCustom variantCustom = new VariantCustom(scriptWrapper, extScript);
        HttpMessage msg = mock(HttpMessage.class);
        given(variantScript.getLeafName(variantCustom, nodeName, msg)).willReturn(expectedName);

        // When
        String name = variantCustom.getLeafName(nodeName, msg);

        // Then
        assertThat(name, is(equalTo(null)));
    }

    @Test
    void shouldReturnNullTreePathWithDisabledScript() throws Exception {
        // Given
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.isEnabled()).willReturn(false);
        List<String> list = new ArrayList<>();
        ExtensionScript extScript = mock(ExtensionScript.class);
        VariantScript variantScript = mock(VariantScript.class);
        given(extScript.getInterface(scriptWrapper, VariantScript.class)).willReturn(variantScript);
        VariantCustom variantCustom = new VariantCustom(scriptWrapper, extScript);
        HttpMessage msg = mock(HttpMessage.class);
        given(variantScript.getTreePath(variantCustom, msg)).willReturn(list);

        // When
        List<String> path = variantCustom.getTreePath(msg);

        // Then
        assertThat(path, is(equalTo(null)));
    }

    @Test
    void shouldReturnNullLeafNameWithScriptException() throws Exception {
        // Given
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        String nodeName = "name";
        ExtensionScript extScript = mock(ExtensionScript.class);
        VariantScript variantScript = mock(VariantScript.class);
        given(extScript.getInterface(scriptWrapper, VariantScript.class)).willReturn(variantScript);
        VariantCustom variantCustom = new VariantCustom(scriptWrapper, extScript);
        HttpMessage msg = mock(HttpMessage.class);
        given(variantScript.getLeafName(variantCustom, nodeName, msg))
                .willThrow(RuntimeException.class);

        // When
        String name = variantCustom.getLeafName(nodeName, msg);

        // Then
        assertThat(name, is(equalTo(null)));
    }

    @Test
    void shouldReturnNullTreePathWithScriptException() throws Exception {
        // Given
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        String expectedPath = "newpath";
        List<String> list = new ArrayList<>();
        list.add(expectedPath);
        ExtensionScript extScript = mock(ExtensionScript.class);
        VariantScript variantScript = mock(VariantScript.class);
        given(extScript.getInterface(scriptWrapper, VariantScript.class)).willReturn(variantScript);
        VariantCustom variantCustom = new VariantCustom(scriptWrapper, extScript);
        HttpMessage msg = mock(HttpMessage.class);
        given(variantScript.getTreePath(variantCustom, msg)).willThrow(RuntimeException.class);

        // When
        List<String> path = variantCustom.getTreePath(msg);

        // Then
        assertThat(path, is(equalTo(null)));
    }
}
