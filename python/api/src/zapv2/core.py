# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2012 ZAP development team
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

	def hosts(self):
		return self.zap._request(self.zap.base + 'core/view/hosts/')

	def sites(self):
		return self.zap._request(self.zap.base + 'core/view/sites/')

	def urls(self):
		return self.zap._request(self.zap.base + 'core/view/urls/')

	def messages(self, baseurl='', start='', count=''):
		return self.zap._request(self.zap.base + 'core/view/messages/', {'baseurl' : baseurl, 'start' : start, 'count' : count})

	def version(self):
		return self.zap._request(self.zap.base + 'core/view/version/')

	def excludedFromProxy(self):
		return self.zap._request(self.zap.base + 'core/view/excludedFromProxy/')

	def homeDirectory(self):
		return self.zap._request(self.zap.base + 'core/view/homeDirectory/')

	def optionHttpStateEnabled(self):
		return self.zap._request(self.zap.base + 'core/view/optionHttpStateEnabled/')

	def optionProxyChainName(self):
		return self.zap._request(self.zap.base + 'core/view/optionProxyChainName/')

	def optionProxyChainPort(self):
		return self.zap._request(self.zap.base + 'core/view/optionProxyChainPort/')

	def optionProxyChainSkipName(self):
		return self.zap._request(self.zap.base + 'core/view/optionProxyChainSkipName/')

	def optionProxyChainRealm(self):
		return self.zap._request(self.zap.base + 'core/view/optionProxyChainRealm/')

	def optionProxyChainUserName(self):
		return self.zap._request(self.zap.base + 'core/view/optionProxyChainUserName/')

	def optionProxyChainPassword(self):
		return self.zap._request(self.zap.base + 'core/view/optionProxyChainPassword/')

	def optionProxyChainPrompt(self):
		return self.zap._request(self.zap.base + 'core/view/optionProxyChainPrompt/')

	def optionUseProxyChain(self):
		return self.zap._request(self.zap.base + 'core/view/optionUseProxyChain/')

	def optionListAuth(self):
		return self.zap._request(self.zap.base + 'core/view/optionListAuth/')

	def optionListAuthEnabled(self):
		return self.zap._request(self.zap.base + 'core/view/optionListAuthEnabled/')

	def optionHttpState(self):
		return self.zap._request(self.zap.base + 'core/view/optionHttpState/')

	def optionTimeoutInSecs(self):
		return self.zap._request(self.zap.base + 'core/view/optionTimeoutInSecs/')

	def optionConfirmRemoveAuth(self):
		return self.zap._request(self.zap.base + 'core/view/optionConfirmRemoveAuth/')

	def shutdown(self):
		"""
		Shuts down ZAP
		"""
		return self.zap._request(self.zap.base + 'core/action/shutdown/')

	def newSession(self, name=''):
		return self.zap._request(self.zap.base + 'core/action/newSession/', {'name' : name})

	def loadSession(self, name):
		return self.zap._request(self.zap.base + 'core/action/loadSession/', {'name' : name})

	def saveSession(self, name):
		return self.zap._request(self.zap.base + 'core/action/saveSession/', {'name' : name})

	def clearExcludedFromProxy(self):
		return self.zap._request(self.zap.base + 'core/action/clearExcludedFromProxy/')

	def excludeFromProxy(self, regex):
		return self.zap._request(self.zap.base + 'core/action/excludeFromProxy/', {'regex' : regex})

	def setHomeDirectory(self, dir):
		return self.zap._request(self.zap.base + 'core/action/setHomeDirectory/', {'dir' : dir})

	def generateRootCA(self):
		return self.zap._request(self.zap.base + 'core/action/generateRootCA/')

	def setOptionProxyChainName(self, string):
		return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainName/', {'String' : string})

	def setOptionProxyChainSkipName(self, string):
		return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainSkipName/', {'String' : string})

	def setOptionProxyChainRealm(self, string):
		return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainRealm/', {'String' : string})

	def setOptionProxyChainUserName(self, string):
		return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainUserName/', {'String' : string})

	def setOptionProxyChainPassword(self, string):
		return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPassword/', {'String' : string})

	def setOptionHttpStateEnabled(self, boolean):
		return self.zap._request(self.zap.base + 'core/action/setOptionHttpStateEnabled/', {'Boolean' : boolean})

	def setOptionProxyChainPort(self, integer):
		return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPort/', {'Integer' : integer})

	def setOptionProxyChainPrompt(self, boolean):
		return self.zap._request(self.zap.base + 'core/action/setOptionProxyChainPrompt/', {'Boolean' : boolean})

	def setOptionTimeoutInSecs(self, integer):
		return self.zap._request(self.zap.base + 'core/action/setOptionTimeoutInSecs/', {'Integer' : integer})

	def setOptionConfirmRemoveAuth(self, boolean):
		return self.zap._request(self.zap.base + 'core/action/setOptionConfirmRemoveAuth/', {'Boolean' : boolean})


