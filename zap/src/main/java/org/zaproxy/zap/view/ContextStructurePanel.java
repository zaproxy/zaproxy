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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
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
import org.zaproxy.zap.model.StructuralNodeModifier;
import org.zaproxy.zap.utils.ZapTextField;

@SuppressWarnings("serial")
public class ContextStructurePanel extends AbstractContextPropertiesPanel {

    private static final String PANEL_NAME = Constant.messages.getString("context.struct.title");
    private static final long serialVersionUID = -1;

    private StructuralNodeModifiersTableModel ddnTableModel;

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
        // Panel names hav to be unique, so prefix with the context id
        return contextId + ": " + PANEL_NAME;
    }

    /**
     * Constructs a {@code ContextStructurePanel} for the given context.
     *
     * @param context the target context, must not be {@code null}.
     */
    public ContextStructurePanel(Context context) {
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
            | |              | Structural Modifiers:                   |
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
            panelSession.setName("SessionStructure");
            panelSession.setLayout(new GridBagLayout());

            panelSession.add(
                    new JLabel(Constant.messages.getString("context.struct.label.url.kvpsep")),
                    LayoutHelper.getGBC(0, 0, 1, 1.0D));
            panelSession.add(
                    getUrlKvPairSeparators(),
                    LayoutHelper.getGBC(1, 0, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    new JLabel(Constant.messages.getString("context.struct.label.url.kvsep")),
                    LayoutHelper.getGBC(0, 1, 1, 1.0D));
            panelSession.add(
                    getUrlKeyValueSeparators(),
                    LayoutHelper.getGBC(1, 1, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    new JLabel(Constant.messages.getString("context.struct.label.post.kvpsep")),
                    LayoutHelper.getGBC(0, 2, 1, 1.0D));
            panelSession.add(
                    getPostKvPairSeparators(),
                    LayoutHelper.getGBC(1, 2, 1, 1.0D, new Insets(2, 0, 2, 0)));
            panelSession.add(
                    new JLabel(Constant.messages.getString("context.struct.label.post.kvsep")),
                    LayoutHelper.getGBC(0, 3, 1, 1.0D));
            panelSession.add(
                    getPostKeyValueSeparators(),
                    LayoutHelper.getGBC(1, 3, 1, 1.0D, new Insets(2, 0, 2, 0)));

            panelSession.add(
                    new JLabel(Constant.messages.getString("context.struct.label.struct")),
                    LayoutHelper.getGBC(0, 4, 1, 1.0D));

            ddnTableModel = new StructuralNodeModifiersTableModel();
            DataDrivenNodesMultipleOptionsPanel ddnOptionsPanel =
                    new DataDrivenNodesMultipleOptionsPanel(ddnTableModel);
            panelSession.add(ddnOptionsPanel, LayoutHelper.getGBC(0, 5, 2, 1.0d, 1.0d));
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

        this.ddnTableModel.setStructuralNodeModifiers(context.getDataDrivenNodes());

        if (urlParamParser instanceof StandardParameterParser) {
            StandardParameterParser urlStdParamParser = (StandardParameterParser) urlParamParser;
            this.getUrlKvPairSeparators().setText(urlStdParamParser.getKeyValuePairSeparators());
            this.getUrlKeyValueSeparators().setText(urlStdParamParser.getKeyValueSeparators());

            for (String structParam : urlStdParamParser.getStructuralParameters()) {
                this.ddnTableModel.addStructuralNodeModifier(
                        new StructuralNodeModifier(
                                StructuralNodeModifier.Type.StructuralParameter,
                                null,
                                structParam));
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
                    Constant.messages.getString("context.struct.warning.stdparser.nokvpsep"));
        }
        if (this.urlKeyValueSeparators.getText().length() == 0) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("context.struct.warning.stdparser.nokvsep"));
        }
        // Don't allow any common characters
        for (char ch : this.urlKvPairSeparators.getText().toCharArray()) {
            if (this.urlKeyValueSeparators.getText().contains("" + ch)) {
                throw new IllegalArgumentException(
                        Constant.messages.getString("context.struct.warning.stdparser.dup"));
            }
        }

        if (this.postKvPairSeparators.getText().length() == 0) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("context.struct.warning.stdparser.nokvpsep"));
        }
        if (this.postKeyValueSeparators.getText().length() == 0) {
            throw new IllegalArgumentException(
                    Constant.messages.getString("context.struct.warning.stdparser.nokvsep"));
        }
        // Don't allow any common characters
        for (char ch : this.postKvPairSeparators.getText().toCharArray()) {
            if (this.postKeyValueSeparators.getText().contains("" + ch)) {
                throw new IllegalArgumentException(
                        Constant.messages.getString("context.struct.warning.stdparser.dup"));
            }
        }
    }

    /**
     * Save the data from this panel to the provided context.
     *
     * @param context the context
     * @param updateSiteStructure {@code true} if the nodes of the context should be restructured,
     *     {@code false} otherwise
     * @see Context#restructureSiteTree()
     */
    private void saveToContext(Context context, boolean updateSiteStructure) {
        ParameterParser urlParamParser = context.getUrlParamParser();
        ParameterParser formParamParser = context.getPostParamParser();
        List<String> structParams = new ArrayList<>();
        List<StructuralNodeModifier> ddns = new ArrayList<>();

        for (StructuralNodeModifier snm : this.ddnTableModel.getElements()) {
            if (snm.getType().equals(StructuralNodeModifier.Type.StructuralParameter)) {
                structParams.add(snm.getName());
            } else {
                ddns.add(snm);
            }
        }

        if (urlParamParser instanceof StandardParameterParser) {
            StandardParameterParser urlStdParamParser = (StandardParameterParser) urlParamParser;
            urlStdParamParser.setKeyValuePairSeparators(this.getUrlKvPairSeparators().getText());
            urlStdParamParser.setKeyValueSeparators(this.getUrlKeyValueSeparators().getText());

            urlStdParamParser.setStructuralParameters(structParams);

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

        context.setDataDrivenNodes(ddns);

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

    public static class DataDrivenNodesMultipleOptionsPanel
            extends AbstractMultipleOptionsBaseTablePanel<StructuralNodeModifier> {
        private static final long serialVersionUID = -7216673905642941770L;
        private static final String REMOVE_DIALOG_TITLE =
                Constant.messages.getString("context.ddn.dialog.remove.title");
        private static final String REMOVE_DIALOG_TEXT =
                Constant.messages.getString("context.ddn.dialog.remove.text");

        private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL =
                Constant.messages.getString("all.button.remove");
        private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL =
                Constant.messages.getString("all.button.cancel");

        private static final String REMOVE_DIALOG_CHECKBOX_LABEL =
                Constant.messages.getString("all.prompt.dontshow");

        public DataDrivenNodesMultipleOptionsPanel(StructuralNodeModifiersTableModel model) {
            super(model);

            getTable().getColumnExt(0).setPreferredWidth(50);
            getTable().getColumnExt(1).setPreferredWidth(50);
            getTable().getColumnExt(2).setPreferredWidth(200);
            getTable().setSortOrder(1, SortOrder.ASCENDING);
        }

        @Override
        public StructuralNodeModifier showAddDialogue() {
            StructuralModifierDialog ddnDialog =
                    new StructuralModifierDialog(
                            View.getSingleton().getSessionDialog(),
                            "context.ddn.dialog.add.title",
                            new Dimension(500, 200));

            return ddnDialog.showDialog(null);
        }

        @Override
        public StructuralNodeModifier showModifyDialogue(StructuralNodeModifier ddn) {
            StructuralModifierDialog ddnDialog =
                    new StructuralModifierDialog(
                            View.getSingleton().getSessionDialog(),
                            "context.ddn.dialog.modify.title",
                            new Dimension(500, 200));

            return ddnDialog.showDialog(ddn);
        }

        @Override
        public boolean showRemoveDialogue(StructuralNodeModifier e) {
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

    public static class StructuralModifierDialog extends StandardFieldsDialog {
        private static final long serialVersionUID = 1L;

        private static final String FIELD_TYPE = "context.ddn.dialog.type";
        private static final String FIELD_NAME = "context.ddn.dialog.name";
        private static final String FIELD_REGEX = "context.ddn.dialog.regex";

        private static final String VALUE_TYPE_DATA = "context.ddn.dialog.type.data";
        private static final String VALUE_TYPE_STRUCT = "context.ddn.dialog.type.struct";

        private StructuralNodeModifier.Type type = StructuralNodeModifier.Type.StructuralParameter;
        private StructuralNodeModifier ddn = null;
        private boolean ro = false;

        public StructuralModifierDialog(JDialog owner, String titleLabel, Dimension dim) {
            super(owner, titleLabel, dim, true);
        }

        public StructuralNodeModifier showDialog(StructuralNodeModifier ddn) {
            String regex = "";
            String name = "";

            this.ddn = ddn;
            if (ddn != null) {
                type = ddn.getType();
                if (ddn.getPattern() != null) {
                    regex = ddn.getPattern().pattern();
                }
                name = ddn.getName();
                ro = true;
                this.addReadOnlyField(FIELD_NAME, getModVal(type), false);
            } else {
                this.addComboField(
                        FIELD_TYPE,
                        new String[] {
                            Constant.messages.getString(VALUE_TYPE_STRUCT),
                            Constant.messages.getString(VALUE_TYPE_DATA)
                        },
                        getModVal(type));
                this.addFieldListener(
                        FIELD_TYPE,
                        new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                setFieldStates();
                            }
                        });
            }

            this.addTextField(FIELD_NAME, name);
            this.addTextField(FIELD_REGEX, regex);

            setFieldStates();

            this.setVisible(true);

            return this.ddn;
        }

        private void setFieldStates() {
            if (!ro) {
                if (Constant.messages
                        .getString(VALUE_TYPE_STRUCT)
                        .equals(this.getStringValue(FIELD_TYPE))) {
                    type = StructuralNodeModifier.Type.StructuralParameter;
                } else {
                    type = StructuralNodeModifier.Type.DataDrivenNode;
                }
            }
            this.getField(FIELD_REGEX)
                    .setEnabled(StructuralNodeModifier.Type.DataDrivenNode.equals(type));
        }

        private String getModVal(StructuralNodeModifier.Type type) {
            switch (type) {
                case StructuralParameter:
                    return Constant.messages.getString(VALUE_TYPE_STRUCT);
                case DataDrivenNode:
                    return Constant.messages.getString(VALUE_TYPE_DATA);
            }
            return "";
        }

        @Override
        public void save() {
            ddn =
                    new StructuralNodeModifier(
                            type,
                            Pattern.compile(this.getStringValue(FIELD_REGEX)),
                            this.getStringValue(FIELD_NAME));
        }

        @Override
        public String validateFields() {
            if (!this.getStringValue(FIELD_NAME).matches("[A-Za-z0-9_]+")) {
                // Must supply a name just made up of alphanumeric characters
                return Constant.messages.getString("context.ddn.dialog.error.name");
            }

            if (StructuralNodeModifier.Type.DataDrivenNode.equals(type)) {
                if (this.isEmptyField(FIELD_REGEX)) {
                    return Constant.messages.getString("context.ddn.dialog.error.regex");
                }
                if (!this.getStringValue(FIELD_REGEX).matches(".*\\(.*\\).*\\(.*\\).*")) {
                    // We need at least 2 groups
                    return Constant.messages.getString("context.ddn.dialog.error.regex");
                }
                try {
                    Pattern.compile(this.getStringValue(FIELD_REGEX));
                } catch (Exception e) {
                    // Not a valid regex expression
                    return Constant.messages.getString("context.ddn.dialog.error.regex");
                }
            }

            return null;
        }
    }
}
