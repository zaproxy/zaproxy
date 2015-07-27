/*
 * OWASP Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package org.zaproxy.zap.network;

import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpSender;

/**
 * A listener that will be notified when a new request is ready to be forwarded
 * to the server and when a new response is ready to be forwarded to the client.
 */
public interface HttpSenderListener {

    /**
     * Gets the order of when this listener should be notified.
     * 
     * <p>
     * The listeners are ordered in a natural order, the greater the order the
     * later it will be notified.
     * </p>
     * 
     * <p>
     * <strong>Note:</strong> If two or more listeners have the same order, the
     * order that those listeners will be notified is undefined.
     * </p>
     * 
     * @return an {@code int} with the value of the order that this listener
     *         should be notified about
     */
    int getListenerOrder();

    /**
     * Notifies the listener that a new request was received from the client and
     * is ready to be forwarded to the server.
     * <p>
     * The {@code HttpMessage} {@code msg} can be modified (only the request
     * should be modified). If the return value is {@code true} the message
     * <i>may be</i> forwarded and the following listeners will be notified, if
     * the value is {@code false} the message <i>will not</i> be forwarded and
     * no more listeners will be notified.
     * <p>
     * 
     * <p>
     * <strong>Note:</strong> In the presence of more than one listener there
     * are <i>no</i> guarantees that:
     * <ul>
     * <li>the {@code HttpMessage} {@code msg} is equal to the one forwarded to
     * the server, as the following listeners may modify it;</li>
     * <li>the message will really be forwarded to the server, even if the
     * return value is {@code true}, as the following listeners may return
     * {@code false}.</li>
     * <li>the {@code int} {@code initiator} as defined in the {@code HttpSender}
     * class. </li>
     * </ul>
     * </p>
     * 
     * @param msg
     *            the {@code HttpMessage} that may be forwarded to the server
     */
    void onHttpRequestSend(HttpMessage msg, int initiator, HttpSender sender);

    /**
     * Notifies the listener that a new response was received from the server
     * and is ready to be forwarded to the client.
     * <p>
     * The {@code HttpMessage} {@code msg} can be modified (only the response
     * should be modified). If the return value is {@code true} the message
     * <i>may be</i> forwarded and the following listeners will be notified, if
     * the value is {@code false} the message <i>will not</i> be forwarded and
     * no more listeners will be notified.
     * <p>
     * 
     * <p>
     * <strong>Note:</strong> In the presence of more than one listener there
     * are <i>no</i> guarantees that:
     * <ul>
     * <li>the {@code HttpMessage} {@code msg} is equal to the one forwarded to
     * the client, as the following listeners may modify it;</li>
     * <li>the message will really be forwarded to the client, even if the
     * return value is {@code true}, as the following listeners may return
     * {@code false}.</li>
     * <li>the {@code int} {@code initiator} as defined in the {@code HttpSender}
     * class. </li>
     * </ul>
     * </p>
     * 
     * @param msg
     *            the {@code HttpMessage} that may be forwarded to the client
     */
    void onHttpResponseReceive(HttpMessage msg, int initiator, HttpSender sender);

}
