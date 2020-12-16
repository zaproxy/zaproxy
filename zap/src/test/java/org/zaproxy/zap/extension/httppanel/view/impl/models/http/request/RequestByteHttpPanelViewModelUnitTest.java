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
package org.zaproxy.zap.extension.httppanel.view.impl.models.http.request;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.ByteHttpPanelViewModelTest;
import org.zaproxy.zap.network.HttpRequestBody;

/** Unit test for {@link RequestByteHttpPanelViewModel}. */
class RequestByteHttpPanelViewModelUnitTest
        extends ByteHttpPanelViewModelTest<HttpRequestHeader, HttpRequestBody> {

    @Override
    protected RequestByteHttpPanelViewModel createModel() {
        return new RequestByteHttpPanelViewModel();
    }

    @Override
    protected Class<HttpRequestHeader> getHeaderClass() {
        return HttpRequestHeader.class;
    }

    @Override
    protected void prepareHeader() {
        given(header.getURI()).willReturn(mock(URI.class));
        super.prepareHeader();
    }

    @Override
    protected void verifyHeader(String header) throws HttpMalformedHeaderException {
        verify(message).setRequestHeader(header);
    }

    @Override
    protected void headerThrowsHttpMalformedHeaderException() throws HttpMalformedHeaderException {
        willThrow(HttpMalformedHeaderException.class).given(message).setRequestHeader(anyString());
    }

    @Override
    protected Class<HttpRequestBody> getBodyClass() {
        return HttpRequestBody.class;
    }

    @Override
    protected void prepareMessage() {
        given(message.getRequestHeader()).willReturn(header);
        given(message.getRequestBody()).willReturn(body);
    }
}
