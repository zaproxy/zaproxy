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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.bouncycastle.util.encoders.Base32;
import java.net.URLDecoder;
import java.net.URI;
import java.security.Key;
import javax.crypto.spec.SecretKeySpec;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;

/**
 * The credentials implementation for use in systems that require a username,
 * password and optionally a OTC token combination for authentication.
 */
public class UsernamePasswordAuthenticationCredentials implements AuthenticationCredentials {
    private static final Logger LOGGER = LogManager.getLogger(UsernamePasswordAuthenticationCredentials.class);

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
    private String mfaURI = ""; //optional

    public UsernamePasswordAuthenticationCredentials() {
        super();
    }

    public UsernamePasswordAuthenticationCredentials(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public UsernamePasswordAuthenticationCredentials(String username, String password, String mfaURI) {
        this(username, password);
        this.mfaURI = mfaURI;
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

    /**
     * Gets the MFA URI.
     *
     * @return the MFA URI
     */
    public String getMfaUri() {
        return mfaURI;
    }

    // Utility function to parse URI query parameters into a map
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    // URL decode the key and value
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    private static final List<String> SUPPORTED_OTP_ALGORITHMS = List.of("SHA1", "SHA256", "SHA512");

    public class BadOTPException extends Exception {
        private static final long serialVersionUID = 8881219014296985804L;

        public BadOTPException(String message) {
            super(message);
        }
    }

    public String getOneTimeCode() throws BadOTPException {
      return getOneTimeCode(Instant.now());
    }
    public String getOneTimeCode(Instant when) throws BadOTPException {
      if (mfaURI.isEmpty()) {
        throw new BadOTPException("The MFA URI has not been set!");
      }
        URI uri;
        try {
          uri = new URI(mfaURI);
        } catch (URISyntaxException e) {
          throw new BadOTPException("The MFA URI is malformed!");
        }
        String query = uri.getQuery();

        // Parse query parameters into a map
        Map<String, String> params = parseQuery(query);

        LOGGER.warn("TOTP: Decomposing "+mfaURI);
        // Extract parameters from the URI
        String secret = params.get("secret");
        String algorithm = params.getOrDefault("algorithm", "SHA1");
        String totpAlgorithm = TimeBasedOneTimePasswordGenerator.TOTP_ALGORITHM_HMAC_SHA1;
        int digits = Integer.parseInt(params.getOrDefault("digits", "6"));
        int period = Integer.parseInt(params.getOrDefault("period", "30"));

        LOGGER.warn("TOTP: Decomposed as {secret = "+secret+", algorithm = "+algorithm+", totpAlgorithm = "+totpAlgorithm+", digits = "+String.valueOf(digits)+", period = "+String.valueOf(period)+"}");
        // Decode the secret using Bouncy Castle Base32 decoder
        byte[] decodedSecret = Base32.decode(secret);

        // Validate the algorithm
        if (!SUPPORTED_OTP_ALGORITHMS.contains(algorithm.toUpperCase())) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }


        // Choose the algorithm dynamically
        if (totpAlgorithm.toUpperCase().equals("SHA256")) {
          totpAlgorithm = TimeBasedOneTimePasswordGenerator.TOTP_ALGORITHM_HMAC_SHA256;
        } else if (algorithm.toUpperCase().equals("SHA512")) {
          totpAlgorithm = TimeBasedOneTimePasswordGenerator.TOTP_ALGORITHM_HMAC_SHA512;
        }

        try {
          Key key = new SecretKeySpec(decodedSecret, totpAlgorithm);
          TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator(Duration.ofSeconds(period), digits, totpAlgorithm);
          return totp.generateOneTimePasswordString(key, when);
        } catch (NoSuchAlgorithmException e) {
          throw new BadOTPException("The MFA URI specifies an unsupported or invalid algorithm name");
        } catch (InvalidKeyException e) {
          throw new BadOTPException("The MFA URI specifies an invalid secret & algorithm combination");
        }
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
        if (mfaURI.length() > 0) {
          out.append(Base64.encodeBase64String(mfaURI.getBytes())).append(FIELD_SEPARATOR);
        }
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

        if (pieces.length > 0) {
          this.username = new String(Base64.decodeBase64(pieces[0]));
        } else {
          this.username = "";
        }

        if (pieces.length > 1) {
          this.password = new String(Base64.decodeBase64(pieces[1]));
        } else {
          this.password = "";
        }

        if (pieces.length > 2) {
          this.mfaURI = new String(Base64.decodeBase64(pieces[2]));
        } else {
          this.mfaURI = "";
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

        private static final String MFA_LABEL =
                Constant.messages.getString(
                        "authentication.method.fb.credentials.field.label.mfauri");

        private ZapTextField usernameTextField;
        private JPasswordField passwordTextField;
        private ZapTextField mfaUriField;

        public UsernamePasswordAuthenticationCredentialsOptionsPanel(
                UsernamePasswordAuthenticationCredentials credentials) {
            super(credentials);
            initialize();
        }

        private void initialize() {
            this.setLayout(new GridBagLayout());

            this.add(new JLabel(USERNAME_LABEL), LayoutHelper.getGBC(0, 0, 1, 0.0d));
            this.usernameTextField = new ZapTextField();
            if (this.getCredentials().username != null) {
              this.usernameTextField.setText(this.getCredentials().username);
            }
            this.add(
                    this.usernameTextField,
                    LayoutHelper.getGBC(1, 0, 1, 0.0d, new Insets(0, 4, 0, 0)));

            this.add(new JLabel(PASSWORD_LABEL), LayoutHelper.getGBC(0, 1, 1, 0.0d));
            this.passwordTextField = new JPasswordField();
            if (this.getCredentials().password != null) {
              this.passwordTextField.setText(this.getCredentials().password);
            }
            this.add(
                    this.passwordTextField,
                    LayoutHelper.getGBC(1, 1, 1, 1.0d, new Insets(0, 4, 0, 0)));

            this.add(new JLabel(MFA_LABEL), LayoutHelper.getGBC(0, 2, 1, 0.0d));
            this.mfaUriField = new ZapTextField();
            if (this.getCredentials().mfaURI != null) {
              this.mfaUriField.setText(this.getCredentials().mfaURI);
            }
            this.add(
                    this.mfaUriField,
                    LayoutHelper.getGBC(1, 2, 1, 1.0d, new Insets(0, 4, 0, 0)));

        }

        @Override
        public boolean validateFields() {
            //add some validation for the otp URI to make sure it includes the URI scheme and that sort of thing
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
            getCredentials().mfaURI = new String(mfaUriField.getText());
        }
    }

    /* API related constants and methods. */

    @Override
    public ApiResponse getApiResponseRepresentation() {
        Map<String, String> values = new HashMap<>();
        values.put("type", API_NAME);
        values.put("username", username);
        values.put("password", password);
        values.put("mfauri", mfaURI);
        return new ApiResponseSet<>("credentials", values);
    }

    private static final String ACTION_SET_CREDENTIALS = "formBasedAuthenticationCredentials";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_MFA = "mfauri";

    /**
     * Gets the api action for setting a {@link UsernamePasswordAuthenticationCredentials} for an
     * User.
     *
     * @param methodType the method type for which this is called
     * @return the sets the credentials for user api action
     */
    public static ApiDynamicActionImplementor getSetCredentialsForUserApiAction(
            final AuthenticationMethodType methodType) {
        return new ApiDynamicActionImplementor(
                ACTION_SET_CREDENTIALS, new String[] {PARAM_USERNAME, PARAM_PASSWORD, PARAM_MFA}, null) {

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
                        new UsernamePasswordAuthenticationCredentials();
                credentials.username = ApiUtils.getNonEmptyStringParam(params, PARAM_USERNAME);
                credentials.password = params.optString(PARAM_PASSWORD, "");
                credentials.mfaURI = params.optString(PARAM_MFA, "");
                user.setAuthenticationCredentials(credentials);
            }
        };
    }
}
