/* Zed Attack Proxy (ZAP) and its related class files.
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


'use strict';

/**
 * This file was automatically generated.
 */
function Ascan(clientApi) {
  this.api = clientApi;
}

Ascan.prototype.status = function (scanid, callback) {
  this.api.request('/ascan/view/status/', {'scanId' : scanid}, callback);
};

Ascan.prototype.messagesIds = function (scanid, callback) {
  this.api.request('/ascan/view/messagesIds/', {'scanId' : scanid}, callback);
};

Ascan.prototype.alertsIds = function (scanid, callback) {
  this.api.request('/ascan/view/alertsIds/', {'scanId' : scanid}, callback);
};

Ascan.prototype.scans = function (callback) {
  this.api.request('/ascan/view/scans/', callback);
};

Ascan.prototype.scanPolicyNames = function (callback) {
  this.api.request('/ascan/view/scanPolicyNames/', callback);
};

Ascan.prototype.excludedFromScan = function (callback) {
  this.api.request('/ascan/view/excludedFromScan/', callback);
};

Ascan.prototype.scanners = function (scanpolicyname, policyid, callback) {
  this.api.request('/ascan/view/scanners/', {'scanPolicyName' : scanpolicyname, 'policyId' : policyid}, callback);
};

Ascan.prototype.policies = function (scanpolicyname, policyid, callback) {
  this.api.request('/ascan/view/policies/', {'scanPolicyName' : scanpolicyname, 'policyId' : policyid}, callback);
};

Ascan.prototype.optionMaxScansInUI = function (callback) {
  this.api.request('/ascan/view/optionMaxScansInUI/', callback);
};

Ascan.prototype.optionShowAdvancedDialog = function (callback) {
  this.api.request('/ascan/view/optionShowAdvancedDialog/', callback);
};

Ascan.prototype.optionExcludedParamList = function (callback) {
  this.api.request('/ascan/view/optionExcludedParamList/', callback);
};

Ascan.prototype.optionThreadPerHost = function (callback) {
  this.api.request('/ascan/view/optionThreadPerHost/', callback);
};

Ascan.prototype.optionHostPerScan = function (callback) {
  this.api.request('/ascan/view/optionHostPerScan/', callback);
};

Ascan.prototype.optionMaxResultsToList = function (callback) {
  this.api.request('/ascan/view/optionMaxResultsToList/', callback);
};

Ascan.prototype.optionDelayInMs = function (callback) {
  this.api.request('/ascan/view/optionDelayInMs/', callback);
};

Ascan.prototype.optionHandleAntiCSRFTokens = function (callback) {
  this.api.request('/ascan/view/optionHandleAntiCSRFTokens/', callback);
};

Ascan.prototype.optionRescanInAttackMode = function (callback) {
  this.api.request('/ascan/view/optionRescanInAttackMode/', callback);
};

Ascan.prototype.optionPromptInAttackMode = function (callback) {
  this.api.request('/ascan/view/optionPromptInAttackMode/', callback);
};

Ascan.prototype.optionTargetParamsInjectable = function (callback) {
  this.api.request('/ascan/view/optionTargetParamsInjectable/', callback);
};

Ascan.prototype.optionTargetParamsEnabledRPC = function (callback) {
  this.api.request('/ascan/view/optionTargetParamsEnabledRPC/', callback);
};

Ascan.prototype.optionPromptToClearFinishedScans = function (callback) {
  this.api.request('/ascan/view/optionPromptToClearFinishedScans/', callback);
};

Ascan.prototype.optionDefaultPolicy = function (callback) {
  this.api.request('/ascan/view/optionDefaultPolicy/', callback);
};

Ascan.prototype.optionAttackPolicy = function (callback) {
  this.api.request('/ascan/view/optionAttackPolicy/', callback);
};

Ascan.prototype.scan = function (url, recurse, inscopeonly, scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/scan/', {'url' : url, 'recurse' : recurse, 'inScopeOnly' : inscopeonly, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.pause = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/pause/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

Ascan.prototype.resume = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/resume/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

Ascan.prototype.stop = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/stop/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

Ascan.prototype.removeScan = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/removeScan/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

Ascan.prototype.pauseAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/pauseAllScans/', {'apikey' : apikey}, callback);
};

Ascan.prototype.resumeAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/resumeAllScans/', {'apikey' : apikey}, callback);
};

Ascan.prototype.stopAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/stopAllScans/', {'apikey' : apikey}, callback);
};

Ascan.prototype.removeAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/removeAllScans/', {'apikey' : apikey}, callback);
};

Ascan.prototype.clearExcludedFromScan = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/clearExcludedFromScan/', {'apikey' : apikey}, callback);
};

Ascan.prototype.excludeFromScan = function (regex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/excludeFromScan/', {'regex' : regex, 'apikey' : apikey}, callback);
};

Ascan.prototype.enableAllScanners = function (scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/enableAllScanners/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.disableAllScanners = function (scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/disableAllScanners/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.enableScanners = function (ids, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/enableScanners/', {'ids' : ids, 'apikey' : apikey}, callback);
};

Ascan.prototype.disableScanners = function (ids, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/disableScanners/', {'ids' : ids, 'apikey' : apikey}, callback);
};

Ascan.prototype.setEnabledPolicies = function (ids, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setEnabledPolicies/', {'ids' : ids, 'apikey' : apikey}, callback);
};

Ascan.prototype.setPolicyAttackStrength = function (id, attackstrength, scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setPolicyAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.setPolicyAlertThreshold = function (id, attackstrength, scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setPolicyAlertThreshold/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.setScannerAttackStrength = function (id, attackstrength, scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setScannerAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.setScannerAlertThreshold = function (id, attackstrength, scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setScannerAlertThreshold/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.addScanPolicy = function (scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/addScanPolicy/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.removeScanPolicy = function (scanpolicyname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/removeScanPolicy/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionDefaultPolicy = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionDefaultPolicy/', {'String' : string, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionAttackPolicy = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionAttackPolicy/', {'String' : string, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionMaxScansInUI = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionMaxScansInUI/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionShowAdvancedDialog = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionShowAdvancedDialog/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionThreadPerHost = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionThreadPerHost/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionHostPerScan = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionHostPerScan/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionMaxResultsToList = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionMaxResultsToList/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionDelayInMs = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionDelayInMs/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionHandleAntiCSRFTokens = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionHandleAntiCSRFTokens/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionRescanInAttackMode = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionRescanInAttackMode/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionPromptInAttackMode = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionPromptInAttackMode/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionTargetParamsInjectable = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionTargetParamsInjectable/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionTargetParamsEnabledRPC = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionTargetParamsEnabledRPC/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionPromptToClearFinishedScans = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionPromptToClearFinishedScans/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

module.exports = Ascan;
