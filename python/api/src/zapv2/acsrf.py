# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright the ZAP development team
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

    @property
    def option_confirm_remove_token(self):
        return self.zap._request(self.zap.base + 'acsrf/view/optionConfirmRemoveToken/').get('ConfirmRemoveToken')

    def add_option_token(self, apikey, string):
        return self.zap._request(self.zap.base + 'acsrf/action/addOptionToken/', {'String' : string})

    def remove_option_token(self, apikey, string):
        return self.zap._request(self.zap.base + 'acsrf/action/removeOptionToken/', {'String' : string})

    def set_option_confirm_remove_token(self, apikey, boolean):
        return self.zap._request(self.zap.base + 'acsrf/action/setOptionConfirmRemoveToken/', {'Boolean' : boolean})

    def gen_form(self, apikey, hrefid):
        """
        Generate a form for testing lack of anti CSRF tokens - typically invoked via ZAP
        """
        return self.zap._request(self.zap.base + 'acsrf/other/genForm/', {'hrefId' : hrefid})


