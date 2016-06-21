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
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

public class PopupMenuPurgeSites extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = 4827464631678110752L;

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
        ExtensionHistory extHistory =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);

        if (extHistory != null) {
            extHistory.purge(Model.getSingleton().getSession().getSiteTree(), sn);
        }
    }
    
    /**
     * @deprecated  As of release 2.5.0, replaced by 
     * {@link org.parosproxy.paros.extension.history.ExtensionHistory#purge(
     *      org.parosproxy.paros.model.SiteMap, org.parosproxy.paros.model.SiteNode)} 
     */
    @Deprecated
    public static void purge(SiteMap map, SiteNode node) {
        ExtensionHistory extHistory =
                Control.getSingleton().getExtensionLoader().getExtension(ExtensionHistory.class);

        if (extHistory != null) {
            extHistory.purge(map, node);
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }

}
