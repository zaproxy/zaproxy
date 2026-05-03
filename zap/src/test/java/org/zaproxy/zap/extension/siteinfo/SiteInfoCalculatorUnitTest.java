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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;

/** Unit test for {@link SiteInfoCalculator}. */
class SiteInfoCalculatorUnitTest {

    @Test
    void shouldReturnEmptyStatsForNullRoot() {
        SiteInfoCalculator.Stats stats = SiteInfoCalculator.compute(null);

        assertThat(stats.getNodeCount(), is(equalTo(0)));
        assertThat(stats.hasNewestHistoryRef(), is(false));
    }

    @Test
    void shouldCountSubtreeNodesIncludingRoot() {
        SiteNode root = nodeWithHistoryReference(100);
        SiteNode child1 = nodeWithHistoryReference(200);
        SiteNode child2 = nodeWithHistoryReference(300);
        wireSubtree(root, List.of(root, child1, child2));

        SiteInfoCalculator.Stats stats = SiteInfoCalculator.compute(root);

        assertThat(stats.getNodeCount(), is(equalTo(3)));
    }

    @Test
    void shouldPickNewestHistoryReferenceTimeAcrossSubtree() {
        SiteNode root = nodeWithHistoryReference(100);
        SiteNode child1 = nodeWithHistoryReference(500);
        SiteNode child2 = nodeWithHistoryReference(300);
        wireSubtree(root, List.of(root, child1, child2));

        SiteInfoCalculator.Stats stats = SiteInfoCalculator.compute(root);

        assertThat(stats.hasNewestHistoryRef(), is(true));
        assertThat(stats.getNewestHistoryRefTimeMillis(), is(equalTo(500L)));
        assertThat(stats.getNewestHistoryRefType(), is(equalTo(HistoryReference.TYPE_SPIDER)));
    }

    @Test
    void shouldHandleNodesWithoutHistoryReferences() {
        SiteNode root = nodeWithHistoryReference(100);
        SiteNode child = mock(SiteNode.class);
        given(child.getHistoryReference()).willReturn(null);
        wireSubtree(root, List.of(root, child));

        SiteInfoCalculator.Stats stats = SiteInfoCalculator.compute(root);

        assertThat(stats.getNodeCount(), is(equalTo(2)));
        assertThat(stats.hasNewestHistoryRef(), is(true));
        assertThat(stats.getNewestHistoryRefTimeMillis(), is(equalTo(100L)));
    }

    @Test
    void shouldReportNoHistoryWhenSubtreeHasNone() {
        SiteNode root = mock(SiteNode.class);
        given(root.getHistoryReference()).willReturn(null);
        wireSubtree(root, List.of(root));

        SiteInfoCalculator.Stats stats = SiteInfoCalculator.compute(root);

        assertThat(stats.getNodeCount(), is(equalTo(1)));
        assertThat(stats.hasNewestHistoryRef(), is(false));
    }

    private static SiteNode nodeWithHistoryReference(long timeSentMillis) {
        SiteNode node = mock(SiteNode.class);
        HistoryReference href = mock(HistoryReference.class);
        given(href.getTimeSentMillis()).willReturn(timeSentMillis);
        given(href.getHistoryType()).willReturn(HistoryReference.TYPE_SPIDER);
        given(node.getHistoryReference()).willReturn(href);
        return node;
    }

    /**
     * Stub {@code root.depthFirstEnumeration()} to walk the supplied nodes in order. Mockito cannot
     * generate an enumeration directly, so this helper does it by hand.
     */
    private static void wireSubtree(SiteNode root, List<? extends TreeNode> nodes) {
        given(root.depthFirstEnumeration()).willReturn(asEnumeration(nodes));
    }

    private static Enumeration<TreeNode> asEnumeration(List<? extends TreeNode> nodes) {
        Iterator<? extends TreeNode> it = nodes.iterator();
        return new Enumeration<TreeNode>() {
            @Override
            public boolean hasMoreElements() {
                return it.hasNext();
            }

            @Override
            public TreeNode nextElement() {
                return it.next();
            }
        };
    }
}
