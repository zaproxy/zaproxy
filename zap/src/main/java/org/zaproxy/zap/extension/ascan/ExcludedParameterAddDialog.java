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
package org.zaproxy.zap.extension.ascan;

import java.awt.Dialog;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.NameValuePair;
import org.parosproxy.paros.core.scanner.ScannerParamFilter;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;

@SuppressWarnings("serial")
class ExcludedParameterAddDialog extends AbstractFormDialog {

    private static final long serialVersionUID = 1L;
    private static final String DIALOG_TITLE =
            Constant.messages.getString("variant.options.excludedparam.dialog.token.add.title");
    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString(
                    "variant.options.excludedparam.dialog.token.add.button.confirm");

    private static final String NAME_FIELD_LABEL =
            Constant.messages.getString("variant.options.excludedparam.table.header.name");
    private static final String TYPE_FIELD_LABEL =
            Constant.messages.getString("variant.options.excludedparam.table.header.type");
    private static final String URL_FIELD_LABEL =
            Constant.messages.getString("variant.options.excludedparam.table.header.url");

    private static final String TITLE_NAME_REPEATED_DIALOG =
            Constant.messages.getString(
                    "variant.options.excludedparam.dialog.token.warning.name.repeated.title");
    private static final String TEXT_NAME_REPEATED_DIALOG =
            Constant.messages.getString(
                    "variant.options.excludedparam.dialog.token.warning.name.repeated.text");

    private static final String TITLE_WARNING_INVALID_REGEX =
            Constant.messages.getString(
                    "variant.options.excludedparam.dialog.token.warning.invalid.regex.title");
    private static final String MESSAGE_INVALID_NAME_REGEX =
            Constant.messages.getString(
                    "variant.options.excludedparam.dialog.token.warning.invalid.regex.field.name");

    private ZapTextField nameTextField;
    private ZapTextField urlTextField;
    private JComboBox<String> typeTextField;

    protected ScannerParamFilter token;
    private List<ScannerParamFilter> tokens;

    public ExcludedParameterAddDialog(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    protected ExcludedParameterAddDialog(Dialog owner, String title) {
        super(owner, title);
    }

    @Override
    protected JPanel getFieldsPanel() {
        JPanel fieldsPanel = new JPanel();

        GroupLayout layout = new GroupLayout(fieldsPanel);
        fieldsPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        JLabel nameLabel = new JLabel(NAME_FIELD_LABEL);
        JLabel whereLabel = new JLabel(TYPE_FIELD_LABEL);
        JLabel urlLabel = new JLabel(URL_FIELD_LABEL);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(nameLabel)
                                        .addComponent(whereLabel)
                                        .addComponent(urlLabel))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(getNameTextField())
                                        .addComponent(getWhereComboField())
                                        .addComponent(getUrlTextField())));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(nameLabel)
                                        .addComponent(getNameTextField()))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(whereLabel)
                                        .addComponent(getWhereComboField()))
                        .addGroup(
                                layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(urlLabel)
                                        .addComponent(getUrlTextField())));

        return fieldsPanel;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    @Override
    protected void init() {
        getNameTextField().setText("");
        getUrlTextField().setText("*");
        getWhereComboField()
                .setSelectedItem(ScannerParamFilter.getStringType(NameValuePair.TYPE_UNDEFINED));
        token = null;
    }

    @Override
    protected boolean validateFields() {
        if (!validateName(getNameTextField().getText())) {
            return false;
        }

        if (!ScannerParamFilter.isValidParamNameRegex(getNameTextField().getText())) {
            JOptionPane.showMessageDialog(
                    this,
                    MESSAGE_INVALID_NAME_REGEX,
                    TITLE_WARNING_INVALID_REGEX,
                    JOptionPane.WARNING_MESSAGE);
            getNameTextField().requestFocusInWindow();
            return false;
        }

        return true;
    }

    protected boolean validateName(String tokenName) {
        for (ScannerParamFilter t : tokens) {
            if (tokenName.equals(t.getParamName())) {
                JOptionPane.showMessageDialog(
                        this,
                        TEXT_NAME_REPEATED_DIALOG,
                        TITLE_NAME_REPEATED_DIALOG,
                        JOptionPane.INFORMATION_MESSAGE);

                getNameTextField().requestFocusInWindow();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void performAction() {
        token = new ScannerParamFilter();
        token.setParamName(getNameTextField().getText());
        token.setWildcardedUrl(getUrlTextField().getText());
        token.setType((String) getWhereComboField().getSelectedItem());
    }

    @Override
    protected void clearFields() {
        getNameTextField().setText("");
        getNameTextField().discardAllEdits();
        getUrlTextField().setText("*");
        getUrlTextField().discardAllEdits();
        getWhereComboField()
                .setSelectedItem(ScannerParamFilter.getStringType(NameValuePair.TYPE_UNDEFINED));
    }

    public ScannerParamFilter getToken() {
        return token;
    }

    protected ZapTextField getNameTextField() {
        if (nameTextField == null) {
            nameTextField = new ZapTextField(25);
            nameTextField
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
                                            getNameTextField().getDocument().getLength() > 0);
                                }
                            });
        }

        return nameTextField;
    }

    protected ZapTextField getUrlTextField() {
        if (urlTextField == null) {
            urlTextField = new ZapTextField(25);
        }

        return urlTextField;
    }

    protected JComboBox<String> getWhereComboField() {
        if (typeTextField == null) {
            typeTextField = new JComboBox<>();
            for (String where : ScannerParamFilter.getListTypes()) {
                typeTextField.addItem(where);
            }
        }

        return typeTextField;
    }

    public void setTokens(List<ScannerParamFilter> tokens) {
        this.tokens = tokens;
    }

    public void clear() {
        this.tokens = null;
        this.token = null;
    }
}
