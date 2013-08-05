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
package org.zaproxy.zap.extension.userauth;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

public class ContextUsersPanel extends AbstractParamPanel {

	private int contextId;
	private UsersMultipleOptionsPanel usersOptionsPanel;
	private ContextUserAuthManager contextManager;
	private ExtensionUserManagement extension;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3920598166129639573L;
	private static final String PANEL_NAME = Constant.messages.getString("userauth.user.panel.title");

	public ContextUsersPanel(ExtensionUserManagement extension, int contextId) {
		super();
		this.contextId = contextId;
		this.contextManager = extension.getContextUserAuthManager(contextId);
		this.extension = extension;
		initialize();
	}

	public static String getPanelName(int contextId) {
		// Panel names have to be unique, so prefix with the context id
		return contextId + ": " + PANEL_NAME;
	}

	private void initialize() {
		this.setLayout(new CardLayout());
		this.setName(getPanelName(this.contextId));
		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.BOTH;

		JLabel tokenNamesLabel = new JLabel();
		tokenNamesLabel.setText(Constant.messages.getString("httpsessions.options.label.tokens"));

		this.add(tokenNamesLabel, gbc);

		usersOptionsPanel = new UsersMultipleOptionsPanel(this.extension,
				this.contextManager.getUsersModel(), contextId);

		gbc.weighty = 1.0;
		this.add(usersOptionsPanel, gbc);
	}

	@Override
	public void initParam(Object obj) {
		// TODO Auto-generated method stub

	}

	@Override
	public void validateParam(Object obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveParam(Object obj) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHelpIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	protected int getContextId() {
		return contextId;
	}

	private static class UsersMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<User> {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7216673905642941770L;
		private static final String REMOVE_DIALOG_TITLE = Constant.messages
				.getString("userauth.user.dialog.remove.title");
		private static final String REMOVE_DIALOG_TEXT = Constant.messages
				.getString("userauth.user.dialog.remove.text");

		private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages
				.getString("userauth.user.dialog.remove.button.confirm");
		private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages
				.getString("userauth.user.dialog.remove.button.cancel");

		private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages
				.getString("userauth.user.dialog.remove.checkbox.label");

		private DialogAddUser addDialog = null;
		private DialogModifyUser modifyDialog = null;
		private ExtensionUserManagement extension;
		private int contextId;

		public UsersMultipleOptionsPanel(ExtensionUserManagement extension, UserAuthUserTableModel model,
				int contextId) {
			super(model);
			this.contextId = contextId;
			this.extension = extension;

			getTable().getColumnExt(0).setPreferredWidth(20);
			getTable().setSortOrder(1, SortOrder.ASCENDING);
		}

		@Override
		public User showAddDialogue() {
			if (addDialog == null) {
				addDialog = new DialogAddUser(View.getSingleton().getOptionsDialog(null), this.extension,
						contextId);
				addDialog.pack();
			}
			// addDialog.setTokens(model.getElements());
			addDialog.setVisible(true);

			User user = addDialog.getUser();
			addDialog.clear();

			return user;
		}

		@Override
		public User showModifyDialogue(User user) {
			if (modifyDialog == null) {
				modifyDialog = new DialogModifyUser(View.getSingleton().getOptionsDialog(null),
						this.extension, contextId);
				modifyDialog.pack();
			}
			// addDialog.setTokens(model.getElements());
			modifyDialog.setUser(user);
			modifyDialog.setVisible(true);

			user = modifyDialog.getUser();
			modifyDialog.clear();

			return user;
		}

		@Override
		public boolean showRemoveDialogue(User e) {
			JCheckBox removeWithoutConfirmationCheckBox = new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
			Object[] messages = { REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox };
			int option = JOptionPane.showOptionDialog(View.getSingleton().getMainFrame(), messages,
					REMOVE_DIALOG_TITLE, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
					new String[] { REMOVE_DIALOG_CONFIRM_BUTTON_LABEL, REMOVE_DIALOG_CANCEL_BUTTON_LABEL },
					null);

			if (option == JOptionPane.OK_OPTION) {
				setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());
				return true;
			}

			return false;
		}

	}
}
