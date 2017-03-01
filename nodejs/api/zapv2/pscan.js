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
function Pscan(clientApi) {
  this.api = clientApi;
}

/**
 * The number of records the passive scanner still has to scan
 **/
Pscan.prototype.recordsToScan = function (callback) {
  this.api.request('/pscan/view/recordsToScan/', callback);
};

/**
 * Lists all passive scanners with its ID, name, enabled state and alert threshold.
 **/
Pscan.prototype.scanners = function (callback) {
  this.api.request('/pscan/view/scanners/', callback);
};

/**
 * Sets whether or not the passive scanning is enabled
 **/
Pscan.prototype.setEnabled = function (enabled, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pscan/action/setEnabled/', {'enabled' : enabled, 'apikey' : apikey}, callback);
};

/**
 * Enables all passive scanners
 **/
Pscan.prototype.enableAllScanners = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pscan/action/enableAllScanners/', {'apikey' : apikey}, callback);
};

/**
 * Disables all passive scanners
 **/
Pscan.prototype.disableAllScanners = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pscan/action/disableAllScanners/', {'apikey' : apikey}, callback);
};

/**
 * Enables all passive scanners with the given IDs (comma separated list of IDs)
 **/
Pscan.prototype.enableScanners = function (ids, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pscan/action/enableScanners/', {'ids' : ids, 'apikey' : apikey}, callback);
};

/**
 * Disables all passive scanners with the given IDs (comma separated list of IDs)
 **/
Pscan.prototype.disableScanners = function (ids, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pscan/action/disableScanners/', {'ids' : ids, 'apikey' : apikey}, callback);
};

/**
 * Sets the alert threshold of the passive scanner with the given ID, accepted values for alert threshold: OFF, DEFAULT, LOW, MEDIUM and HIGH
 **/
Pscan.prototype.setScannerAlertThreshold = function (id, alertthreshold, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pscan/action/setScannerAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'apikey' : apikey}, callback);
};

module.exports = Pscan;
