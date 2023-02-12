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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.core.scanner.VariantCookie;
import org.parosproxy.paros.core.scanner.VariantCustom;
import org.parosproxy.paros.core.scanner.VariantDdnPath;
import org.parosproxy.paros.core.scanner.VariantDirectWebRemotingQuery;
import org.parosproxy.paros.core.scanner.VariantFormQuery;
import org.parosproxy.paros.core.scanner.VariantGWTQuery;
import org.parosproxy.paros.core.scanner.VariantHeader;
import org.parosproxy.paros.core.scanner.VariantJSONQuery;
import org.parosproxy.paros.core.scanner.VariantMultipartFormParameters;
import org.parosproxy.paros.core.scanner.VariantODataFilterQuery;
import org.parosproxy.paros.core.scanner.VariantODataIdQuery;
import org.parosproxy.paros.core.scanner.VariantScript;
import org.parosproxy.paros.core.scanner.VariantURLPath;
import org.parosproxy.paros.core.scanner.VariantURLQuery;
import org.parosproxy.paros.core.scanner.VariantUserDefined;
import org.parosproxy.paros.core.scanner.VariantXMLQuery;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.script.ExtensionScript;
import org.zaproxy.zap.extension.script.ScriptWrapper;
import org.zaproxy.zap.extension.script.ScriptsCache;
import org.zaproxy.zap.extension.script.ScriptsCache.CachedScript;
import org.zaproxy.zap.extension.script.ScriptsCache.Configuration;
import org.zaproxy.zap.extension.script.ScriptsCache.InterfaceErrorMessageProvider;
import org.zaproxy.zap.extension.script.ScriptsCache.ScriptWrapperAction;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

class VariantFactoryUnitTest extends WithConfigsTest {

    private static final String SCRIPT_TYPE = ExtensionActiveScan.SCRIPT_TYPE_VARIANT;
    private static final Class<VariantScript> TARGET_INTERFACE = VariantScript.class;

    private ExtensionScript extensionScript;

    VariantFactory factory;

    @BeforeEach
    void setUp() {
        extensionScript = mock(ExtensionScript.class);
        given(extensionLoader.getExtension(ExtensionScript.class)).willReturn(extensionScript);

        factory = new VariantFactory();
    }

    @Test
    void shouldReturnDefaultVariants() {
        // Given
        ScannerParam scanOptions = new ScannerParam();
        HttpMessage message = new HttpMessage();

        // When
        List<Variant> variants = factory.createVariants(scanOptions, message);
        // Then
        assertThat(variants.size(), is(equalTo(10)));
        assertThat(variants.get(0).getClass(), is(equalTo(VariantURLQuery.class)));
        assertThat(variants.get(1).getClass(), is(equalTo(VariantODataIdQuery.class)));
        assertThat(variants.get(2).getClass(), is(equalTo(VariantODataFilterQuery.class)));
        assertThat(variants.get(3).getClass(), is(equalTo(VariantDdnPath.class)));
        assertThat(variants.get(4).getClass(), is(equalTo(VariantFormQuery.class)));
        assertThat(variants.get(5).getClass(), is(equalTo(VariantMultipartFormParameters.class)));
        assertThat(variants.get(6).getClass(), is(equalTo(VariantXMLQuery.class)));
        assertThat(variants.get(7).getClass(), is(equalTo(VariantJSONQuery.class)));
        assertThat(variants.get(8).getClass(), is(equalTo(VariantGWTQuery.class)));
        assertThat(variants.get(9).getClass(), is(equalTo(VariantDirectWebRemotingQuery.class)));
    }

    @Test
    void shouldReturnNoVariantsWhenUnset() {
        // Given
        ScannerParam scanOptions =
                Mockito.mock(ScannerParam.class, withSettings().strictness(Strictness.LENIENT));
        Mockito.when(scanOptions.getConfig()).thenReturn(new ZapXmlConfiguration());
        HttpMessage message = new HttpMessage();

        scanOptions.setTargetParamsInjectable(0);

        // When
        List<Variant> variants = factory.createVariants(scanOptions, message);

        // Then
        assertThat(variants.size(), is(equalTo(0)));
    }

    @Test
    void shouldReturnAllVariantsWhenSet() throws Exception {
        // Given
        ScannerParam scanOptions =
                Mockito.mock(ScannerParam.class, withSettings().strictness(Strictness.LENIENT));
        Mockito.when(scanOptions.getConfig()).thenReturn(new ZapXmlConfiguration());
        Mockito.when(scanOptions.getTargetParamsInjectable()).thenReturn(-1);
        Mockito.when(scanOptions.getTargetParamsEnabledRPC()).thenReturn(-1);
        HttpMessage message = new HttpMessage(new URI("https://www.example.com/path?query", true));

        // When
        List<Variant> variants = factory.createVariants(scanOptions, message);

        // Then
        assertThat(variants.size(), is(equalTo(13)));
        assertThat(variants.get(0).getClass(), is(equalTo(VariantURLQuery.class)));
        assertThat(variants.get(1).getClass(), is(equalTo(VariantODataIdQuery.class)));
        assertThat(variants.get(2).getClass(), is(equalTo(VariantODataFilterQuery.class)));
        assertThat(variants.get(3).getClass(), is(equalTo(VariantFormQuery.class)));
        assertThat(variants.get(4).getClass(), is(equalTo(VariantMultipartFormParameters.class)));
        assertThat(variants.get(5).getClass(), is(equalTo(VariantXMLQuery.class)));
        assertThat(variants.get(6).getClass(), is(equalTo(VariantJSONQuery.class)));
        assertThat(variants.get(7).getClass(), is(equalTo(VariantGWTQuery.class)));
        assertThat(variants.get(8).getClass(), is(equalTo(VariantDirectWebRemotingQuery.class)));
        assertThat(variants.get(9).getClass(), is(equalTo(VariantHeader.class)));
        assertThat(variants.get(10).getClass(), is(equalTo(VariantURLPath.class)));
        assertThat(variants.get(11).getClass(), is(equalTo(VariantCookie.class)));
        assertThat(variants.get(12).getClass(), is(equalTo(VariantUserDefined.class)));
    }

    @Test
    void shouldReturnCustomVariants() {
        // Given
        factory.addVariant(TestVariant.class);

        ScannerParam scanOptions = new ScannerParam();
        HttpMessage message = new HttpMessage();

        // When
        List<Variant> variants = factory.createVariants(scanOptions, message);
        // Then
        assertThat(variants.size(), is(equalTo(11)));
        assertThat(variants.get(0).getClass(), is(equalTo(VariantURLQuery.class)));
        assertThat(variants.get(1).getClass(), is(equalTo(VariantODataIdQuery.class)));
        assertThat(variants.get(2).getClass(), is(equalTo(VariantODataFilterQuery.class)));
        assertThat(variants.get(3).getClass(), is(equalTo(VariantDdnPath.class)));
        assertThat(variants.get(4).getClass(), is(equalTo(VariantFormQuery.class)));
        assertThat(variants.get(5).getClass(), is(equalTo(VariantMultipartFormParameters.class)));
        assertThat(variants.get(6).getClass(), is(equalTo(VariantXMLQuery.class)));
        assertThat(variants.get(7).getClass(), is(equalTo(VariantJSONQuery.class)));
        assertThat(variants.get(8).getClass(), is(equalTo(VariantGWTQuery.class)));
        assertThat(variants.get(9).getClass(), is(equalTo(VariantDirectWebRemotingQuery.class)));
        assertThat(variants.get(10).getClass(), is(equalTo(TestVariant.class)));
    }

    @Test
    void shouldReturnNoSiteModifyingVariantsByDefault() {
        // Given

        // When
        List<Variant> variants = factory.createSiteModifyingVariants();

        // Then
        assertThat(variants.size(), is(equalTo(0)));
    }

    @Test
    void shouldReturnAddedSiteModifyingVariants() {
        // Given
        factory.addVariant(TestVariant.class);

        // When
        List<Variant> variants = factory.createSiteModifyingVariants();

        // Then
        assertThat(variants.size(), is(equalTo(1)));
        assertThat(variants.get(0).getClass(), is(equalTo(TestVariant.class)));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldScanNullJsonValuesAsDefinedInOptions(boolean scanNulls) {
        // Given
        ScannerParam scanOptions = mock(ScannerParam.class);
        given(scanOptions.getTargetParamsInjectable()).willReturn(ScannerParam.TARGET_POSTDATA);
        given(scanOptions.getTargetParamsEnabledRPC()).willReturn(ScannerParam.RPC_JSON);
        given(scanOptions.isScanNullJsonValues()).willReturn(scanNulls);
        HttpMessage message = new HttpMessage();
        // When
        List<Variant> variants = factory.createVariants(scanOptions, message);
        // Then
        VariantJSONQuery jsonVariant = getVariant(variants, VariantJSONQuery.class);
        assertThat(jsonVariant.isScanNullValues(), is(equalTo(scanNulls)));
    }

    @Test
    void shouldNotCreateVariantScriptsIfExtensionScriptIsDisabled() {
        // Given
        given(extensionLoader.getExtension(ExtensionScript.class)).willReturn(null);
        // When
        List<Variant> variants = factory.createSiteModifyingVariants();
        // Then
        variants.forEach(e -> assertThat(e, is(not(instanceOf(VariantScript.class)))));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCreateScriptsCacheWithExpectedConfiguration() {
        // Given / When
        factory.createSiteModifyingVariants();
        // Then
        ArgumentCaptor<Configuration<VariantScript>> argumentCaptor =
                ArgumentCaptor.forClass(Configuration.class);
        verify(extensionScript).createScriptsCache(argumentCaptor.capture());
        Configuration<VariantScript> configuration = argumentCaptor.getValue();
        assertThat(configuration.getScriptType(), is(equalTo(SCRIPT_TYPE)));
        assertThat(configuration.getTargetInterface(), is(equalTo(TARGET_INTERFACE)));
        InterfaceErrorMessageProvider errorMessageProvider =
                configuration.getInterfaceErrorMessageProvider();
        assertThat(errorMessageProvider, is(not(nullValue())));
        assertThat(
                errorMessageProvider.getErrorMessage(mock(ScriptWrapper.class)),
                is(not(nullValue())));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldUseVariantScripts() {
        // Given
        VariantScript script = mock(TARGET_INTERFACE);
        CachedScript<VariantScript> cachedScript = createCachedScript(script);
        ScriptsCache<VariantScript> scriptsCache = createScriptsCache(cachedScript);
        given(extensionScript.<VariantScript>createScriptsCache(any())).willReturn(scriptsCache);
        // When
        List<Variant> variants = factory.createSiteModifyingVariants();
        // Then
        verify(scriptsCache, times(1)).refreshAndExecute(any(ScriptWrapperAction.class));
        assertThat(variants, hasSize(1));
        assertThat(variants.get(0), is(instanceOf(VariantCustom.class)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotUseVariantScriptsIfNone() {
        // Given
        ScriptsCache<VariantScript> scriptsCache = mock(ScriptsCache.class);
        given(extensionScript.<VariantScript>createScriptsCache(any())).willReturn(scriptsCache);
        // When
        List<Variant> variants = factory.createSiteModifyingVariants();
        // Then
        verify(scriptsCache, times(1)).refreshAndExecute(any(ScriptWrapperAction.class));
        assertThat(variants, hasSize(0));
    }

    static class TestVariant implements Variant {
        TestVariant() {}

        @Override
        public void setMessage(HttpMessage msg) {}

        @Override
        public List<NameValuePair> getParamList() {
            return null;
        }

        @Override
        public String setParameter(
                HttpMessage msg, NameValuePair originalPair, String param, String value) {
            return null;
        }

        @Override
        public String setEscapedParameter(
                HttpMessage msg, NameValuePair originalPair, String param, String value) {
            return null;
        }
    }

    private static <T> T getVariant(List<Variant> variants, Class<T> clazz) {
        return clazz.cast(
                variants.stream()
                        .filter(v -> v.getClass() == clazz)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Variant " + clazz + " not found in the list.")));
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
