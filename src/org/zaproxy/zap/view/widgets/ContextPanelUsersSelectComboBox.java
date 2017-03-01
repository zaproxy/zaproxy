package org.zaproxy.zap.view.widgets;

import javax.swing.JComboBox;

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
	 * @return the selected user, or {@code null} if none 
	 */
	public User getSelectedUser() {
		return (User) getSelectedItem();
	}

	/**
	 * Allows adding 'custom' users besides the ones already loaded from the context. Can be used,
	 * for example, to add a 'Any User' entry or 'Unauthenticated' entry.
	 * @param customUsers the custom users, should not be {@code null}
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

}
