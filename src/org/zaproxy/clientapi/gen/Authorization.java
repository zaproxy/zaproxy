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
public class Authorization {

	private ClientApi api = null;

	public Authorization(ClientApi api) {
		this.api = api;
	}

	/**
	 * Obtains all the configuration of the authorization detection method that is currently set for a context.
	 */
	public ApiResponse getAuthorizationDetectionMethod(String contextid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("contextId", contextid);
		return api.callApi("authorization", "view", "getAuthorizationDetectionMethod", map);
	}

	/**
	 * Sets the authorization detection method for a context as one that identifies un-authorized messages based on: the message's status code or a regex pattern in the response's header or body. Also, whether all conditions must match or just some can be specified via the logicalOperator parameter, which accepts two values: "AND" (default), "OR".  
	 */
	public ApiResponse setBasicAuthorizationDetectionMethod(String apikey, String contextid, String headerregex, String bodyregex, String statuscode, String logicaloperator) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("contextId", contextid);
		if (headerregex != null) {
			map.put("headerRegex", headerregex);
		}
		if (bodyregex != null) {
			map.put("bodyRegex", bodyregex);
		}
		if (statuscode != null) {
			map.put("statusCode", statuscode);
		}
		if (logicaloperator != null) {
			map.put("logicalOperator", logicaloperator);
		}
		return api.callApi("authorization", "action", "setBasicAuthorizationDetectionMethod", map);
	}

}
