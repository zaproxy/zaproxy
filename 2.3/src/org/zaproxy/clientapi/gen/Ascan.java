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

	public ApiResponse scanners(String policyid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("policyId", policyid);
		return api.callApi("ascan", "view", "scanners", map);
	}

	public ApiResponse policies() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "policies", map);
	}

	public ApiResponse optionExcludedParamList() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionExcludedParamList", map);
	}

	public ApiResponse optionThreadPerHost() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionThreadPerHost", map);
	}

	public ApiResponse optionHostPerScan() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionHostPerScan", map);
	}

	public ApiResponse optionMaxResultsToList() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionMaxResultsToList", map);
	}

	public ApiResponse optionDelayInMs() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionDelayInMs", map);
	}

	public ApiResponse optionHandleAntiCSRFTokens() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionHandleAntiCSRFTokens", map);
	}

	public ApiResponse optionDeleteRequestsOnShutdown() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionDeleteRequestsOnShutdown", map);
	}

	public ApiResponse optionAlertThreshold() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionAlertThreshold", map);
	}

	public ApiResponse optionAttackStrength() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionAttackStrength", map);
	}

	public ApiResponse optionTargetParamsInjectable() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionTargetParamsInjectable", map);
	}

	public ApiResponse optionTargetParamsEnabledRPC() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("ascan", "view", "optionTargetParamsEnabledRPC", map);
	}

	public ApiResponse scan(String apikey, String url, String recurse, String inscopeonly) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("url", url);
		map.put("recurse", recurse);
		map.put("inScopeOnly", inscopeonly);
		return api.callApi("ascan", "action", "scan", map);
	}

	public ApiResponse clearExcludedFromScan(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		return api.callApi("ascan", "action", "clearExcludedFromScan", map);
	}

	public ApiResponse excludeFromScan(String apikey, String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("regex", regex);
		return api.callApi("ascan", "action", "excludeFromScan", map);
	}

	public ApiResponse enableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		return api.callApi("ascan", "action", "enableAllScanners", map);
	}

	public ApiResponse disableAllScanners(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		return api.callApi("ascan", "action", "disableAllScanners", map);
	}

	public ApiResponse enableScanners(String apikey, String ids) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("ids", ids);
		return api.callApi("ascan", "action", "enableScanners", map);
	}

	public ApiResponse disableScanners(String apikey, String ids) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("ids", ids);
		return api.callApi("ascan", "action", "disableScanners", map);
	}

	public ApiResponse setEnabledPolicies(String apikey, String ids) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("ids", ids);
		return api.callApi("ascan", "action", "setEnabledPolicies", map);
	}

	public ApiResponse setPolicyAttackStrength(String apikey, String id, String attackstrength) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		map.put("attackStrength", attackstrength);
		return api.callApi("ascan", "action", "setPolicyAttackStrength", map);
	}

	public ApiResponse setPolicyAlertThreshold(String apikey, String id, String alertthreshold) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		map.put("alertThreshold", alertthreshold);
		return api.callApi("ascan", "action", "setPolicyAlertThreshold", map);
	}

	public ApiResponse setScannerAttackStrength(String apikey, String id, String attackstrength) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		map.put("attackStrength", attackstrength);
		return api.callApi("ascan", "action", "setScannerAttackStrength", map);
	}

	public ApiResponse setScannerAlertThreshold(String apikey, String id, String alertthreshold) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		map.put("alertThreshold", alertthreshold);
		return api.callApi("ascan", "action", "setScannerAlertThreshold", map);
	}

	public ApiResponse setOptionAlertThreshold(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("ascan", "action", "setOptionAlertThreshold", map);
	}

	public ApiResponse setOptionAttackStrength(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("String", string);
		return api.callApi("ascan", "action", "setOptionAttackStrength", map);
	}

	public ApiResponse setOptionThreadPerHost(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionThreadPerHost", map);
	}

	public ApiResponse setOptionHostPerScan(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionHostPerScan", map);
	}

	public ApiResponse setOptionMaxResultsToList(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionMaxResultsToList", map);
	}

	public ApiResponse setOptionDelayInMs(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionDelayInMs", map);
	}

	public ApiResponse setOptionHandleAntiCSRFTokens(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionHandleAntiCSRFTokens", map);
	}

	public ApiResponse setOptionDeleteRequestsOnShutdown(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("ascan", "action", "setOptionDeleteRequestsOnShutdown", map);
	}

	public ApiResponse setOptionTargetParamsInjectable(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionTargetParamsInjectable", map);
	}

	public ApiResponse setOptionTargetParamsEnabledRPC(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("ascan", "action", "setOptionTargetParamsEnabledRPC", map);
	}

}
