/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright ZAP Development Team
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Component;
import java.util.HashSet;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.zap.extension.alert.ExtensionAlert;


public abstract class PopupMenuItemAlert extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 1L;
    private final boolean multiSelect;
    private final ExtensionAlert extAlert;

    private static final Logger log = Logger.getLogger(PopupMenuItemAlert.class);

    public PopupMenuItemAlert(String label) {
        this(label, false);
    }

    public PopupMenuItemAlert(String label, boolean multiSelect) {
        super(label);
        this.multiSelect = multiSelect;
        addActionListener(new PerformActionsActionListener());
        this.extAlert = Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
	}

    public final boolean isMultiSelect() {
        return multiSelect;
    }

    private Set<Alert> getAlertNodes() {
        TreePath[] paths = this.extAlert.getAlertPanel().getTreeAlert().getSelectionPaths();
        HashSet<Alert> alertNodes = new HashSet<Alert>();
        for(int i = 0; i < paths.length; i++ ) {
            DefaultMutableTreeNode alertNode = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
            if(alertNode.getChildCount() == 0) {
                alertNodes.add((Alert)alertNode.getUserObject());
                continue;
            }
            for(int j = 0; j < alertNode.getChildCount(); j++ ) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)alertNode.getChildAt(j);
                alertNodes.add((Alert)node.getUserObject());
            }
        }
        return alertNodes;
    }

    protected abstract void performAction(Alert alert);

    protected void performActions(Set<Alert> alerts) {
        for(Alert alert: alerts) {
            performAction(alert);
        }
    }

    protected boolean isButtonEnabledForNumberOfSelectedAlerts(int count) {
        if(count == 0 ) {
            return false;
        } else if(!isMultiSelect() && count>1 ) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        if(this.extAlert == null) {
            return false;
        }
        if ("treeAlert".equals(invoker.getName())) {
            int count = this.extAlert.getAlertPanel().getTreeAlert().getSelectionCount();
            setEnabled(!this.extAlert.getAlertPanel().getTreeAlert().isRowSelected(0) && isButtonEnabledForNumberOfSelectedAlerts(count));
            return true;
        }
        return false;
    }

    private class PerformActionsActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent evt) {
            try {
                Set<Alert> alerts = getAlertNodes();
                performActions(alerts);
            } catch (Exception e) {
                log.error(e.getMessage(),e);
            }
        }
    }

}