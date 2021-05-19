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
package org.zaproxy.zap.model;

import java.util.ArrayList;
import java.util.List;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.SiteNode;

/**
 * The target of a scan
 *
 * @author simon
 * @since 2.4.0
 */
public class Target {

    private List<StructuralNode> startNodes;
    private Context context;
    private boolean inScopeOnly = false;
    private int maxChildren = -1;
    private int maxDepth = -1;
    private boolean recurse = false;

    /**
     * Constructs a {@code Target} with no start node.
     *
     * @since 2.8.0
     * @see #setStartNode(StructuralNode)
     */
    public Target() {
        super();
    }

    public Target(boolean inScopeOnly) {
        super();
        this.inScopeOnly = inScopeOnly;
    }

    public Target(StructuralNode startNode) {
        super();
        this.startNodes = new ArrayList<>();
        this.startNodes.add(startNode);
    }

    public Target(List<StructuralNode> startNodes) {
        super();
        this.startNodes = startNodes;
    }

    public Target(SiteNode startNode) {
        super();
        this.setStartNode(startNode);
    }

    public Target(SiteNode startNode, boolean recurse) {
        this(startNode);
        this.setRecurse(recurse);
    }

    public Target(Context context) {
        super();
        this.context = context;
    }

    /**
     * Constructs a {@code Target} with the given data.
     *
     * <p>One or both startNode and context should be specified. If both a used then the startNode
     * should belong to the context. Note that nodes can belong to multiple contexts.
     *
     * @param startNode the starting node
     * @param context the context
     * @param inScopeOnly {@code true} to only scan nodes that are in scope (only relevant if
     *     context not specified), {@code false} otherwise
     * @param maxChildren maximum number of child nodes to scan
     * @param maxDepth maximum depth to scan
     */
    public Target(
            SiteNode startNode,
            Context context,
            boolean inScopeOnly,
            int maxChildren,
            int maxDepth) {
        this(startNode);
        this.context = context;
        this.inScopeOnly = inScopeOnly;
        this.maxChildren = maxChildren;
        this.maxDepth = maxDepth;
    }

    public Target(SiteNode startNode, Context context, boolean inScopeOnly, boolean recurse) {
        this(startNode);
        this.context = context;
        this.inScopeOnly = inScopeOnly;
        this.recurse = recurse;
    }

    public boolean isValid() {
        return (this.startNodes != null && this.startNodes.size() > 0)
                || this.context != null
                || this.inScopeOnly;
    }

    public SiteNode getStartNode() {
        if (startNodes != null
                && startNodes.size() > 0
                && startNodes.get(0) instanceof StructuralSiteNode) {
            return ((StructuralSiteNode) startNodes.get(0)).getSiteNode();
        }
        return null;
    }

    public Context getContext() {
        return context;
    }

    public boolean isInScopeOnly() {
        return inScopeOnly;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Sets the given node as the start node.
     *
     * <p>Start nodes previously set are discarded.
     *
     * <p><strong>Note:</strong> The call to this method as no effect if the node is {@code null}.
     *
     * @param startNode the start node.
     */
    public void setStartNode(SiteNode startNode) {
        if (startNode != null) {
            setStartNode(new StructuralSiteNode(startNode));
        }
    }

    /**
     * Sets the given node as the start node.
     *
     * <p>Start nodes previously set are discarded.
     *
     * @param startNode the start node.
     * @since 2.8.0
     */
    public void setStartNode(StructuralNode startNode) {
        this.startNodes = new ArrayList<>();
        this.startNodes.add(startNode);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<StructuralNode> getStartNodes() {
        return startNodes;
    }

    public void setInScopeOnly(boolean inScopeOnly) {
        this.inScopeOnly = inScopeOnly;
    }

    public void setMaxChildren(int maxChildren) {
        this.maxChildren = maxChildren;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public boolean isRecurse() {
        return recurse;
    }

    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

    public String getDisplayName() {
        if (this.getStartNode() == null) {
            if (context != null) {
                return Constant.messages.getString("context.prefixName", context.getName());
            } else if (this.inScopeOnly) {
                return Constant.messages.getString("target.allInScope");
            } else {
                return Constant.messages.getString("target.empty");
            }
        } else {
            String name = this.getStartNode().getHierarchicNodeName(false);
            if (name.length() < 30) {
                return name;
            }

            // Just use the first and last 14 chrs to prevent huge urls messing up the display
            return name.substring(0, 14) + ".." + name.substring(name.length() - 15, name.length());
        }
    }
}
