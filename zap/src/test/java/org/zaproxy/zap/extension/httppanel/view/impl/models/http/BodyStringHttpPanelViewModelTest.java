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
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpBody;
import org.parosproxy.paros.network.HttpHeader;
import org.parosproxy.paros.network.HttpMessage;

public abstract class BodyStringHttpPanelViewModelTest<T1 extends HttpHeader, T2 extends HttpBody> {

    private static final String BODY = "Body 123 ABC";

    private AbstractHttpStringHttpPanelViewModel model;

    protected HttpMessage message;
    protected T1 header;
    protected T2 body;

    @BeforeEach
    void setup() {
        model = createModel();

        message = mock(HttpMessage.class);
        header = mock(getHeaderClass());
        body = mock(getBodyClass());

        given(body.toString()).willReturn(BODY);

        prepareMessage();
    }

    protected abstract AbstractHttpStringHttpPanelViewModel createModel();

    protected abstract Class<T1> getHeaderClass();

    protected abstract Class<T2> getBodyClass();

    protected abstract void prepareMessage();

    protected abstract void verifyBodySet(HttpMessage message, String body);

    @Test
    void shouldGetEmptyDataFromNullMessage() {
        // Given
        model.setMessage(null);
        // When
        String data = model.getData();
        // Then
        assertThat(data, is(emptyString()));
    }

    @Test
    void shouldGetDataFromBodyAsString() {
        // Given
        given(body.toString()).willReturn(BODY);
        model.setMessage(message);
        // When
        String data = model.getData();
        // Then
        assertThat(data, is(equalTo(BODY)));
    }

    @Test
    void shouldNotSetDataWithNullMessage() {
        // Given
        model.setMessage(null);
        // When / Then
        assertDoesNotThrow(() -> model.setData(BODY));
    }

    @Test
    void shouldSetDataIntoBodyAsContent() {
        // Given
        model.setMessage(message);
        String otherBodyContent = "Other Body";
        // When
        model.setData(otherBodyContent);
        // Then
        verifyBodySet(message, otherBodyContent);
        verify(header, times(0)).setContentLength(anyInt());
    }
}
