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
// ZAP: 2011/12/14 Support for extension dependencies
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/03/15 Reflected the change in the name of the method optionsChanged of
// the class OptionsChangedListener. Changed the method destroyAllExtension() to
// save the configurations of the main http panels and save the configuration file.
// ZAP: 2012/04/23 Reverted the changes of the method destroyAllExtension(),
// now the configurations of the main http panels and the configuration file
// are saved in the method Control.shutdown(boolean).
// ZAP: 2012/04/24 Changed the method destroyAllExtension to catch exceptions.
// ZAP: 2012/04/25 Added the type argument and removed unnecessary cast.
// ZAP: 2012/07/23 Removed parameter from View.getSessionDialog call.
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/11/30 Issue 425: Added tab index to support quick start tab 
// ZAP: 2012/12/27 Added hookPersistentConnectionListener() method.
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
// ZAP: 2013/01/25 Added removeExtension(...) method and further helper methods
// to remove listeners, menu items, etc.
// ZAP: 2013/01/25 Refactored hookMenu(). Resolved some Checkstyle issues.
// ZAP: 2013/01/29 Catch Errors thrown by out of date extensions as well as Exceptions
// ZAP: 2013/07/23 Issue 738: Options to hide tabs
// ZAP: 2013/11/16 Issue 807: Error while loading ZAP when Quick Start Tab is closed
// ZAP: 2013/11/16 Issue 845: AbstractPanel added twice to TabbedPanel2 in ExtensionLoader#addTabPanel
// ZAP: 2013/12/03 Issue 934: Handle files on the command line via extension
// ZAP: 2013/12/13 Added support for Full Layout DISPLAY_OPTION_TOP_FULL in the hookView function.
// ZAP: 2014/03/23 Issue 1022: Proxy - Allow to override a proxied message
// ZAP: 2014/03/23 Issue 1090: Do not add pop up menus if target extension is not enabled
// ZAP: 2014/05/20 Issue 1202: Issue with loading addons that did not initialize correctly
// ZAP: 2014/08/14 Catch Exceptions thrown by extensions when stopping them
// ZAP: 2014/08/14 Issue 1309: NullPointerExceptions during a failed uninstallation of an add-on
// ZAP: 2014/10/07 Issue 1357: Hide unused tabs
// ZAP: 2014/10/09 Issue 1359: Added info logging for splash screen
// ZAP: 2014/10/25 Issue 1062: Added scannerhook to be loaded by an active scanner.
// ZAP: 2014/11/11 Issue 1406: Move online menu items to an add-on
// ZAP: 2014/11/21 Reviewed foreach loops and commented startup process for splash screen progress bar
// ZAP: 2015/01/04 Issue 1379: Not all extension's listeners are hooked during add-on installation
// ZAP: 2015/01/19 Remove online menus when removeMenu(View, ExtensionHook) is called.
// ZAP: 2015/01/19 Issue 1510: New Extension.postInit() method to be called once all extensions loaded
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative implementations
// ZAP: 2015/02/10 Issue 1208: Search classes/resources in add-ons declared as dependencies
// ZAP: 2015/04/09 Generify Extension.getExtension(Class) to avoid unnecessary casts
// ZAP: 2015/09/07 Start GUI on EDT
// ZAP: 2016/04/06 Fix layouts' issues
// ZAP: 2016/04/08 Hook ContextDataFactory/ContextPanelFactory 
// ZAP: 2016/05/30 Notification of installation status of the add-ons
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab
// ZAP: 2016/08/18 Hook ApiImplementor

package org.parosproxy.paros.extension;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.control.Proxy;
import org.parosproxy.paros.core.proxy.ConnectRequestProxyListener;
import org.parosproxy.paros.core.proxy.OverrideMessageProxyListener;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.Scanner;
import org.parosproxy.paros.core.scanner.ScannerHook;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseUnsupportedException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.AbstractParamDialog;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.MainMenuBar;
import org.parosproxy.paros.view.SiteMapPanel;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WorkbenchPanel;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.AddonFilesChangedListener;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.AddOnInstallationStatusListener;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.view.ContextPanelFactory;
import org.zaproxy.zap.view.SiteMapListener;

public class ExtensionLoader {

    private final List<Extension> extensionList = new ArrayList<>();
    private final Map<Class<? extends Extension>, Extension> extensionsMap = new HashMap<>();
    private final Map<Extension, ExtensionHook> extensionHooks = new HashMap<>();
    private Model model = null;

    private View view = null;
    private static final Logger logger = Logger.getLogger(ExtensionLoader.class);

    public ExtensionLoader(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    public void addExtension(Extension extension) {
        extensionList.add(extension);
        extensionsMap.put(extension.getClass(), extension);
    }

    public void destroyAllExtension() {
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                getExtension(i).destroy();
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public Extension getExtension(int i) {
        return extensionList.get(i);
    }

    public Extension getExtension(String name) {
        if (name != null) {
            for (int i = 0; i < extensionList.size(); i++) {
                Extension p = getExtension(i);
                if (p.getName().equalsIgnoreCase(name)) {
                    return p;
                }
            }
        }
        
        return null;
    }

    public Extension getExtensionByClassName(String name) {
        if (name != null) {
            for (int i = 0; i < extensionList.size(); i++) {
                Extension p = getExtension(i);
                if (p.getClass().getName().equals(name)) {
                    return p;
                }
            }
        }
        
        return null;
    }

    /**
     * Gets the {@code Extension} with the given class.
     *
     * @param clazz the class of the {@code Extension}
     * @return the {@code Extension} or {@code null} if not found.
     */
    public <T extends Extension> T getExtension(Class<T> clazz) {
        if (clazz != null) {
            Extension extension = extensionsMap.get(clazz);
            if (extension != null) {
                return clazz.cast(extension);
            }
        }
        return null;
    }

    /**
     * Tells whether or not an {@code Extension} with the given
     * {@code extensionName} is enabled.
     *
     * @param extensionName the name of the extension
     * @return {@code true} if the extension is enabled, {@code false}
     * otherwise.
     * @throws IllegalArgumentException if the {@code extensionName} is
     * {@code null}.
     * @see #getExtension(String)
     * @see Extension
     */
    public boolean isExtensionEnabled(String extensionName) {
        if (extensionName == null) {
            throw new IllegalArgumentException("Parameter extensionName must not be null.");
        }

        Extension extension = getExtension(extensionName);
        if (extension == null) {
            return false;
        }
        
        return extension.isEnabled();
    }

    public int getExtensionCount() {
        return extensionList.size();
    }

    public void hookProxyListener(Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookProxyListeners(proxy, hook.getProxyListenerList());
        }
    }

    private static void hookProxyListeners(Proxy proxy, List<ProxyListener> listeners) {
        for (ProxyListener listener : listeners) {
            try {
                if (listener != null) {
                    proxy.addProxyListener(listener);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void removeProxyListener(ExtensionHook hook) {
        Proxy proxy = Control.getSingleton().getProxy();
        List<ProxyListener> listenerList = hook.getProxyListenerList();
        for (ProxyListener listener : listenerList) {
            try {
                if (listener != null) {
                    proxy.removeProxyListener(listener);
                }
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void hookOverrideMessageProxyListener(Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            List<OverrideMessageProxyListener> listenerList = hook.getOverrideMessageProxyListenerList();
            for (OverrideMessageProxyListener listener : listenerList) {
                try {
                    if (listener != null) {
                        proxy.addOverrideMessageProxyListener(listener);
                    }
                    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    private void removeOverrideMessageProxyListener(ExtensionHook hook) {
        Proxy proxy = Control.getSingleton().getProxy();
        List<OverrideMessageProxyListener> listenerList = hook.getOverrideMessageProxyListenerList();
        for (OverrideMessageProxyListener listener : listenerList) {
            try {
                if (listener != null) {
                    proxy.removeOverrideMessageProxyListener(listener);
                }
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Hooks (adds) the {@code ConnectRequestProxyListener}s of the loaded extensions to the given {@code proxy}.
     * <p>
     * <strong>Note:</strong> even if public this method is expected to be called only by core classes (for example,
     * {@code Control}).
     *
     * @param proxy the local proxy
     * @since 2.5.0
     */
    public void hookConnectRequestProxyListeners(Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookConnectRequestProxyListeners(proxy, hook.getConnectRequestProxyListeners());
        }
    }

    private static void hookConnectRequestProxyListeners(Proxy proxy, List<ConnectRequestProxyListener> listeners) {
        for (ConnectRequestProxyListener listener : listeners) {
            proxy.addConnectRequestProxyListener(listener);
        }
    }

    private void removeConnectRequestProxyListener(ExtensionHook hook) {
        Proxy proxy = Control.getSingleton().getProxy();
        for (ConnectRequestProxyListener listener : hook.getConnectRequestProxyListeners()) {
            proxy.removeConnectRequestProxyListener(listener);
        }
    }

    public void hookPersistentConnectionListener(Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookPersistentConnectionListeners(proxy, hook.getPersistentConnectionListener());
        }
    }

    private static void hookPersistentConnectionListeners(Proxy proxy, List<PersistentConnectionListener> listeners) {
        for (PersistentConnectionListener listener : listeners) {
            try {
                if (listener != null) {
                    proxy.addPersistentConnectionListener(listener);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void removePersistentConnectionListener(ExtensionHook hook) {
        Proxy proxy = Control.getSingleton().getProxy();
        List<PersistentConnectionListener> listenerList = hook.getPersistentConnectionListener();
        for (PersistentConnectionListener listener : listenerList) {
            try {
                if (listener != null) {
                    proxy.removePersistentConnectionListener(listener);
                }
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // ZAP: Added support for site map listeners
    public void hookSiteMapListener(SiteMapPanel siteMapPanel) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookSiteMapListeners(siteMapPanel, hook.getSiteMapListenerList());
        }
    }

    private static void hookSiteMapListeners(SiteMapPanel siteMapPanel, List<SiteMapListener> listeners) {
        for (SiteMapListener listener : listeners) {
            try {
                if (listener != null) {
                    siteMapPanel.addSiteMapListener(listener);
                }
            } catch (Exception e) {
                // ZAP: Log the exception
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void removeSiteMapListener(ExtensionHook hook) {
        if (view != null) {
            SiteMapPanel siteMapPanel = view.getSiteTreePanel();
            List<SiteMapListener> listenerList = hook.getSiteMapListenerList();
            for (SiteMapListener listener : listenerList) {
                try {
                    if (listener != null) {
                        siteMapPanel.removeSiteMapListener(listener);
                    }
                    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    // ZAP: method called by the scanner to load all scanner hooks. 
    public void hookScannerHook(Scanner scan) {
        Iterator<ExtensionHook> iter = extensionHooks.values().iterator();
        while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            List<ScannerHook> scannerHookList = hook.getScannerHookList();

            for (ScannerHook scannerHook : scannerHookList) {
                try {
                    if (hook != null) {
                        scan.addScannerHook(scannerHook);
                    }
                    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void optionsChangedAllPlugin(OptionsParam options) {
        for (ExtensionHook hook : extensionHooks.values()) {
            List<OptionsChangedListener> listenerList = hook.getOptionsChangedListenerList();
            for (OptionsChangedListener listener : listenerList) {
                try {
                    if (listener != null) {
                        listener.optionsChanged(options);
                    }
                    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void runCommandLine() {
        Extension ext;
        for (int i = 0; i < getExtensionCount(); i++) {
            ext = getExtension(i);
            if (ext instanceof CommandLineListener) {
                CommandLineListener listener = (CommandLineListener) ext;
                listener.execute(extensionHooks.get(ext).getCommandLineArgument());
            }
        }
    }

    public void sessionChangedAllPlugin(Session session) {
        logger.debug("sessionChangedAllPlugin");
        for (ExtensionHook hook : extensionHooks.values()) {
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (SessionChangedListener listener : listenerList) {
                try {
                    if (listener != null) {
                        listener.sessionChanged(session);
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void databaseOpen(Database db) {
        Extension ext;
        for (int i = 0; i < getExtensionCount(); i++) {
            ext = getExtension(i);
            try {
				ext.databaseOpen(db);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
			}
        }
    }

    public void sessionAboutToChangeAllPlugin(Session session) {
        logger.debug("sessionAboutToChangeAllPlugin");
        for (ExtensionHook hook : extensionHooks.values()) {
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (SessionChangedListener listener : listenerList) {
                try {
                    if (listener != null) {
                        listener.sessionAboutToChange(session);
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void sessionScopeChangedAllPlugin(Session session) {
        logger.debug("sessionScopeChangedAllPlugin");
        for (ExtensionHook hook : extensionHooks.values()) {
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (SessionChangedListener listener : listenerList) {
                try {
                    if (listener != null) {
                        listener.sessionScopeChanged(session);
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void sessionModeChangedAllPlugin(Mode mode) {
        logger.debug("sessionModeChangedAllPlugin");
        for (ExtensionHook hook : extensionHooks.values()) {
            List<SessionChangedListener> listenerList = hook.getSessionListenerList();
            for (SessionChangedListener listener : listenerList) {
                try {
                    if (listener != null) {
                        listener.sessionModeChanged(mode);
                    }
                    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void addonFilesAdded() {
        for (ExtensionHook hook : extensionHooks.values()) {
            List<AddonFilesChangedListener> listenerList = hook.getAddonFilesChangedListener();
            for (AddonFilesChangedListener listener : listenerList) {
                try {
                    listener.filesAdded();
                    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void addonFilesRemoved() {
        for (ExtensionHook hook : extensionHooks.values()) {
            List<AddonFilesChangedListener> listenerList = hook.getAddonFilesChangedListener();
            for (AddonFilesChangedListener listener : listenerList) {
                try {
                    listener.filesRemoved();
                    
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
    
    /**
     * Notifies {@code Extension}s' {@code AddOnInstallationStatusListener}s that the given add-on was installed.
     *
     * @param addOn the add-on that was installed, must not be {@code null}
     * @since 2.5.0
     */
    public void addOnInstalled(AddOn addOn) {
        for (ExtensionHook hook : extensionHooks.values()) {
            for (AddOnInstallationStatusListener listener : hook.getAddOnInstallationStatusListeners()) {
                try {
                    listener.addOnInstalled(addOn);
                } catch (Exception e) {
                    logger.error("An error occurred while notifying: " + listener.getClass().getCanonicalName(), e);
                }
            }
        }
    }

    /**
     * Notifies {@code Extension}s' {@code AddOnInstallationStatusListener}s that the given add-on was soft uninstalled.
     *
     * @param addOn the add-on that was soft uninstalled, must not be {@code null}
     * @param successfully if the soft uninstallation was successful, that is, no errors occurred while uninstalling it
     * @since 2.5.0
     */
    public void addOnSoftUninstalled(AddOn addOn, boolean successfully) {
        for (ExtensionHook hook : extensionHooks.values()) {
            for (AddOnInstallationStatusListener listener : hook.getAddOnInstallationStatusListeners()) {
                try {
                    listener.addOnSoftUninstalled(addOn, successfully);
                } catch (Exception e) {
                    logger.error("An error occurred while notifying: " + listener.getClass().getCanonicalName(), e);
                }
            }
        }
    }

    /**
     * Notifies {@code Extension}s' {@code AddOnInstallationStatusListener}s that the given add-on was uninstalled.
     *
     * @param addOn the add-on that was uninstalled, must not be {@code null}
     * @param successfully if the uninstallation was successful, that is, no errors occurred while uninstalling it
     * @since 2.5.0
     */
    public void addOnUninstalled(AddOn addOn, boolean successfully) {
        for (ExtensionHook hook : extensionHooks.values()) {
            for (AddOnInstallationStatusListener listener : hook.getAddOnInstallationStatusListeners()) {
                try {
                    listener.addOnUninstalled(addOn, successfully);
                } catch (Exception e) {
                    logger.error("An error occurred while notifying: " + listener.getClass().getCanonicalName(), e);
                }
            }
        }
    }

    public void startAllExtension(double progressFactor) {
        double factorPerc = progressFactor / getExtensionCount();
        
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                getExtension(i).start();
                if (view != null) {
                    view.addSplashScreenLoadingCompletion(factorPerc);
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Initialize and start all Extensions
     * This function loops for all getExtensionCount() exts
     * launching each specific initialization element (model, xml, view, hook, etc.)
     */
    public void startLifeCycle() {
        
        // Percentages are passed into the calls as doubles
    	if (view != null) {
    		view.setSplashScreenLoadingCompletion(0.0);
    	}

        // Step 3: initialize all (slow)
        initAllExtension(5.0);
        // Step 4: initialize models (quick)
        initModelAllExtension(model, 0.0);
        // Step 5: initialize xmls (quick)
        initXMLAllExtension(model.getSession(), model.getOptionsParam(), 0.0);
        // Step 6: initialize viewes (slow)
        initViewAllExtension(view, 10.0);
        // Step 7: initialize hooks (slowest)
        hookAllExtension(75.0);
        // Step 8: start all extensions(quick)
        startAllExtension(10.0);
    }

    /**
     * Initialize a specific Extension
     * @param ext the Extension that need to be initialized
     * @throws DatabaseUnsupportedException 
     * @throws DatabaseException 
     */
    public void startLifeCycle(Extension ext) throws DatabaseException, DatabaseUnsupportedException {
        ext.init();
        ext.databaseOpen(model.getDb());
        ext.initModel(model);
        ext.initXML(model.getSession(), model.getOptionsParam());
        ext.initView(view);
        
        ExtensionHook extHook = new ExtensionHook(model, view);
        try {
            ext.hook(extHook);
            extensionHooks.put(ext, extHook);

            hookContextDataFactories(ext, extHook);
            hookApiImplementors(ext, extHook);

            if (view != null) {
                // no need to hook view if no GUI
                hookView(ext, view, extHook);
                hookMenu(view, extHook);
            }
            
            hookOptions(extHook);
            ext.optionsLoaded();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        
        ext.start();

        Proxy proxy = Control.getSingleton().getProxy();
        hookProxyListeners(proxy, extHook.getProxyListenerList());

        hookPersistentConnectionListeners(proxy, extHook.getPersistentConnectionListener());
        hookConnectRequestProxyListeners(proxy, extHook.getConnectRequestProxyListeners());

        if (view != null) {
            hookSiteMapListeners(view.getSiteTreePanel(), extHook.getSiteMapListenerList());
        }
    }

    public void stopAllExtension() {
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                getExtension(i).stop();
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    // ZAP: Added the type argument.
    private void addParamPanel(List<AbstractParamPanel> panelList, AbstractParamDialog dialog) {
        String[] ROOT = {};
        for (AbstractParamPanel panel : panelList) {
            try {
                dialog.addParamPanel(ROOT, panel, true);
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    private void removeParamPanel(List<AbstractParamPanel> panelList, AbstractParamDialog dialog) {
        for (AbstractParamPanel panel : panelList) {
            try {
                dialog.removeParamPanel(panel);
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        dialog.revalidate();
    }

    private void hookAllExtension(double progressFactor) {
        final double factorPerc = progressFactor / getExtensionCount();
        
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                final Extension ext = getExtension(i);
                logger.info("Initializing " + ext.getDescription());
                final ExtensionHook extHook = new ExtensionHook(model, view);
                ext.hook(extHook);
                extensionHooks.put(ext, extHook);

                hookContextDataFactories(ext, extHook);
                hookApiImplementors(ext, extHook);

                if (view != null) {
                    EventQueue.invokeAndWait(new Runnable() {

                        @Override
                        public void run() {
                            // no need to hook view if no GUI
                            hookView(ext, view, extHook);
                            hookMenu(view, extHook);
                            view.addSplashScreenLoadingCompletion(factorPerc);
                        }
                    });
                }
                
                hookOptions(extHook);
                ext.optionsLoaded();
                
            } catch (Throwable e) {
                // Catch Errors thrown by out of date extensions as well as Exceptions
                logger.error(e.getMessage(), e);
            }
        }
        // Call postInit for all extensions after they have all been initialized
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                getExtension(i).postInit();
            } catch (Throwable e) {
                // Catch Errors thrown by out of date extensions as well as Exceptions
                logger.error(e.getMessage(), e);
            }
        }

        if (view != null) {
            view.getMainFrame().getMainMenuBar().validate();
            view.getMainFrame().validate();
        }
    }

    private void hookContextDataFactories(Extension extension, ExtensionHook extHook) {
        for (ContextDataFactory contextDataFactory : extHook.getContextDataFactories()) {
            try {
                model.addContextDataFactory(contextDataFactory);
            } catch (Exception e) {
                logger.error("Error while adding a ContextDataFactory from " + extension.getClass().getCanonicalName(), e);
            }
        }
    }

    private void hookApiImplementors(Extension extension, ExtensionHook extHook) {
        for (ApiImplementor apiImplementor : extHook.getApiImplementors()) {
            try {
                API.getInstance().registerApiImplementor(apiImplementor);
            } catch (Exception e) {
                logger.error("Error while adding an ApiImplementor from " + extension.getClass().getCanonicalName(), e);
            }
        }
    }

    /**
     * Hook command line listener with the command line processor
     *
     * @param cmdLine
     * @throws java.lang.Exception
     */
    public void hookCommandLineListener(CommandLine cmdLine) throws Exception {
        List<CommandLineArgument[]> allCommandLineList = new ArrayList<>();
        Map<String, CommandLineListener> extMap = new HashMap<>();
        for (Map.Entry<Extension, ExtensionHook> entry : extensionHooks.entrySet()) {
            ExtensionHook hook = entry.getValue();
            CommandLineArgument[] arg = hook.getCommandLineArgument();
            if (arg.length > 0) {
                allCommandLineList.add(arg);
            }
            
            Extension extension = entry.getKey();
            if (extension instanceof CommandLineListener) {
                CommandLineListener cli = (CommandLineListener) extension;
                List<String> exts = cli.getHandledExtensions();
                if (exts != null) {
                    for (String ext : exts) {
                        extMap.put(ext, cli);
                    }
                }
            }
        }

        cmdLine.parse(allCommandLineList, extMap);
    }

    private void hookMenu(View view, ExtensionHook hook) {
        if (view == null) {
            return;
        }

        ExtensionHookMenu hookMenu = hook.getHookMenu();
        if (hookMenu == null) {
            return;
        }

        MainMenuBar menuBar = view.getMainFrame().getMainMenuBar();

        // 2 menus at the back (Tools/Help)
        addMenuHelper(menuBar, hookMenu.getNewMenus(), 2);

        addMenuHelper(menuBar.getMenuFile(), hookMenu.getFile(), 2);
        addMenuHelper(menuBar.getMenuTools(), hookMenu.getTools(), 2);
        addMenuHelper(menuBar.getMenuEdit(), hookMenu.getEdit());
        addMenuHelper(menuBar.getMenuView(), hookMenu.getView());
        addMenuHelper(menuBar.getMenuAnalyse(), hookMenu.getAnalyse());
        addMenuHelper(menuBar.getMenuHelp(), hookMenu.getHelpMenus());
        addMenuHelper(menuBar.getMenuReport(), hookMenu.getReportMenus());
        addMenuHelper(menuBar.getMenuOnline(), hookMenu.getOnlineMenus());

        addMenuHelper(view.getPopupList(), hookMenu.getPopupMenus());
    }

    private void addMenuHelper(JMenu menu, List<JMenuItem> items) {
        addMenuHelper(menu, items, 0);
    }

    private void addMenuHelper(JMenuBar menuBar, List<JMenuItem> items, int existingCount) {
        for (JMenuItem item : items) {
            if (item != null) {
                menuBar.add(item, menuBar.getMenuCount() - existingCount);
            }
        }
        menuBar.revalidate();
    }

    private void addMenuHelper(JMenu menu, List<JMenuItem> items, int existingCount) {
        for (JMenuItem item : items) {
            if (item != null) {
                if (item == ExtensionHookMenu.MENU_SEPARATOR) {
                    menu.addSeparator();
                    continue;
                }

                menu.add(item, menu.getItemCount() - existingCount);
            }
        }

        menu.revalidate();
    }

    private void addMenuHelper(List<JMenuItem> menuList, List<JMenuItem> items) {
        for (JMenuItem item : items) {
            if (item != null) {
                menuList.add(item);
            }
        }
    }

    private void removeMenu(View view, ExtensionHook hook) {
        if (view == null) {
            return;
        }

        ExtensionHookMenu hookMenu = hook.getHookMenu();
        if (hookMenu == null) {
            return;
        }

        MainMenuBar menuBar = view.getMainFrame().getMainMenuBar();

        // clear up various menus
        removeMenuHelper(menuBar, hookMenu.getNewMenus());

        removeMenuHelper(menuBar.getMenuFile(), hookMenu.getFile());
        removeMenuHelper(menuBar.getMenuTools(), hookMenu.getTools());
        removeMenuHelper(menuBar.getMenuEdit(), hookMenu.getEdit());
        removeMenuHelper(menuBar.getMenuView(), hookMenu.getView());
        removeMenuHelper(menuBar.getMenuAnalyse(), hookMenu.getAnalyse());
        removeMenuHelper(menuBar.getMenuHelp(), hookMenu.getHelpMenus());
        removeMenuHelper(menuBar.getMenuReport(), hookMenu.getReportMenus());
        removeMenuHelper(menuBar.getMenuOnline(), hookMenu.getOnlineMenus());

        removeMenuHelper(view.getPopupList(), hookMenu.getPopupMenus());

        view.refreshTabViewMenus();
    }

    private void removeMenuHelper(JMenuBar menuBar, List<JMenuItem> items) {
        for (JMenuItem item : items) {
            if (item != null) {
                menuBar.remove(item);
            }
        }
        menuBar.revalidate();
    }

    private void removeMenuHelper(JMenu menu, List<JMenuItem> items) {
        for (JMenuItem item : items) {
            if (item != null) {
                menu.remove(item);
            }
        }
        menu.revalidate();
    }

    private void removeMenuHelper(List<JMenuItem> menuList, List<JMenuItem> items) {
        for (JMenuItem item : items) {
            if (item != null) {
                menuList.remove(item);
            }
        }
    }

    private void hookOptions(ExtensionHook hook) {
        List<AbstractParam> list = hook.getOptionsParamSetList();
        
        for (AbstractParam paramSet : list) {
            try {
                model.getOptionsParam().addParamSet(paramSet);
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void unloadOptions(ExtensionHook hook) {
        List<AbstractParam> list = hook.getOptionsParamSetList();

        for (AbstractParam paramSet : list) {
            try {
                model.getOptionsParam().removeParamSet(paramSet);
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void hookView(Extension extension, View view, ExtensionHook hook) {
        if (view == null) {
            return;
        }

        ExtensionHookView pv = hook.getHookView();
        if (pv == null) {
            return;
        }

        for (ContextPanelFactory contextPanelFactory : pv.getContextPanelFactories()) {
            try {
                view.addContextPanelFactory(contextPanelFactory);
            } catch (Exception e) {
                logger.error("Error while adding a ContextPanelFactory from " + extension.getClass().getCanonicalName(), e);
            }
        }

        view.getWorkbench().addPanels(pv.getSelectPanel(), WorkbenchPanel.PanelType.SELECT);
        view.getWorkbench().addPanels(pv.getWorkPanel(), WorkbenchPanel.PanelType.WORK);
        view.getWorkbench().addPanels(pv.getStatusPanel(), WorkbenchPanel.PanelType.STATUS);

        addParamPanel(pv.getSessionPanel(), view.getSessionDialog());
        addParamPanel(pv.getOptionsPanel(), view.getOptionsDialog(""));
    }

    private void removeView(Extension extension, View view, ExtensionHook hook) {
        if (view == null) {
            return;
        }

        ExtensionHookView pv = hook.getHookView();
        if (pv == null) {
            return;
        }

        for (ContextPanelFactory contextPanelFactory : pv.getContextPanelFactories()) {
            try {
                view.removeContextPanelFactory(contextPanelFactory);
            } catch (Exception e) {
                logger.error("Error while removing a ContextPanelFactory from " + extension.getClass().getCanonicalName(), e);
            }
        }

        view.getWorkbench().removePanels(pv.getSelectPanel(), WorkbenchPanel.PanelType.SELECT);
        view.getWorkbench().removePanels(pv.getWorkPanel(), WorkbenchPanel.PanelType.WORK);
        view.getWorkbench().removePanels(pv.getStatusPanel(), WorkbenchPanel.PanelType.STATUS);

        removeParamPanel(pv.getSessionPanel(), view.getSessionDialog());
        removeParamPanel(pv.getOptionsPanel(), view.getOptionsDialog(""));
    }

    public void removeStatusPanel(AbstractPanel panel) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getWorkbench().removePanel(panel, WorkbenchPanel.PanelType.STATUS);
    }

    public void removeOptionsPanel(AbstractParamPanel panel) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getOptionsDialog("").removeParamPanel(panel);
    }

    public void removeOptionsParamSet(AbstractParam params) {
        model.getOptionsParam().removeParamSet(params);
    }

    public void removeWorkPanel(AbstractPanel panel) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getWorkbench().removePanel(panel, WorkbenchPanel.PanelType.WORK);
    }

    public void removePopupMenuItem(ExtensionPopupMenuItem popupMenuItem) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getPopupList().remove(popupMenuItem);
    }

    public void removeFileMenuItem(JMenuItem menuItem) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getMainFrame().getMainMenuBar().getMenuFile().remove(menuItem);
    }

    public void removeEditMenuItem(JMenuItem menuItem) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getMainFrame().getMainMenuBar().getMenuEdit().remove(menuItem);
    }

    public void removeViewMenuItem(JMenuItem menuItem) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getMainFrame().getMainMenuBar().getMenuView().remove(menuItem);
    }

    public void removeToolsMenuItem(JMenuItem menuItem) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getMainFrame().getMainMenuBar().getMenuTools().remove(menuItem);
    }

    public void removeHelpMenuItem(JMenuItem menuItem) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getMainFrame().getMainMenuBar().getMenuHelp().remove(menuItem);
    }

    public void removeReportMenuItem(JMenuItem menuItem) {
        if (!View.isInitialised()) {
            return;
        }
        
        View.getSingleton().getMainFrame().getMainMenuBar().getMenuReport().remove(menuItem);
    }

    /**
     * Init all extensions
     */
    private void initAllExtension(double progressFactor) {        
        double factorPerc = progressFactor / getExtensionCount();
        
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                getExtension(i).init();
                getExtension(i).databaseOpen(Model.getSingleton().getDb());
                if (view != null) {
                	view.addSplashScreenLoadingCompletion(factorPerc);
                }
                
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Init all extensions with the same Model
     * @param model the model to apply to all extensions
     */
    private void initModelAllExtension(Model model, double progressFactor) {
        double factorPerc = progressFactor / getExtensionCount();
        
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                getExtension(i).initModel(model);
                if (view != null) {
                    view.addSplashScreenLoadingCompletion(factorPerc);
                }
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Init all extensions with the same View
     * @param view the View that need to be applied
     */
    private void initViewAllExtension(final View view, double progressFactor) {
        if (view == null) {
            return;
        }

        final double factorPerc = progressFactor / getExtensionCount();
        
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                final Extension extension = getExtension(i);
                EventQueue.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        extension.initView(view);
                        view.addSplashScreenLoadingCompletion(factorPerc);
                    }
                });
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void initXMLAllExtension(Session session, OptionsParam options, double progressFactor) {
        double factorPerc = progressFactor / getExtensionCount();
        
        for (int i = 0; i < getExtensionCount(); i++) {
            try {
                getExtension(i).initXML(session, options);
                if (view != null) {
                    view.addSplashScreenLoadingCompletion(factorPerc);
                }
                
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Removes an extension from internal list. As a result listeners added via
     * the {@link ExtensionHook} object are unregistered.
     *
     * @param extension
     * @param hook
     */
    public void removeExtension(Extension extension, ExtensionHook hook) {
        extensionList.remove(extension);
        extensionsMap.remove(extension.getClass());

        if (hook == null) {
            logger.info("ExtensionHook is null for \"" + extension.getClass().getCanonicalName()
                    + "\" the hooked objects will not be automatically removed.");
            return;
        }

        // by removing the ExtensionHook object,
        // the following listeners are no longer informed:
        // 		* SessionListeners
        // 		* OptionsChangedListeners
        extensionHooks.values().remove(hook);

        unloadOptions(hook);

        removePersistentConnectionListener(hook);

        removeProxyListener(hook);

        removeOverrideMessageProxyListener(hook);

        removeConnectRequestProxyListener(hook);

        removeSiteMapListener(hook);

        for (ContextDataFactory contextDataFactory : hook.getContextDataFactories()) {
            try {
                model.removeContextDataFactory(contextDataFactory);
            } catch (Exception e) {
                logger.error("Error while removing a ContextDataFactory from " + extension.getClass().getCanonicalName(), e);
            }
        }

        for (ApiImplementor apiImplementor : hook.getApiImplementors()) {
            try {
                API.getInstance().removeApiImplementor(apiImplementor);
            } catch (Exception e) {
                logger.error("Error while removing an ApiImplementor from " + extension.getClass().getCanonicalName(), e);
            }
        }

        removeViewInEDT(extension, hook);
    }

    private void removeViewInEDT(final Extension extension, final ExtensionHook hook) {
        if (view == null) {
            return;
        }

        if (EventQueue.isDispatchThread()) {
            removeView(extension, view, hook);
            removeMenu(view, hook);
        } else {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    removeViewInEDT(extension, hook);
                }
            });
        }
    }

    /**
     * Gets the names of all unsaved resources of all the extensions.
     *
     * @return a {@code List} containing all the unsaved resources of all add-ons, never {@code null}
     * @see Extension#getActiveActions()
     */
    public List<String> getUnsavedResources() {
        List<String> list = new ArrayList<>();
        List<String> l;

        for (int i = 0; i < getExtensionCount(); i++) {
            l = getExtension(i).getUnsavedResources();
            if (l != null) {
                list.addAll(l);
            }
        }
        
        return list;
    }

    /**
     * Gets the names of all active actions of all the extensions.
     *
     * @return a {@code List} containing all the active actions of all add-ons, never {@code null}
     * @since 2.4.0
     * @see Extension#getActiveActions()
     */
    public List<String> getActiveActions() {
        List<String> list = new ArrayList<>();
        List<String> l;

        for (int i = 0; i < getExtensionCount(); i++) {
            l = getExtension(i).getActiveActions();
            if (l != null) {
                list.addAll(l);
            }
        }
        
        return list;
    }

}
