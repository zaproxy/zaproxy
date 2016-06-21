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

import javax.swing.SortOrder;
import javax.swing.table.TableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.table.HistoryReferencesTable;

class SpiderMessagesTable extends HistoryReferencesTable {

    private static final long serialVersionUID = -1910120966638329368L;

    private final ExtensionHistory extensionHistory;

    public SpiderMessagesTable(SpiderMessagesTableModel resultsModel) {
        super(resultsModel);

        setName("SpiderMessagesTable");

        setAutoCreateColumnsFromModel(false);

        getColumnExt(Constant.messages.getString("view.href.table.header.hrefid")).setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.timestamp.response")).setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.size.requestheader")).setVisible(false);
        getColumnExt(Constant.messages.getString("view.href.table.header.size.requestbody")).setVisible(false);

        setSortOrder(0, SortOrder.ASCENDING);

        extensionHistory = Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);
    }

    @Override
    public void setModel(TableModel dataModel) {
        // Keep the same column sorted when model is changed
        int sortedcolumnIndex = getSortedColumnIndex();
        SortOrder sortOrder = getSortOrder(sortedcolumnIndex);
        super.setModel(dataModel);
        if (sortedcolumnIndex != -1) {
            setSortOrder(sortedcolumnIndex, sortOrder);
        }
    }

    @Override
    protected HistoryReference getHistoryReferenceAtViewRow(int row) {
        HistoryReference historyReference = super.getHistoryReferenceAtViewRow(row);
        if (historyReference == null) {
            return null;
        }

        if (extensionHistory == null || extensionHistory.getHistoryReference(historyReference.getHistoryId()) == null) {
            // Associated message was deleted in the meantime.
            return null;
        }

        return historyReference;
    }
}
