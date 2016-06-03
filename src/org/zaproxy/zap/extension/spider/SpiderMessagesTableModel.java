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
package org.zaproxy.zap.extension.spider;

import java.awt.EventQueue;

import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.extension.alert.AlertEventPublisher;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableModel;

class SpiderMessagesTableModel extends DefaultHistoryReferencesTableModel {

    private static final long serialVersionUID = 1093393768186896931L;

    private final ExtensionHistory extensionHistory;
    private AlertEventConsumer alertEventConsumer;

    public SpiderMessagesTableModel() {
        this(true);
    }

    public SpiderMessagesTableModel(boolean createAlertEventConsumer) {
        super(new Column[] {
                Column.HREF_ID,
                Column.REQUEST_TIMESTAMP,
                Column.RESPONSE_TIMESTAMP,
                Column.METHOD,
                Column.URL,
                Column.STATUS_CODE,
                Column.STATUS_REASON,
                Column.RTT,
                Column.SIZE_REQUEST_HEADER,
                Column.SIZE_REQUEST_BODY,
                Column.SIZE_RESPONSE_HEADER,
                Column.SIZE_RESPONSE_BODY,
                Column.HIGHEST_ALERT,
                Column.TAGS });

        if (createAlertEventConsumer) {
            alertEventConsumer = new AlertEventConsumer();
            extensionHistory = Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);
            ZAP.getEventBus().registerConsumer(alertEventConsumer, AlertEventPublisher.getPublisher().getPublisherName());
        } else {
            alertEventConsumer = null;
            extensionHistory = null;
        }
    }

    @Override
    public void addHistoryReference(HistoryReference historyReference) {
        HistoryReference latestHistoryReference = historyReference;
        if (extensionHistory != null) {
            latestHistoryReference = extensionHistory.getHistoryReference(historyReference.getHistoryId());
        }
        super.addHistoryReference(latestHistoryReference);
    }

    @Override
    public void clear() {
        super.clear();

        if (alertEventConsumer != null) {
            ZAP.getEventBus().unregisterConsumer(alertEventConsumer, AlertEventPublisher.getPublisher().getPublisherName());
            alertEventConsumer = null;
        }
    }

    private class AlertEventConsumer implements EventConsumer {

        @Override
        public void eventReceived(Event event) {
            switch (event.getEventType()) {
            case AlertEventPublisher.ALERT_ADDED_EVENT:
            case AlertEventPublisher.ALERT_CHANGED_EVENT:
            case AlertEventPublisher.ALERT_REMOVED_EVENT:
                refreshEntry(Integer.valueOf(event.getParameters().get(AlertEventPublisher.HISTORY_REFERENCE_ID)));
                break;
            case AlertEventPublisher.ALL_ALERTS_REMOVED_EVENT:
            default:
                refreshEntries();
                break;
            }
        }

        private void refreshEntry(final int id) {
            if (EventQueue.isDispatchThread()) {
                refreshEntryRow(id);
                return;
            }

            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    refreshEntry(id);
                }
            });
        }

        private void refreshEntries() {
            if (EventQueue.isDispatchThread()) {
                refreshEntryRows();
                return;
            }

            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    refreshEntries();
                }
            });
        }
    }
}
