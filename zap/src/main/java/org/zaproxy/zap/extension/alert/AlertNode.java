/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2011 The ZAP Development Team
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

import java.util.Collections;
import java.util.Comparator;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.parosproxy.paros.core.scanner.Alert;

@SuppressWarnings("serial")
public class AlertNode extends DefaultMutableTreeNode {
    private static final long serialVersionUID = 1L;

    private final Comparator<TreeNode> childComparator;
    private String nodeName = null;
    private int risk = -1;
    private Alert alert;

    public AlertNode(int risk, String nodeName) {
        this(risk, nodeName, null);
    }

    public AlertNode(int risk, String nodeName, Comparator<AlertNode> childComparator) {
        super();
        this.nodeName = nodeName;
        this.setRisk(risk);
        this.childComparator = new AlertNodeComparatorWrapper(childComparator);
    }

    @Override
    public void setUserObject(Object userObject) {
        if (!(userObject instanceof Alert)) {
            throw new IllegalArgumentException("Parameter userObject must be an Alert.");
        }
        this.alert = (Alert) userObject;
    }

    @Override
    public Alert getUserObject() {
        return alert;
    }

    @Override
    public AlertNode getChildAt(int index) {
        return (AlertNode) super.getChildAt(index);
    }

    @Override
    public AlertNode getParent() {
        return (AlertNode) super.getParent();
    }

    @Override
    public void add(MutableTreeNode newChild) {
        if (!(newChild instanceof AlertNode)) {
            throw new IllegalArgumentException("Parameter newChild must be an AlertNode.");
        }
        super.add(newChild);
    }

    @Override
    public void insert(MutableTreeNode newChild, int childIndex) {
        if (!(newChild instanceof AlertNode)) {
            throw new IllegalArgumentException("Parameter newChild must be an AlertNode.");
        }
        super.insert(newChild, childIndex);
    }

    @Override
    public int getIndex(TreeNode aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }
        if (!(aChild instanceof AlertNode)) {
            return -1;
        }

        if (!isNodeChild(aChild)) {
            return -1;
        }

        int idx = findIndex((AlertNode) aChild);
        if (idx < 0) {
            return -1;
        }
        return idx;
    }

    public int findIndex(AlertNode aChild) {
        if (children == null) {
            return -1;
        }
        // Safe, only child AlertNode are allowed to be added/inserted to AlertNode;
        // Variable idx is only used to add the @SuppressWarnings annotation locally (instead of the
        // whole method).
        @SuppressWarnings("unchecked")
        int idx = Collections.binarySearch(children, aChild, childComparator);
        return idx;
    }

    @Override
    public String toString() {
        if (this.getChildCount() > 1) {
            return nodeName + " (" + this.getChildCount() + ")";
        }
        return nodeName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }

    public int getRisk() {
        return risk;
    }

    private static class AlertNodeComparatorWrapper implements Comparator<TreeNode> {

        private final Comparator<AlertNode> comparator;

        public AlertNodeComparatorWrapper(Comparator<AlertNode> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(TreeNode o1, TreeNode o2) {
            return comparator.compare((AlertNode) o1, (AlertNode) o2);
        }
    }
}
