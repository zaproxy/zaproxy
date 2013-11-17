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

import java.sql.SQLException;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.ContextIncludePanel;
import org.zaproxy.zap.view.PopupMenuSiteNode;

public class PopupIncludeInContextMenu extends PopupMenuSiteNode {

	private static final long serialVersionUID = 2282358266003940700L;

	protected Context context;

	/**
	 * This method initializes
	 * 
	 */
	public PopupIncludeInContextMenu() {
		super(Constant.messages.getString("context.new.title"), true);
		this.context = null;
		this.setPrecedeWithSeparator(true);
	}

	public PopupIncludeInContextMenu(Context context) {
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
	public void performAction(SiteNode sn) throws Exception {
		String url = new URI(sn.getHierarchicNodeName(), false).toString();

		if (sn.isLeaf()) {
			url = Pattern.quote(url);
		} else {
			url = Pattern.quote(url) + ".*";
		}

		performAction(url);
	}

	protected void performAction(String url) throws SQLException {
		Session session = Model.getSingleton().getSession();

		if (context == null) {
			context = session.getNewContext();
		}

		// Manually create the UI shared contexts so any modifications are done
		// on an UI shared Context, so changes can be undone by pressing Cancel
		SessionDialog sessionDialog = View.getSingleton().getSessionDialog();
		sessionDialog.recreateUISharedContexts(session);
		Context uiSharedContext = sessionDialog.getUISharedContext(context.getIndex());

		uiSharedContext.addIncludeInContextRegex(url);

		// Show the session dialog without recreating UI Shared contexts
		View.getSingleton().showSessionDialog(session, ContextIncludePanel.getPanelName(context.getIndex()),
				false);
	}

	@Override
	public boolean isEnableForInvoker(Invoker invoker) {
		return true;
	}

	@Override
	public boolean isEnabledForSiteNode(SiteNode sn) {
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
