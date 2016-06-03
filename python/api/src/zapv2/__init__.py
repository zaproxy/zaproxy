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
from acsrf import acsrf
from ascan import ascan
from ajaxSpider import ajaxSpider
from authentication import authentication
from authorization import authorization
from autoupdate import autoupdate
from brk import brk
from context import context
from core import core
from forcedUser import forcedUser
from httpSessions import httpSessions
from importLogFiles import importLogFiles
from params import params
from pnh import pnh
from pscan import pscan
from reveal import reveal
from script import script
from search import search
from selenium import selenium
from sessionManagement import sessionManagement
from spider import spider
from stats import stats
from users import users

class ZapError(Exception):
    """
    Base ZAP exception.
    """
    pass


class ZAPv2(object):
    """
    Client API implementation for integrating with ZAP v2.
    """

    # base JSON api url
    base = 'http://zap/JSON/'
    # base OTHER api url
    base_other = 'http://zap/OTHER/'

    def __init__(self, proxies={'http': 'http://127.0.0.1:8080',
        'https': 'http://127.0.0.1:8080'}):
        """
        Creates an instance of the ZAP api client.

        :Parameters:
           - `proxies`: dictionary of ZAP proxies to use.
           
        Note that all of the other classes in this directory are generated
        new ones will need to be manually added to this file
        """
        self.__proxies = proxies
        
        self.acsrf = acsrf(self)
        self.ajaxSpider = ajaxSpider(self)
        self.ascan = ascan(self)
        self.authentication = authentication(self)
        self.authorization = authorization(self)
        self.autoupdate = autoupdate(self)
        self.brk = brk(self)
        self.context = context(self)
        self.core = core(self)
        self.forcedUser = forcedUser(self)
        self.httpsessions = httpSessions(self)
        self.importLogFiles = importLogFiles(self)
        self.params = params(self)
        self.pnh = pnh(self)
        self.pscan = pscan(self)
        self.reveal = reveal(self)
        self.script = script(self)
        self.search = search(self)
        self.selenium = selenium(self)
        self.sessionManagement = sessionManagement(self)
        self.spider = spider(self)
        self.stats = stats(self)
        self.users = users(self)

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

    def status_code(self, *args, **kwargs):
      """
      Open a url forcing the proxies to be used.

      :Parameters:
         - `args`: all non-keyword arguments.
         - `kwargs`: all other keyword arguments.
      """
      kwargs['proxies'] = self.__proxies
      return urllib.urlopen(*args, **kwargs).getcode()

    def _request(self, url, get={}):
        """
        Shortcut for a GET request.

        :Parameters:
           - `url`: the url to GET at.
           - `get`: the disctionary to turn into GET variables.
        """
        return json.loads(self.urlopen(url + '?' + urllib.urlencode(get)))

    def _request_other(self, url, get={}):
        """
        Shortcut for an API OTHER GET request.

        :Parameters:
           - `url`: the url to GET at.
           - `get`: the disctionary to turn into GET variables.
        """
        return self.urlopen(url + '?' + urllib.urlencode(get))
