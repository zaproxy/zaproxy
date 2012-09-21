/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/03/14 Changed to use the internationalised strings.
// ZAP: 2012/04/25 Changed to use the method Boolean.valueOf.
package org.zaproxy.zap.extension.ascan;

import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CategoryTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	
	// ZAP: i18n
	private static final String[] columnNames = {
		Constant.messages.getString("ascan.policy.table.testname"), 
		Constant.messages.getString("ascan.policy.table.enabled") };
	
    private Vector<Plugin> listTestCategory = new Vector<>();
    
    /**
     * 
     */
    public CategoryTableModel() {
    }
    
    public void setTable(int category, List<Plugin> allTest) {
        listTestCategory.clear();
        for (int i=0; i<allTest.size(); i++) {
            Plugin test = allTest.get(i);
            if (test.getCategory() == category) {
                listTestCategory.add(test);
            }
        }
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
        
        Plugin test = listTestCategory.get(row);
        switch (col) {
        	case 0:	break;
        	case 1: test.setEnabled(((Boolean) value).booleanValue());
        			break;
        }
        fireTableCellUpdated(row, col);
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return getTestList().size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int col) {
        Plugin test = listTestCategory.get(row);
        Object result = null;
        switch (col) {
        	case 0:	result = test.getName();
        			break;
        	case 1: // ZAP: Changed to use the method Boolean.valueOf.
        			result = Boolean.valueOf(test.isEnabled());
        			break;
        	default: result = "";
        }
        return result;
    }
    
    private List<Plugin> getTestList() {
        if (listTestCategory == null) {
            listTestCategory = new Vector<>();
        }
        return listTestCategory;
    }
    
}
