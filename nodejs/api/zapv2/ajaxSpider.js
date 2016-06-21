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
function AjaxSpider(clientApi) {
  this.api = clientApi;
}

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.status = function (callback) {
  this.api.request('/ajaxSpider/view/status/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.results = function (start, count, callback) {
  var params = {};
  if (start && start !== null) {
    params['start'] = start;
  }
  if (count && count !== null) {
    params['count'] = count;
  }
  this.api.request('/ajaxSpider/view/results/', params, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.numberOfResults = function (callback) {
  this.api.request('/ajaxSpider/view/numberOfResults/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionBrowserId = function (callback) {
  this.api.request('/ajaxSpider/view/optionBrowserId/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionEventWait = function (callback) {
  this.api.request('/ajaxSpider/view/optionEventWait/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionMaxCrawlDepth = function (callback) {
  this.api.request('/ajaxSpider/view/optionMaxCrawlDepth/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionMaxCrawlStates = function (callback) {
  this.api.request('/ajaxSpider/view/optionMaxCrawlStates/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionMaxDuration = function (callback) {
  this.api.request('/ajaxSpider/view/optionMaxDuration/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionNumberOfBrowsers = function (callback) {
  this.api.request('/ajaxSpider/view/optionNumberOfBrowsers/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionReloadWait = function (callback) {
  this.api.request('/ajaxSpider/view/optionReloadWait/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionClickDefaultElems = function (callback) {
  this.api.request('/ajaxSpider/view/optionClickDefaultElems/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionClickElemsOnce = function (callback) {
  this.api.request('/ajaxSpider/view/optionClickElemsOnce/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.optionRandomInputs = function (callback) {
  this.api.request('/ajaxSpider/view/optionRandomInputs/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.scan = function (url, inscope, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'url' : url, 'apikey' : apikey};
  if (inscope && inscope !== null) {
    params['inScope'] = inscope;
  }
  this.api.request('/ajaxSpider/action/scan/', params, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.stop = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/stop/', {'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionBrowserId = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionBrowserId/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionClickDefaultElems = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionClickDefaultElems/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionClickElemsOnce = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionClickElemsOnce/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionEventWait = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionEventWait/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionMaxCrawlDepth = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionMaxCrawlDepth/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionMaxCrawlStates = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionMaxCrawlStates/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionMaxDuration = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionMaxDuration/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionNumberOfBrowsers = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionNumberOfBrowsers/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionRandomInputs = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionRandomInputs/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
AjaxSpider.prototype.setOptionReloadWait = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/setOptionReloadWait/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

module.exports = AjaxSpider;
