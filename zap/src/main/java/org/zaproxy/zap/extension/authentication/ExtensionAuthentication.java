/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2013 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType;
import org.zaproxy.zap.authentication.FormBasedAuthenticationMethodType.FormBasedAuthenticationMethod;
import org.zaproxy.zap.authentication.HttpAuthenticationMethodType;
import org.zaproxy.zap.authentication.JsonBasedAuthenticationMethodType;
import org.zaproxy.zap.authentication.ManualAuthenticationMethodType;
import org.zaproxy.zap.authentication.ScriptBasedAuthenticationMethodType;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenuItemFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

/** The Extension that handles Authentication methods, in correlation with Contexts. */
public class ExtensionAuthentication extends ExtensionAdaptor
        implements ContextPanelFactory, ContextDataFactory {

    public static final int EXTENSION_ORDER = 52;

    /** The NAME of the extension. */
    public static final String NAME = "ExtensionAuthentication";

    /** The ID that indicates that there's no authentication method. */
    private static final int NO_AUTH_METHOD = -1;

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ExtensionAuthentication.class);

    /** The automatically loaded authentication method types. */
    List<AuthenticationMethodType> authenticationMethodTypes = new ArrayList<>();

    /** The context panels map. */
    private Map<Integer, ContextAuthenticationPanel> contextPanelsMap = new HashMap<>();

    private PopupContextMenuItemFactory popupFlagLoggedInIndicatorMenuFactory;

    private PopupContextMenuItemFactory popupFlagLoggedOutIndicatorMenuFactory;

    private HttpSenderAuthHeaderListener httpSenderAuthHeaderListener;

    AuthenticationAPI api;

    public ExtensionAuthentication() {
        super();
        initialize();
    }

    /** Initialize the extension. */
    private void initialize() {
        this.setName(NAME);
        this.setOrder(EXTENSION_ORDER);
    }

    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("authentication.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        // Register this as a context data factory
        extensionHook.addContextDataFactory(this);

        if (getView() != null) {
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoggedInIndicatorMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagLoggedOutIndicatorMenu());

            // Factory for generating Session Context UserAuth panels
            extensionHook.getHookView().addContextPanelFactory(this);
        }

        // Load the Authentication and Session Management methods
        this.loadAuthenticationMethodTypes(extensionHook);

        // Register the api
        this.api = new AuthenticationAPI(this);
        extensionHook.addApiImplementor(api);

        extensionHook.addHttpSenderListener(getHttpSenderAuthHeaderListener());
    }

    @Override
    public AbstractContextPropertiesPanel getContextPanel(Context context) {
        ContextAuthenticationPanel panel = this.contextPanelsMap.get(context.getId());
        if (panel == null) {
            panel = new ContextAuthenticationPanel(this, context);
            this.contextPanelsMap.put(context.getId(), panel);
        }
        return panel;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    /**
     * Gets the popup menu for flagging the "Logged in" pattern.
     *
     * @return the popup menu
     */
    private PopupContextMenuItemFactory getPopupFlagLoggedInIndicatorMenu() {
        if (this.popupFlagLoggedInIndicatorMenuFactory == null) {
            popupFlagLoggedInIndicatorMenuFactory =
                    new PopupContextMenuItemFactory(
                            "dd - " + Constant.messages.getString("context.flag.popup")) {

                        private static final long serialVersionUID = 2453839120088204122L;

                        @Override
                        public ExtensionPopupMenuItem getContextMenu(
                                Context context, String parentMenu) {
                            return new PopupFlagLoggedInIndicatorMenu(context);
                        }
                    };
        }
        return this.popupFlagLoggedInIndicatorMenuFactory;
    }

    /**
     * Gets the popup menu for flagging the "Logged out" pattern.
     *
     * @return the popup menu
     */
    private PopupContextMenuItemFactory getPopupFlagLoggedOutIndicatorMenu() {
        if (this.popupFlagLoggedOutIndicatorMenuFactory == null) {
            popupFlagLoggedOutIndicatorMenuFactory =
                    new PopupContextMenuItemFactory(
                            "dd - " + Constant.messages.getString("context.flag.popup")) {

                        private static final long serialVersionUID = 2453839120088204123L;

                        @Override
                        public ExtensionPopupMenuItem getContextMenu(
                                Context context, String parentMenu) {
                            return new PopupFlagLoggedOutIndicatorMenu(context);
                        }
                    };
        }
        return this.popupFlagLoggedOutIndicatorMenuFactory;
    }

    /**
     * Loads the authentication method types and hooks them up.
     *
     * @param hook the extension hook
     */
    private void loadAuthenticationMethodTypes(ExtensionHook hook) {
        this.authenticationMethodTypes.add(new FormBasedAuthenticationMethodType());
        this.authenticationMethodTypes.add(new HttpAuthenticationMethodType());
        this.authenticationMethodTypes.add(new ManualAuthenticationMethodType());
        this.authenticationMethodTypes.add(new ScriptBasedAuthenticationMethodType());
        this.authenticationMethodTypes.add(new JsonBasedAuthenticationMethodType());

        for (AuthenticationMethodType a : authenticationMethodTypes) {
            a.hook(hook);
        }

        log.info("Loaded authentication method types: {}", authenticationMethodTypes);
    }

    /**
     * Gets all the registered/loaded authentication method types.
     *
     * @return the authentication method types
     */
    public List<AuthenticationMethodType> getAuthenticationMethodTypes() {
        return authenticationMethodTypes;
    }

    /**
     * Gets the authentication method type for a given identifier.
     *
     * @param id the id
     * @return the authentication method type for identifier
     */
    public AuthenticationMethodType getAuthenticationMethodTypeForIdentifier(int id) {
        for (AuthenticationMethodType t : getAuthenticationMethodTypes())
            if (t.getUniqueIdentifier() == id) return t;
        return null;
    }

    /**
     * Gets the URI for the login request that corresponds to a given context, if any.
     *
     * @param ctx the context
     * @return the login request uri for context, or <code>null</code>, if the context does not have
     *     a 'login request' configured
     */
    public URI getLoginRequestURIForContext(Context ctx) {
        if (!(ctx.getAuthenticationMethod() instanceof FormBasedAuthenticationMethod)) return null;
        FormBasedAuthenticationMethod method =
                (FormBasedAuthenticationMethod) ctx.getAuthenticationMethod();
        try {
            return new URI(method.getLoginRequestURL(), false);
        } catch (URIException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void loadContextData(Session session, Context context) {
        try {
            List<String> typeL =
                    session.getContextDataStrings(
                            context.getId(), RecordContext.TYPE_AUTH_METHOD_TYPE);
            if (typeL != null && typeL.size() > 0) {
                AuthenticationMethodType t =
                        getAuthenticationMethodTypeForIdentifier(Integer.parseInt(typeL.get(0)));
                if (t != null) {
                    context.setAuthenticationMethod(
                            t.loadMethodFromSession(session, context.getId()));

                    String strategy =
                            session.getContextDataString(
                                    context.getId(), RecordContext.TYPE_AUTH_VERIF_STRATEGY, null);
                    if (strategy != null) {
                        try {
                            context.getAuthenticationMethod()
                                    .setAuthCheckingStrategy(
                                            AuthCheckingStrategy.valueOf(strategy));
                        } catch (Exception e) {
                            log.error("Failed to parse auth checking strategy {}", strategy, e);
                        }
                    }

                    context.getAuthenticationMethod()
                            .setPollUrl(
                                    session.getContextDataString(
                                            context.getId(),
                                            RecordContext.TYPE_AUTH_POLL_URL,
                                            null));

                    context.getAuthenticationMethod()
                            .setPollData(
                                    session.getContextDataString(
                                            context.getId(),
                                            RecordContext.TYPE_AUTH_POLL_DATA,
                                            null));

                    context.getAuthenticationMethod()
                            .setPollHeaders(
                                    session.getContextDataString(
                                            context.getId(),
                                            RecordContext.TYPE_AUTH_POLL_HEADERS,
                                            null));

                    context.getAuthenticationMethod()
                            .setPollFrequency(
                                    session.getContextDataInteger(
                                            context.getId(), RecordContext.TYPE_AUTH_POLL_FREQ, 0));

                    String freqUnits =
                            session.getContextDataString(
                                    context.getId(), RecordContext.TYPE_AUTH_POLL_FREQ_UNITS, null);
                    if (freqUnits != null) {
                        try {
                            context.getAuthenticationMethod()
                                    .setPollFrequencyUnits(
                                            AuthPollFrequencyUnits.valueOf(freqUnits));
                        } catch (Exception e) {
                            log.error("Failed to parse auth frequency units {}", freqUnits, e);
                        }
                    }

                    context.getAuthenticationMethod()
                            .setLoggedInIndicatorPattern(
                                    session.getContextDataString(
                                            context.getId(),
                                            RecordContext.TYPE_AUTH_METHOD_LOGGEDIN_INDICATOR,
                                            null));

                    context.getAuthenticationMethod()
                            .setLoggedOutIndicatorPattern(
                                    session.getContextDataString(
                                            context.getId(),
                                            RecordContext.TYPE_AUTH_METHOD_LOGGEDOUT_INDICATOR,
                                            null));
                }
            }

        } catch (DatabaseException e) {
            log.error("Unable to load Authentication method.", e);
        }
    }

    @Override
    public void persistContextData(Session session, Context context) {
        try {
            int contextIdx = context.getId();
            AuthenticationMethodType t = context.getAuthenticationMethod().getType();
            session.setContextData(
                    contextIdx,
                    RecordContext.TYPE_AUTH_METHOD_TYPE,
                    Integer.toString(t.getUniqueIdentifier()));

            if (context.getAuthenticationMethod().getAuthCheckingStrategy() != null) {
                session.setContextData(
                        contextIdx,
                        RecordContext.TYPE_AUTH_VERIF_STRATEGY,
                        context.getAuthenticationMethod().getAuthCheckingStrategy().name());
            } else {
                session.clearContextDataForType(contextIdx, RecordContext.TYPE_AUTH_VERIF_STRATEGY);
            }

            if (context.getAuthenticationMethod().getPollUrl() != null) {
                session.setContextData(
                        contextIdx,
                        RecordContext.TYPE_AUTH_POLL_URL,
                        context.getAuthenticationMethod().getPollUrl());
            } else {
                session.clearContextDataForType(contextIdx, RecordContext.TYPE_AUTH_POLL_URL);
            }
            if (context.getAuthenticationMethod().getPollData() != null) {
                session.setContextData(
                        contextIdx,
                        RecordContext.TYPE_AUTH_POLL_DATA,
                        context.getAuthenticationMethod().getPollData());
            } else {
                session.clearContextDataForType(contextIdx, RecordContext.TYPE_AUTH_POLL_DATA);
            }
            if (context.getAuthenticationMethod().getPollHeaders() != null) {
                session.setContextData(
                        contextIdx,
                        RecordContext.TYPE_AUTH_POLL_HEADERS,
                        context.getAuthenticationMethod().getPollHeaders());
            } else {
                session.clearContextDataForType(contextIdx, RecordContext.TYPE_AUTH_POLL_HEADERS);
            }
            session.setContextData(
                    contextIdx,
                    RecordContext.TYPE_AUTH_POLL_FREQ,
                    Integer.toString(context.getAuthenticationMethod().getPollFrequency()));

            if (context.getAuthenticationMethod().getPollFrequencyUnits() != null) {
                session.setContextData(
                        contextIdx,
                        RecordContext.TYPE_AUTH_POLL_FREQ_UNITS,
                        context.getAuthenticationMethod().getPollFrequencyUnits().name());
            } else {
                session.clearContextDataForType(contextIdx, RecordContext.TYPE_AUTH_VERIF_STRATEGY);
            }

            persistLoggedIndicator(
                    session,
                    contextIdx,
                    RecordContext.TYPE_AUTH_METHOD_LOGGEDIN_INDICATOR,
                    context.getAuthenticationMethod().getLoggedInIndicatorPattern());

            persistLoggedIndicator(
                    session,
                    contextIdx,
                    RecordContext.TYPE_AUTH_METHOD_LOGGEDOUT_INDICATOR,
                    context.getAuthenticationMethod().getLoggedOutIndicatorPattern());

            t.persistMethodToSession(session, contextIdx, context.getAuthenticationMethod());
        } catch (DatabaseException e) {
            log.error("Unable to persist Authentication method.", e);
        }
    }

    private static void persistLoggedIndicator(
            Session session, int contextIdx, int recordType, Pattern pattern)
            throws DatabaseException {
        if (pattern != null) {
            session.setContextData(contextIdx, recordType, pattern.toString());
        } else {
            session.clearContextDataForType(contextIdx, recordType);
        }
    }

    @Override
    public void discardContexts() {
        contextPanelsMap.clear();
    }

    @Override
    public void discardContext(Context ctx) {
        contextPanelsMap.remove(ctx.getId());
    }

    @Override
    public void exportContextData(Context ctx, Configuration config) {
        config.setProperty(
                AuthenticationMethod.CONTEXT_CONFIG_AUTH_TYPE,
                ctx.getAuthenticationMethod().getType().getUniqueIdentifier());
        if (ctx.getAuthenticationMethod().getAuthCheckingStrategy() != null) {
            config.setProperty(
                    AuthenticationMethod.CONTEXT_CONFIG_AUTH_STRATEGY,
                    ctx.getAuthenticationMethod().getAuthCheckingStrategy().name());
        }
        config.setProperty(
                AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_URL,
                ctx.getAuthenticationMethod().getPollUrl());
        config.setProperty(
                AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_DATA,
                ctx.getAuthenticationMethod().getPollData());
        config.setProperty(
                AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_HEADERS,
                ctx.getAuthenticationMethod().getPollHeaders());
        config.setProperty(
                AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_FREQ,
                ctx.getAuthenticationMethod().getPollFrequency());
        if (ctx.getAuthenticationMethod().getPollFrequencyUnits() != null) {
            config.setProperty(
                    AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_UNITS,
                    ctx.getAuthenticationMethod().getPollFrequencyUnits().name());
        }
        if (ctx.getAuthenticationMethod().getLoggedInIndicatorPattern() != null) {
            config.setProperty(
                    AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDIN,
                    ctx.getAuthenticationMethod().getLoggedInIndicatorPattern().toString());
        }
        if (ctx.getAuthenticationMethod().getLoggedOutIndicatorPattern() != null) {
            config.setProperty(
                    AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDOUT,
                    ctx.getAuthenticationMethod().getLoggedOutIndicatorPattern().toString());
        }
        ctx.getAuthenticationMethod().getType().exportData(config, ctx.getAuthenticationMethod());
    }

    @Override
    public void importContextData(Context ctx, Configuration config) throws ConfigurationException {
        int typeId = config.getInt(AuthenticationMethod.CONTEXT_CONFIG_AUTH_TYPE, NO_AUTH_METHOD);
        if (typeId == NO_AUTH_METHOD) {
            return;
        }

        AuthenticationMethodType authMethodType = getAuthenticationMethodTypeForIdentifier(typeId);
        if (authMethodType == null) {
            log.warn("No authentication method type found for ID: {}", typeId);
            return;
        }
        ctx.setAuthenticationMethod(authMethodType.createAuthenticationMethod(ctx.getId()));
        AuthenticationMethod method = ctx.getAuthenticationMethod();

        AuthCheckingStrategy strategy =
                AuthCheckingStrategy.valueOf(
                        config.getString(
                                AuthenticationMethod.CONTEXT_CONFIG_AUTH_STRATEGY,
                                AuthCheckingStrategy.EACH_RESP.name()));
        method.setAuthCheckingStrategy(strategy);

        method.setPollUrl(config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_URL, ""));
        method.setPollData(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_DATA, ""));
        method.setPollHeaders(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_HEADERS, ""));
        method.setPollFrequency(
                config.getInt(
                        AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_FREQ,
                        AuthenticationMethod.DEFAULT_POLL_FREQUENCY));

        AuthPollFrequencyUnits units =
                AuthPollFrequencyUnits.valueOf(
                        config.getString(
                                AuthenticationMethod.CONTEXT_CONFIG_AUTH_POLL_UNITS,
                                AuthPollFrequencyUnits.REQUESTS.name()));
        method.setPollFrequencyUnits(units);

        method.setLoggedInIndicatorPattern(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDIN, ""));
        method.setLoggedOutIndicatorPattern(
                config.getString(AuthenticationMethod.CONTEXT_CONFIG_AUTH_LOGGEDOUT, ""));
        method.getType().importData(config, method);
    }

    private HttpSenderAuthHeaderListener getHttpSenderAuthHeaderListener() {
        if (this.httpSenderAuthHeaderListener == null) {
            this.httpSenderAuthHeaderListener = new HttpSenderAuthHeaderListener(System::getenv);
        }
        return this.httpSenderAuthHeaderListener;
    }
}
