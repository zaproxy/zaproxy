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
 */
package org.zaproxy.zap.extension.bruteforce;

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.AddonFilesChangedListener;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.view.SiteMapListener;
import org.zaproxy.zap.view.SiteMapTreeCellRenderer;

public class ExtensionBruteForce extends ExtensionAdaptor 
		implements SessionChangedListener, ProxyListener, SiteMapListener, AddonFilesChangedListener {

    private static final Logger logger = Logger.getLogger(ExtensionBruteForce.class);
    
    //Could be after the last one that saves the HttpMessage, as this ProxyListener doesn't change the HttpMessage.
	public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;
	
	public static final String HAMMER_ICON_RESOURCE = "/resource/icon/fugue/hammer.png";

	
	private BruteForcePanel bruteForcePanel = null;
	private OptionsBruteForcePanel optionsBruteForcePanel = null;
    private PopupMenuBruteForceSite popupMenuBruteForceSite = null;
    private PopupMenuBruteForceDirectory popupMenuBruteForceDirectory = null;
    private PopupMenuBruteForceDirectoryAndChildren popupMenuBruteForceDirectoryAndChildren = null;

	private BruteForceParam params = null;

	/**
     * 
     */
    public ExtensionBruteForce() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionBruteForce(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 */
	private void initialize() {
        this.setName("ExtensionBruteForce");
        this.setOrder(32);
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    extensionHook.addSessionListener(this);
        extensionHook.addProxyListener(this);
        extensionHook.addSiteMapListener(this);
        extensionHook.addAddonFilesChangedListener(this);

        extensionHook.addOptionsParamSet(getBruteForceParam());

	    if (getView() != null) {
	        @SuppressWarnings("unused")
			ExtensionHookView pv = extensionHook.getHookView();
	        extensionHook.getHookView().addStatusPanel(getBruteForcePanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsBruteForcePanel());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuBruteForceSite());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuBruteForceDirectory());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuBruteForceDirectoryAndChildren());

	    	ExtensionHelp.enableHelpKey(getBruteForcePanel(), "ui.tabs.bruteforce");
	    }
	}
	
	@Override
	public void unload() {
	    if (getView() != null) {
	    	Control.getSingleton().getExtensionLoader().removeStatusPanel(getBruteForcePanel());
	    	Control.getSingleton().getExtensionLoader().removeOptionsPanel(getOptionsBruteForcePanel());

	    	Control.getSingleton().getExtensionLoader().removePopupMenuItem(getPopupMenuBruteForceSite());
	    	Control.getSingleton().getExtensionLoader().removePopupMenuItem(getPopupMenuBruteForceDirectory());
            Control.getSingleton().getExtensionLoader().removePopupMenuItem(getPopupMenuBruteForceDirectoryAndChildren());
	    }
	    Control.getSingleton().getExtensionLoader().removeOptionsParamSet(getBruteForceParam());
	    // TODO remove listenners
	}
	
    @Override
	public boolean canUnload() {
    	return true;
    }
	
	private BruteForceParam getBruteForceParam() {
		if (params == null) {
			params = new BruteForceParam();
		}
		return params;
	}

	protected BruteForcePanel getBruteForcePanel() {
		if (bruteForcePanel == null) {
			bruteForcePanel = new BruteForcePanel(this, getBruteForceParam());
		}
		return bruteForcePanel;
	}
	
	@Override
	public void optionsLoaded() {
		this.getBruteForcePanel().setDefaultFile(this.getBruteForceParam().getDefaultFile());
	}

	protected void bruteForceSite (SiteNode siteNode) {
		this.getBruteForcePanel().bruteForceSite(siteNode);
	}
	
	protected void bruteForceDirectory (SiteNode siteNode) {
		this.getBruteForcePanel().bruteForceDirectory(siteNode);
	}
    
    protected void bruteForceDirectoryAndChildren(SiteNode siteNode) {
        this.getBruteForcePanel().bruteForceDirectoryAndChildren(siteNode);
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
		this.getBruteForcePanel().reset();
		if (session == null) {
			// Closedown
			return;
		}
		// Add new hosts
		SiteNode root = (SiteNode)session.getSiteTree().getRoot();
		@SuppressWarnings("unchecked")
		Enumeration<SiteNode> en = root.children();
		while (en.hasMoreElements()) {
			HistoryReference hRef = en.nextElement().getHistoryReference();
			if (hRef != null) {
				try {
					this.getBruteForcePanel().addSite(hRef.getURI());
				} catch (HttpMalformedHeaderException | SQLException e) {
					logger.warn("Failed to add a scan target: " + e.getMessage(), e);
				}
			}
		}
	}
	
	@Override
	public int getArrangeableListenerOrder() {
		return PROXY_LISTENER_ORDER;
	}

	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		this.getBruteForcePanel().addSite(msg.getRequestHeader().getURI());
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
		this.getBruteForcePanel().nodeSelected(node);
	}

	@Override
	public void onReturnNodeRendererComponent(
			SiteMapTreeCellRenderer component, boolean leaf, SiteNode value) {
	}

    private PopupMenuBruteForceSite getPopupMenuBruteForceSite() {
        if (popupMenuBruteForceSite == null) {
        	popupMenuBruteForceSite = new PopupMenuBruteForceSite(Constant.messages.getString("bruteforce.site.popup"));
        	popupMenuBruteForceSite.setExtension(this);
        }
        return popupMenuBruteForceSite;
    }

	private PopupMenuBruteForceDirectory getPopupMenuBruteForceDirectory() {
        if (popupMenuBruteForceDirectory == null) {
        	popupMenuBruteForceDirectory = new PopupMenuBruteForceDirectory(Constant.messages.getString("bruteforce.dir.popup"));
        	popupMenuBruteForceDirectory.setExtension(this);
        }
        return popupMenuBruteForceDirectory;
    }

    private PopupMenuBruteForceDirectoryAndChildren getPopupMenuBruteForceDirectoryAndChildren() {
        if (popupMenuBruteForceDirectoryAndChildren == null) {
            popupMenuBruteForceDirectoryAndChildren = new PopupMenuBruteForceDirectoryAndChildren(Constant.messages.getString("bruteforce.dir.and.children.popup"));
            popupMenuBruteForceDirectoryAndChildren.setExtension(this);
        }
        return popupMenuBruteForceDirectoryAndChildren;
    }

	private OptionsBruteForcePanel getOptionsBruteForcePanel() {
		if (optionsBruteForcePanel == null) {
			optionsBruteForcePanel = new OptionsBruteForcePanel(this);
		}
		return optionsBruteForcePanel;
	}
	
	public int getThreadPerScan() {
    	return this.getOptionsBruteForcePanel().getThreadPerScan();
    }

	public boolean getRecursive() {
    	return this.getOptionsBruteForcePanel().getRecursive();
    }

	public boolean isScanning(SiteNode node) {
		return this.getBruteForcePanel().isScanning(node);
	}

	public void refreshFileList() {
		this.getBruteForcePanel().refreshFileList();
	}
	
	public List<ForcedBrowseFile> getFileList() {
		return this.getBruteForcePanel().getFileList();
	}
	
	public void setDefaultFile(ForcedBrowseFile file) {
		this.getBruteForcePanel().setDefaultFile(file);
	}

	@Override
	public void sessionAboutToChange(Session session) {
	}
	
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("bruteforce.desc");
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
		this.getBruteForcePanel().sessionScopeChanged(session);
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		this.getBruteForcePanel().sessionModeChanged(mode);
	}

	@Override
	public void filesAdded() {
		this.refreshFileList();
	}

	@Override
	public void filesRemoved() {
		this.refreshFileList();
	}
}