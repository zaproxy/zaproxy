/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2017 The ZAP Development Team
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

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.parosproxy.paros.core.scanner.Alert;
import org.zaproxy.zap.utils.DisplayUtils;
import org.zaproxy.zap.view.SiteMapTreeCellRenderer;

public class AlertTreeCellRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    private static final ImageIcon LEAF_ICON =
            DisplayUtils.getScaledIcon(
                    new ImageIcon(
                            SiteMapTreeCellRenderer.class.getResource(
                                    "/resource/icon/fugue/document.png")));
    private static final ImageIcon FOLDER_OPEN_ICON =
            DisplayUtils.getScaledIcon(
                    new ImageIcon(
                            SiteMapTreeCellRenderer.class.getResource(
                                    "/resource/icon/fugue/folder-horizontal-open.png")));
    private static final ImageIcon FOLDER_CLOSED_ICON =
            DisplayUtils.getScaledIcon(
                    new ImageIcon(
                            SiteMapTreeCellRenderer.class.getResource(
                                    "/resource/icon/fugue/folder-horizontal.png")));

    public AlertTreeCellRenderer() {
        this.putClientProperty("html.disable", Boolean.TRUE);
    }

    /** Sets the relevant tree icons. */
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

        if (value instanceof AlertNode) {
            AlertNode alertNode = (AlertNode) value;
            if (alertNode.isRoot()) {
                if (expanded) {
                    this.setIcon(FOLDER_OPEN_ICON);
                } else {
                    this.setIcon(FOLDER_CLOSED_ICON);
                }
            } else if (alertNode.getParent().isRoot()) {
                // Add the alert flag icon
                Alert alert = alertNode.getUserObject();
                this.setIcon(alert.getIcon());
            } else {
                this.setIcon(LEAF_ICON);
            }
        }
        return this;
    }
}
