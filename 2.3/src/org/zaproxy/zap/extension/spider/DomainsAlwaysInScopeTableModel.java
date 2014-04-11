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
package org.zaproxy.zap.extension.spider;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

class DomainsAlwaysInScopeTableModel extends AbstractMultipleOptionsTableModel<DomainAlwaysInScopeMatcher> {

    private static final long serialVersionUID = -5411351965957264957L;

    private static final String[] COLUMN_NAMES = {
            Constant.messages.getString("spider.options.domains.in.scope.table.header.enabled"),
            Constant.messages.getString("spider.options.domains.in.scope.table.header.regex"),
            Constant.messages.getString("spider.options.domains.in.scope.table.header.value") };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<DomainAlwaysInScopeMatcher> domainsInScope = new ArrayList<>(5);

    public DomainsAlwaysInScopeTableModel() {
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
        return domainsInScope.size();
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
            domainsInScope.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
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

    public List<DomainAlwaysInScopeMatcher> getDomainsAlwaysInScope() {
        return domainsInScope;
    }

    public void setDomainsAlwaysInScope(List<DomainAlwaysInScopeMatcher> domainsInScope) {
        this.domainsInScope = new ArrayList<>(domainsInScope.size());

        for (DomainAlwaysInScopeMatcher excludedDomain : domainsInScope) {
            this.domainsInScope.add(new DomainAlwaysInScopeMatcher(excludedDomain));
        }

        fireTableDataChanged();
    }

    @Override
    public List<DomainAlwaysInScopeMatcher> getElements() {
        return domainsInScope;
    }
}
