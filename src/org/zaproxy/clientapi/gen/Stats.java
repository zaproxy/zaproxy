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
public class Stats {

	private ClientApi api = null;

	public Stats(ClientApi api) {
		this.api = api;
	}

	/**
	 * Statistics
	 */
	public ApiResponse stats(String keyprefix) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (keyprefix != null) {
			map.put("keyPrefix", keyprefix);
		}
		return api.callApi("stats", "view", "stats", map);
	}

	/**
	 * Gets all of the site based statistics, optionally filtered by a key prefix
	 */
	public ApiResponse allSitesStats(String keyprefix) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (keyprefix != null) {
			map.put("keyPrefix", keyprefix);
		}
		return api.callApi("stats", "view", "allSitesStats", map);
	}

	/**
	 * Gets all of the global statistics, optionally filtered by a key prefix
	 */
	public ApiResponse siteStats(String site, String keyprefix) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("site", site);
		if (keyprefix != null) {
			map.put("keyPrefix", keyprefix);
		}
		return api.callApi("stats", "view", "siteStats", map);
	}

	/**
	 * Gets the Statsd service hostname
	 */
	public ApiResponse optionStatsdHost() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("stats", "view", "optionStatsdHost", map);
	}

	/**
	 * Gets the Statsd service port
	 */
	public ApiResponse optionStatsdPort() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("stats", "view", "optionStatsdPort", map);
	}

	/**
	 * Gets the prefix to be applied to all stats sent to the configured Statsd service
	 */
	public ApiResponse optionStatsdPrefix() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("stats", "view", "optionStatsdPrefix", map);
	}

	/**
	 * Returns 'true' if in memory statistics are enabled, otherwise returns 'false'
	 */
	public ApiResponse optionInMemoryEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("stats", "view", "optionInMemoryEnabled", map);
	}

	/**
	 * Returns 'true' if a Statsd server has been correctly configured, otherwise returns 'false'
	 */
	public ApiResponse optionStatsdEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("stats", "view", "optionStatsdEnabled", map);
	}

	/**
	 * Clears all of the statistics
	 */
	public ApiResponse clearStats(String apikey, String keyprefix) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		if (keyprefix != null) {
			map.put("keyPrefix", keyprefix);
		}
		return api.callApi("stats", "action", "clearStats", map);
	}

	/**
	 * Sets the Statsd service hostname, supply an empty string to stop using a Statsd service
	 */
	public ApiResponse setOptionStatsdHost(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("stats", "action", "setOptionStatsdHost", map);
	}

	/**
	 * Sets the prefix to be applied to all stats sent to the configured Statsd service
	 */
	public ApiResponse setOptionStatsdPrefix(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("stats", "action", "setOptionStatsdPrefix", map);
	}

	/**
	 * Sets whether in memory statistics are enabled
	 */
	public ApiResponse setOptionInMemoryEnabled(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("stats", "action", "setOptionInMemoryEnabled", map);
	}

	/**
	 * Sets the Statsd service port
	 */
	public ApiResponse setOptionStatsdPort(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("stats", "action", "setOptionStatsdPort", map);
	}

}
