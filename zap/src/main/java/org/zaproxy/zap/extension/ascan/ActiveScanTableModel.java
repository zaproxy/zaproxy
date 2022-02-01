/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.table.CustomColumn;
import org.zaproxy.zap.view.table.DefaultCustomColumnHistoryReferencesTableModel;

public class ActiveScanTableModel
        extends DefaultCustomColumnHistoryReferencesTableModel<ActiveScanTableEntry> {

    private static final long serialVersionUID = 5732679524771190690L;
    private static final ActiveScanProcessedCellItem SUCCESSFULLY_PROCESSED_CELL_ITEM;
    private Map<String, ActiveScanProcessedCellItem> cacheProcessedCellItems;
    public static final Column[] COLUMNS =
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
                Column.SIZE_RESPONSE_HEADER,
                Column.SIZE_RESPONSE_BODY
            };

    private static final List<CustomColumn<ActiveScanTableEntry>> CUSTOM_COLUMNS =
            Arrays.asList(createProcessedColumn());

    static {
        SUCCESSFULLY_PROCESSED_CELL_ITEM =
                new ActiveScanProcessedCellItem(
                        true,
                        Constant.messages.getString(
                                "ascan.panel.tab.scannedMessages.column.processed.successfully"));
    }

    public ActiveScanTableModel() {
        super(COLUMNS, CUSTOM_COLUMNS, ActiveScanTableEntry.class);
        cacheProcessedCellItems = new HashMap<>();
    }

    private static CustomColumn<ActiveScanTableEntry> createProcessedColumn() {
        return new CustomColumn<ActiveScanTableEntry>(
                String.class,
                Constant.messages.getString("ascan.panel.tab.scannedMessages.column.processed")) {

            @Override
            public Object getValue(ActiveScanTableEntry model) {
                return model.getProcessedCellItem();
            }
        };
    }

    public void addEntry(HistoryReference hRef, ScannerTaskResult scannerTaskResult) {
        addEntry(new ActiveScanTableEntry(hRef, getProcessedCellItem(scannerTaskResult)));
    }

    private ActiveScanProcessedCellItem getProcessedCellItem(ScannerTaskResult scannerTaskResult) {
        if (scannerTaskResult.isProcessed()) {
            return SUCCESSFULLY_PROCESSED_CELL_ITEM;
        }
        ActiveScanProcessedCellItem processedCellItem =
                cacheProcessedCellItems.get(scannerTaskResult.getReasonNotProcessed());
        if (processedCellItem == null) {
            processedCellItem =
                    new ActiveScanProcessedCellItem(
                            scannerTaskResult.isProcessed(),
                            scannerTaskResult.getReasonNotProcessed());
            cacheProcessedCellItems.put(
                    scannerTaskResult.getReasonNotProcessed(), processedCellItem);
        }
        return processedCellItem;
    }
}
