/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordScan;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.OptionsChangedListener;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.MainFooterPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.ZAP;
import org.zaproxy.zap.db.TableAlertTag;
import org.zaproxy.zap.eventBus.Event;
import org.zaproxy.zap.extension.XmlReporterExtension;
import org.zaproxy.zap.extension.help.ExtensionHelp;
import org.zaproxy.zap.model.SessionStructure;
import org.zaproxy.zap.model.Target;

public class ExtensionAlert extends ExtensionAdaptor
        implements SessionChangedListener, XmlReporterExtension, OptionsChangedListener {

    public static final String NAME = "ExtensionAlert";
    private static final Logger logger = LogManager.getLogger(ExtensionAlert.class);
    private Map<Integer, HistoryReference> hrefs = new HashMap<>();
    private AlertTreeModel treeModel = null;
    private AlertTreeModel filteredTreeModel = null;
    private AlertPanel alertPanel = null;
    private RecordScan recordScan = null;
    private PopupMenuAlert popupMenuAlertAdd;
    private PopupMenuAlertEdit popupMenuAlertEdit = null;
    private PopupMenuAlertSetFalsePositive popupMenuAlertSetFalsePositive = null;
    private PopupMenuAlertDelete popupMenuAlertDelete = null;
    private PopupMenuAlertsRefresh popupMenuAlertsRefresh = null;
    private PopupMenuShowAlerts popupMenuShowAlerts = null;
    private AlertParam alertParam = null;
    private OptionsAlertPanel optionsPanel = null;
    private Properties alertOverrides = new Properties();
    private AlertAddDialog dialogAlertAdd;

    public ExtensionAlert() {
        super(NAME);
        this.setOrder(27);
    }

    @Override
    public String getUIName() {
        return Constant.messages.getString("alerts.name");
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        extensionHook.addOptionsParamSet(getAlertParam());
        if (getView() != null) {
            extensionHook.getHookView().addOptionPanel(getOptionsPanel());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertAdd());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertEdit());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertSetFalsePositive());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertDelete());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertsRefresh());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowAlerts());

            extensionHook.getHookView().addStatusPanel(getAlertPanel());

            ExtensionHelp.enableHelpKey(getAlertPanel(), "ui.tabs.alerts");
        }
        extensionHook.addSessionListener(this);
        extensionHook.addOptionsChangedListener(this);
        extensionHook.addApiImplementor(new AlertAPI(this));
    }

    @Override
    public void optionsLoaded() {
        reloadOverridesFile();
    }

    @Override
    public void optionsChanged(OptionsParam optionsParam) {
        reloadOverridesFile();
    }

    public boolean reloadOverridesFile() {
        this.alertOverrides.clear();

        String filename = this.getAlertParam().getOverridesFilename();
        if (filename != null && filename.length() > 0) {
            File file = new File(filename);
            if (!file.isFile() || !file.canRead()) {
                logger.error("Cannot read alert overrides file {}", file.getAbsolutePath());
                return false;
            }

            try (BufferedReader br =
                    Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
                this.alertOverrides.load(br);
                logger.info(
                        "Read {} overrides from {}",
                        this.alertOverrides.size(),
                        file.getAbsolutePath());
                return true;
            } catch (IOException e) {
                logger.error("Failed to read alert overrides file {}", file.getAbsolutePath(), e);
                return false;
            }
        }
        return true;
    }

    private OptionsAlertPanel getOptionsPanel() {
        if (optionsPanel == null) {
            optionsPanel = new OptionsAlertPanel();
        }
        return optionsPanel;
    }

    private AlertParam getAlertParam() {
        if (alertParam == null) {
            alertParam = new AlertParam();
        }
        return alertParam;
    }

    public void alertFound(Alert alert, HistoryReference ref) {
        if (isInvalid(alert)) {
            return;
        }

        try {
            logger.debug("alertFound {} {}", alert.getName(), alert.getUri());
            if (ref == null) {
                ref = alert.getHistoryRef();
            }
            if (ref == null) {
                ref =
                        new HistoryReference(
                                getModel().getSession(),
                                HistoryReference.TYPE_SCANNER,
                                alert.getMessage());
                alert.setHistoryRef(ref);
            }

            if (alert.getSource() == Alert.Source.UNKNOWN) {
                alert.setSource(Alert.Source.TOOL);
            }

            alert.setSourceHistoryId(ref.getHistoryId());

            hrefs.put(ref.getHistoryId(), ref);

            this.applyOverrides(alert);

            writeAlertToDB(alert, ref);

            try {
                if (getView() == null || EventQueue.isDispatchThread()) {
                    SessionStructure.addPath(Model.getSingleton(), ref, alert.getMessage());
                } else {
                    final HistoryReference fRef = ref;
                    final HttpMessage fMsg = alert.getMessage();
                    EventQueue.invokeAndWait(
                            new Runnable() {

                                @Override
                                public void run() {
                                    SessionStructure.addPath(Model.getSingleton(), fRef, fMsg);
                                }
                            });
                }

                ref.addAlert(alert);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            addAlertToTree(alert);

            // Clear the message so that it can be GC'ed
            alert.setMessage(null);

            publishAlertEvent(alert, AlertEventPublisher.ALERT_ADDED_EVENT);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static boolean isInvalid(Alert alert) {
        if (alert.getUri().isEmpty() || alert.getMessage() == null) {
            logger.error(
                    "Attempting to raise an alert without URI or HTTP message, Plugin ID: {} Alert Name:{}\n\t{}",
                    alert.getPluginId(),
                    alert.getName(),
                    StringUtils.join(Thread.currentThread().getStackTrace(), "\n\t"));
            return true;
        }
        return false;
    }

    /*
     * This method is intended for internal use only, and should only
     * be called by other classes for unit testing.
     */
    protected void applyOverrides(Alert alert) {
        if (this.alertOverrides.isEmpty()) {
            // Nothing to do
            return;
        }
        String changedName = this.alertOverrides.getProperty(alert.getPluginId() + ".name");
        if (changedName != null) {
            alert.setName(applyOverride(alert.getName(), changedName));
        }
        String changedDesc = this.alertOverrides.getProperty(alert.getPluginId() + ".description");
        if (changedDesc != null) {
            alert.setDescription(applyOverride(alert.getDescription(), changedDesc));
        }
        String changedSolution = this.alertOverrides.getProperty(alert.getPluginId() + ".solution");
        if (changedSolution != null) {
            alert.setSolution(applyOverride(alert.getSolution(), changedSolution));
        }
        String changedOther = this.alertOverrides.getProperty(alert.getPluginId() + ".otherInfo");
        if (changedOther != null) {
            alert.setOtherInfo(applyOverride(alert.getOtherInfo(), changedOther));
        }
        String changedReference =
                this.alertOverrides.getProperty(alert.getPluginId() + ".reference");
        if (changedReference != null) {
            alert.setReference(applyOverride(alert.getReference(), changedReference));
        }
        Map<String, String> tags = new HashMap<>(alert.getTags());
        for (Map.Entry<Object, Object> e : this.alertOverrides.entrySet()) {
            String propertyKey = e.getKey().toString();
            if (propertyKey.startsWith(alert.getPluginId() + ".tag.")) {
                String tagKey = propertyKey.substring((alert.getPluginId() + ".tag.").length());
                tags.put(
                        tagKey,
                        applyOverride(
                                alert.getTags().getOrDefault(tagKey, ""), e.getValue().toString()));
            }
        }
        alert.setTags(tags);
    }

    /*
     * This method should only be used for testing
     */
    protected void setAlertOverrideProperty(String key, String value) {
        this.alertOverrides.put(key, value);
    }

    private String applyOverride(String original, String override) {
        if (override.startsWith("+")) {
            return original + override.substring(1);
        } else if (override.startsWith("-")) {
            return override.substring(1) + original;
        } else {
            return override;
        }
    }

    private void publishAlertEvent(Alert alert, String event) {
        HistoryReference historyReference = hrefs.get(alert.getSourceHistoryId());
        if (historyReference == null) {
            historyReference =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionHistory.class)
                            .getHistoryReference(alert.getSourceHistoryId());
        }

        Map<String, String> map = new HashMap<>();
        map.put(AlertEventPublisher.ALERT_ID, Integer.toString(alert.getAlertId()));
        map.put(
                AlertEventPublisher.HISTORY_REFERENCE_ID,
                Integer.toString(alert.getSourceHistoryId()));
        map.put(AlertEventPublisher.NAME, alert.getName());
        map.put(AlertEventPublisher.URI, alert.getUri().toString());
        map.put(AlertEventPublisher.PARAM, alert.getParam());
        map.put(AlertEventPublisher.RISK, Integer.toString(alert.getRisk()));
        map.put(AlertEventPublisher.RISK_STRING, Alert.MSG_RISK[alert.getRisk()]);
        map.put(AlertEventPublisher.CONFIDENCE, Integer.toString(alert.getConfidence()));
        map.put(AlertEventPublisher.CONFIDENCE_STRING, Alert.MSG_CONFIDENCE[alert.getConfidence()]);
        map.put(AlertEventPublisher.SOURCE, Integer.toString(alert.getSource().getId()));
        ZAP.getEventBus()
                .publishSyncEvent(
                        AlertEventPublisher.getPublisher(),
                        new Event(
                                AlertEventPublisher.getPublisher(),
                                event,
                                new Target(historyReference.getSiteNode()),
                                map));
    }

    private void addAlertToTree(final Alert alert) {

        if (Constant.isLowMemoryOptionSet()) {
            return;
        }

        if (!hasView() || EventQueue.isDispatchThread()) {
            addAlertToTreeEventHandler(alert);

        } else {

            try {
                // Changed from invokeAndWait due to the number of interrupt exceptions
                // And its likely to always to run from a background thread anyway
                // EventQueue.invokeAndWait(new Runnable() {
                EventQueue.invokeLater(
                        new Runnable() {

                            @Override
                            public void run() {
                                addAlertToTreeEventHandler(alert);
                            }
                        });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private boolean isInFilter(Alert alert) {
        // Just support scope for now
        return this.getModel().getSession().isInScope(alert.getHistoryRef());
    }

    private void addAlertToTreeEventHandler(Alert alert) {

        synchronized (this.getTreeModel()) {
            this.getTreeModel().addPath(alert);
            if (isInFilter(alert)) {
                this.getFilteredTreeModel().addPath(alert);
            }
            if (getView() != null) {
                getAlertPanel().expandRoot();
                this.recalcAlerts();
            }
        }
    }

    /**
     * This method initializes alertPanel
     *
     * @return org.parosproxy.paros.extension.scanner.AlertPanel
     */
    AlertPanel getAlertPanel() {
        if (alertPanel == null) {
            alertPanel = new AlertPanel(this);
            alertPanel.setSize(345, 122);
            setMainTreeModel();
        }

        return alertPanel;
    }

    AlertTreeModel getTreeModel() {
        if (treeModel == null) {
            treeModel = new AlertTreeModel();
        }
        return treeModel;
    }

    private AlertTreeModel getFilteredTreeModel() {
        if (filteredTreeModel == null) {
            filteredTreeModel = new AlertTreeModel();
        }
        return filteredTreeModel;
    }

    private void writeAlertToDB(Alert alert, HistoryReference ref)
            throws HttpMalformedHeaderException, DatabaseException {

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        int scanId = 0;
        if (recordScan != null) {
            scanId = recordScan.getScanId();
        }
        RecordAlert recordAlert =
                tableAlert.write(
                        scanId,
                        alert.getPluginId(),
                        alert.getName(),
                        alert.getRisk(),
                        alert.getConfidence(),
                        alert.getDescription(),
                        alert.getUri(),
                        alert.getParam(),
                        alert.getAttack(),
                        alert.getOtherInfo(),
                        alert.getSolution(),
                        alert.getReference(),
                        alert.getEvidence(),
                        alert.getCweId(),
                        alert.getWascId(),
                        ref.getHistoryId(),
                        alert.getSourceHistoryId(),
                        alert.getSource().getId(),
                        alert.getAlertRef(),
                        alert.getInputVector());

        int alertId = recordAlert.getAlertId();
        alert.setAlertId(alertId);

        TableAlertTag tableAlertTag = getModel().getDb().getTableAlertTag();
        for (Map.Entry<String, String> e : alert.getTags().entrySet()) {
            tableAlertTag.insertOrUpdate(alertId, e.getKey(), e.getValue());
        }
    }

    public void updateAlert(Alert alert) throws HttpMalformedHeaderException, DatabaseException {
        logger.debug("updateAlert {} {}", alert.getName(), alert.getUri());
        HistoryReference hRef = hrefs.get(alert.getSourceHistoryId());
        if (hRef != null) {
            updateAlertInDB(alert);
            hRef.updateAlert(alert);
            publishAlertEvent(alert, AlertEventPublisher.ALERT_CHANGED_EVENT);
            updateAlertInTree(alert, alert);
        }
    }

    private void updateAlertInDB(Alert alert)
            throws HttpMalformedHeaderException, DatabaseException {

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        tableAlert.update(
                alert.getAlertId(),
                alert.getName(),
                alert.getRisk(),
                alert.getConfidence(),
                alert.getDescription(),
                alert.getUri(),
                alert.getParam(),
                alert.getAttack(),
                alert.getOtherInfo(),
                alert.getSolution(),
                alert.getReference(),
                alert.getEvidence(),
                alert.getCweId(),
                alert.getWascId(),
                alert.getSourceHistoryId(),
                alert.getInputVector());

        int alertId = alert.getAlertId();
        TableAlertTag tableAlertTag = getModel().getDb().getTableAlertTag();
        Map<String, String> existingTags = tableAlertTag.getTagsByAlertId(alertId);
        Map<String, String> newTags = alert.getTags();
        for (Map.Entry<String, String> e : existingTags.entrySet()) {
            if (!newTags.containsKey(e.getKey())) {
                tableAlertTag.delete(alertId, e.getKey());
            }
        }
        for (Map.Entry<String, String> e : newTags.entrySet()) {
            tableAlertTag.insertOrUpdate(alertId, e.getKey(), e.getValue());
        }
    }

    public void displayAlert(Alert alert) {
        logger.debug("displayAlert {} {}", alert.getName(), alert.getUri());
        this.alertPanel.getAlertViewPanel().displayAlert(alert);
    }

    public void updateAlertInTree(Alert originalAlert, Alert alert) {
        if (Constant.isLowMemoryOptionSet()) {
            return;
        }

        if (getView() == null || EventQueue.isDispatchThread()) {
            updateAlertInTreeEventHandler(originalAlert, alert);
        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                updateAlertInTreeEventHandler(originalAlert, alert);
                            }
                        });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void updateAlertInTreeEventHandler(Alert originalAlert, Alert alert) {
        this.getTreeModel().updatePath(originalAlert, alert);
        if (isInFilter(alert)) {
            this.getFilteredTreeModel().updatePath(originalAlert, alert);
        }
        this.recalcAlerts();

        if (hasView()) {
            JTree alertTree = this.getAlertPanel().getTreeAlert();
            TreePath alertPath = new TreePath(getTreeModel().getAlertNode(alert).getPath());
            alertTree.setSelectionPath(alertPath);
            alertTree.scrollPathToVisible(alertPath);
        }
    }

    @Override
    public void sessionChanged(final Session session) {
        if (EventQueue.isDispatchThread()) {
            sessionChangedEventHandler(session);

        } else {
            try {
                EventQueue.invokeAndWait(
                        new Runnable() {
                            @Override
                            public void run() {
                                sessionChangedEventHandler(session);
                            }
                        });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void sessionChangedEventHandler(Session session) {
        setTreeModel(new AlertTreeModel());

        treeModel = null;
        filteredTreeModel = null;
        hrefs = new HashMap<>();

        if (session == null) {
            // Null session indicated we're shutting down
            return;
        }

        try {
            refreshAlert(session);
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
        }
        setTreeModel(getTreeModel());
    }

    private void refreshAlert(Session session) throws DatabaseException {
        if (Constant.isLowMemoryOptionSet()) {
            return;
        }
        SiteMap siteTree = this.getModel().getSession().getSiteTree();

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        TableAlertTag tableAlertTag = getModel().getDb().getTableAlertTag();
        // TODO this doesn't work, but should be used when its fixed :/
        // Vector<Integer> v =
        // tableAlert.getAlertListBySession(Model.getSingleton().getSession().getSessionId());
        Vector<Integer> v = tableAlert.getAlertList();

        final ExtensionHistory extensionHistory =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);

        for (int i = 0; i < v.size(); i++) {
            int alertId = v.get(i);
            RecordAlert recAlert = tableAlert.read(alertId);
            int historyId = recAlert.getHistoryId();
            HistoryReference historyReference = null;
            if (extensionHistory != null) {
                historyReference = extensionHistory.getHistoryReference(historyId);
            }

            if (historyReference == null) {
                historyReference = this.hrefs.get(historyId);
            }

            Alert alert;
            if (historyReference != null) {
                alert = new Alert(recAlert, historyReference);
            } else {
                alert = new Alert(recAlert);
            }
            alert.setTags(tableAlertTag.getTagsByAlertId(alertId));
            historyReference = alert.getHistoryRef();
            if (historyReference != null) {
                // The ref can be null if hrefs are purged
                addAlertToTree(alert);
                Integer key = historyId;
                if (!hrefs.containsKey(key)) {
                    this.hrefs.put(key, alert.getHistoryRef());
                }
            }
        }
        siteTree.nodeStructureChanged(siteTree.getRoot());
    }

    private PopupMenuAlert getPopupMenuAlertAdd() {
        if (popupMenuAlertAdd == null) {
            popupMenuAlertAdd =
                    new PopupMenuAlert(Constant.messages.getString("alert.add.popup"), this);
        }
        return popupMenuAlertAdd;
    }

    private PopupMenuAlertEdit getPopupMenuAlertEdit() {
        if (popupMenuAlertEdit == null) {
            popupMenuAlertEdit = new PopupMenuAlertEdit(this);
        }
        return popupMenuAlertEdit;
    }

    private PopupMenuAlertSetFalsePositive getPopupMenuAlertSetFalsePositive() {
        if (popupMenuAlertSetFalsePositive == null) {
            popupMenuAlertSetFalsePositive = new PopupMenuAlertSetFalsePositive();
        }
        return popupMenuAlertSetFalsePositive;
    }

    private PopupMenuAlertDelete getPopupMenuAlertDelete() {
        if (popupMenuAlertDelete == null) {
            popupMenuAlertDelete = new PopupMenuAlertDelete();
        }
        return popupMenuAlertDelete;
    }

    private PopupMenuAlertsRefresh getPopupMenuAlertsRefresh() {
        if (popupMenuAlertsRefresh == null) {
            popupMenuAlertsRefresh = new PopupMenuAlertsRefresh();
        }
        return popupMenuAlertsRefresh;
    }

    private PopupMenuShowAlerts getPopupMenuShowAlerts() {
        if (popupMenuShowAlerts == null) {
            popupMenuShowAlerts =
                    new PopupMenuShowAlerts(Constant.messages.getString("alerts.view.popup"), this);
        }
        return popupMenuShowAlerts;
    }

    public void deleteAlert(Alert alert) {
        logger.debug("deleteAlert {} {}", alert.getName(), alert.getUri());

        try {
            getModel().getDb().getTableAlert().deleteAlert(alert.getAlertId());
            getModel().getDb().getTableAlertTag().deleteAllTagsForAlert(alert.getAlertId());
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
        }

        deleteAlertFromDisplay(alert);
        publishAlertEvent(alert, AlertEventPublisher.ALERT_REMOVED_EVENT);
    }

    public void deleteAllAlerts() {
        try {
            getModel().getDb().getTableAlert().deleteAllAlerts();
            getModel().getDb().getTableAlertTag().deleteAllTags();
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
        }

        if (!Constant.isLowMemoryOptionSet()) {
            SiteMap siteTree = this.getModel().getSession().getSiteTree();
            siteTree.getRoot().deleteAllAlerts();
        }

        for (HistoryReference href : hrefs.values()) {
            href.deleteAllAlerts();
        }

        ZAP.getEventBus()
                .publishSyncEvent(
                        AlertEventPublisher.getPublisher(),
                        new Event(
                                AlertEventPublisher.getPublisher(),
                                AlertEventPublisher.ALL_ALERTS_REMOVED_EVENT,
                                null));

        hrefs = new HashMap<>();

        treeModel = null;
        filteredTreeModel = null;
        setTreeModel(getTreeModel());
    }

    private void deleteAlertFromDisplay(final Alert alert) {
        if (getView() == null || Constant.isLowMemoryOptionSet()) {
            // Running as a daemon
            return;
        }
        if (EventQueue.isDispatchThread()) {
            deleteAlertFromDisplayEventHandler(alert);

        } else {

            try {
                EventQueue.invokeAndWait(
                        new Runnable() {

                            @Override
                            public void run() {
                                deleteAlertFromDisplayEventHandler(alert);
                            }
                        });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void deleteAlertFromDisplayEventHandler(Alert alert) {
        synchronized (this.getTreeModel()) {
            this.getTreeModel().deletePath(alert);
            this.getFilteredTreeModel().deletePath(alert);
            List<HistoryReference> toDelete = new ArrayList<>();
            for (HistoryReference href : hrefs.values()) {
                if (href.hasAlert(alert)) {
                    href.deleteAlert(alert);
                    if (!href.hasAlerts()) {
                        toDelete.add(href);
                    }
                }
            }
            for (HistoryReference href : toDelete) {
                hrefs.remove(href.getHistoryId());
            }
        }

        this.recalcAlerts();
    }

    public void deleteHistoryReferenceAlerts(HistoryReference hRef) {
        List<Alert> alerts = hRef.getAlerts();
        SiteMap siteTree = this.getModel().getSession().getSiteTree();

        synchronized (this.getTreeModel()) {
            for (int i = 0; i < alerts.size(); i++) {
                Alert alert = alerts.get(i);

                this.getTreeModel().deletePath(alert);
                this.getFilteredTreeModel().deletePath(alert);

                try {
                    getModel().getDb().getTableAlert().deleteAlert(alert.getAlertId());
                    getModel().getDb().getTableAlertTag().deleteAllTagsForAlert(alert.getAlertId());
                } catch (DatabaseException e) {
                    logger.error("Failed to delete alert with ID: {}", alert.getAlertId(), e);
                }
            }

            SiteNode node = hRef.getSiteNode();
            if (node == null) {
                node = siteTree.findNode(hRef.getURI(), hRef.getMethod(), hRef.getRequestBody());
            }

            if (node != null) {
                node.deleteAlerts(alerts);
            }
            alerts.clear();
            this.recalcAlerts();
        }

        hrefs.remove(hRef.getHistoryId());
    }

    /**
     * Recalculates the total number of alerts by alert's risk contained in the "Alerts" tree and
     * updates the alert's risks footer status labels with the new values.
     *
     * <p>The method has no effect if the view is not initialised.
     *
     * @see AlertPanel#getTreeAlert()
     * @see MainFooterPanel#setAlertHigh(int)
     * @see MainFooterPanel#setAlertInfo(int)
     * @see MainFooterPanel#setAlertLow(int)
     * @see MainFooterPanel#setAlertMedium(int)
     * @see View#isInitialised()
     */
    void recalcAlerts() {
        if (!hasView()) {
            return;
        }
        // Must only be called when View is initialised
        int totalInfo = 0;
        int totalLow = 0;
        int totalMedium = 0;
        int totalHigh = 0;

        AlertNode parent = (AlertNode) getAlertPanel().getTreeAlert().getModel().getRoot();
        if (parent != null) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                AlertNode child = parent.getChildAt(i);
                switch (child.getRisk()) {
                    case Alert.RISK_INFO:
                        totalInfo++;
                        break;
                    case Alert.RISK_LOW:
                        totalLow++;
                        break;
                    case Alert.RISK_MEDIUM:
                        totalMedium++;
                        break;
                    case Alert.RISK_HIGH:
                        totalHigh++;
                        break;
                }
            }
        }
        MainFooterPanel footer = getView().getMainFrame().getMainFooterPanel();
        footer.setAlertInfo(totalInfo);
        footer.setAlertLow(totalLow);
        footer.setAlertMedium(totalMedium);
        footer.setAlertHigh(totalHigh);
    }

    public List<Alert> getAllAlerts() {
        List<Alert> allAlerts = new ArrayList<>();

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        TableAlertTag tableAlertTag = getModel().getDb().getTableAlertTag();
        Vector<Integer> v;
        try {
            // TODO this doesn't work, but should be used when its fixed :/
            // v =
            // tableAlert.getAlertListBySession(Model.getSingleton().getSession().getSessionId());
            v = tableAlert.getAlertList();

            for (int i = 0; i < v.size(); i++) {
                int alertId = v.get(i);
                RecordAlert recAlert = tableAlert.read(alertId);
                Alert alert = new Alert(recAlert);
                if (alert.getHistoryRef() != null) {
                    // Only use the alert if it has a history reference.
                    if (!allAlerts.contains(alert)) {
                        alert.setTags(tableAlertTag.getTagsByAlertId(alertId));
                        allAlerts.add(alert);
                    }
                }
            }
        } catch (DatabaseException e) {
            logger.error(e.getMessage(), e);
        }
        return allAlerts;
    }

    @Override
    public String getXml(SiteNode site) {
        StringBuilder xml = new StringBuilder();
        xml.append("<alerts>");
        List<Alert> alerts = site.getAlerts();
        SortedSet<String> handledAlerts = new TreeSet<>();

        for (int i = 0; i < alerts.size(); i++) {
            Alert alert = alerts.get(i);
            if (alert.getConfidence() != Alert.CONFIDENCE_FALSE_POSITIVE) {
                if (this.getAlertParam().isMergeRelatedIssues()) {
                    String fingerprint = alertFingerprint(alert);
                    if (handledAlerts.add(fingerprint)) {
                        // Its a new one
                        // Build up the full set of details
                        StringBuilder sb = new StringBuilder();
                        sb.append("  <instances>\n");
                        int count = 0;
                        for (int j = i; j < alerts.size(); j++) {
                            // Deliberately include i!
                            Alert alert2 = alerts.get(j);
                            if (fingerprint.equals(alertFingerprint(alert2))) {
                                if (this.getAlertParam().getMaximumInstances() == 0
                                        || count < this.getAlertParam().getMaximumInstances()) {
                                    sb.append("  <instance>\n");
                                    sb.append(alert2.getUrlParamXML());
                                    sb.append("  </instance>\n");
                                }
                                count++;
                            }
                        }
                        sb.append("  </instances>\n");
                        sb.append("  <count>");
                        sb.append(count);
                        sb.append("</count>\n");
                        xml.append(alert.toPluginXML(sb.toString()));
                    }
                } else {
                    String urlParamXML = alert.getUrlParamXML();
                    xml.append(alert.toPluginXML(urlParamXML));
                }
            }
        }
        xml.append("</alerts>");
        return xml.toString();
    }

    private String alertFingerprint(Alert alert) {
        return alert.getPluginId()
                + "/"
                + alert.getName()
                + "/"
                + alert.getRisk()
                + "/"
                + alert.getConfidence();
    }

    @Override
    public void sessionAboutToChange(Session session) {}

    @Override
    public String getAuthor() {
        return Constant.ZAP_TEAM;
    }

    @Override
    public String getDescription() {
        return Constant.messages.getString("alerts.desc");
    }

    @Override
    public void sessionScopeChanged(Session session) {
        // Have to recheck all alerts to see if they are in scope
        synchronized (this.getTreeModel()) {
            ((AlertNode) this.getFilteredTreeModel().getRoot()).removeAllChildren();
            AlertNode root = (AlertNode) this.getTreeModel().getRoot();
            filterTree(root);
            this.getFilteredTreeModel().nodeStructureChanged(root);
        }

        this.recalcAlerts();
    }

    private void filterTree(AlertNode node) {
        if (node.getUserObject() != null) {
            Alert alert = node.getUserObject();
            if (this.isInFilter(alert)) {
                this.getFilteredTreeModel().addPath(alert);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            this.filterTree(node.getChildAt(i));
        }
    }

    @Override
    public void sessionModeChanged(Mode mode) {
        // Ignore
    }

    public void setAlertTabFocus() {
        this.getAlertPanel().setTabFocus();
    }

    /**
     * Sets whether the "Alerts" tree is filtered, or not based on current session scope.
     *
     * <p>If {@code inScope} is {@code true} only the alerts that are in the current session scope
     * will be shown.
     *
     * <p>Calling this method removes the filter "Link with Sites selection", if enabled, as they
     * are mutually exclusive.
     *
     * @param inScope {@code true} if the "Alerts" tree should be filtered based on current session
     *     scope, {@code false} otherwise.
     * @see #setLinkWithSitesTreeSelection(boolean)
     * @see Session
     */
    public void setShowJustInScope(boolean inScope) {
        if (inScope) {
            setLinkWithSitesTreeSelection(false);
            setTreeModel(this.getFilteredTreeModel());
        } else {
            setMainTreeModel();
        }
    }

    /**
     * Sets the main tree model to the "Alerts" tree, by calling the method {@code
     * setTreeModel(AlertTreeModel)} with the model returned by the method {@code getTreeModel()} as
     * parameter.
     *
     * @see #getTreeModel()
     * @see #setTreeModel(AlertTreeModel)
     */
    void setMainTreeModel() {
        setTreeModel(getTreeModel());
    }

    /**
     * Sets the given {@code alertTreeModel} to the "Alerts" tree and recalculates the number of
     * alerts by calling the method {@code recalcAlerts()}.
     *
     * @param alertTreeModel the model that will be set to the tree.
     * @see #recalcAlerts()
     * @see AlertPanel#getTreeAlert()
     */
    private void setTreeModel(AlertTreeModel alertTreeModel) {
        if (getView() == null) {
            return;
        }
        getAlertPanel().getTreeAlert().setModel(alertTreeModel);
        recalcAlerts();
    }

    /**
     * Sets whether the "Alerts" tree is filtered, or not based on a selected "Sites" tree node.
     *
     * <p>If {@code enabled} is {@code true} only the alerts of the selected "Sites" tree node will
     * be shown.
     *
     * <p>Calling this method removes the filter "Just in Scope", if enabled, as they are mutually
     * exclusive.
     *
     * @param enabled {@code true} if the "Alerts" tree should be filtered based on a selected
     *     "Sites" tree node, {@code false} otherwise.
     * @see #setShowJustInScope(boolean)
     * @see View#getSiteTreePanel()
     */
    public void setLinkWithSitesTreeSelection(boolean enabled) {
        getAlertPanel().setLinkWithSitesTreeSelection(enabled);
    }

    /**
     * Shows the Add Alert dialogue, to add a new alert for the given {@code HistoryReference}.
     *
     * @param ref the history reference for the alert.
     * @since 2.7.0
     */
    public void showAlertAddDialog(HistoryReference ref) {
        if (dialogAlertAdd == null || !dialogAlertAdd.isVisible()) {
            dialogAlertAdd = new AlertAddDialog(getView().getMainFrame(), false);
            dialogAlertAdd.setVisible(true);
            dialogAlertAdd.setHistoryRef(ref);
        }
    }

    /**
     * Shows the Add Alert dialogue, using the given {@code HttpMessage} and history type for the
     * {@code HistoryReference} that will be created if the user creates the alert. The current
     * session will be used to create the {@code HistoryReference}. The alert created will be added
     * to the newly created {@code HistoryReference}.
     *
     * <p>Should be used when the alert is added to a temporary {@code HistoryReference} as the
     * temporary {@code HistoryReference}s are deleted when the session is closed.
     *
     * @param httpMessage the {@code HttpMessage} that will be used to create the {@code
     *     HistoryReference}, must not be {@code null}.
     * @param historyType the type of the history reference that will be used to create the {@code
     *     HistoryReference}.
     * @since 2.7.0
     * @see Model#getSession()
     * @see HistoryReference#HistoryReference(org.parosproxy.paros.model.Session, int, HttpMessage)
     */
    public void showAlertAddDialog(HttpMessage httpMessage, int historyType) {
        if (dialogAlertAdd == null || !dialogAlertAdd.isVisible()) {
            dialogAlertAdd = new AlertAddDialog(getView().getMainFrame(), false);
            dialogAlertAdd.setHttpMessage(httpMessage, historyType);
            dialogAlertAdd.setVisible(true);
        }
    }

    /**
     * Shows the "Edit Alert" dialogue, with the given alert.
     *
     * @param alert the alert to be edited.
     * @since 2.7.0
     */
    public void showAlertEditDialog(Alert alert) {
        if (dialogAlertAdd == null || !dialogAlertAdd.isVisible()) {
            dialogAlertAdd = new AlertAddDialog(getView().getMainFrame(), false);
            dialogAlertAdd.setVisible(true);
            dialogAlertAdd.setAlert(alert);
        }
    }

    AlertAddDialog getDialogAlertAdd() {
        return dialogAlertAdd;
    }

    /** Part of the core set of features that should be supported by all db types */
    @Override
    public boolean supportsDb(String type) {
        return true;
    }

    @Override
    public boolean supportsLowMemory() {
        return true;
    }

    /**
     * Check if an alert already exists in the alerts tree.
     *
     * @param alertToCheck
     * @return true if new alert, false otherwise.
     */
    public boolean isNewAlert(Alert alertToCheck) {
        return (getTreeModel().getAlertNode(alertToCheck) == null);
    }
}
