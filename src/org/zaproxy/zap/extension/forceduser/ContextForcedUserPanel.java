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
package org.zaproxy.zap.extension.forceduser;

import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.widgets.ContextPanelUsersSelectComboBox;

public class ContextForcedUserPanel extends AbstractContextPropertiesPanel {

	private static final long serialVersionUID = -6668491574669367809L;

	/** The Constant PANEL NAME. */
	private static final String PANEL_NAME = Constant.messages.getString("forceduser.panel.title");

	private ExtensionForcedUser extension;

	private ContextPanelUsersSelectComboBox usersComboBox;

	public ContextForcedUserPanel(ExtensionForcedUser extensionForcedUser, int contextId) {
		super(contextId);
		this.extension = extensionForcedUser;
		initialize();
	}

	/**
	 * Initialize the panel.
	 */
	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(getContextIndex() + ": " + PANEL_NAME);
		this.setLayout(new GridBagLayout());
		this.setBorder(new EmptyBorder(2, 2, 2, 2));

		this.add(new JLabel("<html><p>" + Constant.messages.getString("forceduser.panel.label.description")
				+ "</p></html>"), LayoutHelper.getGBC(0, 0, 1, 1.0D));

		// Forced User combo box
		this.add(getUsersComboBox(), LayoutHelper.getGBC(0, 2, 1, 1.0D, new Insets(5, 0, 0, 0)));

		// Padding
		this.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
	}

	private ContextPanelUsersSelectComboBox getUsersComboBox() {
		if (usersComboBox == null) {
			usersComboBox = new ContextPanelUsersSelectComboBox(getContextIndex());
		}
		return usersComboBox;
	}

	@Override
	public void initContextData(Session session, Context uiSharedContext) {
		usersComboBox.setSelectedInternalItem(extension.getForcedUser(getContextIndex()));
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// Nothing to validate
	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		// Nothing to save in the context
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		extension.setForcedUser(getContextIndex(), (User) getUsersComboBox().getSelectedItem());
	}

	@Override
	public String getHelpIndex() {
		// TODO Needs to be filled
		return null;
	}
}
