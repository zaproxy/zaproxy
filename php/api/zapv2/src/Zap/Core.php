<?php
/**
 * Zed Attack Proxy (ZAP) and its related class files.
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
	public function alerts($baseurl='', $start='', $count='') {
		return $this->zap->request($this->zap->base . 'core/view/alerts/', array('baseurl' => $baseurl, 'start' => $start, 'count' => $count))->{'alerts'};
	}

	/**
	 * Gets the number of alerts, optionally filtering by URL
	 */
	public function numberOfAlerts($baseurl='') {
		return $this->zap->request($this->zap->base . 'core/view/numberOfAlerts/', array('baseurl' => $baseurl))->{'numberOfAlerts'};
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
	 * Gets the HTTP messages sent by ZAP, request and response in HAR format, optionally filtered by URL and paginated with 'start' position and 'count' of messages
	 */
	public function messages($baseurl='', $start='', $count='') {
		return $this->zap->request($this->zap->base . 'core/view/messages/', array('baseurl' => $baseurl, 'start' => $start, 'count' => $count))->{'messages'};
	}

	/**
	 * Gets the number of messages, optionally filtering by URL
	 */
	public function numberOfMessages($baseurl='') {
		return $this->zap->request($this->zap->base . 'core/view/numberOfMessages/', array('baseurl' => $baseurl))->{'numberOfMessages'};
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

	public function optionHttpStateEnabled() {
		return $this->zap->request($this->zap->base . 'core/view/optionHttpStateEnabled/')->{'HttpStateEnabled'};
	}

	public function optionUseProxyChain() {
		return $this->zap->request($this->zap->base . 'core/view/optionUseProxyChain/')->{'UseProxyChain'};
	}

	public function optionProxyChainName() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainName/')->{'ProxyChainName'};
	}

	public function optionProxyChainPort() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainPort/')->{'ProxyChainPort'};
	}

	public function optionProxyChainSkipName() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainSkipName/')->{'ProxyChainSkipName'};
	}

	public function optionUseProxyChainAuth() {
		return $this->zap->request($this->zap->base . 'core/view/optionUseProxyChainAuth/')->{'UseProxyChainAuth'};
	}

	public function optionProxyChainUserName() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainUserName/')->{'ProxyChainUserName'};
	}

	public function optionProxyChainRealm() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainRealm/')->{'ProxyChainRealm'};
	}

	public function optionProxyChainPassword() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainPassword/')->{'ProxyChainPassword'};
	}

	public function optionProxyChainPrompt() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyChainPrompt/')->{'ProxyChainPrompt'};
	}

	public function optionHttpState() {
		return $this->zap->request($this->zap->base . 'core/view/optionHttpState/')->{'HttpState'};
	}

	public function optionTimeoutInSecs() {
		return $this->zap->request($this->zap->base . 'core/view/optionTimeoutInSecs/')->{'TimeoutInSecs'};
	}

	public function optionSingleCookieRequestHeader() {
		return $this->zap->request($this->zap->base . 'core/view/optionSingleCookieRequestHeader/')->{'SingleCookieRequestHeader'};
	}

	public function optionProxyExcludedDomains() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyExcludedDomains/')->{'ProxyExcludedDomains'};
	}

	public function optionProxyExcludedDomainsEnabled() {
		return $this->zap->request($this->zap->base . 'core/view/optionProxyExcludedDomainsEnabled/')->{'ProxyExcludedDomainsEnabled'};
	}

	/**
	 * Shuts down ZAP
	 */
	public function shutdown($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/shutdown/', array('apikey' => $apikey));
	}

	/**
	 * Creates a new session, optionally overwriting existing files
	 */
	public function newSession($name='', $overwrite='', $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/newSession/', array('name' => $name, 'overwrite' => $overwrite, 'apikey' => $apikey));
	}

	/**
	 * Loads the session with the given name
	 */
	public function loadSession($name, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/loadSession/', array('name' => $name, 'apikey' => $apikey));
	}

	/**
	 * Saves the session with the name supplied, optionally overwriting existing files
	 */
	public function saveSession($name, $overwrite='', $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/saveSession/', array('name' => $name, 'overwrite' => $overwrite, 'apikey' => $apikey));
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

	public function generateRootCA($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/generateRootCA/', array('apikey' => $apikey));
	}

	/**
	 * Sends the HTTP request, optionally following redirections. Returns the request sent and response received and followed redirections, if any.
	 */
	public function sendRequest($request, $followredirects='', $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/sendRequest/', array('request' => $request, 'followRedirects' => $followredirects, 'apikey' => $apikey));
	}

	public function deleteAllAlerts($apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/deleteAllAlerts/', array('apikey' => $apikey));
	}

	public function setOptionProxyChainName($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainName/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainRealm($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainRealm/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainUserName($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainUserName/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainPassword($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainPassword/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionProxyChainSkipName($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionProxyChainSkipName/', array('String' => $string, 'apikey' => $apikey));
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

	public function setOptionTimeoutInSecs($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionTimeoutInSecs/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionUseProxyChain($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionUseProxyChain/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionUseProxyChainAuth($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionUseProxyChainAuth/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionSingleCookieRequestHeader($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'core/action/setOptionSingleCookieRequestHeader/', array('Boolean' => $boolean, 'apikey' => $apikey));
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
