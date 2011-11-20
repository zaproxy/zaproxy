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
package org.zaproxy.zap.extension.portscan;

import java.awt.EventQueue;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.view.SiteMapListener;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionPortScan extends ExtensionAdaptor 
		implements SessionChangedListener, ProxyListener, SiteMapListener {

	private PortScanPanel portScanPanel = null;
    private PopupMenuPortScan popupMenuPortScan = null;
	private OptionsPortScanPanel optionsPortScanPanel = null;
	private PopupMenuPortCopy popupMenuPortCopy = null;
	private PortScanParam params = null;
    private Logger logger = Logger.getLogger(ExtensionPortScan.class);

	/**
     * 
     */
    public ExtensionPortScan() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionPortScan(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionPortScan");
        this.setOrder(34);
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    extensionHook.addSessionListener(this);
        extensionHook.addProxyListener(this);
        extensionHook.addSiteMapListner(this);
	    
	    if (getView() != null) {
	        @SuppressWarnings("unused")
			ExtensionHookView pv = extensionHook.getHookView();
	        extensionHook.getHookView().addStatusPanel(getPortScanPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsPortScanPanel());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuPortScan());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuPortCopy());

	    	ExtensionHelp.enableHelpKey(getPortScanPanel(), "ui.tabs.portscan");
	    }
        extensionHook.addOptionsParamSet(getPortScanParam());
	}
	
	private PortScanParam getPortScanParam() {
		if (params == null) {
			params = new PortScanParam();
		}
		return params;
	}

	protected PortScanPanel getPortScanPanel() {
		if (portScanPanel == null) {
			portScanPanel = new PortScanPanel(this, getPortScanParam());
		}
		return portScanPanel;
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
		this.getPortScanPanel().reset();
		SiteNode root = (SiteNode)session.getSiteTree().getRoot();
		Enumeration<SiteNode> en = root.children();
		while (en.hasMoreElements()) {
			this.getPortScanPanel().addSite(en.nextElement().getNodeName(), false);
		}
	}

	@Override
	public boolean onHttpRequestSend(HttpMessage msg) {
		// The panel will handle duplicates
		this.getPortScanPanel().addSite(msg.getRequestHeader().getHostName(), false);
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
		this.getPortScanPanel().nodeSelected(node, false);
	}

    private PopupMenuPortScan getPopupMenuPortScan() {
        if (popupMenuPortScan == null) {
        	popupMenuPortScan = new PopupMenuPortScan();
        	popupMenuPortScan.setExtension(this);
        }
        return popupMenuPortScan;
    }

	private OptionsPortScanPanel getOptionsPortScanPanel() {
		if (optionsPortScanPanel == null) {
			optionsPortScanPanel = new OptionsPortScanPanel();
		}
		return optionsPortScanPanel;
	}
	
	private PopupMenuPortCopy getPopupMenuPortCopy() {
		if (popupMenuPortCopy == null) {
			popupMenuPortCopy = new PopupMenuPortCopy();
			popupMenuPortCopy.setExtension(this);
		}
		return popupMenuPortCopy;
	}

	
	protected void portScanSite(SiteNode node) {
		this.getPortScanPanel().scanSite(node, false);
	}
	
    public int getThreadPerScan() {
    	return this.getOptionsPortScanPanel().getThreadPerScan();
    }

    public int getMaxPort() {
    	return this.getOptionsPortScanPanel().getMaxPort();
    }

	public boolean isScanning(SiteNode node) {
		return 	this.getPortScanPanel().isScanning(node, false);
	}

}