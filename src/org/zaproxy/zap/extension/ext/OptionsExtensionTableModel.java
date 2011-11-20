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

package org.zaproxy.zap.extension.ext;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.zaproxy.zap.control.ExtensionFactory;

public class OptionsExtensionTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final String[] columnNames = {
				Constant.messages.getString("options.ext.label.enabled"),
				Constant.messages.getString("options.ext.label.extension")};
    
    private List<Extension> extensions = ExtensionFactory.getAllExtensions();
    
    /**
     * 
     */
    public OptionsExtensionTableModel() {
        super();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return extensions.size();
    }

    public Object getValueAt(int row, int col) {
        Extension ext = extensions.get(row);
        if (ext != null) {
        	if (col == 0) {
        		return ext.isEnabled();
        	} else {
        		return ext.getDescription();
        	}
        }
        return null;
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
    	if (columnIndex == 0) {
    		return true;
    	}
        return false;
    }
    
    public void setValueAt(Object value, int row, int col) {
    	if (col == 0) {
    		extensions.get(row).setEnabled((Boolean) value);
    	}
        fireTableCellUpdated(row, col);
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }
    
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
    	if (c == 0) {
    		return Boolean.class;
    	}
        return String.class;
        
    }

	protected List<Extension> getExtensions() {
		return extensions;
	}
    
}
