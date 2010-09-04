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

import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.Plugin;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AllCategoryTableModel extends DefaultTableModel {

    private static final String[] columnNames = {"Category", "Enabled"};
    private List allPlugins = new Vector();
    
    /**
     * @param allPlugins The allPlugins to set.
     */
    public void setAllPlugins(List allPlugins) {
        this.allPlugins = allPlugins;
    }
    /**
     * 
     */
    public AllCategoryTableModel() {
    }
    
    public void setTable(List allPlugins) {
        setAllPlugins(allPlugins);
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
        
        Object result = null;
        switch (col) {
        	case 0:	break;
        	case 1: setPluginCategoryEnabled(row, ((Boolean) value).booleanValue());
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
        return Category.length();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
        Object result = null;
        switch (col) {
        	case 0:	result = Category.getName(row);
        			break;
        	case 1: result = new Boolean(isPluginCategoryEnabled(row));
        			break;
        	default: result = "";
        }
        return result;
    }

    private boolean isPluginCategoryEnabled(int category) {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = (Plugin) allPlugins.get(i);
            if (plugin.getCategory() != category) {
                continue;
            }
            if (!plugin.isEnabled()) {
                return false;
            }
        }
        return true;
    }
    
    private void setPluginCategoryEnabled(int category, boolean enabled) {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = (Plugin) allPlugins.get(i);
            if (plugin.getCategory() != category) {
                continue;
            }
            plugin.setEnabled(enabled);
        }
        
    }
    
    void setAllCategoryEnabled(boolean enabled) {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = (Plugin) allPlugins.get(i);
            plugin.setEnabled(enabled);            
        }
        fireTableDataChanged();        

    }
    
    boolean isAllCategoryEnabled() {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = (Plugin) allPlugins.get(i);
            if (!plugin.isEnabled()) {
                return false;
            }
        }
        return true;
        
    }
}
