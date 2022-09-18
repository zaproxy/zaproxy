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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import org.apache.commons.httpclient.URI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.authentication.AbstractAuthenticationMethodOptionsPanel;
import org.zaproxy.zap.authentication.AuthenticationIndicatorsPanel;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.authentication.AuthenticationMethodType;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.NodeSelectDialog;

/** The Context Panel shown for configuring a Context's authentication methods. */
@SuppressWarnings("serial")
public class ContextAuthenticationPanel extends AbstractContextPropertiesPanel {

    private static final Logger log = LogManager.getLogger(ContextAuthenticationPanel.class);
    private static final long serialVersionUID = -898084998156067286L;

    /** The Constant PANEL NAME. */
    private static final String PANEL_NAME =
            Constant.messages.getString("authentication.panel.title");

    private static final String FIELD_LABEL_LOGGED_IN_INDICATOR =
            Constant.messages.getString("authentication.panel.label.loggedIn");
    private static final String FIELD_LABEL_LOGGED_OUT_INDICATOR =
            Constant.messages.getString("authentication.panel.label.loggedOut");
    private static final String FIELD_LABEL_TYPE_SELECT =
            Constant.messages.getString("authentication.panel.label.typeSelect");
    private static final String LABEL_DESCRIPTION =
            Constant.messages.getString("authentication.panel.label.description");
    private static final String PANEL_TITLE_CONFIG =
            Constant.messages.getString("authentication.panel.label.configTitle");
    private static final String PANEL_TITLE_VERIF =
            Constant.messages.getString("authentication.panel.label.verifTitle");
    private static final String LABEL_POLL_URL =
            Constant.messages.getString("authentication.panel.label.pollurl");
    private static final String LABEL_POLL_DATA =
            Constant.messages.getString("authentication.panel.label.polldata");
    private static final String LABEL_POLL_HEADERS =
            Constant.messages.getString("authentication.panel.label.pollheaders");
    private static final String LABEL_POLL_FREQUENCY =
            Constant.messages.getString("authentication.panel.label.freq");
    private static final String LABEL_CONFIG_NOT_NEEDED =
            Constant.messages.getString("sessionmanagement.panel.label.noConfigPanel");
    private static final String LABEL_STRATEGY =
            Constant.messages.getString("authentication.panel.label.strategy");
    private static final String STRATEGY_PREFIX = "authentication.panel.label.strategy.";
    private static final String FREQUENCY_UNITS_PREFIX = "authentication.panel.label.units.";

    /** The extension. */
    private ExtensionAuthentication extension;

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

    /** The container panel for the authentication verification configuration. */
    private JPanel verifContainerPanel;

    private JComboBox<AuthCheckingStrategyType> authenticationVerifComboBox;
    private JComboBox<AuthPollFrequencyUnitsType> authFrequencyUnitsComboBox;

    private JButton pollUrlSelectButton = null;
    private ZapTextField pollUrlField = null;
    private ZapTextField pollDataField = null;
    private ZapTextArea pollHeadersField = null;
    private ZapNumberSpinner pollFrequency = null;

    private ZapTextField loggedInIndicatorRegexField = null;
    private ZapTextField loggedOutIndicatorRegexField = null;

    /** Hacked used to make sure a confirmation is not needed if changes where done during init. */
    private boolean needsConfirm = true;

    private AuthenticationIndicatorsPanel authenticationIndicatorsPanel;

    /**
     * Instantiates a new context authentication configuration panel.
     *
     * @param extension the extension
     * @param context the context
     */
    public ContextAuthenticationPanel(ExtensionAuthentication extension, Context context) {
        super(context.getId());
        this.extension = extension;
        initialize();
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
        panel.setPreferredSize(new Dimension(400, 800));

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

        // Verification panel container
        panel.add(
                getVerifContainerPanel(),
                LayoutHelper.getGBC(0, 4, 1, 1.0d, new Insets(10, 0, 10, 0)));

        int y = 0;
        int fullWidth = 3;
        getVerifContainerPanel()
                .add(new JLabel(LABEL_STRATEGY), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(
                        getAuthenticationVerifComboBox(),
                        LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));

        getVerifContainerPanel()
                .add(
                        new JLabel(FIELD_LABEL_LOGGED_IN_INDICATOR),
                        LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(
                        getLoggedInIndicatorRegexField(),
                        LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(
                        new JLabel(FIELD_LABEL_LOGGED_OUT_INDICATOR),
                        LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(
                        getLoggedOutIndicatorRegexField(),
                        LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));

        getVerifContainerPanel()
                .add(new JLabel(LABEL_POLL_FREQUENCY), LayoutHelper.getGBC(0, y, 1, 0.34D));
        getVerifContainerPanel()
                .add(getPollFrequencySpinner(), LayoutHelper.getGBC(1, y, 1, 0.33D));
        getVerifContainerPanel()
                .add(getAuthFrequencyUnitsComboBox(), LayoutHelper.getGBC(2, y++, 1, 0.33D));

        getVerifContainerPanel()
                .add(new JLabel(LABEL_POLL_URL), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        JPanel urlPanel = new JPanel(new GridBagLayout());
        urlPanel.add(this.getPollUrlField(), LayoutHelper.getGBC(0, 0, 1, 1.0D));
        urlPanel.add(getPollUrlSelectButton(), LayoutHelper.getGBC(1, 0, 1, 0.0D));
        getVerifContainerPanel().add(urlPanel, LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(new JLabel(LABEL_POLL_DATA), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(this.getPollDataField(), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(new JLabel(LABEL_POLL_HEADERS), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        getVerifContainerPanel()
                .add(this.getPollHeadersField(), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));

        // Padding
        panel.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
    }

    /**
     * Changes the shown method's configuration panel (used to display brief info about the method
     * and configure it) with a new one, based on a new method type. If {@code null} is provided as
     * a parameter, nothing is shown. If the provided method type does not require configuration, a
     * simple message is shown stating that no configuration is needed.
     *
     * @param newMethodType the new method type. If null, nothing is shown.
     */
    private void changeMethodConfigPanel(AuthenticationMethodType newMethodType) {
        // If there's no new method, don't display anything
        if (newMethodType == null) {
            getConfigContainerPanel().removeAll();
            getConfigContainerPanel().setVisible(false);
            this.shownMethodType = null;
            return;
        }

        // If a panel of the correct type is already shown, do nothing
        if (shownMethodType != null
                && newMethodType.getClass().equals(shownMethodType.getClass())) {
            return;
        }

        log.debug("Creating new panel for configuring: {}", newMethodType.getName());

        this.getConfigContainerPanel().removeAll();

        // show the panel according to whether the authentication type needs configuration
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

            // Prepare the listener for the change of selection
            authenticationMethodsComboBox.addItemListener(
                    new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED
                                    && !e.getItem().equals(shownMethodType)) {
                                log.debug("Selected new Authentication type: {}", e.getItem());

                                AuthenticationMethodType type =
                                        ((AuthenticationMethodType) e.getItem());
                                if (shownMethodType == null
                                        || type.getAuthenticationCredentialsType()
                                                != shownMethodType
                                                        .getAuthenticationCredentialsType()) {

                                    if (needsConfirm && !confirmAndResetUsersCredentials(type)) {
                                        log.debug("Cancelled change of authentication type.");

                                        authenticationMethodsComboBox.setSelectedItem(
                                                shownMethodType);
                                        return;
                                    }
                                }
                                resetLoggedInOutIndicators();

                                // If no authentication method was previously selected or it's a
                                // different
                                // class, create a new authentication method object
                                if (selectedAuthenticationMethod == null
                                        || !type.isTypeForMethod(selectedAuthenticationMethod)) {
                                    selectedAuthenticationMethod =
                                            type.createAuthenticationMethod(getContextId());
                                }

                                // Show the configuration panel
                                changeMethodConfigPanel(type);
                                if (type.hasOptionsPanel()) {
                                    shownConfigPanel.bindMethod(
                                            selectedAuthenticationMethod,
                                            getAuthenticationIndicatorsPanel());
                                }
                            }
                        }
                    });
        }
        return authenticationMethodsComboBox;
    }

    private JComboBox<AuthCheckingStrategyType> getAuthenticationVerifComboBox() {
        if (authenticationVerifComboBox == null) {
            authenticationVerifComboBox = new JComboBox<>();
            for (AuthCheckingStrategyType acst : AuthCheckingStrategyType.getAllValues()) {
                authenticationVerifComboBox.addItem(acst);
            }
            // Prepare the listener for the change of selection
            authenticationVerifComboBox.addItemListener(
                    new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED
                                    && !e.getItem().equals(shownMethodType)) {

                                setPollFieldStatuses(((AuthCheckingStrategyType) e.getItem()));
                            }
                        }
                    });
        }
        return authenticationVerifComboBox;
    }

    private void setPollFieldStatuses(AuthCheckingStrategyType type) {
        boolean isPoll = type.getStrategy().equals(AuthCheckingStrategy.POLL_URL);
        getAuthFrequencyUnitsComboBox().setEnabled(isPoll);
        getPollFrequencySpinner().setEnabled(isPoll);
        getPollUrlSelectButton().setEnabled(isPoll);
        getPollUrlField().setEnabled(isPoll);
        getPollDataField().setEnabled(isPoll);
        getPollHeadersField().setEnabled(isPoll);
    }

    private JComboBox<AuthPollFrequencyUnitsType> getAuthFrequencyUnitsComboBox() {
        if (authFrequencyUnitsComboBox == null) {
            authFrequencyUnitsComboBox = new JComboBox<>();
            for (AuthPollFrequencyUnitsType acst : AuthPollFrequencyUnitsType.getAllValues()) {
                authFrequencyUnitsComboBox.addItem(acst);
            }
        }
        return authFrequencyUnitsComboBox;
    }

    private ZapNumberSpinner getPollFrequencySpinner() {
        if (pollFrequency == null) {
            pollFrequency =
                    new ZapNumberSpinner(
                            1, AuthenticationMethod.DEFAULT_POLL_FREQUENCY, Integer.MAX_VALUE);
            // Reduce the field size otherwise it takes up too much space
            Component mySpinnerEditor = pollFrequency.getEditor();
            JFormattedTextField jftf = ((JSpinner.DefaultEditor) mySpinnerEditor).getTextField();
            jftf.setColumns(6);
        }
        return pollFrequency;
    }

    private AuthenticationIndicatorsPanel getAuthenticationIndicatorsPanel() {
        if (authenticationIndicatorsPanel == null) {
            authenticationIndicatorsPanel = new AuthenticationIndicatorsPanelImpl();
        }
        return authenticationIndicatorsPanel;
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

    private JPanel getVerifContainerPanel() {
        if (verifContainerPanel == null) {
            verifContainerPanel = new JPanel(new GridBagLayout());
            verifContainerPanel.setBorder(
                    javax.swing.BorderFactory.createTitledBorder(
                            null,
                            PANEL_TITLE_VERIF,
                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
                            FontUtils.getFont(FontUtils.Size.standard)));
        }
        return verifContainerPanel;
    }

    private JButton getPollUrlSelectButton() {
        if (pollUrlSelectButton == null) {
            pollUrlSelectButton = new JButton(Constant.messages.getString("all.button.select"));
            pollUrlSelectButton.setIcon(
                    new ImageIcon(
                            View.class.getResource("/resource/icon/16/094.png"))); // Globe Icon
            // Add behaviour for Node Select dialog
            pollUrlSelectButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            NodeSelectDialog nsd =
                                    new NodeSelectDialog(View.getSingleton().getMainFrame());
                            // Try to pre-select the node according to what has been inserted in the
                            // fields
                            SiteNode node = null;
                            if (getPollUrlField().getText().trim().length() > 0)
                                try {
                                    // If it's a POST query
                                    if (getPollDataField().getText().trim().length() > 0)
                                        node =
                                                Model.getSingleton()
                                                        .getSession()
                                                        .getSiteTree()
                                                        .findNode(
                                                                new URI(
                                                                        getPollUrlField().getText(),
                                                                        false),
                                                                HttpRequestHeader.POST,
                                                                getPollDataField().getText());
                                    else
                                        node =
                                                Model.getSingleton()
                                                        .getSession()
                                                        .getSiteTree()
                                                        .findNode(
                                                                new URI(
                                                                        getPollUrlField().getText(),
                                                                        false));
                                } catch (Exception e2) {
                                    // Ignore. It means we could not properly get a node for the
                                    // existing
                                    // value and does not have any harmful effects
                                }

                            // Show the dialog and wait for input
                            node = nsd.showDialog(node);
                            if (node != null && node.getHistoryReference() != null) {
                                try {

                                    getPollUrlField()
                                            .setText(
                                                    node.getHistoryReference().getURI().toString());
                                    getPollDataField()
                                            .setText(
                                                    node.getHistoryReference()
                                                            .getHttpMessage()
                                                            .getRequestBody()
                                                            .toString());
                                } catch (Exception e1) {
                                    log.error(e1.getMessage(), e1);
                                }
                            }
                        }
                    });
        }
        return pollUrlSelectButton;
    }

    private ZapTextField getPollUrlField() {
        if (pollUrlField == null) {
            pollUrlField = new ZapTextField();
        }
        return pollUrlField;
    }

    private ZapTextField getPollDataField() {
        if (pollDataField == null) {
            pollDataField = new ZapTextField();
        }
        return pollDataField;
    }

    private ZapTextArea getPollHeadersField() {
        if (pollHeadersField == null) {
            pollHeadersField = new ZapTextArea(2, 0);
        }
        return pollHeadersField;
    }

    private ZapTextField getLoggedInIndicatorRegexField() {
        if (loggedInIndicatorRegexField == null) loggedInIndicatorRegexField = new ZapTextField();
        return loggedInIndicatorRegexField;
    }

    private ZapTextField getLoggedOutIndicatorRegexField() {
        if (loggedOutIndicatorRegexField == null) loggedOutIndicatorRegexField = new ZapTextField();
        return loggedOutIndicatorRegexField;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.context-auth";
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        selectedAuthenticationMethod = uiSharedContext.getAuthenticationMethod();
        log.debug(
                "Initializing configuration panel for authentication method: {} for context {}",
                selectedAuthenticationMethod,
                uiSharedContext.getName());

        resetLoggedInOutIndicators();

        // If something was already configured, find the type and set the UI accordingly
        if (selectedAuthenticationMethod != null) {
            // Set verification
            if (selectedAuthenticationMethod.getAuthCheckingStrategy() != null) {
                getAuthenticationVerifComboBox()
                        .getModel()
                        .setSelectedItem(
                                new AuthCheckingStrategyType(
                                        selectedAuthenticationMethod.getAuthCheckingStrategy()));
            }
            setPollFieldStatuses(
                    (AuthCheckingStrategyType) getAuthenticationVerifComboBox().getSelectedItem());

            getPollUrlField().setText(selectedAuthenticationMethod.getPollUrl());
            getPollDataField().setText(selectedAuthenticationMethod.getPollData());
            getPollHeadersField().setText(selectedAuthenticationMethod.getPollHeaders());
            getPollFrequencySpinner().setValue(selectedAuthenticationMethod.getPollFrequency());
            getAuthFrequencyUnitsComboBox()
                    .setSelectedItem(
                            new AuthPollFrequencyUnitsType(
                                    selectedAuthenticationMethod.getPollFrequencyUnits()));

            if (selectedAuthenticationMethod.getLoggedInIndicatorPattern() != null)
                getLoggedInIndicatorRegexField()
                        .setText(
                                selectedAuthenticationMethod
                                        .getLoggedInIndicatorPattern()
                                        .pattern());
            else getLoggedInIndicatorRegexField().setText("");
            if (selectedAuthenticationMethod.getLoggedOutIndicatorPattern() != null)
                getLoggedOutIndicatorRegexField()
                        .setText(
                                selectedAuthenticationMethod
                                        .getLoggedOutIndicatorPattern()
                                        .pattern());
            else getLoggedOutIndicatorRegexField().setText("");

            // If the proper type is already selected, just rebind the data
            if (shownMethodType != null
                    && shownMethodType.isTypeForMethod(selectedAuthenticationMethod)) {
                if (shownMethodType.hasOptionsPanel()) {
                    log.debug(
                            "Binding authentication method to existing panel of proper type for context {}",
                            uiSharedContext.getName());
                    shownConfigPanel.bindMethod(
                            selectedAuthenticationMethod, getAuthenticationIndicatorsPanel());
                }
                return;
            }

            // Select what needs to be selected
            for (AuthenticationMethodType type : extension.getAuthenticationMethodTypes())
                if (type.isTypeForMethod(selectedAuthenticationMethod)) {
                    // Selecting the type here will also force the selection listener to run and
                    // change the config panel accordingly
                    log.debug(
                            "Binding authentication method to new panel of proper type for context {}",
                            uiSharedContext.getName());
                    // Add hack to make sure no confirmation is needed if a change has been done
                    // somewhere else (e.g. API)
                    needsConfirm = false;
                    getAuthenticationMethodsComboBox().setSelectedItem(type);
                    needsConfirm = true;
                    break;
                }
        }
    }

    /**
     * Resets the tool tip and enables the fields of the logged in/out indicators.
     *
     * @see #getLoggedInIndicatorRegexField()
     * @see #getLoggedOutIndicatorRegexField()
     */
    private void resetLoggedInOutIndicators() {
        getLoggedInIndicatorRegexField().setToolTipText(null);
        getLoggedInIndicatorRegexField().setEnabled(true);
        getLoggedOutIndicatorRegexField().setToolTipText(null);
        getLoggedOutIndicatorRegexField().setEnabled(true);
    }

    @Override
    public void validateContextData(Session session) throws Exception {
        if (shownConfigPanel != null) shownConfigPanel.validateFields();
        try {
            Pattern.compile(getLoggedInIndicatorRegexField().getText());
            Pattern.compile(getLoggedOutIndicatorRegexField().getText());
        } catch (PatternSyntaxException e) {
            throw new IllegalStateException(
                    Constant.messages.getString(
                            "authentication.panel.error.illegalPattern",
                            getUISharedContext().getName()),
                    e);
        }
        if (((AuthCheckingStrategyType) getAuthenticationVerifComboBox().getSelectedItem())
                .getStrategy()
                .equals(AuthCheckingStrategy.POLL_URL)) {
            String url = this.getPollUrlField().getText();
            if (url.length() == 0) {
                throw new IllegalStateException(
                        Constant.messages.getString(
                                "authentication.panel.error.nopollurl",
                                getUISharedContext().getName()));
            } else {
                try {
                    new URI(url, true);
                } catch (Exception e) {
                    throw new IllegalStateException(
                            Constant.messages.getString(
                                    "authentication.panel.error.badpollurl",
                                    getUISharedContext().getName()),
                            e);
                }
            }
            for (String header : this.getPollHeadersField().getText().split("\n")) {
                if (header.trim().length() > 0) {
                    String[] headerValue = header.split(":");
                    if (headerValue.length != 2) {
                        throw new IllegalStateException(
                                Constant.messages.getString(
                                        "authentication.panel.error.badpollheaders",
                                        getUISharedContext().getName()));
                    }
                }
            }
        }
    }

    private void saveMethod() {
        if (shownConfigPanel != null) shownConfigPanel.saveMethod();

        selectedAuthenticationMethod.setAuthCheckingStrategy(
                ((AuthCheckingStrategyType) getAuthenticationVerifComboBox().getSelectedItem())
                        .getStrategy());
        selectedAuthenticationMethod.setPollUrl(this.getPollUrlField().getText());
        selectedAuthenticationMethod.setPollData(this.getPollDataField().getText());
        selectedAuthenticationMethod.setPollHeaders(this.getPollHeadersField().getText());
        selectedAuthenticationMethod.setPollFrequency(this.getPollFrequencySpinner().getValue());
        selectedAuthenticationMethod.setPollFrequencyUnits(
                ((AuthPollFrequencyUnitsType)
                                this.getAuthFrequencyUnitsComboBox().getSelectedItem())
                        .getUnits());
        selectedAuthenticationMethod.setLoggedInIndicatorPattern(
                getLoggedInIndicatorRegexField().getText());
        selectedAuthenticationMethod.setLoggedOutIndicatorPattern(
                getLoggedOutIndicatorRegexField().getText());
    }

    @Override
    public void saveContextData(Session session) throws Exception {
        saveMethod();

        Context context = session.getContext(getContextId());
        // Notify the previously saved method that it's being discarded so the changes can be
        // reflected in the UI
        if (context.getAuthenticationMethod() != null)
            if (!shownMethodType.isTypeForMethod(context.getAuthenticationMethod()))
                context.getAuthenticationMethod().onMethodDiscarded();

        context.setAuthenticationMethod(selectedAuthenticationMethod);

        // Notify the newly saved method that it's being persisted so the changes can be
        // reflected in the UI
        selectedAuthenticationMethod.onMethodPersisted();
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        saveMethod();
        uiSharedContext.setAuthenticationMethod(selectedAuthenticationMethod);
    }

    private class AuthenticationIndicatorsPanelImpl implements AuthenticationIndicatorsPanel {

        @Override
        public String getLoggedInIndicatorPattern() {
            return getLoggedInIndicatorRegexField().getText();
        }

        @Override
        public void setLoggedInIndicatorPattern(String loggedInIndicatorPattern) {
            getLoggedInIndicatorRegexField().setText(loggedInIndicatorPattern);
        }

        @Override
        public void setLoggedInIndicatorEnabled(boolean enabled) {
            getLoggedInIndicatorRegexField().setEnabled(enabled);
        }

        @Override
        public void setLoggedInIndicatorToolTip(String toolTip) {
            getLoggedInIndicatorRegexField().setToolTipText(toolTip);
        }

        @Override
        public String getLoggedOutIndicatorPattern() {
            return getLoggedOutIndicatorRegexField().getText();
        }

        @Override
        public void setLoggedOutIndicatorPattern(String loggedOutIndicatorPattern) {
            getLoggedOutIndicatorRegexField().setText(loggedOutIndicatorPattern);
        }

        @Override
        public void setLoggedOutIndicatorEnabled(boolean enabled) {
            getLoggedOutIndicatorRegexField().setEnabled(enabled);
        }

        @Override
        public void setLoggedOutIndicatorToolTip(String toolTip) {
            getLoggedOutIndicatorRegexField().setToolTipText(toolTip);
        }
    }

    private static class AuthCheckingStrategyType {
        private AuthCheckingStrategy strategy;

        public AuthCheckingStrategyType(AuthCheckingStrategy strategy) {
            super();
            this.strategy = strategy;
        }

        public AuthCheckingStrategy getStrategy() {
            return strategy;
        }

        @Override
        public String toString() {
            return Constant.messages.getString(STRATEGY_PREFIX + strategy.name().toLowerCase());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AuthCheckingStrategyType)) {
                return false;
            }
            return this.strategy.equals(((AuthCheckingStrategyType) o).getStrategy());
        }

        @Override
        public int hashCode() {
            return this.strategy.hashCode();
        }

        public static List<AuthCheckingStrategyType> getAllValues() {
            List<AuthCheckingStrategyType> list = new ArrayList<>();
            for (AuthenticationMethod.AuthCheckingStrategy strategy :
                    AuthenticationMethod.AuthCheckingStrategy.values()) {
                list.add(new AuthCheckingStrategyType(strategy));
            }
            return list;
        }
    }

    private static class AuthPollFrequencyUnitsType {
        private AuthPollFrequencyUnits units;

        public AuthPollFrequencyUnitsType(AuthPollFrequencyUnits units) {
            super();
            this.units = units;
        }

        public AuthPollFrequencyUnits getUnits() {
            return units;
        }

        @Override
        public String toString() {
            return Constant.messages.getString(FREQUENCY_UNITS_PREFIX + units.name().toLowerCase());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AuthPollFrequencyUnitsType)) {
                return false;
            }
            return this.units.equals(((AuthPollFrequencyUnitsType) o).getUnits());
        }

        @Override
        public int hashCode() {
            return this.units.hashCode();
        }

        public static List<AuthPollFrequencyUnitsType> getAllValues() {
            List<AuthPollFrequencyUnitsType> list = new ArrayList<>();
            for (AuthenticationMethod.AuthPollFrequencyUnits strategy :
                    AuthenticationMethod.AuthPollFrequencyUnits.values()) {
                list.add(new AuthPollFrequencyUnitsType(strategy));
            }
            return list;
        }
    }
}
