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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import javax.script.ScriptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.zaproxy.zap.extension.script.ScriptsCache.CachedScript;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.extension.script.ScriptsCache.InterfaceErrorMessageProvider;
import org.zaproxy.zap.extension.script.ScriptsCache.InterfaceProvider;
import org.zaproxy.zap.utils.Pair;

/** Unit test for {@link ScriptsCache}. */
class ScriptsCacheUnitTest {

    private ExtensionScript extensionScript;
    private String scriptType;
    private Class<Script> targetInterface;
    private String interfaceErrorMessage;

    private ScriptsCache<Script> scriptsCache;

    @BeforeEach
    void setUp() {
        extensionScript = mock(ExtensionScript.class);
        scriptType = "ScriptType";
        targetInterface = Script.class;
        interfaceErrorMessage = "Script does not implement the interface.";

        scriptsCache =
                new ScriptsCache<>(
                        extensionScript,
                        Configuration.<Script>builder()
                                .setScriptType(scriptType)
                                .setTargetInterface(targetInterface)
                                .setInterfaceErrorMessageProvider(sw -> interfaceErrorMessage)
                                .build());
    }

    @Test
    void shouldHaveNoCachedScriptsIfNotRefreshed() {
        // Given / When
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        // Then
        assertThat(cachedScripts, is(empty()));
        verifyNoInteractions(extensionScript);
    }

    @Test
    void shouldNotCacheDisabledScripts() throws Exception {
        // Given
        ScriptWrapper scriptWrapper1 = mockScriptWrapper(mock(Script.class));
        given(scriptWrapper1.isEnabled()).willReturn(false);
        ScriptWrapper scriptWrapper2 = mockScriptWrapper(mock(Script.class));
        given(scriptWrapper2.isEnabled()).willReturn(false);
        given(extensionScript.getScripts(scriptType))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        // When
        scriptsCache.refresh();
        // Then
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        assertThat(cachedScripts, hasSize(0));
        verify(extensionScript, times(1)).getScripts(scriptType);
        verifyNoMoreInteractions(extensionScript);
    }

    @Test
    void shouldCacheEnabledScripts() throws Exception {
        // Given
        Script script1 = mock(Script.class);
        ScriptWrapper scriptWrapper1 = mockScriptWrapper(script1);
        Script script2 = mock(Script.class);
        ScriptWrapper scriptWrapper2 = mockScriptWrapper(script2);
        given(extensionScript.getScripts(scriptType))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        // When
        scriptsCache.refresh();
        // Then
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        assertThat(cachedScripts, hasSize(2));
        assertCachedScript(cachedScripts.get(0), scriptWrapper1, script1);
        assertCachedScript(cachedScripts.get(1), scriptWrapper2, script2);
        verify(extensionScript, times(1)).getScripts(scriptType);
        verify(extensionScript, times(1)).getInterface(scriptWrapper1, targetInterface);
        verify(extensionScript, times(1)).getInterface(scriptWrapper2, targetInterface);
        verifyNoMoreInteractions(extensionScript);
    }

    @Test
    void shouldCacheEnabledScriptsAndIgnoreDisabledScriptsWhenRefreshing() throws Exception {
        // Given
        Script script1 = mock(Script.class);
        ScriptWrapper scriptWrapper1 = mockScriptWrapper(script1);
        given(scriptWrapper1.isEnabled()).willReturn(false);
        Script script2 = mock(Script.class);
        ScriptWrapper scriptWrapper2 = mockScriptWrapper(script2);
        given(extensionScript.getScripts(scriptType))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        // When
        scriptsCache.refresh();
        // Then
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        assertThat(cachedScripts, hasSize(1));
        assertCachedScript(cachedScripts.get(0), scriptWrapper2, script2);
        verify(extensionScript, times(1)).getScripts(scriptType);
        verify(extensionScript, times(1)).getInterface(scriptWrapper2, targetInterface);
        verifyNoMoreInteractions(extensionScript);
    }

    @Test
    void shouldNotCacheScriptsNoLongerEnabledWhenRefreshing() throws Exception {
        // Given
        Script script1 = mock(Script.class);
        ScriptWrapper scriptWrapper1 = mockScriptWrapper(script1);
        Script script2 = mock(Script.class);
        ScriptWrapper scriptWrapper2 = mockScriptWrapper(script2);
        given(extensionScript.getScripts(scriptType))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        // When
        scriptsCache.refresh();
        given(scriptWrapper1.isEnabled()).willReturn(false);
        scriptsCache.refresh();
        // Then
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        assertThat(cachedScripts, hasSize(1));
        assertCachedScript(cachedScripts.get(0), scriptWrapper2, script2);
        verify(extensionScript, times(2)).getScripts(scriptType);
        verify(extensionScript, times(1)).getInterface(scriptWrapper1, targetInterface);
        verify(extensionScript, times(1)).getInterface(scriptWrapper2, targetInterface);
        verifyNoMoreInteractions(extensionScript);
    }

    @Test
    void shouldRefreshCachedScriptIfChanged() throws Exception {
        // Given
        Script script1 = mock(Script.class);
        ScriptWrapper scriptWrapper1 = mockScriptWrapper(script1);
        Script script2 = mock(Script.class);
        ScriptWrapper scriptWrapper2 = mockScriptWrapper(script2);
        given(extensionScript.getScripts(scriptType))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        // When
        scriptsCache.refresh();
        given(scriptWrapper2.getModCount()).willReturn(1);
        Script refreshedScript = mock(Script.class);
        given(extensionScript.getInterface(scriptWrapper2, targetInterface))
                .willReturn(refreshedScript);
        scriptsCache.refresh();
        // Then
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        assertThat(cachedScripts, hasSize(2));
        assertCachedScript(cachedScripts.get(0), scriptWrapper1, script1);
        assertCachedScript(cachedScripts.get(1), scriptWrapper2, refreshedScript);
        verify(extensionScript, times(2)).getScripts(scriptType);
        verify(extensionScript, times(1)).getInterface(scriptWrapper1, targetInterface);
        verify(extensionScript, times(2)).getInterface(scriptWrapper2, targetInterface);
        verifyNoMoreInteractions(extensionScript);
    }

    @Test
    void shouldNotCacheScriptsThatDoNotImplementInterface() throws Exception {
        // Given
        Script script1 = mock(Script.class);
        ScriptWrapper scriptWrapper1 = mockScriptWrapper(script1);
        Script script2 = mock(Script.class);
        ScriptWrapper scriptWrapper2 = mockScriptWrapper(script2);
        given(extensionScript.getInterface(scriptWrapper2, targetInterface)).willReturn(null);
        given(extensionScript.getScripts(scriptType))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        // When
        scriptsCache.refresh();
        // Then
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        assertThat(cachedScripts, hasSize(1));
        assertCachedScript(cachedScripts.get(0), scriptWrapper1, script1);
        verify(extensionScript, times(1)).getScripts(scriptType);
        verify(extensionScript, times(1)).getInterface(scriptWrapper1, targetInterface);
        verify(extensionScript, times(1)).getInterface(scriptWrapper2, targetInterface);
        verify(extensionScript, times(1))
                .handleFailedScriptInterface(scriptWrapper2, interfaceErrorMessage);
        verifyNoMoreInteractions(extensionScript);
    }

    @Test
    void shouldNotCacheScriptsThatHaveErrors() throws Exception {
        // Given
        Script script1 = mock(Script.class);
        ScriptWrapper scriptWrapper1 = mockScriptWrapper(script1);
        Script script2 = mock(Script.class);
        ScriptWrapper scriptWrapper2 = mockScriptWrapper(script2);
        ScriptException scriptException = mock(ScriptException.class);
        given(extensionScript.getInterface(scriptWrapper2, targetInterface))
                .willThrow(scriptException);
        given(extensionScript.getScripts(scriptType))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        // When
        scriptsCache.refresh();
        // Then
        List<CachedScript<Script>> cachedScripts = scriptsCache.getCachedScripts();
        assertThat(cachedScripts, hasSize(1));
        assertCachedScript(cachedScripts.get(0), scriptWrapper1, script1);
        verify(extensionScript, times(1)).getScripts(scriptType);
        verify(extensionScript, times(1)).getInterface(scriptWrapper1, targetInterface);
        verify(extensionScript, times(1)).getInterface(scriptWrapper2, targetInterface);
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper2, scriptException);
        verifyNoMoreInteractions(extensionScript);
    }

    @Test
    void shouldExecuteCachedScripts() throws Exception {
        // Given
        Script script = mock(Script.class);
        ScriptWrapper scriptWrapper = mockScriptWrapper(script);
        given(extensionScript.getScripts(scriptType)).willReturn(asList(scriptWrapper));
        scriptsCache.refresh();
        List<Script> scriptsExecuted = new ArrayList<>();
        // When
        scriptsCache.execute(e -> scriptsExecuted.add(e));
        // Then
        assertThat(scriptsExecuted, hasSize(1));
        assertThat(scriptsExecuted.get(0), is(sameInstance(script)));
    }

    @Test
    void shouldHandleScriptExceptionWhenExecutingCachedScripts() throws Exception {
        // Given
        Script script = mock(Script.class);
        ScriptWrapper scriptWrapper = mockScriptWrapper(script);
        given(extensionScript.getScripts(scriptType)).willReturn(asList(scriptWrapper));
        scriptsCache.refresh();
        ScriptException exception = new ScriptException("");
        List<Script> scriptsExecuted = new ArrayList<>();
        // When
        scriptsCache.execute(
                e -> {
                    scriptsExecuted.add(e);
                    throw exception;
                });
        // Then
        assertThat(scriptsExecuted, hasSize(1));
        assertThat(scriptsExecuted.get(0), is(sameInstance(script)));
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper, exception);
    }

    @Test
    void shouldRefreshAndExecuteCachedScripts() throws Exception {
        // Given
        Script script = mock(Script.class);
        ScriptWrapper scriptWrapper = mockScriptWrapper(script);
        given(extensionScript.getScripts(scriptType)).willReturn(asList(scriptWrapper));
        List<Script> scriptsExecuted = new ArrayList<>();
        // When
        scriptsCache.refreshAndExecute(e -> scriptsExecuted.add(e));
        // Then
        assertThat(scriptsExecuted, hasSize(1));
        assertThat(scriptsExecuted.get(0), is(sameInstance(script)));
    }

    @Test
    void shouldHandleScriptExceptionWhenRefreshAndExecutingCachedScripts() throws Exception {
        // Given
        Script script = mock(Script.class);
        ScriptWrapper scriptWrapper = mockScriptWrapper(script);
        given(extensionScript.getScripts(scriptType)).willReturn(asList(scriptWrapper));
        scriptsCache.refresh();
        ScriptException exception = new ScriptException("");
        List<Script> scriptsExecuted = new ArrayList<>();
        // When
        scriptsCache.refreshAndExecute(
                e -> {
                    scriptsExecuted.add(e);
                    throw exception;
                });
        // Then
        assertThat(scriptsExecuted, hasSize(1));
        assertThat(scriptsExecuted.get(0), is(sameInstance(script)));
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper, exception);
    }

    @Test
    void shouldRefreshAndExecuteWithScriptWrapperAndCachedScripts() throws Exception {
        // Given
        Script script = mock(Script.class);
        ScriptWrapper scriptWrapper = mockScriptWrapper(script);
        given(extensionScript.getScripts(scriptType)).willReturn(asList(scriptWrapper));
        List<Pair<ScriptWrapper, Script>> scriptsExecuted = new ArrayList<>();
        // When / When
        scriptsCache.refreshAndExecute((sw, s) -> scriptsExecuted.add(new Pair<>(sw, s)));
        // Then
        assertThat(scriptsExecuted, hasSize(1));
        assertThat(scriptsExecuted.get(0).first, is(sameInstance(scriptWrapper)));
        assertThat(scriptsExecuted.get(0).second, is(sameInstance(script)));
    }

    @Test
    void shouldHandleScriptExceptionWhenRefreshAndExecutingWithScriptWrapperAndCachedScripts()
            throws Exception {
        // Given
        Script script = mock(Script.class);
        ScriptWrapper scriptWrapper = mockScriptWrapper(script);
        given(extensionScript.getScripts(scriptType)).willReturn(asList(scriptWrapper));
        scriptsCache.refresh();
        ScriptException exception = new ScriptException("");
        List<Pair<ScriptWrapper, Script>> scriptsExecuted = new ArrayList<>();
        // When
        scriptsCache.refreshAndExecute(
                (sw, s) -> {
                    scriptsExecuted.add(new Pair<>(sw, s));
                    throw exception;
                });
        // Then
        assertThat(scriptsExecuted, hasSize(1));
        assertThat(scriptsExecuted.get(0).first, is(sameInstance(scriptWrapper)));
        assertThat(scriptsExecuted.get(0).second, is(sameInstance(script)));
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper, exception);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseInterfaceProvider() throws Exception {
        // Given
        InterfaceProvider<Script> interfaceProvider = mock(InterfaceProvider.class);
        scriptsCache =
                new ScriptsCache<>(
                        extensionScript,
                        Configuration.<Script>builder()
                                .setScriptType(scriptType)
                                .setTargetInterface(targetInterface)
                                .setInterfaceProvider(interfaceProvider)
                                .build());
        ScriptWrapper scriptWrapper = mockScriptWrapper(mock(Script.class));
        given(extensionScript.getScripts(scriptType)).willReturn(asList(scriptWrapper));
        // When
        scriptsCache.refresh();
        // Then
        verify(interfaceProvider, times(1)).getInterface(scriptWrapper, Script.class);
    }

    @Nested
    static class ConfigurationUnitTest {

        private static final String SCRIPT_TYPE = "ScriptType";

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowExceptionWhenBuildingWithoutScriptType(String scriptType) {
            // Given
            Configuration.Builder<Script> builder =
                    Configuration.<Script>builder().setScriptType(scriptType);
            // When
            IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
            // Then
            assertThat(e.getMessage(), containsString("script type"));
        }

        @Test
        void shouldThrowExceptionWhenBuildingWithoutTargetInterface() {
            // Given
            Configuration.Builder<Script> builder =
                    Configuration.<Script>builder().setScriptType(SCRIPT_TYPE);
            builder.setTargetInterface(null);
            // When
            IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
            // Then
            assertThat(e.getMessage(), containsString("target interface"));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldThrowExceptionWhenBuildingWithTargetInterfaceProviderAndErrorMessageProvider() {
            // Given
            Configuration.Builder<Script> builder =
                    Configuration.<Script>builder()
                            .setScriptType(SCRIPT_TYPE)
                            .setTargetInterface(Script.class);
            builder.setInterfaceProvider(mock(InterfaceProvider.class));
            builder.setInterfaceErrorMessageProvider(mock(InterfaceErrorMessageProvider.class));
            // When
            IllegalStateException e = assertThrows(IllegalStateException.class, builder::build);
            // Then
            assertThat(
                    e.getMessage(),
                    allOf(
                            containsString("interface error message provider"),
                            containsString("interface provider")));
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldBuildWithInterfaceProvider() {
            // Given
            InterfaceProvider<Script> interfaceProvider = mock(InterfaceProvider.class);
            Configuration.Builder<Script> builder =
                    Configuration.<Script>builder()
                            .setScriptType(SCRIPT_TYPE)
                            .setTargetInterface(Script.class)
                            .setInterfaceProvider(interfaceProvider);
            // When
            Configuration<Script> configuration = builder.build();
            // Then
            assertThat(configuration.getScriptType(), is(equalTo(SCRIPT_TYPE)));
            assertThat(configuration.getTargetInterface(), is(equalTo(Script.class)));
            assertThat(configuration.getInterfaceProvider(), is(equalTo(interfaceProvider)));
            assertThat(configuration.getInterfaceErrorMessageProvider(), is(nullValue()));
        }

        @Test
        void shouldBuildWithInterfaceErrorMessageProvider() {
            // Given
            InterfaceErrorMessageProvider interfaceErrorMessageProvider =
                    mock(InterfaceErrorMessageProvider.class);
            Configuration.Builder<Script> builder =
                    Configuration.<Script>builder()
                            .setScriptType(SCRIPT_TYPE)
                            .setTargetInterface(Script.class)
                            .setInterfaceErrorMessageProvider(interfaceErrorMessageProvider);
            // When
            Configuration<Script> configuration = builder.build();
            // Then
            assertThat(configuration.getScriptType(), is(equalTo(SCRIPT_TYPE)));
            assertThat(configuration.getTargetInterface(), is(equalTo(Script.class)));
            assertThat(
                    configuration.getInterfaceErrorMessageProvider(),
                    is(equalTo(interfaceErrorMessageProvider)));
            assertThat(configuration.getInterfaceProvider(), is(nullValue()));
        }
    }

    private ScriptWrapper mockScriptWrapper(Script script) throws Exception {
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.isEnabled()).willReturn(true);
        given(scriptWrapper.getModCount()).willReturn(0);
        given(extensionScript.getInterface(scriptWrapper, targetInterface)).willReturn(script);
        return scriptWrapper;
    }

    private static void assertCachedScript(
            CachedScript<Script> cachedScript, ScriptWrapper scriptWrapper, Script script) {
        assertThat(cachedScript.getScriptWrapper(), is(sameInstance(scriptWrapper)));
        assertThat(cachedScript.getScript(), is(sameInstance(script)));
    }

    private interface Script {}
}
