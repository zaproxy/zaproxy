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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.view.AbstractContextPropertiesPanel;
import org.zaproxy.zap.view.AbstractMultipleOptionsTablePanel;
import org.zaproxy.zap.view.LayoutHelper;

@SuppressWarnings("serial")
class ContextCustomPagePanel extends AbstractContextPropertiesPanel {

    // The i18n prefix
    protected static final String PREFIX = "custompages";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -3920598166129639573L;

    private static final String PANEL_NAME = Constant.messages.getString(PREFIX + ".panel.title");

    private CustomPagesMultipleOptionsPanel customPagesOptionsPanel;
    private Context context;
    private CustomPageTableModel customPageTableModel;

    public ContextCustomPagePanel(int contextId) {
        super(contextId);
        this.context = Model.getSingleton().getSession().getContext(contextId);
        initialize();
    }

    public static String getPanelName(int contextId) {
        // Panel names have to be unique, so prefix with the context id
        return contextId + ": " + PANEL_NAME;
    }

    private void initialize() {
        this.setLayout(new CardLayout());
        this.setName(getPanelName(getContextId()));
        this.setLayout(new GridBagLayout());

        this.add(
                new JLabel(Constant.messages.getString(PREFIX + ".panel.description")),
                LayoutHelper.getGBC(0, 0, 1, 1.0d, 0.0d));

        customPageTableModel = new CustomPageTableModel();
        customPagesOptionsPanel = new CustomPagesMultipleOptionsPanel(customPageTableModel);
        this.add(customPagesOptionsPanel, LayoutHelper.getGBC(0, 1, 1, 1.0d, 1.0d));
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.contexts";
    }

    private static class CustomPagesMultipleOptionsPanel
            extends AbstractMultipleOptionsTablePanel<CustomPage> {

        private static final long serialVersionUID = -7216673905642941770L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString(PREFIX + ".dialog.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString(PREFIX + ".dialog.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString(PREFIX + ".dialog.remove.button.confirm");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString(PREFIX + ".dialog.remove.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString(PREFIX + ".dialog.remove.checkbox.label");

        private DialogAddCustomPage addDialog = null;
        private DialogModifyCustomPage modifyDialog = null;
        private Context uiSharedContext;

        public CustomPagesMultipleOptionsPanel(CustomPageTableModel model) {
            super(model);

            Component rendererComponent;
            if (getTable().getColumnExt(0).getHeaderRenderer() == null) { // If there isn't a header
                // renderer then get the
                // default renderer
                rendererComponent =
                        getTable()
                                .getTableHeader()
                                .getDefaultRenderer()
                                .getTableCellRendererComponent(
                                        null,
                                        getTable().getColumnExt(0).getHeaderValue(),
                                        false,
                                        false,
                                        0,
                                        0);
            } else { // If there is a custom renderer then get it
                rendererComponent =
                        getTable()
                                .getColumnExt(0)
                                .getHeaderRenderer()
                                .getTableCellRendererComponent(
                                        null,
                                        getTable().getColumnExt(0).getHeaderValue(),
                                        false,
                                        false,
                                        0,
                                        0);
            }

            getTable().getColumnExt(0).setMaxWidth(rendererComponent.getMaximumSize().width);
            getTable().packAll();
        }

        @Override
        public CustomPage showAddDialogue() {
            if (addDialog == null) {
                addDialog = new DialogAddCustomPage(View.getSingleton().getSessionDialog());
            }
            addDialog.setWorkingContext(this.uiSharedContext);
            addDialog.setVisible(true);

            CustomPage customPage = addDialog.getCustomPage();
            addDialog.clear();

            return customPage;
        }

        @Override
        public CustomPage showModifyDialogue(CustomPage customPage) {
            if (modifyDialog == null) {
                modifyDialog = new DialogModifyCustomPage(View.getSingleton().getSessionDialog());
            }
            modifyDialog.setWorkingContext(this.uiSharedContext);
            modifyDialog.setCustomPage(customPage);
            modifyDialog.setVisible(true);

            customPage = modifyDialog.getCustomPage();
            modifyDialog.clear();

            return customPage;
        }

        @Override
        public boolean showRemoveDialogue(CustomPage customPage) {
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

        protected void setWorkingContext(Context context) {
            this.uiSharedContext = context;
        }
    }

    @Override
    public void initContextData(Session session, Context uiCommonContext) {
        this.customPagesOptionsPanel.setWorkingContext(uiCommonContext);
        getCustomPagesTableModel().setCustomPages(uiCommonContext.getCustomPages());
    }

    @Override
    public void validateContextData(Session session) throws Exception {
        // Nothing to validate
    }

    @Override
    public void saveContextData(Session session) throws Exception {
        this.context.setCustomPages(customPageTableModel.getCustomPages());
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        // Data is already saved in the uiSharedContext
    }

    protected CustomPageTableModel getCustomPagesTableModel() {
        return customPageTableModel;
    }
}
