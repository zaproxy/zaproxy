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

import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.utils.ZapTextArea;

@SuppressWarnings("serial")
public class NotesAddDialog extends AbstractDialog {

    private static final long serialVersionUID = 1L;
    private ZapTextArea txtDisplay = null;
    private JButton btnOk = null;
    private JButton btnCancel = null;

    private HistoryReference historyRef;

    private JScrollPane jScrollPane = null;

    /**
     * @throws HeadlessException
     */
    public NotesAddDialog() throws HeadlessException {
        super();
        initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public NotesAddDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setTitle(Constant.messages.getString("history.addnote.title"));

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        Component buttonsGlue = Box.createHorizontalGlue();

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addComponent(getJScrollPane())
                        .addGroup(
                                layout.createSequentialGroup()
                                        .addComponent(
                                                buttonsGlue,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE)
                                        .addComponent(getBtnCancel())
                                        .addComponent(getBtnOk())));

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(getJScrollPane())
                        .addGroup(
                                layout.createParallelGroup()
                                        .addComponent(buttonsGlue)
                                        .addComponent(getBtnCancel())
                                        .addComponent(getBtnOk())));

        setContentPane(panel);

        this.addWindowListener(
                new java.awt.event.WindowAdapter() {

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        clearAndDispose();
                    }
                });

        pack();
    }

    private ZapTextArea getTxtDisplay() {
        if (txtDisplay == null) {
            txtDisplay = new ZapTextArea("", 15, 25);
        }
        return txtDisplay;
    }

    /**
     * This method initializes btnStart
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText(Constant.messages.getString("all.button.save"));
            btnOk.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            historyRef.setNote(getTxtDisplay().getText());
                            clearAndDispose();
                        }
                    });
        }
        return btnOk;
    }

    private void clearAndDispose() {
        setNote("");
        historyRef = null;
        dispose();
    }

    /**
     * This method initializes btnStop
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText(Constant.messages.getString("all.button.cancel"));
            btnCancel.addActionListener(e -> clearAndDispose());
        }
        return btnCancel;
    }

    /**
     * @param plugin unused.
     * @deprecated (2.7.0) No longer used/needed.
     */
    @Deprecated
    public void setPlugin(ExtensionHistory plugin) {}

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setHorizontalScrollBarPolicy(
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setVerticalScrollBarPolicy(
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            jScrollPane.setViewportView(getTxtDisplay());
        }
        return jScrollPane;
    }

    public HistoryReference getHistoryRef() {
        return historyRef;
    }

    public void setHistoryRef(HistoryReference historyRef) {
        this.historyRef = historyRef;
    }

    public void setNote(String note) {
        getTxtDisplay().setText(note);
        getTxtDisplay().discardAllEdits();
    }
}
