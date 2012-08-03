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
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.view.ScanPanel;

public class ExtensionHttpSessions extends ExtensionAdaptor implements SessionChangedListener, ProxyListener {

	private static final String NAME = "ExtensionHttpSessions";

	private static final Logger log = Logger.getLogger(ExtensionHttpSessions.class);

	private HttpSessionsPanel httpSessionsPanel;

	/** The map of sessions corresponding to each site. */
	Map<String, HttpSessionsSite> sessions;

	/** The http sessions manager. */
	private HttpSessionsManager manager;

	public ExtensionHttpSessions() {
		super();
		initialize();
	}

	private void initialize() {
		this.setOrder(68);
		this.setName(NAME);
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("httpsession.desc");
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
	public void hook(ExtensionHook extensionHook) {
		super.hook(extensionHook);

		extensionHook.addSessionListener(this);
		extensionHook.addProxyListener(this);

		if (getView() != null) {
			extensionHook.getHookView().addStatusPanel(getHttpSessionsPanel());

			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuParamSearch());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddAntiCSRF());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveAntiCSRF());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddSession());
			// extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveSession());
		}
	}

	//
	// /**
	// * Gets the http sessions manager.
	// *
	// * @return the http sessions manager
	// */
	// protected HttpSessionsManager getHttpSessionsManager() {
	// if (manager == null) {
	// manager = new HttpSessionsManager(this);
	// }
	// return manager;
	// }

	protected String[] getSessionTokens(String site) {
		return new String[] { "jsessionid" };
	}

	protected HashSet<String> getSessionTokensSet(String site) {
		HashSet<String> set = new LinkedHashSet<String>();
		set.add("jsessionid");
		return set;
	}

	protected HttpSessionsPanel getHttpSessionsPanel() {
		if (httpSessionsPanel == null) {
			httpSessionsPanel = new HttpSessionsPanel(this);
		}
		return httpSessionsPanel;
	}

	@Override
	public int getProxyListenerOrder() {
		return 20;
	}

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
		HttpSessionsSite session = this.getHttpSessionsSite(site);
		boolean res = session.processHttpResponseMessage(msg);
		if (res == true) {
			log.debug("Refreshing model for site: " + site);
			this.getHttpSessionsPanel().getHttpSessionsTable().setModel(session.getModel());
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

	@Override
	public void sessionChanged(Session session) {
		log.info("Session changed."); // TODO Auto-generated method stub

	}

	@Override
	public void sessionAboutToChange(Session session) {
		log.info("Session about to change.");
	}

	@Override
	public void sessionScopeChanged(Session session) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionModeChanged(Mode mode) {
		// TODO Auto-generated method stub

	}

}
