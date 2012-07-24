/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.websocket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.parosproxy.paros.model.HistoryReference;

/**
 * This class represents two WebSocket channels. Code is inspired by the Monsoon
 * project (http://code.google.com/p/monsoon/).
 * 
 * It is not based on Java's NIO features as with Monsoon, as the underlying
 * Paros Proxy is based on Sockets and I got huge problems when adding SSL
 * support when switching from {@link Socket} to {@link SocketChannel} in this
 * class.
 * 
 * Therefore each instance has got two threads that listen on each side for new
 * messages (blocking reads).
 */
public abstract class WebSocketProxy {
	
	private static final Logger logger = Logger.getLogger(WebSocketProxy.class);

	public enum State {  
		CONNECTING, OPEN, CLOSING, CLOSED, // ready state
		EXCLUDED, INCLUDED; // new black- or whitelisted channel
	}
	
	/**
	 * State of this channel, start in CONNECTING state and evolve over time.
	 */
	protected State state;
	
	/**
	 * Timestamp is set when {@link WebSocketProxy} reaches {@link State#OPEN}.
	 */
	protected Timestamp start;

	/**
	 * Timestamp is set when {@link WebSocketProxy} reaches {@link State#CLOSED}.
	 */
	protected Timestamp end;

	/**
	 * Non-finished messages.
	 */
	protected HashMap<InputStream, WebSocketMessage> currentMessages;

	/**
	 * Socket for connection: Browser <-> ZAP
	 */
	protected final Socket localSocket;
	
	/**
	 * Socket for connection: ZAP <-> Server
	 */
	protected final Socket remoteSocket;

	/**
	 * Listens for messages from the server.
	 */
	private WebSocketListener remoteListener;
	
	/**
	 * Listens for messages from the browser.
	 */
	private WebSocketListener localListener;

	/**
	 * List of observers, that are informed of in- or outgoing messages.
	 */
	private Vector<WebSocketObserver> observerList;

	/**
	 * Contains link to handshake message.
	 */
	private HistoryReference handshakeReference;

	/**
	 * Host of remote socket.
	 */
	private final String host;

	/**
	 * Port of remote socket.
	 */
	private final int port;

	/**
	 * Just a consecutive number, identifying one channel within a session.
	 */
	private final int channelId;

	/**
	 * To ease identification of different WebSocket connections.
	 */
	private static AtomicInteger channelCounter;
	
	/**
	 * Add a unique id to each message of one view model.
	 */
	private AtomicInteger messageCounter;

	/**
	 * When true, no observer is called and each frame is forwarded instantly.
	 */
	private boolean isForwardOnly;

	/**
	 * Used to determine how to call WebSocketListener.
	 */
	public static Comparator<WebSocketObserver> observersComparator;
	
	static {
		channelCounter = new AtomicInteger(0);
	}

	/**
	 * Factory method to create appropriate version.
	 * 
	 * @param version Protocol version.
	 * @param localSocket Channel from browser to ZAP.
	 * @param remoteSocket Channel from ZAP to server.
	 * @param subprotocol Provide null if there is no subprotocol specified.
	 * @param extensions Map of negotiated extensions, null or empty list.
	 * @throws WebSocketException
	 * @return Version specific proxy object.
	 */
	public static WebSocketProxy create(String version, Socket localSocket, Socket remoteSocket, String subprotocol, Map<String, String> extensions) throws WebSocketException {
		logger.debug("Create WebSockets proxy for version '" + version + "'.");
		WebSocketProxy wsProxy = null;
		
		// TODO: provide a registry for WebSocketProxy versions
		if (version.equals("13")) {
			wsProxy = new WebSocketProxyV13(localSocket, remoteSocket);
			
			if (subprotocol != null) {
				// TODO: do something with this subprotocol
			}
			
			if (extensions != null && extensions.size() > 0) {
				// TODO: do something with these extensions
			}
		} else {
			throw new WebSocketException("Unsupported Sec-WebSocket-Version '"
					+ version + "' provided in factory method!");
		}
		
		return wsProxy;
	}

	/**
	 * Create a WebSocket on a channel. You need to call
	 * {@link WebSocketProxy#startListeners(ExecutorService, InputStream)} to
	 * turn on this proxy.
	 * 
	 * @param localSocket Channel from local machine to ZAP.
	 * @param remoteSocket Channel from ZAP to remote machine.
	 * @throws WebSocketException
	 */
	public WebSocketProxy(Socket localSocket, Socket remoteSocket) throws WebSocketException {
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;
		
		currentMessages = new HashMap<InputStream, WebSocketMessage>();
		observerList = new Vector<WebSocketObserver>();
		
		// create unique identifier for this WebSocket connection
		channelId = channelCounter.incrementAndGet();
		messageCounter = new AtomicInteger(0);
		host = remoteSocket.getInetAddress().getHostName();
		port = remoteSocket.getPort();
		
		isForwardOnly = false;
	}
	
	protected void setState(State newState) {
		if (state == newState) {
			return;
		}
		
		switch (newState) {
		case OPEN:
			start = new Timestamp(Calendar.getInstance().getTimeInMillis());
			break;
		case CLOSED:
			end = new Timestamp(Calendar.getInstance().getTimeInMillis());
			break;
		}
		
		state = newState;
		
		if (!isForwardOnly) {
			notifyStateObservers(state);
		}
	}

	/**
	 * Registers both channels (local & remote) with the given selector,
	 * that fires on read.
	 * 
	 * @param listenerThreadPool Thread pool is provided by {@link ExtensionWebSocket}.
	 * @param remoteReader This {@link InputStream} that contained the handshake response.
	 * @throws WebSocketException
	 */
	public void startListeners(ExecutorService listenerThreadPool, InputStream remoteReader) throws WebSocketException {
		setState(State.CONNECTING);
		
		// check if both sockets are open, otherwise no need for listening
		if (localSocket.isClosed() || !localSocket.isConnected()) {
			throw new WebSocketException("local socket is closed or not connected");
		}
		
		if (remoteSocket.isClosed() || !remoteSocket.isConnected()) {
			throw new WebSocketException("remote socket is closed or not connected");
		}
		
		// ensure right settings are used for our sockets
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
		
		logger.debug("Start listeners for channel '" + toString() + "'.");
		
		try {
			// use existing InputStream for remote socket,
			// as it may already contain first WebSocket-frames
			remoteListener = createListener(remoteSocket, remoteReader, "remote");
			localListener = createListener(localSocket, "local");
		} catch (WebSocketException e) {
			shutdown();
			throw e;
		}
		
		// need to set State.OPEN before listening to sockets, otherwise
		// it might happen, that observers are notified about a new message
		// before they are informed about a new channel state.
		setState(State.OPEN);
		
		listenerThreadPool.execute(remoteListener);
		listenerThreadPool.execute(localListener);
	}
	
	/**
	 * Create a listener object that encapsulates the input stream
	 * from the given {@link Socket} and the output stream of the
	 * opposite socket connection.
	 * 
	 * @param readEnd {@link Socket} from which is read.
	 * @param reader InputStream from given {@link Socket}.
	 * @param side Used to identify if local or remote.
	 * @return
	 * @throws WebSocketException
	 */
	private WebSocketListener createListener(Socket readEnd, InputStream reader, String side) throws WebSocketException {
		try {
			OutputStream writer = getOppositeSocket(readEnd).getOutputStream();
			String name = "ZAP-WS-Listener (" + side + ") '" + toString() + "'";
			
			return new WebSocketListener(this, reader, writer, name);
		} catch (IOException e) {
			throw new WebSocketException("Failed to start listener due to: " + e.getMessage());
		}
	}

	/**
	 * Create a listener object that encapsulates the input stream
	 * from the given {@link Socket} and the output stream of the
	 * opposite socket connection.
	 * 
	 * @param readEnd {@link Socket} from which is read.
	 * @param side Used to identify if local or remote.
	 * @return
	 * @throws WebSocketException
	 */
	private WebSocketListener createListener(Socket readEnd, String side) throws WebSocketException {
		try {
			InputStream reader = new BufferedInputStream(readEnd.getInputStream());
			
			return createListener(readEnd, reader, side);
		} catch (IOException e) {
			throw new WebSocketException("Failed to start listener due to: " + e.getMessage());
		}
	}

	/**
	 * Stop & close all resources, i.e.: threads, streams & sockets
	 */
	public void shutdown() {
		setState(State.CLOSING);
		
		int closedCount = 0;
		
		if (localListener != null && !localListener.isFinished()) {
			localListener.stop();
		} else {
			closedCount++;
		}
		
		if (remoteListener != null && !remoteListener.isFinished()) {
			remoteListener.stop();
		} else {
			closedCount++;
		}

		// after stopping any listener, that was still running this method
		// will be called again by him, which ensures a closedCount of 2
		// and subsequent closing sockets
		if (closedCount == 2) {
			logger.debug("close WebSockets");
			
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
			
			setState(State.CLOSED);
		}
	}

	/**
	 * Returns true if this proxies' state is {@link State#OPEN}.
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if (state.equals(State.OPEN)) {
			return true;
		}
		return false;
	}

	/**
	 * Read one frame from given input stream
	 * and forward it to given output stream.
	 * 
	 * @param in Here comes the frame.
	 * @param out There should it be forwarded.
	 * @param frameHeader The first byte of the frame, that was already read.
	 * @throws IOException
	 */
	public void processRead(InputStream in, OutputStream out, byte frameHeader) throws IOException {
		WebSocketMessage message = null;
	
		int opcode = (frameHeader & 0x0F); // last 4 bits represent opcode
		String readableOpcode = WebSocketMessage.opcode2string(opcode);
		
		logger.debug("Process WebSocket frame: " + opcode + " (" + readableOpcode + ")");
		
		if (WebSocketMessage.isControl(opcode)) {
			// control messages may interrupt non-control messages
			// control messages are ALWAYS just one frame long
			message = createWebSocketMessage(in, frameHeader);
		} else {
			// non-control messages may be split across several frames
			
			// it may happen, that a continuation frame is coming along,
			// without a previous frame to continue.

			// assume that there is only one message to be continued
			
			boolean shouldContinueMessage = currentMessages.containsKey(in);
			if (opcode == WebSocketMessage.OPCODE_CONTINUATION) {
				if (shouldContinueMessage) {
					// continue temporarily buffered message
					message = currentMessages.remove(in);
					message.readContinuation(in, frameHeader);
				} else {
					// no message here that can be continued
					// => forward in any case, as the endpoint has to close
					// the connection immediately
					logger.warn("Got continuation frame, but there is no message to continue - forward frame in any case!");
					
					message = createWebSocketMessage(in, frameHeader);
					if (!isForwardOnly) {
						if (!notifyMessageObservers(message)) {
							logger.warn("Ignore observer's wish to skip forwarding as we have received an invalid frame!");
						}
					}
					message.forward(out);
					
					return;
				}
			} else {
				// another non-control frame
				message = createWebSocketMessage(in, frameHeader);
			}
			
			if (!message.isFinished()) {
				// temporarily buffer unfinished message
				currentMessages.put(in, message);
			}
		}
		
		// do not buffer frames until message is finished,
		// as messages might have several MegaBytes!
		if (isForwardOnly || notifyMessageObservers(message)) {
			// skip forwarding only if observer told us to skip this message (frame)
			message.forward(out);
		}	
	}

	/**
	 * Returns a version specific WebSockets message.
	 * 
	 * @param in {@link InputStream} to read bytes from.
	 * @param frameHeader First byte of frame, containing FIN flag and opcode.
	 * @return
	 * @throws IOException 
	 */
	protected abstract WebSocketMessage createWebSocketMessage(InputStream in, byte frameHeader) throws IOException;

	/**
	 * Returns the opposed socket.
	 * 
	 * @param socket
	 * @return
	 */
	protected Socket getOppositeSocket(Socket socket) {
		if (socket == localSocket) {
			return remoteSocket;
		} else {
			return localSocket;
		}
	}

	/**
	 * If true, then no observer is called, resulting in immediate forwarding.
	 * 
	 * @return
	 */
	public boolean isForwardOnly() {
		return isForwardOnly;
	}

	/**
	 * If true, then no observer is called, resulting in immediate forwarding.
	 * 
	 * @param shouldBeForwardOnly
	 */
	public void setForwardOnly(boolean shouldBeForwardOnly) {
		if (isForwardOnly == shouldBeForwardOnly) {
			// nothing changed
			return;
		}
		
		if (isForwardOnly && !shouldBeForwardOnly) {
			// formerly channel was ignored - maybe the whole time
			// be sure that observers got to know this channel
			logger.info(toString() + " is re-included in storage & UI!");
			
			// set to false first, otherwise no notification is sent
			isForwardOnly = false;
			setState(State.INCLUDED);
		} else if (!isForwardOnly && shouldBeForwardOnly) {
			// current channel is not tracked in future
			logger.info(toString() + " is excluded from storage & UI!");
			
			// set to true afterwards, otherwise no notification is sent
			setState(State.EXCLUDED);
			isForwardOnly = true;
		}
	}
	
	/**
	 * Call each observer. If one observer has told us to drop the message, then
	 * they skip further notifications and return false.
	 * <p>
	 * Call this helper only when {@link WebSocketProxy#isForwardOnly} is set to
	 * false.
	 * 
	 * @param message
	 * @return
	 */
	protected boolean notifyMessageObservers(WebSocketMessage message) {		
		synchronized (observerList) {
			for (WebSocketObserver observer : observerList) {
				try {
				    if (!observer.onMessageFrame(channelId, message)) {
				    	return false;
				    }
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
		return true;
	}

	/**
	 * Helper to inform about new {@link WebSocketProxy#state}. Also called when
	 * a former {@link WebSocketProxy#isForwardOnly} channel is no longer
	 * blacklisted.
	 */
	protected void notifyStateObservers(State state) {
		synchronized (observerList) {
			for (WebSocketObserver observer : observerList) {
				observer.onStateChange(state, this);
			}
		}
	}
	
	/**
	 * Add observer that gets informed about in- & outgoing messages.
	 * 
	 * @param observer
	 */
	public void addObserver(WebSocketObserver observer) {
		synchronized (observerList) {
			observerList.add(observer);
			Collections.sort(observerList, getObserversComparator());
		}
	}
	
	/**
	 * Stop getting informed about in- & outgoing messages.
	 * 
	 * @param observer
	 */
	public void removeObserver(WebSocketObserver observer) {
		synchronized (observerList) {
			observerList.remove(observer);
		}
	}
    
	/**
	 * Returns the comparator used for determining order of notification.
	 * 
	 * @return
	 */
	private synchronized Comparator<WebSocketObserver> getObserversComparator() {
		if(observersComparator == null) {
			observersComparator = new Comparator<WebSocketObserver>() {
				
				@Override
				public int compare(WebSocketObserver o1, WebSocketObserver o2) {
					int order1 = o1.getObservingOrder();
					int order2 = o2.getObservingOrder();
					
					if (order1 < order2) {
						return -1;
					} else if (order1 > order2) {
						return 1;
					}
					
					return 0;
				}
			};
		}
		
		return observersComparator;
	}

	public int getChannelId() {
		return channelId;
	}
	
	public int getIncrementedMessageCount() {
		return messageCounter.incrementAndGet();
	}
	
	public HistoryReference getHandshakeReference() {
		return handshakeReference;
	}
	
	public void setHandshakeReference(HistoryReference handshakeReference) {
		this.handshakeReference = handshakeReference;
	}

	public WebSocketChannelDAO getDAO() {
		WebSocketChannelDAO dao = new WebSocketChannelDAO();
		dao.channelId = getChannelId();
		dao.host = host;
		dao.port = port;
		dao.startTimestamp = (start != null) ? start.getTime() : null;
		dao.endTimestamp = (end != null) ? end.getTime() : null;
		
		HistoryReference handshakeRef = getHandshakeReference();
		dao.historyId = (handshakeRef != null) ? handshakeRef.getHistoryId() : null;
		
		return dao;
	}
	
	public String toString() {
		return host + ":" + port + " (#" + channelId + ")";
	}
}