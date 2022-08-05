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
package org.zaproxy.zap.extension.alert;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.AbstractDialog;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.network.HttpMessage;

@SuppressWarnings("serial")
public class AlertAddDialog extends AbstractDialog {

    private static final Logger logger = LogManager.getLogger(AlertAddDialog.class);

    private static final long serialVersionUID = 1L;

    private JPanel jPanel = null;
    private JButton btnOk = null;
    private JButton btnCancel = null;

    private HistoryReference historyRef;

    /**
     * The history type that will be used, along with the instance variable {@code httpMessage}, to
     * create a new {@code HistoryReference}. Used when the user wants to add an alert to a
     * temporary {@code HistoryReference} (created when active scanning, fuzzing...), as the
     * temporary {@code HistoryReference}s are deleted when the session is closed a new {@code
     * HistoryReference} must be created.
     *
     * @see #httpMessage
     * @see #setHttpMessage(HttpMessage, int)
     * @see HistoryReference#HistoryReference(org.parosproxy.paros.model.Session, int, HttpMessage)
     */
    private int historyType;

    /**
     * The {@code HttpMessage} that will be used, along with {@code historyType} , to create a new
     * {@code HistoryReference} (created when active scanning, fuzzing...). Used when the user wants
     * to add an alert to a temporary {@code HistoryReference}, as the temporary {@code
     * HistoryReference}s are deleted when the session is closed a new {@code HistoryReference} must
     * be created. If {@code null} indicates that no {@code HistoryReference} should be created.
     *
     * @see #historyType
     * @see #setHttpMessage(HttpMessage, int)
     * @see HistoryReference#HistoryReference(org.parosproxy.paros.model.Session, int, HttpMessage)
     */
    private HttpMessage httpMessage;

    private AlertViewPanel alertViewPanel = null;

    /** @throws HeadlessException */
    public AlertAddDialog() throws HeadlessException {
        super();
        initialize();
    }

    /**
     * @param arg0
     * @param arg1
     * @throws HeadlessException
     */
    public AlertAddDialog(Frame arg0, boolean arg1) throws HeadlessException {
        super(arg0, arg1);
        initialize();
    }

    /** This method initializes this */
    private void initialize() {
        this.setTitle(Constant.messages.getString("alert.add.title"));
        this.setContentPane(getJPanel());
        if (Model.getSingleton().getOptionsParam().getViewParam().getWmUiHandlingOption() == 0) {
            this.setSize(407, 657);
        }
        this.addWindowListener(
                new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowOpened(java.awt.event.WindowEvent e) {}

                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        btnCancel.doClick();
                    }
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
            GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
            java.awt.GridBagConstraints gridBagConstraints13 = new GridBagConstraints();

            javax.swing.JLabel jLabel2 = new JLabel();

            java.awt.GridBagConstraints gridBagConstraints3 = new GridBagConstraints();

            java.awt.GridBagConstraints gridBagConstraints2 = new GridBagConstraints();

            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.setPreferredSize(new java.awt.Dimension(450, 650));
            jPanel.setMinimumSize(new java.awt.Dimension(450, 650));
            gridBagConstraints2.gridx = 1;
            gridBagConstraints2.gridy = 5;
            gridBagConstraints2.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints3.gridx = 2;
            gridBagConstraints3.gridy = 5;
            gridBagConstraints3.insets = new java.awt.Insets(2, 2, 2, 10);
            gridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;

            gridBagConstraints13.gridx = 0;
            gridBagConstraints13.gridy = 5;
            gridBagConstraints13.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints13.weightx = 1.0D;
            gridBagConstraints13.insets = new java.awt.Insets(2, 10, 2, 5);

            gridBagConstraints15.weightx = 1.0D;
            gridBagConstraints15.weighty = 1.0D;
            gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints15.insets = new java.awt.Insets(2, 2, 2, 2);
            gridBagConstraints15.gridwidth = 3;
            gridBagConstraints15.gridx = 0;
            gridBagConstraints15.gridy = 2;
            gridBagConstraints15.anchor = java.awt.GridBagConstraints.NORTHWEST;
            gridBagConstraints15.ipadx = 0;
            gridBagConstraints15.ipady = 10;

            jPanel.add(getAlertViewPanel(), gridBagConstraints15);
            jPanel.add(jLabel2, gridBagConstraints13);
            jPanel.add(getBtnCancel(), gridBagConstraints2);
            jPanel.add(getBtnOk(), gridBagConstraints3);
        }
        return jPanel;
    }
    /**
     * This method initializes btnStart
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnOk() {
        if (btnOk == null) {
            btnOk = new JButton();
            btnOk.setText(Constant.messages.getString("alert.add.button.save"));
            btnOk.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            Alert alert = alertViewPanel.getAlert();
                            try {
                                ExtensionAlert extAlert =
                                        Control.getSingleton()
                                                .getExtensionLoader()
                                                .getExtension(ExtensionAlert.class);
                                if (alert.getAlertId() >= 0) {
                                    // Its an existing alert so save it
                                    extAlert.updateAlert(alert);
                                } else {
                                    if (httpMessage != null) {
                                        historyRef =
                                                new HistoryReference(
                                                        Model.getSingleton().getSession(),
                                                        historyType,
                                                        httpMessage);
                                    }

                                    alert.setSource(Alert.Source.MANUAL);
                                    // Raise it
                                    extAlert.alertFound(alert, historyRef);
                                }
                            } catch (Exception ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                            clearAndCloseDialog();
                        }
                    });
        }
        return btnOk;
    }

    /**
     * This method initializes btnStop
     *
     * @return javax.swing.JButton
     */
    private JButton getBtnCancel() {
        if (btnCancel == null) {
            btnCancel = new JButton();
            btnCancel.setText(Constant.messages.getString("alert.add.button.cancel"));
            btnCancel.setEnabled(true);
            btnCancel.addActionListener(
                    new java.awt.event.ActionListener() {

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            clearAndCloseDialog();
                        }
                    });
        }
        return btnCancel;
    }

    private void clearAndCloseDialog() {
        getAlertViewPanel().clearAlert();
        historyRef = null;
        httpMessage = null;
        dispose();
    }

    private AlertViewPanel getAlertViewPanel() {
        if (alertViewPanel == null) {
            alertViewPanel = new AlertViewPanel(true);
        }
        return this.alertViewPanel;
    }

    public void setAlert(Alert alert) {
        this.getAlertViewPanel().displayAlert(alert);
        if (alert.getHistoryRef() != null) {
            this.setHistoryRef(alert.getHistoryRef());
        } else {
            this.setHistoryRef(alert.getMessage().getHistoryRef());
        }
        // Change the title as we're editing an existing alert
        this.setTitle(Constant.messages.getString("alert.edit.title"));
    }

    public HistoryReference getHistoryRef() {
        return historyRef;
    }

    public void setHistoryRef(HistoryReference historyRef) {
        this.historyRef = historyRef;
        this.httpMessage = null;
        alertViewPanel.setHistoryRef(historyRef);
    }

    /**
     * Sets the {@code HttpMessage} and the history type of the {@code HistoryReference} that will
     * be created if the user creates the alert. The current session will be used to create the
     * {@code HistoryReference}. The alert created will be added to the newly created {@code
     * HistoryReference}.
     *
     * <p>Should be used when the alert is added to a temporary {@code HistoryReference} as the
     * temporary {@code HistoryReference}s are deleted when the session is closed.
     *
     * @param httpMessage the {@code HttpMessage} that will be used to create the {@code
     *     HistoryReference}, must not be {@code null}
     * @param historyType the type of the history reference that will be used to create the {@code
     *     HistoryReference}
     * @see Model#getSession()
     * @see HistoryReference#HistoryReference(org.parosproxy.paros.model.Session, int, HttpMessage)
     */
    public void setHttpMessage(HttpMessage httpMessage, int historyType) {
        this.historyRef = null;
        this.httpMessage = httpMessage;
        this.historyType = historyType;
        alertViewPanel.setHttpMessage(httpMessage);
    }
}
