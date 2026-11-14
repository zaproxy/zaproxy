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
package org.zaproxy.zap.view.popup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link PopupMenuItemSiteNodeContainer}. */
class PopupMenuItemSiteNodeContainerUnitTest {

    @BeforeAll
    static void setupAll() {
        Constant.messages = mock(I18N.class);
    }

    @AfterAll
    static void cleanup() {
        Constant.messages = null;
    }

    @Test
    void shouldPerformActionWithSiteNodeAndSelectedHistoryReference() {
        // Given
        SiteNode siteNode = mock(SiteNode.class);
        HistoryReference nodeHistoryReference = mock(HistoryReference.class);
        given(siteNode.getHistoryReference()).willReturn(nodeHistoryReference);
        HistoryReference selectedHistoryReference = mock(HistoryReference.class);
        given(selectedHistoryReference.getSiteNode()).willReturn(siteNode);
        HistoryReferenceAwarePopup popup = new HistoryReferenceAwarePopup();
        // When
        popup.performAction(selectedHistoryReference);
        // Then
        assertThat(popup.siteNodes.size(), is(1));
        assertThat(popup.siteNodes.get(0), is(sameInstance(siteNode)));
        assertThat(popup.historyReferences.get(0), is(sameInstance(selectedHistoryReference)));
    }

    @Test
    void shouldPerformActionWithSiteNodeByDefault() {
        // Given
        SiteNode siteNode = mock(SiteNode.class);
        HistoryReference selectedHistoryReference = mock(HistoryReference.class);
        given(selectedHistoryReference.getSiteNode()).willReturn(siteNode);
        SiteNodePopup popup = new SiteNodePopup();
        // When
        popup.performAction(selectedHistoryReference);
        // Then
        assertThat(popup.siteNodes.size(), is(1));
        assertThat(popup.siteNodes.get(0), is(sameInstance(siteNode)));
    }

    @Test
    void shouldNotPerformActionIfNoSiteNode() {
        // Given
        HistoryReference selectedHistoryReference = mock(HistoryReference.class);
        given(selectedHistoryReference.getSiteNode()).willReturn(null);
        given(selectedHistoryReference.getHistoryId()).willReturn(1);
        Model model = mock(Model.class);
        Session session = mock(Session.class);
        SiteMap siteTree = mock(SiteMap.class);
        given(model.getSession()).willReturn(session);
        given(session.getSiteTree()).willReturn(siteTree);
        given(siteTree.getSiteNode(1)).willReturn(null);
        Model.setSingletonForTesting(model);
        HistoryReferenceAwarePopup popup = new HistoryReferenceAwarePopup();
        // When
        popup.performAction(selectedHistoryReference);
        // Then
        assertThat(popup.siteNodes, is(empty()));
        assertThat(popup.historyReferences, is(empty()));
    }

    @SuppressWarnings("serial")
    private static class SiteNodePopup extends PopupMenuItemSiteNodeContainer {

        private static final long serialVersionUID = 1L;

        final List<SiteNode> siteNodes = new ArrayList<>();

        SiteNodePopup() {
            super("Label");
        }

        @Override
        protected void performAction(SiteNode siteNode) {
            siteNodes.add(siteNode);
        }
    }

    @SuppressWarnings("serial")
    private static class HistoryReferenceAwarePopup extends SiteNodePopup {

        private static final long serialVersionUID = 1L;

        final List<HistoryReference> historyReferences = new ArrayList<>();

        @Override
        protected void performAction(SiteNode siteNode, HistoryReference historyReference) {
            siteNodes.add(siteNode);
            historyReferences.add(historyReference);
        }
    }
}
