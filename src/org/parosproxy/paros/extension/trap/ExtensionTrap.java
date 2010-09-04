/*
 *
 * Paros and its related class files.
 * 
 * Paros is an HTTP/HTTPS proxy for assessing web application security.
 * Copyright (C) 2003-2004 Chinotec Technologies Company
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Clarified Artistic License
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Clarified Artistic License for more details.
 * 
 * You should have received a copy of the Clarified Artistic License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.parosproxy.paros.extension.trap;

import java.awt.EventQueue;

import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionTrap extends ExtensionAdaptor implements SessionChangedListener {

    
	private TrapPanel trapPanel = null;  //  @jve:decl-index=0:visual-constraint="161,134"
	private ProxyListenerTrap proxyListener = null;
    		

	private OptionsTrapPanel optionsTrapPanel = null;
	private TrapParam trapParam = null;   //  @jve:decl-index=0:
    /**
     * 
     */
    public ExtensionTrap() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionTrap(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionTrap");
			
	}
	
	/**
	 * This method initializes logPanel	
	 * 	
	 * @return com.proofsecure.paros.extension.history.LogPanel	
	 */    
	TrapPanel getTrapPanel() {
		if (trapPanel == null) {
		    trapPanel = new TrapPanel();
		    trapPanel.setName("Trap");
		}
		return trapPanel;
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        ExtensionHookView pv = extensionHook.getHookView();
	        pv.addWorkPanel(getTrapPanel());
	        pv.addOptionPanel(getOptionsTrapPanel());
	    }

        extensionHook.addOptionsParamSet(getTrapParam());
        
        extensionHook.addProxyListener(getProxyListenerTrap());
        extensionHook.addSessionListener(this);
        

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

	    getTrapPanel().setMessage("","", false);
	    
	    
	}
	
	
	private ProxyListenerTrap getProxyListenerTrap() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerTrap(getModel(), getTrapParam());
            proxyListener.setTrapPanel(getTrapPanel());

        }
        return proxyListener;
	}
	
	/**
	 * This method initializes optionsTrapPanel	
	 * 	
	 * @return org.parosproxy.paros.extension.trap.OptionsTrapPanel	
	 */    
	private OptionsTrapPanel getOptionsTrapPanel() {
		if (optionsTrapPanel == null) {
			optionsTrapPanel = new OptionsTrapPanel();
		}
		return optionsTrapPanel;
	}
	/**
	 * This method initializes trapParam	
	 * 	
	 * @return org.parosproxy.paros.extension.trap.TrapParam	
	 */    
	private TrapParam getTrapParam() {
		if (trapParam == null) {
			trapParam = new TrapParam();
		}
		return trapParam;
	}
  }