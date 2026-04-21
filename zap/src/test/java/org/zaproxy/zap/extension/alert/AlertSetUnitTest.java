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

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(add1).isTrue();
        assertThat(add2).isTrue();
        assertThat(add3).isTrue();
        assertThat(alertSet.size()).isEqualTo(5);
        assertThat(alertSet.hasAlert(a1.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a2.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a3.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a4.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a5.getAlertId())).isTrue();
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
        assertThat(add1).isFalse();
        assertThat(add2).isFalse();
        assertThat(add3).isFalse();
        assertThat(alertSet.size()).isEqualTo(5);
        assertThat(alertSet.hasAlert(a1.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a2.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a3.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a4.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a5.getAlertId())).isTrue();
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
        assertThat(rem1).isTrue();
        assertThat(rem2).isTrue();
        assertThat(rem3).isFalse();
        assertThat(rem4).isFalse();
        assertThat(alertSet.size()).isEqualTo(2);
        assertThat(alertSet.hasAlert(a2.getAlertId())).isTrue();
        assertThat(alertSet.hasAlert(a3.getAlertId())).isTrue();
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
        assertThat(get1).isNull();
        assertThat(getl1).hasSize(0);
        assertThat(get2).isEqualTo(a1);
        assertThat(getl2).hasSize(1);
        assertThat(getl2.get(0)).isEqualTo(a1);
        assertThat(get2).isEqualTo(a1);
        assertThat(get3).isNotNull();
        assertThat(getl3).hasSize(2);
        assertThat(getl3.contains(a1)).isTrue();
        assertThat(getl3.contains(a2)).isTrue();
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
        assertThat(alertSet.hasSimilar(a2)).isTrue();
        assertThat(alertSet.hasSimilar(a3)).isFalse();
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
        assertThat(empty1).isFalse();
        assertThat(empty2).isTrue();
        assertThat(alertSet.size()).isEqualTo(0);
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
        assertThat(high0).isNull();
        assertThat(high1).isNull();
        assertThat(high2).isEqualTo(a2);
        assertThat(high3).isEqualTo(a3);
        assertThat(high4).isEqualTo(a4);
        assertThat(high5).isEqualTo(a5);
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
