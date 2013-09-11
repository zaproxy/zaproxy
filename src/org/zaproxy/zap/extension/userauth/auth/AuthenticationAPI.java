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
package org.zaproxy.zap.extension.userauth.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiException.Type;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.userauth.authentication.AuthenticationMethodType;

/**
 * The API for manipulating {@link User Users}.
 */
public class AuthenticationAPI extends ApiImplementor {

	private static final Logger log = Logger.getLogger(AuthenticationAPI.class);

	private static final String PREFIX = "authentication";

	private static final String VIEW_GET_AUTHENTICATION = "getAuthenticationMethod";
	private static final String VIEW_GET_LOGGED_IN_INDICATOR = "getLoggedInIndicator";
	private static final String VIEW_GET_LOGGED_OUT_INDICATOR = "getLoggedOutIndicator";

	private static final String ACTION_SET_LOGGED_IN_INDICATOR = "setLoggedInIndicator";
	private static final String ACTION_SET_LOGGED_OUT_INDICATOR = "setLoggedOutIndicator";

	private static final String PARAM_CONTEXT_ID = "contextId";
	private static final String PARAM_LOGGED_IN_INDICATOR = "loggedInIndicatorRegex";
	private static final String PARAM_LOGGED_OUT_INDICATOR = "loggedOutIndicatorRegex";

	@SuppressWarnings("unused")
	private ExtensionAuthentication extension;
	private Map<String, AuthenticationMethodType> loadedAuthenticationMethodActions;

	public AuthenticationAPI(ExtensionAuthentication extension) {
		super();
		this.extension = extension;

		this.addApiView(new ApiView(VIEW_GET_AUTHENTICATION, new String[] { PARAM_CONTEXT_ID }));
		this.addApiView(new ApiView(VIEW_GET_LOGGED_IN_INDICATOR, new String[] { PARAM_CONTEXT_ID }));
		this.addApiView(new ApiView(VIEW_GET_LOGGED_OUT_INDICATOR, new String[] { PARAM_CONTEXT_ID }));

		this.loadedAuthenticationMethodActions = new HashMap<String, AuthenticationMethodType>();
		for (AuthenticationMethodType t : extension.getAuthenticationMethodTypes()) {
			ApiAction action = t.getSetMethodForContextApiAction();
			if (action != null) {
				loadedAuthenticationMethodActions.put(action.getName(), t);
				this.addApiAction(action);
			}
		}
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
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());

		switch (name) {
		case ACTION_SET_LOGGED_IN_INDICATOR:
			String loggedInIndicator = params.getString(PARAM_LOGGED_IN_INDICATOR);
			if (loggedInIndicator == null || loggedInIndicator.isEmpty())
				throw new ApiException(Type.MISSING_PARAMETER, PARAM_LOGGED_IN_INDICATOR);
			getContext(params).getAuthenticationMethod().setLoggedInIndicatorPattern(loggedInIndicator);
			return ApiResponseElement.OK;

		case ACTION_SET_LOGGED_OUT_INDICATOR:
			String loggedOutIndicator = params.getString(PARAM_LOGGED_OUT_INDICATOR);
			if (loggedOutIndicator == null || loggedOutIndicator.isEmpty())
				throw new ApiException(Type.MISSING_PARAMETER, PARAM_LOGGED_OUT_INDICATOR);
			getContext(params).getAuthenticationMethod().setLoggedOutIndicatorPattern(loggedOutIndicator);
			return ApiResponseElement.OK;

		default:
			if (!loadedAuthenticationMethodActions.containsKey(name))
				throw new ApiException(Type.BAD_ACTION);
			loadedAuthenticationMethodActions.get(name).handleSetMethodForContextApiAction(params);
			return ApiResponseElement.OK;
		}

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
