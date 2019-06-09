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
package org.zaproxy.zap.view.table;

import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.view.table.HistoryReferencesTableModel.Column;

/**
 * An entry of {@code HistoryReferencesTableModel}.
 *
 * @see HistoryReferencesTableModel
 */
public interface HistoryReferencesTableEntry {

    /**
     * Returns the {@code HistoryReference} of the table entry.
     *
     * @return the {@code HistoryReference} of the table entry
     * @see HistoryReference
     */
    HistoryReference getHistoryReference();

    /**
     * Returns the value of the entry for the given {@code column}.
     *
     * @param column the column whose entry value will be returned
     * @return the value of the entry for the given {@code column}
     */
    Object getValue(Column column);
}
