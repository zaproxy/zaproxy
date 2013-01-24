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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.RecordSessionUrl;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookMenu;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.filter.ExtensionFilter;
import org.parosproxy.paros.extension.manualrequest.ExtensionManualRequestEditor;
import org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog;
import org.parosproxy.paros.extension.manualrequest.http.impl.ManualHttpRequestEditorDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.ZapGetMethod;
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
import org.zaproxy.zap.extension.websocket.db.TableWebSocket;
import org.zaproxy.zap.extension.websocket.db.WebSocketStorage;
import org.zaproxy.zap.extension.websocket.filter.FilterWebSocketPayload;
import org.zaproxy.zap.extension.websocket.filter.WebSocketFilter;
import org.zaproxy.zap.extension.websocket.filter.WebSocketFilterListener;
import org.zaproxy.zap.extension.websocket.fuzz.ShowFuzzMessageInWebSocketsTabMenuItem;
import org.zaproxy.zap.extension.websocket.fuzz.WebSocketFuzzerHandler;
import org.zaproxy.zap.extension.websocket.manualsend.ManualWebSocketSendEditorDialog;
import org.zaproxy.zap.extension.websocket.manualsend.WebSocketPanelSender;
import org.zaproxy.zap.extension.websocket.ui.ExcludeFromWebSocketsMenuItem;
import org.zaproxy.zap.extension.websocket.ui.OptionsParamWebSocket;
import org.zaproxy.zap.extension.websocket.ui.OptionsWebSocketPanel;
import org.zaproxy.zap.extension.websocket.ui.PopupExcludeWebSocketContextMenu;
import org.zaproxy.zap.extension.websocket.ui.PopupIncludeWebSocketContextMenu;
import org.zaproxy.zap.extension.websocket.ui.ResendWebSocketMessageMenuItem;
import org.zaproxy.zap.extension.websocket.ui.SessionExcludeFromWebSocket;
import org.zaproxy.zap.extension.websocket.ui.WebSocketPanel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.component.WebSocketComponent;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.ByteWebSocketPanelViewModel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.models.StringWebSocketPanelViewModel;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.WebSocketSyntaxHighlightTextView;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.large.WebSocketLargePayloadUtil;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.large.WebSocketLargePayloadView;
import org.zaproxy.zap.extension.websocket.ui.httppanel.views.large.WebSocketLargetPayloadViewModel;
import org.zaproxy.zap.view.HttpPanelManager;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelComponentFactory;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelDefaultViewSelectorFactory;
import org.zaproxy.zap.view.HttpPanelManager.HttpPanelViewFactory;
import org.zaproxy.zap.view.SiteMapListener;
import org.zaproxy.zap.view.SiteMapTreeCellRenderer;
 
/**
 * The WebSockets-extension takes over after the HTTP based WebSockets handshake
 * is finished.
 * 
 * @author Robert Koch
 */
public class ExtensionWebSocket extends ExtensionAdaptor implements
		PersistentConnectionListener, SessionChangedListener, SiteMapListener {
    
	private static final Logger logger = Logger.getLogger(ExtensionWebSocket.class);
	
	public static final int HANDSHAKE_LISTENER = 10;
	
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
	 * Contains all proxies with their corresponding handshake message.
	 */
	private Map<Integer, WebSocketProxy> wsProxies;

	/**
	 * Interface to database.
	 */
	private WebSocketStorage storage;

	/**
	 * List of WebSocket related filters.
	 */
	private WebSocketFilterListener wsFilterListener;

	/**
	 * Different options in config.xml can change this extension's behavior.
	 */
	private OptionsParamWebSocket config;

	/**
	 * Current mode of ZAP. Determines if "unsafe" actions are allowed.
	 */
	private Mode mode;

	/**
	 * Link to {@link ExtensionFuzz}.
	 */
	private WebSocketFuzzerHandler fuzzHandler;

	/**
	 * Messages for some {@link WebSocketProxy} on this list are just
	 * forwarded, but not stored nor shown in UI.
	 */
	private List<Pattern> preparedIgnoredChannels;

	/**
	 * Contains raw regex values, as they appear in the sessions dialogue.
	 */
	private List<String> ignoredChannelList;
	
	public ExtensionWebSocket() {
		super(NAME);
		
		// should be initialized after ExtensionBreak (24) and
		// ExtensionFilter (8) and ManualRequestEditor (36)
		setOrder(150);
	}
	
	@Override
	public void init() {
		super.init();
		
		allChannelObservers = new Vector<>();
		wsProxies = new HashMap<>();
		config = new OptionsParamWebSocket();
		
		// setup database
		storage = new WebSocketStorage(createTableWebSocket());
		addAllChannelObserver(storage);
		
		preparedIgnoredChannels = new ArrayList<>();
		ignoredChannelList = new ArrayList<>();
	}
	
	private TableWebSocket createTableWebSocket() {
		TableWebSocket table = new TableWebSocket();
		Database db = Model.getSingleton().getDb();
		db.addDatabaseListener(table);
		try {
			table.databaseOpen(db.getDatabaseServer());
		} catch (SQLException e) {
			logger.warn(e.getMessage(), e);
		}
		return table;
	}

	/**
	 * This method interweaves the WebSocket extension with the rest of ZAP.
	 * <p>
	 * It does the following things:
	 * <ul>
	 * <li>listens to new WebSocket connections</li>
	 * <li>installs itself as session listener in order to react on session
	 * changes</li>
	 * <li>adds a WebSocket tab to the status panel (information window containing
	 * e.g.: the History tab)</li>
	 * <li>adds a WebSocket specific options panel</li>
	 * <li>sets up context menu for WebSockets panel with 'Break' & 'Exclude'</li>
	 * </ul>
	 * </p>
	 */
	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		
		extensionHook.addPersistentConnectionListener(this);
		
		extensionHook.addSessionListener(this);
		
		// setup configuration
		extensionHook.addOptionsParamSet(config);
		
		try {
			setChannelIgnoreList(Model.getSingleton().getSession().getExcludeFromProxyRegexs());
		} catch (WebSocketException e) {
			logger.warn(e.getMessage(), e);
		}
		
		if (getView() != null) {
			ExtensionLoader extLoader = Control.getSingleton().getExtensionLoader();
			ExtensionHookView hookView = extensionHook.getHookView();
			ExtensionHookMenu hookMenu = extensionHook.getHookMenu();
			
			// setup WebSocket tab
			WebSocketPanel wsPanel = getWebSocketPanel();
			wsPanel.setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());
			
			extensionHook.addSessionListener(wsPanel.getSessionListener());
			
			addAllChannelObserver(wsPanel);
			ExtensionHelp.enableHelpKey(wsPanel, "ui.tabs.websocket");
			
			hookView.addStatusPanel(getWebSocketPanel());
			
			// setup Options Panel
			hookView.addOptionPanel(getOptionsPanel());
			
			// add 'Exclude from WebSockets' menu item to WebSocket tab context menu
			hookMenu.addPopupMenuItem(new ExcludeFromWebSocketsMenuItem(this, storage.getTable()));

			// setup Session Properties
			getView().getSessionDialog().addParamPanel(new String[]{}, new SessionExcludeFromWebSocket(this), false);
			
			// setup Breakpoints
			ExtensionBreak extBreak = (ExtensionBreak) extLoader.getExtension(ExtensionBreak.NAME);
			if (extBreak != null) {
				// setup custom breakpoint handler
				BreakpointMessageHandler wsBrkMessageHandler = new WebSocketBreakpointMessageHandler(extBreak.getBreakPanel(), config);
				wsBrkMessageHandler.setEnabledBreakpoints(extBreak.getBreakpointsEnabledList());
				
				// listen on new messages such that breakpoints can apply
				addAllChannelObserver(new WebSocketProxyListenerBreak(this, wsBrkMessageHandler));

				// pop up to add the breakpoint
				hookMenu.addPopupMenuItem(new PopupMenuAddBreakWebSocket(extBreak));
				extBreak.addBreakpointsUiManager(getBrkManager());
			}
			
			// setup replace payload filter
			wsFilterListener = new WebSocketFilterListener();
			addAllChannelObserver(wsFilterListener);
			addWebSocketFilter(new FilterWebSocketPayload(this, wsPanel.getChannelsModel()));
			
			// setup fuzzable extension
			ExtensionFuzz extFuzz = (ExtensionFuzz) extLoader.getExtension(ExtensionFuzz.NAME);
			if (extFuzz != null) {
				hookMenu.addPopupMenuItem(new ShowFuzzMessageInWebSocketsTabMenuItem(getWebSocketPanel()));
				
				fuzzHandler = new WebSocketFuzzerHandler(storage.getTable());
				extFuzz.addFuzzerHandler(WebSocketMessageDTO.class, fuzzHandler);
				addAllChannelObserver(fuzzHandler);
			}
			
			// add exclude/include scope
			hookMenu.addPopupMenuItem(new PopupIncludeWebSocketContextMenu());
			hookMenu.addPopupMenuItem(new PopupExcludeWebSocketContextMenu());
			
			// setup workpanel (window containing Request, Response & Break tab)
			initializeWebSocketsForWorkPanel();
			
			// setup manualrequest extension
			ExtensionManualRequestEditor extManReqEdit = (ExtensionManualRequestEditor) extLoader
					.getExtension(ExtensionManualRequestEditor.NAME);
			if (extManReqEdit != null) {
				WebSocketPanelSender sender = new WebSocketPanelSender();
				addAllChannelObserver(sender);
				
				ManualWebSocketSendEditorDialog sendDialog = createManualSendDialog(sender);
				extManReqEdit.addManualSendEditor(sendDialog);
				extensionHook.getHookMenu().addToolsMenuItem(sendDialog.getMenuItem());
				
				// add 'Resend Message' menu item to WebSocket tab context menu
				hookMenu.addPopupMenuItem(new ResendWebSocketMessageMenuItem(createReSendDialog(sender)));
				
				
				// setup persistent connection listener for http manual send editor
				ManualRequestEditorDialog sendEditor = extManReqEdit.getManualSendEditor(HttpMessage.class);
				if (sendEditor != null) {
					ManualHttpRequestEditorDialog httpSendEditor = (ManualHttpRequestEditorDialog) sendEditor;
					httpSendEditor.addPersistentConnectionListener(this);
				}
			}
		}
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}
	
	@Override
	public String getDescription() {
		return Constant.messages.getString("websocket.desc");
	}

	/**
	 * Add an observer that is attached to every channel connected in future.
	 * 
	 * @param observer
	 */
	public void addAllChannelObserver(WebSocketObserver observer) {
		allChannelObservers.add(observer);
	}

	/**
	 * Add another WebSocket specific filter instance. Listens also to normal
	 * HTTP communication.
	 * 
	 * @param filter Instance receives payloads and is able to change it.
	 */
	public void addWebSocketFilter(WebSocketFilter filter) {
		ExtensionLoader extLoader = Control.getSingleton().getExtensionLoader();
		ExtensionFilter extFilter = (ExtensionFilter) extLoader.getExtension(ExtensionFilter.NAME);
		if (extFilter != null) {
			filter.initView(getView());
			extFilter.addFilter(filter);
			
			wsFilterListener.addFilter(filter);
		} else {
			logger.warn("Filter '" + filter.getClass().toString() + "' couldn't be added as the filter extension is not available!");
		}
	}

	@Override
	public int getArrangeableListenerOrder() {
		return HANDSHAKE_LISTENER;
	}

	@Override
	public boolean onHandshakeResponse(HttpMessage httpMessage, Socket inSocket, ZapGetMethod method) {
		boolean keepSocketOpen = false;
		
		if (httpMessage.isWebSocketUpgrade()) {
			logger.debug("Got WebSockets upgrade request. Handle socket connection over to WebSockets extension.");
			
			if (method != null) {
				Socket outSocket = method.getUpgradedConnection();
				InputStream outReader = method.getUpgradedInputStream();
				
				keepSocketOpen = true;
				
				addWebSocketsChannel(httpMessage, inSocket, outSocket, outReader);
			} else {
				logger.error("Unable to retrieve upgraded outgoing channel.");
			}
		}
		
		return keepSocketOpen;
	}

	/**
	 * Add an open channel to this extension after
	 * HTTP handshake has been completed.
	 * 
	 * @param handshakeMessage HTTP-based handshake.
	 * @param localSocket Current connection channel from the browser to ZAP.
	 * @param remoteSocket Current connection channel from ZAP to the server.
	 * @param remoteReader Current {@link InputStream} of remote connection.
	 */
	public void addWebSocketsChannel(HttpMessage handshakeMessage, Socket localSocket, Socket remoteSocket, InputStream remoteReader) {
		try {			
			if (logger.isDebugEnabled()) {
				String source = (localSocket != null) ? localSocket.getInetAddress().toString() + ":" + localSocket.getPort() : "ZAP";
				String destination = remoteSocket.getInetAddress() + ":" + remoteSocket.getPort();
				
				logger.debug("Got WebSockets channel from " + source + " to " + destination);
			}
			
			// parse HTTP handshake
			Map<String, String> wsExtensions = parseWebSocketExtensions(handshakeMessage);
			String wsProtocol = parseWebSocketSubProtocol(handshakeMessage);
			String wsVersion = parseWebSocketVersion(handshakeMessage);
	
			WebSocketProxy wsProxy = null;
			wsProxy = WebSocketProxy.create(wsVersion, localSocket, remoteSocket, wsProtocol, wsExtensions);
			
			// set other observers and handshake reference, before starting listeners
			for (WebSocketObserver observer : allChannelObservers) {
				wsProxy.addObserver(observer);
			}
			
			// wait until HistoryReference is saved to database
			while (handshakeMessage.getHistoryRef() == null) {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
					logger.warn(e.getMessage(), e);
				}
			}
			wsProxy.setHandshakeReference(handshakeMessage.getHistoryRef());
			wsProxy.setForwardOnly(isChannelIgnored(wsProxy.getDTO()));
			wsProxy.startListeners(getListenerThreadPool(), remoteReader);
			
			synchronized (wsProxies) {
				wsProxies.put(wsProxy.getChannelId(), wsProxy);
			}
		} catch (Exception e) {
			// defensive measure to catch all possible exceptions
			// cleanly close resources
			if (localSocket != null && !localSocket.isClosed()) {
				try {
					localSocket.close();
				} catch (IOException e1) {
					logger.warn(e.getMessage(), e1);
				}
			}
			
			if (remoteReader != null) {
				try {
					remoteReader.close();
				} catch (IOException e1) {
					logger.warn(e.getMessage(), e1);
				}
			}
			
			if (remoteSocket != null && !remoteSocket.isClosed()) {
				try {
					remoteSocket.close();
				} catch (IOException e1) {
					logger.warn(e.getMessage(), e1);
				}
			}
			logger.error("Adding WebSockets channel failed due to: '" + e.getClass() + "' " + e.getMessage());
			return;
		}
	}

	/**
	 * Parses the negotiated WebSockets extensions. It splits them up into name
	 * and params of the extension. In future we want to look up if given
	 * extension is available as ZAP extension and then use their knowledge to
	 * process frames.
	 * <p>
	 * If multiple extensions are to be used, they can all be listed in a single
	 * {@link WebSocketProtocol#HEADER_EXTENSION} field or split between multiple
	 * instances of the {@link WebSocketProtocol#HEADER_EXTENSION} header field.
	 * 
	 * @param msg
	 * @return Map with extension name and parameter string.
	 */
	private Map<String, String> parseWebSocketExtensions(HttpMessage msg) {
		Vector<String> extensionHeaders = msg.getResponseHeader().getHeaders(
				WebSocketProtocol.HEADER_EXTENSION);

		if (extensionHeaders == null) {
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
		Map<String, String> wsExtensions = new LinkedHashMap<>();
		for (String extensionHeader : extensionHeaders) {
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
		}
		
		/*
		 * The interpretation of any extension parameters, and what constitutes
		 * a valid response by a server to a requested set of parameters by a
		 * client, will be defined by each such extension.
		 * 
		 * Note that the order of extensions is significant!
		 */
		
		return wsExtensions;
	}

	/**
	 * Parses negotiated protocols out of the response header.
	 * <p>
	 * The {@link WebSocketProtocol#HEADER_PROTOCOL} header is only allowed to
	 * appear once in the HTTP response (but several times in the HTTP request).
	 * 
	 * A server that speaks multiple sub-protocols has to make sure it selects
	 * one based on the client's handshake and specifies it in its handshake.
	 * 
	 * @param msg
	 * @return Name of negotiated sub-protocol or null.
	 */
	private String parseWebSocketSubProtocol(HttpMessage msg) {
		String subProtocol = msg.getResponseHeader().getHeader(
				WebSocketProtocol.HEADER_PROTOCOL);
		return subProtocol;
	}

	/**
	 * The {@link WebSocketProtocol#HEADER_VERSION} header might not always
	 * contain a number. Therefore I return a string. Use the version to choose
	 * the appropriate processing class.
	 * 
	 * @param msg
	 * @return Version of the WebSockets channel, defining the protocol.
	 */
	private String parseWebSocketVersion(HttpMessage msg) {
		String version = msg.getResponseHeader().getHeader(
				WebSocketProtocol.HEADER_VERSION);
		
		if (version == null) {
			// check for requested WebSockets version
			version = msg.getRequestHeader().getHeader(WebSocketProtocol.HEADER_VERSION);
			
			if (version == null) {
				// default to version 13 if non is given, for whatever reason
				logger.debug("No " + WebSocketProtocol.HEADER_VERSION + " header was provided - try version 13");
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
	 * @param handshakeRef
	 * @return True if connection is still alive.
	 */
	public boolean isConnected(HistoryReference handshakeRef) {
		int historyId = handshakeRef.getHistoryId();
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
	 * @return True if connection is still alive.
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
	 * @param ignoreList
     * @throws WebSocketException 
	 */
	public void setChannelIgnoreList(List<String> ignoreList) throws WebSocketException {
		preparedIgnoredChannels.clear();
		
		List<String> nonEmptyIgnoreList = new ArrayList<>();
		for (String regex : ignoreList) {
			if (regex.trim().length() > 0) {
				nonEmptyIgnoreList.add(regex);
			}
		}

		// ensure validity by compiling regular expression
		// store them for better performance
		for (String regex : nonEmptyIgnoreList) {
			if (regex.trim().length() > 0) {
				preparedIgnoredChannels.add(Pattern.compile(regex.trim(), Pattern.CASE_INSENSITIVE));
			}
		}
		
		// save list in database
		try {
			Model.getSingleton().getDb().getTableSessionUrl().setUrls(RecordSessionUrl.TYPE_EXCLUDE_FROM_WEBSOCKET, nonEmptyIgnoreList);
			ignoredChannelList = nonEmptyIgnoreList;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			
			ignoredChannelList.clear();
			preparedIgnoredChannels.clear();
			
			throw new WebSocketException("Ignore list could not be applied! Consequently no channel is ignored.");
		} finally {
			// apply to existing channels
			applyChannelIgnoreList();
		}
	}

	public List<String> getChannelIgnoreList() {
		return ignoredChannelList;
	}
	
	private void applyChannelIgnoreList() {
		synchronized (wsProxies) {
			for (Entry<Integer, WebSocketProxy> entry : wsProxies.entrySet()) {
				WebSocketProxy wsProxy = entry.getValue();
				
				if (isChannelIgnored(wsProxy.getDTO())) {
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
	public boolean isChannelIgnored(WebSocketChannelDTO channel) {
		boolean doNotStore = false;
		
		if (config.isForwardAll()) {
			// all channels are blacklisted
			doNotStore = true;
		} else if (!preparedIgnoredChannels.isEmpty()) {
			for (Pattern p : preparedIgnoredChannels) {
				Matcher m = p.matcher(channel.getFullUri());
				if (m.matches()) {
					doNotStore = true;
					break;
				}
			}
		}
		
		return doNotStore;
	}

	@Override
	public void sessionChanged(final Session session) {
		TableWebSocket table = createTableWebSocket();
		if (View.isInitialised()) {
			getWebSocketPanel().setTable(table);
		}
		storage.setTable(table);
		
		try {
			WebSocketProxy.setChannelIdGenerator(table.getMaxChannelId());
		} catch (SQLException e) {
			logger.error("Unable to retrieve current channelId value!", e);
		}
		
		if (fuzzHandler != null) {
			fuzzHandler.resume();
		}

		List<String> ignoredList = new ArrayList<>();
		try {
			List<RecordSessionUrl> recordSessionUrls = Model.getSingleton()
					.getDb().getTableSessionUrl()
					.getUrlsForType(RecordSessionUrl.TYPE_EXCLUDE_FROM_WEBSOCKET);
		
			for (RecordSessionUrl record  : recordSessionUrls) {
				ignoredList.add(record.getUrl());
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				setChannelIgnoreList(ignoredList);
			} catch (WebSocketException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	@Override
	public void sessionAboutToChange(Session session) {
		if (View.isInitialised()) {
			// Prevent the table from being used
			getWebSocketPanel().setTable(null);
			storage.setTable(null);
		}
		
		// close existing connections
		synchronized (wsProxies) {
			for (WebSocketProxy wsProxy : wsProxies.values()) {
				wsProxy.shutdown();
			}
			wsProxies.clear();
		}
		
		wsFilterListener.reset();
		
		if (fuzzHandler != null) {
			fuzzHandler.pause();
		}
	}

	@Override
	public void sessionScopeChanged(Session session) {
		// do nothing
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		this.mode = mode;
	}

	/**
	 * Returns false when either in {@link Mode#safe} or in {@link Mode#protect}
	 * and the message's channel is not in scope. Call it if you want to do
	 * "unsafe" actions like changing payloads, catch breakpoints, send custom
	 * messages, etc.
	 * 
	 * @param message
	 * @return True if operation on message is not potentially dangerous.
	 */
	public boolean isSafe(WebSocketMessageDTO message) {
		if (mode.equals(Mode.safe)) {
			return false;
		} else if (mode.equals(Mode.protect)) {
			return message.isInScope();
		} else {
			return true;
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
        	// use hex view only when previously selected
//            if (aMessage instanceof WebSocketMessageDTO) {
//                WebSocketMessageDTO msg = (WebSocketMessageDTO)aMessage;
//                
//                return (msg.opcode == WebSocketMessage.OPCODE_BINARY);
//            }
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

	/**
	 * This method initializes the dialog for crafting custom messages.
	 * 
	 * @param sender
	 * 
	 * @return
	 */
	private ManualWebSocketSendEditorDialog createManualSendDialog(WebSocketPanelSender sender) {
		ManualWebSocketSendEditorDialog sendDialog = new ManualWebSocketSendEditorDialog(getWebSocketPanel().getChannelsModel(), sender, true, "websocket.manual_send");
		sendDialog.setTitle(Constant.messages.getString("websocket.manual_send.menu"));
		return sendDialog;
	}
	
	/**
	 * This method initializes the re-send WebSocket message dialog.
	 * 
	 * @param sender
	 * 
	 * @return
	 */    
	private ManualWebSocketSendEditorDialog createReSendDialog(WebSocketPanelSender sender) {
		ManualWebSocketSendEditorDialog	resendDialog = new ManualWebSocketSendEditorDialog(getWebSocketPanel().getChannelsModel(), sender, true, "websocket.manual_resend");
		resendDialog.setTitle(Constant.messages.getString("websocket.manual_send.popup"));
		return resendDialog;
	}

	@Override
	public void nodeSelected(SiteNode node) {
		// do nothing
	}

	@Override
	public void onReturnNodeRendererComponent(SiteMapTreeCellRenderer component,
			boolean leaf, SiteNode node) {
		if (leaf) {
			HistoryReference href = component.getHistoryReferenceFromNode(node);
			boolean isWebSocketNode = href != null && href.isWebSocketUpgrade();
			if (isWebSocketNode) {
				boolean isConnected = isConnected(component.getHistoryReferenceFromNode(node));
				boolean isIncluded = node.isIncludedInScope() && !node.isExcludedFromScope();
				
				setWebSocketIcon(isConnected, isIncluded, component);
			}
		}
	}

	private void setWebSocketIcon(boolean isConnected, boolean isIncluded, SiteMapTreeCellRenderer component) {
		if (isConnected) {
			if (isIncluded) {
				component.setIcon(WebSocketPanel.connectTargetIcon);
			} else {
				component.setIcon(WebSocketPanel.connectIcon);
			}
		} else {
			if (isIncluded) {
				component.setIcon(WebSocketPanel.disconnectTargetIcon);
			} else {
				component.setIcon(WebSocketPanel.disconnectIcon);
			}
		}
	}
}
