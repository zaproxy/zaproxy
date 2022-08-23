/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zaproxy.zap.model.Context;

/** Unit test for {@link DefaultFetchFilter}. */
@SuppressWarnings("deprecation")
@ExtendWith(MockitoExtension.class)
class DefaultFetchFilterUnitTest {

    @Mock Context context;

    private org.zaproxy.zap.spider.filters.DefaultFetchFilter filter;

    @BeforeEach
    void setUp() {
        filter = new org.zaproxy.zap.spider.filters.DefaultFetchFilter();
    }

    @Test
    void shouldFilterUriWithNonSchemeAsIllegalProtocol() throws Exception {
        // Given
        URI uri = createUri("example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(
                        equalTo(
                                org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus
                                        .ILLEGAL_PROTOCOL)));
    }

    @Test
    void shouldFilterUriWithNonHttpOrHttpsSchemeAsIllegalProtocol() throws Exception {
        // Given
        URI uri = createUri("ftp://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(
                        equalTo(
                                org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus
                                        .ILLEGAL_PROTOCOL)));
    }

    @Test
    void shouldFilterUriWithHttpSchemeAsOutOfScopeByDefault() throws Exception {
        // Given
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithHttpsSchemeAsOutOfScopeByDefault() throws Exception {
        // Given
        URI uri = createUri("https://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterOutOfScopeUriAsOutOfScope() throws Exception {
        // Given
        filter.addScopeRegex("scope.example.com");
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterInScopeUriAsValid() throws Exception {
        // Given
        filter.addScopeRegex("example.com");
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status, is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    @Test
    void shouldFilterNonAlwaysInScopeUriAsOutOfScope() throws Exception {
        // Given
        filter.setDomainsAlwaysInScope(domainsAlwaysInScope("scope.example.com"));
        URI uri = createUri("https://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterAlwaysInScopeUriAsValid() throws Exception {
        // Given
        filter.setDomainsAlwaysInScope(domainsAlwaysInScope("example.com"));
        URI uri = createUri("https://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status, is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    @Test
    void shouldFilterExcludedInScopeUriAsUserRules() throws Exception {
        // Given
        filter.addScopeRegex("example.com");
        filter.setExcludeRegexes(excludeRegexes(".*example\\.com.*"));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.USER_RULES)));
    }

    @Test
    void shouldFilterExcludedAlwaysInScopeUriAsUserRules() throws Exception {
        // Given
        filter.setDomainsAlwaysInScope(domainsAlwaysInScope("example.com"));
        filter.setExcludeRegexes(excludeRegexes(".*example\\.com.*"));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.USER_RULES)));
    }

    @Test
    void shouldFilterNonExcludedInScopeUriAsValid() throws Exception {
        // Given
        filter.addScopeRegex("example.com");
        filter.setExcludeRegexes(excludeRegexes("subdomain\\.example\\.com.*"));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status, is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    @Test
    void shouldFilterNonExcludedAlwaysInScopeUriAsValid() throws Exception {
        // Given
        filter.setDomainsAlwaysInScope(domainsAlwaysInScope("example.com"));
        filter.setExcludeRegexes(excludeRegexes("subdomain\\.example\\.com.*"));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status, is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    @Test
    void shouldFilterOutOfContextUriAsOutOfContext() throws Exception {
        // Given
        filter.setScanContext(contextInScope(false));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_CONTEXT)));
    }

    @Test
    void shouldFilterInContextUriAsValid() throws Exception {
        // Given
        filter.setScanContext(contextInScope(true));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status, is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    @Test
    void shouldFilterExcludedInContextUriAsUserRules() throws Exception {
        // Given
        filter.setScanContext(contextInScope(true));
        filter.setExcludeRegexes(excludeRegexes(".*example\\.com.*"));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.USER_RULES)));
    }

    @Test
    void shouldFilterNonExcludedInContextUriAsValid() throws Exception {
        // Given
        filter.setScanContext(contextInScope(true));
        filter.setExcludeRegexes(excludeRegexes("subdomain\\.example\\.com.*"));
        URI uri = createUri("http://example.com");
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus status = filter.checkFilter(uri);
        // Then
        assertThat(
                status, is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    private static URI createUri(String uri) {
        try {
            return new URI(uri, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope(
            String... domains) {
        if (domains == null || domains.length == 0) {
            return Collections.emptyList();
        }

        List<org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher> domainsAlwaysInScope =
                new ArrayList<>(1);
        for (String domain : domains) {
            domainsAlwaysInScope.add(new org.zaproxy.zap.spider.DomainAlwaysInScopeMatcher(domain));
        }
        return domainsAlwaysInScope;
    }

    private static List<String> excludeRegexes(String... regexes) {
        if (regexes == null || regexes.length == 0) {
            return Collections.emptyList();
        }

        List<String> excludedRegexes = new ArrayList<>(1);
        for (String regex : regexes) {
            excludedRegexes.add(regex);
        }
        return excludedRegexes;
    }

    private Context contextInScope(boolean inScope) {
        given(context.isInContext(anyString())).willReturn(inScope);
        return context;
    }
}
