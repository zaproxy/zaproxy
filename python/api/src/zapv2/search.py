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

class search(object):

    def __init__(self, zap):
        self.zap = zap

    def urls_by_url_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/urlsByUrlRegex/', params).itervalues())

    def urls_by_request_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/urlsByRequestRegex/', params).itervalues())

    def urls_by_response_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/urlsByResponseRegex/', params).itervalues())

    def urls_by_header_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/urlsByHeaderRegex/', params).itervalues())

    def messages_by_url_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/messagesByUrlRegex/', params).itervalues())

    def messages_by_request_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/messagesByRequestRegex/', params).itervalues())

    def messages_by_response_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/messagesByResponseRegex/', params).itervalues())

    def messages_by_header_regex(self, regex, baseurl=None, start=None, count=None):
        params = {'regex' : regex}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'search/view/messagesByHeaderRegex/', params).itervalues())

    def har_by_url_regex(self, regex, baseurl=None, start=None, count=None, apikey=''):
        params = {'regex' : regex, 'apikey' : apikey}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return (self.zap._request_other(self.zap.base_other + 'search/other/harByUrlRegex/', params))

    def har_by_request_regex(self, regex, baseurl=None, start=None, count=None, apikey=''):
        params = {'regex' : regex, 'apikey' : apikey}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return (self.zap._request_other(self.zap.base_other + 'search/other/harByRequestRegex/', params))

    def har_by_response_regex(self, regex, baseurl=None, start=None, count=None, apikey=''):
        params = {'regex' : regex, 'apikey' : apikey}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return (self.zap._request_other(self.zap.base_other + 'search/other/harByResponseRegex/', params))

    def har_by_header_regex(self, regex, baseurl=None, start=None, count=None, apikey=''):
        params = {'regex' : regex, 'apikey' : apikey}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return (self.zap._request_other(self.zap.base_other + 'search/other/harByHeaderRegex/', params))


