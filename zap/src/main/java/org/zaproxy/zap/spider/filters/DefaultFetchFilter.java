/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.spider.filters;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.zaproxy.zap.model.Context;

/**
 * The DefaultFetchFilter is an implementation of a FetchFilter that is default for spidering
 * process. Its filter rules are the following:
 *
 * <ul>
 *   <li>the resource protocol/scheme must be 'HTTP' or 'HTTPs'.
 *   <li>the resource must be found in the scope (domain) of the spidering process.
 *   <li>the resource must be not be excluded by user request - exclude list.
 * </ul>
 *
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class DefaultFetchFilter extends FetchFilter {

    /** The scope. */
    private Set<String> scopes = new LinkedHashSet<>();

    private List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope =
            Collections.emptyList();

    /** The exclude list. */
    private List<String> excludeList = null;

    private Context scanContext;

    @Override
    public FetchStatus checkFilter(URI uri) {

        getLogger().debug("Checking: {}", uri);
        // Protocol check
        String scheme = uri.getScheme();
        if (scheme == null
                || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            return FetchStatus.ILLEGAL_PROTOCOL;
        }

        try {

            // Context check
            if (this.scanContext != null) {
                if (!this.scanContext.isInContext(uri.toString())) {
                    return FetchStatus.OUT_OF_CONTEXT;
                }
            } else {
                // Scope check
                String host = uri.getHost();
                if (!isDomainInScope(host) && !isDomainAlwaysInScope(host)) {
                    return FetchStatus.OUT_OF_SCOPE;
                }
            }

            // Check if any of the exclusion regexes match.
            if (isExcluded(uri.toString())) {
                return FetchStatus.USER_RULES;
            }

        } catch (URIException e) {
            getLogger().warn("Error while fetching host for uri: {}", uri, e);
            return FetchStatus.OUT_OF_SCOPE;
        }

        return FetchStatus.VALID;
    }

    /**
     * Tells whether or not the given URI is excluded.
     *
     * @param uri the URI to check
     * @return {@code true} if the URI is excluded, {@code false} otherwise.
     */
    private boolean isExcluded(String uri) {
        if (excludeList == null || excludeList.isEmpty()) {
            return false;
        }

        for (String ex : excludeList) {
            if (uri.matches(ex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether or not the given domain is one of the domains in scope.
     *
     * @param domain the domain to check
     * @return {@code true} if it's a domain in scope, {@code false} otherwise.
     * @see #scopes
     * @see #isDomainAlwaysInScope(String)
     */
    private boolean isDomainInScope(String domain) {
        for (String scope : scopes) {
            if (domain.matches(scope)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether or not the given domain is one of the domains always in scope.
     *
     * @param domain the domain to check
     * @return {@code true} if it's a domain always in scope, {@code false} otherwise.
     * @see #domainsAlwaysInScope
     * @see #isDomainInScope(String)
     */
    private boolean isDomainAlwaysInScope(String domain) {
        for (org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher domainInScope :
                domainsAlwaysInScope) {
            if (domainInScope.matches(domain)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new domain to the scope list of the spider process.
     *
     * @param scope the scope
     */
    public void addScopeRegex(String scope) {
        this.scopes.add(scope);
    }

    /**
     * Sets the domains that will be considered as always in scope.
     *
     * @param domainsAlwaysInScope the list containing all domains that are always in scope.
     * @since 2.3.0
     */
    public void setDomainsAlwaysInScope(
            List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope) {
        if (domainsAlwaysInScope == null || domainsAlwaysInScope.isEmpty()) {
            this.domainsAlwaysInScope = Collections.emptyList();
        } else {
            this.domainsAlwaysInScope = domainsAlwaysInScope;
        }
    }

    /**
     * Sets the regexes which are used for checking if an uri should be skipped.
     *
     * @param excl the new exclude regexes
     */
    public void setExcludeRegexes(List<String> excl) {
        excludeList = excl;
    }

    /**
     * Sets the scan context. If set, only uris that are part of the context are considered valid
     * for fetching.
     *
     * @param scanContext the new scan context
     */
    public void setScanContext(Context scanContext) {
        this.scanContext = scanContext;
    }
}
