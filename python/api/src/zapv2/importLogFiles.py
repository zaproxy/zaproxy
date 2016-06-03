# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2016 the ZAP development team
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
"""
This file was automatically generated.
"""

class importLogFiles(object):

    def __init__(self, zap):
        self.zap = zap

    def import_zap_log_from_file(self, filepath):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'importLogFiles/view/ImportZAPLogFromFile/', {'FilePath' : filepath}).itervalues())

    def import_mod_security_log_from_file(self, filepath):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'importLogFiles/view/ImportModSecurityLogFromFile/', {'FilePath' : filepath}).itervalues())

    def import_zap_http_request_response_pair(self, httprequest, httpresponse):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'importLogFiles/view/ImportZAPHttpRequestResponsePair/', {'HTTPRequest' : httprequest, 'HTTPResponse' : httpresponse}).itervalues())

    def post_mod_security_audit_event(self, auditeventstring=None, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        params = {'apikey' : apikey}
        if auditeventstring is not None:
            params['AuditEventString'] = auditeventstring
        return next(self.zap._request(self.zap.base + 'importLogFiles/action/PostModSecurityAuditEvent/', params).itervalues())

    def other_post_mod_security_audit_event(self, auditeventstring, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return (self.zap._request_other(self.zap.base_other + 'importLogFiles/other/OtherPostModSecurityAuditEvent/', {'AuditEventString' : auditeventstring, 'apikey' : apikey}))


