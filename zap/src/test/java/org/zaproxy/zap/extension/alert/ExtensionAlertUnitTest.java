/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.ParameterParser;
import org.zaproxy.zap.model.StandardParameterParser;
import org.zaproxy.zap.utils.I18N;
import org.zaproxy.zap.utils.ZapXmlConfiguration;

class ExtensionAlertUnitTest {

    private static final String ORIGINAL_NAME = "Original Name";
    private static final String ORIGINAL_DESC = "Original Desc";
    private static final String ORIGINAL_SOLN = "Original Solution";
    private static final String ORIGINAL_OTHER = "Original Other";
    private static final String ORIGINAL_REF = "Original Ref";
    private static final String ORIGINAL_TAG_KEY = "Original Key";
    private static final String ORIGINAL_TAG_VALUE = "Original Value";
    private static final Map<String, String> ORIGINAL_TAG =
            Collections.singletonMap(ORIGINAL_TAG_KEY, ORIGINAL_TAG_VALUE);

    private static final String NEW_NAME = "New Name";
    private static final String NEW_DESC = "New Desc";
    private static final String NEW_SOLN = "New Solution";
    private static final String NEW_OTHER = "New Other";
    private static final String NEW_REF = "New Ref";
    private static final String NEW_TAG_VALUE = "New Value";
    private static final Map<String, String> NEW_TAG =
            Collections.singletonMap("Original Key", "New Value");

    private ExtensionAlert extAlert;

    @BeforeEach
    void setUp() throws Exception {
        extAlert = new ExtensionAlert();
    }

    private static Alert newAlert(int pluginId) {
        Alert alert = new Alert(pluginId);
        alert.setName(ORIGINAL_NAME);
        alert.setDescription(ORIGINAL_DESC);
        alert.setSolution(ORIGINAL_SOLN);
        alert.setOtherInfo(ORIGINAL_OTHER);
        alert.setReference(ORIGINAL_REF);
        alert.setTags(ORIGINAL_TAG);
        return alert;
    }

    @Test
    void shouldReplaceAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", NEW_NAME);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(NEW_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldAppendAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", "+" + NEW_NAME);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME + NEW_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldPrependAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", "-" + NEW_NAME);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(NEW_NAME + ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldReplaceAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", NEW_DESC);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(NEW_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldAppendAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", "+" + NEW_DESC);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC + NEW_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldPrependAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", "-" + NEW_DESC);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(NEW_DESC + ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldReplaceAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", NEW_SOLN);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(NEW_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldAppendAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", "+" + NEW_SOLN);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN + NEW_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldPrependAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", "-" + NEW_SOLN);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(NEW_SOLN + ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldReplaceAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", NEW_OTHER);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(NEW_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldAppendAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", "+" + NEW_OTHER);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER + NEW_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldPrependAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", "-" + NEW_OTHER);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(NEW_OTHER + ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldReplaceAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", NEW_REF);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(NEW_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldAppendAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", "+" + NEW_REF);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF + NEW_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldPrependAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", "-" + NEW_REF);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(NEW_REF + ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(ORIGINAL_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @ParameterizedTest
    @MethodSource("alertTagsMethodSource")
    void shouldReplaceAlertTagCorrectly() {
        extAlert.setAlertOverrideProperty("1.tag." + ORIGINAL_TAG_KEY, NEW_TAG_VALUE);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).isEqualTo(NEW_TAG);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldReplaceOnlySpecifiedTag() {
        // Given
        Alert alert1 = newAlert(1);
        String key1 = "Bounty";
        String value1 = "$200";
        String key2 = "Priority";
        String value2 = "Critical";
        Map<String, String> tags = new HashMap<>();
        tags.put(key1, value1);
        tags.put(key2, value2);
        alert1.setTags(tags);

        extAlert.setAlertOverrideProperty("1.tag." + key1, NEW_TAG_VALUE);

        // When/Then
        extAlert.applyOverrides(alert1);
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags()).hasSize(2);
        assertThat(alert1.getTags().get(key1)).isEqualTo(NEW_TAG_VALUE);
        assertThat(alert1.getTags().get(key2)).isEqualTo(value2);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldCopyCorrectHistoryTags() throws Exception {
        // Given
        HistoryReference href = mock(HistoryReference.class);
        when(href.getHistoryType()).thenReturn(1);
        when(href.getHistoryId()).thenReturn(1);

        Constant.messages = new I18N(Locale.ENGLISH);
        Session session = mock(Session.class);

        Model model = mock(Model.class);
        Model.setSingletonForTesting(model);
        given(model.getSession()).willReturn(session);
        ParameterParser pp = new StandardParameterParser();
        given(session.getUrlParamParser(anyString())).willReturn(pp);
        extAlert.initModel(model);

        Alert alert = newAlert(1);
        alert.setUri("https://www.example.com");
        alert.setSourceHistoryId(1);
        HttpMessage msg = new HttpMessage();
        msg.getRequestHeader().setURI(new URI("https://www.example.com", true));
        alert.setMessage(msg);

        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            List<String> tags =
                    List.of(
                            "ShouldIgnore",
                            "ALERT-TAG:",
                            "ALERT-TAG:=",
                            "ALERT-TAG: \t",
                            "ALERT-TAG: \t=",
                            "ALERT-TAG:AAA=BBB",
                            "ALERT-TAG:CCC=",
                            "ALERT-TAG:DDD=EEE",
                            "ALERT-TAG:FFF=GGG=HHH",
                            "ALERT-TAG:III");
            hr.when(() -> HistoryReference.getTags(1)).thenReturn(tags);

            // When
            extAlert.alertFound(alert, href);
            Map<String, String> alertTags = alert.getTags();

            // Then
            assertThat(alertTags).hasSize(6);
            assertThat(alertTags).containsEntry("AAA", "BBB");
            assertThat(alertTags).containsEntry("CCC", "");
            assertThat(alertTags).containsEntry("DDD", "EEE");
            assertThat(alertTags).containsEntry("FFF", "GGG=HHH");
            assertThat(alertTags).containsEntry("III", "");
            assertThat(alertTags).containsEntry("Original Key", "Original Value");
        }
    }

    private static Stream<Arguments> alertTagsMethodSource() {
        return Stream.of(
                Arguments.of("Key with whitespace", "Value with whitespace"),
                Arguments.of("example.key", "example.value"),
                Arguments.of("example_key", "example_value"),
                Arguments.of("", "emptyKey"),
                Arguments.of("emptyValue", ""));
    }

    @ParameterizedTest
    @MethodSource("alertTagsMethodSource")
    void shouldAddNewAlertTagsCorrectly(String key, String value) {
        extAlert.setAlertOverrideProperty("1.tag." + key, value);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags().get(key)).isEqualTo(value);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Value with whitespace", "example.value", "example_value", ""})
    void shouldAppendAlertTagCorrectly(String value) {
        extAlert.setAlertOverrideProperty("1.tag." + ORIGINAL_TAG_KEY, "+" + value);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags().get(ORIGINAL_TAG_KEY)).isEqualTo(ORIGINAL_TAG_VALUE + value);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Value with whitespace", "example.value", "example_value", ""})
    void shouldPrependAlertTagCorrectly(String value) {
        extAlert.setAlertOverrideProperty("1.tag." + ORIGINAL_TAG_KEY, "-" + value);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertThat(alert1.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert1.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert1.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert1.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert1.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert1.getTags().get(ORIGINAL_TAG_KEY)).isEqualTo(value + ORIGINAL_TAG_VALUE);

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertThat(alert2.getName()).isEqualTo(ORIGINAL_NAME);
        assertThat(alert2.getDescription()).isEqualTo(ORIGINAL_DESC);
        assertThat(alert2.getSolution()).isEqualTo(ORIGINAL_SOLN);
        assertThat(alert2.getOtherInfo()).isEqualTo(ORIGINAL_OTHER);
        assertThat(alert2.getReference()).isEqualTo(ORIGINAL_REF);
        assertThat(alert2.getTags()).isEqualTo(ORIGINAL_TAG);
    }

    @Test
    void shouldIdentifySystemicAlerts() {
        // Given
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(3);

        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=1");
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=2");
        Alert a3 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=3");
        Alert a4 =
                newAlert(
                        1,
                        3,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=4");

        a1.setTags(Map.of("SYSTEMIC", "true"));
        a2.setTags(Map.of("SYSTEMIC", "true"));
        a3.setTags(Map.of("SYSTEMIC", "true"));
        a4.setTags(Map.of("SYSTEMIC", "true"));

        // When
        boolean b1 = extAlert.isOverSystemicLimit(a1);
        boolean b2 = extAlert.isOverSystemicLimit(a2);
        boolean b3 = extAlert.isOverSystemicLimit(a3);
        boolean b4 = extAlert.isOverSystemicLimit(a4);

        // Then
        assertThat(a1.isSystemic()).isTrue();
        assertThat(a2.isSystemic()).isTrue();
        assertThat(a3.isSystemic()).isTrue();
        assertThat(a4.isSystemic()).isTrue();
        assertThat(b1).isFalse();
        assertThat(b2).isFalse();
        assertThat(b3).isFalse();
        assertThat(b4).isTrue();
    }

    private static Alert newAlert(int pluginId, int id, String name, String nodeName, String uri) {
        Alert alert = new Alert(pluginId, Alert.RISK_MEDIUM, Alert.RISK_MEDIUM, name);
        alert.setUri(uri);
        alert.setAlertId(id);
        alert.setNodeName(nodeName);

        HistoryReference href = mock(HistoryReference.class);
        given(href.getMethod()).willReturn("GET");
        try {
            given(href.getURI()).willReturn(new URI(uri, true));
        } catch (Exception e) {
            // Ignore
        }
        alert.setHistoryRef(href);

        return alert;
    }
}
