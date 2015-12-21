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

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.junit.Test;

/**
 * Unit test for {@link JCheckBoxTree}.
 */
public class JCheckBoxTreeUnitTest {

    @Test(expected = NullPointerException.class)
    public void shouldFailToSetAnUndefinedTreeModel() {
        // Given
        TreeModel undefinedTreeModel = null;
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(undefinedTreeModel);
        // Then = NullPointerException
    }

    @Test(expected = ClassCastException.class)
    public void shouldFailToSetATreeModelWithRootNonDefaultMutableTreeNode() {
        // Given
        TreeModel treeModel = new DefaultTreeModel(new TreeNodeImpl());
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then = ClassCastException
    }

    @Test(expected = ClassCastException.class)
    public void shouldFailToSetATreeModelWithChildNonDefaultMutableTreeNodes() {
        // Given
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        rootNode.add(new MutableTreeNodeImpl());
        TreeModel treeModel = new DefaultTreeModel(rootNode);
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then = ClassCastException
    }

    @Test
    public void shouldSetATreeModelWithUndefinedRoot() {
        // Given
        TreeModel treeModel = new DefaultTreeModel(null);
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.getModel(), is(equalTo(treeModel)));
    }

    @Test
    public void shouldSetATreeModelWithRootDefaultMutableTreeNode() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        DefaultMutableTreeNode rootNode = treeModel.getNode("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.getModel(), is(equalTo((TreeModel) treeModel)));
        assertThat(checkBoxTree.getModel().getRoot(), is(equalTo((Object) rootNode)));
    }

    @Test
    public void shouldSetATreeModelWithChildDefaultMutableTreeNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        DefaultMutableTreeNode rootNode = treeModel.getNode("A");
        DefaultMutableTreeNode childNode = treeModel.getNode("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        // When
        checkBoxTree.setModel(treeModel);
        // Then
        assertThat(checkBoxTree.getModel(), is(equalTo((TreeModel) treeModel)));
        assertThat(checkBoxTree.getModel().getRoot(), is(equalTo((Object) rootNode)));
        assertThat(((DefaultMutableTreeNode) checkBoxTree.getModel().getRoot()).getChildAt(0), is(equalTo((Object) childNode)));
    }

    @Test
    public void shouldHaveAllNodesUncheckedByDefault() {
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
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childCChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childCChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childCChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childDChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childDChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childDChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeEPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeEPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeEPath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(emptyArray()));
    }

    @Test
    public void shouldCheckRootNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.check(rootNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(rootNodePath)));
    }

    @Test
    public void shouldCheckRootNodeWithChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B1");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B1");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.check(rootNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(rootNodePath)));
    }

    @Test
    public void shouldCheckChildNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.check(childNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(childNodePath)));
    }

    @Test
    public void shouldCheckChildNodeWithChildNodes() {
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
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(childNodePath)));
    }

    @Test
    public void shouldCheckRootNodeSubTreeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.checkSubTree(rootNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(rootNodePath)));
    }

    @Test
    public void shouldCheckRootNodeSubTreeWithChildNodes() {
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
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(childNodeCPath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(childNodeDPath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath), is(equalTo(true)));
        assertThat(
                checkBoxTree.getCheckedPaths(),
                is(arrayContainingInAnyOrder(rootNodePath, childNodePath, childNodeCPath, childNodeDPath)));
    }

    @Test
    public void shouldCheckSubTreeChildNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A", "A/B");
        TreePath rootNodePath = treeModel.createPath("A");
        TreePath childNodePath = treeModel.createPath("A/B");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        // When
        checkBoxTree.checkSubTree(childNodePath, true);
        // Then
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(childNodePath)));
    }

    @Test
    public void shouldCheckSubTreeChildNodeWithChildNodes() {
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
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(childNodeCPath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(childNodeDPath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(
                checkBoxTree.getCheckedPaths(),
                is(arrayContainingInAnyOrder(childNodePath, childNodeCPath, childNodeDPath)));
    }

    @Test
    public void shouldUncheckRootNodeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.check(rootNodePath, true);
        // When
        checkBoxTree.check(rootNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(emptyArray()));
    }

    @Test
    public void shouldUncheckRootNodeWithChildNodes() {
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
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(childNodePath)));
    }

    @Test
    public void shouldUncheckChildNodeWithoutChildNodes() {
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
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(rootNodePath)));
    }

    @Test
    public void shouldUncheckChildNodeWithChildNodes() {
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
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeCPath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(childNodeDPath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath), is(equalTo(true)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContainingInAnyOrder(rootNodePath, childNodeCPath, childNodeDPath)));
    }

    @Test
    public void shouldUncheckRootNodeSubTreeWithoutChildNodes() {
        // Given
        TreeModelTest treeModel = TreeModelTest.create("A");
        TreePath rootNodePath = treeModel.createPath("A");
        JCheckBoxTree checkBoxTree = new JCheckBoxTree();
        checkBoxTree.setModel(treeModel);
        checkBoxTree.checkSubTree(rootNodePath, true);
        // When
        checkBoxTree.checkSubTree(rootNodePath, false);
        // Then
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(emptyArray()));
    }

    @Test
    public void shouldUncheckRootNodeSubTreeWithChildNodes() {
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
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(emptyArray()));
    }

    @Test
    public void shouldUncheckSubTreeChildNodeWithoutChildNodes() {
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
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(rootNodePath)));
    }

    @Test
    public void shouldUncheckSubTreeChildNodeWithChildNodes() {
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
        assertThat(checkBoxTree.isChecked(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeCPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedPartially(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isSelectedFully(childNodeDPath), is(equalTo(false)));
        assertThat(checkBoxTree.isChecked(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedPartially(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isSelectedFully(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.getCheckedPaths(), is(arrayContaining(rootNodePath)));
    }

    @Test
    public void shouldHaveOnlyRootNodeExpandedByDefault() {
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
        assertThat(checkBoxTree.isExpanded(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isCollapsed(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isExpanded(childNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childCChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childCChildNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childDChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childDChildNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childNodeEPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childNodeEPath), is(equalTo(true)));
    }

    @Test
    public void shouldExpandAllNodes() {
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
        assertThat(checkBoxTree.isExpanded(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isCollapsed(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isExpanded(childNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isCollapsed(childNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isExpanded(childCChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childCChildNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childDChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childDChildNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childNodeEPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childNodeEPath), is(equalTo(true)));
    }

    @Test
    public void shouldCollapseAllNodes() {
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
        assertThat(checkBoxTree.isExpanded(rootNodePath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(rootNodePath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childCChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childCChildNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childDChildNodeBPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childDChildNodeBPath), is(equalTo(true)));
        assertThat(checkBoxTree.isExpanded(childNodeEPath), is(equalTo(false)));
        assertThat(checkBoxTree.isCollapsed(childNodeEPath), is(equalTo(true)));
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
        public Enumeration<?> children() {
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
        public Enumeration<?> children() {
            return null;
        }

        @Override
        public void setUserObject(Object object) {
        }

        @Override
        public void setParent(MutableTreeNode newParent) {
        }

        @Override
        public void removeFromParent() {
        }

        @Override
        public void remove(MutableTreeNode node) {
        }

        @Override
        public void remove(int index) {
        }

        @Override
        public void insert(MutableTreeNode child, int index) {
        }
    }

    private static class TreeModelTest extends DefaultTreeModel {

        private static final long serialVersionUID = -7604697346101531566L;

        private TreeModelTest(TreeNode root) {
            super(root);
        }

        public DefaultMutableTreeNode getNode(String path) {
            return (DefaultMutableTreeNode) createPath(path).getLastPathComponent();
        }

        public TreePath createPath(String path) {
            if (path == null || path.isEmpty()) {
                throw new IllegalArgumentException("TreeModelTest: malformed node path, empty.");
            }
            String[] nodes = path.split("/");
            DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
            if (!rootNode.getUserObject().equals(nodes[0])) {
                throw new IllegalArgumentException("TreeModelTest: malformed node path, root mismatch.");
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
            Enumeration<DefaultMutableTreeNode> children = currentNode.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = children.nextElement();
                if (childNode.getUserObject().equals(nextNodeName)) {
                    addNodes(childNode, nodes, path, level + 1);
                    return;
                }
            }
            throw new IllegalArgumentException("TreeModelTest: malformed node path, nodes mismatch.");
        }

        public static TreeModelTest create(String... paths) {
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
                            "TreeModelTest: must have only one root node: " + rootNode.getUserObject() + " != " + nodes[0]);
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
            Enumeration<DefaultMutableTreeNode> children = node.children();
            while (children.hasMoreElements()) {
                DefaultMutableTreeNode childNode = children.nextElement();
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
