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
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupMenuPurgeSites extends PopupMenuSiteNode {

    private static final long serialVersionUID = 4827464631678110752L;

    private static final Logger logger = Logger.getLogger(PopupMenuPurgeSites.class);

    public PopupMenuPurgeSites() {
        super(Constant.messages.getString("sites.purge.popup"), true);
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker) {
        return (invoker == Invoker.sites);
    }

    @Override
    public boolean isEnabledForSiteNode(SiteNode sn) {
        return !(sn == null || (sn.isRoot() && sn.getChildCount() == 0));
    }

    @Override
    public void performActions(List<HistoryReference> hrefs) throws Exception {
        if (hrefs.size() > 0) {
            int result = View.getSingleton().showConfirmDialog(Constant.messages.getString("sites.purge.warning"));
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }

        super.performActions(hrefs);
    }

    @Override
    public void performAction(SiteNode sn) throws Exception {
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

            ExtensionAlert extAlert = (ExtensionAlert) Control.getSingleton()
                    .getExtensionLoader()
                    .getExtension(ExtensionAlert.NAME);

            if (node.getHistoryReference() != null) {
                if (extAlert != null) {
                    // Iterating over the getAlerts() while deleting the alert will result in a ConcurrentModificationException.
                    while (!node.getHistoryReference().getAlerts().isEmpty()) {
                        // Note the alert is removed as a side effect
                        extAlert.deleteAlert(node.getHistoryReference().getAlerts().get(0));
                    }
                }
                node.getHistoryReference().delete();
            }

            // delete past reference in node
            while (node.getPastHistoryReference().size() > 0) {
                HistoryReference ref = node.getPastHistoryReference().get(0);
                if (extAlert != null) {
                    // Iterating over the getAlerts() while deleting the alert will result in a ConcurrentModificationException.
                    while (!ref.getAlerts().isEmpty()) {
                        extAlert.deleteAlert(ref.getAlerts().get(0));
                        ref.getAlerts().remove(0);
                    }
                }
                ext.removeFromHistoryList(ref);
                ref.delete();
                node.getPastHistoryReference().remove(0);
            }

            map.removeNodeFromParent(node);
        }

    }

}
