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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * An abstract implementation of {@code HistoryReferencesTableModel} with support for custom
 * columns.
 *
 * @param <T> the type of table model entries
 */
@SuppressWarnings("serial")
public abstract class AbstractCustomColumnHistoryReferencesTableModel<
                T extends HistoryReferencesTableEntry>
        extends AbstractHistoryReferencesTableModel<T> {

    private static final long serialVersionUID = 3943406327364886416L;

    private final Map<Integer, Integer> cacheColumnIdxToIdxCustomColumnsOnly;

    /**
     * Constructs an {@code AbstractCustomColumnHistoryReferencesTableModel} with the specified
     * columns (in the specified order).
     *
     * @param columns the columns that will have the model
     * @throws IllegalArgumentException if {@code columns} is null or empty.
     */
    public AbstractCustomColumnHistoryReferencesTableModel(Column[] columns) {
        super(columns);

        cacheColumnIdxToIdxCustomColumnsOnly = buildCacheColumnIdxToIdxCustomColumnsOnly(columns);
    }

    private static Map<Integer, Integer> buildCacheColumnIdxToIdxCustomColumnsOnly(
            Column[] columns) {
        Map<Integer, Integer> tempCustomColumnIndexesMap = new TreeMap<>();
        int countCustomColumns = 0;
        for (int columnIndex = 0; columnIndex < columns.length; ++columnIndex) {
            if (isCustomColumn(columns, columnIndex)) {
                tempCustomColumnIndexesMap.put(columnIndex, countCustomColumns);
                ++countCustomColumns;
            }
        }

        if (tempCustomColumnIndexesMap.isEmpty()) {
            return Collections.emptyMap();
        }
        return tempCustomColumnIndexesMap;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T entry = getEntry(rowIndex);
        Column column = getColumn(columnIndex);
        if (column == Column.CUSTOM) {
            return getCustomValueAt(entry, columnIndex);
        }
        return entry.getValue(column);
    }

    /**
     * Returns the value for the entry at the given column index. Called when the column is a {@code
     * Column#CUSTOM}.
     *
     * @param entry the entry with the values
     * @param columnIndex the column index
     * @return the entry value at the specified column index
     * @see #getCustomColumnIndex(int)
     * @see HistoryReferencesTableModel.Column#CUSTOM
     */
    protected abstract Object getCustomValueAt(T entry, int columnIndex);

    @Override
    public String getColumnName(int columnIndex) {
        if (isCustomColumn(columnIndex)) {
            return getCustomColumnName(columnIndex);
        }
        return super.getColumnName(columnIndex);
    }

    /**
     * Returns the name of the column at the given column index. Called when the column is a {@code
     * Column#CUSTOM}.
     *
     * @param columnIndex the column index
     * @return the name of the custom column
     * @see #getCustomColumnIndex(int)
     * @see HistoryReferencesTableModel.Column#CUSTOM
     */
    protected abstract String getCustomColumnName(int columnIndex);

    /**
     * {@inheritDoc}
     *
     * @see #getColumnClass(HistoryReferencesTableModel.Column)
     * @see #getCustomColumnClass(int)
     */
    @Override
    public final Class<?> getColumnClass(int columnIndex) {
        Column column = getColumn(columnIndex);
        if (column == Column.CUSTOM) {
            return getCustomColumnClass(columnIndex);
        }
        return getColumnClass(column);
    }

    /**
     * Returns the {@code Class} of the column.
     *
     * @param column the column
     * @return the {@code Class} of the column
     */
    protected abstract Class<?> getColumnClass(Column column);

    /**
     * Returns the {@code Class} of the column at the given column index. Called when the column is
     * a {@code Column#CUSTOM}.
     *
     * @param columnIndex the column index
     * @return the {@code Class} of the custom column
     * @see #getCustomColumnIndex(int)
     * @see HistoryReferencesTableModel.Column#CUSTOM
     */
    protected abstract Class<?> getCustomColumnClass(int columnIndex);

    @Override
    public Object getPrototypeValue(int columnIndex) {
        Column column = getColumn(columnIndex);
        if (column == Column.CUSTOM) {
            return getCustomPrototypeValue(columnIndex);
        }
        return getPrototypeValue(column);
    }

    /**
     * Returns the prototype value for the given column.
     *
     * @param column the column
     * @return the prototype value for the column
     */
    protected abstract Object getPrototypeValue(Column column);

    /**
     * Returns the prototype value for the column at the given column index. Called when the column
     * is a {@code Column#CUSTOM}.
     *
     * @param columnIndex the column index
     * @return the prototype value for the column
     * @see #getCustomColumnIndex(int)
     * @see HistoryReferencesTableModel.Column#CUSTOM
     */
    protected abstract Object getCustomPrototypeValue(int columnIndex);

    /**
     * Returns the index of the custom column as if no default columns existed.
     *
     * <p>Helper method for subclasses to use the column index without worrying of the actual
     * position of the column in relation to default columns.
     *
     * <p>It can be used, for example, with {@code getCustomColumnClass(int)} as:
     *
     * <blockquote>
     *
     * <pre>
     * protected Class&lt;?&gt; getCustomColumnClass(int columnIndex) {
     *     switch (getCustomColumnIndex(columnIndex)) {
     *     case 0:
     *         // Class of the first custom column
     *         return String.class;
     *         break;
     *     case 1:
     *         // Class of the second custom column
     *         return Integer.class;
     *     default:
     *         return String.class;
     *     }
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param columnIndex the column index
     * @return the custom column index as if no default columns existed or -1 if not a custom
     *     column.
     */
    protected int getCustomColumnIndex(int columnIndex) {
        Integer customColumnIndex = cacheColumnIdxToIdxCustomColumnsOnly.get(columnIndex);
        if (customColumnIndex != null) {
            return customColumnIndex;
        }
        return -1;
    }
}
