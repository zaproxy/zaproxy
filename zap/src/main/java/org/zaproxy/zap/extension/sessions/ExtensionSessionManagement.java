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
package org.zaproxy.zap.extension.sessions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.session.CookieBasedSessionManagementMethodType;
import org.zaproxy.zap.session.HttpAuthSessionManagementMethodType;
import org.zaproxy.zap.session.ScriptBasedSessionManagementMethodType;
import org.zaproxy.zap.session.SessionManagementMethod;
import org.zaproxy.zap.session.SessionManagementMethodType;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

/** The Extension that handles Session Management methods. */
public class ExtensionSessionManagement extends ExtensionAdaptor
        implements ContextPanelFactory, ContextDataFactory {

    /** The NAME of the extension. */
    public static final String NAME = ExtensionSessionManagement.class.getSimpleName();

    public static final String CONTEXT_CONFIG_SESSION = Context.CONTEXT_CONFIG + ".session";
    public static final String CONTEXT_CONFIG_SESSION_TYPE = CONTEXT_CONFIG_SESSION + ".type";

    /** The Constant log. */
    private static final Logger LOGGER = LogManager.getLogger(ExtensionSessionManagement.class);

    /** The automatically loaded session management method types. */
    private final List<SessionManagementMethodType> sessionManagementMethodTypes =
            new ArrayList<>();

    private final List<SessionManagementMethodChangeListener>
            sessionManagementMethodChangeListeners = new ArrayList<>();

    /** The map of context panels. */
    private final Map<Integer, ContextSessionManagementPanel> contextPanelsMap = new HashMap<>();

    private ExtensionHook extensionHook;

    public ExtensionSessionManagement() {
        super();
        initialize();
    }

    /** Initialize the extension. */
    private void initialize() {
        this.setName(NAME);
        this.setOrder(103);
    }

    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("sessionmanagement.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        this.extensionHook = extensionHook;
        // Register this as a context data factory
        extensionHook.addContextDataFactory(this);

        if (getView() != null) {
            // Factory for generating Session Context UserAuth panels
            extensionHook.getHookView().addContextPanelFactory(this);
        }

        // Load the default Session Management methods
        addSessionManagementMethodType(
                new CookieBasedSessionManagementMethodType(),
                new HttpAuthSessionManagementMethodType(),
                new ScriptBasedSessionManagementMethodType());

        // Register the api
        final SessionManagementAPI api = new SessionManagementAPI(this);
        extensionHook.addApiImplementor(api);
    }

    @Override
    public AbstractContextPropertiesPanel getContextPanel(Context context) {
        ContextSessionManagementPanel panel = this.contextPanelsMap.get(context.getId());
        if (panel == null) {
            panel = new ContextSessionManagementPanel(this, context);
            this.contextPanelsMap.put(context.getId(), panel);
        }
        return panel;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    public List<SessionManagementMethodType> getSessionManagementMethodTypes() {
        return Collections.unmodifiableList(this.sessionManagementMethodTypes);
    }

    /**
     * Adds and loads the provided {@link SessionManagementMethodType}(s)
     *
     * @param sessionManagementMethodType the {@link SessionManagementMethodType}(s) to add
     */
    public void addSessionManagementMethodType(
            SessionManagementMethodType... sessionManagementMethodType) {
        final List<SessionManagementMethodType> methodTypes = List.of(sessionManagementMethodType);
        this.sessionManagementMethodTypes.addAll(methodTypes);

        if (Objects.isNull(this.extensionHook)) {
            throw new IllegalArgumentException(
                    "The ExtensionAuthentication was not properly initialized");
        } else {
            methodTypes.forEach(methodType -> methodType.hook(this.extensionHook));
        }

        this.sessionManagementMethodChangeListeners.forEach(
                l -> l.onStateChanged(this.sessionManagementMethodTypes));

        LOGGER.info("Loaded session management method types: {}", methodTypes);
    }

    /**
     * Removes the provided {@link SessionManagementMethodType}(s)
     *
     * @param sessionManagementMethodType the {@link SessionManagementMethodType}(s) to remove
     */
    public void removeSessionManagementMethodType(
            SessionManagementMethodType... sessionManagementMethodType) {
        final List<SessionManagementMethodType> sessionManagementMethodTypes =
                List.of(sessionManagementMethodType);
        this.sessionManagementMethodTypes.removeAll(sessionManagementMethodTypes);

        this.sessionManagementMethodChangeListeners.forEach(
                l -> l.onStateChanged(this.sessionManagementMethodTypes));

        LOGGER.info("Removed session management method types: {}", sessionManagementMethodTypes);
    }

    /**
     * Register a new listener that will be notified whenever {@link SessionManagementMethodType}(s)
     * are added or removed
     *
     * @param listener the listener to remove
     */
    public void addSessionManagementMethodStateChangeListener(
            SessionManagementMethodChangeListener listener) {
        this.sessionManagementMethodChangeListeners.add(listener);
    }

    @Override
    public void discardContexts() {
        this.contextPanelsMap.clear();
    }

    @Override
    public void discardContext(Context c) {
        this.contextPanelsMap.remove(c.getId());
    }

    /**
     * Gets the session management method type for a given identifier.
     *
     * @param id the id
     * @return the session management method type for identifier
     */
    public SessionManagementMethodType getSessionManagementMethodTypeForIdentifier(int id) {
        for (SessionManagementMethodType t : getSessionManagementMethodTypes())
            if (t.getUniqueIdentifier() == id) return t;
        return null;
    }

    @Override
    public void loadContextData(Session session, Context context) {
        try {
            List<String> typeL =
                    session.getContextDataStrings(
                            context.getId(), RecordContext.TYPE_SESSION_MANAGEMENT_TYPE);
            if (typeL != null && typeL.size() > 0) {
                SessionManagementMethodType t =
                        getSessionManagementMethodTypeForIdentifier(Integer.parseInt(typeL.get(0)));
                if (t != null) {
                    context.setSessionManagementMethod(
                            t.loadMethodFromSession(session, context.getId()));
                }
            }
        } catch (DatabaseException e) {
            LOGGER.error("Unable to load Session Management method.", e);
        }
    }

    @Override
    public void persistContextData(Session session, Context context) {
        try {
            SessionManagementMethodType t = context.getSessionManagementMethod().getType();
            session.setContextData(
                    context.getId(),
                    RecordContext.TYPE_SESSION_MANAGEMENT_TYPE,
                    Integer.toString(t.getUniqueIdentifier()));
            t.persistMethodToSession(
                    session, context.getId(), context.getSessionManagementMethod());
        } catch (DatabaseException e) {
            LOGGER.error("Unable to persist Session Management method.", e);
        }
    }

    @Override
    public void exportContextData(Context ctx, Configuration config) {
        SessionManagementMethodType type = ctx.getSessionManagementMethod().getType();
        config.setProperty(CONTEXT_CONFIG_SESSION_TYPE, type.getUniqueIdentifier());
        type.exportData(config, ctx.getSessionManagementMethod());
    }

    @Override
    public void importContextData(Context ctx, Configuration config) throws ConfigurationException {
        int type = config.getInt(CONTEXT_CONFIG_SESSION_TYPE, -1);
        if (type > -1) {
            SessionManagementMethodType t = getSessionManagementMethodTypeForIdentifier(type);
            if (t != null) {
                SessionManagementMethod method = t.createSessionManagementMethod(ctx.getId());
                t.importData(config, method);
                ctx.setSessionManagementMethod(method);
            }
        }
    }
}
