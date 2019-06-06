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
package org.zaproxy.zap.network;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.network.HttpMessage;

/**
 * A validator of redirections, called for each redirection of a HTTP request.
 *
 * <p>As convenience the validator will also be notified of the HTTP messages sent and received
 * (first message and followed redirections, if any).
 *
 * @since 2.6.0
 */
@FunctionalInterface
public interface HttpRedirectionValidator {

    /**
     * Tells whether or not the given {@code redirection} is valid, to be followed.
     *
     * @param redirection the redirection being checked, never {@code null}.
     * @return {@code true} if the redirection is valid, {@code false} otherwise.
     */
    boolean isValid(URI redirection);

    /**
     * Notifies that a new message was sent and received (called for the first message and followed
     * redirections, if any).
     *
     * @param message the HTTP message that was received, never {@code null}.
     */
    default void notifyMessageReceived(HttpMessage message) {}
}
