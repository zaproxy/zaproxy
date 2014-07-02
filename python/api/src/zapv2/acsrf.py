# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2014 the ZAP development team
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
    def option_tokens(self):
        return self.zap._request(self.zap.base + 'acsrf/view/optionTokens/').get('Tokens')

    @property
    def option_tokens_names(self):
        return self.zap._request(self.zap.base + 'acsrf/view/optionTokensNames/').get('TokensNames')

    def add_option_token(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'acsrf/action/addOptionToken/', {'String' : string, 'apikey' : apikey})

    def remove_option_token(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'acsrf/action/removeOptionToken/', {'String' : string, 'apikey' : apikey})

    def gen_form(self, hrefid, apikey=''):
        """
        Generate a form for testing lack of anti CSRF tokens - typically invoked via ZAP
        """
        return self.zap._request_other(self.zap.base_other + 'acsrf/other/genForm/', {'hrefId' : hrefid, 'apikey' : apikey})


