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

    def status(self, scanid=''):
        return self.zap._request(self.zap.base + 'ascan/view/status/', {'scanId' : scanid}).get('status')

    def messages_ids(self, scanid):
        return self.zap._request(self.zap.base + 'ascan/view/messagesIds/', {'scanId' : scanid}).get('messagesIds')

    def alerts_ids(self, scanid):
        return self.zap._request(self.zap.base + 'ascan/view/alertsIds/', {'scanId' : scanid}).get('alertsIds')

    @property
    def scans(self):
        return self.zap._request(self.zap.base + 'ascan/view/scans/').get('scans')

    @property
    def scan_policy_names(self):
        return self.zap._request(self.zap.base + 'ascan/view/scanPolicyNames/').get('scanPolicyNames')

    @property
    def excluded_from_scan(self):
        return self.zap._request(self.zap.base + 'ascan/view/excludedFromScan/').get('excludedFromScan')

    def scanners(self, scanpolicyname='', policyid=''):
        return self.zap._request(self.zap.base + 'ascan/view/scanners/', {'scanPolicyName' : scanpolicyname, 'policyId' : policyid}).get('scanners')

    def policies(self, scanpolicyname='', policyid=''):
        return self.zap._request(self.zap.base + 'ascan/view/policies/', {'scanPolicyName' : scanpolicyname, 'policyId' : policyid}).get('policies')

    @property
    def option_max_scans_in_ui(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionMaxScansInUI/').get('MaxScansInUI')

    @property
    def option_show_advanced_dialog(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionShowAdvancedDialog/').get('ShowAdvancedDialog')

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
    def option_rescan_in_attack_mode(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionRescanInAttackMode/').get('RescanInAttackMode')

    @property
    def option_prompt_in_attack_mode(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionPromptInAttackMode/').get('PromptInAttackMode')

    @property
    def option_target_params_injectable(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionTargetParamsInjectable/').get('TargetParamsInjectable')

    @property
    def option_target_params_enabled_rpc(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionTargetParamsEnabledRPC/').get('TargetParamsEnabledRPC')

    @property
    def option_prompt_to_clear_finished_scans(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionPromptToClearFinishedScans/').get('PromptToClearFinishedScans')

    @property
    def option_default_policy(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionDefaultPolicy/').get('DefaultPolicy')

    @property
    def option_attack_policy(self):
        return self.zap._request(self.zap.base + 'ascan/view/optionAttackPolicy/').get('AttackPolicy')

    def scan(self, url, recurse='', inscopeonly='', scanpolicyname='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/scan/', {'url' : url, 'recurse' : recurse, 'inScopeOnly' : inscopeonly, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def pause(self, scanid, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/pause/', {'scanId' : scanid, 'apikey' : apikey})

    def resume(self, scanid, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/resume/', {'scanId' : scanid, 'apikey' : apikey})

    def stop(self, scanid, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/stop/', {'scanId' : scanid, 'apikey' : apikey})

    def remove_scan(self, scanid, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/removeScan/', {'scanId' : scanid, 'apikey' : apikey})

    def pause_all_scans(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/pauseAllScans/', {'apikey' : apikey})

    def resume_all_scans(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/resumeAllScans/', {'apikey' : apikey})

    def stop_all_scans(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/stopAllScans/', {'apikey' : apikey})

    def remove_all_scans(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/removeAllScans/', {'apikey' : apikey})

    def clear_excluded_from_scan(self, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/clearExcludedFromScan/', {'apikey' : apikey})

    def exclude_from_scan(self, regex, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/excludeFromScan/', {'regex' : regex, 'apikey' : apikey})

    def enable_all_scanners(self, scanpolicyname='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/enableAllScanners/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def disable_all_scanners(self, scanpolicyname='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/disableAllScanners/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def enable_scanners(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/enableScanners/', {'ids' : ids, 'apikey' : apikey})

    def disable_scanners(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/disableScanners/', {'ids' : ids, 'apikey' : apikey})

    def set_enabled_policies(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setEnabledPolicies/', {'ids' : ids, 'apikey' : apikey})

    def set_policy_attack_strength(self, id, attackstrength, scanpolicyname='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setPolicyAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def set_policy_alert_threshold(self, id, attackstrength, scanpolicyname='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setPolicyAlertThreshold/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def set_scanner_attack_strength(self, id, attackstrength, scanpolicyname='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setScannerAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def set_scanner_alert_threshold(self, id, attackstrength, scanpolicyname='', apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setScannerAlertThreshold/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def add_scan_policy(self, scanpolicyname, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/addScanPolicy/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def remove_scan_policy(self, scanpolicyname, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/removeScanPolicy/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey})

    def set_option_default_policy(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionDefaultPolicy/', {'String' : string, 'apikey' : apikey})

    def set_option_attack_policy(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionAttackPolicy/', {'String' : string, 'apikey' : apikey})

    def set_option_max_scans_in_ui(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionMaxScansInUI/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_show_advanced_dialog(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionShowAdvancedDialog/', {'Boolean' : boolean, 'apikey' : apikey})

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

    def set_option_rescan_in_attack_mode(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionRescanInAttackMode/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_prompt_in_attack_mode(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionPromptInAttackMode/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_target_params_injectable(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionTargetParamsInjectable/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_target_params_enabled_rpc(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionTargetParamsEnabledRPC/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_prompt_to_clear_finished_scans(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'ascan/action/setOptionPromptToClearFinishedScans/', {'Boolean' : boolean, 'apikey' : apikey})


