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
function Core(clientApi) {
  this.api = clientApi;
}

Core.prototype.alert = function (id, callback) {
  this.api.request('/core/view/alert/', {'id' : id}, callback);
};

Core.prototype.alerts = function (baseurl, start, count, callback) {
  this.api.request('/core/view/alerts/', {'baseurl' : baseurl, 'start' : start, 'count' : count}, callback);
};

Core.prototype.numberOfAlerts = function (baseurl, callback) {
  this.api.request('/core/view/numberOfAlerts/', {'baseurl' : baseurl}, callback);
};

Core.prototype.hosts = function (callback) {
  this.api.request('/core/view/hosts/', callback);
};

Core.prototype.sites = function (callback) {
  this.api.request('/core/view/sites/', callback);
};

Core.prototype.urls = function (callback) {
  this.api.request('/core/view/urls/', callback);
};

Core.prototype.message = function (id, callback) {
  this.api.request('/core/view/message/', {'id' : id}, callback);
};

Core.prototype.messages = function (baseurl, start, count, callback) {
  this.api.request('/core/view/messages/', {'baseurl' : baseurl, 'start' : start, 'count' : count}, callback);
};

Core.prototype.numberOfMessages = function (baseurl, callback) {
  this.api.request('/core/view/numberOfMessages/', {'baseurl' : baseurl}, callback);
};

Core.prototype.version = function (callback) {
  this.api.request('/core/view/version/', callback);
};

Core.prototype.excludedFromProxy = function (callback) {
  this.api.request('/core/view/excludedFromProxy/', callback);
};

Core.prototype.homeDirectory = function (callback) {
  this.api.request('/core/view/homeDirectory/', callback);
};

Core.prototype.optionHttpStateEnabled = function (callback) {
  this.api.request('/core/view/optionHttpStateEnabled/', callback);
};

Core.prototype.optionUseProxyChain = function (callback) {
  this.api.request('/core/view/optionUseProxyChain/', callback);
};

Core.prototype.optionProxyChainName = function (callback) {
  this.api.request('/core/view/optionProxyChainName/', callback);
};

Core.prototype.optionProxyChainPort = function (callback) {
  this.api.request('/core/view/optionProxyChainPort/', callback);
};

Core.prototype.optionProxyChainSkipName = function (callback) {
  this.api.request('/core/view/optionProxyChainSkipName/', callback);
};

Core.prototype.optionUseProxyChainAuth = function (callback) {
  this.api.request('/core/view/optionUseProxyChainAuth/', callback);
};

Core.prototype.optionProxyChainUserName = function (callback) {
  this.api.request('/core/view/optionProxyChainUserName/', callback);
};

Core.prototype.optionProxyChainRealm = function (callback) {
  this.api.request('/core/view/optionProxyChainRealm/', callback);
};

Core.prototype.optionProxyChainPassword = function (callback) {
  this.api.request('/core/view/optionProxyChainPassword/', callback);
};

Core.prototype.optionProxyChainPrompt = function (callback) {
  this.api.request('/core/view/optionProxyChainPrompt/', callback);
};

Core.prototype.optionListAuth = function (callback) {
  this.api.request('/core/view/optionListAuth/', callback);
};

Core.prototype.optionListAuthEnabled = function (callback) {
  this.api.request('/core/view/optionListAuthEnabled/', callback);
};

Core.prototype.optionHttpState = function (callback) {
  this.api.request('/core/view/optionHttpState/', callback);
};

Core.prototype.optionTimeoutInSecs = function (callback) {
  this.api.request('/core/view/optionTimeoutInSecs/', callback);
};

Core.prototype.optionSingleCookieRequestHeader = function (callback) {
  this.api.request('/core/view/optionSingleCookieRequestHeader/', callback);
};

Core.prototype.optionProxyExcludedDomains = function (callback) {
  this.api.request('/core/view/optionProxyExcludedDomains/', callback);
};

Core.prototype.optionProxyExcludedDomainsEnabled = function (callback) {
  this.api.request('/core/view/optionProxyExcludedDomainsEnabled/', callback);
};

/**
 * Shuts down ZAP
 **/
Core.prototype.shutdown = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/shutdown/', {'apikey' : apikey}, callback);
};

Core.prototype.newSession = function (name, overwrite, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/newSession/', {'name' : name, 'overwrite' : overwrite, 'apikey' : apikey}, callback);
};

Core.prototype.loadSession = function (name, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/loadSession/', {'name' : name, 'apikey' : apikey}, callback);
};

Core.prototype.saveSession = function (name, overwrite, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/saveSession/', {'name' : name, 'overwrite' : overwrite, 'apikey' : apikey}, callback);
};

Core.prototype.snapshotSession = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/snapshotSession/', {'apikey' : apikey}, callback);
};

Core.prototype.clearExcludedFromProxy = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/clearExcludedFromProxy/', {'apikey' : apikey}, callback);
};

Core.prototype.excludeFromProxy = function (regex, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/excludeFromProxy/', {'regex' : regex, 'apikey' : apikey}, callback);
};

Core.prototype.setHomeDirectory = function (dir, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setHomeDirectory/', {'dir' : dir, 'apikey' : apikey}, callback);
};

Core.prototype.generateRootCA = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/generateRootCA/', {'apikey' : apikey}, callback);
};

Core.prototype.sendRequest = function (request, followredirects, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/sendRequest/', {'request' : request, 'followRedirects' : followredirects, 'apikey' : apikey}, callback);
};

Core.prototype.deleteAllAlerts = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/deleteAllAlerts/', {'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainName = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainName/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainRealm = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainRealm/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainUserName = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainUserName/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainPassword = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainPassword/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainSkipName = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainSkipName/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionHttpStateEnabled = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionHttpStateEnabled/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainPort = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainPort/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainPrompt = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainPrompt/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionTimeoutInSecs = function (integer, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionTimeoutInSecs/', {'Integer' : integer, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionUseProxyChain = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionUseProxyChain/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionUseProxyChainAuth = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionUseProxyChainAuth/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionSingleCookieRequestHeader = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionSingleCookieRequestHeader/', {'Boolean' : bool, 'apikey' : apikey}, callback);
};

Core.prototype.proxypac = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/proxy.pac/', {'apikey' : apikey}, callback);
};

Core.prototype.rootcert = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/rootcert/', {'apikey' : apikey}, callback);
};

Core.prototype.setproxy = function (proxy, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/setproxy/', {'proxy' : proxy, 'apikey' : apikey}, callback);
};

Core.prototype.xmlreport = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/xmlreport/', {'apikey' : apikey}, callback);
};

Core.prototype.messagesHar = function (baseurl, start, count, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/messagesHar/', {'baseurl' : baseurl, 'start' : start, 'count' : count, 'apikey' : apikey}, callback);
};

Core.prototype.sendHarRequest = function (request, followredirects, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/sendHarRequest/', {'request' : request, 'followRedirects' : followredirects, 'apikey' : apikey}, callback);
};

module.exports = Core;
