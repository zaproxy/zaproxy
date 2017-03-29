/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 ZAP development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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

public class ScanProgressTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;
    private static final String[] columnNames = {
        Constant.messages.getString("ascan.progress.table.name"),
        Constant.messages.getString("ascan.policy.table.strength"),
        Constant.messages.getString("ascan.progress.table.progress"),
        Constant.messages.getString("ascan.progress.table.time"),
        Constant.messages.getString("ascan.progress.table.reqs"),
        Constant.messages.getString("ascan.progress.table.status"),
    };
    
    private List<ScanProgressItem> values;
    private List<ScanProgressActionIcon> actions = new ArrayList<ScanProgressActionIcon>();
    private ScanProgressActionIcon focusedAction;
    private String totRequests;
    private String totTime;

    public ScanProgressTableModel() {
        super();
        values = new ArrayList<ScanProgressItem>();
        focusedAction = null;
    }

    /**
     * 
     * @return 
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * 
     * @return 
     */
    @Override
    public int getRowCount() {
        if (values == null) {
            return 0;
        }
        
        // Add other 2 rows for the final table values...
        return values.size() + 2;
    }

    /**
     * 
     * @param row
     * @param col
     * @return 
     */
    @Override
    public Object getValueAt(int row, int col) {
        
        // First check if we're showing the plugin status list
        if (row < values.size()) {
            
            // It's an entry so show the correct values
            final ScanProgressItem item = values.get(row);
            switch (col) {
                case 0:
                    return item.getNameLabel();
                    
                case 1:
                    return item.getAttackStrenghtLabel();
                    
                case 2:
                    if (item.isCompleted() || item.isRunning() || item.isSkipped()) {
                        return item;
                        
                    } else {
                        return null;
                    }

                case 3:
                    return getElapsedTimeLabel(item.getElapsedTime());

                case 4:
                	return item.getReqCount();
                	
                case 5:
                    ScanProgressActionIcon action = null;
                    if (item.isCompleted() || item.isRunning() || item.isSkipped()) {
                        if (row < actions.size()) {
                            action = actions.get(row);
                            action.updateStatus(item);

                        } else {
                            action = new ScanProgressActionIcon(item);
                            actions.add(action);
                        }
                    }

                    return action;

                default:
                    return null;
            }
            
        // We're in the summary "region", first print an empty
        // line and then two line of summary (tot time and to requests)
        // Maybe could be done in a better way, for example using
        // a dedicated panel positioned on top/bottom of the dialog
        } else if (row == values.size()) {
            // The first line after values should be empty
            return null;
            
        } else if (row == (values.size() + 1)) {
            // The second line after values should contains the totals
            switch (col) {
                case 0:
                    return Constant.messages.getString("ascan.progress.label.totals");                    
                
                case 3:
                    return totTime;
                    
                case 4:
                    return totRequests;                    
                
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * 
     * @param rowIndex
     * @param columnIndex
     * @return 
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    /**
     * 
     * @param col
     * @return 
     */
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    /**
     * 
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
     * 
     * @return 
     */
    public List<ScanProgressItem> getValues() {
        return values;
    }

    /**
     * 
     * @param icon 
     */
    public void setFocusedAction(ScanProgressActionIcon actionIcon) {
        focusedAction = actionIcon;
    }

    /**
     * 
     * @return 
     */
    public ScanProgressActionIcon getFocusedAction() {
        return focusedAction;
    }
    
    /**
     * 
     * @param scan 
     */
    public void updateValues(ActiveScan scan, HostProcess hp) {
        values.clear();
        
        // Iterate all Plugins
        for (Plugin plugin : hp.getCompleted()) {
            values.add(new ScanProgressItem(hp, plugin, ScanProgressItem.STATUS_COMPLETED));
        }

        for (Plugin plugin : hp.getRunning()) {
            values.add(new ScanProgressItem(hp, plugin, ScanProgressItem.STATUS_RUNNING));
        }

        for (Plugin plugin : hp.getPending()) {
            values.add(new ScanProgressItem(hp, plugin, ScanProgressItem.STATUS_PENDING));
        }
        
        // Update total elapsed time a and request count
        Date end = (scan.getTimeFinished() == null) ? new Date() : scan.getTimeFinished();
        long elapsed = end.getTime() - scan.getTimeStarted().getTime();
        totTime = getElapsedTimeLabel(elapsed);
        totRequests = Integer.toString(scan.getTotalRequests());
                
        this.fireTableDataChanged();        
    }

    /**
     * Inner method for elapsed time label formatting
     * @param elapsed the time in milliseconds
     * @return the label with the elapsed time in readable format
     */
    private String getElapsedTimeLabel(long elapsed) {
        return (elapsed >= 0) ?
                String.format("%02d:%02d.%03d", elapsed / 60000, (elapsed % 60000) / 1000, (elapsed % 1000)) :
                null;
    }
}
