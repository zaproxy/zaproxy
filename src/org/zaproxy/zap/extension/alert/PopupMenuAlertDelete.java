/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
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
package org.zaproxy.zap.extension.alert;

import java.util.Set;

import javax.swing.JOptionPane;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.view.View;


/**
 * A {@link PopupMenuItemAlert} that allows to delete {@link Alert alerts}.
 * 
 * @since 1.4.0
 */
public class PopupMenuAlertDelete extends PopupMenuItemAlert {

	private static final long serialVersionUID = 1L;

    public PopupMenuAlertDelete() {
        super(Constant.messages.getString("scanner.delete.popup"), true);
	}
	
    @Override
    protected void performActions(Set<Alert> alerts) {
        if (View.getSingleton()
                .showConfirmDialog(Constant.messages.getString("scanner.delete.confirm")) != JOptionPane.OK_OPTION) {
            return;
        }
        super.performActions(alerts);
    }

    @Override
    protected void performAction(Alert alert) {
        getExtensionAlert().deleteAlert(alert);
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
