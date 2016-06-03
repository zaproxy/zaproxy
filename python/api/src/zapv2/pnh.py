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

class pnh(object):

    def __init__(self, zap):
        self.zap = zap

    def monitor(self, id, message, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'pnh/action/monitor/', {'id' : id, 'message' : message, 'apikey' : apikey}).itervalues())

    def oracle(self, id, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'pnh/action/oracle/', {'id' : id, 'apikey' : apikey}).itervalues())

    def start_monitoring(self, url, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'pnh/action/startMonitoring/', {'url' : url, 'apikey' : apikey}).itervalues())

    def stop_monitoring(self, id, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'pnh/action/stopMonitoring/', {'id' : id, 'apikey' : apikey}).itervalues())

    def pnh(self, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return (self.zap._request_other(self.zap.base_other + 'pnh/other/pnh/', {'apikey' : apikey}))

    def manifest(self, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return (self.zap._request_other(self.zap.base_other + 'pnh/other/manifest/', {'apikey' : apikey}))

    def service(self, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return (self.zap._request_other(self.zap.base_other + 'pnh/other/service/', {'apikey' : apikey}))

    def fx__pnh_xpi(self, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return (self.zap._request_other(self.zap.base_other + 'pnh/other/fx_pnh.xpi/', {'apikey' : apikey}))


