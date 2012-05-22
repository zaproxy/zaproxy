package org.zaproxy.zap.extension.websocket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This class represents two WebSocket channels.
 * A lot of code is reused from the Monsoon project at http://code.google.com/p/monsoon/.
 * It is built around Java's NIO features, which takes advantage of non-blocking I/O facilities.
 */
public abstract class WebSocketProxy {
	
	private static Logger logger = Logger.getLogger(WebSocketProxy.class);

	public enum State { // ready state
		CONNECTING, OPEN, CLOSING, CLOSED;
	}
	
	/**
	 * Start in CONNECTING state and evolve over time.
	 */
	protected State state = State.CONNECTING;

	/**
	 * Non-finished messages.
	 */
	protected HashMap<InputStream, WebSocketMessage> currentMessages = new HashMap<InputStream, WebSocketMessage>();

	protected final Socket localSocket;
	protected final Socket remoteSocket;

	private Thread localListenerThread;
	private Thread remoteListenerThread;

	/**
	 * This name is used for debugging messages.
	 * It helps to distinguish between several WebSocket connections.
	 */
	private String name;

	/**
	 * Factory method to create appropriate version.
	 * 
	 * @param version Protocol version. Try 13 if you do not know.
	 * @param localSocket Channel from browser to ZAP.
	 * @param remoteSocket Channel from ZAP to Internet.
	 * @param subprotocol Provide null if there is no subprotocol specified.
	 * @param extensions Map of negotiated extensions, null or empty.
	 * @throws WebSocketException
	 * @return Version specific proxy object.
	 */
	public static WebSocketProxy create(String version,
			Socket localSocket, Socket remoteSocket,
			String subprotocol, Map<String, String> extensions) throws WebSocketException {
		logger.debug("Create WebSockets proxy for version '" + version + "'.");
		WebSocketProxy wsProxy = null;
		
		if (version.equals("13")) {
			wsProxy = new WebSocketProxyV13(localSocket, remoteSocket);
			
			if (subprotocol != null) {
				// TODO: do something with that subprotocol
			}
			
			if (extensions != null && extensions.size() > 0) {
				// TODO: do something with that extensions
			}
		} else {
			throw new WebSocketException("Invalid version '" + version + "' provided in factory method!");
		}
		
		return wsProxy;
	}

	/**
	 * Create a WebSocket on a channel.
	 * 
	 * @param selector This selector is used outside for reading several channels.
	 * @param localSocket Channel from local machine to ZAP.
	 * @param remoteSocket Channel from ZAP to remote machine.
	 * @throws WebSocketException 
	 */
	public WebSocketProxy(Socket localSocket, Socket remoteSocket) throws WebSocketException {
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;
		
		this.name = remoteSocket.getInetAddress().getHostName() + "/" + remoteSocket.getPort();
	}
	
	/**
	 * Registers both channels (local & remote) with the given selector,
	 * that fires on read.
	 * 
	 * @param selector
	 * @throws WebSocketException
	 */
	public void startListeners() throws WebSocketException {
		if (localSocket.isClosed()) {
			throw new WebSocketException("local socket is closed");
		}
		
		if (remoteSocket.isClosed()) {
			throw new WebSocketException("remote socket is closed");
		}
		
		try {
			localSocket.setSoTimeout(0); // infinite timeout
			localSocket.setTcpNoDelay(true);
			localSocket.setKeepAlive(true);
			
			remoteSocket.setSoTimeout(0);
			remoteSocket.setTcpNoDelay(true);
			remoteSocket.setKeepAlive(true);
		} catch (SocketException e) {
			throw new WebSocketException(e);
		}
		
		if (localSocket.isClosed() || !localSocket.isConnected()) {
			logger.debug("closed or not connected");
		}
		
		if (remoteSocket.isClosed() || !remoteSocket.isConnected()) {
			logger.debug("closed or not connected");
		}
		logger.debug("Start listeners for channel '" + name + "'.");

		WebSocketListener remoteListener = createListener(remoteSocket);
		remoteListenerThread = new Thread(remoteListener, "WebSocket-'" + name + "'-remote");
		remoteListenerThread.start();

		WebSocketListener localListener = createListener(localSocket);
		localListenerThread = new Thread(localListener, "WebSocket-'" + name + "'-local");
		localListenerThread.start();
	}
	
	public void shutdown() {
		localListenerThread.interrupt();
		remoteListenerThread.interrupt();
		
		try {
			localSocket.close();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
		
		try {
			remoteSocket.close();
		} catch (IOException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * Configure the given channel to be non-blocking and
	 * register for read events.
	 * 
	 * @param selector
	 * @param channel
	 * @throws WebSocketException
	 */
	private WebSocketListener createListener(Socket readEnd) throws WebSocketException {
		try {
			Socket writeEnd = getOppositeSocket(readEnd);
			
			BufferedInputStream reader = new BufferedInputStream(readEnd.getInputStream());
			
			return new WebSocketListener(this, reader, writeEnd.getOutputStream());
		} catch (IOException e) {
			throw new WebSocketException("Failed to start listener due to: " + e.getMessage());
		}
	}

	/**
	 * Read one or more frames from given channel.
	 * 
	 * @param in Here comes the frame.
	 * @param out There should it be forwarded.
	 * @param flagsByte The first byte of the frame, that was already read.
	 * @throws IOException
	 */
	public void processRead(InputStream in, OutputStream out, byte flagsByte) throws IOException {
		WebSocketMessage message = null;
	
		int opcode = (flagsByte & 0x0F); // last 4 bits represent opcode
		
		logger.debug("Got WebSockets-frame: " + opcode + " from connection '" + name + "'");
		
		if (WebSocketMessage.isControl(opcode)) {
			/*
			 * control messages may interrupt a control message
			 * control message are always just one frame long
			 */
			message = createWebSocketMessage(in, flagsByte);
			message.forward(out);
		} else {
			/*
			 * non-control messages may be split across several frames
			 * temporarily buffer non-finished messages
			 */
			if (opcode == WebSocketMessage.OPCODE_CONTINUATION) {
				message = currentMessages.remove(in);
				message.readContinuation(in, flagsByte);
			} else {
				message = createWebSocketMessage(in, flagsByte);
			}
			
			if (message.isFinished()) {
				message.forward(out);
			} else {
				currentMessages.put(in, message);
			}
		}
	}

	/**
	 * Returns a version specific WebSockets message.
	 * 
	 * @param in First byte of frame, containing FIN flag and opcode.
	 * @param flagsByte
	 * @return
	 * @throws IOException 
	 */
	protected abstract WebSocketMessage createWebSocketMessage(InputStream in, byte flagsByte) throws IOException;

	/**
	 * Returns the opposed channel.
	 * 
	 * @param socket
	 * @return
	 */
	protected Socket getOppositeSocket(Socket socket) {
		if (socket.equals(localSocket)) {
			return remoteSocket;
		} else {
			return localSocket;
		}
	}
}
