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
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.websocket.filter.FilterWebSocketPayload;
import org.zaproxy.zap.extension.websocket.ui.OptionsWebSocketPanel;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessageDAO;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;
import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;
import org.zaproxy.zap.extension.websocket.brk.PopupMenuAddBreakWebSocket;
import org.zaproxy.zap.extension.websocket.brk.WebSocketProxyListenerBreak;
import org.zaproxy.zap.extension.websocket.brk.WebSocketBreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.websocket.ui.httppanel.component.incoming.WebSocketIncomingComponent;
import org.zaproxy.zap.extension.websocket.ui.httppanel.component.outgoing.WebSocketOutgoingComponent;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.ByteWebSocketPanelViewModel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.StringWebSocketPanelViewModel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.WebSocketSyntaxHighlightTextView;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelComponentFactory;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelDefaultViewSelectorFactory;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;
 
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
	 * Will be added to the hook view. 
	 */
	private OptionsWebSocketPanel optionsPanel;
	
	/**
	 * Constructor initializes this class.
	 */
	public ExtensionWebSocket() {
		super();
		setName(NAME);
		setOrder(150);
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
        	
        	extensionHook.getHookView().addOptionPanel(getOptionsPanel());

			// Add "HttpPanel" components and views.
			HttpPanelManager manager = HttpPanelManager.getInstance();

			// Component for outgoing and incoming messages (the component
			// contains the normal text view (without syntax highlight))
			// Probably is needed only one component, it depends if all the
			// views are equal to both the outgoing and incoming messages.
			manager.addRequestComponent(new WebSocketOutgoingComponentFactory());
			manager.addResponseComponent(new WebSocketIncomingComponentFactory());

			// Hex views to outgoing and incoming messages.
			// Can use the same view factory as the view use the same model
			// because the payload is accessed the same way.
			HttpPanelViewFactory viewFactory = new WebSocketHexViewFactory();
			manager.addRequestView(WebSocketOutgoingComponent.NAME, viewFactory);
			manager.addResponseView(WebSocketIncomingComponent.NAME, viewFactory);
			
			// Add the default Hex view for binary-opcode messages.
			// Can use the same default view selector.
			HttpPanelDefaultViewSelectorFactory viewSelectorFactory = new HexDefaultViewSelectorFactory();
			manager.addResponseDefaultView(WebSocketOutgoingComponent.NAME, viewSelectorFactory);
			manager.addResponseDefaultView(WebSocketIncomingComponent.NAME, viewSelectorFactory);

			// Replace the normal text views with the ones that use syntax
			// highlight (use the same type of view).
			viewFactory = new SyntaxHighlightTextViewFactory();
			manager.addRequestView(WebSocketOutgoingComponent.NAME, viewFactory);
			manager.addResponseView(WebSocketIncomingComponent.NAME, viewFactory);

			ExtensionBreak extBreak = (ExtensionBreak) Control.getSingleton().getExtensionLoader().getExtension(ExtensionBreak.NAME);
			if (extBreak != null) {
				// Listen on the new messages so the breakpoints can apply.
				allChannelObservers.add(new WebSocketProxyListenerBreak(extBreak));

				// Pop up to add the breakpoint
				extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddBreakWebSocket(extBreak));
				extBreak.addBreakpointsUiManager(new WebSocketBreakpointsUiManagerInterface(extBreak));
			}
        }
    }

	/**
	 * Lazy initialize options panel.
	 * 
	 * @return
	 */
	private AbstractParamPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new OptionsWebSocketPanel(this);
		}
		
		return optionsPanel;
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
	
	private PopupMenuAddBreakWebSocket popupMenuAddBreakWebSocket;
	
    private PopupMenuAddBreakWebSocket getPopupMenuAddBreakWebSocket(ExtensionBreak extensionBreak) {
        if (popupMenuAddBreakWebSocket == null) {
            popupMenuAddBreakWebSocket = new PopupMenuAddBreakWebSocket();
            popupMenuAddBreakWebSocket.setExtension(extensionBreak);
        }
        return popupMenuAddBreakWebSocket;
    }

    private static final class WebSocketOutgoingComponentFactory implements HttpPanelComponentFactory {
        
        @Override
        public HttpPanelComponentInterface getNewComponent() {
            return new WebSocketOutgoingComponent();
        }

        @Override
        public String getComponentName() {
            return WebSocketOutgoingComponent.NAME;
        }
    }
	
    private static final class WebSocketIncomingComponentFactory implements HttpPanelComponentFactory {
        
        @Override
        public HttpPanelComponentInterface getNewComponent() {
            return new WebSocketIncomingComponent();
        }

        @Override
        public String getComponentName() {
            return WebSocketIncomingComponent.NAME;
        }
    }
	
	private static final class WebSocketHexViewFactory implements HttpPanelViewFactory {
        
        @Override
        public HttpPanelView getNewView() {
            return new HttpPanelHexView(new ByteWebSocketPanelViewModel(), false);
        }

        @Override
        public Object getOptions() {
            return null;
        }
    }
	
	private static final class HexDefaultViewSelector implements HttpPanelDefaultViewSelector {

        @Override
        public String getName() {
            return "HexDefaultViewSelector";
        }
        
        @Override
        public boolean matchToDefaultView(Message aMessage) {
            if (aMessage instanceof WebSocketMessageDAO) {
                WebSocketMessageDAO msg = (WebSocketMessageDAO)aMessage;
                
                return (msg.opcode == WebSocketMessage.OPCODE_BINARY);
            }
            return false;
        }

        @Override
        public String getViewName() {
            return HttpPanelHexView.CONFIG_NAME;
        }
        
        @Override
        public int getOrder() {
            return 20;
        }
    }

    private static final class HexDefaultViewSelectorFactory implements HttpPanelDefaultViewSelectorFactory {
        
        private static HttpPanelDefaultViewSelector defaultViewSelector = null;
        
        private HttpPanelDefaultViewSelector getDefaultViewSelector() {
            if (defaultViewSelector == null) {
                createViewSelector();
            }
            return defaultViewSelector;
        }
        
        private synchronized void createViewSelector() {
            if (defaultViewSelector == null) {
                defaultViewSelector = new HexDefaultViewSelector();
            }
        }
        
        @Override
        public HttpPanelDefaultViewSelector getNewDefaultViewSelector() {
            return getDefaultViewSelector();
        }
        
        @Override
        public Object getOptions() {
            return null;
        }
    }
	
    private static final class SyntaxHighlightTextViewFactory implements HttpPanelViewFactory {
        
        @Override
        public HttpPanelView getNewView() {
            return new WebSocketSyntaxHighlightTextView(new StringWebSocketPanelViewModel());
        }
        
        @Override
        public Object getOptions() {
            return null;
        }
    }
}
