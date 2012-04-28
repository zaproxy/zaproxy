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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
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

	private static final Logger logger = Logger.getLogger(ExtensionBreak.class);
	
	private BreakPanel breakPanel = null;
	private ProxyListenerBreak proxyListener = null;

	private BreakPointsPanel breakPointsPanel = null;

	private PopupMenuAddBreakSites popupMenuAddBreakSites = null;
    private PopupMenuAddBreakHistory popupMenuAddBreakHistory = null;

    private PopupMenuEditBreak popupMenuEditBreak = null;
	private PopupMenuRemove popupMenuRemove = null;

	private BreakAddDialog addDialog = null;
	private BreakEditDialog editDialog = null;
	
	private boolean canShowDialog = true;
	private Object canShowDialogLock = new Object();
	private enum Dialogs {NONE, ADD_DIALOG, EDIT_DIALOG};
	private Dialogs currentDialog = Dialogs.NONE;

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
        this.setOrder(24);
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
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        ExtensionHookView pv = extensionHook.getHookView();
	        pv.addWorkPanel(getBreakPanel());
	        
            extensionHook.getHookMenu().addAnalyseMenuItem(extensionHook.getHookMenu().getMenuSeparator());

	        extensionHook.getHookView().addStatusPanel(getBreakPointsPanel());

	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddBreakSites());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuEdit());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuDelete());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAddBreakHistory());

            extensionHook.addProxyListener(getProxyListenerBreak());
            extensionHook.addSessionListener(this);

	    	ExtensionHelp.enableHelpKey(getBreakPanel(), "ui.tabs.break");
	    	ExtensionHelp.enableHelpKey(getBreakPointsPanel(), "ui.tabs.breakpoints");
	    }
	}
	
	private BreakPointsPanel getBreakPointsPanel() {
		if (breakPointsPanel == null) {
			breakPointsPanel = new BreakPointsPanel();
		}
		return breakPointsPanel;
	}
	
	public void addBreakPoint (String url) {
		this.getBreakPointsPanel().addBreakPoint(url);
	}

	public void editBreakPointAtRow (int row, String url) {
		this.getBreakPointsPanel().editBreakPoint(row, url);
	}
	
	public void removeBreakPointAtRow (int row) {
		this.getBreakPointsPanel().removeBreakPoint(row);
	}
	
	public List<BreakPoint> getBreakPointsList() {
		return getBreakPointsModel().getBreakPointsList();
	}
	
	private int getSelectedBreakPointRow() {
		return this.getBreakPointsPanel().getBreakPoints().getSelectedRow();
	}
	
	private BreakPoint getSelectedBreakPoint() {
		return getBreakPointsModel().getBreakPointAtRow(getSelectedBreakPointRow());
	}
	
	private BreakPointsTableModel getBreakPointsModel() {
		return (BreakPointsTableModel)this.getBreakPointsPanel().getBreakPoints().getModel();
	}
	
	private ProxyListenerBreak getProxyListenerBreak() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerBreak(getModel(), this);
            proxyListener.setBreakPanel(getBreakPanel());
        }
        return proxyListener;
	}

	private PopupMenuAddBreakSites getPopupMenuAddBreakSites() {
		if (popupMenuAddBreakSites == null) {
			popupMenuAddBreakSites = new PopupMenuAddBreakSites();
			popupMenuAddBreakSites.setExtension(this);
		}
		return popupMenuAddBreakSites;
	}
	
    private PopupMenuAddBreakHistory getPopupMenuAddBreakHistory() {
        if (popupMenuAddBreakHistory == null) {
        	popupMenuAddBreakHistory = new PopupMenuAddBreakHistory();
        	popupMenuAddBreakHistory.setExtension(this);
        }
        return popupMenuAddBreakHistory;
    }

	private PopupMenuEditBreak getPopupMenuEdit() {
		if (popupMenuEditBreak == null) {
			popupMenuEditBreak = new PopupMenuEditBreak();
			popupMenuEditBreak.setExtension(this);
		}
		return popupMenuEditBreak;
	}

	private PopupMenuRemove getPopupMenuDelete() {
		if (popupMenuRemove == null) {
			popupMenuRemove = new PopupMenuRemove();
			popupMenuRemove.setExtension(this);
		}
		return popupMenuRemove;
	}

	public boolean canAddBreakPoint() {
		return (currentDialog == Dialogs.NONE || currentDialog == Dialogs.ADD_DIALOG);
	}
    
    public void showBreakAddDialog(String msg) {
		synchronized (canShowDialogLock) {
			if(canShowDialog) {
	    		addDialog = new BreakAddDialog(getView().getMainFrame(), false);
	    		addDialog.setPlugin(this);
	    		addDialog.setVisible(true);
	    		addDialog.setBreakPoint(msg);
	    		
				canShowDialog = false;
				currentDialog = Dialogs.ADD_DIALOG;
	    	} else if (currentDialog == Dialogs.ADD_DIALOG) {
	    		addDialog.setBreakPoint(msg);
	    	}
		}
    }

	public void hideBreakAddDialog() {
		synchronized (canShowDialogLock) {
			addDialog.dispose();
			addDialog = null;
		
			canShowDialog = true;
			currentDialog = Dialogs.NONE;
		}
	}
	
	public boolean canEditBreakPoint() {
		return (currentDialog == Dialogs.EDIT_DIALOG || currentDialog == Dialogs.NONE);
	}
	
    public void showBreakEditDialog() {
    	synchronized (canShowDialogLock) {
	    	if(canShowDialog) {
	    		editDialog = new BreakEditDialog(getView().getMainFrame(), false);
	    		editDialog.setPlugin(this);
	    		editDialog.setVisible(true);
	    		editDialog.setBreakPoint(getSelectedBreakPoint().getUrl());
	    		editDialog.setCurrentBreakPointRow(getSelectedBreakPointRow());
	    		
				canShowDialog = false;
				currentDialog = Dialogs.EDIT_DIALOG;
	    	} else if (currentDialog == Dialogs.EDIT_DIALOG) {
	    		editDialog.setBreakPoint(getSelectedBreakPoint().getUrl());
	    		editDialog.setCurrentBreakPointRow(getSelectedBreakPointRow());
	    	}
    	}
    }

	public void hideBreakEditDialog() {
		synchronized (canShowDialogLock) {
			editDialog.dispose();
			editDialog = null;
		
			canShowDialog = true;
			currentDialog = Dialogs.NONE;
		}
	}
	
	public boolean canRemoveBreakPoint() {
		return (currentDialog == Dialogs.NONE);
	}
	
	public void removeSelectedBreakPoint() {
		removeBreakPointAtRow(getSelectedBreakPointRow());
	}
	
	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("brk.desc");
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
	public void sessionAboutToChange(final Session session) {
		if (EventQueue.isDispatchThread()) {
			sessionAboutToChange();
	    } else {
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                @Override
	                public void run() {
	                	sessionAboutToChange();
	                }
	            });
	        } catch (Exception e) {
	            logger.error(e.getMessage(), e);
	        }
	    }
	}

	@Override
	public void sessionChanged(Session session) {
		getBreakPanel().init();
	}
	
	private void sessionAboutToChange() {
	    getBreakPanel().reset();
	}
	
	@Override
	public void destroy() {
		if (breakPanel != null) {
			breakPanel.savePanels();
		}
	}
}