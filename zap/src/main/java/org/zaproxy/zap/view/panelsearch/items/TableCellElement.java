/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.view.panelsearch.items;

import javax.swing.JTable;

public class TableCellElement {

    private Object value;
    private JTable table;
    private Object columnIdentifier;
    private int columnIndex;
    private int rowIndex;

    public TableCellElement(
            JTable table, Object columnIdentifier, int columnIndex, int rowIndex, Object value) {
        this.table = table;
        this.columnIdentifier = columnIdentifier;
        this.columnIndex = columnIndex;
        this.rowIndex = rowIndex;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public JTable getTable() {
        return table;
    }

    public Object getColumnIdentifier() {
        return columnIdentifier;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }
}
