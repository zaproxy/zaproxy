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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Unit test for {@link HttpResponseHeader}. */
public class HttpResponseHeaderUnitTest {

    @Test
    public void shouldBeEmptyIfNoContents() {
        // Given
        HttpResponseHeader header = new HttpResponseHeader();
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(true)));
    }

    @Test
    public void shouldNotBeEmptyIfItHasStatusLine() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @Test
    public void shouldNotBeEmptyIfItHasStatusAndHeaders() throws Exception {
        // Given
        HttpResponseHeader header =
                new HttpResponseHeader("HTTP/1.1 200 OK\r\nX-Header: value\r\n\r\n");
        // When
        boolean empty = header.isEmpty();
        // Then
        assertThat(empty, is(equalTo(false)));
    }

    @Test
    public void shouldSetValidStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        header.setStatusCode(100);
        // Then
        assertThat(header.getStatusCode(), is(equalTo(100)));
        assertThat(header.getPrimeHeader(), is(equalTo("HTTP/1.1 100 OK")));
    }

    @Test
    public void shouldFailToSetNegative3DigitStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> header.setStatusCode(-200));
        // Then
        assertThat(e.getMessage(), containsString("positive"));
    }

    @Test
    public void shouldFailToSet2DigitStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> header.setStatusCode(99));
        // Then
        assertThat(e.getMessage(), containsString("3 digit number"));
    }

    @Test
    public void shouldFailToSet4DigitStatusCode() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        IllegalArgumentException e =
                assertThrows(IllegalArgumentException.class, () -> header.setStatusCode(1000));
        // Then
        assertThat(e.getMessage(), containsString("3 digit number"));
    }

    @Test
    public void shouldSetReasonPhrase() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        header.setReasonPhrase("So So");
        // Then
        assertThat(header.getReasonPhrase(), is(equalTo("So So")));
        assertThat(header.getPrimeHeader(), is(equalTo("HTTP/1.1 200 So So")));
    }

    @Test
    public void shouldSetEmptyReasonPhraseIfNull() throws Exception {
        // Given
        HttpResponseHeader header = new HttpResponseHeader("HTTP/1.1 200 OK\r\n\r\n");
        // When
        header.setReasonPhrase(null);
        // Then
        assertThat(header.getReasonPhrase(), is(equalTo("")));
        assertThat(header.getPrimeHeader(), is(equalTo("HTTP/1.1 200")));
    }
}
