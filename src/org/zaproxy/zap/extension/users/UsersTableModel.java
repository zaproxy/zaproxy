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
package org.zaproxy.zap.extension.users;

import java.util.ArrayList;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

/**
 * A table model for holding a set of Users, for a {@link Context}.
 */
public class UsersTableModel extends AbstractMultipleOptionsTableModel<User> {

	/** The Constant defining the table column names. */
	private static final String[] COLUMN_NAMES = {
			Constant.messages.getString("users.table.header.enabled"),
			Constant.messages.getString("users.table.header.id"),
			Constant.messages.getString("users.table.header.name") };

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4463944219657112162L;

	/** The users. */
	private List<User> users;

	/**
	 * Instantiates a new users table model. An internal copy of the provided list is stored.
	 * 
	 * @param users the users
	 */
	public UsersTableModel(List<User> users) {
		this.users = new ArrayList<>(users);
	}

	/**
	 * Instantiates a new user table model.
	 */
	public UsersTableModel() {
		this.users = new ArrayList<>();
	}

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
			return users.get(rowIndex).getId();
		case 2:
			return users.get(rowIndex).getName();
		default:
			return null;
		}
	}

	@Override
	public List<User> getElements() {
		return users;
	}

	/**
	 * Gets the internal list of users managed by this model. 
	 * 
	 * @return the users
	 */
	public List<User> getUsers() {
		return users;
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
		this.fireTableDataChanged();
	}
	
	/**
	 * Removes all the users for this model.
	 */
	public void removeAllUsers(){
		this.users=new ArrayList<>();
		this.fireTableDataChanged();
	}
	
	/**
	 * Adds a new user to this model
	 *
	 * @param user the user
	 */
	public void addUser(User user){
		this.users.add(user);
		this.fireTableRowsInserted(this.users.size()-1, this.users.size()-1);
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 1:
			return Integer.class;
		case 2:
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
