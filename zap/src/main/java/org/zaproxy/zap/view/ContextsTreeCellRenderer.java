/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.view;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.model.Target;
import org.zaproxy.zap.utils.DisplayUtils;

/** Custom renderer for contexts tree */
public class ContextsTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final ImageIcon ROOT_ICON =
            new ImageIcon(
                    ContextsTreeCellRenderer.class.getResource(
                            "/resource/icon/fugue/applications-blue.png"));
    private static final ImageIcon CONTEXT_IN_SCOPE_ICON =
            new ImageIcon(
                    ContextsTreeCellRenderer.class.getResource(
                            "/resource/icon/fugue/application-blue-target.png"));
    private static final ImageIcon CONTEXT_ICON =
            new ImageIcon(
                    ContextsTreeCellRenderer.class.getResource(
                            "/resource/icon/fugue/application-blue.png"));
    private static final ImageIcon ALL_IN_SCOPE_ICON =
            new ImageIcon(
                    ContextsTreeCellRenderer.class.getResource("/resource/icon/fugue/target.png"));

    private static final long serialVersionUID = -4278691012245035225L;

    public ContextsTreeCellRenderer() {}

    /** Sets custom tree node logos. */
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        SiteNode node = null;
        Target target = null;
        if (value instanceof SiteNode) {
            node = (SiteNode) value;
            if (node.getUserObject() instanceof Target) {
                target = (Target) node.getUserObject();
            }
        }

        if (node != null) {
            if (node.isRoot()) {
                setIcon(DisplayUtils.getScaledIcon(ROOT_ICON));
            } else if (target != null) {
                if (target.getContext() != null) {
                    if (target.getContext().isInScope()) {
                        setIcon(DisplayUtils.getScaledIcon(CONTEXT_IN_SCOPE_ICON));
                    } else {
                        setIcon(DisplayUtils.getScaledIcon(CONTEXT_ICON));
                    }
                } else if (target.isInScopeOnly()) {
                    setIcon(DisplayUtils.getScaledIcon(ALL_IN_SCOPE_ICON));
                }
            }
        }

        return this;
    }
}
