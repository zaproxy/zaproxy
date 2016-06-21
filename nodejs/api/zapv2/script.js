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
function Script(clientApi) {
  this.api = clientApi;
}

/**
 * Lists the script engines available
 **/
Script.prototype.listEngines = function (callback) {
  this.api.request('/script/view/listEngines/', callback);
};

/**
 * Lists the scripts available, with its engine, name, description, type and error state.
 **/
Script.prototype.listScripts = function (callback) {
  this.api.request('/script/view/listScripts/', callback);
};

/**
 * Enables the script with the given name
 **/
Script.prototype.enable = function (scriptname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/script/action/enable/', {'scriptName' : scriptname, 'apikey' : apikey}, callback);
};

/**
 * Disables the script with the given name
 **/
Script.prototype.disable = function (scriptname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/script/action/disable/', {'scriptName' : scriptname, 'apikey' : apikey}, callback);
};

/**
 * Loads a script into ZAP from the given local file, with the given name, type and engine, optionally with a description
 **/
Script.prototype.load = function (scriptname, scripttype, scriptengine, filename, scriptdescription, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'scriptName' : scriptname, 'scriptType' : scripttype, 'scriptEngine' : scriptengine, 'fileName' : filename, 'apikey' : apikey};
  if (scriptdescription && scriptdescription !== null) {
    params['scriptDescription'] = scriptdescription;
  }
  this.api.request('/script/action/load/', params, callback);
};

/**
 * Removes the script with the given name
 **/
Script.prototype.remove = function (scriptname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/script/action/remove/', {'scriptName' : scriptname, 'apikey' : apikey}, callback);
};

/**
 * Runs the stand alone script with the give name
 **/
Script.prototype.runStandAloneScript = function (scriptname, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/script/action/runStandAloneScript/', {'scriptName' : scriptname, 'apikey' : apikey}, callback);
};

module.exports = Script;
