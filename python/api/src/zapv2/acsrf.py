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

class acsrf(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def option_tokens_names(self):
        """
        Lists the names of all anti CSRF tokens
        """
        return next(self.zap._request(self.zap.base + 'acsrf/view/optionTokensNames/').itervalues())

    def add_option_token(self, string, apikey=''):
        """
        Adds an anti CSRF token with the given name, enabled by default
        """
        return next(self.zap._request(self.zap.base + 'acsrf/action/addOptionToken/', {'String' : string, 'apikey' : apikey}).itervalues())

    def remove_option_token(self, string, apikey=''):
        """
        Removes the anti CSRF token with the given name
        """
        return next(self.zap._request(self.zap.base + 'acsrf/action/removeOptionToken/', {'String' : string, 'apikey' : apikey}).itervalues())

    def gen_form(self, hrefid, apikey=''):
        """
        Generate a form for testing lack of anti CSRF tokens - typically invoked via ZAP
        """
        return (self.zap._request_other(self.zap.base_other + 'acsrf/other/genForm/', {'hrefId' : hrefid, 'apikey' : apikey}))


