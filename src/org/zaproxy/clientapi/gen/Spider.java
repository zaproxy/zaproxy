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
public class Spider {

	private ClientApi api = null;

	public Spider(ClientApi api) {
		this.api = api;
	}

	public ApiResponse status() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "status", map);
	}

	public ApiResponse results() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "results", map);
	}

	public ApiResponse excludedFromScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "excludedFromScan", map);
	}

	public ApiResponse optionMaxDepth() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionMaxDepth", map);
	}

	public ApiResponse optionScopeText() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionScopeText", map);
	}

	public ApiResponse optionScope() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionScope", map);
	}

	public ApiResponse optionThreadCount() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionThreadCount", map);
	}

	public ApiResponse optionPostForm() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionPostForm", map);
	}

	public ApiResponse optionProcessForm() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionProcessForm", map);
	}

	public ApiResponse optionSkipURLString() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionSkipURLString", map);
	}

	public ApiResponse optionRequestWaitTime() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionRequestWaitTime", map);
	}

	public ApiResponse optionUserAgent() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionUserAgent", map);
	}

	public ApiResponse optionSendCookies() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionSendCookies", map);
	}

	public ApiResponse optionParseComments() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionParseComments", map);
	}

	public ApiResponse optionParseRobotsTxt() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionParseRobotsTxt", map);
	}

	public ApiResponse optionHandleParameters() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionHandleParameters", map);
	}

	public ApiResponse scan(String url) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("url", url);
		return api.callApi("spider", "action", "scan", map);
	}

	public ApiResponse stop() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "action", "stop", map);
	}

	public ApiResponse clearExcludedFromScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "action", "clearExcludedFromScan", map);
	}

	public ApiResponse excludeFromScan(String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("regex", regex);
		return api.callApi("spider", "action", "excludeFromScan", map);
	}

	public ApiResponse setOptionScopeString(String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("spider", "action", "setOptionScopeString", map);
	}

	public ApiResponse setOptionSkipURLString(String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("spider", "action", "setOptionSkipURLString", map);
	}

	public ApiResponse setOptionUserAgent(String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("spider", "action", "setOptionUserAgent", map);
	}

	public ApiResponse setOptionMaxDepth(int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("spider", "action", "setOptionMaxDepth", map);
	}

	public ApiResponse setOptionThreadCount(int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("spider", "action", "setOptionThreadCount", map);
	}

	public ApiResponse setOptionPostForm(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionPostForm", map);
	}

	public ApiResponse setOptionProcessForm(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionProcessForm", map);
	}

	public ApiResponse setOptionRequestWaitTime(int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("spider", "action", "setOptionRequestWaitTime", map);
	}

	public ApiResponse setOptionSendCookies(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionSendCookies", map);
	}

	public ApiResponse setOptionParseComments(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionParseComments", map);
	}

	public ApiResponse setOptionParseRobotsTxt(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionParseRobotsTxt", map);
	}

}
