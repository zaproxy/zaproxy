package org.zaproxy.zap.extension.userauth;

import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.AbstractAuthenticationMethodOptionsPanel;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethod;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodFactory;
import org.zaproxy.zap.view.AbstractFormDialog;

public class DialogEditAuthenticationMethod extends AbstractFormDialog {

	private static final String DIALOG_TITLE = Constant.messages
			.getString("httpsessions.options.dialog.token.add.title");

	private static final String CONFIRM_BUTTON_LABEL = Constant.messages
			.getString("userauth.user.dialog.add.button.confirm");

	private AuthenticationMethodFactory<?> authenticationMethodFactory;
	private AbstractAuthenticationMethodOptionsPanel<?> contentsPanel;
	private int contextId;

	public DialogEditAuthenticationMethod(Dialog owner, String title, AuthenticationMethodFactory<?> factory,
			int contextId) {
		super(owner, factory.getName(), false);
		this.contextId = contextId;
		this.authenticationMethodFactory = factory;
		initView();
		this.setMinimumSize(new Dimension(250, 50));
		this.setConfirmButtonEnabled(true);
	}

	@Override
	protected JPanel getFieldsPanel() {
		if (contentsPanel == null) {
			contentsPanel = authenticationMethodFactory.buildOptionsPanel(contextId);
		}
		return contentsPanel;
	}

	@Override
	protected String getConfirmButtonLabel() {
		return CONFIRM_BUTTON_LABEL;
	}

	@Override
	protected boolean validateFields() {
		return contentsPanel.validateFields();
	}

	@Override
	protected void performAction() {
		Logger.getRootLogger().info("Action...");
		contentsPanel.saveMethod();
	}

	public AuthenticationMethod getAuthenticationMethod() {
		return contentsPanel.getMethod();
	}

}
