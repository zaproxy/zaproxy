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

import javax.swing.JComboBox;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.extension.users.UsersTableModel;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.renderer.UserListCellRenderer;

/**
 * A ComboBox widget that displays the list of {@link User Users} based on the list of users that is
 * currently being edited in Users Context Panel. This class should be used when displaying a
 * combo-box for selecting a user in a Context Panel, as it uses the right list of Users: the latest
 * list of Users as being defined in the Users Panel.
 *
 * @see #unload()
 */
public class ContextPanelUsersSelectComboBox extends JComboBox<User> {

    private static final long serialVersionUID = 7254245073685076020L;
    private static ExtensionUserManagement usersExtension;

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

    private final UsersListModel usersListModel;

    /**
     * Instantiates a new user select combo box.
     *
     * @param contextId the context id
     */
    @SuppressWarnings("unchecked")
    public ContextPanelUsersSelectComboBox(int contextId) {
        super();
        // Force loading the UserManagement extension to make sure it's enabled.
        loadUsersManagementExtension();
        UsersTableModel usersTableModel = usersExtension.getUIConfiguredUsersModel(contextId);
        this.usersListModel = new UsersListModel(usersTableModel);
        this.setModel(usersListModel);
        this.setRenderer(new UserListCellRenderer());
    }

    /**
     * Unloads the combo box model.
     *
     * <p>This method should be called once the combo box is no longer needed, to detach it from
     * core (persistent) classes.
     *
     * @since 2.7.0
     */
    public void unload() {
        this.usersListModel.unload();
    }

    /**
     * Performs the same as {@link #getSelectedItem()}, but adds a convenience cast.
     *
     * @return the selected user, or {@code null} if none
     */
    public User getSelectedUser() {
        return (User) getSelectedItem();
    }

    /**
     * Allows adding 'custom' users besides the ones already loaded from the context. Can be used,
     * for example, to add a 'Any User' entry or 'Unauthenticated' entry.
     *
     * @param customUsers the custom users, should not be {@code null}
     */
    public void setCustomUsers(User[] customUsers) {
        ((UsersListModel) getModel()).setCustomUsers(customUsers);
    }

    /**
     * Sets the selected item as the actual internal item with the same id as the provided user.
     *
     * @param user the new selected internal item
     */
    public void setSelectedInternalItem(User user) {
        ((UsersListModel) getModel()).setSelectedInternalItem(user);
    }
}
