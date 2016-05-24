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

class authorization(object):

    def __init__(self, zap):
        self.zap = zap

    def get_authorization_detection_method(self, contextid):
        """
        Obtains all the configuration of the authorization detection method that is currently set for a context.
        """
        return next(self.zap._request(self.zap.base + 'authorization/view/getAuthorizationDetectionMethod/', {'contextId' : contextid}).itervalues())

    def set_basic_authorization_detection_method(self, contextid, headerregex=None, bodyregex=None, statuscode=None, logicaloperator=None, apikey=''):
        """
        Sets the authorization detection method for a context as one that identifies un-authorized messages based on: the message's status code or a regex pattern in the response's header or body. Also, whether all conditions must match or just some can be specified via the logicalOperator parameter, which accepts two values: "AND" (default), "OR".  
        """
        params = {'contextId' : contextid, 'apikey' : apikey}
        if headerregex is not None:
            params['headerRegex'] = headerregex
        if bodyregex is not None:
            params['bodyRegex'] = bodyregex
        if statuscode is not None:
            params['statusCode'] = statuscode
        if logicaloperator is not None:
            params['logicalOperator'] = logicaloperator
        return next(self.zap._request(self.zap.base + 'authorization/action/setBasicAuthorizationDetectionMethod/', params).itervalues())


