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
public class ForcedUser {

	private ClientApi api = null;

	public ForcedUser(ClientApi api) {
		this.api = api;
	}

	/**
	 * Returns 'true' if 'forced user' mode is enabled, 'false' otherwise
	 */
	public ApiResponse isForcedUserModeEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("forcedUser", "view", "isForcedUserModeEnabled", map);
	}

	/**
	 * Gets the user (ID) set as 'forced user' for the given context (ID)
	 */
	public ApiResponse getForcedUser(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("forcedUser", "view", "getForcedUser", map);
	}

	/**
	 * Sets the user (ID) that should be used in 'forced user' mode for the given context (ID)
	 */
	public ApiResponse setForcedUser(String apikey, String contextid, String userid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		map.put("userId", userid);
		return api.callApi("forcedUser", "action", "setForcedUser", map);
	}

	/**
	 * Sets if 'forced user' mode should be enabled or not
	 */
	public ApiResponse setForcedUserModeEnabled(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("boolean", Boolean.toString(bool));
		return api.callApi("forcedUser", "action", "setForcedUserModeEnabled", map);
	}

}
