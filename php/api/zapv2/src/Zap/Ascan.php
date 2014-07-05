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

	public function status() {
		return $this->zap->request($this->zap->base . 'ascan/view/status/')->{'status'};
	}

	public function excludedFromScan() {
		return $this->zap->request($this->zap->base . 'ascan/view/excludedFromScan/')->{'excludedFromScan'};
	}

	public function scanners($policyid='') {
		return $this->zap->request($this->zap->base . 'ascan/view/scanners/', array('policyId' => $policyid))->{'scanners'};
	}

	public function policies() {
		return $this->zap->request($this->zap->base . 'ascan/view/policies/')->{'policies'};
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

	public function optionAlertThreshold() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionAlertThreshold/')->{'AlertThreshold'};
	}

	public function optionAttackStrength() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionAttackStrength/')->{'AttackStrength'};
	}

	public function optionTargetParamsInjectable() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionTargetParamsInjectable/')->{'TargetParamsInjectable'};
	}

	public function optionTargetParamsEnabledRPC() {
		return $this->zap->request($this->zap->base . 'ascan/view/optionTargetParamsEnabledRPC/')->{'TargetParamsEnabledRPC'};
	}

	public function scan($url, $recurse='', $inscopeonly='', $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/scan/', array('url' => $url, 'recurse' => $recurse, 'inScopeOnly' => $inscopeonly, 'apikey' => $apikey));
	}

	public function clearExcludedFromScan($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/clearExcludedFromScan/', array('apikey' => $apikey));
	}

	public function excludeFromScan($regex, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/excludeFromScan/', array('regex' => $regex, 'apikey' => $apikey));
	}

	public function enableAllScanners($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/enableAllScanners/', array('apikey' => $apikey));
	}

	public function disableAllScanners($apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/disableAllScanners/', array('apikey' => $apikey));
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

	public function setPolicyAttackStrength($id, $attackstrength, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setPolicyAttackStrength/', array('id' => $id, 'attackStrength' => $attackstrength, 'apikey' => $apikey));
	}

	public function setPolicyAlertThreshold($id, $alertthreshold, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setPolicyAlertThreshold/', array('id' => $id, 'alertThreshold' => $alertthreshold, 'apikey' => $apikey));
	}

	public function setScannerAttackStrength($id, $attackstrength, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setScannerAttackStrength/', array('id' => $id, 'attackStrength' => $attackstrength, 'apikey' => $apikey));
	}

	public function setScannerAlertThreshold($id, $alertthreshold, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setScannerAlertThreshold/', array('id' => $id, 'alertThreshold' => $alertthreshold, 'apikey' => $apikey));
	}

	public function setOptionAlertThreshold($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionAlertThreshold/', array('String' => $string, 'apikey' => $apikey));
	}

	public function setOptionAttackStrength($string, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionAttackStrength/', array('String' => $string, 'apikey' => $apikey));
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

	public function setOptionTargetParamsInjectable($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionTargetParamsInjectable/', array('Integer' => $integer, 'apikey' => $apikey));
	}

	public function setOptionTargetParamsEnabledRPC($integer, $apikey='') {
		return $this->zap->request($this->zap->base . 'ascan/action/setOptionTargetParamsEnabledRPC/', array('Integer' => $integer, 'apikey' => $apikey));
	}

}
