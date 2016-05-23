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

	/**
	 * Lists all passive scanners with its ID, name, enabled state and alert threshold.
	 */
	public ApiResponse scanners() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("pscan", "view", "scanners", map);
	}

	/**
	 * Sets whether or not the passive scanning is enabled
	 */
	public ApiResponse setEnabled(String apikey, String enabled) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("enabled", enabled);
		return api.callApi("pscan", "action", "setEnabled", map);
	}

	/**
	 * Enables all passive scanners
	 */
	public ApiResponse enableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("pscan", "action", "enableAllScanners", map);
	}

	/**
	 * Disables all passive scanners
	 */
	public ApiResponse disableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("pscan", "action", "disableAllScanners", map);
	}

	/**
	 * Enables all passive scanners with the given IDs (comma separated list of IDs)
	 */
	public ApiResponse enableScanners(String apikey, String ids) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("ids", ids);
		return api.callApi("pscan", "action", "enableScanners", map);
	}

	/**
	 * Disables all passive scanners with the given IDs (comma separated list of IDs)
	 */
	public ApiResponse disableScanners(String apikey, String ids) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("ids", ids);
		return api.callApi("pscan", "action", "disableScanners", map);
	}

	/**
	 * Sets the alert threshold of the passive scanner with the given ID, accepted values for alert threshold: OFF, DEFAULT, LOW, MEDIUM and HIGH
	 */
	public ApiResponse setScannerAlertThreshold(String apikey, String id, String alertthreshold) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("alertThreshold", alertthreshold);
		return api.callApi("pscan", "action", "setScannerAlertThreshold", map);
	}

}
