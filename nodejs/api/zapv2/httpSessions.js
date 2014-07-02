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
function HttpSessions(clientApi) {
  this.api = clientApi;
}

HttpSessions.prototype.sessions = function (site, session, callback) {
  this.api.request('/httpSessions/view/sessions/', {'site' : site, 'session' : session}, callback);
};

HttpSessions.prototype.activeSession = function (site, callback) {
  this.api.request('/httpSessions/view/activeSession/', {'site' : site}, callback);
};

HttpSessions.prototype.sessionTokens = function (site, callback) {
  this.api.request('/httpSessions/view/sessionTokens/', {'site' : site}, callback);
};

HttpSessions.prototype.createEmptySession = function (site, session, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/createEmptySession/', {'site' : site, 'session' : session, 'apikey' : apikey}, callback);
};

HttpSessions.prototype.removeSession = function (site, session, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/removeSession/', {'site' : site, 'session' : session, 'apikey' : apikey}, callback);
};

HttpSessions.prototype.setActiveSession = function (site, session, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/setActiveSession/', {'site' : site, 'session' : session, 'apikey' : apikey}, callback);
};

HttpSessions.prototype.unsetActiveSession = function (site, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/unsetActiveSession/', {'site' : site, 'apikey' : apikey}, callback);
};

HttpSessions.prototype.addSessionToken = function (site, sessiontoken, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/addSessionToken/', {'site' : site, 'sessionToken' : sessiontoken, 'apikey' : apikey}, callback);
};

HttpSessions.prototype.removeSessionToken = function (site, sessiontoken, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/removeSessionToken/', {'site' : site, 'sessionToken' : sessiontoken, 'apikey' : apikey}, callback);
};

HttpSessions.prototype.setSessionTokenValue = function (site, session, sessiontoken, tokenvalue, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/setSessionTokenValue/', {'site' : site, 'session' : session, 'sessionToken' : sessiontoken, 'tokenValue' : tokenvalue, 'apikey' : apikey}, callback);
};

HttpSessions.prototype.renameSession = function (site, oldsessionname, newsessionname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/renameSession/', {'site' : site, 'oldSessionName' : oldsessionname, 'newSessionName' : newsessionname, 'apikey' : apikey}, callback);
};

module.exports = HttpSessions;
