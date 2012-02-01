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
// ZAP: 2011/04/16 Support for running ZAP as a daemon
// ZAP: 2011/05/09 Support for API
// ZAP: 2011/05/15 Support for exclusions
// ZAP: 2011/10/29 Support for parameters
// ZAP: 2011/11/20 Changed to use ExtensionFactory

package org.parosproxy.paros.control;

import java.util.List;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.control.ExtensionFactory;



/**
 *
 * Overall control with interaction on model and view.
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Control extends AbstractControl {

    private static Logger log = Logger.getLogger(Control.class);

    private static Control control = null;
    private Proxy proxy = null;
    private MenuFileControl menuFileControl = null;
    private MenuToolsControl menuToolsControl = null;
    
    private Control(Model model, View view) {
        super(model, view);
        init();
    }
    
    private void init() {
        
        PluginFactory.loadAllPlugin(model.getOptionsParam().getConfig());
        		
		// start plugin loading
		loadExtension();
		
		// ZAP: Start proxy even if no view
	    getProxy();
	    getExtensionLoader().hookProxyListener(getProxy());
		
		if (view != null) {
		    // ZAP: Add site map listeners
		    getExtensionLoader().hookSiteMapListener(view.getSiteTreePanel());
		}
		
		getProxy().startServer();
    }
    
    public Proxy getProxy() {
        if (proxy == null) {
            proxy = new Proxy(model);
        }
        
        return proxy;
    }
    
    protected void addExtension() {
    	ExtensionFactory.loadAllExtension(getExtensionLoader(), model.getOptionsParam().getConfig());
    }
    
    public MenuFileControl getMenuFileControl() {
        if (menuFileControl == null) {
            menuFileControl = new MenuFileControl(model, view, this);
        }

        return menuFileControl;
    }
    

    public MenuToolsControl getMenuToolsControl() {
        if (menuToolsControl == null) {
            menuToolsControl = new MenuToolsControl(model, view, this);                
        }
        return menuToolsControl;
    }

    /**
     * Override inherited shutdown to add stopping proxy servers.
     */
    public void shutdown(boolean compact) {
        getProxy().stopServer();
        super.shutdown(compact);
    }
    
    public static Control getSingleton() {

        return control;
    }

    public static void initSingletonWithView() {
        control = new Control(Model.getSingleton(), View.getSingleton());
    }
    
    public static void initSingletonWithoutView() {
        control = new Control(Model.getSingleton(), null);
    }

    
    public void runCommandLineNewSession(String fileName) throws Exception {
		
		Session session = new Session(model);
	    log.info("new session file created");
	    model.setSession(session);
		control.getExtensionLoader().sessionChangedAllPlugin(session);

		if (!fileName.endsWith(".session")) {
		    fileName += ".session";
		}
		session.save(fileName);
		getExtensionLoader().runCommandLine();
	}
    
    public void runCommandLineOpenSession(String fileName) {
		
		Session session = new Session(model);
	    log.info("new session file created");
	    model.setSession(session);
		control.getExtensionLoader().sessionChangedAllPlugin(session);
		
    }

    public void setExcludeFromProxyUrls(List<String> urls) {
		this.getProxy().setIgnoreList(urls);
    }
    

}
