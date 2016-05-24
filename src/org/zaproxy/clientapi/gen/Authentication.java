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
public class Authentication {

	private ClientApi api = null;

	public Authentication(ClientApi api) {
		this.api = api;
	}

	public ApiResponse getSupportedAuthenticationMethods() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("authentication", "view", "getSupportedAuthenticationMethods", map);
	}

	public ApiResponse getAuthenticationMethodConfigParams(String authmethodname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("authMethodName", authmethodname);
		return api.callApi("authentication", "view", "getAuthenticationMethodConfigParams", map);
	}

	public ApiResponse getAuthenticationMethod(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("authentication", "view", "getAuthenticationMethod", map);
	}

	public ApiResponse getLoggedInIndicator(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("authentication", "view", "getLoggedInIndicator", map);
	}

	public ApiResponse getLoggedOutIndicator(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("authentication", "view", "getLoggedOutIndicator", map);
	}

	public ApiResponse setAuthenticationMethod(String apikey, String contextid, String authmethodname, String authmethodconfigparams) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("authMethodName", authmethodname);
		if (authmethodconfigparams != null) {
			map.put("authMethodConfigParams", authmethodconfigparams);
		}
		return api.callApi("authentication", "action", "setAuthenticationMethod", map);
	}

	public ApiResponse setLoggedInIndicator(String apikey, String contextid, String loggedinindicatorregex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("loggedInIndicatorRegex", loggedinindicatorregex);
		return api.callApi("authentication", "action", "setLoggedInIndicator", map);
	}

	public ApiResponse setLoggedOutIndicator(String apikey, String contextid, String loggedoutindicatorregex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("loggedOutIndicatorRegex", loggedoutindicatorregex);
		return api.callApi("authentication", "action", "setLoggedOutIndicator", map);
	}

}
