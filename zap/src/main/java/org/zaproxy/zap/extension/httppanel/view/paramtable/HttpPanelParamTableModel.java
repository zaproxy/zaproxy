/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.view.paramtable;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import javax.swing.table.AbstractTableModel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.view.paramtable.addins.ParamAddinInterface;

@SuppressWarnings("serial")
public abstract class HttpPanelParamTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 8714941615215038148L;

    private static final Logger log = LogManager.getLogger(HttpPanelParamTableModel.class);

    private static final String[] columnNames = {
        Constant.messages.getString("http.panel.view.tableparam.type"),
        Constant.messages.getString("http.panel.view.table.paramName"),
        Constant.messages.getString("http.panel.view.table.paramValue"),
        Constant.messages.getString("http.panel.view.tableparam.functions")
    };

    protected HttpMessage httpMessage;
    protected LinkedList<HtmlParameter> allParams;

    private boolean isEditable;
    private boolean hasChanged;

    public HttpPanelParamTableModel() {
        allParams = new LinkedList<>();
    }

    @Override
    public int getColumnCount() {
        if (isEditable) {
            return 4;
        }

        return 3;
    }

    @Override
    public int getRowCount() {
        return allParams.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex > allParams.size() || rowIndex < 0) {
            return null;
        }
        HtmlParameter htmlParameter = allParams.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return htmlParameter.getType();
            case 1:
                return htmlParameter.getName();
            case 2:
                return htmlParameter.getValue();
        }

        return "";
    }

    @Override
    public Class<String> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        boolean changed = false;

        HtmlParameter htmlParameter = allParams.get(row);
        if (col == 0) {
            htmlParameter.setType((HtmlParameter.Type) value);
            changed = true;
        } else if (col == 1) {
            htmlParameter.setName((String) value);
            changed = true;
        } else if (col == 2) {
            htmlParameter.setValue((String) value);
            changed = true;
        } else if (col == 3) {
            if (value instanceof ParamAddinInterface) {
                try {
                    htmlParameter.setValue(
                            ((ParamAddinInterface) value).convertData(htmlParameter.getValue()));
                    changed = true;
                    col = 2;
                } catch (UnsupportedEncodingException e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }

        if (changed) {
            hasChanged = true;
            this.fireTableCellUpdated(row, col);
        }

        if (row == allParams.size() - 1) {
            htmlParameter = allParams.getLast();
            if (!(htmlParameter.getName().isEmpty() && htmlParameter.getValue().isEmpty())) {
                allParams.add(getDefaultHtmlParameter());
                this.fireTableRowsInserted(row + 1, row + 1);
            }
        }
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isEditable;
    }

    public void setEditable(boolean editable) {
        isEditable = editable;
    }

    public void setHttpMessage(HttpMessage ahttpMessage) {
        this.httpMessage = ahttpMessage;

        hasChanged = false;
        allParams.clear();

        if (httpMessage == null) {
            this.fireTableDataChanged();
            return;
        }

        loadAllParams();

        if (isEditable) {
            allParams.add(getDefaultHtmlParameter());
        }

        this.fireTableDataChanged();
    }

    public HttpMessage getHttpMessage() {
        return httpMessage;
    }

    public void save() {
        if (!hasChanged) {
            return;
        }

        saveAllParams();
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    protected abstract void loadAllParams();

    public abstract void saveAllParams();

    protected abstract HtmlParameter getDefaultHtmlParameter();
}
