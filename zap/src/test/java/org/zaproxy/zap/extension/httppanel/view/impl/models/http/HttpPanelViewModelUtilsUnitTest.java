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
package org.zaproxy.zap.extension.httppanel.view.impl.models.http;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.network.HttpRequestBody;
import org.zaproxy.zap.network.HttpResponseBody;

/** Unit test for {@link HttpPanelViewModelUtils}. */
class HttpPanelViewModelUtilsUnitTest {

    @Test
    void shouldUpdateRequestContentLength() {
        // Given
        HttpMessage message = mock(HttpMessage.class);
        HttpRequestHeader requestHeader = spy(HttpRequestHeader.class);
        given(message.getRequestHeader()).willReturn(requestHeader);
        HttpRequestBody requestBody = mock(HttpRequestBody.class);
        given(message.getRequestBody()).willReturn(requestBody);
        int length = 1234;
        given(requestBody.length()).willReturn(length);
        // When
        HttpPanelViewModelUtils.updateRequestContentLength(message);
        // Then
        verify(requestHeader).setContentLength(length);
    }

    @Test
    void shouldUpdateResponseContentLength() {
        // Given
        HttpMessage message = mock(HttpMessage.class);
        HttpResponseHeader responseHeader = spy(HttpResponseHeader.class);
        given(message.getResponseHeader()).willReturn(responseHeader);
        HttpResponseBody responseBody = mock(HttpResponseBody.class);
        given(message.getResponseBody()).willReturn(responseBody);
        int length = 1234;
        given(responseBody.length()).willReturn(length);
        // When
        HttpPanelViewModelUtils.updateResponseContentLength(message);
        // Then
        verify(responseHeader).setContentLength(length);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Header\r\n\r\nBody", "Header\r\n\r\n\r\nBody"})
    void shouldFindHeaderLimitIfPresent(String value) {
        // Given
        byte[] content = value.getBytes(StandardCharsets.US_ASCII);
        // When
        int pos = HttpPanelViewModelUtils.findHeaderLimit(content);
        // Then
        assertThat(pos, is(equalTo(10)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Header\r\nBody", "Header\r\rBody", "Header\n\nBody"})
    void shouldNotFindHeaderLimitIfNotPresent(String value) {
        // Given
        byte[] content = value.getBytes(StandardCharsets.US_ASCII);
        // When
        int pos = HttpPanelViewModelUtils.findHeaderLimit(content);
        // Then
        assertThat(pos, is(equalTo(-1)));
    }
}
