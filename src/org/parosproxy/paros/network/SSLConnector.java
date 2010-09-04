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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ServerSocketFactory;
import javax.net.ssl.HttpsURLConnection;
//import javax.net.ssl.KeyManager;
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


public class SSLConnector implements SecureProtocolSocketFactory {

//	private static final String CRLF = "\r\n";
	private static final String SSL = "SSL";
	
    // client socket factories
	private static SSLSocketFactory clientSSLSockFactory = null;
	private static SSLSocketFactory clientSSLSockCertFactory = null;
    
    // server related socket factories
	private static ServerSocketFactory serverSSLSockFactory = null;
    private static SSLSocketFactory tunnelSSLFactory = null;
    
    
	public SSLConnector() {
		if (clientSSLSockFactory == null) {
			clientSSLSockFactory = getClientSocketFactory(SSL);
		}
		if (serverSSLSockFactory == null) {
			serverSSLSockFactory = getServerSocketFactory(SSL);
		}

	}

	public SSLSocket client(String hostName, int hostPort, boolean useClientCert) throws IOException {

		SSLSocket socket = null;

		socket = clientNoHandshake(hostName, hostPort, useClientCert);

		socket.startHandshake();

		return socket;
	}

	public SSLSocket clientNoHandshake(String hostName, int hostPort, boolean useClientCert) throws IOException {

		SSLSocket socket = null;

		if (useClientCert) {
			socket = (SSLSocket) clientSSLSockCertFactory.createSocket(hostName, hostPort);
		} else {
			socket = (SSLSocket) clientSSLSockFactory.createSocket(hostName, hostPort);
		}

		return socket;
	}

	/*
	public SSLSocket clientViaProxy(String hostName, int hostPort, String proxyName, int proxyPort, boolean useClientCert) throws IOException {

	    SSLSocket socket = clientViaProxyNoHandshake(hostName, hostPort, proxyName, proxyPort, useClientCert);

		socket.startHandshake();
		return socket;
	}

	public SSLSocket clientViaProxyNoHandshake(String hostName, int hostPort, String proxyName, int proxyPort, boolean useClientCert) throws IOException {

		HttpResponseHeader res = new HttpResponseHeader();
		Socket tunnel = establishTunnel(hostName, hostPort, proxyName, proxyPort, res);

		if (tunnel == null) {
			return null;
		}
		
		SSLSocket socket = null;
		if (useClientCert) {
	    	socket = (SSLSocket) clientSSLSockCertFactory.createSocket(tunnel, hostName, hostPort, true);
	    } else {
	    	socket = (SSLSocket) clientSSLSockFactory.createSocket(tunnel, hostName, hostPort, true);
	    }	

		return socket;
	}
	*/

	public void setClientCert(File keyFile, char[] passPhrase) throws Exception {
	
	    // Set up a key manager for client authentication
	    // if asked by the server.  Use the implementation's
	    // default TrustStore and secureRandom routines.
		//
		clientSSLSockCertFactory = null;
//		KeyManager[] keyMgr = null;
		TrustManager[] trustMgr = {new RelaxedX509TrustManager()};	// Trust all invalid server certificate

		SSLContext ctx;
		KeyManagerFactory kmf;
		KeyStore ks;

		ctx = SSLContext.getInstance(SSL);
		kmf = KeyManagerFactory.getInstance("SunX509");
		//ks = KeyStore.getInstance("JKS");
		ks = KeyStore.getInstance("pkcs12");

		ks.load(new FileInputStream(keyFile), passPhrase);
		java.security.SecureRandom x = new java.security.SecureRandom();
		x.setSeed(System.currentTimeMillis());
		kmf.init(ks, passPhrase);
		ctx.init(kmf.getKeyManagers(), trustMgr, x);

		clientSSLSockCertFactory = ctx.getSocketFactory();

	}
	
	public void setEnableClientCert(boolean enabled) {
	    if (enabled) {
	        clientSSLSockFactory = clientSSLSockCertFactory;
	    } else {
	        clientSSLSockFactory = getClientSocketFactory(SSL);
	    }
	}

	/*
	public Socket establishTunnel(String hostName, int hostPort, String proxyName, int proxyPort, HttpResponseHeader res) throws IOException {
		Socket tunnel = new Socket(proxyName, proxyPort);
		HttpInputStream tunnel_in = new HttpInputStream(tunnel.getInputStream());
		HttpOutputStream tunnel_out = new HttpOutputStream(tunnel.getOutputStream());
		HttpRequestHeader req = new HttpRequestHeader(getConnectString(hostName, hostPort));

		tunnel_out.write(req);
		tunnel_out.flush();
		HttpResponseHeader tunnelRes = (HttpResponseHeader) tunnel_in.readHeader();
		res.setMessage(tunnelRes.toString());
		if (res.isMalformedHeader() || res.getStatusCode() != HttpStatusCode.OK) {
			return null;
		}

		return tunnel;

	}
	*/

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

	public ServerSocket listen(int portNum, int maxConnection) throws IOException {
		ServerSocket sslServerPort = null;
		sslServerPort = serverSSLSockFactory.createServerSocket(portNum, maxConnection);
		return sslServerPort;
	}

	public ServerSocket listen(int paramPortNum, int maxConnection, InetAddress ip ) throws IOException {

      	ServerSocket sslServerPort = serverSSLSockFactory.createServerSocket(paramPortNum, maxConnection, ip);
		return sslServerPort;
	}

	public SSLSocketFactory getClientSocketFactory(String type) {
//		KeyManager[] keyMgr = null;
		TrustManager[] trustMgr = new TrustManager[]{new RelaxedX509TrustManager()};	// Trust all invalid server certificate

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

				//ks.load(new FileInputStream("paroskey"), passphrase);
				ks.load(this.getClass().getClassLoader().getResourceAsStream("resource/paroskey"), passphrase);
				
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

//	private static String getConnectString(String hostName, int hostPort) {
//		StringBuffer sb = new StringBuffer(200);
//		sb.append("CONNECT " + hostName + ":" + hostPort + " HTTP/1.0" + CRLF);
//		sb.append("User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0;)" + CRLF);
//		sb.append("Host: " + hostName + ":" + hostPort + CRLF);
//                    sb.append("Pragma: no-cache" + CRLF);
//		sb.append("Content-Length: 0" + CRLF);
//		sb.append(CRLF);
//		return sb.toString();
//	}

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
        String host,
        int port,
        InetAddress clientHost,
        int clientPort)
        throws IOException, UnknownHostException {

        Socket socket = clientSSLSockFactory.createSocket(
            host,
            port,
            clientHost,
            clientPort
        );
        return socket;
    }

    /**
     * Attempts to get a new socket connection to the given host within the given time limit.
     * <p>
     * This method employs several techniques to circumvent the limitations of older JREs that 
     * do not support connect timeout. When running in JRE 1.4 or above reflection is used to 
     * call Socket#connect(SocketAddress endpoint, int timeout) method. When executing in older 
     * JREs a controller thread is executed. The controller thread attempts to create a new socket
     * within the given limit of time. If socket constructor does not return until the timeout 
     * expires, the controller terminates and throws an {@link ConnectTimeoutException}
     * </p>
     *  
     * @param host the host name/IP
     * @param port the port on the host
     * @param clientHost the local host name/IP to bind the socket to
     * @param clientPort the port on the local machine
     * @param params {@link HttpConnectionParams Http connection parameters}
     * 
     * @return Socket a new socket
     * 
     * @throws IOException if an I/O error occurs while creating the socket
     * @throws UnknownHostException if the IP address of the host cannot be
     * determined
     */
    public Socket createSocket(
        final String host,
        final int port,
        final InetAddress localAddress,
        final int localPort,
        final HttpConnectionParams params
    ) throws IOException, UnknownHostException, ConnectTimeoutException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return createSocket(host, port, localAddress, localPort);
        } else {
            // To be eventually deprecated when migrated to Java 1.4 or above
            Socket socket = ReflectionSocketFactory.createSocket(
                "javax.net.ssl.SSLSocketFactory", host, port, localAddress, localPort, timeout);
            if (socket == null) {
                socket = ControllerThreadSocketFactory.createSocket(
                    this, host, port, localAddress, localPort, timeout);
            }
            return socket;
        }
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
        return clientSSLSockFactory.createSocket(
            host,
            port
        );
    }

    /**
     * @see SecureProtocolSocketFactory#createSocket(java.net.Socket,java.lang.String,int,boolean)
     */
    public Socket createSocket(
        Socket socket,
        String host,
        int port,
        boolean autoClose)
        throws IOException, UnknownHostException {
        return clientSSLSockFactory.createSocket(
            socket,
            host,
            port,
            autoClose
        );
    }
    
    /**
     * Create a SSLsocket using an existing connected socket.  It can be used
     * such as a tunneled SSL proxy socket (eg when a CONNECT request is received).
     * This SSLSocket will start server side handshake immediately.
     * @param socket
     * @return
     * @throws IOException
     */
    public Socket createTunnelServerSocket(Socket socket) throws IOException {
        SSLSocket s = (SSLSocket) tunnelSSLFactory.createSocket(socket, socket.getInetAddress().getHostAddress(), socket.getPort(), true);
        s.setUseClientMode(false);
        s.startHandshake();
        return s;
    }

	
}

class RelaxedX509TrustManager implements X509TrustManager {
	public boolean checkClientTrusted(java.security.cert.X509Certificate[] chain){
		return true;
	}

	public boolean isServerTrusted(java.security.cert.X509Certificate[] chain){
		return true;
	}

	public boolean isClientTrusted(java.security.cert.X509Certificate[] chain){
		return true;
	}


	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
	{}

	public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
	{}
}
