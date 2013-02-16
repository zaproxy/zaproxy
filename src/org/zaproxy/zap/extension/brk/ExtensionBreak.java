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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.brk.impl.http.HttpBreakpointsUiManagerInterface;
import org.zaproxy.zap.extension.brk.impl.http.ProxyListenerBreak;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.httppanel.Message;

public class ExtensionBreak extends ExtensionAdaptor implements SessionChangedListener {

    public enum DialogType {NONE, ADD, EDIT, REMOVE};
    
    public static final String NAME = "ExtensionBreak";
    
	private static final Logger logger = Logger.getLogger(ExtensionBreak.class);
	
	private BreakPanel breakPanel = null;
	private ProxyListenerBreak proxyListener = null;

	private BreakpointsPanel breakpointsPanel = null;

    private PopupMenuEditBreak popupMenuEditBreak = null;
	private PopupMenuRemove popupMenuRemove = null;
	
	private BreakpointMessageHandler breakpointMessageHandler;
	
    private DialogType currentDialogType = DialogType.NONE;
	
    private Map<Class<? extends BreakpointMessageInterface>, BreakpointsUiManagerInterface> mapBreakpointUiManager;
    
    private Map<Class<? extends Message>, BreakpointsUiManagerInterface> mapMessageUiManager;
    
	private Mode mode = Control.getSingleton().getMode();
	
	private BreakpointsParam breakpointsParams;
	
	private BreakpointsOptionsPanel breakpointsOptionsPanel;
	
    public ExtensionBreak() {
        super();
 		initialize();
    }

    
    public ExtensionBreak(String name) {
        super(name);
    }

	
	private void initialize() {
        this.setName(NAME);
        this.setOrder(24);
	}
	
	    
	public BreakPanel getBreakPanel() {
		if (breakPanel == null) {
		    breakPanel = new BreakPanel(getOptionsParam());
		    breakPanel.setName(Constant.messages.getString("tab.break"));
		}
		return breakPanel;
	}
	
	@Override
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    
	    extensionHook.addOptionsParamSet(getOptionsParam());
	    
	    if (getView() != null) {
	        breakpointMessageHandler = new BreakpointMessageHandler(getBreakPanel());
	        breakpointMessageHandler.setEnabledBreakpoints(getBreakpointsModel().getBreakpointsEnabledList());
	        
	        ExtensionHookView pv = extensionHook.getHookView();
	        pv.addWorkPanel(getBreakPanel());
	        pv.addOptionPanel(getOptionsPanel());
	        
            extensionHook.getHookMenu().addAnalyseMenuItem(extensionHook.getHookMenu().getMenuSeparator());

	        extensionHook.getHookView().addStatusPanel(getBreakpointsPanel());

	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuEdit());
	        extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuDelete());
            
            mapBreakpointUiManager = new HashMap<>();
            mapMessageUiManager = new HashMap<>();
            
            addBreakpointsUiManager(new HttpBreakpointsUiManagerInterface(extensionHook.getHookMenu(), this));
            
            extensionHook.addProxyListener(getProxyListenerBreak());
            
            
            extensionHook.addSessionListener(this);

	    	ExtensionHelp.enableHelpKey(getBreakPanel(), "ui.tabs.break");
	    	ExtensionHelp.enableHelpKey(getBreakpointsPanel(), "ui.tabs.breakpoints");
	    }
	}
	
	private BreakpointsParam getOptionsParam() {
		if (breakpointsParams == null) {
			breakpointsParams = new BreakpointsParam();
		}
		return breakpointsParams;
	}
	
	private BreakpointsOptionsPanel getOptionsPanel() {
		if (breakpointsOptionsPanel == null) {
			breakpointsOptionsPanel = new BreakpointsOptionsPanel();
		}
		return breakpointsOptionsPanel;
	}
	
	private BreakpointsPanel getBreakpointsPanel() {
		if (breakpointsPanel == null) {
			breakpointsPanel = new BreakpointsPanel();
		}
		return breakpointsPanel;
	}
	
	public void addBreakpoint(BreakpointMessageInterface breakpoint) {
		this.getBreakpointsPanel().addBreakpoint(breakpoint);
	}

	public void editBreakpoint(BreakpointMessageInterface oldBreakpoint, BreakpointMessageInterface newBreakpoint) {
		this.getBreakpointsPanel().editBreakpoint(oldBreakpoint, newBreakpoint);
	}
	
	public void removeBreakpoint(BreakpointMessageInterface breakpoint) {
		this.getBreakpointsPanel().removeBreakpoint(breakpoint);
	}
    
    public List<BreakpointMessageInterface> getBreakpointsList() {
        return getBreakpointsModel().getBreakpointsList();
    }
    
    public BreakpointMessageInterface getUiSelectedBreakpoint() {
        return getBreakpointsPanel().getSelectedBreakpoint();
    }
    
    public void addBreakpointsUiManager(BreakpointsUiManagerInterface uiManager) {
        mapBreakpointUiManager.put(uiManager.getBreakpointClass(), uiManager);
        mapMessageUiManager.put(uiManager.getMessageClass(), uiManager);
    }


	public void removeBreakpointsUiManager(BreakpointsUiManagerInterface uiManager) {
        mapBreakpointUiManager.remove(uiManager.getBreakpointClass());
        mapMessageUiManager.remove(uiManager.getMessageClass());		
	}
    
    public void addUiBreakpoint(Message aMessage) {
       BreakpointsUiManagerInterface uiManager = mapMessageUiManager.get(aMessage.getClass());
       if (uiManager != null) {
           uiManager.handleAddBreakpoint(aMessage);
       }
    }
    
    public void editUiSelectedBreakpoint() {
        BreakpointMessageInterface breakpoint = getBreakpointsPanel().getSelectedBreakpoint();
        if (breakpoint != null) {
            BreakpointsUiManagerInterface uiManager = mapBreakpointUiManager.get(breakpoint.getClass());
            if (uiManager != null) {
                uiManager.handleEditBreakpoint(breakpoint);
            }
        }
    }
	
	public void removeUiSelectedBreakpoint() {
	    BreakpointMessageInterface breakpoint = getBreakpointsPanel().getSelectedBreakpoint();
        if (breakpoint != null) {
            BreakpointsUiManagerInterface uiManager = mapBreakpointUiManager.get(breakpoint.getClass());
            if (uiManager != null) {
                uiManager.handleRemoveBreakpoint(breakpoint);
            }
        }
	}
	
	private BreakpointsTableModel getBreakpointsModel() {
		return (BreakpointsTableModel)this.getBreakpointsPanel().getBreakpoints().getModel();
	}
	
	private ProxyListenerBreak getProxyListenerBreak() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerBreak(getModel(), this);
        }
        return proxyListener;
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

	public boolean canAddBreakpoint() {
		return (currentDialogType == DialogType.NONE || currentDialogType == DialogType.ADD);
	}
    
	public boolean canEditBreakpoint() {
		return (currentDialogType == DialogType.NONE || currentDialogType == DialogType.EDIT);
	}
	
	public boolean canRemoveBreakpoint() {
		return (currentDialogType == DialogType.NONE || currentDialogType == DialogType.REMOVE);
	}
	
	public void dialogShown(DialogType type) {
	    currentDialogType = type;
	}
	
	public void dialogClosed() {
	    currentDialogType = DialogType.NONE;
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
	public void sessionScopeChanged(Session session) {
	}

	@Override
	public void destroy() {
		if (breakPanel != null) {
			breakPanel.savePanels();
		}
	}
	
	public boolean messageReceivedFromClient(Message aMessage) {
		if (mode.equals(Mode.safe)) {
			return true;
		}
	    return breakpointMessageHandler.handleMessageReceivedFromClient(aMessage, mode.equals(Mode.protect));
	}
	
	public boolean messageReceivedFromServer(Message aMessage) {
		if (mode.equals(Mode.safe)) {
			return true;
		}
	    return breakpointMessageHandler.handleMessageReceivedFromServer(aMessage, mode.equals(Mode.protect));
	}

	/**
	 * Exposes list of enabled breakpoints.
	 * 
	 * @return list of enabled breakpoints
	 */
	public List<BreakpointMessageInterface> getBreakpointsEnabledList() {
		if (mode.equals(Mode.safe)) {
			return new ArrayList<>();
		}
		return getBreakpointsModel().getBreakpointsEnabledList();
	}
	
	@Override
	public void sessionModeChanged(Mode mode) {
		this.mode = mode;
		this.getBreakPanel().sessionModeChanged(mode);
	}
}