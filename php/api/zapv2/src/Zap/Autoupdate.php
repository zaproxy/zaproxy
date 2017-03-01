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
class Autoupdate {

	public function __construct ($zap) {
		$this->zap = $zap;
	}

	/**
	 * Returns the latest version number
	 */
	public function latestVersionNumber() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/latestVersionNumber/')->{'latestVersionNumber'};
	}

	/**
	 * Returns 'true' if ZAP is on the latest version
	 */
	public function isLatestVersion() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/isLatestVersion/')->{'isLatestVersion'};
	}

	public function optionAddonDirectories() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionAddonDirectories/')->{'AddonDirectories'};
	}

	public function optionDayLastChecked() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionDayLastChecked/')->{'DayLastChecked'};
	}

	public function optionDayLastInstallWarned() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionDayLastInstallWarned/')->{'DayLastInstallWarned'};
	}

	public function optionDayLastUpdateWarned() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionDayLastUpdateWarned/')->{'DayLastUpdateWarned'};
	}

	public function optionDownloadDirectory() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionDownloadDirectory/')->{'DownloadDirectory'};
	}

	public function optionCheckAddonUpdates() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionCheckAddonUpdates/')->{'CheckAddonUpdates'};
	}

	public function optionCheckOnStart() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionCheckOnStart/')->{'CheckOnStart'};
	}

	public function optionDownloadNewRelease() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionDownloadNewRelease/')->{'DownloadNewRelease'};
	}

	public function optionInstallAddonUpdates() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionInstallAddonUpdates/')->{'InstallAddonUpdates'};
	}

	public function optionInstallScannerRules() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionInstallScannerRules/')->{'InstallScannerRules'};
	}

	public function optionReportAlphaAddons() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionReportAlphaAddons/')->{'ReportAlphaAddons'};
	}

	public function optionReportBetaAddons() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionReportBetaAddons/')->{'ReportBetaAddons'};
	}

	public function optionReportReleaseAddons() {
		return $this->zap->request($this->zap->base . 'autoupdate/view/optionReportReleaseAddons/')->{'ReportReleaseAddons'};
	}

	/**
	 * Downloads the latest release, if any 
	 */
	public function downloadLatestRelease($apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/downloadLatestRelease/', array('apikey' => $apikey));
	}

	public function setOptionCheckAddonUpdates($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionCheckAddonUpdates/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionCheckOnStart($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionCheckOnStart/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionDownloadNewRelease($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionDownloadNewRelease/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionInstallAddonUpdates($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionInstallAddonUpdates/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionInstallScannerRules($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionInstallScannerRules/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionReportAlphaAddons($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionReportAlphaAddons/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionReportBetaAddons($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionReportBetaAddons/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

	public function setOptionReportReleaseAddons($boolean, $apikey='') {
		return $this->zap->request($this->zap->base . 'autoupdate/action/setOptionReportReleaseAddons/', array('Boolean' => $boolean, 'apikey' => $apikey));
	}

}
