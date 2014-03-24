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

import java.awt.Dialog;
import java.util.regex.Pattern;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.ProxyExcludedDomainMatcher;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;

class DialogAddProxyExcludedDomain extends AbstractFormDialog {

    private static final long serialVersionUID = -7356390753317082681L;

    private static final String DIALOG_TITLE = Constant.messages.getString("conn.options.proxy.excluded.domain.add.title");

    private static final String CONFIRM_BUTTON_LABEL = Constant.messages.getString("conn.options.proxy.excluded.domain.add.button.confirm");

    private static final String DOMAIN_FIELD_LABEL = Constant.messages.getString("conn.options.proxy.excluded.domain.field.label.domain");
    private static final String REGEX_FIELD_LABEL = Constant.messages.getString("conn.options.proxy.excluded.domain.field.label.regex");
    private static final String ENABLED_FIELD_LABEL = Constant.messages.getString("conn.options.proxy.excluded.domain.field.label.enabled");

    private static final String TITLE_INVALID_REGEX_DIALOG = Constant.messages.getString("conn.options.proxy.excluded.domain.warning.invalid.regex.title");
    private static final String TEXT_INVALID_REGEX_DIALOG = Constant.messages.getString("conn.options.proxy.excluded.domain.warning.invalid.regex.text");

    private ZapTextField domainTextField;
    private JCheckBox regexCheckBox;
    private JCheckBox enabledCheckBox;

    protected ProxyExcludedDomainMatcher proxyExcludedDomain;

    private ConfirmButtonValidatorDocListener confirmButtonValidatorDocListener;

    public DialogAddProxyExcludedDomain(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    protected DialogAddProxyExcludedDomain(Dialog owner, String title) {
        super(owner, title);
    }

    @Override
    protected JPanel getFieldsPanel() {
        JPanel fieldsPanel = new JPanel();

        GroupLayout layout = new GroupLayout(fieldsPanel);
        fieldsPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel domainLabel = new JLabel(DOMAIN_FIELD_LABEL);
        JLabel regexLabel = new JLabel(REGEX_FIELD_LABEL);
        JLabel enabledLabel = new JLabel(ENABLED_FIELD_LABEL);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(domainLabel)
                                .addComponent(enabledLabel)
                                .addComponent(regexLabel))
                .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(getDomainTextField())
                                .addComponent(getEnabledCheckBox())
                                .addComponent(getRegexCheckBox())));

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(domainLabel)
                                .addComponent(getDomainTextField()))
                .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(regexLabel)
                                .addComponent(getRegexCheckBox()))
                .addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(enabledLabel)
                                .addComponent(getEnabledCheckBox())));

        return fieldsPanel;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    @Override
    protected void init() {
        getDomainTextField().setText("");
        getRegexCheckBox().setSelected(false);
        getEnabledCheckBox().setSelected(true);
        proxyExcludedDomain = null;
    }

    @Override
    protected boolean validateFields() {
        if (getRegexCheckBox().isSelected()) {
            try {
                ProxyExcludedDomainMatcher.createPattern(getDomainTextField().getText());
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                        this,
                        TEXT_INVALID_REGEX_DIALOG,
                        TITLE_INVALID_REGEX_DIALOG,
                        JOptionPane.INFORMATION_MESSAGE);
                getDomainTextField().requestFocusInWindow();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void performAction() {
        String value = getDomainTextField().getText();
        if (getRegexCheckBox().isSelected()) {
            Pattern pattern = ProxyExcludedDomainMatcher.createPattern(value);
            proxyExcludedDomain = new ProxyExcludedDomainMatcher(pattern);
        } else {
            proxyExcludedDomain = new ProxyExcludedDomainMatcher(value);
        }

        proxyExcludedDomain.setEnabled(getEnabledCheckBox().isSelected());
    }

    @Override
    protected void clearFields() {
        getDomainTextField().setText("");
        getDomainTextField().discardAllEdits();
    }

    public ProxyExcludedDomainMatcher getProxyExcludedDomain() {
        return proxyExcludedDomain;
    }

    protected ZapTextField getDomainTextField() {
        if (domainTextField == null) {
            domainTextField = new ZapTextField(25);
            domainTextField.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }

        return domainTextField;
    }

    protected JCheckBox getRegexCheckBox() {
        if (regexCheckBox == null) {
            regexCheckBox = new JCheckBox();
        }
        return regexCheckBox;
    }

    protected JCheckBox getEnabledCheckBox() {
        if (enabledCheckBox == null) {
            enabledCheckBox = new JCheckBox();
        }

        return enabledCheckBox;
    }

    public void clear() {
        this.proxyExcludedDomain = null;
    }

    private ConfirmButtonValidatorDocListener getConfirmButtonValidatorDocListener() {
        if (confirmButtonValidatorDocListener == null) {
            confirmButtonValidatorDocListener = new ConfirmButtonValidatorDocListener();
        }
        return confirmButtonValidatorDocListener;
    }

    private class ConfirmButtonValidatorDocListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        private void checkAndEnableConfirmButton() {
            boolean enabled = (getDomainTextField().getDocument().getLength() > 0);
            setConfirmButtonEnabled(enabled);
        }
    }

}
