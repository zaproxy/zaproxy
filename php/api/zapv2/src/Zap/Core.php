<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
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


namespace Zap;


/**
 * This file was automatically generated.
 */
class Core {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * Gets the alert with the given ID, the corresponding HTTP message can be obtained with the 'messageId' field and 'message' API method
	 */
	public function alert($id) {
		return $this->zap->request($this->zap->base . 'core/view/alert/', array('id' => $id))->{'alert'};
	}

	/**
	 * Gets the alerts raised by ZAP, optionally filtering by URL and paginating with 'start' position and 'count' of alerts
	 */
	public function alerts($baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array();
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'core/view/alerts/', $params)->{'alerts'};
	}

	/**
	 * Gets the number of alerts, optionally filtering by URL
	 */
	public function numberOfAlerts($baseurl=NULL) {
		$params = array();
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		return $this->zap->request($this->zap->base . 'core/view/numberOfAlerts/', $params)->{'numberOfAlerts'};
	}

	/**
	 * Gets the name of the hosts accessed through/by ZAP
	 */
	public function hosts() {
		return $this->zap->request($this->zap->base . 'core/view/hosts/')->{'hosts'};
	}

	/**
	 * Gets the sites accessed through/by ZAP (scheme and domain)
	 */
	public function sites() {
		return $this->zap->request($this->zap->base . 'core/view/sites/')->{'sites'};
	}

	/**
	 * Gets the URLs accessed through/by ZAP
	 */
	public function urls() {
		return $this->zap->request($this->zap->base . 'core/view/urls/')->{'urls'};
	}

	/**
	 * Gets the HTTP message with the given ID. Returns the ID, request/response headers and bodies, cookies and note.
	 */
	public function message($id) {
		return $this->zap->request($this->zap->base . 'core/view/message/', array('id' => $id))->{'message'};
	}

	/**
	 * Gets the HTTP messages sent by ZAP, request and response, optionally filtered by URL and paginated with 'start' position and 'count' of messages
	 */
	public function messages($baseurl=NULL, $start=NULL, $count=NULL) {
		$params = array();
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->request($this->zap->base . 'core/view/messages/', $params)->{'messages'};
	}

	/**
	 * Gets the number of messages, optionally filtering by URL
	 */
	public function numberOfMessages($baseurl=NULL) {
		$params = array();
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		return $this->zap->request($this->zap->base . 'core/view/numberOfMessages/', $params)->{'numberOfMessages'};
	}

	/**
	 * Gets the mode
	 */
	public function mode() {
		return $this->zap->request($this->zap->base . 'core/view/mode/')->{'mode'};
	}

	/**
	 * Gets ZAP version
	 */
	public function version() {
		return $this->zap->request($this->zap->base . 'core/view/version/')->{'version'};
	}

	/**
	 * Gets the regular expressions, applied to URLs, to exclude from the Proxy
	 */
	public function excludedFromProxy() {
		return $this->zap->request($this->zap->base . 'core/view/excludedFromProxy/')->{'excludedFromProxy'};
	}

	public function homeDirectory() {
		return $this->zap->request($this->zap->base . 'core/view/homeDirectory/')->{'homeDirectory'};
	}

	public function optionDefaultUserAgent() {
		return $this->zap->request($this->zap->base . 'core/view/optionDefaultUserAgent/')->{'DefaultUserAgent'};
	}

	public function optionHttpState() {
		return $this->zap->request($this->zap->base . 'core/view/optionHttpState/')->{'HttpState'};
	}

	public function optionProxyChainName() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainName/')->{'ProxyChainName'};
	}

	public function optionProxyChainPassword() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainPassword/')->{'ProxyChainPassword'};
	}

	public function optionProxyChainPort() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainPort/')->{'ProxyChainPort'};
	}

	public function optionProxyChainRealm() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainRealm/')->{'ProxyChainRealm'};
	}

	public function optionProxyChainSkipName() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainSkipName/')->{'ProxyChainSkipName'};
	}

	public function optionProxyChainUserName() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainUserName/')->{'ProxyChainUserName'};
	}

	public function optionProxyExcludedDomains() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyExcludedDomains/')->{'ProxyExcludedDomains'};
	}

	public function optionProxyExcludedDomainsEnabled() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyExcludedDomainsEnabled/')->{'ProxyExcludedDomainsEnabled'};
	}

	public function optionTimeoutInSecs() {
		return $this->zap->request($this->zap->base . 'core/view/optionTimeoutInSecs/')->{'TimeoutInSecs'};
	}

	public function optionHttpStateEnabled() {
		return $this->zap->request($this->zap->base . 'core/view/optionHttpStateEnabled/')->{'HttpStateEnabled'};
	}

	public function optionProxyChainPrompt() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainPrompt/')->{'ProxyChainPrompt'};
	}

	public function optionSingleCookieRequestHeader() {
		return $this->zap->request($this->zap->base . 'core/view/optionSingleCookieRequestHeader/')->{'SingleCookieRequestHeader'};
	}

	public function optionUseProxyChain() {
		return $this->zap->request($this->zap->base . 'core/view/optionUseProxyChain/')->{'UseProxyChain'};
	}

	public function optionUseProxyChainAuth() {
		return $this->zap->request($this->zap->base . 'core/view/optionUseProxyChainAuth/')->{'UseProxyChainAuth'};
	}

	/**
	 * Convenient and simple action to access a URL, optionally following redirections. Returns the request sent and response received and followed redirections, if any. Other actions are available which offer more control on what is sent, like, 'sendRequest' or 'sendHarRequest'.
	 */
	public function accessUrl($url, $followredirects=NULL, $apikey='') {
		$params = array('url' => $url, 'apikey' => $apikey);
		if ($followredirects !== NULL) {
			$params['followRedirects'] = $followredirects;
		}
		return $this->zap->request($this->zap->base . 'core/action/accessUrl/', $params);
	}

	/**
	 * Shuts down ZAP
	 */
	public function shutdown($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/shutdown/', array('apikey' => $apikey));
	}

	/**
	 * Creates a new session, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
	 */
	public function newSession($name=NULL, $overwrite=NULL, $apikey='') {
		$params = array('apikey' => $apikey);
		if ($name !== NULL) {
			$params['name'] = $name;
		}
		if ($overwrite !== NULL) {
			$params['overwrite'] = $overwrite;
		}
		return $this->zap->request($this->zap->base . 'core/action/newSession/', $params);
	}

	/**
	 * Loads the session with the given name. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
	 */
	public function loadSession($name, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/loadSession/', array('name' => $name, 'apikey' => $apikey));
	}

	/**
	 * Saves the session with the name supplied, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
	 */
	public function saveSession($name, $overwrite=NULL, $apikey='') {
		$params = array('name' => $name, 'apikey' => $apikey);
		if ($overwrite !== NULL) {
			$params['overwrite'] = $overwrite;
		}
		return $this->zap->request($this->zap->base . 'core/action/saveSession/', $params);
	}

	public function snapshotSession($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/snapshotSession/', array('apikey' => $apikey));
	}

	public function clearExcludedFromProxy($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/clearExcludedFromProxy/', array('apikey' => $apikey));
	}

	public function excludeFromProxy($regex, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/excludeFromProxy/', array('regex' => $regex, 'apikey' => $apikey));
	}

	public function setHomeDirectory($dir, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setHomeDirectory/', array('dir' => $dir, 'apikey' => $apikey));
	}

	/**
	 * Sets the mode, which may be one of [safe, protect, standard, attack]
	 */
	public function setMode($mode, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setMode/', array('mode' => $mode, 'apikey' => $apikey));
	}

	public function generateRootCA($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/generateRootCA/', array('apikey' => $apikey));
	}

	/**
	 * Sends the HTTP request, optionally following redirections. Returns the request sent and response received and followed redirections, if any.
	 */
	public function sendRequest($request, $followredirects=NULL, $apikey='') {
		$params = array('request' => $request, 'apikey' => $apikey);
		if ($followredirects !== NULL) {
			$params['followRedirects'] = $followredirects;
		}
		return $this->zap->request($this->zap->base . 'core/action/sendRequest/', $params);
	}

	public function deleteAllAlerts($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/deleteAllAlerts/', array('apikey' => $apikey));
	}

	public function runGarbageCollection($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/runGarbageCollection/', array('apikey' => $apikey));
	}

	/**
	 * Deletes the site node found in the Sites Tree on the basis of the URL, HTTP method, and post data (if applicable and specified). 
	 */
	public function deleteSiteNode($url, $method=NULL, $postdata=NULL, $apikey='') {
		$params = array('url' => $url, 'apikey' => $apikey);
		if ($method !== NULL) {
			$params['method'] = $method;
		}
		if ($postdata !== NULL) {
			$params['postData'] = $postdata;
		}
		return $this->zap->request($this->zap->base . 'core/action/deleteSiteNode/', $params);
	}

	public function setOptionDefaultUserAgent($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionDefaultUserAgent/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainName($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainName/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainPassword($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainPassword/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainRealm($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainRealm/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainSkipName($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainSkipName/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainUserName($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainUserName/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionHttpStateEnabled($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionHttpStateEnabled/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionProxyChainPort($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainPort/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionProxyChainPrompt($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainPrompt/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionSingleCookieRequestHeader($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionSingleCookieRequestHeader/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionTimeoutInSecs($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionTimeoutInSecs/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionUseProxyChain($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionUseProxyChain/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionUseProxyChainAuth($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionUseProxyChainAuth/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function proxypac($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'core/other/proxy.pac/', array('apikey' => $apikey));
	}

	public function rootcert($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'core/other/rootcert/', array('apikey' => $apikey));
	}

	public function setproxy($proxy, $apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'core/other/setproxy/', array('proxy' => $proxy, 'apikey' => $apikey));
	}

	/**
	 * Generates a report in XML format
	 */
	public function xmlreport($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'core/other/xmlreport/', array('apikey' => $apikey));
	}

	/**
	 * Generates a report in HTML format
	 */
	public function htmlreport($apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'core/other/htmlreport/', array('apikey' => $apikey));
	}

	/**
	 * Gets the message with the given ID in HAR format
	 */
	public function messageHar($id, $apikey='') {
		return $this->zap->requestother($this->zap->base_other . 'core/other/messageHar/', array('id' => $id, 'apikey' => $apikey));
	}

	/**
	 * Gets the HTTP messages sent through/by ZAP, in HAR format, optionally filtered by URL and paginated with 'start' position and 'count' of messages
	 */
	public function messagesHar($baseurl=NULL, $start=NULL, $count=NULL, $apikey='') {
		$params = array('apikey' => $apikey);
		if ($baseurl !== NULL) {
			$params['baseurl'] = $baseurl;
		}
		if ($start !== NULL) {
			$params['start'] = $start;
		}
		if ($count !== NULL) {
			$params['count'] = $count;
		}
		return $this->zap->requestother($this->zap->base_other . 'core/other/messagesHar/', $params);
	}

	/**
	 * Sends the first HAR request entry, optionally following redirections. Returns, in HAR format, the request sent and response received and followed redirections, if any.
	 */
	public function sendHarRequest($request, $followredirects=NULL, $apikey='') {
		$params = array('request' => $request, 'apikey' => $apikey);
		if ($followredirects !== NULL) {
			$params['followRedirects'] = $followredirects;
		}
		return $this->zap->requestother($this->zap->base_other . 'core/other/sendHarRequest/', $params);
	}

}
