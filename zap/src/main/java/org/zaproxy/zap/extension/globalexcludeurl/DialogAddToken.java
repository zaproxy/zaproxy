/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
 * Copyright 2014 Jay Ball - Aspect Security
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
package org.zaproxy.zap.extension.globalexcludeurl;

import java.awt.Dialog;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;

@SuppressWarnings("serial")
class DialogAddToken extends AbstractFormDialog {

    private static final long serialVersionUID = 4460797449668634319L;

    private static final String DIALOG_TITLE =
            Constant.messages.getString("options.globalexcludeurl.dialog.token.add.title");

    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString("options.globalexcludeurl.dialog.token.add.button.confirm");

    private static final String NAME_FIELD_LABEL =
            Constant.messages.getString("options.globalexcludeurl.dialog.token.field.label.name");
    private static final String ENABLED_FIELD_LABEL =
            Constant.messages.getString(
                    "options.globalexcludeurl.dialog.token.field.label.enabled");
    private static final String DESC_FIELD_LABEL =
            Constant.messages.getString(
                    "options.globalexcludeurl.dialog.token.field.label.description");

    private static final String TITLE_NAME_REPEATED_DIALOG =
            Constant.messages.getString(
                    "options.globalexcludeurl.dialog.token.warning.name.repeated.title");
    private static final String TEXT_NAME_REPEATED_DIALOG =
            Constant.messages.getString(
                    "options.globalexcludeurl.dialog.token.warning.name.repeated.text");

    private ZapTextField regexTextField;
    private JCheckBox enabledCheckBox;
    private ZapTextField descTextField;

    protected GlobalExcludeURLParamToken token;
    private List<GlobalExcludeURLParamToken> tokens;

    public DialogAddToken(Dialog owner) {
        super(owner, DIALOG_TITLE);
        // TODO how????    this.setSize((int)(getWidth() * 1.5), getHeight());   // make window 50%
        // wider than the default
    }

    protected DialogAddToken(Dialog owner, String title) {
        super(owner, title);
        // TODO how????    this.setSize((int)(getWidth() * 1.5), getHeight());   // make window 50%
        // wider than the default
    }

    @Override
    protected JPanel getFieldsPanel() {
        JPanel fieldsPanel = new JPanel();

        GroupLayout layout = new GroupLayout(fieldsPanel);
        fieldsPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel nameLabel = new JLabel(NAME_FIELD_LABEL);
        JLabel enabledLabel = new JLabel(ENABLED_FIELD_LABEL);
        JLabel descLabel = new JLabel(DESC_FIELD_LABEL);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(nameLabel)
                                        .addComponent(enabledLabel)
                                        .addComponent(descLabel))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(getRegexTextField())
                                        .addComponent(getEnabledCheckBox())
                                        .addComponent(getDescTextField())));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(nameLabel)
                                        .addComponent(getRegexTextField()))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(enabledLabel)
                                        .addComponent(getEnabledCheckBox()))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(descLabel)
                                        .addComponent(getDescTextField())));

        return fieldsPanel;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    @Override
    protected void init() {
        getRegexTextField().setText("");
        getEnabledCheckBox().setSelected(true);
        getDescTextField().setText("");
        token = null;
    }

    @Override
    protected boolean validateFields() {
        String tokenName = getRegexTextField().getText();
        for (GlobalExcludeURLParamToken t : tokens) {
            if (tokenName.equals(t.getRegex())) {
                JOptionPane.showMessageDialog(
                        this,
                        TEXT_NAME_REPEATED_DIALOG,
                        TITLE_NAME_REPEATED_DIALOG,
                        JOptionPane.INFORMATION_MESSAGE);
                getRegexTextField().requestFocusInWindow();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void performAction() {
        token =
                new GlobalExcludeURLParamToken(
                        getRegexTextField().getText(),
                        getDescTextField().getText(),
                        getEnabledCheckBox().isSelected());
    }

    @Override
    protected void clearFields() {
        getRegexTextField().setText("");
        getRegexTextField().discardAllEdits();
        getDescTextField().setText("");
        getDescTextField().discardAllEdits();
    }

    public GlobalExcludeURLParamToken getToken() {
        return token;
    }

    protected ZapTextField getRegexTextField() {
        if (regexTextField == null) {
            regexTextField = new ZapTextField(25);
            regexTextField
                    .getDocument()
                    .addDocumentListener(
                            new DocumentListener() {

                                @Override
                                public void removeUpdate(DocumentEvent e) {
                                    checkAndEnableConfirmButton();
                                }

                                @Override
                                public void insertUpdate(DocumentEvent e) {
                                    checkAndEnableConfirmButton();
                                }

                                @Override
                                public void changedUpdate(DocumentEvent e) {
                                    checkAndEnableConfirmButton();
                                }

                                private void checkAndEnableConfirmButton() {
                                    setConfirmButtonEnabled(
                                            getRegexTextField().getDocument().getLength() > 0);
                                }
                            });
        }

        return regexTextField;
    }

    protected JCheckBox getEnabledCheckBox() {
        if (enabledCheckBox == null) {
            enabledCheckBox = new JCheckBox();
        }

        return enabledCheckBox;
    }

    protected ZapTextField getDescTextField() {
        if (descTextField == null) {
            descTextField = new ZapTextField(25);
            descTextField
                    .getDocument()
                    .addDocumentListener(
                            new DocumentListener() {

                                @Override
                                public void removeUpdate(DocumentEvent e) {
                                    checkAndEnableConfirmButton();
                                }

                                @Override
                                public void insertUpdate(DocumentEvent e) {
                                    checkAndEnableConfirmButton();
                                }

                                @Override
                                public void changedUpdate(DocumentEvent e) {
                                    checkAndEnableConfirmButton();
                                }

                                private void checkAndEnableConfirmButton() {
                                    setConfirmButtonEnabled(
                                            getDescTextField().getDocument().getLength() > 0);
                                }
                            });
        }

        return descTextField;
    }

    public void setTokens(List<GlobalExcludeURLParamToken> tokens) {
        this.tokens = tokens;
    }

    public void clear() {
        this.tokens = null;
        this.token = null;
    }
}
