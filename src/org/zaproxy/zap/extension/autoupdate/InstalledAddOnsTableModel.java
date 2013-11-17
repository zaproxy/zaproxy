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
import org.zaproxy.zap.extension.autoupdate.AddOnWrapper.Status;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;


public class InstalledAddOnsTableModel extends AbstractMultipleOptionsTableModel<AddOnWrapper> {

	private static final long serialVersionUID = 1L;

	private static final String[] COLUMN_NAMES = {
		Constant.messages.getString("cfu.table.header.name"),
		Constant.messages.getString("cfu.table.header.desc"),
		Constant.messages.getString("cfu.table.header.update"),
		""};
    
	private static final int COLUMN_COUNT = COLUMN_NAMES.length;
    
	private List <AddOnWrapper> addOns = new ArrayList<>();
    
    /**
     * 
     */
    public InstalledAddOnsTableModel() {
        super();
    }

    public InstalledAddOnsTableModel(List <AddOnWrapper> addOns) {
        super();
        this.setAddOns(addOns);
    }

    /**
     * @param defns
     */
    public void setAddOns(List <AddOnWrapper> addOns) {
    	this.addOns = addOns;
        fireTableDataChanged();
    }

    @Override
    protected List<AddOnWrapper> getElements() {
        return addOns;
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
        return addOns.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return getElement(rowIndex).getAddOn().getName();
        case 1:
            return getElement(rowIndex).getAddOn().getDescription();
        case 2:
        	int progress = getElement(rowIndex).getProgress();
        	if (getElement(rowIndex).isFailed()) {
        		return Constant.messages.getString("cfu.table.label.failed");
        	} else if (progress > 0) {
        		return progress + "%";
        	} else if (getElement(rowIndex).getStatus().equals(AddOnWrapper.Status.newVersion)) {
        		return Constant.messages.getString("cfu.table.label.update");
        	} else if (getElement(rowIndex).getStatus().equals(AddOnWrapper.Status.latest)) {
        		return "";
        	} else {
        		return "";
        	}
        case 3:
            return getElement(rowIndex).isEnabled();
        }
        return null;
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 3) {
            if (aValue instanceof Boolean) {
                getElement(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
                this.fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
    	if (columnIndex == 3 && getElement(rowIndex).getProgress() == 0) {
    		// Its the 'enabled' checkbox, and no download is in progress
    		return true;
    	}
        return false;
    }
    
    public boolean canUpdateSelected() {
    	boolean enable = false;
    	for (AddOnWrapper addon : this.addOns) {
    		if (addon.isEnabled()) {
    			if (addon.getStatus().equals(Status.newVersion)) {
    				enable = true;
    			} else {
    				return false;
    			}
    		}
    	}
    	return enable;
    }
    
    public boolean canUninstallSelected() {
    	boolean enable = false;
    	for (AddOnWrapper addon : this.addOns) {
    		if (addon.isEnabled()) {
   				return true;
    		}
    	}
    	return enable;
    }
    
}
