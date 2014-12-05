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

class ajaxSpider(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def status(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return self.zap._request(self.zap.base + 'ajaxSpider/view/status/').get('status')

    def results(self, start='', count=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return self.zap._request(self.zap.base + 'ajaxSpider/view/results/', {'start' : start, 'count' : count}).get('results')

    @property
    def number_of_results(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return self.zap._request(self.zap.base + 'ajaxSpider/view/numberOfResults/').get('numberOfResults')

    def scan(self, url, inscope='', apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return self.zap._request(self.zap.base + 'ajaxSpider/action/scan/', {'url' : url, 'inScope' : inscope, 'apikey' : apikey})

    def stop(self, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return self.zap._request(self.zap.base + 'ajaxSpider/action/stop/', {'apikey' : apikey})


