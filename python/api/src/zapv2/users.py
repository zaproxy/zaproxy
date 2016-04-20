# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2015 the ZAP development team
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

import six


class users(object):
    def __init__(self, zap):
        self.zap = zap

    def users_list(self, contextid=None):
        params = {}
        if contextid is not None:
            params['contextId'] = contextid
        return next(six.itervalues(self.zap._request(self.zap.base + 'users/view/usersList/', params)))

    def get_user_by_id(self, contextid=None, userid=None):
        params = {}
        if contextid is not None:
            params['contextId'] = contextid
        if userid is not None:
            params['userId'] = userid
        return next(six.itervalues(self.zap._request(self.zap.base + 'users/view/getUserById/', params)))

    def get_authentication_credentials_config_params(self, contextid):
        return next(six.itervalues(
            self.zap._request(self.zap.base + 'users/view/getAuthenticationCredentialsConfigParams/',
                              {'contextId': contextid})))

    def get_authentication_credentials(self, contextid, userid):
        return next(six.itervalues(self.zap._request(self.zap.base + 'users/view/getAuthenticationCredentials/',
                                                     {'contextId': contextid, 'userId': userid})))

    def new_user(self, contextid, name, apikey=''):
        return next(six.itervalues(self.zap._request(self.zap.base + 'users/action/newUser/',
                                                     {'contextId': contextid, 'name': name, 'apikey': apikey})))

    def remove_user(self, contextid, userid, apikey=''):
        return next(six.itervalues(self.zap._request(self.zap.base + 'users/action/removeUser/',
                                                     {'contextId': contextid, 'userId': userid, 'apikey': apikey})))

    def set_user_enabled(self, contextid, userid, enabled, apikey=''):
        return next(six.itervalues(self.zap._request(self.zap.base + 'users/action/setUserEnabled/',
                                                     {'contextId': contextid, 'userId': userid, 'enabled': enabled,
                                                      'apikey'   : apikey})))

    def set_user_name(self, contextid, userid, name, apikey=''):
        return next(six.itervalues(self.zap._request(self.zap.base + 'users/action/setUserName/',
                                                     {'contextId': contextid, 'userId': userid, 'name': name,
                                                      'apikey'   : apikey})))

    def set_authentication_credentials(self, contextid, userid, authcredentialsconfigparams=None, apikey=''):
        params = {'contextId': contextid, 'userId': userid, 'apikey': apikey}
        if authcredentialsconfigparams is not None:
            params['authCredentialsConfigParams'] = authcredentialsconfigparams
        return next(
            six.itervalues(self.zap._request(self.zap.base + 'users/action/setAuthenticationCredentials/', params)))
