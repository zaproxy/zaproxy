/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.ascan.VariantFactory;
import org.zaproxy.zap.network.HttpRequestBody;

/** Unit test for {@link AbstractAppParamPlugin}. */
class AbstractAppParamPluginUnitTest extends PluginTestUtils {

    private static final String URI = "http://example.com";

    private Variant variant;
    private VariantFactory variantFactory;
    private HostProcess hostProcess;
    private HttpMessage alertMessage;
    private AbstractAppParamPlugin plugin;

    @BeforeEach
    void setup() {
        variantFactory = mock(VariantFactory.class);
        variant = mock(Variant.class);
        given(variantFactory.createVariants(any(), any())).willReturn(asList(variant));

        plugin = new AbstractAppParamPluginTest(variantFactory);

        hostProcess = mock(HostProcess.class);
        plugin.init(mock(HttpMessage.class), hostProcess);
        alertMessage = createAlertMessage();
    }

    @Test
    void shouldRaiseAlertWithScanRuleData() {
        // Given / When
        plugin.newAlert().setMessage(alertMessage).raise();
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(plugin.getName())));
        assertThat(alert.getRisk(), is(equalTo(plugin.getRisk())));
        assertThat(alert.getConfidence(), is(equalTo(Alert.CONFIDENCE_MEDIUM)));
        assertThat(alert.getDescription(), is(equalTo(plugin.getDescription())));
        assertThat(alert.getUri(), is(equalTo(URI)));
        assertThat(alert.getParam(), is(equalTo("")));
        assertThat(alert.getAttack(), is(equalTo("")));
        assertThat(alert.getEvidence(), is(equalTo("")));
        assertThat(alert.getOtherInfo(), is(equalTo("")));
        assertThat(alert.getSolution(), is(equalTo(plugin.getSolution())));
        assertThat(alert.getReference(), is(equalTo(plugin.getReference())));
        assertThat(alert.getCweId(), is(equalTo(plugin.getCweId())));
        assertThat(alert.getWascId(), is(equalTo(plugin.getWascId())));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    void shouldRaiseAlertWithCustomData() {
        // Given
        int risk = Alert.RISK_LOW;
        int confidence = Alert.CONFIDENCE_HIGH;
        String name = "name";
        String description = "description";
        String uri = "uri";
        String param = "param";
        String attack = "attack";
        String evidence = "evidence";
        String otherInfo = "otherInfo";
        String solution = "solution";
        String reference = "reference";
        int cweId = 111;
        int wascId = 222;
        // When
        plugin.newAlert()
                .setRisk(risk)
                .setConfidence(confidence)
                .setName(name)
                .setDescription(description)
                .setUri(uri)
                .setParam(param)
                .setAttack(attack)
                .setOtherInfo(otherInfo)
                .setSolution(solution)
                .setEvidence(evidence)
                .setReference(reference)
                .setCweId(cweId)
                .setWascId(wascId)
                .setMessage(alertMessage)
                .raise();
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getPluginId(), is(equalTo(plugin.getId())));
        assertThat(alert.getName(), is(equalTo(name)));
        assertThat(alert.getRisk(), is(equalTo(risk)));
        assertThat(alert.getConfidence(), is(equalTo(confidence)));
        assertThat(alert.getDescription(), is(equalTo(description)));
        assertThat(alert.getUri(), is(equalTo(uri)));
        assertThat(alert.getParam(), is(equalTo(param)));
        assertThat(alert.getAttack(), is(equalTo(attack)));
        assertThat(alert.getEvidence(), is(equalTo(evidence)));
        assertThat(alert.getOtherInfo(), is(equalTo(otherInfo)));
        assertThat(alert.getSolution(), is(equalTo(solution)));
        assertThat(alert.getReference(), is(equalTo(reference)));
        assertThat(alert.getCweId(), is(equalTo(cweId)));
        assertThat(alert.getWascId(), is(equalTo(wascId)));
        assertThat(alert.getMessage(), is(sameInstance(alertMessage)));
    }

    @Test
    void shouldRaiseAlertWithInputVector() {
        // Given
        String inputVector = "IV";
        given(variant.getShortName()).willReturn(inputVector);
        plugin.scan();
        // When
        plugin.newAlert().setMessage(alertMessage).raise();
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getInputVector(), is(equalTo(inputVector)));
    }

    @Test
    void shouldRaiseAlertWithParam() {
        // Given
        String param = "param";
        NameValuePair nvp = param(param, "Value");
        given(variant.getParamList()).willReturn(asList(nvp));
        plugin.scan();
        // When
        plugin.newAlert().setMessage(alertMessage).raise();
        // Then
        Alert alert = getRaisedAlert(hostProcess);
        assertThat(alert.getParam(), is(equalTo(param)));
    }

    private static HttpMessage createAlertMessage() {
        HttpMessage message = mock(HttpMessage.class);
        HttpRequestHeader requestHeader = mock(HttpRequestHeader.class);
        when(message.getRequestHeader()).thenReturn(requestHeader);
        URI uri = mock(URI.class);
        when(uri.toString()).thenReturn(URI);
        when(requestHeader.getURI()).thenReturn(uri);
        when(message.getRequestBody()).thenReturn(mock(HttpRequestBody.class));
        return message;
    }

    private static Alert getRaisedAlert(HostProcess hostProcess) {
        ArgumentCaptor<Alert> arg = ArgumentCaptor.forClass(Alert.class);
        verify(hostProcess).alertFound(arg.capture());
        return arg.getValue();
    }

    private static NameValuePair param(String name, String value) {
        NameValuePair nvp = mock(NameValuePair.class);
        given(nvp.getName()).willReturn(name);
        given(nvp.getValue()).willReturn(value);
        return nvp;
    }

    private static class AbstractAppParamPluginTest extends AbstractAppParamPlugin {

        static final int ID = 42;
        static final String NAME = "name";
        static final String DESCRIPTION = "description";
        static final String SOLUTION = "solution";
        static final String REFERENCE = "reference";

        private final VariantFactory variantFactory;

        public AbstractAppParamPluginTest(VariantFactory variantFactory) {
            this.variantFactory = variantFactory;
        }

        @Override
        VariantFactory getVariantFactory() {
            return variantFactory;
        }

        @Override
        public int getId() {
            return ID;
        }

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getDescription() {
            return DESCRIPTION;
        }

        @Override
        public int getCategory() {
            return 0;
        }

        @Override
        public String getSolution() {
            return SOLUTION;
        }

        @Override
        public String getReference() {
            return REFERENCE;
        }

        @Override
        protected HttpMessage getNewMsg() {
            return createAlertMessage();
        }
    }
}
