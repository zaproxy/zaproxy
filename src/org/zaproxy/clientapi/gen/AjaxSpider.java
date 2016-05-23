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
public class AjaxSpider {

	private ClientApi api = null;

	public AjaxSpider(ClientApi api) {
		this.api = api;
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse status() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "status", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse results(String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (start != null) {
			map.put("start", start);
		}
		if (count != null) {
			map.put("count", count);
		}
		return api.callApi("ajaxSpider", "view", "results", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse numberOfResults() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "numberOfResults", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionBrowserId() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionBrowserId", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionEventWait() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionEventWait", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionMaxCrawlDepth() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionMaxCrawlDepth", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionMaxCrawlStates() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionMaxCrawlStates", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionMaxDuration() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionMaxDuration", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionNumberOfBrowsers() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionNumberOfBrowsers", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionReloadWait() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionReloadWait", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionClickDefaultElems() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionClickDefaultElems", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionClickElemsOnce() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionClickElemsOnce", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse optionRandomInputs() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ajaxSpider", "view", "optionRandomInputs", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse scan(String apikey, String url, String inscope) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		if (inscope != null) {
			map.put("inScope", inscope);
		}
		return api.callApi("ajaxSpider", "action", "scan", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse stop(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("ajaxSpider", "action", "stop", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionBrowserId(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("ajaxSpider", "action", "setOptionBrowserId", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionClickDefaultElems(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ajaxSpider", "action", "setOptionClickDefaultElems", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionClickElemsOnce(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ajaxSpider", "action", "setOptionClickElemsOnce", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionEventWait(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ajaxSpider", "action", "setOptionEventWait", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionMaxCrawlDepth(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ajaxSpider", "action", "setOptionMaxCrawlDepth", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionMaxCrawlStates(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ajaxSpider", "action", "setOptionMaxCrawlStates", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionMaxDuration(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ajaxSpider", "action", "setOptionMaxDuration", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionNumberOfBrowsers(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ajaxSpider", "action", "setOptionNumberOfBrowsers", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionRandomInputs(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ajaxSpider", "action", "setOptionRandomInputs", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse setOptionReloadWait(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ajaxSpider", "action", "setOptionReloadWait", map);
	}

}
