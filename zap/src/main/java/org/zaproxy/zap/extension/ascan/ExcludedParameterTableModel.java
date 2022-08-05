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
package org.zaproxy.zap.extension.ascan;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.ScannerParamFilter;
import org.zaproxy.zap.view.AbstractMultipleOptionsBaseTableModel;

/** @author yhawke (2014) */
@SuppressWarnings("serial")
public class ExcludedParameterTableModel
        extends AbstractMultipleOptionsBaseTableModel<ScannerParamFilter> {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("variant.options.excludedparam.table.header.url"),
        Constant.messages.getString("variant.options.excludedparam.table.header.type"),
        Constant.messages.getString("variant.options.excludedparam.table.header.name")
    };

    private final List<ScannerParamFilter> tokens = new ArrayList<>();

    @Override
    public int getRowCount() {
        return tokens.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 2:
                return tokens.get(rowIndex).getParamName();

            case 1:
                return tokens.get(rowIndex).getTypeString();

            case 0:
                return tokens.get(rowIndex).getWildcardedUrl();
        }

        return null;
    }

    @Override
    public List<ScannerParamFilter> getElements() {
        return tokens;
    }

    /** @param tokens The tokens to set. */
    public void setTokens(List<ScannerParamFilter> tokens) {
        this.tokens.clear();

        for (ScannerParamFilter token : tokens) {
            this.tokens.add(token.clone());
        }

        fireTableDataChanged();
    }
}
