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
// ZAP: 2013/11/16 Issue 845: AbstractPanel added twice to TabbedPanel2 in
// ExtensionLoader#addTabPanel
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
// ZAP: 2014/11/21 Reviewed foreach loops and commented startup process for splash screen progress
// bar
// ZAP: 2015/01/04 Issue 1379: Not all extension's listeners are hooked during add-on installation
// ZAP: 2015/01/19 Remove online menus when removeMenu(View, ExtensionHook) is called.
// ZAP: 2015/01/19 Issue 1510: New Extension.postInit() method to be called once all extensions
// loaded
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/02/10 Issue 1208: Search classes/resources in add-ons declared as dependencies
// ZAP: 2015/04/09 Generify Extension.getExtension(Class) to avoid unnecessary casts
// ZAP: 2015/09/07 Start GUI on EDT
// ZAP: 2016/04/06 Fix layouts' issues
// ZAP: 2016/04/08 Hook ContextDataFactory/ContextPanelFactory
// ZAP: 2016/05/30 Notification of installation status of the add-ons
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab
// ZAP: 2016/08/18 Hook ApiImplementor
// ZAP: 2016/11/23 Call postInit() when starting an extension, startLifeCycle(Extension).
// ZAP: 2017/02/19 Hook/remove extensions' components to/from the main tool bar.
// ZAP: 2017/06/07 Allow to notify of changes in the session's properties (e.g. name, description).
// ZAP: 2017/07/25 Hook HttpSenderListener.
// ZAP: 2017/10/11 Include add-on in extensions' initialisation errors.
// ZAP: 2017/10/31 Add JavaDoc to ExtensionLoader.getExtension(String).
// ZAP: 2018/04/25 Allow to add ProxyServer to automatically add/remove proxy related listeners to
// it.
// ZAP: 2018/07/18 Tweak logging.
// ZAP: 2018/10/05 Get menu/view hooks without initialising them.
// ZAP: 2018/10/09 Use managed ExtensionHook when removing extensions.
// ZAP: 2019/03/15 Issue 3578: Added Helper options for Import menu
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/07/25 Relocate null check to be earlier in hookScannerHook(scan) [LGTM issue].
// ZAP: 2019/08/19 Validate menu and main frame in EDT.
// ZAP: 2019/09/30 Use instance variable for view checks.
// ZAP: 2020/05/14 Hook HttpSenderListener when starting single extension.
// ZAP: 2020/08/27 Added support for plugable variants
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2021/04/13 Issue 6536: Stop and destroy extensions being removed.
// ZAP: 2021/08/17 Issue 6755: Extension's errors during shutdown prevent ZAP to exit.
// ZAP: 2021/10/01 Do not initialise view if there's none when starting a single extension.
// ZAP: 2022/02/09 Deprecate code related to core proxy, remove code no longer needed.
// ZAP: 2022/04/17 Log extension name prior to description when loading.
// ZAP: 2022/04/17 Address various SAST (SonarLint) issues.
// ZAP: 2022/06/13 Hook HrefTypeInfo.
// ZAP: 2022/08/17 Install updates before running other cmdline args.
// ZAP: 2022/11/23 Refresh tabs menu when tabs are removed.
package org.parosproxy.paros.extension;

import java.awt.Component;
import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.CommandLine;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.proxy.ConnectRequestProxyListener;
import org.parosproxy.paros.core.proxy.OverrideMessageProxyListener;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.Scanner;
import org.parosproxy.paros.core.scanner.ScannerHook;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.DatabaseUnsupportedException;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpSender;
import org.parosproxy.paros.view.AbstractParamDialog;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.MainMenuBar;
import org.parosproxy.paros.view.SiteMapPanel;
import org.parosproxy.paros.view.View;
import org.parosproxy.paros.view.WorkbenchPanel;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.control.AddOn;
import org.zaproxy.zap.extension.AddOnInstallationStatusListener;
import org.zaproxy.zap.extension.AddonFilesChangedListener;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.extension.httppanel.DisplayedMessageChangedListener;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.view.ContextPanelFactory;
import org.zaproxy.zap.view.HrefTypeInfo;
import org.zaproxy.zap.view.MainToolbarPanel;
import org.zaproxy.zap.view.SiteMapListener;

public class ExtensionLoader {

    private final List<Extension> extensionList = new ArrayList<>();
    private final Map<Class<? extends Extension>, Extension> extensionsMap = new HashMap<>();
    private final Map<Extension, ExtensionHook> extensionHooks = new HashMap<>();
    private Model model = null;

    private View view = null;
    private CommandLine cmdLine;
    private static final Logger logger = LogManager.getLogger(ExtensionLoader.class);

    @SuppressWarnings("deprecation")
    private List<org.parosproxy.paros.core.proxy.ProxyServer> proxyServers;

    public ExtensionLoader(Model model, View view) {
        this.model = model;
        this.view = view;

        this.proxyServers = new ArrayList<>();
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

    /**
     * Gets the {@code Extension} with the given name.
     *
     * @param name the name of the {@code Extension}.
     * @return the {@code Extension} or {@code null} if not found/enabled.
     * @see #getExtension(Class)
     */
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
     * @return the {@code Extension} or {@code null} if not found/enabled.
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
     * Tells whether or not an {@code Extension} with the given {@code extensionName} is enabled.
     *
     * @param extensionName the name of the extension
     * @return {@code true} if the extension is enabled, {@code false} otherwise.
     * @throws IllegalArgumentException if the {@code extensionName} is {@code null}.
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

    /**
     * Adds the given proxy server, to be automatically updated with proxy related listeners.
     *
     * @param proxyServer the proxy server to add, must not be null.
     * @since 2.8.0
     * @see #removeProxyServer(ProxyServer)
     */
    @SuppressWarnings("deprecation")
    public void addProxyServer(org.parosproxy.paros.core.proxy.ProxyServer proxyServer) {
        proxyServers.add(proxyServer);
        extensionHooks.values().forEach(extHook -> hookProxyServer(extHook, proxyServer));
    }

    @SuppressWarnings("deprecation")
    private static void hookProxyServer(
            ExtensionHook extHook, org.parosproxy.paros.core.proxy.ProxyServer proxyServer) {
        process(extHook.getProxyListenerList(), proxyServer::addProxyListener);
        process(
                extHook.getOverrideMessageProxyListenerList(),
                proxyServer::addOverrideMessageProxyListener);
        process(
                extHook.getPersistentConnectionListener(),
                proxyServer::addPersistentConnectionListener);
        process(
                extHook.getConnectRequestProxyListeners(),
                proxyServer::addConnectRequestProxyListener);
    }

    private static <T> void process(List<T> elements, Consumer<T> action) {
        try {
            elements.stream().filter(Objects::nonNull).forEach(action);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    private void hookProxies(ExtensionHook extHook) {
        for (org.parosproxy.paros.core.proxy.ProxyServer proxyServer : proxyServers) {
            hookProxyServer(extHook, proxyServer);
        }
    }

    /**
     * Removes the given proxy server.
     *
     * @param proxyServer the proxy server to remove, must not be null.
     * @since 2.8.0
     * @see #addProxyServer(ProxyServer)
     */
    @SuppressWarnings("deprecation")
    public void removeProxyServer(org.parosproxy.paros.core.proxy.ProxyServer proxyServer) {
        proxyServers.remove(proxyServer);
        extensionHooks.values().forEach(extHook -> unhookProxyServer(extHook, proxyServer));
    }

    @SuppressWarnings("deprecation")
    private void unhookProxyServer(
            ExtensionHook extHook, org.parosproxy.paros.core.proxy.ProxyServer proxyServer) {
        process(extHook.getProxyListenerList(), proxyServer::removeProxyListener);
        process(
                extHook.getOverrideMessageProxyListenerList(),
                proxyServer::removeOverrideMessageProxyListener);
        process(
                extHook.getPersistentConnectionListener(),
                proxyServer::removePersistentConnectionListener);
        process(
                extHook.getConnectRequestProxyListeners(),
                proxyServer::removeConnectRequestProxyListener);
    }

    @SuppressWarnings("deprecation")
    private void unhookProxies(ExtensionHook extHook) {
        for (org.parosproxy.paros.core.proxy.ProxyServer proxyServer : proxyServers) {
            unhookProxyServer(extHook, proxyServer);
        }
    }

    @SuppressWarnings("deprecation")
    public void hookProxyListener(org.parosproxy.paros.control.Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookProxyListeners(proxy, hook.getProxyListenerList());
        }
    }

    @SuppressWarnings("deprecation")
    private static void hookProxyListeners(
            org.parosproxy.paros.control.Proxy proxy, List<ProxyListener> listeners) {
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

    @SuppressWarnings("deprecation")
    public void hookOverrideMessageProxyListener(org.parosproxy.paros.control.Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookOverrideMessageProxyListeners(proxy, hook.getOverrideMessageProxyListenerList());
        }
    }

    @SuppressWarnings("deprecation")
    private static void hookOverrideMessageProxyListeners(
            org.parosproxy.paros.control.Proxy proxy,
            List<OverrideMessageProxyListener> listeners) {
        for (OverrideMessageProxyListener listener : listeners) {
            try {
                if (listener != null) {
                    proxy.addOverrideMessageProxyListener(listener);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Hooks (adds) the {@code ConnectRequestProxyListener}s of the loaded extensions to the given
     * {@code proxy}.
     *
     * <p><strong>Note:</strong> even if public this method is expected to be called only by core
     * classes (for example, {@code Control}).
     *
     * @param proxy the local proxy
     * @since 2.5.0
     */
    @SuppressWarnings("deprecation")
    public void hookConnectRequestProxyListeners(org.parosproxy.paros.control.Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookConnectRequestProxyListeners(proxy, hook.getConnectRequestProxyListeners());
        }
    }

    @SuppressWarnings("deprecation")
    private static void hookConnectRequestProxyListeners(
            org.parosproxy.paros.control.Proxy proxy, List<ConnectRequestProxyListener> listeners) {
        for (ConnectRequestProxyListener listener : listeners) {
            proxy.addConnectRequestProxyListener(listener);
        }
    }

    @SuppressWarnings("deprecation")
    public void hookPersistentConnectionListener(org.parosproxy.paros.control.Proxy proxy) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookPersistentConnectionListeners(proxy, hook.getPersistentConnectionListener());
        }
    }

    @SuppressWarnings("deprecation")
    private static void hookPersistentConnectionListeners(
            org.parosproxy.paros.control.Proxy proxy,
            List<PersistentConnectionListener> listeners) {
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

    // ZAP: Added support for site map listeners
    public void hookSiteMapListener(SiteMapPanel siteMapPanel) {
        for (ExtensionHook hook : extensionHooks.values()) {
            hookSiteMapListeners(siteMapPanel, hook.getSiteMapListenerList());
        }
    }

    private static void hookSiteMapListeners(
            SiteMapPanel siteMapPanel, List<SiteMapListener> listeners) {
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
        if (!hasView()) {
            return;
        }

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

    private boolean hasView() {
        return view != null;
    }

    // ZAP: method called by the scanner to load all scanner hooks.
    public void hookScannerHook(Scanner scan) {
        Iterator<ExtensionHook> iter = extensionHooks.values().iterator();
        while (iter.hasNext()) {
            ExtensionHook hook = iter.next();
            if (hook == null) {
                continue;
            }
            List<ScannerHook> scannerHookList = hook.getScannerHookList();

            for (ScannerHook scannerHook : scannerHookList) {
                try {
                    scan.addScannerHook(scannerHook);

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
                listener.preExecute(extensionHooks.get(ext).getCommandLineArgument());
            }
        }
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

    /**
     * Notifies that the properties (e.g. name, description) of the current session were changed.
     *
     * <p>Should be called only by "core" classes.
     *
     * @param session the session changed.
     * @since 2.7.0
     */
    public void sessionPropertiesChangedAllPlugin(Session session) {
        logger.debug("sessionPropertiesChangedAllPlugin");
        for (ExtensionHook hook : extensionHooks.values()) {
            for (SessionChangedListener listener : hook.getSessionListenerList()) {
                try {
                    if (listener != null) {
                        listener.sessionPropertiesChanged(session);
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
     * Notifies {@code Extension}s' {@code AddOnInstallationStatusListener}s that the given add-on
     * was installed.
     *
     * @param addOn the add-on that was installed, must not be {@code null}
     * @since 2.5.0
     */
    public void addOnInstalled(AddOn addOn) {
        for (ExtensionHook hook : extensionHooks.values()) {
            for (AddOnInstallationStatusListener listener :
                    hook.getAddOnInstallationStatusListeners()) {
                try {
                    listener.addOnInstalled(addOn);
                } catch (Exception e) {
                    logger.error(
                            "An error occurred while notifying: {}",
                            listener.getClass().getCanonicalName(),
                            e);
                }
            }
        }
    }

    /**
     * Notifies {@code Extension}s' {@code AddOnInstallationStatusListener}s that the given add-on
     * was soft uninstalled.
     *
     * @param addOn the add-on that was soft uninstalled, must not be {@code null}
     * @param successfully if the soft uninstallation was successful, that is, no errors occurred
     *     while uninstalling it
     * @since 2.5.0
     */
    public void addOnSoftUninstalled(AddOn addOn, boolean successfully) {
        for (ExtensionHook hook : extensionHooks.values()) {
            for (AddOnInstallationStatusListener listener :
                    hook.getAddOnInstallationStatusListeners()) {
                try {
                    listener.addOnSoftUninstalled(addOn, successfully);
                } catch (Exception e) {
                    logger.error(
                            "An error occurred while notifying: {}",
                            listener.getClass().getCanonicalName(),
                            e);
                }
            }
        }
    }

    /**
     * Notifies {@code Extension}s' {@code AddOnInstallationStatusListener}s that the given add-on
     * was uninstalled.
     *
     * @param addOn the add-on that was uninstalled, must not be {@code null}
     * @param successfully if the uninstallation was successful, that is, no errors occurred while
     *     uninstalling it
     * @since 2.5.0
     */
    public void addOnUninstalled(AddOn addOn, boolean successfully) {
        for (ExtensionHook hook : extensionHooks.values()) {
            for (AddOnInstallationStatusListener listener :
                    hook.getAddOnInstallationStatusListeners()) {
                try {
                    listener.addOnUninstalled(addOn, successfully);
                } catch (Exception e) {
                    logger.error(
                            "An error occurred while notifying: {}",
                            listener.getClass().getCanonicalName(),
                            e);
                }
            }
        }
    }

    public void startAllExtension(double progressFactor) {
        double factorPerc = progressFactor / getExtensionCount();

        for (int i = 0; i < getExtensionCount(); i++) {
            Extension extension = getExtension(i);
            try {
                extension.start();
                if (hasView()) {
                    view.addSplashScreenLoadingCompletion(factorPerc);
                }

            } catch (Exception e) {
                logExtensionInitError(extension, e);
            }
        }
    }

    /**
     * Initialize and start all Extensions This function loops for all getExtensionCount() exts
     * launching each specific initialization element (model, xml, view, hook, etc.)
     */
    public void startLifeCycle() {

        // Percentages are passed into the calls as doubles
        if (hasView()) {
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

        // Clear so that manually updated add-ons dont get called with cmdline args again
        this.cmdLine = null;
    }

    /**
     * Initialize a specific Extension
     *
     * @param ext the Extension that need to be initialized
     * @throws DatabaseUnsupportedException
     * @throws DatabaseException
     */
    public void startLifeCycle(Extension ext)
            throws DatabaseException, DatabaseUnsupportedException {
        ext.init();
        ext.databaseOpen(model.getDb());
        ext.initModel(model);
        ext.initXML(model.getSession(), model.getOptionsParam());

        if (hasView()) {
            ext.initView(view);
        }

        ExtensionHook extHook = new ExtensionHook(model, view);
        extensionHooks.put(ext, extHook);
        try {
            ext.hook(extHook);

            if (cmdLine != null) {
                // This extension has been added or updated via the commandline args, so
                // re-apply any args
                CommandLineArgument[] arg = extHook.getCommandLineArgument();

                if (arg.length > 0 && ext instanceof CommandLineListener) {
                    List<CommandLineArgument[]> allCommandLineList = new ArrayList<>();
                    Map<String, CommandLineListener> extMap = new HashMap<>();
                    allCommandLineList.add(arg);

                    CommandLineListener cli = (CommandLineListener) ext;
                    List<String> extensions = cli.getHandledExtensions();
                    if (extensions != null) {
                        for (String extension : extensions) {
                            extMap.put(extension, cli);
                        }
                    }
                    cmdLine.resetArgs();
                    cmdLine.parse(allCommandLineList, extMap, false);
                }
            }

            hookContextDataFactories(ext, extHook);
            hookApiImplementors(ext, extHook);
            hookHttpSenderListeners(ext, extHook);
            hookVariant(ext, extHook);
            hookHrefTypeInfo(ext, extHook);

            if (hasView()) {
                // no need to hook view if no GUI
                hookView(ext, view, extHook);
                hookMenu(view, extHook);
            }

            hookOptions(extHook);
            hookProxies(extHook);
            ext.optionsLoaded();
            ext.postInit();
        } catch (Exception e) {
            logExtensionInitError(ext, e);
        }

        ext.start();

        if (hasView()) {
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
            final Extension ext = getExtension(i);
            try {
                logger.info("Initializing {} - {}", ext.getUIName(), ext.getDescription());
                final ExtensionHook extHook = new ExtensionHook(model, view);
                extensionHooks.put(ext, extHook);
                ext.hook(extHook);

                hookContextDataFactories(ext, extHook);
                hookApiImplementors(ext, extHook);
                hookHttpSenderListeners(ext, extHook);
                hookVariant(ext, extHook);
                hookHrefTypeInfo(ext, extHook);

                if (hasView()) {
                    EventQueue.invokeAndWait(
                            () -> {
                                // no need to hook view if no GUI
                                hookView(ext, view, extHook);
                                hookMenu(view, extHook);
                                view.addSplashScreenLoadingCompletion(factorPerc);
                            });
                }

                hookOptions(extHook);
                hookProxies(extHook);
                ext.optionsLoaded();

            } catch (Throwable e) {
                // Catch Errors thrown by out of date extensions as well as Exceptions
                logExtensionInitError(ext, e);
            }
        }
        // Call postInit for all extensions after they have all been initialized
        for (int i = 0; i < getExtensionCount(); i++) {
            Extension extension = getExtension(i);
            try {
                extension.postInit();
            } catch (Throwable e) {
                // Catch Errors thrown by out of date extensions as well as Exceptions
                logExtensionInitError(extension, e);
            }
        }

        if (hasView()) {
            try {
                EventQueue.invokeAndWait(
                        () -> {
                            view.getMainFrame().getMainMenuBar().validate();
                            view.getMainFrame().validate();
                        });
            } catch (InvocationTargetException | InterruptedException e) {
                logger.warn("An error occurred while updating the UI:", e);
            }
        }
    }

    private static void logExtensionInitError(Extension extension, Throwable e) {
        StringBuilder strBuilder = new StringBuilder(150);
        strBuilder.append("Failed to initialise extension ");
        strBuilder.append(extension.getClass().getCanonicalName());
        AddOn addOn = extension.getAddOn();
        if (addOn != null) {
            strBuilder.append(" (from add-on ").append(addOn).append(')');
        }
        strBuilder.append(", cause: ");
        strBuilder.append(ExceptionUtils.getRootCauseMessage(e));
        logger.error(strBuilder, e);
    }

    private void hookContextDataFactories(Extension extension, ExtensionHook extHook) {
        for (ContextDataFactory contextDataFactory : extHook.getContextDataFactories()) {
            try {
                model.addContextDataFactory(contextDataFactory);
            } catch (Exception e) {
                logger.error(
                        "Error while adding a ContextDataFactory from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }
    }

    private void hookApiImplementors(Extension extension, ExtensionHook extHook) {
        for (ApiImplementor apiImplementor : extHook.getApiImplementors()) {
            try {
                API.getInstance().registerApiImplementor(apiImplementor);
            } catch (Exception e) {
                logger.error(
                        "Error while adding an ApiImplementor from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }
    }

    private void hookHttpSenderListeners(Extension extension, ExtensionHook extHook) {
        for (HttpSenderListener httpSenderListener : extHook.getHttpSenderListeners()) {
            try {
                HttpSender.addListener(httpSenderListener);
            } catch (Exception e) {
                logger.error(
                        "Error while adding an HttpSenderListener from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }
    }

    private void hookVariant(Extension extension, ExtensionHook extHook) {
        for (Class<? extends Variant> variant : extHook.getVariants()) {
            try {
                // Try to create a new instance just to check its possible
                variant.getDeclaredConstructor().newInstance();
                Model.getSingleton().getVariantFactory().addVariant(variant);
            } catch (Exception e) {
                logger.error(
                        "Error while adding a Variant from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }
    }

    private void hookHrefTypeInfo(Extension extension, ExtensionHook extHook) {
        for (HrefTypeInfo hrefTypeInfo : extHook.getHrefsTypeInfo()) {
            try {
                HrefTypeInfo.addType(hrefTypeInfo);
            } catch (Exception e) {
                logger.error(
                        "Error while adding a HrefTypeInfo from {}",
                        extension.getClass().getCanonicalName(),
                        e);
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
        // Save the CommandLine in case add-ons are added or replaced
        this.cmdLine = cmdLine;
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
        if (!hasView()) {
            return;
        }

        ExtensionHookMenu hookMenu = hook.getHookMenuNoInit();
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
        addMenuHelper(menuBar.getMenuImport(), hookMenu.getImport());

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
        if (!hasView()) {
            return;
        }

        ExtensionHookMenu hookMenu = hook.getHookMenuNoInit();
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
        removeMenuHelper(menuBar.getMenuImport(), hookMenu.getImport());

        removeMenuHelper(view.getPopupList(), hookMenu.getPopupMenus());
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
        if (!hasView()) {
            return;
        }

        ExtensionHookView pv = hook.getHookViewNoInit();
        if (pv == null) {
            return;
        }

        for (ContextPanelFactory contextPanelFactory : pv.getContextPanelFactories()) {
            try {
                view.addContextPanelFactory(contextPanelFactory);
            } catch (Exception e) {
                logger.error(
                        "Error while adding a ContextPanelFactory from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        MainToolbarPanel mainToolBarPanel = view.getMainFrame().getMainToolbarPanel();
        for (Component component : pv.getMainToolBarComponents()) {
            try {
                mainToolBarPanel.addToolBarComponent(component);
            } catch (Exception e) {
                logger.error(
                        "Error while adding a component to the main tool bar panel, from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        view.getWorkbench().addPanels(pv.getSelectPanel(), WorkbenchPanel.PanelType.SELECT);
        view.getWorkbench().addPanels(pv.getWorkPanel(), WorkbenchPanel.PanelType.WORK);
        view.getWorkbench().addPanels(pv.getStatusPanel(), WorkbenchPanel.PanelType.STATUS);

        addParamPanel(pv.getSessionPanel(), view.getSessionDialog());
        addParamPanel(pv.getOptionsPanel(), view.getOptionsDialog(""));

        for (DisplayedMessageChangedListener changedListener :
                pv.getRequestPanelDisplayedMessageChangedListeners()) {
            view.getRequestPanel().addDisplayedMessageChangedListener(changedListener);
        }

        for (DisplayedMessageChangedListener changedListener :
                pv.getResponsePanelDisplayedMessageChangedListeners()) {
            view.getResponsePanel().addDisplayedMessageChangedListener(changedListener);
        }
    }

    private void removeView(Extension extension, View view, ExtensionHook hook) {
        if (!hasView()) {
            return;
        }

        ExtensionHookView pv = hook.getHookViewNoInit();
        if (pv == null) {
            return;
        }

        for (ContextPanelFactory contextPanelFactory : pv.getContextPanelFactories()) {
            try {
                view.removeContextPanelFactory(contextPanelFactory);
            } catch (Exception e) {
                logger.error(
                        "Error while removing a ContextPanelFactory from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        MainToolbarPanel mainToolBarPanel = view.getMainFrame().getMainToolbarPanel();
        for (Component component : pv.getMainToolBarComponents()) {
            try {
                mainToolBarPanel.removeToolBarComponent(component);
            } catch (Exception e) {
                logger.error(
                        "Error while removing a component from the main tool bar panel, from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        view.getWorkbench().removePanels(pv.getSelectPanel(), WorkbenchPanel.PanelType.SELECT);
        view.getWorkbench().removePanels(pv.getWorkPanel(), WorkbenchPanel.PanelType.WORK);
        view.getWorkbench().removePanels(pv.getStatusPanel(), WorkbenchPanel.PanelType.STATUS);

        if (!(pv.getSelectPanel().isEmpty()
                && pv.getWorkPanel().isEmpty()
                && pv.getStatusPanel().isEmpty())) {
            view.refreshTabViewMenus();
        }

        removeParamPanel(pv.getSessionPanel(), view.getSessionDialog());
        removeParamPanel(pv.getOptionsPanel(), view.getOptionsDialog(""));

        for (DisplayedMessageChangedListener changedListener :
                pv.getRequestPanelDisplayedMessageChangedListeners()) {
            view.getRequestPanel().removeDisplayedMessageChangedListener(changedListener);
        }

        for (DisplayedMessageChangedListener changedListener :
                pv.getResponsePanelDisplayedMessageChangedListeners()) {
            view.getResponsePanel().removeDisplayedMessageChangedListener(changedListener);
        }
    }

    public void removeStatusPanel(AbstractPanel panel) {
        if (!hasView()) {
            return;
        }

        view.getWorkbench().removePanel(panel, WorkbenchPanel.PanelType.STATUS);
    }

    public void removeOptionsPanel(AbstractParamPanel panel) {
        if (!hasView()) {
            return;
        }

        view.getOptionsDialog("").removeParamPanel(panel);
    }

    public void removeOptionsParamSet(AbstractParam params) {
        model.getOptionsParam().removeParamSet(params);
    }

    public void removeWorkPanel(AbstractPanel panel) {
        if (!hasView()) {
            return;
        }

        view.getWorkbench().removePanel(panel, WorkbenchPanel.PanelType.WORK);
    }

    public void removePopupMenuItem(ExtensionPopupMenuItem popupMenuItem) {
        if (!hasView()) {
            return;
        }

        view.getPopupList().remove(popupMenuItem);
    }

    public void removeFileMenuItem(JMenuItem menuItem) {
        if (!hasView()) {
            return;
        }

        view.getMainFrame().getMainMenuBar().getMenuFile().remove(menuItem);
    }

    public void removeEditMenuItem(JMenuItem menuItem) {
        if (!hasView()) {
            return;
        }

        view.getMainFrame().getMainMenuBar().getMenuEdit().remove(menuItem);
    }

    public void removeViewMenuItem(JMenuItem menuItem) {
        if (!hasView()) {
            return;
        }

        view.getMainFrame().getMainMenuBar().getMenuView().remove(menuItem);
    }

    public void removeToolsMenuItem(JMenuItem menuItem) {
        if (!hasView()) {
            return;
        }

        view.getMainFrame().getMainMenuBar().getMenuTools().remove(menuItem);
    }

    public void removeHelpMenuItem(JMenuItem menuItem) {
        if (!hasView()) {
            return;
        }

        view.getMainFrame().getMainMenuBar().getMenuHelp().remove(menuItem);
    }

    public void removeReportMenuItem(JMenuItem menuItem) {
        if (!hasView()) {
            return;
        }

        view.getMainFrame().getMainMenuBar().getMenuReport().remove(menuItem);
    }

    /** Init all extensions */
    private void initAllExtension(double progressFactor) {
        double factorPerc = progressFactor / getExtensionCount();

        for (int i = 0; i < getExtensionCount(); i++) {
            Extension extension = getExtension(i);
            try {
                extension.init();
                extension.databaseOpen(Model.getSingleton().getDb());
                if (hasView()) {
                    view.addSplashScreenLoadingCompletion(factorPerc);
                }

            } catch (Throwable e) {
                logExtensionInitError(extension, e);
            }
        }
    }

    /**
     * Init all extensions with the same Model
     *
     * @param model the model to apply to all extensions
     */
    private void initModelAllExtension(Model model, double progressFactor) {
        double factorPerc = progressFactor / getExtensionCount();

        for (int i = 0; i < getExtensionCount(); i++) {
            Extension extension = getExtension(i);
            try {
                extension.initModel(model);
                if (hasView()) {
                    view.addSplashScreenLoadingCompletion(factorPerc);
                }

            } catch (Exception e) {
                logExtensionInitError(extension, e);
            }
        }
    }

    /**
     * Init all extensions with the same View
     *
     * @param view the View that need to be applied
     */
    private void initViewAllExtension(final View view, double progressFactor) {
        if (!hasView()) {
            return;
        }

        final double factorPerc = progressFactor / getExtensionCount();

        for (int i = 0; i < getExtensionCount(); i++) {
            final Extension extension = getExtension(i);
            try {
                EventQueue.invokeAndWait(
                        () -> {
                            extension.initView(view);
                            view.addSplashScreenLoadingCompletion(factorPerc);
                        });

            } catch (Exception e) {
                logExtensionInitError(extension, e);
            }
        }
    }

    private void initXMLAllExtension(Session session, OptionsParam options, double progressFactor) {
        double factorPerc = progressFactor / getExtensionCount();

        for (int i = 0; i < getExtensionCount(); i++) {
            Extension extension = getExtension(i);
            try {
                extension.initXML(session, options);
                if (hasView()) {
                    view.addSplashScreenLoadingCompletion(factorPerc);
                }

            } catch (Exception e) {
                logExtensionInitError(extension, e);
            }
        }
    }

    /**
     * Removes the given extension and any components added through its extension hook.
     *
     * <p>The extension is also {@link Extension#stop() stopped} and {@link Extension#destroy()
     * destroyed}.
     *
     * <p><strong>Note:</strong> This method should be called only by bootstrap classes.
     *
     * @param extension the extension to remove.
     * @since 2.8.0
     */
    public void removeExtension(Extension extension) {
        extensionList.remove(extension);
        extensionsMap.remove(extension.getClass());

        extension.stop();
        unhook(extension);
        extension.destroy();
    }

    private void unhook(Extension extension) {
        ExtensionHook hook = extensionHooks.remove(extension);
        if (hook == null) {
            logger.error(
                    "ExtensionHook not found for: {}", extension.getClass().getCanonicalName());
            return;
        }

        unloadOptions(hook);

        unhookProxies(hook);

        removeSiteMapListener(hook);

        for (ContextDataFactory contextDataFactory : hook.getContextDataFactories()) {
            try {
                model.removeContextDataFactory(contextDataFactory);
            } catch (Exception e) {
                logger.error(
                        "Error while removing a ContextDataFactory from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        for (ApiImplementor apiImplementor : hook.getApiImplementors()) {
            try {
                API.getInstance().removeApiImplementor(apiImplementor);
            } catch (Exception e) {
                logger.error(
                        "Error while removing an ApiImplementor from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        for (HttpSenderListener httpSenderListener : hook.getHttpSenderListeners()) {
            try {
                HttpSender.removeListener(httpSenderListener);
            } catch (Exception e) {
                logger.error(
                        "Error while removing an HttpSenderListener from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        for (Class<? extends Variant> variant : hook.getVariants()) {
            try {
                model.getVariantFactory().removeVariant(variant);
            } catch (Exception e) {
                logger.error(
                        "Error while removing a Variant from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        for (HrefTypeInfo hrefTypeInfo : hook.getHrefsTypeInfo()) {
            try {
                HrefTypeInfo.removeType(hrefTypeInfo);
            } catch (Exception e) {
                logger.error(
                        "Error while removing a HrefTypeInfo from {}",
                        extension.getClass().getCanonicalName(),
                        e);
            }
        }

        removeViewInEDT(extension, hook);
    }

    private void removeViewInEDT(final Extension extension, final ExtensionHook hook) {
        if (!hasView()) {
            return;
        }

        if (EventQueue.isDispatchThread()) {
            removeView(extension, view, hook);
            removeMenu(view, hook);
        } else {
            EventQueue.invokeLater(() -> removeViewInEDT(extension, hook));
        }
    }

    /**
     * Gets the names of all unsaved resources of all the extensions.
     *
     * @return a {@code List} containing all the unsaved resources of all add-ons, never {@code
     *     null}
     * @see Extension#getActiveActions()
     */
    public List<String> getUnsavedResources() {
        return collectMessages(Extension::getUnsavedResources);
    }

    private List<String> collectMessages(Function<Extension, List<String>> function) {
        return extensionList.stream()
                .map(
                        e -> {
                            try {
                                List<String> messages = function.apply(e);
                                if (messages != null) {
                                    return messages;
                                }
                            } catch (Throwable ex) {
                                logger.error(
                                        "Error while getting messages from {}",
                                        e.getClass().getCanonicalName(),
                                        ex);
                            }
                            return Collections.<String>emptyList();
                        })
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * Gets the names of all active actions of all the extensions.
     *
     * @return a {@code List} containing all the active actions of all add-ons, never {@code null}
     * @since 2.4.0
     * @see Extension#getActiveActions()
     */
    public List<String> getActiveActions() {
        return collectMessages(Extension::getActiveActions);
    }
}
