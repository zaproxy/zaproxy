/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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

package org.zaproxy.zap.extension.autoupdate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsBaseTableModel;

public class OptionsAutoupdateDirsTableModel extends AbstractMultipleOptionsBaseTableModel<File> {

	private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
            Constant.messages.getString("cfu.options.table.header.dir")};
    
	private static final int COLUMN_COUNT = COLUMN_NAMES.length;
	
    private List<File> tokens = new ArrayList<>(0);
    
    public OptionsAutoupdateDirsTableModel() {
        super();
    }
    
    @Override
    public List<File> getElements() {
        return tokens;
    }

    /**
     * @param tokens The tokens to set.
     */
    public void setFiles(List<File> files) {
		this.tokens = new ArrayList<>(tokens.size());
		
		for (File file : files) {
			this.tokens.add(file);
		}
    	
  	  	fireTableDataChanged();
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
        return String.class;
    }

    @Override
    public int getRowCount() {
        return tokens.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            return getElement(rowIndex).getAbsolutePath();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }
    
}
