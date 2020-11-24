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
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

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
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.VerticalLayout;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.view.View;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.DataDrivenNode;

public class ContextDdnPanel extends AbstractContextPropertiesPanel {

	private static final long serialVersionUID = 1L;
	
    private static final String PANEL_NAME = Constant.messages.getString("context.ddn.panel.name");
    private static final String TITLE_LABEL = Constant.messages.getString("context.ddn.label.title");
    private static final String ADD_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.add");
    private static final String MODIFY_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.modify");
    private static final String REMOVE_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.remove");
    private static final String REMOVE_CONFIRMATION_LABEL = Constant.messages.getString("context.ddn.checkbox.removeConfirmation");
    
    private static final String REMOVE_DIALOG_TITLE = Constant.messages.getString("context.ddn.dialog.remove.title");
    private static final String REMOVE_DIALOG_TEXT = Constant.messages.getString("context.ddn.dialog.remove.text");
    private static final String REMOVE_DIALOG_CONFIRM_BUTTON_LABEL = Constant.messages.getString("all.button.remove");
    private static final String REMOVE_DIALOG_CANCEL_BUTTON_LABEL = Constant.messages.getString("all.button.cancel");
    private static final String REMOVE_DIALOG_CHECKBOX_LABEL = Constant.messages.getString("all.prompt.dontshow");

    private JPanel mainPanel;
    private JTree ddnTree;
    private JButton addButton;
    private JButton modifyButton;
    private JButton removeButton;
    private JCheckBox removePromptCheckbox;
    
    private DefaultTreeModel treeModel;

    public static String getPanelName(int contextId) {
        // Panel names have to be unique, so prefix with the context id
        return contextId + ": " + PANEL_NAME;
    }

    public ContextDdnPanel(Context context) {
        super(context.getId());
        
        this.treeModel = new DefaultTreeModel(new DefaultMutableTreeNode(DataDrivenNode.ROOT_DDN));

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
        	ddnTree.addTreeSelectionListener(new TreeSelectionListener() {
				@Override
				public void valueChanged(TreeSelectionEvent e) {
					addButton.setEnabled(true);
					
					DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
					TreePath selectedPath = ddnTree.getSelectionPath();
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
					boolean notRootSelected = (selectedNode != rootNode);
					
					modifyButton.setEnabled(notRootSelected);
					removeButton.setEnabled(notRootSelected);
				}
			});
        	
        	JScrollPane treeScrollPane = new JScrollPane();
        	treeScrollPane.setViewportView(ddnTree);
        	treeScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        	treePanel.add(treeScrollPane);
        	
        	JPanel buttonsPanel = new JPanel();
        	buttonsPanel.setLayout(new VerticalLayout());
        	addButton = new JButton(ADD_BUTTON_LABEL);
        	addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)treeModel.getRoot();
					TreePath parentNodePath = ddnTree.getSelectionPath();
					if (parentNodePath != null) {
						parentNode = (DefaultMutableTreeNode)parentNodePath.getLastPathComponent(); 
					}
					
					DataDrivenNodeDialog ddnDialog = 
							new DataDrivenNodeDialog(View.getSingleton().getSessionDialog(),
												     "context.ddn.dialog.add.title",
					                                 new Dimension(500, 200));
					DataDrivenNode newDdn = ddnDialog.showDialog();
					if (newDdn != null) {
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDdn);
						treeModel.insertNodeInto(newNode, parentNode, parentNode.getChildCount());
						ddnTree.expandPath(parentNodePath);
					}
				}
			});
        	modifyButton = new JButton(MODIFY_BUTTON_LABEL);
        	modifyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TreePath selectedPath = ddnTree.getSelectionPath();
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
					DataDrivenNode selectedDdn = (DataDrivenNode)selectedNode.getUserObject();
					
					DataDrivenNodeDialog ddnDialog = 
							new DataDrivenNodeDialog(View.getSingleton().getSessionDialog(),
												     "context.ddn.dialog.modify.title",
					                                 new Dimension(500, 200));
					ddnDialog.showDialog(selectedDdn);
					treeModel.reload(selectedNode);
				}
			});
        	removeButton = new JButton(REMOVE_BUTTON_LABEL);
        	removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					TreePath selectedPath = ddnTree.getSelectionPath();
					DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
					
					if (!removePromptCheckbox.isSelected()) {
						JCheckBox removeWithoutConfirmationCheckBox =
			                    new JCheckBox(REMOVE_DIALOG_CHECKBOX_LABEL);
			            Object[] messages = {REMOVE_DIALOG_TEXT, " ", removeWithoutConfirmationCheckBox};
			            int option = JOptionPane.showOptionDialog(
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
			            	removePromptCheckbox.setSelected(removeWithoutConfirmationCheckBox.isSelected());
			            }
			            else {
			            	return;
			            }
					}
					
					treeModel.removeNodeFromParent(selectedNode);
				}
			});
        	addButton.setEnabled(false);
        	modifyButton.setEnabled(false);
        	removeButton.setEnabled(false);
        	buttonsPanel.add(addButton);
        	buttonsPanel.add(modifyButton);
        	buttonsPanel.add(removeButton);
        	treePanel.add(buttonsPanel, BorderLayout.EAST);
        	
        	removePromptCheckbox = new JCheckBox(REMOVE_CONFIRMATION_LABEL);
        	treePanel.add(removePromptCheckbox, BorderLayout.SOUTH);
        	
        	mainPanel.add(treePanel);
        }
        
        return mainPanel;
    }

    @Override
    public void initContextData(Session session, Context uiSharedContext) {
        // TODO (JMG) : Reset DDN Tree Model
    	// TODO (JMG) : Get DDNs from Context
    	// TODO (JMG) : Build DDN Tree Model from Context DDNs
    }

    @Override
    public void validateContextData(Session session) throws Exception {
    	// Nothing to validate at this level ; validations occur on each DataDrivenNode
        return;
    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        // TODO (JMG) : Save Tree Model DDNs to Context
    }

    @Override
    public void saveContextData(Session session) throws Exception {
    	Context context = session.getContext(getContextId()); 
    	saveTemporaryContextData(context);
    	context.restructureSiteTree();
    }
    
    public static class DataDrivenNodeDialog extends StandardFieldsDialog {
    	
		private static final long serialVersionUID = 1L;
		
		private static final String FIELD_DDN_NAME = "context.ddn.dialog.ddnName";
		private static final String FIELD_PATTERN = "context.ddn.dialog.pattern";
		
		private DataDrivenNode data;

		public DataDrivenNodeDialog(JDialog owner, String titleLabel, Dimension dim) {
			super(owner, titleLabel, dim, true);
		}
		
		public DataDrivenNode showDialog(DataDrivenNode data) {
			this.data = data;
			
			String ddnName = this.data.getName();
			Pattern regexPattern = this.data.getPattern();
			String pattern = regexPattern.pattern();
			
			this.addTextField(FIELD_DDN_NAME, ddnName);
			this.addTextField(FIELD_PATTERN, pattern);
			
			this.setVisible(true);
			
			return this.data;
		}
		
		public DataDrivenNode showDialog() {
			return showDialog(new DataDrivenNode("", "", null));
		}

		@Override
		public void save() {
			this.data.setName(this.getStringValue(FIELD_DDN_NAME));
			this.data.setPattern(this.getStringValue(FIELD_PATTERN));
		}

		@Override
		public String validateFields() {
			if (!this.getStringValue(FIELD_DDN_NAME).matches("[A-Za-z0-9_]+")) {
                return Constant.messages.getString("context.ddn.dialog.error.ddnName");
            }
			
			String pattern = this.getStringValue(FIELD_PATTERN);
			if (this.isEmptyField(FIELD_PATTERN)) {
				// TODO (JMG) : Add Check to ensure the appropriate Pattern Format (ie. 2 matching groups, one nested inside the other?)
				return Constant.messages.getString("context.ddn.dialog.error.pattern");
			}
			
			try {
				Pattern.compile(pattern);
			}
			catch (Exception exception) {
				return Constant.messages.getString("context.ddn.dialog.error.pattern");
			}
			
			return null;
		}
    }
}
