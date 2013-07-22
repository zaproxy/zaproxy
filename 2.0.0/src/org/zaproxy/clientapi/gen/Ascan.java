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
public class Ascan {

	private ClientApi api = null;

	public Ascan(ClientApi api) {
		this.api = api;
	}

	public ApiResponse status() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "status", map);
	}

	public ApiResponse excludedFromScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "excludedFromScan", map);
	}

	public ApiResponse optionThreadPerHost() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionThreadPerHost", map);
	}

	public ApiResponse optionHostPerScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionHostPerScan", map);
	}

	public ApiResponse optionDelayInMs() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionDelayInMs", map);
	}

	public ApiResponse optionHandleAntiCSRFTokens() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionHandleAntiCSRFTokens", map);
	}

	public ApiResponse optionAlertThreshold() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionAlertThreshold", map);
	}

	public ApiResponse optionAttackStrength() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionAttackStrength", map);
	}

	public ApiResponse scan(String url, String recurse, String inscopeonly) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("url", url);
		map.put("recurse", recurse);
		map.put("inScopeOnly", inscopeonly);
		return api.callApi("ascan", "action", "scan", map);
	}

	public ApiResponse clearExcludedFromScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "action", "clearExcludedFromScan", map);
	}

	public ApiResponse excludeFromScan(String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("regex", regex);
		return api.callApi("ascan", "action", "excludeFromScan", map);
	}

	public ApiResponse setOptionAlertThreshold(String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("ascan", "action", "setOptionAlertThreshold", map);
	}

	public ApiResponse setOptionAttackStrength(String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("ascan", "action", "setOptionAttackStrength", map);
	}

	public ApiResponse setOptionThreadPerHost(int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionThreadPerHost", map);
	}

	public ApiResponse setOptionHostPerScan(int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionHostPerScan", map);
	}

	public ApiResponse setOptionDelayInMs(int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionDelayInMs", map);
	}

	public ApiResponse setOptionHandleAntiCSRFTokens(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionHandleAntiCSRFTokens", map);
	}

}
