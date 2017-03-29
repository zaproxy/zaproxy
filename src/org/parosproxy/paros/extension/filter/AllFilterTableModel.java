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
// ZAP: 2011/04/16 i18n
// ZAP: 2012/04/24 Added type arguments to generic types, removed unnecessary
// casts, removed unused variable and changed to use the method Boolean.valueOf.
// ZAP: 2012/05/03 Changed the method isCellEditable(int, int) to use directly
// the returning value of Filter.isPropertyExists(). Moved a statement in the
// method setValueAt(Object, int , int).
// ZAP: 2013/01/25 Removed the "(non-Javadoc)" comments.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments

package org.parosproxy.paros.extension.filter;

import java.util.List;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.parosproxy.paros.Constant;

public class AllFilterTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	
	private static final String[] columnNames = {
    	Constant.messages.getString("filter.table.name"), Constant.messages.getString("filter.table.enabled"), ""};
    // ZAP: Added the type argument.
    private List<Filter> allFilters = null;
    
    /**
     * @param allPlugins The allPlugins to set.
     */
    // ZAP: Added the type argument.
    private void setAllFilters(List<Filter> allFilters) {
        this.allFilters = allFilters;
    }

    public AllFilterTableModel() {
        // ZAP: Added the type argument.
        allFilters = new Vector<>();
    }
    
    // ZAP: Added the type argument.
    public void setTable(List<Filter> allFilters) {
        setAllFilters(allFilters);
        fireTableDataChanged();        
    }

    // ZAP: Added the type argument.
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
    public boolean isCellEditable(int row, int col) {
        boolean result = false;
        // ZAP: Removed unnecessary cast.
        Filter filter = getAllFilters().get(row);
        switch (col) {
        	case 0:	result = false;
        			break;
        	case 1: result = true;
        			break;
        	case 2: // ZAP: Changed to use the returned value.
        			result = filter.isPropertyExists();
        }
        return result;
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        
        // ZAP: Removed unused variable (Object result) and unnecessary cast.
        Filter filter = allFilters.get(row);
        switch (col) {
        	case 0:	break;
        	case 1: filter.setEnabled(((Boolean) value).booleanValue());
        			fireTableCellUpdated(row, col);
        			break;
        	case 2: break;
        }
        // ZAP: Moved the statement "fireTableCellUpdated(row, col);" to the
        // above switch case 1.
    }
    
    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public int getRowCount() {
        return getAllFilters().size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object result = null;
        // ZAP: Removed unnecessary cast.
        Filter filter = getAllFilters().get(row);
        switch (col) {
        	case 0:	result = filter.getName();
        			break;
        	case 1: // ZAP: Changed to use the method Boolean.valueOf.
        			result = Boolean.valueOf(filter.isEnabled());
        			break;
        	case 2: if (filter.isPropertyExists()) {
        	    		result = "...";
        			} else {
        			    result = "";
        			}
        			break;
        	default: result = "";
        }
        return result;
    }
    
    void setAllFilterEnabled(boolean enabled) {
        for (int i=0; i<getAllFilters().size(); i++) {
            // ZAP: Removed unnecessary cast.
            Filter filter = getAllFilters().get(i);
            filter.setEnabled(enabled);            
        }
        fireTableDataChanged();        

    }
    
    /**
     * @return Returns the allFilters.
     */
    // ZAP: Added the type arguments.
    public List<Filter> getAllFilters() {
        if (allFilters == null) {
            allFilters = new Vector<>();
        }
        return allFilters;
    }
}


