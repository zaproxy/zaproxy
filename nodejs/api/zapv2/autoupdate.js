/* Zed Attack Proxy (ZAP) and its related class files.
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


'use strict';

/**
 * This file was automatically generated.
 */
function Autoupdate(clientApi) {
  this.api = clientApi;
}

/**
 * Returns the latest version number
 **/
Autoupdate.prototype.latestVersionNumber = function (callback) {
  this.api.request('/autoupdate/view/latestVersionNumber/', callback);
};

/**
 * Returns 'true' if ZAP is on the latest version
 **/
Autoupdate.prototype.isLatestVersion = function (callback) {
  this.api.request('/autoupdate/view/isLatestVersion/', callback);
};

Autoupdate.prototype.optionAddonDirectories = function (callback) {
  this.api.request('/autoupdate/view/optionAddonDirectories/', callback);
};

Autoupdate.prototype.optionDayLastChecked = function (callback) {
  this.api.request('/autoupdate/view/optionDayLastChecked/', callback);
};

Autoupdate.prototype.optionDayLastInstallWarned = function (callback) {
  this.api.request('/autoupdate/view/optionDayLastInstallWarned/', callback);
};

Autoupdate.prototype.optionDayLastUpdateWarned = function (callback) {
  this.api.request('/autoupdate/view/optionDayLastUpdateWarned/', callback);
};

Autoupdate.prototype.optionDownloadDirectory = function (callback) {
  this.api.request('/autoupdate/view/optionDownloadDirectory/', callback);
};

Autoupdate.prototype.optionCheckAddonUpdates = function (callback) {
  this.api.request('/autoupdate/view/optionCheckAddonUpdates/', callback);
};

Autoupdate.prototype.optionCheckOnStart = function (callback) {
  this.api.request('/autoupdate/view/optionCheckOnStart/', callback);
};

Autoupdate.prototype.optionDownloadNewRelease = function (callback) {
  this.api.request('/autoupdate/view/optionDownloadNewRelease/', callback);
};

Autoupdate.prototype.optionInstallAddonUpdates = function (callback) {
  this.api.request('/autoupdate/view/optionInstallAddonUpdates/', callback);
};

Autoupdate.prototype.optionInstallScannerRules = function (callback) {
  this.api.request('/autoupdate/view/optionInstallScannerRules/', callback);
};

Autoupdate.prototype.optionReportAlphaAddons = function (callback) {
  this.api.request('/autoupdate/view/optionReportAlphaAddons/', callback);
};

Autoupdate.prototype.optionReportBetaAddons = function (callback) {
  this.api.request('/autoupdate/view/optionReportBetaAddons/', callback);
};

Autoupdate.prototype.optionReportReleaseAddons = function (callback) {
  this.api.request('/autoupdate/view/optionReportReleaseAddons/', callback);
};

/**
 * Downloads the latest release, if any 
 **/
Autoupdate.prototype.downloadLatestRelease = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/downloadLatestRelease/', {'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionCheckAddonUpdates = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionCheckAddonUpdates/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionCheckOnStart = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionCheckOnStart/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionDownloadNewRelease = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionDownloadNewRelease/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionInstallAddonUpdates = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionInstallAddonUpdates/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionInstallScannerRules = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionInstallScannerRules/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionReportAlphaAddons = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionReportAlphaAddons/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionReportBetaAddons = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionReportBetaAddons/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Autoupdate.prototype.setOptionReportReleaseAddons = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/autoupdate/action/setOptionReportReleaseAddons/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

module.exports = Autoupdate;
