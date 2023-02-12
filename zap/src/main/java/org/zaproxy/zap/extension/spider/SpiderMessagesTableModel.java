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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.HistoryReferenceEventPublisher;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.extension.alert.AlertEventPublisher;
import org.zaproxy.zap.view.table.AbstractCustomColumnHistoryReferencesTableModel;
import org.zaproxy.zap.view.table.AbstractHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;

@SuppressWarnings("serial")
/** @deprecated (2.12.0) See the spider add-on in zap-extensions instead. */
@Deprecated
class SpiderMessagesTableModel
        extends AbstractCustomColumnHistoryReferencesTableModel<
                SpiderMessagesTableModel.SpiderTableEntry> {

    private static final long serialVersionUID = 1093393768186896931L;

    private static final Column[] COLUMNS =
            new Column[] {
                Column.CUSTOM,
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
                Column.TAGS
            };

    private static final String[] CUSTOM_COLUMN_NAMES = {
        Constant.messages.getString("spider.table.messages.header.processed")
    };

    private static final ProcessedCellItem SUCCESSFULLY_PROCESSED_CELL_ITEM;

    private final ExtensionHistory extensionHistory;
    private EventConsumerImpl eventConsumer;

    private List<SpiderTableEntry> resources;
    private Map<Integer, Integer> idsToRows;

    private Map<String, ProcessedCellItem> cacheProcessedCellItems;

    static {
        SUCCESSFULLY_PROCESSED_CELL_ITEM =
                new ProcessedCellItem(
                        true,
                        Constant.messages.getString(
                                "spider.table.messages.column.processed.successfully"));
    }

    public SpiderMessagesTableModel() {
        this(true);
    }

    public SpiderMessagesTableModel(boolean createAlertEventConsumer) {
        super(COLUMNS);

        resources = new ArrayList<>();
        idsToRows = new HashMap<>();
        cacheProcessedCellItems = new HashMap<>();

        if (createAlertEventConsumer) {
            eventConsumer = new EventConsumerImpl();
            extensionHistory =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionHistory.class);
            ZAP.getEventBus()
                    .registerConsumer(
                            eventConsumer, AlertEventPublisher.getPublisher().getPublisherName());
            ZAP.getEventBus()
                    .registerConsumer(
                            eventConsumer,
                            HistoryReferenceEventPublisher.getPublisher().getPublisherName());
        } else {
            eventConsumer = null;
            extensionHistory = null;
        }
    }

    @Override
    public void addEntry(SpiderTableEntry entry) {
        // Nothing to do, the entries are added with the following method.
    }

    public void addHistoryReference(
            HistoryReference historyReference, boolean processed, String reasonNotProcessed) {
        HistoryReference latestHistoryReference = historyReference;
        if (extensionHistory != null) {
            latestHistoryReference =
                    extensionHistory.getHistoryReference(historyReference.getHistoryId());
        }
        final SpiderTableEntry entry =
                new SpiderTableEntry(
                        latestHistoryReference,
                        getProcessedCellItem(processed, reasonNotProcessed));
        EventQueue.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        final int row = resources.size();
                        idsToRows.put(entry.getHistoryId(), row);
                        resources.add(entry);
                        fireTableRowsInserted(row, row);
                    }
                });
    }

    private ProcessedCellItem getProcessedCellItem(boolean processed, String reasonNotProcessed) {
        if (processed) {
            return SUCCESSFULLY_PROCESSED_CELL_ITEM;
        }
        ProcessedCellItem processedCellItem = cacheProcessedCellItems.get(reasonNotProcessed);
        if (processedCellItem == null) {
            processedCellItem = new ProcessedCellItem(processed, reasonNotProcessed);
            cacheProcessedCellItems.put(reasonNotProcessed, processedCellItem);
        }
        return processedCellItem;
    }

    @Override
    public void clear() {
        cacheProcessedCellItems = new HashMap<>();
        resources = new ArrayList<>();
        idsToRows = new HashMap<>();
        fireTableDataChanged();

        if (eventConsumer != null) {
            ZAP.getEventBus()
                    .unregisterConsumer(
                            eventConsumer, AlertEventPublisher.getPublisher().getPublisherName());
            ZAP.getEventBus()
                    .unregisterConsumer(
                            eventConsumer,
                            HistoryReferenceEventPublisher.getPublisher().getPublisherName());
            eventConsumer = null;
        }
    }

    @Override
    public void refreshEntryRow(int historyReferenceId) {
        final DefaultHistoryReferencesTableEntry entry = getEntryWithHistoryId(historyReferenceId);

        if (entry != null) {
            int rowIndex = getEntryRowIndex(historyReferenceId);
            getEntryWithHistoryId(historyReferenceId).refreshCachedValues();

            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    @Override
    public void removeEntry(int historyReferenceId) {
        // Nothing to do, the entries are not removed.
    }

    @Override
    public SpiderTableEntry getEntry(int rowIndex) {
        return resources.get(rowIndex);
    }

    @Override
    public SpiderTableEntry getEntryWithHistoryId(int historyReferenceId) {
        final int row = getEntryRowIndex(historyReferenceId);
        if (row != -1) {
            return resources.get(row);
        }
        return null;
    }

    @Override
    public int getEntryRowIndex(int historyReferenceId) {
        final Integer row = idsToRows.get(historyReferenceId);
        if (row != null) {
            return row;
        }
        return -1;
    }

    @Override
    public int getRowCount() {
        return resources.size();
    }

    @Override
    protected Class<?> getColumnClass(Column column) {
        return AbstractHistoryReferencesTableEntry.getColumnClass(column);
    }

    @Override
    protected Object getPrototypeValue(Column column) {
        return AbstractHistoryReferencesTableEntry.getPrototypeValue(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == -1) {
            return getEntry(rowIndex);
        }
        return super.getValueAt(rowIndex, columnIndex);
    }

    @Override
    protected Object getCustomValueAt(SpiderTableEntry entry, int columnIndex) {
        if (getCustomColumnIndex(columnIndex) == 0) {
            return entry.getProcessedCellItem();
        }
        return null;
    }

    @Override
    protected String getCustomColumnName(int columnIndex) {
        return CUSTOM_COLUMN_NAMES[getCustomColumnIndex(columnIndex)];
    }

    @Override
    protected Class<?> getCustomColumnClass(int columnIndex) {
        if (getCustomColumnIndex(columnIndex) == 0) {
            return ProcessedCellItem.class;
        }
        return null;
    }

    @Override
    protected Object getCustomPrototypeValue(int columnIndex) {
        if (getCustomColumnIndex(columnIndex) == 0) {
            return "Successful";
        }
        return null;
    }

    private void refreshEntryRows() {
        if (resources.isEmpty()) {
            return;
        }

        for (SpiderTableEntry entry : resources) {
            entry.refreshCachedValues();
        }

        fireTableChanged(
                new TableModelEvent(
                        this,
                        0,
                        resources.size() - 1,
                        getColumnIndex(Column.HIGHEST_ALERT),
                        TableModelEvent.UPDATE));
    }

    static class SpiderTableEntry extends DefaultHistoryReferencesTableEntry {

        private final ProcessedCellItem processedCellItem;

        public SpiderTableEntry(
                HistoryReference historyReference, ProcessedCellItem processedCellItem) {
            super(historyReference, COLUMNS);
            this.processedCellItem = processedCellItem;
        }

        public ProcessedCellItem getProcessedCellItem() {
            return processedCellItem;
        }
    }

    static class ProcessedCellItem implements Comparable<ProcessedCellItem> {

        private final boolean successful;
        private final String label;

        public ProcessedCellItem(boolean successful, String label) {
            this.successful = successful;
            this.label = label;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public int hashCode() {
            return 31 * (successful ? 1231 : 1237);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ProcessedCellItem other = (ProcessedCellItem) obj;
            if (successful != other.successful) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(ProcessedCellItem other) {
            if (other == null) {
                return 1;
            }
            if (successful && !other.successful) {
                return 1;
            } else if (!successful && other.successful) {
                return -1;
            }
            return label.compareTo(other.label);
        }
    }

    private class EventConsumerImpl implements EventConsumer {

        @Override
        public void eventReceived(Event event) {
            switch (event.getEventType()) {
                case HistoryReferenceEventPublisher.EVENT_TAG_ADDED:
                case HistoryReferenceEventPublisher.EVENT_TAG_REMOVED:
                case HistoryReferenceEventPublisher.EVENT_TAGS_SET:
                    refreshEntry(
                            Integer.valueOf(
                                    event.getParameters()
                                            .get(
                                                    HistoryReferenceEventPublisher
                                                            .FIELD_HISTORY_REFERENCE_ID)));
                    break;
                case AlertEventPublisher.ALERT_ADDED_EVENT:
                case AlertEventPublisher.ALERT_CHANGED_EVENT:
                case AlertEventPublisher.ALERT_REMOVED_EVENT:
                    refreshEntry(
                            Integer.valueOf(
                                    event.getParameters()
                                            .get(AlertEventPublisher.HISTORY_REFERENCE_ID)));
                    break;
                case AlertEventPublisher.ALL_ALERTS_REMOVED_EVENT:
                    refreshEntries();
                    break;
                default:
            }
        }

        private void refreshEntry(final int id) {
            if (EventQueue.isDispatchThread()) {
                refreshEntryRow(id);
                return;
            }

            EventQueue.invokeLater(
                    new Runnable() {

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

            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            refreshEntries();
                        }
                    });
        }
    }
}
