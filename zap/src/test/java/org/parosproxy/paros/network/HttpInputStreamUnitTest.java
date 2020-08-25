/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.parosproxy.paros.network.HttpInputStream.BUFFER_SIZE;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zaproxy.zap.network.HttpRequestBody;

/** Unit test for {@link HttpInputStream}. */
class HttpInputStreamUnitTest {

    private static final int UNKOWN_LENGTH = -1;

    @ParameterizedTest
    @ValueSource(
            ints = {0, 5, 10, 100, 1_000, BUFFER_SIZE - 1, BUFFER_SIZE, BUFFER_SIZE + 1, 10_000})
    void shouldReadRequestBodyWithKnownLength(int length) throws Exception {
        // Given
        HttpHeader httpHeader = mock(HttpHeader.class);
        given(httpHeader.getContentLength()).willReturn(length);
        byte[] data = data(length);
        HttpInputStream httpInputStream = new HttpInputStream(createSocket(data));
        // When
        HttpRequestBody httpBody = httpInputStream.readRequestBody(httpHeader);
        // Then
        assertThat(httpBody, is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(data)));
    }

    @ParameterizedTest
    @ValueSource(
            ints = {0, 5, 10, 100, 1_000, BUFFER_SIZE - 1, BUFFER_SIZE, BUFFER_SIZE + 1, 10_000})
    void shouldReadRequestBodyWithUnknownLength(int length) throws Exception {
        // Given
        HttpHeader httpHeader = mock(HttpHeader.class);
        given(httpHeader.getContentLength()).willReturn(UNKOWN_LENGTH);
        byte[] data = data(length);
        HttpInputStream httpInputStream = new HttpInputStream(createSocket(data));
        // When
        HttpRequestBody httpBody = httpInputStream.readRequestBody(httpHeader);
        // Then
        assertThat(httpBody, is(not(nullValue())));
        assertThat(httpBody.getBytes(), is(equalTo(data)));
    }

    private static Socket createSocket(byte[] data) throws IOException {
        InputStream inputStream = new ByteArrayInputStream(data);
        Socket socket = mock(Socket.class);
        given(socket.getInputStream()).willReturn(inputStream);
        return socket;
    }

    private static byte[] data(int length) {
        byte[] data = new byte[length];
        Arrays.fill(data, (byte) 1);
        return data;
    }
}
