/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.spider;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;

/** The Class HttpSessionsTableModel that is used as a TableModel for the Http Sessions Panel. */
@SuppressWarnings("serial")
/**
 * The Class HttpSessionsTableModel that is used as a TableModel for the Http Sessions Panel.
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderPanelTableModel extends AbstractTableModel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6380136823410869457L;

    /** The column names. */
    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("spider.table.header.inScope"),
        Constant.messages.getString("spider.table.header.method"),
        Constant.messages.getString("spider.table.header.uri"),
        Constant.messages.getString("spider.table.header.flags")
    };

    /** The Constant defining the COLUMN COUNT. */
    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    /** The Spider scan results. */
    private List<SpiderScanResult> scanResults;

    private boolean incFlags;

    /** Instantiates a new spider panel table model. */
    public SpiderPanelTableModel() {
        this(true);

        scanResults = new ArrayList<>();
    }

    /** Instantiates a new spider panel table model. */
    public SpiderPanelTableModel(boolean incFlags) {
        super();
        this.incFlags = incFlags;

        scanResults = new ArrayList<>();
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public int getColumnCount() {
        if (incFlags) {
            return COLUMN_COUNT;
        } else {
            return COLUMN_COUNT - 1;
        }
    }

    @Override
    public int getRowCount() {
        return scanResults.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Get the ScanResult and the required field
        SpiderScanResult result = scanResults.get(row);
        switch (col) {
            case 0:
                return result.processed;
            case 1:
                return result.method;
            case 2:
                return result.uri;
            case 3:
                return result.flags;
            default:
                return null;
        }
    }

    /** Removes all the elements. Method is synchronized internally. */
    public void removeAllElements() {
        scanResults.clear();
        fireTableDataChanged();
    }

    /**
     * Adds a new spider scan result. Method is synchronized internally.
     *
     * @param uri the uri
     * @param method the method
     * @param flags the flags
     * @param skipped {@code true} if the result was skipped, {@code false} otherwise
     */
    public void addScanResult(String uri, String method, String flags, boolean skipped) {
        SpiderScanResult result = new SpiderScanResult(uri, method, flags, !skipped);
        scanResults.add(result);
        fireTableRowsInserted(scanResults.size() - 1, scanResults.size() - 1);
    }

    /**
     * Removes the scan result for a particular uri and method. Method is synchronized internally.
     *
     * @param uri the uri
     * @param method the method
     */
    public void removesScanResult(String uri, String method) {
        SpiderScanResult toRemove = new SpiderScanResult(uri, method);
        int index = scanResults.indexOf(toRemove);
        if (index >= 0) {
            scanResults.remove(index);
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
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
        }
        return null;
    }

    /**
     * The Class SpiderScanResult that stores an entry in the table (a result for the spidering
     * process).
     */
    private static class SpiderScanResult {

        /** The uri. */
        protected String uri;

        /** The method. */
        protected String method;

        /** The flags. */
        protected String flags;

        /** The in scope. */
        protected boolean processed;

        /**
         * Instantiates a new spider scan result.
         *
         * @param uri the uri
         * @param method the method
         */
        protected SpiderScanResult(String uri, String method) {
            super();
            this.uri = uri;
            this.method = method;
        }

        /**
         * Instantiates a new spider scan result.
         *
         * @param uri the uri
         * @param method the method
         * @param flags the flags
         * @param processed {@code true} if the result was processed, {@code false} otherwise
         */
        protected SpiderScanResult(String uri, String method, String flags, boolean processed) {
            super();
            this.uri = uri;
            this.method = method;
            this.flags = flags;
            this.processed = processed;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            result = prime * result + ((uri == null) ? 0 : uri.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SpiderScanResult)) return false;
            // Removed some irrelevant checks, to speed up the method.
            SpiderScanResult other = (SpiderScanResult) obj;
            if (method == null) {
                if (other.method != null) return false;
            } else if (!method.equals(other.method)) return false;
            if (uri == null) {
                if (other.uri != null) return false;
            } else if (!uri.equals(other.uri)) return false;
            return true;
        }
    }

    public List<String> getAddedNodes() {
        List<String> list = new ArrayList<>(this.scanResults.size());
        for (SpiderScanResult res : this.scanResults) {
            list.add(res.uri);
        }
        return list;
    }
}
