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
        return self.zap._request(self.zap.base + 'core/view/hosts/')

    @property
    def sites(self):
        return self.zap._request(self.zap.base + 'core/view/sites/')

    @property
    def urls(self):
        return self.zap._request(self.zap.base + 'core/view/urls/')

    def messages(self, baseurl='', start='', count=''):
        return self.zap._request(self.zap.base + 'core/view/messages/', {'baseurl' : baseurl, 'start' : start, 'count' : count})

    @property
    def version(self):
        return self.zap._request(self.zap.base + 'core/view/version/')

    @property
    def excluded_from_proxy(self):
        return self.zap._request(self.zap.base + 'core/view/excludedFromProxy/')

    @property
    def home_directory(self):
        return self.zap._request(self.zap.base + 'core/view/homeDirectory/')

    @property
    def option_http_state_enabled(self):
        return self.zap._request(self.zap.base + 'core/view/optionHttpStateEnabled/')

    @property
    def option_proxy_chain_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainName/')

    @property
    def option_proxy_chain_port(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPort/')

    @property
    def option_proxy_chain_skip_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainSkipName/')

    @property
    def option_proxy_chain_realm(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainRealm/')

    @property
    def option_proxy_chain_user_name(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainUserName/')

    @property
    def option_proxy_chain_password(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPassword/')

    @property
    def option_proxy_chain_prompt(self):
        return self.zap._request(self.zap.base + 'core/view/optionProxyChainPrompt/')

    @property
    def option_use_proxy_chain(self):
        return self.zap._request(self.zap.base + 'core/view/optionUseProxyChain/')

    @property
    def option_list_auth(self):
        return self.zap._request(self.zap.base + 'core/view/optionListAuth/')

    @property
    def option_list_auth_enabled(self):
        return self.zap._request(self.zap.base + 'core/view/optionListAuthEnabled/')

    @property
    def option_http_state(self):
        return self.zap._request(self.zap.base + 'core/view/optionHttpState/')

    @property
    def option_timeout_in_secs(self):
        return self.zap._request(self.zap.base + 'core/view/optionTimeoutInSecs/')

    @property
    def option_confirm_remove_auth(self):
        return self.zap._request(self.zap.base + 'core/view/optionConfirmRemoveAuth/')

    @property
    def option_ports_for_ssl_tunneling(self):
        return self.zap._request(self.zap.base + 'core/view/optionPortsForSslTunneling/')

    @property
    def shutdown(self):
        """
        Shuts down ZAP
        """
        return self.zap._request(self.zap.base + 'core/action/shutdown/')

    def new_session(self, name=''):
        return self.zap._request(self.zap.base + 'core/action/newSession/', {'name' : name})

    def load_session(self, name):
        return self.zap._request(self.zap.base + 'core/action/loadSession/', {'name' : name})

    def save_session(self, name):
        return self.zap._request(self.zap.base + 'core/action/saveSession/', {'name' : name})

    @property
    def clear_excluded_from_proxy(self):
        return self.zap._request(self.zap.base + 'core/action/clearExcludedFromProxy/')

    def exclude_from_proxy(self, regex):
        return self.zap._request(self.zap.base + 'core/action/excludeFromProxy/', {'regex' : regex})

    def set_home_directory(self, dir):
        return self.zap._request(self.zap.base + 'core/action/setHomeDirectory/', {'dir' : dir})

    @property
    def generate_root_ca(self):
        return self.zap._request(self.zap.base + 'core/action/generateRootCA/')

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


