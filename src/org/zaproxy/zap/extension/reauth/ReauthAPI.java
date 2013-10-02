/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.reauth;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiResponse;
import org.zaproxy.zap.extension.api.ApiResponseElement;
import org.zaproxy.zap.extension.api.ApiView;

public class ReauthAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(ReauthAPI.class);

	private static final String PREFIX = "auth";
	
	private static final String VIEW_LOGIN_URL = "loginUrl";
	private static final String VIEW_LOGIN_DATA = "loginData";
	private static final String VIEW_LOGGED_IN_INDICATOR = "loggedInIndicator";
	private static final String VIEW_LOGOUT_URL = "logoutUrl";
	private static final String VIEW_LOGOUT_DATA = "logoutData";
	private static final String VIEW_LOGGED_OUT_INDICATOR = "loggedOutIndicator";

	private static final String VIEW_PARAM_CONTEXT_ID = "contextId";

	private static final String ACTION_LOGIN = "login";
	private static final String ACTION_LOGOUT = "logout";
	private static final String ACTION_AUTO_REAUTH_ON = "autoReauthOn";
	private static final String ACTION_AUTO_REAUTH_OFF = "autoReauthOff";
	private static final String ACTION_SET_LOGIN_NODE = "setLoginUrl";
	private static final String ACTION_SET_LOGIN_INDICATOR = "setLoginIndicator";
	private static final String ACTION_SET_LOGOUT_NODE = "setLogoutUrl";
	private static final String ACTION_SET_LOGOUT_INDICATOR = "setLoggedOutIndicator";

	private static final String ACTION_PARAM_CONTEXT_ID = "contextId";
	private static final String ACTION_PARAM_URL = "url";
	private static final String ACTION_PARAM_DATA = "postData";
	private static final String ACTION_PARAM_INDICATOR = "indicator";
	
	private ExtensionReauth extension;
	
	public ReauthAPI (ExtensionReauth extension) {
		this.extension = extension;
		
		this.addApiView(new ApiView(VIEW_LOGIN_URL, 
				new String[] {VIEW_PARAM_CONTEXT_ID}));
		this.addApiView(new ApiView(VIEW_LOGIN_DATA, 
				new String[] {VIEW_PARAM_CONTEXT_ID}));
		this.addApiView(new ApiView(VIEW_LOGGED_IN_INDICATOR, 
				new String[] {VIEW_PARAM_CONTEXT_ID}));
		
		this.addApiView(new ApiView(VIEW_LOGOUT_URL, 
				new String[] {VIEW_PARAM_CONTEXT_ID}));
		this.addApiView(new ApiView(VIEW_LOGOUT_DATA, 
				new String[] {VIEW_PARAM_CONTEXT_ID}));
		this.addApiView(new ApiView(VIEW_LOGGED_OUT_INDICATOR, 
				new String[] {VIEW_PARAM_CONTEXT_ID}));
		
		this.addApiAction(new ApiAction(ACTION_LOGIN, 
				new String[] {ACTION_PARAM_CONTEXT_ID}));
		this.addApiAction(new ApiAction(ACTION_LOGOUT, 
				new String[] {ACTION_PARAM_CONTEXT_ID}));
		this.addApiAction(new ApiAction(ACTION_AUTO_REAUTH_ON));
		this.addApiAction(new ApiAction(ACTION_AUTO_REAUTH_OFF));
		this.addApiAction(new ApiAction(ACTION_SET_LOGIN_NODE, 
				new String[] {ACTION_PARAM_CONTEXT_ID, ACTION_PARAM_URL}, new String[] {ACTION_PARAM_DATA}));
		this.addApiAction(new ApiAction(ACTION_SET_LOGIN_INDICATOR, 
				new String[] {ACTION_PARAM_CONTEXT_ID, ACTION_PARAM_INDICATOR}));
		
		this.addApiAction(new ApiAction(ACTION_SET_LOGOUT_NODE, 
				new String[] {ACTION_PARAM_CONTEXT_ID, ACTION_PARAM_URL}, new String[] {ACTION_PARAM_DATA}));
		this.addApiAction(new ApiAction(ACTION_SET_LOGOUT_INDICATOR, 
				new String[] {ACTION_PARAM_CONTEXT_ID, ACTION_PARAM_INDICATOR}));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public ApiResponse handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		if (ACTION_LOGIN.equals(name)) {
			try {
				int contextId = params.getInt(ACTION_PARAM_CONTEXT_ID);
				if (! this.login(contextId)) {
					return ApiResponseElement.FAIL;
				}
			} catch (JSONException e) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_CONTEXT_ID);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else if (ACTION_LOGOUT.equals(name)) {
			try {
				int contextId = params.getInt(ACTION_PARAM_CONTEXT_ID);
				if (! this.logout(contextId)) {
					JSONArray result = new JSONArray();
					result.add("FAIL");
					return ApiResponseElement.FAIL;
				}
			} catch (JSONException e) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_CONTEXT_ID);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else if (ACTION_AUTO_REAUTH_ON.equals(name)) {
			this.setReauthenticate(true);
		} else if (ACTION_AUTO_REAUTH_OFF.equals(name)) {
			this.setReauthenticate(false);
		} else if (ACTION_SET_LOGIN_NODE.equals(name)) {
			String url = params.getString(ACTION_PARAM_URL);
			String postData = params.getString(ACTION_PARAM_DATA);
			if (url == null || url.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_URL);
			}
			try {
				int contextId = params.getInt(ACTION_PARAM_CONTEXT_ID);
				setLoginRequest(contextId, url, postData);
			} catch (JSONException e) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_CONTEXT_ID);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else if (ACTION_SET_LOGIN_INDICATOR.equals(name)) {
			String ind = params.getString(ACTION_PARAM_INDICATOR);
			if (ind == null || ind.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_INDICATOR);
			}
			try {
				int contextId = params.getInt(ACTION_PARAM_CONTEXT_ID);
				setLoggedInIndicationRegex(contextId, ind);
			} catch (JSONException e) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_CONTEXT_ID);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else if (ACTION_SET_LOGOUT_NODE.equals(name)) {
			String url = params.getString(ACTION_PARAM_URL);
			String postData = params.getString(ACTION_PARAM_DATA);
			if (url == null || url.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_URL);
			}
			try {
				int contextId = params.getInt(ACTION_PARAM_CONTEXT_ID);
				setLogoutRequest(contextId, url, postData);
			} catch (JSONException e) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_CONTEXT_ID);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else if (ACTION_SET_LOGOUT_INDICATOR.equals(name)) {
			String ind = params.getString(ACTION_PARAM_INDICATOR);
			if (ind == null || ind.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_INDICATOR);
			}
			try {
				int contextId = params.getInt(ACTION_PARAM_CONTEXT_ID);
				setLoggedOutIndicationRegex(contextId, ind);
			} catch (JSONException e) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_CONTEXT_ID);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new ApiException(ApiException.Type.INTERNAL_ERROR,
						e.getMessage());
			}
		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		return ApiResponseElement.OK;
	}

	@Override
	public ApiResponse handleApiView(String name, JSONObject params)
			throws ApiException {
		ApiResponseElement result = new ApiResponseElement(name, "");

		try {
			if (VIEW_LOGIN_URL.equals(name)) {
				int contextId = params.getInt(VIEW_PARAM_CONTEXT_ID);
				if (getLoginRequest(contextId) != null) {
					result = new ApiResponseElement(name, getLoginRequest(contextId).getRequestHeader().getURI().toString());
				}
			} else if (VIEW_LOGIN_DATA.equals(name)) {
				int contextId = params.getInt(VIEW_PARAM_CONTEXT_ID);
				if (getLoginRequest(contextId) != null) {
					result = new ApiResponseElement(name, getLoginRequest(contextId).getRequestBody().toString());
				}
			} else if (VIEW_LOGGED_IN_INDICATOR.equals(name)) {
				int contextId = params.getInt(VIEW_PARAM_CONTEXT_ID);
				result = new ApiResponseElement(name, getLoggedInIndicationRegex(contextId));
			} else if (VIEW_LOGOUT_URL.equals(name)) {
				int contextId = params.getInt(VIEW_PARAM_CONTEXT_ID);
				if (getLogoutRequest(contextId) != null) {
					result = new ApiResponseElement(name, getLogoutRequest(contextId).getRequestHeader().getURI().toString());
				}
			} else if (VIEW_LOGOUT_DATA.equals(name)) {
				int contextId = params.getInt(VIEW_PARAM_CONTEXT_ID);
				if (getLogoutRequest(contextId) != null) {
					result = new ApiResponseElement(name, getLogoutRequest(contextId).getRequestBody().toString());
				}
			} else if (VIEW_LOGGED_OUT_INDICATOR.equals(name)) {
				int contextId = params.getInt(VIEW_PARAM_CONTEXT_ID);
				result = new ApiResponseElement(name, getLoggedOutIndicationRegex(contextId));
			} else {
				throw new ApiException(ApiException.Type.BAD_VIEW);
			}
		} catch (JSONException e) {
			throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_CONTEXT_ID);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ApiException(ApiException.Type.INTERNAL_ERROR,
					e.getMessage());
		}
		return result;
	}

	/**
	 * Control automatic reauthentication
	 * @param reauth If true and if the user is logged out and if there is a login url defined then ZAP will
	 * attempt to automatically login when active scanning or fuzzing 
	 */
	public void setReauthenticate(boolean reauth) {
		extension.setReauthenticate(reauth);
	}

	/**
	 * Return the login message defined
	 * @return
	 * @throws Exception 
	 */
	public HttpMessage getLoginRequest(int contextId) throws Exception {
		return extension.getLoginRequest(contextId);
	}
	
	/**
	 * Set the login request
	 * @param sn
	 * @throws Exception
	 */
	public void setLoginRequest(int contextId, SiteNode sn) throws Exception {
		extension.setLoginRequest(contextId, sn);
	}

	/**
	 * Set the login request
	 * @param url
	 * @param postData
	 * @throws Exception
	 */
	public void setLoginRequest(int contextId, String url, String postData) throws Exception {
		extension.setLoginRequest(contextId, url, postData);
	}

	/**
	 * Get the logout request
	 * @return
	 * @throws Exception 
	 */
	public HttpMessage getLogoutRequest(int contextId) throws Exception {
		return extension.getLogoutRequest(contextId);
	}

	/**
	 * Set the logout request
	 * @param sn
	 * @throws Exception
	 */
	public void setLogoutRequest(int contextId, SiteNode sn) throws Exception {
		extension.setLogoutRequest(contextId, sn);
	}

	/**
	 * Set the logout request
	 * @param url
	 * @param postData
	 * @throws Exception
	 */
	public void setLogoutRequest(int contextId, String url, String postData) throws Exception {
		extension.setLogoutRequest(contextId, url, postData);
	}

	/**
	 * Login using the login request previously set
	 * @return
	 * @throws Exception 
	 */
	public boolean login(int contextId) throws Exception {
		return extension.login(contextId);
	}

	/**
	 * Logout using the logout request previously set
	 * @return
	 * @throws Exception 
	 */
	public boolean logout(int contextId) throws Exception {
		return extension.logout(contextId);
	}

	/**
	 * Get the regex pattern used to tell if the user is logged out
	 * @return
	 * @throws Exception 
	 */
	public String getLoggedOutIndicationRegex(int contextId) throws Exception {
		return extension.getLoggedOutIndicationRegex(contextId);
	}

	/**
	 * Set the regex pattern used to tell if the user is logged out
	 * @param unauthIndicationRegex
	 * @throws Exception 
	 */
	public void setLoggedOutIndicationRegex(int contextId, String unauthIndicationRegex) throws Exception {
		extension.setLoggedOutIndicationRegex(contextId, unauthIndicationRegex);
	}
	
	/**
	 * Get the regex pattern used to tell if the user is logged in
	 * @return
	 * @throws Exception 
	 */
	public String getLoggedInIndicationRegex(int contextId) throws Exception {
		return extension.getLoggedInIndicationRegex(contextId);
	}

	/**
	 * Set the regex pattern used to tell if the user is logged in
	 * @param authIndicationRegex
	 * @throws Exception 
	 */
	public void setLoggedInIndicationRegex(int contextId, String authIndicationRegex) throws Exception {
		this.extension.setLoggedInIndicationRegex(contextId, authIndicationRegex);
	}

}
