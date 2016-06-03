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

class httpSessions(object):

    def __init__(self, zap):
        self.zap = zap

    def sessions(self, site, session=None):
        """
        Gets the sessions of the given site. Optionally returning just the session with the given name.
        """
        params = {'site' : site}
        if session is not None:
            params['session'] = session
        return next(self.zap._request(self.zap.base + 'httpSessions/view/sessions/', params).itervalues())

    def active_session(self, site):
        """
        Gets the name of the active session for the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/view/activeSession/', {'site' : site}).itervalues())

    def session_tokens(self, site):
        """
        Gets the names of the session tokens for the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/view/sessionTokens/', {'site' : site}).itervalues())

    def create_empty_session(self, site, session=None, apikey=''):
        """
        Creates an empty session for the given site. Optionally with the given name.
        """
        params = {'site' : site, 'apikey' : apikey}
        if session is not None:
            params['session'] = session
        return next(self.zap._request(self.zap.base + 'httpSessions/action/createEmptySession/', params).itervalues())

    def remove_session(self, site, session, apikey=''):
        """
        Removes the session from the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/action/removeSession/', {'site' : site, 'session' : session, 'apikey' : apikey}).itervalues())

    def set_active_session(self, site, session, apikey=''):
        """
        Sets the given session as active for the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/action/setActiveSession/', {'site' : site, 'session' : session, 'apikey' : apikey}).itervalues())

    def unset_active_session(self, site, apikey=''):
        """
        Unsets the active session of the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/action/unsetActiveSession/', {'site' : site, 'apikey' : apikey}).itervalues())

    def add_session_token(self, site, sessiontoken, apikey=''):
        """
        Adds the session token to the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/action/addSessionToken/', {'site' : site, 'sessionToken' : sessiontoken, 'apikey' : apikey}).itervalues())

    def remove_session_token(self, site, sessiontoken, apikey=''):
        """
        Removes the session token from the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/action/removeSessionToken/', {'site' : site, 'sessionToken' : sessiontoken, 'apikey' : apikey}).itervalues())

    def set_session_token_value(self, site, session, sessiontoken, tokenvalue, apikey=''):
        """
        Sets the value of the session token of the given session for the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/action/setSessionTokenValue/', {'site' : site, 'session' : session, 'sessionToken' : sessiontoken, 'tokenValue' : tokenvalue, 'apikey' : apikey}).itervalues())

    def rename_session(self, site, oldsessionname, newsessionname, apikey=''):
        """
        Renames the session of the given site.
        """
        return next(self.zap._request(self.zap.base + 'httpSessions/action/renameSession/', {'site' : site, 'oldSessionName' : oldsessionname, 'newSessionName' : newsessionname, 'apikey' : apikey}).itervalues())


