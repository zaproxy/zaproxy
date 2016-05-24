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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.extension.alert.ExtensionAlert;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;

public class PopupMenuPurgeHistory extends PopupMenuItemHistoryReferenceContainer {

    private static final long serialVersionUID = -155358408946131183L;

    private final ExtensionHistory extension;

    public PopupMenuPurgeHistory(ExtensionHistory extension) {
        super(Constant.messages.getString("history.purge.popup"), true);

        this.extension = extension;
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return (invoker == Invoker.HISTORY_PANEL);
    }

    @Override
    public void performHistoryReferenceActions(List<HistoryReference> hrefs) {
        if (hrefs.size() > 1) {
            int result = extension.getView().showConfirmDialog(Constant.messages.getString("history.purge.warning"));
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        synchronized (extension) {
            for (HistoryReference href : hrefs) {
                purgeHistory(href);
            }
        }
    }

    @Override
    public void performAction(HistoryReference href) {
    }

    private void purgeHistory(HistoryReference ref) {
        if (ref == null) {
            return;
        }

        extension.removeFromHistoryList(ref);
        extension.clearLogPanelDisplayQueue();

        ExtensionAlert extAlert = (ExtensionAlert) Control.getSingleton()
                .getExtensionLoader()
                .getExtension(ExtensionAlert.NAME);

        if (extAlert != null) {
            extAlert.deleteHistoryReferenceAlerts(ref);
        }

        extension.delete(ref);

        SiteNode node = ref.getSiteNode();
        if (node == null) {
            return;
        }

        SiteMap map = Model.getSingleton().getSession().getSiteTree();

        if (node.getHistoryReference() == ref) {
            // same active Node
            extension.purge(map, node);

        } else {
            node.getPastHistoryReference().remove(ref);
            map.removeHistoryReference(ref.getHistoryId());
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }

}
