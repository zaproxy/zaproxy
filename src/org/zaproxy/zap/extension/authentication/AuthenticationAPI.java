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
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
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
import org.zaproxy.zap.utils.ApiUtils;

/**
 * The API for manipulating the {@link org.zaproxy.zap.authentication.AuthenticationMethod AuthenticationMethod} for
 * {@link Context Contexts}.
 */
public class AuthenticationAPI extends ApiImplementor {

	private static final Logger log = Logger.getLogger(AuthenticationAPI.class);

	private static final String PREFIX = "authentication";

	private static final String VIEW_GET_AUTHENTICATION = "getAuthenticationMethod";
	private static final String VIEW_GET_LOGGED_IN_INDICATOR = "getLoggedInIndicator";
	private static final String VIEW_GET_LOGGED_OUT_INDICATOR = "getLoggedOutIndicator";
	private static final String VIEW_GET_METHOD_CONFIG_PARAMETERS = "getAuthenticationMethodConfigParams";
	private static final String VIEW_GET_SUPPORTED_METHODS = "getSupportedAuthenticationMethods";

	private static final String ACTION_SET_LOGGED_IN_INDICATOR = "setLoggedInIndicator";
	private static final String ACTION_SET_LOGGED_OUT_INDICATOR = "setLoggedOutIndicator";
	private static final String ACTION_SET_METHOD = "setAuthenticationMethod";

	public static final String PARAM_CONTEXT_ID = "contextId";
	private static final String PARAM_LOGGED_IN_INDICATOR = "loggedInIndicatorRegex";
	private static final String PARAM_LOGGED_OUT_INDICATOR = "loggedOutIndicatorRegex";
	private static final String PARAM_METHOD_NAME = "authMethodName";
	private static final String PARAM_METHOD_CONFIG_PARAMS = "authMethodConfigParams";

	private Map<String, ApiDynamicActionImplementor> loadedAuthenticationMethodActions;

	public AuthenticationAPI(ExtensionAuthentication extension) {
		super();

		this.addApiView(new ApiView(VIEW_GET_SUPPORTED_METHODS));
		this.addApiView(new ApiView(VIEW_GET_METHOD_CONFIG_PARAMETERS, new String[] { PARAM_METHOD_NAME }));
		this.addApiView(new ApiView(VIEW_GET_AUTHENTICATION, new String[] { PARAM_CONTEXT_ID }));
		this.addApiView(new ApiView(VIEW_GET_LOGGED_IN_INDICATOR, new String[] { PARAM_CONTEXT_ID }));
		this.addApiView(new ApiView(VIEW_GET_LOGGED_OUT_INDICATOR, new String[] { PARAM_CONTEXT_ID }));

		this.loadedAuthenticationMethodActions = new HashMap<>();
		// Load the authentication method actions
		if (extension != null) {
			for (AuthenticationMethodType t : extension.getAuthenticationMethodTypes()) {
				ApiDynamicActionImplementor i = t.getSetMethodForContextApiAction();
				if (i != null) {
					loadedAuthenticationMethodActions.put(i.getName(), i);
				}
			}
		}

		this.addApiAction(new ApiAction(ACTION_SET_METHOD,
				new String[] { PARAM_CONTEXT_ID, PARAM_METHOD_NAME },
				new String[] { PARAM_METHOD_CONFIG_PARAMS }));
		this.addApiAction(new ApiAction(ACTION_SET_LOGGED_IN_INDICATOR, new String[] { PARAM_CONTEXT_ID,
				PARAM_LOGGED_IN_INDICATOR }));
		this.addApiAction(new ApiAction(ACTION_SET_LOGGED_OUT_INDICATOR, new String[] { PARAM_CONTEXT_ID,
				PARAM_LOGGED_OUT_INDICATOR }));
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params) throws ApiException {
		log.debug("handleApiView " + name + " " + params.toString());

		switch (name) {
		case VIEW_GET_AUTHENTICATION:
			return getContext(params).getAuthenticationMethod().getApiResponseRepresentation();
		case VIEW_GET_LOGGED_IN_INDICATOR:
			Pattern loggedInPattern = getContext(params).getAuthenticationMethod()
					.getLoggedInIndicatorPattern();
			if (loggedInPattern != null)
				return new ApiResponseElement("logged_in_regex", loggedInPattern.toString());
			else
				return new ApiResponseElement("logged_in_regex", "");
		case VIEW_GET_LOGGED_OUT_INDICATOR:
			Pattern loggedOutPattern = getContext(params).getAuthenticationMethod()
					.getLoggedOutIndicatorPattern();
			if (loggedOutPattern != null)
				return new ApiResponseElement("logged_out_regex", loggedOutPattern.toString());
			else
				return new ApiResponseElement("logged_out_regex", "");
		case VIEW_GET_SUPPORTED_METHODS:
			ApiResponseList supportedMethods = new ApiResponseList("supportedMethods");
			for (ApiDynamicActionImplementor a : loadedAuthenticationMethodActions.values())
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
		log.debug("handleApiAction " + name + " " + params.toString());

		Context context;
		switch (name) {
		case ACTION_SET_LOGGED_IN_INDICATOR:
			String loggedInIndicator = params.getString(PARAM_LOGGED_IN_INDICATOR);
			if (loggedInIndicator == null || loggedInIndicator.isEmpty())
				throw new ApiException(Type.MISSING_PARAMETER, PARAM_LOGGED_IN_INDICATOR);
			context = getContext(params);
			context.getAuthenticationMethod().setLoggedInIndicatorPattern(loggedInIndicator);
			context.save();
			return ApiResponseElement.OK;

		case ACTION_SET_LOGGED_OUT_INDICATOR:
			String loggedOutIndicator = params.getString(PARAM_LOGGED_OUT_INDICATOR);
			if (loggedOutIndicator == null || loggedOutIndicator.isEmpty())
				throw new ApiException(Type.MISSING_PARAMETER, PARAM_LOGGED_OUT_INDICATOR);
			context = getContext(params);
			context.getAuthenticationMethod().setLoggedOutIndicatorPattern(loggedOutIndicator);
			context.save();
			return ApiResponseElement.OK;

		case ACTION_SET_METHOD:
			// Prepare the params
			JSONObject actionParams;
			if (params.has(PARAM_METHOD_CONFIG_PARAMS))
				actionParams = API.getParams(params.getString(PARAM_METHOD_CONFIG_PARAMS));
			else
				actionParams = new JSONObject();
			context = getContext(params);
			actionParams.put(PARAM_CONTEXT_ID, context.getIndex());
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
	 * problems occured.
	 * 
	 * @param params the params
	 * @return the sets the method action implementor
	 * @throws ApiException the api exception
	 */
	private ApiDynamicActionImplementor getSetMethodActionImplementor(JSONObject params) throws ApiException {
		ApiDynamicActionImplementor a = loadedAuthenticationMethodActions.get(ApiUtils
				.getNonEmptyStringParam(params, PARAM_METHOD_NAME));
		if (a == null)
			throw new ApiException(Type.DOES_NOT_EXIST,
					"No authentication method type matches the provided value.");
		return a;
	}

	/**
	 * Gets the context from the parameters or throws a Missing Parameter exception, if any problems
	 * occured.
	 * 
	 * @param params the params
	 * @return the context
	 * @throws ApiException the api exception
	 */
	private Context getContext(JSONObject params) throws ApiException {
		// NOTE: Still use this method as maybe we'll switch to using context names instead of id
		int contextId = getContextId(params);
		Context context = Model.getSingleton().getSession().getContext(contextId);
		if (context == null)
			throw new ApiException(Type.CONTEXT_NOT_FOUND, PARAM_CONTEXT_ID);
		return context;
	}

	/**
	 * Gets the context id from the parameters or throws a Missing Parameter exception, if any
	 * problems occured.
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
