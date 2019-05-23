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

import javax.swing.table.TableModel;

import org.parosproxy.paros.model.HistoryReference;

/**
 * A {@code TableModel} specialised in displaying data of {@code HistoryReference}s and handling them.
 * 
 * @param <T> the type of entries of this table model
 * @see TableModel
 * @see HistoryReference
 * @see HistoryReferencesTable
 */
public interface HistoryReferencesTableModel<T extends HistoryReferencesTableEntry> extends TableModel {

    /**
     * The default columns supported by the table model for state of {@code HistoryReference}.
     * 
     * @see Column#CUSTOM
     */
    public enum Column {
        HREF_ID,
        REQUEST_TIMESTAMP,
        RESPONSE_TIMESTAMP,
        HREF_TYPE,
        METHOD,
        URL,
        STATUS_CODE,
        STATUS_REASON,
        RTT,
        SIZE_MESSAGE,
        SIZE_REQUEST_HEADER,
        SIZE_REQUEST_BODY,
        SIZE_RESPONSE_HEADER,
        SIZE_RESPONSE_BODY,
        SESSION_ID,
        HIGHEST_ALERT,
        NOTE,
        TAGS,

        /**
         * Indicates that the column has custom values and that those values should retrieved by other means.
         */
        CUSTOM
    }

    /**
     * Returns all {@code Column}s enabled.
     * 
     * @return all {@code Column}s enabled.
     */
    Column[] getColumns();

    /**
     * Returns the {@code Column} at the given column index.
     * 
     * @param columnIndex the column index used to get the {@code Column}
     * @return the {@code Column} at the given column index
     * @throws IllegalArgumentException if {@code columnIndex} is not valid (is negative, greater or equal than the number of
     *             columns).
     */
    Column getColumn(int columnIndex);

    /**
     * Returns the index of the given {@code column}. If the given {@code column} is not enabled, {@code null} is returned.
     * 
     * @param column the column that will be searched
     * @return the index of the given {@code column}, or {@code null} if not enabled.
     * @throws IllegalArgumentException if {@code column} is {@code null}
     * @see #isColumnEnabled(Column)
     */
    int getColumnIndex(Column column);

    /**
     * Tells whether the given {@code column} is enabled. A column is enabled if the table model was configured to use it.
     * 
     * @param column the column that will be checked
     * @return {@code true} if the column is enabled, {@code false} otherwise.
     * @throws IllegalArgumentException if {@code column} is {@code null}
     */
    boolean isColumnEnabled(Column column);

    /**
     * Returns the prototype value for the given column index. The prototype value is a possible representation of the values
     * that might exist in the column. It's used to set an expected width for the column.
     * 
     * @param columnIndex the column index that will be checked.
     * @return the prototype value for the column
     * @throws IllegalArgumentException if {@code columnIndex} is not valid (is negative, greater or equal than the number of
     *             columns).
     */
    Object getPrototypeValue(int columnIndex);

    /**
     * Appends the given {@code entry} to the end of the entries.
     * 
     * @param entry the entry that will be added
     */
    void addEntry(T entry);

    /**
     * Notifies the table model listeners that the entry with the given {@code historyReferenceId} has changed.
     * <p>
     * The call to this method has no effect if there is no entry with the given ID.
     * 
     * @param historyReferenceId the {@code HistoryReference} ID of the entry that has changed
     * @see javax.swing.event.TableModelListener
     */
    void refreshEntryRow(int historyReferenceId);

    /**
     * Removes the entry with the given {@code historyReferenceId}.
     * <p>
     * The call to this method has no effect if there is no entry with the given ID.
     * 
     * @param historyReferenceId the {@code HistoryReference} ID of the entry that will be removed
     */
    void removeEntry(int historyReferenceId);

    /**
     * Returns the entry at the given {@code rowIndex}.
     * 
     * @param rowIndex the row index of the entry
     * @return the entry at the given row index
     * @throws IllegalArgumentException if {@code rowIndex} is not valid (is negative, greater or equal than the number of
     *             rows).
     */
    T getEntry(int rowIndex);

    /**
     * Returns the entry with the given {@code historyReferenceId}. If there is no entry with the given ID {@code null} is
     * returned.
     * 
     * @param historyReferenceId the {@code HistoryReference} ID of the entry that will be returned
     * @return the entry with the given {@code HistoryReference} ID, or {@code null} if there is no entry with the given ID
     */
    T getEntryWithHistoryId(int historyReferenceId);

    /**
     * Returns the row index of the entry with the given {@code historyReferenceId}. If there is no entry with the given ID
     * {@literal -1} is returned.
     * 
     * @param historyReferenceId the {@code HistoryReference} ID of the entry that will be searched
     * @return the row index of the entry with the given {@code HistoryReference} ID, or {@literal -1} if there is no entry with
     *         the given ID
     */
    int getEntryRowIndex(int historyReferenceId);

    /**
     * Removes all the entries.
     */
    void clear();

}
