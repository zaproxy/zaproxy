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

class spider(object):

	def __init__(self, zap):
		self.zap = zap

	def status(self):
		return self.zap._request(self.zap.base + 'spider/view/status/')

	def results(self):
		return self.zap._request(self.zap.base + 'spider/view/results/')

	def excludedFromScan(self):
		return self.zap._request(self.zap.base + 'spider/view/excludedFromScan/')

	def optionMaxDepth(self):
		return self.zap._request(self.zap.base + 'spider/view/optionMaxDepth/')

	def optionScopeText(self):
		return self.zap._request(self.zap.base + 'spider/view/optionScopeText/')

	def optionScope(self):
		return self.zap._request(self.zap.base + 'spider/view/optionScope/')

	def optionThreadCount(self):
		return self.zap._request(self.zap.base + 'spider/view/optionThreadCount/')

	def optionPostForm(self):
		return self.zap._request(self.zap.base + 'spider/view/optionPostForm/')

	def optionProcessForm(self):
		return self.zap._request(self.zap.base + 'spider/view/optionProcessForm/')

	def optionSkipURLString(self):
		return self.zap._request(self.zap.base + 'spider/view/optionSkipURLString/')

	def optionRequestWaitTime(self):
		return self.zap._request(self.zap.base + 'spider/view/optionRequestWaitTime/')

	def optionUserAgent(self):
		return self.zap._request(self.zap.base + 'spider/view/optionUserAgent/')

	def optionSendCookies(self):
		return self.zap._request(self.zap.base + 'spider/view/optionSendCookies/')

	def optionParseComments(self):
		return self.zap._request(self.zap.base + 'spider/view/optionParseComments/')

	def optionParseRobotsTxt(self):
		return self.zap._request(self.zap.base + 'spider/view/optionParseRobotsTxt/')

	def optionHandleParameters(self):
		return self.zap._request(self.zap.base + 'spider/view/optionHandleParameters/')

	def scan(self, url):
		return self.zap._request(self.zap.base + 'spider/action/scan/', {'url' : url})

	def stop(self):
		return self.zap._request(self.zap.base + 'spider/action/stop/')

	def clearExcludedFromScan(self):
		return self.zap._request(self.zap.base + 'spider/action/clearExcludedFromScan/')

	def excludeFromScan(self, regex):
		return self.zap._request(self.zap.base + 'spider/action/excludeFromScan/', {'regex' : regex})

	def setOptionScopeString(self, string):
		return self.zap._request(self.zap.base + 'spider/action/setOptionScopeString/', {'String' : string})

	def setOptionSkipURLString(self, string):
		return self.zap._request(self.zap.base + 'spider/action/setOptionSkipURLString/', {'String' : string})

	def setOptionUserAgent(self, string):
		return self.zap._request(self.zap.base + 'spider/action/setOptionUserAgent/', {'String' : string})

	def setOptionMaxDepth(self, integer):
		return self.zap._request(self.zap.base + 'spider/action/setOptionMaxDepth/', {'Integer' : integer})

	def setOptionThreadCount(self, integer):
		return self.zap._request(self.zap.base + 'spider/action/setOptionThreadCount/', {'Integer' : integer})

	def setOptionPostForm(self, boolean):
		return self.zap._request(self.zap.base + 'spider/action/setOptionPostForm/', {'Boolean' : boolean})

	def setOptionProcessForm(self, boolean):
		return self.zap._request(self.zap.base + 'spider/action/setOptionProcessForm/', {'Boolean' : boolean})

	def setOptionRequestWaitTime(self, integer):
		return self.zap._request(self.zap.base + 'spider/action/setOptionRequestWaitTime/', {'Integer' : integer})

	def setOptionSendCookies(self, boolean):
		return self.zap._request(self.zap.base + 'spider/action/setOptionSendCookies/', {'Boolean' : boolean})

	def setOptionParseComments(self, boolean):
		return self.zap._request(self.zap.base + 'spider/action/setOptionParseComments/', {'Boolean' : boolean})

	def setOptionParseRobotsTxt(self, boolean):
		return self.zap._request(self.zap.base + 'spider/action/setOptionParseRobotsTxt/', {'Boolean' : boolean})


