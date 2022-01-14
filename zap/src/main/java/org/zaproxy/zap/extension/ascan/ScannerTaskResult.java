/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

import org.parosproxy.paros.network.HttpMessage;

/**
 * Contains the message and processing details.
 *
 * @since 2.12.0
 */
public class ScannerTaskResult {

    private final HttpMessage httpMessage;
    private final boolean processed;
    private final String reasonNotProcessed;

    public ScannerTaskResult(HttpMessage httpMessage) {
        this(httpMessage, true, "");
    }

    public ScannerTaskResult(HttpMessage httpMessage, String reasonNotProcessed) {
        this(httpMessage, false, reasonNotProcessed);
    }

    private ScannerTaskResult(
            HttpMessage httpMessage, boolean processed, String reasonNotProcessed) {
        if (reasonNotProcessed == null) {
            throw new IllegalArgumentException("Parameter reason must not be null.");
        }
        this.httpMessage = httpMessage;
        this.processed = processed;
        this.reasonNotProcessed = reasonNotProcessed;
    }

    public HttpMessage getHttpMessage() {
        return httpMessage;
    }

    public boolean isProcessed() {
        return processed;
    }

    public String getReasonNotProcessed() {
        return reasonNotProcessed;
    }
}
