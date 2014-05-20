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
// ZAP: 2013/01/27 Changed to only notify the listeners if the value was really changed.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/11/28 Issue 923: Allow individual rule thresholds and strengths to be set via GUI
// ZAP: 2014/05/20 Issue 377: Unfulfilled dependencies hang the active scan

package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.View;


public class CategoryTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	private Map<String, String> i18nToStr = null;
	
	// ZAP: i18n
	private static final String[] columnNames = {
		Constant.messages.getString("ascan.policy.table.testname"), 
		Constant.messages.getString("ascan.policy.table.threshold"), 
		Constant.messages.getString("ascan.policy.table.strength") };
	
    private Vector<Plugin> listTestCategory = new Vector<>();

    private int category;
    
    /**
     * 
     */
    public CategoryTableModel() {
    }
    
    public void setTable(int category, List<Plugin> allTest) {
        listTestCategory.clear();
        this.category = category ;
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
        Plugin test = listTestCategory.get(row);
        switch (col) {
        	case 0:	break;
        	case 1: AlertThreshold af = AlertThreshold.valueOf(i18nToStr((String)value));
                    boolean enable = !AlertThreshold.OFF.equals(af);
                    if (test.isEnabled() != enable) {
                        if (enable) {
                            String[] dependencies = test.getDependency();
                            if (dependencies != null && dependencies.length != 0) {
                                List<Plugin> allDeps = new ArrayList<>(dependencies.length);
                                if (!Control.getSingleton().getPluginFactory().addAllDependencies(test, allDeps)) {
                                    View.getSingleton().showWarningDialog(
                                            Constant.messages.getString("ascan.policy.unfulfilled.dependencies"));
                                    return;
                                }

                                List<Plugin> disabledDependencies = new ArrayList<>();
                                for (Plugin plugin : allDeps) {
                                    if (!plugin.isEnabled()) {
                                        disabledDependencies.add(plugin);
                                    }
                                }

                                if (!disabledDependencies.isEmpty()) {
                                    setPluginsEnabled(disabledDependencies, true);
                                }
                            }
                        } else {
                            List<Plugin> enabledDependents = new ArrayList<>();
                            for (Plugin plugin : Control.getSingleton().getPluginFactory().getDependentPlugins(test)) {
                                if (plugin.isEnabled()) {
                                    enabledDependents.add(plugin);
                                }
                            }
    
                            if (!enabledDependents.isEmpty()) {
                                setPluginsEnabled(enabledDependents, false);
                            }
                        }
                    }

                    test.setAlertThreshold(af);
                    test.setEnabled(enable);
                    fireTableCellUpdated(row, col);

        			break;
        	case 2: test.setAttackStrength(AttackStrength.valueOf(i18nToStr((String)value)));
                	fireTableCellUpdated(row, col);
                	break;
        }
    }
    
    private void setPluginsEnabled(List<Plugin> plugins, boolean enabled) {
        AlertThreshold defaultAlertThreshold = ((ScannerParam) Model.getSingleton()
                .getOptionsParam()
                .getParamSet(ScannerParam.class)).getAlertThreshold();
        AlertThreshold alertThreshold = enabled ? defaultAlertThreshold : AlertThreshold.OFF;
        for (Plugin plugin : plugins) {
            plugin.setEnabled(enabled);
            plugin.setAlertThreshold(alertThreshold);

            if (plugin.getCategory() == category) {
                int rowDep = getPluginRow(plugin);
                if (rowDep != -1) {
                    fireTableCellUpdated(rowDep, 1);
                }
            }
        }
    }

    private int getPluginRow(Plugin plugin) {
        for (int i = 0; i < listTestCategory.size(); i++) {
            if (plugin.equals(listTestCategory.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private String strToI18n (String str) {
    	// I18n's threshold and strength enums
    	return Constant.messages.getString("ascan.policy.level." + str.toLowerCase());
    }

    private String i18nToStr (String str) {
    	// Converts to i18n'ed names back to the enum names
    	if (i18nToStr == null) {
    		i18nToStr = new HashMap<>();
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
        return getTestList().size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Plugin test = listTestCategory.get(row);
        Object result = null;
        switch (col) {
        	case 0:	result = test.getName();
        			break;
        	case 1: result = strToI18n(test.getAlertThreshold(true).name());
        			break;
        	case 2: result = strToI18n(test.getAttackStrength(true).name());
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
