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

class ascan(object):

	def __init__(self, zap):
		self.zap = zap

	def status(self):
		return self.zap._request(self.zap.base + 'ascan/view/status/')

	def excludedFromScan(self):
		return self.zap._request(self.zap.base + 'ascan/view/excludedFromScan/')

	def optionThreadPerHost(self):
		return self.zap._request(self.zap.base + 'ascan/view/optionThreadPerHost/')

	def optionHostPerScan(self):
		return self.zap._request(self.zap.base + 'ascan/view/optionHostPerScan/')

	def optionDelayInMs(self):
		return self.zap._request(self.zap.base + 'ascan/view/optionDelayInMs/')

	def optionHandleAntiCSRFTokens(self):
		return self.zap._request(self.zap.base + 'ascan/view/optionHandleAntiCSRFTokens/')

	def optionAlertThreshold(self):
		return self.zap._request(self.zap.base + 'ascan/view/optionAlertThreshold/')

	def optionAttackStrength(self):
		return self.zap._request(self.zap.base + 'ascan/view/optionAttackStrength/')

	def scan(self, url, recurse=''):
		return self.zap._request(self.zap.base + 'ascan/action/scan/', {'url' : url, 'recurse' : recurse})

	def clearExcludedFromScan(self):
		return self.zap._request(self.zap.base + 'ascan/action/clearExcludedFromScan/')

	def excludeFromScan(self, regex):
		return self.zap._request(self.zap.base + 'ascan/action/excludeFromScan/', {'regex' : regex})

	def setOptionAlertThreshold(self, string):
		return self.zap._request(self.zap.base + 'ascan/action/setOptionAlertThreshold/', {'String' : string})

	def setOptionAttackStrength(self, string):
		return self.zap._request(self.zap.base + 'ascan/action/setOptionAttackStrength/', {'String' : string})

	def setOptionThreadPerHost(self, integer):
		return self.zap._request(self.zap.base + 'ascan/action/setOptionThreadPerHost/', {'Integer' : integer})

	def setOptionHostPerScan(self, integer):
		return self.zap._request(self.zap.base + 'ascan/action/setOptionHostPerScan/', {'Integer' : integer})

	def setOptionDelayInMs(self, integer):
		return self.zap._request(self.zap.base + 'ascan/action/setOptionDelayInMs/', {'Integer' : integer})

	def setOptionHandleAntiCSRFTokens(self, boolean):
		return self.zap._request(self.zap.base + 'ascan/action/setOptionHandleAntiCSRFTokens/', {'Boolean' : boolean})


