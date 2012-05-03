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
// ZAP: 2012/04/25 Added type argument to generic type and changed to use the
// method Boolean.valueOf.
// ZAP: 2012/05/03 Moved a statement in the method setValueAt(Object, int , int).
package org.zaproxy.zap.extension.ascan;

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

	private static final long serialVersionUID = 1L;
	private static final String[] columnNames = {"Category", "Enabled"};
    private List<Plugin> allPlugins = new Vector<Plugin>();
    
    /**
     * @param allPlugins The allPlugins to set.
     */
    public void setAllPlugins(List<Plugin> allPlugins) {
        this.allPlugins = allPlugins;
    }
    /**
     * 
     */
    public AllCategoryTableModel() {
    }
    
    public void setTable(List<Plugin> allPlugins) {
        setAllPlugins(allPlugins);
        fireTableDataChanged();        
    }

    @Override
    // ZAP: Added the type argument.
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
        
        switch (col) {
        	case 0:	break;
        	case 1: setPluginCategoryEnabled(row, ((Boolean) value).booleanValue());
        			fireTableCellUpdated(row, col);
        			break;
        }
        // ZAP: Moved the statement "fireTableCellUpdated(row, col);" to the
        // above switch case 1.
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
        return Category.length();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int col) {
        Object result = null;
        switch (col) {
        	case 0:	result = Category.getName(row);
        			break;
        	case 1: // ZAP: Changed to use the method Boolean.valueOf.
        			result = Boolean.valueOf(isPluginCategoryEnabled(row));
        			break;
        	default: result = "";
        }
        return result;
    }

    private boolean isPluginCategoryEnabled(int category) {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = allPlugins.get(i);
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
            Plugin plugin = allPlugins.get(i);
            if (plugin.getCategory() != category) {
                continue;
            }
            plugin.setEnabled(enabled);
        }
        
    }
    
    void setAllCategoryEnabled(boolean enabled) {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = allPlugins.get(i);
            plugin.setEnabled(enabled);            
        }
        fireTableDataChanged();        

    }
    
    boolean isAllCategoryEnabled() {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = allPlugins.get(i);
            if (!plugin.isEnabled()) {
                return false;
            }
        }
        return true;
        
    }
}
