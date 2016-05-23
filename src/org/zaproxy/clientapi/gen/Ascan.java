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
public class Ascan {

	private ClientApi api = null;

	public Ascan(ClientApi api) {
		this.api = api;
	}

	public ApiResponse status(String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (scanid != null) {
			map.put("scanId", scanid);
		}
		return api.callApi("ascan", "view", "status", map);
	}

	public ApiResponse scanProgress(String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (scanid != null) {
			map.put("scanId", scanid);
		}
		return api.callApi("ascan", "view", "scanProgress", map);
	}

	public ApiResponse messagesIds(String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("scanId", scanid);
		return api.callApi("ascan", "view", "messagesIds", map);
	}

	public ApiResponse alertsIds(String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("scanId", scanid);
		return api.callApi("ascan", "view", "alertsIds", map);
	}

	public ApiResponse scans() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "scans", map);
	}

	public ApiResponse scanPolicyNames() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "scanPolicyNames", map);
	}

	public ApiResponse excludedFromScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "excludedFromScan", map);
	}

	public ApiResponse scanners(String scanpolicyname, String policyid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		if (policyid != null) {
			map.put("policyId", policyid);
		}
		return api.callApi("ascan", "view", "scanners", map);
	}

	public ApiResponse policies(String scanpolicyname, String policyid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		if (policyid != null) {
			map.put("policyId", policyid);
		}
		return api.callApi("ascan", "view", "policies", map);
	}

	public ApiResponse attackModeQueue() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "attackModeQueue", map);
	}

	public ApiResponse optionAttackPolicy() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionAttackPolicy", map);
	}

	public ApiResponse optionDefaultPolicy() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionDefaultPolicy", map);
	}

	public ApiResponse optionDelayInMs() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionDelayInMs", map);
	}

	public ApiResponse optionExcludedParamList() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionExcludedParamList", map);
	}

	public ApiResponse optionHandleAntiCSRFTokens() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionHandleAntiCSRFTokens", map);
	}

	public ApiResponse optionHostPerScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionHostPerScan", map);
	}

	public ApiResponse optionMaxChartTimeInMins() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionMaxChartTimeInMins", map);
	}

	public ApiResponse optionMaxResultsToList() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionMaxResultsToList", map);
	}

	public ApiResponse optionMaxScansInUI() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionMaxScansInUI", map);
	}

	public ApiResponse optionTargetParamsEnabledRPC() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionTargetParamsEnabledRPC", map);
	}

	public ApiResponse optionTargetParamsInjectable() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionTargetParamsInjectable", map);
	}

	public ApiResponse optionThreadPerHost() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionThreadPerHost", map);
	}

	public ApiResponse optionAllowAttackOnStart() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionAllowAttackOnStart", map);
	}

	public ApiResponse optionInjectPluginIdInHeader() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionInjectPluginIdInHeader", map);
	}

	public ApiResponse optionPromptInAttackMode() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionPromptInAttackMode", map);
	}

	public ApiResponse optionPromptToClearFinishedScans() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionPromptToClearFinishedScans", map);
	}

	public ApiResponse optionRescanInAttackMode() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionRescanInAttackMode", map);
	}

	/**
	 * Tells whether or not the HTTP Headers of all requests should be scanned. Not just requests that send parameters, through the query or request body.
	 */
	public ApiResponse optionScanHeadersAllRequests() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionScanHeadersAllRequests", map);
	}

	public ApiResponse optionShowAdvancedDialog() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionShowAdvancedDialog", map);
	}

	public ApiResponse scan(String apikey, String url, String recurse, String inscopeonly, String scanpolicyname, String method, String postdata) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		if (recurse != null) {
			map.put("recurse", recurse);
		}
		if (inscopeonly != null) {
			map.put("inScopeOnly", inscopeonly);
		}
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		if (method != null) {
			map.put("method", method);
		}
		if (postdata != null) {
			map.put("postData", postdata);
		}
		return api.callApi("ascan", "action", "scan", map);
	}

	/**
	 * Active Scans from the perspective of a User, obtained using the given Context ID and User ID. See 'scan' action for more details.
	 */
	public ApiResponse scanAsUser(String apikey, String url, String contextid, String userid, String recurse, String scanpolicyname, String method, String postdata) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		map.put("contextId", contextid);
		map.put("userId", userid);
		if (recurse != null) {
			map.put("recurse", recurse);
		}
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		if (method != null) {
			map.put("method", method);
		}
		if (postdata != null) {
			map.put("postData", postdata);
		}
		return api.callApi("ascan", "action", "scanAsUser", map);
	}

	public ApiResponse pause(String apikey, String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scanId", scanid);
		return api.callApi("ascan", "action", "pause", map);
	}

	public ApiResponse resume(String apikey, String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scanId", scanid);
		return api.callApi("ascan", "action", "resume", map);
	}

	public ApiResponse stop(String apikey, String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scanId", scanid);
		return api.callApi("ascan", "action", "stop", map);
	}

	public ApiResponse removeScan(String apikey, String scanid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scanId", scanid);
		return api.callApi("ascan", "action", "removeScan", map);
	}

	public ApiResponse pauseAllScans(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("ascan", "action", "pauseAllScans", map);
	}

	public ApiResponse resumeAllScans(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("ascan", "action", "resumeAllScans", map);
	}

	public ApiResponse stopAllScans(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("ascan", "action", "stopAllScans", map);
	}

	public ApiResponse removeAllScans(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("ascan", "action", "removeAllScans", map);
	}

	public ApiResponse clearExcludedFromScan(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("ascan", "action", "clearExcludedFromScan", map);
	}

	public ApiResponse excludeFromScan(String apikey, String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("regex", regex);
		return api.callApi("ascan", "action", "excludeFromScan", map);
	}

	public ApiResponse enableAllScanners(String apikey, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "enableAllScanners", map);
	}

	public ApiResponse disableAllScanners(String apikey, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "disableAllScanners", map);
	}

	public ApiResponse enableScanners(String apikey, String ids, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("ids", ids);
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "enableScanners", map);
	}

	public ApiResponse disableScanners(String apikey, String ids, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("ids", ids);
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "disableScanners", map);
	}

	public ApiResponse setEnabledPolicies(String apikey, String ids, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("ids", ids);
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "setEnabledPolicies", map);
	}

	public ApiResponse setPolicyAttackStrength(String apikey, String id, String attackstrength, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("attackStrength", attackstrength);
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "setPolicyAttackStrength", map);
	}

	public ApiResponse setPolicyAlertThreshold(String apikey, String id, String alertthreshold, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("alertThreshold", alertthreshold);
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "setPolicyAlertThreshold", map);
	}

	public ApiResponse setScannerAttackStrength(String apikey, String id, String attackstrength, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("attackStrength", attackstrength);
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "setScannerAttackStrength", map);
	}

	public ApiResponse setScannerAlertThreshold(String apikey, String id, String alertthreshold, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		map.put("alertThreshold", alertthreshold);
		if (scanpolicyname != null) {
			map.put("scanPolicyName", scanpolicyname);
		}
		return api.callApi("ascan", "action", "setScannerAlertThreshold", map);
	}

	public ApiResponse addScanPolicy(String apikey, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scanPolicyName", scanpolicyname);
		return api.callApi("ascan", "action", "addScanPolicy", map);
	}

	public ApiResponse removeScanPolicy(String apikey, String scanpolicyname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scanPolicyName", scanpolicyname);
		return api.callApi("ascan", "action", "removeScanPolicy", map);
	}

	public ApiResponse setOptionAttackPolicy(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("ascan", "action", "setOptionAttackPolicy", map);
	}

	public ApiResponse setOptionDefaultPolicy(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("ascan", "action", "setOptionDefaultPolicy", map);
	}

	public ApiResponse setOptionAllowAttackOnStart(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionAllowAttackOnStart", map);
	}

	public ApiResponse setOptionDelayInMs(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionDelayInMs", map);
	}

	public ApiResponse setOptionHandleAntiCSRFTokens(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionHandleAntiCSRFTokens", map);
	}

	public ApiResponse setOptionHostPerScan(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionHostPerScan", map);
	}

	public ApiResponse setOptionInjectPluginIdInHeader(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionInjectPluginIdInHeader", map);
	}

	public ApiResponse setOptionMaxChartTimeInMins(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionMaxChartTimeInMins", map);
	}

	public ApiResponse setOptionMaxResultsToList(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionMaxResultsToList", map);
	}

	public ApiResponse setOptionMaxScansInUI(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionMaxScansInUI", map);
	}

	public ApiResponse setOptionPromptInAttackMode(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionPromptInAttackMode", map);
	}

	public ApiResponse setOptionPromptToClearFinishedScans(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionPromptToClearFinishedScans", map);
	}

	public ApiResponse setOptionRescanInAttackMode(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionRescanInAttackMode", map);
	}

	/**
	 * Sets whether or not the HTTP Headers of all requests should be scanned. Not just requests that send parameters, through the query or request body.
	 */
	public ApiResponse setOptionScanHeadersAllRequests(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionScanHeadersAllRequests", map);
	}

	public ApiResponse setOptionShowAdvancedDialog(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionShowAdvancedDialog", map);
	}

	public ApiResponse setOptionTargetParamsEnabledRPC(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionTargetParamsEnabledRPC", map);
	}

	public ApiResponse setOptionTargetParamsInjectable(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionTargetParamsInjectable", map);
	}

	public ApiResponse setOptionThreadPerHost(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionThreadPerHost", map);
	}

}
