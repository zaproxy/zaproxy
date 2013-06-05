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

class httpSessions(object):

    def __init__(self, zap):
        self.zap = zap

    def sessions(self, site, session=''):
        return self.zap._request(self.zap.base + 'httpSessions/view/sessions/', {'site' : site, 'session' : session})

    def active_session(self, site):
        return self.zap._request(self.zap.base + 'httpSessions/view/activeSession/', {'site' : site})

    def session_tokens(self, site):
        return self.zap._request(self.zap.base + 'httpSessions/view/sessionTokens/', {'site' : site})

    def create_empty_session(self, site):
        return self.zap._request(self.zap.base + 'httpSessions/action/createEmptySession/', {'site' : site})

    def remove_session(self, site, session):
        return self.zap._request(self.zap.base + 'httpSessions/action/removeSession/', {'site' : site, 'session' : session})

    def set_active_session(self, site, session):
        return self.zap._request(self.zap.base + 'httpSessions/action/setActiveSession/', {'site' : site, 'session' : session})

    def unset_active_session(self, site):
        return self.zap._request(self.zap.base + 'httpSessions/action/unsetActiveSession/', {'site' : site})

    def add_session_token(self, site, sessiontoken):
        return self.zap._request(self.zap.base + 'httpSessions/action/addSessionToken/', {'site' : site, 'sessionToken' : sessiontoken})

    def remove_session_token(self, site, sessiontoken):
        return self.zap._request(self.zap.base + 'httpSessions/action/removeSessionToken/', {'site' : site, 'sessionToken' : sessiontoken})

    def set_session_token_value(self, site, session, sessiontoken, tokenvalue):
        return self.zap._request(self.zap.base + 'httpSessions/action/setSessionTokenValue/', {'site' : site, 'session' : session, 'sessionToken' : sessiontoken, 'tokenValue' : tokenvalue})

    def rename_session(self, site, oldsessionname, newsessionname):
        return self.zap._request(self.zap.base + 'httpSessions/action/renameSession/', {'site' : site, 'oldSessionName' : oldsessionname, 'newSessionName' : newsessionname})


