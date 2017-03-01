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
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.event.TableModelEvent;

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

    private SortedMap<Integer, RowIndex> historyIdToRow;
    private SortedSet<RowIndex> rowIndexes;

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
        this.rowIndexes = new TreeSet<>();
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
        RowIndex rowIndex = new RowIndex(hrefList.size() - 1);
        historyIdToRow.put(Integer.valueOf(historyReference.getHistoryReference().getHistoryId()), rowIndex);
        rowIndexes.add(rowIndex);
        fireTableRowsInserted(rowIndex.getValue(), rowIndex.getValue());
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

    public void refreshEntryRows() {
        if (hrefList.isEmpty()) {
            return;
        }

        for (DefaultHistoryReferencesTableEntry entry : hrefList) {
            entry.refreshCachedValues();
        }

        fireTableChanged(
                new TableModelEvent(
                        this,
                        0,
                        hrefList.size() - 1,
                        getColumnIndex(Column.HIGHEST_ALERT),
                        TableModelEvent.UPDATE));
    }

    @Override
    public void removeEntry(final int historyReferenceId) {
        Integer key = Integer.valueOf(historyReferenceId);
        RowIndex rowIndex = historyIdToRow.get(key);
        if (rowIndex != null) {
            hrefList.remove(rowIndex.getValue());
            historyIdToRow.remove(key);
            rowIndexes.remove(rowIndex);
            decreaseRowIndexes(rowIndex);

            fireTableRowsDeleted(rowIndex.getValue(), rowIndex.getValue());
        }
    }

    /**
     * Decreases, by one unit, all the row indexes greater than or equal to {@code fromRowIndex} contained in {@code rowIndexes}.
     * 
     * @param fromRowIndex the start row index
     * @see #rowIndexes
     */
    private void decreaseRowIndexes(RowIndex fromRowIndex) {
        RowIndex[] indexes = removeRowIndexes(fromRowIndex);
        for (RowIndex rowIndex : indexes) {
            rowIndex.decreaseValue();
        }
        rowIndexes.addAll(Arrays.asList(indexes));
    }

    /**
     * Removes and returns all the row indexes greater than or equal to {@code fromRowIndex} contained in {@code rowIndexes}.
     * 
     * @param fromRowIndex the start row index
     * @return the removed row indexes
     * @see #rowIndexes
     */
    private RowIndex[] removeRowIndexes(RowIndex fromRowIndex) {
        SortedSet<RowIndex> indexes = rowIndexes.tailSet(fromRowIndex);
        RowIndex[] removedIndexes = new RowIndex[indexes.size()];
        removedIndexes = indexes.toArray(removedIndexes);
        indexes.clear();
        return removedIndexes;
    }

    @Override
    public void clear() {
        hrefList.clear();
        hrefList.trimToSize();

        historyIdToRow = new TreeMap<>();
        rowIndexes = new TreeSet<>();

        fireTableDataChanged();
    }

    @Override
    public int getEntryRowIndex(final int historyReferenceId) {
        final RowIndex rowIndex = historyIdToRow.get(Integer.valueOf(historyReferenceId));
        if (rowIndex != null) {
            return rowIndex.getValue();
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

    /**
     * A row index, as opposed to an {@code Integer} it allows to change its value.
     * <p>
     * Used for mappings between history IDs and row indexes.
     * 
     * @see DefaultHistoryReferencesTableModel#historyIdToRow
     * @see DefaultHistoryReferencesTableModel#rowIndexes
     */
    private static class RowIndex implements Comparable<RowIndex> {

        private int value;

        public RowIndex(int value) {
            this.value = value;
        }

        public void decreaseValue() {
            this.value -= 1;
        }

        public int getValue() {
            return value;
        }

        @Override
        public int compareTo(RowIndex other) {
            if (other == null) {
                return 1;
            }

            if (value > other.value) {
                return 1;
            } else if (value < other.value) {
                return -1;
            }
            return 0;
        }
    }
}
