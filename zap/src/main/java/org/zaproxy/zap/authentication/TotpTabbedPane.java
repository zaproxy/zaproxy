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

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.authentication.TotpAuthenticationCredentials.TotpData;
import org.zaproxy.zap.utils.ZapNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;

public class TotpTabbedPane extends JTabbedPane {

    private static final long serialVersionUID = 1L;

    private ZapTextField totpSecretTextField;
    private ZapNumberSpinner totpPeriodNumberSpinner;
    private ZapNumberSpinner totpDigitsNumberSpinner;
    private JComboBox<String> totpAlgorithmComboBox;

    private JPanel totpPanel;

    public TotpTabbedPane(JPanel credentialsPanel) {
        addTab(
                Constant.messages.getString(
                        "authentication.method.all.credentials.totp.ui.tab.creds"),
                credentialsPanel);

        totpPanel = new JPanel();
        GroupLayout layout = new GroupLayout(totpPanel);
        totpPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        totpSecretTextField = new ZapTextField(25);
        JLabel totpSecretLabel = createLabel("secret", totpSecretTextField);
        totpPeriodNumberSpinner =
                new ZapNumberSpinner(1, TotpData.EMPTY.period(), Integer.MAX_VALUE);
        JLabel totpPeriodLabel = createLabel("period", totpPeriodNumberSpinner);
        totpDigitsNumberSpinner =
                new ZapNumberSpinner(1, TotpData.EMPTY.digits(), Integer.MAX_VALUE);
        JLabel totpDigitsLabel = createLabel("digits", totpDigitsNumberSpinner);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        TotpAuthenticationCredentials.getGenerator()
                .getSupportedAlgorithms()
                .forEach(model::addElement);
        totpAlgorithmComboBox = new JComboBox<>(model);
        JLabel totpAlgorithmLabel = createLabel("algorithm", totpAlgorithmComboBox);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(totpSecretLabel)
                                        .addComponent(totpPeriodLabel)
                                        .addComponent(totpDigitsLabel)
                                        .addComponent(totpAlgorithmLabel))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(totpSecretTextField)
                                        .addComponent(totpPeriodNumberSpinner)
                                        .addComponent(totpDigitsNumberSpinner)
                                        .addComponent(totpAlgorithmComboBox)));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totpSecretLabel)
                                        .addComponent(totpSecretTextField))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totpPeriodLabel)
                                        .addComponent(totpPeriodNumberSpinner))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totpDigitsLabel)
                                        .addComponent(totpDigitsNumberSpinner))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(totpAlgorithmLabel)
                                        .addComponent(totpAlgorithmComboBox)));
        addTab(
                Constant.messages.getString(
                        "authentication.method.all.credentials.totp.ui.tab.totp"),
                totpPanel);
    }

    private static JLabel createLabel(String key, JComponent field) {
        JLabel label =
                new JLabel(
                        Constant.messages.getString(
                                "authentication.method.all.credentials.totp.ui.field." + key));
        label.setLabelFor(field);
        return label;
    }

    public void setTotpData(TotpData totpData) {
        totpSecretTextField.setText(totpData.secret());
        totpSecretTextField.discardAllEdits();
        totpPeriodNumberSpinner.setValue(totpData.period());
        totpDigitsNumberSpinner.setValue(totpData.digits());
        totpAlgorithmComboBox.setSelectedItem(totpData.algorithm());
    }

    public TotpData getTotpData() {
        return new TotpData(
                totpSecretTextField.getText(),
                totpPeriodNumberSpinner.getValue(),
                totpDigitsNumberSpinner.getValue(),
                (String) totpAlgorithmComboBox.getSelectedItem());
    }
}
