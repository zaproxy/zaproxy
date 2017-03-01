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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.zap.extension.alert.ExtensionAlert;

/**
 * An {@link ExtensionPopupMenuItem} that exposes the selected {@link Alert alerts} of the Alerts tree.
 * 
 * @since TODO add version
 * @see #performAction(Alert)
 */
public abstract class PopupMenuItemAlert extends ExtensionPopupMenuItem {

    private static final long serialVersionUID = 1L;
    private final boolean multiSelect;
    private final ExtensionAlert extAlert;

    private static final Logger log = Logger.getLogger(PopupMenuItemAlert.class);

    /**
     * Constructs a {@code PopupMenuItemAlert} with the given label and with no support for multiple selected alerts (the menu
     * button will not be enabled when the Alerts tree has multiple selected alerts).
     * 
     * @param label the label of the menu item
     * @see #isEnableForComponent(Component)
     */
    public PopupMenuItemAlert(String label) {
        this(label, false);
    }

    /**
     * Constructs a {@code PopupMenuItemAlert} with the given label and whether or not the menu item supports multiple selected
     * alerts (if {@code false} the menu button will not be enabled when the Alerts tree has multiple selected alerts).
     * 
     * @param label the label of the menu item
     * @param multiSelect {@code true} if the menu item supports multiple selected alerts, {@code false} otherwise.
     * @see #isEnableForComponent(Component)
     */
    public PopupMenuItemAlert(String label, boolean multiSelect) {
        super(label);
        this.multiSelect = multiSelect;
        addActionListener(new PerformActionsActionListener());
        this.extAlert = Control.getSingleton().getExtensionLoader().getExtension(ExtensionAlert.class);
	}

    /**
     * Tells whether or not the menu item supports multiple selected alerts. If {@code false} the menu button will not be
     * enabled when the Alerts tree has multiple selected alerts.
     * 
     * @return {@code true} if the menu item supports multiple selected alerts, {@code false} otherwise.
     * @see #isButtonEnabledForNumberOfSelectedAlerts(int)
     */
    public final boolean isMultiSelect() {
        return multiSelect;
    }

    private Set<Alert> getAlertNodes() {
        TreePath[] paths = this.extAlert.getAlertPanel().getTreeAlert().getSelectionPaths();
        if (paths == null || paths.length == 0) {
            return Collections.emptySet();
        }

        HashSet<Alert> alertNodes = new HashSet<Alert>();
        if (!isMultiSelect()) {
            DefaultMutableTreeNode alertNode = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
            alertNodes.add((Alert) alertNode.getUserObject());
            return alertNodes;
        }

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

    /**
     * Performs the action of the menu item for the given selected alert.
     * <p>
     * By default, it's called for each selected alert.
     *
     * @param alert the selected alert, never {@code null}
     * @see #performActions(Set)
     */
    protected abstract void performAction(Alert alert);

    /**
     * Performs the action of the menu item for each of the given selected alerts.
     * <p>
     * Called when the pop up menu item is chosen.
     * 
     * @param alerts the selected alerts, never {@code null}
     * @see #performAction(Alert)
     */
    protected void performActions(Set<Alert> alerts) {
        for(Alert alert: alerts) {
            performAction(alert);
        }
    }

    /**
     * Tells whether or not the button should be enabled for the given number of selected alerts.
     * <p>
     * If multiple alert nodes are selected the {@code count} corresponds to the number of selected alerts. If just a middle
     * alert node (that is, the nodes that show the alert name) is selected the {@code count} is only one alert when
     * {@link #isMultiSelect() multiple selection} is not supported, otherwise it is the number of child nodes (which is one
     * alert per node).
     * <p>
     * By default the button is only enabled if at least one alert is selected. For menu items that do not support multiple
     * selection it's only enabled if just one alert is selected.
     * <p>
     * <strong>Note:</strong> This method is only called if the invoker is the Alerts tree and the root node is not selected.
     * 
     * @param count the number of selected alerts
     * @return {@code true} if the button should be enabled, {@code false} otherwise
     */
    protected boolean isButtonEnabledForNumberOfSelectedAlerts(int count) {
        if(count == 0 ) {
            return false;
        } else if(!isMultiSelect() && count>1 ) {
            return false;
        }
        return true;
    }

    /**
     * @see #isButtonEnabledForNumberOfSelectedAlerts(int)
     */
    @Override
    public boolean isEnableForComponent(Component invoker) {
        if(this.extAlert == null) {
            return false;
        }
        if ("treeAlert".equals(invoker.getName())) {
            setEnabled(!this.extAlert.getAlertPanel().getTreeAlert().isRowSelected(0) && 
                    isButtonEnabledForNumberOfSelectedAlerts(getNumberOfSelectedAlerts()));
            return true;
        }
        return false;
    }

    /**
     * Gets the number of selected alerts in the Alerts tree.
     * <p>
     * If multiple alert nodes are selected it returns the corresponding number of alerts. If just a middle alert node (that is,
     * the nodes that show the alert name) is selected it returns only one selected alert when {@link #isMultiSelect() multiple
     * selection} is not supported, otherwise it returns the number of child nodes (which is one alert per node).
     * 
     * @return the number of selected nodes
     */
    private int getNumberOfSelectedAlerts() {
        JTree treeAlert = this.extAlert.getAlertPanel().getTreeAlert();
        int count = treeAlert.getSelectionCount();
        if (count == 0) {
            return 0;
        }

        if (count == 1) {
            DefaultMutableTreeNode alertNode = (DefaultMutableTreeNode) treeAlert.getSelectionPath().getLastPathComponent();
            if (alertNode.getChildCount() == 0 || !isMultiSelect()) {
                return 1;
            }
            return alertNode.getChildCount();
        }

        count = 0;
        TreePath[] paths = treeAlert.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            TreePath nodePath = paths[i];
            int childCount = ((DefaultMutableTreeNode) nodePath.getLastPathComponent()).getChildCount();
            count += childCount != 0 ? childCount : (treeAlert.isPathSelected(nodePath.getParentPath()) ? 0 : 1);
        }
        return count;
    }

    /**
     * Gets the {@code ExtensionAlert}.
     *
     * @return the {@code ExtensionAlert}, or {@code null} if the extension is not enabled.
     */
    protected ExtensionAlert getExtensionAlert() {
        return extAlert;
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