/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;

/** Unit test for {@link HttpPrefixFetchFilter}. */
@SuppressWarnings("deprecation")
class HttpPrefixFetchFilterUnitTest {

    @Test
    void shouldFailToCreateFetchFilterWithUndefinedURI() {
        // Given
        URI undefinedUri = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(undefinedUri));
    }

    @Test
    void shouldFailToCreateFetchFilterWithNoScheme() throws Exception {
        // Given
        URI prefixUri = new URI("example.org/", true);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri));
    }

    @Test
    void shouldFailToCreateFetchFilterWithNonHttpOrHttpsScheme() throws Exception {
        // Given
        URI prefixUri = new URI("ftp://example.org/", true);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri));
    }

    @Test
    void shouldFailToCreateFetchFilterWithNoHost() throws Exception {
        // Given
        URI prefixUri = new URI("http://", true);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri));
    }

    @Test
    void shouldFailToCreateFetchFilterWithMalformedHost() throws Exception {
        // Given
        URI prefixUri = new URI("http://a%0/", true);
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri));
    }

    @Test
    void shouldNotAddPathToNormalisedPrefixIfPrefixDoesNotHavePath() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org")));
    }

    @Test
    void shouldDiscardUserInfoFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("http://user:pass@example.org", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org")));
    }

    @Test
    void shouldDiscardEverythingAfterPathComponentFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org/path?query#fragment", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org/path")));
    }

    @Test
    void shouldDiscardDefaultHttpPortFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org:80/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org/")));
    }

    @Test
    void shouldDiscardDefaultHttpsPortFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org:443/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org/")));
    }

    @Test
    void shouldKeepNonDefaultPortFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org:8443/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org:8443/")));
    }

    @Test
    void shouldKeepDefaultHttpPortInHttpsPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org:80/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org:80/")));
    }

    @Test
    void shouldKeepDefaultHttpsPortInHttpPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org:443/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org:443/")));
    }

    @Test
    void shouldFilterUndefinedUriAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(null);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithNoSchemeAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("/path", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithNonHttpOrHttpsSchemeAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("ftp://example.org/", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithNoHostAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithMalformedHostAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://a%0/", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithDifferentSchemeAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("https://example.org/", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithDifferentSchemeButSamePortAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("https://example.org:80/", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithDifferentPortAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org:1234/", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithDifferentHostAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://domain.example.org/", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithDifferentSmallerPathAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/p", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithDifferentPathAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/not/same/path", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithDifferentNonEmptyPathAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    void shouldFilterUriWithSamePathPrefixAsValid() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/path/subtree", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    @Test
    void shouldFilterUriAsValidWhenPathPrefixIsEmpty() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/path/subtree", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }

    @Test
    void shouldFilterUriWithSamePathPrefixEvenIfHasQueryOrFragmentAsValid() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter fetchFilter =
                new org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/path/subtree/a?query#fragment", true);
        // When
        org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus filterStatus =
                fetchFilter.checkFilter(uri);
        // Then
        assertThat(
                filterStatus,
                is(equalTo(org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus.VALID)));
    }
}
