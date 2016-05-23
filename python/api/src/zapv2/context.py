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

class context(object):

    def __init__(self, zap):
        self.zap = zap

    @property
    def context_list(self):
        """
        List context names of current session
        """
        return next(self.zap._request(self.zap.base + 'context/view/contextList/').itervalues())

    def exclude_regexs(self, contextname):
        """
        List excluded regexs for context
        """
        return next(self.zap._request(self.zap.base + 'context/view/excludeRegexs/', {'contextName' : contextname}).itervalues())

    def include_regexs(self, contextname):
        """
        List included regexs for context
        """
        return next(self.zap._request(self.zap.base + 'context/view/includeRegexs/', {'contextName' : contextname}).itervalues())

    def context(self, contextname):
        """
        List the information about the named context
        """
        return next(self.zap._request(self.zap.base + 'context/view/context/', {'contextName' : contextname}).itervalues())

    @property
    def technology_list(self):
        """
        Lists the names of all built in technologies
        """
        return next(self.zap._request(self.zap.base + 'context/view/technologyList/').itervalues())

    def included_technology_list(self, contextname):
        """
        Lists the names of all technologies included in a context
        """
        return next(self.zap._request(self.zap.base + 'context/view/includedTechnologyList/', {'contextName' : contextname}).itervalues())

    def excluded_technology_list(self, contextname):
        """
        Lists the names of all technologies excluded from a context
        """
        return next(self.zap._request(self.zap.base + 'context/view/excludedTechnologyList/', {'contextName' : contextname}).itervalues())

    def exclude_from_context(self, contextname, regex, apikey=''):
        """
        Add exclude regex to context
        """
        return next(self.zap._request(self.zap.base + 'context/action/excludeFromContext/', {'contextName' : contextname, 'regex' : regex, 'apikey' : apikey}).itervalues())

    def include_in_context(self, contextname, regex, apikey=''):
        """
        Add include regex to context
        """
        return next(self.zap._request(self.zap.base + 'context/action/includeInContext/', {'contextName' : contextname, 'regex' : regex, 'apikey' : apikey}).itervalues())

    def new_context(self, contextname, apikey=''):
        """
        Creates a new context with the given name in the current session
        """
        return next(self.zap._request(self.zap.base + 'context/action/newContext/', {'contextName' : contextname, 'apikey' : apikey}).itervalues())

    def remove_context(self, contextname, apikey=''):
        """
        Removes a context in the current session
        """
        return next(self.zap._request(self.zap.base + 'context/action/removeContext/', {'contextName' : contextname, 'apikey' : apikey}).itervalues())

    def export_context(self, contextname, contextfile, apikey=''):
        """
        Exports the context with the given name to a file. If a relative file path is specified it will be resolved against the "contexts" directory in ZAP "home" dir.
        """
        return next(self.zap._request(self.zap.base + 'context/action/exportContext/', {'contextName' : contextname, 'contextFile' : contextfile, 'apikey' : apikey}).itervalues())

    def import_context(self, contextfile, apikey=''):
        """
        Imports a context from a file. If a relative file path is specified it will be resolved against the "contexts" directory in ZAP "home" dir.
        """
        return next(self.zap._request(self.zap.base + 'context/action/importContext/', {'contextFile' : contextfile, 'apikey' : apikey}).itervalues())

    def include_context_technologies(self, contextname, technologynames, apikey=''):
        """
        Includes technologies with the given names, separated by a comma, to a context
        """
        return next(self.zap._request(self.zap.base + 'context/action/includeContextTechnologies/', {'contextName' : contextname, 'technologyNames' : technologynames, 'apikey' : apikey}).itervalues())

    def include_all_context_technologies(self, contextname, apikey=''):
        """
        Includes all built in technologies in to a context
        """
        return next(self.zap._request(self.zap.base + 'context/action/includeAllContextTechnologies/', {'contextName' : contextname, 'apikey' : apikey}).itervalues())

    def exclude_context_technologies(self, contextname, technologynames, apikey=''):
        """
        Excludes technologies with the given names, separated by a comma, from a context
        """
        return next(self.zap._request(self.zap.base + 'context/action/excludeContextTechnologies/', {'contextName' : contextname, 'technologyNames' : technologynames, 'apikey' : apikey}).itervalues())

    def exclude_all_context_technologies(self, contextname, apikey=''):
        """
        Excludes all built in technologies from a context
        """
        return next(self.zap._request(self.zap.base + 'context/action/excludeAllContextTechnologies/', {'contextName' : contextname, 'apikey' : apikey}).itervalues())

    def set_context_in_scope(self, contextname, booleaninscope, apikey=''):
        """
        Sets a context to in scope (contexts are in scope by default)
        """
        return next(self.zap._request(self.zap.base + 'context/action/setContextInScope/', {'contextName' : contextname, 'booleanInScope' : booleaninscope, 'apikey' : apikey}).itervalues())


