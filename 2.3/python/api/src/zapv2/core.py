# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright the ZAP development team
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
        return self.zap._request(self.zap.base + 'core/view/alert/', {'id' : id})

    def alerts(self, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'core/view/alerts/', {'baseurl' : baseurl, 'start' : start, 'count' : count})

    def number_of_alerts(self, baseurl=''):
        return self.zap._request(self.zap.base + 'core/view/numberOfAlerts/', {'baseurl' : baseurl})

    @property
    def hosts(self):
        return self.zap._request(self.zap.base + 'core/view/hosts/').get('hosts')

    @property
    def sites(self):
        return self.zap._request(self.zap.base + 'core/view/sites/').get('sites')

    @property
    def urls(self):
        return self.zap._request(self.zap.base + 'core/view/urls/').get('urls')

    def message(self, id):
        return self.zap._request(self.zap.base + 'core/view/message/', {'id' : id})

    def messages(self, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'core/view/messages/', {'baseurl' : baseurl, 'start' : start, 'count' : count})

    def number_of_messages(self, baseurl=''):
        return self.zap._request(self.zap.base + 'core/view/numberOfMessages/', {'baseurl' : baseurl})

    @property
    def version(self):
        return self.zap._request(self.zap.base + 'core/view/version/').get('version')

    @property
    def excluded_from_proxy(self):
        return self.zap._request(self.zap.base + 'core/view/excludedFromProxy/').get('excludedFromProxy')

    @property
    def home_directory(self):
        return self.zap._request(self.zap.base + 'core/view/homeDirectory/').get('homeDirectory')

    @property
    def option_http_state_enabled(self):
        return self.zap._request(self.zap.base + 'core/view/optionHttpStateEnabled/').get('HttpStateEnabled')

    @property
    def option_proxy_chain_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainName/').get('ProxyChainName')

    @property
    def option_proxy_chain_port(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPort/').get('ProxyChainPort')

    @property
    def option_proxy_chain_skip_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainSkipName/').get('ProxyChainSkipName')

    @property
    def option_proxy_chain_realm(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainRealm/').get('ProxyChainRealm')

    @property
    def option_proxy_chain_user_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainUserName/').get('ProxyChainUserName')

    @property
    def option_proxy_chain_password(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPassword/').get('ProxyChainPassword')

    @property
    def option_proxy_chain_prompt(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPrompt/').get('ProxyChainPrompt')

    @property
    def option_use_proxy_chain(self):
        return self.zap._request(self.zap.base + 'core/view/optionUseProxyChain/').get('UseProxyChain')

    @property
    def option_list_auth(self):
        return self.zap._request(self.zap.base + 'core/view/optionListAuth/').get('ListAuth')

    @property
    def option_list_auth_enabled(self):
        return self.zap._request(self.zap.base + 'core/view/optionListAuthEnabled/').get('ListAuthEnabled')

    @property
    def option_http_state(self):
        return self.zap._request(self.zap.base + 'core/view/optionHttpState/').get('HttpState')

    @property
    def option_timeout_in_secs(self):
        return self.zap._request(self.zap.base + 'core/view/optionTimeoutInSecs/').get('TimeoutInSecs')

    @property
    def option_confirm_remove_auth(self):
        return self.zap._request(self.zap.base + 'core/view/optionConfirmRemoveAuth/').get('ConfirmRemoveAuth')

    @property
    def option_single_cookie_request_header(self):
        return self.zap._request(self.zap.base + 'core/view/optionSingleCookieRequestHeader/').get('SingleCookieRequestHeader')

    @property
    def shutdown(self, apikey=''):
        """
        Shuts down ZAP
        """
        return self.zap._request(self.zap.base + 'core/action/shutdown/').get('shutdown')

    def new_session(self, name='', overwrite='', apikey=''):
        return self.zap._request(self.zap.base + 'core/action/newSession/', {'name' : name, 'overwrite' : overwrite})

    def load_session(self, name, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/loadSession/', {'name' : name})

    def save_session(self, name, overwrite='', apikey=''):
        return self.zap._request(self.zap.base + 'core/action/saveSession/', {'name' : name, 'overwrite' : overwrite})

    @property
    def snapshot_session(self, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/snapshotSession/').get('snapshotSession')

    @property
    def clear_excluded_from_proxy(self, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/clearExcludedFromProxy/').get('clearExcludedFromProxy')

    def exclude_from_proxy(self, regex, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/excludeFromProxy/', {'regex' : regex})

    def set_home_directory(self, dir, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setHomeDirectory/', {'dir' : dir})

    @property
    def generate_root_ca(self, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/generateRootCA/').get('generateRootCA')

    def send_request(self, request, followredirects='', apikey=''):
        return self.zap._request(self.zap.base + 'core/action/sendRequest/', {'request' : request, 'followRedirects' : followredirects})

    def set_option_proxy_chain_name(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainName/', {'String' : string})

    def set_option_proxy_chain_skip_name(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainSkipName/', {'String' : string})

    def set_option_proxy_chain_realm(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainRealm/', {'String' : string})

    def set_option_proxy_chain_user_name(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainUserName/', {'String' : string})

    def set_option_proxy_chain_password(self, string, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPassword/', {'String' : string})

    def set_option_http_state_enabled(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionHttpStateEnabled/', {'Boolean' : boolean})

    def set_option_proxy_chain_port(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPort/', {'Integer' : integer})

    def set_option_proxy_chain_prompt(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPrompt/', {'Boolean' : boolean})

    def set_option_timeout_in_secs(self, integer, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionTimeoutInSecs/', {'Integer' : integer})

    def set_option_confirm_remove_auth(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionConfirmRemoveAuth/', {'Boolean' : boolean})

    def set_option_single_cookie_request_header(self, boolean, apikey=''):
        return self.zap._request(self.zap.base + 'core/action/setOptionSingleCookieRequestHeader/', {'Boolean' : boolean})

    @property
    def proxy_pac(self, apikey=''):
        return self.zap._request(self.zap.base + 'core/other/proxy.pac/').get('proxy.pac')

    @property
    def rootcert(self, apikey=''):
        return self.zap._request(self.zap.base + 'core/other/rootcert/').get('rootcert')

    def setproxy(self, proxy, apikey=''):
        return self.zap._request(self.zap.base + 'core/other/setproxy/', {'proxy' : proxy})

    @property
    def xmlreport(self, apikey=''):
        return self.zap._request(self.zap.base + 'core/other/xmlreport/').get('xmlreport')

    def messages_har(self, baseurl='', start='', count='', apikey=''):
        return self.zap._request(self.zap.base + 'core/other/messagesHar/', {'baseurl' : baseurl, 'start' : start, 'count' : count})

    def send_har_request(self, request, followredirects='', apikey=''):
        return self.zap._request(self.zap.base + 'core/other/sendHarRequest/', {'request' : request, 'followRedirects' : followredirects})


