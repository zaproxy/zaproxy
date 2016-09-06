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
// ZAP: 2013/04/16 Issue 638: Persist and snapshot sessions instead of saving them
// ZAP: 2013/08/28 Issue 695: Sites tree doesnt clear on new session created by API
// ZAP: 2013/08/29 Issue 776: Allow add-ons to warn user if they're closing ZAP with unsaved resources open
// ZAP: 2013/09/16 Issue 791: Saved sessions are discarded on ZAP's exit
// ZAP: 2014/01/16 Issue 979: Sites and Alerts trees can get corrupted
// ZAP: 2014/02/21 Issue 1043: Custom active scan dialog
// ZAP: 2014/02/27 Issue 1055: Load extensions before plugins
// ZAP: 2014/05/20 Issue 1114: core.newSession doesn't clear Sites
// ZAP: 2014/05/20 Issue 1191: Cmdline session params have no effect
// ZAP: 2014/09/22 Issue 1345: Support Attack mode
// ZAP: 2014/11/19 Issue 1412: Manage scan policies
// ZAP: 2015/01/29 Issue 1489: Version number in window title
// ZAP: 2015/02/05 Issue 1524: New Persist Session dialog
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations
// ZAP: 2015/02/10 Issue 1208: Search classes/resources in add-ons declared as dependencies
// ZAP: 2015/04/02 Issue 321: Support multiple databases and Issue 1582: Low memory option
// ZAP: 2015/09/17 Issue 1914: Support multiple add-on directories
// ZAP: 2015/11/04 Issue 1920: Report the host:port ZAP is listening on in daemon mode, or exit if it cant
// ZAP: 2016/03/23 Issue 2331: Custom Context Panels not show in existing contexts after installation of add-on
// ZAP: 2016/04/22 Issue 2428: Memory leak on session creation/loading
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab
// ZAP: 2016/09/06 Hook OverrideMessageProxyListener into the Proxy

package org.parosproxy.paros.control;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
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

	public enum Mode {safe, protect, standard, attack};
	
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

	private boolean init(ControlOverrides overrides) {

		// Load extensions first as message bundles are loaded as a side effect
		loadExtension();

		// ZAP: Start proxy even if no view
	    Proxy proxy = getProxy(overrides);
	    getExtensionLoader().hookProxyListener(proxy);
	    getExtensionLoader().hookOverrideMessageProxyListener(proxy);
	    getExtensionLoader().hookPersistentConnectionListener(proxy);
	    getExtensionLoader().hookConnectRequestProxyListeners(proxy);
		
		if (view != null) {
		    // ZAP: Add site map listeners
		    getExtensionLoader().hookSiteMapListener(view.getSiteTreePanel());
		}
		
		model.postInit();
		return proxy.startServer();
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
    	ExtensionFactory.loadAllExtension(getExtensionLoader(), model.getOptionsParam());
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
	    int rootCount = 0;
	    if (! Constant.isLowMemoryOptionSet()) {
	    	rootCount = model.getSession().getSiteTree().getChildCount(model.getSession().getSiteTree().getRoot());
	    }
	    boolean askOnExit = view != null && Model.getSingleton().getOptionsParam().getViewParam().getAskOnExitOption() > 0;
	    boolean sessionUnsaved = isNewState && rootCount > 0;
	    		
	    if (! noPrompt) {
		    List<String> list = getExtensionLoader().getUnsavedResources();
		    if (sessionUnsaved && askOnExit) {
		    	list.add(0, Constant.messages.getString("menu.file.exit.message.sessionResNotSaved"));
		    }

            String message = null;
            String activeActions = wrapEntriesInLiTags(getExtensionLoader().getActiveActions());
            if (list.size() > 0) {
                String unsavedResources = wrapEntriesInLiTags(list);

                if (activeActions.isEmpty()) {
                    message = MessageFormat.format(
                            Constant.messages.getString("menu.file.exit.message.resourcesNotSaved"),
                            unsavedResources);
                } else {
                    message = MessageFormat.format(
                            Constant.messages.getString("menu.file.exit.message.resourcesNotSavedAndActiveActions"),
                            unsavedResources,
                            activeActions);
                }
            } else if (!activeActions.isEmpty()) {
                message = MessageFormat.format(
                        Constant.messages.getString("menu.file.exit.message.activeActions"),
                        activeActions);
            }

            if (message != null && view.showConfirmDialog(message) != JOptionPane.OK_OPTION) {
                return;
            }
	    }

	    if (sessionUnsaved) {
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

    private static String wrapEntriesInLiTags(List<String> entries) {
        if (entries.isEmpty()) {
            return "";
        }

        StringBuilder strBuilder = new StringBuilder(entries.size() * 15);
        for (String entry : entries) {
            strBuilder.append("<li>");
            strBuilder.append(entry);
            strBuilder.append("</li>");
        }
        return strBuilder.toString();
    }
    
    public void exitAndDeleteSession (String sessionName) {
    	shutdown(false);
    	Model.getSingleton().getDb().deleteSession(sessionName);
    	
	    log.info(Constant.PROGRAM_TITLE + " terminated.");
		System.exit(0);   
    }

    public static Control getSingleton() {

        return control;
    }

    public static boolean initSingletonWithView(ControlOverrides overrides) {
        control = new Control(Model.getSingleton(), View.getSingleton());
        return control.init(overrides);
    }
    
    public static boolean initSingletonWithoutView(ControlOverrides overrides) {
        control = new Control(Model.getSingleton(), null);
        return control.init(overrides);
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
		
		model.createAndOpenUntitledDb();
    	final Session session = model.newSession();
		model.saveSession(fileName);

		if (View.isInitialised()) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					view.getSiteTreePanel().getTreeSite().setModel(session.getSiteTree());

					// refresh display
					view.getMainFrame().setTitle(session.getSessionName());
					view.getOutputPanel().clear();
				}
			});
		}

	    log.info("New session file created");
		control.getExtensionLoader().databaseOpen(model.getDb());
		control.getExtensionLoader().sessionChangedAllPlugin(session);
	}
    
    public void runCommandLineOpenSession(String fileName) throws Exception {
	    log.debug("runCommandLineOpenSession " + fileName);
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		
    	Session session = Model.getSingleton().getSession();
    	Model.getSingleton().openSession(fileName);
	    log.info("Session file opened");
		control.getExtensionLoader().databaseOpen(model.getDb());
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

    public void openSession(final String fileName, final SessionListener callback) {
	    log.info("Open Session");
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		lastCallback = callback;
    	model.openSession(fileName, this);
		// The session is opened in a thread, so notify the listeners via the callback
    }

	public Session newSession() throws Exception {
	    log.info("New Session");
		closeSessionAndCreateAndOpenUntitledDb();
		final Session session = model.newSession();
		getExtensionLoader().databaseOpen(model.getDb());
		getExtensionLoader().sessionChangedAllPlugin(session);

		if (View.isInitialised()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					view.getSiteTreePanel().getTreeSite().setModel(session.getSiteTree());
					view.getSiteTreePanel().reloadContextTree();
				}
			});
			
			// refresh display
			view.getMainFrame().setTitle(session.getSessionName());
			view.getOutputPanel().clear();
		}
		
		try {
			model.getDb().getTableSession().insert(session.getSessionId(), session.getSessionName());
		} catch (DatabaseException e) {
			log.error(e.getMessage(), e);
		}

		return session;
	}

    /**
     * Closes the old session and creates and opens an untitled database.
     * 
     * @throws Exception if an error occurred while creating or opening the database.
     */
    private void closeSessionAndCreateAndOpenUntitledDb() throws Exception {
        getExtensionLoader().sessionAboutToChangeAllPlugin(null);
        model.closeSession();
        log.info("Create and Open Untitled Db");
        model.createAndOpenUntitledDb();
    }

    public void newSession(String fileName, final SessionListener callback) {
        log.info("New Session");
        try {
            closeSessionAndCreateAndOpenUntitledDb();
            lastCallback = callback;
            model.newSession();
            model.saveSession(fileName, this);
        } catch (Exception e) {
            if (lastCallback != null) {
                lastCallback.sessionSaved(e);
                lastCallback = null;
            }
        }
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

	/**
	 * @deprecated (2.5.0) Use just {@link #newSession()} (or {@link #newSession(String, SessionListener)}) instead,
	 *             which already takes care to create and open an untitled database.
	 */
	@Deprecated
	@SuppressWarnings("javadoc")
	public void createAndOpenUntitledDb() throws ClassNotFoundException, Exception {
	    log.info("Create and Open Untitled Db");
		getExtensionLoader().sessionAboutToChangeAllPlugin(null);
		model.closeSession();
		model.createAndOpenUntitledDb();
		getExtensionLoader().databaseOpen(model.getDb());
		getExtensionLoader().sessionChangedAllPlugin(model.getSession());
	}

	@Override
	public void sessionOpened(File file, Exception e) {
		getExtensionLoader().databaseOpen(model.getDb());
		getExtensionLoader().sessionChangedAllPlugin(model.getSession());
		if (lastCallback != null) {
			lastCallback.sessionOpened(file, e);
			lastCallback = null;
		}
		
	}

	@Override
	public void sessionSaved(Exception e) {
		getExtensionLoader().databaseOpen(model.getDb());
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
