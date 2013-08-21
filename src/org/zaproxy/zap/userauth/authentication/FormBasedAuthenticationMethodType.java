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
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
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

	/**
	 * The implementation for an {@link AuthenticationMethod} where the Users are authenticated by
	 * posting a form with user and password.
	 */
	public static class FormBasedAuthenticationMethod implements
			AuthenticationMethod<FormBasedAuthenticationMethod> {

		private static final String LOGIN_ICON_RESOURCE = "/resource/icon/fugue/door-open-green-arrow.png";
		private static final String HISTORY_TAG_AUTHENTICATION = "Authentication";
		private static final String ENCODING_TYPE = "UTF-8";
		private static final String MSG_USER_PATTERN = "{%username%}";
		private static final String MSG_PASS_PATTERN = "{%password%}";

		private static final Logger log = Logger.getLogger(FormBasedAuthenticationMethod.class);

		private HttpSender httpSender;
		private SiteNode loginSiteNode = null;
		private HttpMessage loginMsg = null;

		@Override
		public boolean isConfigured() {
			// check if the login url is valid
			return loginMsg != null;
		}

		@Override
		public AuthenticationCredentials createAuthenticationCredentials() {
			return new UsernamePasswordAuthenticationCredentials();
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

		/**
		 * Prepares a request message, by filling the appropriate 'username' and 'password' fields
		 * in the request URI and the POST data, if any.
		 * 
		 * @param requestMessage the request message
		 * @param credentials the credentials
		 */
		private void prepareRequestMessage(HttpMessage requestMessage,
				UsernamePasswordAuthenticationCredentials credentials) throws URIException,
				NullPointerException {

			// Replace the username and password in the uri
			String requestUri = requestMessage.getRequestHeader().getURI().toString();
			requestUri = requestUri.replace(MSG_USER_PATTERN, credentials.username);
			requestUri = requestUri.replace(MSG_PASS_PATTERN, credentials.password);
			requestMessage.getRequestHeader().setURI(new URI(requestUri, false));

			// Replace the username and password in the post data of the request, if needed
			if (!requestMessage.getRequestHeader().getMethod().equals(HttpRequestHeader.GET)) {
				String requestBody = requestMessage.getRequestBody().toString();
				requestBody = requestBody.replace(MSG_USER_PATTERN, credentials.username);
				requestBody = requestBody.replace(MSG_PASS_PATTERN, credentials.password);
				requestMessage.getRequestBody().setBody(requestBody);
			}
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
			HttpMessage msg = this.loginMsg.cloneRequest();
			try {
				prepareRequestMessage(msg, cred);
				if (log.isDebugEnabled()) {
					log.debug("Authentication request header: \n" + msg.getRequestHeader());
					if (!msg.getRequestHeader().getMethod().equals(HttpRequestHeader.GET))
						log.debug("Authentication request body: \n" + msg.getRequestBody());
				}
			} catch (Exception e) {
				log.error("Unable to prepare authentication message: " + e.getMessage());
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

			// Return the web session as extracted by the session management method
			return sessionManagementMethod.extractWebSession(msg);
		}

		/**
		 * Sets the login request as being an existing SiteNode.
		 * 
		 * @param sn the new login request
		 */
		protected void setLoginRequest(SiteNode sn) throws Exception {
			// No need for resetting everything up it's already the right node
			if (sn != null && this.loginSiteNode == sn) {
				return;
			}

			if (this.loginSiteNode != null) {
				this.loginSiteNode.removeCustomIcon(LOGIN_ICON_RESOURCE);
			}

			this.loginSiteNode = sn;
			if (sn == null) {
				this.loginMsg = null;
				return;
			}
			sn.addCustomIcon(LOGIN_ICON_RESOURCE, false);

			// Set a cloned http message
			this.setLoginMsg(sn.getHistoryReference().getHttpMessage().cloneRequest());
		}

		/**
		 * Sets the login request, based on a given url and, if needed, post data. If post data is
		 * provided, the assumed HTTP method is POST.
		 * <p>
		 * If there is a SiteNode that matches the URL and post data (with the exception of the
		 * 'username' and 'password' parameters), it is marked as the 'Login' site node.
		 * </p>
		 * 
		 * @param url the url
		 * @param postData the post data, or {@code null} if the request should be a GET one
		 * @throws Exception the exception
		 */
		protected void setLoginRequest(String url, String postData) throws Exception {
			if (url == null || url.length() == 0) {
				this.setLoginRequest(null);
			} else {
				String method = HttpRequestHeader.GET;
				if (postData != null && postData.length() > 0) {
					method = HttpRequestHeader.POST;
				}
				URI uri = new URI(url, true);
				// Note: the findNode just checks the parameter names, not their values
				SiteNode sn = Model.getSingleton().getSession().getSiteTree().findNode(uri, method, postData);
				// TODO: Make sure the other parameters (besides user/password) are the same
				if (sn != null) {
					this.setLoginRequest(sn);
					this.loginMsg.getRequestHeader().setURI(uri);
					this.loginMsg.getRequestBody().setBody(postData);
				} else {
					// Haven't visited this node before, not a problem
					HttpMessage msg = new HttpMessage();
					msg.setRequestHeader(new HttpRequestHeader(method, uri, HttpHeader.HTTP10));
					msg.setRequestBody(postData);
					this.setLoginMsg(msg);
				}
			}
		}

		private void setLoginMsg(HttpMessage msg) throws Exception {
			this.loginMsg = msg;
			if (log.isDebugEnabled()) {
				log.debug("New login message set for form-based authentication:\n"
						+ msg.getRequestHeader().toString());
				if (msg.getRequestHeader().getMethod().equals(HttpRequestHeader.POST))
					log.debug(msg.getRequestBody());
			}
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

		private static final String POST_DATA_LABEL = Constant.messages
				.getString("authentication.method.fb.field.label.postData");
		private static final String LOGIN_URL_LABEL = Constant.messages
				.getString("authentication.method.fb.field.label.loginUrl");
		private static final String AUTH_DESCRIPTION = Constant.messages
				.getString("authentication.method.fb.field.label.description");

		private ZapTextField loginUrlField;
		private ZapTextField postDataField;

		public FormBasedAuthenticationMethodOptionsPanel() {
			super();
			initialize();
		}

		private void initialize() {
			this.setLayout(new GridBagLayout());

			this.add(new JLabel(LOGIN_URL_LABEL), LayoutHelper.getGBC(0, 0, 1, 1.0d, 0.0d));
			this.loginUrlField = new ZapTextField();
			this.add(this.loginUrlField, LayoutHelper.getGBC(0, 1, 1, 1.0d, 0.0d));

			this.add(new JLabel(POST_DATA_LABEL), LayoutHelper.getGBC(0, 2, 1, 1.0d, 0.0d));
			this.postDataField = new ZapTextField();
			this.add(this.postDataField, LayoutHelper.getGBC(0, 3, 1, 1.0d, 0.0d));

			this.add(new JLabel(AUTH_DESCRIPTION), LayoutHelper.getGBC(0, 4, 1, 1.0d, 0.0d));
		}

		@Override
		public boolean validateFields() {
			try {
				new URL(loginUrlField.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(this,
						Constant.messages.getString("authentication.method.fb.dialog.error.url.text"),
						Constant.messages.getString("authentication.method.fb.dialog.error.title"),
						JOptionPane.WARNING_MESSAGE);
				loginUrlField.requestFocusInWindow();
				return false;
			}

			return true;
		}

		@Override
		public void saveMethod() {
			try {
				getMethod().setLoginRequest(loginUrlField.getText(), postDataField.getText());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void bindMethod(AuthenticationMethod method) {
			this.authenticationMethod = (FormBasedAuthenticationMethod) method;
			if (authenticationMethod.loginMsg != null) {
				this.loginUrlField.setText(authenticationMethod.loginMsg.getRequestHeader().getURI()
						.toString());
				this.postDataField.setText(authenticationMethod.loginMsg.getRequestBody().toString());
			}
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
