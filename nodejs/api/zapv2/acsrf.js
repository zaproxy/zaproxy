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
function Acsrf(clientApi) {
  this.api = clientApi;
}

/**
 * Lists the names of all anti CSRF tokens
 **/
Acsrf.prototype.optionTokensNames = function (callback) {
  this.api.request('/acsrf/view/optionTokensNames/', callback);
};

/**
 * Adds an anti CSRF token with the given name, enabled by default
 **/
Acsrf.prototype.addOptionToken = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/acsrf/action/addOptionToken/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * Removes the anti CSRF token with the given name
 **/
Acsrf.prototype.removeOptionToken = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/acsrf/action/removeOptionToken/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * Generate a form for testing lack of anti CSRF tokens - typically invoked via ZAP
 **/
Acsrf.prototype.genForm = function (hrefid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/acsrf/other/genForm/', {'hrefId' : hrefid, 'apikey' : apikey}, callback);
};

module.exports = Acsrf;
