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

class search(object):

    def __init__(self, zap):
        self.zap = zap

    def urls_by_url_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByUrlRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('urlsByUrlRegex')

    def urls_by_request_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByRequestRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('urlsByRequestRegex')

    def urls_by_response_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByResponseRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('urlsByResponseRegex')

    def urls_by_header_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/urlsByHeaderRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('urlsByHeaderRegex')

    def messages_by_url_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/messagesByUrlRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('messagesByUrlRegex')

    def messages_by_request_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/messagesByRequestRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('messagesByRequestRegex')

    def messages_by_response_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/messagesByResponseRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('messagesByResponseRegex')

    def messages_by_header_regex(self, regex, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'search/view/messagesByHeaderRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count}).get('messagesByHeaderRegex')

    def har_by_url_regex(self, regex, baseurl='', start='', count='', apikey=''):
        return self.zap._request_other(self.zap.base_other + 'search/other/harByUrlRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count, 'apikey' : apikey})

    def har_by_request_regex(self, regex, baseurl='', start='', count='', apikey=''):
        return self.zap._request_other(self.zap.base_other + 'search/other/harByRequestRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count, 'apikey' : apikey})

    def har_by_response_regex(self, regex, baseurl='', start='', count='', apikey=''):
        return self.zap._request_other(self.zap.base_other + 'search/other/harByResponseRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count, 'apikey' : apikey})

    def har_by_header_regex(self, regex, baseurl='', start='', count='', apikey=''):
        return self.zap._request_other(self.zap.base_other + 'search/other/harByHeaderRegex/', {'regex' : regex, 'baseurl' : baseurl, 'start' : start, 'count' : count, 'apikey' : apikey})


