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
import org.zaproxy.zap.extension.api.ApiResponseSet;
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

	private static final String PARAM_CONTEXT_ID = "contextId";

	private ExtensionAuthentication extension;
	private Map<String, AuthenticationMethodType> loadedAuthenticationMethodActions;

	public AuthenticationAPI(ExtensionAuthentication extension) {
		super();
		this.extension = extension;

		this.addApiView(new ApiView(VIEW_GET_AUTHENTICATION, null, new String[] { PARAM_CONTEXT_ID }));

		this.loadedAuthenticationMethodActions = new HashMap<String, AuthenticationMethodType>();
		for (AuthenticationMethodType t : extension.getAuthenticationMethodTypes()) {
			ApiAction action = t.getSetMethodForContextApiAction();
			if (action != null) {
				loadedAuthenticationMethodActions.put(action.getName(), t);
				this.addApiAction(action);
			}
		}

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
			int contextId = getContextId(params);
			Context context = Model.getSingleton().getSession().getContext(contextId);
			if (context == null)
				throw new ApiException(Type.CONTEXT_NOT_FOUND, PARAM_CONTEXT_ID);
			return context.getAuthenticationMethod().getApiResponseRepresentation();
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());

		if (!loadedAuthenticationMethodActions.containsKey(name))
			throw new ApiException(Type.BAD_ACTION);
		loadedAuthenticationMethodActions.get(name).handleSetMethodForContextApiAction(params);
		return ApiResponseElement.OK;
	}

	/**
	 * Builds the response describing an User.
	 * 
	 * @param u the user
	 * @return the api response
	 */
	private ApiResponse buildResponseFromUser(User u) {
		Map<String, String> fields = new HashMap<>();
		fields.put("name", u.getName());
		fields.put("id", Integer.toString(u.getId()));
		fields.put("contextId", Integer.toString(u.getContextId()));
		fields.put("enabled", Boolean.toString(u.isEnabled()));
		ApiResponseSet response = new ApiResponseSet("user", fields);
		return response;
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

	private boolean hasContextId(JSONObject params) {
		try {
			params.getInt(PARAM_CONTEXT_ID);
		} catch (JSONException ex) {
			return false;
		}
		return true;
	}

}
