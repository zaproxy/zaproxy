/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2012 The ZAP Development Team
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SiteMap;
import org.parosproxy.paros.model.SiteNode;
import org.zaproxy.zap.utils.I18N;

/** Unit test for {@link Context}. */
@ExtendWith(MockitoExtension.class)
class ContextUnitTest {

    @Mock Session session;

    private Context context;

    @BeforeEach
    void setUp() throws Exception {
        context = new Context(session, 1);
        Constant.messages = new I18N(Locale.ENGLISH);
    }

    @Test
    void shouldNullUrlNeverBeIncluded() {
        assertThat(context.isIncluded((String) null), is(false));
    }

    @Test
    void shouldUseIndexAsDefaultName() {
        // Given
        int index = 1010;
        // When
        Context context = new Context(session, index);
        // Then
        assertThat(context.getName(), is(equalTo(String.valueOf(index))));
    }

    @Test
    void shouldNotAllowToSetNullName() {
        // Given
        String name = null;
        // When / Then
        assertThrows(IllegalContextNameException.class, () -> context.setName(name));
    }

    @Test
    void shouldNotAllowToSetAnEmptyName() {
        // Given
        String name = "";
        // When / Then
        assertThrows(IllegalContextNameException.class, () -> context.setName(name));
    }

    @Test
    void shouldSetNonEmptyName() {
        // Given
        String name = "Default Context";
        // When
        context.setName(name);
        // Then
        assertThat(context.getName(), is(equalTo(name)));
    }

    @Test
    void shouldIncludeDataDrivenNodesInContext() {
        // Given
        SiteMap siteMap = SiteMap.createTree(mock(Model.class));
        var hostNode = new SiteNode(siteMap, HistoryReference.TYPE_ZAP_USER, "https://example.com");
        var ddn =
                new SiteNode(
                        siteMap,
                        HistoryReference.TYPE_ZAP_USER,
                        SessionStructure.DATA_DRIVEN_NODE_PREFIX
                                + "id"
                                + SessionStructure.DATA_DRIVEN_NODE_POSTFIX);
        var endNode = new SiteNode(siteMap, HistoryReference.TYPE_ZAP_USER, "endpoint");
        siteMap.getRoot().add(hostNode);
        hostNode.add(ddn);
        ddn.add(endNode);
        context.setIncludeInContextRegexs(List.of("https://example.com/[^/?]+/endpoint"));
        // When / Then
        assertThat(context.isIncluded(endNode), is(true));
        assertThat(context.isInContext(endNode), is(true));
    }

    @Test
    void shouldExcludeDataDrivenNodesFromContext() {
        // Given
        SiteMap siteMap = SiteMap.createTree(mock(Model.class));
        var hostNode = new SiteNode(siteMap, HistoryReference.TYPE_ZAP_USER, "https://example.com");
        var ddn =
                new SiteNode(
                        siteMap,
                        HistoryReference.TYPE_ZAP_USER,
                        SessionStructure.DATA_DRIVEN_NODE_PREFIX
                                + "id"
                                + SessionStructure.DATA_DRIVEN_NODE_POSTFIX);
        var endNode = new SiteNode(siteMap, HistoryReference.TYPE_ZAP_USER, "endpoint");
        siteMap.getRoot().add(hostNode);
        hostNode.add(ddn);
        ddn.add(endNode);
        context.setIncludeInContextRegexs(List.of("https://example.com.*"));
        context.setExcludeFromContextRegexs(List.of("https://example.com/[^/?]+/endpoint"));
        // When / Then
        assertThat(context.isExcluded(endNode), is(true));
        assertThat(context.isInContext(endNode), is(false));
    }

    @Test
    void shouldIncludeNodesWithParamsInContext() {
        // Given
        SiteMap siteMap = SiteMap.createTree(mock(Model.class));
        var hostNode = new SiteNode(siteMap, HistoryReference.TYPE_ZAP_USER, "https://example.com");
        var endNode =
                new SiteNode(
                        siteMap,
                        HistoryReference.TYPE_ZAP_USER,
                        "endpoint(param1)(param2)",
                        "endpoint");
        siteMap.getRoot().add(hostNode);
        hostNode.add(endNode);
        context.setIncludeInContextRegexs(List.of("https://example.com/endpoint"));
        // When / Then
        assertThat(context.isIncluded(endNode), is(true));
        assertThat(context.isInContext(endNode), is(true));
    }

    @Test
    void shouldExcludeNodesWithParamsFromContext() {
        // Given
        SiteMap siteMap = SiteMap.createTree(mock(Model.class));
        var hostNode = new SiteNode(siteMap, HistoryReference.TYPE_ZAP_USER, "https://example.com");
        var endNode =
                new SiteNode(
                        siteMap,
                        HistoryReference.TYPE_ZAP_USER,
                        "endpoint(param1)(param2)",
                        "endpoint");
        siteMap.getRoot().add(hostNode);
        hostNode.add(endNode);
        context.setIncludeInContextRegexs(List.of("https://example.com.*"));
        context.setExcludeFromContextRegexs(List.of("https://example.com/endpoint"));
        // When / Then
        assertThat(context.isExcluded(endNode), is(true));
        assertThat(context.isInContext(endNode), is(false));
    }
}
