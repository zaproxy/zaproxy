/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.lang3.tuple.MutablePair;
import org.parosproxy.paros.Constant;

@SuppressWarnings("serial")
class AlertTagsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    private List<MutablePair<String, String>> tags = new ArrayList<>();

    public Map<String, String> getTags() {
        Map<String, String> result = new HashMap<>();
        tags.forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    public void setTags(Map<String, String> tagsMap) {
        if (tagsMap != null) {
            tags = new ArrayList<>();
            tagsMap.forEach((key, value) -> tags.add(MutablePair.of(key, value)));
            fireTableDataChanged();
        }
    }

    public void addTag(String key, String value) {
        tags.add(MutablePair.of(key.trim(), value.trim()));
        fireTableRowsInserted(tags.size() - 1, tags.size() - 1);
    }

    public void deleteTags(int[] rows) {
        Arrays.sort(rows);
        for (int i = rows.length - 1; i >= 0; --i) {
            tags.remove(rows[i]);
        }
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return tags.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int col) {
        return col == 0
                ? Constant.messages.getString("alert.tags.table.key")
                : Constant.messages.getString("alert.tags.table.value");
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || col < 0 || row >= tags.size()) {
            return null;
        }
        return col == 0 ? tags.get(row).getKey() : tags.get(row).getValue();
    }

    @Override
    public void setValueAt(Object val, int row, int col) {
        String valStr = val.toString().trim();
        if (row >= 0 && col >= 0 && row < tags.size()) {
            if (col == 0) {
                tags.get(row).setLeft(valStr);
            } else {
                tags.get(row).setValue(valStr);
            }
            fireTableCellUpdated(row, col);
        }
    }
}
