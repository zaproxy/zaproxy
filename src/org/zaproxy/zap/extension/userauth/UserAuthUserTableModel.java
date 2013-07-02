package org.zaproxy.zap.extension.userauth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.AbstractMultipleOptionsTableModel;

public class UserAuthUserTableModel extends AbstractMultipleOptionsTableModel<User> {

	/** The Constant defining the table column names. */
	private static final String[] COLUMN_NAMES = {
			Constant.messages.getString("userauth.user.table.header.enabled"),
			Constant.messages.getString("userauth.user.table.header.name") };

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4463944219657112162L;

	/** The users. */
	List<User> users=new ArrayList<User>();

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

}
