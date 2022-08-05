/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.view.widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.users.User;

/**
 * A {@link JTable} based widget that displays the list of {@link User Users} for a given context
 * and allows selection of multiple users.
 *
 * <p><strong>NOTE:</strong> Does not automatically refresh when the Users have changed. For this,
 * make sure you manually call {@link #reloadUsers(int)}.
 */
@SuppressWarnings("serial")
public class UsersMultiSelectTable extends JTable {

    private static final long serialVersionUID = 7473652413044348214L;
    private static ExtensionUserManagement usersExtension;
    private UsersSelectTableModel tableModel;

    /**
     * Instantiates a new multi user select table.
     *
     * @param contextId the context id
     */
    public UsersMultiSelectTable(int contextId) {
        super();
        // Force loading the UserManagement extension to make sure it's enabled.
        loadUsersManagementExtension();
        // Setup the table
        this.setTableHeader(null);
        reloadUsers(contextId);
        // Note: Adjust the column size after loading the initial model so the table has the
        // columnModel generated
        this.getColumnModel().getColumn(0).setMaxWidth(40);
    }

    private static void loadUsersManagementExtension() {
        if (usersExtension == null) {
            usersExtension =
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionUserManagement.class);
            if (usersExtension == null)
                throw new IllegalStateException(
                        "Trying to create MultiUserSelectBox without the ExtensionUsersManagement"
                                + " being enabled.");
        }
    }

    /**
     * Reloads/refreshes the list of {@link User users} associated to the context.
     *
     * @param contextId the ID of the context
     */
    public void reloadUsers(int contextId) {
        List<User> users =
                new ArrayList<>(usersExtension.getContextUserAuthManager(contextId).getUsers());
        tableModel = new UsersSelectTableModel(users);
        this.setModel(tableModel);
    }

    /**
     * Gets the selected users. This method will generates and returns a new {@link List} of users
     * for each call, so method callers should cache the returned value when possible.
     *
     * @return the selected users
     */
    public List<User> getSelectedUsers() {
        return tableModel.generateSelectedUsers();
    }

    /**
     * Gets the number of selected users.
     *
     * @return the selected users count
     */
    public int getSelectedUsersCount() {
        return tableModel.getSelectedUsersCount();
    }

    /**
     * Allows adding a 'custom' users besides the ones already loaded from the context. Can be used,
     * for example, to add a 'Any User' entry.
     *
     * @param user the custom user to add
     */
    public void addCustomUser(User user) {
        tableModel.addUser(user);
    }

    /** Table model used for selecting a set of users. */
    private static class UsersSelectTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -2187948264137599317L;
        private Set<Integer> selectedUsersIds;
        private List<User> users;

        public UsersSelectTableModel(List<User> users) {
            super();
            this.users = users;
            this.selectedUsersIds = new HashSet<>(this.users.size());
        }

        public List<User> generateSelectedUsers() {
            List<User> selectedUsers = new ArrayList<>(selectedUsersIds.size());
            for (User u : users) if (selectedUsersIds.contains(u.getId())) selectedUsers.add(u);
            return selectedUsers;
        }

        public int getSelectedUsersCount() {
            return selectedUsersIds.size();
        }

        public void addUser(User u) {
            this.users.add(u);
            fireTableRowsInserted(this.users.size() - 1, this.users.size() - 1);
        }

        @Override
        public int getRowCount() {
            return users.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return selectedUsersIds.contains(users.get(rowIndex).getId());
                case 1:
                    return users.get(rowIndex).getName();
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                    return String.class;
                default:
                    return super.getColumnClass(columnIndex);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Just the enable/disable
            return (columnIndex == 0);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                if (aValue instanceof Boolean) {
                    if ((Boolean) aValue) selectedUsersIds.add(users.get(rowIndex).getId());
                    else selectedUsersIds.remove(users.get(rowIndex).getId());
                    fireTableCellUpdated(rowIndex, columnIndex);
                }
            }
        }
    }
}
