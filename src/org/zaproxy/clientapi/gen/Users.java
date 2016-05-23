/* Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 the ZAP development team
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
public class Users {

	private ClientApi api = null;

	public Users(ClientApi api) {
		this.api = api;
	}

	public ApiResponse usersList(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (contextid != null) {
			map.put("contextId", contextid);
		}
		return api.callApi("users", "view", "usersList", map);
	}

	public ApiResponse getUserById(String contextid, String userid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (contextid != null) {
			map.put("contextId", contextid);
		}
		if (userid != null) {
			map.put("userId", userid);
		}
		return api.callApi("users", "view", "getUserById", map);
	}

	public ApiResponse getAuthenticationCredentialsConfigParams(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("users", "view", "getAuthenticationCredentialsConfigParams", map);
	}

	public ApiResponse getAuthenticationCredentials(String contextid, String userid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		map.put("userId", userid);
		return api.callApi("users", "view", "getAuthenticationCredentials", map);
	}

	public ApiResponse newUser(String apikey, String contextid, String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("name", name);
		return api.callApi("users", "action", "newUser", map);
	}

	public ApiResponse removeUser(String apikey, String contextid, String userid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		return api.callApi("users", "action", "removeUser", map);
	}

	public ApiResponse setUserEnabled(String apikey, String contextid, String userid, String enabled) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		map.put("enabled", enabled);
		return api.callApi("users", "action", "setUserEnabled", map);
	}

	public ApiResponse setUserName(String apikey, String contextid, String userid, String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		map.put("name", name);
		return api.callApi("users", "action", "setUserName", map);
	}

	public ApiResponse setAuthenticationCredentials(String apikey, String contextid, String userid, String authcredentialsconfigparams) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		if (authcredentialsconfigparams != null) {
			map.put("authCredentialsConfigParams", authcredentialsconfigparams);
		}
		return api.callApi("users", "action", "setAuthenticationCredentials", map);
	}

}
