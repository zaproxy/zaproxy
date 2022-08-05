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
package org.zaproxy.zap.extension.ruleconfig;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.ZapTextArea;
import org.zaproxy.zap.view.StandardFieldsDialog;

@SuppressWarnings("serial")
public class DialogEditRuleConfig extends StandardFieldsDialog {

    private static final String FIELD_KEY = "ruleconfig.dialog.label.key";
    private static final String FIELD_VALUE = "ruleconfig.dialog.label.value";
    private static final String FIELD_DEFAULT = "ruleconfig.dialog.label.default";
    private static final String FIELD_DESC = "ruleconfig.dialog.label.desc";

    private static final long serialVersionUID = 1L;

    private JButton resetButton;
    private RuleConfig rc;
    private RuleConfigTableModel model;

    public DialogEditRuleConfig(Window owner) {
        super(owner, "ruleconfig.dialog.title", new Dimension(400, 300), true);
    }

    public void init(RuleConfig rc, RuleConfigTableModel model) {
        this.rc = rc;
        this.model = model;

        this.removeAllFields();

        this.addReadOnlyField(FIELD_KEY, rc.getKey(), false);
        this.addReadOnlyField(FIELD_DEFAULT, rc.getDefaultValue(), false);
        this.addTextField(FIELD_VALUE, rc.getValue());
        String desc = "";
        if (Constant.messages.containsKey(rc.getKey())) {
            desc = Constant.messages.getString(rc.getKey());
        }
        this.addMultilineField(FIELD_DESC, desc);
        ZapTextArea descField = (ZapTextArea) this.getField(FIELD_DESC);
        descField.setEditable(false);
        descField.setWrapStyleWord(true);
    }

    private JButton getResetButton() {
        if (resetButton == null) {
            resetButton =
                    new JButton(Constant.messages.getString("ruleconfig.dialog.button.reset"));
            resetButton.addActionListener(
                    new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            setFieldValue(FIELD_VALUE, rc.getDefaultValue());
                        }
                    });
        }
        return resetButton;
    }

    @Override
    public JButton[] getExtraButtons() {
        return new JButton[] {getResetButton()};
    }

    @Override
    public void save() {
        if (!this.getStringValue(FIELD_VALUE).equals(this.rc.getValue())) {
            this.model.setRuleConfigValue(rc.getKey(), this.getStringValue(FIELD_VALUE));
        }
    }

    @Override
    public String validateFields() {
        // Nothing to do
        return null;
    }
}
