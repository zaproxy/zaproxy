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
package org.zaproxy.zap.extension.sessions;

import java.util.HashMap;
import java.util.Map;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiDynamicActionImplementor;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.SessionManagementMethodType;
import org.zaproxy.zap.utils.ApiUtils;

/**
 * The API for manipulating {@link SessionManagementMethod SessionManagementMethods} for a {@link
 * Context}.
 */
public class SessionManagementAPI extends ApiImplementor {

    private static final Logger log = LogManager.getLogger(SessionManagementAPI.class);

    private static final String PREFIX = "sessionManagement";

    private static final String VIEW_GET_SESSION_MANAGEMENT_METHOD = "getSessionManagementMethod";
    private static final String VIEW_GET_METHOD_CONFIG_PARAMETERS =
            "getSessionManagementMethodConfigParams";
    private static final String VIEW_GET_SUPPORTED_METHODS = "getSupportedSessionManagementMethods";

    private static final String ACTION_SET_METHOD = "setSessionManagementMethod";

    public static final String PARAM_CONTEXT_ID = "contextId";
    private static final String PARAM_METHOD_NAME = "methodName";
    private static final String PARAM_METHOD_CONFIG_PARAMS = "methodConfigParams";

    @SuppressWarnings("unused")
    private ExtensionSessionManagement extension;

    private Map<String, ApiDynamicActionImplementor> loadedSessionManagementMethodActions;

    public SessionManagementAPI(ExtensionSessionManagement extension) {
        super();
        this.extension = extension;

        this.addApiView(new ApiView(VIEW_GET_SUPPORTED_METHODS));
        this.addApiView(
                new ApiView(VIEW_GET_METHOD_CONFIG_PARAMETERS, new String[] {PARAM_METHOD_NAME}));
        this.addApiView(
                new ApiView(VIEW_GET_SESSION_MANAGEMENT_METHOD, new String[] {PARAM_CONTEXT_ID}));

        this.addApiAction(
                new ApiAction(
                        ACTION_SET_METHOD,
                        new String[] {PARAM_CONTEXT_ID, PARAM_METHOD_NAME},
                        new String[] {PARAM_METHOD_CONFIG_PARAMS}));

        this.loadedSessionManagementMethodActions = new HashMap<>();
        // Load the session management method actions
        if (extension != null) {
            for (SessionManagementMethodType t : extension.getSessionManagementMethodTypes()) {
                ApiDynamicActionImplementor i = t.getSetMethodForContextApiAction();
                if (i != null) {
                    loadedSessionManagementMethodActions.put(i.getName(), i);
                }
            }
        }
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
        log.debug("handleApiView {} {}", name, params);

        switch (name) {
            case VIEW_GET_SESSION_MANAGEMENT_METHOD:
                return getContext(params)
                        .getSessionManagementMethod()
                        .getApiResponseRepresentation();
            case VIEW_GET_SUPPORTED_METHODS:
                ApiResponseList supportedMethods = new ApiResponseList("supportedMethods");
                for (ApiDynamicActionImplementor a : loadedSessionManagementMethodActions.values())
                    supportedMethods.addItem(new ApiResponseElement("methodName", a.getName()));
                return supportedMethods;
            case VIEW_GET_METHOD_CONFIG_PARAMETERS:
                ApiDynamicActionImplementor a = getSetMethodActionImplementor(params);
                return a.buildParamsDescription();
            default:
                throw new ApiException(ApiException.Type.BAD_VIEW);
        }
    }

    @Override
    public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
        log.debug("handleApiAction {} {} ", name, params);

        switch (name) {
            case ACTION_SET_METHOD:
                // Prepare the params
                JSONObject actionParams;
                if (params.has(PARAM_METHOD_CONFIG_PARAMS))
                    actionParams = API.getParams(params.getString(PARAM_METHOD_CONFIG_PARAMS));
                else actionParams = new JSONObject();
                Context context = getContext(params);
                actionParams.put(PARAM_CONTEXT_ID, context.getId());
                // Run the method
                getSetMethodActionImplementor(params).handleAction(actionParams);
                context.save();
                return ApiResponseElement.OK;
            default:
                throw new ApiException(Type.BAD_ACTION);
        }
    }

    /**
     * Gets the sets the method action implementor or throws a Missing Parameter exception, if any
     * problems occurred.
     *
     * @param params the params
     * @return the sets the method action implementor
     * @throws ApiException the api exception
     */
    private ApiDynamicActionImplementor getSetMethodActionImplementor(JSONObject params)
            throws ApiException {
        ApiDynamicActionImplementor a =
                loadedSessionManagementMethodActions.get(
                        ApiUtils.getNonEmptyStringParam(params, PARAM_METHOD_NAME));
        if (a == null)
            throw new ApiException(
                    Type.DOES_NOT_EXIST,
                    "No session management method type matches the provided value.");
        return a;
    }

    /**
     * Gets the context from the parameters or throws a Missing Parameter exception, if any problems
     * occurred.
     *
     * @param params the params
     * @return the context
     * @throws ApiException the api exception
     */
    private Context getContext(JSONObject params) throws ApiException {
        int contextId = getContextId(params);
        Context context = Model.getSingleton().getSession().getContext(contextId);
        if (context == null) throw new ApiException(Type.CONTEXT_NOT_FOUND, PARAM_CONTEXT_ID);
        return context;
    }

    /**
     * Gets the context id from the parameters or throws a Missing Parameter exception, if any
     * problems occurred.
     *
     * @param params the params
     * @return the context id
     * @throws ApiException the api exception
     */
    private int getContextId(JSONObject params) throws ApiException {
        try {
            return params.getInt(PARAM_CONTEXT_ID);
        } catch (JSONException ex) {
            throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_CONTEXT_ID);
        }
    }

    @SuppressWarnings("unused")
    private boolean hasContextId(JSONObject params) {
        try {
            params.getInt(PARAM_CONTEXT_ID);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
}
