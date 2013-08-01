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

import java.awt.Dialog;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.userauth.session.AbstractSessionManagementMethodOptionsPanel;
import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethodType;
import org.zaproxy.zap.view.AbstractFormDialog;

/**
 * The Dialog that contains the panel for setting up a Session Management Method.
 */
public class DialogConfigSessionManagementMethod<T extends SessionManagementMethod> extends
		AbstractFormDialog {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4896945401407014417L;

	private static final String CONFIRM_BUTTON_LABEL = Constant.messages
			.getString("userauth.session.dialog.config.button.confirm");

	private SessionManagementMethodType<T> sessionManagementMethodFactory;
	private AbstractSessionManagementMethodOptionsPanel<T> contentsPanel;
	private T existingMethod;
	private int contextId;

	public DialogConfigSessionManagementMethod(Dialog owner, String title,
			SessionManagementMethodType<T> factory, T existingMethod, int contextId) {
		super(owner, factory.getName(), false);
		this.contextId = contextId;
		this.sessionManagementMethodFactory = factory;
		this.existingMethod = existingMethod;
		initView();
		this.setMinimumSize(new Dimension(250, 50));
		this.setConfirmButtonEnabled(true);
	}

	@Override
	protected JPanel getFieldsPanel() {
		if (contentsPanel == null) {
			contentsPanel = sessionManagementMethodFactory.buildOptionsPanel(existingMethod, contextId);
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
		contentsPanel.saveMethod();
	}

	public SessionManagementMethod getSessionManagementMethod() {
		return contentsPanel.getMethod();
	}

}
