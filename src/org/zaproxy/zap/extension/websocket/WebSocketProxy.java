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
import java.sql.SQLException;
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
import org.parosproxy.paros.network.HttpMalformedHeaderException;

/**
 * Intercepts WebSocket communication and forwards frames. Code is inspired by
 * the <a href="http://code.google.com/p/monsoon/">Monsoon</a> project.
 * <p>
 * It is not based on Java's NIO features as with Monsoon, as the underlying
 * Paros Proxy is based on Sockets and I got huge problems when adding SSL
 * support when switching from Sockets to SocketChannels in this class.
 * Therefore each instance has got two threads that listen on each side for new
 * messages (these are blocking reads).
 * <p>
 * Is able to act as WebSocket client (i.e.: the WebSocket connection is set-up
 * between ZAP and the server, without the browser).
 */
public abstract class WebSocketProxy {
	
	private static final Logger logger = Logger.getLogger(WebSocketProxy.class);

	/**
	 * WebSocket communication state.
	 */
	public enum State {  
		CONNECTING, OPEN, CLOSING, CLOSED, // ready state
		EXCLUDED, INCLUDED; // no WebSocket state, used for new black- or whitelisted channels
	}
	
	/**
	 * To ease identification of different WebSocket connections.
	 */
	private static AtomicInteger channelIdGenerator = new AtomicInteger(0);

	/**
	 * Used to determine when to call which {@link WebSocketObserver}.
	 */
	private static Comparator<WebSocketObserver> observersComparator;
	
	/**
	 * State of this channel, start in {@link State#CONNECTING} and evolve over
	 * time. Never set value to {@link State#EXCLUDED} or {@link State#INCLUDED}
	 * . While observers are notified of these two extra-states, the internal
	 * state is never set to one of these two values.
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
	 * Non-finished messages are temporarily buffered. WebSocket messages are
	 * allowed to consist of an arbitrary number of frames.
	 */
	protected Map<InputStream, WebSocketMessage> unfinishedMessages;

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
	 * Add a unique id to each message of one view model.
	 */
	private AtomicInteger messageIdGenerator;

	/**
	 * When true, no observer is called and each frame is forwarded instantly.
	 */
	private boolean isForwardOnly;
	
	/**
	 * In client mode there is no connection Browser <-> ZAP, but only ZAP <-> Server.
	 */
	private boolean isClientMode;
	
	/**
	 * After loading another session, the channelCount should be initialized.
	 * 
	 * @param currentChannelCount
	 */
	static void setChannelIdGenerator(int currentChannelCount) {
		channelIdGenerator.set(currentChannelCount);
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
	 */
	public WebSocketProxy(Socket localSocket, Socket remoteSocket) {
		if (localSocket == null) {
			isClientMode = true;
		} else {
			isClientMode = false;
		}
		
		this.localSocket = localSocket;
		this.remoteSocket = remoteSocket;
		
		unfinishedMessages = new HashMap<>();
		observerList = new Vector<>();
		
		// create unique identifier for this WebSocket connection
		channelId = channelIdGenerator.incrementAndGet();
		messageIdGenerator = new AtomicInteger(0);
		host = remoteSocket.getInetAddress().getHostName();
		port = remoteSocket.getPort();
		
		isForwardOnly = false;
	}
	
	/**
	 * {@link State#EXCLUDED} and {@link State#INCLUDED} are never set as status,
	 * but are used to inform observers.
	 *  
	 * @param newState
	 */
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
		default:
		}
		
		state = newState;
		
		if (!isForwardOnly) {
			notifyStateObservers(state);
		}
	}

	/**
	 * Start listening to the WebSocket-connection using threads from the given
	 * pool. Read also buffered bytes from incoming connection (Server -> ZAP).
	 * 
	 * @param listenerThreadPool Thread pool is provided by {@link ExtensionWebSocket}.
	 * @param remoteReader This {@link InputStream} that contained the handshake response.
	 * @throws WebSocketException
	 */
	public void startListeners(ExecutorService listenerThreadPool, InputStream remoteReader) throws WebSocketException {
		setState(State.CONNECTING);
		
		// check if both sockets are open, otherwise no need for listening
		if (localSocket != null && (localSocket.isClosed() || !localSocket.isConnected())) {
			throw new WebSocketException("local socket is closed or not connected");
		}
		
		if (remoteSocket.isClosed() || !remoteSocket.isConnected()) {
			throw new WebSocketException("remote socket is closed or not connected");
		}
		
		// ensure right settings are used for our sockets
		try {
			if (localSocket != null) {
				localSocket.setSoTimeout(0); // infinite timeout
				localSocket.setTcpNoDelay(true);
				localSocket.setKeepAlive(true);
			}
			
			remoteSocket.setSoTimeout(0);
			remoteSocket.setTcpNoDelay(true);
			remoteSocket.setKeepAlive(true);
		} catch (SocketException e) {
			throw new WebSocketException(e);
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Start listeners for channel '" + toString() + "'.");
		}
		
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
			OutputStream writer = null;
			Socket writeSocket = getOppositeSocket(readEnd);
			if (writeSocket != null) {
				writer = writeSocket.getOutputStream();
			}

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
			InputStream reader = null;
			if (readEnd != null) {
				reader = new BufferedInputStream(readEnd.getInputStream());
			}
			
			return createListener(readEnd, reader, side);
		} catch (IOException e) {
			throw new WebSocketException("Failed to start listener due to: " + e.getMessage());
		}
	}

	/**
	 * Stop listening & close all resources, i.e.: threads, streams & sockets
	 */
	public void shutdown() {
		if (isClientMode && localListener.isFinished() && !remoteListener.isFinished()) {
			// in client mode closing shutdown should be prevented
			return;
		}
		
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
				if (localSocket != null) {
					localSocket.close();
				}
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
	 * @return True if proxy's state is {@link State#OPEN}.
	 */
	public boolean isConnected() {
		if (state != null && state.equals(State.OPEN)) {
			return true;
		}
		return false;
	}

	/**
	 * Read one frame from given input stream and forward it to given output
	 * stream, if forwarding is allowed by WebSocket-observers.
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
			
			boolean shouldContinueMessage = unfinishedMessages.containsKey(in);
			if (opcode == WebSocketMessage.OPCODE_CONTINUATION) {
				if (shouldContinueMessage) {
					// continue temporarily buffered message
					message = unfinishedMessages.remove(in);
					message.readContinuation(in, frameHeader);
				} else {
					// no message here that can be continued
					handleInvalidContinuation(in, out, frameHeader);					
					return;
				}
			} else {
				// another non-control frame
				message = createWebSocketMessage(in, frameHeader);
			}
			
			if (!message.isFinished()) {
				// temporarily buffer unfinished message
				unfinishedMessages.put(in, message);
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
	 * Invalid frame given, forward it in any case, as the endpoint is required
	 * to close the connection immediately.
	 * 
	 * @param frameHeader
	 * @param in
	 * @param out
	 * @throws IOException
	 */
	private void handleInvalidContinuation(InputStream in, OutputStream out, byte frameHeader) throws IOException {
		logger.warn("Got continuation frame, but there is no message to continue - forward frame in any case!");
		
		WebSocketMessage message = createWebSocketMessage(in, frameHeader);
		if (!isForwardOnly) {
			if (!notifyMessageObservers(message)) {
				logger.warn("Ignore observer's wish to skip forwarding as we have received an invalid frame!");
			}
		}
		message.forward(out);
	}

	/**
	 * @param in Read bytes from here.
	 * @param frameHeader First byte of frame, containing FIN flag and opcode.
	 * @return version specific WebSocket message
	 * @throws IOException 
	 */
	protected abstract WebSocketMessage createWebSocketMessage(InputStream in, byte frameHeader) throws IOException;

	/**
	 * @param message Contains content to be used to create {@link WebSocketMessage}.
	 * @return version specific WebSocket message, that is build upon given base-DTO
	 * @throws WebSocketException
	 */
	protected abstract WebSocketMessage createWebSocketMessage(WebSocketMessageDTO message) throws WebSocketException;

	/**
	 * @param socket
	 * @return opposed socket
	 */
	protected Socket getOppositeSocket(Socket socket) {
		Socket oppositeSocket;
		if (socket == localSocket) {
			oppositeSocket = remoteSocket;
		} else {
			oppositeSocket = localSocket;
		}
		return oppositeSocket;
	}

	/**
	 * If true, then no observer is called, resulting in immediate forwarding.
	 * 
	 * @return Nothing will be stored if true.
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
			
			isForwardOnly = false;
			notifyStateObservers(State.INCLUDED);
		} else if (!isForwardOnly && shouldBeForwardOnly) {
			// current channel is not tracked in future
			logger.info(toString() + " is excluded from storage & UI!");

			isForwardOnly = true;
			notifyStateObservers(State.EXCLUDED);
		}
	}
	
	/**
	 * Call each observer as long as no observer has told us to drop the message. Then
	 * further notifications are skipped and false is returned.
	 * <p>
	 * Call this helper only when {@link WebSocketProxy#isForwardOnly} is set to
	 * false.
	 * 
	 * @param message
	 * @return False if message should be dropped.
	 */
	protected boolean notifyMessageObservers(WebSocketMessage message) {
		for (WebSocketObserver observer : observerList) {
			try {
			    if (!observer.onMessageFrame(channelId, message)) {
			    	return false;
			    }
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return true;
	}

	/**
	 * Helper to inform about new {@link WebSocketProxy#state}. Also called when
	 * a former {@link WebSocketProxy#isForwardOnly} channel is no longer
	 * blacklisted {@link State#INCLUDED} or vice-versa {@link State#EXCLUDED}.
	 */
	protected void notifyStateObservers(State state) {
		for (WebSocketObserver observer : observerList) {
			observer.onStateChange(state, this);
		}
	}
	
	/**
	 * Add observer that gets informed about in- & outgoing messages.
	 * 
	 * @param observer
	 */
	public void addObserver(WebSocketObserver observer) {
		observerList.add(observer);
		Collections.sort(observerList, getObserversComparator());
	}
	
	/**
	 * Stop getting informed about in- & outgoing messages.
	 * 
	 * @param observer
	 */
	public void removeObserver(WebSocketObserver observer) {
		observerList.remove(observer);
	}
    
	/**
	 * Returns the comparator used for determining order of notification.
	 * 
	 * @return
	 */
	private static Comparator<WebSocketObserver> getObserversComparator() {
		if(observersComparator == null) {
			createObserversComparator();
		}
		
		return observersComparator;
	}
	
	private static synchronized void createObserversComparator() {
		if (observersComparator == null) {
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
	}

	public int getChannelId() {
		return channelId;
	}
	
	public int getIncrementedMessageCount() {
		return messageIdGenerator.incrementAndGet();
	}
	
	public HistoryReference getHandshakeReference() {
		return handshakeReference;
	}
	
	public void setHandshakeReference(HistoryReference handshakeReference) {
		this.handshakeReference = handshakeReference;
	}

	public WebSocketChannelDTO getDTO() {
		WebSocketChannelDTO dto = new WebSocketChannelDTO();
		dto.id = getChannelId();
		dto.host = host;
		dto.port = port;
		dto.startTimestamp = (start != null) ? start.getTime() : null;
		dto.endTimestamp = (end != null) ? end.getTime() : null;
		
		HistoryReference handshakeRef = getHandshakeReference();
		if (handshakeRef != null) {
			try {
				dto.url = handshakeRef.getHttpMessage().getRequestHeader().getURI().toString();
			} catch (HttpMalformedHeaderException e) {
				dto.url = "";
				logger.error("HttpMessage for WebSockets-handshake not found!");
			} catch (SQLException e) {
				dto.url = "";
				logger.error("HttpMessage for WebSockets-handshake not found!");
			}
			dto.historyId = handshakeRef.getHistoryId();
		} else {
			dto.url = "";
			dto.historyId = null;
		}
		
		return dto;
	}
	
	@Override
	public String toString() {
		return host + ":" + port + " (#" + channelId + ")";
	}

	/**
	 * Sends a custom message and informs {@link WebSocketObserver} instances of
	 * this new message.
	 * 
	 * @param msg
	 * @throws IOException
	 */
	public void sendAndNotify(WebSocketMessageDTO msg) throws IOException {
		logger.info("send custom message");
		WebSocketMessage message = createWebSocketMessage(msg);
		
		OutputStream out;
		if (msg.isOutgoing) {
			// an outgoing message is caught by the local listener
			// and forwarded to its output stream
			out = localListener.getOutputStream();
		} else {
			// an incoming message is caught by the remote listener
			out = remoteListener.getOutputStream();
		}
	
		if (message.forward(out)) {
			notifyMessageObservers(message);
		}
	}

	public boolean isClientMode() {
		return isClientMode;
	}
}