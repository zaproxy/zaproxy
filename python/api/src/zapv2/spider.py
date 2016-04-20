# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2015 the ZAP development team
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

import six


class spider(object):
	def __init__(self, zap):
		self.zap = zap

	def status(self, scanid=None):
		params = {}
		if scanid is not None:
			params['scanId'] = scanid
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/status/', params)))

	def results(self, scanid=None):
		params = {}
		if scanid is not None:
			params['scanId'] = scanid
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/results/', params)))

	def full_results(self, scanid):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/fullResults/', {'scanId': scanid})))

	@property
	def scans(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/scans/')))

	@property
	def excluded_from_scan(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/excludedFromScan/')))

	@property
	def option_domains_always_in_scope(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionDomainsAlwaysInScope/')))

	@property
	def option_domains_always_in_scope_enabled(self):
		return next(six.itervalues(self.zap._request(self.zap.base +
                                                     'spider/view/optionDomainsAlwaysInScopeEnabled/')))

	@property
	def option_handle_parameters(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionHandleParameters/')))

	@property
	def option_max_depth(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionMaxDepth/')))

	@property
	def option_max_scans_in_ui(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionMaxScansInUI/')))

	@property
	def option_request_wait_time(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionRequestWaitTime/')))

	@property
	def option_scope(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionScope/')))

	@property
	def option_scope_text(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionScopeText/')))

	@property
	def option_skip_url_string(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionSkipURLString/')))

	@property
	def option_thread_count(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionThreadCount/')))

	@property
	def option_user_agent(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionUserAgent/')))

	@property
	def option_handle_o_data_parameters_visited(self):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionHandleODataParametersVisited/')))

	@property
	def option_parse_comments(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionParseComments/')))

	@property
	def option_parse_git(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionParseGit/')))

	@property
	def option_parse_robots_txt(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionParseRobotsTxt/')))

	@property
	def option_parse_svn_entries(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionParseSVNEntries/')))

	@property
	def option_parse_sitemap_xml(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionParseSitemapXml/')))

	@property
	def option_post_form(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionPostForm/')))

	@property
	def option_process_form(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionProcessForm/')))

	@property
	def option_send_referer_header(self):
		"""
        Sets whether or not the 'Referer' header should be sent while spidering
        """
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionSendRefererHeader/')))

	@property
	def option_show_advanced_dialog(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/view/optionShowAdvancedDialog/')))

	def scan(self, url, maxchildren=None, recurse=None, contextname=None, apikey=''):
		"""
        Runs the spider against the given URL. Optionally, the 'maxChildren' parameter can be set to limit the number
        of children scanned, the 'recurse' parameter can be used to prevent the spider from seeding recursively and
        the parameter 'contextName' can be used to constrain the scan to a Context.
        """
		params = {'url': url, 'apikey': apikey}
		if maxchildren is not None:
			params['maxChildren'] = maxchildren
		if recurse is not None:
			params['recurse'] = recurse
		if contextname is not None:
			params['contextName'] = contextname
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/scan/', params)))

	def scan_as_user(self, url, contextid, userid, maxchildren=None, recurse=None, apikey=''):
		"""
        Runs the spider from the perspective of a User, obtained using the given Context ID and User ID. See 'scan'
        action for more details.
        """
		params = {'url': url, 'contextId': contextid, 'userId': userid, 'apikey': apikey}
		if maxchildren is not None:
			params['maxChildren'] = maxchildren
		if recurse is not None:
			params['recurse'] = recurse
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/scanAsUser/', params)))

	def pause(self, scanid, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'spider/action/pause/', {'scanId': scanid, 'apikey': apikey})))

	def resume(self, scanid, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'spider/action/resume/', {'scanId': scanid, 'apikey': apikey})))

	def stop(self, scanid=None, apikey=''):
		params = {'apikey': apikey}
		if scanid is not None:
			params['scanId'] = scanid
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/stop/', params)))

	def remove_scan(self, scanid, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'spider/action/removeScan/', {'scanId': scanid, 'apikey': apikey})))

	def pause_all_scans(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'spider/action/pauseAllScans/', {'apikey': apikey})))

	def resume_all_scans(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'spider/action/resumeAllScans/', {'apikey': apikey})))

	def stop_all_scans(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'spider/action/stopAllScans/', {'apikey': apikey})))

	def remove_all_scans(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'spider/action/removeAllScans/', {'apikey': apikey})))

	def clear_excluded_from_scan(self, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'spider/action/clearExcludedFromScan/', {'apikey': apikey})))

	def exclude_from_scan(self, regex, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'spider/action/excludeFromScan/', {'regex': regex, 'apikey': apikey})))

	def set_option_handle_parameters(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionHandleParameters/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_scope_string(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionScopeString/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_skip_url_string(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionSkipURLString/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_user_agent(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionUserAgent/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_handle_o_data_parameters_visited(self, boolean, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'spider/action/setOptionHandleODataParametersVisited/',
			                  {'Boolean': boolean, 'apikey': apikey})))

	def set_option_max_depth(self, integer, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionMaxDepth/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_max_scans_in_ui(self, integer, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionMaxScansInUI/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_parse_comments(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionParseComments/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_parse_git(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionParseGit/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_parse_robots_txt(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionParseRobotsTxt/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_parse_svn_entries(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionParseSVNEntries/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_parse_sitemap_xml(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionParseSitemapXml/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_post_form(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionPostForm/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_process_form(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionProcessForm/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_request_wait_time(self, integer, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionRequestWaitTime/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_send_referer_header(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionSendRefererHeader/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_show_advanced_dialog(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionShowAdvancedDialog/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_thread_count(self, integer, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'spider/action/setOptionThreadCount/',
		                                             {'Integer': integer, 'apikey': apikey})))
