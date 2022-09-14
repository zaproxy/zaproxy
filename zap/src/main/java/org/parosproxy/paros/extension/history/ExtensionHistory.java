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

// ZAP: 2011/04/08 Changed to support clearview() in HttpPanels
// ZAP: 2011/04/08 Changed to use PopupMenuResendMessage
// ZAP: 2011/07/23 Use new add alert popup
// ZAP: 2011/09/06 Fix alert save plus concurrent mod exceptions
// ZAP: 2011/10/23 Fix add note and manage tags dialogs
// ZAP: 2011/11/20 Set order
// ZAP: 2011/12/21 Added 'show in history' popup
// ZAP: 2012/02/18 Rationalised session handling
// ZAP: 2012/03/03 Moved popups to stdmenus extension
// ZAP: 2012/03/15 Changed the method getResendDialog to pass the configuration key
// to the ManualRequestEditorDialog.
// ZAP: 2012/03/17 Issue 282 Added getAuthor()
// ZAP: 2012/04/24 Added type arguments to generic types, removed unnecessary
// cast and added @Override annotation to all appropriate methods.
// ZAP: 2012/04/28 Added log of exception.
// ZAP: 2012/05/31 Issue 308 NPE in sessionChangedEventHandler in daemon mode
// ZAP: 2012/07/02 Added the method showAlertAddDialog(HttpMessage, int).
// ZAP: 2012/07/29 Issue 43: added sessionScopeChanged event and removed access to some UI elements
// ZAP: 2012/08/01 Issue 332: added support for Modes
// ZAP: 2012/10/08 Issue 391: Performance improvements
// ZAP: 2013/03/03 Issue 546: Remove all template Javadoc comments
// ZAP: 2013/03/03 Issue 547: Deprecate unused classes and methods
// ZAP: 2013/04/14 Issue 588: ExtensionHistory.historyIdToRef should be cleared when changing
// session
// ZAP: 2013/04/14 Issue 598: Replace/update "old" pop up menu items
// ZAP: 2013/07/14 Issue 725: Clear alert's panel fields
// ZAP: 2013/07/23 Issue 738: Options to hide tabs
// ZAP: 2013/08/07 Also show Authentication messages
// ZAP: 2013/11/16 Issue 869: Differentiate proxied requests from (ZAP) user requests
// ZAP: 2013/12/02 Issue 915: Dynamically filter history based on selection in the sites window
// ZAP: 2014/03/23 Issue 503: Change the footer tabs to display the data
// with tables instead of lists
// ZAP: 2014/03/23 Issue 999: History loaded in wrong order
// ZAP: 2014/04/10 Remove cached history reference when a history reference is removed
// ZAP: 2014/04/10 Issue 1042: Having significant issues opening a previous session
// ZAP: 2014/05/20 Issue 1206: "History" tab is not cleared when a new session is created
// through the API with ZAP in GUI mode
// ZAP: 2014/12/12 Issue 1449: Added help button
// ZAP: 2015/02/09 Issue 1525: Introduce a database interface layer to allow for alternative
// implementations
// ZAP: 2015/03/03 Added delete(href) method to ensure local map updated
// ZAP: 2015/04/02 Issue 321: Support multiple databases and Issue 1582: Low memory option
// ZAP: 2015/07/16 Issue 1617: ZAP 2.4.0 throws HeadlessExceptions when running in daemon mode on
// headless machine
// ZAP: 2015/09/16 Issue 1890: ZAP can't completely scan OWASP Benchmark
// ZAP: 2016/01/26 Fixed findbugs warning
// ZAP: 2016/04/12 Listen to alert events to update the table model entries
// ZAP: 2016/04/14 Use View to display the HTTP messages
// ZAP: 2016/04/05 Issue 2458: Fix xlint warning messages
// ZAP: 2016/05/20 Moved purge method to here from PopupMenuPurgeSites
// ZAP: 2016/05/30 Issue 2494: ZAP Proxy is not showing the HTTP CONNECT Request in history tab
// ZAP: 2016/06/20 Removed unnecessary/unused constructor
// ZAP: 2016/06/21 Prevent deadlock between EDT and threads adding messages to the History tab
// ZAP: 2017/01/30 Use HistoryTableModel.
// ZAP: 2017/03/02 Issue 1634 Improve URL export.
// ZAP: 2017/03/28 Issue 3253 Allow URLs to be exported per context.
// ZAP: 2017/04/07 Added getUIName()
// ZAP: 2017/05/01 Issue 3446 - Add ability to export a Site Map via Context Menu.
// ZAP: 2017/05/02 Move alert related code to ExtensionAlert.
// ZAP: 2017/05/03 Register and process events from HistoryReference.
// ZPA: 2017/06/05 Sync HistoryReference cache.
// ZAP: 2017/06/13 Handle notification of notes set and deprecate/remove code no longer needed.
// ZAP: 2017/10/20 Move methods to delete history entries (Issue 3626).
// ZAP: 2017/11/06 Added (un)registerProxy (Issue 3983)
// ZAP: 2017/11/16 Update the table on sessionChanged (Issue 3207).
// ZAP: 2017/11/22 Delete just the history references selected (Issue 4065).
// ZAP: 2018/01/29 Add getter to expose historyReferencesTable of History tab (Issue 4000).
// ZAP: 2018/02/14 Remove unnecessary boxing / unboxing
// ZAP: 2018/03/12 Use the same help page in request editors.
// ZAP: 2019/06/01 Normalise line endings.
// ZAP: 2019/06/05 Normalise format/style.
// ZAP: 2019/09/30 Use hasView().
// ZAP: 2020/01/02 Do not display messages being deleted.
// ZAP: 2020/11/26 Use Log4j 2 classes for logging.
// ZAP: 2022/02/09 Deprecate methods related to core proxy.
// ZAP: 2022/02/28 Remove code deprecated in 2.6.0
// ZAP: 2022/05/12 Remove URL, messages, and response export menus and functionality, migrated to
// the exim add-on.
// ZAP: 2022/06/12 Deprecate getResendDialog().
// ZAP: 2022/06/27 Make delete more consistent and protective (Issue 7336).
// ZAP: 2022/09/14 Address deprecation warnings.
package org.parosproxy.paros.extension.history;

import java.awt.EventQueue;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.httpclient.URIException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.ExtensionHookView;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.HistoryReferenceEventPublisher;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.eventBus.EventConsumer;
import org.zaproxy.zap.extension.alert.AlertEventPublisher;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.extension.history.HistoryFilterPlusDialog;
import org.zaproxy.zap.extension.history.ManageTagsDialog;
import org.zaproxy.zap.extension.history.NotesAddDialog;
import org.zaproxy.zap.extension.history.PopupMenuNote;
import org.zaproxy.zap.extension.history.PopupMenuPurgeHistory;
import org.zaproxy.zap.extension.history.PopupMenuTag;
import org.zaproxy.zap.view.table.HistoryReferencesTable;

public class ExtensionHistory extends ExtensionAdaptor implements SessionChangedListener {

    public static final String NAME = "ExtensionHistory";

    private static final HistoryTableModel EMPTY_MODEL = new HistoryTableModel();
    private static final String REMOVE_CONFIRMATION_KEY = "view.deleteconfirmation.history";

    private LogPanel logPanel = null; //  @jve:decl-index=0:visual-constraint="161,134"
    private ProxyListenerLog proxyListener = null;
    private HistoryTableModel historyTableModel;

    // ZAP: added filter plus dialog
    private HistoryFilterPlusDialog filterPlusDialog = null;

    private PopupMenuPurgeHistory popupMenuPurgeHistory = null;

    private PopupMenuTag popupMenuTag = null;

    // ZAP: Added history notes
    private PopupMenuNote popupMenuNote = null;
    private NotesAddDialog dialogNotesAdd = null;
    private ManageTagsDialog manageTags = null;

    private boolean showJustInScope = false;
    private boolean linkWithSitesTree;
    private String linkWithSitesTreeBaseUri;

    // Used to cache hrefs not added into the historyList
    @SuppressWarnings("unchecked")
    private Map<Integer, HistoryReference> historyIdToRef =
            Collections.synchronizedMap(new ReferenceMap());

    /**
     * Flag that indicates whether or not the session is changing. To prevent updating the table
     * more than once when opening a session.
     *
     * @see #sessionAboutToChange(Session)
     * @see #sessionScopeChanged(Session)
     * @see #sessionChanged(Session)
     */
    private boolean sessionChanging;

    private Logger logger = LogManager.getLogger(ExtensionHistory.class);

    public ExtensionHistory() {
        super(NAME);
        this.setOrder(16);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("history.name");
    }

    /**
     * This method initializes logPanel
     *
     * @return org.parosproxy.paros.extension.history.LogPanel
     */
    private LogPanel getLogPanel() {
        if (logPanel == null) {
            logPanel = new LogPanel(getView());
            logPanel.setName(Constant.messages.getString("history.panel.title")); // ZAP: i18n
            // ZAP: Added History (calendar) icon
            logPanel.setIcon(
                    new ImageIcon(
                            ExtensionHistory.class.getResource(
                                    "/resource/icon/16/025.png"))); // 'calendar' icon
            // Dont allow this tab to be hidden
            logPanel.setHideable(false);

            logPanel.setExtension(this);
            logPanel.setModel(historyTableModel);
        }
        return logPanel;
    }

    public HistoryReference getSelectedHistoryReference() {
        return getLogPanel().getSelectedHistoryReference();
    }

    public List<HistoryReference> getSelectedHistoryReferences() {
        return getLogPanel().getSelectedHistoryReferences();
    }

    @Override
    public void init() {
        super.init();

        historyTableModel = new HistoryTableModel();
        EventConsumerImpl eventConsumerImpl = new EventConsumerImpl();
        ZAP.getEventBus()
                .registerConsumer(
                        eventConsumerImpl, AlertEventPublisher.getPublisher().getPublisherName());
        ZAP.getEventBus()
                .registerConsumer(
                        eventConsumerImpl,
                        HistoryReferenceEventPublisher.getPublisher().getPublisherName());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        extensionHook.addSessionListener(this);
        extensionHook.addProxyListener(getProxyListenerLog());
        extensionHook.addConnectionRequestProxyListener(getProxyListenerLog());

        if (hasView()) {
            ExtensionHookView pv = extensionHook.getHookView();
            pv.addStatusPanel(getLogPanel());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuTag());
            // ZAP: Added history notes
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuNote());

            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuPurgeHistory());

            ExtensionHelp.enableHelpKey(this.getLogPanel(), "ui.tabs.history");
        }
    }

    @Override
    public void sessionChanged(final Session session) {
        sessionChanging = false;
        sessionChanged();
    }

    private ProxyListenerLog getProxyListenerLog() {
        if (proxyListener == null) {
            proxyListener = new ProxyListenerLog(getModel(), getView(), this);
        }
        return proxyListener;
    }

    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated
    public void registerProxy(org.parosproxy.paros.core.proxy.ProxyServer ps) {
        ps.addProxyListener(this.getProxyListenerLog());
    }

    /** @deprecated (2.12.0) No longer used/needed. It will be removed in a future release. */
    @Deprecated
    public void unregisterProxy(org.parosproxy.paros.core.proxy.ProxyServer ps) {
        ps.removeProxyListener(this.getProxyListenerLog());
    }

    public void removeFromHistoryList(final HistoryReference href) {
        if (!hasView() || EventQueue.isDispatchThread()) {
            if (hasView()) {
                logPanel.setDisplaySelectedMessage(false);
            }
            this.historyTableModel.removeEntry(href.getHistoryId());
            if (hasView()) {
                logPanel.setDisplaySelectedMessage(true);
            }
            historyIdToRef.remove(href.getHistoryId());
        } else {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            removeFromHistoryList(href);
                        }
                    });
        }
    }

    public void notifyHistoryItemChanged(HistoryReference href) {
        notifyHistoryItemChanged(href.getHistoryId());
    }

    private void notifyHistoryItemChanged(final int historyId) {
        if (!hasView() || EventQueue.isDispatchThread()) {
            this.historyTableModel.refreshEntryRow(historyId);
        } else {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            notifyHistoryItemChanged(historyId);
                        }
                    });
        }
    }

    private void notifyHistoryItemsChanged() {
        if (!hasView() || EventQueue.isDispatchThread()) {
            this.historyTableModel.refreshEntryRows();
        } else {
            EventQueue.invokeLater(
                    new Runnable() {

                        @Override
                        public void run() {
                            notifyHistoryItemsChanged();
                        }
                    });
        }
    }

    public void delete(HistoryReference href) {
        if (href != null) {
            this.historyIdToRef.remove(href.getHistoryId());
            href.delete();
        }
    }

    public HistoryReference getHistoryReference(int historyId) {
        HistoryReference href = historyTableModel.getHistoryReference(historyId);
        if (href != null) {
            return href;
        }
        href = historyIdToRef.get(historyId);
        if (href == null) {
            try {
                href = new HistoryReference(historyId);
                if (href.getHistoryType() != HistoryReference.TYPE_SCANNER_TEMPORARY) {
                    addToMap(href);
                }
            } catch (Exception e) {
                return null;
            }
        }
        return href;
    }

    public int getLastHistoryId() {
        return Model.getSingleton().getDb().getTableHistory().lastIndex();
    }

    public void addHistory(HttpMessage msg, int type) {
        try {
            this.addHistory(new HistoryReference(Model.getSingleton().getSession(), type, msg));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void addToMap(HistoryReference historyRef) {
        historyIdToRef.put(historyRef.getHistoryId(), historyRef);
    }

    public void addHistory(HistoryReference historyRef) {
        if (Constant.isLowMemoryOptionSet()) {
            return;
        }
        try {
            synchronized (historyTableModel) {
                if (isHistoryTypeToShow(historyRef.getHistoryType())) {
                    final String uri = historyRef.getURI().toString();
                    if (this.showJustInScope && !getModel().getSession().isInScope(uri)) {
                        // Not in scope
                        addToMap(historyRef);
                        return;
                    } else if (linkWithSitesTree
                            && linkWithSitesTreeBaseUri != null
                            && !uri.startsWith(linkWithSitesTreeBaseUri)) {
                        // Not under the selected node
                        addToMap(historyRef);
                        return;
                    }
                    if (hasView()) {
                        // Dont do this in daemon mode
                        HistoryFilterPlusDialog dialog = getFilterPlusDialog();
                        HistoryFilter historyFilter = dialog.getFilter();
                        if (historyFilter != null && !historyFilter.matches(historyRef)) {
                            // Not in filter
                            addToMap(historyRef);
                            return;
                        }

                        addHistoryInEventQueue(historyRef);
                    }
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Tells whether or not the messages with the given history type should be shown in the History
     * tab.
     *
     * @param historyType the history type that will be checked
     * @return {@code true} if it should be shown, {@code false} otherwise
     */
    private static boolean isHistoryTypeToShow(int historyType) {
        return historyType == HistoryReference.TYPE_PROXIED
                || historyType == HistoryReference.TYPE_ZAP_USER
                || historyType == HistoryReference.TYPE_AUTHENTICATION
                || historyType == HistoryReference.TYPE_PROXY_CONNECT;
    }

    private void addHistoryInEventQueue(final HistoryReference ref) {
        if (!hasView() || EventQueue.isDispatchThread()) {
            historyTableModel.addHistoryReference(ref);
        } else {
            EventQueue.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            addHistoryInEventQueue(ref);
                        }
                    });
        }
    }

    private void searchHistory(HistoryFilter historyFilter) {
        Session session = getModel().getSession();

        synchronized (historyTableModel) {
            try {
                // ZAP: Added type argument.
                List<Integer> list =
                        getModel()
                                .getDb()
                                .getTableHistory()
                                .getHistoryIdsOfHistType(
                                        session.getSessionId(),
                                        HistoryReference.TYPE_PROXIED,
                                        HistoryReference.TYPE_ZAP_USER,
                                        HistoryReference.TYPE_PROXY_CONNECT);

                buildHistory(list, historyFilter);
            } catch (DatabaseException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void buildHistory(List<Integer> dbList, HistoryFilter historyFilter) {
        HistoryReference historyRef = null;
        synchronized (historyTableModel) {
            if (hasView()) {
                getLogPanel().setModel(EMPTY_MODEL);
            }
            historyTableModel.clear();

            for (int i = 0; i < dbList.size(); i++) {
                int historyId = dbList.get(i);

                try {
                    SiteNode sn = getModel().getSession().getSiteTree().getSiteNode(historyId);
                    if (sn != null
                            && sn.getHistoryReference() != null
                            && sn.getHistoryReference().getHistoryId() == historyId) {
                        historyRef = sn.getHistoryReference();
                    } else {
                        historyRef = getHistoryReference(historyId);
                        if (sn != null) {
                            sn.setHistoryReference(historyRef);
                        }
                    }
                    final String uri = historyRef.getURI().toString();
                    if (this.showJustInScope && !getModel().getSession().isInScope(uri)) {
                        // Not in scope
                        continue;
                    } else if (linkWithSitesTree
                            && linkWithSitesTreeBaseUri != null
                            && !uri.startsWith(linkWithSitesTreeBaseUri)) {
                        // Not under the selected node
                        continue;
                    }
                    if (historyFilter != null && !historyFilter.matches(historyRef)) {
                        // Not in filter
                        continue;
                    }
                    historyRef.loadAlerts();
                    historyTableModel.addHistoryReference(historyRef);

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (hasView()) {
                getLogPanel().setModel(historyTableModel);
            }
        }
    }

    private HistoryFilterPlusDialog getFilterPlusDialog() {
        if (filterPlusDialog == null) {
            filterPlusDialog = new HistoryFilterPlusDialog(getView().getMainFrame(), true);
        }
        return filterPlusDialog;
    }

    protected int showFilterPlusDialog() {
        HistoryFilterPlusDialog dialog = getFilterPlusDialog();
        dialog.setModal(true);
        try {
            dialog.setAllTags(getModel().getDb().getTableTag().getAllTags());
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
        }

        int exit = dialog.showDialog();
        int result = 0; // cancel, state unchanged
        HistoryFilter historyFilter = dialog.getFilter();
        if (exit == JOptionPane.OK_OPTION) {
            searchHistory(historyFilter);
            logPanel.setFilterStatus(historyFilter);
            result = 1; // applied

        } else if (exit == JOptionPane.NO_OPTION) {
            searchHistory(historyFilter);
            logPanel.setFilterStatus(historyFilter);
            result = -1; // reset
        }

        return result;
    }

    private PopupMenuPurgeHistory getPopupMenuPurgeHistory() {
        if (popupMenuPurgeHistory == null) {
            popupMenuPurgeHistory = new PopupMenuPurgeHistory(this);
        }
        return popupMenuPurgeHistory;
    }
    /**
     * This method initializes resendDialog
     *
     * @return org.parosproxy.paros.extension.history.ResendDialog
     * @deprecated (2.12.0) Replaced by Requester add-on.
     */
    @Deprecated
    public org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog
            getResendDialog() {
        org.parosproxy.paros.extension.manualrequest.ManualRequestEditorDialog resendDialog =
                new org.parosproxy.paros.extension.manualrequest.http.impl
                        .ManualHttpRequestEditorDialog(true, "resend", "ui.dialogs.manreq");
        resendDialog.setTitle(Constant.messages.getString("manReq.dialog.title")); // ZAP: i18n
        return resendDialog;
    }

    private PopupMenuTag getPopupMenuTag() {
        if (popupMenuTag == null) {
            popupMenuTag = new PopupMenuTag(this);
        }
        return popupMenuTag;
    }

    private PopupMenuNote getPopupMenuNote() {
        if (popupMenuNote == null) {
            popupMenuNote = new PopupMenuNote(this);
        }
        return popupMenuNote;
    }

    private void populateNotesAddDialogAndSetVisible(HistoryReference ref, String note) {
        dialogNotesAdd.setNote(note);
        dialogNotesAdd.setHistoryRef(ref);
        dialogNotesAdd.setVisible(true);
    }

    public void showNotesAddDialog(HistoryReference ref, String note) {
        if (dialogNotesAdd == null) {
            dialogNotesAdd = new NotesAddDialog(getView().getMainFrame(), false);
            populateNotesAddDialogAndSetVisible(ref, note);
        } else if (!dialogNotesAdd.isVisible()) {
            populateNotesAddDialogAndSetVisible(ref, note);
        }
    }

    /** @deprecated (2.7.0) No longer used/needed. */
    @Deprecated
    public void hideNotesAddDialog() {}

    /**
     * @deprecated (2.7.0) Use {@link ExtensionAlert#showAlertAddDialog(HistoryReference)} instead.
     * @param ref the {@code HistoryReference} that will have the new alert, if created.
     */
    @Deprecated
    public void showAlertAddDialog(HistoryReference ref) {
        ExtensionAlert extAlert =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
        if (extAlert == null) {
            return;
        }
        extAlert.showAlertAddDialog(ref);
    }

    /**
     * Sets the {@code HttpMessage} and the history type of the {@code HistoryReference} that will
     * be created if the user creates the alert. The current session will be used to create the
     * {@code HistoryReference}. The alert created will be added to the newly created {@code
     * HistoryReference}.
     *
     * <p>Should be used when the alert is added to a temporary {@code HistoryReference} as the
     * temporary {@code HistoryReference}s are deleted when the session is closed.
     *
     * @deprecated (2.7.0) Use {@link ExtensionAlert#showAlertAddDialog(HttpMessage, int)} instead.
     * @param httpMessage the {@code HttpMessage} that will be used to create the {@code
     *     HistoryReference}, must not be {@code null}
     * @param historyType the type of the history reference that will be used to create the {@code
     *     HistoryReference}
     * @see Model#getSession()
     * @see HistoryReference#HistoryReference(org.parosproxy.paros.model.Session, int, HttpMessage)
     */
    @Deprecated
    public void showAlertAddDialog(HttpMessage httpMessage, int historyType) {
        ExtensionAlert extAlert =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
        if (extAlert == null) {
            return;
        }
        extAlert.showAlertAddDialog(httpMessage, historyType);
    }

    /**
     * @deprecated (2.7.0) Use {@link ExtensionAlert#showAlertEditDialog(Alert)} instead.
     * @param alert the alert to edit
     */
    @Deprecated
    public void showAlertAddDialog(Alert alert) {
        ExtensionAlert extAlert =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
        if (extAlert == null) {
            return;
        }
        extAlert.showAlertEditDialog(alert);
    }

    private void populateManageTagsDialogAndSetVisible(HistoryReference ref, List<String> tags) {
        try {
            manageTags.setAllTags(getModel().getDb().getTableTag().getAllTags());
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
        }
        manageTags.setTags(tags);
        manageTags.setHistoryRef(ref);
        manageTags.setVisible(true);
    }

    public void showManageTagsDialog(HistoryReference ref, List<String> tags) {
        if (manageTags == null) {
            manageTags = new ManageTagsDialog(getView().getMainFrame(), false);
            populateManageTagsDialogAndSetVisible(ref, tags);
        } else if (!manageTags.isVisible()) {
            populateManageTagsDialogAndSetVisible(ref, tags);
        }
    }

    public void showInHistory(HistoryReference href) {
        this.getLogPanel().display(href);
        this.getLogPanel().setTabFocus();
    }

    @Override
    public void sessionAboutToChange(final Session session) {
        sessionChanging = true;

        if (!hasView() || EventQueue.isDispatchThread()) {
            historyTableModel.clear();
            historyIdToRef.clear();

            if (hasView()) {
                getView().displayMessage(null);
            }
        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                sessionAboutToChange(session);
                            }
                        });
            } catch (Exception e) {
                // ZAP: Added logging.
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public String getAuthor() {
        return Constant.PAROS_TEAM;
    }

    public boolean isShowJustInScope() {
        return showJustInScope;
    }

    public void setShowJustInScope(boolean showJustInScope) {
        this.showJustInScope = showJustInScope;
        if (showJustInScope) {
            linkWithSitesTree = false;
        }
        // Refresh with the next option
        searchHistory(getFilterPlusDialog().getFilter());
    }

    public void purge(SiteMap map, SiteNode node) {
        SiteNode child = null;
        synchronized (map) {
            while (node.getChildCount() > 0) {
                try {
                    child = (SiteNode) node.getChildAt(0);
                    purge(map, child);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (node.isRoot()) {
                return;
            }

            // delete reference in node
            removeFromHistoryList(node.getHistoryReference());

            ExtensionAlert extAlert =
                    Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);

            if (node.getHistoryReference() != null) {
                deleteAlertsFromExtensionAlert(extAlert, node.getHistoryReference());
                node.getHistoryReference().delete();
                map.removeHistoryReference(node.getHistoryReference().getHistoryId());
            }

            // delete past reference in node
            while (node.getPastHistoryReference().size() > 0) {
                HistoryReference ref = node.getPastHistoryReference().get(0);
                deleteAlertsFromExtensionAlert(extAlert, ref);
                removeFromHistoryList(ref);
                delete(ref);
                node.getPastHistoryReference().remove(0);
                map.removeHistoryReference(ref.getHistoryId());
            }

            map.removeNodeFromParent(node);
        }
    }

    private static void deleteAlertsFromExtensionAlert(
            ExtensionAlert extAlert, HistoryReference historyReference) {
        if (extAlert == null) {
            return;
        }

        extAlert.deleteHistoryReferenceAlerts(historyReference);
    }

    void setLinkWithSitesTree(boolean linkWithSitesTree, String baseUri) {
        this.linkWithSitesTree = linkWithSitesTree;
        this.linkWithSitesTreeBaseUri = baseUri;
        if (linkWithSitesTree) {
            this.showJustInScope = false;
        }
        searchHistory(getFilterPlusDialog().getFilter());
    }

    void updateLinkWithSitesTreeBaseUri(String baseUri) {
        this.linkWithSitesTreeBaseUri = baseUri;
        searchHistory(getFilterPlusDialog().getFilter());
    }

    @Override
    public void sessionScopeChanged(Session session) {
        if (sessionChanging) {
            return;
        }
        sessionChanged();
    }

    private void sessionChanged() {
        if (hasView()) {
            searchHistory(getFilterPlusDialog().getFilter());
        } else {
            searchHistory(null);
        }
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        // Ignore
    }

    @Override
    public boolean supportsLowMemory() {
        return true;
    }

    /** Part of the core set of features that should be supported by all db types */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    /** @since 2.8.0 */
    public HistoryReferencesTable getHistoryReferencesTable() {
        return logPanel.getHistoryReferenceTable();
    }

    private class EventConsumerImpl implements EventConsumer {

        @Override
        public void eventReceived(Event event) {
            switch (event.getEventType()) {
                case HistoryReferenceEventPublisher.EVENT_NOTE_SET:
                case HistoryReferenceEventPublisher.EVENT_TAG_ADDED:
                case HistoryReferenceEventPublisher.EVENT_TAG_REMOVED:
                case HistoryReferenceEventPublisher.EVENT_TAGS_SET:
                    notifyHistoryItemChanged(
                            Integer.valueOf(
                                    event.getParameters()
                                            .get(
                                                    HistoryReferenceEventPublisher
                                                            .FIELD_HISTORY_REFERENCE_ID)));
                    break;
                case AlertEventPublisher.ALERT_ADDED_EVENT:
                case AlertEventPublisher.ALERT_CHANGED_EVENT:
                case AlertEventPublisher.ALERT_REMOVED_EVENT:
                    notifyHistoryItemChanged(
                            Integer.valueOf(
                                    event.getParameters()
                                            .get(AlertEventPublisher.HISTORY_REFERENCE_ID)));
                    break;
                case AlertEventPublisher.ALL_ALERTS_REMOVED_EVENT:
                    notifyHistoryItemsChanged();
                    break;
                default:
            }
        }
    }

    /**
     * Deletes the given history references from the {@link LogPanel History tab} and the session
     * (database), along with the corresponding {@link SiteNode}s and {@link Alert}s.
     *
     * @param hrefs the history entries to delete.
     * @see View#getDefaultDeleteKeyStroke()
     * @since 2.7.0
     */
    public void purgeHistory(List<HistoryReference> hrefs) {
        if (hrefs.isEmpty()) {
            return;
        }
        if (hasView()) {
            FileConfiguration config = Model.getSingleton().getOptionsParam().getConfig();
            boolean confirmRemoval = config.getBoolean(REMOVE_CONFIRMATION_KEY, false);

            if (confirmRemoval) {
                JCheckBox removeWithoutConfirmationCheckBox =
                        new JCheckBox(Constant.messages.getString("history.purge.confirm.message"));
                Object[] messages = {
                    Constant.messages.getString("history.purge.warning"),
                    " ",
                    removeWithoutConfirmationCheckBox
                };
                int result =
                        JOptionPane.showOptionDialog(
                                View.getSingleton().getMainFrame(),
                                messages,
                                Constant.messages.getString("history.purge.title"),
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                new String[] {
                                    Constant.messages.getString("history.purge.confirm"),
                                    Constant.messages.getString("history.purge.cancel")
                                },
                                null);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
                Model.getSingleton()
                        .getOptionsParam()
                        .getConfig()
                        .setProperty(
                                REMOVE_CONFIRMATION_KEY,
                                removeWithoutConfirmationCheckBox.isSelected());
            }
        }
        synchronized (this) {
            for (HistoryReference href : hrefs) {
                purgeHistory(href);
            }
        }
    }

    private void purgeHistory(HistoryReference href) {
        if (href == null) {
            return;
        }

        removeFromHistoryList(href);

        ExtensionAlert extAlert =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);

        if (extAlert != null) {
            extAlert.deleteHistoryReferenceAlerts(href);
        }

        SiteNode node = href.getSiteNode();
        if (node != null) {
            SiteMap map = Model.getSingleton().getSession().getSiteTree();
            if (node.getHistoryReference() != href) {
                node.getPastHistoryReference().remove(href);
            } else if (!node.getPastHistoryReference().isEmpty()) {
                node.setHistoryReference(node.getPastHistoryReference().remove(0));
                node.getPastHistoryReference().remove(href);
            } else {
                if (node.isLeaf()) {
                    SiteNode parent = node.getParent();
                    map.removeNodeFromParent(node);
                    purgeTemporaryParents(map, parent);
                } else {
                    try {
                        node.setHistoryReference(
                                map.createReference(node, href, href.getHttpMessage()));
                    } catch (URIException
                            | HttpMalformedHeaderException
                            | NullPointerException
                            | DatabaseException e) {
                        logger.error("Failed to create temporary node:", e);
                    }
                }
            }
            map.removeHistoryReference(href.getHistoryId());
        }

        delete(href);
    }

    private void purgeTemporaryParents(SiteMap map, SiteNode node) {
        if (node == null
                || node.isRoot()
                || !node.isLeaf()
                || node.getHistoryReference().getHistoryType() != HistoryReference.TYPE_TEMPORARY) {
            return;
        }

        SiteNode parent = node.getParent();
        purge(map, node);
        purgeTemporaryParents(map, parent);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (hasView()) {
            this.getHistoryReferencesTable().persistColumnConfiguration();
        }
    }
}
