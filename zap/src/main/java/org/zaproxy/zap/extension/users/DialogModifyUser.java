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
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.users;

import java.awt.Dialog;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.users.User;

public class DialogModifyUser extends DialogAddUser {

    private static final long serialVersionUID = 7828871270310672334L;
    private static final String DIALOG_TITLE =
            Constant.messages.getString("users.dialog.modify.title");

    public DialogModifyUser(Dialog owner, ExtensionUserManagement extension) {
        super(owner, extension, DIALOG_TITLE);
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return Constant.messages.getString("users.dialog.modify.button.confirm");
    }

    @Override
    protected void init() {
        log.debug("Initializing modify user dialog for: {}", user);

        getNameTextField().setText(user.getName());
        getEnabledCheckBox().setSelected(user.isEnabled());

        if (this.workingContext == null)
            throw new IllegalStateException(
                    "A working Context should be set before setting the 'Add Dialog' visible.");

        // Initialize the credentials that will be configured
        configuredCredentials = this.user.getAuthenticationCredentials();

        initializeCredentialsConfigPanel();
    }
}
