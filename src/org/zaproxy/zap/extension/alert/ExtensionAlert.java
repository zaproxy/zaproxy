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
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control.Mode;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.db.RecordAlert;
import org.parosproxy.paros.db.RecordScan;
import org.parosproxy.paros.db.TableAlert;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.view.MainFooterPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.XmlReporterExtension;
import org.zaproxy.zap.extension.help.ExtensionHelp;

public class ExtensionAlert extends ExtensionAdaptor implements SessionChangedListener, XmlReporterExtension {

    public static final String NAME = "ExtensionAlert";
    private List<HistoryReference> hrefs = new ArrayList<>();
    private AlertTreeModel treeModel = null;
    private AlertTreeModel filteredTreeModel = null;
    private AlertPanel alertPanel = null;
    private RecordScan recordScan = null;
    private PopupMenuAlertEdit popupMenuAlertEdit = null;
    private PopupMenuAlertDelete popupMenuAlertDelete = null;
    private PopupMenuAlertsRefresh popupMenuAlertsRefresh = null;
    private PopupMenuShowAlerts popupMenuShowAlerts = null;
    private Logger logger = Logger.getLogger(ExtensionAlert.class);
    private boolean inScope = false;

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
     */
    private void initialize() {
        this.setName(NAME);
        this.setOrder(27);
    }

    @Override
    public void hook(ExtensionHook extensionHook) {
        super.hook(extensionHook);
        if (getView() != null) {
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertEdit());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertDelete());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuAlertsRefresh());
            extensionHook.getHookMenu().addPopupMenuItem(getPopupMenuShowAlerts());

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
                alert.setHistoryRef(ref);
            }

            hrefs.add(ref);

            writeAlertToDB(alert, ref);
            addAlertToDisplay(alert, ref, alert.getMessage());

            // The node node may have a new alert flag...
            this.siteNodeChanged(ref.getSiteNode());

            // Clear the message so that it can be GC'ed
            alert.setMessage(null);

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

    private void addAlertToDisplay(final Alert alert, final HistoryReference ref, final HttpMessage msg) {
        if (getView() == null) {
            // Running as a daemon
            return;
        }
        if (EventQueue.isDispatchThread()) {
            addAlertToDisplayEventHandler(alert, ref, msg);

        } else {

            try {
                // Changed from invokeAndWait due to the number of interrupt exceptions
                // And its likely to always to run from a background thread anyway
                //EventQueue.invokeAndWait(new Runnable() {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        addAlertToDisplayEventHandler(alert, ref, msg);
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

    private void addAlertToDisplayEventHandler(Alert alert, HistoryReference ref, HttpMessage msg) {

        synchronized (this.getTreeModel()) {
        	this.getTreeModel().addPath(alert);
        	if (isInFilter(alert)) {
	        	this.getFilteredTreeModel().addPath(alert);
        	}
            getAlertPanel().expandRoot();
            this.recalcAlerts();
        }

        SiteMap siteTree = this.getModel().getSession().getSiteTree();
        SiteNode node = siteTree.findNode(alert.getMsgUri(), alert.getMethod(), alert.getPostData());
        if (ref != null && (node == null || !node.hasAlert(alert))) {
            // Add new alerts to the site tree
        	if (msg != null) {
        		// Saves a db read, which is always well worth it!
        		siteTree.addPath(ref, msg);
        	} else {
        		siteTree.addPath(ref);
        	}
            ref.addAlert(alert);
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
                alert.getEvidence(), alert.getCweId(), alert.getWascId(),
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
                alert.getParam(), alert.getAttack(), alert.getOtherInfo(), alert.getSolution(), alert.getReference(), 
                alert.getEvidence(), alert.getCweId(), alert.getWascId(), alert.getSourceHistoryId());
    }

    public void displayAlert(Alert alert) {
        logger.debug("displayAlert " + alert.getAlert() + " " + alert.getUri());
        this.alertPanel.getAlertViewPanel().displayAlert(alert);
    }

    public void updateAlertInTree(Alert originalAlert, Alert alert) {
        this.getTreeModel().updatePath(originalAlert, alert);
    	if (isInFilter(alert)) {
    		this.getFilteredTreeModel().updatePath(originalAlert, alert);
    	}
    	this.recalcAlerts();
    }

    @Override
    public void sessionChanged(final Session session) {
        if (EventQueue.isDispatchThread()) {
            sessionChangedEventHandler(session);

        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
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
        AlertTreeModel tree = this.getTreeModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getRoot();

        while (root.getChildCount() > 0) {
            tree.removeNodeFromParent((MutableTreeNode) root.getChildAt(0));
        }
        
        tree = this.getFilteredTreeModel();
        root = (DefaultMutableTreeNode) tree.getRoot();

        while (root.getChildCount() > 0) {
            tree.removeNodeFromParent((MutableTreeNode) root.getChildAt(0));
        }
        
        hrefs = new ArrayList<>();

    	if (session == null) {
    		// Null session indicated we're sutting down
    		return;
    	}

        try {
            refreshAlert(session);
            // ZAP: this prevent the UI getting corrupted
            tree.nodeStructureChanged(root);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        this.recalcAlerts();
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
                addAlertToDisplay(alert, alert.getHistoryRef(), null);
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

    private PopupMenuShowAlerts getPopupMenuShowAlerts() {
        if (popupMenuShowAlerts == null) {
            popupMenuShowAlerts = new PopupMenuShowAlerts(Constant.messages.getString("alerts.view.popup"));
        }
        return popupMenuShowAlerts;
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
        // Note - tried doing this in a SwingWorker but it too a LOT longer to run
        SiteMap siteTree = this.getModel().getSession().getSiteTree();
        SiteNode node = siteTree.findNode(alert.getMsgUri(), alert.getMethod(), alert.getPostData());
        if (node != null && node.hasAlert(alert)) {
            siteNodeChanged(node);
        }

        synchronized (this.getTreeModel()) {
        	this.getTreeModel().deletePath(alert);
        	this.getFilteredTreeModel().deletePath(alert);
            List<HistoryReference> toDelete = new ArrayList<>();
            for (HistoryReference href : hrefs) {
                if (href.getAlerts().contains(alert)) {
                    href.deleteAlert(alert);
                    try {
                        // TODO Ideally should cache the param names (and change findNode) so we dont have to get
                        // the message from the db
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

        this.recalcAlerts();
    }
    
    private void recalcAlerts() {
    	if (View.isInitialised()) {
            if (inScope) {
            	this.recalcAlerts(getFilteredTreeModel());
            } else {
            	this.recalcAlerts(getTreeModel());
            }
    	}
    }
    
    private void recalcAlerts(AlertTreeModel tree) {
    	// Must only be called when View is initialised
    	int totalInfo = 0;
    	int totalLow = 0;
    	int totalMedium = 0;
    	int totalHigh = 0;

    	AlertNode parent = (AlertNode) tree.getRoot();
    	if (parent != null) {
            for (int i=0; i<parent.getChildCount(); i++) {
                AlertNode child = (AlertNode) parent.getChildAt(i);
            	switch (child.getRisk()) {
            	case Alert.RISK_INFO:	totalInfo++;	break;
            	case Alert.RISK_LOW:	totalLow++;		break;
            	case Alert.RISK_MEDIUM:	totalMedium++;	break;
            	case Alert.RISK_HIGH:	totalHigh++;	break;
            	}
            }
    	}
    	MainFooterPanel footer = View.getSingleton().getMainFrame().getMainFooterPanel();
        footer.setAlertInfo(totalInfo);
        footer.setAlertLow(totalLow);
        footer.setAlertMedium(totalMedium);
        footer.setAlertHigh(totalHigh);
    }

    public List<Alert> getAllAlerts() {
        List<Alert> allAlerts = new ArrayList<>();

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

    @Override
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

	@Override
	public String getAuthor() {
		return Constant.ZAP_TEAM;
	}

	@Override
	public String getDescription() {
		return Constant.messages.getString("alerts.desc");
	}

	@Override
	public URL getURL() {
		try {
			return new URL(Constant.ZAP_HOMEPAGE);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public void sessionScopeChanged(Session session) {
		// Have to recheck all alerts to see if they are in scope
		synchronized (this.getTreeModel()) {
			((AlertNode)this.getFilteredTreeModel().getRoot()).removeAllChildren();
			AlertNode root = (AlertNode)this.getTreeModel().getRoot();
			filterTree(root);
			this.getFilteredTreeModel().nodeStructureChanged(root);
		}
		
		this.recalcAlerts();
	}
	
	private void filterTree(AlertNode node) {
		if (node.getUserObject() != null && node.getUserObject() instanceof Alert) {
			Alert alert = (Alert) node.getUserObject();
			if (this.isInFilter(alert)) {
				this.getFilteredTreeModel().addPath(alert);
			}
		}
		for (int i=0; i < node.getChildCount(); i++) {
			this.filterTree((AlertNode)node.getChildAt(i));
		}
	}

	@Override
	public void sessionModeChanged(Mode mode) {
		// Ignore
	}

	public void setAlertTabFocus() {
		this.getAlertPanel().setTabFocus();
	}

	public void setShowJustInScope(boolean inScope) {
		this.inScope = inScope;
		if (inScope) {
			this.getAlertPanel().getTreeAlert().setModel(this.getFilteredTreeModel());
		} else {
			this.getAlertPanel().getTreeAlert().setModel(this.getTreeModel());
		}
		this.recalcAlerts();
	}
}
