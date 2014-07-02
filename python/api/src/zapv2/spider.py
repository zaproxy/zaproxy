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

class spider(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def status(self):
        return self.zap._request(self.zap.base + 'spider/view/status/').get('status')

    @property
    def results(self):
        return self.zap._request(self.zap.base + 'spider/view/results/').get('results')

    @property
    def excluded_from_scan(self):
        return self.zap._request(self.zap.base + 'spider/view/excludedFromScan/').get('excludedFromScan')

    @property
    def option_max_depth(self):
        return self.zap._request(self.zap.base + 'spider/view/optionMaxDepth/').get('MaxDepth')

    @property
    def option_scope_text(self):
        return self.zap._request(self.zap.base + 'spider/view/optionScopeText/').get('ScopeText')

    @property
    def option_scope(self):
        return self.zap._request(self.zap.base + 'spider/view/optionScope/').get('Scope')

    @property
    def option_thread_count(self):
        return self.zap._request(self.zap.base + 'spider/view/optionThreadCount/').get('ThreadCount')

    @property
    def option_post_form(self):
        return self.zap._request(self.zap.base + 'spider/view/optionPostForm/').get('PostForm')

    @property
    def option_process_form(self):
        return self.zap._request(self.zap.base + 'spider/view/optionProcessForm/').get('ProcessForm')

    @property
    def option_skip_url_string(self):
        return self.zap._request(self.zap.base + 'spider/view/optionSkipURLString/').get('SkipURLString')

    @property
    def option_request_wait_time(self):
        return self.zap._request(self.zap.base + 'spider/view/optionRequestWaitTime/').get('RequestWaitTime')

    @property
    def option_user_agent(self):
        return self.zap._request(self.zap.base + 'spider/view/optionUserAgent/').get('UserAgent')

    @property
    def option_parse_comments(self):
        return self.zap._request(self.zap.base + 'spider/view/optionParseComments/').get('ParseComments')

    @property
    def option_parse_robots_txt(self):
        return self.zap._request(self.zap.base + 'spider/view/optionParseRobotsTxt/').get('ParseRobotsTxt')

    @property
    def option_parse_svn_entries(self):
        return self.zap._request(self.zap.base + 'spider/view/optionParseSVNEntries/').get('ParseSVNEntries')

    @property
    def option_parse_git(self):
        return self.zap._request(self.zap.base + 'spider/view/optionParseGit/').get('ParseGit')

    @property
    def option_handle_parameters(self):
        return self.zap._request(self.zap.base + 'spider/view/optionHandleParameters/').get('HandleParameters')

    @property
    def option_handle_o_data_parameters_visited(self):
        return self.zap._request(self.zap.base + 'spider/view/optionHandleODataParametersVisited/').get('HandleODataParametersVisited')

    @property
    def option_domains_always_in_scope(self):
        return self.zap._request(self.zap.base + 'spider/view/optionDomainsAlwaysInScope/').get('DomainsAlwaysInScope')

    @property
    def option_domains_always_in_scope_enabled(self):
        return self.zap._request(self.zap.base + 'spider/view/optionDomainsAlwaysInScopeEnabled/').get('DomainsAlwaysInScopeEnabled')

    def scan(self, url, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/scan/', {'url' : url, 'apikey' : apikey})

    def scan_as_user(self, url, contextid, userid, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/scanAsUser/', {'url' : url, 'contextId' : contextid, 'userId' : userid, 'apikey' : apikey})

    def stop(self, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/stop/', {'apikey' : apikey})

    def clear_excluded_from_scan(self, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/clearExcludedFromScan/', {'apikey' : apikey})

    def exclude_from_scan(self, regex, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/excludeFromScan/', {'regex' : regex, 'apikey' : apikey})

    def set_option_skip_url_string(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionSkipURLString/', {'String' : string, 'apikey' : apikey})

    def set_option_handle_parameters(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionHandleParameters/', {'String' : string, 'apikey' : apikey})

    def set_option_scope_string(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionScopeString/', {'String' : string, 'apikey' : apikey})

    def set_option_user_agent(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionUserAgent/', {'String' : string, 'apikey' : apikey})

    def set_option_max_depth(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionMaxDepth/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_thread_count(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionThreadCount/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_post_form(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionPostForm/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_process_form(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionProcessForm/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_request_wait_time(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionRequestWaitTime/', {'Integer' : integer, 'apikey' : apikey})

    def set_option_parse_comments(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionParseComments/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_parse_robots_txt(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionParseRobotsTxt/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_parse_svn_entries(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionParseSVNEntries/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_parse_git(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionParseGit/', {'Boolean' : boolean, 'apikey' : apikey})

    def set_option_handle_o_data_parameters_visited(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'spider/action/setOptionHandleODataParametersVisited/', {'Boolean' : boolean, 'apikey' : apikey})


