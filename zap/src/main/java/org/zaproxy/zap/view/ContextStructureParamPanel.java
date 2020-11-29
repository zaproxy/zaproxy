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
package org.zaproxy.zap.view;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SortOrder;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.ParameterParser;
import org.zaproxy.zap.model.StandardParameterParser;
import org.zaproxy.zap.model.StructuralParameter;
import org.zaproxy.zap.utils.ZapTextField;

public class ContextStructureParamPanel extends AbstractContextPropertiesPanel {

    private static final String PANEL_NAME =
            Constant.messages.getString("context.structParams.title");
    private static final long serialVersionUID = -1;

    private StruturalParametersOptionsModel structuralParamsModel;

    private JPanel panelSession = null;
    private ZapTextField urlKvPairSeparators = null;
    private ZapTextField urlKeyValueSeparators = null;
    private ZapTextField postKeyValueSeparators = null;
    private ZapTextField postKvPairSeparators = null;

    /**
     * Returns the name of the panel "Structure" for the given context index.
     *
     * @param contextId the context index that will be used to create the name of the panel
     * @return the name of the panel "Include in context" for the given context index
     * @since 2.2.0
     * @see Context#getId()
     */
    public static String getPanelName(int contextId) {
        // Panel names have to be unique, so prefix with the context id
        return contextId + ": " + PANEL_NAME;
    }

    public ContextStructureParamPanel(Context context) {
        super(context.getId());

        this.setLayout(new CardLayout());
        this.setName(getPanelName(this.getContextId()));
        this.add(getPanel(), getPanel().getName());
    }

    private JPanel getPanel() {
        if (panelSession == null) {
            /*
            +----------------+-----------------------------------------+
            | | + Contexts   | 1: Structure                            |
            | |  + Include   |                                         |
            | |  + Exclude   | URL Key value pair delimiters  [ &    ] |
            | |  + Structure | URL Key value delimiters       [ =    ] |
            | |              | POST Key value pair delimiters [ &    ] |
            | |              | POST Key value delimiters      [ =    ] |
            | |              | Structural Parameters:                  |
            | |              | +-----------------------------+         |
            | |              | |                             | [ Add ] |
            | |              | |                             | [ Mod ] |
            | |              | |                             | [ Rem ] |
            | |              | |                             |         |
            | |              | |                             |         |
            | |              | +-----------------------------+         |
             */
            panelSession = new JPanel();
            panelSession.setLayout(new GridBagLayout());
            panelSession.setName("StructuralParameters");
            panelSession.setLayout(new GridBagLayout());

            panelSession.add(
                    new JLabel(
                            Constant.messages.getString("context.structParams.label.url.kvpsep")),
                    LayoutHelper.getGBC(0, 0, 1, 1.0D));
            panelSession.add(
                    getUrlKvPairSeparators(),
                    LayoutHelper.getGBC(1, 0, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    new JLabel(Constant.messages.getString("context.structParams.label.url.kvsep")),
                    LayoutHelper.getGBC(0, 1, 1, 1.0D));
            panelSession.add(
                    getUrlKeyValueSeparators(),
                    LayoutHelper.getGBC(1, 1, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    new JLabel(
                            Constant.messages.getString("context.structParams.label.post.kvpsep")),
                    LayoutHelper.getGBC(0, 2, 1, 1.0D));
            panelSession.add(
                    getPostKvPairSeparators(),
                    LayoutHelper.getGBC(1, 2, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    new JLabel(
                            Constant.messages.getString("context.structParams.label.post.kvsep")),
                    LayoutHelper.getGBC(0, 3, 1, 1.0D));
            panelSession.add(
                    getPostKeyValueSeparators(),
                    LayoutHelper.getGBC(1, 3, 1, 1.0D, new Insets(2, 0, 2, 0)));

            panelSession.add(
                    new JLabel(Constant.messages.getString("context.structParams.label.struct")),
                    LayoutHelper.getGBC(0, 4, 1, 1.0D));

            structuralParamsModel = new StruturalParametersOptionsModel();
            StructuralParametersOptionsPanel structParamsOptionsPanel =
                    new StructuralParametersOptionsPanel(structuralParamsModel);
            panelSession.add(structParamsOptionsPanel, LayoutHelper.getGBC(0, 5, 2, 1.0d, 1.0d));
        }
        return panelSession;
    }

    private ZapTextField getUrlKvPairSeparators() {
        if (urlKvPairSeparators == null) {
            urlKvPairSeparators = new ZapTextField();
        }
        return urlKvPairSeparators;
    }

    private ZapTextField getUrlKeyValueSeparators() {
        if (urlKeyValueSeparators == null) {
            urlKeyValueSeparators = new ZapTextField();
        }
        return urlKeyValueSeparators;
    }

    private ZapTextField getPostKeyValueSeparators() {
        if (postKeyValueSeparators == null) {
            postKeyValueSeparators = new ZapTextField();
        }
        return postKeyValueSeparators;
    }

    private ZapTextField getPostKvPairSeparators() {
        if (postKvPairSeparators == null) {
            postKvPairSeparators = new ZapTextField();
        }
        return postKvPairSeparators;
    }

    @Override
    public void initContextData(Session session, Context context) {
        ParameterParser urlParamParser = context.getUrlParamParser();
        ParameterParser formParamParser = context.getPostParamParser();

        this.structuralParamsModel.removeAllStructuralParameters();

        if (urlParamParser instanceof StandardParameterParser) {
            StandardParameterParser urlStdParamParser = (StandardParameterParser) urlParamParser;
            this.getUrlKvPairSeparators().setText(urlStdParamParser.getKeyValuePairSeparators());
            this.getUrlKeyValueSeparators().setText(urlStdParamParser.getKeyValueSeparators());

            // TODO (JMG) : Potentially store Structural Params on Context instead of
            // ParameterParser
            for (String structParam : urlStdParamParser.getStructuralParameters()) {
                this.structuralParamsModel.addStructuralParameter(
                        new StructuralParameter(structParam, true));
            }
        }
        if (formParamParser instanceof StandardParameterParser) {
            StandardParameterParser formStdParamParser = (StandardParameterParser) formParamParser;
            this.getPostKvPairSeparators().setText(formStdParamParser.getKeyValuePairSeparators());
            this.getPostKeyValueSeparators().setText(formStdParamParser.getKeyValueSeparators());
        }
    }

    @Override
    public void validateContextData(Session session) throws Exception {
        if (this.urlKvPairSeparators.getText().length() == 0) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("context.structParams.warning.stdparser.nokvpsep"));
        }
        if (this.urlKeyValueSeparators.getText().length() == 0) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("context.structParams.warning.stdparser.nokvsep"));
        }
        // Don't allow any common characters
        for (char ch : this.urlKvPairSeparators.getText().toCharArray()) {
            if (this.urlKeyValueSeparators.getText().contains("" + ch)) {
                throw new IllegalArgumentException(
                        Constant.messages.getString("context.structParams.warning.stdparser.dup"));
            }
        }

        if (this.postKvPairSeparators.getText().length() == 0) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("context.structParams.warning.stdparser.nokvpsep"));
        }
        if (this.postKeyValueSeparators.getText().length() == 0) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("context.structParams.warning.stdparser.nokvsep"));
        }
        // Don't allow any common characters
        for (char ch : this.postKvPairSeparators.getText().toCharArray()) {
            if (this.postKeyValueSeparators.getText().contains("" + ch)) {
                throw new IllegalArgumentException(
                        Constant.messages.getString("context.structParams.warning.stdparser.dup"));
            }
        }
    }

    private void saveToContext(Context context, boolean updateSiteStructure) {
        ParameterParser urlParamParser = context.getUrlParamParser();
        ParameterParser formParamParser = context.getPostParamParser();

        List<StructuralParameter> structParams = this.structuralParamsModel.getElements();
        List<String> structParamNames = new ArrayList<String>();
        for (StructuralParameter structParam : structParams) {
            structParamNames.add(structParam.getName());
        }

        if (urlParamParser instanceof StandardParameterParser) {
            StandardParameterParser urlStdParamParser = (StandardParameterParser) urlParamParser;
            urlStdParamParser.setKeyValuePairSeparators(this.getUrlKvPairSeparators().getText());
            urlStdParamParser.setKeyValueSeparators(this.getUrlKeyValueSeparators().getText());

            urlStdParamParser.setStructuralParameters(structParamNames);

            context.setUrlParamParser(urlStdParamParser);
            urlStdParamParser.setContext(context);
        }
        if (formParamParser instanceof StandardParameterParser) {
            StandardParameterParser formStdParamParser = (StandardParameterParser) formParamParser;
            formStdParamParser.setKeyValuePairSeparators(this.getPostKvPairSeparators().getText());
            formStdParamParser.setKeyValueSeparators(this.getPostKeyValueSeparators().getText());
            context.setPostParamParser(formStdParamParser);
            formStdParamParser.setContext(context);
        }

        if (updateSiteStructure) {
            context.restructureSiteTree();
        }
    }

    @Override
    public void saveContextData(Session session) throws Exception {
        Context context = session.getContext(getContextId());
        saveToContext(context, true);
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        saveToContext(uiSharedContext, false);
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.context-struct";
    }

    public static class StruturalParametersOptionsModel
            extends AbstractMultipleOptionsTableModel<StructuralParameter> {

        private static final long serialVersionUID = 1L;

        private static final String[] COLUMN_NAMES = {
            Constant.messages.getString("context.structParams.table.header.paramName")
        };

        private List<StructuralParameter> structParams;

        public StruturalParametersOptionsModel() {
            this.structParams = new ArrayList<StructuralParameter>();
        }

        public StruturalParametersOptionsModel(List<StructuralParameter> structParams) {
            this.structParams = structParams;
        }

        @Override
        public int getRowCount() {
            return structParams.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StructuralParameter rowData = this.structParams.get(rowIndex);
            String columnData = null;

            switch (columnIndex) {
                case 0:
                    columnData = rowData.getName();
                    break;
            }

            return columnData;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMN_NAMES[column];
        }

        @Override
        public List<StructuralParameter> getElements() {
            return this.structParams;
        }

        public void removeAllStructuralParameters() {
            this.structParams = new ArrayList<StructuralParameter>();
            this.fireTableDataChanged();
        }

        public void addStructuralParameter(StructuralParameter structParam) {
            this.structParams.add(structParam);
            int newRowIndex = this.structParams.size() - 1;
            this.fireTableRowsInserted(newRowIndex, newRowIndex);
        }
    }

    public static class StructuralParametersOptionsPanel
            extends AbstractMultipleOptionsBaseTablePanel<StructuralParameter> {
        private static final long serialVersionUID = -1L;

        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("context.structParams.dialog.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("context.structParams.dialog.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString("all.button.remove");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString("all.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString("all.prompt.dontshow");

        public StructuralParametersOptionsPanel(StruturalParametersOptionsModel model) {
            super(model);

            getTable().getColumnExt(0).setPreferredWidth(50);
            getTable().setSortOrder(0, SortOrder.ASCENDING);
        }

        @Override
        public StructuralParameter showAddDialogue() {
            StructuralParameterDialog ddnDialog =
                    new StructuralParameterDialog(
                            View.getSingleton().getSessionDialog(),
                            "context.structParams.dialog.add.title",
                            new Dimension(500, 200));

            return ddnDialog.showDialog(null);
        }

        @Override
        public StructuralParameter showModifyDialogue(StructuralParameter ddn) {
            StructuralParameterDialog ddnDialog =
                    new StructuralParameterDialog(
                            View.getSingleton().getSessionDialog(),
                            "context.structParams.dialog.modify.title",
                            new Dimension(500, 200));

            return ddnDialog.showDialog(ddn);
        }

        @Override
        public boolean showRemoveDialogue(StructuralParameter e) {
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

    public static class StructuralParameterDialog extends StandardFieldsDialog {

        private static final long serialVersionUID = 1L;

        private static final String FIELD_PARAM_NAME = "context.structParams.dialog.paramName";

        private StructuralParameter data = null;

        public StructuralParameterDialog(JDialog owner, String titleLabel, Dimension dim) {
            super(owner, titleLabel, dim, true);
        }

        public StructuralParameter showDialog(StructuralParameter data) {
            String name = "";

            this.data = data;
            if (this.data != null) {
                name = this.data.getName();
            }

            this.addTextField(FIELD_PARAM_NAME, name);

            this.setVisible(true);

            return this.data;
        }

        @Override
        public void save() {
            this.data = new StructuralParameter(this.getStringValue(FIELD_PARAM_NAME), true);
        }

        @Override
        public String validateFields() {
            if (!this.getStringValue(FIELD_PARAM_NAME).matches("[A-Za-z0-9_]+")) {
                // Must supply a name just made up of alphanumeric characters
                return Constant.messages.getString("context.structParams.dialog.error.paramName");
            }

            return null;
        }
    }
}
