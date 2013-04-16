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
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/04/23 Changed the method shutdown(boolean) to save the configurations
// of the main http panels and save the configuration file.
// ZAP: 2012/04/23 Added @Override annotation to all appropriate methods.
// ZAP: 2012/05/16 Added new initialization method plus ctor for testing purposes.
// ZAP: 2012/06/25 Moved call of init() from ctor to singleton methods to
// initialize singleton variable first. Allows to retrieve the singleton while
// not fully initialized (e.g.: to get another extension in the hook() method of
// and extension).
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/11/01 Issue 411: Allow proxy port to be specified on the command line
// ZAP: 2012/12/06 Issue 428: Added exit method to support the marketplace
// ZAP: 2012/12/27 Hook new persistent connection listener.
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/20 Issue 568: Allow extensions to run from the command line
// ZAP: 2013/04/16 Issue TBA: Persist and snapshot sessions instead of saving them

package org.parosproxy.paros.control;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.PluginFactory;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WaitMessageDialog;
import org.zaproxy.zap.control.ControlOverrides;
import org.zaproxy.zap.control.ExtensionFactory;

/**
 *
 * Overall control with interaction on model and view.
 */
public class Control extends AbstractControl implements SessionListener {

	public enum Mode {safe, protect, standard};
	
    private static Logger log = Logger.getLogger(Control.class);

    private static Control control = null;
    private Proxy proxy = null;
    private MenuFileControl menuFileControl = null;
    private MenuToolsControl menuToolsControl = null;
    private SessionListener lastCallback = null;
	private Mode mode = null;
    
    private Control(Model model, View view) {
        super(model, view);
        // ZAP: moved call of init() to singleton methods
    }
    
    // ZAP: Added constructor that will be used by initSingletonForTesting()
    private Control() {
		super(null, null);
	}

	private void init(ControlOverrides overrides) {
        
        PluginFactory.loadAllPlugin(model.getOptionsParam().getConfig());
        		
		// start plugin loading
		loadExtension();
		
		// ZAP: Start proxy even if no view
	    Proxy proxy = getProxy(overrides);
	    getExtensionLoader().hookProxyListener(proxy);
	    getExtensionLoader().hookPersistentConnectionListener(proxy);
		
		if (view != null) {
		    // ZAP: Add site map listeners
		    getExtensionLoader().hookSiteMapListener(view.getSiteTreePanel());
		}
		
		proxy.startServer();
    }

    public Proxy getProxy() {
    	return this.getProxy(null);
    }

    public Proxy getProxy(ControlOverrides overrides) {
        if (proxy == null) {
            proxy = new Proxy(model, overrides);
        }
        
        return proxy;
    }
    
    @Override
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
    @Override
    public void shutdown(boolean compact) {
        // ZAP: Save the configurations of the main panels.
        if (view != null) {
	        view.getRequestPanel().saveConfig(model.getOptionsParam().getConfig());
	        view.getResponsePanel().saveConfig(model.getOptionsParam().getConfig());
        }
        
	    // ZAP: Save the configuration file.
		try {
			model.getOptionsParam().getConfig().save();
		} catch (ConfigurationException e) {
			log.error("Error saving config", e);
		}
		
        getProxy(null).stopServer();
        super.shutdown(compact);
    }
    
    public void exit (boolean noPrompt, final File openOnExit) {
	    boolean isNewState = model.getSession().isNewState();
	    int rootCount = model.getSession().getSiteTree().getChildCount(model.getSession().getSiteTree().getRoot());
	    boolean askOnExit = view != null && Model.getSingleton().getOptionsParam().getViewParam().getAskOnExitOption() > 0;
	    
	    if (isNewState && rootCount > 0 && askOnExit && ! noPrompt) {
	    	// ZAP: i18n
			if (view.showConfirmDialog(Constant.messages.getString("menu.file.sessionNotSaved")) != JOptionPane.OK_OPTION) {
				return;
			}
			control.discardSession();
	    }

	    Thread t = new Thread(new Runnable() {
	        @Override
	        public void run() {
	            // ZAP: Changed to use the option compact database.
	            control.shutdown(Model.getSingleton().getOptionsParam().getDatabaseParam().isCompactDatabase());
	    	    log.info(Constant.PROGRAM_TITLE + " terminated.");
	    	    
	    	    if (openOnExit != null && Desktop.isDesktopSupported()) {
					try {
			    	    log.info("Openning file " + openOnExit.getAbsolutePath());
						Desktop.getDesktop().open(openOnExit);
					} catch (IOException e) {
						log.error("Failed to open file " + openOnExit.getAbsolutePath(), e);
					}
	    	    }
	    		System.exit(0);   
	        }
	    });

	    if (view != null) {
		    WaitMessageDialog dialog = view.getWaitMessageDialog(Constant.messages.getString("menu.file.shuttingDown"));	// ZAP: i18n
		    t.start();
		    dialog.setVisible(true);
	    } else {
		    t.start();
	    }
    }
    
    public static Control getSingleton() {

        return control;
    }

    public static void initSingletonWithView(ControlOverrides overrides) {
        control = new Control(Model.getSingleton(), View.getSingleton());
        control.init(overrides);
        // Initialise the mode
        control.setMode(control.getMode());
    }
    
    public static void initSingletonWithoutView(ControlOverrides overrides) {
        control = new Control(Model.getSingleton(), null);
        control.init(overrides);
    }

    // ZAP: Added method to allow for testing
	public static void initSingletonForTesting() {
        control = new Control();
	}

    
    public void runCommandLine() throws Exception {
	    log.debug("runCommand");
		getExtensionLoader().runCommandLine();
	}
    
    public void runCommandLineNewSession(String fileName) throws Exception {
	    log.debug("runCommandLineNewSession " + fileName);
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		
    	Session session = Model.getSingleton().newSession();
		Model.getSingleton().saveSession(fileName);
	    log.info("New session file created");
		control.getExtensionLoader().sessionChangedAllPlugin(session);
		getExtensionLoader().runCommandLine();
	}
    
    public void runCommandLineOpenSession(String fileName) {
	    log.debug("runCommandLineOpenSession " + fileName);
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		
    	Session session = Model.getSingleton().getSession();
    	try {
    		Model.getSingleton().openSession(fileName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	    log.info("Session file opened");
		control.getExtensionLoader().sessionChangedAllPlugin(session);
    }

    public void setExcludeFromProxyUrls(List<String> urls) {
		this.getProxy(null).setIgnoreList(urls);
    }
    
    public void openSession(final File file, final SessionListener callback) {
	    log.info("Open Session");
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		lastCallback = callback;
    	model.openSession(file, this);
		// The session is opened in a thread, so notify the listeners via the callback
    }

	public Session newSession() {
	    log.info("New Session");
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		Session session = model.newSession();
		getExtensionLoader().sessionChangedAllPlugin(session);

		return session;
	}
	
    public void saveSession(final String fileName) {
    	this.saveSession(fileName, null);
    }

    public void saveSession(final String fileName, final SessionListener callback) {
	    log.info("Save Session");
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		lastCallback = callback;
		model.saveSession(fileName, this);
		// The session is saved in a thread, so notify the listeners via the callback
    }

    public void snapshotSession(final String fileName, final SessionListener callback) {
	    log.info("Snapshot Session");
		lastCallback = callback;
		model.snapshotSession(fileName, this);
		// The session is saved in a thread, so notify the listeners via the callback
    }
	
	public void discardSession() {
	    log.info("Discard Session");
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		model.discardSession();
		getExtensionLoader().sessionChangedAllPlugin(null);
	}

	public void createAndOpenUntitledDb() throws ClassNotFoundException, Exception {
	    log.info("Create and Open Untitled Db");
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		model.closeSession();
		model.createAndOpenUntitledDb();
		getExtensionLoader().sessionChangedAllPlugin(model.getSession());
	}

	@Override
	public void sessionOpened(File file, Exception e) {
		getExtensionLoader().sessionChangedAllPlugin(model.getSession());
		if (lastCallback != null) {
			lastCallback.sessionOpened(file, e);
			lastCallback = null;
		}
		
	}

	@Override
	public void sessionSaved(Exception e) {
		getExtensionLoader().sessionChangedAllPlugin(model.getSession());
		if (lastCallback != null) {
			lastCallback.sessionSaved(e);
			lastCallback = null;
		}
	}
	
	@Override
	public void sessionSnapshot(Exception e) {
		if (lastCallback != null) {
			lastCallback.sessionSnapshot(e);
			lastCallback = null;
		}
	}
	
	public void sessionScopeChanged() {
		getExtensionLoader().sessionScopeChangedAllPlugin(model.getSession());
	}
	
	public Mode getMode() {
		if (mode == null) {
			mode = Mode.valueOf(model.getOptionsParam().getViewParam().getMode());
		}
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
		getExtensionLoader().sessionModeChangedAllPlugin(mode);
		model.getOptionsParam().getViewParam().setMode(mode.name());
	}
}
