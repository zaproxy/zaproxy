/*
 * Created on May 31, 2004
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
// ZAP: 2012/04/25 Added @Override annotation to all appropriate methods.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2014/01/22 Add the possibility to bound the proxy to all interfaces if null IP address has
// been set
// ZAP: 2017/11/06 Marked as Deprecated
//
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2022/06/07 Address deprecation warnings with SSLConnector.
package org.parosproxy.paros.core.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import org.parosproxy.paros.network.HttpSender;

@Deprecated
public class ProxyServerSSL extends ProxyServer {

    private static org.parosproxy.paros.network.SSLConnector ssl = HttpSender.getSSLConnector();

    public ProxyServerSSL() {
        super();
    }

    @Override
    protected ServerSocket createServerSocket(String ip, int port)
            throws UnknownHostException, IOException {

        // ZAP: added the possibility to bound to all interfaces (using null as InetAddress)
        //      when the ip is null or an empty string
        InetAddress addr = null;
        if ((ip != null) && !ip.isEmpty()) {
            addr = InetAddress.getByName(ip);
        }

        // ServerSocket socket = ssl.listen(port, 300,
        // InetAddress.getByName(getProxyParam().getProxyIp()));
        ServerSocket socket = ssl.listen(port, 300, addr);

        return socket;
    }

    @Override
    protected ProxyThread createProxyProcess(Socket clientSocket) {
        ProxyThreadSSL process = new ProxyThreadSSL(this, clientSocket);
        return process;
    }
}
