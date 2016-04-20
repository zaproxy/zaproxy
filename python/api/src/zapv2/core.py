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


class core(object):
	def __init__(self, zap):
		self.zap = zap

	def alert(self, id):
		"""
		Gets the alert with the given ID, the corresponding HTTP message can be obtained with the 'messageId' field
		and 'message' API method
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/alert/', {'id': id})))

	def alerts(self, baseurl=None, start=None, count=None):
		"""
		Gets the alerts raised by ZAP, optionally filtering by URL and paginating with 'start' position and 'count' of
		alerts
		"""
		params = {}
		if baseurl is not None:
			params['baseurl'] = baseurl
		if start is not None:
			params['start'] = start
		if count is not None:
			params['count'] = count
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/alerts/', params)))

	def number_of_alerts(self, baseurl=None):
		"""
		Gets the number of alerts, optionally filtering by URL
		"""
		params = {}
		if baseurl is not None:
			params['baseurl'] = baseurl
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/numberOfAlerts/', params)))

	@property
	def hosts(self):
		"""
		Gets the name of the hosts accessed through/by ZAP
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/hosts/')))

	@property
	def sites(self):
		"""
		Gets the sites accessed through/by ZAP (scheme and domain)
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/sites/')))

	@property
	def urls(self):
		"""
		Gets the URLs accessed through/by ZAP
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/urls/')))

	def message(self, id):
		"""
		Gets the HTTP message with the given ID. Returns the ID, request/response headers and bodies, cookies and note.
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/message/', {'id': id})))

	def messages(self, baseurl=None, start=None, count=None):
		"""
		Gets the HTTP messages sent by ZAP, request and response, optionally filtered by URL and paginated with
		'start' position and 'count' of messages
		"""
		params = {}
		if baseurl is not None:
			params['baseurl'] = baseurl
		if start is not None:
			params['start'] = start
		if count is not None:
			params['count'] = count
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/messages/', params)))

	def number_of_messages(self, baseurl=None):
		"""
		Gets the number of messages, optionally filtering by URL
		"""
		params = {}
		if baseurl is not None:
			params['baseurl'] = baseurl
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/numberOfMessages/', params)))

	@property
	def version(self):
		"""
		Gets ZAP version
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/version/')))

	@property
	def excluded_from_proxy(self):
		"""
		Gets the regular expressions, applied to URLs, to exclude from the Proxy
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/excludedFromProxy/')))

	@property
	def home_directory(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/homeDirectory/')))

	def stats(self, keyprefix=None):
		params = {}
		if keyprefix is not None:
			params['keyPrefix'] = keyprefix
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/stats/', params)))

	@property
	def option_default_user_agent(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionDefaultUserAgent/')))

	@property
	def option_http_state(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionHttpState/')))

	@property
	def option_proxy_chain_name(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyChainName/')))

	@property
	def option_proxy_chain_password(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyChainPassword/')))

	@property
	def option_proxy_chain_port(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyChainPort/')))

	@property
	def option_proxy_chain_realm(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyChainRealm/')))

	@property
	def option_proxy_chain_skip_name(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyChainSkipName/')))

	@property
	def option_proxy_chain_user_name(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyChainUserName/')))

	@property
	def option_proxy_excluded_domains(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyExcludedDomains/')))

	@property
	def option_proxy_excluded_domains_enabled(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyExcludedDomainsEnabled/')))

	@property
	def option_timeout_in_secs(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionTimeoutInSecs/')))

	@property
	def option_http_state_enabled(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionHttpStateEnabled/')))

	@property
	def option_proxy_chain_prompt(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionProxyChainPrompt/')))

	@property
	def option_single_cookie_request_header(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionSingleCookieRequestHeader/')))

	@property
	def option_use_proxy_chain(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionUseProxyChain/')))

	@property
	def option_use_proxy_chain_auth(self):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/view/optionUseProxyChainAuth/')))

	def shutdown(self, apikey=''):
		"""
		Shuts down ZAP
		"""
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/shutdown/', {'apikey': apikey})))

	def new_session(self, name=None, overwrite=None, apikey=''):
		"""
		Creates a new session, optionally overwriting existing files. If a relative path is specified it will be
		resolved against the "session" directory in ZAP "home" dir.
		"""
		params = {'apikey': apikey}
		if name is not None:
			params['name'] = name
		if overwrite is not None:
			params['overwrite'] = overwrite
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/newSession/', params)))

	def load_session(self, name, apikey=''):
		"""
		Loads the session with the given name. If a relative path is specified it will be resolved against the
		"session" directory in ZAP "home" dir.
		"""
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'core/action/loadSession/', {'name': name, 'apikey': apikey})))

	def save_session(self, name, overwrite=None, apikey=''):
		"""
		Saves the session with the name supplied, optionally overwriting existing files. If a relative path is
		specified it will be resolved against the "session" directory in ZAP "home" dir.
		"""
		params = {'name': name, 'apikey': apikey}
		if overwrite is not None:
			params['overwrite'] = overwrite
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/saveSession/', params)))

	def snapshot_session(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'core/action/snapshotSession/', {'apikey': apikey})))

	def clear_excluded_from_proxy(self, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'core/action/clearExcludedFromProxy/', {'apikey': apikey})))

	def exclude_from_proxy(self, regex, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'core/action/excludeFromProxy/', {'regex': regex, 'apikey': apikey})))

	def set_home_directory(self, dir, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'core/action/setHomeDirectory/', {'dir': dir, 'apikey': apikey})))

	def generate_root_ca(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'core/action/generateRootCA/', {'apikey': apikey})))

	def send_request(self, request, followredirects=None, apikey=''):
		"""
		Sends the HTTP request, optionally following redirections. Returns the request sent and response received and
		followed redirections, if any.
		"""
		params = {'request': request, 'apikey': apikey}
		if followredirects is not None:
			params['followRedirects'] = followredirects
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/sendRequest/', params)))

	def delete_all_alerts(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'core/action/deleteAllAlerts/', {'apikey': apikey})))

	def run_garbage_collection(self, apikey=''):
		return next(
			six.itervalues(self.zap._request(self.zap.base + 'core/action/runGarbageCollection/', {'apikey': apikey})))

	def clear_stats(self, keyprefix, apikey=''):
		return next(six.itervalues(
			self.zap._request(self.zap.base + 'core/action/clearStats/', {'keyPrefix': keyprefix, 'apikey': apikey})))

	def set_option_default_user_agent(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionDefaultUserAgent/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_proxy_chain_name(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainName/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_proxy_chain_password(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPassword/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_proxy_chain_realm(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainRealm/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_proxy_chain_skip_name(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainSkipName/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_proxy_chain_user_name(self, string, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainUserName/',
		                                             {'String': string, 'apikey': apikey})))

	def set_option_http_state_enabled(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionHttpStateEnabled/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_proxy_chain_port(self, integer, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPort/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_proxy_chain_prompt(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPrompt/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_single_cookie_request_header(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionSingleCookieRequestHeader/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_timeout_in_secs(self, integer, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionTimeoutInSecs/',
		                                             {'Integer': integer, 'apikey': apikey})))

	def set_option_use_proxy_chain(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionUseProxyChain/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def set_option_use_proxy_chain_auth(self, boolean, apikey=''):
		return next(six.itervalues(self.zap._request(self.zap.base + 'core/action/setOptionUseProxyChainAuth/',
		                                             {'Boolean': boolean, 'apikey': apikey})))

	def proxy_pac(self, apikey=''):
		return self.zap._request_other(self.zap.base_other + 'core/other/proxy.pac/', {'apikey': apikey})

	def rootcert(self, apikey=''):
		return self.zap._request_other(self.zap.base_other + 'core/other/rootcert/', {'apikey': apikey})

	def setproxy(self, proxy, apikey=''):
		return (
			self.zap._request_other(self.zap.base_other + 'core/other/setproxy/', {'proxy': proxy, 'apikey': apikey}))

	def xmlreport(self, apikey=''):
		"""
		Generates a report in XML format
		"""
		return self.zap._request_other(self.zap.base_other + 'core/other/xmlreport/', {'apikey': apikey})

	def htmlreport(self, apikey=''):
		"""
		Generates a report in HTML format
		"""
		return self.zap._request_other(self.zap.base_other + 'core/other/htmlreport/', {'apikey': apikey})

	def message_har(self, id, apikey=''):
		"""
		Gets the message with the given ID in HAR format
		"""
		return self.zap._request_other(self.zap.base_other + 'core/other/messageHar/', {'id': id, 'apikey': apikey})

	def messages_har(self, baseurl=None, start=None, count=None, apikey=''):
		"""
		Gets the HTTP messages sent through/by ZAP, in HAR format, optionally filtered by URL and paginated with
		'start' position and 'count' of messages
		"""
		params = {'apikey': apikey}
		if baseurl is not None:
			params['baseurl'] = baseurl
		if start is not None:
			params['start'] = start
		if count is not None:
			params['count'] = count
		return self.zap._request_other(self.zap.base_other + 'core/other/messagesHar/', params)

	def send_har_request(self, request, followredirects=None, apikey=''):
		"""
		Sends the first HAR request entry, optionally following redirections. Returns, in HAR format, the request sent
		and response received and followed redirections, if any.
		"""
		params = {'request': request, 'apikey': apikey}
		if followredirects is not None:
			params['followRedirects'] = followredirects
		return self.zap._request_other(self.zap.base_other + 'core/other/sendHarRequest/', params)
