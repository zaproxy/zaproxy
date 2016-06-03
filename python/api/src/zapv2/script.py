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

class script(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def list_engines(self):
        """
        Lists the script engines available
        """
        return next(self.zap._request(self.zap.base + 'script/view/listEngines/').itervalues())

    @property
    def list_scripts(self):
        """
        Lists the scripts available, with its engine, name, description, type and error state.
        """
        return next(self.zap._request(self.zap.base + 'script/view/listScripts/').itervalues())

    def enable(self, scriptname, apikey=''):
        """
        Enables the script with the given name
        """
        return next(self.zap._request(self.zap.base + 'script/action/enable/', {'scriptName' : scriptname, 'apikey' : apikey}).itervalues())

    def disable(self, scriptname, apikey=''):
        """
        Disables the script with the given name
        """
        return next(self.zap._request(self.zap.base + 'script/action/disable/', {'scriptName' : scriptname, 'apikey' : apikey}).itervalues())

    def load(self, scriptname, scripttype, scriptengine, filename, scriptdescription=None, apikey=''):
        """
        Loads a script into ZAP from the given local file, with the given name, type and engine, optionally with a description
        """
        params = {'scriptName' : scriptname, 'scriptType' : scripttype, 'scriptEngine' : scriptengine, 'fileName' : filename, 'apikey' : apikey}
        if scriptdescription is not None:
            params['scriptDescription'] = scriptdescription
        return next(self.zap._request(self.zap.base + 'script/action/load/', params).itervalues())

    def remove(self, scriptname, apikey=''):
        """
        Removes the script with the given name
        """
        return next(self.zap._request(self.zap.base + 'script/action/remove/', {'scriptName' : scriptname, 'apikey' : apikey}).itervalues())

    def run_stand_alone_script(self, scriptname, apikey=''):
        """
        Runs the stand alone script with the give name
        """
        return next(self.zap._request(self.zap.base + 'script/action/runStandAloneScript/', {'scriptName' : scriptname, 'apikey' : apikey}).itervalues())


