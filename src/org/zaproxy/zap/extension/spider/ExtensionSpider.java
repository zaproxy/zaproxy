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
 * 
 * Note that this extension and the other classes in this package are heavily 
 * based on the original Paros ExtensionSpider! 
 */

package org.zaproxy.zap.extension.spider;

import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.KeyStroke;

import org.apache.commons.httpclient.URI;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ScanController;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.spider.filters.FetchFilter;
import org.zaproxy.zap.spider.filters.ParseFilter;
import org.zaproxy.zap.spider.filters.HttpPrefixFetchFilter;
import org.zaproxy.zap.spider.parser.SpiderParser;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.ZapMenuItem;

/**
 * The ExtensionSpider is the Extension that controls the Spider.
 */
public class ExtensionSpider extends ExtensionAdaptor implements SessionChangedListener, ScanController<SpiderScan> {

	public static final int EXTENSION_ORDER = 30;
	
	/** The Constant logger. */
	private static final Logger log = Logger.getLogger(ExtensionSpider.class);

	/** The Constant defining the NAME of the extension. */
	public static final String NAME = "ExtensionSpider";

	/** The spider panel. */
	private SpiderPanel spiderPanel = null;

	SpiderDialog spiderDialog = null;

	/** The options spider panel. */
	private OptionsSpiderPanel optionsSpiderPanel = null;

	/** The params for the spider. */
	private SpiderParam params = null;
	
	private List<SpiderParser> customParsers;
	private List<FetchFilter> customFetchFilters;
	private List<ParseFilter> customParseFilters;

	private SpiderAPI spiderApi;
	
	private SpiderScanController scanController = null;

	/**
	 * The list of excluded patterns of sites. Patterns are added here with the ExcludeFromSpider
	 * Popup Menu.
	 */
	private List<String> excludeList = null;

	private ZapMenuItem menuItemCustomScan = null;

	/**
	 * Instantiates a new spider extension.
	 */
	public ExtensionSpider() {
		super(NAME);
		initialize();
	}

	/**
	 * This method initializes this extension.
	 */
	private void initialize() {
		this.setOrder(EXTENSION_ORDER);
		this.customParsers = new LinkedList<>();
		this.customFetchFilters = new LinkedList<>();
		this.customParseFilters = new LinkedList<>();
		this.scanController = new SpiderScanController(this);
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		// Register for listeners
		extensionHook.addSessionListener(this);

		// Initialize views
		if (getView() != null) {
			extensionHook.getHookMenu().addToolsMenuItem(getMenuItemCustomScan());
			extensionHook.getHookView().addStatusPanel(getSpiderPanel());
			extensionHook.getHookView().addOptionPanel(getOptionsSpiderPanel());
			ExtensionHelp.enableHelpKey(getSpiderPanel(), "ui.tabs.spider");
		}

		// Register the params
		extensionHook.addOptionsParamSet(getSpiderParam());

		// Register as an API implementor
		spiderApi = new SpiderAPI(this);
		spiderApi.addApiOptions(getSpiderParam());
		extensionHook.addApiImplementor(spiderApi);
	}

	@Override
	public List<String> getActiveActions() {
		List<SpiderScan> activeSpiders = scanController.getActiveScans();
		if (activeSpiders.isEmpty()) {
			return null;
		}

		String spiderActionPrefix = Constant.messages.getString("spider.activeActionPrefix");
		List<String> activeActions = new ArrayList<>(activeSpiders.size());
		for (SpiderScan activeSpider : activeSpiders) {
			activeActions.add(MessageFormat.format(spiderActionPrefix, activeSpider.getDisplayName()));
		}
		return activeActions;
	}

	/**
	 * Gets the spider parameters (options).
	 * 
	 * @return the spider parameters
	 */
	protected SpiderParam getSpiderParam() {
		if (params == null) {
			params = new SpiderParam();
		}
		return params;
	}

	/**
	 * Gets the spider panel.
	 * 
	 * @return the spider panel
	 */
	protected SpiderPanel getSpiderPanel() {
		if (spiderPanel == null) {
			spiderPanel = new SpiderPanel(this, getSpiderParam());
		}
		return spiderPanel;
	}
	
	@Override
	public void sessionAboutToChange(Session session) {
		// Shut all of the scans down and remove them
		this.scanController.reset();
		if (View.isInitialised()) {
			this.getSpiderPanel().reset();
			if (spiderDialog != null) {
				spiderDialog.reset();
			}
		}
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
				log.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Session changed event handler.
	 * 
	 * @param session the session
	 */
	private void sessionChangedEventHandler(Session session) {
		// Clear all scans
		if (View.isInitialised()) {
			this.getSpiderPanel().reset();
		}
		if (session == null) {
			// Closedown
			return;
		}
	}

	/**
	 * Gets the options spider panel.
	 * 
	 * @return the options spider panel
	 */
	private OptionsSpiderPanel getOptionsSpiderPanel() {
		if (optionsSpiderPanel == null) {
			optionsSpiderPanel = new OptionsSpiderPanel();
		}
		return optionsSpiderPanel;
	}

	/**
	 * Sets the exclude list.
	 * 
	 * @param ignoredRegexs the new exclude list
	 */
	public void setExcludeList(List<String> ignoredRegexs) {
		this.excludeList = ignoredRegexs;
	}

	/**
	 * Gets the exclude list.
	 * 
	 * @return the exclude list
	 */
	public List<String> getExcludeList() {
		return excludeList;
	}


	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("spider.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public void sessionScopeChanged(Session session) {
		if (View.isInitialised()) {
			this.getSpiderPanel().sessionScopeChanged(session);
		}
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		if (View.isInitialised()) {
			this.getSpiderPanel().sessionModeChanged(mode);
			getMenuItemCustomScan().setEnabled( ! Mode.safe.equals(mode));
		}
	}

	/**
	 * Start scan node.
	 * 
	 * @param node the node
	 */
	public void startScanNode(SiteNode node) {
		Target target = new Target(node);
		target.setRecurse(true);
		this.startScan(target, null, null);
	}
	
	/**
	 * Start the scan of an URL (Node) from the POV of a User.
	 * 
	 * @param node the node
	 */
	public void startScanNode(SiteNode node, User user) {
		Target target = new Target(node);
		target.setRecurse(true);
		this.startScan(target, user, null);
	}

	/**
	 * Start scan all in scope.
	 */
	public void startScanAllInScope() {
		Target target = new Target(true);
		target.setRecurse(true);
		this.startScan(target, null, null);
	}

	/**
	 * Start scan.
	 * 
	 * @param startNode the start node
	 */
	public void startScan(SiteNode startNode) {
		Target target = new Target(startNode);
		target.setRecurse(true);
		this.startScan(target, null, null);
	}

	/**
	 * Start scan all in context, from the POV of an User.
	 */
	public void startScanAllInContext(Context context, User user) {
		Target target = new Target(context);
		target.setRecurse(true);
		this.startScan(target, user, null);
	}
	
	
	@Override
    public void destroy() {
		// Shut all of the scans down
		this.stopAllScans();
		if (View.isInitialised()) {
			this.getSpiderPanel().reset();
		}
	}

	/**
	 * Gets the custom parsers loaded.
	 *
	 * @return the custom parsers
	 */
	public List<SpiderParser> getCustomParsers() {
		return customParsers;
	}
	
	/**
	 * Gets the custom fetch filters loaded.
	 *
	 * @return the custom fetch filters
	 */
	public List<FetchFilter> getCustomFetchFilters() {
		return customFetchFilters;
	}

	/**
	 * Gets the custom parse filters loaded.
	 *
	 * @return the custom parse filters
	 */
	public List<ParseFilter> getCustomParseFilters() {
		return customParseFilters;
	}

	/**
	 * Adds a new custom Spider Parser. The parser is added at the beginning of the parsers list so
	 * it will be processed before other already loaded parsers and before the default parsers.
	 * <p/>
	 * This method should be used to customize the Spider from any other extension of ZAP. The
	 * parsers added will be loaded whenever starting any scan.
	 * 
	 * @param parser the parser
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @see #removeCustomParser(SpiderParser)
	 */
	public void addCustomParser(SpiderParser parser) {
		validateParameterNonNull(parser, "parser");
		this.customParsers.add(parser);
	}

	private static void validateParameterNonNull(Object object, String name) {
		if (object == null) {
			throw new IllegalArgumentException("Parameter " + name + " must not be null.");
		}
	}

	/**
	 * Removes the given spider parser.
	 * <p>
	 * Nothing happens if the given parser was not previously added.
	 * 
	 * @param parser the parser
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @since TODO add version
	 * @see #addCustomParser(SpiderParser)
	 */
	public void removeCustomParser(SpiderParser parser) {
		validateParameterNonNull(parser, "parser");
		this.customParsers.remove(parser);
	}

	/**
	 * Adds a custom fetch filter that would be used during the spidering.
	 * <p/>
	 * This method should be used to customize the Spider from any other extension of ZAP. The
	 * filters added will be loaded whenever starting any scan.
	 * 
	 * @param filter the filter
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @see #removeCustomFetchFilter(FetchFilter)
	 */
	public void addCustomFetchFilter(FetchFilter filter) {
		validateParameterNonNull(filter, "filter");
		this.customFetchFilters.add(filter);
	}

	/**
	 * Removes the given fetch filter.
	 * <p>
	 * Nothing happens if the given filter was not previously added.
	 * 
	 * @param filter the filter
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @since TODO add version
	 * @see #addCustomFetchFilter(FetchFilter)
	 */
	public void removeCustomFetchFilter(FetchFilter filter) {
		validateParameterNonNull(filter, "filter");
		this.customFetchFilters.remove(filter);
	}

	/**
	 * Adds a custom parse filter that would be used during the spidering.
	 * <p/>
	 * This method should be used to customize the Spider from any other extension of ZAP. The
	 * filters added will be loaded whenever starting any scan.
	 * 
	 * @param filter the filter
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @see #removeCustomParseFilter(ParseFilter)
	 */
	public void addCustomParseFilter(ParseFilter filter) {
		validateParameterNonNull(filter, "filter");
		this.customParseFilters.add(filter);
	}

	/**
	 * Removes the given parse filter.
	 * <p>
	 * Nothing happens if the given filter was not previously added.
	 * 
	 * @param filter the filter
	 * @throws IllegalArgumentException if the given parameter is {@code null}.
	 * @since TODO add version
	 * @see #addCustomParseFilter(ParseFilter)
	 */
	public void removeCustomParseFilter(ParseFilter filter) {
		validateParameterNonNull(filter, "filter");
		this.customParseFilters.remove(filter);
	}

	/**
	 * Starts a new spider scan using the given target and, optionally, spidering from the perspective of a user and with custom
	 * configurations.
	 * <p>
	 * The spider scan will use the most appropriate display name created from the given target, user and custom configurations.
	 *
	 * @param target the target that will be spidered
	 * @param user the user that will be used to spider, might be {@code null}
	 * @param customConfigurations other custom configurations for the spider, might be {@code null}
	 * @return the ID of the spider scan
	 * @since 2.5.0
	 * @see #startScan(String, Target, User, Object[])
	 * @throws IllegalStateException if the target or custom configurations are not allowed in the current
	 *             {@link org.parosproxy.paros.control.Control.Mode mode}.
	 */
	public int startScan(Target target, User user, Object[] customConfigurations) {
		return startScan(createDisplayName(target, customConfigurations), target, user, customConfigurations);
	}

	/**
	 * Creates the display name for the given target and, optionally, the given custom configurations.
	 *
	 * @param target the target that will be spidered
	 * @param customConfigurations other custom configurations for the spider, might be {@code null}
	 * @return a {@code String} containing the display name, never {@code null}
	 */
	private String createDisplayName(Target target, Object[] customConfigurations) {
		HttpPrefixFetchFilter subtreeFecthFilter = getUriPrefixFecthFilter(customConfigurations);
		if (subtreeFecthFilter != null) {
			return abbreviateDisplayName(subtreeFecthFilter.getNormalisedPrefix());
		}

		if (target.getContext() != null) {
			return Constant.messages.getString("context.prefixName", target.getContext().getName());
		} else if (target.isInScopeOnly()) {
			return Constant.messages.getString("target.allInScope");
		} else if (target.getStartNode() == null) {
			if (customConfigurations != null) {
				for (Object customConfiguration : customConfigurations) {
					if (customConfiguration instanceof URI) {
						return abbreviateDisplayName(((URI) customConfiguration).toString());
					}
				}
			}
			return Constant.messages.getString("target.empty");
		}
		return abbreviateDisplayName(target.getStartNode().getHierarchicNodeName(false));
	}

	/**
	 * Gets the {@code HttpPrefixFetchFilter} from the given {@code customConfigurations}.
	 *
	 * @param customConfigurations the custom configurations of the spider
	 * @return the {@code HttpPrefixFetchFilter} found, {@code null} otherwise.
	 */
	private HttpPrefixFetchFilter getUriPrefixFecthFilter(Object[] customConfigurations) {
		if (customConfigurations != null) {
			for (Object customConfiguration : customConfigurations) {
				if (customConfiguration instanceof HttpPrefixFetchFilter) {
					return (HttpPrefixFetchFilter) customConfiguration;
				}
			}
		}
		return null;
	}

	/**
	 * Abbreviates (the middle of) the given display name if greater than 30 characters.
	 *
	 * @param displayName the display name that might be abbreviated
	 * @return the, possibly, abbreviated display name
	 */
	private static String abbreviateDisplayName(String displayName) {
		return StringUtils.abbreviateMiddle(displayName, "..", 30);
	}

	/**
	 * Starts a new spider scan, with the given display name, using the given target and, optionally, spidering from the
	 * perspective of a user and with custom configurations.
	 * <p>
	 * <strong>Note:</strong> The preferred method to start the scan is with {@link #startScan(Target, User, Object[])}, unless
	 * a custom display name is really needed.
	 * 
	 * @param target the target that will be spidered
	 * @param user the user that will be used to spider, might be {@code null}
	 * @param customConfigurations other custom configurations for the spider, might be {@code null}
	 * @return the ID of the spider scan
	 * @throws IllegalStateException if the target or custom configurations are not allowed in the current
	 *             {@link org.parosproxy.paros.control.Control.Mode mode}.
	 */
	@SuppressWarnings({"fallthrough"})
	@Override
	public int startScan(String displayName, Target target, User user, Object[] customConfigurations) {
		switch (Control.getSingleton().getMode()) {
		case safe:
			throw new IllegalStateException("Scans are not allowed in Safe mode");
		case protect:
			String uri = getTargetUriOutOfScope(target, customConfigurations);
			if (uri != null) {
				throw new IllegalStateException("Scans are not allowed on targets not in scope when in Protected mode: " + uri);
			}
			//$FALL-THROUGH$
		case standard:
		case attack:
			// No problem
			break;
		}

		int id = this.scanController.startScan(displayName, target, user, customConfigurations);
    	if (View.isInitialised()) {
    		SpiderScan scanner = this.scanController.getScan(id);
			this.getSpiderPanel().scannerStarted(scanner);
    		scanner.setListener(getSpiderPanel());	// So the UI gets updated
    		this.getSpiderPanel().switchView(scanner);
    		this.getSpiderPanel().setTabFocus();
    	}
    	return id;
	}

	/**
	 * Returns the first URI that is out of scope in the given {@code target}.
	 *
	 * @param target the target that will be checked
	 * @return a {@code String} with the first URI out of scope, {@code null} if none found
	 * @since 2.5.0
	 * @see Session#isInScope(String)
	 */
	protected String getTargetUriOutOfScope(Target target) {
		return getTargetUriOutOfScope(target, null);
	}

	/**
	 * Returns the first URI that is out of scope in the given {@code target} or {@code contextSpecificObjects}.
	 *
	 * @param target the target that will be checked
	 * @param contextSpecificObjects other {@code Objects} used to enhance the target
	 * @return a {@code String} with the first URI out of scope, {@code null} if none found
	 * @since 2.5.0
	 * @see Session#isInScope(String)
	 */
	protected String getTargetUriOutOfScope(Target target, Object[] contextSpecificObjects) {
		List<StructuralNode> nodes = target.getStartNodes();
		if (nodes != null) {
			for (StructuralNode node : nodes) {
				if (node == null) {
					continue;
				}
				if (node instanceof StructuralSiteNode) {
					SiteNode siteNode = ((StructuralSiteNode) node).getSiteNode();
					if (!siteNode.isIncludedInScope()) {
						return node.getURI().toString();
					}
				} else {
					String uri = node.getURI().toString();
					if (!isTargetUriInScope(uri)) {
						return uri;
					}
				}
			}
		}
		if (contextSpecificObjects != null) {
			for (Object obj : contextSpecificObjects) {
				if (obj instanceof URI) {
					String uri = ((URI) obj).toString();
					if (!isTargetUriInScope(uri)) {
						return uri;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Tells whether or not the given {@code uri} is in scope.
	 *
	 * @param uri the uri that will be checked
	 * @return {@code true} if the {@code uri} is in scope, {@code false} otherwise
	 * @since 2.5.0
	 * @see Session#isInScope(String)
	 */
	protected boolean isTargetUriInScope(String uri) {
		if (uri == null) {
			return false;
		}
		return getModel().getSession().isInScope(uri);
	}

	@Override
	public List<SpiderScan> getAllScans() {
		return this.scanController.getAllScans();
	}

	@Override
	public List<SpiderScan> getActiveScans() {
		return this.scanController.getActiveScans();
	}

	@Override
	public SpiderScan getScan(int id) {
		return this.scanController.getScan(id);
	}

	@Override
	public void stopScan(int id) {
		this.scanController.stopScan(id);
	}

	@Override
	public void stopAllScans() {
		this.scanController.stopAllScans();		
	}

	@Override
	public void pauseScan(int id) {
		this.scanController.pauseScan(id);		
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getSpiderPanel().updateScannerUI();
		}
	}

	@Override
	public void pauseAllScans() {
		this.scanController.pauseAllScans();		
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getSpiderPanel().updateScannerUI();
		}
	}

	@Override
	public void resumeScan(int id) {
		this.scanController.resumeScan(id);		
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getSpiderPanel().updateScannerUI();
		}
	}

	@Override
	public void resumeAllScans() {
		this.scanController.resumeAllScans();
		if (View.isInitialised()) {
			// Update the UI in case this was initiated from the API
			this.getSpiderPanel().updateScannerUI();
		}
	}

	@Override
	public SpiderScan removeScan(int id) {
		return this.scanController.removeScan(id);
	}

	@Override
	public int removeAllScans() {
		return this.scanController.removeAllScans();
	}

	@Override
	public int removeFinishedScans() {
		return this.scanController.removeFinishedScans();
	}

	@Override
	public SpiderScan getLastScan() {
		return this.scanController.getLastScan();
	}

    private ZapMenuItem getMenuItemCustomScan() {
        if (menuItemCustomScan  == null) {
            menuItemCustomScan = new ZapMenuItem("menu.tools.spider",
                    KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.ALT_MASK, false));
            menuItemCustomScan.setEnabled(Control.getSingleton().getMode() != Mode.safe);

            menuItemCustomScan.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	showSpiderDialog(null);
                }
            });

        }
        
        return menuItemCustomScan;
    }

	public void showSpiderDialog(SiteNode node) {
		if (spiderDialog == null) {
			spiderDialog = new SpiderDialog(this, View.getSingleton().getMainFrame(), new Dimension(700, 400));
		}
		if (spiderDialog.isVisible()) {
			// Its behind you! Actually not needed no the window is alwaysOnTop, but keeping in case we change that ;)
			spiderDialog.toFront();
			return;
		}
		if (node != null) {
			spiderDialog.init(new Target(node));
		} else {
			// Keep the previous target
			spiderDialog.init(null);
		}
		spiderDialog.setVisible(true);
	}
	
    @Override
    public boolean supportsLowMemory() {
    	return true;
    }

	/**
	 * No database tables used, so all supported
	 */
	@Override
	public boolean supportsDb(String type) {
		return true;
	}
}