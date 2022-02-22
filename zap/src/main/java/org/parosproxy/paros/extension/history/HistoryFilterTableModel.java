/*
 *  Zed Attack Proxy (ZAP) and its related class files.
 *
 *  ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 *  Copyright 2018 The ZAP Development Team
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.parosproxy.paros.extension.history;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

public class HistoryFilterTableModel extends AbstractMultipleOptionsTableModel<HistoryFilter> {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("history.filter.options.table.header.enabled"),
        Constant.messages.getString("history.filter.options.table.header.name"),
        Constant.messages.getString("history.filter.options.table.header.description")
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<HistoryFilter> filters = new ArrayList<>(0);

    public HistoryFilterTableModel() {
        super();
    }

    @Override
    public List<HistoryFilter> getElements() {
        return filters;
    }

    public void setFilters(List<HistoryFilter> filters) {
        this.filters = new ArrayList<>(filters.size());

        for (HistoryFilter filter : filters) {
            this.filters.add(filter);
        }

        fireTableDataChanged();
    }

    @Override
    public void addElement(HistoryFilter filter) {
        super.addElement(filter);
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == 0) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public int getRowCount() {
        return filters.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0 || columnIndex == 1);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.valueOf(getElement(rowIndex).isEnabled());
            case 1:
                return getElement(rowIndex).getName();
            case 2:
                return getElement(rowIndex).toLongString();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (aValue instanceof Boolean) {
                filters.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        } else if (columnIndex == 1) {
            if (aValue instanceof String) {
                filters.get(rowIndex).setName((String) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
