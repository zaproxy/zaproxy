/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.apache.commons.lang.ArrayUtils;
import org.zaproxy.zap.extension.users.UsersTableModel;
import org.zaproxy.zap.users.User;

@SuppressWarnings("serial")
class UsersListModel extends AbstractListModel<User> implements ComboBoxModel<User> {

    private static final long serialVersionUID = 5648260449088479312L;
    private final TableModelListenerImpl tableModelListenerImpl;
    private User selectedItem;
    private UsersTableModel tableModel;
    private User[] customUsers;

    UsersListModel(UsersTableModel tableModel) {
        super();
        this.tableModel = tableModel;
        this.tableModelListenerImpl = new TableModelListenerImpl();
        this.tableModel.addTableModelListener(tableModelListenerImpl);
    }

    /** Unloads this instance, by removing the listener previously added to the table model. */
    public void unload() {
        this.tableModel.removeTableModelListener(tableModelListenerImpl);
    }

    @Override
    public User getElementAt(int index) {
        if (index < tableModel.getRowCount()) return tableModel.getElement(index);
        else if (customUsers != null) return customUsers[index - tableModel.getRowCount()];
        return null;
    }

    @Override
    public int getSize() {
        return tableModel.getUsers().size() + (customUsers == null ? 0 : customUsers.length);
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem == null) {
            if (selectedItem == null) {
                // No item is selected and object is null, so no change required.
                return;
            }
            // Item selected and object is null, just remove selection.
            setSelectedItemImpl(null);
            return;
        }

        // Wrong class element selected
        if (!(anItem instanceof User)) return;

        // object is already selected so no change required.
        if (selectedItem != null && selectedItem.equals(anItem)) return;

        // Simply return if object is not in the list.
        if (getIndexOf(anItem) == -1) return;

        // Here we know that object is an item in the list.
        setSelectedItemImpl((User) anItem);
    }

    /**
     * Sets the given {@code user} as selected, bypassing any validations done by {@code
     * setSelectedItem(Object)} (for example, if the user exists in the combo box or if it's already
     * selected).
     *
     * @param user the user that should be selected, or {@code null} for none
     * @see #setSelectedItem(Object)
     */
    private void setSelectedItemImpl(User user) {
        selectedItem = user;
        fireContentsChanged(this, -1, -1);
    }

    /**
     * Sets the selected item as the actual internal item with the same id as the provided user.
     *
     * @param user the new selected internal item
     */
    public void setSelectedInternalItem(User user) {
        User internalUser = null;
        int index = getIndexOf(user);
        if (index != -1) {
            internalUser = getElementAt(index);
        } else if (getSize() > 0) {
            internalUser = getElementAt(0);
        }

        setSelectedItemImpl(internalUser);
    }

    /**
     * Returns the index of the specified element in the model's item list.
     *
     * @param object the element.
     * @return The index of the specified element in the model's item list, or -1 if it wasn't found
     */
    public int getIndexOf(Object object) {
        int index = tableModel.getUsers().indexOf(object);
        if (index < 0 && customUsers != null)
            return tableModel.getUsers().size() + ArrayUtils.indexOf(customUsers, object);
        return index;
    }

    public void setCustomUsers(User[] users) {
        User[] oldCustomUsers = customUsers;
        int tableUsersSize = tableModel.getUsers().size();
        customUsers = users;
        if (oldCustomUsers == null || oldCustomUsers.length == 0) {
            if (users != null && users.length != 0) {
                fireIntervalAdded(this, tableUsersSize, tableUsersSize + customUsers.length - 1);
            }
        } else {
            if (users == null || users.length == 0) {
                fireIntervalRemoved(
                        this, tableUsersSize, tableUsersSize + oldCustomUsers.length - 1);
            } else {
                fireContentsChanged(
                        this,
                        tableUsersSize,
                        tableUsersSize + Math.max(customUsers.length, oldCustomUsers.length) - 1);
            }
        }
    }

    private class TableModelListenerImpl implements TableModelListener {

        @Override
        public void tableChanged(TableModelEvent e) {
            switch (e.getType()) {
                case TableModelEvent.INSERT:
                    if (selectedItem == null) {
                        selectFirstUser();
                    }
                    fireIntervalAdded(this, e.getFirstRow(), e.getLastRow());
                    break;
                case TableModelEvent.UPDATE:
                    if (selectedItem != null) {
                        int idx = getIndexOf(selectedItem);
                        if (idx == -1) {
                            selectionFallback();
                        } else {
                            User user = getElementAt(idx);
                            if (user != selectedItem) {
                                // Same user (by ID) but different objects, refresh it.
                                setSelectedItemImpl(user);
                            }
                        }
                    } else {
                        selectionFallback();
                    }

                    fireContentsChanged(this, e.getFirstRow(), e.getLastRow());
                    break;
                case TableModelEvent.DELETE:
                    if (selectedItem != null && getIndexOf(selectedItem) == -1) {
                        selectionFallback();
                    }
                    fireIntervalRemoved(this, e.getFirstRow(), e.getLastRow());
                    break;
                default:
                    // Nothing to do.
            }
        }

        private void selectFirstUser() {
            setSelectedItemImpl(getElementAt(0));
        }

        private void selectionFallback() {
            if (getSize() != 0) {
                selectFirstUser();
            } else if (selectedItem != null) {
                setSelectedItemImpl(null);
            }
        }
    }
}
