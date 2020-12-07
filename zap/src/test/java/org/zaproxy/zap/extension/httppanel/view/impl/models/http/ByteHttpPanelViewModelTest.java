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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpBody;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.InvalidMessageDataException;
import org.zaproxy.zap.utils.I18N;

public abstract class ByteHttpPanelViewModelTest<T1 extends HttpHeader, T2 extends HttpBody> {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    private static final String HEADER = "Start Line\r\nHeader1: A\r\nHeader2: B\r\n\r\n";
    private static final byte[] HEADER_BYTES = HEADER.getBytes(DEFAULT_CHARSET);

    private static final String BODY = "Body\r\n 123\n ABC";
    private static final byte[] BODY_BYTES = "Body\r\n 123\n ABC".getBytes(DEFAULT_CHARSET);
    private static final byte[] MESSAGE = (HEADER + BODY).getBytes(DEFAULT_CHARSET);
    private static final byte[] EMPTY_BODY = new byte[0];

    protected AbstractHttpByteHttpPanelViewModel model;

    protected HttpMessage message;
    protected T1 header;
    protected T2 body;

    @BeforeEach
    void setup() {
        Constant.messages = mock(I18N.class);
        model = createModel();

        message = mock(HttpMessage.class);
        header = mock(getHeaderClass());
        body = mock(getBodyClass());

        prepareHeader();
        given(body.getBytes()).willReturn(BODY_BYTES);

        prepareMessage();
    }

    protected abstract AbstractHttpByteHttpPanelViewModel createModel();

    protected abstract Class<T1> getHeaderClass();

    protected void prepareHeader() {
        given(header.toString()).willReturn(HEADER);
    }

    protected abstract void verifyHeader(String header) throws HttpMalformedHeaderException;

    protected abstract void headerThrowsHttpMalformedHeaderException()
            throws HttpMalformedHeaderException;

    protected abstract Class<T2> getBodyClass();

    protected abstract void prepareMessage();

    @Test
    void shouldGetEmptyDataFromNullMessage() {
        // Given
        model.setMessage(null);
        // When
        byte[] data = model.getData();
        // Then
        assertThat(data.length, is(equalTo(0)));
    }

    @Test
    void shouldGetDataFromHeaderAndBody() {
        // Given
        model.setMessage(message);
        // When
        byte[] data = model.getData();
        // Then
        assertThat(data, is(equalTo(MESSAGE)));
    }

    @Test
    void shouldNotSetDataWithNullMessage() {
        // Given
        model.setMessage(null);
        // When / Then
        assertDoesNotThrow(() -> model.setData(BODY_BYTES));
    }

    @Test
    void shouldSetDataIntoHeaderAndBody() throws HttpMalformedHeaderException {
        // Given
        model.setMessage(message);
        String otherHeaderContent = "Other Start Line\r\nHeader1: A\r\nHeader2: B\r\n\r\n";
        String otherBodyContent = "Other Body\r\n 123\n ABC";
        byte[] data = (otherHeaderContent + otherBodyContent).getBytes(DEFAULT_CHARSET);
        // When
        model.setData(data);
        // Then
        verifyHeader(otherHeaderContent);
        verify(header, times(0)).setContentLength(anyInt());
        verify(body).setBody(otherBodyContent.getBytes(DEFAULT_CHARSET));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\r\n", "\r\r", "\n\n"})
    void shouldThrowExceptionIfHeaderLimitNotFound(String notHeaderLimit)
            throws HttpMalformedHeaderException {
        // Given
        model.setMessage(message);
        String otherHeaderContent = "Other Start Line\r\nHeader1: A\r\nHeader2: B" + notHeaderLimit;
        String otherBodyContent = "Other Body\r\n 123\n ABC";
        byte[] data = (otherHeaderContent + otherBodyContent).getBytes(DEFAULT_CHARSET);
        // When / Then
        assertThrows(InvalidMessageDataException.class, () -> model.setData(data));
    }

    @Test
    void shouldThrowExceptionWhenSettingMalformedHeader() throws HttpMalformedHeaderException {
        // Given
        model.setMessage(message);
        String otherHeaderContent = "Malformed Header";
        headerThrowsHttpMalformedHeaderException();
        String otherBodyContent = "Other Body\r\n 123\n ABC";
        byte[] data =
                (otherHeaderContent + "\r\n\r\n" + otherBodyContent).getBytes(DEFAULT_CHARSET);
        // When / Then
        assertThrows(InvalidMessageDataException.class, () -> model.setData(data));
        verify(header, times(0)).setContentLength(anyInt());
        verify(body, times(0)).setBody(any(byte[].class));
    }

    @Test
    void shouldSetDataOnlyIntoHeaderIfBodyEmpty() throws HttpMalformedHeaderException {
        // Given
        model.setMessage(message);
        byte[] data = HEADER_BYTES;
        // When
        model.setData(data);
        // Then
        verifyHeader(HEADER);
        verify(header, times(0)).setContentLength(anyInt());
        verify(body).setBody(EMPTY_BODY);
    }
}
