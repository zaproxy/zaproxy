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
package org.zaproxy.zap.extension.brk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;

@SuppressWarnings("serial")
public class BreakpointsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -8160051343126299124L;

    private static final int COLUMN_COUNT = 3;

    private static final String[] columnNames = {
        Constant.messages.getString("brk.table.header.enabled"),
        Constant.messages.getString("brk.table.header.type"),
        Constant.messages.getString("brk.table.header.condition")
    };

    private List<BreakpointMessageInterface> breakpoints;
    private List<BreakpointMessageInterface> breakpointsEnabled;

    private Map<BreakpointMessageInterface, Integer> mapBreakpointRow;

    private int lastAffectedRow;

    public BreakpointsTableModel() {
        super();

        breakpoints = new ArrayList<>(0);
        breakpointsEnabled = new ArrayList<>(0);

        mapBreakpointRow = new HashMap<>();

        lastAffectedRow = -1;
    }

    public List<BreakpointMessageInterface> getBreakpointsList() {
        return breakpoints;
    }

    public List<BreakpointMessageInterface> getBreakpointsEnabledList() {
        return breakpointsEnabled;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public int getRowCount() {
        return breakpoints.size();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object obj = null;
        BreakpointMessageInterface breakpoint = breakpoints.get(row);
        if (column == 0) {
            obj = breakpoint.isEnabled();
        } else if (column == 1) {
            obj = breakpoint.getType();
        } else {
            obj = breakpoint.getDisplayMessage();
        }
        return obj;
    }

    public BreakpointMessageInterface getBreakpointAtRow(int row) {
        return breakpoints.get(row);
    }

    public void addBreakpoint(BreakpointMessageInterface breakpoint) {
        breakpoints.add(breakpoint);
        this.fireTableRowsInserted(breakpoints.size() - 1, breakpoints.size() - 1);

        rebuildMapBreakpointRow();
        lastAffectedRow = mapBreakpointRow.get(breakpoint);

        if (breakpoint.isEnabled()) {
            synchronized (breakpointsEnabled) {
                breakpointsEnabled.add(breakpoint);
            }
        }
    }

    public void editBreakpoint(
            BreakpointMessageInterface oldBreakpoint, BreakpointMessageInterface newBreakpoint) {
        int row = mapBreakpointRow.remove(oldBreakpoint);
        breakpoints.remove(row);
        this.fireTableRowsDeleted(row, row);

        mapBreakpointRow.put(newBreakpoint, 0);
        breakpoints.add(newBreakpoint);
        this.fireTableRowsInserted(breakpoints.size() - 1, breakpoints.size() - 1);

        rebuildMapBreakpointRow();
        lastAffectedRow = mapBreakpointRow.get(newBreakpoint);

        synchronized (breakpointsEnabled) {
            if (oldBreakpoint.isEnabled()) {
                breakpointsEnabled.remove(oldBreakpoint);
            }
            if (newBreakpoint.isEnabled()) {
                breakpointsEnabled.add(newBreakpoint);
            }
        }
    }

    public void removeBreakpoint(BreakpointMessageInterface breakpoint) {
        Integer row = mapBreakpointRow.remove(breakpoint);

        if (row != null) {
            breakpoints.remove(breakpoint);
            this.fireTableRowsDeleted(row, row);

            rebuildMapBreakpointRow();

            synchronized (breakpointsEnabled) {
                if (breakpoint.isEnabled()) {
                    breakpointsEnabled.remove(breakpoint);
                }
            }
        }
    }

    public int getLastAffectedRow() {
        return lastAffectedRow;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return (column == 0);
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        if (column == 0) {
            if (value instanceof Boolean) {
                boolean isEnabled = breakpoints.get(row).isEnabled();
                breakpoints.get(row).setEnabled((Boolean) value);
                this.fireTableCellUpdated(row, column);

                if (isEnabled) {
                    synchronized (breakpointsEnabled) {
                        breakpointsEnabled.remove(breakpoints.get(row));
                    }
                } else {
                    synchronized (breakpointsEnabled) {
                        breakpointsEnabled.add(breakpoints.get(row));
                    }
                }
            }
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        if (column == 0) {
            return Boolean.class;
        }
        return String.class;
    }

    private void rebuildMapBreakpointRow() {
        mapBreakpointRow.clear();
        int i = 0;
        for (Iterator<BreakpointMessageInterface> iterator = breakpoints.iterator();
                iterator.hasNext();
                ++i) {
            mapBreakpointRow.put(iterator.next(), i);
        }
    }
}
