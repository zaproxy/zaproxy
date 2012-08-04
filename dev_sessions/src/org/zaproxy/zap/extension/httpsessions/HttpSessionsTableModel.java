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
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import org.parosproxy.paros.Constant;

/**
 * The Class HttpSessionsTableModel that is used as a TableModel for the Http Sessions Panel.
 */
public class HttpSessionsTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -6380136823410869457L;

	/** The Constant defining the COLUMN COUNT. */
	private static final int COLUMN_COUNT = 3;

	/** The column names. */
	private final Vector<String> columnNames;

	/** The http sessions. */
	private List<HttpSession> sessions;

	private static final ImageIcon activeIcon;

	static {
		activeIcon = new ImageIcon(HttpSessionsTableModel.class.getResource("/resource/icon/16/102.png"));
	}

	/**
	 * Instantiates a new http sessions table model.
	 */
	public HttpSessionsTableModel() {
		super();
		columnNames = new Vector<String>(COLUMN_COUNT);
		columnNames.add(Constant.messages.getString("httpsessions.table.header.active"));
		columnNames.add(Constant.messages.getString("httpsessions.table.header.name"));
		columnNames.add(Constant.messages.getString("httpsessions.table.header.tokens"));

		sessions = Collections.synchronizedList(new ArrayList<HttpSession>());
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public int getRowCount() {
		return sessions.size();
	}

	public Object getValueAt(int row, int col) {
		// Check if the row is valid
		if (row >= sessions.size()) {
			return null;
		}

		// Get the session and the required field
		HttpSession session = sessions.get(row);
		switch (col) {
		case 0:
			if (session.isActive())
				return activeIcon;
			else
				return null;
		case 1:
			return session.getName();
		case 2:
			return session.getTokenValuesString();
		default:
			return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		return columnNames.get(column);
	}

	/**
	 * Removes all the elements.
	 */
	public void removeAllElements() {
		sessions.clear();
	}

	/**
	 * Adds a new http session to the model.
	 * 
	 * @param session the session
	 */
	public void addHttpSession(HttpSession session) {
		if (sessions.contains(session))
			return;
		sessions.add(session);
		fireTableRowsInserted(sessions.size() - 1, sessions.size() - 1);
	}

	/**
	 * Cells are not editable.
	 * 
	 * @return
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 1)
			return true;
		else
			return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// Allow change only for the name column
		if (columnIndex == 1) {
			sessions.get(rowIndex).setName((String) aValue);
			super.setValueAt(aValue, rowIndex, columnIndex);
		}
	}

	/**
	 * Returns the type of column for given column index.
	 * 
	 * @param columnIndex the column index
	 * @return the column class
	 */
	@Override
	public Class<? extends Object> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return ImageIcon.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		}
		return null;
	}

	/**
	 * Gets the http session at a particular row index.
	 * 
	 * @param rowIndex the row index
	 * @return the http session at the given index, or null
	 */
	protected HttpSession getHttpSessionAt(int rowIndex) {
		if (rowIndex < 0 || rowIndex >= sessions.size())
			return null;
		return sessions.get(rowIndex);
	}

	/**
	 * Removes the http session.
	 * 
	 * @param session the session
	 */
	public void removeHttpSession(HttpSession session) {
		sessions.remove(session);

	}
}
