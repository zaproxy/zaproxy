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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.db.TableAlertTag;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
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

    private static final int HISTORY_ID = 7;

    private ExtensionAlert extAlert;
    private TableAlert tableAlert;
    private TableAlertTag tableAlertTag;

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
        assertEquals(NEW_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldAppendAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", "+" + NEW_NAME);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME + NEW_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldPrependAlertNameCorrectly() {
        extAlert.setAlertOverrideProperty("1.name", "-" + NEW_NAME);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(NEW_NAME + ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldReplaceAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", NEW_DESC);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(NEW_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldAppendAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", "+" + NEW_DESC);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC + NEW_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldPrependAlertDescCorrectly() {
        extAlert.setAlertOverrideProperty("1.description", "-" + NEW_DESC);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(NEW_DESC + ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldReplaceAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", NEW_SOLN);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(NEW_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldAppendAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", "+" + NEW_SOLN);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN + NEW_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldPrependAlertSolnCorrectly() {
        extAlert.setAlertOverrideProperty("1.solution", "-" + NEW_SOLN);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(NEW_SOLN + ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldReplaceAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", NEW_OTHER);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(NEW_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldAppendAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", "+" + NEW_OTHER);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER + NEW_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldPrependAlertOtherCorrectly() {
        extAlert.setAlertOverrideProperty("1.otherInfo", "-" + NEW_OTHER);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(NEW_OTHER + ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldReplaceAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", NEW_REF);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(NEW_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldAppendAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", "+" + NEW_REF);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF + NEW_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldPrependAlertRefCorrectly() {
        extAlert.setAlertOverrideProperty("1.reference", "-" + NEW_REF);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(NEW_REF + ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @ParameterizedTest
    @MethodSource("alertTagsMethodSource")
    void shouldReplaceAlertTagCorrectly() {
        extAlert.setAlertOverrideProperty("1.tag." + ORIGINAL_TAG_KEY, NEW_TAG_VALUE);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(NEW_TAG, alert1.getTags());

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
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
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(2, alert1.getTags().size());
        assertEquals(NEW_TAG_VALUE, alert1.getTags().get(key1));
        assertEquals(value2, alert1.getTags().get(key2));

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
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
            assertEquals(6, alertTags.size());
            assertThat(alertTags, hasEntry("AAA", "BBB"));
            assertThat(alertTags, hasEntry("CCC", ""));
            assertThat(alertTags, hasEntry("DDD", "EEE"));
            assertThat(alertTags, hasEntry("FFF", "GGG=HHH"));
            assertThat(alertTags, hasEntry("III", ""));
            assertThat(alertTags, hasEntry("Original Key", "Original Value"));
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
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(value, alert1.getTags().get(key));

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Value with whitespace", "example.value", "example_value", ""})
    void shouldAppendAlertTagCorrectly(String value) {
        extAlert.setAlertOverrideProperty("1.tag." + ORIGINAL_TAG_KEY, "+" + value);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(ORIGINAL_TAG_VALUE + value, alert1.getTags().get(ORIGINAL_TAG_KEY));

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Value with whitespace", "example.value", "example_value", ""})
    void shouldPrependAlertTagCorrectly(String value) {
        extAlert.setAlertOverrideProperty("1.tag." + ORIGINAL_TAG_KEY, "-" + value);

        Alert alert1 = newAlert(1);
        extAlert.applyOverrides(alert1);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert1.getName());
        assertEquals(ORIGINAL_DESC, alert1.getDescription());
        assertEquals(ORIGINAL_SOLN, alert1.getSolution());
        assertEquals(ORIGINAL_OTHER, alert1.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert1.getReference());
        assertEquals(value + ORIGINAL_TAG_VALUE, alert1.getTags().get(ORIGINAL_TAG_KEY));

        // Check other alerts are not affected
        Alert alert2 = newAlert(2);
        extAlert.applyOverrides(alert2);
        // When/Then
        assertEquals(ORIGINAL_NAME, alert2.getName());
        assertEquals(ORIGINAL_DESC, alert2.getDescription());
        assertEquals(ORIGINAL_SOLN, alert2.getSolution());
        assertEquals(ORIGINAL_OTHER, alert2.getOtherInfo());
        assertEquals(ORIGINAL_REF, alert2.getReference());
        assertEquals(ORIGINAL_TAG, alert2.getTags());
    }

    @Test
    void shouldShowAlertsUpToSystemicLimit() throws Exception {
        // Given - a systemic rule with a limit of three
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(3);

        // When - four alerts of the rule are raised
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            for (int i = 1; i <= 4; i++) {
                Alert alert = newAlertToRaise("https://www.example.com/" + i);
                alert.setTags(Map.of("SYSTEMIC", "true"));
                extAlert.alertFound(alert, href);
            }
        }

        // Then - only the alerts up to the limit are shown
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildCount());
        assertEquals(3, root.getChildAt(0).getChildCount());
    }

    @Test
    void shouldNotCountTheSameAlertTwiceTowardsTheSystemicLimit() throws Exception {
        // Given - a systemic rule with a limit of one, with one alert shown
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(1);
        Alert a1 = newAlertToRaise("https://www.example.com/1");
        a1.setTags(Map.of("SYSTEMIC", "true"));
        Alert a2 = newAlertToRaise("https://www.example.com/2");
        a2.setTags(Map.of("SYSTEMIC", "true"));
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            extAlert.alertFound(a1, href);
            extAlert.alertFound(a2, href);
        }
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildAt(0).getChildCount());

        // When - the shown alert is changed, as an alert filter does, which takes it out of the
        // tree and adds it back
        for (int i = 0; i < 3; i++) {
            a1.setRiskConfidence(a1.getRisk(), Alert.CONFIDENCE_MEDIUM);
            extAlert.updateAlert(a1);
        }

        // Then - it does not count more than once, thus it is still shown and the other alert is
        // still not shown
        assertEquals(1, root.getChildCount());
        assertEquals(1, root.getChildAt(0).getChildCount());
    }

    @Test
    void shouldCountSystemicLimitPerHost() throws Exception {
        // Given - a systemic rule with a limit of two
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(2);

        // When - three alerts are raised for each of two hosts
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            for (String host : List.of("https://www.example.com", "https://www.example.net")) {
                for (int i = 1; i <= 3; i++) {
                    Alert alert = newAlertToRaise(host + "/" + i);
                    alert.setTags(Map.of("SYSTEMIC", "true"));
                    extAlert.alertFound(alert, href);
                }
            }
        }

        // Then - the limit is counted for each host
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildCount());
        assertEquals(4, root.getChildAt(0).getChildCount());
    }

    @Test
    void shouldNotApplySystemicLimitToAlertsThatAreNotSystemic() throws Exception {
        // Given - a limit of one, for a rule whose alerts are not systemic
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(1);

        // When - three alerts of the rule are raised
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            for (int i = 1; i <= 3; i++) {
                extAlert.alertFound(newAlertToRaise("https://www.example.com/" + i), href);
            }
        }

        // Then - all of them are shown, the limit does not apply to them
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildCount());
        assertEquals(3, root.getChildAt(0).getChildCount());
    }

    @Test
    void shouldDeriveGroupWhenCheckingSystemicLimit() throws Exception {
        // Given - a systemic rule with a limit of one, with one alert of the rule shown
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(1);
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            Alert a1 = newAlertToRaise("https://www.example.com/1");
            a1.setTags(Map.of("SYSTEMIC", "true"));
            extAlert.alertFound(a1, href);
        }

        // When/Then - the group of an alert of the same rule and risk/confidence is derived, the
        // limit is already reached
        Alert sameGroup = newAlertToRaise("https://www.example.com/2");
        sameGroup.setTags(Map.of("SYSTEMIC", "true"));
        assertTrue(extAlert.isOverSystemicLimit(sameGroup));

        // ...and an alert of another rule, for which the tree has no group, is not over the limit
        Alert otherGroup = newAlertToRaise("https://www.example.com/2");
        otherGroup.setName("Alert B");
        otherGroup.setTags(Map.of("SYSTEMIC", "true"));
        assertFalse(extAlert.isOverSystemicLimit(otherGroup));

        // ...and the checks have no side effects, the limit is still reached
        assertTrue(extAlert.isOverSystemicLimit(sameGroup));
        assertFalse(extAlert.isOverSystemicLimit(null));
    }

    @Test
    void shouldNotShowMoreAlertsThanTheSystemicLimitWhenFiltered() throws Exception {
        // Given - fifty alerts of a systemic rule, with the default limit of five, thus only the
        // first ones raised are shown in the tree
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(5);
        List<Alert> raised = new ArrayList<>();
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            for (int i = 1; i <= 50; i++) {
                Alert alert = newAlertToRaise("https://www.example.com/" + i);
                alert.setTags(Map.of("SYSTEMIC", "true"));
                extAlert.alertFound(alert, href);
                raised.add(alert);
            }
        }
        assertEquals(5, extAlert.getTreeModel().getRoot().getChildAt(0).getChildCount());

        // When - all the alerts are changed to false positives, as the alert filters do
        for (Alert alert : raised) {
            alert.setRiskConfidence(alert.getRisk(), Alert.CONFIDENCE_FALSE_POSITIVE);
            extAlert.updateAlert(alert);
        }

        // Then - the limit is still honoured, the alerts are not shown above it just because they
        // changed to the same risk/confidence
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildCount());
        // False positive alerts are shown with a risk of -1 in the tree
        assertEquals(-1, root.getChildAt(0).getRisk());
        assertTrue(
                root.getChildAt(0).getChildCount() <= 5,
                "Alerts shown above the systemic limit: " + root.getChildAt(0).getChildCount());
    }

    @Test
    void shouldShowAllAlertsAsFalsePositiveWhenFiltered() throws Exception {
        // Given - nine alerts of a systemic rule, with a limit of five, thus only the first five
        // are shown in the tree
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(5);
        List<Alert> raised = new ArrayList<>();
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            for (int i = 1; i <= 9; i++) {
                Alert alert = newAlertToRaise("https://www.example.com/" + i);
                alert.setTags(Map.of("SYSTEMIC", "true"));
                extAlert.alertFound(alert, href);
                raised.add(alert);
            }
        }
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildCount());
        assertEquals(5, root.getChildAt(0).getChildCount());

        // When - all the alerts are changed to false positives, as the alert filters do
        for (Alert alert : raised) {
            alert.setRiskConfidence(alert.getRisk(), Alert.CONFIDENCE_FALSE_POSITIVE);
            extAlert.updateAlert(alert);
        }

        // Then - the alerts shown in the tree are all false positives, none is left with the
        // original risk, the alerts that were over the systemic limit are not shown
        assertEquals(
                """
                - Alerts
                  - False Positive: Alert A
                    - :https://www.example.com/1
                    - :https://www.example.com/2
                    - :https://www.example.com/3
                    - :https://www.example.com/4
                    - :https://www.example.com/5
                """,
                TextAlertTree.toString(extAlert.getTreeModel()));
    }

    @Test
    void shouldUpdateAlertRebuiltWithoutMessage() throws Exception {
        // Given - an alert shown in the tree
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(5);
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            extAlert.alertFound(newAlertToRaise("https://www.example.com/1"), href);
        }
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildAt(0).getChildCount());

        // When - the alert is rebuilt from the database, without the message (nor the history
        // reference) that raised it, as the alert filters do, and changed to a false positive
        Alert rebuilt = newSystemicAlertWithoutMessage(1);
        rebuilt.setName("Alert A");
        rebuilt.setHistoryId(HISTORY_ID);
        rebuilt.setNodeName("https://www.example.com/1");
        rebuilt.setRiskConfidence(Alert.RISK_MEDIUM, Alert.CONFIDENCE_FALSE_POSITIVE);
        extAlert.updateAlert(rebuilt);

        // Then - the alert is no longer shown with the original confidence
        assertEquals(1, root.getChildCount());
        assertEquals(1, root.getChildAt(0).getChildCount());
        assertEquals(
                Alert.CONFIDENCE_FALSE_POSITIVE,
                root.getChildAt(0).getChildAt(0).getAlert().getConfidence());
    }

    @Test
    void shouldReadAlertWithTags() throws Exception {
        // Given - an alert stored with tags
        HistoryReference href = setUpExtensionWithDb();
        given(tableAlertTag.getTagsByAlertId(anyLong())).willReturn(Map.of("SYSTEMIC", "true"));
        Alert raised = newAlertToRaise("https://www.example.com/");
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            extAlert.alertFound(raised, href);
        }

        // When
        Alert alert = extAlert.getAlert(raised.getAlertId());

        // Then - the alert is read with the tags stored for it
        assertEquals(raised.getAlertId(), alert.getAlertId());
        assertEquals(Map.of("SYSTEMIC", "true"), alert.getTags());
        assertTrue(alert.isSystemic());
    }

    @Test
    void shouldReturnNullWhenAlertNotFound() throws Exception {
        // Given
        setUpExtensionWithDb();
        given(tableAlert.read(anyInt())).willReturn(null);

        // When/Then
        assertNull(extAlert.getAlert(1234));
    }

    @Test
    void shouldNotDeleteStoredTagsWhenAlertReadAndUpdated() throws Exception {
        // Given - an alert stored with tags
        HistoryReference href = setUpExtensionWithDb();
        given(tableAlertTag.getTagsByAlertId(anyLong())).willReturn(Map.of("SYSTEMIC", "true"));
        Alert raised = newAlertToRaise("https://www.example.com/");
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            extAlert.alertFound(raised, href);
        }
        Alert alert = extAlert.getAlert(raised.getAlertId());

        // When - the alert is changed, as done by the API
        alert.setRisk(Alert.RISK_LOW);
        extAlert.updateAlert(alert);

        // Then - the tags stored for the alert were not removed
        verify(tableAlertTag, never()).delete(anyLong(), anyString());
        verify(tableAlertTag, never()).deleteAllTagsForAlert(anyLong());
    }

    @Test
    void shouldApplySystemicLimitToAlertsReadFromDb() throws Exception {
        // Given - six alerts of a systemic rule, with a limit of five, thus only the first five are
        // shown in the tree
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(5);
        // The tags of the alerts are stored, as written when they were raised
        given(tableAlertTag.getTagsByAlertId(anyLong())).willReturn(Map.of("SYSTEMIC", "true"));
        List<Alert> raised = new ArrayList<>();
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            for (int i = 1; i <= 6; i++) {
                Alert alert = newAlertToRaise("https://www.example.com/" + i);
                alert.setTags(Map.of("SYSTEMIC", "true"));
                extAlert.alertFound(alert, href);
                raised.add(alert);
            }
        }
        AlertNode root = extAlert.getTreeModel().getRoot();
        assertEquals(1, root.getChildCount());
        assertEquals(5, root.getChildAt(0).getChildCount());

        // And - the alerts shown are changed to false positives, using up the limit
        for (int i = 0; i < 5; i++) {
            Alert alert = raised.get(i);
            alert.setRiskConfidence(alert.getRisk(), Alert.CONFIDENCE_FALSE_POSITIVE);
            extAlert.updateAlert(alert);
        }
        assertEquals(1, root.getChildCount());
        assertEquals(5, root.getChildAt(0).getChildCount());

        // And - the sixth alert, which was not shown, is read from the database and changed to a
        // false positive, as the alert filters do
        Alert read = extAlert.getAlert(raised.get(5).getAlertId());
        read.setRiskConfidence(read.getRisk(), Alert.CONFIDENCE_FALSE_POSITIVE);

        // When
        extAlert.updateAlert(read);

        // Then - the alert is known to be systemic, as its tags were read, and counted, the limit
        // is already reached so it is not shown
        assertTrue(read.isSystemic());
        assertEquals(1, root.getChildCount());
        assertEquals(5, root.getChildAt(0).getChildCount());
    }

    @Test
    void shouldPublishAlertAddedEventWhenAlertIsNotAddedToTree() throws Exception {
        // Given - two identical alerts (e.g. the same URL scanned twice), only the first is added
        HistoryReference href = setUpExtensionWithDb();
        List<Event> events = new ArrayList<>();
        EventConsumer consumer = events::add;
        String publisherName = AlertEventPublisher.getPublisher().getPublisherName();
        ZAP.getEventBus()
                .registerConsumer(consumer, publisherName, AlertEventPublisher.ALERT_ADDED_EVENT);
        try {
            // When
            try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
                hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
                extAlert.alertFound(newAlertToRaise("https://www.example.com/"), href);
                extAlert.alertFound(newAlertToRaise("https://www.example.com/"), href);
            }

            // Then - only the first alert was added to the tree
            AlertNode root = extAlert.getTreeModel().getRoot();
            assertEquals(1, root.getChildCount());
            assertEquals(1, root.getChildAt(0).getChildCount());
            // ...but both alerts were published so consumers, e.g. alert filters, can act on them
            assertEquals(2, events.size());
        } finally {
            ZAP.getEventBus().unregisterConsumer(consumer, publisherName);
        }
    }

    @Test
    void shouldPublishAlertAddedEventWhenAlertIsOverSystemicLimit() throws Exception {
        // Given - only one alert of the rule is allowed
        HistoryReference href = setUpExtensionWithDb();
        extAlert.getAlertParam().load(new ZapXmlConfiguration());
        extAlert.getAlertParam().setSystemicLimit(1);
        Alert a1 = newAlertToRaise("https://www.example.com/1");
        Alert a2 = newAlertToRaise("https://www.example.com/2");
        a1.setTags(Map.of("SYSTEMIC", "true"));
        a2.setTags(Map.of("SYSTEMIC", "true"));

        List<Event> events = new ArrayList<>();
        EventConsumer consumer = events::add;
        String publisherName = AlertEventPublisher.getPublisher().getPublisherName();
        ZAP.getEventBus()
                .registerConsumer(consumer, publisherName, AlertEventPublisher.ALERT_ADDED_EVENT);
        try {
            // When
            try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
                hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
                extAlert.alertFound(a1, href);
                extAlert.alertFound(a2, href);
            }

            // Then - the second alert is over the limit and was not added to the tree
            AlertNode root = extAlert.getTreeModel().getRoot();
            assertEquals(1, root.getChildCount());
            assertEquals(1, root.getChildAt(0).getChildCount());
            // ...but both alerts were published so consumers, e.g. alert filters, can act on them
            assertEquals(2, events.size());
        } finally {
            ZAP.getEventBus().unregisterConsumer(consumer, publisherName);
        }
    }

    @Test
    void shouldUpdateStoredTagsWhenAlertUpdatedWithTags() throws Exception {
        // Given - an alert stored with tags, updated with an alert that has its tags loaded
        HistoryReference href = setUpExtensionWithDb();
        given(tableAlertTag.getTagsByAlertId(anyLong())).willReturn(Map.of("SYSTEMIC", "true"));
        Alert raised = newAlertToRaise("https://www.example.com/");
        try (MockedStatic<HistoryReference> hr = mockStatic(HistoryReference.class)) {
            hr.when(() -> HistoryReference.getTags(anyInt())).thenReturn(List.of());
            extAlert.alertFound(raised, href);
        }

        Alert updated = new Alert(1, Alert.RISK_MEDIUM, Alert.CONFIDENCE_MEDIUM, "Alert A");
        updated.setUri("https://www.example.com/");
        updated.setAlertId(raised.getAlertId());
        updated.setHistoryId(HISTORY_ID);
        updated.setTags(Map.of("New", "Value"));
        extAlert.updateAlert(updated);

        // Then - the stored tags are replaced with the new ones
        verify(tableAlertTag).delete(anyLong(), eq("SYSTEMIC"));
        verify(tableAlertTag).insertOrUpdate(anyLong(), eq("New"), eq("Value"));
    }

    /**
     * Initialises the extension with a mocked model and database so that alerts can be raised and
     * updated, and returns the {@code HistoryReference} used for the raised alerts. Each alert
     * raised is written to the database with a new alert ID.
     */
    private HistoryReference setUpExtensionWithDb() throws Exception {
        Constant.messages = new I18N(Locale.ENGLISH);

        Session session = mock(Session.class);
        Model model = mock(Model.class);
        Model.setSingletonForTesting(model);
        given(model.getSession()).willReturn(session);
        given(session.getUrlParamParser(anyString())).willReturn(new StandardParameterParser());
        extAlert.initModel(model);

        Database database = mock(Database.class);
        given(model.getDb()).willReturn(database);
        tableAlert = mock(TableAlert.class);
        given(database.getTableAlert()).willReturn(tableAlert);
        tableAlertTag = mock(TableAlertTag.class);
        given(database.getTableAlertTag()).willReturn(tableAlertTag);
        AtomicInteger nextAlertId = new AtomicInteger();
        Map<Integer, RecordAlert> writtenAlerts = new HashMap<>();
        given(
                        tableAlert.write(
                                anyInt(), anyInt(), any(), anyInt(), anyInt(), any(), any(), any(),
                                any(), any(), any(), any(), any(), anyInt(), anyInt(), anyInt(),
                                anyInt(), anyInt(), any(), any(), any()))
                .willAnswer(
                        invocation -> {
                            RecordAlert recordAlert = mock(RecordAlert.class);
                            int alertId = nextAlertId.incrementAndGet();
                            given(recordAlert.getAlertId()).willReturn(alertId);
                            given(recordAlert.getHistoryId())
                                    .willReturn(invocation.getArgument(15));
                            // Echo the values written, as they are read back
                            given(recordAlert.getPluginId()).willReturn(invocation.getArgument(1));
                            given(recordAlert.getAlert()).willReturn(invocation.getArgument(2));
                            given(recordAlert.getRisk()).willReturn(invocation.getArgument(3));
                            given(recordAlert.getConfidence())
                                    .willReturn(invocation.getArgument(4));
                            given(recordAlert.getDescription())
                                    .willReturn(invocation.getArgument(5));
                            given(recordAlert.getUri()).willReturn(invocation.getArgument(6));
                            given(recordAlert.getParam()).willReturn(invocation.getArgument(7));
                            given(recordAlert.getAttack()).willReturn(invocation.getArgument(8));
                            given(recordAlert.getOtherInfo()).willReturn(invocation.getArgument(9));
                            given(recordAlert.getSolution()).willReturn(invocation.getArgument(10));
                            given(recordAlert.getReference())
                                    .willReturn(invocation.getArgument(11));
                            given(recordAlert.getEvidence()).willReturn(invocation.getArgument(12));
                            given(recordAlert.getCweId()).willReturn(invocation.getArgument(13));
                            given(recordAlert.getWascId()).willReturn(invocation.getArgument(14));
                            given(recordAlert.getSourceHistoryId())
                                    .willReturn(invocation.getArgument(16));
                            given(recordAlert.getSourceId()).willReturn(invocation.getArgument(17));
                            given(recordAlert.getAlertRef()).willReturn(invocation.getArgument(18));
                            given(recordAlert.getInputVector())
                                    .willReturn(invocation.getArgument(19));
                            given(recordAlert.getNodeName()).willReturn(invocation.getArgument(20));
                            writtenAlerts.put(alertId, recordAlert);
                            return recordAlert;
                        });
        given(tableAlert.read(anyInt()))
                .willAnswer(invocation -> writtenAlerts.get(invocation.getArgument(0)));

        HistoryReference href = mock(HistoryReference.class);
        given(href.getHistoryId()).willReturn(HISTORY_ID);
        given(href.getHistoryType()).willReturn(HistoryReference.TYPE_SCANNER);
        return href;
    }

    private static Alert newAlertToRaise(String uri) throws Exception {
        Alert alert = new Alert(1, Alert.RISK_MEDIUM, Alert.CONFIDENCE_MEDIUM, "Alert A");
        alert.setUri(uri);
        HttpMessage msg = new HttpMessage();
        msg.getRequestHeader().setURI(new URI(uri, true));
        alert.setMessage(msg);
        return alert;
    }

    private static Alert newAlertOfRule(int id) {
        return newAlert(
                1, id, "Alert A", "https://www.example.com(a)", "https://www.example.com?a=" + id);
    }

    private static Alert newSystemicAlert(int id) {
        Alert alert = newAlertOfRule(id);
        alert.setTags(Map.of("SYSTEMIC", "true"));
        return alert;
    }

    private static Alert newSystemicAlertWithoutMessage(int id) {
        Alert alert = newAlertWithoutMessage(id);
        alert.setTags(Map.of("SYSTEMIC", "true"));
        return alert;
    }

    private static Alert newAlertWithoutMessage(int id) {
        Alert alert = new Alert(1, Alert.RISK_MEDIUM, Alert.CONFIDENCE_MEDIUM, "Alert A");
        alert.setUri("https://www.example.com?a=" + id);
        alert.setAlertId(id);
        alert.setNodeName("https://www.example.com(a)");
        return alert;
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
