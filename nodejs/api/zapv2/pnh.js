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
function Pnh(clientApi) {
  this.api = clientApi;
}

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.monitor = function (id, message, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pnh/action/monitor/', {'id' : id, 'message' : message, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.oracle = function (id, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pnh/action/oracle/', {'id' : id, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.startMonitoring = function (url, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pnh/action/startMonitoring/', {'url' : url, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.stopMonitoring = function (id, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/pnh/action/stopMonitoring/', {'id' : id, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.pnh = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/pnh/other/pnh/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.manifest = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/pnh/other/manifest/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.service = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/pnh/other/service/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Pnh.prototype.fx_pnhxpi = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/pnh/other/fx_pnh.xpi/', {'apikey' : apikey}, callback);
};

module.exports = Pnh;
