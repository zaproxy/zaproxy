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
package org.zaproxy.zap.extension.keyboard;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.KeyStroke;
import org.parosproxy.paros.Constant;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.StandardFieldsDialog;

@SuppressWarnings("serial")
public class DialogEditShortcut extends StandardFieldsDialog {

    private static final String FIELD_ACTION = "keyboard.dialog.label.action";
    private static final String FIELD_PREVIEW = "keyboard.dialog.label.preview";
    private static final String FIELD_KEY = "keyboard.dialog.label.key";
    private static final String FIELD_INFO = "keyboard.dialog.label.info";

    private static final long serialVersionUID = 1L;

    private KeyboardShortcut shortcut;
    private KeyboardShortcutTableModel model;

    /**
     * Constructs a modal {@code DialogEditShortcut}, with the given {@code Window} as its owner.
     *
     * @param owner the owner of the dialogue
     * @since 2.5.0
     */
    public DialogEditShortcut(Window owner) {
        super(owner, "keyboard.dialog.title", DisplayUtils.getScaledDimension(300, 200), true);
    }

    public DialogEditShortcut(Frame owner) {
        super(owner, "keyboard.dialog.title", DisplayUtils.getScaledDimension(300, 200));
    }

    public void init(KeyboardShortcut shortcut, KeyboardShortcutTableModel model) {
        this.shortcut = shortcut;
        this.model = model;

        this.removeAllFields();

        ActionListener listener =
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        updatePreview();
                        checkDuplicate();
                    }
                };

        KeyStrokeCaptureField captureField = new KeyStrokeCaptureField(shortcut.getKeyStroke());

        this.addReadOnlyField(FIELD_ACTION, shortcut.getName(), false);
        this.addReadOnlyField(FIELD_PREVIEW, captureField.getText(), false);
        this.addCustomComponent(FIELD_KEY, captureField);
        this.addFieldListener(FIELD_KEY, listener);
        this.addReadOnlyField(FIELD_INFO, "", true);

        this.getField(FIELD_INFO).setForeground(Color.RED);

        // Let the subsequent pack() size the dialogue to fit the fields added above,
        // rather than the fixed size used for the initial (empty) layout.
        this.getContentPane().setPreferredSize(null);
    }

    @Override
    public String getSaveButtonText() {
        // Not really saving, just setting here..
        return Constant.messages.getString("keyboard.dialog.button.save");
    }

    private void updatePreview() {
        this.setFieldValue(
                FIELD_PREVIEW, ((KeyStrokeCaptureField) this.getField(FIELD_KEY)).getText());
    }

    /**
     * Checks to see if the chosen shortcut is already being used and if so shows a message warning
     * the user
     */
    private void checkDuplicate() {
        KeyboardShortcut ks = this.getDuplicate();
        if (ks != null) {
            this.setFieldValue(
                    FIELD_INFO,
                    Constant.messages.getString("keyboard.dialog.warning.dup", ks.getName()));
        } else {
            this.setFieldValue(FIELD_INFO, "");
        }
    }

    private KeyboardShortcut getDuplicate() {
        KeyStroke chosenKs = this.getKeyStroke();
        if (chosenKs != null) {
            for (KeyboardShortcut ks : this.model.getElements()) {
                if (!ks.equals(this.shortcut)) {
                    KeyStroke testKs = ks.getKeyStroke();
                    if (testKs != null
                            && chosenKs.getKeyCode() == testKs.getKeyCode()
                            && chosenKs.getModifiers() == testKs.getModifiers()) {
                        return ks;
                    }
                }
            }
        }
        return null;
    }

    public KeyStroke getKeyStroke() {
        return ((KeyStrokeCaptureField) this.getField(FIELD_KEY)).getKeyStroke();
    }

    @Override
    public void save() {
        KeyboardShortcut ksDup = this.getDuplicate();
        if (ksDup != null) {
            // used for another shortcut, so remove it from that
            ksDup.setKeyStroke(null);
        }
        KeyStroke ks = getKeyStroke();
        shortcut.setKeyStroke(ks);
    }

    @Override
    public String validateFields() {
        // Nothing to do
        return null;
    }
}
