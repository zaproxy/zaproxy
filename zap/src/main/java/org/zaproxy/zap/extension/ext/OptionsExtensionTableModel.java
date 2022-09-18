/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.ext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.Extension;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.control.ExtensionFactory;

@SuppressWarnings("serial")
public class OptionsExtensionTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] columnNames = {
        Constant.messages.getString("options.ext.label.enabled"),
        Constant.messages.getString("options.ext.label.core"),
        Constant.messages.getString("options.ext.label.author"),
        Constant.messages.getString("options.ext.label.extension")
    };

    private List<Extension> extensions = ExtensionFactory.getAllExtensions();

    private static Logger log = LogManager.getLogger(OptionsExtensionTableModel.class);

    private Map<String, Boolean> extensionsState = new HashMap<>();

    public OptionsExtensionTableModel() {
        super();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return extensions.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        Extension ext = getExtension(row);
        if (ext != null) {
            try {
                switch (col) {
                    case 0:
                        return getEnabledState(ext);
                    case 1:
                        if (ext.isCore()) {
                            return Constant.messages.getString("options.ext.label.iscore");
                        }
                        return "";
                    case 2:
                        return ext.getAuthor();
                    case 3:
                        return ext.getUIName();
                }
            } catch (Exception e) {
                log.error("Failed on extension {}", ext.getName(), e);
            }
        }
        return null;
    }

    private boolean getEnabledState(Extension extension) {
        Boolean enabledState = extensionsState.get(extension.getName());
        if (enabledState == null) {
            return true;
        }
        return enabledState;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            Extension selectedExtension = getExtension(rowIndex);
            AddOn addOn = selectedExtension.getAddOn();
            if (addOn != null && addOn.isMandatory()) {
                return false;
            }
            // Dont allow enabled core extensions to be edited via the UI (can edit the config file
            // directly;)
            if (selectedExtension.isCore() && getEnabledState(selectedExtension)) {
                return false;
            }
            // Check dependencies
            List<Class<? extends Extension>> deps = selectedExtension.getDependencies();
            for (Class<? extends Extension> dep : deps) {
                Extension ext = getExtension(dep);
                if (ext == null || !getEnabledState(ext)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private Extension getExtension(Class<? extends Extension> c) {
        for (Extension ext : extensions) {
            if (ext.getClass().equals(c)) {
                return ext;
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            extensionsState.put(getExtension(row).getName(), (Boolean) value);
            fireTableCellUpdated(row, col);
            // En/Disable dependencies
            enableDependants(getExtension(row), (Boolean) value);
        }
    }

    private void enableDependants(Extension extension, Boolean enabled) {
        int row = 0;
        for (Extension ext : extensions) {
            if (ext.getDependencies().contains(extension.getClass())) {
                extensionsState.put(ext.getName(), enabled);
                this.fireTableCellUpdated(row, 0);
                enableDependants(ext, enabled);
            }
            row++;
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == 0) {
            return Boolean.class;
        }
        return String.class;
    }

    protected Extension getExtension(int row) {
        return extensions.get(row);
    }

    void setExtensionsState(Map<String, Boolean> extensionsState) {
        this.extensionsState = extensionsState;
        fireTableDataChanged();
    }

    Map<String, Boolean> getExtensionsState() {
        return extensionsState;
    }
}
