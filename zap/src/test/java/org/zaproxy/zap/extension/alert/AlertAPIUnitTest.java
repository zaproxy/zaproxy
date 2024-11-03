/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import net.sf.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.quality.Strictness;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.db.TableAlertTag;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;

/** Unit test for {@link AlertAPI}. */
public class AlertAPIUnitTest {

    private TableAlert tableAlert;
    private TableAlertTag tableAlertTag;
    private ExtensionAlert extensionAlert;

    private AlertAPI api;

    @BeforeEach
    void setUp() {
        WithConfigsTest.setUpConstantMessages();

        extensionAlert = mock(ExtensionAlert.class);

        Model model = mock(Model.class, withSettings().strictness(Strictness.LENIENT));
        Model.setSingletonForTesting(model);

        Database db = mock(Database.class);
        given(model.getDb()).willReturn(db);
        tableAlert = mock(TableAlert.class);
        given(db.getTableAlert()).willReturn(tableAlert);
        tableAlertTag = mock(TableAlertTag.class);
        given(db.getTableAlertTag()).willReturn(tableAlertTag);

        api = new AlertAPI(extensionAlert);
    }

    @Test
    void shouldHavePrefix() throws Exception {
        // Given / When
        String prefix = api.getPrefix();
        // Then
        assertThat(prefix, is(equalTo("alert")));
    }

    @Test
    void shouldReturnAlertData() throws Exception {
        // Given
        String name = "alert";
        JSONObject params = new JSONObject();
        int alertId = 1;
        params.put("id", alertId);
        RecordAlert recordAlert = mock(RecordAlert.class);
        given(recordAlert.getAlert()).willReturn("name");

        given(recordAlert.getAlertId()).willReturn(alertId);
        given(recordAlert.getPluginId()).willReturn(1234);
        given(recordAlert.getAlert()).willReturn("Alert Name");
        given(recordAlert.getRisk()).willReturn(1);
        given(recordAlert.getConfidence()).willReturn(2);
        given(recordAlert.getDescription()).willReturn("Alert Description");
        given(recordAlert.getUri()).willReturn("uri");
        given(recordAlert.getParam()).willReturn("param");
        given(recordAlert.getAttack()).willReturn("attack");
        given(recordAlert.getOtherInfo()).willReturn("other info");
        given(recordAlert.getSolution()).willReturn("solution");
        given(recordAlert.getReference()).willReturn("reference");
        given(recordAlert.getHistoryId()).willReturn(123);
        given(recordAlert.getSourceHistoryId()).willReturn(1234);
        given(recordAlert.getEvidence()).willReturn("evidence");
        given(recordAlert.getInputVector()).willReturn("input Vector");
        given(recordAlert.getCweId()).willReturn(10);
        given(recordAlert.getWascId()).willReturn(11);
        given(recordAlert.getSourceId()).willReturn(2);
        given(recordAlert.getAlertRef()).willReturn("1234-1");

        given(tableAlert.read(alertId)).willReturn(recordAlert);
        // When
        ApiResponse response = api.handleApiView(name, params);
        // Then
        assertThat(response.getName(), is(equalTo(name)));
        assertThat(response, is(instanceOf(ApiResponseElement.class)));
        assertThat(
                response.toJSON().toString(),
                is(
                        equalTo(
                                "{\"alert\":{\"sourceid\":\"2\",\"other\":\"other info\",\"method\":\"\",\"evidence\":\"evidence\",\"pluginId\":\"1234\",\"cweid\":\"10\",\"confidence\":\"Medium\",\"sourceMessageId\":1234,\"wascid\":\"11\",\"description\":\"Alert Description\",\"messageId\":\"123\",\"inputVector\":\"input Vector\",\"url\":\"uri\",\"tags\":{},\"reference\":\"reference\",\"solution\":\"solution\",\"alert\":\"Alert Name\",\"param\":\"param\",\"attack\":\"attack\",\"name\":\"Alert Name\",\"risk\":\"Low\",\"id\":\"1\",\"alertRef\":\"1234-1\"}}")));
    }
}
