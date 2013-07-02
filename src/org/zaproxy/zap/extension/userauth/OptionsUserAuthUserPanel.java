package org.zaproxy.zap.extension.userauth;

import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.SortOrder;
import javax.swing.table.TableModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.httpsessions.HttpSessionToken;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.utils.Enableable;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

public class OptionsUserAuthUserPanel extends AbstractParamPanel {

	private int contextId;
	private UsersMultipleOptionsPanel usersOptionsPanel;
	private UserAuthUserTableModel usersTableModel;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3920598166129639573L;
	private static final String PANEL_NAME = Constant.messages.getString("userauth.user.panel.title");

	public OptionsUserAuthUserPanel(int contextId) {
		super();
		this.contextId = contextId;
		initialize();
	}

	public static String getPanelName(int contextId) {
		// Panel names hav to be unique, so prefix with the context id
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

		usersOptionsPanel = new UsersMultipleOptionsPanel(getUsersTableModel(), contextId);

		gbc.weighty = 1.0;
		this.add(usersOptionsPanel, gbc);
	}

	private UserAuthUserTableModel getUsersTableModel() {
		if (usersTableModel == null) {
			usersTableModel=new UserAuthUserTableModel();
		}
		return usersTableModel;
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
				.getString("httpsessions.options.dialog.token.remove.title");
		private static final String REMOVE_DIALOG_TEXT = Constant.messages
				.getString("httpsessions.options.dialog.token.remove.text");

		private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages
				.getString("httpsessions.options.dialog.token.remove.button.confirm");
		private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages
				.getString("httpsessions.options.dialog.token.remove.button.cancel");

		private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages
				.getString("httpsessions.options.dialog.token.remove.checkbox.label");

		private DialogAddUser addDialog = null;
		private int contextId;

		public UsersMultipleOptionsPanel(UserAuthUserTableModel model, int contextId) {
			super(model);
			this.contextId = contextId;

			getTable().getColumnExt(0).setPreferredWidth(20);
			getTable().setSortOrder(1, SortOrder.ASCENDING);
		}

		@Override
		public User showAddDialogue() {
			if (addDialog == null) {
				addDialog = new DialogAddUser(View.getSingleton().getOptionsDialog(null), contextId);
				addDialog.pack();
			}
			// addDialog.setTokens(model.getElements());
			addDialog.setVisible(true);

			User user = addDialog.getUser();
			addDialog.clear();

			return user;
		}

		@Override
		public User showModifyDialogue(User e) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean showRemoveDialogue(User e) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
