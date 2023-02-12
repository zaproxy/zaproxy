/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

@SuppressWarnings("serial")
public class OptionsGlobalExcludeURLTableModel
        extends AbstractMultipleOptionsTableModel<GlobalExcludeURLParamToken> {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("options.globalexcludeurl.table.header.enabled"),
        Constant.messages.getString("options.globalexcludeurl.table.header.description"),
        Constant.messages.getString("options.globalexcludeurl.table.header.token")
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<GlobalExcludeURLParamToken> tokens = new ArrayList<>(0);

    public OptionsGlobalExcludeURLTableModel() {
        super();
    }

    @Override
    public List<GlobalExcludeURLParamToken> getElements() {
        return tokens;
    }

    /** @param tokens The tokens to set. */
    public void setTokens(List<GlobalExcludeURLParamToken> tokens) {
        this.tokens = new ArrayList<>(tokens.size());

        for (GlobalExcludeURLParamToken token : tokens) {
            this.tokens.add(new GlobalExcludeURLParamToken(token));
        }

        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == 0) {
            return Boolean.class;
        }
        return String.class;
    }

    @Override
    public int getRowCount() {
        return tokens.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return getElement(rowIndex).isEnabled();
            case 1:
                return getElement(rowIndex).getDescription();
            case 2:
                return getElement(rowIndex).getRegex();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (aValue instanceof Boolean) {
                tokens.get(rowIndex).setEnabled((Boolean) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
