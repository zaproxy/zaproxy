/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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

import java.util.regex.Pattern;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.StructuralNodeModifier;
import org.zaproxy.zap.view.ContextStructurePanel;

/** @since 2.4.3 */
@SuppressWarnings("serial")
public class PopupMenuItemContextDataDrivenNode extends PopupMenuItemSiteNodeContainer {

    private static final long serialVersionUID = 990419495607725846L;

    protected Context context;

    public PopupMenuItemContextDataDrivenNode(Context context, String name) {
        super(name, true);
        this.context = context;
    }

    @Override
    public String getParentMenuName() {
        return Constant.messages.getString("context.flag.popup");
    }

    @Override
    public boolean isSubMenu() {
        return true;
    }

    @Override
    public void performAction(SiteNode sn) {
        Session session = Model.getSingleton().getSession();

        SessionDialog sessionDialog = View.getSingleton().getSessionDialog();
        sessionDialog.recreateUISharedContexts(session);
        Context uiSharedContext = sessionDialog.getUISharedContext(context.getId());

        // We want to form a regex expression like:
        // https://www.example.com/(aa/bb/cc/)(.+?)(/.*)

        StringBuilder sb = new StringBuilder();

        SiteNode parent = sn.getParent();

        while (!parent.getParent().isRoot()) {
            sb.insert(0, "/");
            if (parent.isDataDriven()) {
                // Don't want these in their own regex group
                sb.insert(0, ".+?");
            } else {
                sb.insert(0, parent.getCleanNodeName());
            }
            parent = parent.getParent();
        }
        sb.insert(0, "/(");
        sb.insert(0, parent.getCleanNodeName());
        sb.append(")(.+?)(/.*)");
        Pattern p = Pattern.compile(sb.toString());

        uiSharedContext.addDataDrivenNodes(
                new StructuralNodeModifier(
                        StructuralNodeModifier.Type.DataDrivenNode,
                        p,
                        uiSharedContext.getDefaultDDNName()));

        // Show the session dialog without recreating UI Shared contexts
        View.getSingleton()
                .showSessionDialog(
                        session, ContextStructurePanel.getPanelName(context.getId()), false);
    }

    @Override
    public boolean isButtonEnabledForSiteNode(SiteNode sn) {
        return sn.getLevel() > 2;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
