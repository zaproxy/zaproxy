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
package org.zaproxy.zap.extension.invoke;

import java.awt.Dialog;

import org.parosproxy.paros.Constant;

class DialogModifyApp extends DialogAddApp {

    private static final long serialVersionUID = 2527021826845655293L;

    private static final String DIALOG_TITLE = Constant.messages.getString("invoke.options.dialog.app.modify.title");
    
    private static final String CONFIRM_BUTTON_LABEL = Constant.messages.getString("invoke.options.dialog.app.modify.button.confirm");
    
    protected DialogModifyApp(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    public void setApp(InvokableApp app) {
        this.app = app;
    }
    
    @Override
    protected boolean validateFields() {
        if (app.getDisplayName().equals(getDisplayNameTextField().getText())) {
            return true;
        }
        return super.validateFields();
    }

    @Override
    protected void init() {
        getDisplayNameTextField().setText(app.getDisplayName());
        getDisplayNameTextField().discardAllEdits();
        
        getFullCommandTextField().setText(app.getFullCommand());
        getFullCommandTextField().discardAllEdits();
        
        if (app.getWorkingDirectory() != null) {
            getWorkingDirTextField().setText(app.getWorkingDirectory().getAbsolutePath());
        }
        getWorkingDirTextField().discardAllEdits();
        
        getParametersTextField().setText(app.getParameters());
        getParametersTextField().discardAllEdits();
        
        getCaptureOutputCheckBox().setSelected(app.isCaptureOutput());
        
        getOutputToNoteCheckBox().setEnabled(app.isCaptureOutput());
        getOutputToNoteCheckBox().setSelected(app.isOutputNote());
        
        getEnabledCheckBox().setSelected(app.isEnabled());
    }

}
