package org.zaproxy.zap.extension.users;

import java.awt.Dialog;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.users.User;

public class DialogModifyUser extends DialogAddUser {

	private static final long serialVersionUID = 7828871270310672334L;
	private static final String DIALOG_TITLE = Constant.messages
			.getString("users.dialog.modify.title");

	public DialogModifyUser(Dialog owner, ExtensionUserManagement extension) {
		super(owner, extension, DIALOG_TITLE);
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	protected String getConfirmButtonLabel() {
		return Constant.messages.getString("users.dialog.modify.button.confirm");
	}

	@Override
	protected void init() {
		log.info("Initializing modify user dialog for: " + user);
		getNameTextField().setText(user.getName());
		getEnabledCheckBox().setSelected(user.isEnabled());

		if (this.workingContext == null)
			throw new IllegalStateException(
					"A working Context should be set before setting the 'Add Dialog' visible.");

		// Initialize the credentials that will be configured
		configuredCredentials = this.user.getAuthenticationCredentials();

		initializeCredentialsConfigPanel();
	}
}
