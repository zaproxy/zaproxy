/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.parosproxy.paros.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.network.HttpMessage;

class HistoryReferenceUnitTest {

    private HistoryReference historyReference;

    @BeforeEach
    void setUp() throws Exception {
        int historyId = 42;
        long sessionId = 1L;

        Session session = mock(Session.class);
        given(session.getSessionId()).willReturn(sessionId);

        RecordHistory recordHistory = new RecordHistory();
        recordHistory.setHistoryId(historyId);
        recordHistory.setHistoryType(HistoryReference.TYPE_PROXIED);
        recordHistory.setSessionId(sessionId);

        TableHistory tableHistory = mock(TableHistory.class);
        given(
                        tableHistory.write(
                                eq(sessionId),
                                eq(HistoryReference.TYPE_PROXIED),
                                any(HttpMessage.class)))
                .willReturn(recordHistory);
        HistoryReference.setTableHistory(tableHistory);

        TableAlert tableAlert = mock(TableAlert.class);
        given(tableAlert.getAlertsBySourceHistoryId(anyInt())).willReturn(Collections.emptyList());
        HistoryReference.setTableAlert(tableAlert);

        historyReference =
                new HistoryReference(
                        session,
                        HistoryReference.TYPE_PROXIED,
                        new HttpMessage(new URI("https://www.example.com/", true)));
    }

    @AfterEach
    void cleanUp() {
        HistoryReference.setTableHistory(null);
        HistoryReference.setTableAlert(null);
    }

    @Test
    void shouldKeepSemanticallyEqualAlertsWithDifferentIds() {
        // Given
        Alert alert1 = createAlert(1);
        Alert alert2 = createAlert(2);
        alert1.setHistoryRef(historyReference);
        alert2.setHistoryRef(historyReference);

        // When
        historyReference.addAlert(alert1);
        historyReference.addAlert(alert2);

        // Then
        assertThat(historyReference.getAlerts(), hasSize(2));
        assertThat(
                historyReference.getAlerts().stream().map(Alert::getAlertId).toList(),
                containsInAnyOrder(1, 2));
    }

    @Test
    void shouldDeleteSemanticallyEqualAlertById() {
        // Given
        Alert alert1 = createAlert(1);
        Alert alert2 = createAlert(2);
        alert1.setHistoryRef(historyReference);
        alert2.setHistoryRef(historyReference);
        historyReference.addAlert(alert1);
        historyReference.addAlert(alert2);

        // When
        historyReference.deleteAlert(alert1);

        // Then
        assertThat(historyReference.getAlerts(), hasSize(1));
        assertThat(
                historyReference.getAlerts().stream().map(Alert::getAlertId).toList(),
                containsInAnyOrder(2));
    }

    private static Alert createAlert(int alertId) {
        Alert alert = new Alert(10001, Alert.RISK_LOW, Alert.CONFIDENCE_MEDIUM, "Test Alert");
        alert.setAlertId(alertId);
        alert.setUri("https://www.example.com/");
        alert.setNodeName("https://www.example.com/");
        return alert;
    }
}
