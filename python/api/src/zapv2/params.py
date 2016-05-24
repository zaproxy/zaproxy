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

class params(object):

    def __init__(self, zap):
        self.zap = zap

    def params(self, site=None):
        """
        Shows the parameters for the specified site, or for all sites if the site is not specified
        """
        params = {}
        if site is not None:
            params['site'] = site
        return next(self.zap._request(self.zap.base + 'params/view/params/', params).itervalues())


