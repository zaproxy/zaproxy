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
function Stats(clientApi) {
  this.api = clientApi;
}

/**
 * Statistics
 **/
Stats.prototype.stats = function (keyprefix, callback) {
  var params = {};
  if (keyprefix && keyprefix !== null) {
    params['keyPrefix'] = keyprefix;
  }
  this.api.request('/stats/view/stats/', params, callback);
};

/**
 * Gets all of the site based statistics, optionally filtered by a key prefix
 **/
Stats.prototype.allSitesStats = function (keyprefix, callback) {
  var params = {};
  if (keyprefix && keyprefix !== null) {
    params['keyPrefix'] = keyprefix;
  }
  this.api.request('/stats/view/allSitesStats/', params, callback);
};

/**
 * Gets all of the global statistics, optionally filtered by a key prefix
 **/
Stats.prototype.siteStats = function (site, keyprefix, callback) {
  var params = {'site' : site};
  if (keyprefix && keyprefix !== null) {
    params['keyPrefix'] = keyprefix;
  }
  this.api.request('/stats/view/siteStats/', params, callback);
};

/**
 * Gets the Statsd service hostname
 **/
Stats.prototype.optionStatsdHost = function (callback) {
  this.api.request('/stats/view/optionStatsdHost/', callback);
};

/**
 * Gets the Statsd service port
 **/
Stats.prototype.optionStatsdPort = function (callback) {
  this.api.request('/stats/view/optionStatsdPort/', callback);
};

/**
 * Gets the prefix to be applied to all stats sent to the configured Statsd service
 **/
Stats.prototype.optionStatsdPrefix = function (callback) {
  this.api.request('/stats/view/optionStatsdPrefix/', callback);
};

/**
 * Returns 'true' if in memory statistics are enabled, otherwise returns 'false'
 **/
Stats.prototype.optionInMemoryEnabled = function (callback) {
  this.api.request('/stats/view/optionInMemoryEnabled/', callback);
};

/**
 * Returns 'true' if a Statsd server has been correctly configured, otherwise returns 'false'
 **/
Stats.prototype.optionStatsdEnabled = function (callback) {
  this.api.request('/stats/view/optionStatsdEnabled/', callback);
};

/**
 * Clears all of the statistics
 **/
Stats.prototype.clearStats = function (keyprefix, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'apikey' : apikey};
  if (keyprefix && keyprefix !== null) {
    params['keyPrefix'] = keyprefix;
  }
  this.api.request('/stats/action/clearStats/', params, callback);
};

/**
 * Sets the Statsd service hostname, supply an empty string to stop using a Statsd service
 **/
Stats.prototype.setOptionStatsdHost = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/stats/action/setOptionStatsdHost/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * Sets the prefix to be applied to all stats sent to the configured Statsd service
 **/
Stats.prototype.setOptionStatsdPrefix = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/stats/action/setOptionStatsdPrefix/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * Sets whether in memory statistics are enabled
 **/
Stats.prototype.setOptionInMemoryEnabled = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/stats/action/setOptionInMemoryEnabled/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * Sets the Statsd service port
 **/
Stats.prototype.setOptionStatsdPort = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/stats/action/setOptionStatsdPort/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

module.exports = Stats;
