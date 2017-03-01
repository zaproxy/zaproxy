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
function HttpSessions(clientApi) {
  this.api = clientApi;
}

/**
 * Gets the sessions of the given site. Optionally returning just the session with the given name.
 **/
HttpSessions.prototype.sessions = function (site, session, callback) {
  var params = {'site' : site};
  if (session && session !== null) {
    params['session'] = session;
  }
  this.api.request('/httpSessions/view/sessions/', params, callback);
};

/**
 * Gets the name of the active session for the given site.
 **/
HttpSessions.prototype.activeSession = function (site, callback) {
  this.api.request('/httpSessions/view/activeSession/', {'site' : site}, callback);
};

/**
 * Gets the names of the session tokens for the given site.
 **/
HttpSessions.prototype.sessionTokens = function (site, callback) {
  this.api.request('/httpSessions/view/sessionTokens/', {'site' : site}, callback);
};

/**
 * Creates an empty session for the given site. Optionally with the given name.
 **/
HttpSessions.prototype.createEmptySession = function (site, session, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'site' : site, 'apikey' : apikey};
  if (session && session !== null) {
    params['session'] = session;
  }
  this.api.request('/httpSessions/action/createEmptySession/', params, callback);
};

/**
 * Removes the session from the given site.
 **/
HttpSessions.prototype.removeSession = function (site, session, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/removeSession/', {'site' : site, 'session' : session, 'apikey' : apikey}, callback);
};

/**
 * Sets the given session as active for the given site.
 **/
HttpSessions.prototype.setActiveSession = function (site, session, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/setActiveSession/', {'site' : site, 'session' : session, 'apikey' : apikey}, callback);
};

/**
 * Unsets the active session of the given site.
 **/
HttpSessions.prototype.unsetActiveSession = function (site, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/unsetActiveSession/', {'site' : site, 'apikey' : apikey}, callback);
};

/**
 * Adds the session token to the given site.
 **/
HttpSessions.prototype.addSessionToken = function (site, sessiontoken, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/addSessionToken/', {'site' : site, 'sessionToken' : sessiontoken, 'apikey' : apikey}, callback);
};

/**
 * Removes the session token from the given site.
 **/
HttpSessions.prototype.removeSessionToken = function (site, sessiontoken, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/removeSessionToken/', {'site' : site, 'sessionToken' : sessiontoken, 'apikey' : apikey}, callback);
};

/**
 * Sets the value of the session token of the given session for the given site.
 **/
HttpSessions.prototype.setSessionTokenValue = function (site, session, sessiontoken, tokenvalue, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/setSessionTokenValue/', {'site' : site, 'session' : session, 'sessionToken' : sessiontoken, 'tokenValue' : tokenvalue, 'apikey' : apikey}, callback);
};

/**
 * Renames the session of the given site.
 **/
HttpSessions.prototype.renameSession = function (site, oldsessionname, newsessionname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/httpSessions/action/renameSession/', {'site' : site, 'oldSessionName' : oldsessionname, 'newSessionName' : newsessionname, 'apikey' : apikey}, callback);
};

module.exports = HttpSessions;
