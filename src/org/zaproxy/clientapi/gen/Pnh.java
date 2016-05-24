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
public class Pnh {

	private ClientApi api = null;

	public Pnh(ClientApi api) {
		this.api = api;
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse monitor(String apikey, String id, String message) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("message", message);
		return api.callApi("pnh", "action", "monitor", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse oracle(String apikey, String id) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		return api.callApi("pnh", "action", "oracle", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse startMonitoring(String apikey, String url) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		return api.callApi("pnh", "action", "startMonitoring", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse stopMonitoring(String apikey, String id) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		return api.callApi("pnh", "action", "stopMonitoring", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public byte[] pnh(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("pnh", "other", "pnh", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public byte[] manifest(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("pnh", "other", "manifest", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public byte[] service(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("pnh", "other", "service", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public byte[] fx_pnhxpi(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("pnh", "other", "fx_pnh.xpi", map);
	}

}
