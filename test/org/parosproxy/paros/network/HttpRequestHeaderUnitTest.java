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
package org.parosproxy.paros.network;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.commons.httpclient.URI;
import org.junit.Test;

/**
 * Unit test for {@link HttpRequestHeader}.
 */
public class HttpRequestHeaderUnitTest {

    @Test
    public void shouldBeEmptyIfNoContents() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(true)));
    }

    @Test
    public void shouldNotBeEmptyIfItHasRequestLine() throws Exception {
        // Given
        HttpRequestHeader header = new HttpRequestHeader("GET http://example.com/ HTTP/1.1\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @Test
    public void shouldNotBeEmptyIfItHasRequestLineAndHeaders() throws Exception {
        // Given
        HttpRequestHeader header = new HttpRequestHeader("GET / HTTP/1.1\r\nHost: example.com\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @Test
    public void shouldNotBeImageIfItHasNoRequestUri() {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        // When
        boolean image = header.isImage();
        // Then
        assertThat(image, is(equalTo(false)));
    }

    @Test
    public void shouldNotBeImageIfRequestUriHasNoPath() throws Exception {
        // Given
        HttpRequestHeader header = new HttpRequestHeader();
        header.setURI(new URI("http://example.com", true));
        // When
        boolean image = header.isImage();
        // Then
        assertThat(image, is(equalTo(false)));
    }

    @Test
    public void shouldBeImageIfRequestUriHasPathWithImageExtension() throws Exception {
        // Given
        String[] extensions = { "bmp", "ico", "jpg", "jpeg", "gif", "tiff", "tif", "png" };
        HttpRequestHeader header = new HttpRequestHeader();
        for (String extension : extensions) {
            header.setURI(new URI("http://example.com/image." + extension, true));
            // When
            boolean image = header.isImage();
            // Then
            assertThat(image, is(equalTo(true)));
        }
    }
}
