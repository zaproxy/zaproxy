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

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.network.HostAuthentication;

class DialogModifyHostAuthentication extends DialogAddHostAuthentication {

    private static final long serialVersionUID = 4169797008846547121L;

    private static final String DIALOG_TITLE = Constant.messages.getString("options.auth.dialog.hostAuth.modify.title");
    
    private static final String CONFIRM_BUTTON_LABEL = Constant.messages.getString("options.auth.dialog.hostAuth.modify.button.confirm");
    
    protected DialogModifyHostAuthentication(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    public void setHostAuthentication(HostAuthentication hostAuthentication) {
        this.hostAuthentication = hostAuthentication;
    }
    
    @Override
    protected boolean validateFields() {
        if (hostAuthentication.getName().equals(getNameTextField().getText())) {
            return true;
        }
        return super.validateFields();
    }

    @Override
    protected void init() {
        getNameTextField().setText(hostAuthentication.getName());
        getNameTextField().discardAllEdits();
        
        getHostTextField().setText(hostAuthentication.getHostName());
        getHostTextField().discardAllEdits();
        
        getPortNumberSpinner().setValue(Integer.valueOf(hostAuthentication.getPort()));
        
        getUserNameTextField().setText(hostAuthentication.getUserName());
        getUserNameTextField().discardAllEdits();
        
        getPasswordTextField().setText(hostAuthentication.getPassword());
        getPasswordTextField().discardAllEdits();
        
        getRealmTextField().setText(hostAuthentication.getRealm());
        getRealmTextField().discardAllEdits();
        
        getEnabledCheckBox().setSelected(hostAuthentication.isEnabled());
    }

}
