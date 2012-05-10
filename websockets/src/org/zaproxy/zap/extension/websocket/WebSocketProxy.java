package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
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
	protected HashMap<SocketChannel, WebSocketMessage> currentMessages = new HashMap<SocketChannel, WebSocketMessage>();

	protected final SocketChannel localChannel;
	protected final SocketChannel remoteChannel;

	/**
	 * Factory method to create appropriate version.
	 * 
	 * @param version Protocol version. Try 13 if you do not know.
	 * @param localChannel Channel from browser to ZAP.
	 * @param remoteChannel Channel from ZAP to Internet.
	 * @param subprotocol Provide null if there is no subprotocol specified.
	 * @param extensions Map of negotiated extensions, null or empty.
	 * @throws WebSocketException
	 * @return Version specific proxy object.
	 */
	public static WebSocketProxy create(String version,
			SocketChannel localChannel, SocketChannel remoteChannel,
			String subprotocol, Map<String, String> extensions) throws WebSocketException {
		logger.debug("Create WebSockets proxy for version '" + version + "'.");
		WebSocketProxy wsProxy = null;
		
		if (version.equals("13")) {
			wsProxy = new WebSocketProxyV13(localChannel, remoteChannel);
			
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
	 * @param localChannel Channel from local machine to ZAP.
	 * @param remoteChannel Channel from ZAP to remote machine.
	 * @throws WebSocketException 
	 */
	public WebSocketProxy(SocketChannel localChannel, SocketChannel remoteChannel) throws WebSocketException {
		this.localChannel = localChannel;
		this.remoteChannel = remoteChannel;
	}
	
	/**
	 * Registers both channels (local & remote) with the given selector,
	 * that fires on read.
	 * 
	 * @param selector
	 * @throws WebSocketException
	 */
	public void register(Selector selector) throws WebSocketException {
		logger.debug("Register this WebSocket-proxy on given selector.");
		prepareChannel(selector, localChannel);
		prepareChannel(selector, remoteChannel);
	}

	/**
	 * Configure the given channel to be non-blocking and
	 * register for read events.
	 * 
	 * @param selector
	 * @param channel
	 * @throws WebSocketException
	 */
	private void prepareChannel(Selector selector, SocketChannel channel) throws WebSocketException {
		try {
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_READ, this);
		} catch (ClosedChannelException e) {
			throw new WebSocketException("Preparing channel failed due to ClosedChannelException!");
		} catch (IOException e) {
			throw new WebSocketException("Preparing channel failed due to IOException!");
		}
	}

	/**
	 * Read one or more frames from given channel.
	 * 
	 * @param key This key contains the channel top be read.
	 * @throws IOException
	 */
	public void processRead(SelectionKey key) throws IOException {
		SocketChannel channel = getChannelFromKey(key);
		if (channel == null) {
			return;
		}
		
		ByteBuffer firstFrameByte = ByteBuffer.allocate(1);
		WebSocketMessage message = null;
		
		while (0 < channel.read(firstFrameByte)) {
			firstFrameByte.flip(); // make buffer readable
	
			byte flagsByte = firstFrameByte.get();
			int opcode = (flagsByte & 0x0F); // last 4 bits is OpCode
			
			logger.debug("Got WebSockets-frame: " + opcode);
			
			if (WebSocketMessage.isControl(opcode)) {
				/*
				 * control messages may interrupt a control message
				 * control message are always just one frame long
				 */
				message = createWebSocketMessage(flagsByte, channel);
				message.forward(getOppositeChannel(channel));
			} else {
				/*
				 * non-control messages may be split across several frames
				 * temporarily buffer non-finished messages
				 */
				if (opcode == WebSocketMessage.OPCODE_CONTINUATION) {
					message = currentMessages.remove(channel);
					message.readContinuation(flagsByte, channel);
				} else {
					message = createWebSocketMessage(flagsByte, channel);
				}
				
				if (message.isFinished()) {
					message.forward(getOppositeChannel(channel));
				} else {
					currentMessages.put(channel, message);
				}
			}
			
			firstFrameByte.clear();
		}
	}

	/**
	 * Returns a version specific WebSockets message.
	 * 
	 * @param flagsByte First byte of frame, containing FIN flag and opcode.
	 * @param channel
	 * @return
	 * @throws IOException 
	 */
	protected abstract WebSocketMessage createWebSocketMessage(byte flagsByte, SocketChannel channel) throws IOException;

	/**
	 * Returns the opposed channel.
	 * 
	 * @param channel
	 * @return
	 */
	protected SocketChannel getOppositeChannel(SocketChannel channel) {
		if (channel.equals(localChannel)) {
			return remoteChannel;
		} else {
			return localChannel;
		}
	}

	/**
	 * Retrieves the {@link SocketChannel} from the {@link SelectionKey}.
	 * 
	 * @param key
	 * @return
	 */
	protected SocketChannel getChannelFromKey(SelectionKey key) {
		SelectableChannel channel = key.channel();
		
		if (channel.equals(localChannel)) {
			return localChannel;
		} else if (channel.equals(remoteChannel)) {
			return remoteChannel;
		} else {
			logger.error("SelectableChannel is not contained in this WebSocketsProxy instance.");
			return null;
		}
	}
}
