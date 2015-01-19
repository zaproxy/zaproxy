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
class Spider {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function status($scanid='') {
		return $this->zap->request($this->zap->base . 'spider/view/status/', array('scanId' => $scanid))->{'status'};
	}

	public function results($scanid='') {
		return $this->zap->request($this->zap->base . 'spider/view/results/', array('scanId' => $scanid))->{'results'};
	}

	public function fullResults($scanid) {
		return $this->zap->request($this->zap->base . 'spider/view/fullResults/', array('scanId' => $scanid))->{'fullResults'};
	}

	public function scans() {
		return $this->zap->request($this->zap->base . 'spider/view/scans/')->{'scans'};
	}

	public function excludedFromScan() {
		return $this->zap->request($this->zap->base . 'spider/view/excludedFromScan/')->{'excludedFromScan'};
	}

	public function optionMaxDepth() {
		return $this->zap->request($this->zap->base . 'spider/view/optionMaxDepth/')->{'MaxDepth'};
	}

	public function optionScopeText() {
		return $this->zap->request($this->zap->base . 'spider/view/optionScopeText/')->{'ScopeText'};
	}

	public function optionScope() {
		return $this->zap->request($this->zap->base . 'spider/view/optionScope/')->{'Scope'};
	}

	public function optionThreadCount() {
		return $this->zap->request($this->zap->base . 'spider/view/optionThreadCount/')->{'ThreadCount'};
	}

	public function optionPostForm() {
		return $this->zap->request($this->zap->base . 'spider/view/optionPostForm/')->{'PostForm'};
	}

	public function optionProcessForm() {
		return $this->zap->request($this->zap->base . 'spider/view/optionProcessForm/')->{'ProcessForm'};
	}

	public function optionSkipURLString() {
		return $this->zap->request($this->zap->base . 'spider/view/optionSkipURLString/')->{'SkipURLString'};
	}

	public function optionRequestWaitTime() {
		return $this->zap->request($this->zap->base . 'spider/view/optionRequestWaitTime/')->{'RequestWaitTime'};
	}

	public function optionUserAgent() {
		return $this->zap->request($this->zap->base . 'spider/view/optionUserAgent/')->{'UserAgent'};
	}

	public function optionParseComments() {
		return $this->zap->request($this->zap->base . 'spider/view/optionParseComments/')->{'ParseComments'};
	}

	public function optionParseRobotsTxt() {
		return $this->zap->request($this->zap->base . 'spider/view/optionParseRobotsTxt/')->{'ParseRobotsTxt'};
	}

	public function optionParseSitemapXml() {
		return $this->zap->request($this->zap->base . 'spider/view/optionParseSitemapXml/')->{'ParseSitemapXml'};
	}

	public function optionParseSVNEntries() {
		return $this->zap->request($this->zap->base . 'spider/view/optionParseSVNEntries/')->{'ParseSVNEntries'};
	}

	public function optionParseGit() {
		return $this->zap->request($this->zap->base . 'spider/view/optionParseGit/')->{'ParseGit'};
	}

	public function optionHandleParameters() {
		return $this->zap->request($this->zap->base . 'spider/view/optionHandleParameters/')->{'HandleParameters'};
	}

	public function optionHandleODataParametersVisited() {
		return $this->zap->request($this->zap->base . 'spider/view/optionHandleODataParametersVisited/')->{'HandleODataParametersVisited'};
	}

	public function optionDomainsAlwaysInScope() {
		return $this->zap->request($this->zap->base . 'spider/view/optionDomainsAlwaysInScope/')->{'DomainsAlwaysInScope'};
	}

	public function optionDomainsAlwaysInScopeEnabled() {
		return $this->zap->request($this->zap->base . 'spider/view/optionDomainsAlwaysInScopeEnabled/')->{'DomainsAlwaysInScopeEnabled'};
	}

	public function optionMaxScansInUI() {
		return $this->zap->request($this->zap->base . 'spider/view/optionMaxScansInUI/')->{'MaxScansInUI'};
	}

	public function optionShowAdvancedDialog() {
		return $this->zap->request($this->zap->base . 'spider/view/optionShowAdvancedDialog/')->{'ShowAdvancedDialog'};
	}

	/**
	 * Sets whether or not the 'Referer' header should be sent while spidering
	 */
	public function optionSendRefererHeader() {
		return $this->zap->request($this->zap->base . 'spider/view/optionSendRefererHeader/')->{'SendRefererHeader'};
	}

	public function scan($url, $maxchildren='', $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/scan/', array('url' => $url, 'maxChildren' => $maxchildren, 'apikey' => $apikey));
	}

	public function scanAsUser($url, $contextid, $userid, $maxchildren, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/scanAsUser/', array('url' => $url, 'contextId' => $contextid, 'userId' => $userid, 'maxChildren' => $maxchildren, 'apikey' => $apikey));
	}

	public function pause($scanid, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/pause/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function resume($scanid, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/resume/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function stop($scanid='', $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/stop/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function removeScan($scanid, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/removeScan/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function pauseAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/pauseAllScans/', array('apikey' => $apikey));
	}

	public function resumeAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/resumeAllScans/', array('apikey' => $apikey));
	}

	public function stopAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/stopAllScans/', array('apikey' => $apikey));
	}

	public function removeAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/removeAllScans/', array('apikey' => $apikey));
	}

	public function clearExcludedFromScan($apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/clearExcludedFromScan/', array('apikey' => $apikey));
	}

	public function excludeFromScan($regex, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/excludeFromScan/', array('regex' => $regex, 'apikey' => $apikey));
	}

	public function setOptionSkipURLString($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionSkipURLString/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionHandleParameters($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionHandleParameters/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionScopeString($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionScopeString/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionUserAgent($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionUserAgent/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionMaxDepth($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionMaxDepth/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionThreadCount($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionThreadCount/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionPostForm($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionPostForm/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionProcessForm($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionProcessForm/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionRequestWaitTime($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionRequestWaitTime/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionParseComments($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionParseComments/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionParseRobotsTxt($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionParseRobotsTxt/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionParseSitemapXml($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionParseSitemapXml/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionParseSVNEntries($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionParseSVNEntries/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionParseGit($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionParseGit/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionHandleODataParametersVisited($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionHandleODataParametersVisited/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionMaxScansInUI($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionMaxScansInUI/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionShowAdvancedDialog($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionShowAdvancedDialog/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionSendRefererHeader($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'spider/action/setOptionSendRefererHeader/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

}
