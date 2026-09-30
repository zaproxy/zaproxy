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
    private boolean mainTreeModel;

    AlertTreeModel(ExtensionAlert ext) {
        this(ext, true);
    }

    /**
     * Creates a tree model.
     *
     * @param ext the extension.
     * @param mainTreeModel whether the model is the main alerts tree, the systemic limit is only
     *     applied to it, the other models (e.g. the filtered alerts tree) are a subset of it and
     *     thus do not need to apply the limit.
     */
    AlertTreeModel(ExtensionAlert ext, boolean mainTreeModel) {
        super(
                new AlertNode(
                        -1,
                        Constant.messages.getString("alerts.tree.title"),
                        GROUP_ALERT_CHILD_COMPARATOR));
        this.ext = ext;
        this.mainTreeModel = mainTreeModel;
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
        AlertNode node = addLeaf(parent, name, alert);
        if (node == null && parent.getChildCount() == 0) {
            // The alert was not added (e.g. it's over the systemic limit) so remove the group
            // node added for it, otherwise an empty node is left behind in the tree.
            this.removeNodeFromParent(parent);
            nodeStructureChanged(getRoot());
        }
        return node;
    }

    /**
     * Finds the node for the given alert, preferring the node of the alert itself, falling back to
     * an equivalent alert (i.e. an alert de-duplicated with the given one).
     */
    private AlertNode findLeafNodeForAlert(AlertNode parent, Alert alert) {
        AlertNode node = findLeafNodeForAlert(parent, alert, true);
        if (node == null) {
            node = findLeafNodeForAlert(parent, alert, false);
        }
        return node;
    }

    /**
     * Finds the node for the given alert, matching the alert itself if {@code exactMatch},
     * otherwise any equivalent alert. Note that the returned node can be a group node with no
     * alerts (i.e. it has no children but its parent is the root).
     */
    private AlertNode findLeafNodeForAlert(AlertNode parent, Alert alert, boolean exactMatch) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            AlertNode child = parent.getChildAt(i);
            if (child.getChildCount() == 0) {
                // Its a leaf node, or a group node with no alerts
                Alert childAlert = child.getAlert();
                if (childAlert != null && matches(childAlert, alert, exactMatch)) {
                    return child;
                }
            } else {
                // check its children
                AlertNode node = findLeafNodeForAlert(child, alert, exactMatch);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    private static boolean matches(Alert alert, Alert otherAlert, boolean exactMatch) {
        if (exactMatch) {
            return alert.getAlertId() == otherAlert.getAlertId();
        }
        return alert.compareTo(otherAlert) == 0;
    }

    public AlertNode getAlertNode(Alert alert) {
        AlertNode parent = getRoot();
        int risk = alert.getRisk();
        if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case!
            risk = -1;
        }

        AlertNode needle =
                new AlertNode(
                        risk, alert.getName(), alert.getAlertRef(), GROUP_ALERT_CHILD_COMPARATOR);
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
        if (node == null) {
            // The alert is not shown in the tree, e.g. it was not added when raised (because it was
            // de-duplicated or over the systemic limit), add it now that it changed.
            this.addPath(alert);
            return;
        }

        // Remove the old version
        AlertNode parent = node.getParent();

        if (parent.isRoot()) {
            // The node is a group node with no alerts, it represents the alert so remove it,
            // it will be added back as needed below.
            this.removeNodeFromParent(node);
            nodeStructureChanged(this.getRoot());
        } else {
            // Cannot use removeNodeFromParent as the risk or name might have changed
            removeChildNode(parent, node);
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

    /**
     * Removes the given child node from the given parent node.
     *
     * <p>The node is removed by identity, not by alert ID, as the node found for an alert can be an
     * equivalent (de-duplicated) alert.
     */
    private static void removeChildNode(AlertNode parent, AlertNode node) {
        int idx = -1;
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (parent.getChildAt(i) == node) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            parent.remove(idx);
        }
    }

    private AlertNode findAndAddGroup(AlertNode parent, String nodeName, Alert alert) {
        AlertNode node =
                new AlertNode(
                        getRisk(alert), nodeName, alert.getAlertRef(), ALERT_CHILD_COMPARATOR);
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

    /**
     * Returns the node of the group of alerts the given alert is, or would be, added to, or {@code
     * null} if the tree has no such group.
     *
     * @param alert the alert.
     * @return the node of the group of alerts, or {@code null} if not shown in the tree.
     */
    AlertNode getGroupNode(Alert alert) {
        AlertNode node =
                new AlertNode(
                        getRisk(alert),
                        alert.getName(),
                        alert.getAlertRef(),
                        ALERT_CHILD_COMPARATOR);
        int idx = getRoot().findIndex(node);
        if (idx < 0) {
            return null;
        }
        return getRoot().getChildAt(idx);
    }

    private static int getRisk(Alert alert) {
        if (alert.getConfidence() == Alert.CONFIDENCE_FALSE_POSITIVE) {
            // Special case!
            return -1;
        }
        return alert.getRisk();
    }

    private AlertNode addLeaf(AlertNode parent, String nodeName, Alert alert) {
        int risk = getRisk(alert);

        AlertNode needle =
                new AlertNode(risk, nodeName, alert.getAlertRef(), ALERT_CHILD_COMPARATOR);
        needle.setAlert(alert);
        int idx = parent.findIndex(needle);
        if (idx < 0) {
            // Not a duplicate alert
            // The limit is applied to the main tree model only, the other models (e.g. the filtered
            // alerts tree) are a subset of it and thus are not subject to it.
            if (mainTreeModel && ext.isOverSystemicLimit(alert, parent)) {
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
            if (parent.isRoot()) {
                // The node is a group node with no alerts, just remove it
                this.removeNodeFromParent(node);
                this.nodeStructureChanged(parent);
                return;
            }
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

            int res = alertNode.getNodeName().compareTo(anotherAlertNode.getNodeName());
            if (res == 0) {
                return alertNode.getAlertRef().compareTo(anotherAlertNode.getAlertRef());
            }
            return res;
        }
    }

    private static class AlertChildNodeComparator implements Comparator<AlertNode> {

        @Override
        public int compare(AlertNode alertNode, AlertNode anotherAlertNode) {
            return alertNode.getAlert().compareTo(anotherAlertNode.getAlert());
        }
    }
}
