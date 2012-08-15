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

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class OptionsInvokeTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

	private static final String[] columnNames = {
		Constant.messages.getString("invoke.options.label.name"), 
		Constant.messages.getString("invoke.options.label.command")};
    
    private List<InvokableApp> listApps = new ArrayList<InvokableApp>();
    
    /**
     * 
     */
    public OptionsInvokeTableModel() {
        super();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return listApps.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
    	InvokableApp app = listApps.get(row);
        Object result = null;
        switch (col) {
        	case 0:	result = app.getDisplayName();
        			break;
        	case 1: result = app.getFullCommand();
        			break;
        	default: result = "";
        }
        return result;
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    /**
     * @param apps The listAuth to set.
     */
    public void setListInvokableApps(List<InvokableApp> apps) {
        this.listApps = new ArrayList<InvokableApp>(apps);
  	  	fireTableDataChanged();
    }
    
    public List<InvokableApp> getListInvokableApps() {
        return new ArrayList<InvokableApp>(listApps);
    }

    public void addInvokableApp(InvokableApp app) {
        this.listApps.add(app);
  	  	fireTableDataChanged();
    }

    public InvokableApp getInvokableApp(int index) {
    	return this.listApps.get(index);
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
	public Class<String> getColumnClass(int c) {
        return String.class;
    }

	public void replaceInvokableApp(int index, InvokableApp app) {
        this.listApps.remove(index);
        this.listApps.add(index, app);
  	  	fireTableDataChanged();
	}

	public void removeInvokableApp(int index) {
        this.listApps.remove(index);
  	  	fireTableDataChanged();
	}
    
}
