# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2012 ZAP development team
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
Client implementation for using the ZAP pentesting proxy remotely.
"""

__docformat__ = 'restructuredtext'

import json
import urllib


class ZapError(Exception):
    """
    Base ZAP exception.
    """
    pass


class ZAP(object):
    """
    Client API implementation for integrating with ZAP.
    """

    # base api url
    base = 'http://zap/JSON/'

    def __init__(self, proxies={'http': 'http://127.0.0.1:8080',
        'https': 'http://127.0.0.1:8080'}):
        """
        Creates an instance of the ZAP api client.

        :Parameters:
           - `proxies`: dictionary of ZAP proxies to use.
        """
        self.__proxies = proxies

    def _expect_ok(self, json_data):
        """
        Checks that we have an OK response, else raises an exception.

        :Parameters:
           - `json_data`: the json data to look at.
        """
        if type(json_data) == type(list()) and json_data[0] == u'OK':
            return json_data
        raise ZapError(*json_data.values())

    def urlopen(self, *args, **kwargs):
        """
        Opens a url forcing the proxies to be used.

        :Parameters:
           - `args`:  all non-keyword arguments.
           - `kwargs`: all other keyword arguments.
        """
        kwargs['proxies'] = self.__proxies
        return urllib.urlopen(*args, **kwargs).read()

    def _request(self, url, get={}):
        """
        Shortcut for a GET request.

        :Parameters:
           - `url`: the url to GET at.
           - `get`: the disctionary to turn into GET variables.
        """
        return json.loads(self.urlopen(url + '?' + urllib.urlencode(get)))

    def start_spider(self, url):
        """
        Starts a spider session.

        :Parameters:
           - `url`: url to start spidering from.
        """
        return self._expect_ok(
            self._request(self.base + 'spider/action/scan/', {'url': url}))

    def start_session(self, name):
        """
        Starts a session.

        :Parameters:
           - `name`: name of the session to start. It must exist.
        """
        return self._expect_ok(self._request(
            self.base + 'core/action/newsession/', {'name': name}))

    def load_session(self, name):
        """
        Loads a previous session.

        :Parameters:
           - `name`: name of the session to load. It must exist.
        """
        return self._expect_ok(
            self._request(self.base + 'core/action/loadsession/',
            {'name': name}))

    def save_session(self, name):
        """
        Saves a session.

        :Parameters:
           - `name`: name of the session to save. It must not exist.
        """
        return self._expect_ok(
            self._request(self.base + 'core/action/savesession/',
            {'name': name}))

    def start_scan(self, url):
        """
        Starts a scan.

        :Parameters:
           - `url`: url to start the scan from.
        """
        return self._expect_ok(self._request(
            self.base + 'ascan/action/scan/', {'url': url}))

    def shutdown(self):
        """
        SHUT IT DOWN!

        Closes the proxy.
        """
        try:
            self._request(self.base + 'core/action/shutdown/')
        except IOError:
            # This is expected since shutdown kills the proxy
            pass

    # Read-only properties start here
    def _get_property(self, unit, name):
        """
        Way of getting properties from URL's without repeating so much code.

        :Parameters:
           - `unit`: the unit being requested.
           - `name`: name under the unit.
        """
        return self._request(self.base + '%s/view/%s/' % (unit, name))

    #:Property showing the scan status
    scan_status = property(lambda s: s._get_property('ascan', 'status'))
    #:Property showing the spider status
    spider_status = property(lambda s: s._get_property('spider', 'status'))
    #:Property showing alerts
    alerts = property(lambda s: s._get_property('core', 'alerts'))
    #:Property showing hosts
    hosts = property(lambda s: s._get_property('core', 'hosts'))
    #:Property showing sites
    sites = property(lambda s: s._get_property('core', 'sites'))
    #:Property showing urls
    urls = property(lambda s: s._get_property('core', 'urls'))
