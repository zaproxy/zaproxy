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
package org.zaproxy.zap.extension.auth;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.ApiAction;
import org.zaproxy.zap.extension.api.ApiException;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.api.ApiView;

public class AuthAPI extends ApiImplementor {

    private static Logger log = Logger.getLogger(AuthAPI.class);

	private static final String PREFIX = "auth";
	
	private static final String VIEW_LOGIN_URL = "loginUrl";
	private static final String VIEW_LOGIN_DATA = "loginData";
	private static final String VIEW_LOGGED_IN_INDICATOR = "loggedInIndicator";
	private static final String VIEW_LOGOUT_URL = "logoutUrl";
	private static final String VIEW_LOGOUT_DATA = "logoutData";
	private static final String VIEW_LOGGED_OUT_INDICATOR = "loggedOutIndicator";
	
	private static final String ACTION_AUTHENTICATE = "authenticate";
	private static final String ACTION_AUTO_REAUTH_ON = "autoReauthOn";
	private static final String ACTION_AUTO_REAUTH_OFF = "autoReauthOff";
	private static final String ACTION_SET_LOGIN_NODE = "setLoginUrl";
	private static final String ACTION_SET_LOGIN_INDICATOR = "setLoginIndicator";
	private static final String ACTION_SET_LOGOUT_NODE = "setLogoutUrl";
	private static final String ACTION_SET_LOGOUT_INDICATOR = "setLoggedOutIndicator";

	private static final String ACTION_PARAM_URL = "url";
	private static final String ACTION_PARAM_DATA = "postData";
	private static final String ACTION_PARAM_INDICATOR = "indicator";
	
	private ExtensionAuth extension;
	
	public AuthAPI (ExtensionAuth extension) {
		this.extension = extension;
		
		this.addApiView(new ApiView(VIEW_LOGIN_URL));
		this.addApiView(new ApiView(VIEW_LOGIN_DATA));
		this.addApiView(new ApiView(VIEW_LOGGED_IN_INDICATOR));
		
		this.addApiView(new ApiView(VIEW_LOGOUT_URL));
		this.addApiView(new ApiView(VIEW_LOGOUT_DATA));
		this.addApiView(new ApiView(VIEW_LOGGED_OUT_INDICATOR));
		
		this.addApiAction(new ApiAction(ACTION_AUTHENTICATE));
		this.addApiAction(new ApiAction(ACTION_AUTO_REAUTH_ON));
		this.addApiAction(new ApiAction(ACTION_AUTO_REAUTH_OFF));
		this.addApiAction(new ApiAction(ACTION_SET_LOGIN_NODE, 
				new String[] {ACTION_PARAM_URL, ACTION_PARAM_DATA}));
		this.addApiAction(new ApiAction(ACTION_SET_LOGIN_INDICATOR, 
				new String[] {ACTION_PARAM_INDICATOR}));
		
		this.addApiAction(new ApiAction(ACTION_SET_LOGOUT_NODE, 
				new String[] {ACTION_PARAM_URL, ACTION_PARAM_DATA}));
		this.addApiAction(new ApiAction(ACTION_SET_LOGOUT_INDICATOR, 
				new String[] {ACTION_PARAM_INDICATOR}));

	}
	
	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public JSON handleApiAction(String name, JSONObject params) throws ApiException {
		log.debug("handleApiAction " + name + " " + params.toString());
		if (ACTION_AUTHENTICATE.equals(name)) {
		} else if (ACTION_AUTO_REAUTH_ON.equals(name)) {
			if (! this.login()) {
				JSONArray result = new JSONArray();
				result.add("FAIL");
				return result;
			}
		} else if (ACTION_AUTO_REAUTH_OFF.equals(name)) {
			this.setReauthenticate(false);
		} else if (ACTION_SET_LOGIN_NODE.equals(name)) {
			String url = params.getString(ACTION_PARAM_URL);
			String postData = params.getString(ACTION_PARAM_DATA);
			if (url == null || url.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_URL);
			}
			try {
				setLoginRequest(url, postData);
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
			setLoggedInIndicationRegex(ind);
		} else if (ACTION_SET_LOGOUT_NODE.equals(name)) {
			String url = params.getString(ACTION_PARAM_URL);
			String postData = params.getString(ACTION_PARAM_DATA);
			if (url == null || url.length() == 0) {
				throw new ApiException(ApiException.Type.MISSING_PARAMETER, ACTION_PARAM_URL);
			}
			try {
				setLogoutRequest(url, postData);
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
			setLoggedOutIndicationRegex(ind);
		} else {
			throw new ApiException(ApiException.Type.BAD_ACTION);
		}
		JSONArray result = new JSONArray();
		result.add("OK");
		return result;
	}

	@Override
	public JSON handleApiView(String name, JSONObject params)
			throws ApiException {
		JSONArray result = new JSONArray();
		if (VIEW_LOGIN_URL.equals(name)) {
			if (getLoginRequest() != null) {
				result.add(getLoginRequest().getRequestHeader().getURI().toString());
			}
		} else if (VIEW_LOGIN_DATA.equals(name)) {
			if (getLoginRequest() != null) {
				result.add(getLoginRequest().getRequestBody().toString());
			}
		} else if (VIEW_LOGGED_IN_INDICATOR.equals(name)) {
			result.add(getLoggedInIndicationRegex());
		} else if (VIEW_LOGOUT_URL.equals(name)) {
			if (getLogoutRequest() != null) {
				result.add(getLogoutRequest().getRequestHeader().getURI().toString());
			}
		} else if (VIEW_LOGOUT_DATA.equals(name)) {
			if (getLogoutRequest() != null) {
				result.add(getLogoutRequest().getRequestBody().toString());
			}
		} else if (VIEW_LOGGED_OUT_INDICATOR.equals(name)) {
			result.add(getLoggedOutIndicationRegex());
		} else {
			throw new ApiException(ApiException.Type.BAD_VIEW);
		}
		return result;
	}
	/*
	@Override
	public String viewResultToXML (String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		if (VIEW_STATUS.equals(name)) {
			serializer.setArrayName("status");
			serializer.setElementName("percent");
		}
		return serializer.write(result);
	}

	@Override
	public String actionResultToXML (String name, JSON result) {
		XMLSerializer serializer = new XMLSerializer();
		serializer.setArrayName("result");
		return serializer.write(result);
	}
	*/

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
	 */
	public HttpMessage getLoginRequest() {
		return extension.getLoginRequest();
	}
	
	/**
	 * Set the login request
	 * @param sn
	 * @throws Exception
	 */
	public void setLoginRequest(SiteNode sn) throws Exception {
		extension.setLoginRequest(sn);
	}

	/**
	 * Set the login request
	 * @param url
	 * @param postData
	 * @throws Exception
	 */
	public void setLoginRequest(String url, String postData) throws Exception {
		extension.setLoginRequest(url, postData);
	}

	/**
	 * Get the logout request
	 * @return
	 */
	public HttpMessage getLogoutRequest() {
		return extension.getLogoutRequest();
	}

	/**
	 * Set the logout request
	 * @param sn
	 * @throws Exception
	 */
	public void setLogoutRequest(SiteNode sn) throws Exception {
		extension.setLogoutRequest(sn);
	}

	/**
	 * Set the logout request
	 * @param url
	 * @param postData
	 * @throws Exception
	 */
	public void setLogoutRequest(String url, String postData) throws Exception {
		extension.setLogoutRequest(url, postData);
	}

	/**
	 * Login using the login request previously set
	 * @return
	 */
	public boolean login() {
		return extension.login();
	}

	/**
	 * Logout using the logout request previously set
	 * @return
	 */
	public boolean logout() {
		return extension.logout();
	}

	/**
	 * Get the regex pattern used to tell if the user is logged out
	 * @return
	 */
	public String getLoggedOutIndicationRegex() {
		return extension.getLoggedOutIndicationRegex();
	}

	/**
	 * Set the regex pattern used to tell if the user is logged out
	 * @param unauthIndicationRegex
	 */
	public void setLoggedOutIndicationRegex(String unauthIndicationRegex) {
		extension.setLoggedOutIndicationRegex(unauthIndicationRegex);
	}
	
	/**
	 * Get the regex pattern used to tell if the user is logged in
	 * @return
	 */
	public String getLoggedInIndicationRegex() {
		return extension.getLoggedInIndicationRegex();
	}

	/**
	 * Set the regex pattern used to tell if the user is logged in
	 * @param authIndicationRegex
	 */
	public void setLoggedInIndicationRegex(String authIndicationRegex) {
		this.extension.setLoggedInIndicationRegex(authIndicationRegex);
	}

}
