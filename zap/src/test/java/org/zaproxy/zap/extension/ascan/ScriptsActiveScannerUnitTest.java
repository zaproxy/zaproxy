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
package org.zaproxy.zap.extension.ascan;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Locale;
import javax.script.ScriptException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.extension.script.ScriptsCache;
import org.zaproxy.zap.extension.script.ScriptsCache.CachedScript;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link ScriptsActiveScanner}. */
class ScriptsActiveScannerUnitTest extends WithConfigsTest {

    private static final String SCRIPT_TYPE = ExtensionActiveScan.SCRIPT_TYPE_ACTIVE;
    private static final Class<ActiveScript> TARGET_INTERFACE_CACHE = ActiveScript.class;

    private ExtensionScript extensionScript;
    private HostProcess parent;
    private HttpMessage message;

    @BeforeEach
    void setUp() throws Exception {
        extensionScript = mock(ExtensionScript.class);
        parent = mock(HostProcess.class);
        message = new HttpMessage(new HttpRequestHeader("GET / HTTP/1.1"));

        given(extensionLoader.getExtension(ExtensionScript.class)).willReturn(extensionScript);
    }

    @Test
    void shouldSkipIfExtensionScriptIsNull() {
        // Given
        Constant.messages = new I18N(Locale.ENGLISH);
        given(extensionLoader.getExtension(ExtensionScript.class)).willReturn(null);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        // When
        scriptsActiveScanner.init(message, parent);
        // Then
        verify(parent).pluginSkipped(scriptsActiveScanner, "no scripts enabled");
    }

    @Test
    void shouldSkipIfNoActiveScripts() {
        // Given
        Constant.messages = new I18N(Locale.ENGLISH);
        given(extensionScript.getScripts(SCRIPT_TYPE)).willReturn(asList());
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        // When
        scriptsActiveScanner.init(message, parent);
        // Then
        verify(parent).pluginSkipped(scriptsActiveScanner, "no scripts enabled");
    }

    @Test
    void shouldSkipIfNoEnabledActiveScripts() {
        // Given
        Constant.messages = new I18N(Locale.ENGLISH);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(mock(ScriptWrapper.class)));
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        // When
        scriptsActiveScanner.init(message, parent);
        // Then
        verify(parent).pluginSkipped(scriptsActiveScanner, "no scripts enabled");
    }

    @Test
    void shouldHaveAName() {
        // Given
        Constant.messages = new I18N(Locale.ENGLISH);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        // When
        String name = scriptsActiveScanner.getName();
        // Then
        assertThat(name, is(equalTo("Script Active Scan Rules")));
    }

    @Test
    void shouldHaveSpecificPluginId() {
        // Given
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        // When
        int pluginId = scriptsActiveScanner.getId();
        // Then
        assertThat(pluginId, is(equalTo(50000)));
    }

    @Test
    void shouldHaveSpecificCategory() {
        // Given
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        // When
        int category = scriptsActiveScanner.getCategory();
        // Then
        assertThat(category, is(equalTo(Category.MISC)));
    }

    @Test
    void shouldHaveNoDependencies() {
        // Given
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        // When
        String[] dependencies = scriptsActiveScanner.getDependency();
        // Then
        assertThat(dependencies, is(nullValue()));
    }

    @Test
    void shouldScanNodesWithActiveScript2() throws Exception {
        // Given
        ActiveScript2 script1 = mock(ActiveScript2.class);
        ScriptWrapper scriptWrapper1 = createScriptWrapper(script1, ActiveScript2.class);
        ActiveScript2 script2 = mock(ActiveScript2.class);
        ScriptWrapper scriptWrapper2 = createScriptWrapper(script2, ActiveScript2.class);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(mock(Variant.class)));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(script1, times(1)).scanNode(scriptsActiveScanner, message);
        verify(script2, times(1)).scanNode(scriptsActiveScanner, message);
    }

    @Test
    void shouldNotCallScanNodeOnDisabledActiveScript2() throws Exception {
        // Given
        ScriptWrapper scriptWrapper1 = mock(ScriptWrapper.class);
        given(scriptWrapper1.isEnabled()).willReturn(false);
        ActiveScript2 script2 = mock(ActiveScript2.class);
        ScriptWrapper scriptWrapper2 = createScriptWrapper(script2, ActiveScript2.class);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(mock(Variant.class)));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(extensionScript, times(0)).getInterface(scriptWrapper1, ActiveScript2.class);
        verify(script2, times(1)).scanNode(scriptsActiveScanner, message);
    }

    @Test
    void shouldHandleExceptionsThrownByActiveScript2() throws Exception {
        // Given
        ActiveScript2 script1 = mock(ActiveScript2.class);
        ScriptException exception = mock(ScriptException.class);
        doThrow(exception).when(script1).scanNode(any(), any());
        ScriptWrapper scriptWrapper1 = createScriptWrapper(script1, ActiveScript2.class);
        ActiveScript2 script2 = mock(ActiveScript2.class);
        ScriptWrapper scriptWrapper2 = createScriptWrapper(script2, ActiveScript2.class);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(mock(Variant.class)));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(script1, times(1)).scanNode(scriptsActiveScanner, message);
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper1, exception);
        verify(script2, times(1)).scanNode(scriptsActiveScanner, message);
    }

    @Test
    void shouldStopScanningNodesWithActiveScript2WhenScanStopped() throws Exception {
        // Given
        ActiveScript2 script1 = mock(ActiveScript2.class);
        doAnswer(stopScan()).when(script1).scanNode(any(), any());
        ScriptWrapper scriptWrapper1 = createScriptWrapper(script1, ActiveScript2.class);
        ScriptWrapper scriptWrapper2 = mock(ScriptWrapper.class);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(script1, times(1)).scanNode(scriptsActiveScanner, message);
        verify(extensionScript, times(0)).getInterface(scriptWrapper2, ActiveScript2.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateScriptsCacheWithExpectedConfiguration() throws Exception {
        // Given
        ActiveScript script = mock(ActiveScript.class);
        ScriptWrapper scriptWrapper = createScriptWrapper(script, ActiveScript.class);
        given(extensionScript.getScripts(SCRIPT_TYPE)).willReturn(asList(scriptWrapper));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(mock(Variant.class)));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        ArgumentCaptor<Configuration<ActiveScript>> argumentCaptor =
                ArgumentCaptor.forClass(Configuration.class);
        verify(extensionScript).createScriptsCache(argumentCaptor.capture());
        Configuration<ActiveScript> configuration = argumentCaptor.getValue();
        assertThat(configuration.getScriptType(), is(equalTo(SCRIPT_TYPE)));
        assertThat(configuration.getTargetInterface(), is(equalTo(TARGET_INTERFACE_CACHE)));
        assertThat(configuration.getInterfaceProvider(), is(not(nullValue())));
        assertThat(configuration.getInterfaceErrorMessageProvider(), is(nullValue()));
    }

    @Test
    void shouldFailScriptsThatDoNotImplementNeitherActiveScript2NorActiveScript() {
        // Given
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.isEnabled()).willReturn(true);
        given(extensionScript.getScripts(SCRIPT_TYPE)).willReturn(asList(scriptWrapper));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(mock(Variant.class)));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        given(extensionScript.<ActiveScript>createScriptsCache(any()))
                .willAnswer(
                        e -> {
                            Configuration<ActiveScript> configuration = e.getArgument(0);
                            configuration
                                    .getInterfaceProvider()
                                    .getInterface(scriptWrapper, ActiveScript.class);
                            return null;
                        });
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(extensionScript).handleFailedScriptInterface(eq(scriptWrapper), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldScanParamsWithActiveScript() throws Exception {
        // Given
        ActiveScript script1 = mock(ActiveScript.class);
        ScriptWrapper scriptWrapper1 = createScriptWrapper(script1, ActiveScript.class);
        ActiveScript script2 = mock(ActiveScript.class);
        ScriptWrapper scriptWrapper2 = createScriptWrapper(script2, ActiveScript.class);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        ScriptsCache<ActiveScript> scriptsCache =
                createScriptsCache(
                        createCachedScript(script1, scriptWrapper1),
                        createCachedScript(script2, scriptWrapper2));
        given(extensionScript.<ActiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        given(parent.getScannerParam()).willReturn(mock(ScannerParam.class));
        String name1 = "Name1";
        String value1 = "Value1";
        NameValuePair param1 = param(name1, value1);
        String name2 = "Name2";
        String value2 = "Value2";
        NameValuePair param2 = param(name2, value2);
        Variant variant = mock(Variant.class);
        given(variant.getParamList()).willReturn(asList(param1, param2));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(variant));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(scriptsCache, times(2)).refresh();
        verify(scriptsCache, times(2)).getCachedScripts();
        verify(script1, times(1)).scan(scriptsActiveScanner, message, name1, value1);
        verify(script1, times(1)).scan(scriptsActiveScanner, message, name2, value2);
        verify(script2, times(1)).scan(scriptsActiveScanner, message, name1, value1);
        verify(script2, times(1)).scan(scriptsActiveScanner, message, name2, value2);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldStopScanningParamsWithActiveScriptWhenScanStopped() throws Exception {
        // Given
        ActiveScript script1 = mock(ActiveScript.class);
        doAnswer(stopScan()).when(script1).scan(any(), any(), any(), any());
        ScriptWrapper scriptWrapper1 = createScriptWrapper(script1, ActiveScript.class);
        ActiveScript script2 = mock(ActiveScript.class);
        ScriptWrapper scriptWrapper2 = createScriptWrapper(script2, ActiveScript.class);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        ScriptsCache<ActiveScript> scriptsCache =
                createScriptsCache(
                        createCachedScript(script1, scriptWrapper1),
                        createCachedScript(script2, scriptWrapper2));
        given(extensionScript.<ActiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        given(parent.getScannerParam()).willReturn(mock(ScannerParam.class));
        String name1 = "Name1";
        String value1 = "Value1";
        NameValuePair param1 = param(name1, value1);
        String name2 = "Name2";
        String value2 = "Value2";
        NameValuePair param2 = param(name2, value2);
        Variant variant = mock(Variant.class);
        given(variant.getParamList()).willReturn(asList(param1, param2));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(variant));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(scriptsCache, times(1)).refresh();
        verify(scriptsCache, times(1)).getCachedScripts();
        verify(script1, times(1)).scan(scriptsActiveScanner, message, name1, value1);
        verify(script1, times(0)).scan(scriptsActiveScanner, message, name2, value2);
        verify(script2, times(0)).scan(any(), any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleExceptionsThrownByActiveScript() throws Exception {
        // Given
        ActiveScript script1 = mock(ActiveScript.class);
        ScriptWrapper scriptWrapper1 = createScriptWrapper(script1, ActiveScript.class);
        ActiveScript script2 = mock(ActiveScript.class);
        ScriptWrapper scriptWrapper2 = createScriptWrapper(script2, ActiveScript.class);
        given(extensionScript.getScripts(SCRIPT_TYPE))
                .willReturn(asList(scriptWrapper1, scriptWrapper2));
        ScriptsCache<ActiveScript> scriptsCache =
                createScriptsCache(
                        createCachedScript(script1, scriptWrapper1),
                        createCachedScript(script2, scriptWrapper2));
        given(extensionScript.<ActiveScript>createScriptsCache(any())).willReturn(scriptsCache);
        given(parent.getScannerParam()).willReturn(mock(ScannerParam.class));
        String name1 = "Name1";
        String value1 = "Value1";
        NameValuePair param1 = param(name1, value1);
        ScriptException exception = mock(ScriptException.class);
        doThrow(exception).when(script1).scan(any(), any(), eq(name1), eq(value1));
        String name2 = "Name2";
        String value2 = "Value2";
        NameValuePair param2 = param(name2, value2);
        Variant variant = mock(Variant.class);
        given(variant.getParamList()).willReturn(asList(param1, param2));
        VariantFactory variantFactory = mock(VariantFactory.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(variant));
        given(model.getVariantFactory()).willReturn(variantFactory);
        ScriptsActiveScanner scriptsActiveScanner = new ScriptsActiveScanner();
        scriptsActiveScanner.init(message, parent);
        // When
        scriptsActiveScanner.scan();
        // Then
        verify(scriptsCache, times(2)).refresh();
        verify(scriptsCache, times(2)).getCachedScripts();
        verify(script1, times(1)).scan(scriptsActiveScanner, message, name1, value1);
        verify(extensionScript, times(1)).handleScriptException(scriptWrapper1, exception);
        verify(script2, times(1)).scan(scriptsActiveScanner, message, name1, value1);
        verify(script2, times(1)).scan(scriptsActiveScanner, message, name2, value2);
    }

    private <T> ScriptWrapper createScriptWrapper(T script, Class<T> scriptClass) throws Exception {
        ScriptWrapper scriptWrapper = mock(ScriptWrapper.class);
        given(scriptWrapper.isEnabled()).willReturn(true);
        given(extensionScript.getInterface(scriptWrapper, scriptClass)).willReturn(script);
        return scriptWrapper;
    }

    private Answer<Void> stopScan() {
        return e -> {
            given(parent.isStop()).willReturn(true);
            return null;
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> CachedScript<T> createCachedScript(T script, ScriptWrapper scriptWrapper) {
        CachedScript<T> cachedScript = mock(CachedScript.class);
        given(cachedScript.getScript()).willReturn(script);
        given(cachedScript.getScriptWrapper()).willReturn(scriptWrapper);
        return cachedScript;
    }

    @SuppressWarnings("unchecked")
    private static <T> ScriptsCache<T> createScriptsCache(CachedScript<T>... cachedScripts) {
        ScriptsCache<T> scriptsCache = mock(ScriptsCache.class);
        given(scriptsCache.getCachedScripts()).willReturn(asList(cachedScripts));
        return scriptsCache;
    }

    private static NameValuePair param(String name, String value) {
        NameValuePair nvp = mock(NameValuePair.class);
        given(nvp.getName()).willReturn(name);
        given(nvp.getValue()).willReturn(value);
        return nvp;
    }
}
