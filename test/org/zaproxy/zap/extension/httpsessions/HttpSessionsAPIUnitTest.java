/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.extension.httpsessions;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link HttpSessionsAPI}.
 */
public class HttpSessionsAPIUnitTest {

    private static final String HOST = "example.com";

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionWhenGettingAuthorityFromNullSite() {
        // Given
        String nullSite = null;
        // When
        HttpSessionsAPI.getAuthority(nullSite);
        // Then = NullPointerException
    }

    @Test
    public void shouldReturnEmptySiteWhenGettingAuthorityFromEmptySite() {
        // Given
        String emptySite = "";
        // When
        String authority = HttpSessionsAPI.getAuthority(emptySite);
        // Then
        assertThat(authority, is(equalTo(emptySite)));
    }

    @Test
    public void shouldNotRemovePortWhenGettingAuthorityFromSite() {
        // Given
        String siteWithPort = HOST + ":8080";
        // When
        String authority = HttpSessionsAPI.getAuthority(siteWithPort);
        // Then
        assertThat(authority, is(equalTo(siteWithPort)));
    }

    @Test
    public void shouldRemoveHttpSchemeWhenGettingAuthorityFromSite() {
        // Given
        String site = "http://" + HOST;
        // When
        String authority = HttpSessionsAPI.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST)));
    }

    @Test
    public void shouldRemoveSecureHttpSchemeWhenGettingAuthorityFromSite() {
        // Given
        String site = "https://" + HOST;
        // When
        String authority = HttpSessionsAPI.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST)));
    }

    @Test
    public void shouldIgnoreEmptyPathComponentWhenGettingAuthorityFromSite() {
        // Given
        String site = HOST;
        // When
        String authority = HttpSessionsAPI.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST)));
    }

    @Test
    public void shouldRemoveNonEmptyPathComponentWhenGettingAuthorityFromSite() {
        // Given
        String site = HOST + "/path";
        // When
        String authority = HttpSessionsAPI.getAuthority(site);
        // Then
        assertThat(authority, is(equalTo(HOST)));
    }
}
