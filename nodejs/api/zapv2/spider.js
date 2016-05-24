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
function Spider(clientApi) {
  this.api = clientApi;
}

Spider.prototype.status = function (scanid, callback) {
  var params = {};
  if (scanid && scanid !== null) {
    params['scanId'] = scanid;
  }
  this.api.request('/spider/view/status/', params, callback);
};

Spider.prototype.results = function (scanid, callback) {
  var params = {};
  if (scanid && scanid !== null) {
    params['scanId'] = scanid;
  }
  this.api.request('/spider/view/results/', params, callback);
};

Spider.prototype.fullResults = function (scanid, callback) {
  this.api.request('/spider/view/fullResults/', {'scanId' : scanid}, callback);
};

Spider.prototype.scans = function (callback) {
  this.api.request('/spider/view/scans/', callback);
};

Spider.prototype.excludedFromScan = function (callback) {
  this.api.request('/spider/view/excludedFromScan/', callback);
};

Spider.prototype.optionDomainsAlwaysInScope = function (callback) {
  this.api.request('/spider/view/optionDomainsAlwaysInScope/', callback);
};

Spider.prototype.optionDomainsAlwaysInScopeEnabled = function (callback) {
  this.api.request('/spider/view/optionDomainsAlwaysInScopeEnabled/', callback);
};

Spider.prototype.optionHandleParameters = function (callback) {
  this.api.request('/spider/view/optionHandleParameters/', callback);
};

Spider.prototype.optionMaxDepth = function (callback) {
  this.api.request('/spider/view/optionMaxDepth/', callback);
};

Spider.prototype.optionMaxDuration = function (callback) {
  this.api.request('/spider/view/optionMaxDuration/', callback);
};

Spider.prototype.optionMaxScansInUI = function (callback) {
  this.api.request('/spider/view/optionMaxScansInUI/', callback);
};

Spider.prototype.optionRequestWaitTime = function (callback) {
  this.api.request('/spider/view/optionRequestWaitTime/', callback);
};

Spider.prototype.optionScope = function (callback) {
  this.api.request('/spider/view/optionScope/', callback);
};

Spider.prototype.optionScopeText = function (callback) {
  this.api.request('/spider/view/optionScopeText/', callback);
};

Spider.prototype.optionSkipURLString = function (callback) {
  this.api.request('/spider/view/optionSkipURLString/', callback);
};

Spider.prototype.optionThreadCount = function (callback) {
  this.api.request('/spider/view/optionThreadCount/', callback);
};

Spider.prototype.optionUserAgent = function (callback) {
  this.api.request('/spider/view/optionUserAgent/', callback);
};

Spider.prototype.optionHandleODataParametersVisited = function (callback) {
  this.api.request('/spider/view/optionHandleODataParametersVisited/', callback);
};

Spider.prototype.optionParseComments = function (callback) {
  this.api.request('/spider/view/optionParseComments/', callback);
};

Spider.prototype.optionParseGit = function (callback) {
  this.api.request('/spider/view/optionParseGit/', callback);
};

Spider.prototype.optionParseRobotsTxt = function (callback) {
  this.api.request('/spider/view/optionParseRobotsTxt/', callback);
};

Spider.prototype.optionParseSVNEntries = function (callback) {
  this.api.request('/spider/view/optionParseSVNEntries/', callback);
};

Spider.prototype.optionParseSitemapXml = function (callback) {
  this.api.request('/spider/view/optionParseSitemapXml/', callback);
};

Spider.prototype.optionPostForm = function (callback) {
  this.api.request('/spider/view/optionPostForm/', callback);
};

Spider.prototype.optionProcessForm = function (callback) {
  this.api.request('/spider/view/optionProcessForm/', callback);
};

/**
 * Sets whether or not the 'Referer' header should be sent while spidering
 **/
Spider.prototype.optionSendRefererHeader = function (callback) {
  this.api.request('/spider/view/optionSendRefererHeader/', callback);
};

Spider.prototype.optionShowAdvancedDialog = function (callback) {
  this.api.request('/spider/view/optionShowAdvancedDialog/', callback);
};

/**
 * Runs the spider against the given URL (or context). Optionally, the 'maxChildren' parameter can be set to limit the number of children scanned, the 'recurse' parameter can be used to prevent the spider from seeding recursively, the parameter 'contextName' can be used to constrain the scan to a Context and the parameter 'subtreeOnly' allows to restrict the spider under a site's subtree (using the specified 'url').
 **/
Spider.prototype.scan = function (url, maxchildren, recurse, contextname, subtreeonly, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'apikey' : apikey};
  if (url && url !== null) {
    params['url'] = url;
  }
  if (maxchildren && maxchildren !== null) {
    params['maxChildren'] = maxchildren;
  }
  if (recurse && recurse !== null) {
    params['recurse'] = recurse;
  }
  if (contextname && contextname !== null) {
    params['contextName'] = contextname;
  }
  if (subtreeonly && subtreeonly !== null) {
    params['subtreeOnly'] = subtreeonly;
  }
  this.api.request('/spider/action/scan/', params, callback);
};

/**
 * Runs the spider from the perspective of a User, obtained using the given Context ID and User ID. See 'scan' action for more details.
 **/
Spider.prototype.scanAsUser = function (contextid, userid, url, maxchildren, recurse, subtreeonly, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'contextId' : contextid, 'userId' : userid, 'apikey' : apikey};
  if (url && url !== null) {
    params['url'] = url;
  }
  if (maxchildren && maxchildren !== null) {
    params['maxChildren'] = maxchildren;
  }
  if (recurse && recurse !== null) {
    params['recurse'] = recurse;
  }
  if (subtreeonly && subtreeonly !== null) {
    params['subtreeOnly'] = subtreeonly;
  }
  this.api.request('/spider/action/scanAsUser/', params, callback);
};

Spider.prototype.pause = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/pause/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

Spider.prototype.resume = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/resume/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

Spider.prototype.stop = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'apikey' : apikey};
  if (scanid && scanid !== null) {
    params['scanId'] = scanid;
  }
  this.api.request('/spider/action/stop/', params, callback);
};

Spider.prototype.removeScan = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/removeScan/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

Spider.prototype.pauseAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/pauseAllScans/', {'apikey' : apikey}, callback);
};

Spider.prototype.resumeAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/resumeAllScans/', {'apikey' : apikey}, callback);
};

Spider.prototype.stopAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/stopAllScans/', {'apikey' : apikey}, callback);
};

Spider.prototype.removeAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/removeAllScans/', {'apikey' : apikey}, callback);
};

Spider.prototype.clearExcludedFromScan = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/clearExcludedFromScan/', {'apikey' : apikey}, callback);
};

Spider.prototype.excludeFromScan = function (regex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/excludeFromScan/', {'regex' : regex, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionHandleParameters = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionHandleParameters/', {'String' : string, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionScopeString = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionScopeString/', {'String' : string, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionSkipURLString = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionSkipURLString/', {'String' : string, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionUserAgent = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionUserAgent/', {'String' : string, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionHandleODataParametersVisited = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionHandleODataParametersVisited/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionMaxDepth = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionMaxDepth/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionMaxDuration = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionMaxDuration/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionMaxScansInUI = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionMaxScansInUI/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionParseComments = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseComments/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionParseGit = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseGit/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionParseRobotsTxt = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseRobotsTxt/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionParseSVNEntries = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseSVNEntries/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionParseSitemapXml = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseSitemapXml/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionPostForm = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionPostForm/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionProcessForm = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionProcessForm/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionRequestWaitTime = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionRequestWaitTime/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionSendRefererHeader = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionSendRefererHeader/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionShowAdvancedDialog = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionShowAdvancedDialog/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Spider.prototype.setOptionThreadCount = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionThreadCount/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

module.exports = Spider;
