package org.zaproxy.zap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import org.apache.commons.httpclient.protocol.DefaultProtocolSocketFactory;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

public class ZapDefaultProtocolSocketFactory extends DefaultProtocolSocketFactory {
	/**
     * @see #createSocket(java.lang.String,int,java.net.InetAddress,int)
     */
    public Socket createSocket(
        String host,
        int port,
        InetAddress localAddress,
        int localPort
    ) throws IOException, UnknownHostException {
    	// do not create "new Socket()", as it would not allow us
    	// to switch to channel processing later on
    	// blocking mode is on per default
		SocketChannel sc = SocketChannel.open();
		
		Socket s = sc.socket();
		
		// bind to local address
		s.bind(new InetSocketAddress(localAddress, localPort));
		
		// connect to remote address
		s.connect(new InetSocketAddress(host, port));

		return s;
    }

    /**
     * @see ProtocolSocketFactory#createSocket(java.lang.String,int)
     */
    public Socket createSocket(String host, int port)
        throws IOException, UnknownHostException {
    	// do not create "new Socket()", as it would not allow us
    	// to switch to channel processing later on
    	// blocking mode is on per default
		SocketChannel sc = SocketChannel.open();
		
		Socket s = sc.socket();
		
		// connect to remote address
		s.connect(new InetSocketAddress(host, port));

		return s;
    }
}
