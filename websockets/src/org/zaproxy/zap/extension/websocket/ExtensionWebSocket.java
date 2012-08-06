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
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.filter.ExtensionFilter;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.brk.BreakpointMessageHandler;
import org.zaproxy.zap.extension.brk.ExtensionBreak;
import org.zaproxy.zap.extension.fuzz.ExtensionFuzz;
import org.zaproxy.zap.extension.help.ExtensionHelp;

import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.component.HttpPanelComponentInterface;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelDefaultViewSelector;
import org.zaproxy.zap.extension.httppanel.view.HttpPanelView;
import org.zaproxy.zap.extension.httppanel.view.hex.HttpPanelHexView;

import org.zaproxy.zap.extension.websocket.brk.PopupMenuAddBreakWebSocket;
import org.zaproxy.zap.extension.websocket.brk.WebSocketBreakpointMessageHandler;
import org.zaproxy.zap.extension.websocket.brk.WebSocketBreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.websocket.brk.WebSocketProxyListenerBreak;
import org.zaproxy.zap.extension.websocket.db.WebSocketStorage;
import org.zaproxy.zap.extension.websocket.filter.FilterWebSocketPayload;
import org.zaproxy.zap.extension.websocket.fuzz.ShowFuzzMessageInWebSocketsTabMenuItem;
import org.zaproxy.zap.extension.websocket.fuzz.WebSocketFuzzerHandler;
import org.zaproxy.zap.extension.websocket.ui.OptionsParamWebSocket;
import org.zaproxy.zap.extension.websocket.ui.OptionsWebSocketPanel;
import org.zaproxy.zap.extension.websocket.ui.ExcludeFromWebSocketsMenuItem;
import org.zaproxy.zap.extension.websocket.ui.ExcludeFromWebSocketSessionPanel;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.component.WebSocketComponent;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.ByteWebSocketPanelViewModel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.StringWebSocketPanelViewModel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.WebSocketSyntaxHighlightTextView;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.large.WebSocketLargePayloadUtil;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.large.WebSocketLargePayloadView;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.large.WebSocketLargetPayloadViewModel;
import org.zaproxy.zap.extension.websocket.utility.Pair;
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
	 * List of observers where each element is informed on all channel's
	 * messages.
	 */
	private Vector<WebSocketObserver> allChannelObservers;

	/**
	 * Used for setting up all channel observers.
	 */
	private boolean hasHookedAllObserver;

	/**
	 * Contains all proxies with their corresponding handshake message.
	 */
	private Map<Integer, WebSocketProxy> wsProxies;

	/**
	 * Interface to database.
	 */
	private WebSocketStorage storage;

	/**
	 * Replace payload in background.
	 */
	private FilterWebSocketPayload payloadFilter;

	/**
	 * Messages for those {@link WebSocketProxy} on this list are just
	 * forwarded, but not stored nor shown in UI.
	 */
	private ArrayList<Pair<String, Integer>> storageBlacklist;

	/**
	 * Different options in config.xml can change this extension's behavior.
	 */
	private OptionsParamWebSocket config;
	
	/**
	 * Constructor initializes this class.
	 */
	public ExtensionWebSocket() {
		super();
		allChannelObservers = new Vector<WebSocketObserver>();
		hasHookedAllObserver = false;
		wsProxies = new HashMap<Integer, WebSocketProxy>();
		storageBlacklist = new ArrayList<Pair<String, Integer>>();
		config = new OptionsParamWebSocket();
		
		initialize();
	}
	
	private void initialize() {
		setName(NAME);
		
		// should be initialized after ExtensionBreak (24) & ExtensionFilter (8)
		setOrder(150);
		
		Model model = Model.getSingleton();
		
		// setup database
		storage = new WebSocketStorage(model.getDb().getTableWebSocket());
		addAllChannelObserver(storage);
		
		// setup configuration        
    	config.load(model.getOptionsParam().getConfig());
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}
	
	@Override
	public String getDescription() {
		return Constant.messages.getString("websocket.desc");
	}
	
	@Override
	public void init() {
		super.init();
	}

	/**
	 * Add an observer that is attached to every channel connected in future.
	 * 
	 * @param listener
	 */
	public void addAllChannelObserver(WebSocketObserver listener) {
		allChannelObservers.add(listener);
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
		if (!hasHookedAllObserver) {
			hasHookedAllObserver = true;
			// Cannot call this method in own hook() method, as one or another
			// extension had no chance to add webSocketObserver!
			Control.getSingleton().getExtensionLoader().hookWebSocketObserver(this);
		}
		
		logger.debug("Got WebSockets channel from " + localSocket.getInetAddress()
				+ " port " + localSocket.getPort() + " to "
				+ remoteSocket.getInetAddress() + " port "
				+ remoteSocket.getPort());
		
		// parse HTTP handshake
		Map<String, String> wsExtensions = parseWebSocketExtensions(msg);
		String wsProtocol = parseWebSocketSubProtocol(msg);
		String wsVersion = parseWebSocketVersion(msg);

		WebSocketProxy wsProxy = null;
		try {
			wsProxy = WebSocketProxy.create(wsVersion, localSocket, remoteSocket, wsProtocol, wsExtensions);
			
			// set other observers and handshake reference, before starting listeners
			for (WebSocketObserver observer : allChannelObservers) {
				wsProxy.addObserver(observer);
			}
			
			// wait until HistoryReference is saved to database
			while (msg.getHistoryRef() == null) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					logger.warn(e.getMessage(), e);
				}
			}
			wsProxy.setHandshakeReference(msg.getHistoryRef());
			wsProxy.setForwardOnly(isStorageBlacklisted(wsProxy));
			wsProxy.startListeners(getListenerThreadPool(), remoteReader);
			
			try {
				// Workaround: Have to wait for HistoryReference object. Immediately it is null.
				while (msg.getHistoryRef() == null) {
					Thread.sleep(100);
				}
			} catch (InterruptedException e) {
				logger.error(e);
			}
			
			synchronized (wsProxies) {
				wsProxies.put(wsProxy.getChannelId(), wsProxy);
			}
		} catch (WebSocketException e) {
			logger.error("Adding WebSockets channel failed due to: " + e.getMessage());
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

	/**
	 * Returns true if the WebSocket connection that followed the given
	 * WebSocket handshake is already alive.
	 * 
	 * @param handshakeMessage
	 * @return
	 */
	public boolean isConnected(HttpMessage handshakeMessage) {
		int historyId = handshakeMessage.getHistoryRef().getHistoryId();
		synchronized (wsProxies) {
			for (Entry<Integer, WebSocketProxy> entry : wsProxies.entrySet()) {
				WebSocketProxy proxy = entry.getValue();
				if (historyId == proxy.getHandshakeReference().getHistoryId()) {
					return proxy.isConnected();
				}
			}
		}
		return false;
	}

	/**
	 * Returns true if given channel id is connected.
	 * 
	 * @param channelId
	 * @return
	 */
	public boolean isConnected(Integer channelId) {
		synchronized (wsProxies) {
			if (wsProxies.containsKey(channelId)) {
				return wsProxies.get(channelId).isConnected();
			}
		}
		return false;
	}

    /**
	 * Submitted list of strings will be interpreted as regular expression on
	 * WebSocket channel URLs.
	 * <p>
	 * While connections to those excluded URLs will be established and messages
	 * will be forwarded, nothing is stored nor can you view the communication
	 * in the UI.
	 * 
	 * @param ignoredStrings
	 */
	public void setStorageBlacklist(List<String> ignoredStrings) {
		synchronized (storageBlacklist) {
			storageBlacklist.clear();
			for (String regex : ignoredStrings) {
				Integer port = null;
				try {
					port = Integer.parseInt(regex.replaceAll("^.*:([0-9]+).*$", "$1"));
				} catch (NumberFormatException e) {
					// safely ignore, null is used as wildcard
				}
				
				String host = regex.replaceAll("^(.*):[0-9]+(.*)$", "$1$2");
				if (host.isEmpty()) {
					// special case if ":80" is entered
					host = null;
				}
				
				storageBlacklist.add(new Pair<String, Integer>(host, port));
			}
		}

		synchronized (wsProxies) {
			for (Entry<Integer, WebSocketProxy> entry : wsProxies.entrySet()) {
				WebSocketProxy wsProxy = entry.getValue();
				
				if (isStorageBlacklisted(wsProxy)) {
					wsProxy.setForwardOnly(true);
				} else {
					wsProxy.setForwardOnly(false);
				}
			}
		}
	}

	/**
	 * If given channel is blacklisted, then nothing should be stored. Moreover
	 * it should not appear in user interface, but messages should be forwarded.
	 * 
	 * @param wsProxy
	 * @return
	 */
	private boolean isStorageBlacklisted(WebSocketProxy wsProxy) {
		if (config.isForwardAll()) {
			// all channels are blacklisted
			return true;
		}
		
		WebSocketChannelDAO dao = wsProxy.getDAO();
		for (Pair<String, Integer> regex : storageBlacklist) {
			if (regex.x == null || dao.host.matches(regex.x)) {
				if (regex.y == null || dao.port.equals(regex.y)) {
					// match found => should go onto storage blacklist
					return true;
				}
			}
		}
		
		return false;
	}

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
	
	private void sessionChangedEventHandler(Session session) {
		// close existing connections
		synchronized (wsProxies) {
			for (WebSocketProxy wsProxy : wsProxies.values()) {
				wsProxy.shutdown();
			}
			wsProxies.clear();	
		}
		
		// reset WebSocket panel
		getWebSocketPanel().reset();
		
		// reset replace payload filter
		if (payloadFilter != null) {
			payloadFilter.reset();
		}
	}

	@Override
	public void sessionAboutToChange(Session session) {
		// do nothing
	}

	@Override
	public void sessionScopeChanged(Session session) {
		// TODO: Find out what to do!
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		// TODO: Find out what to do!
	}
	
	/**
	 * This method interweaves the WebSocket extension with the rest of ZAP.
	 * <p>
	 * It does the following things:
	 * <ul>
	 * <li>installs itself as session listener in order to react on session
	 * changes</li>
	 * <li>adds WebSocket tab to the status panel (information window containing
	 * e.g.: the History tab)</li>
	 * <li>adds a WebSocket specific options panel</li>
	 * <li>adds 'Exclude From WebSockets' to Session Properties</li>
	 * <li>sets up context menu for WebSockets panel with 'Break' & 'Exclude'</li>
	 * </ul>
	 * </p>
	 */
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    
        extensionHook.addSessionListener(this);

		// TODO: ensure that it is working in headless mode
        if (getView() != null) {
        	ExtensionLoader extLoader = Control.getSingleton().getExtensionLoader();
        	ExtensionHookView hookView = extensionHook.getHookView();
        	ExtensionHookMenu hookMenu = extensionHook.getHookMenu();
        	
        	// setup WebSocket tab
        	WebSocketPanel wsPanel = getWebSocketPanel();
        	wsPanel.setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());
        	
			addAllChannelObserver(wsPanel);
	    	ExtensionHelp.enableHelpKey(wsPanel, "ui.tabs.websocket");
	        
        	hookView.addStatusPanel(getWebSocketPanel());
        	
        	// setup Options Panel
        	hookView.addOptionPanel(getOptionsPanel());
        	
        	// add 'Exclude from WebSockets' menu item to WebSocket tab context menu
			hookMenu.addPopupMenuItem(new ExcludeFromWebSocketsMenuItem(storage.getTable()));

			// setup Session Properties
			getView().getSessionDialog().addParamPanel(new String[]{}, new ExcludeFromWebSocketSessionPanel(), false);
			
			// setup Breakpoints
			ExtensionBreak extBreak = (ExtensionBreak) extLoader.getExtension(ExtensionBreak.NAME);
			if (extBreak != null) {
				// setup custom breakpoint handler
				BreakpointMessageHandler wsBrkMessageHandler = new WebSocketBreakpointMessageHandler(extBreak.getBreakPanel(), config);
			    wsBrkMessageHandler.setEnabledBreakpoints(extBreak.getBreakpointsEnabledList());
			    
				// listen on new messages such that breakpoints can apply
				addAllChannelObserver(new WebSocketProxyListenerBreak(wsBrkMessageHandler));

				// pop up to add the breakpoint
				hookMenu.addPopupMenuItem(new PopupMenuAddBreakWebSocket(extBreak));
				extBreak.addBreakpointsUiManager(getBrkManager());
			}
        	
        	// setup replace payload filter
        	ExtensionFilter extFilter = (ExtensionFilter) extLoader.getExtension(ExtensionFilter.NAME);
        	if (extFilter != null) {
        		payloadFilter = new FilterWebSocketPayload(wsPanel.getChannelComboBoxModel());
        		payloadFilter.initView(getView());
        		extFilter.addFilter(payloadFilter);
        	}
            
        	// setup fuzzable extension
            ExtensionFuzz extFuzz = (ExtensionFuzz) extLoader.getExtension(ExtensionFuzz.NAME);
            if (extFuzz != null) {
            	hookMenu.addPopupMenuItem(new ShowFuzzMessageInWebSocketsTabMenuItem(getWebSocketPanel()));
            	
            	WebSocketFuzzerHandler fuzzHandler = new WebSocketFuzzerHandler(storage.getTable());
                extFuzz.addFuzzerHandler(WebSocketMessageDAO.class, fuzzHandler);
                addAllChannelObserver(fuzzHandler);
            }
			
			// setup Workpanel (window containing Request, Response & Break tab)
        	initializeWebSocketsForWorkPanel();
        }
    }
	
	/*
	 * ************************************************************************
	 * GUI specific code follows here now. It is accessed only by methods hook()
	 * and sessionChangedEventHandler() (latter calls only getWebSocketPanel()).
	 * All of this UI-related code is private and should not be accessible from
	 * outside.
	 */

	/**
	 * Displayed in the bottom area beside the History, Spider, etc. tabs.
	 */
	private WebSocketPanel panel;

	/**
	 * Will be added to the hook view. 
	 */
	private OptionsWebSocketPanel optionsPanel;

	/**
	 * Allows to set custom breakpoints, e.g.: for specific opcodes only.
	 */
	private WebSocketBreakpointsUiManagerInterface brkManager;

	private WebSocketPanel getWebSocketPanel() {
		if (panel == null) {
			panel = new WebSocketPanel(storage.getTable(), getBrkManager());
		}
		return panel;
	}

	private WebSocketBreakpointsUiManagerInterface getBrkManager() {
		if (brkManager == null) {
			ExtensionBreak extBreak = (ExtensionBreak) Control.getSingleton().getExtensionLoader().getExtension(ExtensionBreak.NAME);
			if (extBreak != null) {
				brkManager = new WebSocketBreakpointsUiManagerInterface(extBreak);
			}
		}
		return brkManager;
	}
	
	private AbstractParamPanel getOptionsPanel() {
		if (optionsPanel == null) {
			optionsPanel = new OptionsWebSocketPanel(config);
		}
		return optionsPanel;
	}


	private void initializeWebSocketsForWorkPanel() {
		// Add "HttpPanel" components and views.
		HttpPanelManager manager = HttpPanelManager.getInstance();

		// component factory for outgoing and incoming messages with Text view
		HttpPanelComponentFactory componentFactory = new WebSocketComponentFactory();
		manager.addRequestComponent(componentFactory);
		manager.addResponseComponent(componentFactory);

		// use same factory for request & response,
		// as Hex payloads are accessed the same way
		HttpPanelViewFactory viewFactory = new WebSocketHexViewFactory();
		manager.addRequestView(WebSocketComponent.NAME, viewFactory);
		manager.addResponseView(WebSocketComponent.NAME, viewFactory);
		
		// add the default Hex view for binary-opcode messages
		HttpPanelDefaultViewSelectorFactory viewSelectorFactory = new HexDefaultViewSelectorFactory();
		manager.addRequestDefaultView(WebSocketComponent.NAME, viewSelectorFactory);
		manager.addResponseDefaultView(WebSocketComponent.NAME, viewSelectorFactory);

		// replace the normal Text views with the ones that use syntax highlighting
		viewFactory = new SyntaxHighlightTextViewFactory();
		manager.addRequestView(WebSocketComponent.NAME, viewFactory);
		manager.addResponseView(WebSocketComponent.NAME, viewFactory);

		// support large payloads on incoming and outgoing messages
		viewFactory = new WebSocketLargePayloadViewFactory();
		manager.addRequestView(WebSocketComponent.NAME, viewFactory);
		manager.addResponseView(WebSocketComponent.NAME, viewFactory);
		
		viewSelectorFactory = new WebSocketLargePayloadDefaultViewSelectorFactory();
		manager.addRequestDefaultView(WebSocketComponent.NAME, viewSelectorFactory);
		manager.addResponseDefaultView(WebSocketComponent.NAME, viewSelectorFactory);
	}

	/**
	 * The component returned by this factory contain the normal text view
	 * (without syntax highlighting).
	 */
    private static final class WebSocketComponentFactory implements HttpPanelComponentFactory {
        
        @Override
        public HttpPanelComponentInterface getNewComponent() {
            return new WebSocketComponent();
        }

        @Override
        public String getComponentName() {
            return WebSocketComponent.NAME;
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
	
	private static final class WebSocketLargePayloadViewFactory implements HttpPanelViewFactory {
		
		@Override
		public HttpPanelView getNewView() {
			return new WebSocketLargePayloadView(new WebSocketLargetPayloadViewModel());
		}

		@Override
		public Object getOptions() {
			return null;
		}
	}
	
	private static final class WebSocketLargePayloadDefaultViewSelectorFactory implements HttpPanelDefaultViewSelectorFactory {
		
		private static HttpPanelDefaultViewSelector defaultViewSelector = null;
		
		private HttpPanelDefaultViewSelector getDefaultViewSelector() {
			if (defaultViewSelector == null) {
				createViewSelector();
			}
			return defaultViewSelector;
		}
		
		private synchronized void createViewSelector() {
			if (defaultViewSelector == null) {
				defaultViewSelector = new WebSocketLargePayloadDefaultViewSelector();
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

	private static final class WebSocketLargePayloadDefaultViewSelector implements HttpPanelDefaultViewSelector {

		@Override
		public String getName() {
			return "WebSocketLargePayloadDefaultViewSelector";
		}
		
		@Override
		public boolean matchToDefaultView(Message aMessage) {
		    return WebSocketLargePayloadUtil.isLargePayload(aMessage);
		}

		@Override
		public String getViewName() {
			return WebSocketLargePayloadView.CONFIG_NAME;
		}
        
        @Override
        public int getOrder() {
        	// has to come before HexDefaultViewSelector
            return 15;
        }
	}
}
