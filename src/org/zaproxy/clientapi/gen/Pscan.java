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
public class Pscan {

	private ClientApi api = null;

	public Pscan(ClientApi api) {
		this.api = api;
	}

	/**
	 * The number of records the passive scanner still has to scan
	 */
	public ApiResponse recordsToScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("pscan", "view", "recordsToScan", map);
	}

	public ApiResponse scanners() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("pscan", "view", "scanners", map);
	}

	public ApiResponse setEnabled(String apikey, String enabled) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("enabled", enabled);
		return api.callApi("pscan", "action", "setEnabled", map);
	}

	public ApiResponse enableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("pscan", "action", "enableAllScanners", map);
	}

	public ApiResponse disableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("pscan", "action", "disableAllScanners", map);
	}

	public ApiResponse enableScanners(String apikey, String ids) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("ids", ids);
		return api.callApi("pscan", "action", "enableScanners", map);
	}

	public ApiResponse disableScanners(String apikey, String ids) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("ids", ids);
		return api.callApi("pscan", "action", "disableScanners", map);
	}

}
