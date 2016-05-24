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


class ajaxSpider(object):
	def __init__(self, zap):
		self.zap = zap

	@property
	def status(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/status/')))

	def results(self, start='', count=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'ajaxSpider/view/results/', {'start': start, 'count': count})))

	@property
	def number_of_results(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/numberOfResults/')))

	@property
	def option_browser_id(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionBrowserId/')))

	@property
	def option_config_version_key(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionConfigVersionKey/')))

	@property
	def option_current_version(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionCurrentVersion/')))

	@property
	def option_elems(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionElems/')))

	@property
	def option_elems_names(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionElemsNames/')))

	@property
	def option_event_wait(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionEventWait/')))

	@property
	def option_max_crawl_depth(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionMaxCrawlDepth/')))

	@property
	def option_max_crawl_states(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionMaxCrawlStates/')))

	@property
	def option_max_duration(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionMaxDuration/')))

	@property
	def option_number_of_browsers(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionNumberOfBrowsers/')))

	@property
	def option_reload_wait(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionReloadWait/')))

	@property
	def option_click_default_elems(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionClickDefaultElems/')))

	@property
	def option_click_elems_once(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionClickElemsOnce/')))

	@property
	def option_random_inputs(self):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/view/optionRandomInputs/')))

	def scan(self, url, inscope='', apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/scan/',
		                                             {'url': url, 'inScope': inscope, 'apikey': apikey})))

	def stop(self, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/stop/', {'apikey': apikey})))

	def set_option_browser_id(self, string, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionBrowserId/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_click_default_elems(self, boolean, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionClickDefaultElems/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_click_elems_once(self, boolean, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionClickElemsOnce/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_event_wait(self, integer, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionEventWait/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_max_crawl_depth(self, integer, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionMaxCrawlDepth/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_max_crawl_states(self, integer, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionMaxCrawlStates/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_max_duration(self, integer, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionMaxDuration/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_number_of_browsers(self, integer, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionNumberOfBrowsers/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_random_inputs(self, boolean, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionRandomInputs/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_reload_wait(self, integer, apikey=''):
		"""
		This component is optional and therefore the API will only work if it is installed
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'ajaxSpider/action/setOptionReloadWait/',
		                                             {'Integer': integer, 'apikey': apikey})))
