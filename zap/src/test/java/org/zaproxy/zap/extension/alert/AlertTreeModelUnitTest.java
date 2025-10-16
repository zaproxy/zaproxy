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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.zaproxy.zap.WithConfigsTest;

public class AlertTreeModelUnitTest extends WithConfigsTest {

    private AlertTreeModel atModel;

    @BeforeEach
    void setUp() throws Exception {
        WithConfigsTest.setUpConstantMessages();
        atModel = new AlertTreeModel();
    }

    @Test
    void shouldAddUniqueAlerts() {
        // Given
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_HIGH,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.net",
                        "https://www.example.net",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a3 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a3);
        atModel.addPath(a2);

        // Then

        assertEquals(
                """
                - Alerts
                  - High: Alert A
                    - :https://www.example.com
                  - Medium: Alert A
                    - :https://www.example.com
                    - :https://www.example.net
                """,
                TextAlertTree.toString(atModel));

        assertEquals(a1, atModel.getRoot().getChildAt(0).getChildAt(0).getAlert());
        assertEquals(a3, atModel.getRoot().getChildAt(1).getChildAt(0).getAlert());
        assertEquals(a2, atModel.getRoot().getChildAt(1).getChildAt(1).getAlert());
    }

    @Test
    void shouldAddDuplicateAlerts() {
        // Given
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=2",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a3 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=3",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);

        // Then
        assertEquals(1, atModel.getRoot().getChildCount());

        // Only child - Medium risk
        assertEquals("Alert A", atModel.getRoot().getChildAt(0).getNodeName());
        assertEquals(Alert.RISK_MEDIUM, atModel.getRoot().getChildAt(0).getRisk());
        assertEquals(1, atModel.getRoot().getChildAt(0).getChildCount());

        assertEquals(
                ":https://www.example.com(a)",
                atModel.getRoot().getChildAt(0).getChildAt(0).getNodeName());
        assertEquals(Alert.RISK_MEDIUM, atModel.getRoot().getChildAt(0).getChildAt(0).getRisk());
    }

    @Test
    void shouldFindDuplicateAlerts() {
        // Given
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=2",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a3 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=3",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);

        AlertNode an1 = atModel.getAlertNode(a1);
        AlertNode an2 = atModel.getAlertNode(a2);
        AlertNode an3 = atModel.getAlertNode(a3);

        // Then
        assertEquals(":https://www.example.com(a)", an1.getNodeName());
        assertEquals(":https://www.example.com(a)", an2.getNodeName());
        assertEquals(":https://www.example.com(a)", an3.getNodeName());
    }

    @Test
    void shouldChangeDuplicateAlerts() {
        ExtensionHistory extHistory = mock(ExtensionHistory.class);
        given(extensionLoader.getExtension(ExtensionHistory.class)).willReturn(extHistory);

        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=2",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a3 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=3",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);
        a1.setRisk(Alert.RISK_HIGH);
        atModel.updatePath(a1);

        // Then
        assertEquals(1, atModel.getRoot().getChildCount());

        // Only child - Medium risk
        assertEquals("Alert A", atModel.getRoot().getChildAt(0).getNodeName());
        assertEquals(Alert.RISK_HIGH, atModel.getRoot().getChildAt(0).getRisk());
        assertEquals(1, atModel.getRoot().getChildAt(0).getChildCount());

        assertEquals(
                ":https://www.example.com(a)",
                atModel.getRoot().getChildAt(0).getChildAt(0).getNodeName());
        assertEquals(Alert.RISK_HIGH, atModel.getRoot().getChildAt(0).getChildAt(0).getRisk());
    }

    @Test
    void shouldDeleteUniqueAlert() {
        // Given
        ExtensionHistory extHistory = mock(ExtensionHistory.class);
        given(extensionLoader.getExtension(ExtensionHistory.class)).willReturn(extHistory);

        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com/a2",
                        "https://www.example.com/a2",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a3 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.net",
                        "https://www.example.net",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);

        atModel.deletePath(a1);

        // Then
        assertEquals(1, atModel.getRoot().getChildCount());

        assertEquals(
                """
                - Alerts
                  - Medium: Alert A
                    - :https://www.example.com/a2
                    - :https://www.example.net
                """,
                TextAlertTree.toString(atModel));

        assertEquals(a2, atModel.getRoot().getChildAt(0).getChildAt(0).getAlert());
        assertEquals(a3, atModel.getRoot().getChildAt(0).getChildAt(1).getAlert());
    }

    @Test
    void shouldChangeUniqueAlert() {
        // Given
        ExtensionHistory extHistory = mock(ExtensionHistory.class);
        given(extensionLoader.getExtension(ExtensionHistory.class)).willReturn(extHistory);

        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.net",
                        "https://www.example.net",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a3 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com/a2",
                        "https://www.example.com/a2",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);

        a1.setRisk(Alert.RISK_HIGH);
        atModel.updatePath(a1);

        // Then
        assertEquals(
                """
                - Alerts
                  - High: Alert A
                    - :https://www.example.com/a1
                  - Medium: Alert A
                    - :https://www.example.com/a2
                    - :https://www.example.net
                """,
                TextAlertTree.toString(atModel));

        assertEquals(a1, atModel.getRoot().getChildAt(0).getChildAt(0).getAlert());
        assertEquals(a3, atModel.getRoot().getChildAt(1).getChildAt(0).getAlert());
        assertEquals(a2, atModel.getRoot().getChildAt(1).getChildAt(1).getAlert());
    }

    @Test
    void shouldDeleteNodeWhenNoAlertsLeft() {
        // Given
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=2",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a3 =
                newAlert(
                        1,
                        2,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=3",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);

        atModel.deletePath(a1);
        atModel.deletePath(a3);
        atModel.deletePath(a2);

        // Then
        assertEquals(0, atModel.getRoot().getChildCount());
    }

    private static Alert newAlert(
            int pluginId,
            int id,
            String name,
            String nodeName,
            String uri,
            int risk,
            int confidence) {
        Alert alert = new Alert(pluginId, risk, confidence, name);
        alert.setUri(uri);
        alert.setAlertId(id);
        alert.setNodeName(nodeName);
        return alert;
    }
}
