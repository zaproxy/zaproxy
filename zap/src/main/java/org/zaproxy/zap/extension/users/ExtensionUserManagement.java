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
package org.zaproxy.zap.extension.users;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.relation.Role;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.db.RecordContext;
import org.parosproxy.paros.extension.Extension;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.control.ExtensionFactory;
import org.zaproxy.zap.extension.authentication.ExtensionAuthentication;
import org.zaproxy.zap.extension.httpsessions.ExtensionHttpSessions;
import org.zaproxy.zap.extension.sessions.ExtensionSessionManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;

/**
 * The Extension for managing {@link User Users}, {@link Role Roles}, and related entities.
 *
 * <p>This class also handles the loading of {@link AuthenticationMethodType} and {@link
 * AuthenticationMethodType} classes in the system using the AddOnLoader ( {@link
 * ExtensionFactory#getAddOnLoader()}).
 */
public class ExtensionUserManagement extends ExtensionAdaptor
        implements ContextPanelFactory, ContextDataFactory {

    public static final String CONTEXT_CONFIG_USERS = Context.CONTEXT_CONFIG + ".users";
    public static final String CONTEXT_CONFIG_USERS_USER = CONTEXT_CONFIG_USERS + ".user";

    /**
     * The extension's order during loading. Make sure we load this extension AFTER the
     * Authentication one.
     */
    public static final int EXTENSION_ORDER = ExtensionAuthentication.EXTENSION_ORDER + 5;

    /** The NAME of the extension. */
    public static final String NAME = "ExtensionUserManagement";

    /** The Constant log. */
    private static final Logger log = LogManager.getLogger(ExtensionUserManagement.class);

    /** The user panels, mapped to each context. */
    private Map<Integer, ContextUsersPanel> userPanelsMap = new HashMap<>();

    /** The context managers, mapped to each context. */
    private Map<Integer, ContextUserAuthManager> contextManagers = new HashMap<>();

    private UsersAPI api;

    /** The Constant EXTENSION DEPENDENCIES. */
    private static final List<Class<? extends Extension>> EXTENSION_DEPENDENCIES;

    static {
        // Prepare a list of Extensions on which this extension depends
        List<Class<? extends Extension>> dependencies = new ArrayList<>(3);
        dependencies.add(ExtensionHttpSessions.class);
        dependencies.add(ExtensionAuthentication.class);
        dependencies.add(ExtensionSessionManagement.class);
        EXTENSION_DEPENDENCIES = Collections.unmodifiableList(dependencies);
    }

    /** A reference to the http sessions extension. */
    private ExtensionHttpSessions extensionHttpSessions;

    /** Instantiates a new extension. */
    public ExtensionUserManagement() {
        initialize();
    }

    /**
     * Gets the ExtensionHttpSessions, if it's enabled.
     *
     * @return the Http Sessions extension or null, if it's not available
     */
    protected ExtensionHttpSessions getExtensionHttpSessions() {
        if (extensionHttpSessions == null) {
            extensionHttpSessions =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionHttpSessions.class);
            if (extensionHttpSessions == null)
                log.error(
                        "Http Sessions Extension should be enabled for the {} to work.",
                        ExtensionUserManagement.class.getSimpleName());
        }
        return extensionHttpSessions;
    }

    /** Initialize the extension. */
    private void initialize() {
        this.setName(NAME);
        // Added to make sure the ExtensionForcedUser is loaded after this one.
        // See: ExtensionForcedUser#getOrder()
        this.setOrder(EXTENSION_ORDER);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("users.name");
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        // Register this as a context data factory
        extensionHook.addContextDataFactory(this);

        if (getView() != null) {
            // Factory for generating Session Context Users panels
            extensionHook.getHookView().addContextPanelFactory(this);
        }

        // Prepare API
        this.api = new UsersAPI(this);
        extensionHook.addApiImplementor(api);
    }

    @Override
    public List<Class<? extends Extension>> getDependencies() {
        return EXTENSION_DEPENDENCIES;
    }

    @Override
    public AbstractContextPropertiesPanel getContextPanel(Context ctx) {
        return getContextPanel(ctx.getId());
    }

    /**
     * Gets the context panel for a given context.
     *
     * @param contextId the context id
     * @return the context panel
     */
    private ContextUsersPanel getContextPanel(int contextId) {
        ContextUsersPanel panel = this.userPanelsMap.get(contextId);
        if (panel == null) {
            panel = new ContextUsersPanel(this, contextId);
            this.userPanelsMap.put(contextId, panel);
        }
        return panel;
    }

    /**
     * Gets the context user auth manager for a given context.
     *
     * @param contextId the context id
     * @return the context user auth manager
     */
    public ContextUserAuthManager getContextUserAuthManager(int contextId) {
        ContextUserAuthManager manager = contextManagers.get(contextId);
        if (manager == null) {
            manager = new ContextUserAuthManager(contextId);
            contextManagers.put(contextId, manager);
        }
        return manager;
    }

    /**
     * Gets an unmodifiable view of the users that are currently shown in the UI.
     *
     * @param contextId the context id
     * @return the uI configured users
     */
    public List<User> getUIConfiguredUsers(int contextId) {
        ContextUsersPanel panel = this.userPanelsMap.get(contextId);
        if (panel != null) {
            return Collections.unmodifiableList(panel.getUsersTableModel().getUsers());
        }
        return null;
    }

    /**
     * Gets the model of the users that are currently shown in the UI.
     *
     * @param contextId the context id
     * @return the users model, if any, or null, if there is no panel for the given model
     */
    public UsersTableModel getUIConfiguredUsersModel(int contextId) {
        ContextUsersPanel panel = this.userPanelsMap.get(contextId);
        if (panel != null) {
            return panel.getUsersTableModel();
        }
        return null;
    }

    @Override
    public void discardContexts() {
        this.contextManagers.clear();
        this.userPanelsMap.clear();
    }

    @Override
    public void discardContext(Context ctx) {
        this.contextManagers.remove(ctx.getId());
        this.userPanelsMap.remove(ctx.getId());
    }

    @Override
    public void loadContextData(Session session, Context context) {
        try {
            List<String> encodedUsers =
                    session.getContextDataStrings(context.getId(), RecordContext.TYPE_USER);
            ContextUserAuthManager usersManager = getContextUserAuthManager(context.getId());
            for (String e : encodedUsers) {
                User u = User.decode(context.getId(), e);
                usersManager.addUser(u);
            }
        } catch (Exception ex) {
            log.error("Unable to load Users.", ex);
        }
    }

    @Override
    public void persistContextData(Session session, Context context) {
        try {
            List<String> encodedUsers = new ArrayList<>();
            ContextUserAuthManager m = contextManagers.get(context.getId());
            if (m != null) {
                for (User u : m.getUsers()) {
                    encodedUsers.add(User.encode(u));
                }
                session.setContextData(context.getId(), RecordContext.TYPE_USER, encodedUsers);
            }
        } catch (Exception ex) {
            log.error("Unable to persist Users.", ex);
        }
    }

    /**
     * Removes all the users that are shown in the UI (for the Users context panel) and correspond
     * to a particular shared Context.
     *
     * @param sharedContext the shared context
     */
    public void removeSharedContextUsers(Context sharedContext) {
        this.getContextPanel(sharedContext.getId()).getUsersTableModel().removeAllUsers();
    }

    /**
     * Add a new user shown in the UI (for the Users context panel) that corresponds to a particular
     * shared Context.
     *
     * @param sharedContext the shared context
     * @param user the user
     */
    public void addSharedContextUser(Context sharedContext, User user) {
        this.getContextPanel(sharedContext.getId()).getUsersTableModel().addUser(user);
    }

    public List<User> getSharedContextUsers(Context sharedContext) {
        return getContextPanel(sharedContext.getId()).getUsersTableModel().getUsers();
    }

    /**
     * Removes all the that correspond to a Context with a given id.
     *
     * @param contextId the context id
     */
    public void removeContextUsers(int contextId) {
        this.getContextUserAuthManager(contextId).removeAllUsers();
    }

    @Override
    public void exportContextData(Context ctx, Configuration config) {
        ContextUserAuthManager m = contextManagers.get(ctx.getId());
        if (m != null) {
            for (User u : m.getUsers()) {
                config.addProperty(CONTEXT_CONFIG_USERS_USER, User.encode(u));
            }
        }
    }

    @Override
    public void importContextData(Context ctx, Configuration config) {
        List<Object> list = config.getList(CONTEXT_CONFIG_USERS_USER);
        ContextUserAuthManager m = getContextUserAuthManager(ctx.getId());
        for (Object o : list) {
            User usersManager = User.decode(ctx.getId(), o.toString());
            m.addUser(usersManager);
        }
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
