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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.WithConfigsTest;

public class AlertTreeModelUnitTest extends WithConfigsTest {

    private AlertTreeModel atModel;
    private ExtensionAlert extAlert;

    @BeforeEach
    void setUp() throws Exception {
        WithConfigsTest.setUpConstantMessages();
        extAlert = mock(ExtensionAlert.class);
        atModel = new AlertTreeModel(extAlert);
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
        Alert a4 =
                newAlert(
                        1,
                        "1-2",
                        3,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a3);
        atModel.addPath(a2);
        atModel.addPath(a4);

        // Then

        assertEquals(
                """
                - Alerts
                  - High: Alert A
                    - GET:https://www.example.com
                  - Medium: Alert A
                    - GET:https://www.example.com
                    - GET:https://www.example.net
                  - Medium: Alert A
                    - GET:https://www.example.com
                """,
                TextAlertTree.toString(atModel));

        assertEquals(a1, atModel.getRoot().getChildAt(0).getChildAt(0).getAlert());
        assertEquals(a3, atModel.getRoot().getChildAt(1).getChildAt(0).getAlert());
        assertEquals(a2, atModel.getRoot().getChildAt(1).getChildAt(1).getAlert());
        assertEquals(a4, atModel.getRoot().getChildAt(2).getChildAt(0).getAlert());
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
                "GET:https://www.example.com(a)",
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
        Alert a4 =
                newAlert(
                        1,
                        "1-2",
                        3,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=4",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);
        atModel.addPath(a4);

        AlertNode an1 = atModel.getAlertNode(a1);
        AlertNode an2 = atModel.getAlertNode(a2);
        AlertNode an3 = atModel.getAlertNode(a3);

        // Then
        assertEquals("GET:https://www.example.com(a)", an1.getNodeName());
        assertEquals("GET:https://www.example.com(a)", an2.getNodeName());
        assertEquals("GET:https://www.example.com(a)", an3.getNodeName());
        assertEquals("GET:https://www.example.com(a)", atModel.getAlertNode(a4).getNodeName());
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
        Alert a4 =
                newAlert(
                        1,
                        "1-2",
                        3,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=4",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);
        atModel.addPath(a4);
        a1.setRisk(Alert.RISK_HIGH);
        atModel.updatePath(a1);

        // Then
        assertEquals(2, atModel.getRoot().getChildCount());

        // Only child - Medium risk
        assertEquals("Alert A", atModel.getRoot().getChildAt(0).getNodeName());
        assertEquals(Alert.RISK_HIGH, atModel.getRoot().getChildAt(0).getRisk());
        assertEquals(1, atModel.getRoot().getChildAt(0).getChildCount());

        assertEquals(
                "GET:https://www.example.com(a)",
                atModel.getRoot().getChildAt(0).getChildAt(0).getNodeName());
        assertEquals(Alert.RISK_HIGH, atModel.getRoot().getChildAt(0).getChildAt(0).getRisk());

        assertEquals(
                "GET:https://www.example.com(a)",
                atModel.getRoot().getChildAt(1).getChildAt(0).getNodeName());
        assertEquals(Alert.RISK_MEDIUM, atModel.getRoot().getChildAt(1).getChildAt(0).getRisk());
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
        Alert a4 =
                newAlert(
                        1,
                        "1-2",
                        3,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);
        atModel.addPath(a4);

        atModel.deletePath(a1);

        // Then
        assertEquals(2, atModel.getRoot().getChildCount());

        assertEquals(
                """
                - Alerts
                  - Medium: Alert A
                    - GET:https://www.example.com/a2
                    - GET:https://www.example.net
                  - Medium: Alert A
                    - GET:https://www.example.com/a1
                """,
                TextAlertTree.toString(atModel));

        assertEquals(a2, atModel.getRoot().getChildAt(0).getChildAt(0).getAlert());
        assertEquals(a3, atModel.getRoot().getChildAt(0).getChildAt(1).getAlert());
        assertEquals(a4, atModel.getRoot().getChildAt(1).getChildAt(0).getAlert());
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
        Alert a4 =
                newAlert(
                        1,
                        "1-2",
                        3,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);
        atModel.addPath(a4);

        a1.setRisk(Alert.RISK_HIGH);
        atModel.updatePath(a1);

        // Then
        assertEquals(
                """
                - Alerts
                  - High: Alert A
                    - GET:https://www.example.com/a1
                  - Medium: Alert A
                    - GET:https://www.example.com/a2
                    - GET:https://www.example.net
                  - Medium: Alert A
                    - GET:https://www.example.com/a1
                """,
                TextAlertTree.toString(atModel));

        assertEquals(a1, atModel.getRoot().getChildAt(0).getChildAt(0).getAlert());
        assertEquals(a3, atModel.getRoot().getChildAt(1).getChildAt(0).getAlert());
        assertEquals(a2, atModel.getRoot().getChildAt(1).getChildAt(1).getAlert());
        assertEquals(a4, atModel.getRoot().getChildAt(2).getChildAt(0).getAlert());
    }

    @Test
    void shouldNotAddNodeWhenAlertOverSystemicLimit() {
        // Given
        given(extAlert.isOverSystemicLimit(any(), any())).willReturn(true);
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);

        // Then
        assertEquals(0, atModel.getRoot().getChildCount());
    }

    @Test
    void shouldNotLeaveOldNodeWhenUpdatingAlertOverSystemicLimit() {
        // Given
        ExtensionHistory extHistory = mock(ExtensionHistory.class);
        given(extensionLoader.getExtension(ExtensionHistory.class)).willReturn(extHistory);
        given(extAlert.isOverSystemicLimit(any(), any()))
                .willAnswer(
                        invocation -> {
                            Alert alert = invocation.getArgument(0);
                            return alert.getUri().endsWith("/a2");
                        });
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com/a2",
                        "https://www.example.com/a2",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        atModel.addPath(a1);
        updatePathToFalsePositive(a1);

        atModel.addPath(a2);
        assertNoEmptyGroupNodes(atModel);

        // When
        updatePathToFalsePositive(a2);

        // Then
        assertEquals(
                """
                - Alerts
                  - False Positive: Alert A
                    - GET:https://www.example.com/a1
                """,
                TextAlertTree.toString(atModel));
        assertNoEmptyGroupNodes(atModel);
    }

    @Test
    void shouldNotApplySystemicLimitToFilteredTreeModel() {
        // Given - the limit is reached
        given(extAlert.isOverSystemicLimit(any(), any())).willReturn(true);
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);

        // When - the alert is added to the filtered tree, which is a subset of the main tree
        AlertTreeModel filteredModel = new AlertTreeModel(extAlert, false);
        filteredModel.addPath(a1);

        // Then - it is added, the limit is applied to the main tree model only
        assertEquals(1, filteredModel.getRoot().getChildCount());
        assertEquals(1, filteredModel.getRoot().getChildAt(0).getChildCount());

        // ...while the main tree model does apply it
        atModel.addPath(a1);
        assertEquals(0, atModel.getRoot().getChildCount());
    }

    @Test
    void shouldNotAddUpdatedAlertToNewGroupWhenOverSystemicLimit() {
        // Given - the alert is shown but the group of alerts it now belongs to is over the systemic
        // limit
        given(extAlert.isOverSystemicLimit(any(), any()))
                .willAnswer(
                        invocation ->
                                ((Alert) invocation.getArgument(0)).getConfidence()
                                        != Alert.CONFIDENCE_MEDIUM);
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        atModel.addPath(a1);

        // When - the alert is changed to a false positive, as an alert filter would do
        updatePathToFalsePositive(a1);

        // Then - the alert is not shown, the systemic limit is still honoured, and no node is left
        // behind in the tree
        assertEquals(0, atModel.getRoot().getChildCount());
    }

    @Test
    void shouldNotLeaveOldNodeWhenUpdatingDeDuplicatedAlerts() {
        // Given - two equivalent alerts (e.g. the same URL scanned twice), only one is added
        ExtensionHistory extHistory = mock(ExtensionHistory.class);
        given(extensionLoader.getExtension(ExtensionHistory.class)).willReturn(extHistory);

        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        atModel.addPath(a1);
        atModel.addPath(a2);

        // When - both alerts are changed to false positives and the tree updated
        updatePathToFalsePositive(a1);
        updatePathToFalsePositive(a2);

        // Then - only the false positive node is left, no node with the old risk
        assertEquals(
                """
                - Alerts
                  - False Positive: Alert A
                    - GET:https://www.example.com
                """,
                TextAlertTree.toString(atModel));
        assertNoEmptyGroupNodes(atModel);
    }

    @Test
    void shouldNotLeaveOldNodeWhenAlertOverSystemicLimitIsNotUpdated() {
        // Given - the first alert is added and changed to a false positive, the second alert is
        // over the systemic limit so no node is added for it (and no alert added event is published
        // for it, i.e. it is never updated in the tree)
        given(extAlert.isOverSystemicLimit(any(), any()))
                .willAnswer(
                        invocation -> {
                            Alert alert = invocation.getArgument(0);
                            return alert.getUri().endsWith("/a2");
                        });
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com/a1",
                        "https://www.example.com/a1",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        Alert a2 =
                newAlert(
                        1,
                        1,
                        "Alert A",
                        "https://www.example.com/a2",
                        "https://www.example.com/a2",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        atModel.addPath(a1);
        updatePathToFalsePositive(a1);
        atModel.addPath(a2);

        // Then - no node with the old risk is left in the tree, the node that is shown represents
        // the false positive alert
        assertEquals(
                """
                - Alerts
                  - False Positive: Alert A
                    - GET:https://www.example.com/a1
                """,
                TextAlertTree.toString(atModel));
        assertNoEmptyGroupNodes(atModel);
        assertEquals(
                Alert.CONFIDENCE_FALSE_POSITIVE,
                atModel.getRoot().getChildAt(0).getAlert().getConfidence());
    }

    @Test
    void shouldAddFalsePositiveNodeWhenAlertOverSystemicLimitChangedToFalsePositive() {
        // Given - the alert is over the systemic limit so no node is added for it
        given(extAlert.isOverSystemicLimit(any(), any()))
                .willAnswer(
                        invocation -> {
                            Alert alert = invocation.getArgument(0);
                            return alert.getConfidence() != Alert.CONFIDENCE_FALSE_POSITIVE;
                        });
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        atModel.addPath(a1);

        // When - the alert filter changes it to a false positive
        updatePathToFalsePositive(a1);

        // Then - the false positive node is added, no node with the old risk
        assertEquals(
                """
                - Alerts
                  - False Positive: Alert A
                    - GET:https://www.example.com
                """,
                TextAlertTree.toString(atModel));
        assertNoEmptyGroupNodes(atModel);
    }

    @Test
    void shouldRemoveEmptyGroupNodeWhenAlertUpdated() {
        // Given - a group node without alerts, which should not be in the tree
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        atModel.addPath(a1);
        atModel.getRoot().getChildAt(0).remove(0);

        // When
        updatePathToFalsePositive(a1);

        // Then - the group node with the old risk is replaced by the false positive node
        assertEquals(
                """
                - Alerts
                  - False Positive: Alert A
                    - GET:https://www.example.com
                """,
                TextAlertTree.toString(atModel));
        assertNoEmptyGroupNodes(atModel);
    }

    @Test
    void shouldRemoveEmptyGroupNodeWhenAlertDeleted() {
        // Given - a group node without alerts, which should not be in the tree
        Alert a1 =
                newAlert(
                        1,
                        0,
                        "Alert A",
                        "https://www.example.com",
                        "https://www.example.com",
                        Alert.RISK_LOW,
                        Alert.CONFIDENCE_MEDIUM);
        atModel.addPath(a1);
        atModel.getRoot().getChildAt(0).remove(0);

        // When
        atModel.deletePath(a1);

        // Then - the group node without alerts is removed and no other nodes are affected
        assertEquals(0, atModel.getRoot().getChildCount());
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
        Alert a4 =
                newAlert(
                        1,
                        "1-2",
                        3,
                        "Alert A",
                        "https://www.example.com(a)",
                        "https://www.example.com?a=4",
                        Alert.RISK_MEDIUM,
                        Alert.CONFIDENCE_MEDIUM);

        // When
        atModel.addPath(a1);
        atModel.addPath(a2);
        atModel.addPath(a3);
        atModel.addPath(a4);

        atModel.deletePath(a1);
        atModel.deletePath(a3);
        atModel.deletePath(a2);

        // Then
        assertEquals(1, atModel.getRoot().getChildCount());
        assertEquals(a4, atModel.getRoot().getChildAt(0).getChildAt(0).getAlert());
    }

    /**
     * Updates the given alert in the tree as the alert filter does, i.e. with a new alert instance
     * with false positive confidence, calling the update twice as done by {@code ExtensionAlert}
     * and the {@code alertFilters} add-on.
     */
    private void updatePathToFalsePositive(Alert alert) {
        atModel.updatePath(falsePositive(alert));
        atModel.updatePath(falsePositive(alert));
    }

    private static Alert falsePositive(Alert alert) {
        Alert fp =
                new Alert(
                        alert.getPluginId(),
                        alert.getRisk(),
                        Alert.CONFIDENCE_FALSE_POSITIVE,
                        alert.getName());
        fp.setAlertRef(alert.getAlertRef());
        fp.setUri(alert.getUri());
        fp.setNodeName(alert.getNodeName());
        fp.setAlertId(alert.getAlertId());
        fp.setHistoryRef(alert.getHistoryRef());
        return fp;
    }

    /** Asserts that every group node (i.e. every node below the root) has alerts. */
    private static void assertNoEmptyGroupNodes(AlertTreeModel model) {
        AlertNode root = model.getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            AlertNode groupNode = root.getChildAt(i);
            assertTrue(
                    groupNode.getChildCount() > 0,
                    "Group node without alerts in the tree: " + groupNode.getNodeName());
        }
    }

    private static Alert newAlert(
            int pluginId,
            int id,
            String name,
            String nodeName,
            String uri,
            int risk,
            int confidence) {
        return newAlert(pluginId, null, id, name, nodeName, uri, risk, confidence);
    }

    private static Alert newAlert(
            int pluginId,
            String alertRef,
            int id,
            String name,
            String nodeName,
            String uri,
            int risk,
            int confidence) {
        Alert alert = new Alert(pluginId, risk, confidence, name);
        if (alertRef != null) {
            alert.setAlertRef(alertRef);
        }
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
