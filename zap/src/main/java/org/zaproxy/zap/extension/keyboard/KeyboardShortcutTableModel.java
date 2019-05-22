/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
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

public class KeyboardShortcutTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = {
            Constant.messages.getString("keyboard.options.table.header.menu"),
            Constant.messages.getString("keyboard.options.table.header.mods"),
            Constant.messages.getString("keyboard.options.table.header.key")};
    
	private static final int COLUMN_COUNT = COLUMN_NAMES.length;
	
    private List<KeyboardShortcut> tokens = new ArrayList<>(0);
    
    public KeyboardShortcutTableModel() {
        super();
    }
    
    protected List<KeyboardShortcut> getElements() {
        return tokens;
    }

    /**
     * @param shortcuts The shortcuts to set.
     */
    public void setShortcuts(List<KeyboardShortcut> shortcuts) {
		this.tokens = new ArrayList<>(shortcuts.size());
		
		for (KeyboardShortcut token : shortcuts) {
			this.tokens.add(token);
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
        return tokens.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch(columnIndex) {
        case 0:
            return tokens.get(rowIndex).getName();
        case 1:
        	return tokens.get(rowIndex).getKeyStrokeModifiersString();
        case 2:
        	return tokens.get(rowIndex).getKeyStrokeKeyCodeString();
        }
        return null;
    }

	public void addShortcut(KeyboardShortcut shortcut) {
		this.tokens.add(shortcut);
  	  	fireTableDataChanged();
	}

}
