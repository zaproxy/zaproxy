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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.VerticalLayout;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;

public class ContextDdnPanel extends AbstractContextPropertiesPanel {

    private static final String PANEL_NAME = Constant.messages.getString("context.ddn.panel.name");
    private static final String TITLE_LABEL = Constant.messages.getString("context.ddn.label.title");
    private static final String ADD_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.add");
    private static final String MODIFY_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.modify");
    private static final String REMOVE_BUTTON_LABEL = Constant.messages.getString("context.ddn.button.remove");
    private static final String REMOVE_CONFIRMATION_LABEL = Constant.messages.getString("context.ddn.checkbox.removeConfirmation");

    private static final long serialVersionUID = 1L;
    private JPanel mainPanel;
    private JXTreeTable ddnTree;
    private JButton addButton;
    private JButton modifyButton;
    private JButton removeButton;
    private JCheckBox removePromptCheckbox;

    public static String getPanelName(int contextId) {
        // Panel names have to be unique, so prefix with the context id
        return contextId + ": " + PANEL_NAME;
    }

    public ContextDdnPanel(Context context) {
        super(context.getId());

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
        	
        	ddnTree = new JXTreeTable();
        	JScrollPane treeScrollPane = new JScrollPane();
        	treeScrollPane.setViewportView(ddnTree);
        	treeScrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        	treePanel.add(treeScrollPane);
        	
        	JPanel buttonsPanel = new JPanel();
        	buttonsPanel.setLayout(new VerticalLayout());
        	addButton = new JButton(ADD_BUTTON_LABEL);
        	modifyButton = new JButton(MODIFY_BUTTON_LABEL);
        	modifyButton.setEnabled(false);
        	removeButton = new JButton(REMOVE_BUTTON_LABEL);
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
        // TODO Auto-generated method stub

    }

    @Override
    public void validateContextData(Session session) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveTemporaryContextData(Context uiSharedContext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveContextData(Session session) throws Exception {
        // TODO Auto-generated method stub

    }
}
