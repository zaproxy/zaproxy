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
import java.util.List;
import javax.swing.tree.DefaultTreeModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.control.Control;
import org.parosproxy.paros.core.scanner.Alert;
import org.parosproxy.paros.extension.history.ExtensionHistory;
import org.parosproxy.paros.view.View;

class AlertTreeModel extends DefaultTreeModel {

    private static final long serialVersionUID = 1L;

    private static final Comparator<AlertNode> GROUP_ALERT_CHILD_COMPARATOR =
            new GroupAlertChildNodeComparator();
    private static final Comparator<AlertNode> ALERT_CHILD_COMPARATOR =
            new AlertChildNodeComparator();

    private static final Logger LOGGER = LogManager.getLogger(AlertTreeModel.class);

    AlertTreeModel() {
        super(
                new AlertNode(
                        -1,
                        Constant.messages.getString("alerts.tree.title"),
                        GROUP_ALERT_CHILD_COMPARATOR));
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

    @Override
    public AlertNode getRoot() {
        return (AlertNode) super.getRoot();
    }

    protected synchronized AlertNode addPathEventHandler(Alert alert) {
        AlertNode parent = getRoot();
        parent = findAndAddChild(parent, alert.getName(), alert);
        // Show the method first, if present
        String method = "";
        if (alert.getMethod() != null) {
            method = alert.getMethod() + ": ";
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
                if (child.hasAlert(alert)) {
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

        AlertNode needle = new AlertNode(risk, alert.getName());
        needle.setUserObject(alert);
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

    void updatePath(final Alert originalAlert, final Alert alert) {
        if (!View.isInitialised() || EventQueue.isDispatchThread()) {
            updatePathEventHandler(originalAlert, alert);
        } else {
            try {
                EventQueue.invokeLater(
                        new Runnable() {
                            @Override
                            public void run() {
                                updatePathEventHandler(originalAlert, alert);
                            }
                        });
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    private synchronized void updatePathEventHandler(Alert originalAlert, Alert alert) {

        List<Alert> alerts = null;
        AlertNode node = findLeafNodeForAlert(getRoot(), originalAlert);
        if (node != null) {
            alerts = node.getAllAlerts();
            for (Alert a : alerts) {
                boolean changed =
                        a.getRisk() != alert.getRisk()
                                || a.getConfidence() != alert.getConfidence();
                alert.propagateInto(a);
                if (changed) {
                    Control.getSingleton()
                            .getExtensionLoader()
                            .getExtension(ExtensionHistory.class)
                            .notifyHistoryItemChanged(a.getHistoryRef());
                }
            }

            // Remove the old version
            AlertNode parent = node.getParent();

            this.removeNodeFromParent(node);
            nodeStructureChanged(parent);

            if (parent.getChildCount() == 0) {
                // Parent has no other children, remove it also
                this.removeNodeFromParent(parent);
                nodeStructureChanged(this.getRoot());
            }
        }
        // Add it back in again
        node = this.addPathEventHandler(alert);
        if (node != null && alerts != null) {
            node.setAlerts(alerts);
        }
    }

    private AlertNode findAndAddChild(AlertNode parent, String nodeName, Alert alert) {
        int risk = alert.getRisk();
        if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case!
            risk = -1;
        }

        int idx = parent.findIndex(new AlertNode(risk, nodeName));
        if (idx < 0) {
            idx = -(idx + 1);
            AlertNode node = new AlertNode(risk, nodeName, ALERT_CHILD_COMPARATOR);
            node.setUserObject(alert);
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

        AlertNode needle = new AlertNode(risk, nodeName);
        needle.setUserObject(alert);
        int idx = parent.findIndex(needle);
        if (idx < 0) {
            idx = -(idx + 1);
            parent.insert(needle, idx);
            nodesWereInserted(parent, new int[] {idx});
            nodeChanged(parent);
            return needle;
        }
        parent.getChildAt(idx).setUserObject(alert);
        return null;
    }

    public synchronized void deletePath(Alert alert) {

        AlertNode node = findLeafNodeForAlert(getRoot(), alert);
        if (node != null) {
            AlertNode parent = node.getParent();
            if (node.deleteAlert(alert) == 0) {
                // No more alerts for this node
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
            }
            if (parent.deleteAlert(alert) == 0) {
                // We've deleted the last alert of this type the parent knows about
                parent.setUserObject(parent.getChildAt(0).getUserObject());
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
            return alertNode.getUserObject().compareTo(anotherAlertNode.getUserObject());
        }
    }
}
