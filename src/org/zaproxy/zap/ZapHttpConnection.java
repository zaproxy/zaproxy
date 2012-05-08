package org.zaproxy.zap;

import java.nio.channels.SocketChannel;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.parosproxy.paros.network.HttpSender;

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
	 * Returns the channel of this connection's socket.
	 * Requires a custom ProtocolSocketFactory that creates
	 * the socket via <code>SocketChannel.open()</code>.
	 * <p>
	 * You can use either {@link ZapDefaultProtocolSocketFactory} or
	 * {@link ZapSecureProtocolSocketFactory}. You can set them in the
	 * static initializer of the {@link HttpSender} class.
	 * 
	 * @return Outgoing blocking channel.
	 */
	public SocketChannel getSocketChannel() {
		return getSocket().getChannel();
	}
}
