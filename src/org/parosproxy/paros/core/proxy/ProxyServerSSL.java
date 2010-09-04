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
package org.parosproxy.paros.core.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.parosproxy.paros.network.*;


/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ProxyServerSSL extends ProxyServer {
	
	private static SSLConnector ssl = HttpSender.getSSLConnector();
    
	public ProxyServerSSL() {
		super();
	}
	
	
	protected ServerSocket createServerSocket(String ip, int port) throws UnknownHostException, IOException {
		
//		ServerSocket socket = ssl.listen(port, 300, InetAddress.getByName(getProxyParam().getProxyIp()));
		ServerSocket socket = ssl.listen(port, 300, InetAddress.getByName(ip));

		return socket;
	}
	
	protected ProxyThread createProxyProcess(Socket clientSocket) {
		ProxyThreadSSL process = new ProxyThreadSSL(this, clientSocket);
		return process;
	}
}
