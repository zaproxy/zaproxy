/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.junit.jupiter.api.Test;

/** Unit test for {@link JCheckBoxTree}. */
class JCheckBoxTreeUnitTest {

    @Test
    void shouldNotFailToSetAnUndefinedTreeModel() {
        // Given
        TreeModel undefinedTreeModel = null;
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When / Then
        assertDoesNotThrow(() -> checkBoxTree.setModel(undefinedTreeModel));
    }

    @Test
    void shouldFailToSetATreeModelWithRootNonDefaultMutableTreeNode() {
        // Given
        TreeModel treeModel = new DefaultTreeModel(new TreeNodeImpl());
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When / Then
        assertThrows(ClassCastException.class, () -> checkBoxTree.setModel(treeModel));
    }

    @Test
    void shouldFailToSetATreeModelWithChildNonDefaultMutableTreeNodes() {
        // Given
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.add(new MutableTreeNodeImpl());
        TreeModel treeModel = new DefaultTreeModel(rootNode);
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When / Then
        assertThrows(ClassCastException.class, () -> checkBoxTree.setModel(treeModel));
    }

    @Test
    void shouldSetATreeModelWithUndefinedRoot() {
        // Given
        TreeModel treeModel = new DefaultTreeModel(null);
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.getModel()).isEqualTo(treeModel);
    }

    @Test
    void shouldSetATreeModelWithRootDefaultMutableTreeNode() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        DefaultMutableTreeNode rootNode = treeModel.getNode("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.getModel()).isEqualTo((TreeModel) treeModel);
        assertThat(checkBoxTree.getModel().getRoot()).isEqualTo((Object) rootNode);
    }

    @Test
    void shouldSetATreeModelWithChildDefaultMutableTreeNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        DefaultMutableTreeNode rootNode = treeModel.getNode("A");
        DefaultMutableTreeNode childNode = treeModel.getNode("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.getModel()).isEqualTo((TreeModel) treeModel);
        assertThat(checkBoxTree.getModel().getRoot()).isEqualTo((Object) rootNode);
        assertThat(((DefaultMutableTreeNode) checkBoxTree.getModel().getRoot()).getChildAt(0))
                .isEqualTo((Object) childNode);
    }

    @Test
    void shouldHaveAllNodesUncheckedByDefault() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D", "A/E");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodeBPath = treeModel.createPath("A/B");
        TreePath childCChildNodeBPath = treeModel.createPath("A/B/C");
        TreePath childDChildNodeBPath = treeModel.createPath("A/B/D");
        TreePath childNodeEPath = treeModel.createPath("A/E");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeBPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeBPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeBPath)).isFalse();
        assertThat(checkBoxTree.isChecked(childCChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childCChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childCChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isChecked(childDChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childDChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childDChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeEPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeEPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeEPath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).isEmpty();
    }

    @Test
    void shouldCheckRootNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.check(rootNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isTrue();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(rootNodePath);
    }

    @Test
    void shouldCheckRootNodeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B1");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B1");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.check(rootNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(rootNodePath);
    }

    @Test
    void shouldCheckChildNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.check(childNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isTrue();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(childNodePath);
    }

    @Test
    void shouldCheckChildNodeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        TreePath childNodeCPath = treeModel.createPath("A/B/C");
        TreePath childNodeDPath = treeModel.createPath("A/B/D");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.check(childNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(childNodePath);
    }

    @Test
    void shouldCheckRootNodeSubTreeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.checkSubTree(rootNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isTrue();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(rootNodePath);
    }

    @Test
    void shouldCheckRootNodeSubTreeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        TreePath childNodeCPath = treeModel.createPath("A/B/C");
        TreePath childNodeDPath = treeModel.createPath("A/B/D");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.checkSubTree(rootNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isChecked(childNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isTrue();
        assertThat(checkBoxTree.isChecked(childNodeCPath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath)).isTrue();
        assertThat(checkBoxTree.isChecked(childNodeDPath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath)).isTrue();
        assertThat(checkBoxTree.getCheckedPaths())
                .containsExactlyInAnyOrder(
                        rootNodePath, childNodePath, childNodeCPath, childNodeDPath);
    }

    @Test
    void shouldCheckSubTreeChildNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.checkSubTree(childNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isTrue();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(childNodePath);
    }

    @Test
    void shouldCheckSubTreeChildNodeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        TreePath childNodeCPath = treeModel.createPath("A/B/C");
        TreePath childNodeDPath = treeModel.createPath("A/B/D");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.checkSubTree(childNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isTrue();
        assertThat(checkBoxTree.isChecked(childNodeCPath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath)).isTrue();
        assertThat(checkBoxTree.isChecked(childNodeDPath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath)).isTrue();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths())
                .containsExactlyInAnyOrder(childNodePath, childNodeCPath, childNodeDPath);
    }

    @Test
    void shouldUncheckRootNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.check(rootNodePath, true);
        // When
        checkBoxTree.check(rootNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).isEmpty();
    }

    @Test
    void shouldUncheckRootNodeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B1");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B1");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.check(rootNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isTrue();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(childNodePath);
    }

    @Test
    void shouldUncheckChildNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.check(childNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(rootNodePath);
    }

    @Test
    void shouldUncheckChildNodeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        TreePath childNodeCPath = treeModel.createPath("A/B/C");
        TreePath childNodeDPath = treeModel.createPath("A/B/D");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.check(childNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeCPath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath)).isTrue();
        assertThat(checkBoxTree.isChecked(childNodeDPath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath)).isTrue();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths())
                .containsExactlyInAnyOrder(rootNodePath, childNodeCPath, childNodeDPath);
    }

    @Test
    void shouldUncheckRootNodeSubTreeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.checkSubTree(rootNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).isEmpty();
    }

    @Test
    void shouldUncheckRootNodeSubTreeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        TreePath childNodeCPath = treeModel.createPath("A/B/C");
        TreePath childNodeDPath = treeModel.createPath("A/B/D");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.checkSubTree(rootNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).isEmpty();
    }

    @Test
    void shouldUncheckSubTreeChildNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.checkSubTree(childNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(rootNodePath);
    }

    @Test
    void shouldUncheckSubTreeChildNodeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        TreePath childNodeCPath = treeModel.createPath("A/B/C");
        TreePath childNodeDPath = treeModel.createPath("A/B/D");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.checkSubTree(childNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodePath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodePath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath)).isFalse();
        assertThat(checkBoxTree.isChecked(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath)).isFalse();
        assertThat(checkBoxTree.isChecked(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isSelectedFully(rootNodePath)).isFalse();
        assertThat(checkBoxTree.getCheckedPaths()).containsExactly(rootNodePath);
    }

    @Test
    void shouldHaveOnlyRootNodeExpandedByDefault() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D", "A/E");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodeBPath = treeModel.createPath("A/B");
        TreePath childCChildNodeBPath = treeModel.createPath("A/B/C");
        TreePath childDChildNodeBPath = treeModel.createPath("A/B/D");
        TreePath childNodeEPath = treeModel.createPath("A/E");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.isExpanded(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isCollapsed(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isExpanded(childNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childCChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childCChildNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childDChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childDChildNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childNodeEPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childNodeEPath)).isTrue();
    }

    @Test
    void shouldExpandAllNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D", "A/E");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodeBPath = treeModel.createPath("A/B");
        TreePath childCChildNodeBPath = treeModel.createPath("A/B/C");
        TreePath childDChildNodeBPath = treeModel.createPath("A/B/D");
        TreePath childNodeEPath = treeModel.createPath("A/E");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.expandAll();
        // Then
        assertThat(checkBoxTree.isExpanded(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isCollapsed(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isExpanded(childNodeBPath)).isTrue();
        assertThat(checkBoxTree.isCollapsed(childNodeBPath)).isFalse();
        assertThat(checkBoxTree.isExpanded(childCChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childCChildNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childDChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childDChildNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childNodeEPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childNodeEPath)).isTrue();
    }

    @Test
    void shouldCollapseAllNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B", "A/B/C", "A/B/D", "A/E");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodeBPath = treeModel.createPath("A/B");
        TreePath childCChildNodeBPath = treeModel.createPath("A/B/C");
        TreePath childDChildNodeBPath = treeModel.createPath("A/B/D");
        TreePath childNodeEPath = treeModel.createPath("A/E");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.expandAll();
        // When
        checkBoxTree.collapseAll();
        // Then
        assertThat(checkBoxTree.isExpanded(rootNodePath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(rootNodePath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childCChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childCChildNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childDChildNodeBPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childDChildNodeBPath)).isTrue();
        assertThat(checkBoxTree.isExpanded(childNodeEPath)).isFalse();
        assertThat(checkBoxTree.isCollapsed(childNodeEPath)).isTrue();
    }

    private static class TreeNodeImpl implements TreeNode {

        @Override
        public TreeNode getChildAt(int childIndex) {
            return null;
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public TreeNode getParent() {
            return null;
        }

        @Override
        public int getIndex(TreeNode node) {
            return 0;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public Enumeration<? extends TreeNode> children() {
            return null;
        }
    }

    private static class MutableTreeNodeImpl implements MutableTreeNode {

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public TreeNode getParent() {
            return null;
        }

        @Override
        public int getIndex(TreeNode node) {
            return 0;
        }

        @Override
        public int getChildCount() {
            return 0;
        }

        @Override
        public TreeNode getChildAt(int childIndex) {
            return null;
        }

        @Override
        public boolean getAllowsChildren() {
            return false;
        }

        @Override
        public Enumeration<? extends TreeNode> children() {
            return null;
        }

        @Override
        public void setUserObject(Object object) {}

        @Override
        public void setParent(MutableTreeNode newParent) {}

        @Override
        public void removeFromParent() {}

        @Override
        public void remove(MutableTreeNode node) {}

        @Override
        public void remove(int index) {}

        @Override
        public void insert(MutableTreeNode child, int index) {}
    }

    private static class TreeModelTest extends DefaultTreeModel {

        private static final long serialVersionUID = -7604697346101531566L;

        private TreeModelTest(TreeNode root) {
            super(root);
        }

        DefaultMutableTreeNode getNode(String path) {
            return (DefaultMutableTreeNode) createPath(path).getLastPathComponent();
        }

        TreePath createPath(String path) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("TreeModelTest: malformed node path, empty.");
            }
            String[] nodes = path.split("/");
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
            if (!rootNode.getUserObject().equals(nodes[0])) {
                throw new IllegalArgumentException(
                        "TreeModelTest: malformed node path, root mismatch.");
            }

            List<DefaultMutableTreeNode> nodePath = new ArrayList<>(nodes.length);
            addNodes(rootNode, nodePath, nodes, 1);
            return new TreePath(nodePath.toArray(new DefaultMutableTreeNode[nodePath.size()]));
        }

        private static void addNodes(
                DefaultMutableTreeNode currentNode,
                List<DefaultMutableTreeNode> nodes,
                String[] path,
                int level) {
            nodes.add(currentNode);

            if (level >= path.length) {
                return;
            }

            String nextNodeName = path[level];

            @SuppressWarnings("unchecked")
            Enumeration<TreeNode> children = currentNode.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
                if (childNode.getUserObject().equals(nextNodeName)) {
                    addNodes(childNode, nodes, path, level + 1);
                    return;
                }
            }
            throw new IllegalArgumentException(
                    "TreeModelTest: malformed node path, nodes mismatch.");
        }

        static TreeModelTest create(String... paths) {
            if (paths == null || paths.length == 0) {
                return new TreeModelTest(new DefaultMutableTreeNode());
            }
            return new TreeModelTest(buildNodeTree(paths));
        }

        private static DefaultMutableTreeNode buildNodeTree(String[] paths) {
            DefaultMutableTreeNode rootNode = null;
            for (String nodePath : paths) {
                String[] nodes = nodePath.split("/");
                if (rootNode == null) {
                    rootNode = new DefaultMutableTreeNode(nodes[0]);
                } else if (!rootNode.getUserObject().equals(nodes[0])) {
                    throw new IllegalArgumentException(
                            "TreeModelTest: must have only one root node: "
                                    + rootNode.getUserObject()
                                    + " != "
                                    + nodes[0]);
                }

                addNodes(rootNode, nodes, 1);
            }
            return rootNode;
        }

        private static void addNodes(DefaultMutableTreeNode node, String[] nodes, int level) {
            if (level >= nodes.length) {
                return;
            }

            DefaultMutableTreeNode nextNode = null;
            String nextNodeName = nodes[level];

            @SuppressWarnings("unchecked")
            Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
                if (childNode.getUserObject().equals(nextNodeName)) {
                    nextNode = childNode;
                    break;
                }
            }

            if (nextNode == null) {
                nextNode = new DefaultMutableTreeNode(nextNodeName);
                node.add(nextNode);
            }
            addNodes(nextNode, nodes, level + 1);
        }
    }
}
