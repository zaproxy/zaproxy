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
package org.zaproxy.zap.userauth.authentication;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.network.HttpUtil;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.authentication.FormBasedAuthenticationMethodType.FormBasedAuthenticationMethod;
import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.userauth.session.WebSession;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The implementation for an {@link AuthenticationMethodType} where the Users are authenticated by
 * posting a form with user and password.
 */
public class FormBasedAuthenticationMethodType extends
		AuthenticationMethodType<FormBasedAuthenticationMethod> {

	/** The Authentication method's name. */
	private static final String METHOD_NAME = Constant.messages.getString("authentication.method.fb.name");

	public static class FormBasedAuthenticationMethod implements
			AuthenticationMethod<FormBasedAuthenticationMethod> {

		private static final String HISTORY_TAG_AUTHENTICATION = "Authentication";
		private static final String ENCODING_TYPE = "UTF-8";
		private static final Logger log = Logger.getLogger(FormBasedAuthenticationMethod.class);
		private String usernameFormFieldName;
		private String passwordFormFieldName;
		private String loginURL;
		private HttpSender httpSender;

		@Override
		public boolean isConfigured() {
			// check if the login url is valid
			try {
				new URL(loginURL);
			} catch (Exception e) {
				return false;
			}
			return usernameFormFieldName != null && passwordFormFieldName != null;
		}

		@Override
		public AuthenticationCredentials createAuthenticationCredentials() {
			return new UsernamePasswordAuthenticationCredentials();
		}

		@Override
		public String getStatusDescription() {
			return "<p>Login URL: " + loginURL + "</p><p>Username Form Field: " + usernameFormFieldName
					+ "</p><p>Password Form Field: " + passwordFormFieldName + "</p>";
		}

		@Override
		public AuthenticationMethodType<FormBasedAuthenticationMethod> getType() {
			return new FormBasedAuthenticationMethodType();
		}

		private HttpSender getHttpSender() {
			if (this.httpSender == null) {
				this.httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(),
						false, HttpSender.AUTHENTICATION_INITIATOR);
			}
			return httpSender;
		}

		@Override
		public WebSession authenticate(SessionManagementMethod sessionManagementMethod,
				AuthenticationCredentials credentials)
				throws AuthenticationMethod.UnsupportedAuthenticationCredentialsException {

			// type check
			if (!(credentials instanceof UsernamePasswordAuthenticationCredentials)) {
				throw new UnsupportedAuthenticationCredentialsException(
						"Form based authentication method only supports "
								+ UsernamePasswordAuthenticationCredentials.class.getSimpleName());
			}
			UsernamePasswordAuthenticationCredentials cred = (UsernamePasswordAuthenticationCredentials) credentials;

			// Prepare login message
			HttpMessage msg = new HttpMessage();
			try {
				msg.setRequestHeader(new HttpRequestHeader(HttpRequestHeader.POST, new URI(loginURL, false),
						HttpRequestHeader.HTTP10));
				String reqBody = usernameFormFieldName + "="
						+ URLEncoder.encode(cred.username, ENCODING_TYPE) + "&" + passwordFormFieldName + "="
						+ URLEncoder.encode(cred.password, ENCODING_TYPE);
				msg.setRequestBody(reqBody);
				if (log.isDebugEnabled())
					log.debug("Sending authentication message with content: " + reqBody);
			} catch (Exception e) {
				log.error("Unable to build authentication message: " + e.getMessage(), e);
				return null;
			}

			// Send the authentication message
			try {
				getHttpSender().sendAndReceive(msg);
			} catch (IOException e) {
				log.error("Unable to send authentication message: " + e.getMessage());
				return null;
			}

			// Add message to history
			try {
				ExtensionHistory extHistory = (ExtensionHistory) Control.getSingleton().getExtensionLoader()
						.getExtension(ExtensionHistory.class);
				HistoryReference ref = new HistoryReference(Model.getSingleton().getSession(),
						HistoryReference.TYPE_AUTHENTICATION, msg);
				ref.addTag(HISTORY_TAG_AUTHENTICATION);
				extHistory.addHistory(ref);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			return sessionManagementMethod.extractWebSession(msg);
		}
	}

	/**
	 * The credentials implementation for use in systems that require a username and password
	 * combination for authentication.
	 */
	private static class UsernamePasswordAuthenticationCredentials implements AuthenticationCredentials {

		private String username;
		private String password;

		@Override
		public boolean isConfigured() {
			return username != null && password != null;
		}
	}

	/**
	 * The Options Panel used for configuring a {@link FormBasedAuthenticationMethod}.
	 */
	private static class FormBasedAuthenticationMethodOptionsPanel extends
			AbstractAuthenticationMethodOptionsPanel<FormBasedAuthenticationMethod> {

		private static final long serialVersionUID = -9010956260384814566L;

		private static final String USER_FORM_FIELD_LABEL = Constant.messages
				.getString("authentication.method.fb.field.label.userFieldName");
		private static final String PASS_FORM_FIELD_LABEL = Constant.messages
				.getString("authentication.method.fb.field.label.passFieldName");
		private static final String LOGIN_URL_LABEL = Constant.messages
				.getString("authentication.method.fb.field.label.loginUrl");

		private ZapTextField userFormField;
		private ZapTextField passFormField;
		private ZapTextField loginUrl;

		public FormBasedAuthenticationMethodOptionsPanel() {
			super();
			initialize();
		}

		private void initialize() {
			this.setLayout(new GridBagLayout());

			this.add(new JLabel(USER_FORM_FIELD_LABEL), LayoutHelper.getGBC(0, 0, 1, 1.0d, 0.0d));
			this.userFormField = new ZapTextField();
			this.add(this.userFormField, LayoutHelper.getGBC(0, 1, 1, 1.0d, 0.0d));

			this.add(new JLabel(PASS_FORM_FIELD_LABEL), LayoutHelper.getGBC(0, 2, 1, 1.0d, 0.0d));
			this.passFormField = new ZapTextField();
			this.add(this.passFormField, LayoutHelper.getGBC(0, 3, 1, 1.0d, 0.0d));

			this.add(new JLabel(LOGIN_URL_LABEL), LayoutHelper.getGBC(0, 4, 1, 1.0d, 0.0d));
			this.loginUrl = new ZapTextField();
			this.add(this.loginUrl, LayoutHelper.getGBC(0, 5, 1, 1.0d, 0.0d));
		}

		@Override
		public boolean validateFields() {
			if (userFormField.getText().isEmpty() || passFormField.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this,
						Constant.messages.getString("authentication.method.fb.dialog.error.nofields.text"),
						Constant.messages.getString("authentication.method.fb.dialog.error.title"),
						JOptionPane.WARNING_MESSAGE);
				userFormField.requestFocusInWindow();
				return false;
			}

			try {
				new URL(loginUrl.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this,
						Constant.messages.getString("authentication.method.fb.dialog.error.url.text"),
						Constant.messages.getString("authentication.method.fb.dialog.error.title"),
						JOptionPane.WARNING_MESSAGE);
				loginUrl.requestFocusInWindow();
				return false;
			}

			return true;
		}

		@Override
		public void saveMethod() {
			getMethod().loginURL = loginUrl.getText();
			getMethod().passwordFormFieldName = passFormField.getText();
			getMethod().usernameFormFieldName = userFormField.getText();
		}

		@Override
		public void bindMethod(AuthenticationMethod<FormBasedAuthenticationMethod> method) {
			this.authenticationMethod = (FormBasedAuthenticationMethod) method;
			this.userFormField.setText(getMethod().usernameFormFieldName);
			this.passFormField.setText(getMethod().passwordFormFieldName);
			this.loginUrl.setText(getMethod().loginURL);
		}
	}

	/**
	 * The Options Panel used for configuring a {@link UsernamePasswordAuthenticationCredentials}.
	 */
	private static class UsernamePasswordAuthenticationCredentialsOptionsPanel extends
			AbstractCredentialsOptionsPanel<UsernamePasswordAuthenticationCredentials> {

		private static final long serialVersionUID = 8881019014296985804L;

		private static final String USERNAME_LABEL = Constant.messages
				.getString("authentication.method.fb.credentials.field.label.user");
		private static final String PASSWORD_LABEL = Constant.messages
				.getString("authentication.method.fb.credentials.field.label.pass");

		private ZapTextField usernameTextField;
		private JPasswordField passwordTextField;

		public UsernamePasswordAuthenticationCredentialsOptionsPanel(
				UsernamePasswordAuthenticationCredentials credentials) {
			super(credentials);
			initialize();
		}

		private void initialize() {
			this.setLayout(new GridBagLayout());

			this.add(new JLabel(USERNAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d));
			this.usernameTextField = new ZapTextField();
			if (this.getCredentials().username != null)
				this.usernameTextField.setText(this.getCredentials().username);
			this.add(this.usernameTextField, LayoutHelper.getGBC(1, 0, 1, 0.0d, new Insets(0, 4, 0, 0)));

			this.add(new JLabel(PASSWORD_LABEL), LayoutHelper.getGBC(0, 1, 1, 0.0d));
			this.passwordTextField = new JPasswordField();
			if (this.getCredentials().password != null)
				this.passwordTextField.setText(this.getCredentials().password);
			this.add(this.passwordTextField, LayoutHelper.getGBC(1, 1, 1, 1.0d, new Insets(0, 4, 0, 0)));
		}

		@Override
		public boolean validateFields() {
			if (usernameTextField.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, Constant.messages
						.getString("authentication.method.fb.credentials.dialog.error.user.text"),
						Constant.messages.getString("authentication.method.fb.dialog.error.title"),
						JOptionPane.WARNING_MESSAGE);
				usernameTextField.requestFocusInWindow();
				return false;
			}
			return true;
		}

		@Override
		public void saveCredentials() {
			getCredentials().username = usernameTextField.getText();
			getCredentials().password = new String(passwordTextField.getPassword());
		}

	}

	@Override
	public AuthenticationMethod<FormBasedAuthenticationMethod> createAuthenticationMethod(int contextId) {
		return new FormBasedAuthenticationMethod();
	}

	@Override
	public String getName() {
		return METHOD_NAME;
	}

	@Override
	public AbstractAuthenticationMethodOptionsPanel<FormBasedAuthenticationMethod> buildOptionsPanel(
			Context uiSharedContext) {
		return new FormBasedAuthenticationMethodOptionsPanel();
	}

	@Override
	public boolean hasOptionsPanel() {
		return true;
	}

	@Override
	public AbstractCredentialsOptionsPanel<? extends AuthenticationCredentials> buildCredentialsOptionsPanel(
			AuthenticationCredentials credentials, Context uiSharedContext) {
		return new UsernamePasswordAuthenticationCredentialsOptionsPanel(
				(UsernamePasswordAuthenticationCredentials) credentials);
	}

	@Override
	public boolean hasCredentialsOptionsPanel() {
		return true;
	}

	@Override
	public boolean isFactoryForMethod(Class<? extends AuthenticationMethod<?>> methodClass) {
		return methodClass.equals(FormBasedAuthenticationMethod.class);
	}

}
