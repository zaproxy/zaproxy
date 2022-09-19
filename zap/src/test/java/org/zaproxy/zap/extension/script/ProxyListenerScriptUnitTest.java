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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import javax.script.ScriptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.script.ScriptsCache.CachedScript;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.extension.script.ScriptsCache.InterfaceErrorMessageProvider;

/** Unit test for {@link ProxyListenerScript}. */
class ProxyListenerScriptUnitTest extends WithConfigsTest {

    private static final String SCRIPT_TYPE = ExtensionScript.TYPE_PROXY;
    private static final Class<ProxyScript> TARGET_INTERFACE = ProxyScript.class;

    private ExtensionScript extensionScript;
    private HttpMessage message;

    @BeforeEach
    void setUp() {
        extensionScript = mock(ExtensionScript.class);
    }

    @Test
    void shouldThrowExceptionIfExtensionScriptIsNull() {
        // Given
        ExtensionScript extensionScript = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new ProxyListenerScript(extensionScript));
    }

    @Test
    void shouldHaveListenerOrderLowerThanProxyListenerOrder() {
        // Given
        ProxyListenerScript proxyListenerScript = new ProxyListenerScript(extensionScript);
        // When
        int listenerOrder = proxyListenerScript.getArrangeableListenerOrder();
        // Then
        assertThat(listenerOrder, is(lessThan(ProxyListenerLog.PROXY_LISTENER_ORDER)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateScriptsCacheWithExpectedConfiguration() {
        // Given / When
        new ProxyListenerScript(extensionScript);
        // Then
        ArgumentCaptor<Configuration<ProxyScript>> argumentCaptor =
                ArgumentCaptor.forClass(Configuration.class);
        verify(extensionScript).createScriptsCache(argumentCaptor.capture());
        Configuration<ProxyScript> configuration = argumentCaptor.getValue();
        assertThat(configuration.getScriptType(), is(equalTo(SCRIPT_TYPE)));
        assertThat(configuration.getTargetInterface(), is(equalTo(TARGET_INTERFACE)));
        InterfaceErrorMessageProvider errorMessageProvider =
                configuration.getInterfaceErrorMessageProvider();
        assertThat(errorMessageProvider, is(not(nullValue())));
        assertThat(errorMessageProvider.getErrorMessage(null), is(not(nullValue())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldRefreshScriptsAndCallOnHttpRequestSend() throws Exception {
        // Given
        ProxyScript script = mockProxyScript();
        CachedScript<ProxyScript> cachedScript = createCachedScript(script);
        ScriptsCache<ProxyScript> scriptsCache = createScriptsCache(cachedScript);
        given(extensionScript.<ProxyScript>createScriptsCache(any())).willReturn(scriptsCache);
        ProxyListenerScript proxyListenerScript = new ProxyListenerScript(extensionScript);
        // When
        boolean forwardMessage = proxyListenerScript.onHttpRequestSend(message);
        // Then
        assertThat(forwardMessage, is(equalTo(true)));
        verify(scriptsCache, times(1)).refresh();
        verify(scriptsCache, times(1)).getCachedScripts();
        verify(script, times(1)).proxyRequest(message);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleExceptionsThrownByScriptsOnHttpRequestSend() throws Exception {
        // Given
        ProxyScript script = mockProxyScript();
        Exception exception = mock(ScriptException.class);
        given(script.proxyRequest(any())).willThrow(exception);
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        CachedScript<ProxyScript> cachedScript = createCachedScript(script, scriptWrapper);
        ScriptsCache<ProxyScript> scriptsCache = createScriptsCache(cachedScript);
        given(extensionScript.<ProxyScript>createScriptsCache(any())).willReturn(scriptsCache);
        ProxyListenerScript proxyListenerScript = new ProxyListenerScript(extensionScript);
        // When
        boolean forwardMessage = proxyListenerScript.onHttpRequestSend(message);
        // Then
        assertThat(forwardMessage, is(equalTo(true)));
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper, exception);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDropMessageOnHttpRequestSend() throws Exception {
        // Given
        ProxyScript script1 = mockProxyScript();
        given(script1.proxyRequest(any())).willReturn(false);
        CachedScript<ProxyScript> cachedScript1 = createCachedScript(script1);
        ProxyScript script2 = mockProxyScript();
        CachedScript<ProxyScript> cachedScript2 = createCachedScript(script2);
        ScriptsCache<ProxyScript> scriptsCache = createScriptsCache(cachedScript1, cachedScript2);
        given(extensionScript.<ProxyScript>createScriptsCache(any())).willReturn(scriptsCache);
        ProxyListenerScript proxyListenerScript = new ProxyListenerScript(extensionScript);
        // When
        boolean forwardMessage = proxyListenerScript.onHttpRequestSend(message);
        // Then
        assertThat(forwardMessage, is(equalTo(false)));
        verify(script1, times(1)).proxyRequest(message);
        verify(script2, times(0)).proxyRequest(message);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCallOnHttpResponseReceive() throws Exception {
        // Given
        ProxyScript script = mockProxyScript();
        CachedScript<ProxyScript> cachedScript = createCachedScript(script);
        ScriptsCache<ProxyScript> scriptsCache = createScriptsCache(cachedScript);
        given(extensionScript.<ProxyScript>createScriptsCache(any())).willReturn(scriptsCache);
        ProxyListenerScript proxyListenerScript = new ProxyListenerScript(extensionScript);
        // When
        boolean forwardMessage = proxyListenerScript.onHttpResponseReceive(message);
        // Then
        assertThat(forwardMessage, is(equalTo(true)));
        verify(scriptsCache, times(0)).refresh();
        verify(scriptsCache, times(1)).getCachedScripts();
        verify(script, times(1)).proxyResponse(message);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleExceptionsThrownByScriptsOnHttpResponseReceive() throws Exception {
        // Given
        ProxyScript script = mockProxyScript();
        Exception exception = mock(ScriptException.class);
        given(script.proxyResponse(any())).willThrow(exception);
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        CachedScript<ProxyScript> cachedScript = createCachedScript(script, scriptWrapper);
        ScriptsCache<ProxyScript> scriptsCache = createScriptsCache(cachedScript);
        given(extensionScript.<ProxyScript>createScriptsCache(any())).willReturn(scriptsCache);
        ProxyListenerScript proxyListenerScript = new ProxyListenerScript(extensionScript);
        // When
        boolean forwardMessage = proxyListenerScript.onHttpResponseReceive(message);
        // Then
        assertThat(forwardMessage, is(equalTo(true)));
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper, exception);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDropMessageOnHttpResponseReceive() throws Exception {
        // Given
        ProxyScript script1 = mockProxyScript();
        given(script1.proxyResponse(any())).willReturn(false);
        CachedScript<ProxyScript> cachedScript1 = createCachedScript(script1);
        ProxyScript script2 = mockProxyScript();
        CachedScript<ProxyScript> cachedScript2 = createCachedScript(script2);
        ScriptsCache<ProxyScript> scriptsCache = createScriptsCache(cachedScript1, cachedScript2);
        given(extensionScript.<ProxyScript>createScriptsCache(any())).willReturn(scriptsCache);
        ProxyListenerScript proxyListenerScript = new ProxyListenerScript(extensionScript);
        // When
        boolean forwardMessage = proxyListenerScript.onHttpResponseReceive(message);
        // Then
        assertThat(forwardMessage, is(equalTo(false)));
        verify(script1, times(1)).proxyResponse(message);
        verify(script2, times(0)).proxyResponse(message);
    }

    private static ProxyScript mockProxyScript() throws ScriptException {
        ProxyScript script = mock(TARGET_INTERFACE, withSettings().strictness(Strictness.LENIENT));
        given(script.proxyRequest(any())).willReturn(true);
        given(script.proxyResponse(any())).willReturn(true);
        return script;
    }

    private static <T> CachedScript<T> createCachedScript(T script) {
        return createCachedScript(script, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> CachedScript<T> createCachedScript(T script, ScriptWrapper scriptWrapper) {
        CachedScript<T> cachedScript =
                mock(CachedScript.class, withSettings().strictness(Strictness.LENIENT));
        given(cachedScript.getScript()).willReturn(script);
        if (scriptWrapper != null) {
            given(cachedScript.getScriptWrapper()).willReturn(scriptWrapper);
        }
        return cachedScript;
    }

    @SuppressWarnings("unchecked")
    private static <T> ScriptsCache<T> createScriptsCache(CachedScript<T>... cachedScripts) {
        ScriptsCache<T> scriptsCache = mock(ScriptsCache.class);
        given(scriptsCache.getCachedScripts()).willReturn(asList(cachedScripts));
        return scriptsCache;
    }
}
