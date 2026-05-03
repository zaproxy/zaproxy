/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2026 The ZAP Development Team
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
package org.zaproxy.zap.extension.siteinfo;

import java.util.Enumeration;
import javax.swing.tree.TreeNode;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;

/**
 * Walks a {@link SiteNode} subtree and gathers descriptive statistics: number of nodes, the most
 * recently added {@link HistoryReference} timestamp, and the {@link HistoryReference} type that
 * produced it.
 *
 * <p>The calculator is split out from the popup menu so it can be exercised by a unit test without
 * a Swing or database fixture.
 */
public final class SiteInfoCalculator {

    /** Result snapshot for a subtree walk. */
    public static final class Stats {
        private final int nodeCount;
        private final long newestHistoryRefTimeMillis;
        private final int newestHistoryRefType;

        public Stats(int nodeCount, long newestHistoryRefTimeMillis, int newestHistoryRefType) {
            this.nodeCount = nodeCount;
            this.newestHistoryRefTimeMillis = newestHistoryRefTimeMillis;
            this.newestHistoryRefType = newestHistoryRefType;
        }

        /** Total number of {@link SiteNode} nodes in the subtree, including the root. */
        public int getNodeCount() {
            return nodeCount;
        }

        /**
         * Time the newest {@link HistoryReference} in the subtree was sent, in milliseconds since
         * epoch. Returns {@code 0} when the subtree has no history references at all.
         */
        public long getNewestHistoryRefTimeMillis() {
            return newestHistoryRefTimeMillis;
        }

        /**
         * Returns {@code true} if the walk found at least one {@link HistoryReference} in the
         * subtree.
         */
        public boolean hasNewestHistoryRef() {
            return newestHistoryRefTimeMillis > 0;
        }

        /**
         * History type ({@code HistoryReference.TYPE_*}) of the newest {@link HistoryReference} in
         * the subtree. Only meaningful when {@link #hasNewestHistoryRef()} returns {@code true}.
         */
        public int getNewestHistoryRefType() {
            return newestHistoryRefType;
        }
    }

    private SiteInfoCalculator() {}

    /**
     * Walk {@code root} (depth-first, pre-order) and collect descriptive stats for the whole
     * subtree. The {@code root} node itself is included in the count.
     */
    public static Stats compute(SiteNode root) {
        if (root == null) {
            return new Stats(0, 0, 0);
        }

        int count = 0;
        long newestTime = 0;
        int newestType = 0;

        Enumeration<TreeNode> nodes = root.depthFirstEnumeration();
        while (nodes.hasMoreElements()) {
            TreeNode tn = nodes.nextElement();
            if (!(tn instanceof SiteNode)) {
                continue;
            }
            count++;
            SiteNode node = (SiteNode) tn;
            HistoryReference href = node.getHistoryReference();
            if (href == null) {
                continue;
            }
            long t = href.getTimeSentMillis();
            if (t > newestTime) {
                newestTime = t;
                newestType = href.getHistoryType();
            }
        }

        return new Stats(count, newestTime, newestType);
    }
}
