/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2018 The ZAP Development Team
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
package org.zaproxy.zap.view.panelsearch.items;

import java.util.ArrayList;
import javax.swing.JTree;

public final class TreeUtils {

    public static ArrayList<TreeNodeElement> getTreeNodeElement(JTree component) {
        Object rootNode = component.getModel().getRoot();
        return getTreeNodeElementRecursive(component, rootNode);
    }

    private static ArrayList<TreeNodeElement> getTreeNodeElementRecursive(JTree tree, Object node) {
        ArrayList<TreeNodeElement> elements = new ArrayList<>();
        elements.add(new TreeNodeElement(node, tree));

        for (Object childNodes : getChildNodes(tree, node)) {
            elements.addAll(getTreeNodeElementRecursive(tree, childNodes));
        }
        return elements;
    }

    private static ArrayList<Object> getChildNodes(JTree tree, Object node) {
        ArrayList<Object> nodes = new ArrayList<>();
        int childCount = tree.getModel().getChildCount(node);

        for (int i = 0; i < childCount; i++) {
            Object child = tree.getModel().getChild(node, i);
            nodes.add(child);
        }

        return nodes;
    }
}
