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

class autoupdate(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def latest_version_number(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/latestVersionNumber/').get('latest_version_number')

    @property
    def is_latest_version(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/isLatestVersion/').get('is_latest_version')

    @property
    def option_check_on_start_unset(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckOnStartUnset/').get('option_check_on_start_unset')

    @property
    def option_check_on_start(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckOnStart/').get('option_check_on_start')

    @property
    def option_download_new_release(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionDownloadNewRelease/').get('option_download_new_release')

    @property
    def option_check_addon_updates(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckAddonUpdates/').get('option_check_addon_updates')

    @property
    def option_install_addon_updates(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionInstallAddonUpdates/').get('option_install_addon_updates')

    @property
    def option_install_scanner_rules(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionInstallScannerRules/').get('option_install_scanner_rules')

    @property
    def option_report_release_addons(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionReportReleaseAddons/').get('option_report_release_addons')

    @property
    def option_report_beta_addons(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionReportBetaAddons/').get('option_report_beta_addons')

    @property
    def option_report_alpha_addons(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionReportAlphaAddons/').get('option_report_alpha_addons')

    @property
    def download_latest_release(self):
        return self.zap._request(self.zap.base + 'autoupdate/action/downloadLatestRelease/').get('download_latest_release')

    def set_option_check_on_start(self, integer):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionCheckOnStart/', {'Integer' : integer})

    def set_option_download_new_release(self, boolean):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionDownloadNewRelease/', {'Boolean' : boolean})

    def set_option_check_addon_updates(self, boolean):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionCheckAddonUpdates/', {'Boolean' : boolean})

    def set_option_install_addon_updates(self, boolean):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionInstallAddonUpdates/', {'Boolean' : boolean})

    def set_option_install_scanner_rules(self, boolean):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionInstallScannerRules/', {'Boolean' : boolean})

    def set_option_report_release_addons(self, boolean):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportReleaseAddons/', {'Boolean' : boolean})

    def set_option_report_beta_addons(self, boolean):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportBetaAddons/', {'Boolean' : boolean})

    def set_option_report_alpha_addons(self, boolean):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportAlphaAddons/', {'Boolean' : boolean})


