/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.httppanel.InvalidMessageDataException;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.AbstractHttpByteHttpPanelViewModel;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.HttpPanelViewModelUtils;

public class RequestByteHttpPanelViewModel extends AbstractHttpByteHttpPanelViewModel {

    private static final Logger logger = LogManager.getLogger(RequestByteHttpPanelViewModel.class);

    @Override
    public byte[] getData() {
        if (httpMessage == null) {
            return new byte[0];
        }

        byte[] headerBytes = httpMessage.getRequestHeader().toString().getBytes();
        byte[] bodyBytes = httpMessage.getRequestBody().getBytes();

        byte[] bytes = new byte[headerBytes.length + bodyBytes.length];

        System.arraycopy(headerBytes, 0, bytes, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, bytes, headerBytes.length, bodyBytes.length);

        return bytes;
    }

    @Override
    public void setData(byte[] data) {
        if (httpMessage == null) {
            return;
        }

        int pos = HttpPanelViewModelUtils.findHeaderLimit(data);

        if (pos == -1) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not Save Header, limit not found. Header: {}", new String(data));
            }
            throw new InvalidMessageDataException(
                    Constant.messages.getString("http.panel.model.header.warn.notfound"));
        }

        try {
            httpMessage.setRequestHeader(new String(data, 0, pos));
        } catch (HttpMalformedHeaderException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not Save Header: {}", new String(data, 0, pos), e);
            }
            throw new InvalidMessageDataException(
                    Constant.messages.getString("http.panel.model.header.warn.malformed"), e);
        }

        httpMessage.getRequestBody().setBody(ArrayUtils.subarray(data, pos, data.length));
    }
}
