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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.history;

import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import org.apache.commons.configuration.FileConfiguration;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

public class PopupMenuPurgeSites extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = 4827464631678110752L;
    private static final String REMOVE_CONFIRMATION_KEY = "view.deleteconfirmation.sites";

    public PopupMenuPurgeSites() {
        super(Constant.messages.getString("sites.purge.popup"), true);

        setAccelerator(View.getSingleton().getDefaultDeleteKeyStroke());
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        switch (invoker) {
            case SITES_PANEL:
            case SEARCH_PANEL:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean isButtonEnabledForSiteNode(SiteNode sn) {
        return !(sn.isRoot() && sn.getChildCount() == 0);
    }

    @Override
    public void performHistoryReferenceActions(List<HistoryReference> hrefs) {
        if (hrefs.isEmpty()) {
            return;
        }

        FileConfiguration config = Model.getSingleton().getOptionsParam().getConfig();
        boolean confirmRemoval = config.getBoolean(REMOVE_CONFIRMATION_KEY, false);

        if (confirmRemoval) {
            JCheckBox removeWithoutConfirmationCheckBox =
                    new JCheckBox(Constant.messages.getString("sites.purge.confirm.message"));
            Object[] messages = {
                Constant.messages.getString("sites.purge.warning"),
                " ",
                removeWithoutConfirmationCheckBox
            };
            int result =
                    JOptionPane.showOptionDialog(
                            View.getSingleton().getMainFrame(),
                            messages,
                            Constant.messages.getString("sites.purge.title"),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[] {
                                Constant.messages.getString("sites.purge.confirm"),
                                Constant.messages.getString("sites.purge.cancel")
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

    @Override
    public boolean isSafe() {
        return true;
    }
}
