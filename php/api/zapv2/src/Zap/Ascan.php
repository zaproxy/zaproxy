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
class Ascan {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function status($scanid='') {
		return $this->zap->request($this->zap->base . 'ascan/view/status/', array('scanId' => $scanid))->{'status'};
	}

	public function scanProgress($scanid='') {
		return $this->zap->request($this->zap->base . 'ascan/view/scanProgress/', array('scanId' => $scanid))->{'scanProgress'};
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

	public function scanners($scanpolicyname='', $policyid='') {
		return $this->zap->request($this->zap->base . 'ascan/view/scanners/', array('scanPolicyName' => $scanpolicyname, 'policyId' => $policyid))->{'scanners'};
	}

	public function policies($scanpolicyname='', $policyid='') {
		return $this->zap->request($this->zap->base . 'ascan/view/policies/', array('scanPolicyName' => $scanpolicyname, 'policyId' => $policyid))->{'policies'};
	}

	public function attackModeQueue() {
		return $this->zap->request($this->zap->base . 'ascan/view/attackModeQueue/')->{'attackModeQueue'};
	}

	public function optionMaxScansInUI() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionMaxScansInUI/')->{'MaxScansInUI'};
	}

	public function optionShowAdvancedDialog() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionShowAdvancedDialog/')->{'ShowAdvancedDialog'};
	}

	public function optionExcludedParamList() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionExcludedParamList/')->{'ExcludedParamList'};
	}

	public function optionThreadPerHost() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionThreadPerHost/')->{'ThreadPerHost'};
	}

	public function optionHostPerScan() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionHostPerScan/')->{'HostPerScan'};
	}

	public function optionMaxResultsToList() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionMaxResultsToList/')->{'MaxResultsToList'};
	}

	public function optionDelayInMs() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionDelayInMs/')->{'DelayInMs'};
	}

	public function optionHandleAntiCSRFTokens() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionHandleAntiCSRFTokens/')->{'HandleAntiCSRFTokens'};
	}

	public function optionRescanInAttackMode() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionRescanInAttackMode/')->{'RescanInAttackMode'};
	}

	public function optionPromptInAttackMode() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionPromptInAttackMode/')->{'PromptInAttackMode'};
	}

	public function optionTargetParamsInjectable() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionTargetParamsInjectable/')->{'TargetParamsInjectable'};
	}

	public function optionTargetParamsEnabledRPC() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionTargetParamsEnabledRPC/')->{'TargetParamsEnabledRPC'};
	}

	public function optionPromptToClearFinishedScans() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionPromptToClearFinishedScans/')->{'PromptToClearFinishedScans'};
	}

	public function optionDefaultPolicy() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionDefaultPolicy/')->{'DefaultPolicy'};
	}

	public function optionAttackPolicy() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionAttackPolicy/')->{'AttackPolicy'};
	}

	public function optionAllowAttackOnStart() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionAllowAttackOnStart/')->{'AllowAttackOnStart'};
	}

	public function scan($url, $recurse='', $inscopeonly='', $scanpolicyname='', $method='', $postdata='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/scan/', array('url' => $url, 'recurse' => $recurse, 'inScopeOnly' => $inscopeonly, 'scanPolicyName' => $scanpolicyname, 'method' => $method, 'postData' => $postdata, 'apikey' => $apikey));
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

	public function enableAllScanners($scanpolicyname='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/enableAllScanners/', array('scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function disableAllScanners($scanpolicyname='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/disableAllScanners/', array('scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function enableScanners($ids, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/enableScanners/', array('ids' => $ids, 'apikey' => $apikey));
	}

	public function disableScanners($ids, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/disableScanners/', array('ids' => $ids, 'apikey' => $apikey));
	}

	public function setEnabledPolicies($ids, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setEnabledPolicies/', array('ids' => $ids, 'apikey' => $apikey));
	}

	public function setPolicyAttackStrength($id, $attackstrength, $scanpolicyname='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setPolicyAttackStrength/', array('id' => $id, 'attackStrength' => $attackstrength, 'scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function setPolicyAlertThreshold($id, $alertthreshold, $scanpolicyname='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setPolicyAlertThreshold/', array('id' => $id, 'alertThreshold' => $alertthreshold, 'scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function setScannerAttackStrength($id, $attackstrength, $scanpolicyname='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setScannerAttackStrength/', array('id' => $id, 'attackStrength' => $attackstrength, 'scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function setScannerAlertThreshold($id, $alertthreshold, $scanpolicyname='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setScannerAlertThreshold/', array('id' => $id, 'alertThreshold' => $alertthreshold, 'scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function addScanPolicy($scanpolicyname, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/addScanPolicy/', array('scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function removeScanPolicy($scanpolicyname, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/removeScanPolicy/', array('scanPolicyName' => $scanpolicyname, 'apikey' => $apikey));
	}

	public function setOptionDefaultPolicy($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionDefaultPolicy/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionAttackPolicy($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionAttackPolicy/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionMaxScansInUI($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionMaxScansInUI/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionShowAdvancedDialog($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionShowAdvancedDialog/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionThreadPerHost($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionThreadPerHost/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionHostPerScan($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionHostPerScan/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionMaxResultsToList($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionMaxResultsToList/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionDelayInMs($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionDelayInMs/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionHandleAntiCSRFTokens($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionHandleAntiCSRFTokens/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionRescanInAttackMode($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionRescanInAttackMode/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionPromptInAttackMode($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionPromptInAttackMode/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionTargetParamsInjectable($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionTargetParamsInjectable/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionTargetParamsEnabledRPC($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionTargetParamsEnabledRPC/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionPromptToClearFinishedScans($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionPromptToClearFinishedScans/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionAllowAttackOnStart($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionAllowAttackOnStart/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

}
