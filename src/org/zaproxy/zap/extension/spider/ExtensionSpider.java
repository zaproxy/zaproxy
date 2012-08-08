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

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.spider.SpiderParam;
import org.zaproxy.zap.view.SiteMapListener;

/**
 * The ExtensionSpider is the Extension that controls the Spider.
 */
public class ExtensionSpider extends ExtensionAdaptor implements SessionChangedListener, ProxyListener, SiteMapListener {

	/** The Constant logger. */
	private static final Logger log = Logger.getLogger(ExtensionSpider.class);

	/** The Constant defining the NAME of the extension. */
	public static final String NAME = "ExtensionSpider";

	/**
	 * The Constant PROXY_LISTENER_ORDER. Could be after the last one that saves the HttpMessage, as
	 * this ProxyListener doesn't change the HttpMessage.
	 */
	private static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;

	/** The spider panel. */
	private SpiderPanel spiderPanel = null;

	/** The options spider panel. */
	private OptionsSpiderPanel optionsSpiderPanel = null;

	/** The params for the spider. */
	private SpiderParam params = null;

	/**
	 * The list of excluded patterns of sites. Patterns are added here with the ExcludeFromSpider
	 * Popup Menu.
	 */
	private List<String> excludeList = null;

	/**
	 * Instantiates a new spider extension
	 */
	public ExtensionSpider() {
		super();
		initialize();
	}

	/**
	 * Instantiates a new extension spider.
	 * 
	 * @param name the name
	 */
	public ExtensionSpider(String name) {
		super(name);
	}

	/**
	 * This method initializes this extension.
	 */
	private void initialize() {
		this.setOrder(30);
		this.setName(NAME);

		API.getInstance().registerApiImplementor(new SpiderAPI(this));
	}

	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);
		// Register for listeners
		extensionHook.addSessionListener(this);
		extensionHook.addProxyListener(this);
		extensionHook.addSiteMapListner(this);

		// Initialize views
		if (getView() != null) {
			ExtensionHookView pv = extensionHook.getHookView();
			pv.addStatusPanel(getSpiderPanel());
			pv.addOptionPanel(getOptionsSpiderPanel());
			ExtensionHelp.enableHelpKey(getSpiderPanel(), "ui.tabs.spider");
		}

		// Register the params
		extensionHook.addOptionsParamSet(getSpiderParam());
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
		this.getSpiderPanel().clear();
		this.getSpiderPanel().reset();
		if (session == null) {
			// Closedown
			return;
		}
		// Add new hosts
		SiteNode root = (SiteNode) session.getSiteTree().getRoot();
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = root.children();
		while (en.hasMoreElements()) {
			this.getSpiderPanel().addSite(en.nextElement().getNodeName(), true);
		}
	}

	@Override
	public int getProxyListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}

	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		// The panel will handle duplicates
		String site = msg.getRequestHeader().getHostName();
		if (msg.getRequestHeader().getHostPort() > 0 && msg.getRequestHeader().getHostPort() != 80) {
			site += ":" + msg.getRequestHeader().getHostPort();
		}
		this.getSpiderPanel().addSite(site, true);
		return true;
	}

	@Override
	public boolean onHttpResponseReceive(HttpMessage msg) {
		// Do nothing
		return true;
	}

	@Override
	public void nodeSelected(SiteNode node) {
		this.getSpiderPanel().nodeSelected(node, true);
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
	 * Spider site.
	 * 
	 * @param node the node
	 * @param incPort the inc port
	 */
	public void spiderSite(SiteNode node, boolean incPort) {
		this.getSpiderPanel().scanSite(node, incPort);
	}

	/**
	 * Checks if there is a scan in progress.
	 * 
	 * @param node the node
	 * @param incPort the inc port
	 * @return true, if is scanning
	 */
	public boolean isScanning(SiteNode node, boolean incPort) {
		return this.getSpiderPanel().isScanning(node, incPort);
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

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.extension.SessionChangedListener#sessionAboutToChange(org.parosproxy
	 * .paros.model.Session) */
	@Override
	public void sessionAboutToChange(Session session) {
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.extension.Extension#getAuthor() */
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.extension.ExtensionAdaptor#getDescription() */
	@Override
	public String getDescription() {
		return Constant.messages.getString("spider.desc");
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.extension.ExtensionAdaptor#getURL() */
	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.extension.SessionChangedListener#sessionScopeChanged(org.parosproxy
	 * .paros.model.Session) */
	@Override
	public void sessionScopeChanged(Session session) {
		this.getSpiderPanel().sessionScopeChanged(session);
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.extension.SessionChangedListener#sessionModeChanged(org.parosproxy.
	 * paros.control.Control.Mode) */
	@Override
	public void sessionModeChanged(Mode mode) {
		this.getSpiderPanel().sessionModeChanged(mode);
	}

	public void startScanNode(SiteNode node) {
		this.getSpiderPanel().clear();
		this.getSpiderPanel().scanNode(node, true);
	}

	public void startScanAllInScope() {
		this.getSpiderPanel().clear();
		this.getSpiderPanel().scanAllInScope();
	}

	public void startScan(SiteNode startNode) {
		this.getSpiderPanel().clear();
		this.getSpiderPanel().scanSite(startNode, true);
	}
}