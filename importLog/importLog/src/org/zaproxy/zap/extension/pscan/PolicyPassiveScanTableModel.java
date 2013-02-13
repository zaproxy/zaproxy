/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The Zed Attack Proxy team
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
package org.zaproxy.zap.extension.pscan;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.DefaultTableModel;

import org.parosproxy.paros.Constant;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PolicyPassiveScanTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
    private static final String[] columnNames = {
									Constant.messages.getString("ascan.policy.table.testname"), 
									Constant.messages.getString("ascan.policy.table.enabled")};

    private List<PluginPassiveScanner> listScanners = new ArrayList<>();
    
    /**
     * 
     */
    public PolicyPassiveScanTableModel() {
    }
    
    public void addScanner (PluginPassiveScanner scanner) {
        listScanners.add(scanner);
        fireTableDataChanged();
    }
    
    public void removeScanner (PluginPassiveScanner scanner) {
        listScanners.remove(scanner);
        fireTableDataChanged();
    }
    
    @Override
	public Class<?> getColumnClass(int c) {
        if (c == 1) {
            return Boolean.class;
        }
        return String.class;
        
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return true;
        }
        return false;
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        
    	PluginPassiveScanner test = listScanners.get(row);
        switch (col) {
        	case 0:	break;
        	case 1: test.setEnabled(((Boolean) value).booleanValue());
        			test.save();
        			fireTableCellUpdated(row, col);
        			break;
        }
    }
    
    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
    	if (listScanners == null) {
    		return 0;
    	}
        return listScanners.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
    	PassiveScanner test = listScanners.get(row);
        Object result = null;
        switch (col) {
        	case 0:	result = test.getName();
        			break;
        	case 1: result = Boolean.valueOf(test.isEnabled());
        			break;
        	default: result = "";
        }
        return result;
    }
}
