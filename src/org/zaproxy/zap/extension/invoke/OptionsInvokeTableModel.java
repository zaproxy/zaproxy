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
package org.zaproxy.zap.extension.invoke;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;


public class OptionsInvokeTableModel extends AbstractMultipleOptionsTableModel<InvokableApp> {

	private static final long serialVersionUID = 1L;

	private static final String[] COLUMN_NAMES = {
	    Constant.messages.getString("invoke.options.table.header.enabled"),
        Constant.messages.getString("invoke.options.table.header.name"),
        Constant.messages.getString("invoke.options.table.header.command"),
        Constant.messages.getString("invoke.options.table.header.directory"),
        Constant.messages.getString("invoke.options.table.header.parameters"),
        Constant.messages.getString("invoke.options.table.header.ouput"),
        Constant.messages.getString("invoke.options.table.header.toNote")
	};
    
	private static final int COLUMN_COUNT = COLUMN_NAMES.length;
	
    private List<InvokableApp> listApps = new ArrayList<>(5);
    
    public OptionsInvokeTableModel() {
        super();
    }
    
    /**
     * @param apps The listAuth to set.
     */
    public void setListInvokableApps(List<InvokableApp> apps) {
        this.listApps = new ArrayList<>(apps.size());
        
        for (InvokableApp app : apps) {
            this.listApps.add(new InvokableApp(app));
        }
        
        fireTableDataChanged();
    }
    
    public List<InvokableApp> getListInvokableApps() {
        return new ArrayList<>(listApps);
    }

    @Override
    public List<InvokableApp> getElements() {
        return listApps;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public int getRowCount() {
        return listApps.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch(columnIndex) {
        case 0:
        case 5:
        case 6:
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return Boolean.valueOf(getElement(rowIndex).isEnabled());
        case 1:
            return getElement(rowIndex).getDisplayName();
        case 2:
            return getElement(rowIndex).getFullCommand();
        case 3:
            return getElement(rowIndex).getWorkingDirectory();
        case 4:
            return getElement(rowIndex).getParameters();
        case 5:
            return Boolean.valueOf(getElement(rowIndex).isCaptureOutput());
        case 6:
            return Boolean.valueOf(getElement(rowIndex).isOutputNote());
        }
        return null;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (aValue instanceof Boolean) {
                getElement(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0);
    }

}
