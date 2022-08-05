/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.treetable.TreeTableModel;

@SuppressWarnings("serial")
public class MultipleOptionsTablePanel extends JPanel {

    private static final long serialVersionUID = 5282581470011033565L;

    private JXTable table;

    private JScrollPane scrollPane;

    private JPanel buttonsPanel;

    private TableModel model;

    private JPanel footerPanel;

    public MultipleOptionsTablePanel(TableModel model) {
        super(new BorderLayout());

        this.model = model;
        this.table = createTable();
        if (table instanceof JXTreeTable) {
            if (model instanceof TreeTableModel) {
                ((JXTreeTable) table).setTreeTableModel((TreeTableModel) model);
            }
        } else {
            this.table.setModel(model);
        }

        JPanel panel = new JPanel(new BorderLayout());

        panel.add(getJScrollPane(), BorderLayout.CENTER);
        panel.add(getButtonsPanel(), BorderLayout.LINE_END);

        this.add(panel, BorderLayout.CENTER);
        this.add(getFooterPanel(), BorderLayout.SOUTH);
    }

    protected JXTable createTable() {
        JXTable table = new JXTable();
        table.setColumnControlVisible(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    protected final JXTable getTable() {
        return table;
    }

    protected TableModel getModel() {
        return model;
    }

    private JScrollPane getJScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane();
            scrollPane.setViewportView(getTable());
            scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        }
        return scrollPane;
    }

    public void addButton(JButton button) {
        getButtonsPanel().add(button);
    }

    public void addButtonSpacer() {
        getButtonsPanel().add(Box.createRigidArea(new Dimension(1, 16)));
    }

    public void addButtonSpacer(int height) {
        getButtonsPanel().add(Box.createRigidArea(new Dimension(1, height)));
    }

    public JPanel getFooterPanel() {
        if (footerPanel == null) {
            footerPanel = new JPanel();
        }
        return footerPanel;
    }

    /**
     * Sets whether or not the component and its child components should be enabled.
     *
     * <p>Only the table and corresponding scroll bars are enabled/disabled.
     *
     * @param enabled {@code true} if the component and its child components should be enabled,
     *     {@code false} otherwise.
     */
    public void setComponentEnabled(boolean enabled) {
        super.setEnabled(enabled);

        table.setEnabled(enabled);
        if (scrollPane.getVerticalScrollBar() != null) {
            scrollPane.getVerticalScrollBar().setEnabled(enabled);
        }
        if (scrollPane.getHorizontalScrollBar() != null) {
            scrollPane.getHorizontalScrollBar().setEnabled(enabled);
        }
    }

    protected JPanel getButtonsPanel() {
        if (buttonsPanel == null) {
            buttonsPanel = new JPanel(new VerticalLayout());
        }
        return buttonsPanel;
    }
}
