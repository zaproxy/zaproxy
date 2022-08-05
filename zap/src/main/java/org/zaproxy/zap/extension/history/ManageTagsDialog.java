/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2010 The ZAP Development Team
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
package org.zaproxy.zap.extension.history;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.utils.SortedComboBoxModel;
import org.zaproxy.zap.utils.SortedListModel;

@SuppressWarnings("serial")
public class ManageTagsDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;
    private JPanel jPanel = null;
    private JComboBox<String> txtTagAdd = null;
    private JButton btnAdd = null;
    private JButton btnDelete = null;
    private JButton btnSave = null;
    private JButton btnCancel = null;
    private JList<String> tagList = null;
    private SortedListModel<String> tagListModel = null;

    private HistoryReference historyRef;

    private JScrollPane jScrollPane = null;
    private SortedComboBoxModel<String> tagAddModel = null;

    /** @throws HeadlessException */
    public ManageTagsDialog() throws HeadlessException {
        super();
        initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public ManageTagsDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setTitle(Constant.messages.getString("history.managetags.title"));
        this.setContentPane(getJPanel());
        this.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowOpened(java.awt.event.WindowEvent e) {}

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {}
                });

        pack();
    }
    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {

            GridBagConstraints gridBagConstraints00 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints40 = new GridBagConstraints();
            GridBagConstraints gridBagConstraints41 = new GridBagConstraints();

            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());

            gridBagConstraints00.gridy = 0;
            gridBagConstraints00.gridx = 0;
            gridBagConstraints00.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints00.weightx = 1.0D;
            gridBagConstraints00.insets = new java.awt.Insets(2, 2, 2, 2);

            gridBagConstraints10.gridy = 1;
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints10.weightx = 1.0D;
            gridBagConstraints10.insets = new java.awt.Insets(2, 2, 2, 2);

            gridBagConstraints11.gridy = 1;
            gridBagConstraints11.gridx = 1;
            gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints11.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;

            gridBagConstraints20.gridy = 2;
            gridBagConstraints20.gridx = 0;
            gridBagConstraints20.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints20.insets = new java.awt.Insets(2, 2, 2, 2);

            gridBagConstraints30.weightx = 1.0D;
            gridBagConstraints30.weighty = 1.0D;
            gridBagConstraints30.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints30.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints30.gridy = 3;
            gridBagConstraints30.gridx = 0;
            gridBagConstraints30.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints30.ipadx = 0;
            gridBagConstraints30.ipady = 10;

            gridBagConstraints31.gridy = 3;
            gridBagConstraints31.gridx = 1;
            gridBagConstraints31.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints31.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints31.anchor = java.awt.GridBagConstraints.NORTHEAST;

            gridBagConstraints40.gridy = 4;
            gridBagConstraints40.gridx = 0;
            gridBagConstraints40.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints40.anchor = java.awt.GridBagConstraints.EAST;

            gridBagConstraints41.gridy = 4;
            gridBagConstraints41.gridx = 1;
            gridBagConstraints41.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints41.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints41.anchor = java.awt.GridBagConstraints.EAST;

            jPanel.add(
                    new JLabel(Constant.messages.getString("history.managetags.label.addtag")),
                    gridBagConstraints00);
            jPanel.add(this.getTxtTagAdd(), gridBagConstraints10);
            jPanel.add(getBtnAdd(), gridBagConstraints11);
            jPanel.add(
                    new JLabel(Constant.messages.getString("history.managetags.label.currenttags")),
                    gridBagConstraints20);
            jPanel.add(getJScrollPane(), gridBagConstraints30);
            jPanel.add(getBtnDelete(), gridBagConstraints31);
            jPanel.add(getBtnCancel(), gridBagConstraints40);
            jPanel.add(getBtnSave(), gridBagConstraints41);
        }
        return jPanel;
    }

    private JList<String> getTagList() {
        if (tagList == null) {
            tagList = new JList<>();
            tagList.setLayoutOrientation(JList.VERTICAL);
            tagList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            tagListModel = new SortedListModel<>();
            tagList.setModel(tagListModel);
        }
        return tagList;
    }

    public void setTags(List<String> tags) {
        tagListModel.clear();
        for (String tag : tags) {
            tagListModel.addElement(tag);
            getAllTagsModel().removeElement(tag);
        }
    }

    private void addTag(String tag) {
        if (tag != null && tag.length() > 0 && !tagListModel.contains(tag)) {
            tagListModel.addElement(tag);
            getAllTagsModel().removeElement(tag);
        }
    }

    private void deleteTags(List<String> tags) {
        for (String tag : tags) {
            tagListModel.removeElement(tag);

            getAllTagsModel().addElement(tag);
        }
    }

    private JComboBox<String> getTxtTagAdd() {
        if (txtTagAdd == null) {
            txtTagAdd = new JComboBox<>();
            txtTagAdd.setEditable(true);
            tagAddModel = getAllTagsModel();
            txtTagAdd.setModel(tagAddModel);
        }
        return txtTagAdd;
    }

    private SortedComboBoxModel<String> getAllTagsModel() {
        if (tagAddModel == null) {
            tagAddModel = new SortedComboBoxModel<>();
        }
        return tagAddModel;
    }

    /**
     * This method initializes btnStart
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnAdd() {
        if (btnAdd == null) {
            btnAdd = new JButton();
            btnAdd.setText(Constant.messages.getString("history.managetags.button.add"));
            btnAdd.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            addTag(getTxtTagAdd().getSelectedItem().toString());
                            getTxtTagAdd().setSelectedIndex(0);
                        }
                    });
        }
        return btnAdd;
    }
    /**
     * This method initializes btnDelete
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnDelete() {
        if (btnDelete == null) {
            btnDelete = new JButton();
            btnDelete.setText(Constant.messages.getString("history.managetags.button.delete"));
            btnDelete.setEnabled(true);
            btnDelete.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            deleteTags(tagList.getSelectedValuesList());
                        }
                    });
        }
        return btnDelete;
    }

    private JButton getBtnSave() {
        if (btnSave == null) {
            btnSave = new JButton();
            btnSave.setText(Constant.messages.getString("history.managetags.button.save"));
            btnSave.setEnabled(true);
            btnSave.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            historyRef.setTags(Collections.list(tagListModel.elements()));
                            dispose();
                        }
                    });
        }
        return btnSave;
    }

    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText(Constant.messages.getString("all.button.cancel"));
            btnCancel.setEnabled(true);
            btnCancel.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            dispose();
                        }
                    });
        }
        return btnCancel;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setHorizontalScrollBarPolicy(
                    javax.swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            jScrollPane.setVerticalScrollBarPolicy(
                    javax.swing.JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setViewportView(getTagList());
        }
        return jScrollPane;
    }

    public HistoryReference getHistoryRef() {
        return historyRef;
    }

    public void setHistoryRef(HistoryReference historyRef) {
        this.historyRef = historyRef;
    }

    public void setAllTags(List<String> allTags) {
        getAllTagsModel().removeAllElements();
        getAllTagsModel().addElement(""); // Default is empty so user can type anything in

        for (String tag : allTags) {
            getAllTagsModel().addElement(tag);
        }
    }
}
