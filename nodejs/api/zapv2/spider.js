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
function Spider(clientApi) {
  this.api = clientApi;
}

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.status = function (scanid, callback) {
  this.api.request('/spider/view/status/', {'scanId' : scanid}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.results = function (scanid, callback) {
  this.api.request('/spider/view/results/', {'scanId' : scanid}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.fullResults = function (scanid, callback) {
  this.api.request('/spider/view/fullResults/', {'scanId' : scanid}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.scans = function (callback) {
  this.api.request('/spider/view/scans/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.excludedFromScan = function (callback) {
  this.api.request('/spider/view/excludedFromScan/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionMaxDepth = function (callback) {
  this.api.request('/spider/view/optionMaxDepth/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionScopeText = function (callback) {
  this.api.request('/spider/view/optionScopeText/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionScope = function (callback) {
  this.api.request('/spider/view/optionScope/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionThreadCount = function (callback) {
  this.api.request('/spider/view/optionThreadCount/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionPostForm = function (callback) {
  this.api.request('/spider/view/optionPostForm/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionProcessForm = function (callback) {
  this.api.request('/spider/view/optionProcessForm/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionSkipURLString = function (callback) {
  this.api.request('/spider/view/optionSkipURLString/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionRequestWaitTime = function (callback) {
  this.api.request('/spider/view/optionRequestWaitTime/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionUserAgent = function (callback) {
  this.api.request('/spider/view/optionUserAgent/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionParseComments = function (callback) {
  this.api.request('/spider/view/optionParseComments/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionParseRobotsTxt = function (callback) {
  this.api.request('/spider/view/optionParseRobotsTxt/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionParseSitemapXml = function (callback) {
  this.api.request('/spider/view/optionParseSitemapXml/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionParseSVNEntries = function (callback) {
  this.api.request('/spider/view/optionParseSVNEntries/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionParseGit = function (callback) {
  this.api.request('/spider/view/optionParseGit/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionHandleParameters = function (callback) {
  this.api.request('/spider/view/optionHandleParameters/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionHandleODataParametersVisited = function (callback) {
  this.api.request('/spider/view/optionHandleODataParametersVisited/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionDomainsAlwaysInScope = function (callback) {
  this.api.request('/spider/view/optionDomainsAlwaysInScope/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionDomainsAlwaysInScopeEnabled = function (callback) {
  this.api.request('/spider/view/optionDomainsAlwaysInScopeEnabled/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionMaxScansInUI = function (callback) {
  this.api.request('/spider/view/optionMaxScansInUI/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionShowAdvancedDialog = function (callback) {
  this.api.request('/spider/view/optionShowAdvancedDialog/', callback);
};

/**
 * Sets whether or not the 'Referer' header should be sent while spidering
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.optionSendRefererHeader = function (callback) {
  this.api.request('/spider/view/optionSendRefererHeader/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.scan = function (url, maxchildren, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/scan/', {'url' : url, 'maxChildren' : maxchildren, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.scanAsUser = function (url, contextid, userid, maxchildren, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/scanAsUser/', {'url' : url, 'contextId' : contextid, 'userId' : userid, 'maxChildren' : maxchildren, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.pause = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/pause/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.resume = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/resume/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.stop = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/stop/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.removeScan = function (scanid, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/removeScan/', {'scanId' : scanid, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.pauseAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/pauseAllScans/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.resumeAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/resumeAllScans/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.stopAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/stopAllScans/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.removeAllScans = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/removeAllScans/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.clearExcludedFromScan = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/clearExcludedFromScan/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.excludeFromScan = function (regex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/excludeFromScan/', {'regex' : regex, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionScopeString = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionScopeString/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionSkipURLString = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionSkipURLString/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionUserAgent = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionUserAgent/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionHandleParameters = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionHandleParameters/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionMaxDepth = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionMaxDepth/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionThreadCount = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionThreadCount/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionPostForm = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionPostForm/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionProcessForm = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionProcessForm/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionRequestWaitTime = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionRequestWaitTime/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionParseComments = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseComments/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionParseRobotsTxt = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseRobotsTxt/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionParseSitemapXml = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseSitemapXml/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionParseSVNEntries = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseSVNEntries/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionParseGit = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionParseGit/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionHandleODataParametersVisited = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionHandleODataParametersVisited/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionMaxScansInUI = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionMaxScansInUI/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionShowAdvancedDialog = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionShowAdvancedDialog/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Spider.prototype.setOptionSendRefererHeader = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/spider/action/setOptionSendRefererHeader/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

module.exports = Spider;
