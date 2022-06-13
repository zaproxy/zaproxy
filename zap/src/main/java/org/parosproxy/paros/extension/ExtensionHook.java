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
// ZAP: 2012/12/19 Code Cleanup: Moved array brackets from variable name to type
// ZAP: 2012/12/20 Added listener setter for persistentConnectionListenerList.
// ZAP: 2013/01/16 Issue 453: Dynamic loading and unloading of add-ons
// ZAP: 2013/04/14 Issue 608: Rename the method ExtensionHook.addSiteMapListner to
// addSiteMapListener
// ZAP: 2013/05/02 Re-arranged all modifiers into Java coding standard order
// ZAP: 2014/03/23 Issue 1022: Proxy - Allow to override a proxied message
// ZAP: 2014/10/25 Issue 1062: Added scannerhook to be added by extensions.
// ZAP: 2016/04/08 Allow to add ContextDataFactory
// ZAP: 2016/05/30 Allow to add AddOnInstallationStatusListener
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab
// ZAP: 2016/08/18 Allow to add ApiImplementor
// ZAP: 2017/07/25 Allow to add HttpSenderListener.
// ZAP: 2017/11/23 Add an add method for OverrideMessageProxyListener.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2020/08/27 Added support for plugable variants
// ZAP: 2022/06/13 Allow to add HrefTypeInfo.
package org.parosproxy.paros.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.parosproxy.paros.common.AbstractParam;
import org.parosproxy.paros.core.proxy.ConnectRequestProxyListener;
import org.parosproxy.paros.core.proxy.OverrideMessageProxyListener;
import org.parosproxy.paros.core.proxy.ProxyListener;
import org.parosproxy.paros.core.scanner.ScannerHook;
import org.parosproxy.paros.core.scanner.Variant;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.PersistentConnectionListener;
import org.zaproxy.zap.extension.AddOnInstallationStatusListener;
import org.zaproxy.zap.extension.AddonFilesChangedListener;
import org.zaproxy.zap.extension.api.ApiImplementor;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.network.HttpSenderListener;
import org.zaproxy.zap.view.HrefTypeInfo;
import org.zaproxy.zap.view.SiteMapListener;

public class ExtensionHook {

    /**
     * The hook for menus.
     *
     * <p>Lazily initialised.
     *
     * @see #getHookMenu()
     * @see #getHookMenuNoInit()
     */
    private ExtensionHookMenu hookMenu;

    /**
     * The hook for view components.
     *
     * <p>Lazily initialised.
     *
     * @see #getHookView()
     * @see #getHookViewNoInit()
     */
    private ExtensionHookView hookView;

    private Model model = null;
    private Vector<OptionsChangedListener> optionsListenerList = new Vector<>();

    private Vector<ProxyListener> proxyListenerList = new Vector<>();

    /**
     * The {@link OverrideMessageProxyListener}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addOverrideMessageProxyListener(OverrideMessageProxyListener)
     * @see #getOverrideMessageProxyListenerList()
     */
    private List<OverrideMessageProxyListener> overrideMessageProxyListenersList;

    /**
     * The {@link ConnectRequestProxyListener}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addConnectionRequestProxyListener(ConnectRequestProxyListener)
     * @see #getConnectRequestProxyListeners()
     */
    private List<ConnectRequestProxyListener> connectRequestProxyListeners;

    private Vector<SessionChangedListener> sessionListenerList = new Vector<>();
    private Vector<AbstractParam> optionsParamSetList = new Vector<>();
    // ZAP: Added support for site map listeners
    private Vector<SiteMapListener> siteMapListenerList = new Vector<>();
    // ZAP: Added support for Scanner Hooks
    private Vector<ScannerHook> scannerHookList = new Vector<>();
    private Vector<PersistentConnectionListener> persistentConnectionListenerList = new Vector<>();
    private List<AddonFilesChangedListener> addonFilesChangedListenerList = new ArrayList<>();

    /**
     * The {@link ContextDataFactory}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addContextDataFactory(ContextDataFactory)
     * @see #getContextDataFactories()
     */
    private List<ContextDataFactory> contextDataFactories;

    /**
     * The {@link AddOnInstallationStatusListener}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addAddOnInstallationStatusListener(AddOnInstallationStatusListener)
     * @see #getAddOnInstallationStatusListeners()
     */
    private List<AddOnInstallationStatusListener> addOnInstallationStatusListeners;

    /**
     * The {@link ApiImplementor}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addApiImplementor(ApiImplementor)
     * @see #getApiImplementors()
     */
    private List<ApiImplementor> apiImplementors;

    /**
     * The {@link HttpSenderListener}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addHttpSenderListener(HttpSenderListener)
     * @see #getHttpSenderListeners()
     */
    private List<HttpSenderListener> httpSenderListeners;

    /**
     * The {@link Variant}s added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addVariant(Variant)
     * @see #getVariants()
     */
    private List<Class<? extends Variant>> variants;

    /**
     * The {@link HrefTypeInfo} added to this extension hook.
     *
     * <p>Lazily initialised.
     *
     * @see #addHrefType(HrefTypeInfo)
     * @see #getHrefsTypeInfo()
     */
    private List<HrefTypeInfo> hrefsTypeInfo;

    private ViewDelegate view = null;
    private CommandLineArgument[] arg = new CommandLineArgument[0];

    public ExtensionHook(Model model, ViewDelegate view) {
        this.view = view;
        this.model = model;
    }

    public void addOptionsChangedListener(OptionsChangedListener listener) {
        optionsListenerList.add(listener);
    }

    public void addOptionsParamSet(AbstractParam paramSet) {
        optionsParamSetList.add(paramSet);
    }

    public void addProxyListener(ProxyListener listener) {
        proxyListenerList.add(listener);
    }

    /**
     * Adds the given {@link ConnectRequestProxyListener} to the extension hook, to be later
     * notified of CONNECT requests received by the local proxy.
     *
     * <p>By default, the {@code ConnectRequestProxyListener}s added are removed from the local
     * proxy when the extension is unloaded.
     *
     * @param listener the {@code ConnectRequestProxyListener} that will be added and then notified
     * @throws IllegalArgumentException if the given {@code listener} is {@code null}.
     * @since 2.5.0
     */
    public void addConnectionRequestProxyListener(ConnectRequestProxyListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Parameter listener must not be null.");
        }

        if (connectRequestProxyListeners == null) {
            connectRequestProxyListeners = new ArrayList<>();
        }
        connectRequestProxyListeners.add(listener);
    }

    /**
     * Gets the {@link ConnectRequestProxyListener}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code
     *     ConnectRequestProxyListener}s, never {@code null}.
     * @since 2.5.0
     */
    List<ConnectRequestProxyListener> getConnectRequestProxyListeners() {
        if (connectRequestProxyListeners == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(connectRequestProxyListeners);
    }

    public void addSessionListener(SessionChangedListener listener) {
        sessionListenerList.add(listener);
    }

    /**
     * @deprecated Replaced by the method {@link #addSiteMapListener(SiteMapListener)}. It will be
     *     removed in a future release.
     */
    @Deprecated
    public void addSiteMapListner(SiteMapListener listener) {
        siteMapListenerList.add(listener);
    }

    public void addSiteMapListener(SiteMapListener listener) {
        siteMapListenerList.add(listener);
    }

    // ZAP: add a scanner hook
    public void addScannerHook(ScannerHook hook) {
        scannerHookList.add(hook);
    }

    public void addPersistentConnectionListener(PersistentConnectionListener listener) {
        persistentConnectionListenerList.add(listener);
    }

    public void addCommandLine(CommandLineArgument[] arg) {
        this.arg = arg;
    }

    public void addAddonFilesChangedListener(AddonFilesChangedListener listener) {
        addonFilesChangedListenerList.add(listener);
    }

    /**
     * Adds the given {@code listener} to the extension hook, to be later notified of changes in the
     * installation status of the add-ons.
     *
     * @param listener the listener that will be added and then notified
     * @throws IllegalArgumentException if the given {@code listener} is {@code null}.
     * @since 2.5.0
     */
    public void addAddOnInstallationStatusListener(AddOnInstallationStatusListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Parameter listener must not be null.");
        }

        if (addOnInstallationStatusListeners == null) {
            addOnInstallationStatusListeners = new ArrayList<>();
        }
        addOnInstallationStatusListeners.add(listener);
    }

    /**
     * Gets the {@link AddOnInstallationStatusListener}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code
     *     AddOnInstallationStatusListener}s, never {@code null}.
     * @since 2.5.0
     */
    List<AddOnInstallationStatusListener> getAddOnInstallationStatusListeners() {
        if (addOnInstallationStatusListeners == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(addOnInstallationStatusListeners);
    }

    /**
     * Gets the hook for menus.
     *
     * @return the hook for menus, never {@code null}.
     */
    public ExtensionHookMenu getHookMenu() {
        if (hookMenu == null) {
            hookMenu = new ExtensionHookMenu();
        }
        return hookMenu;
    }

    /**
     * Gets the hook for menus, without initialising it.
     *
     * @return the hook for menus, might be {@code null}.
     * @since 2.8.0
     */
    ExtensionHookMenu getHookMenuNoInit() {
        return hookMenu;
    }

    /**
     * Gets the hook for view components.
     *
     * @return the hook for view components, never {@code null}.
     */
    public ExtensionHookView getHookView() {
        if (hookView == null) {
            hookView = new ExtensionHookView();
        }
        return hookView;
    }

    /**
     * Gets the hook for view components, without initialising it.
     *
     * @return the hook for view components, might be {@code null}.
     * @since 2.8.0
     */
    ExtensionHookView getHookViewNoInit() {
        return hookView;
    }

    /** @return Returns the model. */
    public Model getModel() {
        return model;
    }

    /** @return Returns the optionsListenerList. */
    public Vector<OptionsChangedListener> getOptionsChangedListenerList() {
        return optionsListenerList;
    }

    public Vector<AbstractParam> getOptionsParamSetList() {
        return optionsParamSetList;
    }

    /** @return Returns the proxyListenerList. */
    public Vector<ProxyListener> getProxyListenerList() {
        return proxyListenerList;
    }

    /** @return Returns the sessionListenerList. */
    public Vector<SessionChangedListener> getSessionListenerList() {
        return sessionListenerList;
    }

    public Vector<SiteMapListener> getSiteMapListenerList() {
        return siteMapListenerList;
    }

    // ZAP: get all scannerhooks (used by extensionloader and the scanner)
    public Vector<ScannerHook> getScannerHookList() {
        return scannerHookList;
    }

    public Vector<PersistentConnectionListener> getPersistentConnectionListener() {
        return persistentConnectionListenerList;
    }

    /** @return Returns the view. */
    public ViewDelegate getView() {
        return view;
    }

    public CommandLineArgument[] getCommandLineArgument() {
        return arg;
    }

    public List<AddonFilesChangedListener> getAddonFilesChangedListener() {
        return addonFilesChangedListenerList;
    }

    /**
     * Adds the given {@code overrideMessageProxyListener} to the extension hook, to be later added
     * to the {@link org.parosproxy.paros.control.Proxy Proxy}.
     *
     * <p>By default, the {@code OverrideMessageProxyListener}s added to this extension hook are
     * removed from the {@code Proxy} when the extension is unloaded.
     *
     * @param overrideMessageProxyListener the {@code OverrideMessageProxyListener} that will be
     *     added to the {@code Proxy}
     * @throws IllegalArgumentException if the given {@code overrideMessageProxyListener} is {@code
     *     null}.
     * @since 2.7.0
     */
    public void addOverrideMessageProxyListener(
            OverrideMessageProxyListener overrideMessageProxyListener) {
        getOverrideMessageProxyListenerList().add(overrideMessageProxyListener);
    }

    /**
     * Gets the {@link OverrideMessageProxyListener}s added to this hook.
     *
     * <p>While it's possible to add the listener with this method it's not recommended, use {@link
     * #addOverrideMessageProxyListener(OverrideMessageProxyListener)} whenever possible. The
     * accessibility of this method might change in a future version (to package access).
     *
     * @return a {@code List} containing the added {@code OverrideMessageProxyListener}s, never
     *     {@code null}.
     * @since 2.3.0
     */
    public List<OverrideMessageProxyListener> getOverrideMessageProxyListenerList() {
        if (overrideMessageProxyListenersList == null) {
            overrideMessageProxyListenersList = new ArrayList<>();
        }
        return overrideMessageProxyListenersList;
    }

    /**
     * Adds the given {@link ContextDataFactory} to the extension hook, to be later added to the
     * {@link Model}.
     *
     * <p>By default, the {@code ContextDataFactory}s added are removed from the {@code Model} when
     * the extension is unloaded.
     *
     * @param contextDataFactory the {@code ContextDataFactory} that will be added to the {@code
     *     Model}
     * @since 2.5.0
     */
    public void addContextDataFactory(ContextDataFactory contextDataFactory) {
        if (contextDataFactories == null) {
            contextDataFactories = new ArrayList<>();
        }
        contextDataFactories.add(contextDataFactory);
    }

    /**
     * Gets the {@link ContextDataFactory}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code ContextDataFactory}s, never
     *     {@code null}.
     * @since 2.5.0
     */
    List<ContextDataFactory> getContextDataFactories() {
        if (contextDataFactories == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(contextDataFactories);
    }

    /**
     * Adds the given {@code apiImplementor} to the extension hook, to be later added to the {@link
     * org.zaproxy.zap.extension.api.API API}.
     *
     * <p>By default, the {@code ApiImplementor}s added to this extension hook are removed from the
     * {@code API} when the extension is unloaded.
     *
     * @param apiImplementor the ApiImplementor that will be added to the ZAP API
     * @throws IllegalArgumentException if the given {@code apiImplementor} is {@code null}.
     * @since 2.6.0
     */
    public void addApiImplementor(ApiImplementor apiImplementor) {
        if (apiImplementor == null) {
            throw new IllegalArgumentException("Parameter apiImplementor must not be null.");
        }

        if (apiImplementors == null) {
            apiImplementors = new ArrayList<>();
        }
        apiImplementors.add(apiImplementor);
    }

    /**
     * Gets the {@link ApiImplementor}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code ApiImplementor}s, never
     *     {@code null}.
     * @since 2.6.0
     */
    List<ApiImplementor> getApiImplementors() {
        if (apiImplementors == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(apiImplementors);
    }

    /**
     * Adds the given {@code httpSenderListener} to the extension hook, to be later added to the
     * {@link org.parosproxy.paros.network.HttpSender HttpSender}.
     *
     * <p>By default, the {@code HttpSenderListener}s added to this extension hook are removed from
     * the {@code HttpSender} when the extension is unloaded.
     *
     * @param httpSenderListener the HttpSenderListener that will be added to the {@code HttpSender}
     * @throws IllegalArgumentException if the given {@code httpSenderListener} is {@code null}.
     * @since 2.7.0
     */
    public void addHttpSenderListener(HttpSenderListener httpSenderListener) {
        if (httpSenderListener == null) {
            throw new IllegalArgumentException("Parameter httpSenderListener must not be null.");
        }

        if (httpSenderListeners == null) {
            httpSenderListeners = new ArrayList<>();
        }
        httpSenderListeners.add(httpSenderListener);
    }

    /**
     * Gets the {@link HttpSenderListener}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code HttpSenderListener}s, never
     *     {@code null}.
     * @since 2.7.0
     */
    List<HttpSenderListener> getHttpSenderListeners() {
        if (httpSenderListeners == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(httpSenderListeners);
    }

    /**
     * Adds the given {@code variant} to the extension hook, to be later added to the {@link
     * org.parosproxy.paros.model.Model Model}.
     *
     * <p>By default, the {@code Variant}s added to this extension hook are removed from the {@code
     * Model} when the extension is unloaded.
     *
     * @param variant the Variant that will be added to the {@code Model}
     * @throws IllegalArgumentException if the given {@code variant} is {@code null}.
     * @since 2.10.0
     */
    public void addVariant(Class<? extends Variant> variant) {
        if (variant == null) {
            throw new IllegalArgumentException("Parameter variant must not be null.");
        }

        if (variants == null) {
            variants = new ArrayList<>();
        }
        variants.add(variant);
    }

    /**
     * Gets the {@link Variant}s added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code Variant}s, never {@code
     *     null}.
     * @since 2.10.0
     */
    List<Class<? extends Variant>> getVariants() {
        if (variants == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(variants);
    }

    /**
     * Adds the given {@link HrefTypeInfo} to the hook.
     *
     * @param hrefTypeInfo the {@code HrefTypeInfo}.
     * @since 2.12.0
     */
    public void addHrefType(HrefTypeInfo hrefTypeInfo) {
        if (hrefsTypeInfo == null) {
            hrefsTypeInfo = new ArrayList<>(1);
        }
        hrefsTypeInfo.add(hrefTypeInfo);
    }

    /**
     * Gets the {@link HrefTypeInfo} added to this hook.
     *
     * @return an unmodifiable {@code List} containing the added {@code HrefTypeInfo}, never {@code
     *     null}.
     */
    List<HrefTypeInfo> getHrefsTypeInfo() {
        if (hrefsTypeInfo == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(hrefsTypeInfo);
    }
}
