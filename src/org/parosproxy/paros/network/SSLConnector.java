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
package org.parosproxy.paros.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.ReflectionSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;

import ch.csnc.extension.httpclient.SSLContextManager;

public class SSLConnector implements SecureProtocolSocketFactory {

	private static final String SSL = "SSL";

	// client socket factories
	private static SSLSocketFactory clientSSLSockFactory = null;
	private static SSLSocketFactory clientSSLSockCertFactory = null;

	// server related socket factories
	private static ServerSocketFactory serverSSLSockFactory = null;
	private static SSLSocketFactory tunnelSSLFactory = null;

	
	private static SSLContextManager sslContextManager = null;

	public SSLConnector() {
		if (clientSSLSockFactory == null) {
			clientSSLSockFactory = getClientSocketFactory(SSL);
		}
		if (serverSSLSockFactory == null) {
			serverSSLSockFactory = getServerSocketFactory(SSL);
		}
		if (sslContextManager == null) {
			sslContextManager = new SSLContextManager();
		}

	}

	public SSLSocket client(String hostName, int hostPort, boolean useClientCert)
			throws IOException {

		SSLSocket socket = null;

		socket = clientNoHandshake(hostName, hostPort, useClientCert);
		
		//socket.setEnabledProtocols(new String[] {"SSLv3"});
		//socket.setEnabledCipherSuites(new String[] {"SSL_RSA_WITH_DES_CBC_SHA"});

		socket.startHandshake();

		return socket;
	}

	public SSLSocket clientNoHandshake(String hostName, int hostPort,
			boolean useClientCert) throws IOException {

		SSLSocket socket = null;
		// SSL Strong Cipher Hack
		
		// SSL Strong Cipher Hack
		
		if (useClientCert) {
			socket = (SSLSocket) clientSSLSockCertFactory.createSocket(
					hostName, hostPort);
		} else {
			socket = (SSLSocket) clientSSLSockFactory.createSocket(hostName,
					hostPort);
		}
		socket.setEnabledProtocols(new String[] {"SSLv3"});
		socket.setEnabledCipherSuites(new String[] {"SSL_RSA_WITH_DES_CBC_SHA"});
		
		return socket;
	}

	public SSLContextManager getSSLContextManager() {
		return sslContextManager;
	}

	public void setEnableClientCert(boolean enabled) {
		if (enabled) {

			clientSSLSockFactory = clientSSLSockCertFactory;

			System.out.println();
			System.out.println("ClientCert enabled");
			System.out.println("using: " + sslContextManager.getDefaultKey());
			System.out.println();

		} else {
			clientSSLSockFactory = getClientSocketFactory(SSL);

			System.out.println();
			System.out.println("ClientCert disabled");
			System.out.println();
		}
	}

	public void setActiveCertificate() {

		SSLContext sslcont = sslContextManager.getSSLContext(sslContextManager
				.getDefaultKey());
		clientSSLSockCertFactory = sslcont.getSocketFactory();

		System.out.println();
		System.out.println("ActiveCertificate set to:");
		System.out.println(sslContextManager.getDefaultKey());
		System.out.println();
	}

	public ServerSocket listen(int portNum) throws IOException {
		ServerSocket sslServerPort = null;
		sslServerPort = serverSSLSockFactory.createServerSocket(portNum);
		return sslServerPort;
	}

	public ServerSocket listen() throws IOException {
		ServerSocket sslServerPort = null;
		sslServerPort = serverSSLSockFactory.createServerSocket();
		return sslServerPort;
	}

	public ServerSocket listen(int portNum, int maxConnection)
			throws IOException {
		ServerSocket sslServerPort = null;
		sslServerPort = serverSSLSockFactory.createServerSocket(portNum,
				maxConnection);
		return sslServerPort;
	}

	public ServerSocket listen(int paramPortNum, int maxConnection,
			InetAddress ip) throws IOException {

		ServerSocket sslServerPort = serverSSLSockFactory.createServerSocket(
				paramPortNum, maxConnection, ip);
		return sslServerPort;
	}

	public SSLSocketFactory getClientSocketFactory(String type) {
		// KeyManager[] keyMgr = null;
		TrustManager[] trustMgr = new TrustManager[] { new RelaxedX509TrustManager() }; // Trust
																						// all
																						// invalid
																						// server
																						// certificate

		try {
			SSLContext sslContext = SSLContext.getInstance(type);
			java.security.SecureRandom x = new java.security.SecureRandom();
			x.setSeed(System.currentTimeMillis());
			sslContext.init(null, trustMgr, x);
			clientSSLSockFactory = sslContext.getSocketFactory();
			HttpsURLConnection.setDefaultSSLSocketFactory(clientSSLSockFactory);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return clientSSLSockFactory;

	}

	public ServerSocketFactory getServerSocketFactory(String type) {

		if (type.equals("SSL") || type.equals("SSLv3")) {
			SSLServerSocketFactory ssf = null;
			try {
				// set up key manager to do server authentication
				SSLContext ctx;
				KeyManagerFactory kmf;
				KeyStore ks;
				char[] passphrase = "!@#$%^&*()".toCharArray();

				ctx = SSLContext.getInstance(type);
				kmf = KeyManagerFactory.getInstance("SunX509");
				ks = KeyStore.getInstance("JKS");

				java.security.SecureRandom x = new java.security.SecureRandom();
				x.setSeed(System.currentTimeMillis());

				// ks.load(new FileInputStream("paroskey"), passphrase);
				ks.load(this.getClass().getClassLoader().getResourceAsStream(
						"resource/paroskey"), passphrase);

				kmf.init(ks, passphrase);
				ctx.init(kmf.getKeyManagers(), null, x);

				ssf = ctx.getServerSocketFactory();
			

				tunnelSSLFactory = ctx.getSocketFactory();

				return ssf;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return ServerSocketFactory.getDefault();
		}
		return null;

	}


	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
	 */
	public Socket createSocket(String host, int port, InetAddress clientHost,
			int clientPort) throws IOException, UnknownHostException {

		Socket socket = clientSSLSockFactory.createSocket(host, port, clientHost, clientPort);
		
		return socket;
	}

	/**
	 * Attempts to get a new socket connection to the given host within the
	 * given time limit.
	 * <p>
	 * This method employs several techniques to circumvent the limitations of
	 * older JREs that do not support connect timeout. When running in JRE 1.4
	 * or above reflection is used to call Socket#connect(SocketAddress
	 * endpoint, int timeout) method. When executing in older JREs a controller
	 * thread is executed. The controller thread attempts to create a new socket
	 * within the given limit of time. If socket constructor does not return
	 * until the timeout expires, the controller terminates and throws an
	 * {@link ConnectTimeoutException}
	 * </p>
	 * 
	 * @param host
	 *            the host name/IP
	 * @param port
	 *            the port on the host
	 * @param clientHost
	 *            the local host name/IP to bind the socket to
	 * @param clientPort
	 *            the port on the local machine
	 * @param params
	 *            {@link HttpConnectionParams Http connection parameters}
	 * 
	 * @return Socket a new socket
	 * 
	 * @throws IOException
	 *             if an I/O error occurs while creating the socket
	 * @throws UnknownHostException
	 *             if the IP address of the host cannot be determined
	 */
	public Socket createSocket(final String host, final int port,
			final InetAddress localAddress, final int localPort,
			final HttpConnectionParams params) throws IOException,
			UnknownHostException, ConnectTimeoutException {
		if (params == null) {
			throw new IllegalArgumentException("Parameters may not be null");
		}
		int timeout = params.getConnectionTimeout();
		if (timeout == 0) {
			return createSocket(host, port, localAddress, localPort);
		} else {
			// To be eventually deprecated when migrated to Java 1.4 or above
			Socket socket = ReflectionSocketFactory.createSocket(
					"javax.net.ssl.SSLSocketFactory", host, port, localAddress,
					localPort, timeout);
			if (socket == null) {
				socket = ControllerThreadSocketFactory.createSocket(this, host,
						port, localAddress, localPort, timeout);
			}
			
			return socket;
		}
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
	 */
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		return clientSSLSockFactory.createSocket(host, port);
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return clientSSLSockFactory.createSocket(socket, host, port, autoClose);
	}

	/**
	 * Create a SSLsocket using an existing connected socket. It can be used
	 * such as a tunneled SSL proxy socket (eg when a CONNECT request is
	 * received). This SSLSocket will start server side handshake immediately.
	 * 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	public Socket createTunnelServerSocket(Socket socket) throws IOException {
		SSLSocket s = (SSLSocket) tunnelSSLFactory.createSocket(socket, socket
				.getInetAddress().getHostAddress(), socket.getPort(), true);
		
		s.setUseClientMode(false);
		s.startHandshake();
		return s;
	}

}

class RelaxedX509TrustManager implements X509TrustManager {
	public boolean checkClientTrusted(java.security.cert.X509Certificate[] chain) {
		return true;
	}

	public boolean isServerTrusted(java.security.cert.X509Certificate[] chain) {
		return true;
	}

	public boolean isClientTrusted(java.security.cert.X509Certificate[] chain) {
		return true;
	}

	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
			String authType) {
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
			String authType) {
	}
}
