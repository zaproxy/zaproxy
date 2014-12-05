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
function Context(clientApi) {
  this.api = clientApi;
}

/**
 * List context names of current session
 **/
Context.prototype.contextList = function (callback) {
  this.api.request('/context/view/contextList/', callback);
};

/**
 * List excluded regexs for context
 **/
Context.prototype.excludeRegexs = function (contextname, callback) {
  this.api.request('/context/view/excludeRegexs/', {'contextName' : contextname}, callback);
};

/**
 * List included regexs for context
 **/
Context.prototype.includeRegexs = function (contextname, callback) {
  this.api.request('/context/view/includeRegexs/', {'contextName' : contextname}, callback);
};

/**
 * List the information about the named context
 **/
Context.prototype.context = function (contextname, callback) {
  this.api.request('/context/view/context/', {'contextName' : contextname}, callback);
};

/**
 * Add exclude regex to context
 **/
Context.prototype.excludeFromContext = function (contextname, regex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/context/action/excludeFromContext/', {'contextName' : contextname, 'regex' : regex, 'apikey' : apikey}, callback);
};

/**
 * Add include regex to context
 **/
Context.prototype.includeInContext = function (contextname, regex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/context/action/includeInContext/', {'contextName' : contextname, 'regex' : regex, 'apikey' : apikey}, callback);
};

/**
 * Creates a new context in the current session
 **/
Context.prototype.newContext = function (contextname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/context/action/newContext/', {'contextName' : contextname, 'apikey' : apikey}, callback);
};

Context.prototype.exportContext = function (contextname, contextfile, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/context/action/exportContext/', {'contextName' : contextname, 'contextFile' : contextfile, 'apikey' : apikey}, callback);
};

Context.prototype.importContext = function (contextfile, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/context/action/importContext/', {'contextFile' : contextfile, 'apikey' : apikey}, callback);
};

/**
 * Sets a context to in scope (contexts are in scope by default)
 **/
Context.prototype.setContextInScope = function (contextname, booleaninscope, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/context/action/setContextInScope/', {'contextName' : contextname, 'booleanInScope' : booleaninscope, 'apikey' : apikey}, callback);
};

module.exports = Context;
