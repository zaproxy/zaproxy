/* Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 ZAP development team
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
public class Core {

	private ClientApi api = null;

	public Core(ClientApi api) {
		this.api = api;
	}

	public ApiResponse alerts(String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		map.put("start", start);
		map.put("count", count);
		return api.callApi("core", "view", "alerts", map);
	}

	public ApiResponse hosts() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "hosts", map);
	}

	public ApiResponse sites() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "sites", map);
	}

	public ApiResponse urls() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "urls", map);
	}

	public ApiResponse messages(String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		map.put("start", start);
		map.put("count", count);
		return api.callApi("core", "view", "messages", map);
	}

	public ApiResponse version() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "version", map);
	}

	public ApiResponse excludedFromProxy() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "excludedFromProxy", map);
	}

	public ApiResponse homeDirectory() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "homeDirectory", map);
	}

	/**
	 * Shuts down ZAP
	 */
	public ApiResponse shutdown() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "action", "shutdown", map);
	}

	public ApiResponse newSession(String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("name", name);
		return api.callApi("core", "action", "newSession", map);
	}

	public ApiResponse loadSession(String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("name", name);
		return api.callApi("core", "action", "loadSession", map);
	}

	public ApiResponse saveSession(String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("name", name);
		return api.callApi("core", "action", "saveSession", map);
	}

	public ApiResponse clearExcludedFromProxy() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "action", "clearExcludedFromProxy", map);
	}

	public ApiResponse excludeFromProxy(String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("regex", regex);
		return api.callApi("core", "action", "excludeFromProxy", map);
	}

	public ApiResponse setHomeDirectory(String dir) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("dir", dir);
		return api.callApi("core", "action", "setHomeDirectory", map);
	}

	public ApiResponse generateRootCA() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "action", "generateRootCA", map);
	}

}
