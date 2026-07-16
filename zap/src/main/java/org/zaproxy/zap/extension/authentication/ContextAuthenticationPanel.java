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
package org.zaproxy.zap.extension.authentication;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.authentication.AbstractAuthenticationMethodOptionsPanel;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.authentication.VerificationMethod;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;

/** The Context Panel shown for configuring a Context's authentication methods. */
@SuppressWarnings("serial")
public class ContextAuthenticationPanel extends AbstractContextPropertiesPanel {

    private static final Logger LOGGER = LogManager.getLogger(ContextAuthenticationPanel.class);
    private static final long serialVersionUID = -898084998156067286L;

    /** The Constant PANEL NAME. */
    private static final String PANEL_NAME =
            Constant.messages.getString("authentication.panel.title");

    private static final String FIELD_LABEL_TYPE_SELECT =
            Constant.messages.getString("authentication.panel.label.typeSelect");
    private static final String LABEL_DESCRIPTION =
            Constant.messages.getString("authentication.panel.label.description");
    private static final String PANEL_TITLE_CONFIG =
            Constant.messages.getString("authentication.panel.label.configTitle");
    private static final String LABEL_CONFIG_NOT_NEEDED =
            Constant.messages.getString("sessionmanagement.panel.label.noConfigPanel");

    /** The extension. */
    private ExtensionAuthentication extension;

    /** The verification panel — owns all verification UI fields. */
    private ContextVerificationPanel verificationPanel;

    /** The authentication method types combo box. */
    private JComboBox<AuthenticationMethodType> authenticationMethodsComboBox;

    /** The selected authentication method. */
    private AuthenticationMethod selectedAuthenticationMethod;

    /** The shown method type. */
    private AuthenticationMethodType shownMethodType;

    /** The shown config panel. */
    private AbstractAuthenticationMethodOptionsPanel shownConfigPanel;

    /** The container panel for the authentication method's configuration. */
    private JPanel configContainerPanel;

    /** Hacked used to make sure a confirmation is not needed if changes where done during init. */
    private boolean needsConfirm = true;

    /**
     * Instantiates a new context authentication configuration panel.
     *
     * @param extension the extension
     * @param context the context
     * @param verificationPanel the sibling verification panel that owns indicator fields
     */
    public ContextAuthenticationPanel(
            ExtensionAuthentication extension,
            Context context,
            ContextVerificationPanel verificationPanel) {
        super(context.getId());
        this.extension = extension;
        this.verificationPanel = verificationPanel;
        initialize();
    }

    /**
     * @deprecated Use {@link #ContextAuthenticationPanel(ExtensionAuthentication, Context,
     *     ContextVerificationPanel)} instead.
     */
    @Deprecated
    public ContextAuthenticationPanel(ExtensionAuthentication extension, Context context) {
        this(extension, context, new ContextVerificationPanel(context));
    }

    public static String buildName(int contextId) {
        return contextId + ": " + PANEL_NAME;
    }

    /** Initialize the panel. */
    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(buildName(getContextId()));
        this.setBorder(new EmptyBorder(2, 2, 2, 2));
        this.setLayout(new CardLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        // Only known way to minimise the horizontal space taken up
        // needs to be big enough to cope with Form based auth panel
        panel.setPreferredSize(new Dimension(400, 600));

        JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanel.setViewportView(panel);
        this.add(scrollPanel);

        panel.add(new ZapHtmlLabel(LABEL_DESCRIPTION), LayoutHelper.getGBC(0, 0, 1, 1.0D));

        // Method type combo box
        panel.add(
                new JLabel(FIELD_LABEL_TYPE_SELECT),
                LayoutHelper.getGBC(0, 1, 1, 1.0D, new Insets(20, 0, 5, 5)));
        panel.add(getAuthenticationMethodsComboBox(), LayoutHelper.getGBC(0, 2, 1, 1.0D));

        // Method config panel container
        panel.add(
                getConfigContainerPanel(),
                LayoutHelper.getGBC(0, 3, 1, 1.0d, new Insets(10, 0, 10, 0)));

        // Padding
        panel.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
    }

    /**
     * Changes the shown method's configuration panel with a new one based on a new method type.
     *
     * @param newMethodType the new method type; if null, nothing is shown
     */
    private void changeMethodConfigPanel(AuthenticationMethodType newMethodType) {
        if (newMethodType == null) {
            getConfigContainerPanel().removeAll();
            getConfigContainerPanel().setVisible(false);
            this.shownMethodType = null;
            return;
        }

        if (shownMethodType != null
                && newMethodType.getClass().equals(shownMethodType.getClass())) {
            return;
        }

        LOGGER.debug("Creating new panel for configuring: {}", newMethodType.getName());

        this.getConfigContainerPanel().removeAll();

        if (newMethodType.hasOptionsPanel()) {
            shownConfigPanel = newMethodType.buildOptionsPanel(getUISharedContext());
            getConfigContainerPanel().add(shownConfigPanel, BorderLayout.CENTER);
        } else {
            shownConfigPanel = null;
            getConfigContainerPanel()
                    .add(new ZapHtmlLabel(LABEL_CONFIG_NOT_NEEDED), BorderLayout.CENTER);
        }
        this.shownMethodType = newMethodType;

        this.getConfigContainerPanel().setVisible(true);
        this.getConfigContainerPanel().revalidate();
    }

    /**
     * Gets the authentication method types combo box.
     *
     * @return the authentication methods combo box
     */
    protected JComboBox<AuthenticationMethodType> getAuthenticationMethodsComboBox() {
        if (authenticationMethodsComboBox == null) {
            Vector<AuthenticationMethodType> methods =
                    new Vector<>(extension.getAuthenticationMethodTypes());
            authenticationMethodsComboBox = new JComboBox<>(methods);
            authenticationMethodsComboBox.setSelectedItem(null);

            authenticationMethodsComboBox.addItemListener(
                    new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED
                                    && !e.getItem().equals(shownMethodType)) {
                                LOGGER.debug("Selected new Authentication type: {}", e.getItem());

                                AuthenticationMethodType type =
                                        ((AuthenticationMethodType) e.getItem());
                                if (shownMethodType == null
                                        || type.getAuthenticationCredentialsType()
                                                != shownMethodType
                                                        .getAuthenticationCredentialsType()) {

                                    if (needsConfirm && !confirmAndResetUsersCredentials(type)) {
                                        LOGGER.debug("Cancelled change of authentication type.");

                                        authenticationMethodsComboBox.setSelectedItem(
                                                shownMethodType);
                                        return;
                                    }
                                }
                                if (selectedAuthenticationMethod == null
                                        || !type.isTypeForMethod(selectedAuthenticationMethod)) {
                                    selectedAuthenticationMethod =
                                            type.createAuthenticationMethod(getContextId());
                                    // Copy current verification state into the new auth method's VM
                                    VerificationMethod vmCopy =
                                            verificationPanel.buildVerificationMethodFromUI();
                                    vmCopy.setUserDataReplacer(
                                            selectedAuthenticationMethod
                                                    ::replaceUserDataInPollRequest);
                                    selectedAuthenticationMethod.setVerificationMethod(vmCopy);
                                }

                                changeMethodConfigPanel(type);
                                if (type.hasOptionsPanel()) {
                                    shownConfigPanel.bindMethod(selectedAuthenticationMethod, null);
                                }
                            }
                        }
                    });
        }
        return authenticationMethodsComboBox;
    }

    /**
     * Make sure the user acknowledges the Users corresponding to this context will have the
     * credentials changed with the new type of authentication method.
     *
     * @param type the type of authentication method being set.
     * @return true, if successful
     */
    private boolean confirmAndResetUsersCredentials(AuthenticationMethodType type) {
        ExtensionUserManagement usersExtension =
                Control.getSingleton()
                        .getExtensionLoader()
                        .getExtension(ExtensionUserManagement.class);
        if (usersExtension == null) {
            return true;
        }
        List<User> users = usersExtension.getSharedContextUsers(getUISharedContext());
        if (users.isEmpty()) {
            return true;
        }
        if (users.stream().anyMatch(user -> user.getAuthenticationCredentials().isConfigured())) {
            authenticationMethodsComboBox.transferFocus();
            int choice =
                    JOptionPane.showConfirmDialog(
                            this,
                            Constant.messages.getString(
                                    "authentication.dialog.confirmChange.label"),
                            Constant.messages.getString(
                                    "authentication.dialog.confirmChange.title"),
                            JOptionPane.OK_CANCEL_OPTION);
            if (choice == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        users.replaceAll(
                user -> {
                    User modifiedUser = new User(user.getContextId(), user.getName(), user.getId());
                    modifiedUser.setEnabled(false);
                    modifiedUser.setAuthenticationCredentials(
                            type.createAuthenticationCredentials());
                    return modifiedUser;
                });
        return true;
    }

    private JPanel getConfigContainerPanel() {
        if (configContainerPanel == null) {
            configContainerPanel = new JPanel(new BorderLayout());
            configContainerPanel.setBorder(
                    javax.swing.BorderFactory.createTitledBorder(
                            null,
                            PANEL_TITLE_CONFIG,
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));
        }
        return configContainerPanel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.context-auth";
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        selectedAuthenticationMethod = uiSharedContext.getAuthenticationMethod();
        LOGGER.debug(
                "Initializing configuration panel for authentication method: {} for context {}",
                selectedAuthenticationMethod,
                uiSharedContext.getName());

        if (selectedAuthenticationMethod != null) {
            // If the proper type is already selected, just rebind the data
            if (shownMethodType != null
                    && shownMethodType.isTypeForMethod(selectedAuthenticationMethod)) {
                if (shownMethodType.hasOptionsPanel()) {
                    LOGGER.debug(
                            "Binding authentication method to existing panel of proper type for context {}",
                            uiSharedContext.getName());
                    shownConfigPanel.bindMethod(selectedAuthenticationMethod, null);
                }
                return;
            }

            // Select what needs to be selected
            for (AuthenticationMethodType type : extension.getAuthenticationMethodTypes())
                if (type.isTypeForMethod(selectedAuthenticationMethod)) {
                    LOGGER.debug(
                            "Binding authentication method to new panel of proper type for context {}",
                            uiSharedContext.getName());
                    needsConfirm = false;
                    getAuthenticationMethodsComboBox().setSelectedItem(type);
                    needsConfirm = true;
                    break;
                }
        }
    }

    @Override
    public void validateContextData(Session session) throws Exception {
        if (shownConfigPanel != null) shownConfigPanel.validateFields();
    }

    @Override
    public void saveContextData(Session session) throws Exception {
        if (shownConfigPanel != null) shownConfigPanel.saveMethod();

        Context context = session.getContext(getContextId());
        if (context.getAuthenticationMethod() != null)
            if (!shownMethodType.isTypeForMethod(context.getAuthenticationMethod()))
                context.getAuthenticationMethod().onMethodDiscarded();

        context.setAuthenticationMethod(selectedAuthenticationMethod);

        selectedAuthenticationMethod.onMethodPersisted();
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        if (shownConfigPanel != null) shownConfigPanel.saveMethod();

        // Preserve the current verification method — it is owned by the verification panel.
        // setAuthenticationMethod would replace the VM reference, so we restore it afterwards.
        VerificationMethod currentVm = uiSharedContext.getVerificationMethod();
        uiSharedContext.setAuthenticationMethod(selectedAuthenticationMethod);
        if (currentVm != null) {
            uiSharedContext.setVerificationMethod(currentVm);
        }
    }
}
