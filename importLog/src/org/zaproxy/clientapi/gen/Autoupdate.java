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
public class Autoupdate {

	private ClientApi api = null;

	public Autoupdate(ClientApi api) {
		this.api = api;
	}

	public ApiResponse latestVersionNumber() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "latestVersionNumber", map);
	}

	public ApiResponse isLatestVersion() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "isLatestVersion", map);
	}

	public ApiResponse optionCheckOnStartUnset() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionCheckOnStartUnset", map);
	}

	public ApiResponse optionCheckOnStart() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionCheckOnStart", map);
	}

	public ApiResponse optionDownloadNewRelease() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionDownloadNewRelease", map);
	}

	public ApiResponse optionCheckAddonUpdates() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionCheckAddonUpdates", map);
	}

	public ApiResponse optionInstallAddonUpdates() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionInstallAddonUpdates", map);
	}

	public ApiResponse optionInstallScannerRules() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionInstallScannerRules", map);
	}

	public ApiResponse optionReportReleaseAddons() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionReportReleaseAddons", map);
	}

	public ApiResponse optionReportBetaAddons() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionReportBetaAddons", map);
	}

	public ApiResponse optionReportAlphaAddons() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "view", "optionReportAlphaAddons", map);
	}

	public ApiResponse downloadLatestRelease() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("autoupdate", "action", "downloadLatestRelease", map);
	}

	public ApiResponse setOptionCheckOnStart(int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Integer", Integer.toString(i));
		return api.callApi("autoupdate", "action", "setOptionCheckOnStart", map);
	}

	public ApiResponse setOptionDownloadNewRelease(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("autoupdate", "action", "setOptionDownloadNewRelease", map);
	}

	public ApiResponse setOptionCheckAddonUpdates(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("autoupdate", "action", "setOptionCheckAddonUpdates", map);
	}

	public ApiResponse setOptionInstallAddonUpdates(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("autoupdate", "action", "setOptionInstallAddonUpdates", map);
	}

	public ApiResponse setOptionInstallScannerRules(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("autoupdate", "action", "setOptionInstallScannerRules", map);
	}

	public ApiResponse setOptionReportReleaseAddons(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("autoupdate", "action", "setOptionReportReleaseAddons", map);
	}

	public ApiResponse setOptionReportBetaAddons(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("autoupdate", "action", "setOptionReportBetaAddons", map);
	}

	public ApiResponse setOptionReportAlphaAddons(boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("autoupdate", "action", "setOptionReportAlphaAddons", map);
	}

}
