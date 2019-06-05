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
package org.zaproxy.zap.extension.help;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.help.MergeHelpUtilities;
import javax.help.NavigatorView;
import javax.help.SortMerge;
import javax.help.TreeItem;
import javax.help.UniteAppendMerge;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * <strong>NOTE:</strong> The name (and package) of the class must not be changed lightly! It will
 * break help's TOC merging at runtime. The name and package is hard coded in helpset files and is
 * also referenced in others for documentation purposes. (END NOTE)
 *
 * <p>An {@code UniteAppendMerge} that takes into account the "tocid" attribute of the "tocitem"
 * elements to do the merging. The "tocid" attribute is used to facilitate the merging of the TOC
 * with internationalised helpsets. The node names and targets do not provide enough information to
 * do a safe merging. The name might not be the same (when it is translated) and the target is not
 * present in all nodes. In those cases a "tocid" attribute is set to unambiguously identify those
 * nodes.
 *
 * <p>The merge depends on the information provided by the "tocitem" elements and if they use or not
 * the "tocid" attribute.
 *
 * <p>First the nodes are compared to check if they have the same "tocid" and merged if they have.
 * <br>
 * Otherwise and for backward compatibility with helpsets that still do not use the attribute
 * "tocid" a forced merge is performed if some predefined requirements are met. The requirements are
 * as follow:
 *
 * <ol>
 *   <li>The master node must have an attribute "tocid";
 *   <li>The master node "tocid" must be present in the map of forced merges ({@code
 *       TOC_IDS_FORCE_MERGE_MAP});
 *   <li>The slave node must have the same name as the one defined in the value ({@code
 *       ForceMergeRequirement}) of the map of forced merges;
 *   <li>The master node level must be the same as the one defined in the value of the map of forced
 *       merges (the level is used to prevent matching other nodes with the same name in the tree).
 * </ol>
 *
 * <p>If none of the aforementioned merges are performed the actual merging will be done as defined
 * by {@code UniteAppendMerge}.
 *
 * @see UniteAppendMerge
 * @see ZapTocItem
 * @see ZapTocView
 * @see #TOC_IDS_FORCE_MERGE_MAP
 * @see ZapTocMerger.ForceMergeRequirement
 */
// Note: This class contains copied (verbatim) code from the base class UniteAppendMerge.
public class ZapTocMerger extends UniteAppendMerge {

    private static final String DEFAULT_MERGE_TYPE = ZapTocMerger.class.getCanonicalName();

    private static final String ADDONS_TOC_ID = "addons";

    /**
     * A map containing the requirements to do forced merging.
     *
     * <p>The map key corresponds to the attribute "tocid" of the "tocitem" elements as defined in
     * the toc.xml file. The value has the requirements that should be met to actually do the
     * merging.
     */
    public static final Map<String, ForceMergeRequirement> TOC_IDS_FORCE_MERGE_MAP;

    static {
        Map<String, ForceMergeRequirement> tempMap = new HashMap<>();
        // Note: The attribute "tocid" should match the ones defined in the toc.xml file.
        // Note 2: the TOC tree node names are hard coded because the "older" add-ons use the same
        // (hard coded) names.
        tempMap.put("toplevelitem", new ForceMergeRequirement(1, "ZAP User Guide"));
        tempMap.put(ADDONS_TOC_ID, new ForceMergeRequirement(2, "Add Ons"));
        TOC_IDS_FORCE_MERGE_MAP = Collections.unmodifiableMap(tempMap);
    }

    public ZapTocMerger(NavigatorView master, NavigatorView slave) {
        super(master, slave);
    }

    /**
     * Processes unite-append merge
     *
     * @param node The master node
     * @return Merged master node
     */
    // Note: the implementation and JavaDoc has been copied (verbatim) from the base method to call
    // the method
    // ZapTocMerger#mergeNodes(TreeNode, TreeNode) instead of UniteAppendMerge#mergeNodes(TreeNode,
    // TreeNode).
    @Override
    public TreeNode processMerge(TreeNode node) {

        DefaultMutableTreeNode masterNode = (DefaultMutableTreeNode) node;

        // if master and slave are the same object return the
        // masterNode
        if (masterNode.equals(slaveTopNode)) {
            return masterNode;
        }

        // If there are not children in slaveTopNode return the
        // masterNode
        if (slaveTopNode.getChildCount() == 0) {
            return masterNode;
        }

        mergeNodes(masterNode, slaveTopNode);
        return masterNode;
    }

    /**
     * Merge Nodes. Merge two nodes according to the merging rules of the masterNode. Each Subclass
     * should override this implementation.
     *
     * @param master The master node to merge with
     * @param slave The node to merge into the master
     */
    // Note: the implementation and JavaDoc has been copied (verbatim) from
    // UniteAppendMerge#mergeNodes(TreeNode, TreeNode)
    // except for the call to doCustomMerge(DefaultMutableTreeNode, DefaultMutableTreeNode) and the
    // calls to
    // MergeHelpUtilities.mergeNode* which is set, using DEFAULT_MERGE_TYPE, to merge with this
    // class instead.
    public static void mergeNodes(TreeNode master, TreeNode slave) {
        DefaultMutableTreeNode masterNode = (DefaultMutableTreeNode) master;
        DefaultMutableTreeNode slaveNode = (DefaultMutableTreeNode) slave;

        int masterCnt = masterNode.getChildCount();

        // loop thru the slaves
        while (slaveNode.getChildCount() > 0) {
            DefaultMutableTreeNode slaveNodeChild =
                    (DefaultMutableTreeNode) slaveNode.getFirstChild();

            // loop thru the master children
            for (int m = 0; m < masterCnt; m++) {
                DefaultMutableTreeNode masterAtM =
                        (DefaultMutableTreeNode) masterNode.getChildAt(m);

                if (doCustomMerge(slaveNodeChild, masterAtM)) {
                    slaveNodeChild = null;
                    break;
                }

                // see if the names are the same
                if (MergeHelpUtilities.compareNames(masterAtM, slaveNodeChild) == 0) {
                    if (MergeHelpUtilities.haveEqualID(masterAtM, slaveNodeChild)) {
                        // ID and name the same merge the slave node in
                        MergeHelpUtilities.mergeNodes(
                                DEFAULT_MERGE_TYPE, masterAtM, slaveNodeChild);
                        // Need to remove the slaveNodeChild from the list
                        slaveNodeChild.removeFromParent();
                        slaveNodeChild = null;
                        break;
                    }
                    // Names are the same but the ID are not
                    // Mark the nodes and add the slaveChild
                    MergeHelpUtilities.markNodes(masterAtM, slaveNodeChild);
                    masterNode.add(slaveNodeChild);
                    MergeHelpUtilities.mergeNodeChildren(DEFAULT_MERGE_TYPE, slaveNodeChild);
                    slaveNodeChild = null;
                    break;
                }
            }
            if (slaveNodeChild != null) {
                masterNode.add(slaveNodeChild);
                MergeHelpUtilities.mergeNodeChildren(DEFAULT_MERGE_TYPE, slaveNodeChild);
            }
        }
        // There are no more children.
        // Remove slaveNode from it's parent
        slaveNode.removeFromParent();
        slaveNode = null;
    }

    private static boolean doCustomMerge(
            DefaultMutableTreeNode slaveNodeChild, DefaultMutableTreeNode masterAtM) {
        if (isSameTOCID(masterAtM, slaveNodeChild) || isForceMerge(masterAtM, slaveNodeChild)) {
            MergeHelpUtilities.mergeNodes(DEFAULT_MERGE_TYPE, masterAtM, slaveNodeChild);
            slaveNodeChild.removeFromParent();
            if (ADDONS_TOC_ID.equals(getTOCID(masterAtM))) {
                SortMerge.sortNode(masterAtM, MergeHelpUtilities.getLocale(masterAtM));
            }
            return true;
        }
        return false;
    }

    private static boolean isSameTOCID(
            DefaultMutableTreeNode masterAtM, DefaultMutableTreeNode slaveNodeChild) {
        String slaveTocId = getTOCID(slaveNodeChild);
        if (slaveTocId == null) {
            return false;
        }
        return slaveTocId.equals(getTOCID(masterAtM));
    }

    private static String getTOCID(DefaultMutableTreeNode node) {
        TreeItem treeItem = (TreeItem) node.getUserObject();
        if (treeItem != null && (treeItem instanceof ZapTocItem)) {
            return ((ZapTocItem) treeItem).getTocId();
        }
        return null;
    }

    private static boolean isForceMerge(
            DefaultMutableTreeNode masterAtM, DefaultMutableTreeNode slaveNodeChild) {
        TreeItem slaveNodeChildTreeItem = (TreeItem) slaveNodeChild.getUserObject();
        String slaveName = slaveNodeChildTreeItem.getName();
        if (slaveName == null) {
            return false;
        }

        String tocId = getTOCID(masterAtM);
        if (tocId != null) {
            ForceMergeRequirement forceMergeRequirement = TOC_IDS_FORCE_MERGE_MAP.get(tocId);
            if (forceMergeRequirement != null
                    && forceMergeRequirement.isSameMasterLevel(masterAtM.getLevel())
                    && forceMergeRequirement.isSameSlaveName(slaveName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Merge Node Children. Merge the children of a node according to the merging rules of the
     * parent. Each subclass must implement this method
     *
     * @param node The parent node from which the children are merged
     */
    // Note: the implementation and JavaDoc has been copied (verbatim) from
    // UniteAppendMerge#mergeNodeChildren(TreeNode) except
    // for the call to MergeHelpUtilities.mergeNodeChildren(String, child) which is set, using
    // DEFAULT_MERGE_TYPE, to merge with
    // this class instead.
    public static void mergeNodeChildren(TreeNode node) {
        DefaultMutableTreeNode masterNode = (DefaultMutableTreeNode) node;

        // The rules are there are no rules. Nothing else needs to be done
        // except for merging through the children
        for (int i = 0; i < masterNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) masterNode.getChildAt(i);

            if (!child.isLeaf()) {
                MergeHelpUtilities.mergeNodeChildren(DEFAULT_MERGE_TYPE, child);
            }
        }
    }

    /**
     * The {@code ForceMergeRequirement} class contains the requirements to do a forced merging.
     *
     * @see ForceMergeRequirement#ForceMergeRequirement(int, String)
     */
    public static final class ForceMergeRequirement {

        private final int masterNodeLevel;
        private final String slaveNodeName;

        /**
         * Creates a {@code ForceMergeRequirement} instance.
         *
         * @param masterNodeLevel the level of the master node in the TOC tree
         * @param slaveNodeName the name of the slave node
         * @see DefaultMutableTreeNode#getLevel()
         */
        public ForceMergeRequirement(int masterNodeLevel, String slaveNodeName) {
            if (masterNodeLevel < 0) {
                throw new IllegalArgumentException(
                        "Parameter masterNodeLevel must not be negative.");
            }
            if (slaveNodeName == null || slaveNodeName.isEmpty()) {
                throw new IllegalArgumentException("Parameter slaveNodeName must not be null.");
            }
            this.masterNodeLevel = masterNodeLevel;
            this.slaveNodeName = slaveNodeName;
        }

        public boolean isSameMasterLevel(int level) {
            return (masterNodeLevel == level);
        }

        public boolean isSameSlaveName(String name) {
            return slaveNodeName.equals(name);
        }
    }
}
