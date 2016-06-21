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
class Ascan {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function status($scanid=NULL) {
		$params = array();
		if ($scanid !== NULL) {
			$params['scanId'] = $scanid;
		}
		return $this->zap->request($this->zap->base . 'ascan/view/status/', $params)->{'status'};
	}

	public function scanProgress($scanid=NULL) {
		$params = array();
		if ($scanid !== NULL) {
			$params['scanId'] = $scanid;
		}
		return $this->zap->request($this->zap->base . 'ascan/view/scanProgress/', $params)->{'scanProgress'};
	}

	public function messagesIds($scanid) {
		return $this->zap->request($this->zap->base . 'ascan/view/messagesIds/', array('scanId' => $scanid))->{'messagesIds'};
	}

	public function alertsIds($scanid) {
		return $this->zap->request($this->zap->base . 'ascan/view/alertsIds/', array('scanId' => $scanid))->{'alertsIds'};
	}

	public function scans() {
		return $this->zap->request($this->zap->base . 'ascan/view/scans/')->{'scans'};
	}

	public function scanPolicyNames() {
		return $this->zap->request($this->zap->base . 'ascan/view/scanPolicyNames/')->{'scanPolicyNames'};
	}

	public function excludedFromScan() {
		return $this->zap->request($this->zap->base . 'ascan/view/excludedFromScan/')->{'excludedFromScan'};
	}

	public function scanners($scanpolicyname=NULL, $policyid=NULL) {
		$params = array();
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		if ($policyid !== NULL) {
			$params['policyId'] = $policyid;
		}
		return $this->zap->request($this->zap->base . 'ascan/view/scanners/', $params)->{'scanners'};
	}

	public function policies($scanpolicyname=NULL, $policyid=NULL) {
		$params = array();
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		if ($policyid !== NULL) {
			$params['policyId'] = $policyid;
		}
		return $this->zap->request($this->zap->base . 'ascan/view/policies/', $params)->{'policies'};
	}

	public function attackModeQueue() {
		return $this->zap->request($this->zap->base . 'ascan/view/attackModeQueue/')->{'attackModeQueue'};
	}

	public function optionAttackPolicy() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionAttackPolicy/')->{'AttackPolicy'};
	}

	public function optionDefaultPolicy() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionDefaultPolicy/')->{'DefaultPolicy'};
	}

	public function optionDelayInMs() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionDelayInMs/')->{'DelayInMs'};
	}

	public function optionExcludedParamList() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionExcludedParamList/')->{'ExcludedParamList'};
	}

	public function optionHandleAntiCSRFTokens() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionHandleAntiCSRFTokens/')->{'HandleAntiCSRFTokens'};
	}

	public function optionHostPerScan() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionHostPerScan/')->{'HostPerScan'};
	}

	public function optionMaxChartTimeInMins() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionMaxChartTimeInMins/')->{'MaxChartTimeInMins'};
	}

	public function optionMaxResultsToList() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionMaxResultsToList/')->{'MaxResultsToList'};
	}

	public function optionMaxScansInUI() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionMaxScansInUI/')->{'MaxScansInUI'};
	}

	public function optionTargetParamsEnabledRPC() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionTargetParamsEnabledRPC/')->{'TargetParamsEnabledRPC'};
	}

	public function optionTargetParamsInjectable() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionTargetParamsInjectable/')->{'TargetParamsInjectable'};
	}

	public function optionThreadPerHost() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionThreadPerHost/')->{'ThreadPerHost'};
	}

	public function optionAllowAttackOnStart() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionAllowAttackOnStart/')->{'AllowAttackOnStart'};
	}

	public function optionInjectPluginIdInHeader() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionInjectPluginIdInHeader/')->{'InjectPluginIdInHeader'};
	}

	public function optionPromptInAttackMode() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionPromptInAttackMode/')->{'PromptInAttackMode'};
	}

	public function optionPromptToClearFinishedScans() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionPromptToClearFinishedScans/')->{'PromptToClearFinishedScans'};
	}

	public function optionRescanInAttackMode() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionRescanInAttackMode/')->{'RescanInAttackMode'};
	}

	/**
	 * Tells whether or not the HTTP Headers of all requests should be scanned. Not just requests that send parameters, through the query or request body.
	 */
	public function optionScanHeadersAllRequests() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionScanHeadersAllRequests/')->{'ScanHeadersAllRequests'};
	}

	public function optionShowAdvancedDialog() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionShowAdvancedDialog/')->{'ShowAdvancedDialog'};
	}

	public function scan($url, $recurse=NULL, $inscopeonly=NULL, $scanpolicyname=NULL, $method=NULL, $postdata=NULL, $apikey='') {
		$params = array('url' => $url, 'apikey' => $apikey);
		if ($recurse !== NULL) {
			$params['recurse'] = $recurse;
		}
		if ($inscopeonly !== NULL) {
			$params['inScopeOnly'] = $inscopeonly;
		}
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		if ($method !== NULL) {
			$params['method'] = $method;
		}
		if ($postdata !== NULL) {
			$params['postData'] = $postdata;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/scan/', $params);
	}

	/**
	 * Active Scans from the perspective of a User, obtained using the given Context ID and User ID. See 'scan' action for more details.
	 */
	public function scanAsUser($url, $contextid, $userid, $recurse=NULL, $scanpolicyname=NULL, $method=NULL, $postdata=NULL, $apikey='') {
		$params = array('url' => $url, 'contextId' => $contextid, 'userId' => $userid, 'apikey' => $apikey);
		if ($recurse !== NULL) {
			$params['recurse'] = $recurse;
		}
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		if ($method !== NULL) {
			$params['method'] = $method;
		}
		if ($postdata !== NULL) {
			$params['postData'] = $postdata;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/scanAsUser/', $params);
	}

	public function pause($scanid, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/pause/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function resume($scanid, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/resume/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function stop($scanid, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/stop/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function removeScan($scanid, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/removeScan/', array('scanId' => $scanid, 'apikey' => $apikey));
	}

	public function pauseAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/pauseAllScans/', array('apikey' => $apikey));
	}

	public function resumeAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/resumeAllScans/', array('apikey' => $apikey));
	}

	public function stopAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/stopAllScans/', array('apikey' => $apikey));
	}

	public function removeAllScans($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/removeAllScans/', array('apikey' => $apikey));
	}

	public function clearExcludedFromScan($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/clearExcludedFromScan/', array('apikey' => $apikey));
	}

	public function excludeFromScan($regex, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/excludeFromScan/', array('regex' => $regex, 'apikey' => $apikey));
	}

	public function enableAllScanners($scanpolicyname=NULL, $apikey='') {
		$params = array('apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/enableAllScanners/', $params);
	}

	public function disableAllScanners($scanpolicyname=NULL, $apikey='') {
		$params = array('apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/disableAllScanners/', $params);
	}

	public function enableScanners($ids, $scanpolicyname=NULL, $apikey='') {
		$params = array('ids' => $ids, 'apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/enableScanners/', $params);
	}

	public function disableScanners($ids, $scanpolicyname=NULL, $apikey='') {
		$params = array('ids' => $ids, 'apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/disableScanners/', $params);
	}

	public function setEnabledPolicies($ids, $scanpolicyname=NULL, $apikey='') {
		$params = array('ids' => $ids, 'apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/setEnabledPolicies/', $params);
	}

	public function setPolicyAttackStrength($id, $attackstrength, $scanpolicyname=NULL, $apikey='') {
		$params = array('id' => $id, 'attackStrength' => $attackstrength, 'apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/setPolicyAttackStrength/', $params);
	}

	public function setPolicyAlertThreshold($id, $alertthreshold, $scanpolicyname=NULL, $apikey='') {
		$params = array('id' => $id, 'alertThreshold' => $alertthreshold, 'apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/setPolicyAlertThreshold/', $params);
	}

	public function setScannerAttackStrength($id, $attackstrength, $scanpolicyname=NULL, $apikey='') {
		$params = array('id' => $id, 'attackStrength' => $attackstrength, 'apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/setScannerAttackStrength/', $params);
	}

	public function setScannerAlertThreshold($id, $alertthreshold, $scanpolicyname=NULL, $apikey='') {
		$params = array('id' => $id, 'alertThreshold' => $alertthreshold, 'apikey' => $apikey);
		if ($scanpolicyname !== NULL) {
			$params['scanPolicyName'] = $scanpolicyname;
		}
		return $this->zap->request($this->zap->base . 'ascan/action/setScannerAlertThreshold/', $params);
	}

	public function addScanPolicy($scanpolicyname, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/addScanPolicy/', array('scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function removeScanPolicy($scanpolicyname, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/removeScanPolicy/', array('scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function setOptionAttackPolicy($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionAttackPolicy/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionDefaultPolicy($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionDefaultPolicy/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionAllowAttackOnStart($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionAllowAttackOnStart/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionDelayInMs($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionDelayInMs/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionHandleAntiCSRFTokens($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionHandleAntiCSRFTokens/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionHostPerScan($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionHostPerScan/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionInjectPluginIdInHeader($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionInjectPluginIdInHeader/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionMaxChartTimeInMins($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionMaxChartTimeInMins/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionMaxResultsToList($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionMaxResultsToList/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionMaxScansInUI($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionMaxScansInUI/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionPromptInAttackMode($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionPromptInAttackMode/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionPromptToClearFinishedScans($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionPromptToClearFinishedScans/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionRescanInAttackMode($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionRescanInAttackMode/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	/**
	 * Sets whether or not the HTTP Headers of all requests should be scanned. Not just requests that send parameters, through the query or request body.
	 */
	public function setOptionScanHeadersAllRequests($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionScanHeadersAllRequests/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionShowAdvancedDialog($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionShowAdvancedDialog/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionTargetParamsEnabledRPC($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionTargetParamsEnabledRPC/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionTargetParamsInjectable($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionTargetParamsInjectable/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionThreadPerHost($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionThreadPerHost/', array('Integer' => $integer, 'apikey' => $apikey));
	}

}
