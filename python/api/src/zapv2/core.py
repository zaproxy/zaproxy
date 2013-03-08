# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2013 ZAP development team
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

    def alerts(self, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'core/view/alerts/', {'baseurl' : baseurl, 'start' : start, 'count' : count})

    @property
    def hosts(self):
        return self.zap._request(self.zap.base + 'core/view/hosts/').get('hosts')

    @property
    def sites(self):
        return self.zap._request(self.zap.base + 'core/view/sites/').get('sites')

    @property
    def urls(self):
        return self.zap._request(self.zap.base + 'core/view/urls/').get('urls')

    def messages(self, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'core/view/messages/', {'baseurl' : baseurl, 'start' : start, 'count' : count})

    @property
    def version(self):
        return self.zap._request(self.zap.base + 'core/view/version/').get('version')

    @property
    def excluded_from_proxy(self):
        return self.zap._request(self.zap.base + 'core/view/excludedFromProxy/').get('excluded_from_proxy')

    @property
    def home_directory(self):
        return self.zap._request(self.zap.base + 'core/view/homeDirectory/').get('home_directory')

    @property
    def option_http_state_enabled(self):
        return self.zap._request(self.zap.base + 'core/view/optionHttpStateEnabled/').get('option_http_state_enabled')

    @property
    def option_proxy_chain_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainName/').get('option_proxy_chain_name')

    @property
    def option_proxy_chain_port(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPort/').get('option_proxy_chain_port')

    @property
    def option_proxy_chain_skip_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainSkipName/').get('option_proxy_chain_skip_name')

    @property
    def option_proxy_chain_realm(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainRealm/').get('option_proxy_chain_realm')

    @property
    def option_proxy_chain_user_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainUserName/').get('option_proxy_chain_user_name')

    @property
    def option_proxy_chain_password(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPassword/').get('option_proxy_chain_password')

    @property
    def option_proxy_chain_prompt(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPrompt/').get('option_proxy_chain_prompt')

    @property
    def option_use_proxy_chain(self):
        return self.zap._request(self.zap.base + 'core/view/optionUseProxyChain/').get('option_use_proxy_chain')

    @property
    def option_list_auth(self):
        return self.zap._request(self.zap.base + 'core/view/optionListAuth/').get('option_list_auth')

    @property
    def option_list_auth_enabled(self):
        return self.zap._request(self.zap.base + 'core/view/optionListAuthEnabled/').get('option_list_auth_enabled')

    @property
    def option_http_state(self):
        return self.zap._request(self.zap.base + 'core/view/optionHttpState/').get('option_http_state')

    @property
    def option_timeout_in_secs(self):
        return self.zap._request(self.zap.base + 'core/view/optionTimeoutInSecs/').get('option_timeout_in_secs')

    @property
    def option_confirm_remove_auth(self):
        return self.zap._request(self.zap.base + 'core/view/optionConfirmRemoveAuth/').get('option_confirm_remove_auth')

    @property
    def option_ports_for_ssl_tunneling(self):
        return self.zap._request(self.zap.base + 'core/view/optionPortsForSslTunneling/').get('option_ports_for_ssl_tunneling')

    @property
    def option_single_cookie_request_header(self):
        return self.zap._request(self.zap.base + 'core/view/optionSingleCookieRequestHeader/').get('option_single_cookie_request_header')

    @property
    def shutdown(self):
        """
        Shuts down ZAP
        """
        return self.zap._request(self.zap.base + 'core/action/shutdown/').get('shutdown')

    def new_session(self, name=''):
        return self.zap._request(self.zap.base + 'core/action/newSession/', {'name' : name})

    def load_session(self, name):
        return self.zap._request(self.zap.base + 'core/action/loadSession/', {'name' : name})

    def save_session(self, name):
        return self.zap._request(self.zap.base + 'core/action/saveSession/', {'name' : name})

    @property
    def clear_excluded_from_proxy(self):
        return self.zap._request(self.zap.base + 'core/action/clearExcludedFromProxy/').get('clear_excluded_from_proxy')

    def exclude_from_proxy(self, regex):
        return self.zap._request(self.zap.base + 'core/action/excludeFromProxy/', {'regex' : regex})

    def set_home_directory(self, dir):
        return self.zap._request(self.zap.base + 'core/action/setHomeDirectory/', {'dir' : dir})

    @property
    def generate_root_ca(self):
        return self.zap._request(self.zap.base + 'core/action/generateRootCA/').get('generate_root_ca')

    def set_option_proxy_chain_name(self, string):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainName/', {'String' : string})

    def set_option_proxy_chain_skip_name(self, string):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainSkipName/', {'String' : string})

    def set_option_proxy_chain_realm(self, string):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainRealm/', {'String' : string})

    def set_option_proxy_chain_user_name(self, string):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainUserName/', {'String' : string})

    def set_option_proxy_chain_password(self, string):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPassword/', {'String' : string})

    def set_option_ports_for_ssl_tunneling(self, string):
        return self.zap._request(self.zap.base + 'core/action/setOptionPortsForSslTunneling/', {'String' : string})

    def set_option_http_state_enabled(self, boolean):
        return self.zap._request(self.zap.base + 'core/action/setOptionHttpStateEnabled/', {'Boolean' : boolean})

    def set_option_proxy_chain_port(self, integer):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPort/', {'Integer' : integer})

    def set_option_proxy_chain_prompt(self, boolean):
        return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPrompt/', {'Boolean' : boolean})

    def set_option_timeout_in_secs(self, integer):
        return self.zap._request(self.zap.base + 'core/action/setOptionTimeoutInSecs/', {'Integer' : integer})

    def set_option_confirm_remove_auth(self, boolean):
        return self.zap._request(self.zap.base + 'core/action/setOptionConfirmRemoveAuth/', {'Boolean' : boolean})

    def set_option_single_cookie_request_header(self, boolean):
        return self.zap._request(self.zap.base + 'core/action/setOptionSingleCookieRequestHeader/', {'Boolean' : boolean})


