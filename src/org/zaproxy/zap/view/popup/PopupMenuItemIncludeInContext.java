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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.DatabaseException;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.StructuralSiteNode;
import org.zaproxy.zap.view.ContextIncludePanel;


/**
 * @since 2.3.0
 */
public class PopupMenuItemIncludeInContext extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = 990419495607725846L;

    protected Context context;

    /**
     * This method initializes
     * 
     */
    public PopupMenuItemIncludeInContext() {
        super(Constant.messages.getString("context.new.title"), true);
        this.context = null;
        this.setPrecedeWithSeparator(true);
    }

    public PopupMenuItemIncludeInContext(Context context) {
        super(context.getName(), true);
        this.context = context;
    }

    @Override
    public String getParentMenuName() {
        return Constant.messages.getString("context.include.popup");
    }

    @Override
    public boolean isSubMenu() {
        return true;
    }

    @Override
    public void performAction(SiteNode sn) {
        try {
			performAction(sn.getNodeName(), new StructuralSiteNode(sn).getRegexPattern());
		} catch (DatabaseException e) {
			// Ignore
		}
    }

    protected void performAction(String name, String url) {
        if (context == null) {
            Session session = Model.getSingleton().getSession();
            context = session.getNewContext(name);
            recreateUISharedContexts(session);
        }

        Context uiSharedContext = View.getSingleton().getSessionDialog().getUISharedContext(context.getIndex());
        uiSharedContext.addIncludeInContextRegex(url);
    }

    @Override
    public void performHistoryReferenceActions(List<HistoryReference> hrefs) {
        Session session = Model.getSingleton().getSession();

        if (context != null) {
            recreateUISharedContexts(session);
        }

        super.performHistoryReferenceActions(hrefs);

        // Show the session dialog without recreating UI Shared contexts
        View.getSingleton().showSessionDialog(session, ContextIncludePanel.getPanelName(context.getIndex()),
                false);
    }

    private void recreateUISharedContexts(Session session) {
        // Manually create the UI shared contexts so any modifications are done
        // on an UI shared Context, so changes can be undone by pressing Cancel
        View.getSingleton().getSessionDialog().recreateUISharedContexts(session);
    }

    @Override
    public boolean isButtonEnabledForSiteNode(SiteNode sn) {
        if (context == null) {
            // New context
            return true;
        }
        if (context.isIncluded(sn) || context.isExcluded(sn)) {
            // Either explicitly included or excluded, so would have to change that regex in a non
            // trivial way to include!
            return false;
        }
        return true;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
