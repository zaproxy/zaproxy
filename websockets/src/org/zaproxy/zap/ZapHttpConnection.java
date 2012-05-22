package org.zaproxy.zap;

import java.net.Socket;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;

public class ZapHttpConnection extends HttpConnection {

    /**
     * Creates a new HTTP connection for the given host configuration.
     * 
     * @param hostConfiguration the host/proxy/protocol to use
     */
	public ZapHttpConnection(HostConfiguration hostConfiguration) {
		super(hostConfiguration);
	}
	
	/**
	 * Returns the socket of this connection object.
	 * Make socket available, as parent getSocket() is protected.
	 * 
	 * @return Outgoing (remote) socket connection.
	 */
	public Socket getSocket() {
		return super.getSocket();
	}
	
	/**
     * Avoid closing in- & output stream as that would close the 
     * underlying socket also. But we have to keep it for our
     * WebSocket connection.
     */
    protected void closeSocketAndStreams() {
    	// do not close anything
    }
}
