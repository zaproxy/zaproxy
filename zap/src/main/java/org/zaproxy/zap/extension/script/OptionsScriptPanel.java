/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.extension.script;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.AbstractMultipleOptionsBaseTablePanel;

@SuppressWarnings("serial")
public class OptionsScriptPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;

    private ExtensionScript extension;
    private AntiCsrfMultipleOptionsPanel tokensOptionsPanel;
    private OptionsScriptTableModel scriptDirModel = null;

    public OptionsScriptPanel(ExtensionScript extension) {
        super();
        this.extension = extension;
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setName(Constant.messages.getString("options.script.title"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(new ZapHtmlLabel(Constant.messages.getString("options.script.label.dirs")), gbc);

        tokensOptionsPanel = new AntiCsrfMultipleOptionsPanel(this.extension, getScriptDirModel());

        gbc.weighty = 1.0;
        this.add(tokensOptionsPanel, gbc);
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        ScriptParam param = optionsParam.getParamSet(ScriptParam.class);
        getScriptDirModel().setTokens(param.getScriptDirs());
        tokensOptionsPanel.setRemoveWithoutConfirmation(!param.isConfirmRemoveDir());
    }

    @Override
    public void saveParam(Object obj) throws Exception {
        OptionsParam optionsParam = (OptionsParam) obj;
        ScriptParam param = optionsParam.getParamSet(ScriptParam.class);

        // Work out whats changed..
        List<File> dirs = getScriptDirModel().getElements();
        // Look for removed ones
        for (File dir : param.getScriptDirs()) {
            if (!dirs.contains(dir)) {
                // Its been removed
                extension.removeScriptsFromDir(dir);
            }
        }
        // Look for added ones
        for (File dir : dirs) {
            if (!param.getScriptDirs().contains(dir)) {
                // Its been added
                extension.addScriptsFromDir(dir);
            }
        }

        param.setScriptDirs(dirs);
        param.setConfirmRemoveDir(!tokensOptionsPanel.isRemoveWithoutConfirmation());
    }

    /**
     * This method initializes authModel
     *
     * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel
     */
    private OptionsScriptTableModel getScriptDirModel() {
        if (scriptDirModel == null) {
            scriptDirModel = new OptionsScriptTableModel();
        }
        return scriptDirModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.script";
    }

    private static class AntiCsrfMultipleOptionsPanel
            extends AbstractMultipleOptionsBaseTablePanel<File> {

        private static final long serialVersionUID = -115340627058929308L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("options.script.dialog.dirs.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("options.script.dialog.dirs.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString("options.script.dialog.dirs.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString("options.script.dialog.dirs.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString("options.script.dialog.dirs.remove.checkbox.label");

        private static ExtensionScript extension;

        public AntiCsrfMultipleOptionsPanel(ExtensionScript ext, OptionsScriptTableModel model) {
            super(model);
            extension = ext;
            getTable().setSortOrder(0, SortOrder.ASCENDING);
        }

        @Override
        public File showAddDialogue() {
            return showDirSelectDialog(null);
        }

        @Override
        public File showModifyDialogue(File dir) {
            return showDirSelectDialog(dir);
        }

        private File showDirSelectDialog(File dir) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (extension.getScriptCount(file) == 0) {
                    // Cant find any valid scripts
                    if (View.getSingleton()
                                    .showConfirmDialog(
                                            this,
                                            Constant.messages.getString(
                                                    "options.script.dialog.dirs.noscripts.warning"))
                            == JFileChooser.APPROVE_OPTION) {
                        return file;
                    }
                } else {
                    return file;
                }
            }
            return null;
        }

        @Override
        public boolean showRemoveDialogue(File f) {
            JCheckBox removeWithoutConfirmationCheckBox =
                    new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
            int option =
                    JOptionPane.showOptionDialog(
                            View.getSingleton().getMainFrame(),
                            messages,
                            REMOVE_DIALOG_TITLE,
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[] {
                                REMOVE_DIALOG_CONFIRM_BUTTON_LABEL,
                                REMOVE_DIALOG_CANCEL_BUTTON_LABEL
                            },
                            null);

            if (option == JOptionPane.OK_OPTION) {
                setRemoveWithoutConfirmation(removeWithoutConfirmationCheckBox.isSelected());

                return true;
            }

            return false;
        }
    }
}
