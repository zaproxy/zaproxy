/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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
package org.zaproxy.zap.spider;

import org.parosproxy.paros.network.HttpMessage;

/**
 * A result from a {@link SpiderTask}.
 *
 * <p>Contains the message and processing details.
 *
 * @since 2.7.0
 * @deprecated (2.12.0) See the spider add-on in zap-extensions instead.
 */
@Deprecated
public class SpiderTaskResult {

    private final HttpMessage httpMessage;
    private final boolean processed;
    private final String reasonNotProcessed;

    /**
     * Constructs a {@code SpiderTaskResult} with the given processed message.
     *
     * @param httpMessage the HTTP message that resulted from the spider task.
     */
    public SpiderTaskResult(HttpMessage httpMessage) {
        this(httpMessage, true, "");
    }

    /**
     * Constructs a {@code SpiderTaskResult} with the given non-processed message and the reason of
     * why it as not processed.
     *
     * @param httpMessage the HTTP message that resulted from the spider task.
     * @param reasonNotProcessed the reason of why the HTTP message was not processed.
     * @throws IllegalArgumentException if the given reason is {@code null}.
     */
    public SpiderTaskResult(HttpMessage httpMessage, String reasonNotProcessed) {
        this(httpMessage, false, reasonNotProcessed);
    }

    /**
     * Constructs a {@code SpiderTaskResult} with the given message, processed state, and the reason
     * of why it as not processed.
     *
     * @param httpMessage the HTTP message that resulted from the spider task.
     * @param processed {@code true} if the message was processed, {@code false} otherwise.
     * @param reasonNotProcessed the reason of why the HTTP message was not processed.
     */
    private SpiderTaskResult(
            HttpMessage httpMessage, boolean processed, String reasonNotProcessed) {
        if (reasonNotProcessed == null) {
            throw new IllegalArgumentException("Parameter reason must not be null.");
        }
        this.httpMessage = httpMessage;
        this.processed = processed;
        this.reasonNotProcessed = reasonNotProcessed;
    }

    /**
     * Gets the HTTP message that resulted from a spider task.
     *
     * @return the HTTP message, never {@code null}.
     */
    public HttpMessage getHttpMessage() {
        return httpMessage;
    }

    /**
     * Tells whether or not the HTTP message was processed (obtained and parsed).
     *
     * @return {@code true} if the message was processed, {@code false} otherwise.
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Gets the reason of why the HTTP message was not processed.
     *
     * @return the reason of why the HTTP message was not processed, never {@code null}.
     */
    public String getReasonNotProcessed() {
        return reasonNotProcessed;
    }
}
