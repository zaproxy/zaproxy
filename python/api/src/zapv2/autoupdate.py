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

class autoupdate(object):

	def __init__(self, zap):
		self.zap = zap

	def latestVersionNumber(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/latestVersionNumber/')

	def isLatestVersion(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/isLatestVersion/')

	def optionCheckOnStart(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckOnStart/')

	def optionCheckOnStartUnset(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckOnStartUnset/')

	def optionCheckOnStart(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckOnStart/')

	def optionDownloadNewRelease(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionDownloadNewRelease/')

	def optionCheckAddonUpdates(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionCheckAddonUpdates/')

	def optionInstallAddonUpdates(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionInstallAddonUpdates/')

	def optionInstallScannerRules(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionInstallScannerRules/')

	def optionReportReleaseAddons(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionReportReleaseAddons/')

	def optionReportBetaAddons(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionReportBetaAddons/')

	def optionReportAlphaAddons(self):
		return self.zap._request(self.zap.base + 'autoupdate/view/optionReportAlphaAddons/')

	def downloadLatestRelease(self):
		return self.zap._request(self.zap.base + 'autoupdate/action/downloadLatestRelease/')

	def setOptionChckOnStart(self, integer):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionChckOnStart/', {'Integer' : integer})

	def setOptionDownloadNewRelease(self, boolean):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionDownloadNewRelease/', {'Boolean' : boolean})

	def setOptionCheckAddonUpdates(self, boolean):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionCheckAddonUpdates/', {'Boolean' : boolean})

	def setOptionInstallAddonUpdates(self, boolean):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionInstallAddonUpdates/', {'Boolean' : boolean})

	def setOptionInstallScannerRules(self, boolean):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionInstallScannerRules/', {'Boolean' : boolean})

	def setOptionReportReleaseAddons(self, boolean):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportReleaseAddons/', {'Boolean' : boolean})

	def setOptionReportBetaAddons(self, boolean):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportBetaAddons/', {'Boolean' : boolean})

	def setOptionReportAlphaAddons(self, boolean):
		return self.zap._request(self.zap.base + 'autoupdate/action/setOptionReportAlphaAddons/', {'Boolean' : boolean})


