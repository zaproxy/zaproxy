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
function Selenium(clientApi) {
  this.api = clientApi;
}

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Selenium.prototype.optionChromeDriverPath = function (callback) {
  this.api.request('/selenium/view/optionChromeDriverPath/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Selenium.prototype.optionIeDriverPath = function (callback) {
  this.api.request('/selenium/view/optionIeDriverPath/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Selenium.prototype.optionPhantomJsBinaryPath = function (callback) {
  this.api.request('/selenium/view/optionPhantomJsBinaryPath/', callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Selenium.prototype.setOptionChromeDriverPath = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/selenium/action/setOptionChromeDriverPath/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Selenium.prototype.setOptionIeDriverPath = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/selenium/action/setOptionIeDriverPath/', {'String' : string, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
Selenium.prototype.setOptionPhantomJsBinaryPath = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/selenium/action/setOptionPhantomJsBinaryPath/', {'String' : string, 'apikey' : apikey}, callback);
};

module.exports = Selenium;
