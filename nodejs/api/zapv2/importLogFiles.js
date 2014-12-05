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
function ImportLogFiles(clientApi) {
  this.api = clientApi;
}

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
ImportLogFiles.prototype.ImportZAPLogFromFile = function (filepath, callback) {
  this.api.request('/importLogFiles/view/ImportZAPLogFromFile/', {'FilePath' : filepath}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
ImportLogFiles.prototype.ImportModSecurityLogFromFile = function (filepath, callback) {
  this.api.request('/importLogFiles/view/ImportModSecurityLogFromFile/', {'FilePath' : filepath}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
ImportLogFiles.prototype.ImportZAPHttpRequestResponsePair = function (httprequest, httpresponse, callback) {
  this.api.request('/importLogFiles/view/ImportZAPHttpRequestResponsePair/', {'HTTPRequest' : httprequest, 'HTTPResponse' : httpresponse}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
ImportLogFiles.prototype.PostModSecurityAuditEvent = function (auditeventstring, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.request('/importLogFiles/action/PostModSecurityAuditEvent/', {'AuditEventString' : auditeventstring, 'apikey' : apikey}, callback);
};

/**
 * This component is optional and therefore the API will only work if it is installed
 **/
ImportLogFiles.prototype.OtherPostModSecurityAuditEvent = function (auditeventstring, apikey, callback) {
  if (!callback && typeof(apikey) === 'function') {
    callback = apikey;
    apikey = null;
  }
  this.api.requestOther('/importLogFiles/other/OtherPostModSecurityAuditEvent/', {'AuditEventString' : auditeventstring, 'apikey' : apikey}, callback);
};

module.exports = ImportLogFiles;
