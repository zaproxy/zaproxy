/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.zap.extension.alert;

import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;
import org.zaproxy.zap.view.LayoutHelper;

@SuppressWarnings("serial")
public class DialogAddAlertTag extends AbstractFormDialog {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG_TITLE =
            Constant.messages.getString("alert.tags.dialog.add.title");
    private static final String CONFIRM_BUTTON_LABEL =
            Constant.messages.getString("alert.tags.dialog.add.button.confirm");
    private static final String KEY_FIELD_LABEL =
            Constant.messages.getString("alert.tags.dialog.add.key");
    private static final String VALUE_FIELD_LABEL =
            Constant.messages.getString("alert.tags.dialog.add.value");
    private static final String REPEATED_TAG_KEY_TITLE =
            Constant.messages.getString("alert.tags.dialog.warning.title.repeated.key");
    private static final String REPEATED_TAG_KEY_BODY =
            Constant.messages.getString("alert.tags.dialog.warning.body.repeated.key");
    protected static final int MAX_KEY_LENGTH = 1024;
    protected static final int MAX_VALUE_LENGTH = 4000;

    private ZapTextField keyTextField;
    private ZapTextArea valueTextArea;
    private JScrollPane valueTextAreaScrollPane;
    private ConfirmButtonValidatorDocListener confirmButtonValidatorDocListener;

    protected AlertTagsTableModel model;

    public DialogAddAlertTag(Dialog owner, AlertTagsTableModel model) {
        this(owner, model, DIALOG_TITLE);
    }

    protected DialogAddAlertTag(Dialog owner, AlertTagsTableModel model, String title) {
        super(owner, title);
        this.model = model;
    }

    @Override
    protected JPanel getFieldsPanel() {
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        JLabel keyLabel = new JLabel(KEY_FIELD_LABEL);
        JLabel valueLabel = new JLabel(VALUE_FIELD_LABEL);

        int gbcRow = 0;
        fieldsPanel.add(
                keyLabel,
                LayoutHelper.getGBC(
                        0,
                        gbcRow,
                        1,
                        0,
                        0.1,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2)));
        fieldsPanel.add(
                getKeyTextField(),
                LayoutHelper.getGBC(
                        1,
                        gbcRow,
                        1,
                        1,
                        0.1,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2)));
        gbcRow++;
        fieldsPanel.add(
                valueLabel,
                LayoutHelper.getGBC(
                        0,
                        gbcRow,
                        1,
                        0,
                        0.1,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2)));
        gbcRow++;
        fieldsPanel.add(
                getValueTextAreaScrollPane(),
                LayoutHelper.getGBC(
                        0, gbcRow, 2, 1, 0.5, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2)));
        return fieldsPanel;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    @Override
    protected void init() {
        getKeyTextField().setText("");
        getValueTextArea().setText("");
    }

    @Override
    protected boolean validateFields() {
        return validateKey() && validateValue();
    }

    protected boolean validateKey() {
        String key = getKeyTextField().getText().trim();
        if (model.getTags().containsKey(key)) {
            JOptionPane.showMessageDialog(
                    this,
                    REPEATED_TAG_KEY_BODY,
                    REPEATED_TAG_KEY_TITLE,
                    JOptionPane.INFORMATION_MESSAGE);
            getKeyTextField().requestFocusInWindow();
            return false;
        }
        int len = getKeyTextField().getDocument().getLength();
        return len > 0 && len <= MAX_KEY_LENGTH;
    }

    protected boolean validateValue() {
        return getValueTextArea().getDocument().getLength() <= MAX_VALUE_LENGTH;
    }

    @Override
    protected void performAction() {
        model.addTag(getKeyTextField().getText(), getValueTextArea().getText());
    }

    @Override
    protected void clearFields() {
        getKeyTextField().setText("");
        getKeyTextField().discardAllEdits();

        getValueTextArea().setText("");
        getValueTextArea().discardAllEdits();
    }

    protected ZapTextField getKeyTextField() {
        if (keyTextField == null) {
            keyTextField = new ZapTextField(20);
            keyTextField.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }
        return keyTextField;
    }

    protected ZapTextArea getValueTextArea() {
        if (valueTextArea == null) {
            valueTextArea = new ZapTextArea(5, 20);
            valueTextArea.setLineWrap(true);
            valueTextArea.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }
        return valueTextArea;
    }

    protected JScrollPane getValueTextAreaScrollPane() {
        if (valueTextAreaScrollPane == null) {
            valueTextAreaScrollPane = new JScrollPane(getValueTextArea());
            valueTextAreaScrollPane.setHorizontalScrollBarPolicy(
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            valueTextAreaScrollPane.setVerticalScrollBarPolicy(
                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        }
        return valueTextAreaScrollPane;
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
            int keyLen = getKeyTextField().getDocument().getLength();
            int valueLen = getValueTextArea().getDocument().getLength();
            boolean enabled =
                    keyLen > 0 && keyLen <= MAX_KEY_LENGTH && valueLen <= MAX_VALUE_LENGTH;
            setConfirmButtonEnabled(enabled);
        }
    }
}
