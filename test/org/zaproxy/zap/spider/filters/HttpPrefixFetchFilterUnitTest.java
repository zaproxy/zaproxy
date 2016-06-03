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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.httpclient.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zaproxy.zap.spider.filters.FetchFilter.FetchStatus;

/**
 * Unit test for {@link HttpPrefixFetchFilter}.
 */
public class HttpPrefixFetchFilterUnitTest {

    @BeforeClass
    public static void suppressLogging() {
        Logger.getRootLogger().addAppender(new NullAppender());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateFetchFilterWithUndefinedURI() {
        // Given / When
        new HttpPrefixFetchFilter(null);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateFetchFilterWithNoScheme() throws Exception {
        // Given
        URI prefixUri = new URI("example.org/", true);
        // When
        new HttpPrefixFetchFilter(prefixUri);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateFetchFilterWithNonHttpOrHttpsScheme() throws Exception {
        // Given
        URI prefixUri = new URI("ftp://example.org/", true);
        // When
        new HttpPrefixFetchFilter(prefixUri);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateFetchFilterWithNoHost() throws Exception {
        // Given
        URI prefixUri = new URI("http://", true);
        // When
        new HttpPrefixFetchFilter(prefixUri);
        // Then = IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToCreateFetchFilterWithMalformedHost() throws Exception {
        // Given
        URI prefixUri = new URI("http://a%0/", true);
        // When
        new HttpPrefixFetchFilter(prefixUri);
        // Then = IllegalArgumentException
    }

    @Test
    public void shouldNotAddPathToNormalisedPrefixIfPrefixDoesNotHavePath() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org")));
    }

    @Test
    public void shouldDiscardUserInfoFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("http://user:pass@example.org", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org")));
    }

    @Test
    public void shouldDiscardEverythingAfterPathComponentFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org/path?query#fragment", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org/path")));
    }

    @Test
    public void shouldDiscardDefaultHttpPortFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org:80/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org/")));
    }

    @Test
    public void shouldDiscardDefaultHttpsPortFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org:443/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org/")));
    }

    @Test
    public void shouldKeepNonDefaultPortFromPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org:8443/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org:8443/")));
    }

    @Test
    public void shouldKeepDefaultHttpPortInHttpsPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("https://example.org:80/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("https://example.org:80/")));
    }

    @Test
    public void shouldKeepDefaultHttpsPortInHttpPrefix() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org:443/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        String normalisedPrefix = fetchFilter.getNormalisedPrefix();
        // Then
        assertThat(normalisedPrefix, is(equalTo("http://example.org:443/")));
    }

    @Test
    public void shouldFilterUndefinedUriAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(null);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithNoSchemeAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("/path", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithNonHttpOrHttpsSchemeAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("ftp://example.org/", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithNoHostAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithMalformedHostAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://a%0/", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithDifferentSchemeAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("https://example.org/", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithDifferentSchemeButSamePortAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("https://example.org:80/", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithDifferentPortAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org:1234/", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithDifferentHostAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://domain.example.org/", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithDifferentSmallerPathAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/p", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithDifferentPathAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/not/same/path", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithDifferentNonEmptyPathAsOutOfScope() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.OUT_OF_SCOPE)));
    }

    @Test
    public void shouldFilterUriWithSamePathPrefixAsValid() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/path/subtree", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.VALID)));
    }

    @Test
    public void shouldFilterUriAsValidWhenPathPrefixIsEmpty() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/path/subtree", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.VALID)));
    }

    @Test
    public void shouldFilterUriWithSamePathPrefixEvenIfHasQueryOrFragmentAsValid() throws Exception {
        // Given
        URI prefixUri = new URI("http://example.org/path", true);
        HttpPrefixFetchFilter fetchFilter = new HttpPrefixFetchFilter(prefixUri);
        URI uri = new URI("http://example.org/path/subtree/a?query#fragment", true);
        // When
        FetchStatus filterStatus = fetchFilter.checkFilter(uri);
        // Then
        assertThat(filterStatus, is(equalTo(FetchStatus.VALID)));
    }

}
