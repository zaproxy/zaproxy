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

import java.awt.Component;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;

import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;


/**
 * A {@link PopupMenuItemAlert} that allows to refresh the Alerts tree (the UI tree is rebuilt from the model already set).
 * 
 * @since 1.4.0
 */
public class PopupMenuAlertsRefresh extends PopupMenuItemAlert {

	private static final long serialVersionUID = 1L;

    public PopupMenuAlertsRefresh() {
        super(Constant.messages.getString("alerts.refresh.popup"), true);
	}
	
    @Override
    protected void performActions(Set<Alert> alerts) {
        ((DefaultTreeModel) getExtensionAlert().getAlertPanel().getTreeAlert().getModel()).reload();
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if (super.isEnableForComponent(invoker)) {
            setEnabled(true);
            return true;
        }
        return false;
    }
    
    @Override
    protected void performAction(Alert alert) {
        // Nothing to do.
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
