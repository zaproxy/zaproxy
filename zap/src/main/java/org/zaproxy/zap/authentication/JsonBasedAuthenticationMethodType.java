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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.text.StringEscapeUtils;
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
 * An {@link AuthenticationMethodType} where the Users are authenticated by posting a JSON object
 * with its username and password.
 *
 * @since 2.8.0
 */
public class JsonBasedAuthenticationMethodType extends PostBasedAuthenticationMethodType {

    private static final Logger LOGGER =
            LogManager.getLogger(JsonBasedAuthenticationMethodType.class);

    private static final int METHOD_IDENTIFIER = 5;

    private static final String METHOD_NAME =
            Constant.messages.getString("authentication.method.jb.name");

    private static final String API_METHOD_NAME = "jsonBasedAuthentication";

    public JsonBasedAuthenticationMethodType() {
        super(
                METHOD_NAME,
                METHOD_IDENTIFIER,
                API_METHOD_NAME,
                "authentication.method.jb.popup.login.request",
                true);
    }

    /**
     * An {@link AuthenticationMethod} where the Users are authenticated by posting a JSON object
     * ({@code application/json}) with its username and password.
     */
    public class JsonBasedAuthenticationMethod extends PostBasedAuthenticationMethod {

        /** Constructs a {@code JsonBasedAuthenticationMethod}. */
        public JsonBasedAuthenticationMethod() {
            this(null);
        }

        /**
         * Constructs a {@code JsonBasedAuthenticationMethod} based on the given method.
         *
         * @param jsonBasedAuthenticationMethod the method to copy.
         */
        private JsonBasedAuthenticationMethod(
                JsonBasedAuthenticationMethod jsonBasedAuthenticationMethod) {
            super(
                    HttpHeader.JSON_CONTENT_TYPE,
                    StringEscapeUtils::escapeJson,
                    jsonBasedAuthenticationMethod);
        }

        @Override
        public AuthenticationMethodType getType() {
            return new JsonBasedAuthenticationMethodType();
        }

        @Override
        protected AuthenticationMethod duplicate() {
            return new JsonBasedAuthenticationMethod(this);
        }

        @Override
        public void replaceUserDataInPollRequest(HttpMessage msg, User user) {
            PostBasedAuthenticationMethodType.replaceUserCredentialsDataInPollRequest(
                    msg, user, NULL_ENCODER);
        }
    }

    /** The options panel to configure a {@link JsonBasedAuthenticationMethod}. */
    private class JsonBasedAuthenticationMethodOptionsPanel
            extends PostBasedAuthenticationMethodOptionsPanel {

        private static final long serialVersionUID = 1L;
        private JSONObject jsonObject;

        /**
         * Constructs a {@code JsonBasedAuthenticationMethodOptionsPanel} for the given context.
         *
         * @param context the context to be configured.
         */
        public JsonBasedAuthenticationMethodOptionsPanel(Context context) {
            super(context, StringEscapeUtils::unescapeJson);
        }

        @Override
        protected List<NameValuePair> extractParameters(String postData) {
            if (postData.isEmpty()) {
                jsonObject = null;
                return Collections.emptyList();
            }

            List<NameValuePair> params = new ArrayList<>();
            try {
                jsonObject = JSONObject.fromObject(postData);
                extractJsonStrings(jsonObject, "", params);
            } catch (JSONException e) {
                LOGGER.debug("Unable to parse as JSON: {}", postData, e);
                jsonObject = null;
                return Collections.emptyList();
            }
            return params;
        }

        private void extractJsonStrings(
                JSONObject jsonObject, String parent, List<NameValuePair> params) {
            for (Object key : jsonObject.keySet()) {
                Object obj = jsonObject.get(key);
                if (obj instanceof JSONObject) {
                    extractJsonStrings(
                            (JSONObject) obj, normalisedKey(parent, (String) key), params);
                } else if (obj instanceof String) {
                    params.add(
                            new DefaultNameValuePair(
                                    normalisedKey(parent, (String) key), (String) obj));
                }
            }
        }

        private String normalisedKey(String parent, String key) {
            return parent.isEmpty() ? key : parent + "." + key;
        }

        @Override
        protected String replaceParameterValue(
                String originalString, NameValuePair parameter, String replaceString) {
            if (jsonObject == null) {
                return originalString;
            }

            setValue(jsonObject, parameter.getName().split("\\."), 0, replaceString);
            return jsonObject.toString();
        }
    }

    private static void setValue(JSONObject jsonObject, String[] keys, int idx, String value) {
        Object obj = jsonObject.get(keys[idx]);
        if (obj instanceof JSONObject) {
            setValue((JSONObject) obj, keys, idx + 1, value);
        } else if (obj instanceof String) {
            jsonObject.put(keys[idx], value);
        }
    }

    @Override
    public boolean isTypeForMethod(AuthenticationMethod method) {
        return method instanceof JsonBasedAuthenticationMethod;
    }

    @Override
    public JsonBasedAuthenticationMethod createAuthenticationMethod(int contextId) {
        return new JsonBasedAuthenticationMethod();
    }

    @Override
    public AbstractAuthenticationMethodOptionsPanel buildOptionsPanel(Context uiSharedContext) {
        return new JsonBasedAuthenticationMethodOptionsPanel(uiSharedContext);
    }
}
