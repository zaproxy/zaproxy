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
public class Acsrf {

	private ClientApi api = null;

	public Acsrf(ClientApi api) {
		this.api = api;
	}

	/**
	 * Lists the names of all anti CSRF tokens
	 */
	public ApiResponse optionTokensNames() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("acsrf", "view", "optionTokensNames", map);
	}

	/**
	 * Adds an anti CSRF token with the given name, enabled by default
	 */
	public ApiResponse addOptionToken(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("acsrf", "action", "addOptionToken", map);
	}

	/**
	 * Removes the anti CSRF token with the given name
	 */
	public ApiResponse removeOptionToken(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("acsrf", "action", "removeOptionToken", map);
	}

	/**
	 * Generate a form for testing lack of anti CSRF tokens - typically invoked via ZAP
	 */
	public byte[] genForm(String apikey, String hrefid) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("hrefId", hrefid);
		return api.callApiOther("acsrf", "other", "genForm", map);
	}

}
