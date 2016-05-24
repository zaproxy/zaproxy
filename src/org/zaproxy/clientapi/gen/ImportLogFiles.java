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
public class ImportLogFiles {

	private ClientApi api = null;

	public ImportLogFiles(ClientApi api) {
		this.api = api;
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ImportZAPLogFromFile(String filepath) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("FilePath", filepath);
		return api.callApi("importLogFiles", "view", "ImportZAPLogFromFile", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ImportModSecurityLogFromFile(String filepath) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("FilePath", filepath);
		return api.callApi("importLogFiles", "view", "ImportModSecurityLogFromFile", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse ImportZAPHttpRequestResponsePair(String httprequest, String httpresponse) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("HTTPRequest", httprequest);
		map.put("HTTPResponse", httpresponse);
		return api.callApi("importLogFiles", "view", "ImportZAPHttpRequestResponsePair", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public ApiResponse PostModSecurityAuditEvent(String apikey, String auditeventstring) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		if (auditeventstring != null) {
			map.put("AuditEventString", auditeventstring);
		}
		return api.callApi("importLogFiles", "action", "PostModSecurityAuditEvent", map);
	}

	/**
	 * This component is optional and therefore the API will only work if it is installed
	 */
	public byte[] OtherPostModSecurityAuditEvent(String apikey, String auditeventstring) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("AuditEventString", auditeventstring);
		return api.callApiOther("importLogFiles", "other", "OtherPostModSecurityAuditEvent", map);
	}

}
