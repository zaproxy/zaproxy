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
package org.zaproxy.zap.extension.stdmenus;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import org.parosproxy.paros.extension.ExtensionPopupMenuItem;
import org.parosproxy.paros.model.SiteNode;
import org.parosproxy.paros.view.SiteMapPanel;
import org.zaproxy.zap.model.Target;

public class PopupContextTreeMenu extends ExtensionPopupMenuItem {
    private static final long serialVersionUID = 1L;

    private int contextId = -1;
    private List<Integer> contextIds;

    /** This method initializes */
    public PopupContextTreeMenu() {
        super();
        contextIds = new ArrayList<Integer>();
    }

    @Override
    public boolean isEnableForComponent(Component invoker) {
        contextId = -1;
        if (invoker instanceof JTree
                && SiteMapPanel.CONTEXT_TREE_COMPONENT_NAME.equals(invoker.getName())) {
            JTree contextTree = (JTree) invoker;

            if (!isEnabledForMultipleContexts()) {
                this.setEnabled(contextTree.getSelectionCount() < 2);
            }

            SiteNode node = (SiteNode) contextTree.getLastSelectedPathComponent();

            if (node == null || node.isRoot()) {
                return false;
            }
            contextId = ((Target) node.getUserObject()).getContext().getId();

            // get all selected contexts as well
            TreePath[] paths = contextTree.getSelectionPaths();
            if (paths == null || paths.length == 0) return false;

            SiteNode[] nodes =
                    Arrays.stream(paths)
                            .map(p -> (SiteNode) p.getLastPathComponent())
                            .toArray(SiteNode[]::new);

            // if only the root is selected no contexts are selected
            if (nodes.length == 1 && nodes[0].isRoot()) return false;

            Stream<Target> targets = Arrays.stream(nodes).map(n -> (Target) n.getUserObject());

            contextIds.clear();
            contextIds.addAll(
                    Arrays.asList(
                            targets.map(t -> t.getContext().getId()).toArray(Integer[]::new)));

            return isEnabledForContext(contextId);
        }
        return false;
    }

    /**
     * Override this method if the menu is not relevant all of the time
     *
     * @param contextId
     * @return
     */
    public boolean isEnabledForContext(int contextId) {
        return true;
    }

    /**
     * Returns whether or not the menu should be enabled when multiple contexts are currently
     * selected, override to change the value (by default True).
     *
     * @return Whether or not the menu should be enabled on multiple contexts
     * @since TODO Add version
     */
    public boolean isEnabledForMultipleContexts() {
        return true;
    }

    protected int getContextId() {
        return contextId;
    }

    protected List<Integer> getContextIds() {
        return contextIds;
    }

    @Override
    public boolean isSafe() {
        return true;
    }
}
