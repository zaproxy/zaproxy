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

class core(object):

    def __init__(self, zap):
        self.zap = zap

    def alert(self, id):
        """
        Gets the alert with the given ID, the corresponding HTTP message can be obtained with the 'messageId' field and 'message' API method
        """
        return next(self.zap._request(self.zap.base + 'core/view/alert/', {'id' : id}).itervalues())

    def alerts(self, baseurl=None, start=None, count=None):
        """
        Gets the alerts raised by ZAP, optionally filtering by URL and paginating with 'start' position and 'count' of alerts
        """
        params = {}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'core/view/alerts/', params).itervalues())

    def number_of_alerts(self, baseurl=None):
        """
        Gets the number of alerts, optionally filtering by URL
        """
        params = {}
        if baseurl is not None:
            params['baseurl'] = baseurl
        return next(self.zap._request(self.zap.base + 'core/view/numberOfAlerts/', params).itervalues())

    @property
    def hosts(self):
        """
        Gets the name of the hosts accessed through/by ZAP
        """
        return next(self.zap._request(self.zap.base + 'core/view/hosts/').itervalues())

    @property
    def sites(self):
        """
        Gets the sites accessed through/by ZAP (scheme and domain)
        """
        return next(self.zap._request(self.zap.base + 'core/view/sites/').itervalues())

    @property
    def urls(self):
        """
        Gets the URLs accessed through/by ZAP
        """
        return next(self.zap._request(self.zap.base + 'core/view/urls/').itervalues())

    def message(self, id):
        """
        Gets the HTTP message with the given ID. Returns the ID, request/response headers and bodies, cookies and note.
        """
        return next(self.zap._request(self.zap.base + 'core/view/message/', {'id' : id}).itervalues())

    def messages(self, baseurl=None, start=None, count=None):
        """
        Gets the HTTP messages sent by ZAP, request and response, optionally filtered by URL and paginated with 'start' position and 'count' of messages
        """
        params = {}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return next(self.zap._request(self.zap.base + 'core/view/messages/', params).itervalues())

    def number_of_messages(self, baseurl=None):
        """
        Gets the number of messages, optionally filtering by URL
        """
        params = {}
        if baseurl is not None:
            params['baseurl'] = baseurl
        return next(self.zap._request(self.zap.base + 'core/view/numberOfMessages/', params).itervalues())

    @property
    def mode(self):
        """
        Gets the mode
        """
        return next(self.zap._request(self.zap.base + 'core/view/mode/').itervalues())

    @property
    def version(self):
        """
        Gets ZAP version
        """
        return next(self.zap._request(self.zap.base + 'core/view/version/').itervalues())

    @property
    def excluded_from_proxy(self):
        """
        Gets the regular expressions, applied to URLs, to exclude from the Proxy
        """
        return next(self.zap._request(self.zap.base + 'core/view/excludedFromProxy/').itervalues())

    @property
    def home_directory(self):
        return next(self.zap._request(self.zap.base + 'core/view/homeDirectory/').itervalues())

    @property
    def option_default_user_agent(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionDefaultUserAgent/').itervalues())

    @property
    def option_http_state(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionHttpState/').itervalues())

    @property
    def option_proxy_chain_name(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyChainName/').itervalues())

    @property
    def option_proxy_chain_password(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyChainPassword/').itervalues())

    @property
    def option_proxy_chain_port(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyChainPort/').itervalues())

    @property
    def option_proxy_chain_realm(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyChainRealm/').itervalues())

    @property
    def option_proxy_chain_skip_name(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyChainSkipName/').itervalues())

    @property
    def option_proxy_chain_user_name(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyChainUserName/').itervalues())

    @property
    def option_proxy_excluded_domains(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyExcludedDomains/').itervalues())

    @property
    def option_proxy_excluded_domains_enabled(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyExcludedDomainsEnabled/').itervalues())

    @property
    def option_timeout_in_secs(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionTimeoutInSecs/').itervalues())

    @property
    def option_http_state_enabled(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionHttpStateEnabled/').itervalues())

    @property
    def option_proxy_chain_prompt(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionProxyChainPrompt/').itervalues())

    @property
    def option_single_cookie_request_header(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionSingleCookieRequestHeader/').itervalues())

    @property
    def option_use_proxy_chain(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionUseProxyChain/').itervalues())

    @property
    def option_use_proxy_chain_auth(self):
        return next(self.zap._request(self.zap.base + 'core/view/optionUseProxyChainAuth/').itervalues())

    def access_url(self, url, followredirects=None, apikey=''):
        """
        Convenient and simple action to access a URL, optionally following redirections. Returns the request sent and response received and followed redirections, if any. Other actions are available which offer more control on what is sent, like, 'sendRequest' or 'sendHarRequest'.
        """
        params = {'url' : url, 'apikey' : apikey}
        if followredirects is not None:
            params['followRedirects'] = followredirects
        return next(self.zap._request(self.zap.base + 'core/action/accessUrl/', params).itervalues())

    def shutdown(self, apikey=''):
        """
        Shuts down ZAP
        """
        return next(self.zap._request(self.zap.base + 'core/action/shutdown/', {'apikey' : apikey}).itervalues())

    def new_session(self, name=None, overwrite=None, apikey=''):
        """
        Creates a new session, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
        """
        params = {'apikey' : apikey}
        if name is not None:
            params['name'] = name
        if overwrite is not None:
            params['overwrite'] = overwrite
        return next(self.zap._request(self.zap.base + 'core/action/newSession/', params).itervalues())

    def load_session(self, name, apikey=''):
        """
        Loads the session with the given name. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
        """
        return next(self.zap._request(self.zap.base + 'core/action/loadSession/', {'name' : name, 'apikey' : apikey}).itervalues())

    def save_session(self, name, overwrite=None, apikey=''):
        """
        Saves the session with the name supplied, optionally overwriting existing files. If a relative path is specified it will be resolved against the "session" directory in ZAP "home" dir.
        """
        params = {'name' : name, 'apikey' : apikey}
        if overwrite is not None:
            params['overwrite'] = overwrite
        return next(self.zap._request(self.zap.base + 'core/action/saveSession/', params).itervalues())

    def snapshot_session(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/snapshotSession/', {'apikey' : apikey}).itervalues())

    def clear_excluded_from_proxy(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/clearExcludedFromProxy/', {'apikey' : apikey}).itervalues())

    def exclude_from_proxy(self, regex, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/excludeFromProxy/', {'regex' : regex, 'apikey' : apikey}).itervalues())

    def set_home_directory(self, dir, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setHomeDirectory/', {'dir' : dir, 'apikey' : apikey}).itervalues())

    def set_mode(self, mode, apikey=''):
        """
        Sets the mode, which may be one of [safe, protect, standard, attack]
        """
        return next(self.zap._request(self.zap.base + 'core/action/setMode/', {'mode' : mode, 'apikey' : apikey}).itervalues())

    def generate_root_ca(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/generateRootCA/', {'apikey' : apikey}).itervalues())

    def send_request(self, request, followredirects=None, apikey=''):
        """
        Sends the HTTP request, optionally following redirections. Returns the request sent and response received and followed redirections, if any.
        """
        params = {'request' : request, 'apikey' : apikey}
        if followredirects is not None:
            params['followRedirects'] = followredirects
        return next(self.zap._request(self.zap.base + 'core/action/sendRequest/', params).itervalues())

    def delete_all_alerts(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/deleteAllAlerts/', {'apikey' : apikey}).itervalues())

    def run_garbage_collection(self, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/runGarbageCollection/', {'apikey' : apikey}).itervalues())

    def delete_site_node(self, url, method=None, postdata=None, apikey=''):
        """
        Deletes the site node found in the Sites Tree on the basis of the URL, HTTP method, and post data (if applicable and specified). 
        """
        params = {'url' : url, 'apikey' : apikey}
        if method is not None:
            params['method'] = method
        if postdata is not None:
            params['postData'] = postdata
        return next(self.zap._request(self.zap.base + 'core/action/deleteSiteNode/', params).itervalues())

    def set_option_default_user_agent(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionDefaultUserAgent/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_proxy_chain_name(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainName/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_proxy_chain_password(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPassword/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_proxy_chain_realm(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainRealm/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_proxy_chain_skip_name(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainSkipName/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_proxy_chain_user_name(self, string, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainUserName/', {'String' : string, 'apikey' : apikey}).itervalues())

    def set_option_http_state_enabled(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionHttpStateEnabled/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_proxy_chain_port(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPort/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_proxy_chain_prompt(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPrompt/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_single_cookie_request_header(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionSingleCookieRequestHeader/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_timeout_in_secs(self, integer, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionTimeoutInSecs/', {'Integer' : integer, 'apikey' : apikey}).itervalues())

    def set_option_use_proxy_chain(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionUseProxyChain/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def set_option_use_proxy_chain_auth(self, boolean, apikey=''):
        return next(self.zap._request(self.zap.base + 'core/action/setOptionUseProxyChainAuth/', {'Boolean' : boolean, 'apikey' : apikey}).itervalues())

    def proxy_pac(self, apikey=''):
        return (self.zap._request_other(self.zap.base_other + 'core/other/proxy.pac/', {'apikey' : apikey}))

    def rootcert(self, apikey=''):
        return (self.zap._request_other(self.zap.base_other + 'core/other/rootcert/', {'apikey' : apikey}))

    def setproxy(self, proxy, apikey=''):
        return (self.zap._request_other(self.zap.base_other + 'core/other/setproxy/', {'proxy' : proxy, 'apikey' : apikey}))

    def xmlreport(self, apikey=''):
        """
        Generates a report in XML format
        """
        return (self.zap._request_other(self.zap.base_other + 'core/other/xmlreport/', {'apikey' : apikey}))

    def htmlreport(self, apikey=''):
        """
        Generates a report in HTML format
        """
        return (self.zap._request_other(self.zap.base_other + 'core/other/htmlreport/', {'apikey' : apikey}))

    def message_har(self, id, apikey=''):
        """
        Gets the message with the given ID in HAR format
        """
        return (self.zap._request_other(self.zap.base_other + 'core/other/messageHar/', {'id' : id, 'apikey' : apikey}))

    def messages_har(self, baseurl=None, start=None, count=None, apikey=''):
        """
        Gets the HTTP messages sent through/by ZAP, in HAR format, optionally filtered by URL and paginated with 'start' position and 'count' of messages
        """
        params = {'apikey' : apikey}
        if baseurl is not None:
            params['baseurl'] = baseurl
        if start is not None:
            params['start'] = start
        if count is not None:
            params['count'] = count
        return (self.zap._request_other(self.zap.base_other + 'core/other/messagesHar/', params))

    def send_har_request(self, request, followredirects=None, apikey=''):
        """
        Sends the first HAR request entry, optionally following redirections. Returns, in HAR format, the request sent and response received and followed redirections, if any.
        """
        params = {'request' : request, 'apikey' : apikey}
        if followredirects is not None:
            params['followRedirects'] = followredirects
        return (self.zap._request_other(self.zap.base_other + 'core/other/sendHarRequest/', params))


