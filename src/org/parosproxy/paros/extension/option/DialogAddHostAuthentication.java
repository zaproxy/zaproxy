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
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.parosproxy.paros.extension.option;

import java.awt.Dialog;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HostAuthentication;
import org.zaproxy.zap.utils.ZapPortNumberSpinner;
import org.zaproxy.zap.utils.ZapTextField;
import org.zaproxy.zap.view.AbstractFormDialog;

class DialogAddHostAuthentication extends AbstractFormDialog {

    private static final long serialVersionUID = 2818703974735043009L;

    private static final String DIALOG_TITLE = Constant.messages.getString("options.auth.dialog.hostAuth.add.title");
    
    private static final String CONFIRM_BUTTON_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.add.button.confirm");
    
    private static final String NAME_FIELD_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.field.label.name");
    private static final String HOST_FIELD_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.field.label.host");
    private static final String PORT_NUMBER_FIELD_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.field.label.port");
    private static final String USER_NAME_FIELD_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.field.label.uname");
    private static final String PASSWORD_FIELD_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.field.label.password");
    private static final String REALM_FIELD_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.field.label.realm");
    private static final String ENABLED_FIELD_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.field.label.enabled");

    private static final String TITLE_NAME_REPEATED_DIALOG = Constant.messages.getString("options.auth.dialog.hostAuth.warning.name.repeated.title");
    private static final String TEXT_NAME_REPEATED_DIALOG = Constant.messages.getString("options.auth.dialog.hostAuth.warning.name.repeated.text");

    
    private ZapTextField nameTextField;
    private ZapTextField hostTextField;
    private ZapPortNumberSpinner portNumberSpinner;
    private ZapTextField userNameTextField;
    private ZapTextField passwordTextField;
    private ZapTextField realmTextField;
    private JCheckBox enabledCheckBox;
    
    protected HostAuthentication hostAuthentication;
    private List<HostAuthentication> auths;
    
    private ConfirmButtonValidatorDocListener confirmButtonValidatorDocListener;
    
    public DialogAddHostAuthentication(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }
    
    protected DialogAddHostAuthentication(Dialog owner, String title) {
        super(owner, title);
    }
    
    @Override
    protected JPanel getFieldsPanel() {
        JPanel fieldsPanel = new JPanel();
        
        GroupLayout layout = new GroupLayout(fieldsPanel);
        fieldsPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        
        JLabel nameLabel = new JLabel(NAME_FIELD_LABEL);
        JLabel hostLabel = new JLabel(HOST_FIELD_LABEL);
        JLabel portNumberLabel = new JLabel(PORT_NUMBER_FIELD_LABEL);
        JLabel userNameLabel = new JLabel(USER_NAME_FIELD_LABEL);
        JLabel passwordLabel = new JLabel(PASSWORD_FIELD_LABEL);
        JLabel realmLabel = new JLabel(REALM_FIELD_LABEL);
        JLabel enabledLabel = new JLabel(ENABLED_FIELD_LABEL);
        
        layout.setHorizontalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                .addComponent(nameLabel)
                .addComponent(hostLabel)
                .addComponent(portNumberLabel)
                .addComponent(userNameLabel)
                .addComponent(passwordLabel)
                .addComponent(realmLabel)
                .addComponent(enabledLabel))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(getNameTextField())
                .addComponent(getHostTextField())
                .addComponent(getPortNumberSpinner())
                .addComponent(getUserNameTextField())
                .addComponent(getPasswordTextField())
                .addComponent(getRealmTextField())
                .addComponent(getEnabledCheckBox()))
        );
        
        layout.setVerticalGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(nameLabel)
                .addComponent(getNameTextField()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(hostLabel)
                .addComponent(getHostTextField()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(portNumberLabel)
                .addComponent(getPortNumberSpinner()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(userNameLabel)
                .addComponent(getUserNameTextField()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(passwordLabel)
                .addComponent(getPasswordTextField()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(realmLabel)
                .addComponent(getRealmTextField()))
            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(enabledLabel)
                .addComponent(getEnabledCheckBox()))
        );
        
        return fieldsPanel;
    }
    
    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }
    
    @Override
    protected void init() {
        getNameTextField().setText("");
        getHostTextField().setText("");
        getPortNumberSpinner().setValue(Integer.valueOf(80));
        getUserNameTextField().setText("");
        getPasswordTextField().setText("");
        getRealmTextField().setText("");
        getEnabledCheckBox().setSelected(true);
        
        hostAuthentication = null;
    }

    @Override
    protected boolean validateFields() {
        String hostAuthenticationName = getNameTextField().getText();
        for (HostAuthentication t : auths) {
            if (hostAuthenticationName.equals(t.getName())) {
                JOptionPane.showMessageDialog(this, TEXT_NAME_REPEATED_DIALOG,
                        TITLE_NAME_REPEATED_DIALOG,
                        JOptionPane.INFORMATION_MESSAGE);
                getNameTextField().requestFocusInWindow();
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    protected void performAction() {
        hostAuthentication = new HostAuthentication(
                getNameTextField().getText(),
                getHostTextField().getText(),
                getPortNumberSpinner().getValue().intValue(),
                getUserNameTextField().getText(),
                getPasswordTextField().getText(),
                getRealmTextField().getText());
        hostAuthentication.setEnabled(getEnabledCheckBox().isSelected());
    }
    
    @Override
    protected void clearFields() {
        getNameTextField().setText("");
        getNameTextField().discardAllEdits();
        getHostTextField().setText("");
        getHostTextField().discardAllEdits();
        getUserNameTextField().setText(""); 
        getUserNameTextField().discardAllEdits();
        getPasswordTextField().setText(""); 
        getPasswordTextField().discardAllEdits();
        getRealmTextField().setText(""); 
        getRealmTextField().discardAllEdits();
    }

    public HostAuthentication getToken() {
        return hostAuthentication;
    }
    
    protected ZapTextField getNameTextField() {
        if (nameTextField == null) {
            nameTextField = new ZapTextField(25);
            nameTextField.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }
        
        return nameTextField;
    }
    
    protected ZapTextField getHostTextField() {
        if (hostTextField == null) {
            hostTextField = new ZapTextField(25);
            hostTextField.getDocument().addDocumentListener(getConfirmButtonValidatorDocListener());
        }
        
        return hostTextField;
    }
    
    protected ZapPortNumberSpinner getPortNumberSpinner() {
        if (portNumberSpinner == null) {
            portNumberSpinner = new ZapPortNumberSpinner(80);
        }
        
        return portNumberSpinner;
    }
    
    protected ZapTextField getUserNameTextField() {
        if (userNameTextField == null) {
            userNameTextField = new ZapTextField(25);
        }
        
        return userNameTextField;
    }
    
    protected ZapTextField getPasswordTextField() {
        if (passwordTextField == null) {
            passwordTextField = new ZapTextField(25);
        }
        
        return passwordTextField;
    }
    
    protected ZapTextField getRealmTextField() {
        if (realmTextField == null) {
            realmTextField = new ZapTextField(25);
        }
        
        return realmTextField;
    }
    
    protected JCheckBox getEnabledCheckBox() {
        if (enabledCheckBox == null) {
            enabledCheckBox = new JCheckBox();
        }
        
        return enabledCheckBox;
    }

    public void setAuthentications(List<HostAuthentication> auths) {
        this.auths = auths;
    }

    public void clear() {
        this.auths =  null;
        this.hostAuthentication = null;
    }

    private ConfirmButtonValidatorDocListener getConfirmButtonValidatorDocListener() {
        if (confirmButtonValidatorDocListener == null) {
            confirmButtonValidatorDocListener = new ConfirmButtonValidatorDocListener(); 
        }
        
        return confirmButtonValidatorDocListener;
    }
    
    private class ConfirmButtonValidatorDocListener implements DocumentListener {

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkAndEnableConfirmButton();
        }

        private void checkAndEnableConfirmButton() {
            boolean enabled = (getNameTextField().getDocument().getLength() > 0)
                    && (getHostTextField().getDocument().getLength() > 0);
            setConfirmButtonEnabled(enabled);
        }
    }
    
}
