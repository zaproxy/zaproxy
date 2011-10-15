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
 * 
 * Note that this extension ane the other classes in this package are heavily 
 * based on the orriginal Paros ExtensionSpider! 
 */
package org.zaproxy.zap.extension.spider;

import java.awt.EventQueue;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.spider.OptionsSpiderPanel;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.view.SiteMapListener;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionSpider extends ExtensionAdaptor 
		implements SessionChangedListener, ProxyListener, SiteMapListener {

	private SpiderPanel spiderPanel = null;
    private PopupMenuSpider popupMenuSpider = null;
    // TODO testing
	private OptionsSpiderPanel optionsSpiderPanel = null;
	private org.parosproxy.paros.core.spider.SpiderParam params = null;
    private Logger logger = Logger.getLogger(ExtensionSpider.class);
	private List<String> excludeList = null;
	private PopupExcludeFromSpiderMenu popupExcludeFromSpiderMenu = null;
    
	/**
     * 
     */
    public ExtensionSpider() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionSpider(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionSpider");
        
        API.getInstance().registerApiImplementor(new SpiderAPI(this));

	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    extensionHook.addSessionListener(this);
        extensionHook.addProxyListener(this);
        extensionHook.addSiteMapListner(this);
	    
	    if (getView() != null) {
	        @SuppressWarnings("unused")
			ExtensionHookView pv = extensionHook.getHookView();
	        extensionHook.getHookView().addStatusPanel(getSpiderPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsSpiderPanel());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuSpider());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromSpiderMenu());

        	ExtensionHelp.enableHelpKey(getSpiderPanel(), "ui.tabs.spider");
	    }
        extensionHook.addOptionsParamSet(getSpiderParam());
	}
	
	private PopupExcludeFromSpiderMenu getPopupExcludeFromSpiderMenu() {
		if (popupExcludeFromSpiderMenu == null) {
			popupExcludeFromSpiderMenu = new PopupExcludeFromSpiderMenu();
		}
		return popupExcludeFromSpiderMenu;
	}
	protected org.parosproxy.paros.core.spider.SpiderParam getSpiderParam() {
		if (params == null) {
			params = new org.parosproxy.paros.core.spider.SpiderParam();
		}
		return params;
	}

	protected SpiderPanel getSpiderPanel() {
		if (spiderPanel == null) {
			spiderPanel = new SpiderPanel(this, getSpiderParam());
		}
		return spiderPanel;
	}
	
	public void sessionChanged(final Session session)  {
	    if (EventQueue.isDispatchThread()) {
		    sessionChangedEventHandler(session);

	    } else {
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	        		    sessionChangedEventHandler(session);
	                }
	            });
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }
	    }
	}
	
	@SuppressWarnings("unchecked")
	private void sessionChangedEventHandler(Session session) {
		// clear all scans and add new hosts
		this.getSpiderPanel().clear();
		this.getSpiderPanel().reset();
		SiteNode root = (SiteNode)session.getSiteTree().getRoot();
		Enumeration<SiteNode> en = root.children();
		while (en.hasMoreElements()) {
			this.getSpiderPanel().addSite(en.nextElement().getNodeName(), true);
		}
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
		// Event from SiteMapListenner
		this.getSpiderPanel().nodeSelected(node);
	}

    private PopupMenuSpider getPopupMenuSpider() {
        if (popupMenuSpider == null) {
        	popupMenuSpider = new PopupMenuSpider();
        	popupMenuSpider.setExtension(this);
        }
        return popupMenuSpider;
    }

	private OptionsSpiderPanel getOptionsSpiderPanel() {
		if (optionsSpiderPanel == null) {
			optionsSpiderPanel = new OptionsSpiderPanel();
		}
		return optionsSpiderPanel;
	}
	
	protected void spiderSite(SiteNode node) {
		this.getSpiderPanel().scanSite(node);
	}
	
    public int getThreadPerScan() {
    	return this.getOptionsSpiderPanel().getThreads();
    }

	public boolean isScanning(SiteNode node) {
		return this.getSpiderPanel().isScanning(node);
	}

	public void setExcludeList(List<String> ignoredRegexs) {
		this.excludeList = ignoredRegexs;
	}

	public List<String> getExcludeList() {
		return excludeList;
	}

}