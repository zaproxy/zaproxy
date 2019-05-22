/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class SingleColumnTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private  String[] columnNames = null;
    
    private List<String> lines = new ArrayList<>();
    private boolean editable = true;
    
    public SingleColumnTableModel(String columnName) {
        super();
        this.columnNames = new String[] {columnName};
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        return lines.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        return lines.get(row);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return this.editable;
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
    	if (editable) {
	    	lines.set(row, (String)value);
	        checkAndAppendNewRow();
	        fireTableCellUpdated(row, col);
    	}
    }

    /**
     * @return Returns the tokens.
     */
    public List<String> getLines() {
    	List<String> newList = new ArrayList<>(lines);
    	Iterator<String> it=newList.iterator();
    	while(it.hasNext())
    		if(it.next().equals(""))
    			it.remove();
        return newList;
    }
    /**
     * @param lines The tokens to set.
     */
    public void setLines(List<String> lines) {
    	if (lines == null) {
    		this.lines = new ArrayList<>();
    	} else {
    		this.lines = new ArrayList<>(lines);
    	}
    	if (this.editable) {
    		checkAndAppendNewRow();
    	}
  	  	fireTableDataChanged();
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    private void checkAndAppendNewRow() {
        String auth = null;
        if (lines.size() > 0) {
            auth =  lines.get(lines.size()-1);
            if (!auth.equals("")) {
                auth = "";
                lines.add(auth);
            }
        } else {
            auth = "";
            lines.add(auth);
        }
    }
    
    @Override
	public Class<String> getColumnClass(int c) {
        return String.class;
        
    }

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
    
}
