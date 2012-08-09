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
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.ScanPanel;
import org.zaproxy.zap.view.SiteMapListener;

/**
 * The HttpSessions Extension handles the existing http sessions on the existing site. It allows the
 * management and usage of multiple sessions per site and also handles the session tokens for each
 * of them.
 */
public class ExtensionHttpSessions extends ExtensionAdaptor implements SessionChangedListener, SiteMapListener,
		ProxyListener {

	/** The Constant NAME. */
	public static final String NAME = "ExtensionHttpSessions";

	/** The Constant log. */
	private static final Logger log = Logger.getLogger(ExtensionHttpSessions.class);

	/** The http sessions panel. */
	private HttpSessionsPanel httpSessionsPanel;

	/** The options http sessions panel. */
	private OptionsHttpSessionsPanel optionsHttpSessionsPanel;

	/** The map of sessions corresponding to each site. */
	Map<String, HttpSessionsSite> sessions;

	/** The map of session tokens corresponding to each site. */
	Map<String, LinkedHashSet<String>> sessionTokens;

	/** The http sessions extension's parameters. */
	HttpSessionsParam param;

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
		this.sessionTokens = new HashMap<String, LinkedHashSet<String>>();
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
	 * @see org.parosproxy.paros.core.proxy.ProxyListener#getProxyListenerOrder() */
	@Override
	public int getProxyListenerOrder() {
		return 20;
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

			// Hook the panels
			extensionHook.getHookView().addStatusPanel(getHttpSessionsPanel());
			extensionHook.getHookView().addOptionPanel(getOptionsHttpSessionsPanel());

			// Hook the popup menus
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuSetActiveSession());
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuUnsetActiveSession());
			extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveSession());
		}
	}

	/**
	 * Gets the options panel for this extension.
	 * 
	 * @return the options session panel
	 */
	private OptionsHttpSessionsPanel getOptionsHttpSessionsPanel() {
		if (optionsHttpSessionsPanel == null) {
			optionsHttpSessionsPanel = new OptionsHttpSessionsPanel();
		}
		return optionsHttpSessionsPanel;
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
	 * Gets the parameters (options) for this extension and related classes.
	 * 
	 * @return the param
	 */
	public HttpSessionsParam getParam() {
		if (param == null) {
			param = Model.getSingleton().getOptionsParam().getHttpSessionsParam();
		}
		return param;
	}

	/**
	 * Checks if a particular token is part of the default session tokens set by the user using the
	 * options panel. The default session tokens are valid for all sites. The check is being
	 * performed in a lower-case manner, as session tokens are case-insensitive.
	 * 
	 * @param token the token
	 * @return true, if it is a default session token
	 */
	public boolean isDefaultSessionToken(String token) {
		if (getParam().getDefaultTokens().contains(token.toLowerCase()))
			return true;
		return false;
	}

	/**
	 * Checks if a particular token is a session token name for a particular site. The check is
	 * being performed in a lower-case manner, as session tokens are case-insensitive.
	 * 
	 * @param site the site
	 * @param token the token
	 * @return true, if it is session token
	 */
	public boolean isSessionToken(String site, String token) {
		HashSet<String> siteTokens = sessionTokens.get(site);
		if (siteTokens == null)
			return false;
		return siteTokens.contains(token.toLowerCase());
	}

	/**
	 * Adds a new session token for a particular site. The session tokens are case-insensitive, so
	 * this token will be added lower cased.
	 * 
	 * @param site the site
	 * @param token the token
	 */
	public void addHttpSessionToken(String site, String token) {
		LinkedHashSet<String> siteTokens = sessionTokens.get(site);
		if (siteTokens == null) {
			siteTokens = new LinkedHashSet<String>();
			sessionTokens.put(site, siteTokens);
		}
		log.info("Added new session token for site '" + site + "': " + token);
		siteTokens.add(token.toLowerCase());
	}

	/**
	 * Removes a particular session token for a site.
	 * 
	 * @param site the site
	 * @param token the token
	 */
	public void removeHttpSessionToken(String site, String token) {
		token = token.toLowerCase();
		HashSet<String> siteTokens = sessionTokens.get(site);
		if (siteTokens != null) {
			// Remove the tokens from the tokens associated with the site
			siteTokens.remove(token);
			if (siteTokens.isEmpty())
				sessionTokens.remove(site);
			// Cleanup the existing sessions
			this.getHttpSessionsSite(site).cleanupSessionToken(token);
		}
		log.info("Removed session token for site '" + site + "': " + token);
	}

	/**
	 * Gets the set of session tokens for a particular site. The session tokens are case-insensitive
	 * and are returned lower-cased.
	 * <p>
	 * The internal set of session tokens is returned, so no modifications should be done on the
	 * Session Tokens.
	 * </p>
	 * 
	 * @param site the site
	 * @return the session tokens set, if any have been set, or null, if there are no session tokens
	 *         for this site
	 */
	public final LinkedHashSet<String> getHttpSessionTokens(String site) {
		return sessionTokens.get(site);
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

	/**
	 * Gets the http sessions for a particular site. If it doesn't exist, it is created.
	 * 
	 * @param site the site
	 * @return the http sessions site container
	 */
	protected HttpSessionsSite getHttpSessionsSite(String site) {
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
	 * @see org.zaproxy.zap.view.SiteMapListener#nodeSelected(org.parosproxy.paros.model.SiteNode) */
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
