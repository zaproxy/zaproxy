package org.zaproxy.zap.extension.websocket;

import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

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
	
//	/**
//	 * This list contains all established WebSocket channels.
//	 */
//	private static CopyOnWriteArrayList<WebSocketProxy> wsProxies = new CopyOnWriteArrayList<WebSocketProxy>();

	/**
	 * Constructor initializes this class.
	 */
	public ExtensionWebSocket() {
		super();
		setName(NAME);
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
	 * @param handShakeResponse Response header of HTTP-based handshake.
	 * @param localSocket Current connection channel from the browser to ZAP.
	 * @param remoteSocket Current connection channel from ZAP to the server.
	 */
	public void addWebSocketsChannel(HttpResponseHeader handShakeResponse, Socket localSocket, Socket remoteSocket) {
		log.debug("Got WebSockets channel from " + localSocket.getInetAddress() + 
				" port " + localSocket.getPort() +
				" to " + remoteSocket.getInetAddress() + 
				" port " + remoteSocket.getPort());
		try {
			localSocket.setSoTimeout(0);
			remoteSocket.setSoTimeout(0);
		} catch (SocketException e) {
			log.error("failed to set timeout to infinity for WebSocket-sockets");
		}
		
		// parse HTTP handshake response
		Map<String, String> wsExtensions = parseWebSocketExtensions(handShakeResponse);
		String wsProtocol = parseWebSocketSubProtocol(handShakeResponse);
		String wsVersion = parseWebSocketVersion(handShakeResponse);

		WebSocketProxy ws = null;
		try {
			ws = WebSocketProxy.create(wsVersion, localSocket, remoteSocket, wsProtocol, wsExtensions);
			ws.startListeners();
//			wsProxies.add(ws);
		} catch (WebSocketException e) {
			log.error("Adding WebSockets channel failed due to: " + e.getMessage());
			return;
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
}
