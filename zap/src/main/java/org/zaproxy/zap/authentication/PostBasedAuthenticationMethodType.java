/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.authentication.UsernamePasswordAuthenticationCredentials.UsernamePasswordAuthenticationCredentialsOptionsPanel;
import org.zaproxy.zap.extension.anticsrf.AntiCsrfToken;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.authentication.AuthenticationAPI;
import org.zaproxy.zap.extension.authentication.ContextAuthenticationPanel;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.NameValuePair;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.NodeSelectDialog;
import org.zaproxy.zap.view.popup.PopupMenuItemContext;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContextMenuFactory;

/**
 * An {@link AuthenticationMethodType} where the Users are authenticated by POSTing the username and
 * password.
 *
 * <p>The actual format of the POST body is defined by extending classes.
 *
 * @since 2.8.0
 */
public abstract class PostBasedAuthenticationMethodType extends AuthenticationMethodType {

    private static final String CONTEXT_CONFIG_AUTH_FORM =
            AuthenticationMethod.CONTEXT_CONFIG_AUTH + ".form";
    private static final String CONTEXT_CONFIG_AUTH_FORM_LOGINURL =
            CONTEXT_CONFIG_AUTH_FORM + ".loginurl";
    private static final String CONTEXT_CONFIG_AUTH_FORM_LOGINBODY =
            CONTEXT_CONFIG_AUTH_FORM + ".loginbody";
    private static final String CONTEXT_CONFIG_AUTH_FORM_LOGINPAGEURL =
            CONTEXT_CONFIG_AUTH_FORM + ".loginpageurl";

    private static final String POST_DATA_LABEL =
            Constant.messages.getString("authentication.method.pb.field.label.postData");
    private static final String POST_DATA_REQUIRED_LABEL =
            Constant.messages.getString("authentication.method.pb.field.label.postDataRequired");
    private static final String USERNAME_PARAM_LABEL =
            Constant.messages.getString("authentication.method.pb.field.label.usernameParam");
    private static final String PASSWORD_PARAM_LABEL =
            Constant.messages.getString("authentication.method.pb.field.label.passwordParam");
    private static final String LOGIN_URL_LABEL =
            Constant.messages.getString("authentication.method.pb.field.label.loginUrl");
    private static final String LOGIN_PAGE_URL_LABEL =
            Constant.messages.getString("authentication.method.pb.field.label.loginPageUrl");
    private static final String AUTH_DESCRIPTION =
            Constant.messages.getString("authentication.method.pb.field.label.description");

    private static final Logger LOGGER =
            LogManager.getLogger(PostBasedAuthenticationMethodType.class);

    private static ExtensionAntiCSRF extAntiCsrf;

    private final String methodName;
    private final int methodIdentifier;
    private final String apiMethodName;
    private final String labelPopupMenuKey;
    private final boolean postDataRequired;

    /**
     * Constructs a {@code PostBasedAuthenticationMethodType} with the given data.
     *
     * @param methodName the name of the authentication method, should not be {@code null}.
     * @param methodIdentifier the ID of the authentication method.
     * @param apiMethodName the API name of the authentication method, should not be {@code null}.
     * @param labelPopupMenuKey the name of the menu item that flags the request as login.
     * @param postDataRequired {@code true} if the POST data is required by the authentication
     *     method, {@code false} otherwise.
     */
    protected PostBasedAuthenticationMethodType(
            String methodName,
            int methodIdentifier,
            String apiMethodName,
            String labelPopupMenuKey,
            boolean postDataRequired) {
        this.methodName = methodName;
        this.methodIdentifier = methodIdentifier;
        this.apiMethodName = apiMethodName;
        this.labelPopupMenuKey = labelPopupMenuKey;
        this.postDataRequired = postDataRequired;
    }

    /**
     * An {@link AuthenticationMethod} where the Users are authenticated by POSTing the username and
     * password.
     *
     * <p>The actual format of the POST body is defined by extending classes.
     */
    public abstract class PostBasedAuthenticationMethod extends AuthenticationMethod {

        private static final String LOGIN_ICON_RESOURCE =
                "/resource/icon/fugue/door-open-green-arrow.png";
        public static final String MSG_USER_PATTERN = TOKEN_PREFIX + "username" + TOKEN_POSTFIX;
        public static final String MSG_PASS_PATTERN = TOKEN_PREFIX + "password" + TOKEN_POSTFIX;

        private final String contentType;
        private final UnaryOperator<String> paramEncoder;

        private HttpSender httpSender;
        private SiteNode markedLoginSiteNode;
        private SiteNode loginSiteNode = null;
        /** The URL to which credentials are submitted. */
        private String loginRequestURL;
        /**
         * The URI of the login page(form). When automatically (re)authenticating, {@code ZAP} may
         * need to submit fresh cookie or(and) ACSRF token. {@code ZAP} gets those fresh values by
         * re-requesting the login page. For some web applications URI to get the login page and URI
         * to submit the login credentials({@code #loginRequestURL}) are different. Thus this
         * variable is maintained.
         *
         * <p>Initially(by default), this is equal to the value of {@code #loginRequestURL}.
         */
        private String loginPageUrl;

        private String loginRequestBody;

        /**
         * Constructs a {@code PostBasedAuthenticationMethod} with the given data.
         *
         * @param contentType the value of the Content-Type, to be added to the authentication
         *     message.
         * @param paramEncoder the encoder to be used on the authentication credentials set in the
         *     POST body.
         * @param authenticationMethod the authentication method to copy from, might be {@code
         *     null}.
         */
        protected PostBasedAuthenticationMethod(
                String contentType,
                UnaryOperator<String> paramEncoder,
                PostBasedAuthenticationMethod authenticationMethod) {
            this.contentType = contentType + "; charset=utf-8";
            this.paramEncoder = paramEncoder;
            if (authenticationMethod != null) {
                this.loginRequestURL = authenticationMethod.loginRequestURL;
                this.loginRequestBody = authenticationMethod.loginRequestBody;
                this.loginSiteNode = authenticationMethod.loginSiteNode;
                this.markedLoginSiteNode = authenticationMethod.markedLoginSiteNode;
                this.loginPageUrl = authenticationMethod.loginPageUrl;
            }
        }

        @Override
        public boolean isConfigured() {
            if (postDataRequired) {
                if (loginRequestBody == null || loginRequestBody.isEmpty()) {
                    return false;
                }
            }

            // check if the login url is valid
            return loginRequestURL != null && !loginRequestURL.isEmpty();
        }

        @Override
        public AuthenticationCredentials createAuthenticationCredentials() {
            return new UsernamePasswordAuthenticationCredentials();
        }

        protected HttpSender getHttpSender() {
            if (this.httpSender == null) {
                this.httpSender = new HttpSender(HttpSender.AUTHENTICATION_INITIATOR);
            }
            return httpSender;
        }

        /**
         * Prepares a request message, by filling the appropriate 'username' and 'password' fields
         * in the request URI and the POST data, if any.
         *
         * @param credentials the credentials
         * @return the HTTP message prepared for authentication
         * @throws URIException if failed to create the request URI
         * @throws HttpMalformedHeaderException if the constructed HTTP request is malformed
         * @throws DatabaseException if an error occurred while reading the request from database
         */
        protected HttpMessage prepareRequestMessage(
                UsernamePasswordAuthenticationCredentials credentials)
                throws URIException, HttpMalformedHeaderException, DatabaseException {

            URI requestURI =
                    createLoginUrl(
                            loginRequestURL, credentials.getUsername(), credentials.getPassword());

            // Replace the username and password in the post data of the request, if needed
            String requestBody = null;
            if (loginRequestBody != null && !loginRequestBody.isEmpty()) {
                Map<String, String> kvMap = new HashMap<>();
                kvMap.put(
                        PostBasedAuthenticationMethod.MSG_USER_PATTERN, credentials.getUsername());
                kvMap.put(
                        PostBasedAuthenticationMethod.MSG_PASS_PATTERN, credentials.getPassword());
                requestBody =
                        AuthenticationHelper.replaceUserData(loginRequestBody, kvMap, paramEncoder);
            }

            // Prepare the actual message, either based on the existing one, or create a new one
            HttpMessage requestMessage;
            if (this.loginSiteNode != null) {
                // TODO: What happens if the SiteNode was deleted?
                requestMessage =
                        loginSiteNode.getHistoryReference().getHttpMessage().cloneRequest();
                requestMessage.getRequestHeader().setURI(requestURI);
                setRequestBody(requestMessage, requestBody);
            } else {
                String method =
                        (requestBody != null) ? HttpRequestHeader.POST : HttpRequestHeader.GET;
                requestMessage = new HttpMessage();
                requestMessage.setRequestHeader(
                        new HttpRequestHeader(method, requestURI, HttpHeader.HTTP11));
                if (setRequestBody(requestMessage, requestBody)) {
                    requestMessage
                            .getRequestHeader()
                            .setHeader(HttpHeader.CONTENT_TYPE, contentType);
                }
            }

            return requestMessage;
        }

        private boolean setRequestBody(HttpMessage message, String body) {
            if (body == null) {
                return false;
            }

            message.getRequestBody().setBody(body);
            return true;
        }

        @Override
        public WebSession authenticate(
                SessionManagementMethod sessionManagementMethod,
                AuthenticationCredentials credentials,
                User user)
                throws AuthenticationMethod.UnsupportedAuthenticationCredentialsException {

            // type check
            if (!(credentials instanceof UsernamePasswordAuthenticationCredentials)) {
                user.getAuthenticationState()
                        .setLastAuthFailure(
                                "Credentials not UsernamePasswordAuthenticationCredentials");
                throw new UnsupportedAuthenticationCredentialsException(
                        "Post based authentication method only supports "
                                + UsernamePasswordAuthenticationCredentials.class.getSimpleName()
                                + ". Received: "
                                + credentials.getClass());
            }
            UsernamePasswordAuthenticationCredentials cred =
                    (UsernamePasswordAuthenticationCredentials) credentials;

            if (!cred.isConfigured()) {
                LOGGER.warn("No credentials to authenticate user: {}", user.getName());
                user.getAuthenticationState()
                        .setLastAuthFailure(
                                "No credentials to authenticate user: " + user.getName());
                return null;
            }

            // Prepare login message
            HttpMessage msg;
            try {
                // Make sure the message will be sent with a good WebSession that can record the
                // changes
                if (user.getAuthenticatedSession() == null)
                    user.setAuthenticatedSession(sessionManagementMethod.createEmptyWebSession());

                String requestUri =
                        StringUtils.isBlank(loginPageUrl) ? loginRequestURL : loginPageUrl;
                HttpMessage loginMsgToRenewCookie = new HttpMessage(new URI(requestUri, true));
                loginMsgToRenewCookie.setRequestingUser(user);
                getHttpSender().sendAndReceive(loginMsgToRenewCookie);
                AuthenticationHelper.addAuthMessageToHistory(loginMsgToRenewCookie);

                msg = prepareRequestMessage(cred);
                msg.setRequestingUser(user);

                replaceAntiCsrfTokenValueIfRequired(msg, loginMsgToRenewCookie, paramEncoder);
            } catch (Exception e) {
                LOGGER.error("Unable to prepare authentication message: {}", e.getMessage(), e);
                user.getAuthenticationState()
                        .setLastAuthFailure(
                                "Unable to prepare authentication message: " + e.getMessage());
                return null;
            }
            // Clear any session identifiers
            msg.getRequestHeader().setHeader(HttpRequestHeader.COOKIE, null);

            LOGGER.debug("Authentication request header: \n{}", msg.getRequestHeader());
            if (!msg.getRequestHeader().getMethod().equals(HttpRequestHeader.GET))
                LOGGER.debug("Authentication request body: \n{}", msg.getRequestBody());

            if (!msg.getRequestHeader().getMethod().equals(HttpRequestHeader.GET)) {
                msg.getRequestHeader().setContentLength(msg.getRequestBody().length());
            }

            // Send the authentication message
            try {
                getHttpSender().sendAndReceive(msg);
            } catch (IOException e) {
                LOGGER.error("Unable to send authentication message: {}", e.getMessage());
                user.getAuthenticationState()
                        .setLastAuthFailure(
                                "Unable to send authentication message: " + e.getMessage());
                return null;
            }
            // Add message to history
            AuthenticationHelper.addAuthMessageToHistory(msg);

            try {
                user.getAuthenticationState()
                        .setLastAuthRequestHistoryId(msg.getHistoryRef().getHistoryId());
            } catch (Exception e) {
                LOGGER.warn("Unable to set last auth request history id: {}", e.getMessage(), e);
            }

            // Update the session as it may have changed
            WebSession session = sessionManagementMethod.extractWebSession(msg);
            user.setAuthenticatedSession(session);

            if (this.isAuthenticated(msg, user, true)) {
                // Let the user know it worked
                AuthenticationHelper.notifyOutputAuthSuccessful(msg);
                user.getAuthenticationState().setLastAuthFailure("");
            } else {
                // Let the user know it failed
                AuthenticationHelper.notifyOutputAuthFailure(msg);
            }

            return session;
        }

        /**
         * Sets the login request as being an existing SiteNode.
         *
         * @param loginSiteNode the new login request
         * @throws Exception if an error occurred while obtaining the message from the node
         */
        public void setLoginRequest(SiteNode loginSiteNode) throws Exception {
            this.loginSiteNode = loginSiteNode;

            HttpMessage requestMessage = loginSiteNode.getHistoryReference().getHttpMessage();
            this.loginRequestURL = requestMessage.getRequestHeader().getURI().toString();
            if (!requestMessage
                    .getRequestHeader()
                    .getMethod()
                    .equalsIgnoreCase(HttpRequestHeader.GET)) {
                this.loginRequestBody = requestMessage.getRequestBody().toString();
            } else {
                this.loginRequestBody = null;
            }
        }

        /**
         * Gets the login request url.
         *
         * @return the login request url
         */
        public String getLoginRequestURL() {
            return loginRequestURL;
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
         *
         * <p>If there is a SiteNode that matches the URL and post data (with the exception of the
         * 'username' and 'password' parameters), it is marked as the 'Login' site node.
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

                this.loginRequestURL = url;
                this.loginRequestBody = postData;

                URI uri = createLoginUrl(loginRequestURL, "", "");
                // Note: The findNode just checks the parameter names, not their values
                // Note: No need to make sure the other parameters (besides user/password) are the
                // same, as POSTs with different values are not delimited in the SitesTree anyway
                // Note: Set the login site node anyway (even if null), to make sure any previously
                // marked SiteNode is unmarked
                this.loginSiteNode =
                        Model.getSingleton()
                                .getSession()
                                .getSiteTree()
                                .findNode(uri, method, postData);
            }
        }

        protected void setLoginPageUrl(String loginPageUrl) {
            this.loginPageUrl = loginPageUrl;
        }

        protected void setLoginPageUrl(SiteNode loginFormSiteNode)
                throws HttpMalformedHeaderException, DatabaseException {
            this.loginPageUrl =
                    loginFormSiteNode
                            .getHistoryReference()
                            .getHttpMessage()
                            .getRequestHeader()
                            .getURI()
                            .toString();
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + " [loginURI=" + loginRequestURL + "]";
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
            values.put("methodName", apiMethodName);
            values.put("loginUrl", loginRequestURL);
            values.put("loginPageUrl", loginPageUrl);
            values.put("loginRequestData", this.loginRequestBody);
            return new AuthMethodApiResponseRepresentation<>(values);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result =
                    prime * result + ((loginRequestBody == null) ? 0 : loginRequestBody.hashCode());
            result = prime * result + ((loginRequestURL == null) ? 0 : loginRequestURL.hashCode());
            result = prime * result + ((loginPageUrl == null) ? 0 : loginPageUrl.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!super.equals(obj)) return false;
            if (getClass() != obj.getClass()) return false;
            PostBasedAuthenticationMethod other = (PostBasedAuthenticationMethod) obj;
            if (loginRequestBody == null) {
                if (other.loginRequestBody != null) return false;
            } else if (!loginRequestBody.equals(other.loginRequestBody)) return false;
            if (loginRequestURL == null) {
                if (other.loginRequestURL != null) return false;
            } else if (!loginRequestURL.equals(other.loginRequestURL)) return false;
            if (loginPageUrl == null) {
                if (other.loginPageUrl != null) return false;
            } else if (!loginPageUrl.equals(other.loginPageUrl)) return false;
            return true;
        }
    }

    static void setExtAntiCsrf(ExtensionAntiCSRF ext) {
        extAntiCsrf = ext;
    }

    /**
     * <strong>Modifies</strong> the input {@code requestMessage} by replacing old anti-CSRF(ACSRF)
     * token value with the fresh one in the request body. It first checks if the input {@code
     * loginMsgWithFreshAcsrfToken} has any ACSRF token. If yes, then it modifies the input {@code
     * requestMessage} with the fresh ACSRF token value. If the {@code loginMsgWithFreshAcsrfToken}
     * does not have any ACSRF token then the input {@code requestMessage} is left as it is.
     *
     * <p>This logic relies on {@code ExtensionAntiCSRF} to extract the ACSRF token value from the
     * response. If {@code ExtensionAntiCSRF} is not available for some reason, no further
     * processing is done.
     *
     * @param requestMessage the login ({@code POST})request message with correct credentials
     * @param loginMsgWithFreshAcsrfToken the {@code HttpMessage} of the login page(form) with fresh
     *     cookie and ACSRF token.
     * @param paramEncoder the encoder to be used on the anti-csrf parameters.
     */
    static void replaceAntiCsrfTokenValueIfRequired(
            HttpMessage requestMessage,
            HttpMessage loginMsgWithFreshAcsrfToken,
            UnaryOperator<String> paramEncoder) {
        if (extAntiCsrf == null) {
            extAntiCsrf =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionAntiCSRF.class);
        }
        List<AntiCsrfToken> freshAcsrfTokens = null;
        if (extAntiCsrf != null) {
            freshAcsrfTokens = extAntiCsrf.getTokensFromResponse(loginMsgWithFreshAcsrfToken);
        } else {
            LOGGER.debug("ExtensionAntiCSRF is not available, skipping ACSRF replacing task");
            return;
        }
        if (freshAcsrfTokens == null || freshAcsrfTokens.isEmpty()) {
            LOGGER.debug(
                    "No ACSRF token found in the response of {}",
                    loginMsgWithFreshAcsrfToken.getRequestHeader());
            return;
        }

        LOGGER.debug("The login page has {} ACSRF token(s)", freshAcsrfTokens.size());

        String postRequestBody = requestMessage.getRequestBody().toString();
        Map<String, String> parameters =
                extractParametersFromPostData(
                        requestMessage.getRequestingUser().getContext(), postRequestBody);
        if (!parameters.isEmpty()) {
            String oldAcsrfTokenValue = null;
            String replacedPostData = postRequestBody;
            for (AntiCsrfToken antiCsrfToken : freshAcsrfTokens) {
                oldAcsrfTokenValue = parameters.get(antiCsrfToken.getName());
                if (oldAcsrfTokenValue == null) {
                    LOGGER.debug(
                            "ACSRF token {} not found in the POST data: {}",
                            antiCsrfToken.getName(),
                            postRequestBody);
                    continue;
                }

                replacedPostData =
                        replacedPostData.replace(
                                oldAcsrfTokenValue, paramEncoder.apply(antiCsrfToken.getValue()));

                LOGGER.debug(
                        "replaced {} old ACSRF token value with {}",
                        oldAcsrfTokenValue,
                        antiCsrfToken.getValue());
            }
            requestMessage.getRequestBody().setBody(replacedPostData);
        } else {
            LOGGER.debug("ACSRF token found but could not replace old value with fresh value");
        }
    }

    private static Map<String, String> extractParametersFromPostData(
            Context context, String postRequestBody) {
        Map<String, String> map = new HashMap<>();
        context.getPostParamParser()
                .parseParameters(postRequestBody)
                .forEach(nvp -> map.put(nvp.getName(), nvp.getValue()));
        return map;
    }

    private static URI createLoginUrl(String loginData, String username, String password)
            throws URIException {
        Map<String, String> kvMap = new HashMap<>();
        kvMap.put(PostBasedAuthenticationMethod.MSG_USER_PATTERN, username);
        kvMap.put(PostBasedAuthenticationMethod.MSG_PASS_PATTERN, password);
        return new URI(
                AuthenticationHelper.replaceUserData(
                        loginData, kvMap, PostBasedAuthenticationMethodType::encodeParameter),
                true);
    }

    protected static String encodeParameter(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ignore) {
            // UTF-8 is one of the standard charsets (see StandardCharsets.UTF_8).
        }
        return "";
    }

    private static boolean isValidLoginUrl(String loginUrl) {
        if (loginUrl.isEmpty()) {
            return false;
        }

        try {
            createLoginUrl(loginUrl, "", "");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** The Options Panel used for configuring a {@link PostBasedAuthenticationMethod}. */
    @SuppressWarnings("serial")
    protected abstract class PostBasedAuthenticationMethodOptionsPanel
            extends AbstractAuthenticationMethodOptionsPanel {

        private static final long serialVersionUID = 1L;

        /** The URI to which the login credentials are submitted. */
        private ZapTextField loginUrlField;
        /**
         * The URI to {@code GET} the login page(form). When automatically (re)authenticating,
         * {@code ZAP} may need fresh cookie or(and) ACSRF token value. {@code ZAP} gets those fresh
         * values from the fresh login page. For some web applications URI to get the login page and
         * URI to submit login credentials({@code #loginUrlField}) are different. So this field is
         * maintained to allow the pen-tester to provide the login form URI.
         *
         * <p>Initially(by default), this is equal to the value of {@code #loginUrlField}. {@code
         * ZAP} expects the pen-tester to change this value if both URIs are different.
         */
        private ZapTextField loginPageUrlField;

        private ZapTextField postDataField;
        private JComboBox<NameValuePair> usernameParameterCombo;
        private JComboBox<NameValuePair> passwordParameterCombo;
        private PostBasedAuthenticationMethod authenticationMethod;

        private Context context;
        private ExtensionUserManagement userExt = null;

        private final UnaryOperator<String> paramDecoder;

        public PostBasedAuthenticationMethodOptionsPanel(
                Context context, UnaryOperator<String> paramDecoder) {
            super();
            initialize();
            this.context = context;
            this.paramDecoder = paramDecoder;
        }

        @SuppressWarnings("unchecked")
        private void initialize() {
            this.setLayout(new GridBagLayout());

            this.add(new JLabel(LOGIN_URL_LABEL), LayoutHelper.getGBC(0, 0, 2, 1.0d, 0.0d));

            JPanel urlSelectPanel = new JPanel(new GridBagLayout());

            this.loginUrlField = new ZapTextField();
            this.loginPageUrlField = new ZapTextField();
            this.postDataField = new ZapTextField();

            JButton selectButton = new JButton(Constant.messages.getString("all.button.select"));
            selectButton.setIcon(
                    new ImageIcon(View.class.getResource("/resource/icon/16/094.png"))); // Globe

            JButton loginPageUrlSelectButton =
                    new JButton(Constant.messages.getString("all.button.select"));
            loginPageUrlSelectButton.setIcon(
                    new ImageIcon(
                            View.class.getResource("/resource/icon/16/094.png"))); // Globe Icon

            // Add behaviour for Node Select dialog
            selectButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            NodeSelectDialog nsd =
                                    new NodeSelectDialog(View.getSingleton().getMainFrame());
                            // Try to pre-select the node according to what has been inserted in the
                            // fields
                            SiteNode node = null;
                            if (loginUrlField.getText().trim().length() > 0)
                                try {
                                    // If it's a POST query
                                    if (postDataField.getText().trim().length() > 0)
                                        node =
                                                Model.getSingleton()
                                                        .getSession()
                                                        .getSiteTree()
                                                        .findNode(
                                                                new URI(
                                                                        loginUrlField.getText(),
                                                                        false),
                                                                HttpRequestHeader.POST,
                                                                postDataField.getText());
                                    else
                                        node =
                                                Model.getSingleton()
                                                        .getSession()
                                                        .getSiteTree()
                                                        .findNode(
                                                                new URI(
                                                                        loginUrlField.getText(),
                                                                        false));
                                } catch (Exception e2) {
                                    // Ignore. It means we could not properly get a node for the
                                    // existing
                                    // value and does not have any harmful effects
                                }

                            // Show the dialog and wait for input
                            node = nsd.showDialog(node);
                            if (node != null && node.getHistoryReference() != null) {
                                try {
                                    LOGGER.info(
                                            "Selected Post Based Auth Login URL via dialog: {}",
                                            node.getHistoryReference().getURI());

                                    loginUrlField.setText(
                                            node.getHistoryReference().getURI().toString());
                                    postDataField.setText(
                                            node.getHistoryReference()
                                                    .getHttpMessage()
                                                    .getRequestBody()
                                                    .toString());
                                    updateParameters();
                                    if (StringUtils.isBlank(loginPageUrlField.getText())) {
                                        loginPageUrlField.setText(loginUrlField.getText());
                                    }
                                } catch (Exception e1) {
                                    LOGGER.error(e1.getMessage(), e1);
                                }
                            }
                        }
                    });

            loginPageUrlSelectButton.addActionListener(
                    e -> {
                        NodeSelectDialog nsd =
                                new NodeSelectDialog(View.getSingleton().getMainFrame());
                        // Try to pre-select the node according to what has been inserted in the
                        // fields
                        SiteNode node = null;
                        if (!StringUtils.isBlank(loginPageUrlField.getText())) {
                            try {
                                node =
                                        Model.getSingleton()
                                                .getSession()
                                                .getSiteTree()
                                                .findNode(
                                                        new URI(
                                                                loginPageUrlField.getText(),
                                                                false));
                            } catch (Exception e1) {
                                // Ignore. It means we could not properly get a node for the
                                // existing
                                // value and does not have any harmful effects
                            }
                        }

                        // Show the dialog and wait for input
                        node = nsd.showDialog(node);
                        if (node != null && node.getHistoryReference() != null) {
                            try {
                                LOGGER.info(
                                        "Selected URL of the login page via dialog: {}",
                                        node.getHistoryReference().getURI());

                                loginPageUrlField.setText(
                                        node.getHistoryReference().getURI().toString());
                            } catch (Exception e1) {
                                LOGGER.error(e1.getMessage(), e1);
                            }
                        }
                    });

            urlSelectPanel.add(this.loginUrlField, LayoutHelper.getGBC(0, 0, 1, 1.0D));
            urlSelectPanel.add(selectButton, LayoutHelper.getGBC(1, 0, 1, 0.0D));
            urlSelectPanel.add(
                    new JLabel(LOGIN_PAGE_URL_LABEL), LayoutHelper.getGBC(0, 1, 2, 1.0d, 0.0d));
            urlSelectPanel.add(this.loginPageUrlField, LayoutHelper.getGBC(0, 2, 1, 1.0D));
            urlSelectPanel.add(loginPageUrlSelectButton, LayoutHelper.getGBC(1, 2, 1, 0.0D));
            this.add(urlSelectPanel, LayoutHelper.getGBC(0, 1, 2, 1.0d, 0.0d));

            this.add(
                    new JLabel(postDataRequired ? POST_DATA_REQUIRED_LABEL : POST_DATA_LABEL),
                    LayoutHelper.getGBC(0, 2, 2, 1.0d, 0.0d));
            this.add(this.postDataField, LayoutHelper.getGBC(0, 3, 2, 1.0d, 0.0d));

            this.add(new JLabel(USERNAME_PARAM_LABEL), LayoutHelper.getGBC(0, 4, 1, 1.0d, 0.0d));
            this.usernameParameterCombo = new JComboBox<>();
            this.usernameParameterCombo.setRenderer(NameValuePairRenderer.INSTANCE);
            this.add(usernameParameterCombo, LayoutHelper.getGBC(0, 5, 1, 1.0d, 0.0d));

            this.add(new JLabel(PASSWORD_PARAM_LABEL), LayoutHelper.getGBC(1, 4, 1, 1.0d, 0.0d));
            this.passwordParameterCombo = new JComboBox<>();
            this.passwordParameterCombo.setRenderer(NameValuePairRenderer.INSTANCE);
            this.add(passwordParameterCombo, LayoutHelper.getGBC(1, 5, 1, 1.0d, 0.0d));

            this.add(new ZapHtmlLabel(AUTH_DESCRIPTION), LayoutHelper.getGBC(0, 8, 2, 1.0d, 0.0d));

            // Make sure we update the parameters when something has been changed in the
            // postDataField
            this.postDataField.addFocusListener(
                    new FocusAdapter() {
                        @Override
                        public void focusLost(FocusEvent e) {
                            updateParameters();
                        }
                    });
        }

        /**
         * Gets the context being configured.
         *
         * @return the context, never {@code null}.
         */
        protected Context getContext() {
            return context;
        }

        @Override
        public void validateFields() {
            if (!isValidLoginUrl(loginUrlField.getText())) {
                loginUrlField.requestFocusInWindow();
                throw new IllegalStateException(
                        Constant.messages.getString(
                                "authentication.method.pb.dialog.error.url.text"));
            }

            if (postDataRequired && postDataField.getText().isEmpty()) {
                postDataField.requestFocusInWindow();
                throw new IllegalStateException(
                        Constant.messages.getString(
                                "authentication.method.pb.dialog.error.postData.text"));
            }
        }

        protected abstract String replaceParameterValue(
                String originalString, NameValuePair parameter, String replaceString);

        private ExtensionUserManagement getUserExt() {
            if (userExt == null) {
                userExt =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionUserManagement.class);
            }
            return userExt;
        }

        @Override
        public void saveMethod() {
            try {
                String postData = postDataField.getText();
                if (!postData.isEmpty()) {
                    NameValuePair userParam =
                            (NameValuePair) usernameParameterCombo.getSelectedItem();
                    NameValuePair passwdParam =
                            (NameValuePair) passwordParameterCombo.getSelectedItem();

                    ExtensionUserManagement userExt = getUserExt();
                    if (userExt != null
                            && userExt.getUIConfiguredUsers(context.getId()).isEmpty()) {
                        String username = userParam.getValue();
                        String password = passwdParam.getValue();
                        if (!username.isEmpty()
                                && !username.contains(
                                        PostBasedAuthenticationMethod.MSG_USER_PATTERN)
                                && !password.contains(
                                        PostBasedAuthenticationMethod.MSG_PASS_PATTERN)) {
                            // Add the user based on the details provided
                            String userStr = paramDecoder.apply(username);
                            String passwdStr = paramDecoder.apply(password);
                            if (!userStr.isEmpty() && !passwdStr.isEmpty()) {
                                User user = new User(context.getId(), userStr);
                                UsernamePasswordAuthenticationCredentials upac =
                                        new UsernamePasswordAuthenticationCredentials(
                                                userStr, passwdStr);
                                user.setAuthenticationCredentials(upac);
                                getUserExt()
                                        .getContextUserAuthManager(context.getId())
                                        .addUser(user);
                            }
                        }
                    }

                    postData =
                            this.replaceParameterValue(
                                    postData,
                                    userParam,
                                    PostBasedAuthenticationMethod.MSG_USER_PATTERN);
                    postData =
                            this.replaceParameterValue(
                                    postData,
                                    passwdParam,
                                    PostBasedAuthenticationMethod.MSG_PASS_PATTERN);
                }
                getMethod().setLoginRequest(loginUrlField.getText(), postData);
                getMethod().setLoginPageUrl(loginPageUrlField.getText());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        @Override
        public void bindMethod(AuthenticationMethod method) {
            this.authenticationMethod = (PostBasedAuthenticationMethod) method;
            this.loginUrlField.setText(authenticationMethod.loginRequestURL);
            this.postDataField.setText(authenticationMethod.loginRequestBody);
            this.loginPageUrlField.setText(authenticationMethod.loginPageUrl);

            updateParameters();
        }

        /**
         * Gets the index of the parameter with a given value.
         *
         * @param params the params
         * @param value the value
         * @return the index of param with value, or -1 if no match was found
         */
        private int getIndexOfParamWithValue(NameValuePair[] params, String value) {
            for (int i = 0; i < params.length; i++)
                if (value.equals(params[i].getValue())) return i;
            return -1;
        }

        private void updateParameters() {
            try {
                List<NameValuePair> params = extractParameters(this.postDataField.getText());
                NameValuePair[] paramsArray = params.toArray(new NameValuePair[params.size()]);
                this.usernameParameterCombo.setModel(new DefaultComboBoxModel<>(paramsArray));
                this.passwordParameterCombo.setModel(new DefaultComboBoxModel<>(paramsArray));

                int index =
                        getIndexOfParamWithValue(
                                paramsArray, PostBasedAuthenticationMethod.MSG_USER_PATTERN);
                if (index >= 0) {
                    this.usernameParameterCombo.setSelectedIndex(index);
                }

                index =
                        getIndexOfParamWithValue(
                                paramsArray, PostBasedAuthenticationMethod.MSG_PASS_PATTERN);
                if (index >= 0) {
                    this.passwordParameterCombo.setSelectedIndex(index);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        protected abstract List<NameValuePair> extractParameters(String postData);

        @Override
        public PostBasedAuthenticationMethod getMethod() {
            return this.authenticationMethod;
        }
    }

    /**
     * A renderer for properly displaying the name of a {@link NameValuePair} in a ComboBox.
     *
     * @see #INSTANCE
     */
    private static class NameValuePairRenderer extends BasicComboBoxRenderer {

        public static final NameValuePairRenderer INSTANCE = new NameValuePairRenderer();

        private static final long serialVersionUID = 3654541772447187317L;
        private static final Border BORDER = new EmptyBorder(2, 3, 3, 3);

        private NameValuePairRenderer() {}

        @Override
        @SuppressWarnings("rawtypes")
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                setBorder(BORDER);
                NameValuePair item = (NameValuePair) value;
                setText(item.getName());
            }
            return this;
        }
    }

    @Override
    public abstract PostBasedAuthenticationMethod createAuthenticationMethod(int contextId);

    @Override
    public String getName() {
        return methodName;
    }

    @Override
    public boolean hasOptionsPanel() {
        return true;
    }

    @Override
    public AbstractCredentialsOptionsPanel<? extends AuthenticationCredentials>
            buildCredentialsOptionsPanel(
                    AuthenticationCredentials credentials, Context uiSharedContext) {
        return new UsernamePasswordAuthenticationCredentialsOptionsPanel(
                (UsernamePasswordAuthenticationCredentials) credentials);
    }

    @Override
    public boolean hasCredentialsOptionsPanel() {
        return true;
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        if (!View.isInitialised()) {
            return;
        }
        extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoginRequestMenuFactory());
    }

    /**
     * Gets the popup menu factory for flagging login requests.
     *
     * @return the popup flag login request menu factory
     */
    private PopupMenuItemSiteNodeContextMenuFactory getPopupFlagLoginRequestMenuFactory() {
        PopupMenuItemSiteNodeContextMenuFactory popupFlagLoginRequestMenuFactory =
                new PopupMenuItemSiteNodeContextMenuFactory(
                        Constant.messages.getString("context.flag.popup")) {
                    private static final long serialVersionUID = 8927418764L;

                    @Override
                    public PopupMenuItemContext getContextMenu(Context context, String parentMenu) {
                        return new PopupMenuItemContext(
                                context,
                                parentMenu,
                                Constant.messages.getString(labelPopupMenuKey, context.getName())) {

                            private static final long serialVersionUID = 1967885623005183801L;
                            private ExtensionUserManagement usersExtension;
                            private Context uiSharedContext;

                            /**
                             * Make sure the user acknowledges the Users corresponding to this
                             * context will be deleted.
                             *
                             * @return true, if successful
                             */
                            private boolean confirmUsersDeletion(Context uiSharedContext) {
                                usersExtension =
                                        Control.getSingleton()
                                                .getExtensionLoader()
                                                .getExtension(ExtensionUserManagement.class);
                                if (usersExtension != null) {
                                    if (usersExtension.getSharedContextUsers(uiSharedContext).size()
                                            > 0) {
                                        int choice =
                                                JOptionPane.showConfirmDialog(
                                                        this,
                                                        Constant.messages.getString(
                                                                "authentication.dialog.confirmChange.label"),
                                                        Constant.messages.getString(
                                                                "authentication.dialog.confirmChange.title"),
                                                        JOptionPane.OK_CANCEL_OPTION);
                                        if (choice == JOptionPane.CANCEL_OPTION) {
                                            return false;
                                        }
                                    }
                                }
                                return true;
                            }

                            @Override
                            public void performAction(SiteNode sn) {
                                // Manually create the UI shared contexts so any modifications are
                                // done
                                // on an UI shared Context, so changes can be undone by pressing
                                // Cancel
                                SessionDialog sessionDialog =
                                        View.getSingleton().getSessionDialog();
                                sessionDialog.recreateUISharedContexts(
                                        Model.getSingleton().getSession());
                                uiSharedContext =
                                        sessionDialog.getUISharedContext(this.getContext().getId());

                                // Do the work/changes on the UI shared context
                                if (isTypeForMethod(this.getContext().getAuthenticationMethod())) {
                                    LOGGER.info(
                                            "Selected new login request via PopupMenu. Changing existing {} instance for Context {}",
                                            methodName,
                                            getContext().getId());
                                    PostBasedAuthenticationMethod method =
                                            (PostBasedAuthenticationMethod)
                                                    uiSharedContext.getAuthenticationMethod();

                                    try {
                                        method.setLoginRequest(sn);
                                        initializeLoginPageUrl(sn, method);
                                    } catch (Exception e) {
                                        LOGGER.error(
                                                "Failed to set login request: {}",
                                                e.getMessage(),
                                                e);
                                        return;
                                    }

                                    // Show the session dialog without recreating UI Shared contexts
                                    View.getSingleton()
                                            .showSessionDialog(
                                                    Model.getSingleton().getSession(),
                                                    ContextAuthenticationPanel.buildName(
                                                            this.getContext().getId()),
                                                    false);
                                } else {
                                    LOGGER.info(
                                            "Selected new login request via PopupMenu. Creating new {} instance for Context {}",
                                            methodName,
                                            getContext().getId());
                                    PostBasedAuthenticationMethod method =
                                            createAuthenticationMethod(getContext().getId());

                                    try {
                                        method.setLoginRequest(sn);
                                        initializeLoginPageUrl(sn, method);
                                    } catch (Exception e) {
                                        LOGGER.error(
                                                "Failed to set login request: {}",
                                                e.getMessage(),
                                                e);
                                        return;
                                    }
                                    if (!confirmUsersDeletion(uiSharedContext)) {
                                        LOGGER.debug("Cancelled change of authentication type.");
                                        return;
                                    }
                                    uiSharedContext.setAuthenticationMethod(method);

                                    // Show the session dialog without recreating UI Shared contexts
                                    // NOTE: First init the panels of the dialog so old users data
                                    // gets
                                    // loaded and just then delete the users
                                    // from the UI data model, otherwise the 'real' users from the
                                    // non-shared context would be loaded
                                    // and would override any deletions made.
                                    View.getSingleton()
                                            .showSessionDialog(
                                                    Model.getSingleton().getSession(),
                                                    ContextAuthenticationPanel.buildName(
                                                            this.getContext().getId()),
                                                    false,
                                                    new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            // Removing the users from the 'shared
                                                            // context' (the UI)
                                                            // will cause their removal at
                                                            // save as well
                                                            if (usersExtension != null)
                                                                usersExtension
                                                                        .removeSharedContextUsers(
                                                                                uiSharedContext);
                                                        }
                                                    });
                                }
                            }

                            private void initializeLoginPageUrl(
                                    SiteNode sn, PostBasedAuthenticationMethod method)
                                    throws HttpMalformedHeaderException, DatabaseException {
                                if (method.loginPageUrl == null || method.loginPageUrl.isEmpty()) {
                                    method.setLoginPageUrl(sn);
                                }
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
    public AuthenticationMethod loadMethodFromSession(Session session, int contextId)
            throws DatabaseException {
        PostBasedAuthenticationMethod method = createAuthenticationMethod(contextId);
        List<String> urls =
                session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1);
        String url = "";
        if (urls != null && urls.size() > 0) {
            url = urls.get(0);
        }

        List<String> postDatas =
                session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2);
        String postData = null;
        if (postDatas != null && postDatas.size() > 0) {
            postData = postDatas.get(0);
        }

        List<String> loginPageUrls =
                session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_3);
        String loginPageUrl = null;
        if (loginPageUrls != null && !loginPageUrls.isEmpty()) {
            loginPageUrl = loginPageUrls.get(0);
        }

        try {
            method.setLoginRequest(url, postData);
            method.setLoginPageUrl(loginPageUrl);
        } catch (Exception e) {
            LOGGER.error("Unable to load Post based authentication method data:", e);
        }
        return method;
    }

    @Override
    public void persistMethodToSession(
            Session session, int contextId, AuthenticationMethod authMethod)
            throws DatabaseException {
        if (!(authMethod instanceof PostBasedAuthenticationMethod)) {
            throw new UnsupportedAuthenticationMethodException(
                    "Post based authentication type only supports: "
                            + PostBasedAuthenticationMethod.class);
        }

        PostBasedAuthenticationMethod method = (PostBasedAuthenticationMethod) authMethod;
        session.setContextData(
                contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1, method.loginRequestURL);
        session.setContextData(
                contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2, method.loginRequestBody);
        session.setContextData(
                contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_3, method.loginPageUrl);
    }

    @Override
    public int getUniqueIdentifier() {
        return methodIdentifier;
    }

    @Override
    public UsernamePasswordAuthenticationCredentials createAuthenticationCredentials() {
        return new UsernamePasswordAuthenticationCredentials();
    }

    @Override
    public Class<UsernamePasswordAuthenticationCredentials> getAuthenticationCredentialsType() {
        return UsernamePasswordAuthenticationCredentials.class;
    }

    /* API related constants and methods. */
    private static final String PARAM_LOGIN_URL = "loginUrl";
    private static final String PARAM_LOGIN_REQUEST_DATA = "loginRequestData";
    private static final String PARAM_LOGIN_PAGE_URL = "loginPageUrl";

    @Override
    public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
        String[] mandatoryParamNames;
        String[] optionalParamNames;
        if (postDataRequired) {
            mandatoryParamNames = new String[] {PARAM_LOGIN_URL, PARAM_LOGIN_REQUEST_DATA};
            optionalParamNames = new String[] {PARAM_LOGIN_PAGE_URL};
        } else {
            mandatoryParamNames = new String[] {PARAM_LOGIN_URL};
            optionalParamNames = new String[] {PARAM_LOGIN_REQUEST_DATA, PARAM_LOGIN_PAGE_URL};
        }
        return new ApiDynamicActionImplementor(
                apiMethodName, mandatoryParamNames, optionalParamNames) {

            @Override
            public void handleAction(JSONObject params) throws ApiException {
                Context context =
                        ApiUtils.getContextByParamId(params, AuthenticationAPI.PARAM_CONTEXT_ID);
                String loginUrl = ApiUtils.getNonEmptyStringParam(params, PARAM_LOGIN_URL);
                if (!isValidLoginUrl(loginUrl)) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_LOGIN_URL);
                }
                String loginPageUrl = ApiUtils.getOptionalStringParam(params, PARAM_LOGIN_PAGE_URL);
                if (loginPageUrl == null || loginPageUrl.isEmpty()) {
                    loginPageUrl = loginUrl;
                } else if (!isValidLoginUrl(loginPageUrl)) {
                    throw new ApiException(
                            ApiException.Type.ILLEGAL_PARAMETER, PARAM_LOGIN_PAGE_URL);
                }
                String postData = "";
                if (postDataRequired) {
                    postData = ApiUtils.getNonEmptyStringParam(params, PARAM_LOGIN_REQUEST_DATA);
                } else if (params.containsKey(PARAM_LOGIN_REQUEST_DATA)) {
                    postData = params.getString(PARAM_LOGIN_REQUEST_DATA);
                }

                // Set the method
                PostBasedAuthenticationMethod method = createAuthenticationMethod(context.getId());
                try {
                    method.setLoginRequest(loginUrl, postData);
                    method.setLoginPageUrl(loginPageUrl);
                } catch (Exception e) {
                    throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
                }

                context.setAuthenticationMethod(method);
            }
        };
    }

    @Override
    public ApiDynamicActionImplementor getSetCredentialsForUserApiAction() {
        return UsernamePasswordAuthenticationCredentials.getSetCredentialsForUserApiAction(this);
    }

    @Override
    public void exportData(Configuration config, AuthenticationMethod authMethod) {
        if (!(authMethod instanceof PostBasedAuthenticationMethod)) {
            throw new UnsupportedAuthenticationMethodException(
                    "Post based authentication type only supports: "
                            + PostBasedAuthenticationMethod.class.getName());
        }
        PostBasedAuthenticationMethod method = (PostBasedAuthenticationMethod) authMethod;

        config.setProperty(CONTEXT_CONFIG_AUTH_FORM_LOGINURL, method.loginRequestURL);
        config.setProperty(CONTEXT_CONFIG_AUTH_FORM_LOGINBODY, method.loginRequestBody);
        config.setProperty(CONTEXT_CONFIG_AUTH_FORM_LOGINPAGEURL, method.loginPageUrl);
    }

    @Override
    public void importData(Configuration config, AuthenticationMethod authMethod)
            throws ConfigurationException {
        if (!(authMethod instanceof PostBasedAuthenticationMethod)) {
            throw new UnsupportedAuthenticationMethodException(
                    "Post based authentication type only supports: "
                            + PostBasedAuthenticationMethod.class.getName());
        }
        PostBasedAuthenticationMethod method = (PostBasedAuthenticationMethod) authMethod;

        try {
            method.setLoginRequest(
                    config.getString(CONTEXT_CONFIG_AUTH_FORM_LOGINURL),
                    config.getString(CONTEXT_CONFIG_AUTH_FORM_LOGINBODY));
            method.setLoginPageUrl(config.getString(CONTEXT_CONFIG_AUTH_FORM_LOGINPAGEURL));
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }
    }

    public static void replaceUserCredentialsDataInPollRequest(
            HttpMessage msg, User user, UnaryOperator<String> bodyEncoder) {
        if (user != null) {
            AuthenticationCredentials creds = user.getAuthenticationCredentials();
            if (creds instanceof UsernamePasswordAuthenticationCredentials) {
                Map<String, String> kvMap = new HashMap<>();
                // Only replace the username - requests really shouldnt be using the password
                kvMap.put(
                        PostBasedAuthenticationMethod.MSG_USER_PATTERN,
                        ((UsernamePasswordAuthenticationCredentials) creds).getUsername());
                AuthenticationHelper.replaceUserDataInRequest(msg, kvMap, bodyEncoder);
            }
        }
    }
}
