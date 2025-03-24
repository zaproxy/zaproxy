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

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import net.sf.json.JSONObject;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.authentication.UsernamePasswordAuthenticationCredentials.UsernamePasswordAuthenticationCredentialsOptionsPanel;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.authentication.AuthenticationAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.WebSession;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The implementation for an {@link AuthenticationMethodType} where the Users are authenticated
 * through HTTP Authentication.
 *
 * @see <a href="http://www.w3.org/Protocols/HTTP/1.0/spec.html#AA">HTTP/1.0 - Access
 *     Authentication</a>
 */
public class HttpAuthenticationMethodType extends AuthenticationMethodType {

    public static final String CONTEXT_CONFIG_AUTH_HTTP =
            AuthenticationMethod.CONTEXT_CONFIG_AUTH + ".http";
    public static final String CONTEXT_CONFIG_AUTH_HTTP_HOSTNAME =
            CONTEXT_CONFIG_AUTH_HTTP + ".hostname";
    public static final String CONTEXT_CONFIG_AUTH_HTTP_REALM = CONTEXT_CONFIG_AUTH_HTTP + ".realm";
    public static final String CONTEXT_CONFIG_AUTH_HTTP_PORT = CONTEXT_CONFIG_AUTH_HTTP + ".port";

    private static final Logger LOGGER = LogManager.getLogger(HttpAuthenticationMethodType.class);

    /** The unique identifier of the method. */
    private static final int METHOD_IDENTIFIER = 3;

    /** The human readable Authentication method's name. */
    private static final String METHOD_NAME =
            Constant.messages.getString("authentication.method.http.name");

    /** The Authentication method's name used in the API. */
    private static final String API_METHOD_NAME = "httpAuthentication";

    /**
     * The implementation for an {@link AuthenticationMethodType} where the Users are authenticated
     * through HTTP Authentication.
     */
    public static class HttpAuthenticationMethod extends AuthenticationMethod {

        public HttpAuthenticationMethod() {
            super();
        }

        protected String hostname;
        protected int port = 80;
        protected String realm;

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        @Override
        public boolean isConfigured() {
            return hostname != null && !hostname.isEmpty() && realm != null && !realm.isEmpty();
        }

        @Override
        protected AuthenticationMethod duplicate() {
            HttpAuthenticationMethod method = new HttpAuthenticationMethod();
            method.hostname = this.hostname;
            method.port = this.port;
            method.realm = this.realm;
            return method;
        }

        @Override
        public AuthenticationCredentials createAuthenticationCredentials() {
            return new UsernamePasswordAuthenticationCredentials();
        }

        @Override
        public AuthenticationMethodType getType() {
            return new HttpAuthenticationMethodType();
        }

        @Override
        public WebSession authenticate(
                SessionManagementMethod sessionManagementMethod,
                AuthenticationCredentials credentials,
                User user,
                Instant when)
                throws UnsupportedAuthenticationCredentialsException {

            WebSession session = user.getAuthenticatedSession();
            if (session == null) session = sessionManagementMethod.createEmptyWebSession();

            // type check
            if (!(credentials instanceof UsernamePasswordAuthenticationCredentials)) {
                user.getAuthenticationState()
                        .setLastAuthFailure(
                                "Credentials not UsernamePasswordAuthenticationCredentials");
                throw new UnsupportedAuthenticationCredentialsException(
                        "Form based authentication method only supports "
                                + UsernamePasswordAuthenticationCredentials.class.getSimpleName());
            }
            UsernamePasswordAuthenticationCredentials userCredentials =
                    (UsernamePasswordAuthenticationCredentials) credentials;

            AuthScope stateAuthScope = null;
            NTCredentials stateCredentials = null;
            try {
                stateAuthScope =
                        new AuthScope(
                                this.hostname,
                                this.port,
                                (this.realm == null || this.realm.isEmpty())
                                        ? AuthScope.ANY_REALM
                                        : this.realm);
                stateCredentials =
                        new NTCredentials(
                                userCredentials.getUsername(),
                                userCredentials.getPassword(),
                                InetAddress.getLocalHost().getCanonicalHostName(),
                                realm == null ? "" : realm);
                session.getHttpState().setCredentials(stateAuthScope, stateCredentials);
            } catch (UnknownHostException e1) {
                user.getAuthenticationState().setLastAuthFailure(e1.getMessage());
                LOGGER.error(e1.getMessage(), e1);
            }
            user.getAuthenticationState().setLastAuthFailure("");
            return session;
        }

        @Override
        public ApiResponse getApiResponseRepresentation() {
            Map<String, String> values = new HashMap<>();
            values.put("methodName", API_METHOD_NAME);
            values.put("host", this.hostname);
            values.put("port", Integer.toString(this.port));
            values.put("realm", this.realm);
            return new AuthMethodApiResponseRepresentation<>(values);
        }

        @Override
        public void replaceUserDataInPollRequest(HttpMessage msg, User user) {
            // Nothing to do
        }
    }

    /** The Options Panel used for configuring a {@link HttpAuthenticationMethod}. */
    @SuppressWarnings("serial")
    private static class HttpAuthenticationMethodOptionsPanel
            extends AbstractAuthenticationMethodOptionsPanel {

        private static final long serialVersionUID = 4341092284683481288L;

        private static final String HOSTNAME_LABEL =
                Constant.messages.getString("authentication.method.http.field.label.hostname");
        private static final String PORT_LABEL =
                Constant.messages.getString("authentication.method.http.field.label.port");
        private static final String REALM_LABEL =
                Constant.messages.getString("authentication.method.http.field.label.realm");

        private ZapTextField hostnameField;
        private ZapTextField realmField;
        private ZapPortNumberSpinner portNumberSpinner;
        private HttpAuthenticationMethod method;

        public HttpAuthenticationMethodOptionsPanel() {
            super();
            initialize();
        }

        private void initialize() {
            this.setLayout(new GridBagLayout());

            this.add(new JLabel(HOSTNAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d));
            this.hostnameField = new ZapTextField();
            this.add(
                    this.hostnameField,
                    LayoutHelper.getGBC(1, 0, 1, 1.0d, new Insets(0, 0, 0, 10)));

            this.add(new JLabel(PORT_LABEL), LayoutHelper.getGBC(2, 0, 1, 0.0d));
            this.portNumberSpinner = new ZapPortNumberSpinner(80);
            this.add(this.portNumberSpinner, LayoutHelper.getGBC(3, 0, 1, 0.0d));

            this.add(new JLabel(REALM_LABEL), LayoutHelper.getGBC(0, 1, 1, 0.0d));
            this.realmField = new ZapTextField();
            this.add(this.realmField, LayoutHelper.getGBC(1, 1, 1, 1.0d, new Insets(0, 0, 0, 10)));
        }

        @Override
        public void validateFields() throws IllegalStateException {
            try {
                new URI(hostnameField.getText());
            } catch (Exception ex) {
                hostnameField.requestFocusInWindow();
                throw new IllegalStateException(
                        Constant.messages.getString(
                                "authentication.method.http.dialog.error.url.text"));
            }
        }

        @Override
        public void saveMethod() {
            getMethod().hostname = hostnameField.getText();
            getMethod().port = portNumberSpinner.getValue();
            getMethod().realm = realmField.getText();
        }

        @Override
        public void bindMethod(AuthenticationMethod method)
                throws UnsupportedAuthenticationMethodException {
            this.method = (HttpAuthenticationMethod) method;
            this.hostnameField.setText(this.method.hostname);
            this.portNumberSpinner.setValue(this.method.port);
            this.realmField.setText(this.method.realm);
        }

        @Override
        public HttpAuthenticationMethod getMethod() {
            return method;
        }
    }

    @Override
    public HttpAuthenticationMethod createAuthenticationMethod(int contextId) {
        return new HttpAuthenticationMethod();
    }

    @Override
    public String getName() {
        return METHOD_NAME;
    }

    @Override
    public int getUniqueIdentifier() {
        return METHOD_IDENTIFIER;
    }

    @Override
    public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
        return new HttpAuthenticationMethodOptionsPanel();
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
    public boolean isTypeForMethod(AuthenticationMethod method) {
        return method != null && HttpAuthenticationMethod.class.equals(method.getClass());
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        // Nothing to hook
    }

    @Override
    public AuthenticationMethod loadMethodFromSession(Session session, int contextId)
            throws DatabaseException {
        HttpAuthenticationMethod method = createAuthenticationMethod(contextId);

        List<String> hostnames =
                session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1);
        if (hostnames != null && hostnames.size() > 0) {
            method.hostname = hostnames.get(0);
        }
        List<String> realms =
                session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2);
        if (realms != null && realms.size() > 0) {
            method.realm = realms.get(0);
        }

        List<String> ports =
                session.getContextDataStrings(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_3);
        if (ports != null && ports.size() > 0) {
            try {
                method.port = Integer.parseInt(ports.get(0));
            } catch (Exception ex) {
                LOGGER.error("Unable to load HttpAuthenticationMethod. ", ex);
            }
        }

        return method;
    }

    @Override
    public void persistMethodToSession(
            Session session, int contextId, AuthenticationMethod authMethod)
            throws UnsupportedAuthenticationMethodException, DatabaseException {
        if (!(authMethod instanceof HttpAuthenticationMethod))
            throw new UnsupportedAuthenticationMethodException(
                    "Http Authentication type only supports: " + HttpAuthenticationMethod.class);

        HttpAuthenticationMethod method = (HttpAuthenticationMethod) authMethod;
        session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_1, method.hostname);
        session.setContextData(contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_2, method.realm);
        session.setContextData(
                contextId, RecordContext.TYPE_AUTH_METHOD_FIELD_3, Integer.toString(method.port));
    }

    /* API related constants and methods. */
    private static final String PARAM_HOSTNAME = "hostname";
    private static final String PARAM_REALM = "realm";
    private static final String PARAM_PORT = "port";

    @Override
    public AuthenticationCredentials createAuthenticationCredentials() {
        return new UsernamePasswordAuthenticationCredentials();
    }

    @Override
    public Class<UsernamePasswordAuthenticationCredentials> getAuthenticationCredentialsType() {
        return UsernamePasswordAuthenticationCredentials.class;
    }

    @Override
    public ApiDynamicActionImplementor getSetMethodForContextApiAction() {
        return new ApiDynamicActionImplementor(
                API_METHOD_NAME,
                new String[] {PARAM_HOSTNAME},
                new String[] {PARAM_REALM, PARAM_PORT}) {

            @Override
            public void handleAction(JSONObject params) throws ApiException {
                Context context =
                        ApiUtils.getContextByParamId(params, AuthenticationAPI.PARAM_CONTEXT_ID);
                HttpAuthenticationMethod method = createAuthenticationMethod(context.getId());

                method.hostname = ApiUtils.getNonEmptyStringParam(params, PARAM_HOSTNAME);
                try {
                    new URI(method.hostname);
                } catch (Exception ex) {
                    throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_HOSTNAME);
                }

                method.realm = params.optString(PARAM_REALM);

                if (params.containsKey(PARAM_PORT))
                    try {
                        String portString = params.getString(PARAM_PORT);
                        method.port = Integer.parseInt(portString);
                    } catch (Exception ex) {
                        throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, PARAM_PORT);
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
        if (!(authMethod instanceof HttpAuthenticationMethod)) {
            throw new UnsupportedAuthenticationMethodException(
                    "HTTP based authentication type only supports: "
                            + HttpAuthenticationMethod.class.getName());
        }
        HttpAuthenticationMethod method = (HttpAuthenticationMethod) authMethod;
        config.setProperty(CONTEXT_CONFIG_AUTH_HTTP_HOSTNAME, method.hostname);
        config.setProperty(CONTEXT_CONFIG_AUTH_HTTP_REALM, method.realm);
        config.setProperty(CONTEXT_CONFIG_AUTH_HTTP_PORT, method.port);
    }

    @Override
    public void importData(Configuration config, AuthenticationMethod authMethod)
            throws ConfigurationException {
        if (!(authMethod instanceof HttpAuthenticationMethod)) {
            throw new UnsupportedAuthenticationMethodException(
                    "HTTP based authentication type only supports: "
                            + HttpAuthenticationMethod.class.getName());
        }
        HttpAuthenticationMethod method = (HttpAuthenticationMethod) authMethod;
        method.hostname = config.getString(CONTEXT_CONFIG_AUTH_HTTP_HOSTNAME);
        method.realm = config.getString(CONTEXT_CONFIG_AUTH_HTTP_REALM);
        method.port = config.getInt(CONTEXT_CONFIG_AUTH_HTTP_PORT);
    }
}
