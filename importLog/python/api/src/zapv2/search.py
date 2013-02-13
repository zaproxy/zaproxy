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

class search(object):

    def __init__(self, zap):
        self.zap = zap

    def urls_by_url_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByUrlRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count})

    def urls_by_request_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByRequestRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count})

    def urls_by_response_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByResponseRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count})

    def urls_by_header_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByHeaderRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count})


