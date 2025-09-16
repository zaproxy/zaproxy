/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Alert;

/**
 * Unit tests for {@link ExampleAlertBuilder}.
 */
public class ExampleAlertBuilderTest {

    private static final int TEST_PLUGIN_ID = 12345;

    @Test
    public void shouldCreateBasicExampleAlert() {
        // Given / When
        Alert alert = ExampleAlertBuilder.newExampleAlert(TEST_PLUGIN_ID, "Test Alert").build();

        // Then
        assertNotNull(alert);
        assertEquals(TEST_PLUGIN_ID, alert.getPluginId());
        assertEquals("Test Alert", alert.getName());
        assertEquals("https://www.example.com/app/", alert.getUri());
        assertEquals(Alert.Source.ACTIVE, alert.getSource());
    }

    @Test
    public void shouldCreateHighRiskExample() {
        // Given / When
        Alert alert = ExampleAlertBuilder.createHighRiskExample(
                TEST_PLUGIN_ID, 
                "High Risk Alert", 
                "This is a high risk vulnerability", 
                "Fix it immediately")
                .build();

        // Then
        assertEquals(Alert.RISK_HIGH, alert.getRisk());
        assertEquals(Alert.CONFIDENCE_MEDIUM, alert.getConfidence());
        assertEquals("High Risk Alert", alert.getName());
        assertEquals("This is a high risk vulnerability", alert.getDescription());
        assertEquals("Fix it immediately", alert.getSolution());
    }

    @Test
    public void shouldCreateMediumRiskExample() {
        // Given / When
        Alert alert = ExampleAlertBuilder.createMediumRiskExample(
                TEST_PLUGIN_ID, 
                "Medium Risk Alert", 
                "This is a medium risk vulnerability", 
                "Fix it soon")
                .build();

        // Then
        assertEquals(Alert.RISK_MEDIUM, alert.getRisk());
        assertEquals(Alert.CONFIDENCE_MEDIUM, alert.getConfidence());
        assertEquals("Medium Risk Alert", alert.getName());
    }

    @Test
    public void shouldCreateLowRiskExample() {
        // Given / When
        Alert alert = ExampleAlertBuilder.createLowRiskExample(
                TEST_PLUGIN_ID, 
                "Low Risk Alert", 
                "This is a low risk issue", 
                "Fix when convenient")
                .build();

        // Then
        assertEquals(Alert.RISK_LOW, alert.getRisk());
        assertEquals(Alert.CONFIDENCE_MEDIUM, alert.getConfidence());
        assertEquals("Low Risk Alert", alert.getName());
    }

    @Test
    public void shouldCreateInfoExample() {
        // Given / When
        Alert alert = ExampleAlertBuilder.createInfoExample(
                TEST_PLUGIN_ID, 
                "Info Alert", 
                "This is informational", 
                "Additional details here")
                .build();

        // Then
        assertEquals(Alert.RISK_INFO, alert.getRisk());
        assertEquals(Alert.CONFIDENCE_MEDIUM, alert.getConfidence());
        assertEquals("Info Alert", alert.getName());
        assertEquals("Additional details here", alert.getOtherInfo());
    }

    @Test
    public void shouldCreateSqlInjectionExample() {
        // Given / When
        Alert alert = ExampleAlertBuilder.createSqlInjectionExample(TEST_PLUGIN_ID).build();

        // Then
        assertEquals(Alert.RISK_HIGH, alert.getRisk());
        assertEquals("SQL Injection", alert.getName());
        assertEquals(89, alert.getCweId());
        assertEquals(19, alert.getWascId());
        assertEquals("id", alert.getParam());
        assertEquals("1' OR '1'='1", alert.getAttack());
    }

    @Test
    public void shouldCreateXssExample() {
        // Given / When
        Alert alert = ExampleAlertBuilder.createXssExample(TEST_PLUGIN_ID).build();

        // Then
        assertEquals(Alert.RISK_HIGH, alert.getRisk());
        assertEquals("Cross Site Scripting (Reflected)", alert.getName());
        assertEquals(79, alert.getCweId());
        assertEquals(8, alert.getWascId());
        assertEquals("search", alert.getParam());
        assertTrue(alert.getAttack().contains("script"));
    }

    @Test
    public void shouldCreateInfoDisclosureExample() {
        // Given / When
        Alert alert = ExampleAlertBuilder.createInfoDisclosureExample(TEST_PLUGIN_ID).build();

        // Then
        assertEquals(Alert.RISK_MEDIUM, alert.getRisk());
        assertTrue(alert.getName().contains("Information Disclosure"));
        assertEquals(200, alert.getCweId());
        assertEquals(13, alert.getWascId());
        assertEquals("password", alert.getParam());
    }

    @Test
    public void shouldBuildExampleAlertsFromBuilders() {
        // Given
        Alert.Builder builder1 = ExampleAlertBuilder.newExampleAlert(TEST_PLUGIN_ID, "Alert 1");
        Alert.Builder builder2 = ExampleAlertBuilder.newExampleAlert(TEST_PLUGIN_ID, "Alert 2");

        // When
        List<Alert> alerts = ExampleAlertBuilder.buildExampleAlerts(builder1, builder2);

        // Then
        assertEquals(2, alerts.size());
        assertEquals("Alert 1", alerts.get(0).getName());
        assertEquals("Alert 2", alerts.get(1).getName());
    }
}