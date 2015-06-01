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
class Autoupdate {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	public function latestVersionNumber() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/latestVersionNumber/');
		return reset($res);
	}

	public function isLatestVersion() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/isLatestVersion/');
		return reset($res);
	}

	public function optionCheckOnStart() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionCheckOnStart/');
		return reset($res);
	}

	public function optionDownloadNewRelease() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionDownloadNewRelease/');
		return reset($res);
	}

	public function optionCheckAddonUpdates() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionCheckAddonUpdates/');
		return reset($res);
	}

	public function optionInstallAddonUpdates() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionInstallAddonUpdates/');
		return reset($res);
	}

	public function optionInstallScannerRules() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionInstallScannerRules/');
		return reset($res);
	}

	public function optionReportReleaseAddons() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionReportReleaseAddons/');
		return reset($res);
	}

	public function optionReportBetaAddons() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionReportBetaAddons/');
		return reset($res);
	}

	public function optionReportAlphaAddons() {
		$res = $this->zap->request($this->zap->base . 'autoupdate/view/optionReportAlphaAddons/');
		return reset($res);
	}

	public function downloadLatestRelease($apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/downloadLatestRelease/', array('apikey' => $apikey));
		return reset($res);
	}

	public function setOptionCheckOnStart($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionCheckOnStart/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionDownloadNewRelease($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionDownloadNewRelease/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionCheckAddonUpdates($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionCheckAddonUpdates/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionInstallAddonUpdates($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionInstallAddonUpdates/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionInstallScannerRules($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionInstallScannerRules/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionReportReleaseAddons($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionReportReleaseAddons/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionReportBetaAddons($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionReportBetaAddons/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

	public function setOptionReportAlphaAddons($boolean, $apikey='') {
		$res = $this->zap->request($this->zap->base . 'autoupdate/action/setOptionReportAlphaAddons/', array('Boolean' => $boolean, 'apikey' => $apikey));
		return reset($res);
	}

}
