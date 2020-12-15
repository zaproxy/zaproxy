/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2016 The ZAP Development Team
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

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;

/**
 * A {@code JPanel} that allows to display and select technologies through a check box tree.
 *
 * @see Tech
 * @see TechSet
 * @see JCheckBoxTree
 * @since 2.5.0
 */
public class TechnologyTreePanel extends JPanel {

    private static final long serialVersionUID = 5514692105773714202L;

    private final JCheckBoxTree techTree;
    private final HashMap<Tech, DefaultMutableTreeNode> techToNodeMap;

    private final DefaultMutableTreeNode root;

    public TechnologyTreePanel(String nameRootNode) {
        setLayout(new BorderLayout());

        techToNodeMap = new HashMap<>();
        techTree =
                new JCheckBoxTree() {

                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void setExpandedState(TreePath path, boolean state) {
                        // Ignore all collapse requests; collapse events will not be fired
                        if (state) {
                            super.setExpandedState(path, state);
                        }
                    }
                };

        root = new DefaultMutableTreeNode(nameRootNode);
        refresh();

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(techTree);
        scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Refreshes the tree with the latest technologies.
     *
     * @since 2.10.0
     * @see Tech#getAll()
     */
    public void refresh() {
        root.removeAllChildren();
        techToNodeMap.clear();

        DefaultMutableTreeNode parent;
        DefaultMutableTreeNode node;
        for (Tech tech : Tech.getAll()) {
            if (tech.getParent() != null) {
                parent = techToNodeMap.get(tech.getParent());
            } else {
                parent = null;
            }
            if (parent == null) {
                parent = root;
            }
            node = new DefaultMutableTreeNode(tech.getUiName());
            parent.add(node);
            techToNodeMap.put(tech, node);
        }

        techTree.setModel(new DefaultTreeModel(root));
        techTree.expandAll();
        techTree.setCheckBoxEnabled(new TreePath(root), false);
        reset();
    }

    /**
     * Sets the technologies that should be selected, if included, and not if excluded.
     *
     * @param techSet the technologies that will be selected, if included, and not if excluded.
     * @see TechSet#includes(Tech)
     */
    public void setTechSet(TechSet techSet) {
        Set<Tech> includedTech = techSet.getIncludeTech();
        Iterator<Entry<Tech, DefaultMutableTreeNode>> iter = techToNodeMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Tech, DefaultMutableTreeNode> node = iter.next();
            TreePath tp = this.getPath(node.getValue());
            Tech tech = node.getKey();
            if (Tech.getTopLevel().contains(tech)) {
                techTree.check(tp, containsAnyOfTopLevelTech(includedTech, tech));
            } else {
                techTree.check(tp, techSet.includes(tech));
            }
        }
    }

    /**
     * Gets a {@code TechSet} with the technologies included, if selected, and excluded if not.
     *
     * @return a TechSet with the technologies included and excluded
     * @see TechSet#include(Tech)
     * @see TechSet#exclude(Tech)
     */
    public TechSet getTechSet() {
        TechSet techSet = new TechSet();
        Iterator<Entry<Tech, DefaultMutableTreeNode>> iter = techToNodeMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Tech, DefaultMutableTreeNode> node = iter.next();
            TreePath tp = this.getPath(node.getValue());
            Tech tech = node.getKey();
            if (techTree.isSelectedFully(tp)) {
                techSet.include(tech);
            } else {
                techSet.exclude(tech);
            }
        }
        return techSet;
    }

    /** Resets the selection the panel by selecting all technologies. */
    public void reset() {
        techTree.checkSubTree(techTree.getPathForRow(0), true);
    }

    private TreePath getPath(TreeNode node) {
        List<TreeNode> list = new ArrayList<>();

        // Add all nodes to list
        while (node != null) {
            list.add(node);
            node = node.getParent();
        }
        Collections.reverse(list);

        // Convert array of nodes to TreePath
        return new TreePath(list.toArray());
    }

    private static boolean containsAnyOfTopLevelTech(Set<Tech> techSet, Tech topLevelTech) {
        for (Tech tech : techSet) {
            if (topLevelTech.equals(tech.getParent())) {
                return true;
            }
        }
        return false;
    }
}
