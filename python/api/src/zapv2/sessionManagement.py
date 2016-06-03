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

class sessionManagement(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def get_supported_session_management_methods(self):
        return next(self.zap._request(self.zap.base + 'sessionManagement/view/getSupportedSessionManagementMethods/').itervalues())

    def get_session_management_method_config_params(self, methodname):
        return next(self.zap._request(self.zap.base + 'sessionManagement/view/getSessionManagementMethodConfigParams/', {'methodName' : methodname}).itervalues())

    def get_session_management_method(self, contextid):
        return next(self.zap._request(self.zap.base + 'sessionManagement/view/getSessionManagementMethod/', {'contextId' : contextid}).itervalues())

    def set_session_management_method(self, contextid, methodname, methodconfigparams=None, apikey=''):
        params = {'contextId' : contextid, 'methodName' : methodname, 'apikey' : apikey}
        if methodconfigparams is not None:
            params['methodConfigParams'] = methodconfigparams
        return next(self.zap._request(self.zap.base + 'sessionManagement/action/setSessionManagementMethod/', params).itervalues())


