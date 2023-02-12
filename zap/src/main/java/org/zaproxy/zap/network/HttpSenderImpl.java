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
package org.zaproxy.zap.network;

import java.io.IOException;
import java.nio.file.Path;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;

/** <strong>Note:</strong> Not part of the public API. */
public interface HttpSenderImpl<T extends HttpSenderContext> {

    boolean isGlobalStateEnabled();

    void addListener(HttpSenderListener listener);

    void removeListener(HttpSenderListener listener);

    T createContext(HttpSender parent, int initiator);

    default T getContext(HttpSender httpSender) {
        return null;
    }

    default void sendAndReceive(T ctx, HttpRequestConfig config, HttpMessage msg, Path file)
            throws IOException {}

    default void sendAndReceive(
            HttpSender parent, HttpRequestConfig config, HttpMessage msg, Path file)
            throws IOException {}

    default Object saveState() {
        return null;
    }

    default void restoreState(Object implState) {}
}
