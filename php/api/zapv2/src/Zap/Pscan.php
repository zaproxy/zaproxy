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
class Pscan {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * The number of records the passive scanner still has to scan
	 */
	public function recordsToScan() {
		$res = $this->zap->request($this->zap->base . 'pscan/view/recordsToScan/');
		return reset($res);
	}

	/**
	 * Lists all passive scanners with its ID, name, enabled state and alert threshold.
	 */
	public function scanners() {
		$res = $this->zap->request($this->zap->base . 'pscan/view/scanners/');
		return reset($res);
	}

	/**
	 * Sets whether or not the passive scanning is enabled
	 */
	public function setEnabled($enabled, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'pscan/action/setEnabled/', array('enabled' => $enabled, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Enables all passive scanners
	 */
	public function enableAllScanners($apikey='') {
		$res = $this->zap->request($this->zap->base . 'pscan/action/enableAllScanners/', array('apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Disables all passive scanners
	 */
	public function disableAllScanners($apikey='') {
		$res = $this->zap->request($this->zap->base . 'pscan/action/disableAllScanners/', array('apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Enables all passive scanners with the given IDs (comma separated list of IDs)
	 */
	public function enableScanners($ids, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'pscan/action/enableScanners/', array('ids' => $ids, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Disables all passive scanners with the given IDs (comma separated list of IDs)
	 */
	public function disableScanners($ids, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'pscan/action/disableScanners/', array('ids' => $ids, 'apikey' => $apikey));
		return reset($res);
	}

	/**
	 * Sets the alert threshold of the passive scanner with the given ID, accepted values for alert threshold: OFF, DEFAULT, LOW, MEDIUM and HIGH
	 */
	public function setScannerAlertThreshold($id, $alertthreshold, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'pscan/action/setScannerAlertThreshold/', array('id' => $id, 'alertThreshold' => $alertthreshold, 'apikey' => $apikey));
		return reset($res);
	}

}
