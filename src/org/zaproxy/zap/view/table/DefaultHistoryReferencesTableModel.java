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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.parosproxy.paros.model.HistoryReference;

/**
 * A default implementation of {@code HistoryReferencesTableModel}.
 * 
 * @see HistoryReferencesTableModel
 */
public class DefaultHistoryReferencesTableModel extends AbstractHistoryReferencesTableModel<DefaultHistoryReferencesTableEntry> {

    private static final long serialVersionUID = -8628528927411108669L;

    private static final Column[] DEFAULT_COLUMNS = new Column[] {
            Column.HREF_ID,
            Column.REQUEST_TIMESTAMP,
            Column.METHOD,
            Column.URL,
            Column.STATUS_CODE,
            Column.STATUS_REASON,
            Column.RTT,
            Column.SIZE_RESPONSE_BODY,
            Column.HIGHEST_ALERT,
            Column.NOTE,
            Column.TAGS };

    public static Column[] getDefaultColumns() {
        return Arrays.copyOf(DEFAULT_COLUMNS, DEFAULT_COLUMNS.length);
    }

    private ArrayList<DefaultHistoryReferencesTableEntry> hrefList;

    private SortedMap<Integer, Integer> historyIdToRow;

    /**
     * Constructs a {@code DefaultHistoryReferencesTableModel} with the default columns.
     * 
     * @see #getDefaultColumns()
     */
    public DefaultHistoryReferencesTableModel() {
        this(getDefaultColumns());
    }

    /**
     * Constructs a {@code DefaultHistoryReferencesTableModel} with the specified columns (in the specified order).
     * 
     * @param columns the columns that will have the model
     * @throws IllegalArgumentException if {@code columns} is null or empty.
     */
    public DefaultHistoryReferencesTableModel(final Column[] columns) {
        super(columns);
        this.hrefList = new ArrayList<>();
        this.historyIdToRow = new TreeMap<>();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return DefaultHistoryReferencesTableEntry.getColumnClass(getColumn(columnIndex));
    }

    /**
     * Returns the {@code Class} of the given column.
     * 
     * @param column the column being queried
     * @return the {@code Class} of the column
     */
    public Class<?> getColumnClass(Column column) {
        return DefaultHistoryReferencesTableEntry.getColumnClass(column);
    }

    @Override
    public int getRowCount() {
        return hrefList.size();
    }

    @Override
    public Object getPrototypeValue(int columnIndex) {
        return DefaultHistoryReferencesTableEntry.getPrototypeValue(getColumn(columnIndex));
    }

    /**
     * Returns the prototype value of the given column.
     * 
     * @param column the column being queried
     * @return the prototype value of the column
     */
    public Object getPrototypeValue(Column column) {
        return DefaultHistoryReferencesTableEntry.getPrototypeValue(column);
    }

    @Override
    public void addEntry(final DefaultHistoryReferencesTableEntry historyReference) {
        hrefList.add(historyReference);
        final int rowIndex = hrefList.size() - 1;
        historyIdToRow.put(Integer.valueOf(historyReference.getHistoryReference().getHistoryId()), Integer.valueOf(rowIndex));
        fireTableRowsInserted(rowIndex, rowIndex);
    }

    @Override
    public void refreshEntryRow(final int historyReferenceId) {
        final DefaultHistoryReferencesTableEntry entry = getEntryWithHistoryId(historyReferenceId);

        if (entry != null) {
            int rowIndex = getEntryRowIndex(historyReferenceId);
            getEntryWithHistoryId(historyReferenceId).refreshCachedValues();

            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }

    @Override
    public void removeEntry(final int historyReferenceId) {
        Integer key = Integer.valueOf(historyReferenceId);
        Integer row = historyIdToRow.get(key);
        if (row != null) {
            final int rowIndex = row.intValue();

            hrefList.remove(rowIndex);
            historyIdToRow.remove(key);

            for (Entry<Integer, Integer> mapping : historyIdToRow.subMap(
                    Integer.valueOf(key.intValue() + 1),
                    Integer.valueOf(Integer.MAX_VALUE)).entrySet()) {
                mapping.setValue(Integer.valueOf(mapping.getValue().intValue() - 1));
            }

            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }

    @Override
    public void clear() {
        hrefList.clear();
        hrefList.trimToSize();

        historyIdToRow = new TreeMap<>();

        fireTableDataChanged();
    }

    @Override
    public int getEntryRowIndex(final int historyReferenceId) {
        final Integer row = historyIdToRow.get(Integer.valueOf(historyReferenceId));
        if (row != null) {
            return row.intValue();
        }
        return -1;
    }

    @Override
    public DefaultHistoryReferencesTableEntry getEntryWithHistoryId(final int historyId) {
        final int row = getEntryRowIndex(historyId);
        if (row != -1) {
            return hrefList.get(row);
        }
        return null;
    }

    @Override
    public DefaultHistoryReferencesTableEntry getEntry(final int rowIndex) {
        return hrefList.get(rowIndex);
    }

    /**
     * Convenience method that creates a {@code DefaultHistoryReferencesTableEntry} with the given history reference and adds it
     * to the model.
     * 
     * @param historyReference the history reference that will be added to the model
     * @see DefaultHistoryReferencesTableEntry
     * @see HistoryReference
     */
    public void addHistoryReference(HistoryReference historyReference) {
        addEntry(new DefaultHistoryReferencesTableEntry(historyReference, getColumns()));
    }

    /**
     * Returns the history reference with the give ID. If the history reference is not found {@code null} is returned.
     * 
     * @param historyReferenceId the ID of the history reference that will be searched
     * @return the history refernce, or {@code null} if not found
     */
    public HistoryReference getHistoryReference(int historyReferenceId) {
        DefaultHistoryReferencesTableEntry entry = getEntryWithHistoryId(historyReferenceId);
        if (entry != null) {
            return entry.getHistoryReference();
        }
        return null;
    }

}
