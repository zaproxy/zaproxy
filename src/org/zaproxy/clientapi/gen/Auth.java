/* Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright the ZAP development team
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


package org.zaproxy.clientapi.gen;

import java.util.HashMap;
import java.util.Map;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;


/**
 * This file was automatically generated.
 */
public class Auth {

	private ClientApi api = null;

	public Auth(ClientApi api) {
		this.api = api;
	}

	public ApiResponse loginUrl(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "view", "loginUrl", map);
	}

	public ApiResponse loginData(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "view", "loginData", map);
	}

	public ApiResponse loggedInIndicator(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "view", "loggedInIndicator", map);
	}

	public ApiResponse logoutUrl(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "view", "logoutUrl", map);
	}

	public ApiResponse logoutData(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "view", "logoutData", map);
	}

	public ApiResponse loggedOutIndicator(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "view", "loggedOutIndicator", map);
	}

	public ApiResponse login(String apikey, String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "action", "login", map);
	}

	public ApiResponse logout(String apikey, String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("auth", "action", "logout", map);
	}

	public ApiResponse autoReauthOn(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		return api.callApi("auth", "action", "autoReauthOn", map);
	}

	public ApiResponse autoReauthOff(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		return api.callApi("auth", "action", "autoReauthOff", map);
	}

	public ApiResponse setLoginUrl(String apikey, String contextid, String url, String postdata) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		map.put("url", url);
		map.put("postData", postdata);
		return api.callApi("auth", "action", "setLoginUrl", map);
	}

	public ApiResponse setLoginIndicator(String apikey, String contextid, String indicator) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		map.put("indicator", indicator);
		return api.callApi("auth", "action", "setLoginIndicator", map);
	}

	public ApiResponse setLogoutUrl(String apikey, String contextid, String url, String postdata) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		map.put("url", url);
		map.put("postData", postdata);
		return api.callApi("auth", "action", "setLogoutUrl", map);
	}

	public ApiResponse setLoggedOutIndicator(String apikey, String contextid, String indicator) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		map.put("indicator", indicator);
		return api.callApi("auth", "action", "setLoggedOutIndicator", map);
	}

}
