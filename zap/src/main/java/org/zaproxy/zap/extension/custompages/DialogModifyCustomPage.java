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
package org.zaproxy.zap.extension.custompages;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.parosproxy.paros.Constant;

class DialogModifyCustomPage extends DialogAddCustomPage {

    private static final long serialVersionUID = 7828871270310672334L;
    private static final String DIALOG_TITLE =
            Constant.messages.getString("custompages.dialog.modify.title");

    public DialogModifyCustomPage(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    public void setCustomPage(CustomPage customPage) {
        this.customPage = customPage;
    }

    @Override
    protected String getConfirmButtonLabel() {
        return Constant.messages.getString("custompages.dialog.modify.button.confirm");
    }

    @Override
    protected void init() {
        if (this.workingContext == null) {
            throw new IllegalStateException(
                    "A working Context should be set before setting the 'Add Dialog' visible.");
        }
        LOGGER.debug("Initializing modify Custom Page dialog for: {}", customPage);

        // Handle escape key to close the dialog
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        AbstractAction escapeAction =
                new AbstractAction() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        DialogModifyCustomPage.this.setVisible(false);
                        DialogModifyCustomPage.this.dispose();
                    }
                };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        getEnabledCheckBox().setSelected(customPage.isEnabled());
        getPageMatcherTextField().setText(customPage.getPageMatcher());
        getCustomPagePageMatcherLocationsCombo()
                .setSelectedItem(customPage.getPageMatcherLocation());
        getRegexCheckBox().setSelected(customPage.isRegex());
        getCustomPageTypesCombo().setSelectedItem(customPage.getType());

        this.setConfirmButtonEnabled(true);
        this.pack();
    }
}
