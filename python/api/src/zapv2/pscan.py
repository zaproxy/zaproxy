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

class pscan(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def records_to_scan(self):
        """
        The number of records the passive scanner still has to scan
        """
        return self.zap._request(self.zap.base + 'pscan/view/recordsToScan/').get('recordsToScan')

    @property
    def scanners(self):
        return self.zap._request(self.zap.base + 'pscan/view/scanners/').get('scanners')

    def set_enabled(self, enabled, apikey=''):
        return self.zap._request(self.zap.base + 'pscan/action/setEnabled/', {'enabled' : enabled, 'apikey' : apikey})

    def enable_all_scanners(self, apikey=''):
        return self.zap._request(self.zap.base + 'pscan/action/enableAllScanners/', {'apikey' : apikey})

    def disable_all_scanners(self, apikey=''):
        return self.zap._request(self.zap.base + 'pscan/action/disableAllScanners/', {'apikey' : apikey})

    def enable_scanners(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'pscan/action/enableScanners/', {'ids' : ids, 'apikey' : apikey})

    def disable_scanners(self, ids, apikey=''):
        return self.zap._request(self.zap.base + 'pscan/action/disableScanners/', {'ids' : ids, 'apikey' : apikey})


