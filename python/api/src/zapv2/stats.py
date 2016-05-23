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

class stats(object):

    def __init__(self, zap):
        self.zap = zap

    def stats(self, keyprefix=None):
        """
        Statistics
        """
        params = {}
        if keyprefix is not None:
            params['keyPrefix'] = keyprefix
        return next(self.zap._request(self.zap.base + 'stats/view/stats/', params).itervalues())

    def all_sites_stats(self, keyprefix=None):
        """
        Gets all of the site based statistics, optionally filtered by a key prefix
        """
        params = {}
        if keyprefix is not None:
            params['keyPrefix'] = keyprefix
        return next(self.zap._request(self.zap.base + 'stats/view/allSitesStats/', params).itervalues())

    def site_stats(self, site, keyprefix=None):
        """
        Gets all of the global statistics, optionally filtered by a key prefix
        """
        params = {'site' : site}
        if keyprefix is not None:
            params['keyPrefix'] = keyprefix
        return next(self.zap._request(self.zap.base + 'stats/view/siteStats/', params).itervalues())

    @property
    def option_statsd_host(self):
        """
        Gets the Statsd service hostname
        """
        return next(self.zap._request(self.zap.base + 'stats/view/optionStatsdHost/').itervalues())

    @property
    def option_statsd_port(self):
        """
        Gets the Statsd service port
        """
        return next(self.zap._request(self.zap.base + 'stats/view/optionStatsdPort/').itervalues())

    @property
    def option_statsd_prefix(self):
        """
        Gets the prefix to be applied to all stats sent to the configured Statsd service
        """
        return next(self.zap._request(self.zap.base + 'stats/view/optionStatsdPrefix/').itervalues())

    @property
    def option_in_memory_enabled(self):
        """
        Returns 'true' if in memory statistics are enabled, otherwise returns 'false'
        """
        return next(self.zap._request(self.zap.base + 'stats/view/optionInMemoryEnabled/').itervalues())

    @property
    def option_statsd_enabled(self):
        """
        Returns 'true' if a Statsd server has been correctly configured, otherwise returns 'false'
        """
        return next(self.zap._request(self.zap.base + 'stats/view/optionStatsdEnabled/').itervalues())

    def clear_stats(self, keyprefix=None, apikey=''):
        """
        Clears all of the statistics
        """
        params = {'apikey' : apikey}
        if keyprefix is not None:
            params['keyPrefix'] = keyprefix
        return next(self.zap._request(self.zap.base + 'stats/action/clearStats/', params).itervalues())

    def set_option_statsd_host(self, string, apikey=''):
        """
        Sets the Statsd service hostname, supply an empty string to stop using a Statsd service
        """
        return next(self.zap._request(self.zap.base + 'stats/action/setOptionStatsdHost/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_statsd_prefix(self, string, apikey=''):
        """
        Sets the prefix to be applied to all stats sent to the configured Statsd service
        """
        return next(self.zap._request(self.zap.base + 'stats/action/setOptionStatsdPrefix/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_in_memory_enabled(self, boolean, apikey=''):
        """
        Sets whether in memory statistics are enabled
        """
        return next(self.zap._request(self.zap.base + 'stats/action/setOptionInMemoryEnabled/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_statsd_port(self, integer, apikey=''):
        """
        Sets the Statsd service port
        """
        return next(self.zap._request(self.zap.base + 'stats/action/setOptionStatsdPort/', {'Integer' : integer, 'apikey' : apikey}).itervalues())


