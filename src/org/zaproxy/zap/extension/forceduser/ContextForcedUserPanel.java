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
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.apache.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.extension.userauth.UsersTableModel;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.userauth.User;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;

public class ContextForcedUserPanel extends AbstractContextPropertiesPanel {

	private static final long serialVersionUID = -6668491574669367809L;

	/** The Constant PANEL NAME. */
	private static final String PANEL_NAME = Constant.messages.getString("forceduser.panel.title");

	private static final Logger log = Logger.getLogger(ContextForcedUserPanel.class);

	private ExtensionForcedUser extension;

	private JComboBox<User> usersComboBox;

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
		UsersTableModel currentUsers = extension.getUserManagementExtension().getUIConfiguredUsersModel(
				getContextIndex());
		if (currentUsers != null) {
			UsersListModel usersModel = new UsersListModel(currentUsers);
			usersModel.setSelectedInternalItem(extension.getForcedUser(getContextIndex()));
			getUsersComboBox().setModel(usersModel);
		} else {
			getUsersComboBox().setModel(new DefaultComboBoxModel<User>());
			log.error("Current users not obtained properly. Disabling Forced Users panel for context: "
					+ getContextIndex());
		}
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
		// TODO Auto-generated method stub
		return null;
	}

	public static class UsersListModel extends AbstractListModel<User> implements ComboBoxModel<User>,
			TableModelListener {

		private static final long serialVersionUID = 5648260449088479312L;
		Object selectedItem;
		UsersTableModel tableModel;

		public UsersListModel(UsersTableModel tableModel) {
			super();
			this.tableModel = tableModel;
			this.tableModel.addTableModelListener(this);
		}

		@Override
		public User getElementAt(int index) {
			return tableModel.getElement(index);
		}

		@Override
		public int getSize() {
			return tableModel.getUsers().size();
		}

		@Override
		public void tableChanged(TableModelEvent e) {
			// Handle the situation when nothing selected or the event is a deletion and the
			// previously selected item does not exist any more
			if ((selectedItem == null || (e.getType() == TableModelEvent.DELETE && getIndexOf(selectedItem) == -1)))
				if (getSize() > 0)
					setSelectedItem(getElementAt(0));
				else
					setSelectedItem(null);
			fireContentsChanged(this, e.getFirstRow(), e.getLastRow());
		}

		@Override
		public Object getSelectedItem() {
			return selectedItem;
		}

		@Override
		public void setSelectedItem(Object anItem) {
			// No item is selected and object is null, so no change required.
			if (selectedItem == null && anItem == null)
				return;

			// object is already selected so no change required.
			if (selectedItem != null && selectedItem.equals(anItem))
				return;

			// Simply return if object is not in the list.
			if (anItem != null && getIndexOf(anItem) == -1)
				return;

			// Here we know that object is either an item in the list or null.
			selectedItem = anItem;
			fireContentsChanged(this, -1, -1);
		}

		/**
		 * Sets the selected item as the actual internal item with the same id as the provided user.
		 * 
		 * @param user the new selected internal item
		 */
		public void setSelectedInternalItem(User user) {
			int index = getIndexOf(user);
			if (index != -1)
				setSelectedItem(tableModel.getUsers().get(index));
			else if (getSize() > 0)
				setSelectedItem(getElementAt(0));
			else
				setSelectedItem(null);

		}

		/**
		 * Returns the index of the specified element in the model's item list.
		 * 
		 * @param object the element.
		 * @return The index of the specified element in the model's item list.
		 */
		public int getIndexOf(Object object) {
			return tableModel.getUsers().indexOf(object);
		}
	}

	/**
	 * A renderer for properly displaying the name of an HttpSession in a ComboBox.
	 */
	private static class UserRenderer extends BasicComboBoxRenderer {
		private static final long serialVersionUID = 3654541772447187317L;

		private static final Border BORDER = new EmptyBorder(2, 3, 3, 3);

		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value != null) {
				User item = (User) value;
				setText(item.getName());
				setBorder(BORDER);
				setEnabled(item.isEnabled());
			}
			return this;
		}
	}
}
