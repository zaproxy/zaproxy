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
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.core.scanner.VariantCookie;
import org.parosproxy.paros.core.scanner.VariantDdnPath;
import org.parosproxy.paros.core.scanner.VariantDirectWebRemotingQuery;
import org.parosproxy.paros.core.scanner.VariantFormQuery;
import org.parosproxy.paros.core.scanner.VariantGWTQuery;
import org.parosproxy.paros.core.scanner.VariantHeader;
import org.parosproxy.paros.core.scanner.VariantJSONQuery;
import org.parosproxy.paros.core.scanner.VariantMultipartFormParameters;
import org.parosproxy.paros.core.scanner.VariantODataFilterQuery;
import org.parosproxy.paros.core.scanner.VariantODataIdQuery;
import org.parosproxy.paros.core.scanner.VariantURLPath;
import org.parosproxy.paros.core.scanner.VariantURLQuery;
import org.parosproxy.paros.core.scanner.VariantUserDefined;
import org.parosproxy.paros.core.scanner.VariantXMLQuery;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

public class VariantFactoryUnitTest {

    VariantFactory factory;

    @BeforeEach
    public void setUp() throws Exception {
        ExtensionLoader extLoader = Mockito.mock(ExtensionLoader.class);
        Control control = Mockito.mock(Control.class, withSettings().lenient());
        Mockito.when(control.getExtensionLoader()).thenReturn(extLoader);
        Control.initSingletonForTesting(Model.getSingleton());
        factory = new VariantFactory();
    }

    @Test
    public void shouldReturnDefaultVariants() {
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
    public void shouldReturnNoVariantsWhenUnset() {
        // Given
        ScannerParam scanOptions = Mockito.mock(ScannerParam.class, withSettings().lenient());
        Mockito.when(scanOptions.getConfig()).thenReturn(new ZapXmlConfiguration());
        HttpMessage message = new HttpMessage();

        scanOptions.setTargetParamsInjectable(0);

        // When
        List<Variant> variants = factory.createVariants(scanOptions, message);

        // Then
        assertThat(variants.size(), is(equalTo(0)));
    }

    @Test
    public void shouldReturnAllVariantsWhenSet() throws Exception {
        // Given
        ScannerParam scanOptions = Mockito.mock(ScannerParam.class, withSettings().lenient());
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
    public void shouldReturnCustomVariants() {
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
    public void shouldReturnNoSiteModifyingVariantsByDefault() {
        // Given

        // When
        List<Variant> variants = factory.createSiteModifyingVariants();

        // Then
        assertThat(variants.size(), is(equalTo(0)));
    }

    @Test
    public void shouldReturnAddedSiteModifyingVariants() {
        // Given
        factory.addVariant(TestVariant.class);

        // When
        List<Variant> variants = factory.createSiteModifyingVariants();

        // Then
        assertThat(variants.size(), is(equalTo(1)));
        assertThat(variants.get(0).getClass(), is(equalTo(TestVariant.class)));
    }

    public static class TestVariant implements Variant {
        public TestVariant() {}

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
}
