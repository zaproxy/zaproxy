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
function Authentication(clientApi) {
  this.api = clientApi;
}

Authentication.prototype.getSupportedAuthenticationMethods = function (callback) {
  this.api.request('/authentication/view/getSupportedAuthenticationMethods/', callback);
};

Authentication.prototype.getAuthenticationMethodConfigParams = function (authmethodname, callback) {
  this.api.request('/authentication/view/getAuthenticationMethodConfigParams/', {'authMethodName' : authmethodname}, callback);
};

Authentication.prototype.getAuthenticationMethod = function (contextid, callback) {
  this.api.request('/authentication/view/getAuthenticationMethod/', {'contextId' : contextid}, callback);
};

Authentication.prototype.getLoggedInIndicator = function (contextid, callback) {
  this.api.request('/authentication/view/getLoggedInIndicator/', {'contextId' : contextid}, callback);
};

Authentication.prototype.getLoggedOutIndicator = function (contextid, callback) {
  this.api.request('/authentication/view/getLoggedOutIndicator/', {'contextId' : contextid}, callback);
};

Authentication.prototype.setAuthenticationMethod = function (contextid, authmethodname, authmethodconfigparams, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/authentication/action/setAuthenticationMethod/', {'contextId' : contextid, 'authMethodName' : authmethodname, 'authMethodConfigParams' : authmethodconfigparams, 'apikey' : apikey}, callback);
};

Authentication.prototype.setLoggedInIndicator = function (contextid, loggedinindicatorregex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/authentication/action/setLoggedInIndicator/', {'contextId' : contextid, 'loggedInIndicatorRegex' : loggedinindicatorregex, 'apikey' : apikey}, callback);
};

Authentication.prototype.setLoggedOutIndicator = function (contextid, loggedoutindicatorregex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/authentication/action/setLoggedOutIndicator/', {'contextId' : contextid, 'loggedOutIndicatorRegex' : loggedoutindicatorregex, 'apikey' : apikey}, callback);
};

module.exports = Authentication;
