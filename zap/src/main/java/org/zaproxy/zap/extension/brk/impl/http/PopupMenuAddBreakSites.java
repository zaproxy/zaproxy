/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.brk.impl.http;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.view.messagecontainer.http.HttpMessageContainer;
import org.zaproxy.zap.view.popup.PopupMenuItemSiteNodeContainer;

@SuppressWarnings("serial")
public class PopupMenuAddBreakSites extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = -7635703590177283587L;

    private HttpBreakpointsUiManagerInterface uiManager;

    public PopupMenuAddBreakSites(HttpBreakpointsUiManagerInterface uiManager) {
        super(Constant.messages.getString("brk.add.popup"));

        this.uiManager = uiManager;
    }

    @Override
    public boolean isEnableForInvoker(Invoker invoker, HttpMessageContainer httpMessageContainer) {
        return (invoker == Invoker.SITES_PANEL);
    }

    @Override
    public void performAction(SiteNode sn) {
        try {
            uiManager.handleAddBreakpoint(new StructuralSiteNode(sn).getRegexPattern(false));
        } catch (DatabaseException e) {
            // Ignore
        }
    }
}
