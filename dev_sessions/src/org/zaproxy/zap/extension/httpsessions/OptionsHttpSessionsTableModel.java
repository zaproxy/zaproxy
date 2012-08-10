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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;

/**
 * The OptionsHttpSessionsTableModel is used as a table model to display the token names for
 * {@link OptionsHttpSessionsPanel}.
 */
public class OptionsHttpSessionsTableModel extends AbstractTableModel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant defining the table column names. */
	private static final String[] columnNames = { Constant.messages.getString("httpsessions.options.table.token") };

	/** The token names. */
	private List<String> tokenNames = new ArrayList<String>();

	/**
	 * Instantiates a new options http sessions table model.
	 */
	public OptionsHttpSessionsTableModel() {
		super();
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount() */
	@Override
	public int getColumnCount() {
		return 1;
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount() */
	@Override
	public int getRowCount() {
		return tokenNames.size();
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int) */
	@Override
	public Object getValueAt(int row, int col) {
		return tokenNames.get(row);
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int) */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int) */
	@Override
	public void setValueAt(Object value, int row, int col) {
		tokenNames.set(row, ((String) value).toLowerCase());
		checkAndAppendNewRow();
		fireTableCellUpdated(row, col);
	}

	/**
	 * Gets the token names.
	 * 
	 * @return the list of token names
	 */
	public List<String> getTokens() {
		// Do some cleanup
		String token = null;
		Iterator<String> it = tokenNames.iterator();
		while (it.hasNext()) {
			token = it.next();
			if (token == null || token.isEmpty()) {
				it.remove();
			}
		}

		// Make a copy of the internal list
		List<String> newList = new ArrayList<String>(tokenNames);
		return newList;
	}

	/**
	 * Sets the tokens.
	 * 
	 * @param tokens the new tokens
	 */
	public void setTokens(List<String> tokens) {
		this.tokenNames = new ArrayList<String>();
		if (tokens != null) {
			for (String token : tokens) {
				if (!this.tokenNames.contains(token)) {
					// Ensure duplicated removed
					this.tokenNames.add(token);
				}
			}
			Collections.sort(this.tokenNames);
		}
		checkAndAppendNewRow();
		fireTableDataChanged();
	}

	/**
	 * Checks if the last element is an empty element and adds an empty element if necessary.
	 */
	private void checkAndAppendNewRow() {
		String token = null;
		if (tokenNames.size() > 0) {
			token = tokenNames.get(tokenNames.size() - 1);
			if (!token.isEmpty()) {
				token = "";
				tokenNames.add(token);
			}
		} else {
			token = "";
			tokenNames.add(token);
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int) */
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/* (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int) */
	@Override
	public Class<String> getColumnClass(int c) {
		return String.class;
	}

}
