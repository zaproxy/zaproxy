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
public class Break {

	private ClientApi api = null;

	public Break(ClientApi api) {
		this.api = api;
	}

	public ApiResponse brk(String apikey, String type, String scope, String state) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("type", type);
		map.put("scope", scope);
		map.put("state", state);
		return api.callApi("break", "action", "break", map);
	}

	public ApiResponse addHttpBreakpoint(String apikey, String string, String location, String match, String inverse, String ignorecase) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("string", string);
		map.put("location", location);
		map.put("match", match);
		map.put("inverse", inverse);
		map.put("ignorecase", ignorecase);
		return api.callApi("break", "action", "addHttpBreakpoint", map);
	}

	public ApiResponse removeHttpBreakpoint(String apikey, String string, String location, String match, String inverse, String ignorecase) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("string", string);
		map.put("location", location);
		map.put("match", match);
		map.put("inverse", inverse);
		map.put("ignorecase", ignorecase);
		return api.callApi("break", "action", "removeHttpBreakpoint", map);
	}

}
