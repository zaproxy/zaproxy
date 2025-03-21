/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2025 The ZAP Development Team
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
package org.zaproxy.zap.authentication;

import javax.swing.JButton;
import javax.swing.JPanel;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.authentication.TotpAuthenticationCredentials.TotpData;

@SuppressWarnings("serial")
public class TotpButton extends JButton {

    private static final long serialVersionUID = 1L;

    private final JPanel parent;
    private TotpData totpData;

    public TotpButton(JPanel parent, TotpData totpData) {
        super(
                Constant.messages.getString(
                        "authentication.method.all.credentials.totp.ui.button.configure"));
        this.parent = parent;
        this.totpData = totpData;

        addActionListener(e -> showDialog());
    }

    private void showDialog() {
        DialogTotp dialog = new DialogTotp(parent);
        dialog.setTotpData(totpData);
        dialog.setVisible(true);
        totpData = dialog.getTotpData();
    }

    public TotpData getTotpData() {
        return totpData;
    }
}
