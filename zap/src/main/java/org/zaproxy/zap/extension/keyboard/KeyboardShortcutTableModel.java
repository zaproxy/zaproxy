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
package org.zaproxy.zap.extension.keyboard;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.Constant;

@SuppressWarnings("serial")
public class KeyboardShortcutTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
        Constant.messages.getString("keyboard.options.table.header.action"),
        Constant.messages.getString("keyboard.options.table.header.scope"),
        Constant.messages.getString("keyboard.options.table.header.shortcut")
    };

    private static final int COLUMN_COUNT = COLUMN_NAMES.length;

    private List<KeyboardShortcut> shortcuts = new ArrayList<>(0);
    private boolean showSymbols = KeyStrokeDisplay.isDefaultShowSymbols();

    public KeyboardShortcutTableModel() {
        super();
    }

    public void setShowSymbols(boolean showSymbols) {
        if (this.showSymbols != showSymbols) {
            this.showSymbols = showSymbols;
            fireTableDataChanged();
        }
    }

    public boolean isShowSymbols() {
        return showSymbols;
    }

    protected List<KeyboardShortcut> getElements() {
        return shortcuts;
    }

    /**
     * @param shortcuts The shortcuts to set.
     */
    public void setShortcuts(List<KeyboardShortcut> shortcuts) {
        this.shortcuts = new ArrayList<>(shortcuts.size());

        for (KeyboardShortcut shortcut : shortcuts) {
            this.shortcuts.add(shortcut);
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
        return shortcuts.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return shortcuts.get(rowIndex).getName();
            case 1:
                return shortcuts.get(rowIndex).getScope();
            case 2:
                return KeyStrokeDisplay.formatPlain(
                        shortcuts.get(rowIndex).getKeyStroke(), showSymbols);
        }
        return null;
    }

    public void addShortcut(KeyboardShortcut shortcut) {
        this.shortcuts.add(shortcut);
        fireTableDataChanged();
    }
}
