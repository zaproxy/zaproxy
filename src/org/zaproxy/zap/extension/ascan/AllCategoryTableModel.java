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
// ZAP: 2013/11/28 Issue 923: Allow individual rule thresholds and strengths to be set via GUI

package org.zaproxy.zap.extension.ascan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Category;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;


public class AllCategoryTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	private Map<String, String> i18nToStr = null;
	private static final String[] columnNames = {
            Constant.messages.getString("ascan.policy.table.category"),
    		Constant.messages.getString("ascan.policy.table.threshold"), 
    		Constant.messages.getString("ascan.policy.table.strength") };
    private List<Plugin> allPlugins = new Vector<>();
    
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
        return String.class;
        
    }
    
    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex > 0) {
            return true;
        }
        return false;
    }
    
    @Override
    public void setValueAt(Object value, int row, int col) {
        
        switch (col) {
        	case 0:	break;
        	case 1: 
        			if (value.toString().length() == 0) {
        				break;
        			}
        			setPluginCategoryThreshold(row, AlertThreshold.valueOf(i18nToStr((String)value)));
		            fireTableCellUpdated(row, col);
					break;
			case 2: 
	    			if (value.toString().length() == 0) {
	    				break;
	    			}
					setPluginCategoryStrength(row, AttackStrength.valueOf(i18nToStr((String)value)));
		        	fireTableCellUpdated(row, col);
		        	break;
        }
    }

    private String strToI18n (String str) {
    	// I18n's threshold and strength enums
    	return Constant.messages.getString("ascan.policy.level." + str.toLowerCase());
    }

    private String i18nToStr (String str) {
    	// Converts to i18n'ed names back to the enum names
    	if (i18nToStr == null) {
    		i18nToStr = new HashMap<String, String>();
    		for (AlertThreshold at : AlertThreshold.values()) {
    			i18nToStr.put(this.strToI18n(at.name()), at.name());
    		}
    		for (AttackStrength as : AttackStrength.values()) {
    			i18nToStr.put(this.strToI18n(as.name()), as.name());
    		}
    	}
    	return i18nToStr.get(str);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return Category.length();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Object result = null;
        switch (col) {
        	case 0:	result = Category.getName(row);
        			break;
        	case 1: result = getPluginCategoryThreshold(row);
        			break;
        	case 2: result = getPluginCategoryStrength(row);
					break;
        	default: result = "";
        }
        return result;
    }

    private String getPluginCategoryThreshold(int category) {
    	AlertThreshold at = null;
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = allPlugins.get(i);
            if (plugin.getCategory() != category) {
                continue;
            }
            if (at == null) {
            	at = plugin.getAlertThreshold(true);
            } else if ( ! at.equals(plugin.getAlertThreshold(true))) {
            	// Not all the same
            	return "";
            }
        }
        if (at == null) {
        	return "";
        }
        return strToI18n(at.name());
    }

    private String getPluginCategoryStrength(int category) {
    	AttackStrength at = null;
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = allPlugins.get(i);
            if (plugin.getCategory() != category) {
                continue;
            }
            if (at == null) {
            	at = plugin.getAttackStrength(true);
            } else if ( ! at.equals(plugin.getAttackStrength(true))) {
            	// Not all the same
            	return "";
            }
        }
        if (at == null) {
        	return "";
        }
        return strToI18n(at.name());
    }

    private void setPluginCategoryThreshold(int category, AlertThreshold at) {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = allPlugins.get(i);
            if (plugin.getCategory() != category) {
                continue;
            }
            plugin.setAlertThreshold(at);
			plugin.setEnabled(!AlertThreshold.OFF.equals(at));
        }
        
    }
    
    private void setPluginCategoryStrength(int category, AttackStrength at) {
        for (int i=0; i<allPlugins.size(); i++) {
            Plugin plugin = allPlugins.get(i);
            if (plugin.getCategory() != category) {
                continue;
            }
            plugin.setAttackStrength(at);
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
