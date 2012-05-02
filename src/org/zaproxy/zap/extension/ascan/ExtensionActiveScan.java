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
package org.zaproxy.zap.extension.ascan;

import java.awt.EventQueue;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.HostProcess;
import org.parosproxy.paros.core.scanner.ScannerParam;
import org.parosproxy.paros.extension.CommandLineArgument;
import org.parosproxy.paros.extension.CommandLineListener;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.extension.history.ProxyListenerLog;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.pscan.ExtensionPassiveScan;
import org.zaproxy.zap.view.SiteMapListener;
/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionActiveScan extends ExtensionAdaptor implements  
		SessionChangedListener, CommandLineListener, ProxyListener, SiteMapListener {
    
    private static final Logger logger = Logger.getLogger(ExtensionActiveScan.class);
    
    private static final int ARG_SCAN_IDX = 0;

	public static final String NAME = "ExtensionActiveScan";

    //Could be after the last one that saves the HttpMessage, as this ProxyListener doesn't change the HttpMessage.
	public static final int PROXY_LISTENER_ORDER = ProxyListenerLog.PROXY_LISTENER_ORDER + 1;
	
	private static final List<Class<?>> DEPENDENCIES;
	
	static {
		List<Class<?>> dep = new ArrayList<Class<?>>();
		dep.add(ExtensionAlert.class);
		
		DEPENDENCIES = Collections.unmodifiableList(dep);
	}
    
	private JMenuItem menuItemPolicy = null;
	
	private OptionsScannerPanel optionsScannerPanel = null;
	private ActiveScanPanel activeScanPanel = null;
	private ScannerParam scannerParam = null;
	private CommandLineArgument[] arguments = new CommandLineArgument[1];

	private List<AbstractParamPanel> policyPanels = new ArrayList<AbstractParamPanel>();

    private PopupMenuScanHistory popupMenuScanHistory = null;

    /**
     * 
     */
    public ExtensionActiveScan() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionActiveScan(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName(NAME);
        this.setOrder(28);
			
        API.getInstance().registerApiImplementor(new ActiveScanAPI(this));

	}

	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
            extensionHook.getHookMenu().addAnalyseMenuItem(getMenuItemPolicy());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuScanHistory());

            extensionHook.getHookView().addStatusPanel(getActiveScanPanel());
	        extensionHook.getHookView().addOptionPanel(getOptionsScannerPanel());
	        
	        this.getActiveScanPanel().setDisplayPanel(getView().getRequestPanel(), getView().getResponsePanel());

	    	ExtensionHelp.enableHelpKey(getActiveScanPanel(), "ui.tabs.ascan");
	    }
        extensionHook.addSessionListener(this);
        extensionHook.addProxyListener(this);
        extensionHook.addSiteMapListner(this);

        extensionHook.addOptionsParamSet(getScannerParam());
        extensionHook.addCommandLine(getCommandLineArguments());


	}
	
	private ActiveScanPanel getActiveScanPanel() {
		if (activeScanPanel == null) {
			activeScanPanel = new ActiveScanPanel(this);
		}
		return activeScanPanel;
	}
	
	public void startScan(SiteNode startNode) {
		this.getActiveScanPanel().scanSite(startNode, true);
	}

    public void scannerComplete() {
    }
	
	/**
	 * This method initializes menuItemPolicy	
	 * 	
	 * @return javax.swing.JMenuItem	
	 */    
	private JMenuItem getMenuItemPolicy() {
		if (menuItemPolicy == null) {
			menuItemPolicy = new JMenuItem();
			menuItemPolicy.setText(Constant.messages.getString("menu.analyse.scanPolicy"));
			menuItemPolicy.addActionListener(new java.awt.event.ActionListener() { 
				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {    
					showPolicyDialog();
				}
			});

		}
		return menuItemPolicy;
	}
	
	protected void showPolicyDialog() {
		PolicyDialog dialog = new PolicyDialog(getView().getMainFrame());
	    dialog.initParam(getModel().getOptionsParam());
	    for (AbstractParamPanel panel : policyPanels) {
	    	dialog.addPolicyPanel(panel);
	    }
	    // TODO This could be done in a cleaner way...
		ExtensionPassiveScan pscan = (ExtensionPassiveScan) Control.getSingleton().getExtensionLoader().getExtension(ExtensionPassiveScan.NAME);
		dialog.addPolicyPanel(pscan.getPolicyPanel());

		int result = dialog.showDialog(false);
		if (result == JOptionPane.OK_OPTION) {
		    try {
                getModel().getOptionsParam().getConfig().save();
            } catch (ConfigurationException ce) {
            	logger.error(ce.getMessage(), ce);
                getView().showWarningDialog(Constant.messages.getString("scanner.save.warning"));
            }
		}					
	}

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.ScannerListener#ScannerProgress(java.lang.String, org.parosproxy.paros.network.HttpMessage, int)
     */
    public void hostProgress(String hostAndPort, String msg, int percentage) {
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.ScannerListener#HostComplete(java.lang.String)
     */
    public void hostComplete(String hostAndPort) {
    }

    /* (non-Javadoc)
     * @see org.parosproxy.paros.core.scanner.ScannerListener#hostNewScan(java.lang.String)
     */
    public void hostNewScan(String hostAndPort, HostProcess hostThread) {
    }
    
	@Override
    public void initView(ViewDelegate view) {
    	super.initView(view);
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
	
	@SuppressWarnings("unchecked")
	private void sessionChangedEventHandler(Session session) {
		// Clear all scans
		this.getActiveScanPanel().reset();
		if (session == null) {
			// Closedown
			return;
		}
		// Add new hosts
		SiteNode snroot = (SiteNode)session.getSiteTree().getRoot();
		Enumeration<SiteNode> en = snroot.children();
		while (en.hasMoreElements()) {
			this.getActiveScanPanel().addSite(en.nextElement().getNodeName(), true);
		}
	}


	/**
	 * This method initializes optionsScannerPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.scanner.OptionsScannerPanel	
	 */    
	private OptionsScannerPanel getOptionsScannerPanel() {
		if (optionsScannerPanel == null) {
			optionsScannerPanel = new OptionsScannerPanel();
		}
		return optionsScannerPanel;
	}
	/**
	 * This method initializes scannerParam	
	 * 	
	 * @return org.parosproxy.paros.core.scanner.ScannerParam	
	 */    
	protected ScannerParam getScannerParam() {
		if (scannerParam == null) {
			scannerParam = new ScannerParam();
		}
		return scannerParam;
	}
	
    /* (non-Javadoc)
     * @see org.parosproxy.paros.extension.CommandLineListener#execute(org.parosproxy.paros.extension.CommandLineArgument[])
     */
	// TODO
    @Override
    public void execute(CommandLineArgument[] args) {
    	/*

        if (arguments[ARG_SCAN_IDX].isEnabled()) {
            System.out.println("Scanner started...");
            startScan();
        } else {
            return;
        }

        while (!getScanner().isStop()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        System.out.println("Scanner completed.");

    */
    }

    private CommandLineArgument[] getCommandLineArguments() {
        arguments[ARG_SCAN_IDX] = new CommandLineArgument("-scan", 0, null, "", "-scan : Run vulnerability scan depending on previously saved policy.");
        return arguments;
    }

    /**
     * This method initializes popupMenuScanHistory	
     * 	
     * @return org.parosproxy.paros.extension.scanner.PopupMenuScanHistory	
     */
	private PopupMenuScanHistory getPopupMenuScanHistory() {
        if (popupMenuScanHistory == null) {
            popupMenuScanHistory = new PopupMenuScanHistory();
            popupMenuScanHistory.setExtension(this);
        }
        return popupMenuScanHistory;
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
		} else if (msg.getRequestHeader().getSecure()) {
			site += ":443";
		}
		this.getActiveScanPanel().addSite(site, true);
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
		this.getActiveScanPanel().nodeSelected(node, true);
	}

	public boolean isScanning(SiteNode node) {
		return this.getActiveScanPanel().isScanning(node, true);
	}

	public void setExcludeList(List<String> urls) {
		this.getActiveScanPanel().setExcludeList(urls);
	}

    public void addPolicyPanel(AbstractParamPanel panel) {
		this.policyPanels.add(panel);
	}
	
	@Override
	public List<Class<?>> getDependencies() {
		return DEPENDENCIES;
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
		return Constant.messages.getString("ascan.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}
}
