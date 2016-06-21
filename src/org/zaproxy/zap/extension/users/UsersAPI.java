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
package org.zaproxy.zap.extension.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
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
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ApiUtils;

/**
 * The API for manipulating {@link User Users}.
 */
public class UsersAPI extends ApiImplementor {

	private static final Logger log = Logger.getLogger(UsersAPI.class);

	private static final String PREFIX = "users";

	private static final String VIEW_USERS_LIST = "usersList";
	private static final String VIEW_GET_USER_BY_ID = "getUserById";
	private static final String VIEW_GET_AUTH_CREDENTIALS = "getAuthenticationCredentials";
	private static final String VIEW_GET_AUTH_CREDENTIALS_CONFIG_PARAMETERS = "getAuthenticationCredentialsConfigParams";

	private static final String ACTION_NEW_USER = "newUser";
	private static final String ACTION_REMOVE_USER = "removeUser";
	private static final String ACTION_SET_ENABLED = "setUserEnabled";
	private static final String ACTION_SET_NAME = "setUserName";
	private static final String ACTION_SET_AUTH_CREDENTIALS = "setAuthenticationCredentials";

	public static final String PARAM_CONTEXT_ID = "contextId";
	public static final String PARAM_USER_ID = "userId";
	private static final String PARAM_USER_NAME = "name";
	private static final String PARAM_ENABLED = "enabled";
	private static final String PARAM_CREDENTIALS_CONFIG_PARAMS = "authCredentialsConfigParams";

	private ExtensionUserManagement extension;
	private Map<Integer, ApiDynamicActionImplementor> loadedAuthenticationMethodActions;

	public UsersAPI(ExtensionUserManagement extension) {
		super();
		this.extension = extension;

		this.addApiView(new ApiView(VIEW_USERS_LIST, null, new String[] { PARAM_CONTEXT_ID }));
		this.addApiView(new ApiView(VIEW_GET_USER_BY_ID, null,
				new String[] { PARAM_CONTEXT_ID, PARAM_USER_ID }));
		this.addApiView(new ApiView(VIEW_GET_AUTH_CREDENTIALS_CONFIG_PARAMETERS,
				new String[] { PARAM_CONTEXT_ID }));
		this.addApiView(new ApiView(VIEW_GET_AUTH_CREDENTIALS,
				new String[] { PARAM_CONTEXT_ID, PARAM_USER_ID }));

		this.addApiAction(new ApiAction(ACTION_NEW_USER, new String[] { PARAM_CONTEXT_ID, PARAM_USER_NAME }));
		this.addApiAction(new ApiAction(ACTION_REMOVE_USER, new String[] { PARAM_CONTEXT_ID, PARAM_USER_ID }));
		this.addApiAction(new ApiAction(ACTION_SET_ENABLED, new String[] { PARAM_CONTEXT_ID, PARAM_USER_ID,
				PARAM_ENABLED }));
		this.addApiAction(new ApiAction(ACTION_SET_NAME, new String[] { PARAM_CONTEXT_ID, PARAM_USER_ID,
				PARAM_USER_NAME }));
		this.addApiAction(new ApiAction(ACTION_SET_AUTH_CREDENTIALS, new String[] { PARAM_CONTEXT_ID,
				PARAM_USER_ID }, new String[] { PARAM_CREDENTIALS_CONFIG_PARAMS }));

		// Load the authentication method actions
		if (Control.getSingleton() != null) {
			ExtensionAuthentication authenticationExtension = (ExtensionAuthentication) Control
					.getSingleton().getExtensionLoader().getExtension(ExtensionAuthentication.NAME);
			this.loadedAuthenticationMethodActions = new HashMap<>();
			if (authenticationExtension != null) {
				for (AuthenticationMethodType t : authenticationExtension.getAuthenticationMethodTypes()) {
					ApiDynamicActionImplementor i = t.getSetCredentialsForUserApiAction();
					if (i != null) {
						loadedAuthenticationMethodActions.put(t.getUniqueIdentifier(), i);
					}
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
		log.debug("handleApiView " + name + " " + params.toString());

		switch (name) {
		case VIEW_USERS_LIST:
			ApiResponseList usersListResponse = new ApiResponseList(name);
			// Get the users
			List<User> users;
			if (hasContextId(params))
				users = extension.getContextUserAuthManager(getContextId(params)).getUsers();
			else {
				users = new ArrayList<>();
				for (Context c : Model.getSingleton().getSession().getContexts())
					users.addAll(extension.getContextUserAuthManager(c.getIndex()).getUsers());
			}

			// Prepare the response
			for (User user : users)
				usersListResponse.addItem(buildResponseFromUser(user));
			return usersListResponse;

		case VIEW_GET_USER_BY_ID:
			return buildResponseFromUser(getUser(params));

		case VIEW_GET_AUTH_CREDENTIALS:
			return getUser(params).getAuthenticationCredentials().getApiResponseRepresentation();

		case VIEW_GET_AUTH_CREDENTIALS_CONFIG_PARAMETERS:
			AuthenticationMethodType type = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID)
					.getAuthenticationMethod().getType();
			ApiDynamicActionImplementor a = loadedAuthenticationMethodActions.get(type.getUniqueIdentifier());
			return a.buildParamsDescription();
		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());

		User user;
		Context context;
		switch (name) {
		case ACTION_NEW_USER:
			context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
			String userName = ApiUtils.getNonEmptyStringParam(params, PARAM_USER_NAME);
			user = new User(context.getIndex(), userName);
			user.setAuthenticationCredentials(context.getAuthenticationMethod()
					.createAuthenticationCredentials());
			extension.getContextUserAuthManager(context.getIndex()).addUser(user);
			context.save();
			return new ApiResponseElement(PARAM_USER_ID, String.valueOf(user.getId()));
		case ACTION_REMOVE_USER:
			context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
			int userId = ApiUtils.getIntParam(params, PARAM_USER_ID);
			boolean deleted = extension.getContextUserAuthManager(context.getIndex()).removeUserById(userId);
			if (deleted) {
				context.save();
				return ApiResponseElement.OK;
			} else
				return ApiResponseElement.FAIL;
		case ACTION_SET_ENABLED:
			boolean enabled = false;
			try {
				enabled = params.getBoolean(PARAM_ENABLED);
			} catch (JSONException e) {
				throw new ApiException(Type.ILLEGAL_PARAMETER, PARAM_ENABLED + " - should be boolean");
			}
			user = getUser(params);
			user.setEnabled(enabled);
			user.getContext().save();
			return ApiResponseElement.OK;
		case ACTION_SET_NAME:
			String nameSN = params.getString(PARAM_USER_NAME);
			if (nameSN == null || nameSN.isEmpty())
				throw new ApiException(Type.MISSING_PARAMETER, PARAM_USER_NAME);
			user = getUser(params);
			user.setName(nameSN);
			user.getContext().save();
			return ApiResponseElement.OK;
		case ACTION_SET_AUTH_CREDENTIALS:
			// Prepare the params
			JSONObject actionParams;
			if (params.has(PARAM_CREDENTIALS_CONFIG_PARAMS))
				actionParams = API.getParams(params.getString(PARAM_CREDENTIALS_CONFIG_PARAMS));
			else
				actionParams = new JSONObject();
			context = ApiUtils.getContextByParamId(params, PARAM_CONTEXT_ID);
			actionParams.put(PARAM_CONTEXT_ID, context.getIndex());
			actionParams.put(PARAM_USER_ID, getUserId(params));
			// Run the method
			ApiDynamicActionImplementor a = loadedAuthenticationMethodActions.get(context
					.getAuthenticationMethod().getType().getUniqueIdentifier());
			a.handleAction(actionParams);
			context.save();
			return ApiResponseElement.OK;

		default:
			throw new ApiException(Type.BAD_ACTION);
		}

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
		fields.put("credentials", u.getAuthenticationCredentials().getApiResponseRepresentation().toJSON()
				.toString());
		ApiResponseSet response = new ApiResponseSet("user", fields);
		return response;
	}

	/**
	 * Gets the user id from the parameters or throws a Missing Parameter exception, if any problems
	 * occurred.
	 * 
	 * @param params the params
	 * @return the user id
	 * @throws ApiException the api exception
	 */
	private int getUserId(JSONObject params) throws ApiException {
		return ApiUtils.getIntParam(params, PARAM_USER_ID);
	}

	/**
	 * Gets the user corresponding to the id provided in the parameters or throws an ApiException id
	 * any problems occurred.
	 * 
	 * @param params the params
	 * @return the user
	 * @throws ApiException the api exception
	 */
	private User getUser(JSONObject params) throws ApiException {
		int contextId = getContextId(params);
		int userId = getUserId(params);
		User user = extension.getContextUserAuthManager(contextId).getUserById(userId);
		if (user == null)
			throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
		return user;
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
		return ApiUtils.getIntParam(params, PARAM_CONTEXT_ID);
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
