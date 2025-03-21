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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
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
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.LayoutHelper;

/**
 * The credentials implementation for use in systems that require a username and password
 * combination for authentication.
 */
public class UsernamePasswordAuthenticationCredentials extends TotpAuthenticationCredentials {

    private static final String API_NAME = "UsernamePasswordAuthenticationCredentials";

    /**
     * String used to represent encoded null credentials, that is, when {@code username} is {@code
     * null}.
     *
     * <p>It's a null character Base64 encoded, which will never be equal to encoding of defined
     * {@code username}/{@code password}.
     *
     * @see #encode(String)
     * @see #decode(String)
     */
    private static final String NULL_CREDENTIALS = "AA==";

    private static String FIELD_SEPARATOR = "~";
    private String username;
    private String password;

    public UsernamePasswordAuthenticationCredentials() {
        this(false);
    }

    /**
     * Creates empty credentials enabling or not TOTP.
     *
     * @param enableTotp {@code true} if TOTP should be enabled, {@code false} otherwise.
     * @since 2.16.1
     */
    public UsernamePasswordAuthenticationCredentials(boolean enableTotp) {
        super(enableTotp);
    }

    public UsernamePasswordAuthenticationCredentials(String username, String password) {
        this(username, password, false);
    }

    /**
     * Creates the credentials enabling or not TOTP.
     *
     * @param username the name of the user.
     * @param password the password of the user.
     * @param enableTotp {@code true} if TOTP should be enabled, {@code false} otherwise.
     * @since 2.16.1
     */
    public UsernamePasswordAuthenticationCredentials(
            String username, String password, boolean enableTotp) {
        super(enableTotp);
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isConfigured() {
        return username != null && password != null;
    }

    @Override
    public String encode(String parentStringSeparator) {
        if (FIELD_SEPARATOR.equals(parentStringSeparator)) {
            throw new IllegalArgumentException(
                    "The string separator must not be the same as Field Separator ("
                            + FIELD_SEPARATOR
                            + ").");
        }
        if (username == null) {
            return NULL_CREDENTIALS;
        }

        StringBuilder out = new StringBuilder();
        out.append(Base64.encodeBase64String(username.getBytes())).append(FIELD_SEPARATOR);
        out.append(Base64.encodeBase64String(password.getBytes())).append(FIELD_SEPARATOR);
        encodeTotpData(out, FIELD_SEPARATOR);
        return out.toString();
    }

    @Override
    public void decode(String encodedCredentials) {
        if (NULL_CREDENTIALS.equals(encodedCredentials)) {
            username = null;
            password = null;
            return;
        }

        String[] pieces = encodedCredentials.split(FIELD_SEPARATOR);
        if (pieces.length == 0) {
            this.username = "";
            this.password = "";
            return;
        }

        this.username = new String(Base64.decodeBase64(pieces[0]));
        if (pieces.length > 1) this.password = new String(Base64.decodeBase64(pieces[1]));
        else this.password = "";

        if (pieces.length > 2) {
            decodeTotpData(Arrays.asList(pieces).subList(2, pieces.length));
        }
    }

    /**
     * The Options Panel used for configuring a {@link UsernamePasswordAuthenticationCredentials}.
     */
    public static class UsernamePasswordAuthenticationCredentialsOptionsPanel
            extends AbstractCredentialsOptionsPanel<UsernamePasswordAuthenticationCredentials> {

        private static final long serialVersionUID = 8881019014296985804L;

        private static final String USERNAME_LABEL =
                Constant.messages.getString(
                        "authentication.method.fb.credentials.field.label.user");
        private static final String PASSWORD_LABEL =
                Constant.messages.getString(
                        "authentication.method.fb.credentials.field.label.pass");

        private ZapTextField usernameTextField;
        private JPasswordField passwordTextField;

        private TotpTabbedPane totpTabbedPane;

        public UsernamePasswordAuthenticationCredentialsOptionsPanel(
                UsernamePasswordAuthenticationCredentials credentials) {
            super(credentials);
            initialize();
        }

        private void initialize() {
            this.setLayout(new GridBagLayout());

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            panel.add(new JLabel(USERNAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d));
            this.usernameTextField = new ZapTextField();
            if (this.getCredentials().username != null)
                this.usernameTextField.setText(this.getCredentials().username);
            panel.add(
                    this.usernameTextField,
                    LayoutHelper.getGBC(1, 0, 1, 0.0d, new Insets(0, 4, 0, 0)));

            panel.add(new JLabel(PASSWORD_LABEL), LayoutHelper.getGBC(0, 1, 1, 0.0d));
            this.passwordTextField = new JPasswordField();
            if (this.getCredentials().password != null)
                this.passwordTextField.setText(this.getCredentials().password);
            panel.add(
                    this.passwordTextField,
                    LayoutHelper.getGBC(1, 1, 1, 1.0d, new Insets(0, 4, 0, 0)));

            if (getCredentials().isTotpEnabled()) {
                panel.add(Box.createVerticalGlue(), LayoutHelper.getGBC(0, 2, 2, 0.0d, 1.0d));

                totpTabbedPane = new TotpTabbedPane(panel);
                totpTabbedPane.setTotpData(getCredentials().getTotpData());

                add(totpTabbedPane);
            } else {
                add(panel);
            }
        }

        @Override
        public boolean validateFields() {
            if (usernameTextField.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        Constant.messages.getString(
                                "authentication.method.fb.credentials.dialog.error.user.text"),
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

            if (totpTabbedPane != null) {
                getCredentials().setTotpData(totpTabbedPane.getTotpData());
            }
        }
    }

    /* API related constants and methods. */

    @Override
    public ApiResponse getApiResponseRepresentation() {
        Map<String, Object> values = new HashMap<>();
        values.put("type", API_NAME);
        values.put("username", username);
        values.put("password", password);
        setTotpData(values);
        return new ApiResponseSet<>("credentials", values);
    }

    private static final String ACTION_SET_CREDENTIALS = "formBasedAuthenticationCredentials";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";

    /**
     * Gets the api action for setting a {@link UsernamePasswordAuthenticationCredentials} for an
     * User.
     *
     * @param methodType the method type for which this is called
     * @return the sets the credentials for user api action
     */
    public static ApiDynamicActionImplementor getSetCredentialsForUserApiAction(
            final AuthenticationMethodType methodType) {

        List<String> optionalParameters = List.of();
        if (methodType.createAuthenticationCredentials()
                        instanceof TotpAuthenticationCredentials totpCreds
                && totpCreds.isTotpEnabled()) {
            optionalParameters = TotpAuthenticationCredentials.getApiParameters();
        }

        return new ApiDynamicActionImplementor(
                ACTION_SET_CREDENTIALS,
                List.of(PARAM_USERNAME, PARAM_PASSWORD),
                optionalParameters) {

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
                if (user == null)
                    throw new ApiException(
                            ApiException.Type.USER_NOT_FOUND, UsersAPI.PARAM_USER_ID);
                // Build and set the credentials
                UsernamePasswordAuthenticationCredentials credentials =
                        (UsernamePasswordAuthenticationCredentials)
                                context.getAuthenticationMethod().createAuthenticationCredentials();
                credentials.username = ApiUtils.getNonEmptyStringParam(params, PARAM_USERNAME);
                credentials.password = params.optString(PARAM_PASSWORD, "");
                credentials.readTotpData(params);

                user.setAuthenticationCredentials(credentials);
            }
        };
    }
}
