# Zed Attack Proxy (ZAP) and its related class files.
#
# ZAP is an HTTP/HTTPS proxy for assessing web application security.
#
# Copyright 2014 the ZAP development team
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

class script(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def list_engines(self):
        return self.zap._request(self.zap.base + 'script/view/listEngines/').get('listEngines')

    @property
    def list_scripts(self):
        return self.zap._request(self.zap.base + 'script/view/listScripts/').get('listScripts')

    def enable(self, scriptname, apikey=''):
        return self.zap._request(self.zap.base + 'script/action/enable/', {'scriptName' : scriptname, 'apikey' : apikey})

    def disable(self, scriptname, apikey=''):
        return self.zap._request(self.zap.base + 'script/action/disable/', {'scriptName' : scriptname, 'apikey' : apikey})

    def load(self, scriptname, scripttype, scriptengine, filename, scriptdescription='', apikey=''):
        return self.zap._request(self.zap.base + 'script/action/load/', {'scriptName' : scriptname, 'scriptType' : scripttype, 'scriptEngine' : scriptengine, 'fileName' : filename, 'scriptDescription' : scriptdescription, 'apikey' : apikey})

    def remove(self, scriptname, apikey=''):
        return self.zap._request(self.zap.base + 'script/action/remove/', {'scriptName' : scriptname, 'apikey' : apikey})

    def run_stand_alone_script(self, scriptname, apikey=''):
        return self.zap._request(self.zap.base + 'script/action/runStandAloneScript/', {'scriptName' : scriptname, 'apikey' : apikey})


