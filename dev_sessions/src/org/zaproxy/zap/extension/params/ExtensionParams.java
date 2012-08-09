/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 The ZAP development team
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
package org.zaproxy.zap.extension.params;

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.db.RecordParam;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HtmlParameter;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.anticsrf.ExtensionAntiCSRF;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;
import org.zaproxy.zap.extension.search.ExtensionSearch;
import org.zaproxy.zap.view.SiteMapListener;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionParams extends ExtensionAdaptor 
		implements SessionChangedListener, /*ProxyListener, */ SiteMapListener{

	public static final String NAME = "ExtensionParams"; 
	private ParamsPanel paramsPanel = null;
	private Set<String> stdSessionParamNames = null;
	private PopupMenuParamSearch popupMenuSearch = null;
	private PopupMenuAddAntiCSRF popupMenuAddAntiCsrf = null;
	private PopupMenuRemoveAntiCSRF popupMenuRemoveAntiCsrf = null;
	private PopupMenuAddSession popupMenuAddSession = null;
	private PopupMenuRemoveSession popupMenuRemoveSession = null;
	private Map <String, SiteParameters> siteParamsMap = new HashMap <String, SiteParameters>();
	
    private Logger logger = Logger.getLogger(ExtensionParams.class);
    
	/**
     * 
     */
    public ExtensionParams() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionParams(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName(NAME);
        this.setOrder(58);

        API.getInstance().registerApiImplementor(new ParamsAPI(this));
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    extensionHook.addSessionListener(this);
        extensionHook.addSiteMapListner(this);
	    
	    if (getView() != null) {
	        @SuppressWarnings("unused")
			ExtensionHookView pv = extensionHook.getHookView();
	        extensionHook.getHookView().addStatusPanel(getParamsPanel());

	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuParamSearch());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddAntiCSRF());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveAntiCSRF());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddSession());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuRemoveSession());

	        ExtensionHelp.enableHelpKey(getParamsPanel(), "ui.tabs.params");
	    }
	}
	
	private PopupMenuParamSearch getPopupMenuParamSearch() {
		if (popupMenuSearch == null) {
			popupMenuSearch = new PopupMenuParamSearch();
			popupMenuSearch.setExtension(this);
		}
		return popupMenuSearch;
	}
	
	private PopupMenuAddAntiCSRF getPopupMenuAddAntiCSRF() {
		if (popupMenuAddAntiCsrf == null) {
			popupMenuAddAntiCsrf = new PopupMenuAddAntiCSRF();
			popupMenuAddAntiCsrf.setExtension(this);
		}
		return popupMenuAddAntiCsrf;
	}

	private PopupMenuRemoveAntiCSRF getPopupMenuRemoveAntiCSRF() {
		if (popupMenuRemoveAntiCsrf == null) {
			popupMenuRemoveAntiCsrf = new PopupMenuRemoveAntiCSRF();
			popupMenuRemoveAntiCsrf.setExtension(this);
		}
		return popupMenuRemoveAntiCsrf;
	}

	private PopupMenuAddSession getPopupMenuAddSession() {
		if (popupMenuAddSession == null) {
			popupMenuAddSession = new PopupMenuAddSession();
			popupMenuAddSession.setExtension(this);
		}
		return popupMenuAddSession;
	}

	private PopupMenuRemoveSession getPopupMenuRemoveSession() {
		if (popupMenuRemoveSession == null) {
			popupMenuRemoveSession = new PopupMenuRemoveSession();
			popupMenuRemoveSession.setExtension(this);
		}
		return popupMenuRemoveSession;
	}

	protected ParamsPanel getParamsPanel() {
		if (paramsPanel == null) {
			paramsPanel = new ParamsPanel(this);
		}
		return paramsPanel;
	}
	
	@Override
	public void sessionChanged(final Session session)  {
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
		// Clear all scans
		siteParamsMap = new HashMap <String, SiteParameters>();
		this.getParamsPanel().reset();
		if (session == null) {
			// Closedown
			return;
		}
		
		// Repopulate
		SiteNode root = (SiteNode)session.getSiteTree().getRoot();
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = root.children();
		while (en.hasMoreElements()) {
			String site = en.nextElement().getNodeName();
			if (site.indexOf("//") >= 0) {
				site = site.substring(site.indexOf("//") + 2);
			}
			this.getParamsPanel().addSite(site);
		}
		
		try {
			List<RecordParam> params = Model.getSingleton().getDb().getTableParam().getAll();
			
			for (RecordParam param : params) {
				SiteParameters sps = this.getSiteParameters(param.getSite());
				sps.addParam(param.getSite(), param);
				
			}
		} catch (SQLException e) {
            logger.error(e.getMessage(), e);
		}

	}

	public boolean onHttpRequestSend(HttpMessage msg) {
		
		// Check we know the site
		String site = msg.getRequestHeader().getHostName();
		int port = msg.getRequestHeader().getHostPort();
		if (port > 0) {
			site = site + ":" + port;
		}
		this.getParamsPanel().addSite(site);
		
		SiteParameters sps = this.siteParamsMap.get(site);
		if (sps == null) {
			sps = new SiteParameters(this, site);
			this.siteParamsMap.put(site, sps);
		}
		
		// Cookie Parameters
		TreeSet<HtmlParameter> params;
		Iterator<HtmlParameter> iter;
		try {
			params = msg.getCookieParams();
			iter = params.iterator();
			while (iter.hasNext()) {
				persist(sps.addParam(site, iter.next(), msg));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		// URL Parameters
		params = msg.getUrlParams();
		iter = params.iterator();
		while (iter.hasNext()) {
			persist(sps.addParam(site, iter.next(), msg));
		}

		// Form Parameters
		
		ExtensionAntiCSRF extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);
		
		params = msg.getFormParams();
		iter = params.iterator();
		HtmlParameter param;
		while (iter.hasNext()) {
			param = iter.next();
			if (extAntiCSRF != null && extAntiCSRF.isAntiCsrfToken(param.getName())) {
				param.addFlag(HtmlParameter.Flags.anticsrf.name());
			}
			persist(sps.addParam(site, param, msg));
		}
		
		return true;
	}
	
	private String setToString (Set<String> set) {
		StringBuilder sb = new StringBuilder();
		if (set == null) {
			return "";
		}
		for (String str : set) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			// Escape all commas in the values
			sb.append(str.replace(",", "%2C"));
		}
		return sb.toString();
	}

	private void persist(HtmlParameterStats param) {
		try {
			if (param.getId() < 0) {
				// Its a new one
				RecordParam rp = Model.getSingleton().getDb().getTableParam().insert(
						param.getSite(), param.getType().name(), param.getName(), param.getTimesUsed(), 
						setToString(param.getFlags()), setToString(param.getValues()));
				param.setId(rp.getParamId());
			} else {
				// Its an existing one
				Model.getSingleton().getDb().getTableParam().update(
						param.getId(), param.getTimesUsed(), setToString(param.getFlags()), setToString(param.getValues()));
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public boolean onHttpResponseReceive(HttpMessage msg) {
		
		// Check we know the site
		String site = msg.getRequestHeader().getHostName(); 
		int port = msg.getRequestHeader().getHostPort();
		if (port > 0) {
			site = site + ":" + port;
		}
		this.getParamsPanel().addSite(site);
		
		SiteParameters sps = this.getSiteParameters(site);

		// Cookie Parameters
		TreeSet<HtmlParameter> params = msg.getCookieParams();
		Iterator<HtmlParameter> iter = params.iterator();
		while (iter.hasNext()) {
			persist(sps.addParam(site, iter.next(), msg));
		}

		// TODO Only do if response URL different to request? 
		// URL Parameters
		/*
		params = msg.getUrlParams();
		iter = params.iterator();
		while (iter.hasNext()) {
			sps.addParam(iter.next());
		}
		*/
		
		return true;
	}

	@Override
	public void nodeSelected(SiteNode node) {
		// Event from SiteMapListenner
		this.getParamsPanel().nodeSelected(node);
	}
	
	public Set<String> getStdSessionParamNames() {
		if (this.stdSessionParamNames == null) {
			this.stdSessionParamNames = new HashSet<String>();
			// All lowercase
			// TODO add the rest, or read from configs?
			this.stdSessionParamNames.add("jsessionid");
		}
		return stdSessionParamNames;
	}
	
	protected void searchForSelectedParam() {

		HtmlParameterStats item = this.getParamsPanel().getSelectedParam();
		if (item != null) {
			ExtensionSearch extSearch = 
				(ExtensionSearch) Control.getSingleton().getExtensionLoader().getExtension(ExtensionSearch.NAME);

			if (extSearch != null) {
				if (HtmlParameter.Type.url.equals(item.getType())) {
					extSearch.search("[?&]" + item.getName() + "=.*", ExtensionSearch.Type.URL, true, false);
				} else if (HtmlParameter.Type.cookie.equals(item.getType())) {
						extSearch.search(/*".*" + */item.getName() + "=.*", ExtensionSearch.Type.Header, true, false);
				} else {
					// FORM
					extSearch.search(/*".*" + */item.getName() + "=.*", ExtensionSearch.Type.Request, true, false);
				}
			}
		}
	}

	public void addAntiCsrfToken() {
		HtmlParameterStats item = this.getParamsPanel().getSelectedParam();
		
		ExtensionAntiCSRF extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);

		if (extAntiCSRF != null && item != null) {
			extAntiCSRF.addAntiCsrfTokenName(item.getName());
			item.addFlag(HtmlParameter.Flags.anticsrf.name());
			// Repaint so change shows up
			this.getParamsPanel().getParamsTable().repaint();

			// Dont think we need to do this... at least until rescan option implemented ...
			//Control.getSingleton().getMenuToolsControl().options(Constant.messages.getString("options.acsrf.title"));
			
		}

	}

	public void removeAntiCsrfToken() {
		HtmlParameterStats item = this.getParamsPanel().getSelectedParam();
		
		ExtensionAntiCSRF extAntiCSRF = 
			(ExtensionAntiCSRF) Control.getSingleton().getExtensionLoader().getExtension(ExtensionAntiCSRF.NAME);

		if (extAntiCSRF != null && item != null) {
			extAntiCSRF.removeAntiCsrfTokenName(item.getName());
			item.removeFlag(HtmlParameter.Flags.anticsrf.name());
			// Repaint so change shows up
			this.getParamsPanel().getParamsTable().repaint();

			// Dont think we need to do this... at least until rescan option implemented ...
			//Control.getSingleton().getMenuToolsControl().options(Constant.messages.getString("options.acsrf.title"));
		}
	}

	public SiteParameters getSiteParameters(String site) {
		SiteParameters sps = this.siteParamsMap.get(site);
		if (sps == null) {
			sps = new SiteParameters(this, site);
			siteParamsMap.put(site, sps);
		}
		return sps;
	}

	public Collection<SiteParameters> getAllSiteParameters() {
		Collection<SiteParameters> values = this.siteParamsMap.values();
		return values;
	}

	/**
	 * Adds a new session token from the selected parameter. Also notifies the
	 * {@link ExtensionHttpSessions} if it's active.
	 */
	public void addSessionToken() {
		// Get the selected parameter
		HtmlParameterStats item = this.getParamsPanel().getSelectedParam();
		if (item != null) {
			
			// If the HttpSessions extension is active, notify it of the new session token
			ExtensionHttpSessions extSession = (ExtensionHttpSessions) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionHttpSessions.NAME);
			if (extSession != null && item != null) {
				extSession.addHttpSessionToken(this.getParamsPanel().getCurrentSite(), item.getName());
			}
			
			// Flag the item accordingly
			item.addFlag(HtmlParameter.Flags.session.name());
			// Repaint so change shows up
			this.getParamsPanel().getParamsTable().repaint();
		}
	}

	/**
	 * Removes the currently selected parameter as a session token. Also notifies the
	 * {@link ExtensionHttpSessions} if it's active.
	 */
	public void removeSessionToken() {
		HtmlParameterStats item = this.getParamsPanel().getSelectedParam();

		if (item != null) {
			// If the HttpSessions extension is active, notify it of the removed session token
			ExtensionHttpSessions extSession = (ExtensionHttpSessions) Control.getSingleton().getExtensionLoader()
					.getExtension(ExtensionHttpSessions.NAME);
			if (extSession != null) {
				extSession.removeHttpSessionToken(this.getParamsPanel().getCurrentSite(), item.getName());
			}

			// Unflag the item accordingly
			item.removeFlag(HtmlParameter.Flags.session.name());
			// Repaint so change shows up
			this.getParamsPanel().getParamsTable().repaint();
		}
	}

	public HtmlParameterStats getSelectedParam() {
		return this.getParamsPanel().getSelectedParam();
	}

	@Override
	public void sessionAboutToChange(Session session) {
	}

	@Override
	public void sessionScopeChanged(Session session) {
	}

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("params.desc");
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
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}
}