package org.zaproxy.zap.extension.userauth;

import java.awt.Dialog;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodType;
import org.zaproxy.zap.userauth.session.SessionManagementMethodType;

public class DialogModifyUser extends DialogAddUser {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7828871270310672334L;
	private static final String DIALOG_TITLE = Constant.messages
			.getString("userauth.user.dialog.modify.title");

	public DialogModifyUser(Dialog owner, ExtensionUserAuthentication extension, int contextId) {
		super(owner, extension, DIALOG_TITLE, contextId);
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	protected String getConfirmButtonLabel() {
		return Constant.messages.getString("userauth.user.dialog.modify.button.confirm");
	}

	@Override
	protected void init() {
		log.info("Initializing modify user dialog for: " + user);
		getNameTextField().setText(user.getName());
		getEnabledCheckBox().setSelected(user.isEnabled());

//		// Identify selected authentication method
//		for (AuthenticationMethodType<?> f : extension.getAuthenticationMethodFactories()) {
//			if (f.isFactoryForMethod(selectedAuthenticationMethod.getClass())) {
//				getAuthenticationMethodsComboBox().setSelectedItem(f);
//			}
//		}
//
//		// Identify selected session management method
//		for (AuthenticationMethodType<?> f : extension.getSessionManagementMethodFactories()) {
//			if (f.isFactoryForMethod(selectedSessionManagementMethod.getClass())) {
//				getSessionManagementMethodsComboBox().setSelectedItem(f);
//			}
//		}
	}
}
