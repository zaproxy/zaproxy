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
package org.parosproxy.paros.extension.option;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.FontUtils;

/**
 * A {@code JPanel} for selecting security protocols provided by {@code SSLConnector}.
 *
 * @deprecated (2.12.0) No longer in use.
 */
@Deprecated
@SuppressWarnings("serial")
public class SecurityProtocolsPanel extends JPanel {

    private static final long serialVersionUID = 5096843444189699353L;

    private Map<String, JCheckBox> checkBoxesSslTlsProtocols;
    private boolean supportedSecurityProtocolsInitialised;

    public SecurityProtocolsPanel() {
        setLayout(new GridBagLayout());

        setBorder(
                javax.swing.BorderFactory.createTitledBorder(
                        null,
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.title"),
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        FontUtils.getFont(FontUtils.Size.standard)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(2, 2, 2, 2);
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

        checkBoxesSslTlsProtocols = new HashMap<>();
        JCheckBox checkBox =
                new JCheckBox(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.ssl2hello.label"));
        checkBox.setEnabled(false);
        checkBoxesSslTlsProtocols.put(
                org.parosproxy.paros.network.SSLConnector.SECURITY_PROTOCOL_SSL_V2_HELLO, checkBox);
        add(checkBox, gbc);

        checkBox =
                new JCheckBox(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.ssl3.label"));
        checkBox.setEnabled(false);
        checkBoxesSslTlsProtocols.put(
                org.parosproxy.paros.network.SSLConnector.SECURITY_PROTOCOL_SSL_V3, checkBox);
        add(checkBox, gbc);

        checkBox =
                new JCheckBox(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.tlsv1.label"));
        checkBox.setEnabled(false);
        checkBoxesSslTlsProtocols.put(
                org.parosproxy.paros.network.SSLConnector.SECURITY_PROTOCOL_TLS_V1, checkBox);
        add(checkBox, gbc);

        checkBox =
                new JCheckBox(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.tlsv1.1.label"));
        checkBox.setEnabled(false);
        checkBoxesSslTlsProtocols.put(
                org.parosproxy.paros.network.SSLConnector.SECURITY_PROTOCOL_TLS_V1_1, checkBox);
        add(checkBox, gbc);

        checkBox =
                new JCheckBox(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.tlsv1.2.label"));
        checkBox.setEnabled(false);
        checkBoxesSslTlsProtocols.put(
                org.parosproxy.paros.network.SSLConnector.SECURITY_PROTOCOL_TLS_V1_2, checkBox);
        add(checkBox, gbc);

        checkBox =
                new JCheckBox(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.tlsv1.3.label"));
        checkBox.setEnabled(false);
        checkBoxesSslTlsProtocols.put(
                org.parosproxy.paros.network.SSLConnector.SECURITY_PROTOCOL_TLS_V1_3, checkBox);
        add(checkBox, gbc);
    }

    public void setSecurityProtocolsEnabled(String[] selectedProtocols) {
        if (!supportedSecurityProtocolsInitialised) {
            String[] protocols = org.parosproxy.paros.network.SSLConnector.getSupportedProtocols();
            for (String protocol : protocols) {
                JCheckBox checkBox = checkBoxesSslTlsProtocols.get(protocol);
                if (checkBox != null) {
                    checkBox.setEnabled(true);
                }
            }
            String toolTip = null;
            for (JCheckBox checkBox : checkBoxesSslTlsProtocols.values()) {
                if (!checkBox.isEnabled()) {
                    if (toolTip == null) {
                        toolTip =
                                Constant.messages.getString(
                                        "generic.options.panel.security.protocols.protocol.not.supported.tooltip");
                    }
                    checkBox.setToolTipText(toolTip);
                }
            }
            supportedSecurityProtocolsInitialised = true;
        }

        for (JCheckBox checkBox : checkBoxesSslTlsProtocols.values()) {
            checkBox.setSelected(false);
        }

        if (selectedProtocols != null) {
            for (String protocol : selectedProtocols) {
                JCheckBox checkBox = checkBoxesSslTlsProtocols.get(protocol);
                if (checkBox != null && checkBox.isEnabled()) {
                    checkBox.setSelected(true);
                }
            }
        }
    }

    public void validateSecurityProtocols() {
        int protocolsSelected = 0;
        JCheckBox checkBoxEnabledProtocol = null;
        for (Entry<String, JCheckBox> entry : checkBoxesSslTlsProtocols.entrySet()) {
            JCheckBox checkBox = entry.getValue();
            if (checkBox.isEnabled()) {
                if (checkBoxEnabledProtocol == null) {
                    checkBoxEnabledProtocol = checkBox;
                }
                if (checkBox.isSelected()) {
                    protocolsSelected++;
                    if (protocolsSelected > 1) {
                        break;
                    }
                }
            }
        }

        if (checkBoxEnabledProtocol != null) {
            if (protocolsSelected == 0) {
                checkBoxEnabledProtocol.requestFocusInWindow();
                throw new IllegalArgumentException(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.error.no.protocols.selected"));
            }

            if (protocolsSelected == 1
                    && checkBoxesSslTlsProtocols
                            .get(
                                    org.parosproxy.paros.network.SSLConnector
                                            .SECURITY_PROTOCOL_SSL_V2_HELLO)
                            .isSelected()) {
                checkBoxEnabledProtocol.requestFocusInWindow();
                throw new IllegalArgumentException(
                        Constant.messages.getString(
                                "generic.options.panel.security.protocols.error.just.sslv2hello.selected"));
            }
        }
    }

    public String[] getSelectedProtocols() {
        int countSelectedProtocols = 0;
        String[] selectedProtocols = new String[checkBoxesSslTlsProtocols.values().size()];
        for (Entry<String, JCheckBox> entry : checkBoxesSslTlsProtocols.entrySet()) {
            JCheckBox checkBox = entry.getValue();
            if (checkBox.isEnabled() && checkBox.isSelected()) {
                selectedProtocols[countSelectedProtocols] = entry.getKey();
                countSelectedProtocols++;
            }
        }
        return Arrays.copyOf(selectedProtocols, countSelectedProtocols);
    }
}
