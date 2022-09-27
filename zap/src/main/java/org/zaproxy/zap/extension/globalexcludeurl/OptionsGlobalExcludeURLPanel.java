/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SortOrder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.OptionsParam;
import org.parosproxy.paros.view.AbstractParamPanel;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.utils.ZapHtmlLabel;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;

public class OptionsGlobalExcludeURLPanel extends AbstractParamPanel {

    private static final long serialVersionUID = 1L;

    private GlobalExcludeURLMultipleOptionsPanel tokensOptionsPanel;

    private OptionsGlobalExcludeURLTableModel globalExcludeURLModel = null;

    public OptionsGlobalExcludeURLPanel() {
        super();
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setName(Constant.messages.getString("options.globalexcludeurl.title"));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.BOTH;

        this.add(
                new ZapHtmlLabel(
                        Constant.messages.getString("options.globalexcludeurl.label.tokens")),
                gbc);

        tokensOptionsPanel = new GlobalExcludeURLMultipleOptionsPanel(getGlobalExcludeURLModel());

        gbc.weighty = 1.0;
        this.add(tokensOptionsPanel, gbc);

        // gbc.weighty = 0.0;
    }

    @Override
    public void initParam(Object obj) {
        OptionsParam optionsParam = (OptionsParam) obj;
        GlobalExcludeURLParam param = optionsParam.getGlobalExcludeURLParam();
        getGlobalExcludeURLModel().setTokens(param.getTokens());
        tokensOptionsPanel.setRemoveWithoutConfirmation(!param.isConfirmRemoveToken());
    }

    @Override
    public void saveParam(Object obj) throws Exception {

        OptionsParam optionsParam = (OptionsParam) obj;
        GlobalExcludeURLParam globalExcludeURLParam = optionsParam.getGlobalExcludeURLParam();
        globalExcludeURLParam.setTokens(getGlobalExcludeURLModel().getElements());
        globalExcludeURLParam.setConfirmRemoveToken(
                !tokensOptionsPanel.isRemoveWithoutConfirmation());
    }

    /**
     * This method initializes authModel
     *
     * @return org.parosproxy.paros.view.OptionsAuthenticationTableModel
     */
    private OptionsGlobalExcludeURLTableModel getGlobalExcludeURLModel() {
        if (globalExcludeURLModel == null) {
            globalExcludeURLModel = new OptionsGlobalExcludeURLTableModel();
        }
        return globalExcludeURLModel;
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.options.globalexcludeurl";
    }

    private static class GlobalExcludeURLMultipleOptionsPanel
            extends AbstractMultipleOptionsTablePanel<GlobalExcludeURLParamToken> {

        private static final long serialVersionUID = -115340627058929308L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("options.globalexcludeurl.dialog.token.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("options.globalexcludeurl.dialog.token.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString(
                        "options.globalexcludeurl.dialog.token.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString(
                        "options.globalexcludeurl.dialog.token.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString(
                        "options.globalexcludeurl.dialog.token.remove.checkbox.label");

        private DialogAddToken addDialog = null;
        private DialogModifyToken modifyDialog = null;

        private OptionsGlobalExcludeURLTableModel model;

        public GlobalExcludeURLMultipleOptionsPanel(OptionsGlobalExcludeURLTableModel model) {
            super(model);

            this.model = model;

            getTable().getColumnExt(0).setPreferredWidth(25); // checkbox column should be tiny
            getTable().getColumnExt(1).setPreferredWidth(350); // wide Desc Col
            getTable().getColumnExt(2).setPreferredWidth(300); // less wide Regex Column
            getTable()
                    .setHorizontalScrollEnabled(
                            true); // descriptions / regexs are very wide, so turn on horiz scroll

            getTable().setAutoCreateRowSorter(true);
            getTable().setSortOrder(1, SortOrder.ASCENDING); // sort by description by default

            /* XXX For some reason, sorting isn't accurate in the table.  The
            getColumnClass correctly returns String for the Description, but
            sort order might be: "Ext - MS Word, ExtParam - Bla bla, Ext - PDF."
            It is like string.compare for the column sort ignores the space and
            dash.  Really unsure as to why.  As a work-around, start some
            descriptions with "Extension" instead of the shorter "Ext"   */
        }

        @Override
        public GlobalExcludeURLParamToken showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddToken(View.getSingleton().getOptionsDialog(null));
                addDialog.pack();
            }
            addDialog.setTokens(model.getElements());
            addDialog.setVisible(true);

            GlobalExcludeURLParamToken token = addDialog.getToken();
            addDialog.clear();

            return token;
        }

        @Override
        public GlobalExcludeURLParamToken showModifyDialogue(GlobalExcludeURLParamToken e) {
            if (modifyDialog == null) {
                modifyDialog = new DialogModifyToken(View.getSingleton().getOptionsDialog(null));
                modifyDialog.pack();
            }
            modifyDialog.setTokens(model.getElements());
            modifyDialog.setToken(e);
            modifyDialog.setVisible(true);

            GlobalExcludeURLParamToken token = modifyDialog.getToken();
            modifyDialog.clear();

            if (!token.equals(e)) {
                return token;
            }

            return null;
        }

        @Override
        public boolean showRemoveDialogue(GlobalExcludeURLParamToken e) {
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
