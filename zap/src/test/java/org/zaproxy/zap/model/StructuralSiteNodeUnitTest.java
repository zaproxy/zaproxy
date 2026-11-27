/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.Test;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.SiteNode;

/** Unit test for {@link StructuralSiteNode}. */
class StructuralSiteNodeUnitTest {

    @Test
    void shouldNotAllowToConstructWithNullNode() {
        // Given
        SiteNode node = null;
        // When / Then
        assertThrows(NullPointerException.class, () -> new StructuralSiteNode(node));
    }

    @Test
    void shouldConstructWithNonNullNode() {
        // Given
        SiteNode node = mock(SiteNode.class);
        // When / Then
        assertDoesNotThrow(() -> new StructuralSiteNode(node));
    }

    @Test
    void shouldNotAllowToConstructWithNullNodeAndHistoryReference() {
        // Given
        SiteNode node = null;
        HistoryReference historyReference = mock(HistoryReference.class);
        // When / Then
        assertThrows(
                NullPointerException.class, () -> new StructuralSiteNode(node, historyReference));
    }

    @Test
    void shouldUseHistoryReferenceOfNodeByDefault() {
        // Given
        HistoryReference nodeHistoryReference = mock(HistoryReference.class);
        SiteNode node = mock(SiteNode.class);
        given(node.getHistoryReference()).willReturn(nodeHistoryReference);
        // When
        StructuralSiteNode structuralNode = new StructuralSiteNode(node);
        // Then
        assertThat(structuralNode.getHistoryReference(), is(sameInstance(nodeHistoryReference)));
    }

    @Test
    void shouldUseHistoryReferenceOfNodeIfNullHistoryReferenceGiven() {
        // Given
        HistoryReference nodeHistoryReference = mock(HistoryReference.class);
        SiteNode node = mock(SiteNode.class);
        given(node.getHistoryReference()).willReturn(nodeHistoryReference);
        // When
        StructuralSiteNode structuralNode = new StructuralSiteNode(node, null);
        // Then
        assertThat(structuralNode.getHistoryReference(), is(sameInstance(nodeHistoryReference)));
    }

    @Test
    void shouldUseGivenHistoryReferenceInsteadOfTheOneOfNode() throws Exception {
        // Given
        HistoryReference nodeHistoryReference =
                createHistoryReference(1, "GET", "https://example.com/path");
        SiteNode node = mock(SiteNode.class);
        given(node.getHistoryReference()).willReturn(nodeHistoryReference);
        HistoryReference selectedHistoryReference =
                createHistoryReference(2, "POST", "https://example.com/path?a=b");
        // When
        StructuralSiteNode structuralNode = new StructuralSiteNode(node, selectedHistoryReference);
        // Then
        assertThat(
                structuralNode.getHistoryReference(), is(sameInstance(selectedHistoryReference)));
        assertThat(structuralNode.getHistoryReference().getHistoryId(), is(equalTo(2)));
        assertThat(structuralNode.getMethod(), is(equalTo("POST")));
        assertThat(structuralNode.getURI().toString(), is(equalTo("https://example.com/path?a=b")));
        assertThat(structuralNode.getName(), is(equalTo("https://example.com/path?a=b")));
        assertThat(structuralNode.getSiteNode(), is(sameInstance(node)));
    }

    @Test
    void shouldBeSameAsNodeWrappingSameSiteNodeEvenIfDifferentHistoryReference() {
        // Given
        SiteNode node = mock(SiteNode.class);
        StructuralSiteNode structuralNode = new StructuralSiteNode(node);
        StructuralSiteNode structuralNodeWithHref =
                new StructuralSiteNode(node, mock(HistoryReference.class));
        // When / Then
        assertThat(structuralNodeWithHref.isSameAs(structuralNode), is(equalTo(true)));
        assertThat(structuralNode.isSameAs(structuralNodeWithHref), is(equalTo(true)));
    }

    private static HistoryReference createHistoryReference(int id, String method, String uri)
            throws Exception {
        HistoryReference historyReference = mock(HistoryReference.class);
        given(historyReference.getHistoryId()).willReturn(id);
        given(historyReference.getMethod()).willReturn(method);
        given(historyReference.getURI()).willReturn(new URI(uri, true));
        return historyReference;
    }
}
