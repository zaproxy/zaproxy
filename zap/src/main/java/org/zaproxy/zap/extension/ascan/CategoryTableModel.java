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
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2016/04/04 Use StatusUI in scanners' dialogues
// ZAP: 2017/06/05 Return AlertThreshold.OFF if the plugin is disabled.
// ZAP: 2018/01/30 Do not rely on default locale for upper/lower case conversions (when locale is
// not important).
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2022/08/04 Ensure scan rule names are non-blank (Issue 7228)
package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.table.DefaultTableModel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Plugin;
import org.parosproxy.paros.core.scanner.Plugin.AlertThreshold;
import org.parosproxy.paros.core.scanner.Plugin.AttackStrength;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.StatusUI;

@SuppressWarnings("serial")
public class CategoryTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;
    private Map<String, String> i18nToStr = null;

    // ZAP: i18n
    private static final String[] columnNames = {
        Constant.messages.getString("ascan.policy.table.testname"),
        Constant.messages.getString("ascan.policy.table.threshold"),
        Constant.messages.getString("ascan.policy.table.strength"),
        Constant.messages.getString("ascan.policy.table.status")
    };

    private static final int STATUS_COLUMN_IDX = 3;

    private List<PluginWrapper> listTestCategory;

    private PluginFactory pluginFactory;
    private int category;
    private AlertThreshold defaultThreshold;

    public CategoryTableModel() {}

    public void setTable(
            int category, PluginFactory pluginFactory, AlertThreshold defaultThreshold) {

        listTestCategory.clear();
        this.pluginFactory = pluginFactory;
        this.category = category;
        this.defaultThreshold = defaultThreshold;
        for (Plugin test : pluginFactory.getAllPlugin()) {
            if (test.getCategory() == category) {
                listTestCategory.add(
                        new PluginWrapper(test, View.getSingleton().getStatusUI(test.getStatus())));
            }
        }
        fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == STATUS_COLUMN_IDX) {
            return StatusUI.class;
        }
        return String.class;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: // Name
                return false;
            case 1: // Strength
                return true;
            case 2: // Alert
                return true;
            case STATUS_COLUMN_IDX:
                return false;
            default:
                return false;
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        Plugin test = listTestCategory.get(row).getPlugin();
        switch (col) {
            case 0:
                break;
            case 1:
                AlertThreshold af = AlertThreshold.valueOf(i18nToStr((String) value));
                boolean enable = !AlertThreshold.OFF.equals(af);
                if (test.isEnabled() != enable) {
                    if (enable) {
                        String[] dependencies = test.getDependency();
                        if (dependencies != null && dependencies.length != 0) {
                            List<Plugin> allDeps = new ArrayList<>(dependencies.length);
                            if (!pluginFactory.addAllDependencies(test, allDeps)) {
                                View.getSingleton()
                                        .showWarningDialog(
                                                Constant.messages.getString(
                                                        "ascan.policy.unfulfilled.dependencies"));
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
                        for (Plugin plugin : pluginFactory.getDependentPlugins(test)) {
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
            case 2:
                test.setAttackStrength(AttackStrength.valueOf(i18nToStr((String) value)));
                fireTableCellUpdated(row, col);
                break;
        }
    }

    private void setPluginsEnabled(List<Plugin> plugins, boolean enabled) {
        AlertThreshold alertThreshold = enabled ? defaultThreshold : AlertThreshold.OFF;
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
            if (plugin.equals(listTestCategory.get(i).getPlugin())) {
                return i;
            }
        }
        return -1;
    }

    private String strToI18n(String str) {
        // I18n's threshold and strength enums
        return Constant.messages.getString("ascan.policy.level." + str.toLowerCase(Locale.ROOT));
    }

    private String i18nToStr(String str) {
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
        PluginWrapper wrapper = listTestCategory.get(row);
        switch (col) {
            case 0:
                return wrapper.getPlugin().getDisplayName();
            case 1:
                if (!wrapper.getPlugin().isEnabled()) {
                    return AlertThreshold.OFF;
                }
                return strToI18n(wrapper.getPlugin().getAlertThreshold(true).name());
            case 2:
                return strToI18n(wrapper.getPlugin().getAttackStrength(true).name());
            case STATUS_COLUMN_IDX:
                return wrapper.getStatus();
            default:
                return "";
        }
    }

    private List<PluginWrapper> getTestList() {
        if (listTestCategory == null) {
            listTestCategory = new ArrayList<>();
        }
        return listTestCategory;
    }

    private static class PluginWrapper {

        private final Plugin plugin;
        private final StatusUI status;

        public PluginWrapper(Plugin plugin, StatusUI status) {
            this.plugin = plugin;
            this.status = status;
        }

        public Plugin getPlugin() {
            return plugin;
        }

        public StatusUI getStatus() {
            return status;
        }
    }
}
