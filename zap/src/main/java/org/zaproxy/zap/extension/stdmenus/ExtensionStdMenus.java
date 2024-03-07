/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.stdmenus;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionLoader;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.Model;
import org.zaproxy.zap.extension.ascan.ExtensionActiveScan;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.ContextExportDialog;
import org.zaproxy.zap.view.DeleteContextAction;
import org.zaproxy.zap.view.popup.MenuWeights;
import org.zaproxy.zap.view.popup.PopupMenuItemContextDataDriven;
import org.zaproxy.zap.view.popup.PopupMenuItemContextExclude;
import org.zaproxy.zap.view.popup.PopupMenuItemContextInclude;

public class ExtensionStdMenus extends ExtensionAdaptor implements ClipboardOwner {

    public static final String NAME = "ExtensionStandardMenus";

    private PopupCopyMenu popupCopyMenu = null;
    private PopupPasteMenu popupPaste = null;
    private PopupMenuActiveScanCustom popupMenuActiveScanCustom = null;
    private PopupExcludeFromProxyMenu popupExcludeFromProxyMenu = null;
    private PopupExcludeFromScanMenu popupExcludeFromScanMenu = null;
    private PopupExcludeFromSpiderMenu popupExcludeFromSpiderMenu = null;
    private PopupMenuShowInHistory popupMenuShowInHistory = null;
    private PopupMenuShowInSites popupMenuShowInSites = null;
    private PopupMenuOpenUrlInBrowser popupMenuOpenUrlInBrowser = null;
    private PopupMenuItemContextInclude popupContextIncludeMenu = null;
    private PopupMenuItemContextSiteInclude popupContextIncludeSiteMenu = null;
    private PopupMenuItemContextExclude popupContextExcludeMenu = null;
    private PopupMenuItemContextDataDriven popupContextDataDrivenMenu = null;
    private PopupContextTreeMenu popupContextTreeMenuInScope = null;
    private PopupContextTreeMenu popupContextTreeMenuOutScope = null;
    private PopupContextTreeMenu popupContextTreeMenuDelete = null;
    private PopupContextTreeMenu popupContextTreeMenuExport;

    // Still being developed
    // private PopupMenuShowResponseInBrowser popupMenuShowResponseInBrowser = null;
    private static final Logger LOGGER = LogManager.getLogger(ExtensionStdMenus.class);

    public ExtensionStdMenus() {
        super();
        initialize();
    }

    private void initialize() {
        this.setName(NAME);
        this.setOrder(31);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("std.menu.ext.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);

        if (getView() != null) {
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuCopy());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuPaste());

            final ExtensionLoader extensionLoader = Control.getSingleton().getExtensionLoader();
            boolean isExtensionHistoryEnabled =
                    extensionLoader.isExtensionEnabled(ExtensionHistory.NAME);
            boolean isExtensionActiveScanEnabled =
                    extensionLoader.isExtensionEnabled(ExtensionActiveScan.NAME);
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromProxyMenu());
            if (isExtensionActiveScanEnabled) {
                extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromScanMenu());
            }
            extensionHook.getHookMenu().addPopupMenuItem(getPopupExcludeFromSpiderMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextIncludeMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextIncludeSiteMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextExcludeMenu());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextDataDrivenMenu());

            if (isExtensionActiveScanEnabled) {
                extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuActiveScanCustom());
            }

            if (isExtensionHistoryEnabled) {
                extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowInHistory());
            }
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowInSites());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuOpenUrlInBrowser());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextTreeMenuInScope());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextTreeMenuOutScope());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextTreeMenuDelete());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupContextTreeMenuExport());
        }
    }

    private PopupContextTreeMenu getPopupContextTreeMenuInScope() {
        if (popupContextTreeMenuInScope == null) {
            popupContextTreeMenuInScope =
                    new PopupContextTreeMenu() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public boolean isEnabledForContext(int contextId) {
                            Context ctx = Model.getSingleton().getSession().getContext(contextId);
                            return ctx != null && !ctx.isInScope();
                        }
                    };
            popupContextTreeMenuInScope.setText(
                    Constant.messages.getString("context.inscope.popup"));
            popupContextTreeMenuInScope.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            for (Integer id : popupContextTreeMenuInScope.getContextIds()) {
                                Context ctx = Model.getSingleton().getSession().getContext(id);

                                ctx.setInScope(true);
                                Model.getSingleton().getSession().saveContext(ctx);
                            }
                        }
                    });
            popupContextTreeMenuInScope.setWeight(MenuWeights.MENU_CONTEXT_SCOPE_WEIGHT);
        }
        return popupContextTreeMenuInScope;
    }

    private PopupContextTreeMenu getPopupContextTreeMenuOutScope() {
        if (popupContextTreeMenuOutScope == null) {
            popupContextTreeMenuOutScope =
                    new PopupContextTreeMenu() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public boolean isEnabledForContext(int contextId) {
                            Context ctx = Model.getSingleton().getSession().getContext(contextId);
                            return ctx != null && ctx.isInScope();
                        }
                    };
            popupContextTreeMenuOutScope.setText(
                    Constant.messages.getString("context.outscope.popup"));
            popupContextTreeMenuOutScope.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            for (Integer id : popupContextTreeMenuOutScope.getContextIds()) {
                                Context ctx = Model.getSingleton().getSession().getContext(id);

                                ctx.setInScope(false);
                                Model.getSingleton().getSession().saveContext(ctx);
                            }
                        }
                    });
            popupContextTreeMenuOutScope.setWeight(MenuWeights.MENU_CONTEXT_SCOPE_WEIGHT);
        }
        return popupContextTreeMenuOutScope;
    }

    private PopupContextTreeMenu getPopupContextTreeMenuDelete() {
        if (popupContextTreeMenuDelete == null) {
            popupContextTreeMenuDelete = new PopupContextTreeMenu();
            popupContextTreeMenuDelete.setAction(
                    new DeleteContextAction() {

                        private static final long serialVersionUID = 1L;

                        @Override
                        protected List<Context> getContexts() {
                            List<Context> contexts = new ArrayList<>();
                            for (Integer id : popupContextTreeMenuDelete.getContextIds()) {
                                contexts.add(Model.getSingleton().getSession().getContext(id));
                            }
                            return contexts;
                        }

                        @Override
                        protected Context getContext() {
                            return Model.getSingleton()
                                    .getSession()
                                    .getContext(popupContextTreeMenuDelete.getContextId());
                        }
                    });
            popupContextTreeMenuDelete.setText(Constant.messages.getString("context.delete.popup"));
            popupContextTreeMenuDelete.setWeight(MenuWeights.MENU_CONTEXT_DELETE_WEIGHT);
        }
        return popupContextTreeMenuDelete;
    }

    private PopupContextTreeMenu getPopupContextTreeMenuExport() {
        if (popupContextTreeMenuExport == null) {
            popupContextTreeMenuExport = new PopupContextTreeMenu(false);
            popupContextTreeMenuExport.setText(
                    Constant.messages.getString("menu.file.context.export"));
            popupContextTreeMenuExport.setIcon(
                    DisplayUtils.getScaledIcon(
                            new ImageIcon(
                                    ExtensionStdMenus.class.getResource(
                                            "/resource/icon/fugue/application-blue-export.png"))));
            popupContextTreeMenuExport.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            Context context =
                                    Model.getSingleton()
                                            .getSession()
                                            .getContext(popupContextTreeMenuExport.getContextId());
                            ContextExportDialog exportDialog =
                                    new ContextExportDialog(getView().getMainFrame());
                            exportDialog.setSelectedContext(context);
                            exportDialog.setVisible(true);
                        }
                    });
            popupContextTreeMenuExport.setWeight(MenuWeights.MENU_CONTEXT_EXPORT_WEIGHT);
        }
        return popupContextTreeMenuExport;
    }

    private PopupCopyMenu getPopupMenuCopy() {
        if (popupCopyMenu == null) {
            popupCopyMenu = new PopupCopyMenu();
            popupCopyMenu.setText(Constant.messages.getString("copy.copy.popup"));
            popupCopyMenu.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            setClipboardContents(popupCopyMenu.getLastInvoker().getSelectedText());
                        }
                    });
        }
        return popupCopyMenu;
    }

    private PopupPasteMenu getPopupMenuPaste() {
        if (popupPaste == null) {
            popupPaste = new PopupPasteMenu();
            popupPaste.setText(Constant.messages.getString("paste.paste.popup"));
            popupPaste.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            popupPaste
                                    .getLastInvoker()
                                    .setText(
                                            popupPaste.getLastInvoker().getText()
                                                    + getClipboardContents());
                        }
                    });
        }
        return popupPaste;
    }

    private String getClipboardContents() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clipboard.getContents(null);

        if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                LOGGER.error("Unable to get data from clipboard");
            }
        }

        return "";
    }

    private void setClipboardContents(String str) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(str), this);
    }

    @Override
    public void lostOwnership(Clipboard arg0, Transferable arg1) {
        // Ignore
    }

    private PopupMenuActiveScanCustom getPopupMenuActiveScanCustom() {
        if (popupMenuActiveScanCustom == null) {
            popupMenuActiveScanCustom =
                    new PopupMenuActiveScanCustom(
                            Constant.messages.getString("ascan.custom.popup"));
        }
        return popupMenuActiveScanCustom;
    }

    private PopupMenuOpenUrlInBrowser getPopupMenuOpenUrlInBrowser() {
        if (popupMenuOpenUrlInBrowser == null) {
            popupMenuOpenUrlInBrowser =
                    new PopupMenuOpenUrlInBrowser(
                            Constant.messages.getString("history.browser.popup"));
            popupMenuOpenUrlInBrowser.setWeight(MenuWeights.MENU_OPEN_SYS_BROWSER_WEIGHT);
            popupMenuOpenUrlInBrowser.setParentWeight(MenuWeights.MENU_OPEN_SYS_BROWSER_WEIGHT);
        }
        return popupMenuOpenUrlInBrowser;
    }

    private PopupExcludeFromProxyMenu getPopupExcludeFromProxyMenu() {
        if (popupExcludeFromProxyMenu == null) {
            popupExcludeFromProxyMenu = new PopupExcludeFromProxyMenu();
            popupExcludeFromProxyMenu.setWeight(MenuWeights.MENU_EXCLUDE_PROXY_WEIGHT);
            popupExcludeFromProxyMenu.setParentWeight(MenuWeights.MENU_EXCLUDE_WEIGHT);
        }
        return popupExcludeFromProxyMenu;
    }

    private PopupExcludeFromScanMenu getPopupExcludeFromScanMenu() {
        if (popupExcludeFromScanMenu == null) {
            popupExcludeFromScanMenu = new PopupExcludeFromScanMenu();
            popupExcludeFromScanMenu.setWeight(MenuWeights.MENU_EXCLUDE_SCANNER_WEIGHT);
            popupExcludeFromScanMenu.setParentWeight(MenuWeights.MENU_EXCLUDE_WEIGHT);
        }
        return popupExcludeFromScanMenu;
    }

    private PopupExcludeFromSpiderMenu getPopupExcludeFromSpiderMenu() {
        if (popupExcludeFromSpiderMenu == null) {
            popupExcludeFromSpiderMenu = new PopupExcludeFromSpiderMenu();
            popupExcludeFromSpiderMenu.setWeight(MenuWeights.MENU_EXCLUDE_SPIDER_WEIGHT);
            popupExcludeFromSpiderMenu.setParentWeight(MenuWeights.MENU_EXCLUDE_WEIGHT);
        }
        return popupExcludeFromSpiderMenu;
    }

    private PopupMenuShowInSites getPopupMenuShowInSites() {
        if (popupMenuShowInSites == null) {
            popupMenuShowInSites =
                    new PopupMenuShowInSites(
                            Constant.messages.getString("sites.showinsites.popup"));
            popupMenuShowInSites.setWeight(MenuWeights.MENU_SITES_SHOW_WEIGHT);
        }
        return popupMenuShowInSites;
    }

    private PopupMenuShowInHistory getPopupMenuShowInHistory() {
        if (popupMenuShowInHistory == null) {
            popupMenuShowInHistory =
                    new PopupMenuShowInHistory(
                            Constant.messages.getString("history.showinhistory.popup"),
                            Control.getSingleton()
                                    .getExtensionLoader()
                                    .getExtension(ExtensionHistory.class));
            popupMenuShowInHistory.setWeight(MenuWeights.MENU_HISTORY_SHOW_WEIGHT);
        }
        return popupMenuShowInHistory;
    }

    private PopupMenuItemContextInclude getPopupContextIncludeMenu() {
        if (popupContextIncludeMenu == null) {
            popupContextIncludeMenu = new PopupMenuItemContextInclude();
            popupContextIncludeMenu.setWeight(MenuWeights.MENU_INC_CONTEXT_WEIGHT);
            popupContextIncludeMenu.setParentWeight(MenuWeights.MENU_INC_CONTEXT_WEIGHT);
        }
        return popupContextIncludeMenu;
    }

    private PopupMenuItemContextSiteInclude getPopupContextIncludeSiteMenu() {
        if (popupContextIncludeSiteMenu == null) {
            popupContextIncludeSiteMenu = new PopupMenuItemContextSiteInclude();
            popupContextIncludeSiteMenu.setWeight(MenuWeights.MENU_INC_SITE_CONTEXT_WEIGHT);
            popupContextIncludeSiteMenu.setParentWeight(MenuWeights.MENU_INC_SITE_CONTEXT_WEIGHT);
        }
        return popupContextIncludeSiteMenu;
    }

    private PopupMenuItemContextExclude getPopupContextExcludeMenu() {
        if (popupContextExcludeMenu == null) {
            popupContextExcludeMenu = new PopupMenuItemContextExclude();
            popupContextExcludeMenu.setWeight(MenuWeights.MENU_EXC_CONTEXT_WEIGHT);
            popupContextExcludeMenu.setParentWeight(MenuWeights.MENU_EXC_CONTEXT_WEIGHT);
        }
        return popupContextExcludeMenu;
    }

    private PopupMenuItemContextDataDriven getPopupContextDataDrivenMenu() {
        if (popupContextDataDrivenMenu == null) {
            popupContextDataDrivenMenu = new PopupMenuItemContextDataDriven();
            popupContextDataDrivenMenu.setParentWeight(MenuWeights.MENU_FLAG_CONTEXT_WEIGHT);
        }
        return popupContextDataDrivenMenu;
    }

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("stdexts.desc");
    }

    /** No database tables used, so all supported */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }
}
