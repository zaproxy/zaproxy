/*
 * HeadURL: https://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/trunk/src/java/org/apache/commons/httpclient/protocol/SecureProtocolSocketFactory.jav
 * Revision: 608014
 * Date: 2008-01-02 05:48:53 +0000 (Wed, 02 Jan 2008)
 *
 * ====================================================================
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.httpclient.protocol;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.params.HttpConnectionParams;

/*
 * Forked class...
 *
 * It was forked because ZAP depends (and uses) Commons HttpClient which is not compatible with, the newer version,
 * HttpComponents Client.
 *
 * Changes:
 *  - Removed the characters "$" from the previous SVN keywords (HeadURL, Revision and Date).
 *  - Add a (default) createSocket(...) that allows to provide the HttpConnectionParams.
 */
/**
 * A ProtocolSocketFactory that is secure.
 *
 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory
 *
 * @author Michael Becke
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
public interface SecureProtocolSocketFactory extends ProtocolSocketFactory {

    /**
     * Returns a socket connected to the given host that is layered over an
     * existing socket.  Used primarily for creating secure sockets through
     * proxies.
     *
     * @param socket the existing socket
     * @param host the host name/IP
     * @param port the port on the host
     * @param autoClose a flag for closing the underling socket when the created
     * socket is closed
     *
     * @return Socket a new socket
     *
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    Socket createSocket(
        Socket socket,
        String host,
        int port,
        boolean autoClose
    ) throws IOException, UnknownHostException;

    default Socket createSocket(
        Socket socket,
        String host,
        int port,
        boolean autoClose,
        HttpConnectionParams params
    ) throws IOException {
        return createSocket(socket, host, port, autoClose);
    }

}
