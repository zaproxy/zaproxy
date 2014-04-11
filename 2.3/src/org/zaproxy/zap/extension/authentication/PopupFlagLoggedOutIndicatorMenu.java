/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.authentication;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.view.SessionDialog;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httppanel.HttpPanelResponse;
import org.zaproxy.zap.model.Context;

/**
 * The Popup Menu item used for marking a text in the response panel as Logged out indicator.
 */
public class PopupFlagLoggedOutIndicatorMenu extends ExtensionPopupMenuItem {

	private static final long serialVersionUID = -3162691875698199510L;
	private String selectedText = null;
	private int contextId;

	public PopupFlagLoggedOutIndicatorMenu(Context ctx) {
		this.contextId = ctx.getIndex();

		this.setText(MessageFormat.format(
				Constant.messages.getString("authentication.popup.indicator.loggedOut"), ctx.getName()));
		this.addActionListener(new java.awt.event.ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				performAction();
			}
		});
	}

	public void performAction() {
		// Manually create the UI shared contexts so any modifications are done
		// on an UI shared Context, so changes can be undone by pressing Cancel
		SessionDialog sessionDialog = View.getSingleton().getSessionDialog();
		sessionDialog.recreateUISharedContexts(Model.getSingleton().getSession());
		Context uiSharedContext = sessionDialog.getUISharedContext(this.contextId);

		uiSharedContext.getAuthenticationMethod().setLoggedOutIndicatorPattern(
				Pattern.quote(getSelectedText()));

		// Show the session dialog without recreating UI Shared contexts
		View.getSingleton().showSessionDialog(Model.getSingleton().getSession(),
				ContextAuthenticationPanel.buildName(this.contextId), false);
	}

	@Override
	public boolean isSubMenu() {
		return true;
	}

	@Override
	public String getParentMenuName() {
		return Constant.messages.getString("context.flag.popup");
	}

	@Override
	public int getParentMenuIndex() {
		return CONTEXT_FLAG_MENU_INDEX;
	}

	@Override
	public boolean isEnableForComponent(Component invoker) {
		if (invoker instanceof JTextComponent) {
			// Is it the HttpPanelResponse?
			JTextComponent txtComponent = (JTextComponent) invoker;
			boolean responsePanel = (SwingUtilities.getAncestorOfClass(HttpPanelResponse.class, txtComponent) != null);

			if (!responsePanel) {
				selectedText = null;
				return false;
			}

			// Is anything selected?
			selectedText = txtComponent.getSelectedText();
			if (selectedText == null || selectedText.length() == 0) {
				this.setEnabled(false);
			} else {
				this.setEnabled(true);
			}

			return true;
		} else {
			selectedText = null;
			return false;
		}

	}

	public String getSelectedText() {
		return selectedText;
	}
}
