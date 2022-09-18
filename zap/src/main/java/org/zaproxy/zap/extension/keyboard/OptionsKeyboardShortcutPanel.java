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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdesktop.swingx.JXTable;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.extension.api.API;
import org.zaproxy.zap.utils.DesktopUtils;
import org.zaproxy.zap.view.MultipleOptionsTablePanel;
import org.zaproxy.zap.view.panels.TableFilterPanel;

@SuppressWarnings("serial")
public class OptionsKeyboardShortcutPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LogManager.getLogger(OptionsKeyboardShortcutPanel.class);

    private ExtensionKeyboard extension;
    private KeyboardOptionsPanel tkeyboardOptionsPanel;
    private JButton resetButton = null;
    private JButton cheatsheetAction = null;
    private JButton cheatsheetKey = null;
    private boolean reset = false;

    private KeyboardShortcutTableModel keyboardModel = null;

    public OptionsKeyboardShortcutPanel(ExtensionKeyboard extension) {
        super();
        this.extension = extension;
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setName(Constant.messages.getString("keyboard.options.title"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tkeyboardOptionsPanel = new KeyboardOptionsPanel(getShortcutModel());

        this.add(new TableFilterPanel<>(tkeyboardOptionsPanel.getTableImpl()), gbc);

        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(tkeyboardOptionsPanel, gbc);

        gbc.gridy++;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        gbc.weightx = 1.0;
        this.add(new JLabel(), gbc); // Spacer
        gbc.weightx = 0.0;

        if (DesktopUtils.canOpenUrlInBrowser()) {
            // No point in showing these if they cant be used..
            gbc.gridx++;
            this.add(getCheatsheetAction(), gbc);
            gbc.gridx++;
            this.add(getCheatsheetKey(), gbc);
        }
        gbc.gridx++;
        this.add(getResetButton(), gbc);

        gbc.gridx++;
        gbc.weightx = 1.0;
        this.add(new JLabel(), gbc); // Spacer
    }

    @Override
    public void initParam(Object obj) {
        this.setShortcuts(extension.getShortcuts());
        // The API might have been enabled or disabled
        this.getCheatsheetAction().setEnabled(API.getInstance().isEnabled());
        this.getCheatsheetKey().setEnabled(API.getInstance().isEnabled());

        tkeyboardOptionsPanel.packAll();
    }

    public void setShortcuts(List<KeyboardShortcut> shortcuts) {
        this.getShortcutModel().setShortcuts(shortcuts);
    }

    public List<KeyboardShortcut> getShortcuts() {
        return getShortcutModel().getElements();
    }

    public void addShortcut(KeyboardShortcut shortcut) {
        getShortcutModel().addShortcut(shortcut);
    }

    private JButton getResetButton() {
        if (resetButton == null) {
            resetButton = new JButton(Constant.messages.getString("keyboard.options.button.reset"));
            resetButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            setShortcuts(extension.getShortcuts(true));
                            reset = true;
                        }
                    });
        }
        return resetButton;
    }

    private JButton getCheatsheetAction() {
        if (cheatsheetAction == null) {
            cheatsheetAction =
                    new JButton(Constant.messages.getString("keyboard.options.button.cheatAction"));
            cheatsheetAction.setToolTipText(
                    Constant.messages.getString("keyboard.options.button.cheatAction.tooltip"));
            cheatsheetAction.setEnabled(API.getInstance().isEnabled());
            cheatsheetAction.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            extension.displayCheatsheetSortedByAction();
                        }
                    });
        }
        return cheatsheetAction;
    }

    private JButton getCheatsheetKey() {
        if (cheatsheetKey == null) {
            cheatsheetKey =
                    new JButton(Constant.messages.getString("keyboard.options.button.cheatKey"));
            cheatsheetKey.setToolTipText(
                    Constant.messages.getString("keyboard.options.button.cheatKey.tooltip"));
            cheatsheetKey.setEnabled(API.getInstance().isEnabled());
            cheatsheetKey.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            extension.displayCheatsheetSortedByKey();
                        }
                    });
        }
        return cheatsheetKey;
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        // Actually set the accelerators for any that have changed
        for (KeyboardShortcut ks : getShortcutModel().getElements()) {
            boolean setShortcut = ks.isChanged();
            if (reset) {
                // check to see if it is the same as the defaults
                KeyboardShortcut tmpKs =
                        new KeyboardShortcut(
                                "temp", "temp", extension.getShortcut(ks.getIdentifier()));
                if (!ks.getKeyStrokeString().equals(tmpKs.getKeyStrokeString())) {
                    // Its different to the default
                    setShortcut = true;
                }
            }
            if (setShortcut) {
                logger.debug(
                        "Setting keyboard shortcut for {} to {}",
                        ks.getIdentifier(),
                        ks.getKeyStroke());
                extension.setShortcut(ks.getIdentifier(), ks.getKeyStroke());
            }
        }
        // Save the configs
        extension.getKeyboardParam().setConfigs();
    }

    /**
     * This method initializes authModel
     *
     * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel
     */
    private KeyboardShortcutTableModel getShortcutModel() {
        if (keyboardModel == null) {
            keyboardModel = new KeyboardShortcutTableModel();
        }
        return keyboardModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.keyboard";
    }

    private static class KeyboardOptionsPanel extends MultipleOptionsTablePanel {

        private static final long serialVersionUID = -115340627058929308L;

        private DialogEditShortcut modifyDialog = null;

        private KeyboardShortcutTableModel model;

        public KeyboardOptionsPanel(final KeyboardShortcutTableModel model) {
            super(model);

            this.model = model;

            // Sort on the menu item names
            getTable().setSortOrder(0, SortOrder.ASCENDING);

            getTable()
                    .addMouseListener(
                            new java.awt.event.MouseAdapter() {
                                @Override
                                public void mousePressed(java.awt.event.MouseEvent e) {

                                    if (SwingUtilities.isLeftMouseButton(e)) {
                                        int row = getTable().getSelectedRow();
                                        if (row >= 0) {
                                            // This is just a single click
                                            showModifyDialogue(
                                                    model.getElements()
                                                            .get(
                                                                    getTable()
                                                                            .convertRowIndexToModel(
                                                                                    row)));
                                        }
                                    }
                                }
                            });
        }

        JXTable getTableImpl() {
            return getTable();
        }

        protected void packAll() {
            getTable().packAll();
        }

        public void showModifyDialogue(KeyboardShortcut shortcut) {
            if (modifyDialog == null) {
                modifyDialog = new DialogEditShortcut(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.init(shortcut, model);
            modifyDialog.setVisible(true);
        }
    }
}
