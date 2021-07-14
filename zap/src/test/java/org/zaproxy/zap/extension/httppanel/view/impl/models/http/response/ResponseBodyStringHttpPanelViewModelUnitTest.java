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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpResponseHeader;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.BodyStringHttpPanelViewModelTest;
import org.zaproxy.zap.network.HttpResponseBody;

/** Unit test for {@link ResponseBodyStringHttpPanelViewModel}. */
class ResponseBodyStringHttpPanelViewModelUnitTest
        extends BodyStringHttpPanelViewModelTest<HttpResponseHeader, HttpResponseBody> {

    @Override
    protected ResponseBodyStringHttpPanelViewModel createModel() {
        return new ResponseBodyStringHttpPanelViewModel();
    }

    @Override
    protected Class<HttpResponseHeader> getHeaderClass() {
        return HttpResponseHeader.class;
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

    @Override
    protected void verifyBodySet(HttpMessage message, String body) {
        verify(message).setResponseBody(body);
    }
}
