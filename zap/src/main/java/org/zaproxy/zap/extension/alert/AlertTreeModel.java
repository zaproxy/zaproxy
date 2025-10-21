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

import java.awt.EventQueue;
import java.util.Comparator;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.view.View;

@SuppressWarnings("serial")
class AlertTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;

    private static final Comparator<AlertNode> GROUP_ALERT_CHILD_COMPARATOR =
            new GroupAlertChildNodeComparator();
    private static final Comparator<AlertNode> ALERT_CHILD_COMPARATOR =
            new AlertChildNodeComparator();

    private static final Logger LOGGER = LogManager.getLogger(AlertTreeModel.class);

    private ExtensionAlert ext;

    AlertTreeModel(ExtensionAlert ext) {
        super(
                new AlertNode(
                        -1,
                        Constant.messages.getString("alerts.tree.title"),
                        GROUP_ALERT_CHILD_COMPARATOR));
        this.ext = ext;
    }

    void addPath(final Alert alert) {
        if (!View.isInitialised() || EventQueue.isDispatchThread()) {
            addPathEventHandler(alert);
        } else {
            try {
                EventQueue.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                addPathEventHandler(alert);
                            }
                        });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @since 2.17.0
     */
    @Override
    public AlertNode getRoot() {
        return (AlertNode) super.getRoot();
    }

    protected synchronized AlertNode addPathEventHandler(Alert alert) {
        AlertNode parent = findAndAddGroup(getRoot(), alert.getName(), alert);
        // Show the method first, if present
        String method = "";
        if (alert.getMethod() != null) {
            method = alert.getMethod() + ":";
        }
        String name =
                method
                        + (StringUtils.isNotEmpty(alert.getNodeName())
                                ? alert.getNodeName()
                                : alert.getUri());
        return addLeaf(parent, name, alert);
    }

    private AlertNode findLeafNodeForAlert(AlertNode parent, Alert alert) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            AlertNode child = parent.getChildAt(i);
            if (child.getChildCount() == 0) {
                // Its a leaf node
                if (child.getAlert() != null && child.getAlert().compareTo(alert) == 0) {
                    return child;
                }
            } else {
                // check its children
                AlertNode node = findLeafNodeForAlert(child, alert);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    public AlertNode getAlertNode(Alert alert) {
        AlertNode parent = getRoot();
        int risk = alert.getRisk();
        if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case!
            risk = -1;
        }

        AlertNode needle = new AlertNode(risk, alert.getName(), GROUP_ALERT_CHILD_COMPARATOR);
        needle.setAlert(alert);
        int idx = parent.findIndex(needle);
        if (idx < 0) {
            return null;
        }
        parent = parent.getChildAt(idx);
        idx = parent.findIndex(needle);
        if (idx < 0) {
            return null;
        }
        return parent.getChildAt(idx);
    }

    void updatePath(final Alert alert) {
        if (!View.isInitialised() || EventQueue.isDispatchThread()) {
            updatePathEventHandler(alert);
        } else {
            try {
                EventQueue.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                updatePathEventHandler(alert);
                            }
                        });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private synchronized void updatePathEventHandler(Alert alert) {

        AlertNode node = findLeafNodeForAlert(getRoot(), alert);
        if (node != null) {

            // Remove the old version
            AlertNode parent = node.getParent();

            // Cannot use removeNodeFromParent as the risk or name might have changed
            removeChildById(parent, alert.getAlertId());
            nodeStructureChanged(parent);

            if (parent.getChildCount() == 0) {
                // Parent has no other children, remove it also
                this.removeNodeFromParent(parent);
                nodeStructureChanged(this.getRoot());
            }
        }
        // Add it back in again
        this.addPath(alert);
    }

    private void removeChildById(AlertNode parent, int alertId) {
        int idx = -1;
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i).getAlert().getAlertId() == alertId) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            parent.remove(idx);
        }
    }

    private AlertNode findAndAddGroup(AlertNode parent, String nodeName, Alert alert) {
        int risk = alert.getRisk();
        if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case!
            risk = -1;
        }

        AlertNode node = new AlertNode(risk, nodeName, ALERT_CHILD_COMPARATOR);
        int idx = parent.findIndex(node);
        if (idx < 0) {
            idx = -(idx + 1);
            node.setAlert(alert);
            parent.insert(node, idx);
            nodesWereInserted(parent, new int[] {idx});
            nodeChanged(parent);
            return node;
        }
        return parent.getChildAt(idx);
    }

    private AlertNode addLeaf(AlertNode parent, String nodeName, Alert alert) {
        int risk = alert.getRisk();
        if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case!
            risk = -1;
        }

        AlertNode needle = new AlertNode(risk, nodeName, ALERT_CHILD_COMPARATOR);
        needle.setAlert(alert);
        int idx = parent.findIndex(needle);
        if (idx < 0) {
            // Not a duplicate alert
            if (ext.isOverSystemicLimit(alert)) {
                if (!parent.isSystemic()) {
                    parent.setSystemic(true);
                    nodeChanged(parent);
                }
                return null;
            }
            idx = -(idx + 1);
            parent.insert(needle, idx);
            nodesWereInserted(parent, new int[] {idx});
            nodeChanged(parent);
            return needle;
        }
        return null;
    }

    public synchronized void deletePath(Alert alert) {

        AlertNode node = findLeafNodeForAlert(getRoot(), alert);
        if (node != null) {
            AlertNode parent = node.getParent();
            if (parent.getChildCount() == 1) {
                // Parent has no other children, remove it also
                parent.remove(0);
                AlertNode grandParent = parent.getParent();
                this.removeNodeFromParent(parent);
                this.nodeChanged(grandParent);
                return;
            }

            // Remove it
            this.removeNodeFromParent(node);
            if (parent.getAlert() == node.getAlert()) {
                parent.setAlert(parent.getChildAt(0).getAlert());
            }
            this.nodeChanged(parent);
        }
    }

    private static class GroupAlertChildNodeComparator implements Comparator<AlertNode> {

        @Override
        public int compare(AlertNode alertNode, AlertNode anotherAlertNode) {
            if (alertNode.getRisk() < anotherAlertNode.getRisk()) {
                return 1;
            } else if (alertNode.getRisk() > anotherAlertNode.getRisk()) {
                return -1;
            }

            return alertNode.getNodeName().compareTo(anotherAlertNode.getNodeName());
        }
    }

    private static class AlertChildNodeComparator implements Comparator<AlertNode> {

        @Override
        public int compare(AlertNode alertNode, AlertNode anotherAlertNode) {
            return alertNode.getAlert().compareTo(anotherAlertNode.getAlert());
        }
    }
}
