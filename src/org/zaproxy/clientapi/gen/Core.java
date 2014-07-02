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
public class Core {

	private ClientApi api = null;

	public Core(ClientApi api) {
		this.api = api;
	}

	public ApiResponse alert(String id) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		return api.callApi("core", "view", "alert", map);
	}

	public ApiResponse alerts(String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		map.put("start", start);
		map.put("count", count);
		return api.callApi("core", "view", "alerts", map);
	}

	public ApiResponse numberOfAlerts(String baseurl) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		return api.callApi("core", "view", "numberOfAlerts", map);
	}

	public ApiResponse hosts() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "hosts", map);
	}

	public ApiResponse sites() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "sites", map);
	}

	public ApiResponse urls() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "urls", map);
	}

	public ApiResponse message(String id) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		return api.callApi("core", "view", "message", map);
	}

	public ApiResponse messages(String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		map.put("start", start);
		map.put("count", count);
		return api.callApi("core", "view", "messages", map);
	}

	public ApiResponse numberOfMessages(String baseurl) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("baseurl", baseurl);
		return api.callApi("core", "view", "numberOfMessages", map);
	}

	public ApiResponse version() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "version", map);
	}

	public ApiResponse excludedFromProxy() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "excludedFromProxy", map);
	}

	public ApiResponse homeDirectory() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "homeDirectory", map);
	}

	public ApiResponse optionHttpStateEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionHttpStateEnabled", map);
	}

	public ApiResponse optionUseProxyChain() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionUseProxyChain", map);
	}

	public ApiResponse optionProxyChainName() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainName", map);
	}

	public ApiResponse optionProxyChainPort() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainPort", map);
	}

	public ApiResponse optionProxyChainSkipName() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainSkipName", map);
	}

	public ApiResponse optionUseProxyChainAuth() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionUseProxyChainAuth", map);
	}

	public ApiResponse optionProxyChainUserName() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainUserName", map);
	}

	public ApiResponse optionProxyChainRealm() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainRealm", map);
	}

	public ApiResponse optionProxyChainPassword() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainPassword", map);
	}

	public ApiResponse optionProxyChainPrompt() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainPrompt", map);
	}

	public ApiResponse optionListAuth() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionListAuth", map);
	}

	public ApiResponse optionListAuthEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionListAuthEnabled", map);
	}

	public ApiResponse optionHttpState() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionHttpState", map);
	}

	public ApiResponse optionTimeoutInSecs() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionTimeoutInSecs", map);
	}

	public ApiResponse optionSingleCookieRequestHeader() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionSingleCookieRequestHeader", map);
	}

	public ApiResponse optionProxyExcludedDomains() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyExcludedDomains", map);
	}

	public ApiResponse optionProxyExcludedDomainsEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyExcludedDomainsEnabled", map);
	}

	/**
	 * Shuts down ZAP
	 */
	public ApiResponse shutdown(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("core", "action", "shutdown", map);
	}

	public ApiResponse newSession(String apikey, String name, String overwrite) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("name", name);
		map.put("overwrite", overwrite);
		return api.callApi("core", "action", "newSession", map);
	}

	public ApiResponse loadSession(String apikey, String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("name", name);
		return api.callApi("core", "action", "loadSession", map);
	}

	public ApiResponse saveSession(String apikey, String name, String overwrite) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("name", name);
		map.put("overwrite", overwrite);
		return api.callApi("core", "action", "saveSession", map);
	}

	public ApiResponse snapshotSession(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("core", "action", "snapshotSession", map);
	}

	public ApiResponse clearExcludedFromProxy(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("core", "action", "clearExcludedFromProxy", map);
	}

	public ApiResponse excludeFromProxy(String apikey, String regex) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("regex", regex);
		return api.callApi("core", "action", "excludeFromProxy", map);
	}

	public ApiResponse setHomeDirectory(String apikey, String dir) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("dir", dir);
		return api.callApi("core", "action", "setHomeDirectory", map);
	}

	public ApiResponse generateRootCA(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("core", "action", "generateRootCA", map);
	}

	public ApiResponse sendRequest(String apikey, String request, String followredirects) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("request", request);
		map.put("followRedirects", followredirects);
		return api.callApi("core", "action", "sendRequest", map);
	}

	public ApiResponse deleteAllAlerts(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("core", "action", "deleteAllAlerts", map);
	}

	public ApiResponse setOptionProxyChainName(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionProxyChainName", map);
	}

	public ApiResponse setOptionProxyChainRealm(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionProxyChainRealm", map);
	}

	public ApiResponse setOptionProxyChainUserName(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionProxyChainUserName", map);
	}

	public ApiResponse setOptionProxyChainPassword(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionProxyChainPassword", map);
	}

	public ApiResponse setOptionProxyChainSkipName(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionProxyChainSkipName", map);
	}

	public ApiResponse setOptionHttpStateEnabled(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("core", "action", "setOptionHttpStateEnabled", map);
	}

	public ApiResponse setOptionProxyChainPort(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("core", "action", "setOptionProxyChainPort", map);
	}

	public ApiResponse setOptionProxyChainPrompt(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("core", "action", "setOptionProxyChainPrompt", map);
	}

	public ApiResponse setOptionTimeoutInSecs(String apikey, int i) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Integer", Integer.toString(i));
		return api.callApi("core", "action", "setOptionTimeoutInSecs", map);
	}

	public ApiResponse setOptionUseProxyChain(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("core", "action", "setOptionUseProxyChain", map);
	}

	public ApiResponse setOptionUseProxyChainAuth(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("core", "action", "setOptionUseProxyChainAuth", map);
	}

	public ApiResponse setOptionSingleCookieRequestHeader(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("core", "action", "setOptionSingleCookieRequestHeader", map);
	}

	public byte[] proxypac(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("core", "other", "proxy.pac", map);
	}

	public byte[] rootcert(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("core", "other", "rootcert", map);
	}

	public byte[] setproxy(String apikey, String proxy) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("proxy", proxy);
		return api.callApiOther("core", "other", "setproxy", map);
	}

	public byte[] xmlreport(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("core", "other", "xmlreport", map);
	}

	public byte[] messagesHar(String apikey, String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("baseurl", baseurl);
		map.put("start", start);
		map.put("count", count);
		return api.callApiOther("core", "other", "messagesHar", map);
	}

	public byte[] sendHarRequest(String apikey, String request, String followredirects) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("request", request);
		map.put("followRedirects", followredirects);
		return api.callApiOther("core", "other", "sendHarRequest", map);
	}

}
