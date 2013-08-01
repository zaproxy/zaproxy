/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
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
package org.zaproxy.zap.extension.userauth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

/**
 * A table model for holding a set of Users, for a {@link Context}.
 */
public class UserAuthUserTableModel extends AbstractMultipleOptionsTableModel<User> {

	/** The Constant defining the table column names. */
	private static final String[] COLUMN_NAMES = {
			Constant.messages.getString("userauth.user.table.header.enabled"),
			Constant.messages.getString("userauth.user.table.header.name") };

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4463944219657112162L;

	/** The users. */
	List<User> users = new ArrayList<User>();

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}

	@Override
	public int getRowCount() {
		return users.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return users.get(rowIndex).isEnabled();
		case 1:
			return users.get(rowIndex).getName();
		default:
			return null;
		}
	}

	@Override
	protected List<User> getElements() {
		return users;
	}

	/**
	 * Gets an unmodifiable view of the internal list of users.
	 * 
	 * @return the users
	 */
	public List<User> getUsers() {
		return Collections.unmodifiableList(users);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Just the enable/disable
		return (columnIndex == 0);
	}

	/**
	 * Sets a new list of users for this model. An internal copy of the provided list is stored.
	 * 
	 * @param users the new users
	 */
	public void setUsers(List<User> users) {
		this.users = new ArrayList<>(users);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return String.class;
		default:
			return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			if (aValue instanceof Boolean) {
				users.get(rowIndex).setEnabled(((Boolean) aValue).booleanValue());
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
	}

}
