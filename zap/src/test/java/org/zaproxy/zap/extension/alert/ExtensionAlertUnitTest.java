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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.core.scanner.Alert;

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
}
