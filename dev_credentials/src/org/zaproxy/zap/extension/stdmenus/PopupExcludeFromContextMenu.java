/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2012 The ZAP Development Team
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
package org.zaproxy.zap.extension.stdmenus;

import java.util.List;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.ContextExcludePanel;
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupExcludeFromContextMenu extends PopupMenuSiteNode {

	private static final long serialVersionUID = 2282358266003940700L;

	protected Context context;

	public PopupExcludeFromContextMenu(Context context) {
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
	public void performAction(SiteNode sn) throws Exception {

		Session session = Model.getSingleton().getSession();

		// Manually create the UI shared contexts so any modifications are done
		// on an UI shared Context, so changes can be undone by pressing Cancel
		SessionDialog sessionDialog = View.getSingleton().getSessionDialog();
		sessionDialog.recreateUISharedContexts(session);
		Context uiSharedContext = sessionDialog.getUISharedContext(context.getIndex());

		uiSharedContext.excludeFromContext(sn, !sn.isLeaf());

		// Show the session dialog without recreating UI Shared contexts
		View.getSingleton().showSessionDialog(session, ContextExcludePanel.getPanelName(context.getIndex()),
				false);
	}

	@Override
	public void performActions(List<HistoryReference> hrefs) throws Exception {
		super.performActions(hrefs);
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		return true;
	}

	@Override
	public boolean isEnabledForSiteNode(SiteNode sn) {
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
