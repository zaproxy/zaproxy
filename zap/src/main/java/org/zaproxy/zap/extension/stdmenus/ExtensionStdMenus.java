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
    private static Logger log = LogManager.getLogger(ExtensionStdMenus.class);

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
            // Be careful when changing the menu indexes (and order above) - its easy to get
            // unexpected
            // results!
            int indexMenuItem = 0;
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupExcludeFromProxyMenu(indexMenuItem));
            if (isExtensionActiveScanEnabled) {
                extensionHook
                        .getHookMenu()
                        .addPopupMenuItem(getPopupExcludeFromScanMenu(indexMenuItem));
            }
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupExcludeFromSpiderMenu(indexMenuItem));
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupContextIncludeMenu(++indexMenuItem));
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupContextIncludeSiteMenu(++indexMenuItem));
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupContextExcludeMenu(++indexMenuItem));
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupContextDataDrivenMenu(indexMenuItem)); // TODO ??

            if (isExtensionActiveScanEnabled) {
                extensionHook
                        .getHookMenu()
                        .addPopupMenuItem(getPopupMenuActiveScanCustom(++indexMenuItem));
            }

            indexMenuItem += 1;
            if (isExtensionHistoryEnabled) {
                extensionHook
                        .getHookMenu()
                        .addPopupMenuItem(
                                getPopupMenuShowInHistory(
                                        indexMenuItem)); // Both are the same index
            }
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupMenuShowInSites(indexMenuItem)); // on purpose ;)
            extensionHook
                    .getHookMenu()
                    .addPopupMenuItem(getPopupMenuOpenUrlInBrowser(++indexMenuItem));
            // extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowResponseInBrowser(indexMenuItem));

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
                log.error("Unable to get data from clipboard");
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

    private PopupMenuActiveScanCustom getPopupMenuActiveScanCustom(int menuIndex) {
        if (popupMenuActiveScanCustom == null) {
            popupMenuActiveScanCustom =
                    new PopupMenuActiveScanCustom(
                            Constant.messages.getString("ascan.custom.popup"));
        }
        return popupMenuActiveScanCustom;
    }

    private PopupMenuOpenUrlInBrowser getPopupMenuOpenUrlInBrowser(int menuIndex) {
        if (popupMenuOpenUrlInBrowser == null) {
            popupMenuOpenUrlInBrowser =
                    new PopupMenuOpenUrlInBrowser(
                            Constant.messages.getString("history.browser.popup"));
            popupMenuOpenUrlInBrowser.setMenuIndex(menuIndex);
        }
        return popupMenuOpenUrlInBrowser;
    }

    /*
     * private PopupMenuShowResponseInBrowser getPopupMenuShowResponseInBrowser(int menuIndex) { if
     * (popupMenuShowResponseInBrowser == null) { // TODO! popupMenuShowResponseInBrowser = new
     * PopupMenuShowResponseInBrowser(Constant.messages.getString("history.showresponse.popup"));
     * popupMenuShowResponseInBrowser.setMenuIndex(menuIndex); } return popupMenuShowResponseInBrowser; }
     */

    private PopupExcludeFromProxyMenu getPopupExcludeFromProxyMenu(int menuIndex) {
        if (popupExcludeFromProxyMenu == null) {
            popupExcludeFromProxyMenu = new PopupExcludeFromProxyMenu();
            popupExcludeFromProxyMenu.setMenuIndex(menuIndex);
        }
        return popupExcludeFromProxyMenu;
    }

    private PopupExcludeFromScanMenu getPopupExcludeFromScanMenu(int menuIndex) {
        if (popupExcludeFromScanMenu == null) {
            popupExcludeFromScanMenu = new PopupExcludeFromScanMenu();
            popupExcludeFromScanMenu.setMenuIndex(menuIndex);
        }
        return popupExcludeFromScanMenu;
    }

    private PopupExcludeFromSpiderMenu getPopupExcludeFromSpiderMenu(int menuIndex) {
        if (popupExcludeFromSpiderMenu == null) {
            popupExcludeFromSpiderMenu = new PopupExcludeFromSpiderMenu();
            popupExcludeFromSpiderMenu.setMenuIndex(menuIndex);
        }
        return popupExcludeFromSpiderMenu;
    }

    private PopupMenuShowInSites getPopupMenuShowInSites(int menuIndex) {
        if (popupMenuShowInSites == null) {
            popupMenuShowInSites =
                    new PopupMenuShowInSites(
                            Constant.messages.getString("sites.showinsites.popup"));
            popupMenuShowInSites.setMenuIndex(menuIndex);
        }
        return popupMenuShowInSites;
    }

    private PopupMenuShowInHistory getPopupMenuShowInHistory(int menuIndex) {
        if (popupMenuShowInHistory == null) {
            popupMenuShowInHistory =
                    new PopupMenuShowInHistory(
                            Constant.messages.getString("history.showinhistory.popup"),
                            Control.getSingleton()
                                    .getExtensionLoader()
                                    .getExtension(ExtensionHistory.class));
            popupMenuShowInHistory.setMenuIndex(menuIndex);
        }
        return popupMenuShowInHistory;
    }

    private PopupMenuItemContextInclude getPopupContextIncludeMenu(int menuIndex) {
        if (popupContextIncludeMenu == null) {
            popupContextIncludeMenu = new PopupMenuItemContextInclude();
            popupContextIncludeMenu.setParentMenuIndex(menuIndex);
        }
        return popupContextIncludeMenu;
    }

    private PopupMenuItemContextSiteInclude getPopupContextIncludeSiteMenu(int menuIndex) {
        if (popupContextIncludeSiteMenu == null) {
            popupContextIncludeSiteMenu = new PopupMenuItemContextSiteInclude();
            popupContextIncludeSiteMenu.setParentMenuIndex(menuIndex);
        }
        return popupContextIncludeSiteMenu;
    }

    private PopupMenuItemContextExclude getPopupContextExcludeMenu(int menuIndex) {
        if (popupContextExcludeMenu == null) {
            popupContextExcludeMenu = new PopupMenuItemContextExclude();
            popupContextExcludeMenu.setParentMenuIndex(menuIndex);
        }
        return popupContextExcludeMenu;
    }

    private PopupMenuItemContextDataDriven getPopupContextDataDrivenMenu(int menuIndex) {
        if (popupContextDataDrivenMenu == null) {
            popupContextDataDrivenMenu = new PopupMenuItemContextDataDriven();
            popupContextDataDrivenMenu.setParentMenuIndex(menuIndex);
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
