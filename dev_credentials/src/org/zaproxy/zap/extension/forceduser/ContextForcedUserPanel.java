package org.zaproxy.zap.extension.forceduser;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;

public class ContextForcedUserPanel extends AbstractContextPropertiesPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6668491574669367809L;

	/** The Constant PANEL NAME. */
	private static final String PANEL_NAME = Constant.messages.getString("sessionmanagement.panel.title");

	private static final Logger log = Logger.getLogger(ContextForcedUserPanel.class);

	private ExtensionForcedUser extension;

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

		this.add(new JLabel(Constant.messages.getString("sessionmanagement.panel.label.description")),
				LayoutHelper.getGBC(0, 0, 1, 1.0D));

		// Session management combo box
		this.add(new JLabel(Constant.messages.getString("sessionmanagement.panel.label.typeSelect")),
				LayoutHelper.getGBC(0, 1, 1, 1.0D, new Insets(20, 0, 5, 5)));
		this.add(getUsersComboBox(), LayoutHelper.getGBC(0, 2, 1, 1.0D));

		// Padding
		this.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
	}

	private JComboBox<User> usersComboBox;

	/**
	 * A renderer for properly displaying the name of an HttpSession in a ComboBox.
	 */
	private static class UserRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 3654541772447187317L;

		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				User item = (User) value;
				setText(item.getName());
			}
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	private JComboBox<User> getUsersComboBox() {
		if (usersComboBox == null) {
			usersComboBox = new JComboBox<>();
			usersComboBox.setRenderer(new UserRenderer());
		}
		return usersComboBox;
	}

	@Override
	public void initContextData(Session session, Context uiSharedContext) {

	}

	@Override
	public void validateContextData(Session session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveTemporaryContextData(Context uiSharedContext) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveContextData(Session session) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getHelpIndex() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onShow() {
		super.onShow();
		List<User> currentUsers = extension.getUserManagementExtension().getUIConfiguredUsers(
				getContextIndex());
		if (currentUsers != null) {
			getUsersComboBox().setModel(
					new DefaultComboBoxModel<User>(currentUsers.toArray(new User[currentUsers.size()])));
			//getUsersComboBox().getModel().setSelectedItem(extension.getForcedUser(getContextIndex()));
		}

		else
			getUsersComboBox().setModel(null);
	}

}
