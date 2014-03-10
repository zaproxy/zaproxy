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
package org.zaproxy.zap.extension.httpsessions;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

/**
 * The OptionsHttpSessionsTableModel is used as a table model to display the token names for
 * {@link OptionsHttpSessionsPanel}.
 */
public class OptionsHttpSessionsTableModel extends AbstractMultipleOptionsTableModel<HttpSessionToken> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant defining the table column names. */
	private static final String[] COLUMN_NAMES = {
			Constant.messages.getString("httpsessions.options.table.header.enabled"),
			Constant.messages.getString("httpsessions.options.table.header.token") };

	private static final int COLUMN_COUNT = COLUMN_NAMES.length;
	
	/** The tokens. */
	private List<HttpSessionToken> tokens = new ArrayList<>(0);

	/**
	 * Instantiates a new options http sessions table model.
	 */
	public OptionsHttpSessionsTableModel() {
		super();
	}
	
	@Override
	public List<HttpSessionToken> getElements() {
		return tokens;
	}
	
	public List<HttpSessionToken> getTokens() {
		return tokens;
	}

	/**
	 * @param tokens The tokens to set.
	 */
	public void setTokens(List<HttpSessionToken> tokens) {
		this.tokens = new ArrayList<>(tokens.size());
		
		for (HttpSessionToken token : tokens) {
			this.tokens.add(new HttpSessionToken(token));
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
		switch(columnIndex) {
		case 0:
			return Boolean.valueOf(getElement(rowIndex).isEnabled());
		case 1:
			return getElement(rowIndex).getName();
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			if (aValue instanceof Boolean) {
				tokens.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

}
