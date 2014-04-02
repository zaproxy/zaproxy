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
package org.zaproxy.zap.extension.pscan;

import java.awt.Dialog;

import org.parosproxy.paros.Constant;
import org.zaproxy.zap.extension.pscan.scanner.RegexAutoTagScanner;

class DialogModifyAutoTagScanner extends DialogAddAutoTagScanner {

    private static final long serialVersionUID = 6536266615593275432L;

    private static final String DIALOG_TITLE = Constant.messages.getString("pscan.options.dialog.scanner.modify.title");
    
    private static final String CONFIRM_BUTTON_LABEL = Constant.messages.getString("pscan.options.dialog.scanner.modify.button.confirm");
    
    protected DialogModifyAutoTagScanner(Dialog owner) {
        super(owner, DIALOG_TITLE);
    }

    @Override
    protected String getConfirmButtonLabel() {
        return CONFIRM_BUTTON_LABEL;
    }

    public void setApp(RegexAutoTagScanner app) {
        this.scanner = app;
    }
    
    @Override
    protected boolean validateName(String name) {
        if (scanner.getName().equals(name)) {
            return true;
        }
        return super.validateName(name);
    }

    @Override
    protected void init() {
        getNameTextField().setText(scanner.getName());
        getRequestUrlRegexTextField().discardAllEdits();
        
        getTypeTextField().setText(scanner.getType().name());
        getRequestUrlRegexTextField().discardAllEdits();
        
        getConfigurationTextField().setText(scanner.getConf());
        getConfigurationTextField().discardAllEdits();
        
        getRequestUrlRegexTextField().setText(scanner.getRequestUrlRegex());
        getRequestUrlRegexTextField().discardAllEdits();
        
        getRequestHeaderRegexTextField().setText(scanner.getRequestHeaderRegex());
        getRequestHeaderRegexTextField().discardAllEdits();
        
        getResponseHeaderRegexTextField().setText(scanner.getResponseHeaderRegex());
        getResponseHeaderRegexTextField().discardAllEdits();
        
        getResponseBodyRegexTextField().setText(scanner.getResponseBodyRegex());
        getResponseBodyRegexTextField().discardAllEdits();
        
        getEnabledCheckBox().setSelected(scanner.isEnabled());
    }

}
