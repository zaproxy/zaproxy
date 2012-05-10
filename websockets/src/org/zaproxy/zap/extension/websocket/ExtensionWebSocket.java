package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.network.HttpResponseHeader;


/**
 * This extension adapter takes over after finishing
 * the HTTP based WebSockets handshake.
 * 
 * @author Robert Koch
 */
public class ExtensionWebSocket extends ExtensionAdaptor {
	
	public static final String NAME = "ExtensionWebSocket";
	    
	private static Logger log = Logger.getLogger(ExtensionWebSocket.class);
	
	/**
	 * This list contains all established WebSocket channels.
	 */
	private static CopyOnWriteArrayList<WebSocketProxy> wsProxies = new CopyOnWriteArrayList<WebSocketProxy>();

	/**
	 * Used for reading from WebSockets channels.
	 */
	private Selector selector;

	/**
	 * One thread listens to all WebSockets connections by using Java's NIO features.
	 */
	private Thread thread;

	private WebSocketsThread wsThread;

	/**
	 * Constructor initializes this class.
	 */
	public ExtensionWebSocket() {
		super();
		initialize();
	}
	
	/**
	 * Set name of extension
	 * and open a NIO Selector.
	 * 
	 * @return void
	 */
	private void initialize() {
        setName(NAME);
        
        try {
			selector = Selector.open();
		} catch (IOException e) {
			log.error("Was not able to open Selector for reading from WebSockets.");
		}
	}
	
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}
	
	@Override
	public String getDescription() {
		return Constant.messages.getString("websockets.desc");
	}

	/**
	 * Add open channel to this extension after handshake has been completed.
	 * 
	 * @param httpResponseHeader Response header of HTTP-based handshake.
	 * @param inChannel Current connection channel from the browser to ZAP.
	 * @param outChannel Current connection channel from ZAP to the server.
	 */
	public void addWebSocketsChannel(HttpResponseHeader handShakeResponse, SocketChannel inChannel, SocketChannel outChannel) {
		log.debug("Got WebSockets channel from " + inChannel.socket().getInetAddress() + 
				" port " + inChannel.socket().getPort() +
				" to " + outChannel.socket().getInetAddress() + 
				" port " + outChannel.socket().getPort());
		try {
			inChannel.socket().setSoTimeout(0);
			outChannel.socket().setSoTimeout(0);
		} catch (SocketException e) {
			log.error("failed to set timeout to infinity for WebSocket-sockets");
		}
		
		// parse HTTP handshake response
		Map<String, String> wsExtensions = parseWebSocketExtensions(handShakeResponse);
		String wsProtocol = parseWebSocketSubProtocol(handShakeResponse);
		String wsVersion = parseWebSocketVersion(handShakeResponse);
		
		try {
			WebSocketProxy ws = WebSocketProxy.create(wsVersion, inChannel, outChannel, wsProtocol, wsExtensions);
			ws.register(selector);
			//wsProxies.add(ws);
		} catch (WebSocketException e) {
			log.error("Adding WebSockets channel failed due to: " + e.getMessage());
			return;
		}
		
		if (thread == null && wsThread == null) {
			startCapturing();
		}
	}

	/**
	 * Parses the negotiated WebSockets extensions. It splits them up into
	 * name and params of the extension. In future we want to look up if given
	 * extension is available as ZAP extension and then use their knowledge
	 * to process frames.
	 * <p>
	 * The <em>Sec-WebSocket-Extensions</em> header is only allowed to appear
	 * once in the HTTP response (but several times in the HTTP request).
	 * 
	 * @param handShakeResponse
	 * @return Map with extension name and parameter string.
	 */
	private Map<String, String> parseWebSocketExtensions(HttpResponseHeader handShakeResponse) {
		String extensionHeader = handShakeResponse.getHeader("sec-websocket-extensions");

		if (extensionHeader == null) {
			return null;
		}
		
		/*
		 * From http://tools.ietf.org/html/rfc6455#section-4.3:
		 *   extension-list = 1#extension
      	 *   extension = extension-token *( ";" extension-param )
         *   extension-token = registered-token
         *   registered-token = token
         *   extension-param = token [ "=" (token | quoted-string) ]
         *    ; When using the quoted-string syntax variant, the value
         *    ; after quoted-string unescaping MUST conform to the
         *    ; 'token' ABNF.
         *    
         * e.g.:  	Sec-WebSocket-Extensions: foo
         * 			Sec-WebSocket-Extensions: bar; baz=2
		 *      is exactly equivalent to:
		 * 			Sec-WebSocket-Extensions: foo, bar; baz=2
		 * 
		 * e.g.:	Sec-WebSocket-Extensions: deflate-stream
		 * 			Sec-WebSocket-Extensions: mux; max-channels=4; flow-control, deflate-stream
		 * 			Sec-WebSocket-Extensions: private-extension
		 */
		Map<String, String> wsExtensions = new HashMap<String, String>();
		for (String extension : extensionHeader.split(",")) {
			String key = extension.trim();
			String params = "";
			
			int paramsIndex = key.indexOf(";");
			if (paramsIndex != -1) {
				key = extension.substring(0, paramsIndex).trim();
				params = extension.substring(paramsIndex + 1).trim();
			}
			
			wsExtensions.put(key, params);
		}
		
		return wsExtensions;
	}

	/**
	 * Parses negotiated protocols out of the response header.
	 * <p>
	 * The <em>Sec-WebSocket-Protocol</em> header is only allowed to appear
	 * once in the HTTP response (but several times in the HTTP request).
	 * 
	 * A server that speaks multiple sub-protocols has to make sure it selects
	 * one based on the client's handshake and specifies it in its handshake.
	 * 
	 * @param handShakeResponse
	 * @return Name of negotiated sub-protocol or null.
	 */
	private String parseWebSocketSubProtocol(HttpResponseHeader handShakeResponse) {
		String subProtocol = handShakeResponse.getHeader("sec-websocket-protocol");
		return subProtocol;
	}

	/**
	 * The <em>Sec-WebSocket-Version</em> header might not always contain
	 * a number. Therefore I return a string. Use the version to choose
	 * the appropriate processing class.
	 * 
	 * @param handShakeResponse
	 * @return Version of the WebSockets channel, defining the protocol.
	 */
	private String parseWebSocketVersion(HttpResponseHeader handShakeResponse) {
		String version = handShakeResponse.getHeader("sec-websocket-version");
		
		if (version == null) {
			// default to version 13 if non is given, for whatever reason
			log.debug("No Sec-Websocket-Version header was provided - try version 13");
			version = "13";
		}
		
		return version;
	}

	/**
	 * Start capturing WebSockets traffic.
	 * Normally you will start capturing when
	 * there is at least one WebSockets channel.
	 */
	public void startCapturing () {
		wsThread = new WebSocketsThread(selector);
// TODO: What is going on here - I must have missed something, that this code
// works for the first WebSockets connection at: http://www.websocket.org/echo.html

// works this way - non threaded!
		wsThread.run();
				
// does not work this way - no WebSocket connection has been established
//		thread = new Thread(wsThread, "ZAP-WebSockets");
//		thread.setDaemon(true);
//		thread.start();
	}
	
	/**
	 * Stop capturing WebSockets traffic.
	 * Call it when you don't want to process WebSockets messages.
	 * This means that no WebSocket traffic is forwarded.
	 */
	public void stopCapturing() {
		//TODO: stop thread when determined which method to use
//		thread.interrupt();
	}
	
	public Iterator<WebSocketProxy> getWebSocketsIterator() {
		return wsProxies.iterator();
	}
}
