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
public class Script {

	private ClientApi api = null;

	public Script(ClientApi api) {
		this.api = api;
	}

	/**
	 * Lists the script engines available
	 */
	public ApiResponse listEngines() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("script", "view", "listEngines", map);
	}

	/**
	 * Lists the scripts available, with its engine, name, description, type and error state.
	 */
	public ApiResponse listScripts() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("script", "view", "listScripts", map);
	}

	/**
	 * Enables the script with the given name
	 */
	public ApiResponse enable(String apikey, String scriptname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scriptName", scriptname);
		return api.callApi("script", "action", "enable", map);
	}

	/**
	 * Disables the script with the given name
	 */
	public ApiResponse disable(String apikey, String scriptname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scriptName", scriptname);
		return api.callApi("script", "action", "disable", map);
	}

	/**
	 * Loads a script into ZAP from the given local file, with the given name, type and engine, optionally with a description
	 */
	public ApiResponse load(String apikey, String scriptname, String scripttype, String scriptengine, String filename, String scriptdescription) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scriptName", scriptname);
		map.put("scriptType", scripttype);
		map.put("scriptEngine", scriptengine);
		map.put("fileName", filename);
		if (scriptdescription != null) {
			map.put("scriptDescription", scriptdescription);
		}
		return api.callApi("script", "action", "load", map);
	}

	/**
	 * Removes the script with the given name
	 */
	public ApiResponse remove(String apikey, String scriptname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scriptName", scriptname);
		return api.callApi("script", "action", "remove", map);
	}

	/**
	 * Runs the stand alone script with the give name
	 */
	public ApiResponse runStandAloneScript(String apikey, String scriptname) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("scriptName", scriptname);
		return api.callApi("script", "action", "runStandAloneScript", map);
	}

}
