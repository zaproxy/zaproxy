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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Alert;

public class AlertSetUnitTest {

    private AlertSet alertSet;

    @BeforeEach
    void setUp() throws Exception {
        alertSet = new AlertSet();
    }

    @Test
    void shouldAddUniqueAlerts() {
        // Given
        Alert a1 = getAlert(0, "Test Alert", "https://www.example.com/a/");
        Alert a2 = getAlert(1, "Test Alert", "https://www.example.com/b/");
        Alert a3 = getAlert(2, "Test Alert", "https://www.example.com/c/");
        Alert a4 = getAlert(3, "Test Alert", "https://www.example.com/d/");
        Alert a5 = getAlert(4, "Test Alert", "https://www.example.com/e/");
        List<Alert> alerts = List.of(a3, a4, a5);

        // When
        boolean add1 = alertSet.add(a1);
        boolean add2 = alertSet.add(a2);
        boolean add3 = alertSet.addAll(alerts);

        // Then
        assertTrue(add1);
        assertTrue(add2);
        assertTrue(add3);
        assertEquals(5, alertSet.size());
        assertTrue(alertSet.hasAlert(a1.getAlertId()));
        assertTrue(alertSet.hasAlert(a2.getAlertId()));
        assertTrue(alertSet.hasAlert(a3.getAlertId()));
        assertTrue(alertSet.hasAlert(a4.getAlertId()));
        assertTrue(alertSet.hasAlert(a5.getAlertId()));
    }

    @Test
    void shouldReplaceExistingAlerts() {
        // Given
        Alert a1 = getAlert(0, "Test Alert", "https://www.example.com/a/");
        Alert a2 = getAlert(1, "Test Alert", "https://www.example.com/b/");
        Alert a3 = getAlert(2, "Test Alert", "https://www.example.com/c/");
        Alert a4 = getAlert(3, "Test Alert", "https://www.example.com/d/");
        Alert a5 = getAlert(4, "Test Alert", "https://www.example.com/e/");
        List<Alert> alerts = List.of(a3, a4, a5);
        alertSet.addAll(List.of(a1, a2, a3, a4, a5));

        // When
        boolean add1 = alertSet.add(a1);
        boolean add2 = alertSet.add(a2);
        boolean add3 = alertSet.addAll(alerts);

        // Then
        assertFalse(add1);
        assertFalse(add2);
        assertFalse(add3);
        assertEquals(5, alertSet.size());
        assertTrue(alertSet.hasAlert(a1.getAlertId()));
        assertTrue(alertSet.hasAlert(a2.getAlertId()));
        assertTrue(alertSet.hasAlert(a3.getAlertId()));
        assertTrue(alertSet.hasAlert(a4.getAlertId()));
        assertTrue(alertSet.hasAlert(a5.getAlertId()));
    }

    @Test
    void shouldRemoveAlerts() {
        // Given
        Alert a1 = getAlert(0, "Test Alert", "https://www.example.com/a/");
        Alert a2 = getAlert(1, "Test Alert", "https://www.example.com/b/");
        Alert a3 = getAlert(2, "Test Alert", "https://www.example.com/c/");
        Alert a4 = getAlert(3, "Test Alert", "https://www.example.com/d/");
        Alert a5 = getAlert(4, "Test Alert", "https://www.example.com/e/");
        List<Alert> alerts = List.of(a4, a5);
        alertSet.addAll(List.of(a1, a2, a3, a4, a5));

        // When
        boolean rem1 = alertSet.remove(a1);
        boolean rem2 = alertSet.removeAll(alerts);
        boolean rem3 = alertSet.remove(a1);
        boolean rem4 = alertSet.removeAll(alerts);

        // Then
        assertTrue(rem1);
        assertTrue(rem2);
        assertFalse(rem3);
        assertFalse(rem4);
        assertEquals(2, alertSet.size());
        assertTrue(alertSet.hasAlert(a2.getAlertId()));
        assertTrue(alertSet.hasAlert(a3.getAlertId()));
    }

    @Test
    void shouldGetAlerts() {
        // Given
        Alert a1 = getAlert(0, "Test Alert", "https://www.example.com/a/");
        Alert a2 = getAlert(1, "Test Alert", "https://www.example.com/b/");

        // When
        Alert get1 = alertSet.get();
        List<Alert> getl1 = alertSet.getAll();
        alertSet.add(a1);
        Alert get2 = alertSet.get();
        List<Alert> getl2 = alertSet.getAll();
        alertSet.add(a2);
        Alert get3 = alertSet.get();
        List<Alert> getl3 = alertSet.getAll();

        // Then
        assertNull(get1);
        assertEquals(0, getl1.size());
        assertEquals(a1, get2);
        assertEquals(1, getl2.size());
        assertEquals(a1, getl2.get(0));
        assertEquals(a1, get2);
        assertNotNull(get3);
        assertEquals(2, getl3.size());
        assertTrue(getl3.contains(a1));
        assertTrue(getl3.contains(a2));
    }

    @Test
    void shouldFindSimilarAlert() {
        // Given
        Alert a1 =
                getAlert(
                        0,
                        "Test Alert",
                        "https://www.example.com/ (test)",
                        "https://www.example.com/?test=1");
        Alert a2 =
                getAlert(
                        1,
                        "Test Alert",
                        "https://www.example.com/ (test)",
                        "https://www.example.com/?test=2");
        Alert a3 =
                getAlert(
                        2,
                        "Test Alert",
                        "https://www.example.com/ (x)",
                        "https://www.example.com/?x=2");

        // When
        alertSet.add(a1);

        // Then
        assertTrue(alertSet.hasSimilar(a2));
        assertFalse(alertSet.hasSimilar(a3));
    }

    @Test
    void shouldClearAlerts() {
        // Given
        Alert a1 = getAlert(0, "Test Alert", "https://www.example.com/a/");
        Alert a2 = getAlert(1, "Test Alert", "https://www.example.com/b/");
        Alert a3 = getAlert(2, "Test Alert", "https://www.example.com/c/");
        Alert a4 = getAlert(3, "Test Alert", "https://www.example.com/d/");
        Alert a5 = getAlert(4, "Test Alert", "https://www.example.com/e/");
        alertSet.addAll(List.of(a1, a2, a3, a4, a5));

        // When
        boolean empty1 = alertSet.isEmpty();
        alertSet.clear();
        boolean empty2 = alertSet.isEmpty();

        // Then
        assertFalse(empty1);
        assertTrue(empty2);
        assertEquals(0, alertSet.size());
    }

    @Test
    void shouldGetHighestRiskAlert() {
        // Given
        Alert a1 =
                getAlert(
                        0,
                        "Test Alert",
                        "https://www.example.com/a/",
                        Alert.RISK_HIGH,
                        Alert.CONFIDENCE_FALSE_POSITIVE);
        Alert a2 =
                getAlert(
                        1,
                        "Test Alert",
                        "https://www.example.com/b/",
                        Alert.RISK_INFO,
                        Alert.CONFIDENCE_HIGH);
        Alert a3 =
                getAlert(
                        2,
                        "Test Alert",
                        "https://www.example.com/c/",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a4 =
                getAlert(
                        3,
                        "Test Alert",
                        "https://www.example.com/d/",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_HIGH);
        Alert a5 =
                getAlert(
                        4,
                        "Test Alert",
                        "https://www.example.com/e/",
                        Alert.RISK_HIGH,
                        Alert.CONFIDENCE_USER_CONFIRMED);

        // When
        Alert high0 = alertSet.getHighestRisk();
        alertSet.add(a1);
        Alert high1 = alertSet.getHighestRisk();
        alertSet.add(a2);
        Alert high2 = alertSet.getHighestRisk();
        alertSet.add(a3);
        Alert high3 = alertSet.getHighestRisk();
        alertSet.add(a4);
        Alert high4 = alertSet.getHighestRisk();
        alertSet.add(a5);
        Alert high5 = alertSet.getHighestRisk();

        // Then
        assertNull(high0);
        assertNull(high1);
        assertEquals(a2, high2);
        assertEquals(a3, high3);
        assertEquals(a4, high4);
        assertEquals(a5, high5);
    }

    private Alert getAlert(int alertId, String name, String nodeName, String uri) {
        Alert a1 = new Alert(0, Alert.RISK_INFO, Alert.CONFIDENCE_MEDIUM, name);
        a1.setUri(uri);
        a1.setNodeName(nodeName);
        a1.setAlertId(alertId);
        return a1;
    }

    private Alert getAlert(int alertId, String name, String uri, int risk, int confidence) {
        Alert a = this.getAlert(alertId, name, uri, uri);
        a.setRisk(risk);
        a.setConfidence(confidence);
        return a;
    }

    private Alert getAlert(int alertId, String name, String uri) {
        return this.getAlert(alertId, name, uri, uri);
    }
}
