/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.zaproxy.zap.extension.custompages;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

/** A table model for holding a set of DefaultCustomPage, for a {@link Context}. */
@SuppressWarnings("serial")
class CustomPageTableModel extends AbstractMultipleOptionsTableModel<CustomPage> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4463944219657112162L;

    /** The Constant defining the table column names. */
    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("custompages.table.header.enabled"),
        Constant.messages.getString("custompages.table.header.content"),
        Constant.messages.getString("custompages.table.header.contentlocation"),
        Constant.messages.getString("custompages.table.header.isregex"),
        Constant.messages.getString("custompages.table.header.type")
    };

    private List<CustomPage> customPages = new ArrayList<>();

    /**
     * Instantiates a new custom pages table model. An internal copy of the provided list is stored.
     *
     * @param customPages the list of custom pages
     */
    public CustomPageTableModel(List<CustomPage> customPages) {
        this.customPages = new ArrayList<>(customPages);
    }

    /** Instantiates a new user table model. */
    public CustomPageTableModel() {
        this.customPages = new ArrayList<>();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public int getRowCount() {
        return customPages.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        CustomPage cp = customPages.get(rowIndex);

        if (cp == null) {
            return null;
        }
        switch (columnIndex) {
            case 0:
                return cp.isEnabled();
            case 1:
                return cp.getPageMatcher();
            case 2:
                return cp.getPageMatcherLocation().getName();
            case 3:
                return cp.isRegex();
            case 4:
                return cp.getType().getName();
            default:
                return null;
        }
    }

    @Override
    public List<CustomPage> getElements() {
        return getCustomPages();
    }

    /**
     * Gets the internal list of defaultCustomPages managed by this model.
     *
     * @return the defaultCustomPages
     */
    public List<CustomPage> getCustomPages() {
        return customPages;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0: // Enabled Column
            case 3: // IsRegex Column
                return true;
            default:
                return false;
        }
    }

    /**
     * Sets a new list of defaultCustomPages for this model. An internal copy of the provided list
     * is stored.
     *
     * @param list the list of custom pages (expects non-null list)
     */
    public void setCustomPages(List<CustomPage> list) {
        this.customPages = new ArrayList<>(list);
        this.fireTableDataChanged();
    }

    /** Removes all the defaultCustomPages for this model. */
    public void removeAllCustomPages() {
        this.customPages = new ArrayList<>();
        this.fireTableDataChanged();
    }

    /**
     * Adds a new custom page to this model
     *
     * @param cp the custom page to be added
     */
    public void addCustomPage(CustomPage cp) {
        this.customPages.add(cp);
        this.fireTableRowsInserted(this.customPages.size() - 1, this.customPages.size() - 1);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return Boolean.class;
            case 4:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!(aValue instanceof Boolean)) {
            return;
        }
        if (columnIndex == 0) {
            customPages.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
            fireTableCellUpdated(rowIndex, columnIndex);
        } else if (columnIndex == 3) {
            customPages.get(rowIndex).setRegex(((Boolean) aValue).booleanValue());
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
