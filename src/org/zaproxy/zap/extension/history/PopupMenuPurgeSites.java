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
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.history;

import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

public class PopupMenuPurgeSites extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = 4827464631678110752L;

    private static final Logger logger = Logger.getLogger(PopupMenuPurgeSites.class);

    public PopupMenuPurgeSites() {
        super(Constant.messages.getString("sites.purge.popup"), true);
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return (invoker == Invoker.SITES_PANEL);
    }

    @Override
    public boolean isButtonEnabledForSiteNode(SiteNode sn) {
        return !(sn.isRoot() && sn.getChildCount() == 0);
    }

    @Override
    public void performHistoryReferenceActions(List<HistoryReference> hrefs) {
        if (hrefs.size() > 0) {
            int result = View.getSingleton().showConfirmDialog(Constant.messages.getString("sites.purge.warning"));
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        super.performHistoryReferenceActions(hrefs);
    }

    @Override
    public void performAction(SiteNode sn) {
        purge(Model.getSingleton().getSession().getSiteTree(), sn);
    }

    public static void purge(SiteMap map, SiteNode node) {
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
            ExtensionHistory ext = (ExtensionHistory) Control.getSingleton()
                    .getExtensionLoader()
                    .getExtension(ExtensionHistory.NAME);
            ext.removeFromHistoryList(node.getHistoryReference());
            ext.clearLogPanelDisplayQueue();

            ExtensionAlert extAlert = (ExtensionAlert) Control.getSingleton()
                    .getExtensionLoader()
                    .getExtension(ExtensionAlert.NAME);

            if (node.getHistoryReference() != null) {
                deleteAlertsFromExtensionAlert(extAlert, node.getHistoryReference());
                node.getHistoryReference().delete();
                map.removeHistoryReference(node.getHistoryReference().getHistoryId());
            }

            // delete past reference in node
            while (node.getPastHistoryReference().size() > 0) {
                HistoryReference ref = node.getPastHistoryReference().get(0);
                deleteAlertsFromExtensionAlert(extAlert, ref);
                ext.removeFromHistoryList(ref);
                ext.clearLogPanelDisplayQueue();
                ext.delete(ref);
                node.getPastHistoryReference().remove(0);
                map.removeHistoryReference(ref.getHistoryId());
            }

            map.removeNodeFromParent(node);
        }

    }

    private static void deleteAlertsFromExtensionAlert(ExtensionAlert extAlert, HistoryReference historyReference) {
        if (extAlert == null) {
            return;
        }

        extAlert.deleteHistoryReferenceAlerts(historyReference);
    }

    @Override
    public boolean isSafe() {
        return true;
    }

}
