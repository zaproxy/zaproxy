/*
 * HeadURL: https://svn.apache.org/repos/asf/httpcomponents/oac.hc3x/trunk/src/java/org/apache/commons/httpclient/HttpConnection.java 
 * Revision: 832758 
 * Date: 2009-11-04 14:28:27 +0000 (Wed, 04 Nov 2009) 
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

package org.apache.commons.httpclient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.httpclient.util.ExceptionUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * Forked class...
 * 
 * It was forked because ZAP depends (and uses) Commons HttpClient which is not compatible with, the newer version, 
 * HttpComponents Client.
 * 
 * Changes:
 *  - Removed the characters "$" from the previous SVN keywords (HeadURL, Revision and Date) to avoid accidental expansions.
 *  - Address some JavaDoc warns.
 *  - Remove use of reflection to call Socket.shutdownOutput() in shutdownOutput(), not needed by minimum Java version targeted.
 *  - Add @Deprecated annotations to deprecated methods (by JavaDoc deprecated tag).
 *  - Change method tunnelCreated() to also create the tunnel if requested by calling code.
 *  - Pass the HttpConnectionParams when calling SecureProtocolSocketFactory.
 */
/**
 * An abstraction of an HTTP {@link InputStream} and {@link OutputStream}
 * pair, together with the relevant attributes.
 * <p>
 * The following options are set on the socket before getting the input/output 
 * streams in the {@link #open()} method:
 * <table border=1><tr>
 *    <th>Socket Method
 *    <th>Sockets Option
 *    <th>Configuration
 * </tr><tr>
 *    <td>{@link java.net.Socket#setTcpNoDelay(boolean)}
 *    <td>SO_NODELAY
 *    <td>{@link HttpConnectionParams#setTcpNoDelay(boolean)}
 * </tr><tr>
 *    <td>{@link java.net.Socket#setSoTimeout(int)}
 *    <td>SO_TIMEOUT
 *    <td>{@link HttpConnectionParams#setSoTimeout(int)}
 * </tr><tr>
 *    <td>{@link java.net.Socket#setSendBufferSize(int)}
 *    <td>SO_SNDBUF
 *    <td>{@link HttpConnectionParams#setSendBufferSize(int)}
 * </tr><tr>
 *    <td>{@link java.net.Socket#setReceiveBufferSize(int)}
 *    <td>SO_RCVBUF
 *    <td>{@link HttpConnectionParams#setReceiveBufferSize(int)}
 * </tr></table>
 *
 * @author Rod Waldhoff
 * @author Sean C. Sullivan
 * @author Ortwin Glueck
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @author Michael Becke
 * @author Eric E Johnson
 * @author Laura Werner
 * 
 * @version   $Revision: 832758 $ $Date: 2009-11-04 14:28:27 +0000 (Wed, 04 Nov 2009) $
* @deprecated (2.12.0) Implementation details, do not use. */
@Deprecated
public class HttpConnection {

    // ----------------------------------------------------------- Constructors

    /**
     * Creates a new HTTP connection for the given host and port.
     *
     * @param host the host to connect to
     * @param port the port to connect to
     */
    public HttpConnection(String host, int port) {
        this(null, -1, host, null, port, Protocol.getProtocol("http"));
    }

    /**
     * Creates a new HTTP connection for the given host and port
     * using the given protocol.
     *
     * @param host the host to connect to
     * @param port the port to connect to
     * @param protocol the protocol to use
     */
    public HttpConnection(String host, int port, Protocol protocol) {
        this(null, -1, host, null, port, protocol);
    }

    /**
     * Creates a new HTTP connection for the given host with the virtual 
     * alias and port using given protocol.
     *
     * @param host the host to connect to
     * @param virtualHost the virtual host requests will be sent to
     * @param port the port to connect to
     * @param protocol the protocol to use
     */
    public HttpConnection(String host, String virtualHost, int port, Protocol protocol) {
        this(null, -1, host, virtualHost, port, protocol);
    }

    /**
     * Creates a new HTTP connection for the given host and port via the 
     * given proxy host and port using the default protocol.
     *
     * @param proxyHost the host to proxy via
     * @param proxyPort the port to proxy via
     * @param host the host to connect to
     * @param port the port to connect to
     */
    public HttpConnection(
        String proxyHost,
        int proxyPort,
        String host,
        int port) {
        this(proxyHost, proxyPort, host, null, port, Protocol.getProtocol("http"));
    }

    /**
     * Creates a new HTTP connection for the given host configuration.
     * 
     * @param hostConfiguration the host/proxy/protocol to use
     */
    public HttpConnection(HostConfiguration hostConfiguration) {
        this(hostConfiguration.getProxyHost(),
             hostConfiguration.getProxyPort(),
             hostConfiguration.getHost(),
             hostConfiguration.getPort(),
             hostConfiguration.getProtocol());
        this.localAddress = hostConfiguration.getLocalAddress();
    }

    /**
     * Creates a new HTTP connection for the given host with the virtual 
     * alias and port via the given proxy host and port using the given 
     * protocol.
     * 
     * @param proxyHost the host to proxy via
     * @param proxyPort the port to proxy via
     * @param host the host to connect to. Parameter value must be non-null.
     * @param virtualHost No longer applicable. 
     * @param port the port to connect to
     * @param protocol The protocol to use. Parameter value must be non-null.
     * 
     * @deprecated use #HttpConnection(String, int, String, int, Protocol)
     */
    @Deprecated
    public HttpConnection(
        String proxyHost,
        int proxyPort,
        String host,
        String virtualHost,
        int port,
        Protocol protocol) {
    	this(proxyHost, proxyPort, host, port, protocol);
    }

    /**
     * Creates a new HTTP connection for the given host with the virtual 
     * alias and port via the given proxy host and port using the given 
     * protocol.
     * 
     * @param proxyHost the host to proxy via
     * @param proxyPort the port to proxy via
     * @param host the host to connect to. Parameter value must be non-null.
     * @param port the port to connect to
     * @param protocol The protocol to use. Parameter value must be non-null.
     */
    public HttpConnection(
        String proxyHost,
        int proxyPort,
        String host,
        int port,
        Protocol protocol) {

        if (host == null) {
            throw new IllegalArgumentException("host parameter is null");
        }
        if (protocol == null) {
            throw new IllegalArgumentException("protocol is null");
        }

        proxyHostName = proxyHost;
        proxyPortNumber = proxyPort;
        hostName = host;
        portNumber = protocol.resolvePort(port);
        protocolInUse = protocol;
    }

    // ------------------------------------------ Attribute Setters and Getters

    /**
     * Returns the connection socket.
     *
     * @return the socket.
     * 
     * @since 3.0
     */
    protected Socket getSocket() {
        return this.socket;
    }

    /**
     * Returns the host.
     *
     * @return the host.
     */
    public String getHost() {
        return hostName;
    }

    /**
     * Sets the host to connect to.
     *
     * @param host the host to connect to. Parameter value must be non-null.
     * @throws IllegalStateException if the connection is already open
     */
    public void setHost(String host) throws IllegalStateException {
        if (host == null) {
            throw new IllegalArgumentException("host parameter is null");
        }
        assertNotOpen();
        hostName = host;
    }

    /**
     * Returns the target virtual host.
     *
     * @return the virtual host.
     * 
     * @deprecated no longer applicable
     */

    @Deprecated
    public String getVirtualHost() {
        return this.hostName;
    }

    /**
     * Sets the virtual host to target.
     *
     * @param host the virtual host name that should be used instead of 
     *        physical host name when sending HTTP requests. Virtual host 
     *        name can be set to <tt> null</tt> if virtual host name is not
     *        to be used
     * 
     * @throws IllegalStateException if the connection is already open
     * 
     * @deprecated no longer applicable
     */

    @Deprecated
    public void setVirtualHost(String host) throws IllegalStateException {
        assertNotOpen();
    }

    /**
     * Returns the port of the host.
     *
     * If the port is -1 (or less than 0) the default port for
     * the current protocol is returned.
     *
     * @return the port.
     */
    public int getPort() {
        if (portNumber < 0) {
            return isSecure() ? 443 : 80;
        } else {
            return portNumber;
        }
    }

    /**
     * Sets the port to connect to.
     *
     * @param port the port to connect to
     * 
     * @throws IllegalStateException if the connection is already open
     */
    public void setPort(int port) throws IllegalStateException {
        assertNotOpen();
        portNumber = port;
    }

    /**
     * Returns the proxy host.
     *
     * @return the proxy host.
     */
    public String getProxyHost() {
        return proxyHostName;
    }

    /**
     * Sets the host to proxy through.
     *
     * @param host the host to proxy through.
     * 
     * @throws IllegalStateException if the connection is already open
     */
    public void setProxyHost(String host) throws IllegalStateException {
        assertNotOpen();
        proxyHostName = host;
    }

    /**
     * Returns the port of the proxy host.
     *
     * @return the proxy port.
     */
    public int getProxyPort() {
        return proxyPortNumber;
    }

    /**
     * Sets the port of the host to proxy through.
     *
     * @param port the port of the host to proxy through.
     * 
     * @throws IllegalStateException if the connection is already open
     */
    public void setProxyPort(int port) throws IllegalStateException {
        assertNotOpen();
        proxyPortNumber = port;
    }

    /**
     * Returns <tt>true</tt> if the connection is established over 
     * a secure protocol.
     *
     * @return <tt>true</tt> if connected over a secure protocol.
     */
    public boolean isSecure() {
        return protocolInUse.isSecure();
    }

    /**
     * Returns the protocol used to establish the connection.
     * @return The protocol
     */
    public Protocol getProtocol() {
        return protocolInUse;
    }

    /**
     * Sets the protocol used to establish the connection
     * 
     * @param protocol The protocol to use.
     * 
     * @throws IllegalStateException if the connection is already open
     */
    public void setProtocol(Protocol protocol) {
        assertNotOpen();

        if (protocol == null) {
            throw new IllegalArgumentException("protocol is null");
        }

        protocolInUse = protocol;

    }

    /**
     * Return the local address used when creating the connection.
     * If <tt>null</tt>, the default address is used.
     * 
     * @return InetAddress the local address to be used when creating Sockets
     */
    public InetAddress getLocalAddress() {
        return this.localAddress;
    }
    
    /**
     * Set the local address used when creating the connection.
     * If unset or <tt>null</tt>, the default address is used.
     * 
     * @param localAddress the local address to use
     */
    public void setLocalAddress(InetAddress localAddress) {
        assertNotOpen();
        this.localAddress = localAddress;
    }

    /**
     * Tests if the connection is open. 
     *
     * @return <code>true</code> if the connection is open
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Closes the connection if stale.
     * 
     * @return <code>true</code> if the connection was stale and therefore closed, 
     * <code>false</code> otherwise.
     * 
     * @see #isStale()
     * 
     * @since 3.0
     */
    public boolean closeIfStale() throws IOException {
        if (isOpen && isStale()) {
            LOG.debug("Connection is stale, closing...");
            close();
            return true;
        }
        return false;
    }
    
    /**
     * Tests if stale checking is enabled.
     * 
     * @return <code>true</code> if enabled
     * 
     * @see #isStale()
     * 
     * @deprecated Use {@link HttpConnectionParams#isStaleCheckingEnabled()},
     * {@link HttpConnection#getParams()}.
     */
    @Deprecated
    public boolean isStaleCheckingEnabled() {
        return this.params.isStaleCheckingEnabled();
    }

    /**
     * Sets whether or not isStale() will be called when testing if this connection is open.
     * 
     * <p>Setting this flag to <code>false</code> will increase performance when reusing
     * connections, but it will also make them less reliable.  Stale checking ensures that
     * connections are viable before they are used.  When set to <code>false</code> some
     * method executions will result in IOExceptions and they will have to be retried.</p>
     * 
     * @param staleCheckEnabled <code>true</code> to enable isStale()
     * 
     * @see #isStale()
     * @see #isOpen()
     * 
     * @deprecated Use {@link HttpConnectionParams#setStaleCheckingEnabled(boolean)},
     * {@link HttpConnection#getParams()}.
     */
    @Deprecated
    public void setStaleCheckingEnabled(boolean staleCheckEnabled) {
        this.params.setStaleCheckingEnabled(staleCheckEnabled);
    }

    /**
     * Determines whether this connection is "stale", which is to say that either
     * it is no longer open, or an attempt to read the connection would fail.
     *
     * <p>Unfortunately, due to the limitations of the JREs prior to 1.4, it is
     * not possible to test a connection to see if both the read and write channels
     * are open - except by reading and writing.  This leads to a difficulty when
     * some connections leave the "write" channel open, but close the read channel
     * and ignore the request.  This function attempts to ameliorate that
     * problem by doing a test read, assuming that the caller will be doing a
     * write followed by a read, rather than the other way around.
     * </p>
     *
     * <p>To avoid side-effects, the underlying connection is wrapped by a
     * {@link BufferedInputStream}, so although data might be read, what is visible
     * to clients of the connection will not change with this call.</p>.
     *
     * @throws IOException if the stale connection test is interrupted.
     * 
     * @return <tt>true</tt> if the connection is already closed, or a read would
     * fail.
     */
    protected boolean isStale() throws IOException {
        boolean isStale = true;
        if (isOpen) {
            // the connection is open, but now we have to see if we can read it
            // assume the connection is not stale.
            isStale = false;
            try {
                if (inputStream.available() <= 0) {
                    try {
                        socket.setSoTimeout(1);
                        inputStream.mark(1);
                        int byteRead = inputStream.read();
                        if (byteRead == -1) {
                            // again - if the socket is reporting all data read,
                            // probably stale
                            isStale = true;
                        } else {
                            inputStream.reset();
                        }
                    } finally {
                        socket.setSoTimeout(this.params.getSoTimeout());
                    }
                }
            } catch (InterruptedIOException e) {
                if (!ExceptionUtil.isSocketTimeoutException(e)) {
                    throw e;
                }
                // aha - the connection is NOT stale - continue on!
            } catch (IOException e) {
                // oops - the connection is stale, the read or soTimeout failed.
                LOG.debug(
                    "An error occurred while reading from the socket, is appears to be stale",
                    e
                );
                isStale = true;
            }
        }

        return isStale;
    }

    /**
     * Returns <tt>true</tt> if the connection is established via a proxy,
     * <tt>false</tt> otherwise.
     *
     * @return <tt>true</tt> if a proxy is used to establish the connection, 
     * <tt>false</tt> otherwise.
     */
    public boolean isProxied() {
        return (!(null == proxyHostName || 0 >= proxyPortNumber));
    }

    /**
     * Set the state to keep track of the last response for the last request.
     *
     * <p>The connection managers use this to ensure that previous requests are
     * properly closed before a new request is attempted.  That way, a GET
     * request need not be read in its entirety before a new request is issued.
     * Instead, this stream can be closed as appropriate.</p>
     *
     * @param inStream  The stream associated with an HttpMethod.
     */
    public void setLastResponseInputStream(InputStream inStream) {
        lastResponseInputStream = inStream;
    }

    /**
     * Returns the stream used to read the last response's body.
     *
     * <p>Clients will generally not need to call this function unless
     * using HttpConnection directly, instead of calling {@link HttpClient#executeMethod}.
     * For those clients, call this function, and if it returns a non-null stream,
     * close the stream before attempting to execute a method.  Note that
     * calling "close" on the stream returned by this function <i>may</i> close
     * the connection if the previous response contained a "Connection: close" header. </p>
     *
     * @return An {@link InputStream} corresponding to the body of the last
     *  response.
     */
    public InputStream getLastResponseInputStream() {
        return lastResponseInputStream;
    }

    // --------------------------------------------------- Other Public Methods

    /**
     * Returns {@link HttpConnectionParams HTTP protocol parameters} associated with this method.
     *
     * @return HTTP parameters.
     *
     * @since 3.0
     */
    public HttpConnectionParams getParams() {
        return this.params;
    }

    /**
     * Assigns {@link HttpConnectionParams HTTP protocol parameters} for this method.
     * 
     * @since 3.0
     * 
     * @see HttpConnectionParams
     */
    public void setParams(final HttpConnectionParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        this.params = params;
    }

    /**
     * Set the {@link Socket}'s timeout, via {@link Socket#setSoTimeout}.  If the
     * connection is already open, the SO_TIMEOUT is changed.  If no connection
     * is open, then subsequent connections will use the timeout value.
     * <p>
     * Note: This is not a connection timeout but a timeout on network traffic!
     *
     * @param timeout the timeout value
     * @throws SocketException - if there is an error in the underlying
     * protocol, such as a TCP error.
     * 
     * @deprecated Use {@link HttpConnectionParams#setSoTimeout(int)},
     * {@link HttpConnection#getParams()}.
     */
    @Deprecated
    public void setSoTimeout(int timeout)
        throws SocketException, IllegalStateException {
        this.params.setSoTimeout(timeout);
        if (this.socket != null) {
            this.socket.setSoTimeout(timeout);
        }
    }

    /**
     * Sets <code>SO_TIMEOUT</code> value directly on the underlying {@link Socket socket}. 
     * This method does not change the default read timeout value set via 
     * {@link HttpConnectionParams}.
     *
     * @param timeout the timeout value
     * @throws SocketException - if there is an error in the underlying
     * protocol, such as a TCP error.
     * @throws IllegalStateException if not connected
     * 
     * @since 3.0
     */
    public void setSocketTimeout(int timeout)
        throws SocketException, IllegalStateException {
        assertOpen();
        if (this.socket != null) {
            this.socket.setSoTimeout(timeout);
        }
    }

    /**
     * Returns the {@link Socket}'s timeout, via {@link Socket#getSoTimeout}, if the
     * connection is already open. If no connection is open, return the value subsequent 
     * connection will use.
     * <p>
     * Note: This is not a connection timeout but a timeout on network traffic!
     *
     * @return the timeout value
     * 
     * @deprecated Use {@link HttpConnectionParams#getSoTimeout()},
     * {@link HttpConnection#getParams()}.
     */
    @Deprecated
    public int getSoTimeout() throws SocketException {
        return this.params.getSoTimeout();
    }

    /**
     * Sets the connection timeout. This is the maximum time that may be spent
     * until a connection is established. The connection will fail after this
     * amount of time.
     * @param timeout The timeout in milliseconds. 0 means timeout is not used.
     * 
     * @deprecated Use {@link HttpConnectionParams#setConnectionTimeout(int)},
     * {@link HttpConnection#getParams()}.
     */
    @Deprecated
    public void setConnectionTimeout(int timeout) {
        this.params.setConnectionTimeout(timeout);
    }

    /**
     * Establishes a connection to the specified host and port
     * (via a proxy if specified).
     * The underlying socket is created from the {@link ProtocolSocketFactory}.
     *
     * @throws IOException if an attempt to establish the connection results in an
     *   I/O error.
     */
    public void open() throws IOException {
        LOG.trace("enter HttpConnection.open()");

        final String host = (proxyHostName == null) ? hostName : proxyHostName;
        final int port = (proxyHostName == null) ? portNumber : proxyPortNumber;
        assertNotOpen();
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Open connection to " + host + ":" + port);
        }
        
        try {
            if (this.socket == null) {
                usingSecureSocket = isSecure() && !isProxied();
                // use the protocol's socket factory unless this is a secure
                // proxied connection
                ProtocolSocketFactory socketFactory = null;
                if (isSecure() && isProxied()) {
                    Protocol defaultprotocol = Protocol.getProtocol("http");
                    socketFactory = defaultprotocol.getSocketFactory();
                } else {
                    socketFactory = this.protocolInUse.getSocketFactory();
                }
                this.socket = socketFactory.createSocket(
                            host, port, 
                            localAddress, 0,
                            this.params);
            }

            /*
            "Nagling has been broadly implemented across networks, 
            including the Internet, and is generally performed by default 
            - although it is sometimes considered to be undesirable in 
            highly interactive environments, such as some client/server 
            situations. In such cases, nagling may be turned off through 
            use of the TCP_NODELAY sockets option." */

            socket.setTcpNoDelay(this.params.getTcpNoDelay());
            socket.setSoTimeout(this.params.getSoTimeout());
            
            int linger = this.params.getLinger();
            if (linger >= 0) {
                socket.setSoLinger(linger > 0, linger);
            }
            
            int sndBufSize = this.params.getSendBufferSize();
            if (sndBufSize >= 0) {
                socket.setSendBufferSize(sndBufSize);
            }        
            int rcvBufSize = this.params.getReceiveBufferSize();
            if (rcvBufSize >= 0) {
                socket.setReceiveBufferSize(rcvBufSize);
            }        
            int outbuffersize = socket.getSendBufferSize();
            if ((outbuffersize > 2048) || (outbuffersize <= 0)) {
                outbuffersize = 2048;
            }
            int inbuffersize = socket.getReceiveBufferSize();
            if ((inbuffersize > 2048) || (inbuffersize <= 0)) {
                inbuffersize = 2048;
            }
            inputStream = new BufferedInputStream(socket.getInputStream(), inbuffersize);
            outputStream = new BufferedOutputStream(socket.getOutputStream(), outbuffersize);
            isOpen = true;
        } catch (IOException e) {
            // Connection wasn't opened properly
            // so close everything out
            closeSocketAndStreams();
            throw e;
        }
    }

    /**
     * Instructs the proxy to establish a secure tunnel to the host. The socket will 
     * be switched to the secure socket. Subsequent communication is done via the secure 
     * socket. The method can only be called once on a proxied secure connection.
     *
     * @throws IllegalStateException if connection is not secure and proxied or
     * if the socket is already secure.
     * @throws IOException if an attempt to establish the secure tunnel results in an
     *   I/O error.
     */
    public void tunnelCreated() throws IllegalStateException, IOException {
        LOG.trace("enter HttpConnection.tunnelCreated()");

        if (!isTunnelRequired()) {
            throw new IllegalStateException(
                "Connection must be secure "
                    + "and proxied or a tunnel requested to use this feature");
        }

        if (usingSecureSocket) {
            throw new IllegalStateException("Already using a secure socket");
        }
        
        if (isSecure()) {
        	org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory socketFactory =
                (org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory) protocolInUse.getSocketFactory();

            socket = socketFactory.createSocket(socket, hostName, portNumber, true, params);
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Secure tunnel to " + this.hostName + ":" + this.portNumber);
        }

        int sndBufSize = this.params.getSendBufferSize();
        if (sndBufSize >= 0) {
            socket.setSendBufferSize(sndBufSize);
        }        
        int rcvBufSize = this.params.getReceiveBufferSize();
        if (rcvBufSize >= 0) {
            socket.setReceiveBufferSize(rcvBufSize);
        }        
        int outbuffersize = socket.getSendBufferSize();
        if (outbuffersize > 2048) {
            outbuffersize = 2048;
        }
        int inbuffersize = socket.getReceiveBufferSize();
        if (inbuffersize > 2048) {
            inbuffersize = 2048;
        }
        inputStream = new BufferedInputStream(socket.getInputStream(), inbuffersize);
        outputStream = new BufferedOutputStream(socket.getOutputStream(), outbuffersize);
        usingSecureSocket = true;
        tunnelEstablished = true;
    }

    private boolean isTunnelRequired() {
        return (isSecure() && isProxied()) || tunnelRequested;
    }

    public void setTunnelRequested(boolean tunnelRequested) {
        this.tunnelRequested = tunnelRequested;
    }

    /**
     * Indicates if the connection is completely transparent from end to end.
     *
     * @return true if conncetion is not proxied or tunneled through a transparent
     * proxy; false otherwise.
     */
    public boolean isTransparent() {
        return !isProxied() || tunnelEstablished;
    }

    /**
     * Flushes the output request stream.  This method should be called to 
     * ensure that data written to the request OutputStream is sent to the server.
     * 
     * @throws IOException if an I/O problem occurs
     */
    public void flushRequestOutputStream() throws IOException {
        LOG.trace("enter HttpConnection.flushRequestOutputStream()");
        assertOpen();
        outputStream.flush();
    }

    /**
     * Returns an {@link OutputStream} suitable for writing the request.
     *
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     * @return a stream to write the request to
     */
    public OutputStream getRequestOutputStream()
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.getRequestOutputStream()");
        assertOpen();
        OutputStream out = this.outputStream;
        if (Wire.CONTENT_WIRE.enabled()) {
            out = new WireLogOutputStream(out, Wire.CONTENT_WIRE);
        }
        return out;
    }

    /**
     * Return a {@link InputStream} suitable for reading the response.
     * @return InputStream The response input stream.
     * @throws IOException If an IO problem occurs
     * @throws IllegalStateException If the connection isn't open.
     */
    public InputStream getResponseInputStream()
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.getResponseInputStream()");
        assertOpen();
        return inputStream;
    }

    /**
     * Tests if input data avaialble. This method returns immediately
     * and does not perform any read operations on the input socket
     * 
     * @return boolean <tt>true</tt> if input data is available, 
     *                 <tt>false</tt> otherwise.
     * 
     * @throws IOException If an IO problem occurs
     * @throws IllegalStateException If the connection isn't open.
     */
    public boolean isResponseAvailable() 
        throws IOException {
        LOG.trace("enter HttpConnection.isResponseAvailable()");
        if (this.isOpen) {
            return this.inputStream.available() > 0;
        } else {
            return false;
        }
    }

    /**
     * Tests if input data becomes available within the given period time in milliseconds.
     * 
     * @param timeout The number milliseconds to wait for input data to become available 
     * @return boolean <tt>true</tt> if input data is availble, 
     *                 <tt>false</tt> otherwise.
     * 
     * @throws IOException If an IO problem occurs
     * @throws IllegalStateException If the connection isn't open.
     */
    public boolean isResponseAvailable(int timeout) 
        throws IOException {
        LOG.trace("enter HttpConnection.isResponseAvailable(int)");
        if (!this.isOpen) {
            return false;
        }
        boolean result = false;
        if (this.inputStream.available() > 0) {
            result = true;
        } else {
            try {
                this.socket.setSoTimeout(timeout);
                inputStream.mark(1);
                int byteRead = inputStream.read();
                if (byteRead != -1) {
                    inputStream.reset();
                    LOG.debug("Input data available");
                    result = true;
                } else {
                    LOG.debug("Input data not available");
                }
            } catch (InterruptedIOException e) {
                if (!ExceptionUtil.isSocketTimeoutException(e)) {
                    throw e;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Input data not available after " + timeout + " ms");
                }
            } finally {
                try {
                    socket.setSoTimeout(this.params.getSoTimeout());
                } catch (IOException ioe) {
                    LOG.debug("An error ocurred while resetting soTimeout, we will assume that"
                        + " no response is available.",
                        ioe);
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Writes the specified bytes to the output stream.
     *
     * @param data the data to be written
     * @throws IllegalStateException if not connected
     * @throws IOException if an I/O problem occurs
     * @see #write(byte[],int,int)
     */
    public void write(byte[] data)
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.write(byte[])");
        this.write(data, 0, data.length);
    }

    /**
     * Writes <i>length</i> bytes in <i>data</i> starting at
     * <i>offset</i> to the output stream.
     *
     * The general contract for
     * write(b, off, len) is that some of the bytes in the array b are written
     * to the output stream in order; element b[off] is the first byte written
     * and b[off+len-1] is the last byte written by this operation.
     *
     * @param data array containing the data to be written.
     * @param offset the start offset in the data.
     * @param length the number of bytes to write.
     * @throws IllegalStateException if not connected
     * @throws IOException if an I/O problem occurs
     */
    public void write(byte[] data, int offset, int length)
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.write(byte[], int, int)");

        if (offset < 0) {
            throw new IllegalArgumentException("Array offset may not be negative");
        }
        if (length < 0) {
            throw new IllegalArgumentException("Array length may not be negative");
        }
        if (offset + length > data.length) {
            throw new IllegalArgumentException("Given offset and length exceed the array length");
        }
        assertOpen();
        this.outputStream.write(data, offset, length);
    }

    /**
     * Writes the specified bytes, followed by <tt>"\r\n".getBytes()</tt> to the
     * output stream.
     *
     * @param data the bytes to be written
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     */
    public void writeLine(byte[] data)
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.writeLine(byte[])");
        write(data);
        writeLine();
    }

    /**
     * Writes <tt>"\r\n".getBytes()</tt> to the output stream.
     *
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     */
    public void writeLine()
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.writeLine()");
        write(CRLF);
    }

    /**
     * @deprecated Use {@link #print(String, String)}
     * 
     * Writes the specified String (as bytes) to the output stream.
     *
     * @param data the string to be written
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     */
    @Deprecated
    public void print(String data)
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.print(String)");
        write(EncodingUtil.getBytes(data, "ISO-8859-1"));
    }

    /**
     * Writes the specified String (as bytes) to the output stream.
     *
     * @param data the string to be written
     * @param charset the charset to use for writing the data
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     * 
     * @since 3.0
     */
    public void print(String data, String charset)
    	throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.print(String)");
        write(EncodingUtil.getBytes(data, charset));
    }
    
    /**
     * @deprecated Use {@link #printLine(String, String)}
     * 
     * Writes the specified String (as bytes), followed by
     * <tt>"\r\n".getBytes()</tt> to the output stream.
     *
     * @param data the data to be written
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     */
    @Deprecated
    public void printLine(String data)
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.printLine(String)");
        writeLine(EncodingUtil.getBytes(data, "ISO-8859-1"));
    }

    /**
     * Writes the specified String (as bytes), followed by
     * <tt>"\r\n".getBytes()</tt> to the output stream.
     *
     * @param data the data to be written
     * @param charset the charset to use for writing the data
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     * 
     * @since 3.0
     */
    public void printLine(String data, String charset)
    	throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.printLine(String)");
        writeLine(EncodingUtil.getBytes(data, charset));
    }    
    
    /**
     * Writes <tt>"\r\n".getBytes()</tt> to the output stream.
     *
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     */
    public void printLine()
        throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.printLine()");
        writeLine();
    }

    /**
     * Reads up to <tt>"\n"</tt> from the (unchunked) input stream.
     * If the stream ends before the line terminator is found,
     * the last part of the string will still be returned.
     *
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     * @return a line from the response
     * 
     * @deprecated use #readLine(String)
     */
    @Deprecated
    public String readLine() throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.readLine()");

        assertOpen();
        return HttpParser.readLine(inputStream);
    }

    /**
     * Reads up to <tt>"\n"</tt> from the (unchunked) input stream.
     * If the stream ends before the line terminator is found,
     * the last part of the string will still be returned.
     * 
     * @param charset the charset to use for reading the data
     *
     * @throws IllegalStateException if the connection is not open
     * @throws IOException if an I/O problem occurs
     * @return a line from the response or null at the end of the stream
     * 
     * @since 3.0
     */
    public String readLine(final String charset) throws IOException, IllegalStateException {
        LOG.trace("enter HttpConnection.readLine()");

        assertOpen();
        return HttpParser.readLine(inputStream, charset);
    }

    /**
     * Attempts to shutdown the {@link Socket}'s output, via Socket.shutdownOutput()
     * when running on JVM 1.3 or higher.
     * 
     * @deprecated unused
     */
    @Deprecated
    public void shutdownOutput() {
        LOG.trace("enter HttpConnection.shutdownOutput()");

        try {
            socket.shutdownOutput();
        } catch (Exception ex) {
            LOG.debug("Unexpected Exception caught", ex);
            // Ignore, and hope everything goes right
        }
        // close output stream?
    }

    /**
     * Closes the socket and streams.
     */
    public void close() {
        LOG.trace("enter HttpConnection.close()");
        closeSocketAndStreams();
    }

    /**
     * Returns the httpConnectionManager.
     * @return HttpConnectionManager
     */
    public HttpConnectionManager getHttpConnectionManager() {
        return httpConnectionManager;
    }

    /**
     * Sets the httpConnectionManager.
     * @param httpConnectionManager The httpConnectionManager to set
     */
    public void setHttpConnectionManager(HttpConnectionManager httpConnectionManager) {
        this.httpConnectionManager = httpConnectionManager;
    }

    /**
     * Releases the connection. If the connection is locked or does not have a connection
     * manager associated with it, this method has no effect. Note that it is completely safe 
     * to call this method multiple times.
     */
    public void releaseConnection() {
        LOG.trace("enter HttpConnection.releaseConnection()");
        if (locked) {
            LOG.debug("Connection is locked.  Call to releaseConnection() ignored.");
        } else if (httpConnectionManager != null) {
            LOG.debug("Releasing connection back to connection manager.");
            httpConnectionManager.releaseConnection(this);
        } else {
            LOG.warn("HttpConnectionManager is null.  Connection cannot be released.");
        }
    }

    /**
     * Tests if the connection is locked. Locked connections cannot be released. 
     * An attempt to release a locked connection will have no effect.
     * 
     * @return <tt>true</tt> if the connection is locked, <tt>false</tt> otherwise.
     * 
     * @since 3.0
     */
    protected boolean isLocked() {
        return locked;
    }

    /**
     * Locks or unlocks the connection. Locked connections cannot be released. 
     * An attempt to release a locked connection will have no effect.
     * 
     * @param locked <tt>true</tt> to lock the connection, <tt>false</tt> to unlock
     *  the connection.
     * 
     * @since 3.0
     */
    protected void setLocked(boolean locked) {
        this.locked = locked;
    }
    // ------------------------------------------------------ Protected Methods

    /**
     * Closes everything out.
     */
    protected void closeSocketAndStreams() {
        LOG.trace("enter HttpConnection.closeSockedAndStreams()");

        isOpen = false;
        
        // no longer care about previous responses...
        lastResponseInputStream = null;

        if (null != outputStream) {
            OutputStream temp = outputStream;
            outputStream = null;
            try {
                temp.close();
            } catch (Exception ex) {
                LOG.debug("Exception caught when closing output", ex);
                // ignored
            }
        }

        if (null != inputStream) {
            InputStream temp = inputStream;
            inputStream = null;
            try {
                temp.close();
            } catch (Exception ex) {
                LOG.debug("Exception caught when closing input", ex);
                // ignored
            }
        }

        if (null != socket) {
            Socket temp = socket;
            socket = null;
            try {
                temp.close();
            } catch (Exception ex) {
                LOG.debug("Exception caught when closing socket", ex);
                // ignored
            }
        }
        
        tunnelEstablished = false;
        usingSecureSocket = false;
    }

    /**
     * Throws an {@link IllegalStateException} if the connection is already open.
     *
     * @throws IllegalStateException if connected
     */
    protected void assertNotOpen() throws IllegalStateException {
        if (isOpen) {
            throw new IllegalStateException("Connection is open");
        }
    }

    /**
     * Throws an {@link IllegalStateException} if the connection is not open.
     *
     * @throws IllegalStateException if not connected
     */
    protected void assertOpen() throws IllegalStateException {
        if (!isOpen) {
            throw new IllegalStateException("Connection is not open");
        }
    }

    /**
     * Gets the socket's sendBufferSize.
     * 
     * @return the size of the buffer for the socket OutputStream, -1 if the value
     * has not been set and the socket has not been opened
     * 
     * @throws SocketException if an error occurs while getting the socket value
     * 
     * @see Socket#getSendBufferSize()
     */
    public int getSendBufferSize() throws SocketException {
        if (socket == null) {
            return -1;
        } else {
            return socket.getSendBufferSize();
        }
    }

    /**
     * Sets the socket's sendBufferSize.
     * 
     * @param sendBufferSize the size to set for the socket OutputStream
     * 
     * @throws SocketException if an error occurs while setting the socket value
     * 
     * @see Socket#setSendBufferSize(int)
     * 
     * @deprecated Use {@link HttpConnectionParams#setSendBufferSize(int)},
     * {@link HttpConnection#getParams()}.
     */
    @Deprecated
    public void setSendBufferSize(int sendBufferSize) throws SocketException {
        this.params.setSendBufferSize(sendBufferSize);
    }

    // ------------------------------------------------------- Static Variable

    /** <tt>"\r\n"</tt>, as bytes. */
    private static final byte[] CRLF = new byte[] {(byte) 13, (byte) 10};

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(HttpConnection.class);
    
    // ----------------------------------------------------- Instance Variables
    
    /** My host. */
    private String hostName = null;
    
    /** My port. */
    private int portNumber = -1;
    
    /** My proxy host. */
    private String proxyHostName = null;
    
    /** My proxy port. */
    private int proxyPortNumber = -1;
    
    /** My client Socket. */
    private Socket socket = null;
    
    /** My InputStream. */
    private InputStream inputStream = null;

    /** My OutputStream. */
    private OutputStream outputStream = null;
    
    /** An {@link InputStream} for the response to an individual request. */
    private InputStream lastResponseInputStream = null;
    
    /** Whether or not the connection is connected. */
    protected boolean isOpen = false;
    
    /** the protocol being used */
    private Protocol protocolInUse;
    
    /** Collection of HTTP parameters associated with this HTTP connection*/
    private HttpConnectionParams params = new HttpConnectionParams();
    
    /** flag to indicate if this connection can be released, if locked the connection cannot be 
     * released */
    private boolean locked = false;
    
    /** Whether or not the socket is a secure one. */
    private boolean usingSecureSocket = false;
    
    /** Whether the connection is open via a secure tunnel or not */
    private boolean tunnelEstablished = false;
    
    /** the connection manager that created this connection or null */
    private HttpConnectionManager httpConnectionManager;
    
    /** The local interface on which the connection is created, or null for the default */
    private InetAddress localAddress;

    /** Whether or not a tunnel was requested by calling code. */
    private boolean tunnelRequested;
}
