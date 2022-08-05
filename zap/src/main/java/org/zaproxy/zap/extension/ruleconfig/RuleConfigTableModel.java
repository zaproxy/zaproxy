/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.zaproxy.zap.extension.ruleconfig;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;

@SuppressWarnings("serial")
public class RuleConfigTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("ruleconfig.options.table.header.key"),
        Constant.messages.getString("ruleconfig.options.table.header.default"),
        Constant.messages.getString("ruleconfig.options.table.header.value")
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<RuleConfig> rcs = new ArrayList<>(0);

    public RuleConfigTableModel() {
        super();
    }

    protected List<RuleConfig> getElements() {
        return rcs;
    }

    /** @param rcs The ruleconfigs to set. */
    public void setRuleConfigs(List<RuleConfig> rcs) {
        this.rcs = new ArrayList<>(rcs.size());

        for (RuleConfig token : rcs) {
            this.rcs.add(token);
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
        return String.class;
    }

    @Override
    public int getRowCount() {
        return rcs.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return rcs.get(rowIndex).getKey();
            case 1:
                return rcs.get(rowIndex).getDefaultValue();
            case 2:
                return rcs.get(rowIndex).getValue();
        }
        return null;
    }

    public void setRuleConfigValue(String key, String value) {
        int row = 0;
        for (RuleConfig token : rcs) {
            // Not super efficient, but we dont expect there to be too many of these
            if (token.getKey().equals(key)) {
                token.setValue(value);
                this.fireTableCellUpdated(row, 2);
                return;
            }
            row++;
        }
    }
}
