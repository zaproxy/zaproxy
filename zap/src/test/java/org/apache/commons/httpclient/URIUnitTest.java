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
package org.apache.commons.httpclient;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/** Unit test for {@link URI}. */
class URIUnitTest {

    @Test
    void shouldReturnPortFromUri() throws Exception {
        // Given
        URI uri = new URI("https://example.com:8080/", true);
        // When
        int port = uri.getPort();
        // Then
        assertThat(port, is(equalTo(8080)));
    }

    @Test
    void shouldReturnPortFromUriWithUnderscoresInHostname() throws Exception {
        // Given
        URI uri = new URI("http://hive_test_00.example.com:3001/", true);
        // When
        int port = uri.getPort();
        // Then
        assertThat(port, is(equalTo(3001)));
    }

    @Test
    void shouldCreateUriFromAuthority() throws Exception {
        // Given / When
        URI uri = URI.fromAuthority("www.example.com:8080");
        // Then
        assertThat(uri.getScheme(), is(nullValue()));
        assertThat(uri.getAuthority(), is(equalTo("www.example.com:8080")));
        assertThat(uri.getHost(), is(equalTo("www.example.com")));
        assertThat(uri.getPort(), is(equalTo(8080)));
        assertThat(uri.getPath(), is(nullValue()));
        assertThat(uri.toString(), is(equalTo("www.example.com:8080")));
    }

    @Test
    void shouldCreateUriFromAuthorityWithUnderscoresInHostname() throws Exception {
        // Given / When
        URI uri = URI.fromAuthority("hive_test_00.example.com:443");
        // Then
        assertThat(uri.getScheme(), is(nullValue()));
        assertThat(uri.getAuthority(), is(equalTo("hive_test_00.example.com:443")));
        assertThat(uri.getHost(), is(equalTo("hive_test_00.example.com")));
        assertThat(uri.getPort(), is(equalTo(443)));
        assertThat(uri.getPath(), is(nullValue()));
        assertThat(uri.toString(), is(equalTo("hive_test_00.example.com:443")));
    }
}
