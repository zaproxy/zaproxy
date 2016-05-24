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
public class HttpSessions {

	private ClientApi api = null;

	public HttpSessions(ClientApi api) {
		this.api = api;
	}

	/**
	 * Gets the sessions of the given site. Optionally returning just the session with the given name.
	 */
	public ApiResponse sessions(String site, String session) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("site", site);
		if (session != null) {
			map.put("session", session);
		}
		return api.callApi("httpSessions", "view", "sessions", map);
	}

	/**
	 * Gets the name of the active session for the given site.
	 */
	public ApiResponse activeSession(String site) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("site", site);
		return api.callApi("httpSessions", "view", "activeSession", map);
	}

	/**
	 * Gets the names of the session tokens for the given site.
	 */
	public ApiResponse sessionTokens(String site) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("site", site);
		return api.callApi("httpSessions", "view", "sessionTokens", map);
	}

	/**
	 * Creates an empty session for the given site. Optionally with the given name.
	 */
	public ApiResponse createEmptySession(String apikey, String site, String session) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		if (session != null) {
			map.put("session", session);
		}
		return api.callApi("httpSessions", "action", "createEmptySession", map);
	}

	/**
	 * Removes the session from the given site.
	 */
	public ApiResponse removeSession(String apikey, String site, String session) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		map.put("session", session);
		return api.callApi("httpSessions", "action", "removeSession", map);
	}

	/**
	 * Sets the given session as active for the given site.
	 */
	public ApiResponse setActiveSession(String apikey, String site, String session) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		map.put("session", session);
		return api.callApi("httpSessions", "action", "setActiveSession", map);
	}

	/**
	 * Unsets the active session of the given site.
	 */
	public ApiResponse unsetActiveSession(String apikey, String site) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		return api.callApi("httpSessions", "action", "unsetActiveSession", map);
	}

	/**
	 * Adds the session token to the given site.
	 */
	public ApiResponse addSessionToken(String apikey, String site, String sessiontoken) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		map.put("sessionToken", sessiontoken);
		return api.callApi("httpSessions", "action", "addSessionToken", map);
	}

	/**
	 * Removes the session token from the given site.
	 */
	public ApiResponse removeSessionToken(String apikey, String site, String sessiontoken) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		map.put("sessionToken", sessiontoken);
		return api.callApi("httpSessions", "action", "removeSessionToken", map);
	}

	/**
	 * Sets the value of the session token of the given session for the given site.
	 */
	public ApiResponse setSessionTokenValue(String apikey, String site, String session, String sessiontoken, String tokenvalue) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		map.put("session", session);
		map.put("sessionToken", sessiontoken);
		map.put("tokenValue", tokenvalue);
		return api.callApi("httpSessions", "action", "setSessionTokenValue", map);
	}

	/**
	 * Renames the session of the given site.
	 */
	public ApiResponse renameSession(String apikey, String site, String oldsessionname, String newsessionname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("site", site);
		map.put("oldSessionName", oldsessionname);
		map.put("newSessionName", newsessionname);
		return api.callApi("httpSessions", "action", "renameSession", map);
	}

}
