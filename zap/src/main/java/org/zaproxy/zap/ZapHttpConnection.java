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
package org.zaproxy.zap;

import java.net.Socket;
import org.apache.commons.httpclient.HostConfiguration;

/**
 * Custom {@code HttpConnection} that exposes its socket and avoids closing.
 *
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
public class ZapHttpConnection extends org.apache.commons.httpclient.HttpConnection {

    /**
     * Creates a new HTTP connection for the given host configuration.
     *
     * @param hostConfiguration the host/proxy/protocol to use
     */
    public ZapHttpConnection(HostConfiguration hostConfiguration) {
        super(hostConfiguration);
    }

    /**
     * Returns the socket of this connection object. Make socket available, as parent getSocket() is
     * protected.
     *
     * @return Outgoing (remote) socket connection.
     */
    @Override
    public Socket getSocket() {
        return super.getSocket();
    }

    /**
     * Avoid closing in- &amp; output stream as that would close the underlying socket also. We have
     * to keep it for our WebSocket connection.
     */
    @Override
    protected void closeSocketAndStreams() {
        // do not close anything
    }
}
