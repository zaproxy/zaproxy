/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.extension.httppanel.view.largerequest;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpHeader;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.request.RequestStringHttpPanelViewModel;

/** @deprecated (2.12.0) No longer in use. */
@Deprecated
public class LargeRequestStringHttpPanelViewModel extends RequestStringHttpPanelViewModel {

    @Override
    public String getData() {
        if (httpMessage == null || httpMessage.getRequestHeader().isEmpty()) {
            return "";
        }

        return httpMessage.getRequestHeader().toString().replaceAll(HttpHeader.CRLF, HttpHeader.LF)
                + Constant.messages.getString(
                        "http.panel.view.largerequest.all.warning",
                        httpMessage.getRequestBody().length());
    }

    @Override
    public void setData(String data) {}
}
