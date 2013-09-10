package org.zaproxy.zap.extension.userauth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.zaproxy.zap.extension.api.ApiResponseList;
import org.zaproxy.zap.extension.api.ApiResponseSet;
import org.zaproxy.zap.extension.api.ApiView;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.User;

public class UsersAPI extends ApiImplementor {

	private static final Logger log = Logger.getLogger(UsersAPI.class);

	private static final String PREFIX = "users";

	private static final String VIEW_USERS_LIST = "usersList";

	private static final String ACTION_NEW_USER = "newUser";
	private static final String ACTION_SET_ENABLED = "setUserEnabled";
	private static final String ACTION_SET_NAME = "setUserName";

	private static final String PARAM_CONTEXT_ID = "contextId";
	private static final String PARAM_USER_ID = "userId";
	private static final String PARAM_USER_NAME = "name";
	private static final String PARAM_ENABLED = "enabled";

	private ExtensionUserManagement extension;

	public UsersAPI(ExtensionUserManagement extension) {
		super();
		this.extension = extension;

		this.addApiAction(new ApiAction(ACTION_NEW_USER, new String[] { PARAM_CONTEXT_ID, PARAM_USER_NAME }));
		this.addApiAction(new ApiAction(ACTION_SET_ENABLED, new String[] { PARAM_CONTEXT_ID, PARAM_USER_ID,
				PARAM_ENABLED }));
		this.addApiAction(new ApiAction(ACTION_SET_NAME, new String[] { PARAM_CONTEXT_ID, PARAM_USER_ID,
				PARAM_USER_NAME }));

		this.addApiView(new ApiView(VIEW_USERS_LIST, null, new String[] { PARAM_CONTEXT_ID }));
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
				users = new ArrayList<User>();
				for (Context c : Model.getSingleton().getSession().getContexts())
					users.addAll(extension.getContextUserAuthManager(c.getIndex()).getUsers());
			}

			// Prepare the response
			for (User user : users)
				usersListResponse.addItem(buildResponseFromUser(user));
			return usersListResponse;

		default:
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());

		int contextId;
		int userId;
		User user;
		Context context;
		switch (name) {
		case ACTION_NEW_USER:
			contextId = getContextId(params);
			context = Model.getSingleton().getSession().getContext(contextId);
			if (context == null)
				throw new ApiException(Type.CONTEXT_NOT_FOUND, PARAM_CONTEXT_ID);
			String userName = params.getString(PARAM_USER_NAME);
			if (userName == null || userName.isEmpty()) {
				throw new ApiException(Type.MISSING_PARAMETER, PARAM_USER_NAME);
			}
			user = new User(contextId, userName);
			// TODO: Handle Authentication Credentials
			user.setAuthenticationCredentials(context.getAuthenticationMethod()
					.createAuthenticationCredentials());
			extension.getContextUserAuthManager(contextId).addUser(user);
			return ApiResponseElement.OK;
		case ACTION_SET_ENABLED:
			contextId = getContextId(params);
			userId = getUserId(params);
			boolean enabled = false;
			try {
				enabled = params.getBoolean(PARAM_ENABLED);
			} catch (JSONException e) {
				throw new ApiException(Type.BAD_FORMAT, PARAM_ENABLED + " - should be boolean");
			}
			user = extension.getContextUserAuthManager(contextId).getUserById(userId);
			if (user == null)
				throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
			user.setEnabled(enabled);
			return ApiResponseElement.OK;
		case ACTION_SET_NAME:
			contextId = getContextId(params);
			userId = getUserId(params);
			String nameSN = params.getString(PARAM_USER_NAME);
			if (nameSN == null || nameSN.isEmpty())
				throw new ApiException(Type.MISSING_PARAMETER, PARAM_USER_NAME);
			user = extension.getContextUserAuthManager(contextId).getUserById(userId);
			if (user == null)
				throw new ApiException(Type.USER_NOT_FOUND, PARAM_USER_ID);
			user.setName(nameSN);
			return ApiResponseElement.OK;
		default:
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}

	}

	private ApiResponse buildResponseFromUser(User u) {
		Map<String, String> fields = new HashMap<>();
		fields.put("name", u.getName());
		fields.put("id", Integer.toString(u.getId()));
		fields.put("contextId", Integer.toString(u.getContextId()));
		fields.put("enabled", Boolean.toString(u.isEnabled()));
		ApiResponseSet response = new ApiResponseSet("user", fields);
		return response;
	}

	private int getUserId(JSONObject params) throws ApiException {
		try {
			return params.getInt(PARAM_USER_ID);
		} catch (JSONException ex) {
			throw new ApiException(ApiException.Type.MISSING_PARAMETER, PARAM_CONTEXT_ID);
		}
	}

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
