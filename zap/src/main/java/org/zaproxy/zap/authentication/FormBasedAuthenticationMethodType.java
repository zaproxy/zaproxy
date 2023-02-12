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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.DefaultNameValuePair;
import org.zaproxy.zap.model.NameValuePair;
import org.zaproxy.zap.users.User;

/**
 * An {@link AuthenticationMethodType} where the Users are authenticated by posting a form ({@code
 * application/x-www-form-urlencoded}) with its username and password.
 */
public class FormBasedAuthenticationMethodType extends PostBasedAuthenticationMethodType {

    private static final int METHOD_IDENTIFIER = 2;

    /** The Authentication method's name. */
    private static final String METHOD_NAME =
            Constant.messages.getString("authentication.method.fb.name");

    private static final String API_METHOD_NAME = "formBasedAuthentication";

    private static final Logger LOGGER =
            LogManager.getLogger(FormBasedAuthenticationMethodType.class);

    private static final UnaryOperator<String> PARAM_ENCODER =
            value -> {
                try {
                    return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException ignore) {
                    // Standard charset.
                }
                return "";
            };

    private static final UnaryOperator<String> PARAM_DECODER =
            value -> {
                try {
                    return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException ignore) {
                    // Standard charset.
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Failed to URL decode: {}", value, e);
                }
                return "";
            };

    public FormBasedAuthenticationMethodType() {
        super(
                METHOD_NAME,
                METHOD_IDENTIFIER,
                API_METHOD_NAME,
                "authentication.method.fb.popup.login.request",
                false);
    }

    /**
     * An {@link AuthenticationMethod} where the Users are authenticated by posting a form ({@code
     * application/x-www-form-urlencoded}) with its username and password.
     */
    public class FormBasedAuthenticationMethod extends PostBasedAuthenticationMethod {

        /** Constructs a {@code FormBasedAuthenticationMethod}. */
        public FormBasedAuthenticationMethod() {
            this(null);
        }

        /**
         * Constructs a {@code FormBasedAuthenticationMethod} based on the given method.
         *
         * @param formBasedAuthenticationMethod the method to copy.
         */
        private FormBasedAuthenticationMethod(
                FormBasedAuthenticationMethod formBasedAuthenticationMethod) {
            super(
                    HttpHeader.FORM_URLENCODED_CONTENT_TYPE,
                    PARAM_ENCODER,
                    formBasedAuthenticationMethod);
        }

        @Override
        public AuthenticationMethodType getType() {
            return new FormBasedAuthenticationMethodType();
        }

        @Override
        protected AuthenticationMethod duplicate() {
            return new FormBasedAuthenticationMethod(this);
        }

        @Override
        public void replaceUserDataInPollRequest(HttpMessage msg, User user) {
            PostBasedAuthenticationMethodType.replaceUserCredentialsDataInPollRequest(
                    msg, user, PARAM_ENCODER);
        }
    }

    /** The options panel to configure a {@link FormBasedAuthenticationMethod}. */
    private class FormBasedAuthenticationMethodOptionsPanel
            extends PostBasedAuthenticationMethodOptionsPanel {

        private static final long serialVersionUID = 1L;

        /**
         * Constructs a {@code FormBasedAuthenticationMethodOptionsPanel} for the given context.
         *
         * @param context the context to be configured.
         */
        public FormBasedAuthenticationMethodOptionsPanel(Context context) {
            super(context, PARAM_DECODER);
        }

        @Override
        protected List<NameValuePair> extractParameters(String postData) {
            List<NameValuePair> parameters = new ArrayList<>();
            getContext()
                    .getPostParamParser()
                    .parseParameters(postData)
                    .forEach(
                            (nvp) ->
                                    parameters.add(
                                            new DefaultNameValuePair(
                                                    nvp.getName(), nvp.getValue())));
            return parameters;
        }

        @Override
        protected String replaceParameterValue(
                String originalString, NameValuePair parameter, String replaceString) {
            String name = PARAM_ENCODER.apply(parameter.getName());
            String value = PARAM_ENCODER.apply(parameter.getValue());

            String keyValueSeparator =
                    getContext().getPostParamParser().getDefaultKeyValueSeparator();
            String nameAndSeparator = name + keyValueSeparator;
            // Make sure we handle the case when there's only the parameter name in the POST data
            // instead of
            // parameter name + separator + value (e.g. just 'param1&...' instead of
            // 'param1=...&...')
            if (originalString.contains(nameAndSeparator)) {
                return originalString.replace(
                        nameAndSeparator + value, nameAndSeparator + replaceString);
            }
            return originalString.replace(name, nameAndSeparator + replaceString);
        }
    }

    @Override
    public boolean isTypeForMethod(AuthenticationMethod method) {
        return method instanceof FormBasedAuthenticationMethod;
    }

    @Override
    public FormBasedAuthenticationMethod createAuthenticationMethod(int contextId) {
        return new FormBasedAuthenticationMethod();
    }

    @Override
    public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
        return new FormBasedAuthenticationMethodOptionsPanel(uiSharedContext);
    }
}
