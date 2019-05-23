/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2014 The ZAP Development Team
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
package org.parosproxy.paros.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * A {@code SSLSocketFactory} that allows to decorate {@code SSLSocket}s after creation but before returning them.
 * 
 * @see SSLSocketFactory
 * @see SslSocketDecorator
 */
public class DecoratedSocketsSslSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;

    private final SslSocketDecorator socketDecorator;

    public DecoratedSocketsSslSocketFactory(final SSLSocketFactory delegate, SslSocketDecorator socketDecorator) {
        super();

        if (delegate == null) {
            throw new IllegalArgumentException("Parameter delegate must not be null.");
        }
        if (socketDecorator == null) {
            throw new IllegalArgumentException("Parameter socketDecorator must not be null.");
        }

        this.delegate = delegate;
        this.socketDecorator = socketDecorator;
    }

    private void decorate(final SSLSocket sslSocket) {
        socketDecorator.decorate(sslSocket);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket socket = delegate.createSocket();
        decorate((SSLSocket) socket);
        return socket;
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
        Socket socket = delegate.createSocket(host, port);
        decorate((SSLSocket) socket);
        return socket;
    }

    @Override
    public Socket createSocket(final Socket s, final String host, final int port, final boolean autoClose) throws IOException {
        Socket socket = delegate.createSocket(s, host, port, autoClose);
        decorate((SSLSocket) socket);
        return socket;
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort)
            throws IOException, UnknownHostException {
        Socket socket = delegate.createSocket(host, port, localHost, localPort);
        decorate((SSLSocket) socket);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        Socket socket = delegate.createSocket(host, port);
        decorate((SSLSocket) socket);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localAddress, final int localPort)
            throws IOException {
        Socket socket = delegate.createSocket(address, port, localAddress, localPort);
        decorate((SSLSocket) socket);
        return socket;
    }

    /**
     * Decorator of {@code SSLSocket}s for {@code DecoratedSocketsSslSocketFactory}ies.
     * 
     * @see SSLSocket
     * @see DecoratedSocketsSslSocketFactory
     * @see #decorate(SSLSocket)
     */
    public interface SslSocketDecorator {

        /**
         * Decorates the given SSL socket.
         * 
         * @param sslSocket the SSL socket that will be decorated.
         */
        void decorate(SSLSocket sslSocket);
    }
}
