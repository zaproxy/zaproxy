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

class autoupdate(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def latest_version_number(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/latestVersionNumber/').get('latestVersionNumber')

    @property
    def is_latest_version(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/isLatestVersion/').get('isLatestVersion')

    @property
    def option_check_on_start(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckOnStart/').get('CheckOnStart')

    @property
    def option_download_new_release(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionDownloadNewRelease/').get('DownloadNewRelease')

    @property
    def option_check_addon_updates(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckAddonUpdates/').get('CheckAddonUpdates')

    @property
    def option_install_addon_updates(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionInstallAddonUpdates/').get('InstallAddonUpdates')

    @property
    def option_install_scanner_rules(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionInstallScannerRules/').get('InstallScannerRules')

    @property
    def option_report_release_addons(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionReportReleaseAddons/').get('ReportReleaseAddons')

    @property
    def option_report_beta_addons(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionReportBetaAddons/').get('ReportBetaAddons')

    @property
    def option_report_alpha_addons(self):
        return self.zap._request(self.zap.base + 'autoupdate/view/optionReportAlphaAddons/').get('ReportAlphaAddons')

    def download_latest_release(self, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/downloadLatestRelease/', {'apikey' : apikey})

    def set_option_check_on_start(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionCheckOnStart/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_download_new_release(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionDownloadNewRelease/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_check_addon_updates(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionCheckAddonUpdates/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_install_addon_updates(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionInstallAddonUpdates/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_install_scanner_rules(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionInstallScannerRules/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_report_release_addons(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportReleaseAddons/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_report_beta_addons(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportBetaAddons/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_report_alpha_addons(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportAlphaAddons/', {'Boolean' : boolean, 'apikey' : apikey})


