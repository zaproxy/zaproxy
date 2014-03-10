# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2013 ZAP development team
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

class authentication(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def get_supported_authentication_methods(self):
        return self.zap._request(self.zap.base + 'authentication/view/getSupportedAuthenticationMethods/').get('getSupportedAuthenticationMethods')

    def get_authentication_method_config_params(self, authmethodname):
        return self.zap._request(self.zap.base + 'authentication/view/getAuthenticationMethodConfigParams/', {'authMethodName' : authmethodname})

    def get_authentication_method(self, contextid):
        return self.zap._request(self.zap.base + 'authentication/view/getAuthenticationMethod/', {'contextId' : contextid})

    def get_logged_in_indicator(self, contextid):
        return self.zap._request(self.zap.base + 'authentication/view/getLoggedInIndicator/', {'contextId' : contextid})

    def get_logged_out_indicator(self, contextid):
        return self.zap._request(self.zap.base + 'authentication/view/getLoggedOutIndicator/', {'contextId' : contextid})

    def set_authentication_method(self, contextid, authmethodname, authmethodconfigparams=''):
        return self.zap._request(self.zap.base + 'authentication/action/setAuthenticationMethod/', {'contextId' : contextid, 'authMethodName' : authmethodname, 'authMethodConfigParams' : authmethodconfigparams})

    def set_logged_in_indicator(self, contextid, loggedinindicatorregex):
        return self.zap._request(self.zap.base + 'authentication/action/setLoggedInIndicator/', {'contextId' : contextid, 'loggedInIndicatorRegex' : loggedinindicatorregex})

    def set_logged_out_indicator(self, contextid, loggedoutindicatorregex):
        return self.zap._request(self.zap.base + 'authentication/action/setLoggedOutIndicator/', {'contextId' : contextid, 'loggedOutIndicatorRegex' : loggedoutindicatorregex})


