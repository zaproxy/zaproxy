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
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenu;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenuSiteNodeFactory;
import org.zaproxy.zap.extension.userauth.auth.ContextAuthenticationPanel;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.session.SessionManagementMethod;
import org.zaproxy.zap.userauth.session.WebSession;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The implementation for an {@link AuthenticationMethodType} where the Users are authenticated by
 * posting a form with user and password.
 */
public class FormBasedAuthenticationMethodType extends AuthenticationMethodType {

	/** The Authentication method's name. */
	private static final String METHOD_NAME = Constant.messages.getString("authentication.method.fb.name");

	private static final Logger log = Logger.getLogger(FormBasedAuthenticationMethodType.class);

	/**
	 * The implementation for an {@link AuthenticationMethod} where the Users are authenticated by
	 * posting a form with user and password.
	 */
	public static class FormBasedAuthenticationMethod implements AuthenticationMethod {

		private static final String ENCODING = "UTF-8";
		private static final String LOGIN_ICON_RESOURCE = "/resource/icon/fugue/door-open-green-arrow.png";
		private static final String HISTORY_TAG_AUTHENTICATION = "Authentication";
		private static final String MSG_USER_PATTERN = "{%username%}";
		private static final String MSG_PASS_PATTERN = "{%password%}";

		private HttpSender httpSender;
		private SiteNode loginSiteNode = null;
		private String loginRequestURL;
		private String loginRequestBody;

		@Override
		public boolean isConfigured() {
			// check if the login url is valid
			return loginRequestURL != null && !loginRequestURL.isEmpty();
		}

		@Override
		public AuthenticationCredentials createAuthenticationCredentials() {
			return new UsernamePasswordAuthenticationCredentials();
		}

		@Override
		public AuthenticationMethodType getType() {
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
		 * @throws SQLException
		 * @throws HttpMalformedHeaderException
		 * @throws UnsupportedEncodingException
		 */
		private HttpMessage prepareRequestMessage(UsernamePasswordAuthenticationCredentials credentials)
				throws URIException, NullPointerException, HttpMalformedHeaderException, SQLException,
				UnsupportedEncodingException {

			// Replace the username and password in the uri
			String requestURL = loginRequestURL.replace(MSG_USER_PATTERN,
					URLEncoder.encode(credentials.username, ENCODING));
			requestURL = requestURL.replace(MSG_PASS_PATTERN,
					URLEncoder.encode(credentials.password, ENCODING));
			URI requestURI = new URI(requestURL, false);

			// Replace the username and password in the post data of the request, if needed
			String requestBody = null;
			if (loginRequestBody != null && !loginRequestBody.isEmpty()) {
				requestBody = loginRequestBody.replace(MSG_USER_PATTERN,
						URLEncoder.encode(credentials.username, ENCODING));
				requestBody = requestBody.replace(MSG_PASS_PATTERN,
						URLEncoder.encode(credentials.password, ENCODING));
			}

			// Prepare the actual message, either based on the existing one, or create a new one
			HttpMessage requestMessage;
			if (this.loginSiteNode != null) {
				// TODO: What happens if the SiteNode was deleted?
				requestMessage = loginSiteNode.getHistoryReference().getHttpMessage().cloneRequest();
				requestMessage.getRequestHeader().setURI(requestURI);
				if (requestBody != null) {
					requestMessage.getRequestBody().setBody(requestBody);
					requestMessage.getRequestHeader().setHeader(HttpHeader.CONTENT_LENGTH, null);
				}
			} else {
				String method = (requestBody != null) ? HttpRequestHeader.POST : HttpRequestHeader.GET;
				requestMessage = new HttpMessage();
				requestMessage.setRequestHeader(new HttpRequestHeader(method, requestURI, HttpHeader.HTTP10));
				if (requestBody != null) {
					requestMessage.getRequestBody().setBody(requestBody);
				}
			}

			return requestMessage;
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
			HttpMessage msg;
			try {
				msg = prepareRequestMessage(cred);
			} catch (Exception e) {
				log.error("Unable to prepare authentication message: " + e.getMessage());
				return null;
			}

			// Clear any session identifiers
			sessionManagementMethod.clearWebSessionIdentifiers(msg);

			if (log.isDebugEnabled()) {
				log.debug("Authentication request header: \n" + msg.getRequestHeader());
				if (!msg.getRequestHeader().getMethod().equals(HttpRequestHeader.GET))
					log.debug("Authentication request body: \n" + msg.getRequestBody());
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
		public void setLoginRequest(SiteNode loginSiteNode) throws Exception {
			setLoginSiteNode(loginSiteNode);

			HttpMessage requestMessage = loginSiteNode.getHistoryReference().getHttpMessage();
			this.loginRequestURL = requestMessage.getRequestHeader().getURI().toString();
			if (requestMessage.getRequestHeader().getMethod() != HttpRequestHeader.GET)
				this.loginRequestBody = requestMessage.getRequestBody().toString();
			else
				this.loginRequestBody = null;
		}

		/**
		 * Sets the login site node.
		 * 
		 * @param sn the new login site node
		 */
		private void setLoginSiteNode(SiteNode sn) {
			// No need for resetting everything up if it's already the right node
			if (this.loginSiteNode == sn) {
				return;
			}
			if (this.loginSiteNode != null) {
				this.loginSiteNode.removeCustomIcon(LOGIN_ICON_RESOURCE);
			}

			this.loginSiteNode = sn;
			if (sn == null) {
				return;
			}
			sn.addCustomIcon(LOGIN_ICON_RESOURCE, false);
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

				this.loginRequestURL = url;
				this.loginRequestBody = postData;

				// Note: The findNode just checks the parameter names, not their values
				// Note: No need to make sure the other parameters (besides user/password) are the
				// same, as POSTs with different values are not delimited in the SitesTree anyway
				// Note: Set the login site node anyway (even if null), to make sure any previously
				// marked SiteNode is unmarked
				SiteNode sn = Model.getSingleton().getSession().getSiteTree().findNode(uri, method, postData);
				this.setLoginSiteNode(sn);
			}
		}

		@Override
		public String toString() {
			return "FormBasedAuthenticationMethod [loginURI=" + loginRequestURL + "]";
		}

		@Override
		public AuthenticationMethod duplicate() {
			FormBasedAuthenticationMethod clonedMethod = new FormBasedAuthenticationMethod();
			clonedMethod.loginRequestURL = this.loginRequestURL;
			clonedMethod.loginRequestBody = this.loginRequestBody;
			clonedMethod.loginSiteNode = this.loginSiteNode;
			return clonedMethod;
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
			AbstractAuthenticationMethodOptionsPanel {

		private static final long serialVersionUID = -9010956260384814566L;

		private static final String POST_DATA_LABEL = Constant.messages
				.getString("authentication.method.fb.field.label.postData");
		private static final String LOGIN_URL_LABEL = Constant.messages
				.getString("authentication.method.fb.field.label.loginUrl");
		private static final String AUTH_DESCRIPTION = Constant.messages
				.getString("authentication.method.fb.field.label.description");

		private ZapTextField loginUrlField;
		private ZapTextField postDataField;
		private FormBasedAuthenticationMethod authenticationMethod;

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
		public void validateFields() {
			try {
				new URL(loginUrlField.getText());
			} catch (Exception ex) {
				loginUrlField.requestFocusInWindow();
				throw new IllegalStateException(
						Constant.messages.getString("authentication.method.fb.dialog.error.url.text"));
			}
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
			this.loginUrlField.setText(authenticationMethod.loginRequestURL);
			this.postDataField.setText(authenticationMethod.loginRequestBody);
		}

		@Override
		public FormBasedAuthenticationMethod getMethod() {
			return this.authenticationMethod;
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
	public AuthenticationMethod createAuthenticationMethod(int contextId) {
		return new FormBasedAuthenticationMethod();
	}

	@Override
	public String getName() {
		return METHOD_NAME;
	}

	@Override
	public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
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
	public boolean isTypeForMethod(AuthenticationMethod method) {
		return (method instanceof FormBasedAuthenticationMethod);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoginRequestMenuFactory());
	}

	/**
	 * Gets the popup menu factory for flagging login requests.
	 * 
	 * @return the popup flag login request menu factory
	 */
	private PopupContextMenuSiteNodeFactory getPopupFlagLoginRequestMenuFactory() {
		PopupContextMenuSiteNodeFactory popupFlagLoginRequestMenuFactory = new PopupContextMenuSiteNodeFactory(
				Constant.messages.getString("context.flag.popup")) {
			private static final long serialVersionUID = 8927418764L;

			@Override
			public PopupContextMenu getContextMenu(Context context, String parentMenu) {
				return new PopupContextMenu(context, parentMenu, MessageFormat.format(
						Constant.messages.getString("authentication.method.fb.popup.login.request"),
						context.getName())) {

					private static final long serialVersionUID = 1967885623005183801L;

					@Override
					public void performAction(SiteNode sn) throws Exception {
						// Manually create the UI shared contexts so any modifications are done
						// on an UI shared Context, so changes can be undone by pressing Cancel
						SessionDialog sessionDialog = View.getSingleton().getSessionDialog();
						sessionDialog.recreateUISharedContexts(Model.getSingleton().getSession());
						Context uiSharedContext = sessionDialog.getUISharedContext(this.getContext()
								.getIndex());

						// Do the work/changes on the UI shared context
						if (this.getContext().getAuthenticationMethod() instanceof FormBasedAuthenticationMethod) {
							log.info("Selected new login request via PopupMenu. Changing existing Form-Based Authentication instance for Context "
									+ getContext().getIndex());
							FormBasedAuthenticationMethod method = (FormBasedAuthenticationMethod) uiSharedContext
									.getAuthenticationMethod();
							method.setLoginRequest(sn);
						} else {
							log.info("Selected new login request via PopupMenu. Creating new Form-Based Authentication instance for Context "
									+ getContext().getIndex());
							FormBasedAuthenticationMethod method = new FormBasedAuthenticationMethod();
							method.setLoginRequest(sn);
							uiSharedContext.setAuthenticationMethod(method);
						}

						// Show the session dialog without recreating UI Shared contexts
						View.getSingleton().showSessionDialog(Model.getSingleton().getSession(),
								ContextAuthenticationPanel.buildName(this.getContext().getIndex()), false);
					}
				};
			}

			@Override
			public int getParentMenuIndex() {
				return 3;
			}
		};
		return popupFlagLoginRequestMenuFactory;
	}
}
