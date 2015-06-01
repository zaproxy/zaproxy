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
class Spider {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function status($scanid='') {
		$res = $this->zap->request($this->zap->base . 'spider/view/status/', array('scanId' => $scanid));
		return reset($res);
	}

	public function results($scanid='') {
		$res = $this->zap->request($this->zap->base . 'spider/view/results/', array('scanId' => $scanid));
		return reset($res);
	}

	public function fullResults($scanid) {
		$res = $this->zap->request($this->zap->base . 'spider/view/fullResults/', array('scanId' => $scanid));
		return reset($res);
	}

	public function scans() {
		$res = $this->zap->request($this->zap->base . 'spider/view/scans/');
		return reset($res);
	}

	public function excludedFromScan() {
		$res = $this->zap->request($this->zap->base . 'spider/view/excludedFromScan/');
		return reset($res);
	}

	public function optionScope() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionScope/');
		return reset($res);
	}

	public function optionMaxDepth() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionMaxDepth/');
		return reset($res);
	}

	public function optionScopeText() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionScopeText/');
		return reset($res);
	}

	public function optionThreadCount() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionThreadCount/');
		return reset($res);
	}

	public function optionPostForm() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionPostForm/');
		return reset($res);
	}

	public function optionProcessForm() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionProcessForm/');
		return reset($res);
	}

	public function optionSkipURLString() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionSkipURLString/');
		return reset($res);
	}

	public function optionRequestWaitTime() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionRequestWaitTime/');
		return reset($res);
	}

	public function optionUserAgent() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionUserAgent/');
		return reset($res);
	}

	public function optionParseComments() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionParseComments/');
		return reset($res);
	}

	public function optionParseRobotsTxt() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionParseRobotsTxt/');
		return reset($res);
	}

	public function optionParseSitemapXml() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionParseSitemapXml/');
		return reset($res);
	}

	public function optionParseSVNEntries() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionParseSVNEntries/');
		return reset($res);
	}

	public function optionParseGit() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionParseGit/');
		return reset($res);
	}

	public function optionHandleParameters() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionHandleParameters/');
		return reset($res);
	}

	public function optionHandleODataParametersVisited() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionHandleODataParametersVisited/');
		return reset($res);
	}

	public function optionDomainsAlwaysInScope() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionDomainsAlwaysInScope/');
		return reset($res);
	}

	public function optionDomainsAlwaysInScopeEnabled() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionDomainsAlwaysInScopeEnabled/');
		return reset($res);
	}

	public function optionMaxScansInUI() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionMaxScansInUI/');
		return reset($res);
	}

	public function optionShowAdvancedDialog() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionShowAdvancedDialog/');
		return reset($res);
	}

	/**
	 * Sets whether or not the 'Referer' header should be sent while spidering
	 */
	public function optionSendRefererHeader() {
		$res = $this->zap->request($this->zap->base . 'spider/view/optionSendRefererHeader/');
		return reset($res);
	}

	public function scan($url, $maxchildren='', $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/scan/', array('url' => $url, 'maxChildren' => $maxchildren, 'apikey' => $apikey));
		return reset($res);
	}

	public function scanAsUser($url, $contextid, $userid, $maxchildren, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/scanAsUser/', array('url' => $url, 'contextId' => $contextid, 'userId' => $userid, 'maxChildren' => $maxchildren, 'apikey' => $apikey));
		return reset($res);
	}

	public function pause($scanid, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/pause/', array('scanId' => $scanid, 'apikey' => $apikey));
		return reset($res);
	}

	public function resume($scanid, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/resume/', array('scanId' => $scanid, 'apikey' => $apikey));
		return reset($res);
	}

	public function stop($scanid='', $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/stop/', array('scanId' => $scanid, 'apikey' => $apikey));
		return reset($res);
	}

	public function removeScan($scanid, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/removeScan/', array('scanId' => $scanid, 'apikey' => $apikey));
		return reset($res);
	}

	public function pauseAllScans($apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/pauseAllScans/', array('apikey' => $apikey));
		return reset($res);
	}

	public function resumeAllScans($apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/resumeAllScans/', array('apikey' => $apikey));
		return reset($res);
	}

	public function stopAllScans($apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/stopAllScans/', array('apikey' => $apikey));
		return reset($res);
	}

	public function removeAllScans($apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/removeAllScans/', array('apikey' => $apikey));
		return reset($res);
	}

	public function clearExcludedFromScan($apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/clearExcludedFromScan/', array('apikey' => $apikey));
		return reset($res);
	}

	public function excludeFromScan($regex, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/excludeFromScan/', array('regex' => $regex, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionSkipURLString($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionSkipURLString/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionHandleParameters($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionHandleParameters/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionScopeString($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionScopeString/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionUserAgent($string, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionUserAgent/', array('String' => $string, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionMaxDepth($integer, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionMaxDepth/', array('Integer' => $integer, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionThreadCount($integer, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionThreadCount/', array('Integer' => $integer, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionPostForm($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionPostForm/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionProcessForm($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionProcessForm/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionRequestWaitTime($integer, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionRequestWaitTime/', array('Integer' => $integer, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionParseComments($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionParseComments/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionParseRobotsTxt($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionParseRobotsTxt/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionParseSitemapXml($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionParseSitemapXml/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionParseSVNEntries($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionParseSVNEntries/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionParseGit($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionParseGit/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionHandleODataParametersVisited($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionHandleODataParametersVisited/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionMaxScansInUI($integer, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionMaxScansInUI/', array('Integer' => $integer, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionShowAdvancedDialog($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionShowAdvancedDialog/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionSendRefererHeader($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'spider/action/setOptionSendRefererHeader/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

}
