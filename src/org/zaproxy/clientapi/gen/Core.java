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
public class Core {

	private ClientApi api = null;

	public Core(ClientApi api) {
		this.api = api;
	}

	/**
	 * Gets the alert with the given ID, the corresponding HTTP message can be obtained with the 'messageId' field and 'message' API method
	 */
	public ApiResponse alert(String id) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		return api.callApi("core", "view", "alert", map);
	}

	/**
	 * Gets the alerts raised by ZAP, optionally filtering by URL and paginating with 'start' position and 'count' of alerts
	 */
	public ApiResponse alerts(String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (baseurl != null) {
			map.put("baseurl", baseurl);
		}
		if (start != null) {
			map.put("start", start);
		}
		if (count != null) {
			map.put("count", count);
		}
		return api.callApi("core", "view", "alerts", map);
	}

	/**
	 * Gets the number of alerts, optionally filtering by URL
	 */
	public ApiResponse numberOfAlerts(String baseurl) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (baseurl != null) {
			map.put("baseurl", baseurl);
		}
		return api.callApi("core", "view", "numberOfAlerts", map);
	}

	/**
	 * Gets the name of the hosts accessed through/by ZAP
	 */
	public ApiResponse hosts() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "hosts", map);
	}

	/**
	 * Gets the sites accessed through/by ZAP (scheme and domain)
	 */
	public ApiResponse sites() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "sites", map);
	}

	/**
	 * Gets the URLs accessed through/by ZAP
	 */
	public ApiResponse urls() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "urls", map);
	}

	/**
	 * Gets the HTTP message with the given ID. Returns the ID, request/response headers and bodies, cookies and note.
	 */
	public ApiResponse message(String id) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		map.put("id", id);
		return api.callApi("core", "view", "message", map);
	}

	/**
	 * Gets the HTTP messages sent by ZAP, request and response, optionally filtered by URL and paginated with 'start' position and 'count' of messages
	 */
	public ApiResponse messages(String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (baseurl != null) {
			map.put("baseurl", baseurl);
		}
		if (start != null) {
			map.put("start", start);
		}
		if (count != null) {
			map.put("count", count);
		}
		return api.callApi("core", "view", "messages", map);
	}

	/**
	 * Gets the number of messages, optionally filtering by URL
	 */
	public ApiResponse numberOfMessages(String baseurl) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (baseurl != null) {
			map.put("baseurl", baseurl);
		}
		return api.callApi("core", "view", "numberOfMessages", map);
	}

	/**
	 * Gets the mode
	 */
	public ApiResponse mode() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "mode", map);
	}

	/**
	 * Gets ZAP version
	 */
	public ApiResponse version() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "version", map);
	}

	/**
	 * Gets the regular expressions, applied to URLs, to exclude from the Proxy
	 */
	public ApiResponse excludedFromProxy() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "excludedFromProxy", map);
	}

	public ApiResponse homeDirectory() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "homeDirectory", map);
	}

	public ApiResponse optionDefaultUserAgent() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionDefaultUserAgent", map);
	}

	public ApiResponse optionHttpState() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionHttpState", map);
	}

	public ApiResponse optionProxyChainName() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainName", map);
	}

	public ApiResponse optionProxyChainPassword() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainPassword", map);
	}

	public ApiResponse optionProxyChainPort() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainPort", map);
	}

	public ApiResponse optionProxyChainRealm() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainRealm", map);
	}

	public ApiResponse optionProxyChainSkipName() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainSkipName", map);
	}

	public ApiResponse optionProxyChainUserName() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainUserName", map);
	}

	public ApiResponse optionProxyExcludedDomains() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyExcludedDomains", map);
	}

	public ApiResponse optionProxyExcludedDomainsEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyExcludedDomainsEnabled", map);
	}

	public ApiResponse optionTimeoutInSecs() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionTimeoutInSecs", map);
	}

	public ApiResponse optionHttpStateEnabled() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionHttpStateEnabled", map);
	}

	public ApiResponse optionProxyChainPrompt() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionProxyChainPrompt", map);
	}

	public ApiResponse optionSingleCookieRequestHeader() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionSingleCookieRequestHeader", map);
	}

	public ApiResponse optionUseProxyChain() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionUseProxyChain", map);
	}

	public ApiResponse optionUseProxyChainAuth() throws ClientApiException {
		Map<String, String> map = null;
		return api.callApi("core", "view", "optionUseProxyChainAuth", map);
	}

	/**
	 * Convenient and simple action to access a URL, optionally following redirections. Returns the request sent and response received and followed redirections, if any. Other actions are available which offer more control on what is sent, like, 'sendRequest' or 'sendHarRequest'.
	 */
	public ApiResponse accessUrl(String apikey, String url, String followredirects) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		if (followredirects != null) {
			map.put("followRedirects", followredirects);
		}
		return api.callApi("core", "action", "accessUrl", map);
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

	/**
	 * Creates a new session, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
	 */
	public ApiResponse newSession(String apikey, String name, String overwrite) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		if (name != null) {
			map.put("name", name);
		}
		if (overwrite != null) {
			map.put("overwrite", overwrite);
		}
		return api.callApi("core", "action", "newSession", map);
	}

	/**
	 * Loads the session with the given name. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
	 */
	public ApiResponse loadSession(String apikey, String name) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("name", name);
		return api.callApi("core", "action", "loadSession", map);
	}

	/**
	 * Saves the session with the name supplied, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
	 */
	public ApiResponse saveSession(String apikey, String name, String overwrite) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("name", name);
		if (overwrite != null) {
			map.put("overwrite", overwrite);
		}
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

	/**
	 * Sets the mode, which may be one of [safe, protect, standard, attack]
	 */
	public ApiResponse setMode(String apikey, String mode) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("mode", mode);
		return api.callApi("core", "action", "setMode", map);
	}

	public ApiResponse generateRootCA(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("core", "action", "generateRootCA", map);
	}

	/**
	 * Sends the HTTP request, optionally following redirections. Returns the request sent and response received and followed redirections, if any.
	 */
	public ApiResponse sendRequest(String apikey, String request, String followredirects) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("request", request);
		if (followredirects != null) {
			map.put("followRedirects", followredirects);
		}
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

	public ApiResponse runGarbageCollection(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApi("core", "action", "runGarbageCollection", map);
	}

	/**
	 * Deletes the site node found in the Sites Tree on the basis of the URL, HTTP method, and post data (if applicable and specified). 
	 */
	public ApiResponse deleteSiteNode(String apikey, String url, String method, String postdata) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("url", url);
		if (method != null) {
			map.put("method", method);
		}
		if (postdata != null) {
			map.put("postData", postdata);
		}
		return api.callApi("core", "action", "deleteSiteNode", map);
	}

	public ApiResponse setOptionDefaultUserAgent(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionDefaultUserAgent", map);
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

	public ApiResponse setOptionProxyChainPassword(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionProxyChainPassword", map);
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

	public ApiResponse setOptionProxyChainSkipName(String apikey, String string) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("String", string);
		return api.callApi("core", "action", "setOptionProxyChainSkipName", map);
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

	public ApiResponse setOptionSingleCookieRequestHeader(String apikey, boolean bool) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("Boolean", Boolean.toString(bool));
		return api.callApi("core", "action", "setOptionSingleCookieRequestHeader", map);
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

	/**
	 * Generates a report in XML format
	 */
	public byte[] xmlreport(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("core", "other", "xmlreport", map);
	}

	/**
	 * Generates a report in HTML format
	 */
	public byte[] htmlreport(String apikey) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		return api.callApiOther("core", "other", "htmlreport", map);
	}

	/**
	 * Gets the message with the given ID in HAR format
	 */
	public byte[] messageHar(String apikey, String id) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("id", id);
		return api.callApiOther("core", "other", "messageHar", map);
	}

	/**
	 * Gets the HTTP messages sent through/by ZAP, in HAR format, optionally filtered by URL and paginated with 'start' position and 'count' of messages
	 */
	public byte[] messagesHar(String apikey, String baseurl, String start, String count) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		if (baseurl != null) {
			map.put("baseurl", baseurl);
		}
		if (start != null) {
			map.put("start", start);
		}
		if (count != null) {
			map.put("count", count);
		}
		return api.callApiOther("core", "other", "messagesHar", map);
	}

	/**
	 * Sends the first HAR request entry, optionally following redirections. Returns, in HAR format, the request sent and response received and followed redirections, if any.
	 */
	public byte[] sendHarRequest(String apikey, String request, String followredirects) throws ClientApiException {
		Map<String, String> map = null;
		map = new HashMap<String, String>();
		if (apikey != null) {
			map.put("apikey", apikey);
		}
		map.put("request", request);
		if (followredirects != null) {
			map.put("followRedirects", followredirects);
		}
		return api.callApiOther("core", "other", "sendHarRequest", map);
	}

}
