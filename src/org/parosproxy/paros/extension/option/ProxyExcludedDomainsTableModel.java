/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.parosproxy.paros.extension.option;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.ProxyExcludedDomainMatcher;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

public class ProxyExcludedDomainsTableModel extends AbstractMultipleOptionsTableModel<ProxyExcludedDomainMatcher> {

    private static final long serialVersionUID = -5411351965957264957L;

    private static final String[] COLUMN_NAMES = {
            Constant.messages.getString("conn.options.proxy.excluded.domain.table.header.enabled"),
            Constant.messages.getString("conn.options.proxy.excluded.domain.table.header.regex"),
            Constant.messages.getString("conn.options.proxy.excluded.domain.table.header.value") };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<ProxyExcludedDomainMatcher> excludedDomains = new ArrayList<>(5);

    public ProxyExcludedDomainsTableModel() {
        super();
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
    public int getRowCount() {
        return excludedDomains.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == 0);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
            return Boolean.valueOf(getElement(rowIndex).isEnabled());
        case 1:
            return Boolean.valueOf(getElement(rowIndex).isRegex());
        case 2:
            return getElement(rowIndex).getValue();
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0 && aValue instanceof Boolean) {
            excludedDomains.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == 0 || c == 1) {
            return Boolean.class;
        }
        return String.class;
    }

    public List<ProxyExcludedDomainMatcher> getExcludedDomains() {
        return excludedDomains;
    }

    public void setExcludedDomains(List<ProxyExcludedDomainMatcher> excludedDomains) {
        this.excludedDomains = new ArrayList<>(excludedDomains.size());

        for (ProxyExcludedDomainMatcher excludedDomain : excludedDomains) {
            this.excludedDomains.add(new ProxyExcludedDomainMatcher(excludedDomain));
        }

        fireTableDataChanged();
    }

    @Override
    public List<ProxyExcludedDomainMatcher> getElements() {
        return excludedDomains;
    }
}
