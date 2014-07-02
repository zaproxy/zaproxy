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

Ascan.prototype.status = function (callback) {
  this.api.request('/ascan/view/status/', callback);
};

Ascan.prototype.excludedFromScan = function (callback) {
  this.api.request('/ascan/view/excludedFromScan/', callback);
};

Ascan.prototype.scanners = function (policyid, callback) {
  this.api.request('/ascan/view/scanners/', {'policyId' : policyid}, callback);
};

Ascan.prototype.policies = function (callback) {
  this.api.request('/ascan/view/policies/', callback);
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

Ascan.prototype.optionAlertThreshold = function (callback) {
  this.api.request('/ascan/view/optionAlertThreshold/', callback);
};

Ascan.prototype.optionAttackStrength = function (callback) {
  this.api.request('/ascan/view/optionAttackStrength/', callback);
};

Ascan.prototype.optionTargetParamsInjectable = function (callback) {
  this.api.request('/ascan/view/optionTargetParamsInjectable/', callback);
};

Ascan.prototype.optionTargetParamsEnabledRPC = function (callback) {
  this.api.request('/ascan/view/optionTargetParamsEnabledRPC/', callback);
};

Ascan.prototype.scan = function (url, recurse, inscopeonly, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/scan/', {'url' : url, 'recurse' : recurse, 'inScopeOnly' : inscopeonly, 'apikey' : apikey}, callback);
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

Ascan.prototype.enableAllScanners = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/enableAllScanners/', {'apikey' : apikey}, callback);
};

Ascan.prototype.disableAllScanners = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/disableAllScanners/', {'apikey' : apikey}, callback);
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

Ascan.prototype.setPolicyAttackStrength = function (id, attackstrength, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setPolicyAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'apikey' : apikey}, callback);
};

Ascan.prototype.setPolicyAlertThreshold = function (id, alertthreshold, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setPolicyAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'apikey' : apikey}, callback);
};

Ascan.prototype.setScannerAttackStrength = function (id, attackstrength, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setScannerAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'apikey' : apikey}, callback);
};

Ascan.prototype.setScannerAlertThreshold = function (id, alertthreshold, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setScannerAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionAlertThreshold = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionAlertThreshold/', {'String' : string, 'apikey' : apikey}, callback);
};

Ascan.prototype.setOptionAttackStrength = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ascan/action/setOptionAttackStrength/', {'String' : string, 'apikey' : apikey}, callback);
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

module.exports = Ascan;
