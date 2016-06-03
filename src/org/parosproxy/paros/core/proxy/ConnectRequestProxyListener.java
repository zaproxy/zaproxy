/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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
package org.parosproxy.paros.core.proxy;

import org.parosproxy.paros.network.HttpMessage;

/**
 * A listener that will be notified when a HTTP CONNECT request is received from a client.
 * 
 * @since 2.5.0
 * @see ProxyListener
 * @see OverrideMessageProxyListener
 */
public interface ConnectRequestProxyListener {

    /**
     * Notifies that a HTTP CONNECT request was received from a client.
     * <p>
     * The {@code HttpMessage} {@code connectMessage} should not be modified.
     * 
     * @param connectMessage the HTTP CONNECT request received from a client
     */
    void receivedConnectRequest(HttpMessage connectMessage);
}
