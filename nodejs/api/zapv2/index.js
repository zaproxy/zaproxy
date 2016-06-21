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

var request = require('request'),
    _ = require('lodash');

var Acsrf = require('./acsrf');
var AjaxSpider = require('./ajaxSpider');
var Ascan = require('./ascan');
var Authentication = require('./authentication');
var Authorization = require('./authorization');
var Autoupdate = require('./autoupdate');
var Brk = require('./brk');
var Context = require('./context');
var Core = require('./core');
var ForcedUser = require('./forcedUser');
var HttpSessions = require('./httpSessions');
var ImportLogFiles = require('./importLogFiles');
var Params = require('./params');
var Pnh = require('./pnh');
var Pscan = require('./pscan');
var Script = require('./script');
var Search = require('./search');
var SessionManagement = require('./sessionManagement');
var Spider = require('./spider');
var Stats = require('./stats');
var Users = require('./users');

// base JSON api url
var BASE = 'http://zap/JSON';
// base OTHER api url
var BASE_OTHER = 'http://zap/OTHER';

function ClientApi(options) {
  options = options || {};
  _.defaults(options, {
    proxy: 'http://127.0.0.1:8080'
  });

  var requestOptions = {
    method: 'GET',
    json: true,
    proxy: options.proxy
  };

  this.req = request.defaults(requestOptions);
  this.acsrf = new Acsrf(this);
  this.ajaxSpider = new AjaxSpider(this);
  this.ascan = new Ascan(this);
  this.authentication = new Authentication(this);
  this.authorization = new Authorization(this);
  this.autoupdate = new Autoupdate(this);
  this.brk = new Brk(this);
  this.context = new Context(this);
  this.core = new Core(this);
  this.forcedUser = new ForcedUser(this);
  this.httpSessions = new HttpSessions(this);
  this.importLogFiles = new ImportLogFiles(this);
  this.params = new Params(this);
  this.pnh = new Pnh(this);
  this.pscan = new Pscan(this);
  this.script = new Script(this);
  this.search = new Search(this);
  this.sessionManagement = new SessionManagement(this);
  this.spider = new Spider(this);
  this.stats = new Stats(this);
  this.users = new Users(this);
}

/**
 * Get a handler for REST API responses.
 * We include a workaround here for the fact that the API does not
 * return the correct status codes in the event of an error
 * (i.e. it always returns 200).
 **/
var responseHandler = function (callback) {
  return function handleResponse(err, res, body) {
    if (err) {
      callback(err);
      return;
    }

    // if the response has a code and a message, it's an error.
    if (body && body.code && body.message) {
      callback(body);
    } else {
      callback(null, body);
    }
  };
};

ClientApi.prototype.request = function (url, parms, callback) {
  if (!callback && typeof(parms === 'function')) {
    callback = parms;
    parms = null;
  }

  var options = {
    url: BASE + url
  };
  if (parms) {
    options.qs = parms;
  }
  this.req(options, responseHandler(callback));
};

ClientApi.prototype.requestOther = function (url, parms, callback) {
  if (!callback && typeof(parms === 'function')) {
    callback = parms;
    parms = null;
  }

  var options = {
    url: BASE_OTHER + url
  };
  if (parms) {
    options.qs = parms;
  }
  this.req(options, responseHandler(callback));
};

module.exports = ClientApi;
