/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.view.popup;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.ContextExcludePanel;

/** @since 2.3.0 */
@SuppressWarnings("serial")
public class PopupMenuItemExcludeFromContext extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = 2766535157899537709L;

    private static final Logger LOGGER =
            LogManager.getLogger(PopupMenuItemExcludeFromContext.class);

    protected Context context;

    public PopupMenuItemExcludeFromContext(Context context) {
        super(context.getName(), true);
        this.context = context;
    }

    @Override
    public String getParentMenuName() {
        return Constant.messages.getString("context.exclude.popup");
    }

    @Override
    public boolean isSubMenu() {
        return true;
    }

    @Override
    public void performAction(SiteNode sn) {
        Context uiSharedContext =
                View.getSingleton().getSessionDialog().getUISharedContext(context.getId());

        try {
            uiSharedContext.excludeFromContext(sn, !sn.isLeaf());
        } catch (Exception e) {
            LOGGER.error("Failed to execute action exclude from context: {}", e.getMessage(), e);
        }
    }

    @Override
    public void performHistoryReferenceActions(List<HistoryReference> hrefs) {
        Session session = Model.getSingleton().getSession();
        // Manually create the UI shared contexts so any modifications are done
        // on an UI shared Context, so changes can be undone by pressing Cancel
        View.getSingleton().getSessionDialog().recreateUISharedContexts(session);

        super.performHistoryReferenceActions(hrefs);

        // Show the session dialog without recreating UI Shared contexts
        View.getSingleton()
                .showSessionDialog(
                        session, ContextExcludePanel.getPanelName(context.getId()), false);
    }

    @Override
    public boolean isButtonEnabledForSiteNode(SiteNode sn) {
        if (!context.isIncluded(sn) || context.isExcluded(sn)) {
            // Either not included or excluded, so would have to change that regex in a non trivial
            // way to include!
            return false;
        }
        return true;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
