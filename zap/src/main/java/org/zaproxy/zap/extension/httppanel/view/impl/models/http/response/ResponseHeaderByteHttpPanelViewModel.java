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
package org.zaproxy.zap.extension.httppanel.view.impl.models.http.response;

import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.httppanel.InvalidMessageDataException;
import org.zaproxy.zap.extension.httppanel.view.impl.models.http.AbstractHttpByteHttpPanelViewModel;

public class ResponseHeaderByteHttpPanelViewModel extends AbstractHttpByteHttpPanelViewModel {

    private static final Logger logger =
            LogManager.getLogger(ResponseHeaderByteHttpPanelViewModel.class);

    @Override
    public byte[] getData() {
        if (httpMessage == null || httpMessage.getResponseHeader().isEmpty()) {
            return new byte[0];
        }

        return httpMessage.getResponseHeader().toString().getBytes();
    }

    @Override
    public void setData(byte[] data) {
        if (httpMessage == null) {
            return;
        }

        try {
            httpMessage.setResponseHeader(new String(data));
        } catch (HttpMalformedHeaderException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not Save Header: {}", Arrays.toString(data), e);
            }
            throw new InvalidMessageDataException(
                    Constant.messages.getString("http.panel.model.header.warn.malformed"), e);
        }
    }
}
