/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.parosproxy.paros.db.paros;

import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.TableAlert;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTableAlert extends ParosAbstractTable implements TableAlert {

    private record AlertItem(
            int scanId,
            int pluginId,
            String alert,
            int risk,
            int confidence,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String reference,
            String evidence,
            int cweId,
            int wascId,
            int historyId,
            int sourceHistoryId,
            int sourceId,
            String alertRef,
            String inputVector,
            String nodeName) {

        public RecordAlert toRecordAlert(int alertId) {
            return new RecordAlert(
                    alertId,
                    scanId,
                    pluginId,
                    alert,
                    risk,
                    confidence,
                    description,
                    uri,
                    param,
                    attack,
                    otherInfo,
                    solution,
                    reference,
                    evidence,
                    cweId,
                    wascId,
                    historyId,
                    sourceHistoryId,
                    sourceId,
                    alertRef,
                    inputVector,
                    nodeName);
        }
    }

    private final AtomicInteger nextId = new AtomicInteger();
    private final InMemoryDb<Integer, AlertItem> db = new InMemoryDb<>();

    public InMemoryTableAlert() {}

    @Override
    protected void reconnect(Connection conn) throws DatabaseException {}

    @Override
    public synchronized RecordAlert read(int alertId) throws DatabaseException {
        AlertItem item = db.get(alertId);
        return item != null ? item.toRecordAlert(alertId) : null;
    }

    @Override
    public synchronized RecordAlert write(
            int scanId,
            int pluginId,
            String alert,
            int risk,
            int confidence,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String reference,
            String evidence,
            int cweId,
            int wascId,
            int historyId,
            int sourceHistoryId,
            int sourceId,
            String alertRef,
            String inputVector,
            String nodeName)
            throws DatabaseException {

        AlertItem item =
                new AlertItem(
                        scanId,
                        pluginId,
                        alert,
                        risk,
                        confidence,
                        description,
                        uri,
                        param,
                        attack,
                        otherInfo,
                        solution,
                        reference,
                        evidence,
                        cweId,
                        wascId,
                        historyId,
                        sourceHistoryId,
                        sourceId,
                        alertRef,
                        inputVector,
                        nodeName);
        int id = nextId.incrementAndGet();
        db.put(id, item);
        return item.toRecordAlert(id);
    }

    @Override
    public synchronized void deleteAlert(int alertId) throws DatabaseException {
        db.remove(alertId);
    }

    @Override
    public synchronized int deleteAllAlerts() throws DatabaseException {
        int size = db.size();
        db.clear();
        return size;
    }

    @Override
    public synchronized void update(
            int alertId,
            String alert,
            int risk,
            int confidence,
            String description,
            String uri,
            String param,
            String attack,
            String otherInfo,
            String solution,
            String reference,
            String evidence,
            int cweId,
            int wascId,
            int sourceHistoryId,
            String inputVector,
            String nodeName)
            throws DatabaseException {

        AlertItem oldItem = db.get(alertId);
        if (oldItem == null) {
            return;
        }

        AlertItem newItem =
                new AlertItem(
                        oldItem.scanId,
                        oldItem.pluginId,
                        alert,
                        risk,
                        confidence,
                        description,
                        uri,
                        param,
                        attack,
                        otherInfo,
                        solution,
                        reference,
                        evidence,
                        cweId,
                        wascId,
                        oldItem.historyId,
                        sourceHistoryId,
                        oldItem.sourceId,
                        oldItem.alertRef,
                        inputVector,
                        nodeName);

        db.put(alertId, newItem);
    }

    @Override
    public synchronized List<RecordAlert> getAlertsBySourceHistoryId(int historyId) {
        List<RecordAlert> result = new ArrayList<>();
        db.collect(
                (id, item) -> {
                    if (item.sourceHistoryId == historyId) {
                        result.add(item.toRecordAlert(id));
                    }
                });
        return result;
    }

    @Override
    public synchronized Vector<Integer> getAlertList() {
        Vector<Integer> v = new Vector<>();
        db.collect((id, item) -> v.add(id));
        return v;
    }
}
