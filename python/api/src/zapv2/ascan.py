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

class ascan(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def status(self):
        return self.zap._request(self.zap.base + 'ascan/view/status/').get('status')

    @property
    def excluded_from_scan(self):
        return self.zap._request(self.zap.base + 'ascan/view/excludedFromScan/').get('excludedFromScan')

    def scanners(self, policyid=''):
        return self.zap._request(self.zap.base + 'ascan/view/scanners/', {'policyId' : policyid}).get('scanners')

    @property
    def policies(self):
        return self.zap._request(self.zap.base + 'ascan/view/policies/').get('policies')

    @property
    def option_excluded_param_list(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionExcludedParamList/').get('ExcludedParamList')

    @property
    def option_thread_per_host(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionThreadPerHost/').get('ThreadPerHost')

    @property
    def option_host_per_scan(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionHostPerScan/').get('HostPerScan')

    @property
    def option_max_results_to_list(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionMaxResultsToList/').get('MaxResultsToList')

    @property
    def option_delay_in_ms(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionDelayInMs/').get('DelayInMs')

    @property
    def option_handle_anti_csrf_tokens(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionHandleAntiCSRFTokens/').get('HandleAntiCSRFTokens')

    @property
    def option_alert_threshold(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionAlertThreshold/').get('AlertThreshold')

    @property
    def option_attack_strength(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionAttackStrength/').get('AttackStrength')

    @property
    def option_target_params_injectable(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionTargetParamsInjectable/').get('TargetParamsInjectable')

    @property
    def option_target_params_enabled_rpc(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionTargetParamsEnabledRPC/').get('TargetParamsEnabledRPC')

    def scan(self, url, recurse='', inscopeonly='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/scan/', {'url' : url, 'recurse' : recurse, 'inScopeOnly' : inscopeonly, 'apikey' : apikey})

    def clear_excluded_from_scan(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/clearExcludedFromScan/', {'apikey' : apikey})

    def exclude_from_scan(self, regex, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/excludeFromScan/', {'regex' : regex, 'apikey' : apikey})

    def enable_all_scanners(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/enableAllScanners/', {'apikey' : apikey})

    def disable_all_scanners(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/disableAllScanners/', {'apikey' : apikey})

    def enable_scanners(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/enableScanners/', {'ids' : ids, 'apikey' : apikey})

    def disable_scanners(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/disableScanners/', {'ids' : ids, 'apikey' : apikey})

    def set_enabled_policies(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setEnabledPolicies/', {'ids' : ids, 'apikey' : apikey})

    def set_policy_attack_strength(self, id, attackstrength, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setPolicyAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'apikey' : apikey})

    def set_policy_alert_threshold(self, id, alertthreshold, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setPolicyAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'apikey' : apikey})

    def set_scanner_attack_strength(self, id, attackstrength, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setScannerAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'apikey' : apikey})

    def set_scanner_alert_threshold(self, id, alertthreshold, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setScannerAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'apikey' : apikey})

    def set_option_alert_threshold(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionAlertThreshold/', {'String' : string, 'apikey' : apikey})

    def set_option_attack_strength(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionAttackStrength/', {'String' : string, 'apikey' : apikey})

    def set_option_thread_per_host(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionThreadPerHost/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_host_per_scan(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionHostPerScan/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_max_results_to_list(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionMaxResultsToList/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_delay_in_ms(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionDelayInMs/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_handle_anti_csrf_tokens(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionHandleAntiCSRFTokens/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_target_params_injectable(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionTargetParamsInjectable/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_target_params_enabled_rpc(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionTargetParamsEnabledRPC/', {'Integer' : integer, 'apikey' : apikey})


