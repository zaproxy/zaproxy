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
package org.zaproxy.zap.extension.callback.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableEntry;
import org.zaproxy.zap.view.table.DefaultHistoryReferencesTableModel;

/** @deprecated (2.11.0) Superseded by the OAST add-on. */
@Deprecated
@SuppressWarnings("serial")
public class DefaultCustomColumnHistoryReferencesTableModel<
                T extends DefaultHistoryReferencesTableEntry>
        extends DefaultHistoryReferencesTableModel {

    private static final long serialVersionUID = 1L;
    private Map<Integer, CustomColumn<T>> customColumns;
    private Class<T> type;

    /**
     * The class adds custom columns to the {@code DefaultHistoryReferencesTableModel}.
     *
     * @param columns Add the {@code Column.CUSTOM} at the desired position in the column list.
     * @param customColumns Provide the implementations of the custom columns you passed to the
     *     {@code columns} parameter. When the count does not match then empty columns are created
     *     on that particular index.
     */
    public DefaultCustomColumnHistoryReferencesTableModel(
            final Column[] columns, final ArrayList<CustomColumn<T>> customColumns, Class<T> type) {
        super(columns);
        this.type = type;
        this.customColumns = createCustomColumnMap(columns, customColumns);
    }

    private Map<Integer, CustomColumn<T>> createCustomColumnMap(
            Column[] columns, ArrayList<CustomColumn<T>> customColumns) {
        Map<Integer, CustomColumn<T>> customColumnMap = new HashMap<>();

        int customColumnIndex = 0;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] == Column.CUSTOM) {
                CustomColumn<T> customColumn =
                        getCustomColumnIfExists(customColumns, customColumnIndex);
                customColumnMap.put(i, customColumn);
                customColumnIndex++;
            }
        }

        return customColumnMap;
    }

    private CustomColumn<T> getCustomColumnIfExists(
            ArrayList<CustomColumn<T>> customColumns, Integer index) {
        if (index < customColumns.size()) {
            return customColumns.get(index);
        }
        return emptyColumn();
    }

    private CustomColumn<T> emptyColumn() {
        return new CustomColumn<T>(String.class, "") {

            @Override
            public Object getValue(T model) {
                return "";
            }
        };
    }

    private CustomColumn<T> getCustomColumn(int columnIndex) {
        return customColumns.get(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (isCustomColumn(columnIndex)) {
            return getCustomColumn(columnIndex).getColumnClass();
        }

        return super.getColumnClass(getColumn(columnIndex));
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (isCustomColumn(columnIndex)) {
            return getCustomColumn(columnIndex).getName();
        }

        return super.getColumnName(columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (isCustomColumn(columnIndex)) {
            if (rowIndex >= getRowCount()) {
                return null;
            }

            DefaultHistoryReferencesTableEntry entry = getEntry(rowIndex);
            return getCustomColumn(columnIndex).getValue(uncheckedCast(entry));
        }

        return super.getValueAt(rowIndex, columnIndex);
    }

    @SuppressWarnings({"unchecked"})
    private T uncheckedCast(DefaultHistoryReferencesTableEntry entry) {
        if (!type.isInstance(entry)) {
            return null;
        }

        try {
            return (T) entry;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public Object getPrototypeValue(int columnIndex) {
        if (isCustomColumn(columnIndex)) {
            return getCustomColumn(columnIndex).getPrototypeValue();
        }

        return super.getPrototypeValue(columnIndex);
    }
}
