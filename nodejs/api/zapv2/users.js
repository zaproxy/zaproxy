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
function Users(clientApi) {
  this.api = clientApi;
}

Users.prototype.usersList = function (contextid, callback) {
  var params = {};
  if (contextid && contextid !== null) {
    params['contextId'] = contextid;
  }
  this.api.request('/users/view/usersList/', params, callback);
};

Users.prototype.getUserById = function (contextid, userid, callback) {
  var params = {};
  if (contextid && contextid !== null) {
    params['contextId'] = contextid;
  }
  if (userid && userid !== null) {
    params['userId'] = userid;
  }
  this.api.request('/users/view/getUserById/', params, callback);
};

Users.prototype.getAuthenticationCredentialsConfigParams = function (contextid, callback) {
  this.api.request('/users/view/getAuthenticationCredentialsConfigParams/', {'contextId' : contextid}, callback);
};

Users.prototype.getAuthenticationCredentials = function (contextid, userid, callback) {
  this.api.request('/users/view/getAuthenticationCredentials/', {'contextId' : contextid, 'userId' : userid}, callback);
};

Users.prototype.newUser = function (contextid, name, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/users/action/newUser/', {'contextId' : contextid, 'name' : name, 'apikey' : apikey}, callback);
};

Users.prototype.removeUser = function (contextid, userid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/users/action/removeUser/', {'contextId' : contextid, 'userId' : userid, 'apikey' : apikey}, callback);
};

Users.prototype.setUserEnabled = function (contextid, userid, enabled, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/users/action/setUserEnabled/', {'contextId' : contextid, 'userId' : userid, 'enabled' : enabled, 'apikey' : apikey}, callback);
};

Users.prototype.setUserName = function (contextid, userid, name, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/users/action/setUserName/', {'contextId' : contextid, 'userId' : userid, 'name' : name, 'apikey' : apikey}, callback);
};

Users.prototype.setAuthenticationCredentials = function (contextid, userid, authcredentialsconfigparams, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'contextId' : contextid, 'userId' : userid, 'apikey' : apikey};
  if (authcredentialsconfigparams && authcredentialsconfigparams !== null) {
    params['authCredentialsConfigParams'] = authcredentialsconfigparams;
  }
  this.api.request('/users/action/setAuthenticationCredentials/', params, callback);
};

module.exports = Users;
