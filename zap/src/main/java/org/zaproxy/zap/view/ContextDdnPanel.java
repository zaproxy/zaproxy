/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.VerticalLayout;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.DataDrivenNode;

public class ContextDdnPanel extends AbstractContextPropertiesPanel {

    private static final long serialVersionUID = 1L;

    private static final String PANEL_NAME = Constant.messages.getString("context.ddn.panel.name");
    private static final String TITLE_LABEL =
            Constant.messages.getString("context.ddn.label.title");
    private static final String ADD_BUTTON_LABEL =
            Constant.messages.getString("context.ddn.button.add");
    private static final String MODIFY_BUTTON_LABEL =
            Constant.messages.getString("context.ddn.button.modify");
    private static final String REMOVE_BUTTON_LABEL =
            Constant.messages.getString("context.ddn.button.remove");
    private static final String MOVE_UP_BUTTON_LABEL =
            Constant.messages.getString("context.ddn.button.moveUp");
    private static final String MOVE_DOWN_BUTTON_LABEL =
            Constant.messages.getString("context.ddn.button.moveDown");
    private static final String REMOVE_CONFIRMATION_LABEL =
            Constant.messages.getString("context.ddn.checkbox.removeConfirmation");

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

    private JPanel mainPanel;
    private JTree ddnTree;
    private JButton addButton;
    private JButton modifyButton;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JCheckBox removePromptCheckbox;

    private DefaultTreeModel treeModel;

    public static String getPanelName(int contextId) {
        // Panel names have to be unique, so prefix with the context id
        return contextId + ": " + PANEL_NAME;
    }

    public ContextDdnPanel(Context context) {
        super(context.getId());

        this.treeModel =
                new DefaultTreeModel(
                        new DefaultMutableTreeNode(new DataDrivenNode("Data Driven Nodes", null)));

        this.setLayout(new CardLayout());
        this.setName(getPanelName(this.getContextId()));
        this.add(getPanel(), mainPanel.getName());
    }

    private JPanel getPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setName("DataDrivenNodes");
            mainPanel.setLayout(new VerticalLayout());

            mainPanel.add(new JLabel(TITLE_LABEL));

            JPanel treePanel = new JPanel();
            treePanel.setLayout(new BorderLayout());

            ddnTree = new JTree();
            ddnTree.setModel(this.treeModel);
            ddnTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            ddnTree.addTreeSelectionListener(
                    new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            boolean notRootSelected = !isRootSelected();

                            modifyButton.setEnabled(notRootSelected);
                            removeButton.setEnabled(notRootSelected);
                            moveUpButton.setEnabled(notRootSelected);
                            moveDownButton.setEnabled(notRootSelected);
                        }
                    });

            JScrollPane treeScrollPane = new JScrollPane();
            treeScrollPane.setViewportView(ddnTree);
            treeScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
            treePanel.add(treeScrollPane);

            JPanel buttonsPanel = new JPanel();
            buttonsPanel.setLayout(new VerticalLayout());
            addButton = new JButton(ADD_BUTTON_LABEL);
            addButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            DefaultMutableTreeNode parentNode =
                                    (DefaultMutableTreeNode) treeModel.getRoot();
                            TreePath selectedNodePath = ddnTree.getSelectionPath();
                            if (selectedNodePath != null) {
                                parentNode =
                                        (DefaultMutableTreeNode)
                                                selectedNodePath.getLastPathComponent();
                            }

                            DataDrivenNode parentDdn = null;
                            if (!isRootSelected()) {
                                parentDdn = (DataDrivenNode) parentNode.getUserObject();
                            }

                            DataDrivenNode dialogModel = new DataDrivenNode(parentDdn);
                            DataDrivenNodeDialog ddnDialog =
                                    new DataDrivenNodeDialog(
                                            View.getSingleton().getSessionDialog(),
                                            "context.ddn.dialog.add.title",
                                            new Dimension(500, 200));

                            DataDrivenNode newDdn = ddnDialog.showDialog(dialogModel);
                            if (newDdn != null) {
                                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDdn);
                                treeModel.insertNodeInto(
                                        newNode, parentNode, parentNode.getChildCount());
                                ddnTree.expandPath(selectedNodePath);

                                if (parentDdn != null) {
                                    parentDdn.addChildNode(newDdn);
                                }
                            }
                        }
                    });
            modifyButton = new JButton(MODIFY_BUTTON_LABEL);
            modifyButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            TreePath selectedPath = ddnTree.getSelectionPath();
                            DefaultMutableTreeNode selectedNode =
                                    (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                            DataDrivenNode selectedDdn =
                                    (DataDrivenNode) selectedNode.getUserObject();

                            DataDrivenNodeDialog ddnDialog =
                                    new DataDrivenNodeDialog(
                                            View.getSingleton().getSessionDialog(),
                                            "context.ddn.dialog.modify.title",
                                            new Dimension(500, 200));
                            ddnDialog.showDialog(selectedDdn);
                            treeModel.reload(selectedNode);
                        }
                    });
            removeButton = new JButton(REMOVE_BUTTON_LABEL);
            removeButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            TreePath selectedPath = ddnTree.getSelectionPath();
                            DefaultMutableTreeNode selectedNode =
                                    (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                            DataDrivenNode selectedDdn =
                                    (DataDrivenNode) selectedNode.getUserObject();
                            DefaultMutableTreeNode parentNode =
                                    (DefaultMutableTreeNode) selectedNode.getParent();
                            DataDrivenNode parentDdn = (DataDrivenNode) parentNode.getUserObject();

                            if (!removePromptCheckbox.isSelected()) {
                                JCheckBox removeWithoutConfirmationCheckBox =
                                        new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
                                Object[] messages = {
                                    REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox
                                };
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
                                    removePromptCheckbox.setSelected(
                                            removeWithoutConfirmationCheckBox.isSelected());
                                } else {
                                    return;
                                }
                            }

                            parentDdn.removeChildNode(selectedDdn);
                            treeModel.removeNodeFromParent(selectedNode);
                        }
                    });
            moveUpButton = new JButton(MOVE_UP_BUTTON_LABEL);
            moveUpButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            TreePath selectedPath = ddnTree.getSelectionPath();
                            DefaultMutableTreeNode selectedNode =
                                    (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                            DataDrivenNode selectedDdn =
                                    (DataDrivenNode) selectedNode.getUserObject();
                            DefaultMutableTreeNode parentNode =
                                    (DefaultMutableTreeNode) selectedNode.getParent();
                            DataDrivenNode parentDdn = (DataDrivenNode) parentNode.getUserObject();

                            List<DataDrivenNode> parentDdnChildNodes = parentDdn.getChildNodes();
                            if (parentDdnChildNodes.size() < 2) {
                                return;
                            }

                            int selectedDdnIndex = parentDdnChildNodes.indexOf(selectedDdn);
                            if (selectedDdnIndex < 1) {
                                return;
                            }

                            Collections.swap(
                                    parentDdnChildNodes, selectedDdnIndex, selectedDdnIndex - 1);
                            treeModel.removeNodeFromParent(selectedNode);
                            treeModel.insertNodeInto(
                                    selectedNode, parentNode, selectedDdnIndex - 1);

                            TreePath newSelectedPath = new TreePath(selectedNode.getPath());
                            ddnTree.setSelectionPath(newSelectedPath);
                        }
                    });
            moveDownButton = new JButton(MOVE_DOWN_BUTTON_LABEL);
            moveDownButton.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            TreePath selectedPath = ddnTree.getSelectionPath();
                            DefaultMutableTreeNode selectedNode =
                                    (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                            DataDrivenNode selectedDdn =
                                    (DataDrivenNode) selectedNode.getUserObject();
                            DefaultMutableTreeNode parentNode =
                                    (DefaultMutableTreeNode) selectedNode.getParent();
                            DataDrivenNode parentDdn = (DataDrivenNode) parentNode.getUserObject();

                            List<DataDrivenNode> parentDdnChildNodes = parentDdn.getChildNodes();
                            if (parentDdnChildNodes.size() < 2) {
                                return;
                            }

                            int selectedDdnIndex = parentDdnChildNodes.indexOf(selectedDdn);
                            if (selectedDdnIndex >= parentDdnChildNodes.size() - 1) {
                                return;
                            }

                            Collections.swap(
                                    parentDdnChildNodes, selectedDdnIndex, selectedDdnIndex + 1);
                            treeModel.removeNodeFromParent(selectedNode);
                            treeModel.insertNodeInto(
                                    selectedNode, parentNode, selectedDdnIndex + 1);

                            TreePath newSelectedPath = new TreePath(selectedNode.getPath());
                            ddnTree.setSelectionPath(newSelectedPath);
                        }
                    });
            addButton.setEnabled(true);
            modifyButton.setEnabled(false);
            removeButton.setEnabled(false);
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
            buttonsPanel.add(addButton);
            buttonsPanel.add(modifyButton);
            buttonsPanel.add(removeButton);
            buttonsPanel.add(moveUpButton);
            buttonsPanel.add(moveDownButton);
            treePanel.add(buttonsPanel, BorderLayout.EAST);

            removePromptCheckbox = new JCheckBox(REMOVE_CONFIRMATION_LABEL);
            treePanel.add(removePromptCheckbox, BorderLayout.SOUTH);

            mainPanel.add(treePanel);
        }

        return mainPanel;
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.removeAllChildren();

        List<DataDrivenNode> contextDdns = uiSharedContext.getDataDrivenNodes();

        for (DataDrivenNode ddn : contextDdns) {
            rootNode.add(new DdnTreeNode(ddn.clone()));
        }
        treeModel.reload();
    }

    @Override
    public void validateContextData(Session session) throws Exception {
        // Nothing to validate at this level ; validations occur when adding/modifying each
        // DataDrivenNode
    }

    private boolean isRootSelected() {
        TreePath selectedPath = ddnTree.getSelectionPath();
        boolean rootSelected = true;
        if (selectedPath != null) {
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
            DefaultMutableTreeNode selectedNode =
                    (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            rootSelected = (selectedNode == rootNode);
        }

        return rootSelected;
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();

        List<DataDrivenNode> savedDdns = new ArrayList<>();
        int childCount = rootNode.getChildCount();
        for (int nodeCounter = 0; nodeCounter < childCount; nodeCounter++) {
            DefaultMutableTreeNode ddnNode =
                    (DefaultMutableTreeNode) rootNode.getChildAt(nodeCounter);
            DataDrivenNode ddn = (DataDrivenNode) ddnNode.getUserObject();
            savedDdns.add(ddn.clone());
        }

        uiSharedContext.setDataDrivenNodes(savedDdns);
    }

    @Override
    public void saveContextData(Session session) throws Exception {
        Context context = session.getContext(getContextId());
        saveTemporaryContextData(context);
        context.restructureSiteTree();
    }

    @Override
    public String getHelpIndex() {
        return "ui.dialogs.context-ddn";
    }

    // Note : This type is only used when re-creating the Data Driven Node Tree from a Context in
    // order to easily support adding all Child DDNs to the Tree as well.
    public static class DdnTreeNode extends DefaultMutableTreeNode {

        private static final long serialVersionUID = 1L;

        public DdnTreeNode(DataDrivenNode model) {
            super(model);

            for (DataDrivenNode childModel : model.getChildNodes()) {
                this.add(new DdnTreeNode(childModel));
            }
        }
    }

    public static class DataDrivenNodeDialog extends StandardFieldsDialog {

        private static final long serialVersionUID = 1L;

        private static final String FIELD_DDN_NAME = "context.ddn.dialog.ddnName";

        private static final String FIELD_PREFIX_PATTERN = "context.ddn.dialog.prefixPattern";
        private static final String FIELD_DATA_NODE_PATTERN = "context.ddn.dialog.dataNodePattern";
        private static final String FIELD_SUFFIX_PATTERN = "context.ddn.dialog.suffixPattern";
        private static final String LABEL_PATTERN = "context.ddn.dialog.pattern";

        private DataDrivenNode model;
        private DataDrivenNode labelModel;

        public DataDrivenNodeDialog(JDialog owner, String titleLabel, Dimension dim) {
            super(owner, titleLabel, dim, true);
        }

        public DataDrivenNode showDialog(DataDrivenNode model) {
            this.model = model;
            this.labelModel =
                    new DataDrivenNode("Label Model - Not For Use", model.getParentNode());

            DocumentListener updatePatternListener =
                    new DocumentListener() {
                        @Override
                        public void removeUpdate(DocumentEvent e) {
                            updatePatternLabel();
                        }

                        @Override
                        public void insertUpdate(DocumentEvent e) {
                            updatePatternLabel();
                        }

                        @Override
                        public void changedUpdate(DocumentEvent e) {
                            // Unused for Plaintext Controls such as TextField (see
                            // https://docs.oracle.com/javase/tutorial/uiswing/events/documentlistener.html)
                        }
                    };

            this.addTextField(FIELD_DDN_NAME, this.model.getName());

            this.addTextField(FIELD_PREFIX_PATTERN, this.model.getPrefixPattern());
            this.addFieldListener(FIELD_PREFIX_PATTERN, updatePatternListener);
            this.addTextField(FIELD_DATA_NODE_PATTERN, this.model.getDataNodePattern());
            this.addFieldListener(FIELD_DATA_NODE_PATTERN, updatePatternListener);
            this.addTextField(FIELD_SUFFIX_PATTERN, this.model.getSuffixPattern());
            this.addFieldListener(FIELD_SUFFIX_PATTERN, updatePatternListener);

            this.addPadding();
            this.addReadOnlyField(LABEL_PATTERN, "", true);
            updatePatternLabel();

            this.pack();
            this.setVisible(true);

            return this.model;
        }

        @Override
        public void save() {
            this.model.setName(this.getStringValue(FIELD_DDN_NAME));

            this.model.setPrefixPattern(this.getStringValue(FIELD_PREFIX_PATTERN));
            this.model.setDataNodePattern(this.getStringValue(FIELD_DATA_NODE_PATTERN));
            this.model.setSuffixPattern(this.getStringValue(FIELD_SUFFIX_PATTERN));
        }

        @Override
        public void cancelPressed() {
            super.cancelPressed();

            this.model = null;
        }

        @Override
        public String validateFields() {
            if (!this.getStringValue(FIELD_DDN_NAME).matches("[A-Za-z0-9_]+")) {
                return Constant.messages.getString("context.ddn.dialog.error.ddnName");
            }

            if (StringUtils.isBlank(this.getStringValue(FIELD_PREFIX_PATTERN))) {
                return Constant.messages.getString("context.ddn.dialog.error.prefixPattern");
            }

            // NOTE : Data Node & Suffix Patterns are optional to allow for more effective Tree
            // grouping

            return null;
        }

        private void updatePatternLabel() {
            this.labelModel.setPrefixPattern(this.getStringValue(FIELD_PREFIX_PATTERN));
            this.labelModel.setDataNodePattern(this.getStringValue(FIELD_DATA_NODE_PATTERN));
            this.labelModel.setSuffixPattern(this.getStringValue(FIELD_SUFFIX_PATTERN));

            String labelValue = "Pattern : " + this.labelModel.getPattern();

            this.setFieldValue(LABEL_PATTERN, labelValue);
        }
    }

    // TODO (JMG) : Add Dialog for Testing the Selected DDN Pattern against User Provided Input
}
