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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.VerticalLayout;
import org.parosproxy.paros.model.Session;
import org.zaproxy.zap.model.Context;

public class ContextDdnPanel extends AbstractContextPropertiesPanel {

    private static final long serialVersionUID = 1L;

    public ContextDdnPanel(int contextId) {
        super(contextId);
        setLayout(new CardLayout(0, 0));

        JPanel labelPanel = new JPanel();
        add(labelPanel, "name_138589339403800");
        GridBagLayout gbl_labelPanel = new GridBagLayout();
        gbl_labelPanel.columnWidths = new int[] {0};
        gbl_labelPanel.rowHeights = new int[] {0, 0};
        gbl_labelPanel.columnWeights = new double[] {1.0};
        gbl_labelPanel.rowWeights = new double[] {0.0, 1.0};
        labelPanel.setLayout(gbl_labelPanel);

        JLabel lblTitle = new JLabel("Data Driven Nodes Configuration");
        lblTitle.setFont(new Font("Tahoma", Font.PLAIN, 10));
        GridBagConstraints gbc_lblTitle = new GridBagConstraints();
        gbc_lblTitle.anchor = GridBagConstraints.WEST;
        gbc_lblTitle.insets = new Insets(0, 0, 5, 0);
        gbc_lblTitle.gridx = 0;
        gbc_lblTitle.gridy = 0;
        labelPanel.add(lblTitle, gbc_lblTitle);

        JPanel treePanel = new JPanel();
        GridBagConstraints gbc_treePanel = new GridBagConstraints();
        gbc_treePanel.fill = GridBagConstraints.BOTH;
        gbc_treePanel.gridx = 0;
        gbc_treePanel.gridy = 1;
        labelPanel.add(treePanel, gbc_treePanel);
        treePanel.setLayout(new BorderLayout(0, 0));

        JXTreeTable ddnTree = new JXTreeTable();
        treePanel.add(ddnTree, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        treePanel.add(buttonsPanel, BorderLayout.EAST);
        buttonsPanel.setLayout(new VerticalLayout());

        JButton btnAdd = new JButton("Add...");
        buttonsPanel.add(btnAdd);

        JButton btnEdit = new JButton("Modify...");
        buttonsPanel.add(btnEdit);

        JButton btnRemove = new JButton("Remove...");
        buttonsPanel.add(btnRemove);
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
