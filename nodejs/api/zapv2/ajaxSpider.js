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
  this.api.request('/ajaxSpider/view/results/', {'start' : start, 'count' : count}, callback);
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
AjaxSpider.prototype.scan = function (url, inscope, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/ajaxSpider/action/scan/', {'url' : url, 'inScope' : inscope, 'apikey' : apikey}, callback);
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

module.exports = AjaxSpider;
