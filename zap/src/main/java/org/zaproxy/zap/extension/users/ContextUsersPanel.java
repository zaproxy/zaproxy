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
package org.zaproxy.zap.extension.users;

import java.awt.CardLayout;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

public class ContextUsersPanel extends AbstractContextPropertiesPanel {

	private UsersMultipleOptionsPanel usersOptionsPanel;
	private ContextUserAuthManager contextManager;
	private ExtensionUserManagement extension;
	private UsersTableModel usersTableModel;

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3920598166129639573L;
	private static final String PANEL_NAME = Constant.messages.getString("users.panel.title");

	public ContextUsersPanel(ExtensionUserManagement extension, int contextId) {
		super(contextId);
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
		this.setName(getPanelName(getContextIndex()));
		this.setLayout(new GridBagLayout());

		this.add(new JLabel(Constant.messages.getString("users.panel.description")),
				LayoutHelper.getGBC(0, 0, 1, 1.0d, 0.0d));

		usersTableModel = new UsersTableModel();
		usersOptionsPanel = new UsersMultipleOptionsPanel(this.extension, usersTableModel, getContextIndex());
		this.add(usersOptionsPanel, LayoutHelper.getGBC(0, 1, 1, 1.0d, 1.0d));
	}

	@Override
	public String getHelpIndex() {
		return "ui.dialogs.contexts";
	}

	public static class UsersMultipleOptionsPanel extends AbstractMultipleOptionsTablePanel<User> {

		private static final long serialVersionUID = -7216673905642941770L;
		private static final String REMOVE_DIALOG_TITLE = Constant.messages
				.getString("users.dialog.remove.title");
		private static final String REMOVE_DIALOG_TEXT = Constant.messages
				.getString("users.dialog.remove.text");

		private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages
				.getString("users.dialog.remove.button.confirm");
		private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages
				.getString("users.dialog.remove.button.cancel");

		private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages
				.getString("users.dialog.remove.checkbox.label");

		private DialogAddUser addDialog = null;
		private DialogModifyUser modifyDialog = null;
		private ExtensionUserManagement extension;
		private Context uiSharedContext;

		public UsersMultipleOptionsPanel(ExtensionUserManagement extension, UsersTableModel model,
				int contextId) {
			super(model);
			this.extension = extension;

			getTable().getColumnExt(0).setPreferredWidth(40);
			getTable().getColumnExt(1).setPreferredWidth(30);
			getTable().getColumnExt(1).setMaxWidth(50);
			getTable().setSortOrder(2, SortOrder.ASCENDING);
		}

		@Override
		public User showAddDialogue() {
			boolean valid = uiSharedContext.getAuthenticationMethod().validateCreationOfAuthenticationCredentials();
			if (!valid) {
				return null;
			}

			if (addDialog == null) {
				addDialog = new DialogAddUser(View.getSingleton().getOptionsDialog(null), this.extension);
				addDialog.pack();
			}
			addDialog.setWorkingContext(this.uiSharedContext);
			addDialog.setVisible(true);

			User user = addDialog.getUser();
			addDialog.clear();

			return user;
		}

		@Override
		public User showModifyDialogue(User user) {
			if (modifyDialog == null) {
				modifyDialog = new DialogModifyUser(View.getSingleton().getOptionsDialog(null),
						this.extension);
				modifyDialog.pack();
			}
			modifyDialog.setWorkingContext(this.uiSharedContext);
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

		protected void setWorkingContext(Context context) {
			this.uiSharedContext = context;
		}

	}

	@Override
	public void initContextData(Session session, Context uiCommonContext) {
		this.usersOptionsPanel.setWorkingContext(uiCommonContext);
		this.usersTableModel.setUsers(this.contextManager.getUsers());
	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// Nothing to validate
	}

	@Override
	public void saveContextData(Session session) throws Exception {
		this.contextManager.setUsers(usersTableModel.getUsers());

	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		// Data is already saved in the uiSharedContext
	}

	protected UsersTableModel getUsersTableModel() {
		return usersTableModel;
	}
}
