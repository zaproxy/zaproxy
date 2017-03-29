/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 ZAP development team
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

package org.zaproxy.zap.view;

import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;

public class ContextListTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final String[] columnNames = {
				Constant.messages.getString("context.list.table.index"),
				Constant.messages.getString("context.list.table.name"),
				Constant.messages.getString("context.list.table.inscope")};
    
    private List<Object[]> values = Collections.emptyList();
    
    public ContextListTableModel() {
        super();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return values.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object[] value = this.values.get(row);
        return value[col];
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:	return false;
		case 1:	return false;
		case 2: return false;		// TODO ideally want to be able to change this here...
		default: return false;
		}
    }
    
    
    @Override
    public void setValueAt(Object value, int row, int col) {
    	if (col == 2) {
    		this.values.get(row)[col] = value;
    		fireTableCellUpdated(row, col);
    	}
    }

	@Override
	public String getColumnName(int col) {
        return columnNames[col];
    }
    
	@Override
	public Class<?> getColumnClass(int c) {
		switch (c) {
		case 0:	return Integer.class;
		case 1:	return String.class;
		case 2: return Boolean.class;
    	}
        return null;
        
    }

	public List<Object[]> getValues() {
		return values;
	}

	public void setValues(List<Object[]> values) {
		this.values = values;
		this.fireTableDataChanged();
	}

	public void addValues(Object[] values) {
		this.values.add(values);
		this.fireTableDataChanged();
	}

}
