package org.zaproxy.zap.view.widgets;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.lang.ArrayUtils;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.extension.users.UsersTableModel;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.renderer.UserListCellRenderer;

/**
 * A ComboBox widget that displays the list of {@link User Users} based on the list of users that is
 * currently being edited in Users Context Panel. This class should be used when displaying a
 * combo-box for selecting a user in a Context Panel, as it uses the right list of Users: the latest
 * list of Users as being defined in the Users Panel.
 */
public class ContextPanelUsersSelectComboBox extends JComboBox<User> {

	private static final long serialVersionUID = 7254245073685076020L;
	private static ExtensionUserManagement usersExtension;

	private static void loadUsersManagementExtension() {
		if (usersExtension == null) {
			usersExtension = Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionUserManagement.class);
			if (usersExtension == null)
				throw new IllegalStateException(
						"Trying to create MultiUserSelectBox without the ExtensionUsersManagement"
								+ " being enabled.");
		}
	}

	/**
	 * Instantiates a new user select combo box.
	 * 
	 * @param contextId the context id
	 */
	@SuppressWarnings("unchecked")
	public ContextPanelUsersSelectComboBox(int contextId) {
		super();
		// Force loading the UserManagement extension to make sure it's enabled.
		loadUsersManagementExtension();
		UsersTableModel usersTableModel = usersExtension.getUIConfiguredUsersModel(contextId);
		this.setModel(new UsersListModel(usersTableModel));
		this.setRenderer(new UserListCellRenderer());
	}

	/**
	 * Performs the same as {@link #getSelectedItem()}, but adds a convenience cast.
	 */
	public User getSelectedUser() {
		return (User) getSelectedItem();
	}

	/**
	 * Allows adding 'custom' users besides the ones already loaded from the context. Can be used,
	 * for example, to add a 'Any User' entry or 'Unauthenticated' entry.
	 */
	public void setCustomUsers(User[] customUsers) {
		((UsersListModel) getModel()).setCustomUsers(customUsers);
	}

	/**
	 * Sets the selected item as the actual internal item with the same id as the provided user.
	 * 
	 * @param user the new selected internal item
	 */
	public void setSelectedInternalItem(User user) {
		((UsersListModel) getModel()).setSelectedInternalItem(user);
	}

	private static class UsersListModel extends AbstractListModel<User> implements ComboBoxModel<User>,
			TableModelListener {

		private static final long serialVersionUID = 5648260449088479312L;
		User selectedItem;
		UsersTableModel tableModel;
		User[] customUsers;

		public UsersListModel(UsersTableModel tableModel) {
			super();
			this.tableModel = tableModel;
			this.tableModel.addTableModelListener(this);
		}

		@Override
		public User getElementAt(int index) {
			if (index < tableModel.getRowCount())
				return tableModel.getElement(index);
			else if (customUsers != null)
				return customUsers[index - tableModel.getRowCount()];
			return null;
		}

		@Override
		public int getSize() {
			return tableModel.getUsers().size() + (customUsers == null ? 0 : customUsers.length);
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			// Handle the situation when something is selected and but there aren't any users to
			// select
			if (selectedItem != null && getSize() == 0)
				setSelectedItem(null);
			// Handle the situation when nothing selected or the event is a deletion or update and
			// the previously selected item does not exist any more
			else if (selectedItem == null
					|| ((e.getType() == TableModelEvent.DELETE || e.getType() == TableModelEvent.UPDATE) && getIndexOf(selectedItem) == -1)) {
				if (getSize() > 0)
					setSelectedItem(getElementAt(0));
				else
					setSelectedItem(null);
			}
			fireContentsChanged(this, e.getFirstRow(), e.getLastRow());
		}

		@Override
		public Object getSelectedItem() {
			return selectedItem;
		}

		@Override
		public void setSelectedItem(Object anItem) {
			// No item is selected and object is null, so no change required.
			if (selectedItem == null && anItem == null)
				return;

			// Wrong class element selected
			if (!(anItem instanceof User))
				return;

			// object is already selected so no change required.
			if (selectedItem != null && selectedItem.equals(anItem))
				return;

			// Simply return if object is not in the list.
			if (anItem != null && getIndexOf(anItem) == -1)
				return;

			// Here we know that object is either an item in the list or null.
			selectedItem = (User) anItem;
			fireContentsChanged(this, -1, -1);
		}

		/**
		 * Sets the selected item as the actual internal item with the same id as the provided user.
		 * 
		 * @param user the new selected internal item
		 */
		public void setSelectedInternalItem(User user) {
			int index = getIndexOf(user);
			if (index != -1)
				setSelectedItem(tableModel.getUsers().get(index));
			else if (getSize() > 0)
				setSelectedItem(getElementAt(0));
			else
				setSelectedItem(null);

		}

		/**
		 * Returns the index of the specified element in the model's item list.
		 * 
		 * @param object the element.
		 * @return The index of the specified element in the model's item list, or -1 if it wasn't
		 *         found
		 */
		public int getIndexOf(Object object) {
			int index = tableModel.getUsers().indexOf(object);
			if (index < 0 && customUsers != null)
				return ArrayUtils.indexOf(customUsers, object);
			return index;
		}

		public void setCustomUsers(User[] users) {
			customUsers = users;
		}
	}
}
