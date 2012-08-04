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
package org.zaproxy.zap.extension.httpsessions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.ScanPanel;
import org.zaproxy.zap.view.SiteMapListener;

/**
 * The Class ExtensionHttpSessions.
 */
public class ExtensionHttpSessions extends ExtensionAdaptor implements SessionChangedListener, SiteMapListener,
		ProxyListener {

	/** The Constant NAME. */
	private static final String NAME = "ExtensionHttpSessions";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionHttpSessions.class);

	/** The http sessions panel. */
	private HttpSessionsPanel httpSessionsPanel;

	/** The map of sessions corresponding to each site. */
	Map<String, HttpSessionsSite> sessions;

	/** The popup menu used to set the active session. */
	private PopupMenuSetActiveSession popupMenuSetActiveSession;

	/** The popup menu used to unset the active session. */
	private PopupMenuUnsetActiveSession popupMenuUnsetActiveSession;

	/** The popup menu used to remove a session. */
	private PopupMenuRemoveSession popupMenuRemoveSession;

	/**
	 * Instantiates a new extension http sessions.
	 */
	public ExtensionHttpSessions() {
		super();
		initialize();
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.setOrder(68);
		this.setName(NAME);
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
		return Constant.messages.getString("httpsession.desc");
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
	 * org.parosproxy.paros.extension.ExtensionAdaptor#hook(org.parosproxy.paros.extension.ExtensionHook
	 * ) */
	@Override
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);

		extensionHook.addSessionListener(this);
		extensionHook.addProxyListener(this);
		extensionHook.addSiteMapListner(this);

		if (getView() != null) {

			// Hook the panel
			extensionHook.getHookView().addStatusPanel(getHttpSessionsPanel());

			// Hook the popup menus
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuSetActiveSession());
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuUnsetActiveSession());
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveSession());
		}
	}

	/**
	 * Gets the popup menu set active session.
	 * 
	 * @return the popup menu set active session
	 */
	private PopupMenuSetActiveSession getPopupMenuSetActiveSession() {
		if (popupMenuSetActiveSession == null) {
			popupMenuSetActiveSession = new PopupMenuSetActiveSession();
			popupMenuSetActiveSession.setExtension(this);
		}
		return popupMenuSetActiveSession;
	}

	/**
	 * Gets the popup menu used to delete a session.
	 * 
	 * @return the popup menu used to delete a session
	 */
	private PopupMenuRemoveSession getPopupMenuRemoveSession() {
		if (popupMenuRemoveSession == null) {
			popupMenuRemoveSession = new PopupMenuRemoveSession();
			popupMenuRemoveSession.setExtension(this);
		}
		return popupMenuRemoveSession;
	}

	/**
	 * Gets the popup menu to unset active session.
	 * 
	 * @return the popup menu unset active session
	 */
	private PopupMenuUnsetActiveSession getPopupMenuUnsetActiveSession() {
		if (popupMenuUnsetActiveSession == null) {
			popupMenuUnsetActiveSession = new PopupMenuUnsetActiveSession();
			popupMenuUnsetActiveSession.setExtension(this);
		}
		return popupMenuUnsetActiveSession;
	}

	/**
	 * Gets the session tokens.
	 * 
	 * @param site the site
	 * @return the session tokens
	 */
	protected String[] getSessionTokens(String site) {
		return new String[] { "jsessionid" };
	}

	/**
	 * Gets the session tokens set.
	 * 
	 * @param site the site
	 * @return the session tokens set
	 */
	protected HashSet<String> getSessionTokensSet(String site) {
		HashSet<String> set = new LinkedHashSet<String>();
		set.add("jsessionid");
		return set;
	}

	/**
	 * Gets the http sessions panel.
	 * 
	 * @return the http sessions panel
	 */
	protected HttpSessionsPanel getHttpSessionsPanel() {
		if (httpSessionsPanel == null) {
			httpSessionsPanel = new HttpSessionsPanel(this);
		}
		return httpSessionsPanel;
	}

	/* (non-Javadoc)
	 * 
	 * @see org.parosproxy.paros.core.proxy.ProxyListener#getProxyListenerOrder() */
	@Override
	public int getProxyListenerOrder() {
		return 20;
	}

	@Override
	public void nodeSelected(SiteNode node) {
		// Event from SiteMapListenner
		this.getHttpSessionsPanel().nodeSelected(node);
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.core.proxy.ProxyListener#onHttpRequestSend(org.parosproxy.paros.network
	 * .HttpMessage) */
	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		// Check if we know the site and add it otherwise
		String site = msg.getRequestHeader().getHostName();
		int port = msg.getRequestHeader().getHostPort();
		if (port > 0) {
			site = site + ":" + port;
		}
		site = ScanPanel.cleanSiteName(site, true);
		this.getHttpSessionsPanel().addSite(site);

		// Forward the request for proper processing
		HttpSessionsSite session = this.getHttpSessionsSite(site);
		session.processHttpRequestMessage(msg);

		return true;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.core.proxy.ProxyListener#onHttpResponseReceive(org.parosproxy.paros
	 * .network.HttpMessage) */
	@Override
	public boolean onHttpResponseReceive(HttpMessage msg) {
		// Check if we know the site and add it otherwise
		String site = msg.getRequestHeader().getHostName();
		int port = msg.getRequestHeader().getHostPort();
		if (port > 0) {
			site = site + ":" + port;
		}
		site = ScanPanel.cleanSiteName(site, true);
		this.getHttpSessionsPanel().addSite(site);

		// Forward the request for proper processing
		HttpSessionsSite sessionsSite = this.getHttpSessionsSite(site);
		boolean res = sessionsSite.processHttpResponseMessage(msg);
		if (res == true) {
			log.debug("Refreshing model for site: " + site);
			sessionsSite.getModel().fireTableDataChanged();
		}

		return true;
	}

	/**
	 * Gets the http sessions for a particular site.
	 * 
	 * @param site the site
	 * @return the http sessions site container
	 */
	public HttpSessionsSite getHttpSessionsSite(String site) {
		if (sessions == null) {
			sessions = new HashMap<String, HttpSessionsSite>();
		}
		HttpSessionsSite hss = sessions.get(site);
		if (hss == null) {
			hss = new HttpSessionsSite(this, site);
			sessions.put(site, hss);
		}
		return hss;
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.extension.SessionChangedListener#sessionChanged(org.parosproxy.paros
	 * .model.Session) */
	@Override
	public void sessionChanged(Session session) {
		log.info("Session changed."); // TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.extension.SessionChangedListener#sessionAboutToChange(org.parosproxy
	 * .paros.model.Session) */
	@Override
	public void sessionAboutToChange(Session session) {
		log.info("Session about to change.");
	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.extension.SessionChangedListener#sessionScopeChanged(org.parosproxy
	 * .paros.model.Session) */
	@Override
	public void sessionScopeChanged(Session session) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * 
	 * @see
	 * org.parosproxy.paros.extension.SessionChangedListener#sessionModeChanged(org.parosproxy.
	 * paros.control.Control.Mode) */
	@Override
	public void sessionModeChanged(Mode mode) {
		// TODO Auto-generated method stub

	}

}
