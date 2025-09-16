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
package org.zaproxy.zap.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.zap.extension.ascan.TestSqlInjectionPlugin;
import org.zaproxy.zap.extension.ascan.TestXssPlugin;
import org.zaproxy.zap.extension.pscan.TestInfoDisclosureScanner;

/**
 * Integration tests for getExampleAlerts functionality across different scanner types.
 */
public class ExampleAlertIntegrationTest {

    @Test
    public void shouldGetExampleAlertsFromSqlInjectionPlugin() {
        // Given
        TestSqlInjectionPlugin plugin = new TestSqlInjectionPlugin();

        // When
        List<Alert> examples = plugin.getExampleAlerts();

        // Then
        assertNotNull(examples);
        assertTrue(examples.size() >= 3); // Should have at least 3 SQL injection examples
        
        // Verify we have different types of SQL injection alerts
        boolean hasBasicSql = false;
        boolean hasBlindSql = false;
        boolean hasBooleanSql = false;
        
        for (Alert alert : examples) {
            assertEquals(plugin.getId(), alert.getPluginId());
            assertEquals(Alert.RISK_HIGH, alert.getRisk());
            assertEquals(89, alert.getCweId()); // CWE-89: SQL Injection
            assertEquals(19, alert.getWascId()); // WASC-19: SQL Injection
            
            if (alert.getName().equals("SQL Injection")) {
                hasBasicSql = true;
            } else if (alert.getName().contains("Blind")) {
                hasBlindSql = true;
            } else if (alert.getName().contains("Boolean")) {
                hasBooleanSql = true;
            }
        }
        
        assertTrue(hasBasicSql, "Should include basic SQL injection example");
        assertTrue(hasBlindSql, "Should include blind SQL injection example");
        assertTrue(hasBooleanSql, "Should include boolean-based SQL injection example");
    }

    @Test
    public void shouldGetExampleAlertsFromXssPlugin() {
        // Given
        TestXssPlugin plugin = new TestXssPlugin();

        // When
        List<Alert> examples = plugin.getExampleAlerts();

        // Then
        assertNotNull(examples);
        assertTrue(examples.size() >= 3); // Should have at least 3 XSS examples
        
        // Verify we have different types of XSS alerts
        boolean hasReflected = false;
        boolean hasStored = false;
        boolean hasDom = false;
        
        for (Alert alert : examples) {
            assertEquals(plugin.getId(), alert.getPluginId());
            assertEquals(Alert.RISK_HIGH, alert.getRisk());
            assertEquals(79, alert.getCweId()); // CWE-79: Cross-site Scripting
            assertEquals(8, alert.getWascId()); // WASC-8: Cross-site Scripting
            
            if (alert.getName().contains("Reflected")) {
                hasReflected = true;
            } else if (alert.getName().contains("Stored")) {
                hasStored = true;
            } else if (alert.getName().contains("DOM")) {
                hasDom = true;
            }
        }
        
        assertTrue(hasReflected, "Should include reflected XSS example");
        assertTrue(hasStored, "Should include stored XSS example");
        assertTrue(hasDom, "Should include DOM-based XSS example");
    }

    @Test
    public void shouldGetExampleAlertsFromInfoDisclosureScanner() {
        // Given
        TestInfoDisclosureScanner scanner = new TestInfoDisclosureScanner();

        // When
        List<Alert> examples = scanner.getExampleAlerts();

        // Then
        assertNotNull(examples);
        assertTrue(examples.size() >= 4); // Should have at least 4 info disclosure examples
        
        // Verify we have different types of information disclosure alerts
        boolean hasUrlInfo = false;
        boolean hasServerInfo = false;
        boolean hasDbError = false;
        boolean hasDebugInfo = false;
        
        for (Alert alert : examples) {
            assertEquals(scanner.getPluginId(), alert.getPluginId());
            assertTrue(alert.getName().contains("Information Disclosure"));
            
            if (alert.getName().contains("URL")) {
                hasUrlInfo = true;
                assertEquals(Alert.RISK_MEDIUM, alert.getRisk());
            } else if (alert.getName().contains("Server")) {
                hasServerInfo = true;
                assertEquals(Alert.RISK_LOW, alert.getRisk());
            } else if (alert.getName().contains("Database")) {
                hasDbError = true;
                assertEquals(Alert.RISK_MEDIUM, alert.getRisk());
            } else if (alert.getName().contains("Debug")) {
                hasDebugInfo = true;
                assertEquals(Alert.RISK_INFO, alert.getRisk());
            }
        }
        
        assertTrue(hasUrlInfo, "Should include URL information disclosure example");
        assertTrue(hasServerInfo, "Should include server version disclosure example");
        assertTrue(hasDbError, "Should include database error disclosure example");
        assertTrue(hasDebugInfo, "Should include debug information disclosure example");
    }

    @Test
    public void shouldProvideConsistentExampleAlertStructure() {
        // Given
        TestSqlInjectionPlugin sqlPlugin = new TestSqlInjectionPlugin();
        TestXssPlugin xssPlugin = new TestXssPlugin();
        TestInfoDisclosureScanner infoScanner = new TestInfoDisclosureScanner();

        // When
        List<Alert> sqlExamples = sqlPlugin.getExampleAlerts();
        List<Alert> xssExamples = xssPlugin.getExampleAlerts();
        List<Alert> infoExamples = infoScanner.getExampleAlerts();

        // Then
        validateAlertStructure(sqlExamples);
        validateAlertStructure(xssExamples);
        validateAlertStructure(infoExamples);
    }

    private void validateAlertStructure(List<Alert> alerts) {
        assertNotNull(alerts);
        assertTrue(alerts.size() > 0);
        
        for (Alert alert : alerts) {
            // All example alerts should have basic required fields
            assertNotNull(alert.getName(), "Alert name should not be null");
            assertTrue(alert.getName().length() > 0, "Alert name should not be empty");
            
            assertNotNull(alert.getDescription(), "Alert description should not be null");
            assertTrue(alert.getDescription().length() > 0, "Alert description should not be empty");
            
            assertNotNull(alert.getUri(), "Alert URI should not be null");
            assertTrue(alert.getUri().length() > 0, "Alert URI should not be empty");
            
            // Risk should be valid
            assertTrue(Alert.isValidRisk(alert.getRisk()), "Alert risk should be valid");
            
            // Confidence should be valid
            assertTrue(Alert.isValidConfidence(alert.getConfidence()), "Alert confidence should be valid");
            
            // Plugin ID should be positive
            assertTrue(alert.getPluginId() > 0, "Plugin ID should be positive");
            
            // Example alerts should not be persisted
            assertEquals(-1, alert.getAlertId(), "Example alerts should not have database IDs");
        }
    }
}