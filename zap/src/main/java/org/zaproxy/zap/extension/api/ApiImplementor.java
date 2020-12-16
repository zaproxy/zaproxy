/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.api;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.network.HttpInputStream;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpOutputStream;
import org.zaproxy.zap.extension.api.API.RequestType;

public abstract class ApiImplementor {

    private static final String GET_OPTION_PREFIX = "option";
    private static final String SET_OPTION_PREFIX = "setOption";
    private static final String ADD_OPTION_PREFIX = "addOption";
    private static final String REMOVE_OPTION_PREFIX = "removeOption";

    private static final Comparator<Method> METHOD_NAME_COMPARATOR;

    static {
        METHOD_NAME_COMPARATOR =
                new Comparator<Method>() {

                    @Override
                    public int compare(Method method, Method otherMethod) {
                        if (method == null) {
                            if (otherMethod == null) {
                                return 0;
                            }
                            return -1;
                        } else if (otherMethod == null) {
                            return 1;
                        }

                        return method.getName().compareTo(otherMethod.getName());
                    }
                };
    }

    private List<ApiAction> apiActions = new ArrayList<>();
    private List<ApiView> apiViews = new ArrayList<>();
    private List<ApiOther> apiOthers = new ArrayList<>();
    private List<String> apiShortcuts = new ArrayList<>();
    private List<ApiPersistentConnection> apiPersistentConnections = new ArrayList<>();
    private AbstractParam param = null;

    public List<ApiView> getApiViews() {
        return this.apiViews;
    }

    public List<ApiAction> getApiActions() {
        return this.apiActions;
    }

    public List<ApiOther> getApiOthers() {
        return this.apiOthers;
    }

    public void addApiView(ApiView view) {
        addApiElement(apiViews, view, RequestType.view);
    }

    private <T extends ApiElement> void addApiElement(
            List<T> elements, T element, RequestType type) {
        validateApiElement(elements, element);
        String descKey = element.getDescriptionTag();
        if (StringUtils.isEmpty(descKey)) {
            descKey = getI18nPrefix() + ".api." + type.name() + "." + element.getName();
            element.setDescriptionTag(descKey);
        }

        String descParamKeyPrefix = descKey + ".param.";
        for (ApiParameter parameter : element.getParameters()) {
            if (parameter.getDescriptionKey().isEmpty()) {
                parameter.setDescriptionKey(descParamKeyPrefix + parameter.getName());
            }
        }

        elements.add(element);
    }

    /**
     * Validates that the name of the given {@code ApiElement} does not already exist in the given
     * list of {@code ApiElement}.
     *
     * <p>The names should be unique as they are used to select the {@code ApiElement}s.
     *
     * @param apiElements the existing API elements.
     * @param apiElement the API element to validate.
     * @throws IllegalArgumentException if the name already exists.
     */
    private static void validateApiElement(
            List<? extends ApiElement> apiElements, ApiElement apiElement) {
        String name = apiElement.getName();
        apiElements.stream()
                .filter(e -> name.equals(e.getName()))
                .findFirst()
                .ifPresent(
                        e -> {
                            throw new IllegalArgumentException(
                                    "An ApiElement with the given name already exists: " + name);
                        });
    }

    public void addApiOthers(ApiOther other) {
        addApiElement(apiOthers, other, RequestType.other);
    }

    public void addApiAction(ApiAction action) {
        addApiElement(apiActions, action, RequestType.action);
    }

    public void addApiShortcut(String shortcut) {
        this.apiShortcuts.add(shortcut);
    }

    public void addApiPersistentConnection(ApiPersistentConnection pconn) {
        addApiElement(apiPersistentConnections, pconn, RequestType.pconn);
    }

    /**
     * Adds the given options to the API implementor.
     *
     * @param param the options for the API
     * @see ZapApiIgnore
     */
    public void addApiOptions(AbstractParam param) {
        // Add option parameter getters and setters via reflection
        this.param = param;
        Method[] methods = param.getClass().getDeclaredMethods();
        Arrays.sort(methods, METHOD_NAME_COMPARATOR);
        List<String> addedActions = new ArrayList<>();
        // Check for string setters (which take precedence)
        for (Method method : methods) {
            if (isIgnored(method)) {
                continue;
            }

            boolean deprecated = method.getAnnotation(Deprecated.class) != null;

            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                ApiView view = new ApiView(GET_OPTION_PREFIX + method.getName().substring(3));
                setApiOptionDeprecated(view, deprecated);
                addApiView(view);
            }
            if (method.getName().startsWith("is") && method.getParameterTypes().length == 0) {
                ApiView view = new ApiView(GET_OPTION_PREFIX + method.getName().substring(2));
                setApiOptionDeprecated(view, deprecated);
                addApiView(view);
            }
            if (method.getName().startsWith("set")
                    && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals(String.class)) {
                ApiAction action =
                        new ApiAction(
                                SET_OPTION_PREFIX + method.getName().substring(3),
                                new String[] {"String"});
                setApiOptionDeprecated(action, deprecated);
                this.addApiAction(action);
                addedActions.add(method.getName());
            }
            if (method.getName().startsWith("add")
                    && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals(String.class)) {
                ApiAction action =
                        new ApiAction(
                                ADD_OPTION_PREFIX + method.getName().substring(3),
                                new String[] {"String"});
                setApiOptionDeprecated(action, deprecated);
                this.addApiAction(action);
                addedActions.add(method.getName());
            }
            if (method.getName().startsWith("remove")
                    && method.getParameterTypes().length == 1
                    && method.getParameterTypes()[0].equals(String.class)) {
                ApiAction action =
                        new ApiAction(
                                REMOVE_OPTION_PREFIX + method.getName().substring(6),
                                new String[] {"String"});
                setApiOptionDeprecated(action, deprecated);
                this.addApiAction(action);
                addedActions.add(method.getName());
            }
        }
        // Now check for non string setters
        for (Method method : methods) {
            if (isIgnored(method)) {
                continue;
            }

            boolean deprecated = method.getAnnotation(Deprecated.class) != null;

            if (method.getName().startsWith("set")
                    && method.getParameterTypes().length == 1
                    && !addedActions.contains(method.getName())) {
                // Non String setter
                if (method.getParameterTypes()[0].equals(Integer.class)
                        || method.getParameterTypes()[0].equals(int.class)) {
                    ApiAction action =
                            new ApiAction(
                                    SET_OPTION_PREFIX + method.getName().substring(3),
                                    new String[] {"Integer"});
                    setApiOptionDeprecated(action, deprecated);
                    this.addApiAction(action);
                    addedActions.add(method.getName()); // Just in case there are more overloads
                } else if (method.getParameterTypes()[0].equals(Boolean.class)
                        || method.getParameterTypes()[0].equals(boolean.class)) {
                    ApiAction action =
                            new ApiAction(
                                    SET_OPTION_PREFIX + method.getName().substring(3),
                                    new String[] {"Boolean"});
                    setApiOptionDeprecated(action, deprecated);
                    this.addApiAction(action);
                    addedActions.add(method.getName()); // Just in case there are more overloads
                }
            }
        }
    }

    /**
     * Tells whether or not the given {@code method} should be ignored, thus not included in the ZAP
     * API.
     *
     * <p>Checks if the given {@code method} has been annotated with {@code ZapApiIgnore} or if it's
     * not public, if any of the conditions is {@code true} the {@code method} is ignored.
     *
     * @param method the method that will be checked
     * @return {@code true} if the method should be ignored, {@code false} otherwise.
     * @see ZapApiIgnore
     */
    private static boolean isIgnored(Method method) {
        return method.getAnnotation(ZapApiIgnore.class) != null
                || !Modifier.isPublic(method.getModifiers());
    }

    private void setApiOptionDeprecated(ApiElement apiOption, boolean deprecated) {
        if (deprecated) {
            apiOption.setDeprecated(deprecated);
            if (Constant.messages != null) {
                // Add a custom message when running from ZAP.
                apiOption.setDeprecatedDescription(
                        Constant.messages.getString("api.deprecated.option.endpoint"));
            }
        }
    }

    public ApiResponse handleApiOptionView(String name, JSONObject params) throws ApiException {
        if (this.param == null) {
            return null;
        }
        if (name.startsWith(GET_OPTION_PREFIX)) {
            name = name.substring(GET_OPTION_PREFIX.length());
            Method[] methods = param.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (isIgnored(method)) {
                    continue;
                }

                if ((method.getName().equals("get" + name) || method.getName().equals("is" + name))
                        && method.getParameterTypes().length == 0) {
                    try {
                        return new ApiResponseElement(name, method.invoke(this.param).toString());
                    } catch (Exception e) {
                        throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
                    }
                }
            }
        }
        return null;
    }

    public ApiResponse handleApiOptionAction(String name, JSONObject params) throws ApiException {
        if (this.param == null) {
            return null;
        }
        boolean isApiOption = false;

        if (name.startsWith(SET_OPTION_PREFIX)) {
            name = "set" + name.substring(SET_OPTION_PREFIX.length());
            isApiOption = true;
        } else if (name.startsWith(ADD_OPTION_PREFIX)) {
            name = "add" + name.substring(ADD_OPTION_PREFIX.length());
            isApiOption = true;
        } else if (name.startsWith(REMOVE_OPTION_PREFIX)) {
            name = "remove" + name.substring(REMOVE_OPTION_PREFIX.length());
            isApiOption = true;
        }

        if (isApiOption) {
            try {
                Method[] methods = param.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    if (isIgnored(method)) {
                        continue;
                    }

                    if (method.getName().equals(name) && method.getParameterTypes().length == 1) {
                        Object val = null;
                        if (method.getParameterTypes()[0].equals(String.class)) {
                            val = params.getString("String");
                        } else if (method.getParameterTypes()[0].equals(Integer.class)
                                || method.getParameterTypes()[0].equals(int.class)) {
                            try {
                                val = params.getInt("Integer");
                            } catch (JSONException e) {
                                throw new ApiException(
                                        ApiException.Type.ILLEGAL_PARAMETER, "Integer");
                            }
                        } else if (method.getParameterTypes()[0].equals(Boolean.class)
                                || method.getParameterTypes()[0].equals(boolean.class)) {
                            try {
                                val = params.getBoolean("Boolean");
                            } catch (JSONException e) {
                                throw new ApiException(
                                        ApiException.Type.ILLEGAL_PARAMETER, "Boolean");
                            }
                        }
                        if (val == null) {
                            // Value supplied doesn't match the type - try the next one
                            continue;
                        }
                        method.invoke(this.param, val);
                        return ApiResponseElement.OK;
                    }
                }
            } catch (ApiException e) {
                throw e;
            } catch (Exception e) {
                throw new ApiException(ApiException.Type.INTERNAL_ERROR, e.getMessage());
            }
        }
        return null;
    }

    /**
     * Override if implementing one or more views
     *
     * @param name the name of the requested view
     * @param params the API request parameters
     * @return the API response
     * @throws ApiException if an error occurred while handling the API view endpoint
     */
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        throw new ApiException(ApiException.Type.BAD_VIEW, name);
    }

    /**
     * Override if implementing one or more actions
     *
     * @param name the name of the requested action
     * @param params the API request parameters
     * @return the API response
     * @throws ApiException if an error occurred while handling the API action endpoint
     */
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        throw new ApiException(ApiException.Type.BAD_ACTION, name);
    }

    /**
     * Override if implementing one or more 'other' operations - these are operations that _dont_
     * return structured data
     *
     * @param msg the HTTP message containing the API request
     * @param name the name of the requested other endpoint
     * @param params the API request parameters
     * @return the HTTP message with the API response
     * @throws ApiException if an error occurred while handling the API other endpoint
     */
    public HttpMessage handleApiOther(HttpMessage msg, String name, JSONObject params)
            throws ApiException {
        throw new ApiException(ApiException.Type.BAD_OTHER, name);
    }

    /**
     * Override if implementing one or more 'persistent connection' operations. These are operations
     * that maintain long running connections, potentially staying alive as long as the client holds
     * them open.
     *
     * @param msg the HTTP message containing the API request
     * @param httpIn the input stream
     * @param httpOut the output stream
     * @param name the name of the requested pconn endpoint
     * @param params the API request parameters
     * @throws ApiException if an error occurred while handling the API pconn endpoint
     */
    public void handleApiPersistentConnection(
            HttpMessage msg,
            HttpInputStream httpIn,
            HttpOutputStream httpOut,
            String name,
            JSONObject params)
            throws ApiException {
        throw new ApiException(ApiException.Type.BAD_PCONN, name);
    }

    /**
     * Override if handling callbacks
     *
     * @param msg the HTTP message containing the API request and response
     * @return the API response (set in the HTTP response body)
     * @throws ApiException if an error occurred while handling the API callback
     */
    public String handleCallBack(HttpMessage msg) throws ApiException {
        throw new ApiException(
                ApiException.Type.URL_NOT_FOUND, msg.getRequestHeader().getURI().toString());
    }

    public HttpMessage handleShortcut(HttpMessage msg) throws ApiException {
        throw new ApiException(
                ApiException.Type.URL_NOT_FOUND, msg.getRequestHeader().getURI().toString());
    }

    public abstract String getPrefix();

    /**
     * Gets the prefix for default resource keys for the description of the API implementor and its
     * elements.
     *
     * <p>Defaults to {@code getPrefix()}.
     *
     * @return the prefix for i18n keys.
     * @since 2.10.0
     */
    protected String getI18nPrefix() {
        return getPrefix();
    }

    /**
     * Gets the resource key of the description.
     *
     * <p>Defaults to {@code getI18nPrefix() + ".api.desc"}.
     *
     * @return the key of the description.
     * @since 2.9.0
     * @see org.zaproxy.zap.utils.I18N#getString(String)
     */
    public String getDescriptionKey() {
        return getI18nPrefix() + ".api.desc";
    }

    public ApiAction getApiAction(String name) {
        for (ApiAction action : this.apiActions) {
            if (action.getName().equals(name)) {
                return action;
            }
        }
        return null;
    }

    public ApiView getApiView(String name) {
        for (ApiView view : this.apiViews) {
            if (view.getName().equals(name)) {
                return view;
            }
        }
        return null;
    }

    public ApiOther getApiOther(String name) {
        for (ApiOther other : this.apiOthers) {
            if (other.getName().equals(name)) {
                return other;
            }
        }
        return null;
    }

    protected List<String> getApiShortcuts() {
        return this.apiShortcuts;
    }

    public ApiPersistentConnection getApiPersistentConnection(String name) {
        for (ApiPersistentConnection pconn : this.apiPersistentConnections) {
            if (pconn.getName().equals(name)) {
                return pconn;
            }
        }
        return null;
    }

    public List<ApiPersistentConnection> getApiPersistentConnections() {
        return this.apiPersistentConnections;
    }

    protected int getParam(JSONObject params, String name, int defaultValue) {
        try {
            return params.getInt(name);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected long getParam(JSONObject params, String name, long defaultValue) {
        try {
            return params.getLong(name);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected String getParam(JSONObject params, String name, String defaultValue) {
        try {
            return params.getString(name);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    protected boolean getParam(JSONObject params, String name, boolean defaultValue) {
        try {
            return params.getBoolean(name);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Validates that a parameter with the given {@code name} exists (and it has a value) in the
     * given {@code parameters}.
     *
     * @param parameters the parameters
     * @param name the name of the parameter that must exist
     * @throws ApiException if the parameter with the given name does not exist or it has no value.
     * @since 2.6.0
     */
    protected void validateParamExists(JSONObject parameters, String name) throws ApiException {
        if (!parameters.has(name) || parameters.getString(name).length() == 0) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, name);
        }
    }

    /**
     * Override to add custom headers for specific API operations
     *
     * @param name the name of the operation
     * @param type the type of the operation
     * @param msg the HTTP response message to the API request
     */
    public void addCustomHeaders(String name, RequestType type, HttpMessage msg) {
        // Do nothing in the default implementation
    }

    boolean hasApiOptions() {
        return param != null;
    }
}
