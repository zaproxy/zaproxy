/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.option;

import java.awt.event.ItemEvent;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.FontUtils;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;

/**
 * A panel for SOCKS proxy configuration.
 *
 * @deprecated (2.12.0) No longer in use.
 */
@Deprecated
public class SocksProxyPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JCheckBox useSocksCheckBox;
    private final ZapTextField hostTextField;
    private final ZapPortNumberSpinner portNumberSpinner;
    private final JRadioButton version4RadioButton;
    private final JRadioButton version5RadioButton;
    private final JCheckBox useSocksDnsCheckBox;
    private final ZapTextField usernameTextField;
    private final JPasswordField passwordField;

    public SocksProxyPanel() {
        setBorder(
                BorderFactory.createTitledBorder(
                        null,
                        Constant.messages.getString("conn.options.socks.title"),
                        TitledBorder.DEFAULT_JUSTIFICATION,
                        TitledBorder.DEFAULT_POSITION,
                        FontUtils.getFont(FontUtils.Size.standard)));

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setAutoCreateGaps(true);

        hostTextField = new ZapTextField();
        hostTextField.setText(
                org.parosproxy.paros.network.ConnectionParam.DEFAULT_SOCKS_PROXY.getHost());
        JLabel hostLabel = new JLabel(Constant.messages.getString("conn.options.socks.host"));
        hostLabel.setLabelFor(hostTextField);

        portNumberSpinner =
                new ZapPortNumberSpinner(
                        org.parosproxy.paros.network.ConnectionParam.DEFAULT_SOCKS_PROXY.getPort());
        JLabel portLabel = new JLabel(Constant.messages.getString("conn.options.socks.port"));
        portLabel.setLabelFor(portNumberSpinner);

        JLabel versionLabel = new JLabel(Constant.messages.getString("conn.options.socks.version"));
        version4RadioButton = new JRadioButton("4a");
        version5RadioButton = new JRadioButton("5");

        ButtonGroup versionButtonGroup = new ButtonGroup();
        versionButtonGroup.add(version4RadioButton);
        versionButtonGroup.add(version5RadioButton);
        version4RadioButton.setSelected(true);

        useSocksDnsCheckBox = new JCheckBox(Constant.messages.getString("conn.options.socks.dns"));
        useSocksDnsCheckBox.setToolTipText(
                Constant.messages.getString("conn.options.socks.dns.tooltip"));
        useSocksDnsCheckBox.setSelected(true);

        usernameTextField = new ZapTextField();
        JLabel usernameLabel =
                new JLabel(Constant.messages.getString("conn.options.socks.username"));
        usernameLabel.setLabelFor(usernameTextField);

        passwordField = new JPasswordField();
        JLabel passwordLabel =
                new JLabel(Constant.messages.getString("conn.options.socks.password"));
        passwordLabel.setLabelFor(passwordField);

        useSocksCheckBox =
                new JCheckBox(Constant.messages.getString("conn.options.socks.enabled"), true);
        useSocksCheckBox.addItemListener(
                e -> {
                    boolean state = e.getStateChange() == ItemEvent.SELECTED;
                    hostTextField.setEnabled(state);
                    portNumberSpinner.setEnabled(state);
                    version4RadioButton.setEnabled(state);
                    version5RadioButton.setEnabled(state);
                    useSocksDnsCheckBox.setEnabled(state && version5RadioButton.isSelected());
                    usernameTextField.setEnabled(state);
                    passwordField.setEnabled(state);
                });
        useSocksCheckBox.setSelected(false);

        version5RadioButton.addItemListener(
                e ->
                        useSocksDnsCheckBox.setEnabled(
                                e.getStateChange() == ItemEvent.SELECTED
                                        && useSocksCheckBox.isSelected()));
        setSelectedVersion(
                org.parosproxy.paros.network.ConnectionParam.DEFAULT_SOCKS_PROXY.getVersion());

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(useSocksCheckBox)
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addGroup(
                                                layout.createParallelGroup()
                                                        .addComponent(hostLabel)
                                                        .addComponent(portLabel)
                                                        .addComponent(versionLabel)
                                                        .addComponent(usernameLabel)
                                                        .addComponent(passwordLabel))
                                        .addGroup(
                                                layout.createParallelGroup()
                                                        .addComponent(hostTextField)
                                                        .addComponent(portNumberSpinner)
                                                        .addGroup(
                                                                layout.createParallelGroup()
                                                                        .addGroup(
                                                                                layout.createSequentialGroup()
                                                                                        .addComponent(
                                                                                                version4RadioButton)
                                                                                        .addComponent(
                                                                                                version5RadioButton))
                                                                        .addComponent(
                                                                                useSocksDnsCheckBox))
                                                        .addComponent(usernameTextField)
                                                        .addComponent(passwordField))));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(useSocksCheckBox)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(hostLabel)
                                        .addComponent(hostTextField))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(portLabel)
                                        .addComponent(portNumberSpinner))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(versionLabel)
                                        .addComponent(version4RadioButton)
                                        .addComponent(version5RadioButton))
                        .addComponent(useSocksDnsCheckBox)
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(usernameLabel)
                                        .addComponent(usernameTextField))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordLabel)
                                        .addComponent(passwordField)));
    }

    private void setSelectedVersion(org.zaproxy.zap.network.SocksProxy.Version version) {
        switch (version) {
            case SOCKS4a:
                version4RadioButton.setSelected(true);
                break;
            case SOCKS5:
            default:
                version5RadioButton.setSelected(true);
        }
    }

    private org.zaproxy.zap.network.SocksProxy.Version getSelectedVersion() {
        if (version4RadioButton.isSelected()) {
            return org.zaproxy.zap.network.SocksProxy.Version.SOCKS4a;
        }
        return org.zaproxy.zap.network.SocksProxy.Version.SOCKS5;
    }

    public void initParam(org.parosproxy.paros.network.ConnectionParam options) {
        useSocksCheckBox.setSelected(options.isUseSocksProxy());

        org.zaproxy.zap.network.SocksProxy socksProxy = options.getSocksProxy();
        hostTextField.setText(socksProxy.getHost());
        hostTextField.discardAllEdits();
        portNumberSpinner.setValue(socksProxy.getPort());
        setSelectedVersion(socksProxy.getVersion());
        useSocksDnsCheckBox.setSelected(socksProxy.isUseDns());

        PasswordAuthentication passwordAuthentication = options.getSocksProxyPasswordAuth();
        usernameTextField.setText(passwordAuthentication.getUserName());
        usernameTextField.discardAllEdits();
        passwordField.setText(new String(passwordAuthentication.getPassword()));
    }

    public void validateParam() throws Exception {
        if (hostTextField.getText().isEmpty()) {
            hostTextField.requestFocus();
            throw new Exception(Constant.messages.getString("conn.options.socks.host.empty"));
        }
    }

    public void saveParam(org.parosproxy.paros.network.ConnectionParam options) {
        options.setUseSocksProxy(useSocksCheckBox.isSelected());

        org.zaproxy.zap.network.SocksProxy oldSocksProxy = options.getSocksProxy();
        if (!oldSocksProxy.getHost().equals(hostTextField.getText())
                || oldSocksProxy.getPort() != portNumberSpinner.getValue()
                || oldSocksProxy.getVersion() != getSelectedVersion()
                || oldSocksProxy.isUseDns() != useSocksDnsCheckBox.isSelected()) {
            options.setSocksProxy(
                    new org.zaproxy.zap.network.SocksProxy(
                            hostTextField.getText(),
                            portNumberSpinner.getValue(),
                            getSelectedVersion(),
                            useSocksDnsCheckBox.isSelected()));
        }

        PasswordAuthentication passwordAuthentication = options.getSocksProxyPasswordAuth();
        char[] password = passwordField.getPassword();
        if (!passwordAuthentication.getUserName().equals(usernameTextField.getText())
                || !Arrays.equals(passwordAuthentication.getPassword(), password)) {
            options.setSocksProxyPasswordAuth(
                    new PasswordAuthentication(usernameTextField.getText(), password));
        }
    }
}
