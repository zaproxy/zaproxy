package org.zaproxy.zap.extension.userauth.auth;

import java.awt.Dialog;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.AbstractAuthenticationMethodOptionsPanel;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethod;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodType;
import org.zaproxy.zap.view.AbstractFormDialog;

public class DialogConfigAuthenticationMethod extends AbstractFormDialog {

	private static final long serialVersionUID = 6582375522313849599L;

	private static final String DIALOG_TITLE = Constant.messages.getString("authentication.dialog.title");
	private static final String CONFIRM_BUTTON_LABEL = Constant.messages
			.getString("authentication.dialog.config.button.confirm");

	private AuthenticationMethodType<?> authenticationMethodType;
	private Context uiSharedContext;
	private AuthenticationMethod<?> configuredMethod;
	private AbstractAuthenticationMethodOptionsPanel<?> fieldsPanel;

	public DialogConfigAuthenticationMethod(Dialog owner,
			AuthenticationMethodType<?> authenticationMethodType, AuthenticationMethod<?> existingMethod,
			Context uiSharedContext) {
		super(owner, DIALOG_TITLE, false);
		this.uiSharedContext = uiSharedContext;
		this.configuredMethod = existingMethod;
		this.authenticationMethodType = authenticationMethodType;
		initView();
		this.setConfirmButtonEnabled(true);
	}

	@Override
	protected JPanel getFieldsPanel() {
		if (fieldsPanel == null) {
			fieldsPanel = authenticationMethodType.buildOptionsPanel(configuredMethod, uiSharedContext);
			fieldsPanel.setBorder(new EmptyBorder(4, 4, 4, 4));
		}
		return fieldsPanel;
	}

	@Override
	protected boolean validateFields() {
		return fieldsPanel.validateFields();
	}

	@Override
	protected void performAction() {
		fieldsPanel.saveMethod();
	}

	public AuthenticationMethod<?> getConfiguredMethod() {
		return configuredMethod;
	}

	@Override
	protected String getConfirmButtonLabel() {
		return CONFIRM_BUTTON_LABEL;
	}

}
