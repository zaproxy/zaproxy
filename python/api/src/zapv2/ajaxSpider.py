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

class ajaxSpider(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def status(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/status/').itervalues())

    def results(self, start=None, count=None):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        params = {}
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/results/', params).itervalues())

    @property
    def number_of_results(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/numberOfResults/').itervalues())

    @property
    def option_browser_id(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionBrowserId/').itervalues())

    @property
    def option_event_wait(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionEventWait/').itervalues())

    @property
    def option_max_crawl_depth(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionMaxCrawlDepth/').itervalues())

    @property
    def option_max_crawl_states(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionMaxCrawlStates/').itervalues())

    @property
    def option_max_duration(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionMaxDuration/').itervalues())

    @property
    def option_number_of_browsers(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionNumberOfBrowsers/').itervalues())

    @property
    def option_reload_wait(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionReloadWait/').itervalues())

    @property
    def option_click_default_elems(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionClickDefaultElems/').itervalues())

    @property
    def option_click_elems_once(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionClickElemsOnce/').itervalues())

    @property
    def option_random_inputs(self):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/view/optionRandomInputs/').itervalues())

    def scan(self, url, inscope=None, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        params = {'url' : url, 'apikey' : apikey}
        if inscope is not None:
            params['inScope'] = inscope
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/scan/', params).itervalues())

    def stop(self, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/stop/', {'apikey' : apikey}).itervalues())

    def set_option_browser_id(self, string, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionBrowserId/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_click_default_elems(self, boolean, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionClickDefaultElems/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_click_elems_once(self, boolean, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionClickElemsOnce/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_event_wait(self, integer, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionEventWait/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_max_crawl_depth(self, integer, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionMaxCrawlDepth/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_max_crawl_states(self, integer, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionMaxCrawlStates/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_max_duration(self, integer, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionMaxDuration/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_number_of_browsers(self, integer, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionNumberOfBrowsers/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_random_inputs(self, boolean, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionRandomInputs/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_reload_wait(self, integer, apikey=''):
        """
        This component is optional and therefore the API will only work if it is installed
        """
        return next(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionReloadWait/', {'Integer' : integer, 'apikey' : apikey}).itervalues())


