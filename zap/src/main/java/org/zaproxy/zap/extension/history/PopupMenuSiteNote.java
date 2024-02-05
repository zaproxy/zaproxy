/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2024 The ZAP Development Team
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ViewDelegate;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemHistoryReferenceContainer;

@SuppressWarnings("serial")
public class PopupMenuSiteNote extends PopupMenuItemHistoryReferenceContainer {

    private static final long serialVersionUID = -5692544221103745600L;

    private static final Logger LOGGER = LogManager.getLogger(PopupMenuSiteNote.class);

    private SiteNotesAddDialog siteNotesAddDialog;

    public PopupMenuSiteNote(ViewDelegate view) {
        super(Constant.messages.getString("history.note.popup"));
        siteNotesAddDialog = new SiteNotesAddDialog(view.getMainFrame(), false);
    }

    @Override
    protected boolean isEnableForInvoker(
            Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return invoker == Invoker.SITES_PANEL;
    }

    @Override
    protected boolean isButtonEnabledForHistoryReference(HistoryReference historyReference) {
        return !HistoryReference.getTemporaryTypes().contains(historyReference.getHistoryType());
    }

    private void populateSiteNotesAddDialogAndSetVisible(SiteNode siteNode) {
        siteNotesAddDialog.setSiteNode(siteNode);
        siteNotesAddDialog.setVisible(true);
    }

    @Override
    public void performAction(HistoryReference historyReference) {
        try {
            SiteNode siteNode = historyReference.getSiteNode();
            if (siteNode != null) {
                populateSiteNotesAddDialogAndSetVisible(siteNode);
            }

        } catch (Error e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
