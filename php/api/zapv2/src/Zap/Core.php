<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 the ZAP development team
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
		$res = $this->zap->request($this->zap->base . 'core/view/alert/', array('id' => $id));
		return reset($res);
	}

	/**
	 * Gets the alerts raised by ZAP, optionally filtering by URL and paginating with 'start' position and 'count' of alerts
	 */
	public function alerts($baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'core/view/alerts/', array('baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	/**
	 * Gets the number of alerts, optionally filtering by URL
	 */
	public function numberOfAlerts($baseurl='') {
		$res = $this->zap->request($this->zap->base . 'core/view/numberOfAlerts/', array('baseurl' => $baseurl));
		return reset($res);
	}

	/**
	 * Gets the name of the hosts accessed through/by ZAP
	 */
	public function hosts() {
		$res = $this->zap->request($this->zap->base . 'core/view/hosts/');
		return reset($res);
	}

	/**
	 * Gets the sites accessed through/by ZAP (scheme and domain)
	 */
	public function sites() {
		$res = $this->zap->request($this->zap->base . 'core/view/sites/');
		return reset($res);
	}

	/**
	 * Gets the URLs accessed through/by ZAP
	 */
	public function urls() {
		$res = $this->zap->request($this->zap->base . 'core/view/urls/');
		return reset($res);
	}

	/**
	 * Gets the HTTP message with the given ID. Returns the ID, request/response headers and bodies, cookies and note.
	 */
	public function message($id) {
		$res = $this->zap->request($this->zap->base . 'core/view/message/', array('id' => $id));
		return reset($res);
	}

	/**
	 * Gets the HTTP messages sent by ZAP, request and response in HAR format, optionally filtered by URL and paginated with 'start' position and 'count' of messages
	 */
	public function messages($baseurl='', $start='', $count='') {
		$res = $this->zap->request($this->zap->base . 'core/view/messages/', array('baseurl' => $baseurl, 'start' => $start, 'count' => $count));
		return reset($res);
	}

	/**
	 * Gets the number of messages, optionally filtering by URL
	 */
	public function numberOfMessages($baseurl='') {
		$res = $this->zap->request($this->zap->base . 'core/view/numberOfMessages/', array('baseurl' => $baseurl));
		return reset($res);
	}

	/**
	 * Gets ZAP version
	 */
	public function version() {
		$res = $this->zap->request($this->zap->base . 'core/view/version/');
		return reset($res);
	}

	/**
	 * Gets the regular expressions, applied to URLs, to exclude from the Proxy
	 */
	public function excludedFromProxy() {
		$res = $this->zap->request($this->zap->base . 'core/view/excludedFromProxy/');
		return reset($res);
	}

	public function homeDirectory() {
		$res = $this->zap->request($this->zap->base . 'core/view/homeDirectory/');
		return reset($res);
	}

	public function optionHttpStateEnabled() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionHttpStateEnabled/');
		return reset($res);
	}

	public function optionUseProxyChain() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionUseProxyChain/');
		return reset($res);
	}

	public function optionProxyChainName() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyChainName/');
		return reset($res);
	}

	public function optionProxyChainPort() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyChainPort/');
		return reset($res);
	}

	public function optionProxyChainSkipName() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyChainSkipName/');
		return reset($res);
	}

	public function optionUseProxyChainAuth() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionUseProxyChainAuth/');
		return reset($res);
	}

	public function optionProxyChainUserName() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyChainUserName/');
		return reset($res);
	}

	public function optionProxyChainRealm() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyChainRealm/');
		return reset($res);
	}

	public function optionProxyChainPassword() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyChainPassword/');
		return reset($res);
	}

	public function optionProxyChainPrompt() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyChainPrompt/');
		return reset($res);
	}

	public function optionHttpState() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionHttpState/');
		return reset($res);
	}

	public function optionTimeoutInSecs() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionTimeoutInSecs/');
		return reset($res);
	}

	public function optionSingleCookieRequestHeader() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionSingleCookieRequestHeader/');
		return reset($res);
	}

	public function optionProxyExcludedDomains() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyExcludedDomains/');
		return reset($res);
	}

	public function optionProxyExcludedDomainsEnabled() {
		$res = $this->zap->request($this->zap->base . 'core/view/optionProxyExcludedDomainsEnabled/');
		return reset($res);
	}

	/**
	 * Shuts down ZAP
	 */
	public function shutdown($apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/shutdown/', array('apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Creates a new session, optionally overwriting existing files
	 */
	public function newSession($name='', $overwrite='', $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/newSession/', array('name' => $name, 'overwrite' => $overwrite, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Loads the session with the given name
	 */
	public function loadSession($name, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/loadSession/', array('name' => $name, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Saves the session with the name supplied, optionally overwriting existing files
	 */
	public function saveSession($name, $overwrite='', $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/saveSession/', array('name' => $name, 'overwrite' => $overwrite, 'apikey' => $apikey));
		return reset($res);
	}

	public function snapshotSession($apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/snapshotSession/', array('apikey' => $apikey));
		return reset($res);
	}

	public function clearExcludedFromProxy($apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/clearExcludedFromProxy/', array('apikey' => $apikey));
		return reset($res);
	}

	public function excludeFromProxy($regex, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/excludeFromProxy/', array('regex' => $regex, 'apikey' => $apikey));
		return reset($res);
	}

	public function setHomeDirectory($dir, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setHomeDirectory/', array('dir' => $dir, 'apikey' => $apikey));
		return reset($res);
	}

	public function generateRootCA($apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/generateRootCA/', array('apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Sends the HTTP request, optionally following redirections. Returns the request sent and response received and followed redirections, if any.
	 */
	public function sendRequest($request, $followredirects='', $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/sendRequest/', array('request' => $request, 'followRedirects' => $followredirects, 'apikey' => $apikey));
		return reset($res);
	}

	public function deleteAllAlerts($apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/deleteAllAlerts/', array('apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProxyChainName($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainName/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProxyChainRealm($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainRealm/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProxyChainUserName($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainUserName/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProxyChainPassword($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainPassword/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProxyChainSkipName($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainSkipName/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionHttpStateEnabled($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionHttpStateEnabled/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProxyChainPort($integer, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainPort/', array('Integer' => $integer, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProxyChainPrompt($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainPrompt/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionTimeoutInSecs($integer, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionTimeoutInSecs/', array('Integer' => $integer, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionUseProxyChain($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionUseProxyChain/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionUseProxyChainAuth($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionUseProxyChainAuth/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionSingleCookieRequestHeader($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'core/action/setOptionSingleCookieRequestHeader/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function proxypac($apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/proxy.pac/', array('apikey' => $apikey));
	}

	public function rootcert($apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/rootcert/', array('apikey' => $apikey));
	}

	public function setproxy($proxy, $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/setproxy/', array('proxy' => $proxy, 'apikey' => $apikey));
	}

	/**
	 * Generates a report in XML format
	 */
	public function xmlreport($apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/xmlreport/', array('apikey' => $apikey));
	}

	/**
	 * Generates a report in HTML format
	 */
	public function htmlreport($apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/htmlreport/', array('apikey' => $apikey));
	}

	/**
	 * Gets the message with the given ID in HAR format
	 */
	public function messageHar($id, $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/messageHar/', array('id' => $id, 'apikey' => $apikey));
	}

	/**
	 * Gets the HTTP messages sent through/by ZAP, in HAR format, optionally filtered by URL and paginated with 'start' position and 'count' of messages
	 */
	public function messagesHar($baseurl='', $start='', $count='', $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/messagesHar/', array('baseurl' => $baseurl, 'start' => $start, 'count' => $count, 'apikey' => $apikey));
	}

	/**
	 * Sends the first HAR request entry, optionally following redirections. Returns, in HAR format, the request sent and response received and followed redirections, if any.
	 */
	public function sendHarRequest($request, $followredirects='', $apikey='') {
		return $this->zap->requestother($this->zap->baseother . 'core/other/sendHarRequest/', array('request' => $request, 'followRedirects' => $followredirects, 'apikey' => $apikey));
	}

}
