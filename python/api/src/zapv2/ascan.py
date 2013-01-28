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

class ascan(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def status(self):
        return self.zap._request(self.zap.base + 'ascan/view/status/')

    @property
    def excluded_from_scan(self):
        return self.zap._request(self.zap.base + 'ascan/view/excludedFromScan/')

    @property
    def option_thread_per_host(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionThreadPerHost/')

    @property
    def option_host_per_scan(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionHostPerScan/')

    @property
    def option_delay_in_ms(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionDelayInMs/')

    @property
    def option_handle_anti_csrf_tokens(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionHandleAntiCSRFTokens/')

    @property
    def option_alert_threshold(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionAlertThreshold/')

    @property
    def option_attack_strength(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionAttackStrength/')

    def scan(self, url, recurse='', inscopeonly=''):
        return self.zap._request(self.zap.base + 'ascan/action/scan/', {'url' : url, 'recurse' : recurse, 'inScopeOnly' : inscopeonly})

    @property
    def clear_excluded_from_scan(self):
        return self.zap._request(self.zap.base + 'ascan/action/clearExcludedFromScan/')

    def exclude_from_scan(self, regex):
        return self.zap._request(self.zap.base + 'ascan/action/excludeFromScan/', {'regex' : regex})

    def set_option_alert_threshold(self, string):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionAlertThreshold/', {'String' : string})

    def set_option_attack_strength(self, string):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionAttackStrength/', {'String' : string})

    def set_option_thread_per_host(self, integer):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionThreadPerHost/', {'Integer' : integer})

    def set_option_host_per_scan(self, integer):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionHostPerScan/', {'Integer' : integer})

    def set_option_delay_in_ms(self, integer):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionDelayInMs/', {'Integer' : integer})

    def set_option_handle_anti_csrf_tokens(self, boolean):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionHandleAntiCSRFTokens/', {'Boolean' : boolean})


