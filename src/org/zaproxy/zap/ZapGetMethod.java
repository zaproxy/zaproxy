package org.zaproxy.zap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZapGetMethod extends GetMethod implements HttpMethod
{
    /**
     * Log object for this class.
     */
    private static final Log LOG = LogFactory.getLog(ZapGetMethod.class);
    
    /**
     * If we have got an <em>Connection: Upgrade</em>,
     * this will be set to the current connection.
     */
	private Socket upgradedSocket;

	private InputStream inputStream;
    
	/**
	 * Constructor.
	 */
	public ZapGetMethod() {
		super();
	}
    
	/**
	 * Constructor.
	 * 
	 * @param uriSocketChannel
	 */
	public ZapGetMethod(String uri) {
		super(uri);
	}

	/**
	 * Allow response code 101, that is Switching Protocols.
	 * 
     * @see GetMethod#readResponse(HttpState, HttpConnection)
     */
	@Override
    protected void readResponse(HttpState state, HttpConnection conn)
    throws IOException, HttpException {
        LOG.trace("enter HttpMethodBase.readResponse(HttpState, HttpConnection)");
        
        boolean isUpgrade = false;
        
		while (getStatusLine() == null) {
            readStatusLine(state, conn);
            processStatusLine(state, conn);
            readResponseHeaders(state, conn);
            processResponseHeaders(state, conn);
            
            int status = this.statusLine.getStatusCode();
            if (status == 101) {
            	LOG.debug("Retrieved HTTP status code '101 Switching Protocols'. Keep connection open!");

            	// This means the requester has asked the server to switch protocols
            	// and the server is acknowledging that it will do so
            	// e.g.: upgrade to websocket
            	
				if (conn instanceof ZapHttpConnection) {
	            	isUpgrade = true;
	            	
					ZapHttpConnection zapConn = (ZapHttpConnection) conn;
	            	upgradedSocket = zapConn.getSocket();
					inputStream = zapConn.getResponseInputStream();
					
	            	// avoid releasing connection
	            	conn.setHttpConnectionManager(null);
	            }
            } else if ((status >= 100) && (status < 200)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Discarding unexpected response: " + this.statusLine.toString()); 
                }
                this.statusLine = null;
            }
        }
		
		if (!isUpgrade) {
			// read & process rest of response
			// only if connection should not be kept
	        readResponseBody(state, conn);        
	        processResponseBody(state, conn);
		}
    }

    /**
     * If this response included the header <em>Connection: Upgrade</em>,
     * then this method provides the corresponding connection.
     * 
     * @return Upgraded Socket/SocketChannel or null
     */
	public Socket getUpgradedConnection() {
		return upgradedSocket;
	}

    /**
     * If this response included the header <em>Connection: Upgrade</em>,
     * then this method provides the corresponding input stream.
     * 
     * It might happen, that first WebSocket frames are sent directly after
     * the WebSocket handshake response. In this case the frames are buffered
     * in that stream.
     * 
     * @return {@link BufferedInputStream} from response or null
     */
	public InputStream getUpgradedInputStream() {
		return inputStream;
	}
}
