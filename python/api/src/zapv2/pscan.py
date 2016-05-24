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

class pscan(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def records_to_scan(self):
        """
        The number of records the passive scanner still has to scan
        """
        return next(self.zap._request(self.zap.base + 'pscan/view/recordsToScan/').itervalues())

    @property
    def scanners(self):
        """
        Lists all passive scanners with its ID, name, enabled state and alert threshold.
        """
        return next(self.zap._request(self.zap.base + 'pscan/view/scanners/').itervalues())

    def set_enabled(self, enabled, apikey=''):
        """
        Sets whether or not the passive scanning is enabled
        """
        return next(self.zap._request(self.zap.base + 'pscan/action/setEnabled/', {'enabled' : enabled, 'apikey' : apikey}).itervalues())

    def enable_all_scanners(self, apikey=''):
        """
        Enables all passive scanners
        """
        return next(self.zap._request(self.zap.base + 'pscan/action/enableAllScanners/', {'apikey' : apikey}).itervalues())

    def disable_all_scanners(self, apikey=''):
        """
        Disables all passive scanners
        """
        return next(self.zap._request(self.zap.base + 'pscan/action/disableAllScanners/', {'apikey' : apikey}).itervalues())

    def enable_scanners(self, ids, apikey=''):
        """
        Enables all passive scanners with the given IDs (comma separated list of IDs)
        """
        return next(self.zap._request(self.zap.base + 'pscan/action/enableScanners/', {'ids' : ids, 'apikey' : apikey}).itervalues())

    def disable_scanners(self, ids, apikey=''):
        """
        Disables all passive scanners with the given IDs (comma separated list of IDs)
        """
        return next(self.zap._request(self.zap.base + 'pscan/action/disableScanners/', {'ids' : ids, 'apikey' : apikey}).itervalues())

    def set_scanner_alert_threshold(self, id, alertthreshold, apikey=''):
        """
        Sets the alert threshold of the passive scanner with the given ID, accepted values for alert threshold: OFF, DEFAULT, LOW, MEDIUM and HIGH
        """
        return next(self.zap._request(self.zap.base + 'pscan/action/setScannerAlertThreshold/', {'id' : id, 'alertThreshold' : alertthreshold, 'apikey' : apikey}).itervalues())


