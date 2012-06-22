/*
 * Created on May 26, 2004
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
// ZAP: 2012/03/15 Added the method getProxyListenerOrder.
// ZAP: 2012/06/17 Documented the interface.

package org.parosproxy.paros.core.proxy;

import org.parosproxy.paros.network.HttpMessage;

/**
 * A listener that will be notified when a new request is ready to be forwarded
 * to the server and when a new response is ready to be forwarded to the client.
 */
// ZAP: Changed the JavaDoc.
public interface ProxyListener {

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
    // ZAP: Added the method.
    public int getProxyListenerOrder();

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
     * </ul>
     * </p>
     * 
     * @param msg
     *            the {@code HttpMessage} that may be forwarded to the server
     * @return {@code true} if the message should be forwarded to the server,
     *         {@code false} otherwise
     */
    // ZAP: Added the JavaDoc.
    public boolean onHttpRequestSend(HttpMessage msg);

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
     * </ul>
     * </p>
     * 
     * @param msg
     *            the {@code HttpMessage} that may be forwarded to the client
     * @return {@code true} if the message should be forwarded to the client,
     *         {@code false} otherwise
     */
    // ZAP: Added the JavaDoc.
    public boolean onHttpResponseReceive(HttpMessage msg);

}
