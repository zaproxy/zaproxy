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
package org.zaproxy.zap.utils;

import java.util.Locale;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.model.Context;

/** Utils for manipulating API calls and parameters. */
public final class ApiUtils {

    /**
     * Gets the int param with a given name and throws an exception accordingly if not found or
     * valid.
     *
     * @param params the params
     * @param paramName the param name
     * @return the int param
     * @throws ApiException the api exception
     */
    public static int getIntParam(JSONObject params, String paramName) throws ApiException {
        if (!params.containsKey(paramName)) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, paramName);
        }

        try {
            return params.getInt(paramName);
        } catch (JSONException e) {
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, paramName, e);
        }
    }

    /**
     * Gets a boolean from the parameter with the given name.
     *
     * @param params the API parameters
     * @param paramName the name of the parameter
     * @return the boolean value
     * @throws ApiException if the parameter is missing ({@link
     *     org.zaproxy.zap.extension.api.ApiException.Type#MISSING_PARAMETER MISSING_PARAMETER}) or
     *     not a boolean ({@link org.zaproxy.zap.extension.api.ApiException.Type#ILLEGAL_PARAMETER
     *     ILLEGAL_PARAMETER}).
     * @since 2.8.0
     */
    public static boolean getBooleanParam(JSONObject params, String paramName) throws ApiException {
        if (!params.containsKey(paramName)) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, paramName);
        }

        try {
            return params.getBoolean(paramName);
        } catch (JSONException e) {
            throw new ApiException(ApiException.Type.ILLEGAL_PARAMETER, paramName, e);
        }
    }

    /**
     * Gets an optional string param, returning null if the parameter was not found.
     *
     * @param params the params
     * @param paramName the param name
     * @return the optional string param
     */
    public static String getOptionalStringParam(JSONObject params, String paramName) {
        if (params.containsKey(paramName)) {
            return params.getString(paramName);
        }
        return null;
    }

    /**
     * Gets the non empty string param with a given name and throws an exception accordingly if not
     * found or empty.
     *
     * @param params the params
     * @param paramName the param name
     * @return the non empty string param
     * @throws ApiException the api exception thown if param not found or string empty
     */
    public static String getNonEmptyStringParam(JSONObject params, String paramName)
            throws ApiException {
        if (!params.containsKey(paramName)) {
            throw new ApiException(Type.MISSING_PARAMETER, paramName);
        }
        String value = params.getString(paramName);
        if (value == null || value.isEmpty()) {
            throw new ApiException(Type.MISSING_PARAMETER, paramName);
        }
        return value;
    }

    /**
     * Gets an optional enum param, returning {@code null} if the parameter was not found.
     *
     * @param <E> the type of the enum that will be returned
     * @param params the params
     * @param paramName the param name
     * @param enumType the type of the enum
     * @return the enum, or {@code null}
     * @throws ApiException if the param value does not match any of the possible enum values
     */
    public static <E extends Enum<E>> E getOptionalEnumParam(
            JSONObject params, String paramName, Class<E> enumType) throws ApiException {
        String enumValS = params.optString(paramName, null);
        E enumVal = null;
        if (enumValS != null && !enumValS.isEmpty()) {
            try {
                enumVal = Enum.valueOf(enumType, enumValS);
            } catch (Exception ex) {
                throw new ApiException(
                        ApiException.Type.ILLEGAL_PARAMETER,
                        paramName + ": " + ex.getLocalizedMessage());
            }
        }
        return enumVal;
    }

    /**
     * Gets the {@link Context} whose id is provided as a parameter with the given name. Throws an
     * exception accordingly if not found or valid.
     *
     * @param params the params
     * @param contextIdParamName the context id param name
     * @return the context
     * @throws ApiException the api exception
     */
    public static Context getContextByParamId(JSONObject params, String contextIdParamName)
            throws ApiException {
        int contextId = getIntParam(params, contextIdParamName);
        Context context = Model.getSingleton().getSession().getContext(contextId);
        if (context == null) {
            throw new ApiException(Type.CONTEXT_NOT_FOUND, contextIdParamName);
        }
        return context;
    }

    /**
     * Returns the {@code Context} with the given name. The context's name is obtained from the
     * given {@code parameters}, whose name is the value of {@code parameterName}.
     *
     * <p>The parameter must exist, that is, it should be a mandatory parameter, otherwise a runtime
     * exception is thrown.
     *
     * @param parameters the parameters that contain the context's name
     * @param parameterName the name of the parameter used to obtain the context's name
     * @return the {@code Context} with the given name
     * @throws ApiException If the context with the given name does not exist
     * @since 2.4.3
     * @see #getContextByName(String)
     * @see JSONObject#getString(String)
     */
    public static Context getContextByName(JSONObject parameters, String parameterName)
            throws ApiException {
        return getContextByName(parameters.getString(parameterName));
    }

    /**
     * Returns the {@code Context} with the given name.
     *
     * @param contextName the name of the context
     * @return the {@code Context} with the given name
     * @throws ApiException If the context with the given name does not exist
     * @since 2.4.3
     * @see #getContextByName(JSONObject, String)
     */
    public static Context getContextByName(String contextName) throws ApiException {
        Context context = Model.getSingleton().getSession().getContext(contextName);
        if (context == null) {
            throw new ApiException(ApiException.Type.DOES_NOT_EXIST, contextName);
        }
        return context;
    }

    /**
     * Returns the authority of the given {@code site} (i.e. host ":" port ).
     *
     * <p>For example, the result of returning the authority from:
     *
     * <blockquote>
     *
     * <pre>http://example.com:8080/some/path?a=b#c</pre>
     *
     * </blockquote>
     *
     * is:
     *
     * <blockquote>
     *
     * <pre>example.com:8080</pre>
     *
     * </blockquote>
     *
     * <p>If the provided site does not have a port, it's added the default of the used scheme.
     *
     * <p><strong>Note:</strong> The implementation is optimised to handle only HTTP and HTTPS
     * schemes, the behaviour is undefined for other schemes.
     *
     * @param site the site whose authority will be extracted
     * @return the authority of the site
     * @since 2.5.0
     */
    public static String getAuthority(String site) {
        String authority = site;
        boolean isSecure = false;
        // Remove http(s)://
        if (authority.toLowerCase(Locale.ROOT).startsWith("http://")) {
            authority = authority.substring(7);
        } else if (authority.toLowerCase(Locale.ROOT).startsWith("https://")) {
            authority = authority.substring(8);
            isSecure = true;
        }
        // Remove trailing chrs
        int idx = authority.indexOf('/');
        if (idx > 0) {
            authority = authority.substring(0, idx);
        }
        if (!authority.isEmpty() && authority.indexOf(':') == -1) {
            if (isSecure) {
                return authority + ":443";
            }
            return authority + ":80";
        }
        return authority;
    }

    private ApiUtils() {
        // Utility class
    }
}
