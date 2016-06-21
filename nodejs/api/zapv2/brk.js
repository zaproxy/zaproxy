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
function Break(clientApi) {
  this.api = clientApi;
}

Break.prototype.brk = function (type, scope, state, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/break/action/break/', {'type' : type, 'scope' : scope, 'state' : state, 'apikey' : apikey}, callback);
};

Break.prototype.addHttpBreakpoint = function (string, location, match, inverse, ignorecase, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/break/action/addHttpBreakpoint/', {'string' : string, 'location' : location, 'match' : match, 'inverse' : inverse, 'ignorecase' : ignorecase, 'apikey' : apikey}, callback);
};

Break.prototype.removeHttpBreakpoint = function (string, location, match, inverse, ignorecase, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/break/action/removeHttpBreakpoint/', {'string' : string, 'location' : location, 'match' : match, 'inverse' : inverse, 'ignorecase' : ignorecase, 'apikey' : apikey}, callback);
};

module.exports = Break;
