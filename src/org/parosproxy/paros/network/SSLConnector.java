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
// ZAP: 2013/01/23 Clean up of exception handling/logging.
// ZAP: 2013/01/25 Issue 462: SSLSocketFactory with TLS enabled and default Cipher options
// ZAP: 2013/06/01 Issue 669: Certificate algorithm constraints in Java 1.7
// ZAP: 2014/03/23 Tidy up, removed and deprecated unused methods other minor changes
// ZAP: 2014/03/23 Issue 951: TLS' versions 1.1 and 1.2 not enabled by default
// ZAP: 2014/07/17 Issue 704: ZAP Error: handshake alert: unrecognized_name
// ZAP: 2014/08/14 Issue 1184: Improve support for IBM JDK
// ZAP: 2014/08/14 Issue 1274: ZAP Error [javax.net.ssl.SSLException]: Unsupported record version SSLv2Hello
// ZAP: 2014/10/28 Issue 1390: Force https on cfu call
// ZAP: 2015/10/13 Issue 1975: Allow use of default disabled cipher suites (such as RC4-SHA)

package org.parosproxy.paros.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.apache.log4j.Logger;
import org.parosproxy.paros.security.CachedSslCertifificateServiceImpl;
import org.parosproxy.paros.security.SslCertificateService;

import ch.csnc.extension.httpclient.SSLContextManager;

public class SSLConnector implements SecureProtocolSocketFactory {

	private static final String SSL = "SSL";

	private static final String CONTENTS_UNRECOGNIZED_NAME_EXCEPTION = "unrecognized_name";

	public static final String SECURITY_PROTOCOL_SSL_V2_HELLO = "SSLv2Hello";
	public static final String SECURITY_PROTOCOL_SSL_V3 = "SSLv3";
	public static final String SECURITY_PROTOCOL_TLS_V1 = "TLSv1";
	public static final String SECURITY_PROTOCOL_TLS_V1_1 = "TLSv1.1";
	public static final String SECURITY_PROTOCOL_TLS_V1_2 = "TLSv1.2";

	private static final String[] DEFAULT_ENABLED_PROTOCOLS = {
		SECURITY_PROTOCOL_SSL_V3,
		SECURITY_PROTOCOL_TLS_V1,
		SECURITY_PROTOCOL_TLS_V1_1,
		SECURITY_PROTOCOL_TLS_V1_2 };

	private static final String[] FAIL_SAFE_DEFAULT_ENABLED_PROTOCOLS = { SECURITY_PROTOCOL_TLS_V1 };

	// client socket factories
	private SSLSocketFactory clientSSLSockFactory = null;
	private SSLSocketFactory clientSSLSockCertFactory = null;

	private static String[] supportedProtocols;
	private static String[] clientEnabledProtocols;
	private static String[] serverEnabledProtocols;

	private static ServerSslSocketsDecorator serverSslSocketsDecorator;
	private ClientSslSocketsDecorator clientSslSocketsDecorator;

    /**
     * The maximum time, in minutes, that the cached misconfigured hosts are considered valid.
     * 
     * @see #cacheMisconfiguredHost(String, int, InetAddress)
     */
    private static long MAX_AGE_MISCONFIGURED_HOST_IN_MIN = 5;

    /**
     * The maximum time, in milliseconds, that the cached misconfigured hosts are considered valid.
     * 
     * @see #MAX_AGE_MISCONFIGURED_HOST_IN_MIN
     * @see #cacheMisconfiguredHost(String, int, InetAddress)
     */
    private static long MAX_AGE_MISCONFIGURED_HOST_IN_MS = TimeUnit.MINUTES.toMillis(MAX_AGE_MISCONFIGURED_HOST_IN_MIN);

    /**
     * A cache of misconfigured hosts (i.e. secure connection cannot be established because of "unrecognized_name" exception).
     * The hosts are cached for a (short) period of time to avoid (most likely) failed connections.
     * <p>
     * The {@code key} is the hostname+port and the {@code value} the address of the host.
     * 
     * @see #MAX_AGE_MISCONFIGURED_HOST_IN_MIN
     * @see #timeStampLastStaleCheck
     * @see #cacheMisconfiguredHost(String, int, InetAddress)
     * @see #getCachedMisconfiguredHost(String, int)
     * @see #removeStaleCachedMisconfiguredHosts()
     */
	private static LRUMap misconfiguredHosts;
	
	/**
	 * Time stamp of last time the cache of misconfigured hosts was checked for stale entries.
	 * 
	 * @see #misconfiguredHosts
	 * @see #removeStaleCachedMisconfiguredHosts()
	 */
	private static long timeStampLastStaleCheck;

	// server related socket factories
	
	// ZAP: removed ServerSocketFaktory
	
	// ZAP: Added logger
	private static final Logger logger = Logger.getLogger(SSLConnector.class);
	
	private static SSLContextManager sslContextManager = null;

	/*
	 * If relaxedTrust then we ignore all of the 'usual' https checks.
	 * This is needed in order to test sites with custom certs
	 * However we dont want to override these checks for things like the 'check-for-updates' call
	 */
	private boolean relaxedTrust = true;

	public SSLConnector() {
		this(true);
	}

	public SSLConnector(boolean relaxedTrust) {
		this.relaxedTrust = relaxedTrust;
		if (clientSSLSockFactory == null) {
		    serverSslSocketsDecorator = new ServerSslSocketsDecorator();
		    clientSslSocketsDecorator = new ClientSslSocketsDecorator();

			clientSSLSockFactory = getClientSocketFactory(SSL);
			misconfiguredHosts = new LRUMap(10);
		}
		// ZAP: removed ServerSocketFaktory
		if (sslContextManager == null) {
			sslContextManager = new SSLContextManager();
		}

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
		clientSSLSockCertFactory = createDecoratedClientSslSocketFactory(sslcont.getSocketFactory());
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
			if (relaxedTrust) {
				sslContext.init(null, trustMgr, x);
			} else {
				sslContext.init(null, null, x);
			}
			clientSSLSockFactory = createDecoratedClientSslSocketFactory(sslContext.getSocketFactory());
			HttpsURLConnection.setDefaultSSLSocketFactory(clientSSLSockFactory);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return clientSSLSockFactory;

	}

	// ZAP: removed ServerSocketFaktory


	/**
	 * @deprecated (2.3.0) No longer supported since it's no longer required/called by Commons HttpClient library (version >= 
	 *             3.0). Throws {@code UnsupportedOperationException}.
	 */
	@Override
	@Deprecated
	public Socket createSocket(String host, int port, InetAddress clientHost,
			int clientPort) throws IOException, UnknownHostException {

		throw new UnsupportedOperationException(
				"Method no longer supported since it's no longer required/called by Commons HttpClient library (version >= 3.0).");
	}

	public static String[] getSupportedProtocols() {
		if (supportedProtocols == null) {
			readSupportedProtocols(null);
		}
		return Arrays.copyOf(supportedProtocols, supportedProtocols.length);
	}

	private static synchronized void readSupportedProtocols(SSLSocket sslSocket) {
		if (supportedProtocols == null) {
			logger.info("Reading supported SSL/TLS protocols...");
			String[] tempSupportedProtocols;
			if (sslSocket != null) {
				logger.info("Using an existing SSLSocket...");
				tempSupportedProtocols = sslSocket.getSupportedProtocols();
			} else {
				logger.info("Using a SSLEngine...");
				try {
					SSLContext ctx = SSLContext.getInstance(SSL);
					ctx.init(null, null, null);
					try {
						tempSupportedProtocols = ctx.createSSLEngine().getSupportedProtocols();
					} catch (UnsupportedOperationException e) {
						logger.warn("Failed to use SSLEngine. Trying with unconnected socket...", e);
						try (SSLSocket socket = (SSLSocket) ctx.getSocketFactory().createSocket()) {
							tempSupportedProtocols = socket.getSupportedProtocols();
						}
					}
				} catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
					logger.error("Failed to read the SSL/TLS supported protocols." + " Using default protocol versions: "
							+ Arrays.toString(FAIL_SAFE_DEFAULT_ENABLED_PROTOCOLS), e);
					tempSupportedProtocols = FAIL_SAFE_DEFAULT_ENABLED_PROTOCOLS;
				}
			}
			Arrays.sort(tempSupportedProtocols);
			supportedProtocols = tempSupportedProtocols;
			logger.info("Done reading supported SSL/TLS protocols: " + Arrays.toString(supportedProtocols));
		}
	}

	public static String[] getClientEnabledProtocols() {
		if (clientEnabledProtocols == null) {
			setClientEnabledProtocols(DEFAULT_ENABLED_PROTOCOLS);
		}
		return Arrays.copyOf(clientEnabledProtocols, clientEnabledProtocols.length);
	}

	public static void setClientEnabledProtocols(String[] protocols) {
		clientEnabledProtocols = extractSupportedProtocols(protocols);
	}

	public static String[] getServerEnabledProtocols() {
		if (serverEnabledProtocols == null) {
			setServerEnabledProtocols(DEFAULT_ENABLED_PROTOCOLS);
		}
		return Arrays.copyOf(serverEnabledProtocols, serverEnabledProtocols.length);
	}

	public static void setServerEnabledProtocols(String[] protocols) {
		serverEnabledProtocols = extractSupportedProtocols(protocols);
	}

	private static String[] extractSupportedProtocols(String[] enabledProtocols) {
		if (enabledProtocols == null || enabledProtocols.length == 0) {
			throw new IllegalArgumentException("Protocol(s) required but no protocol set.");
		}
		String[] supportedProtocols = getSupportedProtocols();
		ArrayList<String> enabledSupportedProtocols = new ArrayList<>(supportedProtocols.length);
		for (String protocol : enabledProtocols) {
			if (protocol != null && Arrays.binarySearch(supportedProtocols, protocol) >= 0) {
				enabledSupportedProtocols.add(protocol);
			}
		}
		enabledSupportedProtocols.trimToSize();

		if (enabledSupportedProtocols.isEmpty()) {
			throw new IllegalArgumentException("No supported protocol(s) set.");
		}

		String[] extractedSupportedProtocols = new String[enabledSupportedProtocols.size()];
		enabledSupportedProtocols.toArray(extractedSupportedProtocols);
		return extractedSupportedProtocols;
	}

	/**
	 * Attempts to get a new socket connection to the given host within the
	 * given time limit.
	 * 
	 * @param host
	 *            the host name/IP
	 * @param port
	 *            the port on the host
	 * @param localAddress
	 *            the local host name/IP to bind the socket to
	 * @param localPort
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
	 * @throws 	ConnectTimeoutException        
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
			InetAddress hostAddress = getCachedMisconfiguredHost(host, port);
			if (hostAddress != null) {
				return clientSSLSockFactory.createSocket(hostAddress, port, localAddress, localPort);
			}
			try {
				SSLSocket sslSocket = (SSLSocket) clientSSLSockFactory.createSocket(host, port, localAddress, localPort);
				sslSocket.startHandshake();

				return sslSocket;
			} catch (SSLException e) {
				if (!e.getMessage().contains(CONTENTS_UNRECOGNIZED_NAME_EXCEPTION)) {
					throw e;
				}

				hostAddress = InetAddress.getByName(host);
				cacheMisconfiguredHost(host, port, hostAddress);
				return clientSSLSockFactory.createSocket(hostAddress, port, localAddress, localPort);
			}
		}
		Socket socket = clientSSLSockFactory.createSocket();
		SocketAddress localAddr = new InetSocketAddress(localAddress, localPort);
		socket.bind(localAddr);
		SocketAddress remoteAddr = new InetSocketAddress(host, port);
		socket.connect(remoteAddr, timeout);
		
		return socket;
	}

	private static void cacheMisconfiguredHost(String host, int port, InetAddress address) {
		synchronized (misconfiguredHosts) {
			if (!misconfiguredHosts.isEmpty()) {
				removeStaleCachedMisconfiguredHosts();
			}

			logger.info("Caching address of misconfigured (\"unrecognized_name\") host [host=" + host + ", port=" + port
					+ "] for the next " + MAX_AGE_MISCONFIGURED_HOST_IN_MIN
					+ " minutes, following connections will not use the hostname.");
			misconfiguredHosts.put(host + port, new MisconfiguredHostCacheEntry(host, port, address));
		}
	}

    /**
     * Removes all stale cached misconfigured hosts.
     * <p>
     * <strong>Note:</strong> This method should be called in a {@code synchronized} block with the object
     * {@code misconfiguredHosts}.
     *
     * @see #misconfiguredHosts
     * @see #cacheMisconfiguredHost(String, int, InetAddress)
     * @see #getCachedMisconfiguredHost(String, int)
     */
	private static void removeStaleCachedMisconfiguredHosts() {
		long currentTime = System.currentTimeMillis();
		if (!((currentTime - timeStampLastStaleCheck) >= MAX_AGE_MISCONFIGURED_HOST_IN_MS)) {
			return;
		}
		timeStampLastStaleCheck = currentTime;

		for (MapIterator it = misconfiguredHosts.mapIterator(); it.hasNext();) {
			it.next();

			MisconfiguredHostCacheEntry entry = (MisconfiguredHostCacheEntry) it.getValue();
			if (entry.isStale(currentTime)) {
				logger.info("Removing stale cached address of misconfigured (\"unrecognized_name\") host [host="
						+ entry.getHost() + ", port=" + entry.getPort()
						+ "], following connections will be attempted with the hostname.");
				it.remove();
			}
		}
	}

	private static InetAddress getCachedMisconfiguredHost(String host, int port) {
		synchronized (misconfiguredHosts) {
			if (misconfiguredHosts.isEmpty()) {
				return null;
			}
			removeStaleCachedMisconfiguredHosts();

			MisconfiguredHostCacheEntry entry = (MisconfiguredHostCacheEntry) misconfiguredHosts.get(host + port);
			if (entry != null) {
				return entry.getAddress();
			}
			return null;
		}
	}

	/**
	 * @deprecated (2.3.0) No longer supported since it's no longer required/called by Commons HttpClient library (version >= 
	 *             3.0). Throws {@code UnsupportedOperationException}.
	 */
	@Override
	@Deprecated
	public Socket createSocket(String host, int port) throws IOException,
			UnknownHostException {
		throw new UnsupportedOperationException(
				"Method no longer supported since it's no longer required/called by Commons HttpClient library (version >= 3.0).");
	}

	/**
	 * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
	 */
	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		InetAddress inetAddress = getCachedMisconfiguredHost(host, port);
		if (inetAddress != null) {
			return clientSSLSockFactory.createSocket(socket, inetAddress.getHostAddress(), port, autoClose);
		}

		try {
			SSLSocket socketSSL = (SSLSocket) clientSSLSockFactory.createSocket(socket, host, port, autoClose);
			socketSSL.startHandshake();

			return socketSSL;
		} catch (SSLException e) {
			if (e.getMessage().contains(CONTENTS_UNRECOGNIZED_NAME_EXCEPTION)) {
				cacheMisconfiguredHost(host, port, InetAddress.getByName(host));
			}
			// Throw the exception anyway because the socket might no longer be usable (e.g. closed). The connection will be 
			// retried (see HttpMethodDirector#executeWithRetry(HttpMethod) for more information on the retry policy).
			throw e;
		}
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
			SSLContext ctx = SSLContext.getInstance(SSL);
			// Normally "SunX509", "IbmX509"...
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

			SslCertificateService scs = CachedSslCertifificateServiceImpl.getService();
			KeyStore ks = scs.createCertForHost(hostname);

			kmf.init(ks, SslCertificateService.PASSPHRASE);
			java.security.SecureRandom x = new java.security.SecureRandom();
			x.setSeed(System.currentTimeMillis());
			ctx.init(kmf.getKeyManagers(), null, x);

			SSLSocketFactory tunnelSSLFactory = createDecoratedServerSslSocketFactory(ctx.getSocketFactory());

			return tunnelSSLFactory;

        } catch (NoSuchAlgorithmException | KeyStoreException
                | CertificateException | UnrecoverableKeyException
                | KeyManagementException | InvalidKeyException
                | NoSuchProviderException | SignatureException | IOException e) {
            // Turn into RuntimeException. How to handle this error in a user
            // friendly way?
            throw new RuntimeException(e);
        }
	}

	private static SSLSocketFactory createDecoratedServerSslSocketFactory(final SSLSocketFactory delegate) {
		return new DecoratedSocketsSslSocketFactory(delegate, serverSslSocketsDecorator);
	}

	private SSLSocketFactory createDecoratedClientSslSocketFactory(final SSLSocketFactory delegate) {
		return new DecoratedSocketsSslSocketFactory(delegate, clientSslSocketsDecorator);
	}

	private static class ServerSslSocketsDecorator implements DecoratedSocketsSslSocketFactory.SslSocketDecorator {

		@Override
		public void decorate(SSLSocket sslSocket) {
			if (supportedProtocols == null) {
				readSupportedProtocols(sslSocket);
			}
			sslSocket.setEnabledProtocols(getServerEnabledProtocols());
		}
	}

	private class ClientSslSocketsDecorator implements DecoratedSocketsSslSocketFactory.SslSocketDecorator {

		@Override
		public void decorate(SSLSocket sslSocket) {
			if (supportedProtocols == null) {
				readSupportedProtocols(sslSocket);
			}
			sslSocket.setEnabledProtocols(getClientEnabledProtocols());
			if (relaxedTrust) {
				sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
			}
		}
	}

	private static class MisconfiguredHostCacheEntry {

		private final String host;
		private final int port;
		private final InetAddress address;
		private final long timeStampCreation;

		public MisconfiguredHostCacheEntry(String host, int port, InetAddress address) {
			this.host= host;
			this.port = port;
			this.address = address;
			this.timeStampCreation = System.currentTimeMillis();
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public InetAddress getAddress() {
			return address;
		}

		public boolean isStale(long currentTime) {
			return (currentTime - timeStampCreation) >= MAX_AGE_MISCONFIGURED_HOST_IN_MS;
		}
	}

}

class RelaxedX509TrustManager extends X509ExtendedTrustManager {
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

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
    }

    @Override
    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
    }

    @Override
    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
    }
}
