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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link URI}.
 */
public class URIUnitTest {

    @Test
    public void shouldReturnPortFromUri() throws Exception {
        // Given
        URI uri = new URI("https://example.com:8080/", true);
        // When
        int port = uri.getPort();
        // Then
        assertThat(port, is(equalTo(8080)));
    }

    @Test
    public void shouldReturnPortFromUriWithUnderscoresInHostname() throws Exception {
        // Given
        URI uri = new URI("http://hive_test_00.example.com:3001/", true);
        // When
        int port = uri.getPort();
        // Then
        assertThat(port, is(equalTo(3001)));
    }
}
