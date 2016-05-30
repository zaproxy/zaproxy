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
package org.parosproxy.paros.core.proxy;

import org.parosproxy.paros.network.HttpMessage;

/**
 * A listener that will be notified when a new request is ready to be forwarded to the server and when a new response is ready
 * to be forwarded to the client.
 * <p>
 * It can override a message by returning a response without forwarding the request to the target server. Or override the
 * response from the server without notifying any other listener.
 * </p>
 * 
 * @since 2.3.0
 * @see ProxyListener
 * @see ConnectRequestProxyListener
 */
public interface OverrideMessageProxyListener extends ArrangeableProxyListener {

    /**
     * Notifies the listener that a new request was received from the client and is ready to be forwarded to the server.
     * <p>
     * The {@code HttpMessage} {@code msg} can be modified (preferably only when overriding the request). If the return value is
     * {@code true} the message <i>will not</i> be forwarded to the server, no more listeners will be notified and the response
     * set to the {@code msg} will be returned to the client. If the value is {@code false} the message <i>may be</i> forward
     * and the following listeners will be notified.
     * </p>
     * <p>
     * <strong>Note:</strong> In the presence of more than one listener there are <i>no</i> guarantees that:
     * <ul>
     * <li>the message will really be forwarded to the server, even if the return value is {@code false}, as the following
     * {@code ProxyListener}s may drop it.</li>
     * </ul>
     * </p>
     * 
     * @param msg the {@code HttpMessage} that may be overridden and not forwarded to the server
     * @return {@code true} if the message should be overridden and not forward to the server, {@code false} otherwise
     * @see ProxyListener#onHttpRequestSend(HttpMessage)
     */
    boolean onHttpRequestSend(HttpMessage msg);

    /**
     * Notifies the listener that a new response was received from the server and is ready to be forwarded to the client.
     * <p>
     * The {@code HttpMessage} {@code msg} can be modified (preferably only when overriding the response). If the return value
     * is {@code true} the message <i>will</i> be forwarded to the client and no more listeners will be notified. If the value
     * is {@code false} the message <i>may be</i> forwarded and the following listeners will be notified.
     * </p>
     * <p>
     * <strong>Note:</strong> In the presence of more than one listener there are <i>no</i> guarantees that:
     * <ul>
     * <li>the message will really be forwarded to the client, even if the return value is {@code false}, as the following
     * {@code ProxyListener}s may drop it.</li>
     * </ul>
     * </p>
     * 
     * @param msg the {@code HttpMessage} that may be forwarded to the client
     * @return {@code true} if the message should be forwarded to the client, {@code false} otherwise
     * @see ProxyListener#onHttpResponseReceive(HttpMessage)
     */
    boolean onHttpResponseReceived(HttpMessage msg);

}
