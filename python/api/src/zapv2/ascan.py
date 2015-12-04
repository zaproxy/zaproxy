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

class ascan(object):

    def __init__(self, zap):
        self.zap = zap

    def status(self, scanid=''):
        return next(self.zap._request(self.zap.base + 'ascan/view/status/', {'scanId' : scanid}).itervalues())

    def scan_progress(self, scanid=''):
        return next(self.zap._request(self.zap.base + 'ascan/view/scanProgress/', {'scanId' : scanid}).itervalues())

    def messages_ids(self, scanid):
        return next(self.zap._request(self.zap.base + 'ascan/view/messagesIds/', {'scanId' : scanid}).itervalues())

    def alerts_ids(self, scanid):
        return next(self.zap._request(self.zap.base + 'ascan/view/alertsIds/', {'scanId' : scanid}).itervalues())

    @property
    def scans(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/scans/').itervalues())

    @property
    def scan_policy_names(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/scanPolicyNames/').itervalues())

    @property
    def excluded_from_scan(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/excludedFromScan/').itervalues())

    def scanners(self, scanpolicyname='', policyid=''):
        return next(self.zap._request(self.zap.base + 'ascan/view/scanners/', {'scanPolicyName' : scanpolicyname, 'policyId' : policyid}).itervalues())

    def policies(self, scanpolicyname='', policyid=''):
        return next(self.zap._request(self.zap.base + 'ascan/view/policies/', {'scanPolicyName' : scanpolicyname, 'policyId' : policyid}).itervalues())

    @property
    def attack_mode_queue(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/attackModeQueue/').itervalues())

    @property
    def option_attack_policy(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionAttackPolicy/').itervalues())

    @property
    def option_default_policy(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionDefaultPolicy/').itervalues())

    @property
    def option_delay_in_ms(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionDelayInMs/').itervalues())

    @property
    def option_excluded_param_list(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionExcludedParamList/').itervalues())

    @property
    def option_handle_anti_csrf_tokens(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionHandleAntiCSRFTokens/').itervalues())

    @property
    def option_host_per_scan(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionHostPerScan/').itervalues())

    @property
    def option_max_chart_time_in_mins(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionMaxChartTimeInMins/').itervalues())

    @property
    def option_max_results_to_list(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionMaxResultsToList/').itervalues())

    @property
    def option_max_scans_in_ui(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionMaxScansInUI/').itervalues())

    @property
    def option_target_params_enabled_rpc(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionTargetParamsEnabledRPC/').itervalues())

    @property
    def option_target_params_injectable(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionTargetParamsInjectable/').itervalues())

    @property
    def option_thread_per_host(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionThreadPerHost/').itervalues())

    @property
    def option_allow_attack_on_start(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionAllowAttackOnStart/').itervalues())

    @property
    def option_inject_plugin_id_in_header(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionInjectPluginIdInHeader/').itervalues())

    @property
    def option_prompt_in_attack_mode(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionPromptInAttackMode/').itervalues())

    @property
    def option_prompt_to_clear_finished_scans(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionPromptToClearFinishedScans/').itervalues())

    @property
    def option_rescan_in_attack_mode(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionRescanInAttackMode/').itervalues())

    @property
    def option_show_advanced_dialog(self):
        return next(self.zap._request(self.zap.base + 'ascan/view/optionShowAdvancedDialog/').itervalues())

    def scan(self, url, recurse='', inscopeonly='', scanpolicyname='', method='', postdata='', apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/scan/', {'url' : url, 'recurse' : recurse, 'inScopeOnly' : inscopeonly, 'scanPolicyName' : scanpolicyname, 'method' : method, 'postData' : postdata, 'apikey' : apikey}).itervalues())

    def scan_as_user(self, url, contextid, userid, recurse='', scanpolicyname='', method='', postdata='', apikey=''):
        """
        Active Scans from the perspective of a User, obtained using the given Context ID and User ID. See 'scan' action for more details.
        """
        return next(self.zap._request(self.zap.base + 'ascan/action/scanAsUser/', {'url' : url, 'contextId' : contextid, 'userId' : userid, 'recurse' : recurse, 'scanPolicyName' : scanpolicyname, 'method' : method, 'postData' : postdata, 'apikey' : apikey}).itervalues())

    def pause(self, scanid, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/pause/', {'scanId' : scanid, 'apikey' : apikey}).itervalues())

    def resume(self, scanid, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/resume/', {'scanId' : scanid, 'apikey' : apikey}).itervalues())

    def stop(self, scanid, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/stop/', {'scanId' : scanid, 'apikey' : apikey}).itervalues())

    def remove_scan(self, scanid, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/removeScan/', {'scanId' : scanid, 'apikey' : apikey}).itervalues())

    def pause_all_scans(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/pauseAllScans/', {'apikey' : apikey}).itervalues())

    def resume_all_scans(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/resumeAllScans/', {'apikey' : apikey}).itervalues())

    def stop_all_scans(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/stopAllScans/', {'apikey' : apikey}).itervalues())

    def remove_all_scans(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/removeAllScans/', {'apikey' : apikey}).itervalues())

    def clear_excluded_from_scan(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/clearExcludedFromScan/', {'apikey' : apikey}).itervalues())

    def exclude_from_scan(self, regex, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/excludeFromScan/', {'regex' : regex, 'apikey' : apikey}).itervalues())

    def enable_all_scanners(self, scanpolicyname='', apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/enableAllScanners/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def disable_all_scanners(self, scanpolicyname='', apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/disableAllScanners/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def enable_scanners(self, ids, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/enableScanners/', {'ids' : ids, 'apikey' : apikey}).itervalues())

    def disable_scanners(self, ids, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/disableScanners/', {'ids' : ids, 'apikey' : apikey}).itervalues())

    def set_enabled_policies(self, ids, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setEnabledPolicies/', {'ids' : ids, 'apikey' : apikey}).itervalues())

    def set_policy_attack_strength(self, id, attackstrength, scanpolicyname='', apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setPolicyAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def set_policy_alert_threshold(self, id, alertthreshold, scanpolicyname='', apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setPolicyAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def set_scanner_attack_strength(self, id, attackstrength, scanpolicyname='', apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setScannerAttackStrength/', {'id' : id, 'attackStrength' : attackstrength, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def set_scanner_alert_threshold(self, id, alertthreshold, scanpolicyname='', apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setScannerAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def add_scan_policy(self, scanpolicyname, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/addScanPolicy/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def remove_scan_policy(self, scanpolicyname, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/removeScanPolicy/', {'scanPolicyName' : scanpolicyname, 'apikey' : apikey}).itervalues())

    def set_option_attack_policy(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionAttackPolicy/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_default_policy(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionDefaultPolicy/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_allow_attack_on_start(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionAllowAttackOnStart/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_delay_in_ms(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionDelayInMs/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_handle_anti_csrf_tokens(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionHandleAntiCSRFTokens/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_host_per_scan(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionHostPerScan/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_inject_plugin_id_in_header(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionInjectPluginIdInHeader/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_max_chart_time_in_mins(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionMaxChartTimeInMins/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_max_results_to_list(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionMaxResultsToList/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_max_scans_in_ui(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionMaxScansInUI/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_prompt_in_attack_mode(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionPromptInAttackMode/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_prompt_to_clear_finished_scans(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionPromptToClearFinishedScans/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_rescan_in_attack_mode(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionRescanInAttackMode/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_show_advanced_dialog(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionShowAdvancedDialog/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_target_params_enabled_rpc(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionTargetParamsEnabledRPC/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_target_params_injectable(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionTargetParamsInjectable/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_thread_per_host(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'ascan/action/setOptionThreadPerHost/', {'Integer' : integer, 'apikey' : apikey}).itervalues())


