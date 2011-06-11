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
package org.parosproxy.paros.extension.scanner;

import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.parosproxy.paros.core.scanner.Plugin;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CategoryTableModel extends DefaultTableModel {

    private static final String[] columnNames = {"Test Name", "Enabled"};
    private Vector listTestCategory = new Vector();
    
    /**
     * 
     */
    public CategoryTableModel() {
    }
    
    public void setTable(int category, List allTest) {
        listTestCategory.clear();
        for (int i=0; i<allTest.size(); i++) {
            Plugin test = (Plugin) allTest.get(i);
            if (test.getCategory() == category) {
                listTestCategory.add(test);
            }
        }
        fireTableDataChanged();
        
    }

    public Class getColumnClass(int c) {
        if (c == 1) {
            return Boolean.class;
        }
        return String.class;
        
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return true;
        }
        return false;
    }
    
    public void setValueAt(Object value, int row, int col) {
        
        Plugin test = (Plugin) listTestCategory.get(row);
        Object result = null;
        switch (col) {
        	case 0:	break;
        	case 1: test.setEnabled(((Boolean) value).booleanValue());
        			break;
        }
        fireTableCellUpdated(row, col);
    }
    
    public int getColumnCount() {
        return 2;
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return getTestList().size();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
        Plugin test = (Plugin) listTestCategory.get(row);
        Object result = null;
        switch (col) {
        	case 0:	result = test.getName();
        			break;
        	case 1: result = new Boolean(test.isEnabled());
        			break;
        	default: result = "";
        }
        return result;
    }
    
    private List getTestList() {
        if (listTestCategory == null) {
            listTestCategory = new Vector();
        }
        return listTestCategory;
    }
    
}
