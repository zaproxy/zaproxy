/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 psiinon@gmail.com
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

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AddOnUpdatesTableModel extends AbstractMultipleOptionsTableModel<AddOn> {

	private static final long serialVersionUID = 1L;

	private static final String[] COLUMN_NAMES_UPDATE = {
		Constant.messages.getString("cfu.table.header.id"),
		Constant.messages.getString("cfu.table.header.name"),
		Constant.messages.getString("cfu.table.header.version"),
		Constant.messages.getString("cfu.table.header.status"),
		Constant.messages.getString("cfu.table.header.progress"),
		Constant.messages.getString("cfu.table.header.update")};
    
	private static final String[] COLUMN_NAMES_BROWSE = {
		Constant.messages.getString("cfu.table.header.id"),
		Constant.messages.getString("cfu.table.header.name"),
		Constant.messages.getString("cfu.table.header.version"),
		Constant.messages.getString("cfu.table.header.status"),
		Constant.messages.getString("cfu.table.header.progress"),
		Constant.messages.getString("cfu.table.header.download")};
    
	private static final int COLUMN_COUNT = COLUMN_NAMES_UPDATE.length;
    
	private List <AddOn> addOns = new ArrayList<AddOn>();
	private boolean update;
    
    /**
     * 
     */
    public AddOnUpdatesTableModel(boolean update) {
        super();
        this.update = update;
    }

    public AddOnUpdatesTableModel(boolean update, List <AddOn> addOns) {
        super();
        this.update = update;
        this.setAddOns(addOns);
    }

    /**
     * @param defns
     */
    public void setAddOns(List <AddOn> addOns) {
    	this.addOns = addOns;
        fireTableDataChanged();
    }

    @Override
    protected List<AddOn> getElements() {
        return addOns;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public String getColumnName(int col) {
    	if (update) {
    		return COLUMN_NAMES_UPDATE[col];
    	} else {
    		return COLUMN_NAMES_BROWSE[col];
    	}
    }

    @Override
    public int getRowCount() {
        return addOns.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 5) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return getElement(rowIndex).getId();
        case 1:
            return getElement(rowIndex).getName();
        case 2:
            return getElement(rowIndex).getVersion();
        case 3:
            return Constant.messages.getString("cfu.status." + getElement(rowIndex).getStatus().toString());
        case 4:
        	int progress = getElement(rowIndex).getProgress();
        	if (getElement(rowIndex).isFailed()) {
        		return Constant.messages.getString("cfu.download.failed");
        	} else if (progress == 0) {
        		return "";
        	} else {
        		return progress + "%";
        	}
        case 5:
            return getElement(rowIndex).isEnabled();
        }
        return null;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 5) {
            if (aValue instanceof Boolean) {
                getElement(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
            }
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
    	if (columnIndex == 5 && getElement(rowIndex).getProgress() == 0) {
    		// Its the 'enabled' checkbox, and no download is in progress
    		return true;
    	}
        return false;
    }
    
}
