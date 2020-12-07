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
package org.zaproxy.zap.extension.httppanel.view.impl.models.http.response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.ByteHttpPanelViewModelTest;
import org.zaproxy.zap.network.HttpResponseBody;

/** Unit test for {@link ResponseByteHttpPanelViewModel}. */
class ResponseByteHttpPanelViewModelUnitTest
        extends ByteHttpPanelViewModelTest<HttpResponseHeader, HttpResponseBody> {

    @Override
    protected ResponseByteHttpPanelViewModel createModel() {
        return new ResponseByteHttpPanelViewModel();
    }

    @Override
    protected Class<HttpResponseHeader> getHeaderClass() {
        return HttpResponseHeader.class;
    }

    @Override
    protected void prepareHeader() {
        super.prepareHeader();
        given(header.isEmpty()).willReturn(false);
    }

    @Override
    protected void verifyHeader(String header) throws HttpMalformedHeaderException {
        verify(message).setResponseHeader(header);
    }

    @Override
    protected void headerThrowsHttpMalformedHeaderException() throws HttpMalformedHeaderException {
        willThrow(HttpMalformedHeaderException.class).given(message).setResponseHeader(anyString());
    }

    @Override
    protected Class<HttpResponseBody> getBodyClass() {
        return HttpResponseBody.class;
    }

    @Override
    protected void prepareMessage() {
        given(message.getResponseHeader()).willReturn(header);
        given(message.getResponseBody()).willReturn(body);
    }

    @Test
    void shouldGetEmptyDataWithEmptyHeader() {
        // Given
        given(header.isEmpty()).willReturn(true);
        model.setMessage(message);
        // When
        byte[] data = model.getData();
        // Then
        assertThat(data.length, is(equalTo(0)));
    }
}
