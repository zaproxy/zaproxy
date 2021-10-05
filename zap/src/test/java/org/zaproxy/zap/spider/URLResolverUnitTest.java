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
package org.zaproxy.zap.spider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link org.zaproxy.zap.spider.URLResolver}.
 *
 * @author bjoern.kimminich@gmx.de
 */
class URLResolverUnitTest {

    @Test
    void shouldThrowExceptionOnMissingBaseUrl() {
        // Given
        String baseUrl = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class, () -> URLResolver.resolveUrl(baseUrl, "notNull"));
    }

    @Test
    void shouldThrowExceptionOnMissingRelativeUrl() {
        // Given
        String relativeUrl = null;
        // When / Then
        assertThrows(
                IllegalArgumentException.class,
                () -> URLResolver.resolveUrl("notNull", relativeUrl));
    }

    @Test
    void shouldAppendRelativeUrlToBaseUrlHost() {
        assertThat(
                URLResolver.resolveUrl("http://www.abc.de", "/xy/z"), is("http://www.abc.de/xy/z"));
    }

    @Test
    void shouldInsertSlashBetweenBaseUrlAndRelativeUrlIfMissing() {
        assertThat(URLResolver.resolveUrl("http://www.abc.de", "xyz"), is("http://www.abc.de/xyz"));
    }

    @Test
    void shouldReplaceLastPartOfUrlPathFromBaseUrlWithRelativeUrl() {
        assertThat(
                URLResolver.resolveUrl("http://www.abc.de/w/x", "y/z"),
                is("http://www.abc.de/w/y/z"));
    }

    @Test
    void shouldRemoveFragmentFromBaseUrlBeforeAppendingRelativeUrl() {
        assertThat(
                URLResolver.resolveUrl("http://www.abc.de#anchor", "y"), is("http://www.abc.de/y"));
    }

    @Test
    void shouldRemoveQueryFromBaseUrlBeforeAppendingRelativeUrl() {
        assertThat(
                URLResolver.resolveUrl("http://www.abc.de?y=z", "test"),
                is("http://www.abc.de/test"));
    }

    @Test
    void shouldRemoveParametersFromBaseUrlBeforeAppendingRelativeUrl() {
        assertThat(
                URLResolver.resolveUrl("http://www.abc.de;y;z", "test"),
                is("http://www.abc.de/test"));
    }

    @Test
    void shouldReturnOriginalBaseUrlForGivenEmptyRelativeUrl() {
        assertThat(
                URLResolver.resolveUrl("http://www.abc.de/x?y=z&u=v#123", ""),
                is("http://www.abc.de/x?y=z&u=v#123"));
    }

    @Test
    void shouldReturnOriginalRelativeUrlForGivenAbsoluteUrlAsRelativeUrl() {
        assertThat(
                URLResolver.resolveUrl("http://base.url", "http://www.abc.de/x?y=z&u=v#123"),
                is("http://www.abc.de/x?y=z&u=v#123"));
    }

    @Test
    void shouldUseSchemeOfBaseUrlForGivenUrlWithHostAsRelativeUrl() {
        assertThat(
                URLResolver.resolveUrl("https://base.url", "//www.test.com"),
                is("https://www.test.com"));
    }

    @Test
    void shouldAppendQueryGivenAsRelativeUrlToBaseUrl() {
        assertThat(
                URLResolver.resolveUrl("http://abc.de/123", "?x=y"), is("http://abc.de/123?x=y"));
    }

    @Test
    void shouldAppendParametersGivenAsRelativeUrlToBaseUrl() {
        assertThat(
                URLResolver.resolveUrl("http://abc.de/123", ";x=y"), is("http://abc.de/123;x=y"));
    }

    @Test
    void shouldAppendFragmentGivenAsRelativeUrlToBaseUrl() {
        assertThat(
                URLResolver.resolveUrl("http://abc.de/123", "#test"), is("http://abc.de/123#test"));
    }

    @Test
    void shouldRemoveLeadingSlashPointsFromRelativeUrlBeforeAppendingToBaseUrl() {
        assertThat(
                URLResolver.resolveUrl("http://abc.de/123/xyz", "../test"),
                is("http://abc.de/test"));
    }

    @Test
    void shouldRemoveAllSlashPointSlashOccurrencesFromResolvedUrl() {
        assertThat(
                URLResolver.resolveUrl("http://abc.de/./", "test/./xyz/./123"),
                is("http://abc.de/test/xyz/123"));
    }

    @Test
    void shouldRemoveTrailingPointFromResolvedUrl() {
        assertThat(URLResolver.resolveUrl("http://abc.de", "test/."), is("http://abc.de/test/"));
    }

    @Test
    void shouldApplyDirectoryTraversalWithSlashPointsInResolvedUrl() {
        assertThat(
                URLResolver.resolveUrl("http://abc.de/x/../", "y/../z/../test/123/.."),
                is("http://abc.de/test/"));
    }
}
