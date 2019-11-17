/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;

/** The Class FilteredMessageTableModel is used as a TableModel for the Active Scan Panel. */
public class FilteredMessageTableModel extends AbstractTableModel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6380136823410869457L;

    /** The column names. */
    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("ascan.filter.table.header.uri"),
        Constant.messages.getString("ascan.filter.table.header.reason")
    };

    /** The Constant defining the COLUMN COUNT. */
    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    /** The Filtered message results. */
    private List<FilteredMessageResult> filteredMessageResultList;

    /** Instantiates a new spider panel table model. */
    public FilteredMessageTableModel() {
        filteredMessageResultList = new ArrayList<>();
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public int getRowCount() {
        return filteredMessageResultList.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Get the ScanResult and the required field
        FilteredMessageResult result = filteredMessageResultList.get(row);
        switch (col) {
            case 0:
                return result.uri;
            case 1:
                return result.reason;
            default:
                return null;
        }
    }

    /** Removes all the elements. Method is synchronized internally. */
    public void removeAllElements() {
        filteredMessageResultList.clear();
        fireTableDataChanged();
    }

    /**
     * Adds a new filtered message result.
     *
     * @param uri the uri
     * @param method the reason
     */
    public void addScanResult(String uri, String reason) {
        FilteredMessageResult result = new FilteredMessageResult(uri, reason);
        filteredMessageResultList.add(result);
        fireTableRowsInserted(
                filteredMessageResultList.size() - 1, filteredMessageResultList.size() - 1);
    }

    /**
     * Removes the scan result for a particular uri and method. Method is synchronized internally.
     *
     * @param uri the uri
     * @param method the reason
     */
    public void removesScanResult(String uri, String reason) {
        FilteredMessageResult toRemove = new FilteredMessageResult(uri, reason);
        int index = filteredMessageResultList.indexOf(toRemove);
        if (index >= 0) {
            filteredMessageResultList.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    /**
     * Returns the type of column for given column index.
     *
     * @param columnIndex the column index
     * @return the column class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return String.class;
        }
        return null;
    }

    /**
     * The Class FilteredMessageResult that stores an entry in the table (a result for the filtered
     * messages in Active Scan).
     */
    private static class FilteredMessageResult {

        /** The uri */
        protected String uri;

        /** reason for Filtering */
        protected String reason;

        /**
         * Instantiates a new filtered message result.
         *
         * @param uri the uri
         * @param reason the method
         */
        protected FilteredMessageResult(String uri, String reason) {
            super();
            this.uri = uri;
            this.reason = reason;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((reason == null) ? 0 : reason.hashCode());
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            FilteredMessageResult other = (FilteredMessageResult) obj;
            if (reason == null) {
                if (other.reason != null) return false;
            } else if (!reason.equals(other.reason)) return false;
            if (uri == null) {
                if (other.uri != null) return false;
            } else if (!uri.equals(other.uri)) return false;
            return true;
        }
    }

    public List<String> getAddedNodes() {
        List<String> list = new ArrayList<String>(this.filteredMessageResultList.size());
        for (FilteredMessageResult res : this.filteredMessageResultList) {
            list.add(res.uri);
        }
        return list;
    }
}
