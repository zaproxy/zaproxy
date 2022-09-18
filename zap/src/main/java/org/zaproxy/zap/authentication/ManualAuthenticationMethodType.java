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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.Cookie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.authentication.AuthenticationAPI;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;
import org.zaproxy.zap.extension.httpsessions.HttpSession;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.extension.users.UsersAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.CookieBasedSessionManagementMethodType;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The implementation for an {@link AuthenticationMethodType} where the user manually authenticates
 * and then just selects an already authenticated {@link WebSession}.
 */
@SuppressWarnings("serial")
public class ManualAuthenticationMethodType extends AuthenticationMethodType {

    private static final int METHOD_IDENTIFIER = 0;

    /** The Authentication method's name. */
    private static final String METHOD_NAME =
            Constant.messages.getString("authentication.method.manual.name");

    private static final String API_METHOD_NAME = "manualAuthentication";

    /**
     * The implementation for an {@link AuthenticationMethod} where the user manually authenticates
     * and then just selects an already authenticated {@link WebSession}.
     */
    public static class ManualAuthenticationMethod extends AuthenticationMethod {
        private int contextId;

        public ManualAuthenticationMethod(int contextId) {
            super();
            this.contextId = contextId;
        }

        protected int getContextId() {
            return contextId;
        }

        @Override
        public boolean isConfigured() {
            // Nothing to configure
            return true;
        }

        @Override
        public AuthenticationCredentials createAuthenticationCredentials() {
            return new ManualAuthenticationCredentials();
        }

        @Override
        public WebSession authenticate(
                SessionManagementMethod sessionManagementMethod,
                AuthenticationCredentials credentials,
                User user) {
            // Check proper type
            if (!(credentials instanceof ManualAuthenticationCredentials)) {
                LogManager.getLogger(ManualAuthenticationMethod.class)
                        .error(
                                "Manual authentication credentials should be used for Manual authentication.");
                throw new UnsupportedAuthenticationCredentialsException(
                        "Manual authentication credentials should be used for Manual authentication.");
            }
            ManualAuthenticationCredentials mc = (ManualAuthenticationCredentials) credentials;
            HttpSession httpSession = mc.getSelectedSession();
            if (httpSession == null) {
                return null;
            }

            // Build a new WebSession based on the values from the HttpSession
            // TODO: Changes in either the WebSession or the HttpSession are not
            // visible in the other
            WebSession session = new CookieBasedSessionManagementMethodType.CookieBasedSession();
            for (Entry<String, Cookie> v : httpSession.getTokenValuesUnmodifiableMap().entrySet()) {
                session.getHttpState().addCookie(v.getValue());
            }
            return session;
        }

        @Override
        public AuthenticationMethodType getType() {
            return new ManualAuthenticationMethodType();
        }

        @Override
        public AuthenticationMethod duplicate() {
            return new ManualAuthenticationMethod(contextId);
        }

        @Override
        public void onMethodPersisted() {
            // Do nothing
        }

        @Override
        public void onMethodDiscarded() {
            // Do nothing
        }

        @Override
        public ApiResponse getApiResponseRepresentation() {
            Map<String, String> values = new HashMap<>();
            values.put("methodName", API_METHOD_NAME);
            return new AuthMethodApiResponseRepresentation<>(values);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + contextId;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!super.equals(obj)) return false;
            if (getClass() != obj.getClass()) return false;
            ManualAuthenticationMethod other = (ManualAuthenticationMethod) obj;
            if (contextId != other.contextId) return false;
            return true;
        }

        @Override
        public void replaceUserDataInPollRequest(HttpMessage msg, User user) {
            // Nothing to do
        }
    }

    /**
     * A credentials implementation that allows users to manually select an existing {@link
     * WebSession} that corresponds to an already authenticated session.
     */
    private static class ManualAuthenticationCredentials implements AuthenticationCredentials {

        /** The Constant defining the name/type in api calls. Should not be localized. */
        private static final String API_NAME = "ManualAuthenticationCredentials";

        private HttpSession selectedSession;

        protected HttpSession getSelectedSession() {
            return selectedSession;
        }

        @Override
        public boolean isConfigured() {
            return selectedSession != null;
        }

        protected void setSelectedSession(HttpSession selectedSession) {
            this.selectedSession = selectedSession;
        }

        @Override
        public String encode(String parentStringSeparator) {
            if (selectedSession == null) {
                return "";
            }
            return Base64.encodeBase64String(selectedSession.getName().getBytes());
        }

        @Override
        public void decode(String encodedCredentials) {
            // TODO: Currently, cannot be decoded as HttpSessions are not
            // persisted.
        }

        @Override
        public ApiResponse getApiResponseRepresentation() {
            Map<String, String> values = new HashMap<>();
            values.put("type", API_NAME);
            values.put("sessionName", selectedSession != null ? selectedSession.getName() : "");
            return new ApiResponseSet<>("credentials", values);
        }
    }

    /** The option panel for configuring {@link ManualAuthenticationCredentials} objects. */
    private static class ManualAuthenticationCredentialsOptionsPanel
            extends AbstractCredentialsOptionsPanel<ManualAuthenticationCredentials> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -8081914793980311435L;

        private static final Logger log =
                LogManager.getLogger(ManualAuthenticationCredentialsOptionsPanel.class);
        private JComboBox<HttpSession> sessionsComboBox;
        private Context uiSharedContext;

        public ManualAuthenticationCredentialsOptionsPanel(
                ManualAuthenticationCredentials credentials, Context uiSharedContext) {
            super(credentials);
            this.uiSharedContext = uiSharedContext;
            initialize();
        }

        /** Initialize the panel. */
        @SuppressWarnings("unchecked")
        protected void initialize() {
            this.setLayout(new GridBagLayout());

            JLabel sessionsLabel =
                    new JLabel(
                            Constant.messages.getString(
                                    "authentication.method.manual.field.session"));

            this.add(sessionsLabel, LayoutHelper.getGBC(0, 0, 1, 0.5D));
            this.add(getSessionsComboBox(), LayoutHelper.getGBC(1, 0, 1, 0.5D));
            this.getSessionsComboBox().setRenderer(new HttpSessionRenderer());

            this.add(
                    new ZapHtmlLabel(
                            Constant.messages.getString(
                                    "authentication.method.manual.field.description")),
                    LayoutHelper.getGBC(0, 1, 2, 0.0d, 0.0d));
        }

        /** A renderer for properly displaying the name of an HttpSession in a ComboBox. */
        private static class HttpSessionRenderer extends BasicComboBoxRenderer {
            private static final long serialVersionUID = 3654541772447187317L;

            @Override
            @SuppressWarnings("rawtypes")
            public Component getListCellRendererComponent(
                    JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value != null) {
                    HttpSession item = (HttpSession) value;
                    setText(item.getName());
                }
                return this;
            }
        }

        private JComboBox<HttpSession> getSessionsComboBox() {
            if (sessionsComboBox == null) {
                ExtensionHttpSessions extensionHttpSessions =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionHttpSessions.class);
                List<HttpSession> sessions =
                        extensionHttpSessions.getHttpSessionsForContext(uiSharedContext);
                log.debug("Found sessions for Manual Authentication Config: {}", sessions);
                sessionsComboBox =
                        new JComboBox<>(sessions.toArray(new HttpSession[sessions.size()]));
                sessionsComboBox.setSelectedItem(this.getCredentials().getSelectedSession());
            }
            return sessionsComboBox;
        }

        @Override
        public boolean validateFields() {
            // No validation needed
            return true;
        }

        @Override
        public void saveCredentials() {
            log.info(
                    "Saving Manual Authentication Method: {}",
                    getSessionsComboBox().getSelectedItem());
            getCredentials()
                    .setSelectedSession((HttpSession) getSessionsComboBox().getSelectedItem());
        }
    }

    @Override
    public String getName() {
        return METHOD_NAME;
    }

    @Override
    public boolean hasOptionsPanel() {
        // No options panel for the method
        return false;
    }

    @Override
    public boolean hasCredentialsOptionsPanel() {
        return true;
    }

    @Override
    public ManualAuthenticationMethod createAuthenticationMethod(int contextId) {
        return new ManualAuthenticationMethod(contextId);
    }

    @Override
    public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
        // Not needed
        return null;
    }

    @Override
    public AbstractCredentialsOptionsPanel<? extends AuthenticationCredentials>
            buildCredentialsOptionsPanel(
                    AuthenticationCredentials credentials, Context uiSharedContext) {
        return new ManualAuthenticationCredentialsOptionsPanel(
                (ManualAuthenticationCredentials) credentials, uiSharedContext);
    }

    @Override
    public boolean isTypeForMethod(AuthenticationMethod method) {
        return (method instanceof ManualAuthenticationMethod);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        // Do nothing
    }

    @Override
    public AuthenticationMethod loadMethodFromSession(Session session, int contextId) {
        return new ManualAuthenticationMethod(contextId);
    }

    @Override
    public void persistMethodToSession(
            Session session, int contextId, AuthenticationMethod authMethod) {
        // Nothing to persist
    }

    @Override
    public int getUniqueIdentifier() {
        return METHOD_IDENTIFIER;
    }

    @Override
    public ManualAuthenticationCredentials createAuthenticationCredentials() {
        return new ManualAuthenticationCredentials();
    }

    @Override
    public Class<ManualAuthenticationCredentials> getAuthenticationCredentialsType() {
        return ManualAuthenticationCredentials.class;
    }

    public static ManualAuthenticationCredentials createAuthenticationCredentials(
            HttpSession session) {
        ManualAuthenticationCredentials c = new ManualAuthenticationCredentials();
        c.setSelectedSession(session);
        return c;
    }

    /* API related constants and methods */

    private static final String ACTION_SET_CREDENTIALS = "manualAuthenticationCredentials";
    private static final String PARAM_SESSION_NAME = "sessionName";

    @Override
    public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
        return new ApiDynamicActionImplementor(API_METHOD_NAME, null, null) {
            @Override
            public void handleAction(JSONObject params) throws ApiException {
                Context context =
                        ApiUtils.getContextByParamId(params, AuthenticationAPI.PARAM_CONTEXT_ID);
                ManualAuthenticationMethod method = createAuthenticationMethod(context.getId());
                context.setAuthenticationMethod(method);
            }
        };
    }

    @Override
    public ApiDynamicActionImplementor getSetCredentialsForUserApiAction() {
        return new ApiDynamicActionImplementor(
                ACTION_SET_CREDENTIALS, new String[] {PARAM_SESSION_NAME}, null) {

            @Override
            public void handleAction(JSONObject params) throws ApiException {
                Context context = ApiUtils.getContextByParamId(params, UsersAPI.PARAM_CONTEXT_ID);
                int userId = ApiUtils.getIntParam(params, UsersAPI.PARAM_USER_ID);
                // Make sure the type of authentication method is compatible
                if (!isTypeForMethod(context.getAuthenticationMethod())) {
                    throw new ApiException(
                            ApiException.Type.ILLEGAL_PARAMETER,
                            "User's credentials should match authentication method type of the context: "
                                    + context.getAuthenticationMethod().getType().getName());
                }
                // NOTE: no need to check if extension is loaded as this method
                // is called only if
                // the Users
                // extension is loaded
                ExtensionUserManagement extensionUserManagement =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionUserManagement.class);
                User user =
                        extensionUserManagement
                                .getContextUserAuthManager(context.getId())
                                .getUserById(userId);
                if (user == null) {
                    throw new ApiException(Type.USER_NOT_FOUND, UsersAPI.PARAM_USER_ID);
                }
                String sessionName = ApiUtils.getNonEmptyStringParam(params, PARAM_SESSION_NAME);

                // Get the matching session
                ExtensionHttpSessions extensionHttpSessions =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionHttpSessions.class);
                if (extensionHttpSessions == null) {
                    throw new ApiException(
                            Type.NO_IMPLEMENTOR, "HttpSessions extension is not loaded.");
                }
                List<HttpSession> sessions =
                        extensionHttpSessions.getHttpSessionsForContext(context);
                HttpSession matchedSession = null;
                for (HttpSession session : sessions) {
                    if (session.getName().equals(sessionName)) {
                        matchedSession = session;
                        break;
                    }
                }
                if (matchedSession == null) {
                    throw new ApiException(ApiException.Type.DOES_NOT_EXIST, PARAM_SESSION_NAME);
                }

                // Set the credentials
                ManualAuthenticationCredentials credentials = createAuthenticationCredentials();
                credentials.setSelectedSession(matchedSession);
                user.setAuthenticationCredentials(credentials);
            }
        };
    }

    @Override
    public void exportData(Configuration config, AuthenticationMethod authMethod) {
        // Nothing to do
    }

    @Override
    public void importData(Configuration config, AuthenticationMethod authMethod)
            throws ConfigurationException {
        // Nothing to do
    }
}
