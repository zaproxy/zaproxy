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

	public ApiResponse optionParseComments() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionParseComments", map);
	}

	public ApiResponse optionParseRobotsTxt() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionParseRobotsTxt", map);
	}

	public ApiResponse optionParseSVNEntries() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionParseSVNEntries", map);
	}

	public ApiResponse optionParseGit() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionParseGit", map);
	}

	public ApiResponse optionHandleParameters() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionHandleParameters", map);
	}

	public ApiResponse optionHandleODataParametersVisited() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("spider", "view", "optionHandleODataParametersVisited", map);
	}

	public ApiResponse scan(String apikey, String url) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("url", url);
		return api.callApi("spider", "action", "scan", map);
	}

	public ApiResponse scanAsUser(String apikey, String url, String contextid, String userid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("url", url);
		map.put("contextId", contextid);
		map.put("userId", userid);
		return api.callApi("spider", "action", "scanAsUser", map);
	}

	public ApiResponse stop(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		return api.callApi("spider", "action", "stop", map);
	}

	public ApiResponse clearExcludedFromScan(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		return api.callApi("spider", "action", "clearExcludedFromScan", map);
	}

	public ApiResponse excludeFromScan(String apikey, String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("regex", regex);
		return api.callApi("spider", "action", "excludeFromScan", map);
	}

	public ApiResponse setOptionScopeString(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("spider", "action", "setOptionScopeString", map);
	}

	public ApiResponse setOptionSkipURLString(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("spider", "action", "setOptionSkipURLString", map);
	}

	public ApiResponse setOptionHandleParameters(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("spider", "action", "setOptionHandleParameters", map);
	}

	public ApiResponse setOptionUserAgent(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("spider", "action", "setOptionUserAgent", map);
	}

	public ApiResponse setOptionMaxDepth(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("spider", "action", "setOptionMaxDepth", map);
	}

	public ApiResponse setOptionThreadCount(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("spider", "action", "setOptionThreadCount", map);
	}

	public ApiResponse setOptionPostForm(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionPostForm", map);
	}

	public ApiResponse setOptionProcessForm(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionProcessForm", map);
	}

	public ApiResponse setOptionRequestWaitTime(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("spider", "action", "setOptionRequestWaitTime", map);
	}

	public ApiResponse setOptionParseComments(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionParseComments", map);
	}

	public ApiResponse setOptionParseRobotsTxt(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionParseRobotsTxt", map);
	}

	public ApiResponse setOptionParseSVNEntries(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionParseSVNEntries", map);
	}

	public ApiResponse setOptionParseGit(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionParseGit", map);
	}

	public ApiResponse setOptionHandleODataParametersVisited(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("spider", "action", "setOptionHandleODataParametersVisited", map);
	}

}
