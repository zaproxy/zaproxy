/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 psiinon@gmail.com
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

import java.awt.EventQueue;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.filter.ExtensionFilter;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.websocket.filter.FilterWebSocketPayload;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;

/**
 * This extension adapter takes over after finishing
 * the HTTP based WebSockets handshake.
 * 
 * @author Robert Koch
 */
public class ExtensionWebSocket extends ExtensionAdaptor implements SessionChangedListener {
    
	private static final Logger logger = Logger.getLogger(ExtensionWebSocket.class);
	
	/**
	 * Name of this extension.
	 */
	public static final String NAME = "ExtensionWebSocket";

	/**
	 * Used to shorten the time, a listener is started on a WebSocket channel.
	 */
	private ExecutorService listenerThreadPool;

	/**
	 * Displayed in the bottom area beside the History, Spider, etc. tabs.
	 */
	private WebSocketPanel panel;

	/**
	 * List of observers where each element is informed on all channel's
	 * messages.
	 */
	private Vector<WebSocketObserver> allChannelObservers;
	
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
	
	@Override
	public void init() {
		super.init();

    	ExtensionFilter extFilter = (ExtensionFilter) Control.getSingleton().getExtensionLoader().getExtension(ExtensionFilter.NAME);
    	if (extFilter != null) {
    		// filter is not disabled, otherwise ignore it
    		extFilter.addFilter(new FilterWebSocketPayload());
    	}
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
        extensionHook.addSessionListener(this);
        if (getView() != null) {
        	WebSocketPanel panel = getWebSocketPanel();
        	panel.setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());
	        
        	extensionHook.getHookView().addStatusPanel(panel);	
	        //TODO: Help
//	    	ExtensionHelp.enableHelpKey(getWebSocketPanel(), "ui.tabs.websocket");
        	
        	allChannelObservers = extensionHook.getWebSocketObserverList();
        }
    }

	/**
	 * Add an open channel to this extension after
	 * HTTP handshake has been completed.
	 * 
	 * @param handShakeResponse Response header of HTTP-based handshake.
	 * @param localSocket Current connection channel from the browser to ZAP.
	 * @param remoteSocket Current connection channel from ZAP to the server.
	 * @param remoteReader Current {@link InputStream} of remote connection.
	 */
	public void addWebSocketsChannel(HttpMessage msg, Socket localSocket, Socket remoteSocket, InputStream remoteReader) {
		logger.debug("Got WebSockets channel from " + localSocket.getInetAddress()
				+ " port " + localSocket.getPort() + " to "
				+ remoteSocket.getInetAddress() + " port "
				+ remoteSocket.getPort());
		
		// parse HTTP handshake
		Map<String, String> wsExtensions = parseWebSocketExtensions(msg);
		String wsProtocol = parseWebSocketSubProtocol(msg);
		String wsVersion = parseWebSocketVersion(msg);

		WebSocketProxy ws = null;
		try {
			ws = WebSocketProxy.create(wsVersion, localSocket, remoteSocket, wsProtocol, wsExtensions);
			ws.startListeners(getListenerThreadPool(), remoteReader);
			
			String name = remoteSocket.getInetAddress().getHostName() + ":" + remoteSocket.getPort();
			
			// install GUI listener
			getWebSocketPanel().addProxy(ws, name);
			
			// add other observers
			for (WebSocketObserver observer : allChannelObservers) {
				ws.addObserver(observer);
			}
		} catch (WebSocketException e) {
			logger.error("Adding WebSockets channel failed due to: " + e.getMessage());
			return;
		}
	}

	/**
	 * Small helper to create a hash from an HTTP messages request and response
	 * header.
	 * 
	 * @param msg
	 * @return
	 */
	public static String createHandshakeHash(HttpMessage msg)  {
		try {
			String base = msg.getRequestHeader().toString() + msg.getResponseHeader().toString();
			
			MessageDigest m = MessageDigest.getInstance("MD5");
	        m.update(base.getBytes("UTF-8"), 0, base.length());

	        return new BigInteger(1, m.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			logger.warn(e);
		} catch (UnsupportedEncodingException e) {
			logger.warn(e);
		}
		
		return null;
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
	 * @param msg
	 * @return Map with extension name and parameter string.
	 */
	private Map<String, String> parseWebSocketExtensions(HttpMessage msg) {
		String extensionHeader = msg.getResponseHeader().getHeader("sec-websocket-extensions");

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
	 * @param msg
	 * @return Name of negotiated sub-protocol or null.
	 */
	private String parseWebSocketSubProtocol(HttpMessage msg) {
		String subProtocol = msg.getResponseHeader().getHeader("sec-websocket-protocol");
		return subProtocol;
	}

	/**
	 * The <em>Sec-WebSocket-Version</em> header might not always contain
	 * a number. Therefore I return a string. Use the version to choose
	 * the appropriate processing class.
	 * 
	 * @param msg
	 * @return Version of the WebSockets channel, defining the protocol.
	 */
	private String parseWebSocketVersion(HttpMessage msg) {
		String version = msg.getResponseHeader().getHeader("sec-websocket-version");
		
		if (version == null) {
			// check for requested WebSockets version
			version = msg.getRequestHeader().getHeader("sec-websocket-version");
			
			if (version == null) {
				// default to version 13 if non is given, for whatever reason
				logger.debug("No Sec-Websocket-Version header was provided - try version 13");
				version = "13";
			}
		}
		
		return version;
	}

	/**
	 * Creates and returns a cached thread pool that should speed up
	 * {@link WebSocketListener}.
	 * 
	 * @return
	 */
	private ExecutorService getListenerThreadPool() {
		if (listenerThreadPool == null) {
			listenerThreadPool = Executors.newCachedThreadPool();
		}
		
		return listenerThreadPool;
	}

	private WebSocketPanel getWebSocketPanel() {
		if (panel == null) {
			panel = new WebSocketPanel(getView().getMainFrame());
		}
		
		return panel;
	}

	// TODO: find out what to do in this case
	@Override
	public void sessionChanged(final Session session) {
		if (EventQueue.isDispatchThread()) {
		    sessionChangedEventHandler(session);

	    } else {
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                @Override
	                public void run() {
	        		    sessionChangedEventHandler(session);
	                }
	            });
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }
	    }
	}
	
	// TODO: find out what to do in this case
	private void sessionChangedEventHandler(Session session) {
		// do something here
//		throw new RuntimeException("sessionChangedEventHandler called");
		// clear all scans
//		this.getWebSocketPanel().clear();
//		this.getWebSocketPanel().reset();
//		if (session == null) {
//			// Closedown
//			return;
//		}
//		// Add new hosts
//		SiteNode root = (SiteNode)session.getSiteTree().getRoot();
//		@SuppressWarnings("unchecked")
//		Enumeration<SiteNode> en = root.children();
//		while (en.hasMoreElements()) {
//			this.getWebSocketPanel().addSite(en.nextElement().getNodeName(), true);
//		}
	}

	// TODO: find out what to do in this case
	@Override
	public void sessionAboutToChange(Session session) {
	}
}
