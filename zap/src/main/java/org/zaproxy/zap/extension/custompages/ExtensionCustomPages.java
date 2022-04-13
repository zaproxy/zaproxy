/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.zaproxy.zap.extension.custompages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.extension.stdmenus.PopupContextMenuItemFactory;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ContextDataFactory;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.ContextPanelFactory;
import org.zaproxy.zap.view.popup.PopupMenuItemContext;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContextMenuFactory;

/**
 * A ZAP extension which implements Custom Pages, which allows you to define custom pages which may
 * be used in various ZAP components.
 */
public class ExtensionCustomPages extends ExtensionAdaptor {

    // The name is public so that other extensions can access it
    public static final String NAME = "ExtensionCustomPages";

    public static final String CONTEXT_CONFIG_CUSTOM_PAGES =
            Context.CONTEXT_CONFIG + ".custompages";
    public static final String CONTEXT_CONFIG_CUSTOM_PAGE = CONTEXT_CONFIG_CUSTOM_PAGES + ".page";

    public static final List<Integer> AUTH_HTTP_STATUS_CODES =
            Collections.unmodifiableList(
                    Arrays.asList(HttpStatusCode.UNAUTHORIZED, HttpStatusCode.FORBIDDEN));

    private static final int TYPE_CUSTOM_PAGE = 600;
    private static final Logger LOGGER = LogManager.getLogger(ExtensionCustomPages.class);

    // The i18n prefix
    protected static final String PREFIX = "custompages";

    private CustomPagesUtility customPagesUtility;

    /** The Custom Page panels, mapped to each context. */
    private Map<Integer, ContextCustomPagePanel> customPagePanelsMap;

    private PopupContextMenuItemFactory popupFlagCustomPageIndicatorMenuFactory;
    private PopupMenuItemSiteNodeContextMenuFactory popupFlagCustomPageUrlMenuFactory;

    public ExtensionCustomPages() {
        super();
        this.setName(NAME);
    }

    @Override
    public void init() {
        super.init();
        customPagesUtility = new CustomPagesUtility();
        customPagePanelsMap = new HashMap<>();
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("custompages.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        extensionHook.addSessionListener(customPagesUtility);

        // Register this as a context data factory
        getModel().addContextDataFactory(customPagesUtility);

        if (getView() != null) {
            // Factory for generating Session Context custompage panels
            getView().addContextPanelFactory(customPagesUtility);
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagCustomPageIndicatorMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupFlagCustomPageUrlMenu());
        }
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString(PREFIX + ".desc");
    }

    /**
     * Gets the popup menu for flagging {@code CustomPage} patterns.
     *
     * @return the popup menu
     */
    private PopupContextMenuItemFactory getPopupFlagCustomPageIndicatorMenu() {
        if (this.popupFlagCustomPageIndicatorMenuFactory == null) {
            popupFlagCustomPageIndicatorMenuFactory =
                    new PopupContextMenuItemFactory(
                            "dd - " + Constant.messages.getString("context.flag.popup")) {

                        private static final long serialVersionUID = 2453839120088204123L;

                        @Override
                        public ExtensionPopupMenuItem getContextMenu(
                                Context context, String parentMenu) {
                            return new PopupFlagCustomPageIndicatorMenu(context);
                        }
                    };
        }
        return this.popupFlagCustomPageIndicatorMenuFactory;
    }

    /**
     * Gets the popup menu factory for flagging {@code CustomPage} URLs.
     *
     * @return the popup flag custom page URLs menu factory
     */
    private PopupMenuItemSiteNodeContextMenuFactory getPopupFlagCustomPageUrlMenu() {
        if (popupFlagCustomPageUrlMenuFactory == null) {
            popupFlagCustomPageUrlMenuFactory =
                    new PopupMenuItemSiteNodeContextMenuFactory(
                            Constant.messages.getString("context.flag.popup")) {
                        private static final long serialVersionUID = 8927418764L;

                        @Override
                        public PopupMenuItemContext getContextMenu(
                                Context context, String parentMenu) {
                            return new PopupMenuItemContext(
                                    context,
                                    parentMenu,
                                    Constant.messages.getString(
                                            "custompages.popup.url", context.getName())) {

                                private static final long serialVersionUID = 1967885623005183801L;

                                @Override
                                public void performAction(SiteNode sn) {
                                    DialogAddCustomPage dialogAddCustomPage =
                                            getDialogAddCustomPage(
                                                    context,
                                                    sn.getHistoryReference().getURI().toString());
                                    dialogAddCustomPage.setVisible(true);
                                    context.addCustomPage(dialogAddCustomPage.getCustomPage());
                                }

                                private DialogAddCustomPage getDialogAddCustomPage(
                                        Context currentContext, String url) {
                                    DialogAddCustomPage dialogAddCustomPage =
                                            new DialogAddCustomPage(getView().getMainFrame());
                                    dialogAddCustomPage.setWorkingContext(currentContext);
                                    dialogAddCustomPage
                                            .getCustomPagePageMatcherLocationsCombo()
                                            .setSelectedItem(
                                                    CustomPageMatcherLocation.URL.getName());
                                    dialogAddCustomPage.getPageMatcherTextField().setText(url);
                                    return dialogAddCustomPage;
                                }
                            };
                        }
                    };
        }
        return popupFlagCustomPageUrlMenuFactory;
    }

    private class CustomPagesUtility
            implements ContextPanelFactory, ContextDataFactory, SessionChangedListener {

        @Override
        public void loadContextData(Session session, Context context) {
            try {
                List<String> encodedCustomPages =
                        session.getContextDataStrings(context.getId(), TYPE_CUSTOM_PAGE);
                for (String e : encodedCustomPages) {
                    DefaultCustomPage cp = DefaultCustomPage.decode(context.getId(), e);
                    context.addCustomPage(cp);
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to load CustomPages.", ex);
            }
        }

        @Override
        public void persistContextData(Session session, Context context) {
            try {
                List<String> encodedCustomPages = new ArrayList<>();
                if (context != null) {
                    for (CustomPage cp : context.getCustomPages()) {
                        encodedCustomPages.add(DefaultCustomPage.encode(cp));
                    }
                    session.setContextData(context.getId(), TYPE_CUSTOM_PAGE, encodedCustomPages);
                }
            } catch (Exception ex) {
                LOGGER.error("Unable to persist CustomPages", ex);
            }
        }

        @Override
        public void exportContextData(Context ctx, Configuration config) {
            if (ctx != null) {
                for (CustomPage cp : ctx.getCustomPages()) {
                    config.addProperty(CONTEXT_CONFIG_CUSTOM_PAGE, DefaultCustomPage.encode(cp));
                }
            }
        }

        @Override
        public void importContextData(Context ctx, Configuration config) {
            List<Object> list = config.getList(CONTEXT_CONFIG_CUSTOM_PAGE);
            for (Object o : list) {
                CustomPage cp = DefaultCustomPage.decode(ctx.getId(), o.toString());
                ctx.addCustomPage(cp);
            }
        }

        @Override
        public AbstractContextPropertiesPanel getContextPanel(Context ctx) {
            ContextCustomPagePanel panel = customPagePanelsMap.get(ctx.getId());
            if (panel == null) {
                panel = new ContextCustomPagePanel(ctx.getId());
                customPagePanelsMap.put(ctx.getId(), panel);
            }
            return panel;
        }

        @Override
        public void discardContexts() {
            customPagePanelsMap.clear();
        }

        @Override
        public void discardContext(Context ctx) {
            customPagePanelsMap.remove(ctx.getId());
        }

        @Override
        public void sessionChanged(Session session) {
            // Ignore
        }

        @Override
        public void sessionAboutToChange(Session session) {
            // Ignore
        }

        @Override
        public void sessionScopeChanged(Session session) {
            // Ignore
        }

        @Override
        public void sessionModeChanged(Mode mode) {
            // Ignore
        }
    }
}
