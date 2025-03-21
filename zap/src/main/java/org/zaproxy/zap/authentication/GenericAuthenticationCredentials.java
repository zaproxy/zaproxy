/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import net.sf.json.JSONObject;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.extension.users.UsersAPI;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;
import org.zaproxy.zap.utils.EncodingUtils;
import org.zaproxy.zap.view.DynamicFieldsPanel;

public class GenericAuthenticationCredentials extends TotpAuthenticationCredentials {

    private static final String API_NAME = "GenericAuthenticationCredentials";

    private static final String FIELD_SEPARATOR = "~";

    private String[] paramNames;
    private Map<String, String> paramValues;

    public GenericAuthenticationCredentials(String[] paramNames) {
        this(paramNames, false);
    }

    /**
     * Creates credentials enabling or not TOTP.
     *
     * @param paramNames the parameter names.
     * @param enableTotp {@code true} if TOTP should be enabled, {@code false} otherwise.
     * @since 2.16.1
     */
    public GenericAuthenticationCredentials(String[] paramNames, boolean enableTotp) {
        super(enableTotp);
        this.paramNames = paramNames;
        this.paramValues = new HashMap<>(paramNames.length);
    }

    public String getParam(String paramName) {
        return paramValues.get(paramName);
    }

    public String setParam(String paramName, String paramValue) {
        return paramValues.put(paramName, paramValue);
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public String encode(String parentFieldSeparator) {
        StringBuilder out = new StringBuilder();
        out.append(EncodingUtils.mapToString(paramValues)).append(FIELD_SEPARATOR);
        encodeTotpData(out, FIELD_SEPARATOR);
        return out.toString();
    }

    @Override
    public void decode(String encodedCredentials) {
        String[] pieces = encodedCredentials.split(FIELD_SEPARATOR);

        this.paramValues = EncodingUtils.stringToMap(pieces[0]);
        this.paramNames = this.paramValues.keySet().toArray(new String[this.paramValues.size()]);

        if (pieces.length > 1) {
            decodeTotpData(Arrays.asList(pieces).subList(1, pieces.length));
        }
    }

    @Override
    public ApiResponse getApiResponseRepresentation() {
        Map<String, Object> values = new HashMap<>(paramValues);
        values.put("type", API_NAME);
        setTotpData(values);
        return new ApiResponseSet<>("credentials", values);
    }

    /** The Options Panel used for configuring a {@link GenericAuthenticationCredentials}. */
    protected static class GenericAuthenticationCredentialsOptionsPanel
            extends AbstractCredentialsOptionsPanel<GenericAuthenticationCredentials> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = -6486907666459197059L;

        /** The dynamic fields panel. */
        private DynamicFieldsPanel fieldsPanel;

        private TotpTabbedPane totpTabbedPane;

        public GenericAuthenticationCredentialsOptionsPanel(
                GenericAuthenticationCredentials credentials) {
            super(credentials);
            initialize();
        }

        /** Initialize the options panel, creating the views. */
        private void initialize() {
            this.setLayout(new BorderLayout());

            this.fieldsPanel = new DynamicFieldsPanel(credentials.paramNames);
            this.fieldsPanel.bindFieldValues(this.credentials.paramValues);

            if (getCredentials().isTotpEnabled()) {
                totpTabbedPane = new TotpTabbedPane(fieldsPanel);
                totpTabbedPane.setTotpData(getCredentials().getTotpData());

                add(totpTabbedPane);
            } else {
                add(fieldsPanel);
            }
        }

        @Override
        public boolean validateFields() {
            try {
                this.fieldsPanel.validateFields();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        Constant.messages.getString("authentication.method.fb.dialog.error.title"),
                        JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }

        @Override
        public void saveCredentials() {
            this.credentials.paramValues = new HashMap<>(this.fieldsPanel.getFieldValues());

            if (totpTabbedPane != null) {
                credentials.setTotpData(totpTabbedPane.getTotpData());
            }
        }
    }

    private static final String ACTION_SET_CREDENTIALS = "scriptBasedAuthenticationCredentials";
    private static final String PARAM_CONFIG_PARAMS = "authenticationCredentialsParams";

    /**
     * Gets the api action for setting a {@link GenericAuthenticationCredentials} for an User.
     *
     * @param methodType the method type for which this is called
     * @return api action implementation
     */
    public static ApiDynamicActionImplementor getSetCredentialsForUserApiAction(
            final AuthenticationMethodType methodType) {
        List<String> optionalParameters = new ArrayList<>();
        optionalParameters.add(PARAM_CONFIG_PARAMS);
        if (methodType.createAuthenticationCredentials()
                        instanceof TotpAuthenticationCredentials totpCreds
                && totpCreds.isTotpEnabled()) {
            optionalParameters = TotpAuthenticationCredentials.getApiParameters();
        }
        return new ApiDynamicActionImplementor(ACTION_SET_CREDENTIALS, null, optionalParameters) {

            @Override
            public void handleAction(JSONObject params) throws ApiException {
                Context context = ApiUtils.getContextByParamId(params, UsersAPI.PARAM_CONTEXT_ID);
                int userId = ApiUtils.getIntParam(params, UsersAPI.PARAM_USER_ID);
                // Make sure the type of authentication method is compatible
                if (!methodType.isTypeForMethod(context.getAuthenticationMethod()))
                    throw new ApiException(
                            ApiException.Type.ILLEGAL_PARAMETER,
                            "User's credentials should match authentication method type of the context: "
                                    + context.getAuthenticationMethod().getType().getName());

                // NOTE: no need to check if extension is loaded as this method is called only if
                // the Users extension is loaded
                ExtensionUserManagement extensionUserManagement =
                        Control.getSingleton()
                                .getExtensionLoader()
                                .getExtension(ExtensionUserManagement.class);
                User user =
                        extensionUserManagement
                                .getContextUserAuthManager(context.getId())
                                .getUserById(userId);
                if (user == null)
                    throw new ApiException(
                            ApiException.Type.USER_NOT_FOUND, UsersAPI.PARAM_USER_ID);
                // Build and set the credentials
                GenericAuthenticationCredentials credentials =
                        (GenericAuthenticationCredentials)
                                context.getAuthenticationMethod().createAuthenticationCredentials();
                for (String paramName : credentials.paramNames)
                    credentials.setParam(
                            paramName, ApiUtils.getNonEmptyStringParam(params, paramName));
                credentials.readTotpData(params);

                user.setAuthenticationCredentials(credentials);
            }
        };
    }
}
