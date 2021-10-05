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

import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.view.renderer.UserListCellRenderer;

/**
 * A {@link JList} widget that displays the list of {@link User Users} for a given context and
 * allows selection of multiple users.
 *
 * <p><strong>NOTE:</strong> Does not automatically refresh when the Users have changed. For this,
 * make sure you manually call {@link #reloadUsers(int)}.
 */
public class UsersMultiSelectList extends JList<User> {

    private static final long serialVersionUID = 7473652413044348214L;
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

    /**
     * Instantiates a new multi user select list.
     *
     * @param contextId the context id
     * @param selectionModel the selection model, as in {@link ListSelectionModel} constants
     */
    @SuppressWarnings("unchecked")
    public UsersMultiSelectList(int contextId, int selectionModel) {
        super();
        // Force loading the UserManagement extension to make sure it's enabled.
        loadUsersManagementExtension();
        reloadUsers(contextId);
        this.setSelectionMode(selectionModel);
        this.setCellRenderer(new UserListCellRenderer());
    }

    /**
     * Reloads/refreshes the list of {@link User users} associated to the context.
     *
     * @param contextId the ID of the context
     */
    public void reloadUsers(int contextId) {
        List<User> users = usersExtension.getContextUserAuthManager(contextId).getUsers();
        User[] usersArray = users.toArray(new User[users.size()]);
        ListModel<User> usersModel = new DefaultComboBoxModel<>(usersArray);
        this.setModel(usersModel);
    }
}
