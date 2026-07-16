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
package org.zaproxy.zap.extension.authentication;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import org.apache.commons.httpclient.URI;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.network.HttpRequestHeader;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.authentication.AuthenticationMethod;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthCheckingStrategy;
import org.zaproxy.zap.authentication.AuthenticationMethod.AuthPollFrequencyUnits;
import org.zaproxy.zap.authentication.VerificationMethod;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.LayoutHelper;
import org.zaproxy.zap.view.NodeSelectDialog;

/**
 * The Context Panel shown for configuring a Context's verification method.
 *
 * @since 2.18.0
 */
@SuppressWarnings("serial")
public class ContextVerificationPanel extends AbstractContextPropertiesPanel {

    private static final long serialVersionUID = 1L;

    static final String PANEL_NAME = Constant.messages.getString("verification.panel.title");

    private static final String LABEL_DESCRIPTION =
            Constant.messages.getString("verification.panel.label.description");

    private static final String FIELD_LABEL_LOGGED_IN_INDICATOR =
            Constant.messages.getString("authentication.panel.label.loggedIn");
    private static final String FIELD_LABEL_LOGGED_OUT_INDICATOR =
            Constant.messages.getString("authentication.panel.label.loggedOut");
    private static final String LABEL_POLL_URL =
            Constant.messages.getString("authentication.panel.label.pollurl");
    private static final String LABEL_POLL_DATA =
            Constant.messages.getString("authentication.panel.label.polldata");
    private static final String LABEL_POLL_HEADERS =
            Constant.messages.getString("authentication.panel.label.pollheaders");
    private static final String LABEL_POLL_FREQUENCY =
            Constant.messages.getString("authentication.panel.label.freq");
    private static final String LABEL_STRATEGY =
            Constant.messages.getString("authentication.panel.label.strategy");
    private static final String STRATEGY_PREFIX = "authentication.panel.label.strategy.";
    private static final String FREQUENCY_UNITS_PREFIX = "authentication.panel.label.units.";

    private JComboBox<AuthCheckingStrategyType> authenticationVerifComboBox;
    private JComboBox<AuthPollFrequencyUnitsType> authFrequencyUnitsComboBox;

    private JButton pollUrlSelectButton = null;
    private ZapTextField pollUrlField = null;
    private ZapTextField pollDataField = null;
    private ZapTextArea pollHeadersField = null;
    private ZapNumberSpinner pollFrequency = null;

    private ZapTextField loggedInIndicatorRegexField = null;
    private ZapTextField loggedOutIndicatorRegexField = null;

    public ContextVerificationPanel(Context context) {
        super(context.getId());
        initialize();
    }

    public static String buildName(int contextId) {
        return contextId + ": " + PANEL_NAME;
    }

    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(buildName(getContextId()));
        this.setBorder(new EmptyBorder(2, 2, 2, 2));

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(400, 500));

        JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPanel.setViewportView(panel);
        this.add(scrollPanel);

        int y = 0;
        int fullWidth = 3;
        panel.add(
                new ZapHtmlLabel(LABEL_DESCRIPTION), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(
                new JLabel(LABEL_STRATEGY),
                LayoutHelper.getGBC(0, y++, fullWidth, 1.0D, new Insets(20, 0, 5, 5)));
        panel.add(getAuthenticationVerifComboBox(), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));

        panel.add(
                new JLabel(FIELD_LABEL_LOGGED_IN_INDICATOR),
                LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(getLoggedInIndicatorRegexField(), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(
                new JLabel(FIELD_LABEL_LOGGED_OUT_INDICATOR),
                LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(getLoggedOutIndicatorRegexField(), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));

        panel.add(new JLabel(LABEL_POLL_FREQUENCY), LayoutHelper.getGBC(0, y, 1, 0.34D));
        panel.add(getPollFrequencySpinner(), LayoutHelper.getGBC(1, y, 1, 0.33D));
        panel.add(getAuthFrequencyUnitsComboBox(), LayoutHelper.getGBC(2, y++, 1, 0.33D));

        panel.add(new JLabel(LABEL_POLL_URL), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        JPanel urlPanel = new JPanel(new GridBagLayout());
        urlPanel.add(this.getPollUrlField(), LayoutHelper.getGBC(0, 0, 1, 1.0D));
        urlPanel.add(getPollUrlSelectButton(), LayoutHelper.getGBC(1, 0, 1, 0.0D));
        panel.add(urlPanel, LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(new JLabel(LABEL_POLL_DATA), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(this.getPollDataField(), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(new JLabel(LABEL_POLL_HEADERS), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));
        panel.add(this.getPollHeadersField(), LayoutHelper.getGBC(0, y++, fullWidth, 1.0D));

        // Padding
        panel.add(new JLabel(), LayoutHelper.getGBC(0, 99, 1, 1.0D, 1.0D));
    }

    private JComboBox<AuthCheckingStrategyType> getAuthenticationVerifComboBox() {
        if (authenticationVerifComboBox == null) {
            authenticationVerifComboBox = new JComboBox<>();
            for (AuthCheckingStrategyType acst : AuthCheckingStrategyType.getAllValues()) {
                authenticationVerifComboBox.addItem(acst);
            }
            authenticationVerifComboBox.addItemListener(
                    new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            if (e.getStateChange() == ItemEvent.SELECTED) {
                                setPollFieldStatuses((AuthCheckingStrategyType) e.getItem());
                            }
                        }
                    });
        }
        return authenticationVerifComboBox;
    }

    void setPollFieldStatuses(AuthCheckingStrategyType type) {
        boolean isPoll = type.getStrategy().equals(AuthCheckingStrategy.POLL_URL);
        getAuthFrequencyUnitsComboBox().setEnabled(isPoll);
        getPollFrequencySpinner().setEnabled(isPoll);
        getPollUrlSelectButton().setEnabled(isPoll);
        getPollUrlField().setEnabled(isPoll);
        getPollDataField().setEnabled(isPoll);
        getPollHeadersField().setEnabled(isPoll);
        boolean isAutoDetect = type.getStrategy().equals(AuthCheckingStrategy.AUTO_DETECT);
        getLoggedInIndicatorRegexField().setEnabled(!isAutoDetect);
        getLoggedOutIndicatorRegexField().setEnabled(!isAutoDetect);
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
                            1, VerificationMethod.DEFAULT_POLL_FREQUENCY, Integer.MAX_VALUE);
            Component mySpinnerEditor = pollFrequency.getEditor();
            JFormattedTextField jftf = ((JSpinner.DefaultEditor) mySpinnerEditor).getTextField();
            jftf.setColumns(6);
        }
        return pollFrequency;
    }

    private JButton getPollUrlSelectButton() {
        if (pollUrlSelectButton == null) {
            pollUrlSelectButton = new JButton(Constant.messages.getString("all.button.select"));
            pollUrlSelectButton.setIcon(
                    new ImageIcon(
                            View.class.getResource("/resource/icon/16/094.png"))); // Globe Icon
            pollUrlSelectButton.addActionListener(
                    new java.awt.event.ActionListener() {
                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            NodeSelectDialog nsd =
                                    new NodeSelectDialog(View.getSingleton().getMainFrame());
                            SiteNode node = null;
                            if (getPollUrlField().getText().trim().length() > 0)
                                try {
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
                                    // Could not find node for existing value, no harm done
                                }

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
                                    // Ignore
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

    /** Builds a VerificationMethod from the current UI state. */
    VerificationMethod buildVerificationMethodFromUI() {
        VerificationMethod vm = new VerificationMethod();
        vm.setAuthCheckingStrategy(
                ((AuthCheckingStrategyType) getAuthenticationVerifComboBox().getSelectedItem())
                        .getStrategy());
        vm.setPollUrl(getPollUrlField().getText());
        vm.setPollData(getPollDataField().getText());
        vm.setPollHeaders(getPollHeadersField().getText());
        vm.setPollFrequency(getPollFrequencySpinner().getValue());
        vm.setPollFrequencyUnits(
                ((AuthPollFrequencyUnitsType) getAuthFrequencyUnitsComboBox().getSelectedItem())
                        .getUnits());
        vm.setLoggedInIndicatorPattern(getLoggedInIndicatorRegexField().getText());
        vm.setLoggedOutIndicatorPattern(getLoggedOutIndicatorRegexField().getText());
        return vm;
    }

    private void populateUIFromVm(VerificationMethod vm) {
        if (vm.getAuthCheckingStrategy() != null) {
            getAuthenticationVerifComboBox()
                    .getModel()
                    .setSelectedItem(new AuthCheckingStrategyType(vm.getAuthCheckingStrategy()));
        }
        setPollFieldStatuses(
                (AuthCheckingStrategyType) getAuthenticationVerifComboBox().getSelectedItem());

        getPollUrlField().setText(vm.getPollUrl());
        getPollDataField().setText(vm.getPollData());
        getPollHeadersField().setText(vm.getPollHeaders());
        getPollFrequencySpinner().setValue(vm.getPollFrequency());
        getAuthFrequencyUnitsComboBox()
                .setSelectedItem(new AuthPollFrequencyUnitsType(vm.getPollFrequencyUnits()));

        if (vm.getLoggedInIndicatorPattern() != null)
            getLoggedInIndicatorRegexField().setText(vm.getLoggedInIndicatorPattern().pattern());
        else getLoggedInIndicatorRegexField().setText("");
        if (vm.getLoggedOutIndicatorPattern() != null)
            getLoggedOutIndicatorRegexField().setText(vm.getLoggedOutIndicatorPattern().pattern());
        else getLoggedOutIndicatorRegexField().setText("");
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.context-auth";
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        VerificationMethod vm = uiSharedContext.getVerificationMethod();
        if (vm != null) {
            populateUIFromVm(vm);
        }
    }

    @Override
    public void validateContextData(Session session) throws Exception {
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
            String url = getPollUrlField().getText();
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
            for (String header : getPollHeadersField().getText().split("\n")) {
                if (header.trim().length() > 0) {
                    String[] headerValue = header.split(":", 2);
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

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        VerificationMethod vm = buildVerificationMethodFromUI();
        uiSharedContext.setVerificationMethod(vm);
    }

    @Override
    public void saveContextData(Session session) throws Exception {
        VerificationMethod vm = buildVerificationMethodFromUI();
        session.getContext(getContextId()).setVerificationMethod(vm);
    }

    static class AuthCheckingStrategyType {
        private AuthCheckingStrategy strategy;

        public AuthCheckingStrategyType(AuthCheckingStrategy strategy) {
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

    static class AuthPollFrequencyUnitsType {
        private AuthPollFrequencyUnits units;

        public AuthPollFrequencyUnitsType(AuthPollFrequencyUnits units) {
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
