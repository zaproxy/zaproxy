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
package org.zaproxy.zap.extension.pscan.scanner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;
import net.htmlparser.jericho.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.extension.pscan.PassiveScanData;
import org.zaproxy.zap.extension.pscan.PassiveScanTaskHelper;
import org.zaproxy.zap.extension.pscan.PassiveScript;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.extension.script.ScriptsCache;
import org.zaproxy.zap.extension.script.ScriptsCache.CachedScript;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.extension.script.ScriptsCache.InterfaceErrorMessageProvider;
import org.zaproxy.zap.extension.script.ScriptsCache.ScriptWrapperAction;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link ScriptsPassiveScanner}. */
class ScriptsPassiveScannerUnitTest extends WithConfigsTest {

    private static final String SCRIPT_TYPE = ExtensionPassiveScan.SCRIPT_TYPE_PASSIVE;
    private static final Class<PassiveScript> TARGET_INTERFACE = PassiveScript.class;

    private ExtensionScript extensionScript;
    private HttpMessage message;
    private int id;
    private Source source;

    @BeforeEach
    void setUp() {
        extensionScript = mock(ExtensionScript.class);
        message = mock(HttpMessage.class);
        id = 1;
        source = new Source("");

        given(extensionLoader.getExtension(ExtensionScript.class)).willReturn(extensionScript);
    }

    @Test
    void shouldNotThrowIfExtensionScriptIsNull() {
        // Given
        given(extensionLoader.getExtension(ExtensionScript.class)).willReturn(null);
        // When / Then
        ScriptsPassiveScanner scriptsPassiveScanner =
                assertDoesNotThrow(ScriptsPassiveScanner::new);
        assertDoesNotThrow(() -> scriptsPassiveScanner.scanHttpRequestSend(message, id));
        assertDoesNotThrow(
                () -> scriptsPassiveScanner.scanHttpResponseReceive(message, id, source));
    }

    @Test
    void shouldHaveAName() {
        // Given
        Constant.messages = new I18N(Locale.ENGLISH);
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        // When
        String name = scriptsPassiveScanner.getName();
        // Then
        assertThat(name, is(equalTo("Script Passive Scan Rules")));
    }

    @Test
    void shouldHaveSpecificPluginId() {
        // Given
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        // When
        int pluginId = scriptsPassiveScanner.getPluginId();
        // Then
        assertThat(pluginId, is(equalTo(50001)));
    }

    @Test
    void shouldAddTagsWithTaskHelper() {
        // Given
        String tag = "Tag";
        PassiveScanTaskHelper taskHelper = mock(PassiveScanTaskHelper.class);
        HistoryReference href = mock(HistoryReference.class);
        when(message.getHistoryRef()).thenReturn(href);
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        PassiveScanData passiveScanData = mock(PassiveScanData.class);
        when(passiveScanData.getMessage()).thenReturn(message);
        scriptsPassiveScanner.setHelper(passiveScanData);
        scriptsPassiveScanner.setTaskHelper(taskHelper);
        // When
        scriptsPassiveScanner.addHistoryTag(tag);
        // Then
        verify(taskHelper).addHistoryTag(href, tag);
    }

    @Test
    void shouldThrowExceptionWhenAddingTagWithoutParent() {
        // Given
        String tag = "Tag";
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        // When / Then
        assertThrows(NullPointerException.class, () -> scriptsPassiveScanner.addHistoryTag(tag));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 2, 3, 10, 100})
    void shouldApplyToAllHistoryTypes(int historyType) {
        // Given
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        // When
        boolean applies = scriptsPassiveScanner.appliesToHistoryType(historyType);
        // Then
        assertThat(applies, is(equalTo(true)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateScriptsCacheWithExpectedConfiguration() {
        // Given / When
        new ScriptsPassiveScanner();
        // Then
        ArgumentCaptor<Configuration<PassiveScript>> argumentCaptor =
                ArgumentCaptor.forClass(Configuration.class);
        verify(extensionScript).createScriptsCache(argumentCaptor.capture());
        Configuration<PassiveScript> configuration = argumentCaptor.getValue();
        assertThat(configuration.getScriptType(), is(equalTo(SCRIPT_TYPE)));
        assertThat(configuration.getTargetInterface(), is(equalTo(TARGET_INTERFACE)));
        InterfaceErrorMessageProvider errorMessageProvider =
                configuration.getInterfaceErrorMessageProvider();
        assertThat(errorMessageProvider, is(not(nullValue())));
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.getName()).willReturn("Name");
        assertThat(errorMessageProvider.getErrorMessage(scriptWrapper), is(not(nullValue())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRefreshScriptsAndCallScan() throws Exception {
        // Given
        PassiveScript script = mock(TARGET_INTERFACE);
        given(script.appliesToHistoryType(anyInt())).willReturn(true);
        ScriptsCache<PassiveScript> scriptsCache = createScriptsCache(createCachedScript(script));
        given(extensionScript.<PassiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        int historyType = 5;
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        scriptsPassiveScanner.appliesToHistoryType(historyType);
        // When
        scriptsPassiveScanner.scanHttpResponseReceive(message, id, source);
        // Then
        verify(scriptsCache, times(1)).refreshAndExecute(any(ScriptWrapperAction.class));
        verify(script, times(1)).appliesToHistoryType(historyType);
        ArgumentCaptor<ScriptsPassiveScanner> argumentCaptor =
                ArgumentCaptor.forClass(ScriptsPassiveScanner.class);
        verify(script, times(1)).scan(argumentCaptor.capture(), eq(message), eq(source));
        assertThat(argumentCaptor.getValue(), is(sameInstance(scriptsPassiveScanner)));
    }

    @Test
    void shouldScanWithCopy() throws Exception {
        // Given
        PassiveScript script = mock(TARGET_INTERFACE);
        given(script.appliesToHistoryType(anyInt())).willReturn(true);
        ScriptsCache<PassiveScript> scriptsCache = createScriptsCache(createCachedScript(script));
        given(extensionScript.<PassiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        int historyType = 5;
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        scriptsPassiveScanner.appliesToHistoryType(historyType);
        // When
        ScriptsPassiveScanner copy = scriptsPassiveScanner.copy();
        copy.scanHttpResponseReceive(message, id, source);
        // Then
        verify(script, times(1)).appliesToHistoryType(historyType);
        verify(script, times(1)).scan(any(), any(), any());
    }

    @Test
    void shouldNotCallScanIfScriptDoesNotApplyToHistoryType() throws Exception {
        // Given
        PassiveScript script = mock(TARGET_INTERFACE);
        given(script.appliesToHistoryType(anyInt())).willReturn(false);
        ScriptsCache<PassiveScript> scriptsCache = createScriptsCache(createCachedScript(script));
        given(extensionScript.<PassiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        int historyType = 5;
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        scriptsPassiveScanner.appliesToHistoryType(historyType);
        // When
        scriptsPassiveScanner.scanHttpResponseReceive(message, id, source);
        // Then
        verify(script, times(1)).appliesToHistoryType(historyType);
        verify(script, times(0)).scan(any(), any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10, 15})
    void shouldHandleScriptsThatDoNotImplementAppliesToHistoryType(int historyType)
            throws Exception {
        // Given
        PassiveScript script = mock(TARGET_INTERFACE);
        NoSuchMethodException cause = mock(NoSuchMethodException.class);
        given(cause.getMessage()).willReturn("appliesToHistoryType");
        UndeclaredThrowableException exception = mock(UndeclaredThrowableException.class);
        given(exception.getCause()).willReturn(cause);
        given(script.appliesToHistoryType(anyInt())).willThrow(exception);
        ScriptsCache<PassiveScript> scriptsCache = createScriptsCache(createCachedScript(script));
        given(extensionScript.<PassiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        scriptsPassiveScanner.appliesToHistoryType(historyType);
        // When
        scriptsPassiveScanner.scanHttpResponseReceive(message, id, source);
        // Then
        verify(script, times(1)).appliesToHistoryType(historyType);
        verify(script, times(1)).scan(any(), any(), any());
    }

    @Test
    void shouldPropagateExceptionThrownByAppliesToHistoryTypeIfNotCausedByMissingMethod()
            throws Exception {
        // Given
        PassiveScript script = mock(TARGET_INTERFACE);
        NoSuchMethodException cause = mock(NoSuchMethodException.class);
        given(cause.getMessage()).willReturn("other method");
        UndeclaredThrowableException exception = mock(UndeclaredThrowableException.class);
        given(exception.getCause()).willReturn(cause);
        given(script.appliesToHistoryType(anyInt())).willThrow(exception);
        ScriptsCache<PassiveScript> scriptsCache = createScriptsCache(createCachedScript(script));
        given(extensionScript.<PassiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        int historyType = 5;
        scriptsPassiveScanner.appliesToHistoryType(historyType);
        // When
        scriptsPassiveScanner.scanHttpResponseReceive(message, id, source);
        // Then
        verify(script, times(1)).appliesToHistoryType(historyType);
        verify(script, times(0)).scan(any(), any(), any());
    }

    @Test
    void shouldPropagateExceptionThrownByAppliesToHistoryTypeIfNotNoSuchMethodException()
            throws Exception {
        // Given
        PassiveScript script = mock(TARGET_INTERFACE);
        UndeclaredThrowableException exception = mock(UndeclaredThrowableException.class);
        given(script.appliesToHistoryType(anyInt())).willThrow(exception);
        ScriptsCache<PassiveScript> scriptsCache = createScriptsCache(createCachedScript(script));
        given(extensionScript.<PassiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        ScriptsPassiveScanner scriptsPassiveScanner = new ScriptsPassiveScanner();
        int historyType = 5;
        scriptsPassiveScanner.appliesToHistoryType(historyType);
        // When
        scriptsPassiveScanner.scanHttpResponseReceive(message, id, source);
        // Then
        verify(script, times(1)).appliesToHistoryType(historyType);
        verify(script, times(0)).scan(any(), any(), any());
    }

    @SuppressWarnings("unchecked")
    private static <T> CachedScript<T> createCachedScript(T script) {
        CachedScript<T> cachedScript = mock(CachedScript.class);
        given(cachedScript.getScript()).willReturn(script);
        return cachedScript;
    }

    @SuppressWarnings("unchecked")
    private <T> ScriptsCache<T> createScriptsCache(CachedScript<T> cachedScript) {
        ScriptsCache<T> scriptsCache = mock(ScriptsCache.class);
        Answer<Void> answer =
                invocation -> {
                    ScriptWrapperAction<T> action =
                            (ScriptWrapperAction<T>) invocation.getArguments()[0];
                    try {
                        action.apply(cachedScript.getScriptWrapper(), cachedScript.getScript());
                    } catch (Throwable ignore) {
                    }
                    return null;
                };
        doAnswer(answer).when(scriptsCache).refreshAndExecute(any(ScriptWrapperAction.class));
        return scriptsCache;
    }
}
