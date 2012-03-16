/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2011 The ZAP Development team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.alert;

import java.awt.EventQueue;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordScan;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.ViewDelegate;
import org.zaproxy.zap.extension.XmlReporterExtension;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.zaproxy.zap.extension.help.ExtensionHelp;

/**
 *
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionAlert extends ExtensionAdaptor implements SessionChangedListener, XmlReporterExtension {

    public static final String NAME = "ExtensionAlert";
    private List<HistoryReference> hrefs = new ArrayList<HistoryReference>();
    private AlertTreeModel treeAlert = null;
    private AlertPanel alertPanel = null;
    private RecordScan recordScan = null;
    private PopupMenuAlertEdit popupMenuAlertEdit = null;
    private PopupMenuAlertDelete popupMenuAlertDelete = null;
    private PopupMenuAlertsRefresh popupMenuAlertsRefresh = null;
    private Logger logger = Logger.getLogger(ExtensionAlert.class);

    /**
     *
     */
    public ExtensionAlert() {
        super();
        initialize();
    }

    /**
     * @param name
     */
    public ExtensionAlert(String name) {
        super(name);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setName(NAME);
        this.setOrder(27);
    }

    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertEdit());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertDelete());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertsRefresh());

            extensionHook.getHookView().addStatusPanel(getAlertPanel());

            ExtensionHelp.enableHelpKey(getAlertPanel(), "ui.tabs.alerts");
        }
        extensionHook.addSessionListener(this);

    }

    public void alertFound(Alert alert, HistoryReference ref) {
        try {
            logger.debug("alertFound " + alert.getAlert() + " " + alert.getUri());
            if (ref == null) {
                ref = alert.getHistoryRef();
            }
            if (ref == null) {
                ref = new HistoryReference(getModel().getSession(), HistoryReference.TYPE_SCANNER, alert.getMessage());
            }

            hrefs.add(ref);

            writeAlertToDB(alert, ref);
            addAlertToDisplay(alert, ref);

            // The node node may have a new alert flag...
            this.siteNodeChanged(ref.getSiteNode());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void siteNodeChanged(TreeNode node) {
        if (node == null) {
            return;
        }
        SiteMap siteTree = this.getModel().getSession().getSiteTree();
        siteTree.nodeChanged(node);
        siteNodeChanged(node.getParent());
    }

    private void addAlertToDisplay(final Alert alert, final HistoryReference ref) {
        if (getView() == null) {
            // Running as a daemon
            return;
        }
        if (EventQueue.isDispatchThread()) {
            addAlertToDisplayEventHandler(alert, ref);

        } else {

            try {
                // Changed from invokeAndWait due to the number of interrupt exceptions
                // And its likely to always to run from a background thread anyway
                //EventQueue.invokeAndWait(new Runnable() {
                EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        addAlertToDisplayEventHandler(alert, ref);
                    }
                });
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void addAlertToDisplayEventHandler(Alert alert, HistoryReference ref) {

        synchronized (treeAlert) {
            treeAlert.addPath(alert);
            getAlertPanel().expandRoot();
        }

        SiteMap siteTree = this.getModel().getSession().getSiteTree();
        SiteNode node = siteTree.findNode(alert.getMessage());
        if (ref != null && (node == null || !node.hasAlert(alert))) {
            // Add new alerts to the site tree
            siteTree.addPath(ref);
            ref.addAlert(alert);
            this.siteNodeChanged(ref.getSiteNode());
        }
    }

    /**
     * This method initializes alertPanel
     *
     * @return com.proofsecure.paros.extension.scanner.AlertPanel
     */
    AlertPanel getAlertPanel() {
        if (alertPanel == null) {
            alertPanel = new AlertPanel();
            alertPanel.setView(getView());
            alertPanel.setSize(345, 122);
            alertPanel.getTreeAlert().setModel(getTreeModel());
        }

        return alertPanel;
    }

    @Override
    public void initView(ViewDelegate view) {
        super.initView(view);
        getAlertPanel().setView(view);
    }

    // ZAP: Changed return type for getTreeModel
    private AlertTreeModel getTreeModel() {
        if (treeAlert == null) {
            treeAlert = new AlertTreeModel();
        }
        return treeAlert;
    }

    private void writeAlertToDB(Alert alert, HistoryReference ref) throws HttpMalformedHeaderException, SQLException {

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        int scanId = 0;
        if (recordScan != null) {
            scanId = recordScan.getScanId();
        }
        RecordAlert recordAlert = tableAlert.write(
                scanId, alert.getPluginId(), alert.getAlert(), alert.getRisk(), alert.getReliability(),
                alert.getDescription(), alert.getUri(), alert.getParam(), alert.getAttack(),
                alert.getOtherInfo(), alert.getSolution(), alert.getReference(),
                ref.getHistoryId(), alert.getSourceHistoryId());

        alert.setAlertId(recordAlert.getAlertId());

    }

    public void updateAlert(Alert alert) throws HttpMalformedHeaderException, SQLException {
        logger.debug("updateAlert " + alert.getAlert() + " " + alert.getUri());
        updateAlertInDB(alert);
        if (alert.getHistoryRef() != null) {
            this.siteNodeChanged(alert.getHistoryRef().getSiteNode());
        }
    }

    private void updateAlertInDB(Alert alert) throws HttpMalformedHeaderException, SQLException {

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        tableAlert.update(alert.getAlertId(), alert.getAlert(), alert.getRisk(),
                alert.getReliability(), alert.getDescription(), alert.getUri(),
                alert.getParam(), alert.getAttack(), alert.getOtherInfo(), alert.getSolution(),
                alert.getReference(), alert.getSourceHistoryId());
    }

    public void displayAlert(Alert alert) {
        logger.debug("displayAlert " + alert.getAlert() + " " + alert.getUri());
        this.alertPanel.getAlertViewPanel().displayAlert(alert);
    }

    public void updateAlertInTree(Alert originalAlert, Alert alert) {
        this.getTreeModel().updatePath(originalAlert, alert);
    }

    public void sessionChanged(final Session session) {
        if (EventQueue.isDispatchThread()) {
            sessionChangedEventHandler(session);

        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {

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
        AlertTreeModel tree = (AlertTreeModel) getAlertPanel().getTreeAlert().getModel();

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getRoot();

        while (root.getChildCount() > 0) {
            tree.removeNodeFromParent((MutableTreeNode) root.getChildAt(0));
        }
        // ZAP: Reset the alert counts
        tree.recalcAlertCounts();
        hrefs = new ArrayList<HistoryReference>();

        try {
            refreshAlert(session);
            // ZAP: this prevent the UI getting corrupted
            tree.nodeStructureChanged(root);
        } catch (SQLException e) {
            // ZAP: Print stack trace to Output tab
            getView().getOutputPanel().append(e);
        }

    }

    private void refreshAlert(Session session) throws SQLException {
        SiteMap siteTree = this.getModel().getSession().getSiteTree();

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        Vector<Integer> v = tableAlert.getAlertList();

        for (int i = 0; i < v.size(); i++) {
            int alertId = v.get(i).intValue();
            RecordAlert recAlert = tableAlert.read(alertId);
            Alert alert = new Alert(recAlert);
            if (alert.getHistoryRef() != null) {
                // The ref can be null if hrefs are purged
                addAlertToDisplay(alert, alert.getHistoryRef());
                this.hrefs.add(alert.getHistoryRef());
            }
        }
        siteTree.nodeStructureChanged((SiteNode) siteTree.getRoot());
    }

    private PopupMenuAlertEdit getPopupMenuAlertEdit() {
        if (popupMenuAlertEdit == null) {
            popupMenuAlertEdit = new PopupMenuAlertEdit();
            popupMenuAlertEdit.setExtension(this);
        }
        return popupMenuAlertEdit;
    }

    private PopupMenuAlertDelete getPopupMenuAlertDelete() {
        if (popupMenuAlertDelete == null) {
            popupMenuAlertDelete = new PopupMenuAlertDelete();
            popupMenuAlertDelete.setExtension(this);
        }
        return popupMenuAlertDelete;
    }

    private PopupMenuAlertsRefresh getPopupMenuAlertsRefresh() {
        if (popupMenuAlertsRefresh == null) {
            popupMenuAlertsRefresh = new PopupMenuAlertsRefresh();
            popupMenuAlertsRefresh.setExtension(this);
        }
        return popupMenuAlertsRefresh;
    }

    public void deleteAlert(Alert alert) {
        logger.debug("deleteAlert " + alert.getAlert() + " " + alert.getUri());

        try {
            getModel().getDb().getTableAlert().deleteAlert(alert.getAlertId());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        deleteAlertFromDisplay(alert);

    }

    private void deleteAlertFromDisplay(final Alert alert) {
        if (getView() == null) {
            // Running as a daemon
            return;
        }
        if (EventQueue.isDispatchThread()) {
            deleteAlertFromDisplayEventHandler(alert);

        } else {

            try {
                EventQueue.invokeAndWait(new Runnable() {

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
        // Note - tried doing this in a SwingWorker but it too a LOT longer to run
        SiteMap siteTree = this.getModel().getSession().getSiteTree();
        SiteNode node = siteTree.findNode(alert.getMessage());
        if (node != null && node.hasAlert(alert)) {
            siteNodeChanged(node);
        }

        synchronized (treeAlert) {
            treeAlert.deletePath(alert);
            List<HistoryReference> toDelete = new ArrayList<HistoryReference>();
            for (HistoryReference href : hrefs) {
                if (href.getAlerts().contains(alert)) {
                    href.deleteAlert(alert);
                    try {
                        node = siteTree.findNode(href.getHttpMessage());
                        if (node != null) {
	                        node.deleteAlert(alert);
	                        siteNodeChanged(node);
                        }
                        if (href.getAlerts().size() == 0) {
                            toDelete.add(href);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            for (HistoryReference href : toDelete) {
                hrefs.remove(href);
            }
        }

        AlertTreeModel tree = (AlertTreeModel) getAlertPanel().getTreeAlert().getModel();
        tree.recalcAlertCounts();
    }

    public List<Alert> getAllAlerts() {
        List<Alert> allAlerts = new ArrayList<Alert>();

        TableAlert tableAlert = getModel().getDb().getTableAlert();
        Vector<Integer> v;
        try {
            v = tableAlert.getAlertList();

            for (int i = 0; i < v.size(); i++) {
                int alertId = v.get(i).intValue();
                RecordAlert recAlert = tableAlert.read(alertId);
                Alert alert = new Alert(recAlert);
                if (!allAlerts.contains(alert)) {
                    allAlerts.add(alert);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return allAlerts;
    }

    public String getXml(SiteNode site) {
        StringBuilder xml = new StringBuilder();
        xml.append("<alerts>");
        List<Alert> alerts = site.getAlerts();
        for (Alert alert : alerts) {
            if (alert.getReliability() != Alert.FALSE_POSITIVE) {
                String urlParamXML = alert.getUrlParamXML();
                xml.append(alert.toPluginXML(urlParamXML));
            }
        }
        xml.append("</alerts>");
        return xml.toString();
    }

    @Override
	public void sessionAboutToChange(Session session) {
	}
}
