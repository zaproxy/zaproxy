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
function Core(clientApi) {
  this.api = clientApi;
}

/**
 * Gets the alert with the given ID, the corresponding HTTP message can be obtained with the 'messageId' field and 'message' API method
 **/
Core.prototype.alert = function (id, callback) {
  this.api.request('/core/view/alert/', {'id' : id}, callback);
};

/**
 * Gets the alerts raised by ZAP, optionally filtering by URL and paginating with 'start' position and 'count' of alerts
 **/
Core.prototype.alerts = function (baseurl, start, count, callback) {
  var params = {};
  if (baseurl && baseurl !== null) {
    params['baseurl'] = baseurl;
  }
  if (start && start !== null) {
    params['start'] = start;
  }
  if (count && count !== null) {
    params['count'] = count;
  }
  this.api.request('/core/view/alerts/', params, callback);
};

/**
 * Gets the number of alerts, optionally filtering by URL
 **/
Core.prototype.numberOfAlerts = function (baseurl, callback) {
  var params = {};
  if (baseurl && baseurl !== null) {
    params['baseurl'] = baseurl;
  }
  this.api.request('/core/view/numberOfAlerts/', params, callback);
};

/**
 * Gets the name of the hosts accessed through/by ZAP
 **/
Core.prototype.hosts = function (callback) {
  this.api.request('/core/view/hosts/', callback);
};

/**
 * Gets the sites accessed through/by ZAP (scheme and domain)
 **/
Core.prototype.sites = function (callback) {
  this.api.request('/core/view/sites/', callback);
};

/**
 * Gets the URLs accessed through/by ZAP
 **/
Core.prototype.urls = function (callback) {
  this.api.request('/core/view/urls/', callback);
};

/**
 * Gets the HTTP message with the given ID. Returns the ID, request/response headers and bodies, cookies and note.
 **/
Core.prototype.message = function (id, callback) {
  this.api.request('/core/view/message/', {'id' : id}, callback);
};

/**
 * Gets the HTTP messages sent by ZAP, request and response, optionally filtered by URL and paginated with 'start' position and 'count' of messages
 **/
Core.prototype.messages = function (baseurl, start, count, callback) {
  var params = {};
  if (baseurl && baseurl !== null) {
    params['baseurl'] = baseurl;
  }
  if (start && start !== null) {
    params['start'] = start;
  }
  if (count && count !== null) {
    params['count'] = count;
  }
  this.api.request('/core/view/messages/', params, callback);
};

/**
 * Gets the number of messages, optionally filtering by URL
 **/
Core.prototype.numberOfMessages = function (baseurl, callback) {
  var params = {};
  if (baseurl && baseurl !== null) {
    params['baseurl'] = baseurl;
  }
  this.api.request('/core/view/numberOfMessages/', params, callback);
};

/**
 * Gets the mode
 **/
Core.prototype.mode = function (callback) {
  this.api.request('/core/view/mode/', callback);
};

/**
 * Gets ZAP version
 **/
Core.prototype.version = function (callback) {
  this.api.request('/core/view/version/', callback);
};

/**
 * Gets the regular expressions, applied to URLs, to exclude from the Proxy
 **/
Core.prototype.excludedFromProxy = function (callback) {
  this.api.request('/core/view/excludedFromProxy/', callback);
};

Core.prototype.homeDirectory = function (callback) {
  this.api.request('/core/view/homeDirectory/', callback);
};

Core.prototype.optionDefaultUserAgent = function (callback) {
  this.api.request('/core/view/optionDefaultUserAgent/', callback);
};

Core.prototype.optionHttpState = function (callback) {
  this.api.request('/core/view/optionHttpState/', callback);
};

Core.prototype.optionProxyChainName = function (callback) {
  this.api.request('/core/view/optionProxyChainName/', callback);
};

Core.prototype.optionProxyChainPassword = function (callback) {
  this.api.request('/core/view/optionProxyChainPassword/', callback);
};

Core.prototype.optionProxyChainPort = function (callback) {
  this.api.request('/core/view/optionProxyChainPort/', callback);
};

Core.prototype.optionProxyChainRealm = function (callback) {
  this.api.request('/core/view/optionProxyChainRealm/', callback);
};

Core.prototype.optionProxyChainSkipName = function (callback) {
  this.api.request('/core/view/optionProxyChainSkipName/', callback);
};

Core.prototype.optionProxyChainUserName = function (callback) {
  this.api.request('/core/view/optionProxyChainUserName/', callback);
};

Core.prototype.optionProxyExcludedDomains = function (callback) {
  this.api.request('/core/view/optionProxyExcludedDomains/', callback);
};

Core.prototype.optionProxyExcludedDomainsEnabled = function (callback) {
  this.api.request('/core/view/optionProxyExcludedDomainsEnabled/', callback);
};

Core.prototype.optionTimeoutInSecs = function (callback) {
  this.api.request('/core/view/optionTimeoutInSecs/', callback);
};

Core.prototype.optionHttpStateEnabled = function (callback) {
  this.api.request('/core/view/optionHttpStateEnabled/', callback);
};

Core.prototype.optionProxyChainPrompt = function (callback) {
  this.api.request('/core/view/optionProxyChainPrompt/', callback);
};

Core.prototype.optionSingleCookieRequestHeader = function (callback) {
  this.api.request('/core/view/optionSingleCookieRequestHeader/', callback);
};

Core.prototype.optionUseProxyChain = function (callback) {
  this.api.request('/core/view/optionUseProxyChain/', callback);
};

Core.prototype.optionUseProxyChainAuth = function (callback) {
  this.api.request('/core/view/optionUseProxyChainAuth/', callback);
};

/**
 * Convenient and simple action to access a URL, optionally following redirections. Returns the request sent and response received and followed redirections, if any. Other actions are available which offer more control on what is sent, like, 'sendRequest' or 'sendHarRequest'.
 **/
Core.prototype.accessUrl = function (url, followredirects, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'url' : url, 'apikey' : apikey};
  if (followredirects && followredirects !== null) {
    params['followRedirects'] = followredirects;
  }
  this.api.request('/core/action/accessUrl/', params, callback);
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

/**
 * Creates a new session, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
 **/
Core.prototype.newSession = function (name, overwrite, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'apikey' : apikey};
  if (name && name !== null) {
    params['name'] = name;
  }
  if (overwrite && overwrite !== null) {
    params['overwrite'] = overwrite;
  }
  this.api.request('/core/action/newSession/', params, callback);
};

/**
 * Loads the session with the given name. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
 **/
Core.prototype.loadSession = function (name, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/loadSession/', {'name' : name, 'apikey' : apikey}, callback);
};

/**
 * Saves the session with the name supplied, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
 **/
Core.prototype.saveSession = function (name, overwrite, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'name' : name, 'apikey' : apikey};
  if (overwrite && overwrite !== null) {
    params['overwrite'] = overwrite;
  }
  this.api.request('/core/action/saveSession/', params, callback);
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

/**
 * Sets the mode, which may be one of [safe, protect, standard, attack]
 **/
Core.prototype.setMode = function (mode, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setMode/', {'mode' : mode, 'apikey' : apikey}, callback);
};

Core.prototype.generateRootCA = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/generateRootCA/', {'apikey' : apikey}, callback);
};

/**
 * Sends the HTTP request, optionally following redirections. Returns the request sent and response received and followed redirections, if any.
 **/
Core.prototype.sendRequest = function (request, followredirects, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'request' : request, 'apikey' : apikey};
  if (followredirects && followredirects !== null) {
    params['followRedirects'] = followredirects;
  }
  this.api.request('/core/action/sendRequest/', params, callback);
};

Core.prototype.deleteAllAlerts = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/deleteAllAlerts/', {'apikey' : apikey}, callback);
};

Core.prototype.runGarbageCollection = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/runGarbageCollection/', {'apikey' : apikey}, callback);
};

/**
 * Deletes the site node found in the Sites Tree on the basis of the URL, HTTP method, and post data (if applicable and specified). 
 **/
Core.prototype.deleteSiteNode = function (url, method, postdata, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'url' : url, 'apikey' : apikey};
  if (method && method !== null) {
    params['method'] = method;
  }
  if (postdata && postdata !== null) {
    params['postData'] = postdata;
  }
  this.api.request('/core/action/deleteSiteNode/', params, callback);
};

Core.prototype.setOptionDefaultUserAgent = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionDefaultUserAgent/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainName = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainName/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainPassword = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainPassword/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainRealm = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainRealm/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainSkipName = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainSkipName/', {'String' : string, 'apikey' : apikey}, callback);
};

Core.prototype.setOptionProxyChainUserName = function (string, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionProxyChainUserName/', {'String' : string, 'apikey' : apikey}, callback);
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

Core.prototype.setOptionSingleCookieRequestHeader = function (bool, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/core/action/setOptionSingleCookieRequestHeader/', {'Boolean' : bool, 'apikey' : apikey}, callback);
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

/**
 * Generates a report in XML format
 **/
Core.prototype.xmlreport = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/xmlreport/', {'apikey' : apikey}, callback);
};

/**
 * Generates a report in HTML format
 **/
Core.prototype.htmlreport = function (apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/htmlreport/', {'apikey' : apikey}, callback);
};

/**
 * Gets the message with the given ID in HAR format
 **/
Core.prototype.messageHar = function (id, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/core/other/messageHar/', {'id' : id, 'apikey' : apikey}, callback);
};

/**
 * Gets the HTTP messages sent through/by ZAP, in HAR format, optionally filtered by URL and paginated with 'start' position and 'count' of messages
 **/
Core.prototype.messagesHar = function (baseurl, start, count, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'apikey' : apikey};
  if (baseurl && baseurl !== null) {
    params['baseurl'] = baseurl;
  }
  if (start && start !== null) {
    params['start'] = start;
  }
  if (count && count !== null) {
    params['count'] = count;
  }
  this.api.requestOther('/core/other/messagesHar/', params, callback);
};

/**
 * Sends the first HAR request entry, optionally following redirections. Returns, in HAR format, the request sent and response received and followed redirections, if any.
 **/
Core.prototype.sendHarRequest = function (request, followredirects, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  var params = {'request' : request, 'apikey' : apikey};
  if (followredirects && followredirects !== null) {
    params['followRedirects'] = followredirects;
  }
  this.api.requestOther('/core/other/sendHarRequest/', params, callback);
};

module.exports = Core;
