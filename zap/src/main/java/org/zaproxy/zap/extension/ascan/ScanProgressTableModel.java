/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
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
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.Plugin;

@SuppressWarnings("serial")
public class ScanProgressTableModel extends AbstractTableModel {
	
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_STRENGTH = 1;
	private static final int COLUMN_PROGRESS = 2;
	private static final int COLUMN_TIME = 3;
	private static final int COLUMN_REQUESTS = 4;
	private static final int COLUMN_ALERTS = 5;
	private static final int COLUMN_STATUS = 6;
	
	private static final int ANALYSER_ROW = 0;
	private static final int TOP_SEPARATOR_ROW = 1;
	private static final int PLUGIN_HEADER_ROW = 2;
	private static final int FIRST_PLUGIN_ROW_OFFSET = 3;
	private static final int TOTALS_ROW_OFFSET_FROM_PLUGIN_END = 1;

    private static final long serialVersionUID = 1L;
    private static final String[] columnNames = {
        "",
        Constant.messages.getString("ascan.policy.table.strength"),
        Constant.messages.getString("ascan.progress.table.progress"),
        Constant.messages.getString("ascan.progress.table.time"),
        Constant.messages.getString("ascan.progress.table.reqs"),
        Constant.messages.getString("ascan.progress.table.alerts"),
        Constant.messages.getString("ascan.progress.table.status"),
    };

    private HostProcess hp;
    private List<ScanProgressItem> values;
    private ScanProgressActionIcon focusedAction;
    private String totRequests;
    private String totTime;

    public ScanProgressTableModel() {
        super();
        values = new ArrayList<>();
        focusedAction = null;
    }

    /**
     * @return
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * @return
     */
    @Override
    public int getRowCount() {
        if (values == null) {
            return 0;
        }

        // Add other 5 rows for other info shown.
        return values.size() + 5;
    }

    /**
     * @param row
     * @param col
     * @return
     */
    @Override
    public Object getValueAt(int row, int col) {
        // 1st row is for the Analyser, 2nd row is empty (for separation with the plugins), 3rd for plugin label.
        if (row == ANALYSER_ROW) {
            return getAnalyserRowValue(col);
        } else if (row == TOP_SEPARATOR_ROW) {
            return null;
        } else if (row == PLUGIN_HEADER_ROW) {
            return getPluginHeaderName(col);
        }
        // Adjust row for the plugin checks.
        row -= FIRST_PLUGIN_ROW_OFFSET;
        // First check if we're showing the plugin status list
        if (row < values.size()) {
            // It's an entry so show the correct values
            return getPluginStatusRowValue(row, col);
        // We're in the summary "region", first print an empty line and then two line of summary (tot time and to requests)
        // Maybe could be done in a better way, for example using a dedicated panel positioned on top/bottom of the dialog
        } else if (row == values.size()) {
            // The first line after values should be empty
            return null;
        } else if (row == (values.size() + TOTALS_ROW_OFFSET_FROM_PLUGIN_END)) {
            // The second line after values should contains the totals
            return getTotalsRowValue(col);
        }
        return null;
    }
    
    private Object getAnalyserRowValue(int col) {
    	switch (col) {
	        case COLUMN_NAME:
	            return Constant.messages.getString("ascan.progress.table.analyser");
	        case COLUMN_TIME:
	            return hp != null ? getElapsedTimeLabel(hp.getAnalyser().getRunningTime()) : "";
	        case COLUMN_REQUESTS:
	            return hp != null ? String.valueOf(hp.getAnalyser().getRequestCount()) : "";
	        default:
	            return null;
    	}
    }
    
    private Object getPluginHeaderName(int col) {
    	return col == COLUMN_NAME ? Constant.messages.getString("ascan.progress.table.name") : null;
    }
    
    private Object getPluginStatusRowValue(int row, int col) {
    	final ScanProgressItem item = values.get(row);
        switch (col) {
            case COLUMN_NAME:
                return item.getNameLabel();
            case COLUMN_STRENGTH:
                return item.getAttackStrengthLabel();
            case COLUMN_PROGRESS:
                if (item.isCompleted() || item.isRunning() || item.isSkipped()) {
                    return item;
                } else {
                    return null;
                }
            case COLUMN_TIME:
                return getElapsedTimeLabel(item.getElapsedTime());
            case COLUMN_REQUESTS:
                return item.getReqCount();
            case COLUMN_ALERTS:
                return item.getAlertCount();
            case COLUMN_STATUS:
                return item.getProgressAction();
            default:
                return null;
        }
    }
    
    private Object getTotalsRowValue(int col) {
    	switch (col) {
	        case COLUMN_NAME:
	            return Constant.messages.getString("ascan.progress.label.totals");
	        case COLUMN_TIME:
	            return totTime;
	        case COLUMN_REQUESTS:
	            return totRequests;
	        case COLUMN_ALERTS:
	            return hp != null ? hp.getAlertCount() : 0;
	        default:
	            return null;
	    }
    }

    /**
     * @param rowIndex
     * @param columnIndex
     * @return
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * @param col
     * @return
     */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * @param c
     * @return
     */
    @Override
    public Class<?> getColumnClass(int c) {
        switch (c) {
            case 0:
                return String.class;

            case 1:
                return String.class;

            case 2:
                return ScanProgressItem.class;

            case 3:
                return String.class;

            case 4:
                return ScanProgressActionIcon.class;
        }

        return null;
    }

    /**
     * @return
     */
    public List<ScanProgressItem> getValues() {
        return values;
    }

    /**
     * @param actionIcon
     */
    public void setFocusedAction(ScanProgressActionIcon actionIcon) {
        focusedAction = actionIcon;
    }

    /**
     * @return
     */
    public ScanProgressActionIcon getFocusedAction() {
        return focusedAction;
    }

    /**
     * @param scan
     */
    public void updateValues(ActiveScan scan, HostProcess hp) {
        setHostProcess(hp);

        // Update total elapsed time and request count
        Date end = (scan.getTimeFinished() == null) ? new Date() : scan.getTimeFinished();
        long elapsed = end.getTime() - scan.getTimeStarted().getTime();
        totTime = getElapsedTimeLabel(elapsed);
        totRequests = Integer.toString(scan.getTotalRequests());

        this.fireTableDataChanged();
    }

    private void setHostProcess(HostProcess hp) {
        if (this.hp == hp) {
            for (ScanProgressItem spi : values) {
                spi.refresh();
            }
            return;
        }

        this.hp = hp;
        values.clear();

        for (Plugin plugin : hp.getCompleted()) {
            values.add(new ScanProgressItem(hp, plugin, ScanProgressItem.STATUS_COMPLETED));
        }

        for (Plugin plugin : hp.getRunning()) {
            values.add(new ScanProgressItem(hp, plugin, ScanProgressItem.STATUS_RUNNING));
        }

        for (Plugin plugin : hp.getPending()) {
            values.add(new ScanProgressItem(hp, plugin, ScanProgressItem.STATUS_PENDING));
        }
    }

    /**
     * Inner method for elapsed time label formatting
     *
     * @param elapsed the time in milliseconds
     * @return the label with the elapsed time in readable format
     */
    private String getElapsedTimeLabel(long elapsed) {
        return (elapsed >= 0)
                ? String.format(
                        "%02d:%02d.%03d",
                        elapsed / 60000, (elapsed % 60000) / 1000, (elapsed % 1000))
                : null;
    }
}
