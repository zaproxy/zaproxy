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
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
package org.parosproxy.paros.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ControllerThreadSocketFactory;
import org.apache.commons.httpclient.protocol.ReflectionSocketFactory;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.parosproxy.paros.security.CachedSslCertifificateServiceImpl;
import org.parosproxy.paros.security.SslCertificateService;

import ch.csnc.extension.httpclient.SSLContextManager;

public class SSLConnector implements SecureProtocolSocketFactory {

	private static final String SSL = "SSL";

	// client socket factories
	private static SSLSocketFactory clientSSLSockFactory = null;
	private static SSLSocketFactory clientSSLSockCertFactory = null;

	// server related socket factories
	
	// ZAP: removed ServerSocketFaktory
	
	// ZAP: Added logger
	private Logger logger = Logger.getLogger(this.getClass());
	
	private static SSLContextManager sslContextManager = null;

	public SSLConnector() {
		if (clientSSLSockFactory == null) {
			clientSSLSockFactory = getClientSocketFactory(SSL);
		}
		// ZAP: removed ServerSocketFaktory
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
			logger.info("ClientCert enabled using: " + sslContextManager.getDefaultKey());
		} else {
			clientSSLSockFactory = getClientSocketFactory(SSL);
			logger.info("ClientCert disabled");
		}
	}

	public void setActiveCertificate() {

		SSLContext sslcont = sslContextManager.getSSLContext(sslContextManager
				.getDefaultKey());
		clientSSLSockCertFactory = sslcont.getSocketFactory();
		logger.info("ActiveCertificate set to: " + sslContextManager.getDefaultKey());
	}

	// ZAP: removed server socket methods

	//FIXME: really needed?
	public ServerSocket listen(int paramPortNum, int maxConnection,
			InetAddress ip) throws IOException {
	// ZAP: removed ServerSocketFaktory
//		ServerSocket sslServerPort = serverSSLSockFactory.createServerSocket(
//				paramPortNum, maxConnection, ip);
//		return sslServerPort;
		throw new UnsupportedOperationException("this code is probably not needed any more, SSL server sockets are not \"static\", they're created on the fly");
	}

	public SSLSocketFactory getClientSocketFactory(String type) {
		// Trust all invalid server certificate
		TrustManager[] trustMgr = new TrustManager[] { new RelaxedX509TrustManager() }; 

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

	// ZAP: removed ServerSocketFaktory


	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
	 */
	@Override
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
	@Override
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
		}
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

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
	 */
	@Override
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		return clientSSLSockFactory.createSocket(host, port);
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
	 */
	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return clientSSLSockFactory.createSocket(socket, host, port, autoClose);
	}

	/**
	 * Create a SSLsocket using an existing connected socket. It can be used
	 * such as a tunneled SSL proxy socket (eg when a CONNECT request is
	 * received). This SSLSocket will start server side handshake immediately.
	 * 
	 * @param targethost the host where you want to connect to 
	 * @param socket
	 * @return
	 * @throws IOException
	 */
	public Socket createTunnelServerSocket(String targethost, Socket socket) throws IOException {
		// ZAP: added host name parameter
		SSLSocket s = (SSLSocket) getTunnelSSLSocketFactory(targethost).createSocket(socket, socket
				.getInetAddress().getHostAddress(), socket.getPort(), true);
		
		s.setUseClientMode(false);
		s.startHandshake();
		return s;
	}

	// ZAP: added new ServerSocketFaktory with support of dynamic SSL certificates
	public SSLSocketFactory getTunnelSSLSocketFactory(String hostname) {

		//	SSLServerSocketFactory ssf = null;
		// set up key manager to do server authentication

		//	KeyStore ks;
		try {
			SSLContext ctx = SSLContext.getInstance("SSL");
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

			SslCertificateService scs = CachedSslCertifificateServiceImpl.getService();
			KeyStore ks = scs.createCertForHost(hostname);

			kmf.init(ks, SslCertificateService.PASSPHRASE);
			java.security.SecureRandom x = new java.security.SecureRandom();
			x.setSeed(System.currentTimeMillis());
			ctx.init(kmf.getKeyManagers(), null, x);

			SSLSocketFactory tunnelSSLFactory = ctx.getSocketFactory();

			return tunnelSSLFactory;

		} catch (NoSuchAlgorithmException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		} catch (KeyStoreException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
			//	} catch (IOException e) {
			//		// Turn into RuntimeException. How to handle this error in a user friendly way?
			//		throw new RuntimeException(e);
		} catch (UnrecoverableKeyException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		} catch (InvalidKeyException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		} catch (NoSuchProviderException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		} catch (SignatureException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		} catch (IOException e) {
			// Turn into RuntimeException. How to handle this error in a user friendly way?
			throw new RuntimeException(e);
		}
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

	@Override
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
			String authType) {
	}

	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
			String authType) {
	}
}
