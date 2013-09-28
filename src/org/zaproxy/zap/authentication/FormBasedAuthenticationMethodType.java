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
package org.zaproxy.zap.authentication;

import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.sf.json.JSONObject;

import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HtmlParameter.Type;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.authentication.UsernamePasswordAuthenticationCredentials.UsernamePasswordAuthenticationCredentialsOptionsPanel;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.authentication.AuthenticationAPI;
import org.zaproxy.zap.extension.authentication.ContextAuthenticationPanel;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenu;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenuSiteNodeFactory;
import org.zaproxy.zap.httputils.HtmlParametersUtils;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The implementation for an {@link AuthenticationMethodType} where the Users are authenticated by
 * posting a form with user and password.
 */
public class FormBasedAuthenticationMethodType extends AuthenticationMethodType {

	private static final int METHOD_IDENTIFIER = 2;

	/** The Authentication method's name. */
	private static final String METHOD_NAME = Constant.messages.getString("authentication.method.fb.name");

	private static final String API_METHOD_NAME = "formBasedAuthentication";

	private static final Logger log = Logger.getLogger(FormBasedAuthenticationMethodType.class);

	/**
	 * The implementation for an {@link AuthenticationMethod} where the Users are authenticated by
	 * posting a form with user and password.
	 */
	public static class FormBasedAuthenticationMethod extends AuthenticationMethod {

		private static final String ENCODING = "UTF-8";
		private static final String LOGIN_ICON_RESOURCE = "/resource/icon/fugue/door-open-green-arrow.png";
		private static final String HISTORY_TAG_AUTHENTICATION = "Authentication";
		public static final String MSG_USER_PATTERN = "{%username%}";
		public static final String MSG_PASS_PATTERN = "{%password%}";

		private HttpSender httpSender;
		private SiteNode markedLoginSiteNode;
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

		protected HttpSender getHttpSender() {
			if (this.httpSender == null) {
				this.httpSender = new HttpSender(Model.getSingleton().getOptionsParam().getConnectionParam(),
						true, HttpSender.AUTHENTICATION_INITIATOR);
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
					URLEncoder.encode(credentials.getUsername(), ENCODING));
			requestURL = requestURL.replace(MSG_PASS_PATTERN,
					URLEncoder.encode(credentials.getPassword(), ENCODING));
			URI requestURI = new URI(requestURL, false);

			// Replace the username and password in the post data of the request, if needed
			String requestBody = null;
			if (loginRequestBody != null && !loginRequestBody.isEmpty()) {
				requestBody = loginRequestBody.replace(MSG_USER_PATTERN,
						URLEncoder.encode(credentials.getUsername(), ENCODING));
				requestBody = requestBody.replace(MSG_PASS_PATTERN,
						URLEncoder.encode(credentials.getPassword(), ENCODING));
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
				AuthenticationCredentials credentials, User user)
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

			// Make sure the message will be sent with a good WebSession that can record the changes
			if (user.getAuthenticatedSession() == null)
				user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());
			msg.setRequestingUser(user);

			// Clear any session identifiers
			 msg.getRequestHeader().setHeader(HttpRequestHeader.COOKIE, null);

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
			// Let the user know it worked
			if (View.isInitialised()) {
				View.getSingleton().getOutputPanel()
						.append(Constant.messages.getString("authentication.output.success") + "\n");
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
				log.error("Cannot add authentication message to History tab.", ex);
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
			this.loginSiteNode = loginSiteNode;

			HttpMessage requestMessage = loginSiteNode.getHistoryReference().getHttpMessage();
			this.loginRequestURL = requestMessage.getRequestHeader().getURI().toString();
			if (requestMessage.getRequestHeader().getMethod() != HttpRequestHeader.GET)
				this.loginRequestBody = requestMessage.getRequestBody().toString();
			else
				this.loginRequestBody = null;
		}

		/**
		 * Marks the provided Site Login as being a Login request. If {@code null} is provided, no
		 * site node will be marked as login request (for the {@link Context} corresponding to this
		 * AuthenticationMethod).
		 * 
		 * @param sn the new login site node
		 */
		private void markLoginSiteNode(SiteNode sn) {
			// No need for resetting everything up if it's already the right node
			if (this.markedLoginSiteNode == sn) {
				return;
			}
			if (this.markedLoginSiteNode != null) {
				this.markedLoginSiteNode.removeCustomIcon(LOGIN_ICON_RESOURCE);
			}

			this.markedLoginSiteNode = sn;
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
				this.loginRequestURL = null;
				this.loginRequestBody = null;
				this.loginSiteNode = null;
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
				this.loginSiteNode = Model.getSingleton().getSession().getSiteTree()
						.findNode(uri, method, postData);
			}

		}

		@Override
		public String toString() {
			return "FormBasedAuthenticationMethod [loginURI=" + loginRequestURL + "]";
		}

		@Override
		public FormBasedAuthenticationMethod duplicate() {
			FormBasedAuthenticationMethod clonedMethod = new FormBasedAuthenticationMethod();
			clonedMethod.loginRequestURL = this.loginRequestURL;
			clonedMethod.loginRequestBody = this.loginRequestBody;
			clonedMethod.loginSiteNode = this.loginSiteNode;
			clonedMethod.markedLoginSiteNode = this.markedLoginSiteNode;
			return clonedMethod;
		}

		@Override
		public void onMethodPersisted() {
			markLoginSiteNode(loginSiteNode);
		}

		@Override
		public void onMethodDiscarded() {
			markLoginSiteNode(null);
		}

		@Override
		public ApiResponse getApiResponseRepresentation() {
			Map<String, String> values = new HashMap<>();
			values.put("methodName", API_METHOD_NAME);
			values.put("loginUrl", loginRequestURL);
			values.put("loginRequestData", this.loginRequestBody);
			return new ApiResponseSet("method", values);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((loginRequestBody == null) ? 0 : loginRequestBody.hashCode());
			result = prime * result + ((loginRequestURL == null) ? 0 : loginRequestURL.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			FormBasedAuthenticationMethod other = (FormBasedAuthenticationMethod) obj;
			if (loginRequestBody == null) {
				if (other.loginRequestBody != null)
					return false;
			} else if (!loginRequestBody.equals(other.loginRequestBody))
				return false;
			if (loginRequestURL == null) {
				if (other.loginRequestURL != null)
					return false;
			} else if (!loginRequestURL.equals(other.loginRequestURL))
				return false;
			return true;
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
		private JComboBox<HtmlParameter> usernameParameterCombo;
		private JComboBox<HtmlParameter> passwordParameterCombo;
		private FormBasedAuthenticationMethod authenticationMethod;

		public FormBasedAuthenticationMethodOptionsPanel() {
			super();
			initialize();
		}

		@SuppressWarnings("unchecked")
		private void initialize() {
			this.setLayout(new GridBagLayout());

			this.add(new JLabel(LOGIN_URL_LABEL), LayoutHelper.getGBC(0, 0, 2, 1.0d, 0.0d));
			this.loginUrlField = new ZapTextField();
			this.add(this.loginUrlField, LayoutHelper.getGBC(0, 1, 2, 1.0d, 0.0d));

			this.add(new JLabel(POST_DATA_LABEL), LayoutHelper.getGBC(0, 2, 2, 1.0d, 0.0d));
			this.postDataField = new ZapTextField();
			this.add(this.postDataField, LayoutHelper.getGBC(0, 3, 2, 1.0d, 0.0d));

			this.add(new JLabel("Username Param:"), LayoutHelper.getGBC(0, 4, 1, 1.0d, 0.0d));
			this.usernameParameterCombo = new JComboBox<>();
			this.usernameParameterCombo.setRenderer(new HtmlParameterRenderer());
			this.add(usernameParameterCombo, LayoutHelper.getGBC(0, 5, 1, 1.0d, 0.0d));

			this.add(new JLabel("Password Param:"), LayoutHelper.getGBC(1, 4, 1, 1.0d, 0.0d));
			this.passwordParameterCombo = new JComboBox<>();
			this.passwordParameterCombo.setRenderer(new HtmlParameterRenderer());
			this.add(passwordParameterCombo, LayoutHelper.getGBC(1, 5, 1, 1.0d, 0.0d));

			this.add(new JLabel(AUTH_DESCRIPTION), LayoutHelper.getGBC(0, 8, 2, 1.0d, 0.0d));

			this.postDataField.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					updateParameters();
				}

				@Override
				public void focusGained(FocusEvent e) {
				}
			});
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

		private String replaceParameterValue(String originalString, HtmlParameter parameter,
				String replaceString) {
			return originalString.replace(parameter.getName() + "=" + parameter.getValue(),
					parameter.getName() + "=" + replaceString);
		}

		@Override
		public void saveMethod() {
			try {
				String postData = postDataField.getText();
				if (!postData.isEmpty()) {
					postData = this.replaceParameterValue(postData,
							(HtmlParameter) usernameParameterCombo.getSelectedItem(),
							FormBasedAuthenticationMethod.MSG_USER_PATTERN);
					postData = this.replaceParameterValue(postData,
							(HtmlParameter) passwordParameterCombo.getSelectedItem(),
							FormBasedAuthenticationMethod.MSG_PASS_PATTERN);
				}
				getMethod().setLoginRequest(loginUrlField.getText(), postData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void bindMethod(AuthenticationMethod method) {
			this.authenticationMethod = (FormBasedAuthenticationMethod) method;
			this.loginUrlField.setText(authenticationMethod.loginRequestURL);
			this.postDataField.setText(authenticationMethod.loginRequestBody);

			updateParameters();
		}

		/**
		 * Gets the index of the parameter with a given value.
		 * 
		 * @param params the params
		 * @param value the value
		 * @return the index of param with value, or -1 if no match was found
		 */
		private int getIndexOfParamWithValue(HtmlParameter[] params, String value) {
			for (int i = 0; i < params.length; i++)
				if (params[i].getValue().equals(value))
					return i;
			return -1;
		}

		private void updateParameters() {
			Set<HtmlParameter> params = HtmlParametersUtils.getParamsSet(Type.form,
					this.postDataField.getText());
			HtmlParameter paramsArray[] = params.toArray(new HtmlParameter[params.size()]);
			this.usernameParameterCombo.setModel(new DefaultComboBoxModel<HtmlParameter>(paramsArray));
			this.passwordParameterCombo.setModel(new DefaultComboBoxModel<HtmlParameter>(paramsArray));

			int index = getIndexOfParamWithValue(paramsArray, FormBasedAuthenticationMethod.MSG_USER_PATTERN);
			if (index >= 0)
				this.usernameParameterCombo.setSelectedIndex(index);

			index = getIndexOfParamWithValue(paramsArray, FormBasedAuthenticationMethod.MSG_PASS_PATTERN);
			if (index >= 0)
				this.passwordParameterCombo.setSelectedIndex(index);
		}

		@Override
		public FormBasedAuthenticationMethod getMethod() {
			return this.authenticationMethod;
		}
	}

	/**
	 * A renderer for properly displaying the name of an HtmlParameter in a ComboBox.
	 */
	private static class HtmlParameterRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 3654541772447187317L;
		private static final Border BORDER = new EmptyBorder(2, 3, 3, 3);

		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				setBorder(BORDER);
				HtmlParameter item = (HtmlParameter) value;
				setText(item.getName());
			}
			return this;
		}
	}

	@Override
	public FormBasedAuthenticationMethod createAuthenticationMethod(int contextId) {
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

	@Override
	public AuthenticationMethod loadMethodFromSession(Session session, int contextId) throws SQLException {
		FormBasedAuthenticationMethod method = new FormBasedAuthenticationMethod();
		List<String> urls = session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1);
		String url = "";
		if (urls != null && urls.size() > 0) {
			url = urls.get(0);
		}

		List<String> postDatas = session.getContextDataStrings(contextId,
				RecordContext.TYPE_AUTH_METHOD_FIELD_2);
		String postData = null;
		if (postDatas != null && urls.size() > 0) {
			postData = postDatas.get(0);
		}

		try {
			method.setLoginRequest(url, postData);
		} catch (Exception e) {
			log.error("Unable to load FormBasedAuthenticationMethod. ", e);
		}
		return method;
	}

	@Override
	public void persistMethodToSession(Session session, int contextId, AuthenticationMethod authMethod)
			throws SQLException {
		if (!(authMethod instanceof FormBasedAuthenticationMethod))
			throw new UnsupportedAuthenticationMethodException(
					"Form based authentication type only supports: " + FormBasedAuthenticationMethod.class);

		FormBasedAuthenticationMethod method = (FormBasedAuthenticationMethod) authMethod;
		session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1, method.loginRequestURL);
		session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2, method.loginRequestBody);
	}

	@Override
	public int getUniqueIdentifier() {
		return METHOD_IDENTIFIER;
	}

	@Override
	public UsernamePasswordAuthenticationCredentials createAuthenticationCredentials() {
		return new UsernamePasswordAuthenticationCredentials();
	}

	/* API related constants and methods. */
	private static final String PARAM_LOGIN_URL = "loginUrl";
	private static final String PARAM_LOGIN_REQUEST_DATA = "loginRequestData";


	@Override
	public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
		return new ApiDynamicActionImplementor(API_METHOD_NAME, new String[] { PARAM_LOGIN_URL },
				new String[] { PARAM_LOGIN_REQUEST_DATA }) {

			@Override
			public void handleAction(JSONObject params) throws ApiException {
				Context context = ApiUtils.getContextByParamId(params, AuthenticationAPI.PARAM_CONTEXT_ID);
				String loginUrl = ApiUtils.getNonEmptyStringParam(params, PARAM_LOGIN_URL);
				try {
					new URL(loginUrl);
				} catch (Exception ex) {
					throw new ApiException(ApiException.Type.BAD_FORMAT, PARAM_LOGIN_URL);
				}
				String postData = "";
				if (params.containsKey(PARAM_LOGIN_REQUEST_DATA))
					postData = params.getString(PARAM_LOGIN_REQUEST_DATA);

				// Set the method
				FormBasedAuthenticationMethod method = createAuthenticationMethod(context.getIndex());
				try {
					method.setLoginRequest(loginUrl, postData);
				} catch (Exception e) {
					e.printStackTrace();
					throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
				}

				if(!context.getAuthenticationMethod().isSameType(method))
					apiChangedAuthenticationMethodForContext(context.getIndex());
				context.setAuthenticationMethod(method);
			}
		};
	}

	@Override
	public ApiDynamicActionImplementor getSetCredentialsForUserApiAction() {
		return UsernamePasswordAuthenticationCredentials.getSetCredentialsForUserApiAction(this);
	}

}
