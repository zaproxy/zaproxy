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

/**
 * @author KSASAN preetkaran20@gmail.com
 * @since TODO add version
 */
class FilterMessageTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -6380136823410869457L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("ascan.filter.table.header.url"),
        Constant.messages.getString("ascan.filter.table.header.reason")
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<FilteredMessageResult> filteredMessageResults;

    public FilterMessageTableModel() {
        filteredMessageResults = new ArrayList<>();
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
        return filteredMessageResults.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        FilteredMessageResult result = filteredMessageResults.get(row);
        switch (col) {
            case 0:
                return result.url;
            case 1:
                return result.reason;
            default:
                return null;
        }
    }

    /**
     * Adds a new filtered message result.
     *
     * @param url
     * @param reason for filtering message
     */
    public void addScanResult(String url, String reason) {
        FilteredMessageResult result = new FilteredMessageResult(url, reason);
        filteredMessageResults.add(result);
        fireTableRowsInserted(filteredMessageResults.size() - 1, filteredMessageResults.size() - 1);
    }

    /**
     * Returns the type of column for given column index.
     *
     * @param columnIndex the column index
     * @return the column class
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    private static class FilteredMessageResult {

        protected String url;

        protected String reason;

        /**
         * @param url
         * @param reason for filtering message
         */
        protected FilteredMessageResult(String url, String reason) {
            super();
            this.url = url;
            this.reason = reason;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((reason == null) ? 0 : reason.hashCode());
            result = prime * result + ((url == null) ? 0 : url.hashCode());
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
            if (url == null) {
                if (other.url != null) return false;
            } else if (!url.equals(other.url)) return false;
            return true;
        }
    }
}
