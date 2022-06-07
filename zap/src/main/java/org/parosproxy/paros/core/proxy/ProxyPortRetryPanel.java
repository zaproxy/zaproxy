/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
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
package org.parosproxy.paros.core.proxy;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;

/** @deprecated (2.12.0) */
@Deprecated
class ProxyPortRetryPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final ZapPortNumberSpinner spinnerProxyPort;

    /** Constructs an {@code ProxyPortRetryPanel}. */
    public ProxyPortRetryPanel(String message, int newPort) {
        super(new GridBagLayout(), true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipadx = 0;
        gbc.ipady = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(2, 2, 2, 2);

        JLabel prompt = new JLabel(message);
        this.add(prompt, gbc);
        spinnerProxyPort = new ZapPortNumberSpinner(newPort);
        gbc.gridx = 1;
        this.add(spinnerProxyPort, gbc);
    }

    public int getProxyPort() {
        return spinnerProxyPort.getValue();
    }
}
