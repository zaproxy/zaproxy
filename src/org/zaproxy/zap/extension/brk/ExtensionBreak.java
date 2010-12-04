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
package org.zaproxy.zap.extension.brk;

import java.awt.EventQueue;
import java.awt.Point;

import javax.swing.ListModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.help.ExtensionHelp;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionBreak extends ExtensionAdaptor implements SessionChangedListener {

	private BreakPanel breakPanel = null;
	private ProxyListenerBreak proxyListener = null;

	private BreakPointsPanel breakPointsPanel = null;

	private PopupMenuAddBreakSites popupMenuAddBreakSites = null;
    private PopupMenuAddBreakHistory popupMenuAddBreakHistory = null;

    private PopupMenuEditBreak popupMenuEditBreak = null;
	private PopupMenuRemove popupMenuRemove = null;

	private BreakAddDialog dialog = null;
	

	/**
     * 
     */
    public ExtensionBreak() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionBreak(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionBreak");
        
        ExtensionHelp.enableHelpKey(getBreakPanel(), "ui.tabs.break");
        ExtensionHelp.enableHelpKey(getBreakPointsPanel(), "ui.tabs.breakpoints");

	}
	
	/**
	 * This method initializes logPanel	
	 * 	
	 * @return com.proofsecure.paros.extension.history.LogPanel	
	 */    
	public BreakPanel getBreakPanel() {
		if (breakPanel == null) {
		    breakPanel = new BreakPanel();
		    breakPanel.setName(Constant.messages.getString("tab.break"));
		}
		return breakPanel;
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        ExtensionHookView pv = extensionHook.getHookView();
	        pv.addWorkPanel(getBreakPanel());
	        
            extensionHook.getHookMenu().addAnalyseMenuItem(extensionHook.getHookMenu().getMenuSeparator());

	        extensionHook.getHookView().addStatusPanel(getBreakPointsPanel());

	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuBreakSites());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuEdit());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuDelete());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddBreakHistory());

	    }

        extensionHook.addProxyListener(getProxyListenerBreak());
        extensionHook.addSessionListener(this);

	}
	
	private BreakPointsPanel getBreakPointsPanel() {
		if (breakPointsPanel == null) {
			breakPointsPanel = new BreakPointsPanel();
		}
		return breakPointsPanel;
	}
	
	public void addBreakPoint (String bp) {
		this.getBreakPointsPanel().addBreakPoint(bp);
	}

	public void removeBreakPoint (String bp) {
		this.getBreakPointsPanel().removeBreakPoint(bp);
	}
	
	public ListModel getBreakPointsModel() {
		return this.getBreakPointsPanel().getBreakPoints().getModel();
	}
	
	public String getSelectedBreakPoint() {
		return (String) this.getBreakPointsPanel().getBreakPoints().getSelectedValue();
	}
	
	public void selectBreakPoint (Point point) {
		int index = this.getBreakPointsPanel().getBreakPoints().locationToIndex(point);
		this.getBreakPointsPanel().getBreakPoints().setSelectedIndex(index);
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
	            
	        }
	    }
	}
	
	private void sessionChangedEventHandler(Session session) {

	    getBreakPanel().setMessage("","", false);
	}
	
	
	private ProxyListenerBreak getProxyListenerBreak() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerBreak(getModel(), this);
            proxyListener.setBreakPanel(getBreakPanel());
        }
        return proxyListener;
	}

	private PopupMenuAddBreakSites getPopupMenuBreakSites() {
		if (popupMenuAddBreakSites == null) {
			popupMenuAddBreakSites = new PopupMenuAddBreakSites();
			popupMenuAddBreakSites.setExtension(this);
		}
		return popupMenuAddBreakSites;
	}

	private PopupMenuEditBreak getPopupMenuEdit() {
		if (popupMenuEditBreak == null) {
			popupMenuEditBreak = new PopupMenuEditBreak();
			popupMenuEditBreak.setExtension(this);
		}
		return popupMenuEditBreak;
	}

	public void hidePopupMenuEdit() {
		this.getPopupMenuEdit().getDialog().dispose();
	}


	private PopupMenuRemove getPopupMenuDelete() {
		if (popupMenuRemove == null) {
			popupMenuRemove = new PopupMenuRemove();
			popupMenuRemove.setExtension(this);
		}
		return popupMenuRemove;
	}
	
    private PopupMenuAddBreakHistory getPopupMenuAddBreakHistory() {
        if (popupMenuAddBreakHistory == null) {
        	popupMenuAddBreakHistory = new PopupMenuAddBreakHistory();
        	popupMenuAddBreakHistory.setExtension(this);
        }
        return popupMenuAddBreakHistory;
    }

    public void showBreakAddDialog(String msg) {
		dialog = new BreakAddDialog(getView().getMainFrame(), false);
		dialog.setPlugin(this);
		dialog.setVisible(true);
		dialog.getTxtDisplay().setText(msg);
		
    }

	public void hideBreakAddDialog() {
		dialog.dispose();
	}

  }