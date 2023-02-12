/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.extension.proxies;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

/** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
@Deprecated
@SuppressWarnings("serial")
public class OptionsProxiesTableModel extends AbstractMultipleOptionsTableModel<ProxiesParamProxy> {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("proxies.options.table.header.enabled"),
        Constant.messages.getString("proxies.options.table.header.address"),
        Constant.messages.getString("proxies.options.table.header.port")
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<ProxiesParamProxy> proxies = new ArrayList<>(0);

    public OptionsProxiesTableModel() {
        super();
    }

    @Override
    public List<ProxiesParamProxy> getElements() {
        return proxies;
    }

    public void setProxies(List<ProxiesParamProxy> proxies) {
        this.proxies = new ArrayList<>(proxies.size());

        for (ProxiesParamProxy proxy : proxies) {
            this.proxies.add(new ProxiesParamProxy(proxy));
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
        return proxies.size();
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
                return getElement(rowIndex).getAddress();
            case 2:
                // Return a String as the commas used for integers dont look right in the table
                return Integer.toString(getElement(rowIndex).getPort());
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            if (aValue instanceof Boolean) {
                proxies.get(rowIndex).setEnabled((Boolean) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }
    }
}
