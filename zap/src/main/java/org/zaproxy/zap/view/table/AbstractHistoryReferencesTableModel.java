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

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;

/**
 * An abstract implementation of {@code HistoryReferencesTableModel}. It contains basic
 * implementation of all the methods except the ones that access the data. the names of all the
 * columns and has
 *
 * @param <T> the type of table model entries
 */
@SuppressWarnings("serial")
public abstract class AbstractHistoryReferencesTableModel<T extends HistoryReferencesTableEntry>
        extends AbstractTableModel implements HistoryReferencesTableModel<T> {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("view.href.table.header.hrefid"),
        Constant.messages.getString("view.href.table.header.timestamp.request"),
        Constant.messages.getString("view.href.table.header.timestamp.response"),
        Constant.messages.getString("view.href.table.header.hreftype"),
        Constant.messages.getString("view.href.table.header.hreftype.name"),
        Constant.messages.getString("view.href.table.header.method"),
        Constant.messages.getString("view.href.table.header.url"),
        Constant.messages.getString("view.href.table.header.hostname"),
        Constant.messages.getString("view.href.table.header.pathandquery"),
        Constant.messages.getString("view.href.table.header.code"),
        Constant.messages.getString("view.href.table.header.reason"),
        Constant.messages.getString("view.href.table.header.rtt"),
        Constant.messages.getString("view.href.table.header.size.message"),
        Constant.messages.getString("view.href.table.header.size.requestheader"),
        Constant.messages.getString("view.href.table.header.size.requestbody"),
        Constant.messages.getString("view.href.table.header.size.responseheader"),
        Constant.messages.getString("view.href.table.header.size.responsebody"),
        Constant.messages.getString("view.href.table.header.sessionid"),
        Constant.messages.getString("view.href.table.header.highestalert"),
        Constant.messages.getString("view.href.table.header.note"),
        Constant.messages.getString("view.href.table.header.tags")
    };

    private final Column[] columns;

    private final Map<Column, Integer> cacheColumnToColumnIdx;

    /**
     * Constructs an {@code AbstractHistoryReferencesTableModel} with the specified columns (in the
     * specified order).
     *
     * @param columns the columns that will have the model
     * @throws IllegalArgumentException if {@code columns} is null or empty.
     */
    public AbstractHistoryReferencesTableModel(Column[] columns) {
        super();

        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("Parameter columns must not be null.");
        }

        this.columns = Arrays.copyOf(columns, columns.length);
        this.cacheColumnToColumnIdx = buildCacheColumnToColumnIdx(columns);
    }

    private Map<Column, Integer> buildCacheColumnToColumnIdx(Column[] columns) {
        Map<Column, Integer> cache = new TreeMap<>();
        for (int i = 0; i < columns.length; i++) {
            cache.put(columns[i], i);
        }
        return cache;
    }

    /**
     * Tells whether or not the given column index is a custom column.
     *
     * @param columnIndex the column index
     * @return {@code true} if it is a custom column, {@code false} otherwise.
     * @see HistoryReferencesTableModel.Column#CUSTOM
     */
    protected boolean isCustomColumn(int columnIndex) {
        return isCustomColumn(columns, columnIndex);
    }

    /**
     * Returns the {@code Class} of the given column.
     *
     * @param columnIndex the column being queried
     * @return the {@code Class} of the column
     */
    @Override
    public abstract Class<?> getColumnClass(int columnIndex);

    @Override
    public Column[] getColumns() {
        return Arrays.copyOf(columns, columns.length);
    }

    @Override
    public Column getColumn(int columnIndex) {
        return columns[columnIndex];
    }

    @Override
    public int getColumnIndex(Column column) {
        Integer idx = cacheColumnToColumnIdx.get(column);
        if (idx != null) {
            return idx;
        }
        return -1;
    }

    @Override
    public boolean isColumnEnabled(Column column) {
        return getColumnIndex(column) != -1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMN_NAMES[this.columns[columnIndex].ordinal()];
    }

    @Override
    public int getColumnCount() {
        return this.columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return getEntry(rowIndex).getValue(getColumn(columnIndex));
    }

    /**
     * Tells whether or not the given column index in the given columns is a custom column.
     *
     * @param columns the columns that will checked
     * @param columnIndex the column index
     * @return {@code true} if it is a custom column, {@code false} otherwise.
     */
    protected static boolean isCustomColumn(final Column[] columns, final int columnIndex) {
        return (columns[columnIndex] == Column.CUSTOM);
    }
}
