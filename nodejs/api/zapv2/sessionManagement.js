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
function SessionManagement(clientApi) {
  this.api = clientApi;
}

SessionManagement.prototype.getSupportedSessionManagementMethods = function (callback) {
  this.api.request('/sessionManagement/view/getSupportedSessionManagementMethods/', callback);
};

SessionManagement.prototype.getSessionManagementMethodConfigParams = function (methodname, callback) {
  this.api.request('/sessionManagement/view/getSessionManagementMethodConfigParams/', {'methodName' : methodname}, callback);
};

SessionManagement.prototype.getSessionManagementMethod = function (contextid, callback) {
  this.api.request('/sessionManagement/view/getSessionManagementMethod/', {'contextId' : contextid}, callback);
};

SessionManagement.prototype.setSessionManagementMethod = function (contextid, methodname, methodconfigparams, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/sessionManagement/action/setSessionManagementMethod/', {'contextId' : contextid, 'methodName' : methodname, 'methodConfigParams' : methodconfigparams, 'apikey' : apikey}, callback);
};

module.exports = SessionManagement;
