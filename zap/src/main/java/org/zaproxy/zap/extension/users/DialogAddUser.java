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
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.authentication.AbstractCredentialsOptionsPanel;
import org.zaproxy.zap.authentication.AuthenticationCredentials;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;
import org.zaproxy.zap.view.LayoutHelper;

/** The Dialog for adding and configuring a new {@link User}. */
@SuppressWarnings("serial")
public class DialogAddUser extends AbstractFormDialog {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -7210879426146833234L;

    /** The Constant logger. */
    protected final Logger log = LogManager.getLogger(getClass());

    private static final String DIALOG_TITLE =
            Constant.messages.getString("users.dialog.add.title");
    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString("users.dialog.add.button.confirm");

    private JPanel fieldsPanel;
    private AbstractCredentialsOptionsPanel<?> credentialsPanel;
    private ZapTextField nameTextField;
    protected AuthenticationCredentials configuredCredentials;
    private JCheckBox enabledCheckBox;
    protected Context workingContext;
    protected User user;

    /**
     * Instantiates a new dialog add user.
     *
     * @param owner the owner
     * @param extension the extension
     */
    public DialogAddUser(Dialog owner, ExtensionUserManagement extension) {
        super(owner, DIALOG_TITLE);
    }

    /**
     * Instantiates a new dialog add user.
     *
     * @param owner the owner
     * @param extension the extension
     * @param title the title
     */
    public DialogAddUser(Dialog owner, ExtensionUserManagement extension, String title) {
        super(owner, title);
    }

    /**
     * Sets the context on which the Dialog is working.
     *
     * @param context the new working context
     */
    public void setWorkingContext(Context context) {
        this.workingContext = context;
    }

    @Override
    protected void init() {
        if (this.workingContext == null)
            throw new IllegalStateException(
                    "A working Context should be set before setting the 'Add Dialog' visible.");

        getEnabledCheckBox().setSelected(true);

        // Initialize the credentials that will be configured
        configuredCredentials =
                workingContext.getAuthenticationMethod().createAuthenticationCredentials();
        initializeCredentialsConfigPanel();
    }

    /** Initialize credentials config panel. */
    protected void initializeCredentialsConfigPanel() {
        // Initialize the credentials config panel
        AuthenticationMethodType type = workingContext.getAuthenticationMethod().getType();
        if (type.hasCredentialsOptionsPanel()) {
            credentialsPanel =
                    type.buildCredentialsOptionsPanel(configuredCredentials, workingContext);
            fieldsPanel.add(
                    credentialsPanel, LayoutHelper.getGBC(0, 3, 2, 1, new Insets(4, 8, 2, 4)));
            fieldsPanel.revalidate();
            this.pack();
        }
    }

    public void clear() {
        this.user = null;
        this.workingContext = null;
        // Remove previous config panel
        if (credentialsPanel != null) getFieldsPanel().remove(credentialsPanel);
    }

    @Override
    protected boolean validateFields() {
        return credentialsPanel.validateFields();
    }

    @Override
    protected void performAction() {
        if (this.user != null)
            this.user =
                    new User(
                            workingContext.getId(),
                            getNameTextField().getText(),
                            this.user.getId());
        else this.user = new User(workingContext.getId(), getNameTextField().getText());
        this.user.setEnabled(getEnabledCheckBox().isSelected());
        // Make sure the credentials panel saves its changes first
        credentialsPanel.saveCredentials();
        this.user.setAuthenticationCredentials(credentialsPanel.getCredentials());
    }

    @Override
    protected void clearFields() {
        this.nameTextField.setText("");
        this.enabledCheckBox.setSelected(true);
        this.setConfirmButtonEnabled(false);
    }

    /**
     * Gets the user defined in the dialog, if any.
     *
     * @return the user, if correctly built or null, otherwise
     */
    public User getUser() {
        return user;
    }

    @Override
    protected JPanel getFieldsPanel() {
        if (fieldsPanel == null) {
            fieldsPanel = new JPanel();

            fieldsPanel.setLayout(new GridBagLayout());
            fieldsPanel.setName("DialogAddUser");
            Insets insets = new Insets(4, 8, 2, 4);

            JLabel nameLabel =
                    new JLabel(Constant.messages.getString("users.dialog.add.field.label.name"));
            JLabel enabledLabel =
                    new JLabel(Constant.messages.getString("users.dialog.add.field.label.enabled"));

            fieldsPanel.add(nameLabel, LayoutHelper.getGBC(0, 0, 1, 0.5D, insets));
            fieldsPanel.add(getNameTextField(), LayoutHelper.getGBC(1, 0, 1, 0.5D, insets));

            fieldsPanel.add(enabledLabel, LayoutHelper.getGBC(0, 1, 1, 0.5D, insets));
            fieldsPanel.add(getEnabledCheckBox(), LayoutHelper.getGBC(1, 1, 1, 0.5D, insets));

            fieldsPanel.add(new JSeparator(), LayoutHelper.getGBC(0, 2, 2, 0.5D, insets));
        }
        return fieldsPanel;
    }

    protected ZapTextField getNameTextField() {
        if (nameTextField == null) {
            nameTextField = new ZapTextField(25);
            nameTextField
                    .getDocument()
                    .addDocumentListener(
                            new DocumentListener() {

                                @Override
                                public void removeUpdate(DocumentEvent e) {
                                    checkValidAndEnableConfirmButton();
                                }

                                @Override
                                public void insertUpdate(DocumentEvent e) {
                                    checkValidAndEnableConfirmButton();
                                }

                                @Override
                                public void changedUpdate(DocumentEvent e) {
                                    checkValidAndEnableConfirmButton();
                                }
                            });
        }

        return nameTextField;
    }

    protected JCheckBox getEnabledCheckBox() {
        if (enabledCheckBox == null) {
            enabledCheckBox = new JCheckBox();
        }

        return enabledCheckBox;
    }

    /**
     * Checks if the fields of the dialog are valid and enable confirm button, if everything is
     * valid.
     */
    private void checkValidAndEnableConfirmButton() {
        setConfirmButtonEnabled(getNameTextField().getDocument().getLength() > 0);
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }
}
