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

class auth(object):

    def __init__(self, zap):
        self.zap = zap

    def login_url(self, contextid):
        return self.zap._request(self.zap.base + 'auth/view/loginUrl/', {'contextId' : contextid})

    def login_data(self, contextid):
        return self.zap._request(self.zap.base + 'auth/view/loginData/', {'contextId' : contextid})

    def logged_in_indicator(self, contextid):
        return self.zap._request(self.zap.base + 'auth/view/loggedInIndicator/', {'contextId' : contextid})

    def logout_url(self, contextid):
        return self.zap._request(self.zap.base + 'auth/view/logoutUrl/', {'contextId' : contextid})

    def logout_data(self, contextid):
        return self.zap._request(self.zap.base + 'auth/view/logoutData/', {'contextId' : contextid})

    def logged_out_indicator(self, contextid):
        return self.zap._request(self.zap.base + 'auth/view/loggedOutIndicator/', {'contextId' : contextid})

    def login(self, contextid):
        return self.zap._request(self.zap.base + 'auth/action/login/', {'contextId' : contextid})

    def logout(self, contextid):
        return self.zap._request(self.zap.base + 'auth/action/logout/', {'contextId' : contextid})

    @property
    def auto_reauth_on(self):
        return self.zap._request(self.zap.base + 'auth/action/autoReauthOn/').get('autoReauthOn')

    @property
    def auto_reauth_off(self):
        return self.zap._request(self.zap.base + 'auth/action/autoReauthOff/').get('autoReauthOff')

    def set_login_url(self, contextid, url, postdata=''):
        return self.zap._request(self.zap.base + 'auth/action/setLoginUrl/', {'contextId' : contextid, 'url' : url, 'postData' : postdata})

    def set_login_indicator(self, contextid, indicator):
        return self.zap._request(self.zap.base + 'auth/action/setLoginIndicator/', {'contextId' : contextid, 'indicator' : indicator})

    def set_logout_url(self, contextid, url, postdata=''):
        return self.zap._request(self.zap.base + 'auth/action/setLogoutUrl/', {'contextId' : contextid, 'url' : url, 'postData' : postdata})

    def set_logged_out_indicator(self, contextid, indicator):
        return self.zap._request(self.zap.base + 'auth/action/setLoggedOutIndicator/', {'contextId' : contextid, 'indicator' : indicator})


